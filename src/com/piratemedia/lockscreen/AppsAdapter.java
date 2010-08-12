package com.piratemedia.lockscreen;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AppsAdapter extends BaseAdapter {

    private final LayoutInflater mInflater;
    
    private final ArrayList<ListItem> mItems = new ArrayList<ListItem>();
    
    /**
     * Specific item in our list.
     */
    public class ListItem {
        public final CharSequence text;
        public final CharSequence activityname;
        public final Drawable image;
        public final ResolveInfo actionTag;
        
        public ListItem(CharSequence charSequence, CharSequence charSequence2, Drawable imageResource, ResolveInfo actionTag) {
            text = charSequence;
            activityname = charSequence2;
            image = imageResource;
            this.actionTag = actionTag;
        }
    }
    
    public AppsAdapter(Context context,  List<ResolveInfo> list, CharSequence ignore) {
        super();
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        final int count=list.size();
        final PackageManager pm=context.getPackageManager();
        for(int i=0;i<count;i++){
        	final ResolveInfo item=list.get(i);
        	if(!item.activityInfo.packageName.equals(ignore))
        		mItems.add(new ListItem(item.loadLabel(pm), item.activityInfo.packageName, item.loadIcon(pm),item));
        }

    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ListItem item = (ListItem) getItem(position);
        
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.add_list_item, parent, false);
        }
        
        LinearLayout mainlayout = (LinearLayout) convertView;
        //added some new stuff to make life a little easier :)
        TextView appName = (TextView) convertView.findViewById(R.id.appName);
        TextView activityName = (TextView) convertView.findViewById(R.id.activityName);
        ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
        
        mainlayout.setTag(item);
        appName.setText(item.text);
        activityName.setText(item.activityname);
        icon.setImageDrawable(item.image);
        convertView.setTag(item.actionTag);
        return convertView;
    }

    public int getCount() {
        return mItems.size();
    }

    public Object getItem(int position) {
        return mItems.get(position);
    }

    public long getItemId(int position) {
        return position;
    }
    
}

