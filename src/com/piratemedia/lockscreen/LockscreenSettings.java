/**
 * Android Music Settings by cyanogen (Steve Kondik)
 * 
 * Released under the Apache 2.0 license
 */
package com.piratemedia.lockscreen;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;

public class LockscreenSettings extends PreferenceActivity {

	static final String KEY_MUSIC_PLAYER = "music_player_select";

	static final String KEY_FULLSCREEN = "fullscreen";
	
	static final String KEY_SHOW_ART = "albumart";
	
	static final String KEY_PICK_BG = "bg_picker";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.lockscreensettings);
        
        PreferenceScreen screen = this.getPreferenceScreen();
        Preference pick = (Preference) screen.findPreference(KEY_PICK_BG);
        
        pick.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
            	pickImage();
            	return true;
        	}
            });
        
	}

	private void pickImage() {
		int requestCode = 1;
		Intent i = new Intent(Intent.ACTION_PICK);

		i.setData(android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI);
		startActivityForResult(i, requestCode);
	}
}