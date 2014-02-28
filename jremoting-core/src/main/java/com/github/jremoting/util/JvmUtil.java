package com.github.jremoting.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.github.jremoting.util.concurrent.Executors;
import com.sun.management.HotSpotDiagnosticMXBean;


@SuppressWarnings("restriction")
public class JvmUtil {
	
	
	private static final String HOTSPOT_BEAN_NAME = "com.sun.management:type=HotSpotDiagnostic";
    private static volatile HotSpotDiagnosticMXBean hotspotMBean;
    private static volatile MemoryMXBean memoryMBean;
    private static final Logger LOGGER =  LoggerFactory.getLogger(JvmUtil.class);
    private static ExecutorService dumpExecutor = Executors.newExecutor(1, 1, 10,"JRemoting-JVM-Dumper");
    private static AtomicLong dumpCount = new AtomicLong(0);
    private static long dumpDelay = 1000 * 30; 
    private static volatile boolean isHeapDumped = false;
    
    
    
    public static void main(String[] args) throws Exception {
		JvmUtil.dumpJvmInfo();
		
		System.in.read();
		
		dumpExecutor.shutdown();
	}
    
    /*
     *use single thread to dump jvm info and control dump frequency
     * 
     * allow 5 senconds dump stack once, if dump stack over 10 times will delay 30s to allow next stack dump
     * 
     * dump one heap only
     * */
    public static void dumpJvmInfo() {
   
    	try {
			doJvmDump();
		} catch (Throwable th) {
			LOGGER.error(th.getMessage(), th);
		}
    
    }

	private static void doJvmDump() {
		long dumpTimes =  dumpCount.incrementAndGet();
    	if(dumpTimes > 10) {
    		//dump over 10 times will delay 30 seconds to allow next dump
    		dumpExecutor.execute(new Runnable() {
				
				@Override
				public void run() {
					try {
						Thread.sleep(dumpDelay);
					} catch (InterruptedException e) {
						
					}
					finally {
						dumpCount.set(0);
					}
				}
			});
    		return;
    	}
    	
    	dumpExecutor.execute(new Runnable() {
			@Override
			public void run() {
				
				String jremotingDumpDir  =  System.getProperty("jremoting.dump.dir");
				if(jremotingDumpDir == null) {
					jremotingDumpDir = System.getProperty("user.dir");
			    	if(jremotingDumpDir == null) {
			    		return;
			    	}
				}
			
		    	
		    	final String logDir = jremotingDumpDir + File.separator + "dump";
		    	 File dir = new File(logDir);
		    	 if(!dir.exists()) {
					 dir.mkdirs();
				 }
		    	 
				FileOutputStream jstackStream = null;
				
				try {
					String jstackFileName = "jremoting_jstack.log." + DateUtil.getGenerateIdTime();
					
					jstackStream = new FileOutputStream(new File(logDir, jstackFileName));
					jstack(jstackStream);
					double usedHeap = memoryUsed(jstackStream);
					if (usedHeap > 0.9 && !isHeapDumped) {
						isHeapDumped = true;
						jmap(logDir + File.separator + "jremoting_jmap.bin", false);
					}
				} catch (FileNotFoundException e) {
					LOGGER.error("Dump JVM cache Error!", e);
				} catch (Throwable t) {
					LOGGER.error("Dump JVM cache Error!", t);
				} finally {
					if (jstackStream != null) {
						try {
							jstackStream.close();
						} catch (IOException e) {
						}
					}
				}
				
				//5 senconds allow to dump once
				try {
					Thread.sleep(5000);
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		});
	}
    
    public static void jmap(String fileName, boolean live) throws Exception {
        try {
            initHotspotMBean();
            File f = new File(fileName);
            if (f.exists()) {
                f.delete();
            }
            hotspotMBean.dumpHeap(fileName, live);
        } catch (Exception e) {
            throw e;
        }
    }
    
	public static void jstack(OutputStream output) throws Exception {
		try {
			Map<Thread, StackTraceElement[]> stackTraces = Thread
					.getAllStackTraces();

			for (Thread thread : stackTraces.keySet()) {
				output.write(("Thread Name :[" + thread.getName() + "], ID["+ thread.getId() +"]\n")
						.getBytes());
				StackTraceElement[] elements = stackTraces.get(thread);
				if (elements != null && elements.length > 0) {
					for (StackTraceElement el : elements) {
						output.write("\t".getBytes());
						output.write(el.toString().getBytes());
						output.write("\n".getBytes());
					}

				}
				output.write("\n".getBytes());
			}
		} catch (Exception e) {
			throw e;
		}
	}
	
    private static MemoryMXBean getMemoryMBean() throws Exception {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<MemoryMXBean>() {
                public MemoryMXBean run() throws Exception {
                    return ManagementFactory.getMemoryMXBean();
                }
            });
        } catch (Exception exp) {
            throw exp;
        }
    }

    private static void initHotspotMBean() throws Exception {
        if (hotspotMBean == null) {
            synchronized (JvmUtil.class) {
                if (hotspotMBean == null) {
                    hotspotMBean = getHotspotMBean();
                }
            }
        }
    }

    private static void initMemoryMBean() throws Exception {
        if (memoryMBean == null) {
            synchronized (JvmUtil.class) {
                if (memoryMBean == null) {
                    memoryMBean = getMemoryMBean();
                }
            }
        }
    }
    
    public static double memoryUsed(OutputStream output) throws Exception {
        try {
            initMemoryMBean();
            output.write("**********************************Memory Used**********************************\n".getBytes());
            String heapMemoryUsed = memoryMBean.getHeapMemoryUsage().toString() + "\n";
            output.write(("Heap Memory Used: " + heapMemoryUsed).getBytes());
            String nonHeapMemoryUsed = memoryMBean.getNonHeapMemoryUsage().toString() + "\n";
            output.write(("NonHeap Memory Used: " + nonHeapMemoryUsed).getBytes());

            return memoryMBean.getHeapMemoryUsage().getUsed() / memoryMBean.getHeapMemoryUsage().getMax();
        } catch (Exception e) {
            throw e;
        }
    }
    
    private static HotSpotDiagnosticMXBean getHotspotMBean() throws Exception {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<HotSpotDiagnosticMXBean>() {
                public HotSpotDiagnosticMXBean run() throws Exception {
                    MBeanServer server = ManagementFactory.getPlatformMBeanServer();
                    Set<ObjectName> s = server.queryNames(new ObjectName(HOTSPOT_BEAN_NAME), null);
                    Iterator<ObjectName> itr = s.iterator();
                    if (itr.hasNext()) {
                        ObjectName name = itr.next();
                        HotSpotDiagnosticMXBean bean = ManagementFactory.newPlatformMXBeanProxy(server,
                                name.toString(), HotSpotDiagnosticMXBean.class);
                        return bean;
                    } else {
                        return null;
                    }
                }
            });
        } catch (Exception exp) {
            throw exp;
        }
    }
}
