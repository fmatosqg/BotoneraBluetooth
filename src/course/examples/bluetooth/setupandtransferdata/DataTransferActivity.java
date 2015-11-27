package course.examples.bluetooth.setupandtransferdata;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class DataTransferActivity extends Activity {
	private static final String TAG = "DataTransferActivity";
	private static final int REQUEST_ENABLE_BT = 0;
	private static final int SELECT_SERVER = 1;
	public static final int DATA_RECEIVED = 3;
	public static final int SOCKET_CONNECTED = 4;

	public static final UUID APP_UUID = UUID
			.fromString("aeb9f938-a1a3-4947-ace2-9ebd0c67adf1");
	private Button serverButton, clientButton;
	private Button iiiihButton;

	private TextView tv = null;
	
	private BluetoothAdapter mBluetoothAdapter = null;
	private ConnectionThread mBluetoothConnection = null;
	private String data;
	private boolean mServerMode;

	private SoundPool mSoundPool;
	private AudioManager mAudioManager;
	private int mSoundId;
	private boolean mCanPlayAudio;

	private int mVolume = 6;
	private final int mVolumeMax = 10;
	private final int mVolumeMin = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Log.i(TAG, "Bluetooth not supported");
			finish();
		}

		setContentView(R.layout.main);
		tv = (TextView) findViewById(R.id.text_window);
		serverButton = (Button) findViewById(R.id.server_button);
		serverButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startAsServer();
				mServerMode = true;
			}
		});

		clientButton = (Button) findViewById(R.id.client_button);
		clientButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				selectServer();
			}
		});

		iiiihButton = (Button) findViewById(R.id.iiiih_button);
		iiiihButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mBluetoothConnection.write("iiiiiih".getBytes());
				play();
			}
		});

		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBluetoothIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BT);
		} else {
			setButtonsEnabled(true);
		}

		mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

//		loadSoundPool();

		// Request audio focus
		int result = mAudioManager.requestAudioFocus(afChangeListener,
				AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

		// Set to true if app has audio foucs
		mCanPlayAudio = AudioManager.AUDIOFOCUS_REQUEST_GRANTED == result;

	}

	private void loadSoundPool() {
		// Create a SoundPool
		mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);

		// Load bubble popping sound into the SoundPool
		mSoundId = mSoundPool.load(this, R.raw.slow_whoop_bubble_pop, 1);

		// Set an OnLoadCompleteListener on the SoundPool
		mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {

			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId,
									   int status) {

				// If sound loading was successful enable the play Button
				if (0 == status) {
					iiiihButton.setEnabled(true);
					Log.d(TAG, "Sound pool is cool 1");
				} else {
					Log.i(TAG, "Unable to load sound");
					Log.d(TAG, "Sound pool is broken 1");
					finish();
				}
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
			setButtonsEnabled(true);
		} else if (requestCode == SELECT_SERVER
				&& resultCode == RESULT_OK) {
			BluetoothDevice device = data
					.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			connectToBluetoothServer(device.getAddress());
		}
	}

	private void startAsServer() {
		setButtonsEnabled(false);
		new AcceptThread(mHandler).start();
	}

	private void selectServer() {
		setButtonsEnabled(false);
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
				.getBondedDevices();
		ArrayList<String> pairedDeviceStrings = new ArrayList<String>();
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				pairedDeviceStrings.add(device.getName() + "\n"
						+ device.getAddress());
			}
		}
		Intent showDevicesIntent = new Intent(this, ShowDevices.class);
		showDevicesIntent.putStringArrayListExtra("devices", pairedDeviceStrings);
		startActivityForResult(showDevicesIntent, SELECT_SERVER);
	}

	private void connectToBluetoothServer(String id) {
		tv.setText("Connecting to Server...");
		new ConnectThread(id, mHandler).start();
	}

	// Get ready to play sound effects
	@Override
	protected void onResume() {
		super.onResume();

		mAudioManager.setSpeakerphoneOn(true);
		mAudioManager.loadSoundEffects();

		loadSoundPool();
	}

	// Release resources & clean up
	@Override
	protected void onPause() {

		if (null != mSoundPool) {
			mSoundPool.unload(mSoundId);
			mSoundPool.release();
			mSoundPool = null;

			Log.d(TAG,"Sound pool release");
		}

		mAudioManager.setSpeakerphoneOn(false);
		mAudioManager.unloadSoundEffects();

		Log.d(TAG, "Sound pool pause unloaded");
		super.onPause();
	}


	// Listen for Audio focus changes
	AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
		@Override
		public void onAudioFocusChange(int focusChange) {

			if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
				mAudioManager.abandonAudioFocus(afChangeListener);
				mCanPlayAudio = false;
				Log.d(TAG,"Audio focus loss");
			} else {
				Log.d(TAG,"Audio focus != loss " + focusChange);
			}

		}
	};


	public Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (mSoundPool == null ) {
				Log.d(TAG,"Sound pool is dead 1");
			}
			switch (msg.what) {
			case SOCKET_CONNECTED: {
				mBluetoothConnection = (ConnectionThread) msg.obj;
				if (!mServerMode)
					mBluetoothConnection.write("this is a message".getBytes());
				break;
			}
			case DATA_RECEIVED: {
				data = (String) msg.obj;

				Log.d(TAG, "Receive data");
				play();
				if (mServerMode) {
					tv.setText(data);
//					mBluetoothConnection.write(data.getBytes());
				}
			}
			default:
				break;
			}
		}
	};

	private void play() {
//		AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
//		mAudioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);

		Thread t = new Thread(){
			@Override
			public void run() {
				if (mCanPlayAudio) {
					if (mSoundPool != null ) {
						mSoundPool.play(mSoundId, (float) mVolume / mVolumeMax,
								(float) mVolume / mVolumeMax, 1, 0, 1.0f);
					} else {
						Log.w(TAG,"NULLLLLLL");
					}
				}
			}
		};

		t.start();


	}

	private void setButtonsEnabled(boolean state) {
		serverButton.setEnabled(state);
		clientButton.setEnabled(state);
	}
	
}