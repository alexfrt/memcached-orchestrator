package br.uece.memcached.orchestrator.endpoint;

public interface MessageHandler {
	
	void handle(String message);

}
