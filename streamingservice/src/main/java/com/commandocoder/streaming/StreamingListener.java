package com.commandocoder.streaming;

/**
 * Interface for actitivites listening to streaming events
 * (c) Commando Coder Ltd. 2014
 * @author Tero Katajainen
 */
public interface StreamingListener {

    void buffering(int percentage);

	void streamingStarted();

    void streamingStopping();

	void streamingStopped();

	void streamingException(Exception e);

	void streamingTitle(String title);

}
