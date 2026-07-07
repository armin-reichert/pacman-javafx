/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.ui.config.ui.GameUISettings;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.game.GlobalGameEventHandler;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneManager;
import de.amr.pacmanfx.ui.gamescene.d2.SpriteAnimationManager;
import de.amr.pacmanfx.ui.model.GameViewModel;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.ui.views.GameViewManager;
import de.amr.pacmanfx.ui.window.GameWindow;
import de.amr.pacmanfx.uilib.SettingsLoader;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import javafx.util.Duration;

import static java.util.Objects.requireNonNull;

public class GameUI {

    public static final GameUISettings DEFAULT_SETTINGS =
        SettingsLoader.load(GameUI.class.getResource("/de/amr/pacmanfx/ui/ui.json"), GameUISettings.class);

    private final GameWindow window;
    private final GameViewManager viewManager;
    private final GameSceneManager gameSceneManager;
    private final TranslationManager translations;
    private final SoundManager sounds;
    private final SpriteAnimationManager sprites;
    private final GameViewModel viewModel;

    private GlobalGameEventHandler gameEventHandler;

    public GameUI(GameWindow window, GameViewManager viewManager, GameSceneManager gameSceneManager, TranslationManager translations, SoundManager sounds, SpriteAnimationManager sprites, GameViewModel viewModel) {
        this.window = window;
        this.viewManager = viewManager;
        this.gameSceneManager = gameSceneManager;
        this.translations = translations;
        this.sounds = sounds;
        this.sprites = sprites;
        this.viewModel = viewModel;
    }

    public GameWindow window() {
        return window;
    }

    public GameViewManager viewManager() {
        return viewManager;
    }

    public GameSceneManager gameSceneManager() {
        return gameSceneManager;
    }

    public TranslationManager translations() {
        return translations;
    }

    public SoundManager sounds() {
        return sounds;
    }

    public SpriteAnimationManager sprites() {
        return sprites;
    }

    public GameViewModel viewModel() {
        return viewModel;
    }

    public GlobalGameEventHandler gameEventHandler() {
        return gameEventHandler;
    }

    public void connect(Game game) {
        sounds.connect(game);
        gameSceneManager.connect(game);
        viewManager.connect(game);
        window.connect(game);

        gameEventHandler = new GlobalGameEventHandler(game);
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
        shortMessage(viewModel.flashMessageDurationProperty.get(), message, args);
    }

    public void clearMessage() {
        window.mainScene().flashMessageManager().clearMessage();
    }


}
