package com.example.deepseapong;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

public class MainActivity extends Activity {
    private PongView game;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        game = new PongView(this);
        setContentView(game);
        game.setFocusableInTouchMode(true);
        game.requestFocus();
    }

    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (game != null && game.handleRemoteKey(keyCode)) return true;
        return super.onKeyDown(keyCode, event);
    }

    @Override protected void onPause() {
        super.onPause();
        if (game != null) game.pauseGame();
    }

    @Override protected void onResume() {
        super.onResume();
        if (game != null) game.resumeGame();
    }
}
