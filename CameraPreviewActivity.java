package com.wabug.control;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.camera2.*;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Arrays;

public class CameraPreviewActivity extends Activity {
    private CameraManager manager;
    private CameraDevice device;
    private CameraCaptureSession session;
    private SurfaceView surfaceView;
    private String facing = "back";

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        facing = getIntent().getStringExtra("facing");
        if (facing == null) facing = "back";
        manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF10131A);
        TextView label = new TextView(this);
        label.setText("Live camera preview — " + facing.toUpperCase());
        label.setTextColor(0xFFFFFFFF);
        label.setGravity(Gravity.CENTER);
        root.addView(label, new LinearLayout.LayoutParams(-1, -2));
        surfaceView = new SurfaceView(this);
        root.addView(surfaceView, new LinearLayout.LayoutParams(-1, 0, 1));
        Button close = new Button(this);
        close.setText("Close Camera");
        close.setOnClickListener(v -> finish());
        root.addView(close, new LinearLayout.LayoutParams(-1, -2));
        setContentView(root);
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            public void surfaceCreated(SurfaceHolder h) { openCamera(); }
            public void surfaceChanged(SurfaceHolder h, int f, int w, int he) {}
            public void surfaceDestroyed(SurfaceHolder h) { closeCamera(); }
        });
    }

    private void openCamera() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Camera permission required.", Toast.LENGTH_SHORT).show();
            finish(); return;
        }
        try {
            String chosen = null;
            for (String id : manager.getCameraIdList()) {
                Integer lens = manager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING);
                if ("front".equals(facing) && lens != null && lens == CameraCharacteristics.LENS_FACING_FRONT) { chosen = id; break; }
                if ("back".equals(facing) && lens != null && lens == CameraCharacteristics.LENS_FACING_BACK) { chosen = id; break; }
            }
            if (chosen == null) { Toast.makeText(this, "Requested camera not found.", Toast.LENGTH_SHORT).show(); finish(); return; }
            manager.openCamera(chosen, new CameraDevice.StateCallback() {
                public void onOpened(CameraDevice camera) {
                    device = camera;
                    try {
                        Surface surface = surfaceView.getHolder().getSurface();
                        CaptureRequest.Builder req = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                        req.addTarget(surface);
                        device.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                            public void onConfigured(CameraCaptureSession s) {
                                session = s;
                                try { req.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO); session.setRepeatingRequest(req.build(), null, null); }
                                catch (CameraAccessException e) { Toast.makeText(CameraPreviewActivity.this, "Preview failed.", Toast.LENGTH_SHORT).show(); }
                            }
                            public void onConfigureFailed(CameraCaptureSession s) { Toast.makeText(CameraPreviewActivity.this, "Camera setup failed.", Toast.LENGTH_SHORT).show(); }
                        }, null);
                    } catch (CameraAccessException e) { Toast.makeText(CameraPreviewActivity.this, "Camera unavailable.", Toast.LENGTH_SHORT).show(); finish(); }
                }
                public void onDisconnected(CameraDevice camera) { camera.close(); device = null; }
                public void onError(CameraDevice camera, int error) { camera.close(); device = null; Toast.makeText(CameraPreviewActivity.this, "Camera error: " + error, Toast.LENGTH_SHORT).show(); finish(); }
            }, null);
        } catch (CameraAccessException e) { Toast.makeText(this, "Could not open camera.", Toast.LENGTH_SHORT).show(); finish(); }
    }

    private void closeCamera() {
        if (session != null) { session.close(); session = null; }
        if (device != null) { device.close(); device = null; }
    }
    @Override protected void onPause() { closeCamera(); super.onPause(); }
}
