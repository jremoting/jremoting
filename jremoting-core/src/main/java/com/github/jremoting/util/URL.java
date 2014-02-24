package com.github.jremoting.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public final class URL   {

    private final String protocol;

	private final String host;

	private final int port;

	private final String path;

    private final Map<String, String> parameters;

    protected URL() {
        this.protocol = null;
        this.host = null;
        this.port = 0;
        this.path = null;
        this.parameters = null;
    }
    
    public URL(String protocol, String address,String path, Map<String, String> parameters) {
    	String host;
    	int port;
    	if(address.indexOf(":") > 0) {
    		String[] pair = address.split(":");
    		host = pair[0];
    		port = Integer.parseInt(pair[1]);
    	}
    	else {
			host =address;
			port = 0;
		}
    	
    	this.protocol = protocol;
	
		this.host = host;
		this.port = (port < 0 ? 0 : port);
	    this.path = path;
	
		if (parameters == null) {
		    parameters = new HashMap<String, String>();
		} else {
		    parameters = new HashMap<String, String>(parameters);
		}
		this.parameters = Collections.unmodifiableMap(parameters);
    }
    
	public URL(String protocol, String host, int port) {
	    this(protocol, null, null, host, port, null, (Map<String, String>) null);
	}
	

	public URL(String protocol, String host, int port, Map<String, String> parameters) {
        this(protocol, null, null, host, port, null, parameters);
    }
	
	public URL(String protocol, String host, int port, String path) {
	    this(protocol, null, null, host, port, path, (Map<String, String>) null);
	}

	public URL(String protocol, String host, int port, String path, Map<String, String> parameters) {
		this(protocol, null, null, host, port, path, parameters);
	}
	
	public URL(String protocol, String username, String password, String host, int port, String path) {
        this(protocol, username, password, host, port, path, (Map<String, String>) null);
    }

	public URL(String protocol, String username, String password, String host, int port, String path, Map<String, String> parameters) {
		if ((username == null || username.length() == 0) 
				&& password != null && password.length() > 0) {
			throw new IllegalArgumentException("Invalid url, password without username!");
		}
		this.protocol = protocol;
		this.host = host;
		this.port = (port < 0 ? 0 : port);
		this.path = path;
		// trim the beginning "/"
		while(path != null && path.startsWith("/")) {
		    path = path.substring(1);
		}
		if (parameters == null) {
		    parameters = new HashMap<String, String>();
		} else {
		    parameters = new HashMap<String, String>(parameters);
		}
		this.parameters = Collections.unmodifiableMap(parameters);
	}

    /**
     * Parse url string
     * 
     * @param url URL string
     * @return URL instance
     * @see URL
     */
    public static URL valueOf(String url) {
        if (url == null || (url = url.trim()).length() == 0) {
            throw new IllegalArgumentException("url == null");
        }
        String protocol = null;
        String username = null;
        String password = null;
        String host = null;
        int port = 0;
        String path = null;
        Map<String, String> parameters = null;
        int i = url.indexOf("?"); // seperator between body and parameters 
        if (i >= 0) {
            String[] parts = url.substring(i + 1).split("\\&");
            parameters = new HashMap<String, String>();
            for (String part : parts) {
                part = part.trim();
                if (part.length() > 0) {
                    int j = part.indexOf('=');
                    if (j >= 0) {
                        parameters.put(part.substring(0, j), part.substring(j + 1));
                    } else {
                        parameters.put(part, part);
                    }
                }
            }
            url = url.substring(0, i);
        }
        i = url.indexOf("://");
        if (i >= 0) {
            if(i == 0) throw new IllegalStateException("url missing protocol: \"" + url + "\"");
            protocol = url.substring(0, i);
            url = url.substring(i + 3);
        }
        else {
            // case: file:/path/to/file.txt
            i = url.indexOf(":/");
            if(i>=0) {
                if(i == 0) throw new IllegalStateException("url missing protocol: \"" + url + "\"");
                protocol = url.substring(0, i);
                url = url.substring(i + 1);
            }
        }
        
        i = url.indexOf("/");
        if (i >= 0) {
            path = url.substring(i + 1);
            url = url.substring(0, i);
        }
        i = url.indexOf("@");
        if (i >= 0) {
            username = url.substring(0, i);
            int j = username.indexOf(":");
            if (j >= 0) {
                password = username.substring(j + 1);
                username = username.substring(0, j);
            }
            url = url.substring(i + 1);
        }
        i = url.indexOf(":");
        if (i >= 0 && i < url.length() - 1) {
            port = Integer.parseInt(url.substring(i + 1));
            url = url.substring(0, i);
        }
        if(url.length() > 0) host = url;
        return new URL(protocol, username, password, host, port, path, parameters);
    }

	public String getProtocol() {
		return protocol;
	}

	public String getHost() {
		return host;
	}
	
	public int getPort() {
		return port;
	}

    public int getPort(int defaultPort) {
        return port <= 0 ? defaultPort : port;
    }

	public String getAddress() {
	    return port <= 0 ? host : host + ":" + port;
	}
	
	public String getPath() {
		return path;
	}
	
	public String getAbsolutePath() {
        if (path != null && !path.startsWith("/")) {
            return "/" + path;
        }
        return path;
	}
	
    public Map<String, String> getParameters() {
        return parameters;
    }

    public String getParameter(String key) {
        return parameters.get(key);
    }

    public String getParameter(String key, String defaultValue) {
        String value = getParameter(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return value;
    }

    public boolean hasParameter(String key) {
        String value = getParameter(key);
        return value != null && value.length() > 0;
    }
    
	public String toString() {
		StringBuilder buf = new StringBuilder();
		if (protocol != null && protocol.length() > 0) {
			buf.append(protocol);
			buf.append("://");
		}
		if(host != null && host.length() > 0) {
    		buf.append(host);
    		if (port > 0) {
    			buf.append(":");
    			buf.append(port);
    		}
		}
		if (path != null && path.length() > 0) {
			buf.append("/");
			buf.append(path);
		}
		
		buf.append('?');
		
		for (String parameter: parameters.keySet()) {
			buf.append(parameter).append('=').append(parameters.get(parameter)).append('&');
		}
		
		return buf.substring(0, buf.length() -1);
    }
}