package com.example.emily.qrcontact;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import java.io.File;
import java.io.IOException;
import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.io.text.VCardReader;

public class MainActivity extends AppCompatActivity {

    SurfaceView surfaceView;

    CameraSource cameraSource;

    TextView textView;

    BarcodeDetector barcodeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        surfaceView = (SurfaceView) findViewById(R.id.cameraPreview);
        textView = (TextView) findViewById(R.id.textView);
        barcodeDetector = new BarcodeDetector.Builder(this).setBarcodeFormats(Barcode.QR_CODE).build();
        cameraSource = new CameraSource.Builder(this, barcodeDetector).setAutoFocusEnabled(true).setRequestedPreviewSize(640, 480).build();
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                try {
                    cameraSource.start(holder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {

            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> qrCodes = detections.getDetectedItems();
                if (qrCodes.size() != 0) {
                    textView.post(new Runnable() {

                        @Override
                        public void run() {
                            Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(1000);
                            try {
                                for (int x = 0; x < qrCodes.size(); x++) {
                                    Log.d("ScanTest", qrCodes.valueAt(x).rawValue);
                                }
                            } catch (Exception e) {
                                Log.d("ScanTest", e.toString());
                            }
                            textView.setText(qrCodes.valueAt(0).rawValue);
                            VCard scannedContact = Ezvcard.parse(qrCodes.valueAt(0).rawValue).first();
                            Log.d("ScanTest", scannedContact.getEmails().get(0).toString());
                        }
                    });
                }
            }
        });
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(0);
        menuItem.setChecked(true);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch(menuItem.getItemId()) {
                    case R.id.navigation_scan:
                        break;
                    case R.id.navigation_make:
                        Intent intent2 = new Intent(MainActivity.this, Make.class);
                        startActivity(intent2);
                        break;
                    case R.id.navigation_help:
                        Intent intent3 = new Intent(MainActivity.this, Help.class);
                        startActivity(intent3);
                        break;
                }
                return false;
            }
        });
    }
}
