/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

import de.amr.games.pacman.Globals;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui._2d.*;
import de.amr.games.pacman.ui.input.ArcadeKeyBinding;
import de.amr.games.pacman.ui.input.JoypadKeyBinding;
import de.amr.games.pacman.ui.input.Keyboard;
import de.amr.games.pacman.ui.sound.GameSound;
import de.amr.games.pacman.uilib.FlashMessageView;
import de.amr.games.pacman.uilib.GameClockFX;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.Globals.assertNotNull;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.ui._2d.GlobalProperties2d.PY_DEBUG_INFO_VISIBLE;
import static de.amr.games.pacman.ui.input.ArcadeKeyBinding.DEFAULT_ARCADE_KEY_BINDING;
import static de.amr.games.pacman.ui.input.JoypadKeyBinding.JOYPAD_CURSOR_KEYS;
import static de.amr.games.pacman.ui.input.JoypadKeyBinding.JOYPAD_WASD;
import static de.amr.games.pacman.uilib.Ufx.createIcon;

/**
 * User interface for all Pac-Man game variants (2D only).
 *
 * @author Armin Reichert
 */
public class PacManGamesUI implements GameEventListener, GameUI {

    private final GameClockFX CLOCK = new GameClockFX();

    @Override
    public GameClockFX clock() {
        return CLOCK;
    }

    private final Keyboard KEYBOARD = new Keyboard();

    @Override
    public Keyboard keyboard() {
        return KEYBOARD;
    }

    private final GameAssets ASSETS = new GameAssets();

    @Override
    public GameAssets assets() {
        return ASSETS;
    }

    private final GameSound SOUND = new GameSound();

    @Override
    public GameSound sound() {
        return SOUND;
    }

    protected final GameAction actionOpenEditorView = new GameAction() {
        @Override
        public void execute() {
            currentGameScene().ifPresent(GameScene::end);
            CLOCK.stop();
            SOUND.stopAll();
            EditorView editorView = getOrCreateEditorView();
            stage.titleProperty().bind(editorView.editor().titleProperty());
            editorView.editor().start();
            viewPy.set(editorView);
        }

        @Override
        public boolean isEnabled() {
            return !THE_GAME_CONTROLLER.game().isPlaying();
        }
    };

    protected final ObjectProperty<GameScene> gameScenePy = new SimpleObjectProperty<>();

    protected final ObjectProperty<View> viewPy = new SimpleObjectProperty<>();

    protected final Map<GameVariant, GameUIConfiguration> uiConfigMap = new EnumMap<>(GameVariant.class);

    protected Stage stage;
    protected Scene mainScene;
    protected final StackPane sceneRoot = new StackPane();

    protected StartPagesCarousel startPagesCarousel;
    protected GameView gameView;
    protected EditorView editorView;
    protected final FlashMessageView flashMessageOverlay = new FlashMessageView();

    protected boolean scoreVisible;

    //TODO maybe this is overdesign
    protected final JoypadKeyBinding[] joypadKeyBindings = new JoypadKeyBinding[] { JOYPAD_CURSOR_KEYS, JOYPAD_WASD };
    protected int joypadIndex;
    protected ArcadeKeyBinding arcadeKeyBinding = DEFAULT_ARCADE_KEY_BINDING;

    public PacManGamesUI() {
        CLOCK.setPauseableCallback(this::runOnEveryTickExceptWhenPaused);
        CLOCK.setPermanentCallback(this::runOnEveryTick);
        viewPy.addListener((py, ov, nv) -> {
            if (ov instanceof GameEventListener oldGameEventListener) {
                THE_GAME_CONTROLLER.game().removeGameEventListener(oldGameEventListener);
            }
            if (nv instanceof GameEventListener newGameEventListener) {
                THE_GAME_CONTROLLER.game().addGameEventListener(newGameEventListener);
            }
            if (ov instanceof View oldView) {
                oldView.unregisterGameActionKeyBindings();
            }
            if (nv instanceof View newView) {
                newView.registerGameActionKeyBindings();
                sceneRoot.getChildren().set(0, newView.node());
                newView.node().requestFocus();
            }
        });
    }

    /**
     * Called from application start method (on JavaFX application thread).
     *
     * @param stage primary stage (window)
     * @param initialSize initial UI size
     */
    @Override
    public void build(Stage stage, Dimension2D initialSize) {
        this.stage = assertNotNull(stage);
        createMainScene(assertNotNull(initialSize));
        createStartPagesCarousel();
        createGameView(mainScene);
        init(THE_GAME_CONTROLLER.selectedGameVariant());
        bindStageTitle();
        stage.setMinWidth(ARCADE_MAP_SIZE_IN_PIXELS.x() * 1.25);
        stage.setMinHeight(ARCADE_MAP_SIZE_IN_PIXELS.y() * 1.25);
        stage.setScene(mainScene);
        stage.centerOnScreen();
        stage.setOnShowing(e -> showStartView());
    }

    /**
     * Stores the UI configuration for a game variant and initializes the game scenes (assigns the game context).
     *
     * @param variant a game variant
     * @param uiConfig the UI configuration for this variant
     */
    public void configure(GameVariant variant, GameUIConfiguration uiConfig) {
        assertNotNull(variant);
        assertNotNull(uiConfig);
        uiConfig.gameScenes().forEach(scene -> {
            if (scene instanceof GameScene2D gameScene2D) {
                gameScene2D.debugInfoVisibleProperty().bind(PY_DEBUG_INFO_VISIBLE);
            }
        });
        uiConfigMap.put(variant, uiConfig);
    }

    @Override
    public void addStartPage(GameVariant gameVariant, StartPage startPage) {
        startPagesCarousel.addStartPage(gameVariant, startPage);
    }

    @Override
    public void addDefaultDashboardItems(String... titles) {
        for (String title : titles) {
            gameView.addDefaultDashboardItem(title);
        }
    }

    @Override
    public void show() {
        stage.show();
    }

    protected void runOnEveryTickExceptWhenPaused() {
        try {
            THE_GAME_CONTROLLER.update();
            currentGameScene().ifPresent(GameScene::update);
            logUpdateResult();
        } catch (Exception x) {
            CLOCK.stop();
            Logger.error(x);
            Logger.error("Something very bad happened, game clock has been stopped!");
        }
    }

    protected void runOnEveryTick() {
        try {
            if (viewPy.get() == gameView) {
                gameView.onTick();
                flashMessageOverlay.update();
            } else {
                Logger.warn("Should not happen: tick received when not on game view");
            }
        } catch (Exception x) {
            CLOCK.stop();
            Logger.error(x);
            Logger.error("Something very bad happened, game clock has been stopped!");
        }
    }

    //TODO use nice font icons instead
    private Pane createIconPane() {
        ImageView mutedIcon = createIcon(ASSETS.get("icon.mute"), 48, SOUND.mutedProperty());
        ImageView autoIcon  = createIcon(ASSETS.get("icon.auto"), 48, GlobalProperties2d.PY_AUTOPILOT);

        var pane = new HBox(autoIcon, mutedIcon);
        pane.setMaxWidth(128);
        pane.setMaxHeight(64);
        pane.visibleProperty().bind(Bindings.createBooleanBinding(() -> viewPy.get() != editorView, viewPy));
        return pane;
    }

    // icons indicating autopilot, mute state
    private void addIconPane() {
        Pane iconPane = createIconPane();
        ImageView pauseIcon = createIcon(ASSETS.get("icon.pause"), 64, CLOCK.pausedProperty());
        pauseIcon.visibleProperty().bind(Bindings.createBooleanBinding(
            () -> viewPy.get() != editorView && CLOCK.isPaused(), viewPy, CLOCK.pausedProperty()));
        StackPane.setAlignment(pauseIcon, Pos.CENTER);
        StackPane.setAlignment(iconPane, Pos.BOTTOM_RIGHT);
        sceneRoot.getChildren().addAll(pauseIcon, iconPane);
    }

    protected void createMainScene(Dimension2D size) {
        sceneRoot.getChildren().addAll(new Pane(), flashMessageOverlay);
        sceneRoot.setBackground(ASSETS.get("background.scene"));
        sceneRoot.backgroundProperty().bind(gameScenePy.map(
            gameScene -> currentGameSceneIsPlayScene3D()
                ? ASSETS.get("background.play_scene3d")
                : ASSETS.get("background.scene"))
        );
        addIconPane();

        mainScene = new Scene(sceneRoot, size.getWidth(), size.getHeight());
        mainScene.widthProperty() .addListener((py,ov,nv) -> gameView.setSize(mainScene.getWidth(), mainScene.getHeight()));
        mainScene.heightProperty().addListener((py,ov,nv) -> gameView.setSize(mainScene.getWidth(), mainScene.getHeight()));

        mainScene.addEventFilter(KeyEvent.KEY_PRESSED, KEYBOARD::onKeyPressed);
        mainScene.addEventFilter(KeyEvent.KEY_RELEASED, KEYBOARD::onKeyReleased);

        mainScene.setOnKeyPressed(keyPress -> {
            if (KEY_FULLSCREEN.match(keyPress)) {
                enterFullScreenMode();
            }
            else if (KEY_MUTE.match(keyPress)) {
                SOUND.toggleMuted();
            }
            else if (KEY_OPEN_EDITOR.match(keyPress)) {
                openEditor();
            }
            else if (viewPy.get() instanceof GameActionProvider actionProvider) {
                actionProvider.handleInput();
            }
        });
    }

    private EditorView getOrCreateEditorView() {
        if (editorView == null) {
            editorView = new EditorView(stage);
            editorView.setCloseAction(editor -> {
                editor.executeWithCheckForUnsavedChanges(this::bindStageTitle);
                editor.stop();
                CLOCK.setTargetFrameRate(Globals.TICKS_PER_SECOND);
                THE_GAME_CONTROLLER.restart(GameState.BOOT);
                showStartView();
            });
        }
        return editorView;
    }

    protected void createStartPagesCarousel() {
        startPagesCarousel = new StartPagesCarousel();
        startPagesCarousel.setBackground(ASSETS.background("background.scene"));
        viewPy.addListener((py, ov, view) -> {
            if (view == startPagesCarousel) {
                startPagesCarousel.currentStartPage().ifPresent(startPage ->
                    startPage.onSelected(THE_GAME_CONTROLLER.selectedGameVariant()));
            }
        });
    }

    protected void createGameView(Scene parentScene) {
        gameView = new GameView(parentScene);
        gameView.gameSceneProperty().bind(gameScenePy);
        gameView.setSize(mainScene.getWidth(), mainScene.getHeight());
    }

    protected void handleGameVariantChange(GameVariant gameVariant) {
        THE_GAME_CONTROLLER.game().addGameEventListener(this); //TODO check this
        GameUIConfiguration uiConfig = uiConfiguration(gameVariant);
        SOUND.selectGameVariant(gameVariant, uiConfig.assetNamespace());
        stage.getIcons().setAll(uiConfig.appIcon());
        gameView.canvasContainer().decorationEnabledPy.set(uiConfig.isGameCanvasDecorated());
    }

    protected void bindStageTitle() {
        stage.titleProperty().bind(Bindings.createStringBinding(
            () -> {
                // "app.title.pacman" vs. "app.title.pacman.paused"
                String key = "app.title." + currentUIConfig().assetNamespace();
                if (CLOCK.isPaused()) { key += ".paused"; }
                if (currentGameScene().isPresent() && currentGameScene().get() instanceof GameScene2D gameScene2D) {
                    return ASSETS.localizedText(key, "2D") + " (%.2fx)".formatted(gameScene2D.scaling());
                }
                return ASSETS.localizedText(key, "2D");
            },
            CLOCK.pausedProperty(), gameScenePy, gameView.heightProperty())
        );
    }

    protected void updateGameScene(boolean reloadCurrent) {
        GameScene prevGameScene = gameScenePy.get();
        GameScene nextGameScene = currentUIConfig().selectGameScene();
        boolean sceneChanging = nextGameScene != prevGameScene;
        if (reloadCurrent || sceneChanging) {
            if (prevGameScene != null) {
                prevGameScene.end();
                Logger.info("Game scene ended: {}", prevGameScene.displayName());
            }
            if (nextGameScene != null) {
                gameView.embedGameScene(nextGameScene);
                nextGameScene.init();
                if (is2D3DPlaySceneSwitch(prevGameScene, nextGameScene)) {
                    nextGameScene.onSceneVariantSwitch(prevGameScene);
                }
            } else {
                Logger.error("Could not determine next game scene");
                return;
            }
            if (sceneChanging) {
                gameScenePy.set(nextGameScene);
                Logger.info("Game scene is now: {}", nextGameScene.displayName());
            }
        }
    }

    private boolean is2D3DPlaySceneSwitch(GameScene oldGameScene, GameScene newGameScene) {
        if (oldGameScene == null && newGameScene == null) {
            Logger.error("WTF is going on here, old and new game scene are NULL!");
            return false;
        }
        if (oldGameScene == null) {
            return false; // first scene
        }
        var cfg = currentUIConfig();
        return cfg.gameSceneHasID(oldGameScene, "PlayScene2D") && cfg.gameSceneHasID(newGameScene, "PlayScene3D")
            || cfg.gameSceneHasID(oldGameScene, "PlayScene3D") && cfg.gameSceneHasID(newGameScene, "PlayScene2D");
    }

    private void logUpdateResult() {
        var messageList = THE_GAME_CONTROLLER.game().eventLog().createMessageList();
        if (!messageList.isEmpty()) {
            Logger.info("Simulation step #{}:", CLOCK.updateCount());
            for (var msg : messageList) {
                Logger.info("- " + msg);
            }
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // GameContext interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public ArcadeKeyBinding arcadeKeys() {
        return arcadeKeyBinding;
    }

    @Override
    public JoypadKeyBinding joypadKeyBinding() {
        return joypadKeyBindings[joypadIndex];
    }

    @Override
    public void selectNextJoypadKeyBinding() {
        joypadIndex = (joypadIndex + 1) % joypadKeyBindings.length;
    }

    @Override
    public void enterFullScreenMode() {
        stage.setFullScreen(true);
    }

    @Override
    public ReadOnlyDoubleProperty heightProperty() {
        return mainScene.heightProperty();
    }

    @Override
    public GameView gameView() {
        return gameView;
    }

    @Override
    public ObjectProperty<GameScene> gameSceneProperty() {
        return gameScenePy;
    }

    @Override
    public GameUIConfiguration uiConfiguration(GameVariant variant) {
        return uiConfigMap.get(variant);
    }

    @Override
    public Optional<GameScene> currentGameScene() {
        return Optional.ofNullable(gameScenePy.get());
    }

    @Override
    public void openEditor() {
        if (actionOpenEditorView.isEnabled()) {
            actionOpenEditorView.execute();
        }
    }

    @Override
    public void togglePlayScene2D3D() {}

    @Override
    public boolean isScoreVisible() {
        return scoreVisible;
    }

    @Override
    public void setScoreVisible(boolean visible) {
        scoreVisible = visible;
    }

    @Override
    public void init(GameVariant gameVariant) {
        THE_GAME_CONTROLLER.selectGameVariant(gameVariant);
        handleGameVariantChange(gameVariant);
    }

    @Override
    public void showStartView() {
        CLOCK.stop();
        gameScenePy.set(null);
        gameView.hideDashboard(); // TODO use binding?
        viewPy.set(startPagesCarousel);
        //TODO this is needed for XXL option menu
        startPagesCarousel.currentStartPage().ifPresent(startPage -> startPage.onSelected(THE_GAME_CONTROLLER.selectedGameVariant()));
    }

    @Override
    public StartPagesCarousel startPageSelectionView() { return startPagesCarousel; }

    @Override
    public void showGameView() {
        viewPy.set(gameView);
        if (!THE_GAME_CONTROLLER.isGameVariantSelected(GameVariant.MS_PACMAN_TENGEN)) {
            SOUND.playVoice("voice.explain", 0);
        }
        gameView.setSize(mainScene.getWidth(), mainScene.getHeight());
        GameActions2D.BOOT.execute();
    }

    @Override
    public void showFlashMessageSec(double seconds, String message, Object... args) {
        flashMessageOverlay.showMessage(String.format(message, args), seconds);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // GameEventListener interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public void onGameEvent(GameEvent event) {
        Logger.trace("Received: {}", event);
        // call event specific hook method:
        GameEventListener.super.onGameEvent(event);
        //TODO this looks like crap
        if (viewPy.get() == gameView) {
            updateGameScene(false);
        }
    }

    @Override
    public void onGameVariantChanged(GameEvent event) {
        handleGameVariantChange(THE_GAME_CONTROLLER.selectedGameVariant());
    }

    @Override
    public void onUnspecifiedChange(GameEvent event) {
        updateGameScene(true);
    }

    @Override
    public void onStopAllSounds(GameEvent event) {
        SOUND.stopAll();
    }
}