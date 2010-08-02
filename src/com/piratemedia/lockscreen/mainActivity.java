package com.piratemedia.lockscreen;

import java.util.Date;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.music.IMediaPlaybackService;

public class mainActivity extends Activity {
	
	public static LinearLayout InfoBox;
	public IMediaPlaybackService mService = null;
	public boolean playback = false;
	public String nextAlarm = null;
	
    public static final Uri GMAIL_CONTENT_URI = Uri.parse("content://gmail-ls");
    public static final Uri GMAIL_UNREAD_CONTENT_URI  = Uri.withAppendedPath(GMAIL_CONTENT_URI, "messages");
    public static final String GMAIL_ID = "_id";

    public static final Uri CALL_CONTENT_URI = Uri.parse("content://call_log");
    public static final Uri CALL_LOG_CONTENT_URI  = Uri.withAppendedPath(CALL_CONTENT_URI, "calls");
    public static final String CALLER_ID = "_id";

    public static final Uri SMS_CONTENT_URI = Uri.parse("content://sms");
    public static final Uri SMS_INBOX_CONTENT_URI = Uri.withAppendedPath(SMS_CONTENT_URI, "inbox");
    public static final String SMS_ID = "_id";
    
    private TextView mSmsCount;
    private TextView mMissedCount;
    private TextView mGmailCount;

    private int mGetSmsCount = 0;
    private int mGetMissedCount = 0;
    private int mGetGmailCount = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main);
	
	mSmsCount = (TextView) findViewById(R.id.smscount);
    mMissedCount = (TextView) findViewById(R.id.missedcount);
    mGmailCount = (TextView) findViewById(R.id.gmailcount);
	
	Intent i = new Intent();
	i.setClassName("com.android.music", "com.android.music.MediaPlaybackService");
	ServiceConnection conn = new MediaPlayerServiceConnection();
	this.bindService(i, conn, 0);
	
	Intent serviceIntent = new Intent();
	serviceIntent.setAction("com.android.music.MediaPlaybackService");
	startService(serviceIntent);
	
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
	
	@Override
	public void onResume() {
		super.onResume();
        
	    mGetSmsCount = getUnreadSmsCount(getBaseContext());
		mGetMissedCount = getMissedCallCount(getBaseContext());
		
	    setSmsCountText();
	    setMissedCountText();
	    
	    getNextAlarm();
	    getDate();
	    updateNetworkInfo();
	    muteMode();
		
	}
	
    @Override
    public void onStart() {
        super.onStart();
        
        IntentFilter f = new IntentFilter();
        f.addAction(updateService.MUSIC_CHANGED);
        f.addAction(updateService.MUSIC_STOPPED);
        f.addAction(updateService.SMS_CHANGED);
        f.addAction(updateService.PHONE_CHANGED);
        f.addAction(updateService.MUTE_CHANGED);
        f.addAction(Intent.ACTION_BATTERY_CHANGED);
        f.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        f.addAction(Intent.ACTION_DATE_CHANGED);
        registerReceiver(mStatusListener, new IntentFilter(f));
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

            	updateArt(aAlbumID, aTrackID);
                updateInfo(aArtist, aAlbum, aTrack);
                playback = true;
                setPlayButton();
                showHideArt();
                
            } else if (action.equals(updateService.MUSIC_STOPPED)) {
            	
            	playback = false;
            	setPlayButton();
            	showHideArt();

            } else if (action.equals(updateService.SMS_CHANGED)) {
            	
            	updateCounts(true);
        	    
            } else if (action.equals(updateService.PHONE_CHANGED)) {
        	
            	updateCounts(false);
            	
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
            	
            	muteMode();
            	
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
    	int state = telephonyManager.getPhoneType();
    	if(airplane) {
	    	Network.setText(
                    getBaseContext().getString(R.string.airplane_mode));
    	} else {
    		if(state != telephonyManager.PHONE_TYPE_NONE) {
    			Network.setText(operatorName);
    		} else {
    			Network.setText(
    					getBaseContext().getString(R.string.no_service));
    		}
    	}
    }
    
    private void updateCounts(boolean sms) {
    	
    	if(sms) {
    		mGetSmsCount = getUnreadSmsCount(getBaseContext());
    		//always one behind, so add one
    		mGetSmsCount = mGetSmsCount + 1;
    		setSmsCountText();
    	} else {
		mGetMissedCount = getMissedCallCount(getBaseContext());
		//always one behind, so add one
		mGetMissedCount = mGetMissedCount + 1;
	    setMissedCountText();
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
    
    private void muteMode() {
    	AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
    	ImageView MuteIcon = (ImageView) findViewById(R.id.mute);

    	switch (am.getRingerMode()) {
    	    case AudioManager.RINGER_MODE_SILENT:
    	    	MuteIcon.setVisibility(View.VISIBLE);
    	        break;
    	    case AudioManager.RINGER_MODE_VIBRATE:
    	    	MuteIcon.setVisibility(View.VISIBLE);
    	        break;
    	    case AudioManager.RINGER_MODE_NORMAL:
    	    	MuteIcon.setVisibility(View.GONE);
    	        break;
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
    
    private void updateResumeInfo() {
    	try {
    		
    		// Set views
    		TextView Music = (TextView) findViewById(R.id.MusicInfo);
    		
    		// Get info from service
    		String track = mService.getTrackName();
    		String artist = mService.getArtistName();
    		
    		//Bind info/images to Views
    		
    		String NowPlaying = getString(R.string.music_info, track, artist);
    		Music.setText(NowPlaying);
    		
    	} catch (Exception e) {
    	e.printStackTrace();
    	throw new RuntimeException(e);
    	}
    }
    
    private void updateInfo(String artist, String album, String track) {
    	
    	TextView Music = (TextView) findViewById(R.id.MusicInfo);
    	
    	String NowPlaying = getString(R.string.music_info, track, artist);
		Music.setText(NowPlaying);
    }

    //Get Art
    private class MediaPlayerServiceConnection implements ServiceConnection {
    	public void onServiceConnected(ComponentName name, IBinder service) {
    	mService = IMediaPlaybackService.Stub.asInterface(service);
    	
    	try {
			if (mService.isPlaying()) {
				playback = true;
		    	updateArt(mService.getAlbumId(), mService.getAudioId());
		    	updateResumeInfo();
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
    	
    	back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
             Intent intent;
             intent = new Intent("com.android.music.musicservicecommand.previous");
             getBaseContext().sendBroadcast(intent);
             }
          });

        play.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
             Intent intent;
             intent = new Intent("com.android.music.musicservicecommand.togglepause");
             getBaseContext().sendBroadcast(intent);
             }
            
          });

        pause.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
             Intent intent;
             intent = new Intent("com.android.music.musicservicecommand.togglepause");
             getBaseContext().sendBroadcast(intent);
             }
          });

        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
             Intent intent;
             intent = new Intent("com.android.music.musicservicecommand.next");
             getBaseContext().sendBroadcast(intent);
             }
          });
    }
    
    public boolean onKeyLongPress(int keyCode, KeyEvent event) 
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
    
 // gmail count TODO: doesnt work, must fix.

    public static int getGmailUnreadCount(Context context) { 
             String GMAIL_UNREAD = "label_ids";
             String GMAIL_CONDITION = GMAIL_UNREAD + " like %^u%";
             int count = 0; 
             Cursor cursor = context.getContentResolver().query( 
                   GMAIL_UNREAD_CONTENT_URI, 
                   new String[] { GMAIL_ID }, 
                   GMAIL_CONDITION, null, null); 
             if (cursor != null) {
                Log.d ("Gmail Count", "Gmail cursor != null");
                try { 
                   count = cursor.getCount(); 
                   Log.d ("Gmail Count", "Gmail cursor.getCount()");
                } finally { 
                   cursor.close(); 
                } 
             } 
             return count;
       }

        private void setGmailCountText() {
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
    	   if (mGetSmsCount <= 0) {
                    mSmsCount.setVisibility(View.GONE);
                } else {
    		mSmsCount.setVisibility(View.VISIBLE);
                    mSmsCount.setText(
                            getBaseContext().getString(R.string.lockscreen_sms_count, mGetSmsCount));
                }
        }

    // end sms count
    
}