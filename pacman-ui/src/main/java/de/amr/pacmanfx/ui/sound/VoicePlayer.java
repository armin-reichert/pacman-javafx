/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.sound;

import de.amr.pacmanfx.ui.GameUI;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.tinylog.Logger;

import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class VoicePlayer {

    private static final float FADE_OUT_SECONDS = 1;

    private Set<MediaPlayer> players = new HashSet<>();

    public void play(Media voice) {
        requireNonNull(voice);
        Logger.info("Play voice");
        for (MediaPlayer player : players) {
            if (player.getMedia().equals(voice)) {
                Logger.warn("Voice already playing");
                return;
            }
        }
        var player = new MediaPlayer(voice);
        player.muteProperty().bind(GameUI.PROPERTY_MUTED);
        player.play();
        players.add(player);
    }

    public void stop() {
        players.forEach(MediaPlayer::stop);
        players.clear();
    }
}
