package com.gianxd.audiodev.util;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class MusicDevUtil {



		public static boolean isConnected(Context a) {
				ConnectivityManager connectivityManager = (android.net.ConnectivityManager) 
						a.getSystemService(Activity.CONNECTIVITY_SERVICE);
				NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
				return activeNetworkInfo != null && activeNetworkInfo.isConnected();
		}

		public static String copyFromInputStream(InputStream inputStream) {
				ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				byte buf[] = new byte[1024];
				int i;
				try {
						while ((i = inputStream.read(buf)) != -1){
								outputStream.write(buf, 0, i);
						}
						outputStream.close();
						inputStream.close();
				} catch (IOException e) {

				}
				return outputStream.toString();
		}

	public static void hideKeyboard(Context c){
		InputMethodManager imm = (InputMethodManager) 
		c.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
	}
	
	public static void showKeyboard(Context c){
		InputMethodManager imm = (InputMethodManager)
		c.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
	}
	


		public static int getLocationX(View _view) {
				int _location[] = new int[2];
				_view.getLocationInWindow(_location);
				return _location[0];
		}

		public static int getLocationY(View _view) {
				int _location[] = new int[2];
				_view.getLocationInWindow(_location);
				return _location[1];
		}

		public static int getRandom(int _min, int _max) {
				Random random = new Random();
				return random.nextInt(_max - _min + 1) + _min;
		}

		public static ArrayList<Double> getCheckedItemPositionsToArray(ListView _list) {
				ArrayList<Double> _result = new ArrayList<Double>();
				SparseBooleanArray _arr = _list.getCheckedItemPositions();
				for (int _iIdx = 0; _iIdx < _arr.size(); _iIdx++) {
						if (_arr.valueAt(_iIdx))
								_result.add((double) _arr.keyAt(_iIdx));
				}
				return _result;
		}

		public static float getDip(Context _context, int _input) {
				return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, _input, _context.getResources().getDisplayMetrics());
		}

		public static int getDisplayWidthPixels(Context _context) {
				return _context.getResources().getDisplayMetrics().widthPixels;
		}

		public static int getDisplayHeightPixels(Context _context) {
				return _context.getResources().getDisplayMetrics().heightPixels;
		}

		public static void getAllKeysFromMap(Map<String, Object> map, ArrayList<String> output) {
				if (output == null) return;
				output.clear();

				if (map == null || map.size() <= 0) return;

				Iterator itr = map.entrySet().iterator();
				while (itr.hasNext()) {
						Map.Entry<String, String> entry = (Map.Entry) itr.next();
						output.add(entry.getKey());
				}
		}
}