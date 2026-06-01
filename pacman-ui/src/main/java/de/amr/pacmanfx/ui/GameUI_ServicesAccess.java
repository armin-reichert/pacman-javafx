/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.basics.fsm.State;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameFlow;
import de.amr.pacmanfx.ui.config.ConfigurationsManager;
import de.amr.pacmanfx.ui.config.UIConfig;
import de.amr.pacmanfx.ui.d2.SpriteAnimationManager;
import de.amr.pacmanfx.ui.gamescene.GameSceneManager;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.ui.subviews.SubViewManager;
import de.amr.pacmanfx.uilib.assets.PreferencesManager;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import javafx.util.Duration;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public record GameUI_ServicesAccess(
    GameContext gameContext,
    GameClock gameClock,
    DirectoryWatchdog customDirWatchdog,
    ConfigurationsManager configurations,
    FlashMessageManager flashMessages,
    GameSceneManager gameScenes,
    PreferencesManager prefs,
    SoundManager sounds,
    SpriteAnimationManager sprites,
    TranslationManager translations,
    SubViewManager subViews)
{
    // Model facade

    public String currentGameVariant() {
        return gameContext.gameVariantName();
    }

    public <T extends Game> T currentGame() {
        return gameContext.game();
    }

    public GameFlow currentGameFlow() {
        return currentGame().flow();
    }

    public State<Game> currentGameState() {
        return currentGameFlow().state();
    }

    // UI facade

    public UIConfig currentUIConfig() {
        return configurations.getOrCreateUIConfig(gameContext().gameVariantName());
    }

    public Optional<GameSoundEffects> currentSoundEffects() {
        return currentUIConfig().optSoundEffects();
    }

    /**
     * Displays a flash message.
     *
     * @param duration how long the message remains visible before fading
     * @param message  message text (supports {@link String#format})
     * @param args     formatting arguments
     */
    public void flashMessage(Duration duration, String message, Object... args) {
        requireNonNull(duration);
        requireNonNull(message);
        flashMessages.showMessage(message.formatted(args), duration.toSeconds());
    }

    /**
     * Displays a flash message using the default duration.
     *
     * @param message message text
     * @param args    formatting arguments
     */
    public void flashMessage(String message, Object... args) {
        flashMessage(GameUI_Constants.DEFAULT_FLASH_MESSAGE_DURATION, message, args);
    }
}
