/*
 * Copyright 2020 Hunter J Drum
 */

package theredspy15.ltecleanerfoss;

import android.content.Context;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import com.heinrichreimersoftware.androidissuereporter.IssueReporterLauncher;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportFragmentManager().beginTransaction().replace(R.id.layout, new MyPreferenceFragment()).commit();
    }

    public static class MyPreferenceFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.setHasOptionsMenu(true);
        }

        /**
         * Inflate Preferences
         */
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.preferences);
        }

        /**
         * ClickEvent Listener for Preferences
         */
        @Override
        public boolean onPreferenceTreeClick(androidx.preference.Preference preference) {
            String key = preference.getKey();
            if ("suggestion".equals(key)) {
                reportIssue(getContext());
                return true;
            }
            return super.onPreferenceTreeClick(preference);
        }

        /**
         * Creates a menu that allows the user to create an issue on github
         */
        final void reportIssue(Context context) {

            IssueReporterLauncher.forTarget("TheRedSpy15", "LTECleanerFOSS")
                    .theme(R.style.CustomIssueReportTheme)
                    .guestEmailRequired(false)
                    .guestToken("194835cbf18259752d316f680ef4842aa7ca9dc5")
                    .minDescriptionLength(20)
                    .homeAsUpEnabled(true)
                    .launch(context);
        }
    }
}
