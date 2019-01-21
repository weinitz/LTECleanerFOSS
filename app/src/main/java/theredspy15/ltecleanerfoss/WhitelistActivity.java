package theredspy15.ltecleanerfoss;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class WhitelistActivity extends AppCompatActivity {

    ListView listView;

    BaseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whitelist);

        listView = findViewById(R.id.whitelistView);

        adapter = new ArrayAdapter<>(WhitelistActivity.this,R.layout.custom_textview,MainActivity.whiteList);
        listView.setAdapter(adapter);
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
     * Creates a dialog asking for a file/folder name to add to the whitelist
     * @param view the view that is clicked
     */
    public final void addToWhitelist(View view) {

        final EditText input = new EditText(WhitelistActivity.this);

        new AlertDialog.Builder(WhitelistActivity.this,R.style.MyAlertDialogTheme)
                .setTitle(R.string.add_to_whitelist)
                .setMessage(R.string.enter_file_name)
                .setView(input)
                .setPositiveButton(R.string.add, (dialog, whichButton) -> MainActivity.whiteList.add(String.valueOf(input.getText())))
                .setNegativeButton(R.string.cancel, (dialog, whichButton) -> { }).show();
    }
}
