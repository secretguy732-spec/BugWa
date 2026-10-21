package com.wabug.control;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private CameraManager cameraManager;
    private String flashCameraId;
    private boolean flashOn = false;
    private TextView flashStatus;
    private static final int CAMERA_PERMISSION = 41;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        buildUi();
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
        } else findFlashCamera();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(28, 40, 28, 28);
        root.setBackgroundColor(0xFF10131A);

        TextView title = new TextView(this);
        title.setText("WABug Control");
        title.setTextSize(28);
        title.setTextColor(0xFFFFFFFF);
        title.setGravity(Gravity.CENTER);
        root.addView(title, matchWrap());

        TextView subtitle = new TextView(this);
        subtitle.setText("Camera • Flash • App Lock");
        subtitle.setTextSize(14);
        subtitle.setTextColor(0xFFB8C1D1);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams sp = matchWrap();
        sp.bottomMargin = 28;
        root.addView(subtitle, sp);

        Button front = button("Open Front Camera");
        front.setOnClickListener(v -> openCamera("front"));
        root.addView(front, matchWrap());

        Button back = button("Open Rear Camera");
        back.setOnClickListener(v -> openCamera("back"));
        root.addView(back, matchWrap());

        Button flash = button("Toggle Flash ON / OFF");
        flash.setOnClickListener(v -> toggleFlash());
        root.addView(flash, matchWrap());

        flashStatus = new TextView(this);
        flashStatus.setText("Flash: OFF");
        flashStatus.setTextColor(0xFFFFFFFF);
        flashStatus.setTextSize(16);
        flashStatus.setGravity(Gravity.CENTER);
        root.addView(flashStatus, matchWrap());

        Button lock = button("Set Password & Lock Text");
        lock.setOnClickListener(v -> showLockSetup());
        LinearLayout.LayoutParams lp = matchWrap();
        lp.topMargin = 18;
        root.addView(lock, lp);

        TextView note = new TextView(this);
        note.setText("App lock only. This does not replace Android's system lock screen.");
        note.setTextColor(0xFF9AA5B5);
        note.setTextSize(12);
        note.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams np = matchWrap();
        np.topMargin = 20;
        root.addView(note, np);
        setContentView(root);
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    private Button button(String label) {
        Button b = new Button(this);
        b.setText(label);
        LinearLayout.LayoutParams p = matchWrap();
        p.bottomMargin = 10;
        b.setLayoutParams(p);
        return b;
    }

    private void findFlashCamera() {
        try {
            flashCameraId = null;
            for (String id : cameraManager.getCameraIdList()) {
                CameraCharacteristics c = cameraManager.getCameraCharacteristics(id);
                Boolean hasFlash = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                Integer facing = c.get(CameraCharacteristics.LENS_FACING);
                if (Boolean.TRUE.equals(hasFlash) && facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                    flashCameraId = id;
                    break;
                }
            }
        } catch (CameraAccessException e) {
            toast("Could not inspect camera hardware.");
        }
    }

    private void toggleFlash() {
        if (flashCameraId == null) findFlashCamera();
        if (flashCameraId == null) {
            toast("No rear-camera flash found on this device.");
            return;
        }
        try {
            flashOn = !flashOn;
            cameraManager.setTorchMode(flashCameraId, flashOn);
            flashStatus.setText("Flash: " + (flashOn ? "ON" : "OFF"));
        } catch (CameraAccessException | SecurityException e) {
            flashOn = false;
            flashStatus.setText("Flash: OFF");
            toast("Flash unavailable. Close other apps using the camera.");
        }
    }

    private void openCamera(String facing) {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
            toast("Grant camera permission, then tap again.");
            return;
        }
        Intent i = new Intent(this, CameraPreviewActivity.class);
        i.putExtra("facing", facing);
        startActivity(i);
    }

    private void showLockSetup() {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(24, 8, 24, 8);
        EditText pass = new EditText(this);
        pass.setHint("Set numeric password (4+ digits)");
        pass.setInputType(2);
        box.addView(pass);
        EditText text = new EditText(this);
        text.setHint("Text shown on lock screen");
        box.addView(text);
        new android.app.AlertDialog.Builder(this)
            .setTitle("Configure app lock")
            .setView(box)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save & Lock", (d, w) -> {
                String p = pass.getText().toString().trim();
                String t = text.getText().toString().trim();
                if (p.length() < 4) {
                    toast("Password must be at least 4 digits.");
                    return;
                }
                getSharedPreferences("lock_settings", MODE_PRIVATE).edit()
                    .putString("password", p)
                    .putString("lock_text", t.isEmpty() ? "App locked" : t)
                    .apply();
                startActivity(new Intent(this, LockScreenActivity.class));
            }).show();
    }

    @Override public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);
        if (requestCode == CAMERA_PERMISSION && results.length > 0 && results[0] == PackageManager.PERMISSION_GRANTED) findFlashCamera();
        else if (requestCode == CAMERA_PERMISSION) toast("Camera permission denied.");
    }

    @Override protected void onStop() {
        super.onStop();
        if (flashOn && cameraManager != null && flashCameraId != null) {
            try { cameraManager.setTorchMode(flashCameraId, false); } catch (Exception ignored) {}
            flashOn = false;
            if (flashStatus != null) flashStatus.setText("Flash: OFF");
        }
    }

    private void toast(String message) { Toast.makeText(this, message, Toast.LENGTH_SHORT).show(); }
}
