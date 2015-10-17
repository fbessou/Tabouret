package com.fbessou.sofa;

public class Log {
	public static int FLAG_ERROR = 0b1, FLAG_WARNING = 0b10, FLAG_INFO = 0b100, FLAG_VERBOSE = 0b1000, FLAG_DEBUG = 0b10000;
	public static int FILTER_NOTHING = 0b11111;
	public static int FILTER_LEVEL1 = 0b11101;
	public static int FILTER_LEVEL2 = 0b11001;
	public static int FILTER_LEVEL3 = 0b10001;
	public static int FILTER_ALL = 0b00000;
	private static int filter = FILTER_LEVEL2;
	private static LogListener listener;
	
	/** Sets the filter to define which log message should be shown. Default value: FILTER_LEVEL1  */
	public static void setLogFilterMask(int filterMask) {
		filter = filterMask & FILTER_NOTHING;
	}

	/** Send a ERROR log message. **/
	public static void e(String tag, String msg) {
		if((filter&FLAG_ERROR) != 0) {
			if(listener != null)
				listener.log("E", tag, msg);
			android.util.Log.e(tag, msg);
		}
	}
	/** Send a WARNING log message. **/
	public static void w(String tag, String msg) {
		if((filter&FLAG_WARNING) != 0) {
			if(listener != null)
				listener.log("W", tag, msg);
			android.util.Log.w(tag, msg);
		}
	}
	/** Send a INFO log message. **/
	public static void i(String tag, String msg) {
		if((filter&FLAG_INFO) != 0) {
			if(listener != null)
				listener.log("I", tag, msg);
			android.util.Log.i(tag, msg);
		}
	}
	/** Send a VERBOSE log message. **/
	public static void v(String tag, String msg) {
		if((filter&FLAG_VERBOSE) != 0) {
			if(listener != null)
				listener.log("V", tag, msg);
			android.util.Log.v(tag, msg);
		}
	}
	/** Send a DEBUG log message. **/
	public static void d(String tag, String msg) {
		if((filter&FLAG_DEBUG) != 0) {
			if(listener != null)
				listener.log("D", tag, msg);
			android.util.Log.d(tag, msg);
		}
	}
	/** Send a ERROR log message and log the exception. **/
	public static void e(String tag, String msg, Exception e) {
		if((filter&FLAG_ERROR) != 0) {
			if(listener != null)
				listener.log("E", tag, msg);
			android.util.Log.e(tag, msg, e);
		}
	}
	/** Send a WARNING log message and log the exception. **/
	public static void w(String tag, String msg, Exception e) {
		if((filter&FLAG_WARNING) != 0) {
			if(listener != null)
				listener.log("W", tag, msg);
			android.util.Log.w(tag, msg, e);
		}
	}
	/** Send a INFO log message and log the exception. **/
	public static void i(String tag, String msg, Exception e) {
		if((filter&FLAG_INFO) != 0) {
			if(listener != null)
				listener.log("I", tag, msg);
			android.util.Log.i(tag, msg, e);
		}
	}
	/** Send a VERBOSE log message and log the exception. **/
	public static void v(String tag, String msg, Exception e) {
		if((filter&FLAG_VERBOSE) != 0) {
			if(listener != null)
				listener.log("V", tag, msg);
			android.util.Log.v(tag, msg, e);
		}
	}
	/** Send a DEBUG log message and log the exception. **/
	public static void d(String tag, String msg, Exception e) {
		if((filter&FLAG_DEBUG) != 0) {
			if(listener != null)
				listener.log("D", tag, msg);
			android.util.Log.d(tag, msg, e);
		}
	}

	public static void setLogListener(LogListener l) {
		listener = l;
	}
	
	public interface LogListener {
		void log(String log, String tag, String msg);
	}
}
