/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.core.event.*;
import de.amr.pacmanfx.core.state.GameStateID;
import de.amr.pacmanfx.ui.action.CommonActions;
import de.amr.pacmanfx.ui.action.core.ActionBindingsRegistry;
import de.amr.pacmanfx.ui.action.core.ActionKeyBinding;
import de.amr.pacmanfx.ui.action.core.GameActionBindingsMap;
import de.amr.pacmanfx.ui.config.ui.GameUISettings;
import de.amr.pacmanfx.ui.game.PacManGamesCollection;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneManager;
import de.amr.pacmanfx.ui.gamescene.d2.SpriteAnimationManager;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.ui.model.GameViewModel;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.ui.views.GameViewID;
import de.amr.pacmanfx.ui.views.GameViewManager;
import de.amr.pacmanfx.ui.views.dashboard.DashboardFactory;
import de.amr.pacmanfx.ui.views.editor.EditorView;
import de.amr.pacmanfx.ui.views.playview.GamePlayView;
import de.amr.pacmanfx.ui.views.startpages.StartPagesView;
import de.amr.pacmanfx.ui.window.GameWindow;
import de.amr.pacmanfx.uilib.JsonConfigLoader;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.net.URL;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class GameUI implements GameEventListener {

    public static final String DEFAULT_SETTINGS_PATH = "/de/amr/pacmanfx/ui/ui.json";

    private static GameUISettings loadDefaultSettings() {
        final URL url = GameUI.class.getResource(DEFAULT_SETTINGS_PATH);
        if (url == null) {
            throw new IllegalArgumentException("Could not load default UI settings file from path '%s'".formatted(DEFAULT_SETTINGS_PATH));
        }
        final var settings = JsonConfigLoader.load(url, GameUISettings.class);
        Logger.info("Default UI settings loaded, URL={}", url);
        return settings;
    }

    public static final GameUISettings DEFAULT_UI_SETTINGS = loadDefaultSettings();

    private final GameWindow window;
    private final GameViewManager views;
    private final GameSceneManager gameScenes;
    private final TranslationManager translations;
    private final SoundManager sounds;
    private final SpriteAnimationManager sprites;
    private final GameViewModel viewModel;
    private final ActionBindingsRegistry actionBindings = new GameActionBindingsMap("Global Action Bindings");

    private PacManGamesCollection game;

    public GameUI(Stage stage, int width, int height, GameUISettings settings, DashboardFactory dashboardFactory) {
        viewModel = new GameViewModel();
        viewModel.init(settings);

        sprites = new SpriteAnimationManager();
        window = new GameWindow(stage, width, height);
        gameScenes = new GameSceneManager();
        translations = new CommonTranslationManager();

        views = createGameViews();
        views.gamePlayView().populateDashboard(dashboardFactory, settings.dashboard(), translations);

        sounds = new SoundManager();
        sounds.muteProperty().bind(viewModel.mutedProperty);
    }

    public GameWindow window() {
        return window;
    }

    public GameViewManager views() {
        return views;
    }

    public GameSceneManager gameScenes() {
        return gameScenes;
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

    public void connect(PacManGamesCollection game) {
        this.game = requireNonNull(game);

        sounds.connect(game);
        gameScenes.connect(game);
        views.connect(game);
        window.connect(game);

        connectKeyboard();
        bindCommonActions();
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

    @Override
    public void onGameEvent(GameEvent gameEvent) {
        boolean forceGameSceneReload = false;
        switch (gameEvent) {
            case LevelCreatedEvent e -> views.gamePlayView().onLevelCreated(e.level());
            case GameStateChangeEvent e -> {
                if (GameStateID.GAME_LEVEL_COMPLETE.identifies(e.newState())) {
                    views.gamePlayView().onLevelCompleted();
                }
            }
            case GenericChangeEvent _ -> forceGameSceneReload = true;
            default -> {}
        }
        gameScenes.updateGameSceneAndForceReload(forceGameSceneReload);
        gameScenes.optCurrentGameScene().ifPresent(gameScene -> gameScene.gameEventHandler().onGameEvent(gameEvent));
    }

    // private

    private GameViewManager createGameViews()
    {
        final GameViewManager views = new GameViewManager();
        views.registerView(GameViewID.START_PAGES, new StartPagesView());
        views.registerView(GameViewID.GAMEPLAY, new GamePlayView());
        views.registerView(GameViewID.EDITOR, new EditorView());
        return views;
    }

    private void connectKeyboard() {
        final Keyboard keyboard = game.machine().input().keyboard();
        keyboard.enabledProperty().bind(views.currentViewIDProperty().map(GameUI::isViewAcceptingKeyboardInput));
        keyboard.addStateListener(_ -> handleKeyboardStateChange());
        keyboard.filterKeyEventsFrom(window.mainScene());
    }

    private void handleKeyboardStateChange() {
        final Input input = game.machine().input();
        if (input.keyboard().anyNormalKeyPressed()) { // ignore modifier state change
            final GameViewID currentViewID = views.currentViewID();
            if (isViewAcceptingKeyboardInput(currentViewID)) {
                // Check for matching "global" action first, if none, let current view handle it.
                if (actionBindings.executeMatchingAction(input).isEmpty()) {
                    views.assertView(currentViewID).onInput(input);
                }
            }
        }
    }

    private static boolean isViewAcceptingKeyboardInput(GameViewID viewID) {
        return viewID == GameViewID.START_PAGES || viewID == GameViewID.GAMEPLAY;
    }

    private void bindCommonActions() {
        final CommonActions actions = game.actions();
        final Set<ActionKeyBinding> bindings = actions.bindings();
        actionBindings.selectAnyMatchingBinding(actions.uiSettingsActions().actionToggleKeyboardMonitor(), bindings);
        actionBindings.selectAnyMatchingBinding(actions.uiSettingsActions().actionEnterFullScreen(), bindings);
        actionBindings.selectAnyMatchingBinding(actions.simulationActions().actionToggleMuted(), bindings);
        actionBindings.selectAnyMatchingBinding(actions.editorActions().actionOpenEditor(), bindings);
        Logger.info(actionBindings);
    }
}
