package project.cs.lisa.application;

import project.cs.lisa.R;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Base menu to attach to whatever activity.
 * @author Paolo Boschini
 * @author Harold Martinez
 *
 */
public class BaseMenuActivity extends Activity {

    /** Debugging tag. */
    private static final String TAG = "BaseMenuActivity";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu()");
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_settings:
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            break;
        case R.id.menu_help:
            Intent helpActivity = new Intent(this, HelpActivity.class);
            startActivity(helpActivity);
            break;
        default:
            break;
        }
        return true;
    }
}
