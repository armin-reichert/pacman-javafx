/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.ui.config.UISettings;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneManager;
import de.amr.pacmanfx.ui.gamescene.d2.SpriteAnimationManager;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.ui.views.GameViewManager;
import de.amr.pacmanfx.ui.window.GameWindow;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import javafx.util.Duration;

import static java.util.Objects.requireNonNull;

public record GameUI(
    GameWindow window,
    GameViewManager views,
    GameSceneManager gameScenes,
    TranslationManager translations,
    SoundManager sounds,
    SpriteAnimationManager sprites,
    UISettings settings)
{

    public void connect(Game game) {
        window.connect(game);
        views.connect(game);
        gameScenes.connect(game);
        sounds.connect(game);
    }

    public void terminate() {
        sprites.stopAnimationTimer();
        sprites.animations().clear();
        window.mainScene().flashMessageManager().stopAnimationTimer();
    }

    /**
     * Displays a flash message.
     *
     * @param duration how long the message remains visible before fading
     * @param message  message text (supports {@link String#format})
     * @param args     formatting arguments
     */
    public void shortMessage(Duration duration, String message, Object... args) {
        requireNonNull(duration);
        requireNonNull(message);
        window.mainScene().flashMessageManager().showMessage(message.formatted(args), duration.toSeconds());
    }

    /**
     * Displays a flash message using the default duration.
     *
     * @param message message text
     * @param args    formatting arguments
     */
    public void shortMessage(String message, Object... args) {
        shortMessage(settings.flashMessageDuration(), message, args);
    }

    public void clearMessage() {
        window.mainScene().flashMessageManager().clearMessage();
    }
}
