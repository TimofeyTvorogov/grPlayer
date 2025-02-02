package com.grplayer;

import javafx.animation.PauseTransition;
import javafx.util.Duration;

public abstract class CustomMouseEventHandler {
//    private final ImageView button;
    private final PauseTransition holdTimer;
    private boolean longPressTriggered;

    public CustomMouseEventHandler() {
        holdTimer = new PauseTransition(Duration.seconds(0.5));
        holdTimer.setOnFinished(event -> {
            longPress();
            longPressTriggered = true;
        });
    }

    public void handleMousePressed() {
        longPressTriggered = false;
        holdTimer.playFromStart();
    }

    public void handleMouseReleased() {
        if (!longPressTriggered) {
            holdTimer.stop();
            shortClick();
            return;
        }
        longPressRelease();

    }

    abstract void longPressRelease();
    abstract void longPress();

    abstract void shortClick();


}
