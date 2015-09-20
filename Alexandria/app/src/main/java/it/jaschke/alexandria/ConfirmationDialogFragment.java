package it.jaschke.alexandria;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

/**
 * Created by Ian on 9/18/2015.
 */
public class ConfirmationDialogFragment extends DialogFragment {

    public interface ConfirmationDialogListener {

        public void onDialogPositiveClick(AlertDialog dialog);

        public void onDialogNegativeClick(AlertDialog dialog);

    }

    ConfirmationDialogListener mListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_add_book, null))
                .setTitle(R.string.dialog_add_book_title)
                .setPositiveButton(R.string.dialog_add_book_confirmation, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing book has already been added.
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.dialog_add_book_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Remove book from database.
                        dialog.cancel();
                    }
                });

        return builder.create();
    }

    // Remove Book from database.
    @Override
    public void onCancel(DialogInterface dialog) {

    }
}
