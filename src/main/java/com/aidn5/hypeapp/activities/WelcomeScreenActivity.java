package com.aidn5.hypeapp.activities;

import android.os.Bundle;
import android.view.View;

import com.aidn5.hypeapp.R;

public class WelcomeScreenActivity extends ActivityUI {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.welcome_screen);

        showStartView();
    }

    private void showStartView() {
        final View layout = findViewById(R.id.welcome_start_view);
        findViewById(R.id.welcome_start_continue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                layout.animate().translationY(layout.getHeight() + 100).setDuration(300).start();
            }
        });
    }
}
