package com.liskovsoft.smartyoutubetv2.common.utils;

import com.liskovsoft.smartyoutubetv2.common.utils.Serializer;
import com.liskovsoft.sharedutils.helpers.Helpers;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.io.Serializable;

import javax.annotation.Nullable;

public class DataStore extends Fragment {

    private SharedPreferences mPrefs;

    public DataStore(String name) {

        mPrefs = getContext().getSharedPreferences(name, Context.MODE_PRIVATE);
    
    }

    //====================================
    // GET

    public <T> T get(
        Integer x, 
        T def
    ) {

        String key = x.toString();
        
        String raw = mPrefs.getString(key, null);

        if (raw == null) {
            return def;
        } else {
            return (T) Serializer.decode(raw);
        }
    
    }

    public <T> T get(
        Integer x
    ) {
        return get(x, null);
    }

    //====================================
    // PUT

    public void put(
        Integer x, 
        Serializable value
    ) {

        String key = x.toString();
        String val = Serializer.encode(value);

        mPrefs.edit()
            .putString(key, val)
            .apply();

    }

    //====================================

}