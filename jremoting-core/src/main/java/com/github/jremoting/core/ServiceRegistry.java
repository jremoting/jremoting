package com.github.jremoting.core;

import java.util.List;

public interface ServiceRegistry {
	List<ServiceParticipantInfo> getProviders(String serviceName);
	void registerParticipant(ServiceParticipantInfo participantInfo);
	void start();
	void close();
}
