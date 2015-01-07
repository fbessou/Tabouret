package com.fbessou.tabouret;

import java.io.File;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Environment;

public class Configuration {
	Context mContext;
	SharedPreferences mPreferences;
	private Editor mEditor;
	private final static String APP_DIR = "app_directory";
	private final static String LAYOUT_DIR = "layout_directory";
	private final static String RES_DIR = "layout_directory";

	public Configuration(Context context) {
		//p.
		mContext=context;
		mPreferences = mContext.getSharedPreferences("TabouretConfig",Context.MODE_PRIVATE);
		mEditor = mPreferences.edit();
		init();
		boolean b = isFirstLaunch();
		
	}
	
	private void init() {
		//if(isFirstLaunch())
			reset();
	}
		
	public void reset(){
		mEditor.clear();
		mEditor.putString(APP_DIR, Environment.getExternalStorageDirectory().getAbsolutePath()+"/Tabouret");
		mEditor.commit();
		setLayoutDirectory("layouts");
		setResourcesDirectory("resources");
		mEditor.commit();
	}
	
	public String getAppDir(){
		return mPreferences.getString(APP_DIR, null);
	}
	
	public String getLayoutDirectory(){
		return mPreferences.getString(LAYOUT_DIR, null);
	}
	
	void setLayoutDirectory(String dirName){
		String dirPath = getAppDir()+"/"+dirName;
		mEditor.putString(LAYOUT_DIR, dirPath);
		File f = new File(dirPath);
		f.mkdirs();
	}
	
	public String getResourcesDirectory(){
		return mPreferences.getString(RES_DIR, null);
	}
	
	void setResourcesDirectory(String dirName){
		String dirPath = getAppDir()+"/"+dirName;
		mEditor.putString(RES_DIR, dirPath);
		File f = new File(dirPath);
		f.mkdirs();
	}
	
	boolean isFirstLaunch(){
		return mPreferences.getAll().isEmpty();
	}
	
	
	
}
