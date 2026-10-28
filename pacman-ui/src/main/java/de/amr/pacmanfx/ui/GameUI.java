/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.engine.action.ActionBindingsRegistry;
import de.amr.pacmanfx.engine.action.ActionKeyBinding;
import de.amr.pacmanfx.engine.action.DefaultActionBindingsRegistry;
import de.amr.pacmanfx.engine.input.Keyboard;
import de.amr.pacmanfx.ui.action.CommonGameActions;
import de.amr.pacmanfx.ui.settings.ui.GameUISettings;
import de.amr.pacmanfx.ui.viewmodel.GameViewModel;
import de.amr.pacmanfx.ui.views.GameView;
import de.amr.pacmanfx.ui.views.GameViewManager;
import de.amr.pacmanfx.ui.window.FlashMessageManager;
import de.amr.pacmanfx.ui.window.MainScene;
import de.amr.pacmanfx.ui.window.StageTitleManager;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.Set;

import static de.amr.pacmanfx.engine.action.ActionExecutor.executeMatchingAction;
import static java.util.Objects.requireNonNull;

public class GameUI {

    public static final int MIN_STAGE_WIDTH  = 280;

    public static final int MIN_STAGE_HEIGHT = 360;

    private final GameUISettings settings;

    private final Stage stage;

    private final MainScene mainScene;

    private final FlashMessageManager flashMessageManager;

    private final GameViewModel viewModel;

    private final ActionBindingsRegistry<EngineUIContext> actionBindings = new DefaultActionBindingsRegistry<>("Global Action Bindings");

    private StageTitleManager stageTitleManager;

    public GameUI(Stage stage, int width, int height, GameUISettings settings) {
        this.stage = requireNonNull(stage);
        this.settings = requireNonNull(settings);

        flashMessageManager = new FlashMessageManager();

        viewModel = new GameViewModel();
        viewModel.init(settings);

        mainScene = new MainScene(width, height, flashMessageManager.messageView().rootPane());

        stage.setScene(mainScene.scene());
        stage.setMinWidth(MIN_STAGE_WIDTH);
        stage.setMinHeight(MIN_STAGE_HEIGHT);
    }

    public void setEngine(EngineUIContext engine) {
        stageTitleManager = new StageTitleManager(engine, stage);
        mainScene.setEngine(engine);
        engine.soundManager().muteProperty().bind(viewModel.muteProperty());
        connectKeyboard(engine);
        bindCommonActions();

        Logger.info("UI connected with engine");
        Logger.info(actionBindings);
    }

    public void initForGameVariant(EngineUIContext engine, GameVariantUIConfig uiConfig) {
        uiConfig.load(engine);
        viewModel.maze3DSettings().init(uiConfig.worldSettings().maze());
        updateAppIcon(uiConfig);
    }

    // --- Accessors ---

    public Stage stage() {
        return stage;
    }

    public GameUISettings settings() {
        return settings;
    }

    public GameViewModel viewModel() {
        return viewModel;
    }

    public MainScene mainScene() {
        return mainScene;
    }

    public StageTitleManager stageTitleManager() {
        return stageTitleManager;
    }

    // --- General commands ---

    public void show(GameVariantUIConfig uiConfig) {
        updateAppIcon(uiConfig);
        stage.centerOnScreen();
        stage.show();
    }

    public void setFullScreen(boolean value) {
        stage.setFullScreen(value);
    }

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
    public void showMessage(Duration duration, String message, Object... args) {
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
    public void showMessage(String message, Object... args) {
        showMessage(viewModel.flashMessageDurationProperty().get(), message, args);
    }

    public void clearShortMessage() {
        flashMessageManager.clearMessage();
    }

    // private

    private void connectKeyboard(EngineUIContext engine) {
        final Keyboard keyboard = engine.input().keyboard();
        keyboard.enabledProperty().bind(engine.viewManager().currentViewProperty()
            .map(view -> viewAcceptsKeyboardInput(engine.viewManager(), view)));
        keyboard.addStateListener(kb -> handleKeyboardStateChange(engine, kb));
        keyboard.filterKeyEventsFrom(mainScene.scene());
    }

    private void handleKeyboardStateChange(EngineUIContext engine, Keyboard keyboard) {
        if (keyboard.anyNormalKeyPressed()) { // ignore modifier state change
            final GameView currentView = engine.viewManager().currentView();
            if (viewAcceptsKeyboardInput(engine.viewManager(), currentView)) {
                // Check for matching "global" action first, if none, let current view handle it.
                if (executeMatchingAction(actionBindings, engine).isEmpty()) {
                    engine.viewManager().currentView().onInput(engine.input());
                }
            }
        }
    }

    //TODO improve
    private static boolean viewAcceptsKeyboardInput(GameViewManager viewManager, GameView view) {
        return view == viewManager.startPagesView() || view == viewManager.gamePlayView();
    }

    private void bindCommonActions() {
        final Set<ActionKeyBinding<EngineUIContext>> commonActionBindings = CommonGameActions.instance().bindings();
        actionBindings.selectAnyMatchingBinding(CommonGameActions.instance().uiSettingsActions().actionToggleKeyboardMonitor(), commonActionBindings);
        actionBindings.selectAnyMatchingBinding(CommonGameActions.instance().uiSettingsActions().actionEnterFullScreen(), commonActionBindings);
        actionBindings.selectAnyMatchingBinding(CommonGameActions.instance().simulationActions().actionToggleMuted(), commonActionBindings);
        actionBindings.selectAnyMatchingBinding(CommonGameActions.instance().editorActions().actionOpenEditor(), commonActionBindings);
    }

    private void updateAppIcon(GameVariantUIConfig uiConfig) {
        final Image appIcon = uiConfig.assets().image("app_icon");
        if (appIcon != null) {
            stage.getIcons().setAll(appIcon);
        }
    }
}
