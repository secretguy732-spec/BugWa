package com.wabug.control;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class LockScreenActivity extends Activity {
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(0xFF10131A);
        getWindow().setNavigationBarColor(0xFF10131A);
        String lockText = getSharedPreferences("lock_settings", MODE_PRIVATE).getString("lock_text", "App locked");

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(28, 28, 28, 28);
        root.setBackgroundColor(0xFF10131A);

        TextView title = new TextView(this);
        title.setText(lockText);
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        EditText password = new EditText(this);
        password.setSingleLine(true);
        password.setInputType(2);
        password.setHint("Enter password");
        password.setTextColor(0xFFFFFFFF);
        password.setHintTextColor(0xFF9AA5B5);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(-1, -2);
        p.topMargin = 24;
        root.addView(password, p);

        Button unlock = new Button(this);
        unlock.setText("Unlock");
        root.addView(unlock, new LinearLayout.LayoutParams(-1, -2));
        unlock.setOnClickListener(v -> {
            String saved = getSharedPreferences("lock_settings", MODE_PRIVATE).getString("password", "");
            if (!saved.isEmpty() && saved.equals(password.getText().toString())) finish();
            else Toast.makeText(this, "Incorrect password.", Toast.LENGTH_SHORT).show();
        });
        setContentView(root);
    }

    @Override public void onBackPressed() {
        Toast.makeText(this, "Enter the password to unlock this screen.", Toast.LENGTH_SHORT).show();
    }
}
