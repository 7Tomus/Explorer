package sk.tomus.explorer;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import sk.tomus.explorer.miscellaneous.App;

public class DirectoryManager {

    public static DirectoryManager instance = null;
    private File currentDirectory;
    private Stack<File> navigationHistory;

    private DirectoryManager(){
        navigationHistory = new Stack<>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(App.instance.getCurrentActivity());
        String defaultFolder;
        if(android.os.Build.VERSION.SDK_INT > 24){
            defaultFolder = Environment.getRootDirectory().getAbsolutePath();
        }else{
            defaultFolder = "/";
        }
        currentDirectory = new File(prefs.getString("PREFERENCE_EDIT_DEF_FOLDER", defaultFolder));
    }

    public static DirectoryManager getInstance(){
        if (instance == null){
            instance = new DirectoryManager();
        }
        return instance;
    }

    public File getCurrentDirectory() {
        return currentDirectory;
    }

    public void setCurrentDirectory(File currentDirectory) {
        this.currentDirectory = currentDirectory;
    }

    public boolean hasPreviousDirectory(){
        return !navigationHistory.isEmpty();
    }

    public File getPreviousDirectory(){
        return navigationHistory.pop();
    }

    public void setPreviousDirectory(File f){
        navigationHistory.add(f);
    }

    public List<File> getAllFiles(File f){
        File[] allFiles = f.listFiles();
        List<File> directories = new ArrayList<>();
        List<File> files = new ArrayList<>();

        for (File file : allFiles) {
            if (file.isDirectory()) {
                directories.add(file);
            } else {
                files.add(file);
            }
        }

        Collections.sort(directories);
        Collections.sort(files);
        directories.addAll(files);

        return directories;
    }
}
