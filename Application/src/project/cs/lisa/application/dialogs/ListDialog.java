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

package project.cs.lisa.application.dialogs;

import java.util.Set;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Creates a list dialog.
 * 
 * @author Paolo Boschini
 * @author Linus Sunde
 *
 */
public abstract class ListDialog extends DialogFragment {

    /** The list of items to display in the dialog. */
    private Set<String> mItems;

    /** The selected item from the list. */
    private String mSelectedItem;

    /** The title of the dialog. */
    private String mTitle;

    /**
     * Default constructor. It gets a list of items
     * to be displayed.
     * 
     * @param items     the items to be displayed
     * @param title     the title of this dialog
     */
    public ListDialog(Set<String> items, String title) {
        mItems = items;
        mTitle = title;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final CharSequence[] items = mItems.toArray(new CharSequence[mItems.size()]);
        mSelectedItem = items[0].toString();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mTitle)
        .setSingleChoiceItems(items, 0,
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mSelectedItem = items[whichButton].toString();
            }
        })
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                onConfirm(mSelectedItem);
            }
        });

        return builder.create();
    }

    /**
     * Implements the behavior for when the user click
     * on OK in this dialog.
     * @param item  the 
     */
    public abstract void onConfirm(String item);
}