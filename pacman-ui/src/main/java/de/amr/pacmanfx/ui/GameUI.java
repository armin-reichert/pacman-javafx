/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.basics.json.JsonLoader;
import de.amr.pacmanfx.core.event.GameEvent;
import de.amr.pacmanfx.core.event.GenericChangeEvent;
import de.amr.pacmanfx.core.event.HighScoreAccessErrorEvent;
import de.amr.pacmanfx.core.event.base.GameEventListener;
import de.amr.pacmanfx.core.event.gameplay.LevelCreatedEvent;
import de.amr.pacmanfx.ui.action.CommonGameActions;
import de.amr.pacmanfx.ui.action.core.ActionBindingsRegistry;
import de.amr.pacmanfx.ui.action.core.ActionKeyBinding;
import de.amr.pacmanfx.ui.action.core.GameActionBindingsRegistry;
import de.amr.pacmanfx.ui.action.core.PacManGamesEngine;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.ui.settings.ui.GameUISettings;
import de.amr.pacmanfx.ui.viewmodel.GameViewModel;
import de.amr.pacmanfx.ui.views.GameViewID;
import de.amr.pacmanfx.ui.views.GameViewManager;
import de.amr.pacmanfx.ui.views.dashboard.DashboardFactory;
import de.amr.pacmanfx.ui.views.editor.EditorView;
import de.amr.pacmanfx.ui.views.playview.GamePlayView;
import de.amr.pacmanfx.ui.views.startpages.StartPagesView;
import de.amr.pacmanfx.ui.window.GameWindow;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.net.URL;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class GameUI implements GameEventListener {

    public static final String DEFAULT_UI_SETTINGS_PATH = "/de/amr/pacmanfx/ui/ui.json";

    private static GameUISettings loadDefaultSettings() {
        final URL url = GameUI.class.getResource(DEFAULT_UI_SETTINGS_PATH);
        if (url == null) {
            throw new IllegalArgumentException("Could not load default UI settings file from path '%s'"
                .formatted(DEFAULT_UI_SETTINGS_PATH));
        }
        final var settings = JsonLoader.load(url, GameUISettings.class);
        Logger.info("Default UI settings loaded, URL={}", url);
        return settings;
    }

    public static final GameUISettings DEFAULT_UI_SETTINGS = loadDefaultSettings();

    private final GameWindow window;

    private final GameViewManager viewManager;

    private final GameViewModel viewModel;

    private final ActionBindingsRegistry actionBindings = new GameActionBindingsRegistry("Global Action Bindings");

    private final GameUISettings uiSettings;

    private PacManGamesEngine engine;

    public GameUI(Stage stage, int width, int height, GameUISettings uiSettings) {
        requireNonNull(stage);
        this.uiSettings = requireNonNull(uiSettings);

        window = new GameWindow(stage, width, height);

        viewModel = new GameViewModel();
        viewModel.init(uiSettings);

        viewManager = new GameViewManager();
        viewManager.registerView(GameViewID.START_PAGES, new StartPagesView());
        viewManager.registerView(GameViewID.GAMEPLAY, new GamePlayView());
        viewManager.registerView(GameViewID.EDITOR, new EditorView());
    }

    public void connectEngine(PacManGamesEngine engine, DashboardFactory dashboardFactory) {
        this.engine = requireNonNull(engine);

        viewManager.setGameApp(engine);
        viewManager.gamePlayView().dashboard().populate(dashboardFactory, uiSettings.dashboard(), engine.translationManager());

        window.setGameApp(engine);

        engine.soundManager().muteProperty().bind(viewModel.muteProperty());

        connectKeyboard(engine.input().keyboard());
        bindCommonActions();

        Logger.info("UI connected with engine");
        Logger.info(actionBindings);
    }

    @Override
    public void onGameEvent(GameEvent gameEvent) {
        boolean forceGameSceneReload = false;
        switch (gameEvent) {
            case LevelCreatedEvent levelCreatedEvent -> {
                final GameScene currentGameScene = engine.gameSceneManager().currentGameScene();
                viewManager.gamePlayView().acceptLevel(currentGameScene, levelCreatedEvent.level());
            }

            case GenericChangeEvent _ -> forceGameSceneReload = true;

            case HighScoreAccessErrorEvent failure -> {
                shortMessage(Duration.seconds(5), "Accessing high score failed!\n%s", failure.reason().getMessage());
                return;
            }

            default -> {}
        }

        if (engine != null) {
            engine.gameSceneManager().updateGameSceneAndForceReload(engine, forceGameSceneReload);
            engine.gameSceneManager().optCurrentGameScene()
                .flatMap(GameScene::optGameEventHandler)
                .ifPresent(handler -> handler.onGameEvent(gameEvent));
        }
        else {
            Logger.error("Cannot update and reload game scene: UI not yet connected with app");
        }
    }

    // --- Accessors ---

    public GameViewManager viewManager() {
        return viewManager;
    }

    public GameViewModel viewModel() {
        return viewModel;
    }

    public GameWindow window() {
        return window;
    }

    // --- General commands ---

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
        shortMessage(viewModel.flashMessageDurationProperty().get(), message, args);
    }

    public void clearMessage() {
        window.mainScene().flashMessageManager().clearMessage();
    }

    // private

    private void connectKeyboard(Keyboard keyboard) {
        keyboard.enabledProperty().bind(viewManager.currentViewIDProperty().map(GameUI::viewAcceptsKeyboardInput));
        keyboard.addStateListener(this::handleKeyboardStateChange);
        keyboard.filterKeyEventsFrom(window.mainScene());
    }

    private void handleKeyboardStateChange(Keyboard keyboard) {
        if (keyboard.anyNormalKeyPressed()) { // ignore modifier state change
            final GameViewID currentViewID = viewManager.currentViewID();
            if (viewAcceptsKeyboardInput(currentViewID)) {
                // Check for matching "global" action first, if none, let current view handle it.
                if (actionBindings.executeMatchingAction(engine).isEmpty()) {
                    viewManager.reqView(currentViewID).onInput(engine);
                }
            }
        }
    }

    //TODO improve
    private static boolean viewAcceptsKeyboardInput(GameViewID viewID) {
        return viewID == GameViewID.START_PAGES || viewID == GameViewID.GAMEPLAY;
    }

    private void bindCommonActions() {
        final Set<ActionKeyBinding> commonActionBindings = CommonGameActions.instance().bindings();
        actionBindings.selectAnyMatchingBinding(CommonGameActions.instance().uiSettingsActions().actionToggleKeyboardMonitor(), commonActionBindings);
        actionBindings.selectAnyMatchingBinding(CommonGameActions.instance().uiSettingsActions().actionEnterFullScreen(), commonActionBindings);
        actionBindings.selectAnyMatchingBinding(CommonGameActions.instance().simulationActions().actionToggleMuted(), commonActionBindings);
        actionBindings.selectAnyMatchingBinding(CommonGameActions.instance().editorActions().actionOpenEditor(), commonActionBindings);
    }
}
