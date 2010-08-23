package com.piratemedia.lockscreen;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class mainActivity extends Activity {
	
	public static LinearLayout InfoBox;
	public boolean playback = false;
	public String nextAlarm = null;
	private ServiceConnection conn = null;
	
    public static final Uri GMAIL_CONTENT_URI = Uri.parse("content://gmail-ls/labels/");
    public static final String GMAIL_ID = "_id";
    public static final String CANONICAL_NAME = "canonicalName";
    public static final String NAME = "name";
    public static final String NUM_CONVERSATIONS = "numConversations";
    public static final String NUM_UNREAD_CONVERSATIONS = "numUnreadConversations";

    public static final Uri CALL_CONTENT_URI = Uri.parse("content://call_log");
    public static final Uri CALL_LOG_CONTENT_URI  = Uri.withAppendedPath(CALL_CONTENT_URI, "calls");
    public static final String CALLER_ID = "_id";

    public static final Uri SMS_CONTENT_URI = Uri.parse("content://sms");
    public static final Uri SMS_INBOX_CONTENT_URI = Uri.withAppendedPath(SMS_CONTENT_URI, "inbox");
    public static final String SMS_ID = "_id";
    
	Handler mHandler = new Handler();

	//Slider Initialisation Stuff
	
	private int Displaywidth;
	private HorizontalScrollView slider;
	private LinearLayout LeftAction;
	private LinearLayout RightAction;
	private LinearLayout mainFrame;
	private int unlock_count;
	private boolean left = false;
	private boolean right = false;
	private Timer timer = new Timer();
	private Toast msg;
	private boolean unlock = true;
	
	//End Slider Init
    
    private TextView mSmsCount;
    private TextView mMissedCount;
    private TextView mGmailCount;

    private int mGetSmsCount = 0;
    private int mGetMissedCount = 0;
    private int mGetGmailCount = 0;
    
	private String prevString;
 	private String toggleString;
 	private String nextString;
 	
 	private boolean state = true;
	
 	private String mLauncherPackage;
 	private String mLauncherActivity;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mLauncherPackage=utils.getStringPref(this, LockscreenSettings.KEY_HOME_APP_PACKAGE, "");
		mLauncherActivity=utils.getStringPref(this, LockscreenSettings.KEY_HOME_APP_ACTIVITY, "");
		//First check if we are locking or not.
		Intent intent=getIntent();
		if(intent.getAction().equals("android.intent.action.MAIN") && intent.getCategories().contains("android.intent.category.HOME")){
			//Fire intent to the stock home
			if(mLauncherPackage!="" && mLauncherActivity!=""){
				Intent launcher = new Intent();
				launcher.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		        launcher.setComponent(new ComponentName(mLauncherPackage,mLauncherActivity));
		        //Check if the launcher was already running on top to fire the first intent
		        ActivityManager actvityManager = (ActivityManager)this.getSystemService( ACTIVITY_SERVICE );
		        List<RunningTaskInfo> procInfos = actvityManager.getRunningTasks(2);
		        //Maybe remove the loop and check just the 2nd procInfo?
		        for(int i = 0; i < procInfos.size(); i++)
		        {
		        	if(procInfos.get(i).baseActivity.getPackageName().equals(mLauncherPackage) && procInfos.get(i).baseActivity.getClassName().equals(mLauncherActivity)) {
		    	        startActivity(launcher);
		    	        break;
		        	}
		        }	        
		        launcher.setAction("android.intent.action.MAIN");
		        launcher.addCategory("android.intent.category.HOME");
		        startActivity(launcher);
			}else{
				Intent chooser=new Intent(this, HomeChooserActivity.class);
				chooser.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
				chooser.putExtra("loadOnClick", true);
				startActivity(chooser);
			}
	        finish();
		}else{
			setContentView(R.layout.slide_base);
			
			//Start Slider Stuff
			
	        Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
	        Displaywidth = display.getWidth();
			
			mainFrame = (LinearLayout) findViewById(R.id.base);
			LeftAction = (LinearLayout) findViewById(R.id.left_action);
			RightAction = (LinearLayout) findViewById(R.id.right_action);
			LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(Displaywidth, LinearLayout.LayoutParams.FILL_PARENT);
			mainFrame.setLayoutParams(lp);
			
			slider = (HorizontalScrollView) findViewById(R.id.mainSlide);
			
			slider.setOnTouchListener(new OnTouchListener() {
	            public boolean onTouch(View view, MotionEvent motionevent) {
	                if (motionevent.getAction() == MotionEvent.ACTION_UP || motionevent.getAction() == MotionEvent.ACTION_CANCEL) {
	                	stopAllCounts();
	                	Thread t = new Thread() {
	                        public void run() {
	                            mHandler.post(mScroll);
	                        }
	                    };
	                    t.start();
	                	return true;
	                } else if (motionevent.getAction() == MotionEvent.ACTION_MOVE) {
	                	int pos = slider.getScrollX();
	                	int end = LeftAction.getWidth() + RightAction.getWidth();
	                	if (pos == 0) {
	                    	if (!left) {
	                			left = true;
	                			unlock_count = utils.getIntPref(getBaseContext(), LockscreenSettings.COUNT_KEY, 3);
	                			startCount(false);
	                    	}
	                	} else if (pos == end) {
	                		if (!right) {
	                			right = true;
	                			unlock_count = utils.getIntPref(getBaseContext(), LockscreenSettings.COUNT_KEY, 3);
	                			startCount(true);
	                		}
	                	} else {
	                		left = false;
	                		right = false;
	                		stopAllCounts();
	                	}
	                }
					return false;
	            };
			});
			
			//End Slider Stuff
			
			mSmsCount = (TextView) findViewById(R.id.smscount);
		    mMissedCount = (TextView) findViewById(R.id.missedcount);
		    mGmailCount = (TextView) findViewById(R.id.gmailcount);
			
		    switch(getPlayer()) {
		    	case 1: {
		    		Intent i = new Intent();
		    		i.setClassName("com.android.music", "com.android.music.MediaPlaybackService");
		    		conn = new MediaPlayerServiceConnectionStock();
		    		this.bindService(i, conn, BIND_AUTO_CREATE);		    		
		    		break;
		    	}
		    	case 2: {
		    		Intent i = new Intent();
		    		i.setClassName("com.htc.music", "com.htc.music.MediaPlaybackService");
		    		conn = new MediaPlayerServiceConnectionHTC();
		    		this.bindService(i, conn, BIND_AUTO_CREATE);
		    		break;
		    	}
		    	case 3: {
		    		Intent i = new Intent();
		    		i.setClassName("com.piratemedia.musicmod", "com.piratemedia.musicmod.MediaPlaybackService");
		    		conn = new MediaPlayerServiceConnectionPirate();
		    		this.bindService(i, conn, BIND_AUTO_CREATE);
		    		break;
		    	}
		    }
		    
			setButtonIntents();
			setPlayButton();
			showHideControlsStart(false);
			
			ImageButton toggle = (ImageButton) findViewById(R.id.musicControlsToggle);
			
		    toggle.setOnClickListener(new View.OnClickListener() {
		        public void onClick(View v) {
		        	toggleMusic();
		        }
		    });
		}		    
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
        Thread start = new Thread() {
            public void run() {
                mHandler.post(mScroll);
            }
        };
        start.start();
		
		setFullscreen();
		setLandscape();
		getPlayer();
		setCustomBackground();
		wifiMod();
		usbMsMode();
        
	    mGetSmsCount = getUnreadSmsCount(getBaseContext());
		mGetMissedCount = getMissedCallCount(getBaseContext());
		mGetGmailCount = getGmailUnreadCount(getBaseContext());
		
	    setSmsCountText();
	    setMissedCountText();
	    setGmailCountText();
	    
	    getNextAlarm();
	    getDate();
	    updateNetworkInfo();
	    muteMode(true);
		
	}
	
    @Override
    public void onStart() {
		Log.d("LOCKSCREEN","Displaying lock screen, ONSTART");

        super.onStart();
        
        IntentFilter f = new IntentFilter();
        f.addAction(updateService.MUSIC_CHANGED);
        f.addAction(updateService.MUSIC_STOPPED);
        f.addAction(updateService.SMS_CHANGED);
        f.addAction(updateService.PHONE_CHANGED);
        f.addAction(updateService.MUTE_CHANGED);
        f.addAction(updateService.WIFI_CHANGED);
        f.addAction(Intent.ACTION_BATTERY_CHANGED);
        f.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        f.addAction(Intent.ACTION_DATE_CHANGED);
        registerReceiver(mStatusListener, new IntentFilter(f));
    }
    
    public void onBackPressed () {
    	//slide to unlock is now working, so back can now do nothing
    	//unlockScreen();
    }
    
    private BroadcastReceiver mStatusListener = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(updateService.MUSIC_CHANGED)) {
                
            	String aArtist = intent.getStringExtra("artist");
            	String aAlbum = intent.getStringExtra("album");
            	String aTrack = intent.getStringExtra("track");
            	
            	long aTrackID = intent.getLongExtra("trackID", -1);
            	long aAlbumID = intent.getLongExtra("albumID", -1);

            	if (utils.getCheckBoxPref(getBaseContext(), LockscreenSettings.KEY_SHOW_ART, true)) {
            	updateArt(aAlbumID, aTrackID);
            	}
                updateInfo(aArtist, aAlbum, aTrack);
                playback = true;
                setPlayButton();
            	if (utils.getCheckBoxPref(getBaseContext(), LockscreenSettings.KEY_SHOW_ART, true)) {
                showHideArt();
            	}
                
            } else if (action.equals(updateService.MUSIC_STOPPED)) {
            	
            	playback = false;
            	setPlayButton();
            	if (utils.getCheckBoxPref(getBaseContext(), LockscreenSettings.KEY_SHOW_ART, true)) {
            	showHideArt();
            	}

            } else if (action.equals(updateService.SMS_CHANGED)) {
            	
            	new Thread(new Runnable() {
                    @Override
                    public void run() {
                            try {
                                Thread.sleep(1000);
                                mHandler.post(new Runnable() {

                                    @Override
                                    public void run() {
                                		mGetSmsCount = getUnreadSmsCount(getBaseContext());
                                		setSmsCountText();
                                    }
                                });
                            } catch (Exception e) {
                            }
                    }
                }).start();
            	
        	    
            } else if (action.equals(updateService.PHONE_CHANGED)) {
        	
            	new Thread(new Runnable() {
                    @Override
                    public void run() {
                            try {
                                Thread.sleep(1000);
                                mHandler.post(new Runnable() {

                                    @Override
                                    public void run() {
                                		mGetMissedCount = getMissedCallCount(getBaseContext());
                                		setMissedCountText();
                                    }
                                });
                            } catch (Exception e) {
                            }
                    }
                }).start();
            	
            } else if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
            	
            	int plugType = intent.getIntExtra("plugged", 0);
            	int batLevel = intent.getIntExtra("level", 0);
            	String levelString = String.valueOf(batLevel);
            	getBatteryInfo(levelString, plugType, batLevel);
    	    
            } else if (action.equals(Intent.ACTION_DATE_CHANGED)) {
            	
            	getNextAlarm();
        	    getDate();
        	    
            } else if (action.equals(Intent.ACTION_AIRPLANE_MODE_CHANGED)) {
            	
            	updateNetworkInfo();
            	
            } else if (action.equals(updateService.MUTE_CHANGED)) {
            	
            	muteMode(false);
            	
            } else if (action.equals(updateService.WIFI_CHANGED)) {
            	
            	wifiMod();
            	
            } 
        };
    };
    
    private void updateNetworkInfo() {
    	TextView Network = (TextView) findViewById(R.id.Network);
    	TelephonyManager telephonyManager =((TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE));
    	String operatorName = telephonyManager.getNetworkOperatorName();
    	boolean airplane = Settings.System.getInt(
    		      getBaseContext().getContentResolver(),
    		      Settings.System.AIRPLANE_MODE_ON, 0) == 1;
    	ConnectivityManager connManager =((ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE));
    	state = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isAvailable();
    	if(airplane) {
	    	Network.setText(
                    getBaseContext().getString(R.string.airplane_mode));
    	} else {
    		if(state = true) {
    			Network.setText(operatorName);
    		} else {
    			Network.setText(
    					getBaseContext().getString(R.string.no_service));
    		}
    	}
    }
    
    private void getBatteryInfo(String level, int plugged, int raw_level) {
    	TextView battery = (TextView) findViewById(R.id.batteryInfoText);
    	if(plugged != 0) {
    		if(raw_level != 100) {
    	    	battery.setText(
                        getBaseContext().getString(R.string.battery_charging, level + "%"));
    		} else {
    	    	battery.setText(
                        getBaseContext().getString(R.string.battery_charged));
    		}
    	} else {
	    	battery.setText(
                    getBaseContext().getString(R.string.battery_level, level + "%"));
    	}
    }
    
    private void getNextAlarm() {
    	TextView Alarm = (TextView) findViewById(R.id.nextAlarmText);
    	LinearLayout Alarmbox = (LinearLayout) findViewById(R.id.nextAlarmInfo);
    	
    	nextAlarm = Settings.System.getString(getContentResolver(),
    		    Settings.System.NEXT_ALARM_FORMATTED);
    	
    	if (nextAlarm == null || TextUtils.isEmpty(nextAlarm)) {
    		Alarmbox.setVisibility(View.GONE);
    	} else {
    		Alarmbox.setVisibility(View.VISIBLE);
    		Alarm.setText(nextAlarm);
    	}
    	
    }
    
    private void muteMode(boolean onstart) {
    	AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
    	ImageView MuteIcon = (ImageView) findViewById(R.id.mute);
    	ImageView mute_slide = (ImageView) findViewById(R.id.mute_slide);

    	switch (am.getRingerMode()) {
    	    case AudioManager.RINGER_MODE_SILENT:
    	    	mute_slide.setImageResource(R.drawable.unmute_slide);
    	    	if (utils.getCheckBoxPref(this, LockscreenSettings.MUTE_TOGGLE_KEY, true)) {
    	    		MuteIcon.setVisibility(View.VISIBLE);
    	    	} else {
    	    		MuteIcon.setVisibility(View.GONE);
    	    	}
    	    	if(!onstart) {
    				whatsHappening(R.drawable.mute, 350);
    	    	}
    	        break;
    	    case AudioManager.RINGER_MODE_VIBRATE:
    	    	mute_slide.setImageResource(R.drawable.unmute_slide);
    	    	if (utils.getCheckBoxPref(this, LockscreenSettings.MUTE_TOGGLE_KEY, true)) {
    	    		MuteIcon.setVisibility(View.VISIBLE);
    	    	} else {
    	    		MuteIcon.setVisibility(View.GONE);
    	    	}
    	    	if(!onstart) {
    	    		whatsHappening(R.drawable.mute, 350);
    	    	}
    	        break;
    	    case AudioManager.RINGER_MODE_NORMAL:
    	    	mute_slide.setImageResource(R.drawable.mute_slide);
    	    	MuteIcon.setVisibility(View.GONE);
    	    	if(!onstart) {
    	    		whatsHappening(R.drawable.unmute, 350);
    	    	}
    	        break;
    	}
    }
    
    private void wifiMod() {
    	ConnectivityManager connManager =((ConnectivityManager) getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE));
    	boolean state = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();
    	
    	ImageView wifiIcon = (ImageView) findViewById(R.id.wifi);
    	if (utils.getCheckBoxPref(this, LockscreenSettings.WIFI_MODE_KEY, true)) {
    		if (state) {
    			wifiIcon.setVisibility(View.VISIBLE);
    		} else {
    			wifiIcon.setVisibility(View.GONE);
    		}
    	} else {
			wifiIcon.setVisibility(View.GONE);
    	}
    }
    
    private void usbMsMode() {
    	String state = Environment.getExternalStorageState();
    	
    	ImageView usbMsIcon = (ImageView) findViewById(R.id.usb_ms);
    	
    	if (utils.getCheckBoxPref(this, LockscreenSettings.USB_MS_KEY, true)) {
    		if (Environment.MEDIA_SHARED.equals(state)) {
    			usbMsIcon.setVisibility(View.VISIBLE);
    		} else {
    			usbMsIcon.setVisibility(View.GONE);
    		}
    	} else {
			usbMsIcon.setVisibility(View.GONE);
    	}
    }
    
    private void getDate() {
    	TextView Day = (TextView) findViewById(R.id.day);
    	TextView MonthYear = (TextView) findViewById(R.id.date);
    	TextView suffix = (TextView) findViewById(R.id.sufix);
    	
        Date now = new Date();
        Day.setText(DateFormat.format("dd", now));
        MonthYear.setText(DateFormat.format("MMMM yyyy", now));

        //get day suffix (ie. 'th')
        
    	String fullday = (String) DateFormat.format("dd", now);
    	String halfday = fullday.substring(1);
    	int dayNum = java.lang.Integer.parseInt(halfday);
    	
    	switch (dayNum) {
    	case 1:
    		suffix.setText("st ");
    		break;
    	case 2:
    		suffix.setText("nd ");
    		break;
    	case 3:
    		suffix.setText("rd ");
    		break;
    	case 0:
    	case 4:
    	case 5:
    	case 6:
    	case 7:
    	case 8:
    	case 9:
    		suffix.setText("th ");
    		break;
    	}
    }
    
    private void updateArt(long album, long song) {
    	try {
    		
    		// Set views
    		ImageView AlbumArt = (ImageView) findViewById(R.id.Art);
    		
    		// Get info from service
    		Bitmap art = utils.getArtwork(mainActivity.this, song, album, false);
    		
    		//Bind info/images to Views
    		
    		AlbumArt.setImageBitmap(art);
    		
    	} catch (Exception e) {
    	e.printStackTrace();
    	throw new RuntimeException(e);
    	}
    }
    
    private void showHideArt() {
    	ImageView AlbumArt = (ImageView) findViewById(R.id.Art);
    	
    	if(playback) {
    		if(AlbumArt.getVisibility() != View.VISIBLE) {
    		fadeArt(true, R.anim.fadein);
    	}
    	} else {
    		fadeArt(false, R.anim.fadeout);
    	}
    }
    
    private void updateInfo(String artist, String album, String track) {
    	TextView Music = (TextView) findViewById(R.id.MusicInfo);
    	String NowPlaying = getString(R.string.music_info, track, artist);
		Music.setText(NowPlaying);
    }

    //Get starting info (Stock)
    private class MediaPlayerServiceConnectionStock implements ServiceConnection {
    	public void onServiceConnected(ComponentName name, IBinder service) {
    			com.android.music.IMediaPlaybackService google =
    				com.android.music.IMediaPlaybackService.Stub.asInterface(service);
    	
    			try {
    				if (google.isPlaying()) {
    					playback = true;
    	            	if (utils.getCheckBoxPref(getBaseContext(), LockscreenSettings.KEY_SHOW_ART, true)) {
    					updateArt(google.getAlbumId(), google.getAudioId());
    	            	}
    					updateInfo(google.getArtistName(), google.getAlbumName(), google.getTrackName());
    					setPlayButton();
    					showHideControlsStart(true);
    				} else {
    					showHideControlsStart(false);
    				}
    			} catch (RemoteException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    			}
    	
    	
    	}
    	public void onServiceDisconnected(ComponentName name) {
    	}
    	}
    
    //Get starting info (HTC Music)
    private class MediaPlayerServiceConnectionHTC implements ServiceConnection {
    	public void onServiceConnected(ComponentName name, IBinder service) {

    			com.htc.music.IMediaPlaybackService htc =
    				com.htc.music.IMediaPlaybackService.Stub.asInterface(service);
    	
    			try {
    				if (htc.isPlaying()) {
    					playback = true;
    	            	if (utils.getCheckBoxPref(getBaseContext(), LockscreenSettings.KEY_SHOW_ART, true)) {
    					updateArt(htc.getAlbumId(), htc.getAudioId());
    	            	}
    					updateInfo(htc.getArtistName(), htc.getAlbumName(), htc.getTrackName());
    					setPlayButton();
    					showHideControlsStart(true);
    				} else {
    					showHideControlsStart(false);
    				}
    			} catch (RemoteException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    			}
    	
    	
    	}
    	public void onServiceDisconnected(ComponentName name) {
    	}
    	}
    	
        //Get starting info (Music Mod)
        private class MediaPlayerServiceConnectionPirate implements ServiceConnection {
        	public void onServiceConnected(ComponentName name, IBinder service) {

        			com.piratemedia.musicmod.IMediaPlaybackService piratemedia =
        				com.piratemedia.musicmod.IMediaPlaybackService.Stub.asInterface(service);
        	
        			try {
        				if (piratemedia.isPlaying()) {
        					playback = true;
        					if (utils.getCheckBoxPref(getBaseContext(), LockscreenSettings.KEY_FULLSCREEN, true)) {
        					updateArt(piratemedia.getAlbumId(), piratemedia.getAudioId());
        					}
        					updateInfo(piratemedia.getArtistName(), piratemedia.getAlbumName(), piratemedia.getTrackName());
        					setPlayButton();
        					showHideControlsStart(true);
        				} else {
        					showHideControlsStart(false);
        				}
        			} catch (RemoteException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        			}
        	
        	
        	}
        	public void onServiceDisconnected(ComponentName name) {
        	}
        	}
    
    //set button intents
    private void setPlayButton() {
    	
    	ImageButton play = (ImageButton) findViewById(R.id.playIcon);
    	ImageButton pause = (ImageButton) findViewById(R.id.pauseIcon);
    	
				if(playback) {
					pause.setVisibility(View.VISIBLE);
					play.setVisibility(View.GONE);
				} else {
					play.setVisibility(View.VISIBLE);
					pause.setVisibility(View.GONE);
				}
        
    }
    
    //show/hide Music Controls
    
    private void showHideControlsStart(Boolean show) {
    	LinearLayout InfoBox = (LinearLayout) findViewById(R.id.InfoBox);
    	
    	if(show) {
    		InfoBox.setVisibility(View.VISIBLE);
    	} else {
    		InfoBox.setVisibility(View.GONE);
    	}
    }
    
    private void showHideControls(Boolean show) {    	
    	if(show) {
    		fadeControls(true, R.anim.fadein_fast);
    	} else {
    		fadeControls(false, R.anim.fadeout_fast);
    	}
    }
    
    //set intents for media buttons
    private void setButtonIntents() {

    	ImageButton back = (ImageButton) findViewById(R.id.rewindIcon);
    	ImageButton play = (ImageButton) findViewById(R.id.playIcon);
    	ImageButton pause = (ImageButton) findViewById(R.id.pauseIcon); 
    	ImageButton next = (ImageButton) findViewById(R.id.forwardIcon);
    	
        switch(getPlayer()) {
     	case 1:
     		prevString = "com.android.music.musicservicecommand.previous";
     		toggleString = "com.android.music.musicservicecommand.togglepause";
     		nextString = "com.android.music.musicservicecommand.next";
     		break;
     	case 2:
     		prevString = "com.htc.music.musicservicecommand.previous";
     		toggleString = "com.htc.music.musicservicecommand.togglepause";
     		nextString = "com.htc.music.musicservicecommand.next";
     		break;
     	case 3:
     		prevString = "com.piratemedia.musicmod.musicservicecommand.previous";
     		toggleString = "com.piratemedia.musicmod.musicservicecommand.togglepause";
     		nextString = "com.piratemedia.musicmod.musicservicecommand.next";
     		break;
        }
    	
    	back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
             Intent intent;
             intent = new Intent(prevString);
             getBaseContext().sendBroadcast(intent);
             }
          });

        play.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
             Intent intent;
             intent = new Intent(toggleString);
             getBaseContext().sendBroadcast(intent);
             }
            
          });

        pause.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
             Intent intent;
             intent = new Intent(toggleString);
             getBaseContext().sendBroadcast(intent);
             }
          });

        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
             Intent intent;
             intent = new Intent(nextString);
             getBaseContext().sendBroadcast(intent);
             }
          });
    }
    
    public boolean onKeyDown(int keyCode, KeyEvent event) 
    { 
           if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
               Intent intent;
               intent = new Intent("com.android.music.musicservicecommand.previous");
               getBaseContext().sendBroadcast(intent);
                   return true; 
           } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) { 
               Intent intent;
               intent = new Intent("com.android.music.musicservicecommand.next");
               getBaseContext().sendBroadcast(intent);
                   return true; 
           } else { 
                   return super.onKeyDown(keyCode, event); 
           } 
    } 

    
    private void toggleMusic() {
    	LinearLayout InfoBox = (LinearLayout) findViewById(R.id.InfoBox);
    	if(InfoBox.getVisibility() == View.VISIBLE) {
    		showHideControls(false);
    	} else {
    		showHideControls(true);
    	}
    }
    
    private Animation loadAnim(int id, Animation.AnimationListener listener) {
        Animation anim = AnimationUtils.loadAnimation(getBaseContext(), id);
        if (listener != null) {
            anim.setAnimationListener(listener);
        }
        return anim;
    } 
    
    private void fadeArt(boolean visible, int anim) {
    	ImageView AlbumArt = (ImageView) findViewById(R.id.Art);
        AlbumArt.setVisibility(visible ? View.VISIBLE : View.GONE);
        AlbumArt.startAnimation(loadAnim(anim, null));
    }
    
    private void fadeControls(boolean visible, int anim) {
    	LinearLayout InfoBox = (LinearLayout) findViewById(R.id.InfoBox);
        InfoBox.setVisibility(visible ? View.VISIBLE : View.GONE);
        InfoBox.startAnimation(loadAnim(anim, null));
    }
    
    // gmail count TODO: not crashing anymore, and seems to be
    // doing the right stuff, but its still got working :( andy fix? :P
		public static int getGmailUnreadCount(Context context) { 
    	    
    	    String account="eliot@piratemedia.tv";
    	    Uri LABELS_URI = GMAIL_CONTENT_URI;
    	    Uri ACCOUNT_URI = Uri.withAppendedPath(LABELS_URI, account);
    	    ContentResolver contentResolver = context.getContentResolver();
    	    Cursor cursor = contentResolver.query(ACCOUNT_URI, null, null, null, null);

    	    int count = 0; 
    	    if(cursor==null)return 0;
    	    if (cursor.moveToFirst()) {
    	        int unreadColumn = cursor.getColumnIndex(NUM_UNREAD_CONVERSATIONS);
    	        int nameColumn = cursor.getColumnIndex(NAME);
    	        do {
    	        	String name = cursor.getString(nameColumn);
    	            String unread = cursor.getString(unreadColumn);//here's the value you need
    	            count = Integer.parseInt(unread); 
    	        } while (cursor.moveToNext());
    	    }
    	    return count;
    	}

        private void setGmailCountText() {
        	if (utils.getCheckBoxPref(this, LockscreenSettings.GMAIL_COUNT_KEY, true)) {
        		if (mGetGmailCount <= 0) {
                    mGmailCount.setVisibility(View.GONE);
                } else {
                	if (mGetGmailCount == 1) {
                		mGmailCount.setVisibility(View.VISIBLE);
                		mGmailCount.setText(
                            getBaseContext().getString(R.string.lockscreen_1_email, mGetGmailCount));
                	} else {
                		mGmailCount.setVisibility(View.VISIBLE);
                		mGmailCount.setText(
                            getBaseContext().getString(R.string.lockscreen_lots_email, mGetGmailCount));
                	}
                }
        	} else {
                mGmailCount.setVisibility(View.GONE);
        	}
        }

    // end gmail count

    // missed call count

    public static int getMissedCallCount(Context context) { 
             String CALL_LOG_MISSED = "type"; 
             String MISSED_NEW = "new"; 
             String MISSED_CONDITION = CALL_LOG_MISSED + "=3 AND " + MISSED_NEW + "=1";
             int count = 0; 
             Cursor cursor = context.getContentResolver().query( 
                   CALL_LOG_CONTENT_URI, 
                   new String[] { CALLER_ID }, 
                   MISSED_CONDITION, null, null); 
             if (cursor != null) { 
                try { 
                   count = cursor.getCount(); 
                } finally { 
                   cursor.close(); 
                } 
             } 
             return count;
       }

        private void setMissedCountText() {
        	if (utils.getCheckBoxPref(this, LockscreenSettings.MISSED_CALL_KEY, true)) {
        		if (mGetMissedCount <= 0) {
        			mMissedCount.setVisibility(View.GONE);
                } else {
                	if (mGetMissedCount == 1) {
                		mMissedCount.setVisibility(View.VISIBLE);
                		mMissedCount.setText(
                            getBaseContext().getString(R.string.lockscreen_1_missed, mGetMissedCount));
                	} else {
                		mMissedCount.setVisibility(View.VISIBLE);
                		mMissedCount.setText(
                            getBaseContext().getString(R.string.lockscreen_lots_missed, mGetMissedCount));
                	}
                }
        	} else {
        		mMissedCount.setVisibility(View.GONE);
        	}
        }

    // end missed call count

    // unread sms count

    public static int getUnreadSmsCount(Context context) { 
             String SMS_READ_COLUMN = "read"; 
             String UNREAD_CONDITION = SMS_READ_COLUMN + "=0"; 
             int count = 0; 
             Cursor cursor = context.getContentResolver().query( 
                   SMS_INBOX_CONTENT_URI, 
                   new String[] { SMS_ID }, 
                   UNREAD_CONDITION, null, null); 
             if (cursor != null) { 
                try { 
                   count = cursor.getCount(); 
                } finally { 
                   cursor.close(); 
                } 
             }
             return count;
       }

        private void setSmsCountText() {
        	if (utils.getCheckBoxPref(this, LockscreenSettings.SMS_COUNT_KEY, true)) {
        		if (mGetSmsCount <= 0) {
                    mSmsCount.setVisibility(View.GONE);
                } else {
                	mSmsCount.setVisibility(View.VISIBLE);
                    mSmsCount.setText(
                            getBaseContext().getString(R.string.lockscreen_sms_count, mGetSmsCount));
                }
        	} else {
                mSmsCount.setVisibility(View.GONE);
        	}
        }

    // end sms count
        
    // set fullscreen based on settings
        private void setFullscreen() {
        	if (utils.getCheckBoxPref(this, LockscreenSettings.KEY_FULLSCREEN, true)) {
        			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        		} else {
        			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        		}
        }
        
    // set landscape/portrait based on settings
        private void setLandscape() {
        	if (utils.getCheckBoxPref(this, LockscreenSettings.KEY_LANDSCAPE, false)) {
        		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        	} else {
        		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        	}
        }
        
    // Set Which Media Player we want to use
    	public int getPlayer() {
    		String playerString = utils.getStringPref(this , LockscreenSettings.KEY_MUSIC_PLAYER, DefaultMusicApp());
    		int player = Integer.parseInt(playerString);  
    		switch(player) {
    			case 1:
    				//Set Stock Music Player
    				return 1;
    			case 2:
    				//Set HTC Music as Player
    				return 2;
    			case 3:
    				//Set Music Mod as Player
    				return 3;
    		}
    		return 1;
    	}
    	
    // Set Custom Background Image
    	public void setCustomBackground() {
        	if (utils.getCheckBoxPref(this, LockscreenSettings.KEY_SHOW_CUSTOM_BG, false)) {
        		String BG_FILE = getFilesDir().toString() + File.separator+"bg_pic.jpg";
				BitmapFactory.Options options=new BitmapFactory.Options();
				options.inSampleSize = 5;
        		Bitmap bgBitmap = BitmapFactory.decodeFile(BG_FILE,options);
        		BitmapDrawable background = new BitmapDrawable(getResources(),bgBitmap);
        		background.setGravity(Gravity.FILL);
        		
        		getWindow().setBackgroundDrawable(background);
        	}else{
        		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        	}
    	}
    	
    	//slidyness stuff
        final Runnable mScroll = new Runnable() {
            public void run() {
            	LinearLayout mainFrame = (LinearLayout) findViewById(R.id.base);
        		HorizontalScrollView slider = (HorizontalScrollView) findViewById(R.id.mainSlide);
                slider.smoothScrollTo(mainFrame.getLeft(), 0);
                slider.postInvalidate();
            }
        };
        
        private void fadeCount(boolean visible, int anim) {
        	ImageView Count = (ImageView) findViewById(R.id.count);
            Count.setVisibility(visible ? View.VISIBLE : View.GONE);
            Count.startAnimation(loadAnim(anim, null));
        }
        
        private void doAction() {
        	if(unlock) {
        		unlockScreen();
        	} else {
        		mutePhone();
        		// do mute action
        	}
        }
        
        
        final Runnable mUnlockToast = new Runnable() {
            public void run() {
            	int num = unlock_count + 1;
            	ImageView count = (ImageView) findViewById(R.id.count);
            switch(num) {
            	case 5:
            		count.setImageResource(R.drawable.count_5);
            		fadeCount(true, R.anim.fadeout_fast);
            		break;
            	case 4:
            		count.setImageResource(R.drawable.count_4);
            		fadeCount(true, R.anim.fadeout_fast);
            		break;
            	case 3:
            		count.setImageResource(R.drawable.count_3);
            		fadeCount(true, R.anim.fadeout_fast);
            		break;
            	case 2:
            		count.setImageResource(R.drawable.count_2);
            		fadeCount(true, R.anim.fadeout_fast);
            		break;
            	case 1:
            		count.setImageResource(R.drawable.count_1);
            		fadeCount(true, R.anim.fadeout_fast);
            		break;
            	case 0:
            		doAction();
            		break;
            }
            }
        };
        
        private void startCount(boolean mute) {
        	if(mute) {
        		unlock = false;
        	} else {
        		unlock = true;
        	}
        	timer.scheduleAtFixedRate( new TimerTask() {

        	public void run() {
        		Thread start;

        	switch(unlock_count) {
        	case 5:
                start = new Thread() {
                    public void run() {
                        mHandler.post(mUnlockToast);
                    }
                };
                start.start();
        		unlock_count = 4;
        		break;
        	case 4:
                start = new Thread() {
                    public void run() {
                        mHandler.post(mUnlockToast);
                    }
                };
                start.start();
        		unlock_count = 3;
        		break;
        	case 3:
                start = new Thread() {
                    public void run() {
                        mHandler.post(mUnlockToast);
                    }
                };
                start.start();
        		unlock_count = 2;
        		break;
        	case 2:
                start = new Thread() {
                    public void run() {
                        mHandler.post(mUnlockToast);
                    }
                };
                start.start();
        		unlock_count = 1;
        		break;
        	case 1:
                start = new Thread() {
                    public void run() {
                        mHandler.post(mUnlockToast);
                    }
                };
                start.start();
        		unlock_count = 0;
        		break;
        	case 0:
                start = new Thread() {
                    public void run() {
                        mHandler.post(mUnlockToast);
                    }
                };
                start.start();
        		unlock_count = -1;
        		break;
        	}

        	}

        	}, 0, 500);
        	; }
        
        private void stopAllCounts() {
        	ImageView count = (ImageView) findViewById(R.id.count);
        	count.setVisibility(View.GONE);
        	if (timer != null){
        	timer.cancel();
        	timer = new Timer();
        	}

        	}
        //end slidyness stuff

		/**
		 * ***onNewIntent***
		 * 
		 * 1-If the user already have a selected home app in preferences, load it
		 * 2-Else, show our home-app selection activity for the user to choose
		 */		@Override
		protected void onNewIntent(Intent intent) {
			Log.d("LOCKSCREEN","New intent!!");
			super.onNewIntent(intent);
			if ((intent.getFlags() & Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) !=
                Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) {
				//ADW: This is what happens when user click home button while showing the lock screen
				Log.d("LOCKSCREEN","We should NOT do anything!!");
			}
		}
		 
		//unbind music service
		 private void unbindMusic() {	
			 //we need to unbind the music service to stop NPE's here.
		 }
		 
		/**
		 * ***unlockScreen***
		 * 
		 * I don't know if we should call the user stored launcher
		 * or just call finish()....
		 * If the lock screen is what first gets loaded when the phone boots
		 * we should call the launcher or show the launcher picker
		 * 
		 * But if the lockscreen is visible just cause the user turned on the screen,
		 * we just should call finish() so it goes to the last open app
		 */
		private void unlockScreen(){
			whatsHappening(R.drawable.unlock, Toast.LENGTH_SHORT);
	        finish();
	        overridePendingTransition(R.anim.fadein_fast, R.anim.fadeout_fast);
		}
		/**
		 * ***Cool Custom Toast for unlock, mute etc ***
		 * 
		 * Just to show a nice graphic when unlocking or
		 * muting etc.
		 */
		private void whatsHappening(int imageRes, int dur) {
			LayoutInflater inflater = getLayoutInflater();
			View layout = inflater.inflate(R.layout.cooltoast,
			                               (ViewGroup) findViewById(R.id.toast_layout_root));

			ImageView image = (ImageView) layout.findViewById(R.id.image);
			image.setImageResource(imageRes);

			Toast toast = new Toast(getApplicationContext());
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.setDuration(dur);
			toast.setView(layout);
			toast.show();
		}
		
	    private String DefaultMusicApp() {
	    	
	    	final ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
			final List<RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);
	    
		     		String StockMusic = "com.android.music";
		     		String HTCMusic = "com.htc.music";
		
			for (int i = 0; i < services.size(); i++) {
				if (StockMusic.equals(services.get(i).service.getPackageName())) {
					return "1";
				} else if (HTCMusic.equals(services.get(i).service.getPackageName())) {
					return "2";
				} else {
					return "3";
				}
			}
			return "1";
	    }
	    
	    private void mutePhone() {
	    	AudioManager am = (AudioManager)
	    	this.getSystemService(Context.AUDIO_SERVICE);
		    if (am.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
	    		am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
	    	} else {
		    	am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
	    	}
        	Thread t = new Thread() {
                public void run() {
                    mHandler.post(mScroll);
                }
            };
            t.start();
	    }
}