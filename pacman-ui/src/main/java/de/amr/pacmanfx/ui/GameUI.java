/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.basics.Named;
import de.amr.basics.ui.assets.AssetMap;
import de.amr.pacmanfx.app.config.GameVariantUIConfig;
import de.amr.pacmanfx.app.runtime.DefaultGameVariantRuntime;
import de.amr.pacmanfx.core.event.GameEvent;
import de.amr.pacmanfx.core.event.GenericChangeEvent;
import de.amr.pacmanfx.core.event.HighScoreAccessErrorEvent;
import de.amr.pacmanfx.core.event.base.GameEventListener;
import de.amr.pacmanfx.core.event.gameplay.LevelCreatedEvent;
import de.amr.pacmanfx.engine.GameVariantRuntime;
import de.amr.pacmanfx.engine.action.ActionBindingsRegistry;
import de.amr.pacmanfx.engine.action.ActionKeyBinding;
import de.amr.pacmanfx.engine.action.DefaultActionBindingsRegistry;
import de.amr.pacmanfx.engine.gamescene.GameScene;
import de.amr.pacmanfx.engine.input.Keyboard;
import de.amr.pacmanfx.ui.action.CommonGameActions;
import de.amr.pacmanfx.ui.gamescene.common.DefaultGameSceneManager;
import de.amr.pacmanfx.ui.settings.ui.GameUISettings;
import de.amr.pacmanfx.ui.viewmodel.GameViewModel;
import de.amr.pacmanfx.ui.views.GameViewID;
import de.amr.pacmanfx.ui.views.playview.GamePlayView;
import de.amr.pacmanfx.ui.window.FlashMessageManager;
import de.amr.pacmanfx.ui.window.GameMainScene;
import de.amr.pacmanfx.ui.window.GameWindow;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.Set;

import static de.amr.pacmanfx.engine.action.ActionExecutor.executeMatchingAction;
import static java.util.Objects.requireNonNull;

public class GameUI implements GameEventListener {

    public static final int MIN_STAGE_WIDTH  = 280;

    public static final int MIN_STAGE_HEIGHT = 360;

    private final GameUISettings settings;

    private final GameWindow window;

    private final GameMainScene mainScene;

    private final FlashMessageManager flashMessageManager;

    private final GameViewModel viewModel;

    private final ActionBindingsRegistry<EngineUIContext> actionBindings = new DefaultActionBindingsRegistry<>("Global Action Bindings");

    private EngineUIContext engine;

    public GameUI(Stage stage, int width, int height, GameUISettings settings) {
        requireNonNull(stage);
        this.settings = requireNonNull(settings);

        flashMessageManager = new FlashMessageManager();
        mainScene = new GameMainScene(width, height, flashMessageManager.messageView().rootPane());
        window = new GameWindow(stage);
        viewModel = new GameViewModel();
        viewModel.init(settings);

        stage.setScene(mainScene);
        stage.setMinWidth(MIN_STAGE_WIDTH);
        stage.setMinHeight(MIN_STAGE_HEIGHT);
    }

    public void setEngine(EngineUIContext engine) {
        this.engine = requireNonNull(engine);

        window.setEngineContext(engine);
        mainScene.setEngineContext(engine);
        engine.soundManager().muteProperty().bind(viewModel.muteProperty());
        connectKeyboard(engine.input().keyboard());
        bindCommonActions();

        Logger.info("UI connected with engine");
        Logger.info(actionBindings);
    }

    public void onEnterGameVariant(GameVariantRuntime runtime) {
        if (!(runtime instanceof DefaultGameVariantRuntime defaultRuntime)) {
            Logger.error("Illegal runtime class: {}", runtime.getClass());
            return;
        }
        final GameVariantUIConfig uiConfig = defaultRuntime.uiConfig();
        uiConfig.load(engine);
        viewModel().maze3DSettings().init(uiConfig.worldSettings().maze());
        engine.gameSceneManager().setGameSceneConfig(uiConfig.gameSceneConfig());
        updateStageIcon(runtime);
    }

    public void onExitGameVariant(GameVariantRuntime runtime) {
        if (!(runtime instanceof DefaultGameVariantRuntime defaultRuntime)) {
            Logger.error("Illegal runtime class: {}", runtime.getClass());
            return;
        }
        final GameVariantUIConfig uiConfig = defaultRuntime.uiConfig();
        uiConfig.unload(engine);
    }

    public void updateStageIcon(GameVariantRuntime runtime) {
        if (!(runtime instanceof DefaultGameVariantRuntime defaultRuntime)) {
            Logger.error("Illegal runtime class: {}", runtime.getClass());
            return;
        }
        final AssetMap assets = defaultRuntime.uiConfig().assets();
        final Image icon = assets.image("app_icon");
        if (icon != null) {
            window().stage().getIcons().setAll(icon);
        }
    }

    @Override
    public void onGameEvent(GameEvent gameEvent) {
        boolean forceGameSceneReload = false;
        switch (gameEvent) {
            case LevelCreatedEvent levelCreatedEvent -> {
                final GameScene currentGameScene = engine.gameSceneManager().currentGameScene();
                final GamePlayView playView = engine.viewManager().assertView(GameViewID.GAMEPLAY, GamePlayView.class);
                playView.acceptLevel(currentGameScene, levelCreatedEvent.level());
            }

            case GenericChangeEvent _ -> forceGameSceneReload = true;

            case HighScoreAccessErrorEvent failure -> {
                showShortMessage(Duration.seconds(5), "Accessing high score failed!\n%s", failure.reason().getMessage());
                return;
            }

            default -> {}
        }

        if (engine.gameSceneManager() instanceof DefaultGameSceneManager defaultGameSceneManager) {
            defaultGameSceneManager.updateGameSceneAndForceReload(engine, forceGameSceneReload);
        }
        engine.gameSceneManager().optCurrentGameScene()
            .flatMap(GameScene::optGameEventHandler)
            .ifPresent(handler -> handler.onGameEvent(gameEvent));
    }

    // --- Accessors ---

    public GameUISettings settings() {
        return settings;
    }

    public GameViewModel viewModel() {
        return viewModel;
    }

    public GameWindow window() {
        return window;
    }

    public GameMainScene mainScene() {
        return mainScene;
    }

    // --- General commands ---

    public void startShortMessageSystem() {
        flashMessageManager.startAnimationTimer();
    }

    public void stopShortMessageSystem() {
        flashMessageManager.stopAnimationTimer();
    }

    /**
     * Displays a "flash message" that disappears automatically after a given time.
     *
     * @param duration how long the message remains visible before fading
     * @param message  message text (supports {@link String#format})
     * @param args     formatting arguments
     */
    public void showShortMessage(Duration duration, String message, Object... args) {
        requireNonNull(duration);
        requireNonNull(message);
        flashMessageManager.showMessage(message.formatted(args), duration.toSeconds());
    }

    /**
     * Displays a flash message using the default duration.
     *
     * @param message message text
     * @param args    formatting arguments
     */
    public void showShortMessage(String message, Object... args) {
        showShortMessage(viewModel.flashMessageDurationProperty().get(), message, args);
    }

    public void clearShortMessage() {
        flashMessageManager.clearMessage();
    }

    // private

    private void connectKeyboard(Keyboard keyboard) {
        keyboard.enabledProperty().bind(engine.viewManager().currentViewIDProperty().map(GameUI::viewAcceptsKeyboardInput));
        keyboard.addStateListener(this::handleKeyboardStateChange);
        keyboard.filterKeyEventsFrom(mainScene);
    }

    private void handleKeyboardStateChange(Keyboard keyboard) {
        if (keyboard.anyNormalKeyPressed()) { // ignore modifier state change
            final Named currentViewID = engine.viewManager().currentViewID();
            if (viewAcceptsKeyboardInput(currentViewID)) {
                // Check for matching "global" action first, if none, let current view handle it.
                if (executeMatchingAction(actionBindings, engine).isEmpty()) {
                    engine.viewManager().assertView(currentViewID).onInput(engine.input());
                }
            }
        }
    }

    //TODO improve
    private static boolean viewAcceptsKeyboardInput(Named viewID) {
        return viewID == GameViewID.START_PAGES || viewID == GameViewID.GAMEPLAY;
    }

    private void bindCommonActions() {
        final Set<ActionKeyBinding<EngineUIContext>> commonActionBindings = CommonGameActions.instance().bindings();
        actionBindings.selectAnyMatchingBinding(CommonGameActions.instance().uiSettingsActions().actionToggleKeyboardMonitor(), commonActionBindings);
        actionBindings.selectAnyMatchingBinding(CommonGameActions.instance().uiSettingsActions().actionEnterFullScreen(), commonActionBindings);
        actionBindings.selectAnyMatchingBinding(CommonGameActions.instance().simulationActions().actionToggleMuted(), commonActionBindings);
        actionBindings.selectAnyMatchingBinding(CommonGameActions.instance().editorActions().actionOpenEditor(), commonActionBindings);
    }
}
