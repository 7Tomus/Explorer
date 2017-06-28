package sk.tomus.explorer;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

public class MyPreferencesActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Log.i("SharedPrefChange","changed");
            if (key.equals("PREFERENCE_EDIT_DEF_FOLDER")){
                Preference editDirPref = findPreference(key);
                File existFile = new File(sharedPreferences.getString(key, ""));
                if(existFile.exists() && existFile.canRead()){
                    editDirPref.setSummary("Current: " + sharedPreferences.getString(key, ""));
                }else{
                    Toast.makeText(getActivity(),"Such directory does not exist or it is inaccessible",Toast.LENGTH_SHORT).show();
                    String defaultFolder;
                    if(android.os.Build.VERSION.SDK_INT > 24){
                        defaultFolder = Environment.getRootDirectory().getAbsolutePath();
                    }else{
                        defaultFolder = "/";
                    }
                    sharedPreferences.edit().putString("PREFERENCE_EDIT_DEF_FOLDER",defaultFolder).apply();
                    editDirPref.setSummary("Current: " + sharedPreferences.getString(key, ""));
                }
            }
        }

        @Override
        public void onResume(){
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
            Preference editDirPref = findPreference("PREFERENCE_EDIT_DEF_FOLDER");
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            editDirPref.setSummary("Current: " + prefs.getString("PREFERENCE_EDIT_DEF_FOLDER", ""));
        }

        @Override
        public void onPause(){
            super.onPause();
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }
    }
}
