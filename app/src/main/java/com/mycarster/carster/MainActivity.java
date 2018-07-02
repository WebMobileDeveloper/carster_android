package com.mycarster.carster;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.scandit.barcodepicker.BarcodePicker;
import com.scandit.barcodepicker.OnScanListener;
import com.scandit.barcodepicker.ScanSession;
import com.scandit.barcodepicker.ScanSettings;
import com.scandit.barcodepicker.ScanditLicense;
import com.scandit.recognition.Barcode;
import com.scandit.recognition.SymbologySettings;

public class MainActivity extends AppCompatActivity implements OnScanListener {

    public static String TAG = "MainActivity";


    private FrameLayout mPreview = null;
    private RectLayer mRectLayer;
    private ImageView mFlashImageView;
    public static final String sScanditSdkAppKey = "AbUb+aCBNMGiM+J7yjRo+AYiJm9gMkNysSjXZu99qJ7AWPG4mkWfRQZ2ybDQak2ZJlOnmeELXcJ8enDpeyl9+k9b3ZtjUWR75QREaW1nrDsEYaIhLXdiN/1WG/fdElQ+qi3jRskAYLdhaa8gY3tH+bpCCOer31XNnDpUGd4hBGp3mknBUrs5y6Jhalf11SBmclvZU3Oi95kO5+me244Dm9+jP1m/dP47zIFVud32Lo9OW1wJM/RNrqMnKxo+X/eeYml5qk5hzBXtzgBlw9kAx8kPY2rbZrXIi4p94s/Dyt4JVk6HPKGdWQjyYQnIBuQxkxIz2nnHl1Q2dS2hQMZ+1cIuOf2D3S+jXBc69dBWhBIPVSi78Zwj9z3I+gpRkfQD97xnUENAyQlpT8mMJ1IGnNheiQOIhdNlIilSQIUV84Qd0ljyzRiHvkdAkILIrTl8l0GrsmihHsF0VYfPdXUjAxNOvrCbXJ4n6cHAftCks7W5J61wF7TW0aYGw04nAc6QrjTVl0IAygZ0qo03xvPFkBO8CVWn5DnQjUwBpL5tzPnfsN4s9ETNYaiKS8y1xVqhCz7egBXqJQHlvn63hPZMOO7eyst4nZDHkvoRNPFEG2F7XdtAEfqhZAV+6/oKtmJAPByBiu9KQOObv3tj9TLNxCA6uqdDEVHxHF6q/x3rP4VZkLmZvef6uESr2/T7FYt2iOJnse7jQZQqga934W2GmoqMXzXWl0/HzncnkC9mkuba7Cee/ROveM9hiF6QAd7qbtuclKItUajZEnmWnipGAV+Bt0XM92zSTCab5A9oRmSL";
    private final int CAMERA_PERMISSION_REQUEST = 0;

    // The main object for recognizing and displaying barcodes.
    private BarcodePicker mBarcodePicker;
    private boolean mDeniedCameraAccess = false;
    private boolean mPaused = true;

    OrientationEventListener myOrientationEventListener;
    final int THRESHOLD = 10;
    final int captureTimeout = 15000;
    public int lastOrientation = 0;  //portrate
    private boolean flashStatus = false;
    private Handler mHandler;



    Runnable myTask = new Runnable() {
        @Override
        public void run() {
            mBarcodePicker.pauseScanning();
            mPaused = true;
            AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this, R.style.AppCompatAlertDialogStyle);
            builder1.setTitle("");
            builder1.setMessage("VIN scan timed out!");
            builder1.setCancelable(false);

            builder1.setPositiveButton(
                    "ENTER VIN MANUALLY",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse("https://mycarster.com/?page_id=29/"));
                            startActivity(viewIntent);
                        }
                    });

            builder1.setNegativeButton(
                    "RETRY",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            mPaused = false;
                            mBarcodePicker.startScanning();
                            mHandler.postDelayed(myTask, captureTimeout);
                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mHandler = new Handler(Looper.getMainLooper());

        ScanditLicense.setAppKey(sScanditSdkAppKey);
        myOrientationEventListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL){

            @Override
            public void onOrientationChanged(int arg0) {
                if(isChangedOrientation(arg0)){
                    mRectLayer.invalidate();
                }
            }};

        if (myOrientationEventListener.canDetectOrientation()){
            myOrientationEventListener.enable();
        }
        else{
            finish();
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mPreview = findViewById(R.id.camera_preview);
        mRectLayer = findViewById(R.id.rectLayer);
        initializeAndStartBarcodeScanning();
    }
    /**
     * Initializes and starts the bar code scanning.
     */
    public void initializeAndStartBarcodeScanning() {

        ScanSettings settings = ScanSettings.create();
        int[] symbologiesToEnable = new int[] {
                Barcode.SYMBOLOGY_EAN13,
                Barcode.SYMBOLOGY_EAN8,
                Barcode.SYMBOLOGY_UPCA,
                Barcode.SYMBOLOGY_DATA_MATRIX,
                Barcode.SYMBOLOGY_QR,
                Barcode.SYMBOLOGY_CODE39,
                Barcode.SYMBOLOGY_CODE128,
                Barcode.SYMBOLOGY_INTERLEAVED_2_OF_5,
                Barcode.SYMBOLOGY_UPCE
        };
        for (int sym : symbologiesToEnable) {
            settings.setSymbologyEnabled(sym, true);
        }

        SymbologySettings symSettings = settings.getSymbologySettings(Barcode.SYMBOLOGY_CODE39);
        short[] activeSymbolCounts = new short[] {
                7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20
        };
        symSettings.setActiveSymbolCounts(activeSymbolCounts);
        settings.setCameraFacingPreference(ScanSettings.CAMERA_FACING_BACK);
        boolean emulatePortraitMode = !BarcodePicker.canRunPortraitPicker();
        if (emulatePortraitMode) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        BarcodePicker picker = new BarcodePicker(this, settings);
        picker.removeViewAt(1);
        picker.getOverlayView().setBeepEnabled(false);
        picker.getOverlayView().setVibrateEnabled(true);
        //picker.getOverlayView().drawViewfinder(false);
        mPreview.addView(picker);

        mBarcodePicker = picker;
        mFlashImageView = (ImageView)findViewById(R.id.ivFlash);
        //mBarcodePicker.switchTorchOn(false);
        // Register listener, in order to be notified about relevant events
        // (e.g. a successfully scanned bar code).
        mBarcodePicker.setOnScanListener(this);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void grantCameraPermissionsThenStartScanning() {
        if (this.checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            if (mDeniedCameraAccess == false) {
                // It's pretty clear for why the camera is required. We don't need to give a
                // detailed reason.
                this.requestPermissions(new String[]{ Manifest.permission.CAMERA },
                        CAMERA_PERMISSION_REQUEST);
            }
        } else {
            // We already have the permission.
            mBarcodePicker.startScanning();
            mHandler.postDelayed(myTask, captureTimeout);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mDeniedCameraAccess = false;
                if (!mPaused) {
                    mBarcodePicker.startScanning();
                    mHandler.postDelayed(myTask, captureTimeout);
                }
            } else {
                mDeniedCameraAccess = true;
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private boolean isChangedOrientation(int orientation){
        if(isLandscape(orientation) && lastOrientation == 0){
            lastOrientation = 1;
            return  true;
        }
        if(isPortrait(orientation) && lastOrientation == 1){
            lastOrientation = 0;
            return  true;
        }
        return false;
    }

    private boolean isLandscape(int orientation){
        return orientation >= (90 - THRESHOLD) && orientation <= (90 + THRESHOLD);
    }

    private boolean isPortrait(int orientation){
        return (orientation >= (360 - THRESHOLD) && orientation <= 360) || (orientation >= 0 && orientation <= THRESHOLD);
    }




    @Override
    protected void onResume() {
        super.onResume();
        mPaused = false;
        // Handle permissions for Marshmallow and onwards...
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            grantCameraPermissionsThenStartScanning();
        } else {
            // Once the activity is in the foreground again, restart scanning.
            mBarcodePicker.startScanning();
            mHandler.postDelayed(myTask, captureTimeout);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mBarcodePicker.stopScanning();
        mHandler.removeCallbacks(myTask);
        mPaused = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mBarcodePicker.stopScanning();
        mHandler.removeCallbacks(myTask);
        mPaused = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myOrientationEventListener.disable();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
    public void setFlash(View v) {
        if(flashStatus){
            mFlashImageView.setImageResource(R.mipmap.flashlight_turn_on_icon);
            mBarcodePicker.switchTorchOn(false);
        }else{
            mFlashImageView.setImageResource(R.mipmap.flashlight_turn_off_icon);
            mBarcodePicker.switchTorchOn(true);
        }
        flashStatus = !flashStatus;
    }
    public void closeApp(View v){
        finish();
    }

    @Override
    public void didScan(ScanSession session) {
        mBarcodePicker.pauseScanning();
        mHandler.removeCallbacks(myTask);
        mPaused = true;
        for (Barcode code : session.getNewlyRecognizedCodes()) {
            String vin_number = code.getData();
            if(vin_number.length()>17){
                if(vin_number.charAt(0) == 'I'){
                    vin_number = vin_number.substring(1,18);
                }else{
                    vin_number = vin_number.substring(0,17);
                }
            }
            AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this, R.style.AppCompatAlertDialogStyle);
            builder1.setTitle("Capture Completed!");
            builder1.setMessage("Result: " + vin_number + "\n\n Please select search button for more information.");
            builder1.setCancelable(false);
            final String finalVin_number = vin_number;
            builder1.setPositiveButton(
                    "SEARCH",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            launchSearchActivity(finalVin_number);
                        }
                    });

            builder1.setNegativeButton(
                    "RETRY",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            mPaused = false;
                            mBarcodePicker.startScanning();
                            mHandler.postDelayed(myTask, captureTimeout);
                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();
        }
    }

    private void launchSearchActivity(String vin_number) {
        Intent intent = new Intent(this, resultActivity.class);
        intent.putExtra("vin_number", vin_number);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
