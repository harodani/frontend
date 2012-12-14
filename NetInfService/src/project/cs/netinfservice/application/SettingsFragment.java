/**
 * Copyright 2012 Ericsson, Uppsala University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Uppsala University
 *
 * Project CS course, Fall 2012
 *
 * Projekt DV/Project CS, is a course in which the students develop software for
 * distributed systems. The aim of the course is to give insights into how a big
 * project is run (from planning to realization), how to construct a complex
 * distributed system and to give hands-on experience on modern construction
 * principles and programming methods.
 *
 */
package project.cs.netinfservice.application;

import project.cs.netinfservice.R;
import project.cs.netinfservice.log.NetInfLog;
import project.cs.netinfutilities.UProperties;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * Load the settings to the application.
 *
 * @author Harold Martinez
 * @author Linus Sunde
 *
 */
public class SettingsFragment extends PreferenceFragment
implements OnSharedPreferenceChangeListener {
    /** Key for accessing the NRS IP. */
    private static final String PREF_KEY_NRS_IP = "pref_key_nrs_ip";

    /** Key for accessing the NRS Port. */
    private static final String PREF_KEY_NRS_PORT = "pref_key_nrs_port";

    /**
     * Creates settings fragment, initializing global preferences.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get shared preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        // Set default NRS address
        if (!prefs.contains(PREF_KEY_NRS_IP)) {
            // Edit on the go
            Editor editor = prefs.edit();
            editor.putString(
                    PREF_KEY_NRS_IP,
                    UProperties.INSTANCE.getPropertyWithName("nrs.http.host"));
            editor.commit();
        }

        // Set default NRS port
        if (!prefs.contains(PREF_KEY_NRS_PORT)) {
            Editor editor = prefs.edit();
            editor.putString(
                    PREF_KEY_NRS_PORT,
                    UProperties.INSTANCE.getPropertyWithName("nrs.http.port"));
            editor.commit();
        }

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        // Clear database
        findPreference("pref_key_clear_database").setOnPreferenceClickListener(
                new OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        // DB Delete
                        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                        .setTitle("Do you really want to delete the database?")
                        .setCancelable(false)
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                getActivity().deleteDatabase("IODatabase");
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                        dialog.show();
                        return false;
                    }
                });

        // Set default summaries
        findPreference(PREF_KEY_NRS_IP).setSummary(prefs.getString(PREF_KEY_NRS_IP, ""));
        findPreference(PREF_KEY_NRS_PORT).setSummary(prefs.getString(PREF_KEY_NRS_PORT, ""));

        // Clear Log
        findPreference("pref_key_clear_log").setOnPreferenceClickListener(
                new OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        // DB Delete
                        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                        .setTitle("Do you really want to delete the log?")
                        .setCancelable(false)
                        .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                NetInfLog.deleteLog();
                                NetInfLog.clearLog();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .create();
                        dialog.show();
                        return false;
                    }
                });

    }

    /**
     * Get changes to NRS IP/PORT.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        if (key.equals(PREF_KEY_NRS_IP) || key.equals(PREF_KEY_NRS_PORT))  {
            findPreference(key).setSummary(sharedPreferences.getString(key, ""));
        }
    }

    /**
     * OnResume handler.
     */
    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
        .registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * OnPause handler.
     */
    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
        .unregisterOnSharedPreferenceChangeListener(this);
    }
}
