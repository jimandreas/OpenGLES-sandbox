package com.jimandreas.opengl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;

import android.net.Uri;
import android.os.Bundle;

import android.support.design.widget.FloatingActionButton;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter;

import com.jimandreas.opengl.displayobjfile.ActivityDisplayObjFile;
import com.jimandreas.opengl.displayscaled.ActivityDisplayScaled;
import com.jimandreas.opengl.displayobjects.ActivityDisplayObjects;

import timber.log.Timber;

public class TableOfContents extends ListActivity 
{
	private static final String ITEM_IMAGE = "item_image";
	private static final String ITEM_TITLE = "item_title";
	private static final String ITEM_SUBTITLE = "item_subtitle";	
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		if (savedInstanceState==null && BuildConfig.DEBUG) {
			Timber.plant(new Timber.DebugTree());
		}

		setTitle(R.string.toc);
		setContentView(R.layout.table_of_contents);
		
		// Initialize data
		final List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		final SparseArray<Class<? extends Activity>> activityMapping = new SparseArray<Class<? extends Activity>>();
		
		int i = 0;

		{
			final Map<String, Object> item = new HashMap<String, Object>();
			item.put(ITEM_IMAGE, R.drawable.display_objs);
			item.put(ITEM_TITLE, getText(R.string.objects_title));
			item.put(ITEM_SUBTITLE, getText(R.string.objects_subtitle));
			data.add(item);
			activityMapping.put(i++, ActivityDisplayObjects.class);
		}

		{
			final Map<String, Object> item = new HashMap<String, Object>();
			item.put(ITEM_IMAGE, R.drawable.cow);
			item.put(ITEM_TITLE, getText(R.string.activity_load_obj_file_title));
			item.put(ITEM_SUBTITLE, getText(R.string.activity_load_obj_file_subtitle));
			data.add(item);
			activityMapping.put(i++, ActivityDisplayObjFile.class);
		}

		{
			final Map<String, Object> item = new HashMap<String, Object>();
			item.put(ITEM_IMAGE, R.drawable.more_tris);
			item.put(ITEM_TITLE, getText(R.string.objects_multiple_title));
			item.put(ITEM_SUBTITLE, getText(R.string.objects_multiple_subtitle));
			data.add(item);
			activityMapping.put(i++, ActivityDisplayScaled.class);
		}

		final SimpleAdapter dataAdapter = new SimpleAdapter(
                this,
                data,
                R.layout.toc_item,
                new String[] {ITEM_IMAGE, ITEM_TITLE, ITEM_SUBTITLE},
                new int[] {R.id.Image, R.id.Title, R.id.SubTitle});

		setListAdapter(dataAdapter);	
		
		getListView().setOnItemClickListener(new OnItemClickListener() 
		{
			@Override
			 public void onItemClick(AdapterView<?> parent, View view,
				        int position, long id) 
			{
				final Class<? extends Activity> activityToLaunch = activityMapping.get(position);
				
				if (activityToLaunch != null)
				{
					final Intent launchIntent = new Intent(TableOfContents.this, activityToLaunch);
					startActivity(launchIntent);
				}				
			}
		});

		/*
         * FAB button (floating action button = FAB) to get more information
         *    Vector to the website for more info
         *       Snackbar popup ommitted due to versionitis.
         */
		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.info_fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

			Timber.i("FAB clicked");
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(
					"https://github.com/jimandreas/OpenGLES-sandbox"
					));
			startActivity(intent);

			}
		});
	}	
}
