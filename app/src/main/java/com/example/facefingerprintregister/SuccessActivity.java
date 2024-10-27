package com.example.facefingerprintregister;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import com.google.common.util.concurrent.ListenableFuture;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import androidx.camera.view.PreviewView;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SuccessActivity extends AppCompatActivity {

    private PreviewView previewView;
    private static final int CAMERA_REQUEST_CODE = 100;
    private ExecutorService cameraExecutor;
    private MediaPlayer mediaPlayer;  // Declare MediaPlayer as a class-level variable

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        previewView = findViewById(R.id.previewView);
        Button stopButton = findViewById(R.id.stopButton);  // Find the stop button

        // Set up stop button click listener
        stopButton.setOnClickListener(v -> stopSong());  // Handle stop button click

        if (previewView == null) {
            Log.e("MainActivity", "PreviewView is null. Check your layout XML.");
            return;
        }

        // Check for camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
        }

        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                Log.e("CameraX", "Error initializing CameraX", e);
                Toast.makeText(this, "Error initializing camera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        if (previewView == null) {
            Log.e("MainActivity", "PreviewView is null when trying to bind the camera.");
            return;
        }

        Preview preview = new Preview.Builder().build();
        ImageCapture imageCapture = new ImageCapture.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, createImageAnalysis());
    }

    @OptIn(markerClass = ExperimentalGetImage.class)
    private ImageAnalysis createImageAnalysis() {
        FaceDetectorOptions options = new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().build();

        imageAnalysis.setAnalyzer(cameraExecutor, imageProxy -> {
            if (imageProxy.getImage() == null) {
                Log.e("ImageAnalysis", "ImageProxy is null, skipping analysis.");
                imageProxy.close();
                return;
            }

            InputImage image = InputImage.fromMediaImage(imageProxy.getImage(), imageProxy.getImageInfo().getRotationDegrees());

            FaceDetection.getClient(options).process(image)
                    .addOnSuccessListener(faces -> {
                        for (Face face : faces) {
                            if (face.getSmilingProbability() != null) {
                                float smileProb = face.getSmilingProbability();
                                if (smileProb > 0.5) {
                                    playSongForEmotion("happy");
                                } else {
                                    playSongForEmotion("sad");
                                }
                            }
                        }
                        imageProxy.close();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Emotion Detection", "Error detecting emotion: " + e.getMessage());
                        Toast.makeText(this, "Error detecting emotion", Toast.LENGTH_SHORT).show();
                        imageProxy.close();
                    });
        });

        return imageAnalysis;
    }

    private void playSongForEmotion(String emotion) {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();  // Stop any ongoing playback before starting a new one
            mediaPlayer.release();
        }

        mediaPlayer = new MediaPlayer();  // Initialize new MediaPlayer
        try {
            if (emotion.equals("happy")) {
                mediaPlayer.setDataSource(this, Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.happy_song));
            } else if (emotion.equals("sad")) {
                mediaPlayer.setDataSource(this, Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.sad_song));
            }
            mediaPlayer.prepare();
            mediaPlayer.start();

            mediaPlayer.setOnCompletionListener(mp -> {
                mediaPlayer.release();  // Release the MediaPlayer after playback completes
                mediaPlayer = null;
            });
        } catch (IOException e) {
            Log.e("MediaPlayer", "Error playing song: " + e.getMessage());
            Toast.makeText(this, "Error playing song", Toast.LENGTH_SHORT).show();
        }
    }

    // Method to stop the song when the stop button is pressed
    private void stopSong() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();  // Stop the playback
            mediaPlayer.release();  // Release MediaPlayer resources
            mediaPlayer = null;  // Set to null to avoid multiple calls to stop
            Toast.makeText(this, "Song stopped", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();  // Release MediaPlayer resources when activity is destroyed
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
