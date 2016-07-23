package eatfit.sugarman.com;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Size;
import android.widget.FrameLayout;
import android.widget.Toast;

/* Import ZBar Class files the barcode scanner library*/
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import net.sourceforge.zbar.Config;

import eatfit.sugarman.com.R;

public class CameraTestActivity extends Activity
{
    private Camera mCamera;
    private CameraPreview mPreview;
    private Handler autoFocusHandler;
    ImageScanner scanner;
    private boolean cameraCapturing = true;

    FrameLayout preview;
    FrameLayout dataFrame;
    Button scanButon;
    static {
        System.loadLibrary("iconv");
    } 

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        autoFocusHandler = new Handler();
        mCamera = getCameraInstance();

        /* Instance barcode scanner */
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);

        scanButon=(Button) findViewById(R.id.scanButton);
        dataFrame=(FrameLayout) findViewById(R.id.dataFrame);
        preview = (FrameLayout)findViewById(R.id.cameraPreview);
        dataFrame.setVisibility(View.INVISIBLE);
        /*calls the camera*/
        mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);
        preview.addView(mPreview);
        mCamera.setPreviewCallback(previewCb);
        mCamera.startPreview();
        cameraCapturing = true;
        mCamera.autoFocus(autoFocusCB);

    }
    //release the camera
    public void onPause() {
        super.onPause();
        releaseCamera();
    }

    /* A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open();
        } catch (Exception e){
        }
        return c;
    }

    private void releaseCamera() {
        if (mCamera != null) {
            cameraCapturing = false;
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    private Runnable doAutoFocus = new Runnable() {
            public void run() {
                if (cameraCapturing)
                    mCamera.autoFocus(autoFocusCB);
            }
        };

    //im still working on this function
    public void backToScanner(){
        if(scanButon.getVisibility()==View.VISIBLE) {
            scanButon.setVisibility(View.INVISIBLE);
            dataFrame.setVisibility(View.INVISIBLE);

            Toast.makeText(CameraTestActivity.this, "function invisable".toString(), Toast.LENGTH_SHORT).show();
        }
    }

    PreviewCallback previewCb = new PreviewCallback() {
            public void onPreviewFrame(byte[] data, Camera camera) {
                Camera.Parameters parameters = camera.getParameters();
                Size size = parameters.getPreviewSize();

                Image barcode = new Image(size.width, size.height, "Y800");
                barcode.setData(data);

                int result = scanner.scanImage(barcode);

                //if barcode detected
                if (result != 0) {
                    cameraCapturing = false;
                    mCamera.setPreviewCallback(null);

                    SymbolSet syms = scanner.getResults();

                    //foreach barcode in image//
                    for (Symbol sym : syms) {
                        Toast.makeText(CameraTestActivity.this,sym.getData().toString(),Toast.LENGTH_SHORT).show();
                    }
                    //doesnt work yet
                    if (dataFrame.getVisibility() == View.INVISIBLE){
                        dataFrame.setVisibility(View.VISIBLE);
                        scanButon.setVisibility(View.VISIBLE);
                        Toast.makeText(CameraTestActivity.this, "visable".toString(), Toast.LENGTH_SHORT).show();
                    }

                    mCamera.setPreviewCallback(previewCb);
                    cameraCapturing = true;
                    mCamera.autoFocus(autoFocusCB);
                }
            }
        };

    // every 1 second doing auto focus
    AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
            public void onAutoFocus(boolean success, Camera camera) {
                autoFocusHandler.postDelayed(doAutoFocus, 1000);
            }
        };
}
