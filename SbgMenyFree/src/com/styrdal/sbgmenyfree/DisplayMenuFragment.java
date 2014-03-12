package com.styrdal.sbgmenyfree;


import java.io.IOException;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.styrdal.sbgmenyfree.ItemsContract.ItemsEntry;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class DisplayMenuFragment extends ListFragment {
	
	protected static final String TAG = "DisplayMenuFragment";
	public final static String EXTRA_MESSAGE = "com.styrdal.SbgMeny.MESSAGE";	
	private String dbName = "restauranger.db";
	private SQLiteDatabase db;

	private String idname;
	private Cursor c;
	private View myFragment;
	
	private AdView adView;
	private String adUnitId = "ca-app-pub-6095611948758304/4308520274";

	//Enabling options menu and context menu
	@Override
	public void onActivityCreated(Bundle savedState)
	{
		super.onActivityCreated(savedState);
		setHasOptionsMenu(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		myFragment = inflater.inflate(R.layout.activity_display_menu_fragment, container, false);
		
		adView = (AdView) myFragment.findViewById(R.id.menu_adView);
		
		AdRequest adRequest = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).addTestDevice("F612D0D385B3CF6780BF9DC4CE1F0451").build();
		adView.loadAd(adRequest);
		
		
		SharedPreferences setting = getActivity().getSharedPreferences(MainActivity.PREFS_NAME, 0);
		idname = setting.getString(MainActivity.EXTRA_MESSAGE, null);
		
		Log.i(TAG, "idname: " + idname);
		
		RestaurantsDBHelper dbHelper = null;
		
		try 
		{
			dbHelper = new RestaurantsDBHelper(getActivity(), dbName, dbName);
		}
		catch (IOException e)
		{
			Log.e(TAG, e.toString());
		}
		db = dbHelper.getWritableDatabase();
		
		Cursor c = createCursor(db, getActivity().getIntent());
		
		String[] selection = {ItemsEntry.COLUMN_NAME_NAME, ItemsEntry.COLUMN_NAME_TOPPINGS, ItemsEntry.COLUMN_NAME_PRICE, ItemsEntry.COLUMN_NAME_NUMBER, ItemsEntry.COLUMN_NAME_EXTRA};
		int[] displays =  {R.id.menu_name, R.id.menu_toppings, R.id.menu_price, R.id.menu_number, R.id.menu_extra};
		
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(), R.layout.menu_list, c, selection, displays, 0);
		
		setListAdapter(adapter);
		
		return myFragment;
	}
	
	//Checking if there is a search query
	private boolean isSearched()
	{
		Intent intent = getActivity().getIntent();
		if (Intent.ACTION_SEARCH.equals(intent.getAction()))
		{
			Log.i(TAG, "Activity is searched!");
			return true;
		}
		else
		{
			return false;
		}
	}
	
	//Creating the cursor from the database based on if there is a search query or not
	private Cursor createCursor(SQLiteDatabase db, Intent intent)
	{
		String[] cursorProjection = {ItemsEntry._ID,
				ItemsEntry.COLUMN_NAME_NAME, 
				ItemsEntry.COLUMN_NAME_NUMBER, 
				ItemsEntry.COLUMN_NAME_TOPPINGS, 
				ItemsEntry.COLUMN_NAME_EXTRA, 
				ItemsEntry.COLUMN_NAME_PRICE, 
				ItemsEntry.COLUMN_NAME_ALTPRICE};
		String sortOrder = ItemsEntry._ID + " ASC";
		String tableName = idname;
		
		
		if (isSearched())
		{
			String search = intent.getStringExtra(SearchManager.QUERY);
			String selection = ItemsEntry.COLUMN_NAME_NAME + " LIKE '%" + search + "%' OR " + ItemsEntry.COLUMN_NAME_TOPPINGS + " LIKE '%" + search + "%'";
			
			c = db.query(tableName,
					cursorProjection,
					selection,
					null,
					null,
					null,
					sortOrder);
		}
		else
		{
			c = db.query(tableName,
					cursorProjection,
					null,
					null,
					null,
					null,
					sortOrder);
			
		}
		return c;
	}
}
