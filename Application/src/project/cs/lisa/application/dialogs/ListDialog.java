package project.cs.lisa.application.dialogs;

import java.util.Set;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

/**
 * Creates a list dialog.
 * 
 * @author Paolo Boschini
 * @author Linus Sunde
 *
 */
public abstract class ListDialog extends DialogFragment {

    /** The tag. */
    private static final String TAG = "ListDialog";

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
                Log.d(TAG, whichButton + "");
                mSelectedItem = items[whichButton].toString();
            }
        })
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Log.d(TAG, whichButton + "");
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