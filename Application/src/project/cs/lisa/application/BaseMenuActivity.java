/**
 * Copyright 2012 Ericsson, Uppsala University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Uppsala University
 *
 * Project CS course, Fall 2012
 *
 * Projekt DV/Project CS, is a course in which the students develop software for
 * distributed systems. The aim of the course is to give insights into how a big
 * project is run (from planning to realization), how to construct a complex
 * distributed system and to give hands-on experience on modern construction
 * principles and programming methods.
 *
 */

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
