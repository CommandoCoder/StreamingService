/**
 * Showcase client for streaming library
 * (c) Commando Coder Ltd. 2014
 * @author Tero Katajainen
 */
package com.commandocoder.test.streaming;

import java.net.URI;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.commandocoder.streaming.StreamingListener;
import com.commandocoder.streaming.StreamingService;
import com.commandocoder.test.streaming.R;

public class PlayerActivity extends Activity implements View.OnClickListener, StreamingListener {

	private Button playButton;
	private Button stopButton;
	private TextView statusText;

	private Handler uiHandler;

	private StreamingService streamingService = null;
	private ServiceConnection streamServiceConnection = null;
	private boolean streaming;
    private String streamTitle = null;

	private void start() {

		if(streamingService.isPlayerStarted()) return;
		statusText.setText("Buffer");

		try {
			streamingService.startStreaming(new URI("http://2483.live.streamtheworld.com:80/KFTZFMAACCMP3"));			
			streamingService.setVolume(0);
			playButton.setEnabled(false);
			stopButton.setEnabled(true);
		} catch (Exception e) {
			statusText.setText("Exception:"+e.getMessage());
		}
	}


	private void stop() {
		stopButton.setEnabled(false);
		streamingService.setVolume(0);
		streamingService.stopStreaming(false);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_player);

		playButton = (Button) findViewById( R.id.playButton );
		stopButton = (Button) findViewById( R.id.stopButton );
		stopButton.setEnabled(false);

		statusText = (TextView) findViewById( R.id.statusText );		

		uiHandler = new Handler();

		playButton.setOnClickListener(this);
		stopButton.setOnClickListener(this);

		streamServiceConnection = new ServiceConnection(){

			@Override
			public void onServiceConnected(ComponentName paramComponentName,
					IBinder paramIBinder) 
			{
				streamingService  = ((StreamingService.StreamingBinder)paramIBinder).getService();			
				streamingService.setListener(PlayerActivity.this);
			}

			@Override
			public void onServiceDisconnected(ComponentName paramComponentName) {
				streamingService = null;
			}

		};
		bindService(new Intent(this,StreamingService.class), streamServiceConnection, Context.BIND_AUTO_CREATE);						
	}

	@Override
	protected void onDestroy() {    
		if(streamingService != null && streaming) {
			streamingService.stopStreaming(false);
			streamingService.setVolume(0);
		}
		if(streamServiceConnection != null) {
			unbindService(streamServiceConnection);
			streamServiceConnection = null;
		}
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		try {
			int id = v.getId();
            if (id == R.id.playButton) {
                start();
            }
            else if (id == R.id.stopButton) {
                stop();
            }
    	}
		catch (Exception e) {
			statusText.setText("Exception:"+e.getMessage());
		}
	}



    @Override
	public void streamingStarted() {
		streaming = true;		
		streamingService.setVolume(1);
        if(streamTitle == null) {
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    statusText.setText("Started");
                }
            });
        }
	}

    @Override
    public void buffering(int percentage) {
    }

    @Override
    public void streamingStopping() {

    }

    @Override
	public void streamingStopped() {
		streaming = false;
        streamTitle = null;
		uiHandler.post(new Runnable() {			
			@Override
			public void run() {
				playButton.setEnabled(true);
				statusText.setText("Stopped");
			}
		});
	}


	@Override
	public void streamingException(final Exception e) {
		streaming = false;
		uiHandler.post(new Runnable() {			
			@Override
			public void run() {
				statusText.setText("Exception:"+e.getMessage());
			}
		});
	}


	@Override
	public void streamingTitle(final String title) {
        streamTitle = title;
		uiHandler.post(new Runnable() {			
			@Override
			public void run() {
				statusText.setText("Title:"+title);
			}
		});
	}
}
