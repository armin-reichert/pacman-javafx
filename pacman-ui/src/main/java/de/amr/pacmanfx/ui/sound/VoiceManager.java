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

    private MediaPlayer player;

    public void playVoice(Media voiceMedia) {
        requireNonNull(voiceMedia);
        if (player != null && player.getMedia().equals(voiceMedia)) {
            Logger.warn("Voice {} already playing", voiceMedia);
            return;
        }
        stopVoice();
        player = new MediaPlayer(voiceMedia);
        player.muteProperty().bind(GameUI.PROPERTY_MUTED);
        player.play();
    }

    public void stopVoice() {
        if (player != null) {
            player.stop();
        }
        player = null;
    }
}
