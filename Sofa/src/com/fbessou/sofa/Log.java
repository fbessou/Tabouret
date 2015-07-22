package com.fbessou.sofa;

public class Log {
	public static int FLAG_ERROR = 0b1, FLAG_WARNING = 0b10, FLAG_INFO = 0b100, FLAG_VERBOSE = 0b1000, FLAG_DEBUG = 0b10000;
	public static int FILTER_NOTHING = 0b11111;
	public static int FILTER_LEVEL1 = 0b11101;
	public static int FILTER_LEVEL2 = 0b11001;
	public static int FILTER_LEVEL3 = 0b10001;
	public static int FILTER_ALL = 0b00000;
	private static int filter = FILTER_NOTHING;
	
	public static void setLogFilterMask(int filterMask) {
		filter = filterMask & FILTER_NOTHING;
	}
	
	public static void e(String tag, String msg) {
		if((filter&FLAG_ERROR) != 0)
			android.util.Log.e(tag, msg);
	}
	
	public static void w(String tag, String msg) {
		if((filter&FLAG_WARNING) != 0)
			android.util.Log.w(tag, msg);
	}
	
	public static void i(String tag, String msg) {
		if((filter&FLAG_INFO) != 0)
			android.util.Log.i(tag, msg);
	}
	
	public static void v(String tag, String msg) {
		if((filter&FLAG_VERBOSE) != 0)
			android.util.Log.v(tag, msg);
	}
	
	public static void d(String tag, String msg) {
		if((filter&FLAG_DEBUG) != 0)
			android.util.Log.d(tag, msg);
	}
}
