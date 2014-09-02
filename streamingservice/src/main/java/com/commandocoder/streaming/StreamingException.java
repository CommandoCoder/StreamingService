package com.commandocoder.streaming;

/**
 * Exception for streaming error events
 * (c) Commando Coder Ltd. 2014
 * @author Tero Katajainen
 */
public class StreamingException extends Exception {

	public StreamingException(String string) {
		super(string);
	}

	public StreamingException(Throwable t) {
		super(t);
	}

}
