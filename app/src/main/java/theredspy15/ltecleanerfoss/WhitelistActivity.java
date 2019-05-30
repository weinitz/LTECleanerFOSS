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

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.fxn.stash.Stash;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;

public class WhitelistActivity extends AppCompatActivity {

    ListView listView;

    BaseAdapter adapter;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whitelist);

        listView = findViewById(R.id.whitelistView);

        adapter = new ArrayAdapter<>(WhitelistActivity.this,R.layout.custom_textview, MainActivity.whiteList);
        listView.setAdapter(adapter);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }

    /**
     * Clears the whitelist, then sets it up again without loading saved one from stash
     * @param view the view that is clicked
     */
    public final void emptyWhitelist(View view) {

        new AlertDialog.Builder(WhitelistActivity.this,R.style.MyAlertDialogTheme)
                .setTitle(R.string.reset_whitelist)
                .setMessage(R.string.are_you_reset_whitelist)
                .setPositiveButton(R.string.reset, (dialog, whichButton) -> {
                    MainActivity.whiteList.clear();
                    Stash.put("whiteList", MainActivity.whiteList);
                    runOnUiThread(() -> {
                        adapter.notifyDataSetChanged();
                        listView.invalidateViews();
                        listView.refreshDrawableState();
                    });
                })
                .setNegativeButton(R.string.cancel, (dialog, whichButton) -> { }).show();
    }

    public final void addRecommended(View view) {
        addRecommended();
        runOnUiThread(() -> {
            adapter.notifyDataSetChanged();
            listView.invalidateViews();
            listView.refreshDrawableState();
        });
    }

    public static void addRecommended() {
        if (!MainActivity.whiteList.contains(new File(Environment.getExternalStorageDirectory(), "Music").getPath())) {
            MainActivity.whiteList.add(new File(Environment.getExternalStorageDirectory(), "Music").getPath());
            MainActivity.whiteList.add(new File(Environment.getExternalStorageDirectory(), "Podcasts").getPath());
            MainActivity.whiteList.add(new File(Environment.getExternalStorageDirectory(), "Ringtones").getPath());
            MainActivity.whiteList.add(new File(Environment.getExternalStorageDirectory(), "Alarms").getPath());
            MainActivity.whiteList.add(new File(Environment.getExternalStorageDirectory(), "Notifications").getPath());
            MainActivity.whiteList.add(new File(Environment.getExternalStorageDirectory(), "Pictures").getPath());
            MainActivity.whiteList.add(new File(Environment.getExternalStorageDirectory(), "Movies").getPath());
            MainActivity.whiteList.add(new File(Environment.getExternalStorageDirectory(), "Download").getPath());
            MainActivity.whiteList.add(new File(Environment.getExternalStorageDirectory(), "DCIM").getPath());
            MainActivity.whiteList.add(new File(Environment.getExternalStorageDirectory(), "Documents").getPath());
        }
        Stash.put("whiteList", MainActivity.whiteList);
    }

    /**
     * Creates a dialog asking for a file/folder name to add to the whitelist
     * @param view the view that is clicked
     */
    public final void addToWhitelist(View view) {

        final EditText input = new EditText(WhitelistActivity.this);

        new AlertDialog.Builder(WhitelistActivity.this,R.style.MyAlertDialogTheme)
                .setTitle(R.string.add_to_whitelist)
                .setMessage(R.string.enter_file_name)
                .setView(input)
                .setPositiveButton(R.string.add, (dialog, whichButton) -> {
                    MainActivity.whiteList.add(String.valueOf(input.getText()));
                    Stash.put("whiteList", MainActivity.whiteList);
                    runOnUiThread(() -> {
                        adapter.notifyDataSetChanged();
                        listView.invalidateViews();
                        listView.refreshDrawableState();
                    });
                })
                .setNegativeButton(R.string.cancel, (dialog, whichButton) -> { }).show();
    }
}
