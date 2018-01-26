package com.mycarster.carster;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.dynamsoft.barcode.Barcode;
import com.dynamsoft.barcode.BarcodeReader;
import com.dynamsoft.barcode.FinishCallback;
import com.dynamsoft.barcode.ReadResult;

import java.util.Date;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MainActivity extends AppCompatActivity implements Camera.PreviewCallback {
    public static String TAG = "DBRDemo";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //apply for camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) !=  PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {
            }
            else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA}, PERMISSIONS_REQUEST_CAMERA);
            }
        }

        mPreview = (FrameLayout) findViewById(R.id.camera_preview);
        mBarcodeReader = new BarcodeReader("6440D80E694BD7534AFA04BE1A5A3A9A");
        mFlashImageView = (ImageView)findViewById(R.id.ivFlash);
        mFlashTextView = (TextView)findViewById(R.id.tvFlash);
        mRectLayer = (RectLayer)findViewById(R.id.rectLayer);

        mSurfaceHolder = new CameraPreview(MainActivity.this);
        mPreview.addView(mSurfaceHolder);
    }

    static final int PERMISSIONS_REQUEST_CAMERA = 473;
    static final int RC_BARCODE_TYPE = 8563;
    private FrameLayout mPreview = null;
    private CameraPreview mSurfaceHolder = null;
    private Camera mCamera = null;
    private BarcodeReader mBarcodeReader;
    private long mBarcodeFormat = Barcode.OneD | Barcode.QR_CODE | Barcode.PDF417 |Barcode.DATAMATRIX;
    private ImageView mFlashImageView;
    private TextView mFlashTextView;
    private RectLayer mRectLayer;
    private boolean mIsDialogShowing = false;
    private boolean mIsReleasing = false;
    final ReentrantReadWriteLock mRWLock = new ReentrantReadWriteLock();

    @Override
    protected void onResume() {
        super.onResume();
        waitForRelease();
        if (mCamera == null)
            openCamera();
        else
            mCamera.startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mCamera != null) {
            mSurfaceHolder.stopPreview();
            mCamera.setPreviewCallback(null);
            mIsReleasing = true;
            releaseCamera();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        waitForRelease();
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }

    public void setFlash(View v) {
        if (mCamera != null) {
            Camera.Parameters p = mCamera.getParameters();
            String flashMode = p.getFlashMode();
            if (flashMode.equals(Camera.Parameters.FLASH_MODE_OFF)) {
                p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mFlashImageView.setImageResource(R.mipmap.flash_on);
                mFlashTextView.setText("Flash on");
            }
            else {
                p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mFlashImageView.setImageResource(R.mipmap.flash_off);
                mFlashTextView.setText("Flash off");
            }
            mCamera.setParameters(p);
            mCamera.startPreview();
        }
    }

    private static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            Log.i(TAG, "Camera is not available (in use or does not exist)");
        }
        return c; // returns null if camera is unavailable
    }

    private void openCamera()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mCamera = getCameraInstance();
                if (mCamera != null) {
                    mCamera.setDisplayOrientation(90);
                    Camera.Parameters cameraParameters = mCamera.getParameters();
                    cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                    mCamera.setParameters(cameraParameters);
                }


                Message message = handler.obtainMessage(OPEN_CAMERA, 1);
                message.sendToTarget();
            }
        }).start();
    }

    private void releaseCamera()
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mCamera.release();
                mCamera = null;
                mRWLock.writeLock().lock();
                mIsReleasing = false;
                mRWLock.writeLock().unlock();
            }
        }).start();
    }

    private void waitForRelease() {
        while (true) {
            mRWLock.readLock().lock();
            if (mIsReleasing) {
                mRWLock.readLock().unlock();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                mRWLock.readLock().unlock();
                break;
            }
        }
    }

    private boolean mFinished = true;
    private long mStartTime;
    private final static int READ_RESULT = 1;
    private final static int OPEN_CAMERA = 2;
    private final static int RELEASE_CAMERA = 3;
    private int mImageHeight = 0;

    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case READ_RESULT:
                    ReadResult result = (ReadResult)msg.obj;
                    final Barcode barcode = result.barcodes == null ? null : result.barcodes[0];

                    if (barcode != null) {
                        String vin_number = barcode.displayValue;
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
                        builder1.setCancelable(true);
                        final String finalVin_number = vin_number;
                        builder1.setPositiveButton(
                                "SEARCH",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        mIsDialogShowing = false;
                                        dialog.cancel();
                                        launchSearchActivity(finalVin_number);
                                    }
                                });

                        builder1.setNegativeButton(
                                "RETRY",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                        mIsDialogShowing = false;
                                    }
                                });

                        AlertDialog alert11 = builder1.create();
                        mIsDialogShowing = true;
                        alert11.show();
                    } else {
                        if (result.errorCode != BarcodeReader.DBR_OK)
                            Log.i(TAG, "Error:" + result.errorString);
                    }
                    mFinished = true;
                    break;
                case OPEN_CAMERA:
                    if (mCamera != null) {
                        mCamera.setPreviewCallback(MainActivity.this);
                        mSurfaceHolder.setCamera(mCamera);
                        Camera.Parameters p = mCamera.getParameters();
                        if (mFlashTextView.getText().equals("Flash on"))
                            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                        mCamera.setParameters(p);
                        mSurfaceHolder.startPreview();
                    }
                    break;
                case RELEASE_CAMERA:

                    break;
            }
        }
    };

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (mFinished && !mIsDialogShowing) {
            mFinished = false;
            //mStartTime = SystemClock.currentThreadTimeMillis();
            mStartTime = new Date().getTime();
            Camera.Size size = camera.getParameters().getPreviewSize();
            mImageHeight = size.height;
            mBarcodeReader.readSingleAsync(data, size.width, size.height, mBarcodeFormat, new FinishCallback() {
                @Override
                public void onFinish(ReadResult readResult) {
                    Message message = handler.obtainMessage(READ_RESULT, readResult);
                    message.sendToTarget();
                }
            });
        }
    }

    private void launchSearchActivity(String vin_number) {
        Intent intent = new Intent(this, resultActivity.class);
        intent.putExtra("vin_number", vin_number);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
