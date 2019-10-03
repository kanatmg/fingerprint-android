package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Session {
    private SharedPreferences prefs;

    public Session(Context cntx) {
        // TODO Auto-generated constructor stub
        prefs = PreferenceManager.getDefaultSharedPreferences(cntx);
    }

    public void setOrganizationId(String organizationId) {
        prefs.edit().putString("organizationId", organizationId).commit();
    }

    public String getOrganizationId() {
        String organizationId = prefs.getString("organizationId","");
        return organizationId;
    }
    public void setOrganizationName(String organizationName) {
        prefs.edit().putString("organizationName", organizationName).commit();
    }

    public String getOrganizationName() {
        String organizationName = prefs.getString("organizationName","");
        return organizationName;
    }
    public void setLoggedIn(boolean loggedIn){
        prefs.edit().putBoolean("logged_in_status", loggedIn).commit();
    }
    public boolean getLoggedStatus(){
        return prefs.getBoolean("logged_in_status",false);
    }
}
