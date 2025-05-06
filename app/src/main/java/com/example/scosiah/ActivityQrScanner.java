package com.example.scosiah;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;

import android.graphics.ImageFormat;
import android.graphics.YuvImage;
import android.media.Image;
import android.widget.Toast;

import androidx.camera.view.PreviewView;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ActivityQrScanner extends AppCompatActivity {

    private PreviewView previewView;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private ExecutorService cameraExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        previewView = findViewById(R.id.previewView);

        // Request camera permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }

        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    private void startCamera() {
        // Get the camera provider
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                // Get the camera provider
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Create Preview and CameraSelector
                Preview preview = new Preview.Builder().build();
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                // Set up ImageAnalysis use case
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                // Define the analyzer for barcode scanning
                imageAnalysis.setAnalyzer(cameraExecutor, image -> {
                    scanBarcode(image);
                });

                // Unbind any previous use cases
                cameraProvider.unbindAll();

                // Bind the camera use cases to the lifecycle
                Camera camera = cameraProvider.bindToLifecycle(
                        this,  // Activity or LifecycleOwner
                        cameraSelector,
                        preview,  // Bind the preview to the PreviewView
                        imageAnalysis
                );

                // Set the surface provider to display the camera preview on PreviewView
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void scanBarcode(ImageProxy imageProxy) {
        @androidx.camera.core.ExperimentalGetImage
        Image mediaImage = imageProxy.getImage();
        if (mediaImage != null) {
            // Convert image to InputImage
            InputImage inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());

            // Initialize the BarcodeScanner
            BarcodeScanner scanner = BarcodeScanning.getClient();
            scanner.process(inputImage)
                    .addOnSuccessListener(barcodes -> {
                        // Iterate over the detected barcodes
                        for (Barcode barcode : barcodes) {
                            String rawValue = barcode.getRawValue();
                            Log.d("QR Code", "Scanned: " + rawValue);
                            runOnUiThread(() -> Toast.makeText(this, "Scanned: " + rawValue, Toast.LENGTH_SHORT).show());
                        }
                        imageProxy.close();
                    })
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        imageProxy.close();
                    });
        } else {
            imageProxy.close();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Shut down the executor when the activity is destroyed
        cameraExecutor.shutdown();
    }
}
