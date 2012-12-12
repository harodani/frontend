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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

/**
 * Creates a dialog with only a OK button.
 * 
 * @author Paolo Boschini
 * @author Linus Sunde
 *
 */
public class OkButtonDialog extends DialogFragment {

    /** Listener to attach to this dialog. */ 
    private OnClickListener mOkListener;
    
    /** The message to show on this dialog. */
    private String mMessage;

    /** The title of this dialog. */
    private String mTitle;
    
    /**
     * Default constructor.
     * 
     * @param title         the title for the dialog
     * @param message       the message for the dialog 
     * @param okListener    the listener to attach to the dialog
     */
    public OkButtonDialog(String title, String message, OnClickListener okListener) {
        mOkListener = okListener;
        mMessage = message;
        mTitle = title;
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(mMessage)
                .setTitle(mTitle)
                .setPositiveButton("OK", mOkListener);       
        return builder.create();
    }
}