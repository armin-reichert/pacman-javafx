/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.basics.json.JsonLoader;
import de.amr.pacmanfx.core.event.GameEvent;
import de.amr.pacmanfx.core.event.GenericChangeEvent;
import de.amr.pacmanfx.core.event.HighScoreAccessErrorEvent;
import de.amr.pacmanfx.core.event.base.GameEventListener;
import de.amr.pacmanfx.core.event.gameplay.GameStateChangeEvent;
import de.amr.pacmanfx.core.event.gameplay.LevelCreatedEvent;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.ui.action.CommonGameActions;
import de.amr.pacmanfx.ui.action.core.ActionBindingsRegistry;
import de.amr.pacmanfx.ui.action.core.ActionKeyBinding;
import de.amr.pacmanfx.ui.action.core.GameActionBindingsRegistry;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d2.SpriteAnimationTimer;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.ui.settings.ui.GameUISettings;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.ui.views.GameViewID;
import de.amr.pacmanfx.ui.views.GameViewManager;
import de.amr.pacmanfx.ui.views.dashboard.DashboardFactory;
import de.amr.pacmanfx.ui.views.editor.EditorView;
import de.amr.pacmanfx.ui.views.playview.GamePlayView;
import de.amr.pacmanfx.ui.views.startpages.StartPagesView;
import de.amr.pacmanfx.ui.vm.GameViewModel;
import de.amr.pacmanfx.ui.window.GameWindow;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
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
            throw new IllegalArgumentException("Could not load default UI settings file from path '%s'".formatted(DEFAULT_UI_SETTINGS_PATH));
        }
        final var settings = JsonLoader.load(url, GameUISettings.class);
        Logger.info("Default UI settings loaded, URL={}", url);
        return settings;
    }

    public static final GameUISettings DEFAULT_UI_SETTINGS = loadDefaultSettings();

    private final GameWindow window;
    private final GameViewManager viewManager;
    private final TranslationManager translationManager;
    private final SoundManager soundManager;
    private final SpriteAnimationTimer spriteAnimationTimer;
    private final GameViewModel viewModel;
    private final ActionBindingsRegistry actionBindings = new GameActionBindingsRegistry("Global Action Bindings");

    private GameAppContext app;

    public GameUI(Stage stage, int width, int height, GameUISettings settings, DashboardFactory dashboardFactory) {
        viewModel = new GameViewModel();
        viewModel.init(settings);

        spriteAnimationTimer = new SpriteAnimationTimer();
        window = new GameWindow(stage, width, height);

        soundManager = new SoundManager();
        soundManager.muteProperty().bind(viewModel.muteProperty());

        translationManager = new CommonTranslationManager();

        viewManager = createViewManager();

        //TODO Check this
        viewManager.gamePlayView().populateDashboard(dashboardFactory, settings.dashboard(), translationManager);
    }

    @Override
    public void onGameEvent(GameEvent gameEvent) {
        boolean forceGameSceneReload = false;
        switch (gameEvent) {
            case LevelCreatedEvent e -> viewManager.gamePlayView().onLevelCreated(e.level());
            case GameStateChangeEvent e -> {
                if (CommonGameStateID.GAME_LEVEL_COMPLETE.hasSameNameAs(e.newState())) {
                    viewManager.gamePlayView().onLevelCompleted();
                }
            }
            case GenericChangeEvent _ -> forceGameSceneReload = true;
            case HighScoreAccessErrorEvent failure -> {
                shortMessage(Duration.seconds(5), "Accessing high score failed!\n%s", failure.reason().getMessage());
                return;
            }
            default -> {}
        }
        app.gameSceneManager().updateGameSceneAndForceReload(app, forceGameSceneReload);
        app.gameSceneManager().optCurrentGameScene().ifPresent(gameScene -> gameScene.onGameEvent(gameEvent));
    }


    public void connectWithApp(GameAppContext app) {
        this.app = requireNonNull(app);

        viewManager.setGameApp(app);
        window.setGameApp(app);

        connectKeyboard(app.input().keyboard());
        bindCommonActions(app.commonActions());

        Logger.info("UI connected with application");
        Logger.info(actionBindings);
    }

    // --- Accessors ---

    public SoundManager soundManager() {
        return soundManager;
    }

    public SpriteAnimationTimer spriteAnimTimer() {
        return spriteAnimationTimer;
    }

    public TranslationManager translationManager() {
        return translationManager;
    }

    public GameViewManager viewManager() {
        return viewManager;
    }

    public GameViewModel viewModel() {
        return viewModel;
    }

    public GameWindow window() {
        return window;
    }

    public void terminate() {
        spriteAnimationTimer.stop();
        window.mainScene().flashMessageManager().stopAnimationTimer();
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

    private static GameViewManager createViewManager() {
        final var manager = new GameViewManager();
        manager.registerView(GameViewID.START_PAGES, new StartPagesView());
        manager.registerView(GameViewID.GAMEPLAY, new GamePlayView());
        manager.registerView(GameViewID.EDITOR, new EditorView());
        return manager;
    }

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
                if (actionBindings.executeMatchingAction(app).isEmpty()) {
                    viewManager.assertView(currentViewID).onInput(app);
                }
            }
        }
    }

    //TODO improve
    private static boolean viewAcceptsKeyboardInput(GameViewID viewID) {
        return viewID == GameViewID.START_PAGES || viewID == GameViewID.GAMEPLAY;
    }

    private void bindCommonActions(CommonGameActions actions) {
        final Set<ActionKeyBinding> bindings = actions.bindings();
        actionBindings.selectAnyMatchingBinding(actions.uiSettingsActions().actionToggleKeyboardMonitor(), bindings);
        actionBindings.selectAnyMatchingBinding(actions.uiSettingsActions().actionEnterFullScreen(), bindings);
        actionBindings.selectAnyMatchingBinding(actions.simulationActions().actionToggleMuted(), bindings);
        actionBindings.selectAnyMatchingBinding(actions.editorActions().actionOpenEditor(), bindings);
    }
}
