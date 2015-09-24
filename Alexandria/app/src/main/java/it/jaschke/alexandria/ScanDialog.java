package it.jaschke.alexandria;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;
import it.jaschke.alexandria.services.DownloadImage;

/**
 * Created by Ian on 9/21/2015.
 */
public class ScanDialog extends DialogFragment implements LoaderManager
        .LoaderCallbacks<Cursor> {

    private static final String TAG = "INTENT_TO_SCAN_ACTIVITY";
    private EditText ean;
    private final int LOADER_ID = 1;
    private final String EAN_CONTENT = "eanContent";
    private static final int SCAN_INT = 101;
    private View view;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_add_book, null);

        builder.setView(view)
                .setTitle(R.string.scan);

        return builder.create();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ean = (EditText) view.findViewById(R.id.ean);

        ean.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //no need
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //no need
            }

            @Override
            public void afterTextChanged(Editable s) {
                String ean = s.toString();
                //catch isbn10 numbers
                if (ean.length() == 10 && !ean.startsWith("978")) {
                    ean = "978" + ean;
                }
                if (ean.length() < 13) {
                    // Erasing Detail?
//                    clearFields();
                    return;
                }

                //Once we have an ISBN, start a book intent
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean);
                bookIntent.setAction(BookService.FETCH_BOOK);
                getActivity().startService(bookIntent);

                ScanDialog.this.restartLoader();
            }
        });

        view.findViewById(R.id.scan_button).setOnClickListener(new View.OnClickListener
                () {
            @Override
            public void onClick(View v) {

                Context context = getActivity();
                Intent intent = new Intent(context, SimpleScannerActivity.class);
                startActivityForResult(intent, SCAN_INT);
            }
        });

        view.findViewById(R.id.addButton).setOnClickListener(new View.OnClickListener
                () {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        //TODO: Does it work ??
        view.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener
                () {
            @Override
            public void onClick(View view) {
                onCancel(getDialog());
            }
        });

        if (savedInstanceState != null) {
            ean.setText(savedInstanceState.getString(EAN_CONTENT));
            ean.setHint("");
        }

        return container;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (ean != null) {
            outState.putString(EAN_CONTENT, ean.getText().toString());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == MainActivity.RESULT_OK) {
            switch (requestCode) {
                case SCAN_INT:
                    ean.setText(data.getStringExtra("result"));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (ean.getText().length() == 0) {
            return null;
        }
        String eanStr = ean.getText().toString();
        if (eanStr.length() == 10 && !eanStr.startsWith("978")) {
            eanStr = "978" + eanStr;
        }
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(eanStr)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }

        showDetails();

        String bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        ((TextView) view.findViewById(R.id.bookTitle)).setText(bookTitle);

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        ((TextView) view.findViewById(R.id.bookSubTitle)).setText(bookSubTitle);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        String[] authorsArr = authors.split(",");
        ((TextView) view.findViewById(R.id.authors)).setLines(authorsArr.length);
        ((TextView) view.findViewById(R.id.authors)).setText(authors.replace(",",
                "\n"));
        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if (Patterns.WEB_URL.matcher(imgUrl).matches()) {
            new DownloadImage((ImageView) view.findViewById(R.id.bookCover)).execute
                    (imgUrl);
            view.findViewById(R.id.bookCover).setVisibility(View.VISIBLE);
        }

//        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
//        ((TextView) view.findViewById(R.id.categories)).setText(categories);

//        view.findViewById(R.id.save_button).setVisibility(View.VISIBLE);
//        view.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void restartLoader() {
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    private void showDetails() {

        View details = view.findViewById(R.id.bookContainer);
        if (details == null) {
            return;
        }

        Button addButton = (Button) view.findViewById(R.id.addButton);
        if(addButton == null) {
            return;
        }

        if (details.getVisibility() == View.GONE) {
            details.setVisibility(View.VISIBLE);
            addButton.setVisibility(View.VISIBLE);
        } else {
            details.setVisibility(View.GONE);
            addButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {

        Intent bookIntent = new Intent(getActivity(), BookService.class);
        bookIntent.putExtra(BookService.EAN, ean.getText().toString());
        bookIntent.setAction(BookService.DELETE_BOOK);
        getActivity().startService(bookIntent);
        ean.setText("");

        super.onCancel(dialog);
    }
}
