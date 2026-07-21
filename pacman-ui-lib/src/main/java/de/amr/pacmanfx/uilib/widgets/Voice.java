/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.widgets;

import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public class Voice {

    private final BooleanProperty mute = new SimpleBooleanProperty(false);

    private MediaPlayer mediaPlayer;
    private final Animation delayedReplay;

    public Voice() {
        delayedReplay = new PauseTransition(Duration.seconds(1));
    }

    public BooleanProperty muteProperty() {
        return mute;
    }

    public void playAfterSec(double seconds, Media media) {
        stop();

        delayedReplay.setDelay(Duration.seconds(seconds));
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.muteProperty().bind(muteProperty());
        delayedReplay.setOnFinished(_ -> mediaPlayer.play());
        delayedReplay.play();
    }

    public void stop() {
        delayedReplay.stop();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer = null;
        }
    }
}
