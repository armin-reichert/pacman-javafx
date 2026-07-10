/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.ui.action.CommonActions;
import de.amr.pacmanfx.ui.action.core.ActionBindingsRegistry;
import de.amr.pacmanfx.ui.action.core.ActionKeyBinding;
import de.amr.pacmanfx.ui.action.core.GameActionBindingsMap;
import de.amr.pacmanfx.ui.config.ui.DashboardSectionSettings;
import de.amr.pacmanfx.ui.config.ui.GameUISettings;
import de.amr.pacmanfx.ui.game.Game;
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
import de.amr.pacmanfx.uilib.SettingsLoader;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class GameUI implements GameEventListener {

    public static final GameUISettings DEFAULT_UI_SETTINGS = SettingsLoader.load(
        GameUI.class.getResource("/de/amr/pacmanfx/ui/ui.json"), GameUISettings.class);

    private final GameWindow window;
    private final GameViewManager viewManager;
    private final GameSceneManager gameSceneManager;
    private final TranslationManager translations;
    private final SoundManager sounds;
    private final SpriteAnimationManager sprites;
    private final GameViewModel viewModel;
    private final ActionBindingsRegistry actionBindings = new GameActionBindingsMap("Global Action Bindings");

    private Game game;

    public GameUI(Stage stage, int width, int height, GameUISettings settings, DashboardFactory dashboardFactory) {
        viewModel = new GameViewModel();
        gameSceneManager = new GameSceneManager();
        translations = new GameTranslationManager();
        viewManager = createGameViewManager(settings.dashboard(), dashboardFactory, translations);
        sounds = new SoundManager();
        sprites = new SpriteAnimationManager(60);
        window = new GameWindow(stage, width, height);

        viewModel.init(settings);
        sounds.muteProperty().bind(viewModel.mutedProperty);
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

    public void connect(Game game) {
        this.game = requireNonNull(game);

        sounds.connect(game);
        gameSceneManager.connect(game);
        viewManager.connect(game);
        window.connect(game);

        connectKeyboard();
        bindCommonActions();
    }

    public void terminate() {
        sprites.stopAnimationTimer();
        sprites.animationContainer().clear();
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
            case LevelCreatedEvent e -> viewManager.gamePlayView().onLevelCreated(e.level());
            case GameStateChangeEvent e -> {
                if (GameStateID.GAME_LEVEL_COMPLETE.identifies(e.newState())) {
                    viewManager.gamePlayView().onLevelCompleted();
                }
            }
            case GenericChangeEvent _ -> forceGameSceneReload = true;
            default -> {}
        }
        gameSceneManager.updateGameSceneAndForceReload(forceGameSceneReload);
        gameSceneManager.optCurrentGameScene()
            .ifPresent(gameScene -> gameScene.gameEventHandler().onGameEvent(gameEvent));
    }

    // private

    private GameViewManager createGameViewManager(
        List<DashboardSectionSettings> dashboardSectionSettings,
        DashboardFactory dashboardFactory,
        TranslationManager translationManager)
    {
        final GamePlayView playView = new GamePlayView();
        playView.populateDashboard(dashboardFactory, dashboardSectionSettings, translationManager);

        final GameViewManager viewManager = new GameViewManager();
        viewManager.registerView(GameViewID.START_PAGES, new StartPagesView());
        viewManager.registerView(GameViewID.GAMEPLAY, playView);
        viewManager.registerView(GameViewID.EDITOR, new EditorView());

        return viewManager;
    }

    private void connectKeyboard() {
        final Keyboard keyboard = game.machine().input().keyboard();
        keyboard.enabledProperty().bind(viewManager.currentViewIDProperty().map(GameUI::isViewAcceptingKeyboardInput));
        keyboard.addStateListener(_ -> handleKeyboardStateChange());
        keyboard.filterKeyEventsFrom(window.mainScene());
    }

    private void handleKeyboardStateChange() {
        final Input input = game.machine().input();
        if (input.keyboard().anyNormalKeyPressed()) { // ignore modifier state change
            final GameViewID currentViewID = viewManager.currentViewID();
            if (isViewAcceptingKeyboardInput(currentViewID)) {
                // Check for matching "global" action first, if none, let current view handle it.
                if (actionBindings.executeMatchingAction(input).isEmpty()) {
                    viewManager.assertView(currentViewID).onInput(input);
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
