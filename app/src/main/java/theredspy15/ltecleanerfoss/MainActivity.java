/*
 *  Copyright 2019 TheRedSpy15
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package theredspy15.ltecleanerfoss;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.transition.TransitionManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;

import com.fxn.stash.Stash;
import com.sdsmdg.tastytoast.TastyToast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    static List<String> whiteList = new ArrayList<>();
    static ArrayList<String> filters = new ArrayList<>();
    List<File> foundFiles = new ArrayList<>();
    int filesRemoved = 0;
    int kilobytesTotal = 0;
    static boolean delete = false;
    static private Resources resources;
    ConstraintSet constraintSet = new ConstraintSet();
    SharedPreferences prefs;

    LinearLayout fileListView;
    ScrollView fileScrollView;
    ProgressBar scanPBar;
    TextView progressText;
    TextView statusText;
    ConstraintLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Stash.init(getApplicationContext());
        SafeLooper.install();

        fileListView = findViewById(R.id.fileListView);
        fileScrollView = findViewById(R.id.fileScrollView);
        scanPBar = findViewById(R.id.scanProgress);
        progressText = findViewById(R.id.ScanTextView);
        statusText = findViewById(R.id.statusTextView);
        layout = findViewById(R.id.main_layout);

        resources = getResources();
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        constraintSet.clone(layout);

        requestWriteExternalPermission();
    }

    /**
     * Starts the settings activity
     * @param view the view that is clicked
     */
    public final void settings(View view) {

        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    /**
     * Runs search and delete on background thread
     */
    public final void clean(View view) {

        if (!prefs.getBoolean("one_click", false)) // one-click disabled
            new AlertDialog.Builder(this,R.style.MyAlertDialogTheme)
                    .setTitle(R.string.select_task)
                    .setMessage(R.string.do_you_want_to)
                    .setPositiveButton(R.string.clean, (dialog, whichButton) -> { // clean
                        delete = true;
                        new Thread(this::scan).start();
                    })
                    .setNegativeButton(R.string.analyze, (dialog, whichButton) -> { // analyze
                        delete = false;
                        new Thread(this::scan).start();
                    }).show();
        else { // one-click enabled
            reset();
            delete = true; // clean
            new Thread(this::scan).start();
        }
    }

    public void animateBtn() {

        TransitionManager.beginDelayedTransition(layout);
        constraintSet.clear(R.id.cleanButton,ConstraintSet.TOP);
        constraintSet.setMargin(R.id.statusTextView,ConstraintSet.TOP,10);
        constraintSet.applyTo(layout);
    }

    /**
     * Searches entire device, adds all files to a list, then a for each loop filters
     * out files for deletion. Repeats the process as long as it keeps finding files to clean,
     * unless nothing is found to begin with
     */
    @SuppressLint("SetTextI18n")
    private void scan() {

        Looper.prepare();
        reset();
        whiteList = Stash.getArrayList("whiteList",String.class);
        setUpFilter(prefs.getBoolean("generic", true), prefs.getBoolean("aggressive", false), prefs.getBoolean("apk", false));

        runOnUiThread(() -> {
            animateBtn();
            statusText.setText(getString(R.string.status_running));
        });

        byte cycles = 0;
        byte maxCycles = 10;
        if (!delete) maxCycles = 1; // when nothing is being deleted. Stops duplicates from being found

        // removes the need to 'clean' multiple times to get everything
        while (cycles < maxCycles) {

            // find files
            String path = Environment.getExternalStorageDirectory().toString() + "/"; // just a forward slash for whole device
            foundFiles = getListFiles(new File(path));
            scanPBar.setMax(scanPBar.getMax() + foundFiles.size());

            // scan
            for (File file : foundFiles) {
                if (filter(file)) displayPath(file); // filter

                // progress
                runOnUiThread(() -> {
                    scanPBar.setProgress(scanPBar.getProgress() + 1);
                    double scanPercent = scanPBar.getProgress() * 100.0 / scanPBar.getMax();
                    progressText.setText(String.format(Locale.US, "%.0f", scanPercent) + "%");
                });
            }

            if (filesRemoved == 0) break; // nothing found this run, no need to run again

            filesRemoved = 0; // reset for next cycle
            ++cycles;
        }

        if (kilobytesTotal == 0) {
            TastyToast.makeText(this, getString(R.string.nothing_found), TastyToast.LENGTH_LONG, TastyToast.SUCCESS).show();
        } else {
            // toast view with amount found/freed
            if (delete) TastyToast.makeText( // Clean toast
                    this, getString(R.string.freed) + " " + kilobytesTotal + getString(R.string.kb), TastyToast.LENGTH_LONG, TastyToast.SUCCESS).show();
            else TastyToast.makeText( // Analyze toast
                    this, getString(R.string.found) + " " + kilobytesTotal + getString(R.string.kb), TastyToast.LENGTH_LONG, TastyToast.SUCCESS).show();
        }

        runOnUiThread(() -> statusText.setText(getString(R.string.status_idle)));
        Looper.loop();
    }

    /**
     * Used to generate a list of all files on device
     * @param parentDirectory where to start searching from
     * @return List of all files on device (besides whitelisted ones)
     */
    private synchronized List<File> getListFiles(File parentDirectory) {

        ArrayList<File> inFiles = new ArrayList<>();
        File[] files = parentDirectory.listFiles();

        for (File file : files) {
            if (!isWhiteListed(file)){ // won't touch if whitelisted
                if (file.isDirectory()) { // folder

                    if (prefs.getBoolean("auto_white", true)) {
                        if (!autoWhiteList(file)) inFiles.addAll(getListFiles(file));
                    }
                    else {
                        inFiles.add(file);
                        inFiles.addAll(getListFiles(file)); // add contents to returned list
                    }

                } else inFiles.add(file); // add file
            }
        }

        return inFiles;
    }

    /**
     * Convenience method to quickly create a textview
     * @param color - color text color in textview
     * @param text - text of textview
     * @return - created textview
     */
    private synchronized TextView generateTextView(int color, String text) {

        TextView textView = new TextView(MainActivity.this);
        textView.setTextColor(getResources().getColor(color));
        textView.setText(text);
        textView.setPadding(3,3,3,3);
        return textView;
    }

    /**
     * Increments amount removed, then creates a text view to add to the scroll view.
     * If there is any error while deleting, turns text view of path red
     * @param file file to delete
     */
    private synchronized void displayPath(File file) {

        kilobytesTotal += Integer.parseInt(String.valueOf(file.length()/1024));

        // creating and adding a text view to the scroll view with path to file
        ++filesRemoved;
        TextView textView = generateTextView(R.color.colorAccent, file.getAbsolutePath());

        // adding to scroll view
        runOnUiThread(() -> fileListView.addView(textView));

        // scroll to bottom
        fileScrollView.post(() -> fileScrollView.fullScroll(ScrollView.FOCUS_DOWN));

        // deletion & error effect
        if (delete)
            if (!file.delete()) textView.setTextColor(Color.RED);
    }

    /**
     * Removes all views present in fileListView (linear view), and sets found and removed
     * files to 0
     */
    private synchronized void reset() {

        foundFiles.clear();
        filesRemoved = 0;
        kilobytesTotal = 0;
        resources = getResources();
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        runOnUiThread(() -> {
            fileListView.removeAllViews();
            scanPBar.setProgress(0);
            scanPBar.setMax(1);
        });
    }

    /**
     * Adds paths to the white list that are not to be cleaned. As well as adds
     * extensions to filter. 'generic', 'aggressive', and 'apk' should be assigned
     * by calling preferences.getBoolean()
     */
    @SuppressLint("ResourceType")
    synchronized static void setUpFilter(boolean generic, boolean aggressive, boolean apk) {

        // filters
        filters.clear();
        // generic
        if (generic)
            filters.addAll(Arrays.asList(resources.getStringArray(R.array.generic_filter_array)));
        // aggressive
        if (aggressive)
            filters.addAll(Arrays.asList(resources.getStringArray(R.array.aggressive_filter_array)));
        // apk
        if (apk) filters.add(".apk");
    }

    /**
     * Request write permission
     */
    public synchronized void requestWriteExternalPermission() {

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                1);
    }

    /**
     * Runs a for each loop through the white list, and compares the path of the file
     * to each path in the list
     * @param file file to check if in the whitelist
     * @return true if is the file is in the white list, false if not
     */
    private synchronized boolean isWhiteListed(File file) {

        for (String path : whiteList) if (path.equals(file.getAbsolutePath()) || path.equals(file.getName())) return true;

        return false;
    }

    /**
     * lists the contents of the file to an array, if the array length is 0, then return true,
     * else false
     * @param directory directory to test
     * @return true if empty, false if containing a file(s)
     */
    private synchronized boolean isDirectoryEmpty(File directory) {

        return directory.listFiles().length == 0;
    }

    /**
     * Runs before anything is filtered/cleaned. Automatically adds folders to the whitelist
     * based on the name of the folder itself
     * @param file file to check whether it should be added to the whitelist
     */
    private synchronized boolean autoWhiteList(File file) {

        String[] protectedFileList = {
                "BACKUP", "backup", "Backup", "backups",
                "Backups", "BACKUPS", "copy", "Copy", "copies", "Copies", "IMPORTANT",
                "important", "important"};

        for (String protectedFile : protectedFileList) {
            if (file.getName().contains(protectedFile) && !whiteList.contains(file.getAbsolutePath())) {
                whiteList.add(file.getAbsolutePath());
                Stash.put("whiteList", whiteList);
                return true;
            }
        }

        return false;
    }

    /**
     * Runs as for each loop through the extension filter, and checks if
     * the file name contains the extension
     * @param file file to check
     * @return true if the file's extension is in the filter, false otherwise
     */
    private synchronized boolean filter(File file) {

        for (String extension : filters) {
            if (file.getAbsolutePath().contains(extension)) return true; // file
            else if (file.isDirectory())
                if (isDirectoryEmpty(file) && prefs.getBoolean("empty", false)) return true; // empty folder
        }

        return false; // not empty folder or file in filter
    }

    /**
     * Handles the whether the user grants permission. Closes app on deny
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1)
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED)
                System.exit(0); // Permission denied
    }
}
