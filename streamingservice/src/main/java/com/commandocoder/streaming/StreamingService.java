package com.commandocoder.streaming;

import java.net.URI;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

import com.spoledge.aacdecoder.IcyURLStreamHandler;
import com.spoledge.aacdecoder.MultiPlayer;
import com.spoledge.aacdecoder.PlayerCallback;

import android.app.Service;
import android.content.Intent;
import android.media.AudioTrack;
import android.os.Binder;
import android.os.IBinder;

/**
 * Background service for streaming with aacdecoder
 * (c) Commando Coder Ltd. 2014
 * @author Tero Katajainen
 */
public class StreamingService extends Service implements PlayerCallback {

	public class StreamingBinder extends Binder {
		public StreamingService getService() {
			return StreamingService.this;
		}
	}

	private final StreamingBinder serviceBinder = new StreamingBinder();
    static private StreamingListener listener = null;
    static private MultiPlayer player = null;
	static boolean playerStarted = false;
    static private float volume = 1;
    static private boolean streamingStarted;

    static {
        URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory() {
            @Override
            public URLStreamHandler createURLStreamHandler(String protocol) {
                if("icy".equals(protocol)) return new IcyURLStreamHandler();
                return null;
            }
        });
    }

    public StreamingService() {

    }

	@Override
	public IBinder onBind(Intent intent) {
		return serviceBinder ;
	}

	public void setListener(StreamingListener listener) {
		this.listener  = listener;
	}
	
	public synchronized void startStreaming(final URI streamURI) throws StreamingException {
        final MultiPlayer currentPlayer = player;
        if(player != null) throw new StreamingException("Streaming not stopped yet");
		if(playerStarted) throw new StreamingException("Streaming already started");
		playerStarted = true;
		streamingStarted = false;
		
		new Thread(new Runnable() {			
			@Override
			public void run() {
                listener.buffering(0);
				player = new MultiPlayer(StreamingService.this, 1000, 3000);
				player.setVolume(volume, volume);
				player.playAsync(streamURI.toString());
			}
		},"streamingStartThread").start();
	}

	public synchronized void stopStreaming(boolean emitStopping) {
		final MultiPlayer currentPlayer = player;
		if(currentPlayer != null) {
            if(emitStopping) listener.streamingStopping();
			currentPlayer.setVolume(0, 0);
			if(streamingStarted) {
				new Thread(new Runnable() {			
					@Override
					public void run() {
						currentPlayer.stop();
					}
				},"streamingStopThread").start();				
			}
			else {
				new Thread(new Runnable() {			
					@Override
					public void run() {
						currentPlayer.stop();
						playerStarted = false;
					}
				},"streamingStopThread").start();				
			}
		}
        else {
            listener.streamingStopped();
        }
		player = null;
	}
	
	public void setVolume(float volume) {
		if(player != null)
			player.setVolume(volume, volume);
		this.volume  = volume;
	}

	@Override
	public boolean playerStarted() {
		return player != null;
	}

	@Override
	public void playerPCMFeedBuffer(boolean isPlaying, int audioBufferSizeMs,
			int audioBufferCapacityMs) 
	{
		if(player != null && isPlaying && playerStarted && !streamingStarted) {
			streamingStarted = true;
			new Thread(new Runnable() {			
				@Override
				public void run() {
					listener.streamingStarted();			
				}
			},"streamingStartedThread").start();
		}
	}

	@Override
	public void playerStopped(int perf) {
        playerStarted = false;
        stopStreaming(true);
		listener.streamingStopped();
	}

	@Override
	public void playerException(Throwable t) {
		playerStarted = false;
        stopStreaming(false);
		if(t instanceof Exception)
			listener.streamingException((Exception)t);
		else 
			listener.streamingException(new StreamingException(t));
	}

	@Override
	public void playerMetadata(String key, final String value) {
		if ("StreamTitle".equals( key ) || "icy-name".equals( key ) || "icy-description".equals( key )) {
			new Thread(new Runnable() {			
				@Override
				public void run() {
					listener.streamingTitle(value);
				}
			},"titleUpdateThread").start();
        }
	}

	@Override
	public void playerAudioTrackCreated(AudioTrack audioTrack) {
	}

	public boolean isPlayerStarted() {
		return playerStarted;
	}
}
