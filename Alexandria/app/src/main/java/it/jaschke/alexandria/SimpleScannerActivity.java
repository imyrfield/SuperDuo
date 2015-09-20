package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

/**
 * Created by Ian on 9/16/2015.
 */
public class SimpleScannerActivity extends Activity implements ZBarScannerView.ResultHandler {
    private ZBarScannerView mScannerView;
    private ArrayList<Integer> mSelectedIndices;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        // Programmatically initialize the scanner view
        mScannerView = new ZBarScannerView(this);

        // Set the scanner view as the content view
        setContentView(mScannerView);
        setupFormats();
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {

        //TODO: Exit camera view once barcode has been recorded ??

        // Prints scan results
        Log.d("SimpleScannerActivity", "handleResult (line 39): " + rawResult.getContents());
        // Prints the scan format(qrcode, pdf417 etc.)
        Log.d("SimpleScannerActivity", "handleResult (line 40): " + rawResult.getBarcodeFormat().getName());

        if (rawResult.getContents() != null){

            // Do something with the result here
            String result = rawResult.getContents();
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("result", result);
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    public void setupFormats() {
        List<BarcodeFormat> formats = new ArrayList<BarcodeFormat>();
        if(mSelectedIndices == null || mSelectedIndices.isEmpty()) {
            mSelectedIndices = new ArrayList<Integer>();
            for(int i = 0; i < BarcodeFormat.ALL_FORMATS.size(); i++) {
                mSelectedIndices.add(i);
            }
        }

        for(int index : mSelectedIndices) {
            formats.add(BarcodeFormat.ALL_FORMATS.get(index));
            formats.remove(BarcodeFormat.ISBN10);
        }
        if(mScannerView != null) {
            mScannerView.setFormats(formats);
        }
    }
}

