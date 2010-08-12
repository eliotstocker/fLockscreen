package com.piratemedia.lockscreen;

import java.util.List;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.ListPreference;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

public class HomeChooserActivity extends ListActivity {
	private boolean mNeedlaunch=true;
	public HomeChooserActivity() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Intent intent=new Intent("android.intent.action.MAIN");
		intent.addCategory("android.intent.category.HOME");
		PackageManager pm=getPackageManager();
		List<ResolveInfo> homeApps=pm.queryIntentActivities(intent, 0);
		AppsAdapter adapter=new AppsAdapter(this, homeApps,this.getPackageName());
		this.setListAdapter(adapter);
		Bundle extras = getIntent().getExtras();
		if(extras!=null){
			mNeedlaunch=extras.getBoolean("loadOnClick", true);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		ResolveInfo tag=null;
		try{
			tag=(ResolveInfo) v.getTag();
		}catch (ClassCastException e) {
			tag=null;
		}
		if(tag!=null){
			//Store the preferred launcher inside preferences
			Toast t=Toast.makeText(this, tag.activityInfo.applicationInfo.className,Toast.LENGTH_LONG);
			t.show();
			// Build the intent for the chosen activity
	        Intent intent = new Intent();
	        intent.setComponent(new ComponentName(tag.activityInfo.applicationInfo.packageName,
	                tag.activityInfo.name));
	        //store it in preferences or launch it
	        if(mNeedlaunch){
	        	startActivity(intent);
	        	utils.setStringPref(this, LockscreenSettings.KEY_HOME_APP_PACKAGE, tag.activityInfo.applicationInfo.packageName);
	        	utils.setStringPref(this, LockscreenSettings.KEY_HOME_APP_ACTIVITY, tag.activityInfo.name);
	        }else{
	        	utils.setStringPref(this, LockscreenSettings.KEY_HOME_APP_PACKAGE, tag.activityInfo.applicationInfo.packageName);
	        	utils.setStringPref(this, LockscreenSettings.KEY_HOME_APP_ACTIVITY, tag.activityInfo.name);
	        	finish();
	        }
		}
	}

}
