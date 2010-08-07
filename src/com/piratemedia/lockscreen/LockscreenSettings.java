/**
 * Android Music Settings by cyanogen (Steve Kondik)
 * 
 * Released under the Apache 2.0 license
 */
package com.piratemedia.lockscreen;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class LockscreenSettings extends PreferenceActivity {

	static final String KEY_MUSIC_PLAYER = "music_player_select";

	static final String KEY_FULLSCREEN = "fullscreen";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.lockscreensettings);
	}

}