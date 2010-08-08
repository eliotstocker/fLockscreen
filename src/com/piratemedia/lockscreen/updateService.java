package com.piratemedia.lockscreen;

import android.content.Intent;
import android.content.ServiceConnection;
import android.app.Service;
import android.os.IBinder;
import android.content.ComponentName;

import com.android.music.IMediaPlaybackService;

public class updateService extends Service {
    
	public static final String MUSIC_CHANGED = "com.piratemedia.lockscreen.musicchanged";
	public static final String MUSIC_STOPPED = "com.piratemedia.lockscreen.musicstopped";
	public static final String SMS_CHANGED = "com.piratemedia.lockscreen.smschanged";
	public static final String PHONE_CHANGED = "com.piratemedia.lockscreen.phonechanged";
	public static final String MUTE_CHANGED = "com.piratemedia.lockscreen.mutechanged";
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
	

    @Override
    public void onCreate() {
        super.onCreate();
        
    }

    public int onStartCommand(Intent aIntent, int aFlags, int aStartId) {
        
        onStart(aIntent, aStartId);

        return 2;
    }

    @Override
    public void onStart(Intent aIntent, int aStartId) {

        if (aIntent.getAction().equals("com.android.music.playbackcomplete") && getPlayer() == 1) {
            // The song has ended, stop the service
            stopSelf();
        } else if (aIntent.getAction().equals("com.android.music.playstatechanged") 
                || aIntent.getAction().equals("com.android.music.metachanged")
                || aIntent.getAction().equals("com.android.music.queuechanged")
                || aIntent.getAction().equals("com.android.music.playbackcomplete")
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
        	} else if (aIntent.getAction().equals("com.htc.music.playbackcomplete") && getPlayer() == 2) {
                // The song has ended, stop the service
                stopSelf();
            } else if (aIntent.getAction().equals("com.htc.music.playstatechanged") 
                    || aIntent.getAction().equals("com.htc.music.metachanged")
                    || aIntent.getAction().equals("com.htc.music.queuechanged")
                    || aIntent.getAction().equals("com.htc.music.playbackcomplete")
                    && getPlayer() == 3) {

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
        	} else if (aIntent.getAction().equals("com.piratemedia.musicmod.playbackcomplete") && getPlayer() == 3) {
                // The song has ended, stop the service
                stopSelf();
            } else if (aIntent.getAction().equals("com.piratemedia.musicmod.playstatechanged") 
                    || aIntent.getAction().equals("com.piratemedia.musicmod.metachanged")
                    || aIntent.getAction().equals("com.piratemedia.musicmod.queuechanged")
                    || aIntent.getAction().equals("com.piratemedia.musicmod.playbackcomplete")
                    && getPlayer() == 4) {

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
            } else if (aIntent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            	notifyChange(SMS_CHANGED);
            } else if (aIntent.getAction().equals("android.intent.action.PHONE_STATE")) {
            	notifyChange(PHONE_CHANGED);
            } else if (aIntent.getAction().equals("android.media.RINGER_MODE_CHANGED")) {
            	notifyChange(MUTE_CHANGED);
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
        }