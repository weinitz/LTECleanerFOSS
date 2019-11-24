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

public class MainActivity extends AppCompatActivity {

    ConstraintSet constraintSet = new ConstraintSet();
    static boolean running = false;
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

        fileListView = findViewById(R.id.fileListView);
        fileScrollView = findViewById(R.id.fileScrollView);
        scanPBar = findViewById(R.id.scanProgress);
        progressText = findViewById(R.id.ScanTextView);
        statusText = findViewById(R.id.statusTextView);
        layout = findViewById(R.id.main_layout);

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
        if (!running) {
            if (!prefs.getBoolean("one_click", false)) // one-click disabled
                new AlertDialog.Builder(this,R.style.MyAlertDialogTheme)
                        .setTitle(R.string.select_task)
                        .setMessage(R.string.do_you_want_to)
                        .setPositiveButton(R.string.clean, (dialog, whichButton) -> { // clean
                            new Thread(()-> scan(true)).start();
                        })
                        .setNegativeButton(R.string.analyze, (dialog, whichButton) -> { // analyze
                            new Thread(()-> scan(false)).start();
                        }).show();
            else new Thread(()-> scan(true)).start(); // one-click enabled
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
    private void scan(boolean delete) {
        Looper.prepare();
        running = true;
        reset();

        File path = new File(Environment.getExternalStorageDirectory().toString()
                + "/"); // just a forward slash for whole device

        // scanner setup
        FileScanner fs = new FileScanner(path);
        fs.setEmptyDir(prefs.getBoolean("empty", false));
        fs.setAutoWhite(prefs.getBoolean("auto_white", true));
        fs.setDelete(delete);
        fs.setGUI(this);

        // filters
        fs.setUpFilters(prefs.getBoolean("generic", true),
                prefs.getBoolean("aggressive", false),
                prefs.getBoolean("apk", false));


        if (path.listFiles() == null) // is this needed?
            TastyToast.makeText(this, "Failed Scan", TastyToast.LENGTH_LONG, TastyToast.ERROR).show();

        runOnUiThread(() -> {
            animateBtn();
            statusText.setText(getString(R.string.status_running));
        });

        // start scanning
        int kilobytesTotal = fs.startScan();

        runOnUiThread(() -> { // crappy but working fix for percentage never reaching 100
            scanPBar.setProgress(scanPBar.getMax());
            progressText.setText("100%");
        });

        if (kilobytesTotal == 0) {
            TastyToast.makeText(this, getString(R.string.nothing_found), TastyToast.LENGTH_LONG, TastyToast.SUCCESS).show();
        } else {
            // toast view with amount found/freed
            if (delete) TastyToast.makeText( // Clean toast
                    this, getString(R.string.freed) + " " + convertSize(kilobytesTotal) + getString(R.string.kb), TastyToast.LENGTH_LONG, TastyToast.SUCCESS).show();
            else TastyToast.makeText( // Analyze toast
                    this, getString(R.string.found) + " " + convertSize(kilobytesTotal) + getString(R.string.kb), TastyToast.LENGTH_LONG, TastyToast.SUCCESS).show();
        }

        runOnUiThread(() -> statusText.setText(getString(R.string.status_idle)));
        running = false;
        Looper.loop();
    }


    /**
     * Convenience method to quickly create a textview
     * @param text - text of textview
     * @return - created textview
     */
    private synchronized TextView generateTextView(String text) {
        TextView textView = new TextView(MainActivity.this);
        textView.setTextColor(getResources().getColor(R.color.colorAccent));
        textView.setText(text);
        textView.setPadding(3,3,3,3);
        return textView;
    }

    private int convertSize(int size) {
        if (size >= 1024) return size / 1024;
        else return size;
    }

    /**
     * Increments amount removed, then creates a text view to add to the scroll view.
     * If there is any error while deleting, turns text view of path red
     * @param file file to delete
     */
    synchronized TextView displayPath(File file) {
        // creating and adding a text view to the scroll view with path to file
        TextView textView = generateTextView(file.getAbsolutePath());

        // adding to scroll view
        runOnUiThread(() -> fileListView.addView(textView));

        // scroll to bottom
        fileScrollView.post(() -> fileScrollView.fullScroll(ScrollView.FOCUS_DOWN));

        return textView;
    }


    /**
     * Removes all views present in fileListView (linear view), and sets found and removed
     * files to 0
     */
    private synchronized void reset() {
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        runOnUiThread(() -> {
            fileListView.removeAllViews();
            scanPBar.setProgress(0);
            scanPBar.setMax(1);
        });
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
     * Handles the whether the user grants permission. Launches new fragment asking the user to give file permission.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1 &&
                grantResults.length > 0 &&
                grantResults[0] != PackageManager.PERMISSION_GRANTED)
                //System.exit(0); // Permission denied
            prompt();
    }

    /**
     * Launches the prompt activity
     */
    public final void prompt() {
        Intent intent = new Intent(this, PromptActivity.class);
        startActivity(intent);
    }
}
