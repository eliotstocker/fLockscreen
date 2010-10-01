package com.piratemedia.lockscreen;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.content.ComponentName;

import com.android.music.IMediaPlaybackService;

public class updateService extends Service {
    
	public static final String MUSIC_CHANGED = "com.piratemedia.lockscreen.musicchanged";
	public static final String MUSIC_STOPPED = "com.piratemedia.lockscreen.musicstopped";
	public static final String SMS_CHANGED = "com.piratemedia.lockscreen.smschanged";
	public static final String PHONE_CHANGED = "com.piratemedia.lockscreen.phonechanged";
	public static final String MUTE_CHANGED = "com.piratemedia.lockscreen.mutechanged";
	public static final String WIFI_CHANGED = "com.piratemedia.lockscreen.wifichanged";
	public static final String BT_CHANGED = "com.piratemedia.lockscreen.btchanged";
	public static final String START_STOP_FORGROUND = "com.piratemedia.lockscreen.forground";
	public static final String STOP_SERVICE = "com.piratemedia.lockscreen.kill";
	public static final int NOTIFICATION_ID = 35625;
	public IMediaPlaybackService mService = null;
	public static boolean playing = false;
	public String titleName;
	public String artistName;
	public String albumName;
	public long pos;
	public long dur;
	public long albumId;
	public long songId;
	public int batLevel;
	public int batpercentage;
	private NotificationManager mNM;
	private BroadcastReceiver mReceiver;
    @Override
    public void onCreate() {
        super.onCreate();        
    	if(!utils.getCheckBoxPref(getBaseContext(), LockscreenSettings.ENABLE_KEY, true)) {
    		stopSelf();
    	}

    	mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        //Start as foreground if user settings say so
    	foregroundStuff(utils.getCheckBoxPref(this, LockscreenSettings.SERVICE_FOREGROUND, true));
        //SCREEN ON/OFF suff
    	if(mReceiver==null){
	    	IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
	        filter.addAction(Intent.ACTION_SCREEN_OFF);
	        mReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					String action=intent.getAction();
					if(action==null)return;
		            if (action.equals(Intent.ACTION_SCREEN_ON)) {
		            	Log.d("Lockscreen", "Screen On");
		            	if (!inCall()){
		            		ManageKeyguard.disableKeyguard(getApplicationContext()); 
		            	}
					} else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
		            	Log.d("Lockscreen", "Screen Off");
		            	if (!inCall()){
		            		Intent lock=utils.getLockIntent(updateService.this);
		            		lock.setAction(utils.ACTION_LOCK);
		            		startActivity(lock);
		            		ManageKeyguard.reenableKeyguard(); 
		            	}
					}	
				}
			};;;
	        registerReceiver(mReceiver, filter);
    	}
    }
    private void foregroundStuff(boolean foreground){
    	if(foreground){
	    	CharSequence text = getText(R.string.service_notification);
	        // Set the icon, scrolling text and timestamp
	        Notification notification = new Notification(R.drawable.status_icon, text,
	                System.currentTimeMillis());
	        // The PendingIntent to launch our activity if the user selects this notification
	        Intent lockIntent=utils.getLockIntent(this);
	        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,lockIntent, 0);
	        // Set the info for the views that show in the notification panel.
	        notification.setLatestEventInfo(this, getText(R.string.app_name),text, contentIntent);
	        startForeground(NOTIFICATION_ID, notification);
    	}else{
    		stopForeground(true);
    	}
    }
    public int onStartCommand(Intent aIntent, int aFlags, int aStartId) {
        
        onStart(aIntent, aStartId);
        return START_STICKY;
        //Why did you use the "2"?
        //AFAIK, 2=START_FLAG_RETRY
        
        //Cos i like 2! its all lie 3 isnt the magic number! 2 is!!!!!! AWWWWWW YEAAHHHHH MOTHER FUKA HA HA
    }

    @Override
    public void onStart(Intent aIntent, int aStartId) {
    	//This thing is needed when the service is restarted by the system, cause aIntent can be null
    	//so null.getAction() will throw a NPE
    	if(aIntent==null)return;
    	final String action=aIntent.getAction();
    	if (action==null)return;
        if (action.equals("com.android.music.playbackcomplete") && getPlayer() == 1) {
            // The song has ended
        } else if ((action.equals("com.android.music.playstatechanged") 
                || action.equals("com.android.music.metachanged")
                || action.equals("com.android.music.queuechanged")
                || action.equals("com.android.music.playbackcomplete"))
                && getPlayer() == 1) {

            bindService(new Intent().setClassName("com.android.music", "com.android.music.MediaPlaybackService"), new ServiceConnection() {
        
                public void onServiceConnected(ComponentName aName, IBinder aService) {
                	com.android.music.IMediaPlaybackService mService =
        				com.android.music.IMediaPlaybackService.Stub.asInterface(aService);
                	
                	try {
                		if (mService.isPlaying()){
                		
                		// Get info from service
                		playing = true;
                		titleName = mService.getTrackName();
                		artistName = mService.getArtistName();
                		albumName = mService.getAlbumName();
                		pos = mService.position();
                		dur = mService.duration();
                		albumId = mService.getAlbumId();
                		songId = mService.getAudioId();
                		
                		
                		if (mService.isPlaying()) {
                			notifyChange(MUSIC_CHANGED);
                			playing = true;
                		}
                        } else {
                        	notifyChange(MUSIC_STOPPED);
                        	playing = false;
                        }
                		
                	} catch (Exception e) {
                	e.printStackTrace();
                	throw new RuntimeException(e);
                	}

                    unbindService(this);
                }
                public void onServiceDisconnected(ComponentName aName) {
                    	playing = false;
                    	notifyChange(MUSIC_STOPPED);
                }

            }, 0);
        	} else if (action.equals("com.htc.music.playbackcomplete") && getPlayer() == 2) {
                // The song has ended
            } else if ((action.equals("com.htc.music.playstatechanged") 
                    || action.equals("com.htc.music.metachanged")
                    || action.equals("com.htc.music.queuechanged")
                    || action.equals("com.htc.music.playbackcomplete"))
                    && getPlayer() == 2) {

                bindService(new Intent().setClassName("com.htc.music", "com.htc.music.MediaPlaybackService"), new ServiceConnection() {
            
                    public void onServiceConnected(ComponentName aName, IBinder aService) {
                    	com.htc.music.IMediaPlaybackService mService =
            				com.htc.music.IMediaPlaybackService.Stub.asInterface(aService);
                    	
                    	try {
                    		if (mService.isPlaying()){
                    		
                    		// Get info from service
                    		playing = true;
                    		titleName = mService.getTrackName();
                    		artistName = mService.getArtistName();
                    		albumName = mService.getAlbumName();
                    		pos = mService.position();
                    		dur = mService.duration();
                    		albumId = mService.getAlbumId();
                    		songId = mService.getAudioId();
                    		
                    		
                    		if (mService.isPlaying()) {
                    			notifyChange(MUSIC_CHANGED);
                    			playing = true;
                    		}
                            } else {
                            	notifyChange(MUSIC_STOPPED);
                            	playing = false;
                            }
                    		
                    	} catch (Exception e) {
                    	e.printStackTrace();
                    	throw new RuntimeException(e);
                    	}

                        unbindService(this);
                    }
                    public void onServiceDisconnected(ComponentName aName) {
                        	playing = false;
                        	notifyChange(MUSIC_STOPPED);
                    }

                }, 0);
        	} else if (action.equals("com.piratemedia.musicmod.playbackcomplete") && getPlayer() == 3) {
                // The song has ended
            } else if ((action.equals("com.piratemedia.musicmod.playstatechanged") 
                    || action.equals("com.piratemedia.musicmod.metachanged")
                    || action.equals("com.piratemedia.musicmod.queuechanged")
                    || action.equals("com.piratemedia.musicmod.playbackcomplete"))
                    && getPlayer() == 3) {

                bindService(new Intent().setClassName("com.piratemedia.musicmod", "com.piratemedia.musicmod.MediaPlaybackService"), new ServiceConnection() {
            
                    public void onServiceConnected(ComponentName aName, IBinder aService) {
                    	com.piratemedia.musicmod.IMediaPlaybackService mService =
            				com.piratemedia.musicmod.IMediaPlaybackService.Stub.asInterface(aService);
                    	
                    	try {
                    		if (mService.isPlaying()){
                    		
                    		// Get info from service
                    		playing = true;
                    		titleName = mService.getTrackName();
                    		artistName = mService.getArtistName();
                    		albumName = mService.getAlbumName();
                    		pos = mService.position();
                    		dur = mService.duration();
                    		albumId = mService.getAlbumId();
                    		songId = mService.getAudioId();
                    		
                    		
                    		if (mService.isPlaying()) {
                    			notifyChange(MUSIC_CHANGED);
                    			playing = true;
                    		}
                            } else {
                            	notifyChange(MUSIC_STOPPED);
                            	playing = false;
                            }
                    		
                    	} catch (Exception e) {
                    	e.printStackTrace();
                    	throw new RuntimeException(e);
                    	}

                        unbindService(this);
                    }
                    public void onServiceDisconnected(ComponentName aName) {
                        	playing = false;
                        	notifyChange(MUSIC_STOPPED);
                    }

                }, 0);
        	} else if (action.equals("com.tbig.playerpro.playbackcomplete") && getPlayer() == 4) {
                // The song has ended
            } else if ((action.equals("com.tbig.playerpro.playstatechanged") 
                    || action.equals("com.tbig.playerpro.metachanged")
                    || action.equals("com.tbig.playerpro.queuechanged")
                    || action.equals("com.tbig.playerpro.playbackcomplete"))
                    && getPlayer() == 4) {

                bindService(new Intent().setClassName("com.tbig.playerpro", "com.tbig.playerpro.MediaPlaybackService"), new ServiceConnection() {
            
                    public void onServiceConnected(ComponentName aName, IBinder aService) {
                    	com.tbig.playerpro.IMediaPlaybackService mService =
                    		com.tbig.playerpro.IMediaPlaybackService.Stub.asInterface(aService);
                    	
                    	try {
                    		if (mService.isPlaying()){
                    		
                    		// Get info from service
                    		playing = true;
                    		titleName = mService.getTrackName();
                    		artistName = mService.getArtistName();
                    		albumName = mService.getAlbumName();
                    		pos = mService.position();
                    		dur = mService.duration();
                    		albumId = mService.getAlbumId();
                    		songId = mService.getAudioId();
                    		
                    		
                    		if (mService.isPlaying()) {
                    			notifyChange(MUSIC_CHANGED);
                    			playing = true;
                    		}
                            } else {
                            	notifyChange(MUSIC_STOPPED);
                            	playing = false;
                            }
                    		
                    	} catch (Exception e) {
                    	e.printStackTrace();
                    	throw new RuntimeException(e);
                    	}

                        unbindService(this);
                    }
                    public void onServiceDisconnected(ComponentName aName) {
                        	playing = false;
                        	notifyChange(MUSIC_STOPPED);
                    }

                }, 0);
        	} else if (action.equals("org.abrantix.rockon.rockonnggl.playbackcomplete") && getPlayer() == 4) {
                // The song has ended
            } else if ((action.equals("org.abrantix.rockon.rockonnggl.playstatechanged") 
                    || action.equals("org.abrantix.rockon.rockonnggl.metachanged")
                    || action.equals("org.abrantix.rockon.rockonnggl.queuechanged")
                    || action.equals("org.abrantix.rockon.rockonnggl.playbackcomplete"))
                    && getPlayer() == 5) {

                bindService(new Intent().setClassName("org.abrantix.rockon.rockonnggl", "org.abrantix.rockon.rockonnggl.RockOnNextGenService"), new ServiceConnection() {
            
                    public void onServiceConnected(ComponentName aName, IBinder aService) {
                    	org.abrantix.rockon.rockonnggl.IRockOnNextGenService mService =
                    		org.abrantix.rockon.rockonnggl.IRockOnNextGenService.Stub.asInterface(aService);
                    	
                    	try {
                    		if (mService.isPlaying()){
                    		
                    		// Get info from service
                    		playing = true;
                    		titleName = mService.getTrackName();
                    		artistName = mService.getArtistName();
                    		albumName = mService.getAlbumName();
                    		pos = mService.position();
                    		dur = mService.duration();
                    		albumId = mService.getAlbumId();
                    		songId = mService.getAudioId();
                    		
                    		
                    		if (mService.isPlaying()) {
                    			notifyChange(MUSIC_CHANGED);
                    			playing = true;
                    		}
                            } else {
                            	notifyChange(MUSIC_STOPPED);
                            	playing = false;
                            }
                    		
                    	} catch (Exception e) {
                    	e.printStackTrace();
                    	throw new RuntimeException(e);
                    	}

                        unbindService(this);
                    }
                    public void onServiceDisconnected(ComponentName aName) {
                        	playing = false;
                        	notifyChange(MUSIC_STOPPED);
                    }

                }, 0);
            } else if (action.equals("android.provider.Telephony.SMS_RECEIVED")) {
            	notifyChange(SMS_CHANGED);
            } else if (action.equals("android.intent.action.PHONE_STATE")) {
            	notifyChange(PHONE_CHANGED);
            } else if (action.equals("android.media.RINGER_MODE_CHANGED")) {
            	notifyChange(MUTE_CHANGED);
            } else if (action.equals("android.net.wifi.STATE_CHANGE")) {
            	notifyChange(WIFI_CHANGED);
            } else if (action.equals("android.net.wifi.WIFI_STATE_CHANGED")) {
            	notifyChange(WIFI_CHANGED);
            } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            	notifyChange(BT_CHANGED);
            } else if(action.equals(Intent.ACTION_BOOT_COMPLETED)){
            	Log.d("Lockscreen", "Boot Completed");
            	Intent lock=utils.getLockIntent(this);
            	lock.setAction(utils.ACTION_UNLOCK);
            	startActivity(lock);
            	Intent levelup;
            	levelup = new Intent("com.teslacoilsw.widgetlocker.intent.LOCKED");
                getBaseContext().sendBroadcast(levelup);
            } else if(action.equals(START_STOP_FORGROUND)){
            	foregroundStuff(utils.getCheckBoxPref(getBaseContext(), LockscreenSettings.SERVICE_FOREGROUND, true));
            } else if(action.equals(STOP_SERVICE)){
            	stopSelf();
            }
        
        }
    
private void notifyChange(String what) {
        
        Intent i = new Intent(what);
        i.putExtra("artist", artistName);
        i.putExtra("album", albumName);
        i.putExtra("track", titleName);
        i.putExtra("trackID", songId);
        i.putExtra("albumID", albumId);
        sendBroadcast(i);
    }
            public IBinder onBind(Intent aIntent) {

                return null;
            }
            
            // Set Which Media Player we want to use
        	public int getPlayer() {
        		String playerString = utils.getStringPref(this , LockscreenSettings.KEY_MUSIC_PLAYER, "1");
        		int player = Integer.parseInt(playerString);  
        		return player;
        	}
		
		private boolean inCall() {
			TelephonyManager telMan =((TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE));
	    	int state = telMan.getCallState();
	    	
	    	switch(state) {
	    		case TelephonyManager.CALL_STATE_IDLE:
	    			return false;
	    		case TelephonyManager.CALL_STATE_RINGING:
	    			return false;
	    		case TelephonyManager.CALL_STATE_OFFHOOK:
	    			return true;
	    	}
	    	return false;
		}
        @Override
        public void onDestroy() {
            if(mReceiver!=null){
                unregisterReceiver(mReceiver);
                mReceiver=null;
            }
            super.onDestroy();
        }
}