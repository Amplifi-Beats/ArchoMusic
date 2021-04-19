package com.gianxd.audiodev.util;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Objects;

public class ListUtil {

    public static void sortArrayList(ArrayList<HashMap<String, Object>> list, String key, boolean isNumber, boolean isAscending) {
        Collections.sort(list, new Comparator<HashMap<String,Object>>(){
            public int compare(HashMap<String,Object> compareMap1, HashMap<String,Object> compareMap2){
                if (isNumber) {
                    int count1 = Integer.parseInt(compareMap1.get(key).toString());
                    int count2 = Integer.parseInt(compareMap2.get(key).toString());
                    if (isAscending) {
                        return count1 < count2 ? -1 : count1 < count2 ? 1 : 0;
                    } else {
                        return count1 > count2 ? -1 : count1 > count2 ? 1 : 0;
                    }
                } else {
                    if (isAscending) {
                        return (compareMap1.get(key).toString()).compareTo(compareMap2.get(key).toString());
                    } else {
                        return (compareMap2.get(key).toString()).compareTo(compareMap1.get(key).toString());
                    }
                }
            }
        });
    }

    public static ArrayList<HashMap<String, Object>> getArrayListFromSharedJSON(SharedPreferences sharedPreferences, String key) {
        return new Gson().fromJson(sharedPreferences.getString(key, ""), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
    }

    public static HashMap<String, Object> getHashMapFromSharedJSON(SharedPreferences sharedPreferences, String key) {
        return new Gson().fromJson(sharedPreferences.getString(key, ""), new TypeToken<HashMap<String, Object>>(){}.getType());
    }

    public static String setArrayListToSharedJSON(ArrayList<HashMap<String, Object>> arrayList) {
        return new Gson().toJson(arrayList);
    }

    public static String setHashMapToSharedJSON(HashMap<String, Object> hashMap) {
        return new Gson().toJson(hashMap);
    }

    public static ArrayList<HashMap<String, Object>> getArrayListFromFile(String path) {
        return new Gson().fromJson(FileUtil.readFile(path), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
    }

    public static HashMap<String, Object> getHashMapFromFile(String path) {
        return new Gson().fromJson(FileUtil.readFile(path), new TypeToken<HashMap<String, Object>>(){}.getType());
    }

}
