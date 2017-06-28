package sk.tomus.explorer;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.MimeTypeMap;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import sk.tomus.explorer.adapters.FileListAdapter;
import sk.tomus.explorer.miscellaneous.App;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<File>>, FileListAdapter.FileViewHolder.ClickListener {

    private RecyclerView recyclerView;
    private FileListAdapter fileListAdapter;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;
    private List<File> fileList = new ArrayList<>();
    private ActionModeCallback actionModeCallback = new ActionModeCallback();
    private ActionMode actionMode;
    private android.support.v4.content.AsyncTaskLoader<List<File>> fileLoader;
    private boolean initAnim = true;
    private int rows;
    boolean okToDelete = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = (RecyclerView) findViewById(R.id.main_recycler_view);
        float screenHeight,screenWidth,toolbarHeight;
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);
        float density = getResources().getDisplayMetrics().density;
        screenWidth = outMetrics.widthPixels / density;
        screenHeight = outMetrics.heightPixels / density;
        TypedValue tv = new TypedValue();
        toolbarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());

        //zistí orientáciu a vypočíta počet zobrazených položiek na obrazovke - používa sa na fade animáciu
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            initAnim = true;
            rows = (int)(screenHeight-toolbarHeight)/61;
            recyclerViewLayoutManager = new LinearLayoutManager(this);
            recyclerView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorWhiteTotal));
        } else {
            initAnim = true;
            int columns = Math.round(screenWidth / 160);
            rows = ((int)(screenHeight-toolbarHeight)/98)*columns;
            recyclerViewLayoutManager = new GridLayoutManager(this, columns);
            recyclerView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorGrayLight));
        }

        recyclerView.setLayoutManager(recyclerViewLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        fileListAdapter = new FileListAdapter(fileList, this);
        recyclerView.setAdapter(fileListAdapter);
        getSupportActionBar().setSubtitle(DirectoryManager.getInstance().getCurrentDirectory().getAbsolutePath());
        getSupportLoaderManager().initLoader(0, null, this);
    }

    public int getRows() {
        return rows;
    }

    public void setInitAnim(boolean initAnim) {
        this.initAnim = initAnim;
    }

    public boolean getInitAnim(){
        return initAnim;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_normal, menu);
        return true;
    }

    @Override
    public void onItemClicked(int position) {
        if (actionMode != null) {
            toggleSelection(position);
        } else {
            onFileClicked(fileListAdapter.getFile(position));
        }

    }

    @Override
    public boolean onItemLongClicked(int position) {
        if (actionMode == null) {
            actionMode = startSupportActionMode(actionModeCallback);
        }
        toggleSelection(position);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_refresh:
                load();
                Toast.makeText(this, "Refreshed", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.menu_settings:
                Intent i = new Intent(this, MyPreferencesActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    private void toggleSelection(int position) {
        fileListAdapter.toggleSelection(position);
        int count = fileListAdapter.getSelectedItemCount();

        if (count == 0) {
            actionMode.finish();
        } else {
            actionMode.setTitle(String.valueOf(count));
            actionMode.invalidate();
        }
    }

    @Override
    public Loader<List<File>> onCreateLoader(int i, Bundle bundle) {
        fileLoader = new android.support.v4.content.AsyncTaskLoader<List<File>>(this) {

            @Override
            public void onStartLoading() {
                forceLoad();
            }

            @Override
            public List<File> loadInBackground() {
                Log.i("loader loading:", DirectoryManager.getInstance().getCurrentDirectory().toString());
                if(DirectoryManager.getInstance().getCurrentDirectory() == null){
                    return DirectoryManager.getInstance().getAllFiles(DirectoryManager.getInstance().getCurrentDirectory());
                }
                return DirectoryManager.getInstance().getAllFiles(DirectoryManager.getInstance().getCurrentDirectory());
            }
        };
        return fileLoader;
    }

    @Override
    public void onLoadFinished(Loader<List<File>> loader, List<File> data) {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            LinearLayoutManager layoutManager = (LinearLayoutManager)recyclerView.getLayoutManager();
            layoutManager.scrollToPositionWithOffset(0, 0);
        }else{
            GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
            layoutManager.scrollToPositionWithOffset(0, 0);
        }
        initAnim = true;
        fileListAdapter.setFiles(data);
        fileListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<List<File>> loader) {
    }


    private class ActionModeCallback implements ActionMode.Callback {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_action, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.menu_delete:
                    Log.d("TEST", "menu_remove");

                    AlertDialog.Builder builder = new AlertDialog.Builder(App.instance.getCurrentActivity());
                    builder.setTitle("Delete?")
                            .setMessage("Are you sure?")
                            .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    int length = fileListAdapter.getItemCount();
                                    List<Integer> selectedItems = fileListAdapter.getSelectedItems();
                                    for(int i = length; i>0; i--){
                                        if(selectedItems.contains(i)){

                                            File toDelFile = fileListAdapter.getFile(i);
                                            okToDelete = true;
                                            checkAccessRecursive(toDelFile);
                                            if(okToDelete){
                                                deleteRecursive(toDelFile);
                                            }else{
                                                Toast.makeText(App.instance.getCurrentActivity(),"Cannot delete this file",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                    load();

                                    mode.finish();
                                }
                            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                    }).show();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            fileListAdapter.clearSelection();
            actionMode = null;
        }
    }

    //------------------------------------

    private void checkAccessRecursive(File fileOrDirectory){
        if (fileOrDirectory.isDirectory() && fileOrDirectory.canWrite()) {
            for (File child : fileOrDirectory.listFiles())
                checkAccessRecursive(child);
        }else {
            okToDelete = false;
        }
        File file = new File(fileOrDirectory.getPath());
        if(okToDelete) {
            okToDelete = file.canWrite();
        }
    }

    private void deleteRecursive(File fileOrDirectory) {

        String fileName = fileOrDirectory.getName();
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        File file = new File(fileOrDirectory.getPath());
        boolean deleted = file.delete();
        Log.i("deleted?" + fileName, String.valueOf(deleted));
    }

    public String getMimeType(Uri uri) {
        String mimeType = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(uri.getPath());
        if (MimeTypeMap.getSingleton().hasExtension(extension)) {
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return mimeType;
    }

    private void onFileClicked(File file) {
        if (file.isDirectory()) {
            Log.i("current directory", file.getAbsolutePath());
            if (!file.canRead()) {
                Toast.makeText(getApplicationContext(), "inaccessible", Toast.LENGTH_SHORT).show();
                return;
            }
            DirectoryManager.instance.setPreviousDirectory(DirectoryManager.instance.getCurrentDirectory());
            DirectoryManager.instance.setCurrentDirectory(file);
            getSupportActionBar().setSubtitle(DirectoryManager.getInstance().getCurrentDirectory().getAbsolutePath());
            load();

        } else {
            openFile(Uri.fromFile(file));
        }
    }

    private void openFile(Uri fileUri) {
        String mimeType = getMimeType(fileUri);

        if (mimeType != null) {
            try {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setDataAndType(fileUri, mimeType);
                startActivity(i);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, "file type recognized, but no apps to open it", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, "unknown file type", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (DirectoryManager.getInstance().hasPreviousDirectory()) {
            DirectoryManager.getInstance().setCurrentDirectory(DirectoryManager.getInstance().getPreviousDirectory());
            getSupportActionBar().setSubtitle(DirectoryManager.getInstance().getCurrentDirectory().getAbsolutePath());
            load();
        } else {
            if (!DirectoryManager.getInstance().getCurrentDirectory().equals(new File("/"))) {
                DirectoryManager.getInstance().setCurrentDirectory(new File(DirectoryManager.getInstance().getCurrentDirectory().getParent()));
                getSupportActionBar().setSubtitle(DirectoryManager.getInstance().getCurrentDirectory().getAbsolutePath());
                load();
            }
        }
    }

    public void load() {
        if (fileLoader != null) {
            fileLoader.onContentChanged();
        } else {
            getSupportLoaderManager().restartLoader(0, null, this);
        }
    }
}
