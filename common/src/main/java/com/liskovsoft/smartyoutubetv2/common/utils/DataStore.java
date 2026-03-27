package com.liskovsoft.smartyoutubetv2.common.utils;

import android.content.Context;
import android.content.SharedPreferences;

import javax.annotation.Nullable;

import com.liskovsoft.smartyoutubetv2.common.utils.Serialize;

public class DataStore {

    private SharedPreferences mPrefs;

    public DataStore(String name) {
        
        mPrefs = getSharedPreferences(name, Context.MODE_PRIVATE);
    
    }

    public Object get(
        Integer key, 
        Object defval
    ) {

        String hex = mPrefs.getString(key, null);

        if (hex == null) {
            return defval;
        } else {
            return Serialize.decode(hex);
        }

    }

    public boolean getBool(Integer key, boolean defval) {

        return (boolean) get(key, defval);

    }

    public Object get(Integer key) {
        return get(key, null);
    } 

    public void put(
        Integer x, 
        Object value
    ) {

        String key = x.toString();

        String hex = Serialize.encode(value);

        mPrefs.edit()
            .putString(key, hex)
            .apply();

    }

}