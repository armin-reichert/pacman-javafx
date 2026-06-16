/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.ui.gamescene.d2.SpriteAnimationManager;
import de.amr.pacmanfx.ui.gamescene.d3.UISettings3D;
import de.amr.pacmanfx.ui.game.UISettings;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneManager;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.ui.views.GameViewManager;
import de.amr.pacmanfx.ui.window.GameWindow;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import javafx.util.Duration;

import static java.util.Objects.requireNonNull;

public record GameUI(
    GameSceneManager gameScenes,
    SoundManager sounds,
    SpriteAnimationManager sprites,
    TranslationManager translations,
    GameWindow window,
    GameViewManager views,
    UISettings settings,
    UISettings3D settings3D)
{

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
        window().mainScene().flashMessageManager().showMessage(message.formatted(args), duration.toSeconds());
    }

    /**
     * Displays a flash message using the default duration.
     *
     * @param message message text
     * @param args    formatting arguments
     */
    public void shortMessage(String message, Object... args) {
        shortMessage(settings().flashMessageDuration(), message, args);
    }

    public void clearMessage() {
        window().mainScene().flashMessageManager().clearMessage();
    }
}
