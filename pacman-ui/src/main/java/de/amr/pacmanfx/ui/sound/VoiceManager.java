/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.sound;

import de.amr.pacmanfx.ui.GameUI;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.tinylog.Logger;

import static java.util.Objects.requireNonNull;

public class VoiceManager {

    private MediaPlayer activePlayer;

    public void play(Media voice) {
        requireNonNull(voice);
        if (activePlayer != null && activePlayer.getMedia().equals(voice)) {
            Logger.warn("Voice {} already playing", voice);
            return;
        }
        activePlayer = new MediaPlayer(voice);
        activePlayer.muteProperty().bind(GameUI.PROPERTY_MUTED);
        activePlayer.play();
    }

    public void stop() {
        if (activePlayer != null) {
            activePlayer.stop();
        }
        activePlayer = null;
    }
}
