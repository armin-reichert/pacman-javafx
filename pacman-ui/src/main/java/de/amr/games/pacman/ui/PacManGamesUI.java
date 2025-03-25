/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui._2d.*;
import de.amr.games.pacman.ui.input.ArcadeKeyBinding;
import de.amr.games.pacman.ui.input.JoypadKeyBinding;
import de.amr.games.pacman.ui.input.Keyboard;
import de.amr.games.pacman.uilib.FlashMessageView;
import de.amr.games.pacman.uilib.Picker;
import de.amr.games.pacman.uilib.ResourceManager;
import de.amr.games.pacman.uilib.Ufx;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.Globals.assertNotNull;
import static de.amr.games.pacman.controller.GameController.TICKS_PER_SECOND;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.ui.UIGlobals.*;
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
public class PacManGamesUI implements GameEventListener, GameContext {

    private static final KeyCodeCombination KEY_FULLSCREEN = Keyboard.naked(KeyCode.F11);
    private static final KeyCodeCombination KEY_MUTE = Keyboard.alt(KeyCode.M);
    private static final KeyCodeCombination KEY_OPEN_EDITOR = Keyboard.shift_alt(KeyCode.E);

    protected final GameAction actionOpenEditorView = new GameAction() {
        @Override
        public void execute() {
            currentGameScene().ifPresent(GameScene::end);
            THE_CLOCK.stop();
            THE_SOUND.stopAll();
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

    protected final ObjectProperty<GameVariant> gameVariantPy = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() { handleGameVariantChange(get()); }
    };

    protected final ObjectProperty<GameScene> gameScenePy = new SimpleObjectProperty<>();

    protected final ObjectProperty<Node> viewPy = new SimpleObjectProperty<>();

    protected final Map<GameVariant, GameUIConfiguration> uiConfigMap = new EnumMap<>(GameVariant.class);

    protected Stage stage;
    protected Scene mainScene;
    protected final StackPane sceneRoot = new StackPane();

    protected StartPageSelectionView startPageSelectionView;
    protected GameView gameView;
    protected EditorView editorView;
    protected final FlashMessageView flashMessageOverlay = new FlashMessageView();

    protected boolean scoreVisible;
    protected Picker<String> pickerForGameOverTexts;
    protected Picker<String> pickerForLevelCompleteTexts;

    //TODO maybe this is overdesign
    protected final JoypadKeyBinding[] joypadKeyBindings = new JoypadKeyBinding[] { JOYPAD_CURSOR_KEYS, JOYPAD_WASD };
    protected int joypadIndex;
    protected ArcadeKeyBinding arcadeKeyBinding = DEFAULT_ARCADE_KEY_BINDING;

    public PacManGamesUI() {
        THE_CLOCK.setPauseableCallback(this::runOnEveryTickExceptWhenPaused);
        THE_CLOCK.setPermanentCallback(this::runOnEveryTick);
        viewPy.addListener((py, oldView, newView) -> {
            if (oldView instanceof GameActionProvider oldActionProvider) {
                oldActionProvider.unregisterGameActionKeyBindings();
            }
            if (newView instanceof GameActionProvider newActionProvider) {
                newActionProvider.registerGameActionKeyBindings();
            }
            if (oldView instanceof GameEventListener oldGameEventListener) {
                THE_GAME_CONTROLLER.game().removeGameEventListener(oldGameEventListener);
            }
            if (newView instanceof GameEventListener newGameEventListener) {
                THE_GAME_CONTROLLER.game().addGameEventListener(newGameEventListener);
            }
            sceneRoot.getChildren().set(0, newView);
            newView.requestFocus();
        });
    }

    /**
     * Called from application start method (on JavaFX application thread).
     *
     * @param stage primary stage (window)
     * @param initialSize initial UI size
     */
    public void create(Stage stage, Dimension2D initialSize) {
        this.stage = assertNotNull(stage);
        mainScene = createMainScene(assertNotNull(initialSize));
        startPageSelectionView = new StartPageSelectionView();
        startPageSelectionView().setBackground(THE_ASSETS.background("background.scene"));
        createGameView(mainScene);
        setGameVariant(THE_GAME_CONTROLLER.selectedGameVariant());
        bindStageTitle();
        setGlobalKeyboardShortcuts();
        stage.setMinWidth(ARCADE_MAP_SIZE_IN_PIXELS.x() * 1.25);
        stage.setMinHeight(ARCADE_MAP_SIZE_IN_PIXELS.y() * 1.25);
        stage.setScene(mainScene);
        stage.centerOnScreen();
        stage.setOnShowing(e -> showStartView());
    }

    public void loadAssets2D() {
        ResourceManager rm = () -> PacManGamesUI.class;

        ResourceBundle textResources = rm.getModuleBundle("de.amr.games.pacman.ui.texts.messages2d");
        THE_ASSETS.addBundle(textResources);

        pickerForGameOverTexts = Picker.fromBundle(textResources, "game.over");
        pickerForLevelCompleteTexts = Picker.fromBundle(textResources, "level.complete");

        THE_ASSETS.store("background.scene",  Ufx.imageBackground(rm.loadImage("graphics/pacman_wallpaper.png")));
        THE_ASSETS.store("background.play_scene3d", Ufx.imageBackground(rm.loadImage("graphics/blue_sky.jpg")));

        THE_ASSETS.store("font.arcade",             rm.loadFont("fonts/emulogic.ttf", 8));
        THE_ASSETS.store("font.handwriting",        rm.loadFont("fonts/Molle-Italic.ttf", 9));
        THE_ASSETS.store("font.monospaced",         rm.loadFont("fonts/Inconsolata_Condensed-Bold.ttf", 12));

        THE_ASSETS.store("icon.auto",               rm.loadImage("graphics/icons/auto.png"));
        THE_ASSETS.store("icon.mute",               rm.loadImage("graphics/icons/mute.png"));
        THE_ASSETS.store("icon.pause",              rm.loadImage("graphics/icons/pause.png"));

        THE_ASSETS.store("voice.explain",           rm.url("sound/voice/press-key.mp3"));
        THE_ASSETS.store("voice.autopilot.off",     rm.url("sound/voice/autopilot-off.mp3"));
        THE_ASSETS.store("voice.autopilot.on",      rm.url("sound/voice/autopilot-on.mp3"));
        THE_ASSETS.store("voice.immunity.off",      rm.url("sound/voice/immunity-off.mp3"));
        THE_ASSETS.store("voice.immunity.on",       rm.url("sound/voice/immunity-on.mp3"));
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

    public Stage stage() { return stage; }

    protected void runOnEveryTickExceptWhenPaused() {
        try {
            THE_GAME_CONTROLLER.update();
            currentGameScene().ifPresent(GameScene::update);
            logUpdateResult();
        } catch (Exception x) {
            THE_CLOCK.stop();
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
            THE_CLOCK.stop();
            Logger.error(x);
            Logger.error("Something very bad happened, game clock has been stopped!");
        }
    }

    //TODO use nice font icons instead
    private Pane createIconPane() {
        ImageView mutedIcon = createIcon(THE_ASSETS.get("icon.mute"), 48, THE_SOUND.mutedProperty());
        ImageView autoIcon  = createIcon(THE_ASSETS.get("icon.auto"), 48, GlobalProperties2d.PY_AUTOPILOT);

        var pane = new HBox(autoIcon, mutedIcon);
        pane.setMaxWidth(128);
        pane.setMaxHeight(64);
        pane.visibleProperty().bind(Bindings.createBooleanBinding(() -> viewPy.get() != editorView, viewPy));
        return pane;
    }

    protected Scene createMainScene(Dimension2D size) {
        Pane iconPane = createIconPane();

        ImageView pauseIcon = createIcon(THE_ASSETS.get("icon.pause"), 64, THE_CLOCK.pausedProperty());
        pauseIcon.visibleProperty().bind(Bindings.createBooleanBinding(
            () -> viewPy.get() != editorView && THE_CLOCK.isPaused(), viewPy, THE_CLOCK.pausedProperty()));

        StackPane.setAlignment(iconPane, Pos.BOTTOM_RIGHT);
        StackPane.setAlignment(pauseIcon, Pos.CENTER);

        sceneRoot.getChildren().addAll(new Pane(), flashMessageOverlay, pauseIcon, iconPane);
        sceneRoot.setBackground(THE_ASSETS.get("background.scene"));
        sceneRoot.backgroundProperty().bind(gameScenePy.map(
            gameScene -> currentGameSceneIsPlayScene3D()
                ? THE_ASSETS.get("background.play_scene3d")
                : THE_ASSETS.get("background.scene"))
        );

        Scene mainScene = new Scene(sceneRoot, size.getWidth(), size.getHeight());
        mainScene.widthProperty() .addListener((py,ov,nv) -> gameView.setSize(mainScene.getWidth(), mainScene.getHeight()));
        mainScene.heightProperty().addListener((py,ov,nv) -> gameView.setSize(mainScene.getWidth(), mainScene.getHeight()));

        mainScene.addEventFilter(KeyEvent.KEY_PRESSED, THE_KEYBOARD::onKeyPressed);
        mainScene.addEventFilter(KeyEvent.KEY_RELEASED, THE_KEYBOARD::onKeyReleased);
        return mainScene;
    }

    protected void setGlobalKeyboardShortcuts() {
        mainScene.setOnKeyPressed(keyPress -> {
            if (KEY_FULLSCREEN.match(keyPress)) {
                enterFullScreenMode();
            }
            else if (KEY_MUTE.match(keyPress)) {
                THE_SOUND.toggleMuted();
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
                THE_CLOCK.setTargetFrameRate(TICKS_PER_SECOND);
                THE_GAME_CONTROLLER.restart(GameState.BOOT);
                showStartView();
            });
        }
        return editorView;
    }

    protected void createGameView(Scene parentScene) {
        gameView = new GameView(parentScene);
        gameView.gameSceneProperty().bind(gameScenePy);
        gameView.setSize(mainScene.getWidth(), mainScene.getHeight());
    }

    protected void handleGameVariantChange(GameVariant gameVariant) {
        THE_GAME_CONTROLLER.game().removeGameEventListener(this);
        THE_GAME_CONTROLLER.selectGameVariant(gameVariant);
        THE_GAME_CONTROLLER.game().addGameEventListener(this);
        THE_SOUND.selectGameVariant(gameVariant, gameConfiguration().assetNamespace());
        stage.getIcons().setAll(gameConfiguration().appIcon());
        //TODO check this
        gameView.canvasContainer().decorationEnabledPy.set(gameConfiguration().isGameCanvasDecorated());
        Logger.info("Game variant changed to {}", gameVariant);
    }

    protected void bindStageTitle() {
        stage.titleProperty().bind(Bindings.createStringBinding(
            () -> {
                // "app.title.pacman" vs. "app.title.pacman.paused"
                String key = "app.title." + gameConfiguration().assetNamespace();
                if (THE_CLOCK.isPaused()) { key += ".paused"; }
                if (currentGameScene().isPresent() && currentGameScene().get() instanceof GameScene2D gameScene2D) {
                    return THE_ASSETS.localizedText(key, "2D") + " (%.2fx)".formatted(gameScene2D.scaling());
                }
                return THE_ASSETS.localizedText(key, "2D");
            },
            THE_CLOCK.pausedProperty(), gameVariantPy, gameScenePy, gameView.heightProperty())
        );
    }

    protected void updateGameScene(boolean reloadCurrent) {
        GameScene prevGameScene = gameScenePy.get();
        GameScene nextGameScene = gameConfiguration().selectGameScene();
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
        var cfg = gameConfiguration();
        return cfg.gameSceneHasID(oldGameScene, "PlayScene2D") && cfg.gameSceneHasID(newGameScene, "PlayScene3D")
            || cfg.gameSceneHasID(oldGameScene, "PlayScene3D") && cfg.gameSceneHasID(newGameScene, "PlayScene2D");
    }

    private void logUpdateResult() {
        var messageList = THE_GAME_CONTROLLER.game().eventLog().createMessageList();
        if (!messageList.isEmpty()) {
            Logger.info("Simulation step #{}:", THE_CLOCK.updateCount());
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
    public String localizedGameOverMessage() {
        return pickerForGameOverTexts.next();
    }

    @Override
    public String localizedLevelCompleteMessage(int levelNumber) {
        return pickerForLevelCompleteTexts.next() + "\n\n" + THE_ASSETS.localizedText("level_complete", levelNumber);
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
    public ObjectProperty<Node> viewProperty() { return viewPy; }

    @Override
    public GameView gameView() {
        return gameView;
    }

    @Override
    public ObjectProperty<GameScene> gameSceneProperty() {
        return gameScenePy;
    }

    @Override
    public GameVariant gameVariant() {
        return gameVariantPy.get();
    }

    @Override
    public GameUIConfiguration gameConfiguration(GameVariant variant) {
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
    public void setGameVariant(GameVariant variant) {
        gameVariantPy.set(variant);
    }

    @Override
    public void showStartView() {
        THE_CLOCK.stop();
        gameScenePy.set(null);
        gameView.hideDashboard(); // TODO use binding?
        viewPy.set(startPageSelectionView);
        // Note: this must be called last such that option menu gets focus!
        startPageSelectionView.currentSlide().ifPresent(Node::requestFocus);
    }

    @Override
    public StartPageSelectionView startPageSelectionView() { return startPageSelectionView; }

    @Override
    public void showGameView() {
        viewPy.set(gameView);
        if (gameVariant() != GameVariant.MS_PACMAN_TENGEN) {
            THE_SOUND.playVoice("voice.explain", 0);
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
        gameVariantPy.set(THE_GAME_CONTROLLER.selectedGameVariant());
    }

    @Override
    public void onUnspecifiedChange(GameEvent event) {
        updateGameScene(true);
    }

    @Override
    public void onStopAllSounds(GameEvent event) {
        THE_SOUND.stopAll();
    }
}