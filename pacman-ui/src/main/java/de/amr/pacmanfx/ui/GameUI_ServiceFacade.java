/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.ui.d2.SpriteAnimationManager;
import de.amr.pacmanfx.ui.dashboard.CommonDashboardID;
import de.amr.pacmanfx.ui.layout.ViewManager;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.assets.PreferencesManager;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import javafx.util.Duration;

import java.util.List;
import java.util.Optional;

public record GameUI_ServiceFacade(
    GameContext gameContext,
    ConfigurationsManager configurations,
    FlashMessageManager flashMessages,
    GameSceneManager gameScenes,
    PreferencesManager prefs,
    SoundManager sounds,
    SpriteAnimationManager sprites,
    TranslationManager translations,
    ViewManager views)
{
    // Model facade

    public <T extends Game> T currentGame() {
        return gameContext.game();
    }

    // UI facade

    public UIConfig getUIConfig(String gameVariantName) {
        return configurations().getOrCreateUIConfig(gameVariantName);
    }

    public UIConfig currentUIConfig() {
        return getUIConfig(gameContext().gameVariantName());
    }

    public void configureDashboard(List<CommonDashboardID> dashboardIDList) {
        views().playView().dashboard().addCommonSections(translations(), dashboardIDList);
    }

    public Optional<GameSoundEffects> _optSoundEffects(String gameVariantName) {
        return configurations().getOrCreateUIConfig(gameVariantName).optSoundEffects();
    }

    public Optional<GameSoundEffects> currentSoundEffects() {
        return configurations().getOrCreateUIConfig(gameContext.gameVariantName()).optSoundEffects();
    }

    /**
     * Displays a fading flash message on screen.
     *
     * @param duration how long the message remains visible before fading
     * @param message  message text (supports {@link String#format})
     * @param args     formatting arguments
     */
    public void showFlashMessage(Duration duration, String message, Object... args) {
        flashMessages().showMessage(String.format(message, args), duration.toSeconds());
    }

    /**
     * Displays a fading flash message using the default duration.
     *
     * @param message message text
     * @param args    formatting arguments
     */
    public void showFlashMessage(String message, Object... args) {
        showFlashMessage(GameUI_Constants.DEFAULT_FLASH_MESSAGE_DURATION, message, args);
    }
}
