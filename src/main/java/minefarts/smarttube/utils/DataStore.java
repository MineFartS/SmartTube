package minefarts.smarttube.utils;

import minefarts.smarttube.utils.Serializer;
import minefarts.smarttube.utils.helpers.Helpers;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.io.Serializable;
import java.io.IOException;
import java.lang.ClassNotFoundException;

import javax.annotation.Nullable;

public class DataStore {

    private SharedPreferences mPrefs;

    public DataStore(
        Context context,
        String name
    ) {
        mPrefs = context.getSharedPreferences(name, Context.MODE_PRIVATE);
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
            
            try {
                return (T) Serializer.decode(raw);
            } catch (IOException|ClassNotFoundException e) {
                return null;
            }

        }
    
    }

    public <T> T get(Integer x) {
        return get(x, null);
    }

    //====================================
    // PUT

    public void put(
        Integer x, 
        Serializable value
    ) {

        String key = x.toString();

        String val;
        try {
            val = Serializer.encode(value);
        } catch (IOException e) {
            val = null;
        }

        mPrefs.edit()
            .putString(key, val)
            .apply();

    }

    //====================================

}