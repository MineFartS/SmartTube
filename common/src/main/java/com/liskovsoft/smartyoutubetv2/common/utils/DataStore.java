package com.liskovsoft.smartyoutubetv2.common.utils;

import com.liskovsoft.sharedutils.helpers.Helpers;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import javax.annotation.Nullable;

public class DataStore {

    private SharedPreferences mPrefs;

    public DataStore(String name) {
        mPrefs = getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    private String[] get(Integer x) {
        return mPrefs.getString(x.toString(), null);
    }

    //====================================

    public String getStr(Integer x, String def) {
        return Helpers.parseStr(get(x), def);
    }

    public String getStr(Integer x) {
        return getStr(x, null);
    }

    //====================================

    public boolean getBool(Integer x, boolean def) {
        return Helpers.parseBoolean(get(x), def);
    }

    public boolean getBool(Integer x) {
        return getBool(x, null);
    }

    //====================================

    public Integer getInt(Integer x, Integer def) {
        return Helpers.parseInt(get(x), def);
    }

    public Integer getInt(Integer x) {
        return getInt(x, null);
    }

    //====================================

    public Map getMap(Integer x, Map def) {

        Map<T, K> result = new HashMap<>();

        if (spec != null) {

            String[] listArr = splitArray(spec);

            for (String item : listArr) {
                //String[] keyValPair = item.split("\\|");
                String[] keyValPair = split(item, PAIR_DELIM);

                if (keyValPair.length != 2) {
                    continue;
                }

                result.put(keyParser.parse(keyValPair[0]), valueParser.parse(keyValPair[1]));
            }
        }

        return result;
    }

    //====================================

    public <T> List<T> getList(
        Integer x, 
        @Nullable List<T> def,
        Helpers.Parser<T> parser
    ) {

        String[] raw = get(x);

        List<T> result = new ArrayList<>();

        if (raw == null) {

            return def;

        } else {

            String[] listArr = Helpers.split(raw, Helpers.ARRAY_DELIM);

            for (String item : listArr) {

                T parsed = parser.parse(item);

                if (parsed != null) {
                    result.add(parsed);
                }

            }

        }

        return result;
    }

    public List<String> getStrList(Integer x, List<String> def) {
        getList(x, def, Helpers::parseStr);
    }

    public List<String> getStrList(Integer x) {
        return getStrList(x, null);
    }

    //====================================

    public void put(Integer x, Object value) {

        String key = x.toString();
        String val = value.toString();

        mPrefs.edit()
            .putString(key, val)
            .apply();

    }

}