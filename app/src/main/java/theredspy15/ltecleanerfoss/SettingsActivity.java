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

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import androidx.appcompat.app.AppCompatActivity;

import com.heinrichreimersoftware.androidissuereporter.IssueReporterLauncher;

public class SettingsActivity extends AppCompatActivity {

    @SuppressLint("CommitPrefEdits")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getFragmentManager().beginTransaction().replace(R.id.layout, new MyPreferenceFragment()).commit();
}

    public static class MyPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
            this.setHasOptionsMenu(true);
            this.addPreferencesFromResource(R.xml.preferences);
            Preference button = findPreference("suggestion");
            button.setOnPreferenceClickListener(preference -> {
                reportIssue(button.getContext());
                return true;
            });
        }

        /**
         * Creates a menu that allows the user to create an issue on github
         */
        public final void reportIssue(Context context) {

            IssueReporterLauncher.forTarget("TheRedSpy15", "LTECleanerFOSS")
                    .theme(R.style.CustomIssueReportTheme)
                    .guestEmailRequired(false)
                    .guestToken("5b88864377fb229774278868687f9c113eee8430")
                    .minDescriptionLength(20)
                    .homeAsUpEnabled(true)
                    .launch(context);
        }
    }
}
