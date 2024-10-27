package com.example.facefingerprintregister;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.File;
import java.util.concurrent.ExecutionException;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private File registeredFaceFile; // File for the registered face
    private boolean isFingerprintAuthenticated = false; // Track if fingerprint authentication is successful

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        previewView = findViewById(R.id.camera_preview_login);
        Button loginButton = findViewById(R.id.login_button);

        // Assuming the registered face image path is stored in the database
        registeredFaceFile = new File(getExternalFilesDir(null), "registered_face.jpg");

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1002);
        } else {
            startCamera();
        }

        // Set up face authentication
        loginButton.setOnClickListener(v -> capturePhotoForLogin());

        // Set up fingerprint authentication
        setupFingerprintAuthentication();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();
        imageCapture = new ImageCapture.Builder().build();
        CameraSelector cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA;

        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
    }

    private void capturePhotoForLogin() {
        File file = new File(getExternalFilesDir(null), "login_face.jpg");
        ImageCapture.OutputFileOptions options = new ImageCapture.OutputFileOptions.Builder(file).build();

        imageCapture.takePicture(options, ContextCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Toast.makeText(LoginActivity.this, "Face captured for login", Toast.LENGTH_SHORT).show();
                // Here, compare the captured face with the registered face (using simple file comparison for now)
                if (isFaceMatching(file, registeredFaceFile)) {
                    if (isFingerprintAuthenticated) {
                        navigateToNextPage();
                    } else {
                        Toast.makeText(LoginActivity.this, "Please authenticate using fingerprint", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Face does not match", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                Toast.makeText(LoginActivity.this, "Failed to capture face", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isFaceMatching(File loginFace, File registeredFace) {
        // A simple file comparison for demonstration purposes.
        // You should use an actual face recognition technique such as OpenCV for better results.
        return loginFace.length() == registeredFace.length(); // Dummy comparison based on file size
    }

    private void setupFingerprintAuthentication() {
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate()) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                // Fingerprint can be used
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(this, "Device does not have biometric hardware", Toast.LENGTH_SHORT).show();
                return;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(this, "No biometric credentials registered", Toast.LENGTH_SHORT).show();
                return;
        }

        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                isFingerprintAuthenticated = true;
                Toast.makeText(LoginActivity.this, "Fingerprint authentication successful", Toast.LENGTH_SHORT).show();
                // If face is already matched, go to the next page
                navigateToNextPage();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(LoginActivity.this, "Fingerprint authentication failed", Toast.LENGTH_SHORT).show();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Authentication")
                .setSubtitle("Authenticate using fingerprint")
                .setNegativeButtonText("Cancel")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void navigateToNextPage() {
        // Navigate to the next activity if both face and fingerprint are authenticated
        Intent intent = new Intent(LoginActivity.this, SuccessActivity.class);
        startActivity(intent);
    }

    // Handle camera permission request response
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1002 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
        }
    }
}
