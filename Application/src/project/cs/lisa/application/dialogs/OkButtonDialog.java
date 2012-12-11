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