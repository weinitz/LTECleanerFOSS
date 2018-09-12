/*
 *  Copyright 2018 TheRedSpy15
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package theredspy15.ltecleanerfoss;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;

import com.fxn.stash.Stash;
import com.heinrichreimersoftware.androidissuereporter.IssueReporterLauncher;
import com.sdsmdg.tastytoast.TastyToast;

public class SettingsActivity extends AppCompatActivity {

    CheckBox genericBox;
    CheckBox emptyCheckBox;
    CheckBox aggressiveBox;
    CheckBox oneClickBox;
    ListView listView;

    BaseAdapter adapter;

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // linking to UI
        genericBox = findViewById(R.id.tmpBox);
        aggressiveBox = findViewById(R.id.aggressiveBox);
        emptyCheckBox = findViewById(R.id.emptyFolderBox);
        oneClickBox = findViewById(R.id.oneClickBox);
        listView = findViewById(R.id.whitelistView);

        // whitelist view
        adapter = new ArrayAdapter<>(SettingsActivity.this,R.layout.custom_textview,MainActivity.whiteList);
        listView.setAdapter(adapter);

        // checkboxes
        genericBox.setChecked(Stash.getBoolean("genericFilter",true));
        emptyCheckBox.setChecked(Stash.getBoolean("deleteEmpty",false));
        aggressiveBox.setChecked(Stash.getBoolean("aggressiveFilter",false));
        oneClickBox.setChecked(Stash.getBoolean("oneClick",false));
    }

    /**
     * Saves all settings to the shared preferences file
     * @param view view that is clicked
     */
    public final void save(View view) {

        // loading preferences from stash
        Stash.put("deleteEmpty",emptyCheckBox.isChecked());
        Stash.put("aggressiveFilter",aggressiveBox.isChecked());
        Stash.put("genericFilter",genericBox.isChecked());
        Stash.put("oneClick",oneClickBox.isChecked());
        Stash.put("whiteList",MainActivity.whiteList);

        TastyToast.makeText(this,"Saved",TastyToast.LENGTH_SHORT,TastyToast.SUCCESS).show();
    }

    /**
     * Loads the privacy policy url in a browser
     * @param view the view that is clicked
     */
    public final void viewPrivacyPolicy(View view) {

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://cdn.rawgit.com/TheRedSpy15/LTECleanerFOSS/d9522c76/privacy_policy.html"));
        startActivity(browserIntent);
    }

    /**
     * Starts the main activity
     * @param view the view that is clicked
     */
    public final void back(View view) {

        Intent randomIntent = new Intent(this, MainActivity.class);
        startActivity(randomIntent);
    }

    /**
     * Creates a dialog asking for a file/folder name to add to the whitelist
     * @param view the view that is clicked
     */
    public final void addToWhitelist(View view) {

        final EditText input = new EditText(SettingsActivity.this);

        new AlertDialog.Builder(SettingsActivity.this,R.style.MyAlertDialogTheme)
                .setTitle(R.string.add_to_whitelist)
                .setMessage(R.string.enter_file_name)
                .setView(input)
                .setPositiveButton(R.string.add, (dialog, whichButton) -> MainActivity.whiteList.add(String.valueOf(input.getText())))
                .setNegativeButton(R.string.cancel, (dialog, whichButton) -> { }).show();
    }

    /**
     * Clears the whitelist, then sets it up again without loading saved one from stash
     * @param view the view that is clicked
     */
    public final void resetWhitelist(View view) {

        MainActivity.whiteList.clear();

        runOnUiThread(() -> {
            adapter.notifyDataSetChanged();
            listView.invalidateViews();
            listView.refreshDrawableState();
        });

        MainActivity.setUpWhiteListAndFilter(false); // false so we don't end up with the same thing we just reset
    }

    /**
     * Creates a menu that allows the user to create an issue on github
     * @param view the view that is clicked
     */
    public final void reportIssue(View view) {

        IssueReporterLauncher.forTarget("TheRedSpy15", "LTECleanerFOSS")
                .theme(R.style.CustomIssueReportTheme)
                .guestEmailRequired(false)
                .guestToken("5b88864377fb229774278868687f9c113eee8430")
                .minDescriptionLength(20)
                .homeAsUpEnabled(true)
                .launch(this);
    }
}
