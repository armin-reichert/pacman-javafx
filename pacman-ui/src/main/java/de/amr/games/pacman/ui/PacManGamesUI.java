/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui._2d.*;
import de.amr.games.pacman.ui.dashboard.InfoBox;
import de.amr.games.pacman.ui.input.ArcadeKeyBinding;
import de.amr.games.pacman.ui.input.JoypadKeyBinding;
import de.amr.games.pacman.ui.input.Keyboard;
import de.amr.games.pacman.ui.sound.GameSound;
import de.amr.games.pacman.uilib.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
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

import static de.amr.games.pacman.controller.GameController.TICKS_PER_SECOND;
import static de.amr.games.pacman.lib.Globals.assertNotNull;
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
public class PacManGamesUI implements GameEventListener, GameContext {

    protected final GameAction actionOpenEditorView = new GameAction() {
        @Override
        public void execute(GameContext context) {
            context.currentGameScene().ifPresent(GameScene::end);
            context.sound().stopAll();
            context.gameClock().stop();
            EditorView editorView = getOrCreateEditorView();
            editorView.startEditor(context.level().world().map());
            showView(editorView);
        }

        @Override
        public boolean isEnabled(GameContext context) {
            return (context.gameVariant() == GameVariant.PACMAN_XXL || context.gameVariant() == GameVariant.MS_PACMAN_XXL)
                && context.game().level().isPresent()
                && context.level().world() != null;
        }
    };

    protected final ObjectProperty<GameVariant> gameVariantPy = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            GameVariant gameVariant = get();
            Logger.info("Game variant changed to {}", gameVariant);
            handleGameVariantChange(gameVariant);
        }
    };

    protected final ObjectProperty<GameScene> gameScenePy = new SimpleObjectProperty<>();

    protected final ObjectProperty<Node> viewPy = new SimpleObjectProperty<>() {
        @Override
        protected void invalidated() {
            Node view = get();
            view.requestFocus();
            Logger.info("Request focus for view {}", view);
        }
    };

    protected final Map<GameVariant, GameUIConfiguration> uiConfigByVariant = new EnumMap<>(GameVariant.class);
    protected final Keyboard keyboard = new Keyboard();
    protected final GameClockFX clock = new GameClockFX();
    protected final AssetStorage assets = new AssetStorage();
    protected final GameSound gameSound = new GameSound();

    protected Stage stage;
    protected Scene mainScene;
    protected final StackPane sceneRoot = new StackPane();

    protected StartPageSelectionView startPageSelectionView;
    protected GameView gameView;
    protected EditorView editorView;
    protected final FlashMessageView flashMessageOverlay = new FlashMessageView();

    protected boolean scoreVisible;
    protected Picker<String> textPickerGameOverTexts;
    protected Picker<String> textPickerLevelCompleteTexts;

    //TODO maybe this is overdesign
    protected final JoypadKeyBinding[] joypadKeyBindings = new JoypadKeyBinding[] { JOYPAD_CURSOR_KEYS, JOYPAD_WASD };
    protected int selectedJoypadIndex;
    protected ArcadeKeyBinding arcadeKeyBinding = DEFAULT_ARCADE_KEY_BINDING;

    public PacManGamesUI() {
        clock.setPauseableCallback(this::runIfNotPausedOnEveryTick);
        clock.setPermanentCallback(this::runOnEveryTick);
        loadAssets2D();
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
        startPageSelectionView = new StartPageSelectionView(this);
        startPagesCarousel().setBackground(assets.background("background.scene"));
        createGameView(mainScene);
        selectGameVariant(gameController().currentGameVariant());
        bindStageTitle();
        stage.setScene(mainScene);
        //TODO This doesn't fit for NES aspect ratio
        stage.setMinWidth(ARCADE_MAP_SIZE_IN_PIXELS.x() * 1.25);
        stage.setMinHeight(ARCADE_MAP_SIZE_IN_PIXELS.y() * 1.25);
        stage.centerOnScreen();
        stage.setOnShowing(e -> showStartView());
    }

    private void loadAssets2D() {
        ResourceManager rm = () -> PacManGamesUI.class;

        ResourceBundle textResources = rm.getModuleBundle("de.amr.games.pacman.ui.texts.messages2d");
        assets.addBundle(textResources);

        textPickerGameOverTexts      = Picker.fromBundle(textResources, "game.over");
        textPickerLevelCompleteTexts = Picker.fromBundle(textResources, "level.complete");

        assets.store("background.scene",  Ufx.imageBackground(rm.loadImage("graphics/pacman_wallpaper.png")));
        assets.store("background.play_scene3d", Ufx.imageBackground(rm.loadImage("graphics/blue_sky.jpg")));

        assets.store("font.arcade",             rm.loadFont("fonts/emulogic.ttf", 8));
        assets.store("font.handwriting",        rm.loadFont("fonts/Molle-Italic.ttf", 9));
        assets.store("font.monospaced",         rm.loadFont("fonts/Inconsolata_Condensed-Bold.ttf", 12));

        assets.store("icon.auto",               rm.loadImage("graphics/icons/auto.png"));
        assets.store("icon.mute",               rm.loadImage("graphics/icons/mute.png"));
        assets.store("icon.pause",              rm.loadImage("graphics/icons/pause.png"));

        assets.store("voice.explain",           rm.url("sound/voice/press-key.mp3"));
        assets.store("voice.autopilot.off",     rm.url("sound/voice/autopilot-off.mp3"));
        assets.store("voice.autopilot.on",      rm.url("sound/voice/autopilot-on.mp3"));
        assets.store("voice.immunity.off",      rm.url("sound/voice/immunity-off.mp3"));
        assets.store("voice.immunity.on",       rm.url("sound/voice/immunity-on.mp3"));

        gameSound.setAssets(assets);
    }

    /**
     * Stores the UI configuration for a game variant and initializes the game scenes (assigns the game context).
     *
     * @param variant a game variant
     * @param uiConfiguration the UI configuration for this variant
     */
    public void setConfiguration(GameVariant variant, GameUIConfiguration uiConfiguration) {
        assertNotNull(variant);
        assertNotNull(uiConfiguration);
        uiConfiguration.gameScenes().forEach(scene -> {
            scene.setGameContext(this);
            if (scene instanceof GameScene2D gameScene2D) {
                gameScene2D.debugInfoVisibleProperty().bind(PY_DEBUG_INFO_VISIBLE);
            }
        });
        uiConfigByVariant.put(variant, uiConfiguration);
    }

    public void show() {
        stage.show();
    }

    public Scene mainScene() {
        return mainScene;
    }

    public StartPageSelectionView startPagesCarousel() { return startPageSelectionView; }

    public ObjectProperty<Node> viewProperty() { return viewPy; }

    public ObjectProperty<GameVariant> gameVariantProperty() { return gameVariantPy; }

    public void addStartPage(GameVariant variant, StartPage page) {
        startPageSelectionView.addStartPage(variant, page);
    }

    protected void runIfNotPausedOnEveryTick() {
        try {
            gameController().update();
            currentGameScene().ifPresent(GameScene::update);
            logUpdateResult();
        } catch (Exception x) {
            clock.stop();
            Logger.error("Something very bad happened, game clock stopped!");
            Logger.error(x);
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
            clock.stop();
            Logger.error("Something very bad happened, game clock stopped!");
            Logger.error(x);
        }
    }

    public void stop() {
        clock.stop();
    }

    private Pane createIconPane() {
        ImageView mutedIcon = createIcon(assets.get("icon.mute"), 48, sound().mutedProperty());
        ImageView autoIcon  = createIcon(assets.get("icon.auto"), 48, GlobalProperties2d.PY_AUTOPILOT);

        var pane = new HBox(autoIcon, mutedIcon);
        pane.setMaxWidth(128);
        pane.setMaxHeight(64);
        pane.visibleProperty().bind(Bindings.createBooleanBinding(() -> viewPy.get() != editorView, viewPy));
        return pane;
    }

    protected Scene createMainScene(Dimension2D size) {
        Pane iconPane = createIconPane();

        ImageView pauseIcon = createIcon(assets.get("icon.pause"), 64, clock.pausedPy);
        pauseIcon.visibleProperty().bind(Bindings.createBooleanBinding(
            () -> viewPy.get() != editorView && clock.isPaused(), viewPy, clock.pausedPy));

        StackPane.setAlignment(iconPane, Pos.BOTTOM_RIGHT);
        StackPane.setAlignment(pauseIcon, Pos.CENTER);

        sceneRoot.getChildren().addAll(new Pane(), flashMessageOverlay, pauseIcon, iconPane);
        sceneRoot.setBackground(assets.get("background.scene"));
        sceneRoot.backgroundProperty().bind(gameScenePy.map(
            gameScene -> currentGameSceneHasID("PlayScene3D")
                ? assets.get("background.play_scene3d")
                : assets.get("background.scene"))
        );

        Scene mainScene = new Scene(sceneRoot, size.getWidth(), size.getHeight());
        mainScene.addEventFilter(KeyEvent.KEY_PRESSED, keyboard::onKeyPressed);
        mainScene.addEventFilter(KeyEvent.KEY_RELEASED, keyboard::onKeyReleased);
        // Global keyboard shortcuts
        mainScene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.F11) {
                stage.setFullScreen(true);
            }
            else if (e.isAltDown() && e.getCode() == KeyCode.M) {
                sound().toggleMuted();
            }
            else {
                if (viewPy.get() instanceof GameActionProvider actionProvider) {
                    actionProvider.handleInput(this);
                }
            }
        });

        mainScene.widthProperty() .addListener((py,ov,nv) -> gameView.setSize(mainScene.getWidth(), mainScene.getHeight()));
        mainScene.heightProperty().addListener((py,ov,nv) -> gameView.setSize(mainScene.getWidth(), mainScene.getHeight()));

        return mainScene;
    }

    private EditorView getOrCreateEditorView() {
        if (editorView == null) {
            editorView = new EditorView(stage, this);
            editorView.setCloseAction(editor -> {
                editor.executeWithCheckForUnsavedChanges(this::bindStageTitle);
                editor.stop();
                clock.setTargetFrameRate(TICKS_PER_SECOND);
                gameController().restart(GameState.BOOT);
                showStartView();
            });
        }
        return editorView;
    }

    protected void createGameView(Scene parentScene) {
        gameView = new GameView(this, parentScene);
        gameView.gameSceneProperty().bind(gameScenePy);
    }

    public <I extends InfoBox> I getDashboardItem(DashboardItemID id) {
        return gameView.dashboard().getItem(id);
    }

    protected void handleGameVariantChange(GameVariant variant) {
        gameController().selectGame(variant);

        GameModel game = gameController().gameModel(variant);
        game.addGameEventListener(this);

        String assetNamespace = gameConfiguration().assetNamespace();
        Image icon = assets.image(assetNamespace + ".icon");
        if (icon != null) {
            stage.getIcons().setAll(icon);
        }
        try {
            sound().useSoundsForGameVariant(variant, gameConfiguration().assetNamespace());
        } catch (Exception x) {
            Logger.error(x);
        }
        gameView.canvasContainer().decorationEnabledPy.set(gameConfiguration().isGameCanvasDecorated());
    }

    protected void bindStageTitle() {
        stage.titleProperty().bind(Bindings.createStringBinding(
            () -> {
                // "app.title.pacman" vs. "app.title.pacman.paused"
                String key = "app.title." + gameConfiguration().assetNamespace();
                if (clock.isPaused()) { key += ".paused"; }
                if (currentGameScene().isPresent() && currentGameScene().get() instanceof GameScene2D gameScene2D) {
                    return locText(key, "2D") + " (%.2fx)".formatted(gameScene2D.scaling());
                }
                return locText(key, "2D");
            },
            clock.pausedPy, gameVariantPy, gameScenePy, gameView.heightProperty())
        );
    }

    protected void updateGameScene(boolean reloadCurrent) {
        GameScene prevGameScene = gameScenePy.get();
        GameScene nextGameScene = gameConfiguration().selectGameScene(this);
        boolean sceneChanging = nextGameScene != prevGameScene;
        if (reloadCurrent || sceneChanging) {
            if (prevGameScene != null) {
                prevGameScene.end();
                Logger.info("Game scene ended: {}", prevGameScene.displayName());
            }
            if (nextGameScene != null) {
                gameView.embedGameScene(nextGameScene);
                nextGameScene.init();
                if (is2D3DSwitch(prevGameScene, nextGameScene)) {
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

    private boolean is2D3DSwitch(GameScene oldGameScene, GameScene newGameScene) {
        var cfg = gameConfiguration();
        return
            cfg.gameSceneHasID(oldGameScene, "PlayScene2D") &&
            cfg.gameSceneHasID(newGameScene, "PlayScene3D") ||
            cfg.gameSceneHasID(oldGameScene, "PlayScene3D") &&
            cfg.gameSceneHasID(newGameScene, "PlayScene2D");
    }

    private void logUpdateResult() {
        var messageList = game().eventLog().createMessageList();
        if (!messageList.isEmpty()) {
            Logger.info("Simulation step #{}:", clock.getUpdateCount());
            for (var msg : messageList) {
                Logger.info("- " + msg);
            }
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // GameContext interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public Keyboard keyboard() {
        return keyboard;
    }

    @Override
    public ArcadeKeyBinding arcadeKeys() {
        return arcadeKeyBinding;
    }

    @Override
    public JoypadKeyBinding currentJoypadKeyBinding() {
        return joypadKeyBindings[selectedJoypadIndex];
    }

    @Override
    public void selectNextJoypadKeyBinding() {
        selectedJoypadIndex = selectedJoypadIndex + 1;
        if (selectedJoypadIndex == joypadKeyBindings.length) {
            selectedJoypadIndex = 0;
        }
    }

    @Override
    public void registerJoypadKeyBinding() {
        Logger.info("Enable joypad key binding {}", currentJoypadKeyBinding());
        currentJoypadKeyBinding().register(keyboard);
    }

    @Override
    public void unregisterJoypadKeyBinding() {
        Logger.info("Disable joypad key binding {}", currentJoypadKeyBinding());
        currentJoypadKeyBinding().unregister(keyboard);
    }

    @Override
    public String locGameOverMessage() {
        return textPickerGameOverTexts.next();
    }

    @Override
    public String locLevelCompleteMessage(int levelNumber) {
        return textPickerLevelCompleteTexts.next() + "\n\n" + locText("level_complete", levelNumber);
    }

    @Override
    public GameSound sound() {
        return gameSound;
    }

    @Override
    public GameClockFX gameClock() {
        return clock;
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
    public GameVariant gameVariant() {
        return gameVariantPy.get();
    }

    @Override
    public GameUIConfiguration gameConfiguration(GameVariant variant) {
        return uiConfigByVariant.get(variant);
    }

    @Override
    public Optional<GameScene> currentGameScene() {
        return Optional.ofNullable(gameScenePy.get());
    }

    @Override
    public boolean currentGameSceneHasID(String sceneID) {
        if (currentGameScene().isEmpty()) {
            return false;
        }
        return gameConfiguration().gameSceneHasID(currentGameScene().get(), sceneID);
    }

    @Override
    public void togglePlayScene2D3D() {}

    @Override
    public AssetStorage assets() {
        return assets;
    }

    @Override
    public boolean isScoreVisible() {
        return scoreVisible;
    }

    @Override
    public void setScoreVisible(boolean visible) {
        scoreVisible = visible;
    }

    @Override
    public void selectGameVariant(GameVariant variant) {
        gameVariantPy.set(variant);
    }

    @Override
    public void showStartView() {
        clock.stop();
        gameScenePy.set(null);
        gameView.hideDashboard(); // TODO use binding?
        showView(startPageSelectionView);
        // Note: this must be called last such that option menu gets focus!
        startPageSelectionView.currentSlide().ifPresent(Node::requestFocus);
    }

    @Override
    public void showGameView() {
        showView(gameView);
        if (gameVariant() != GameVariant.MS_PACMAN_TENGEN) {
            sound().playVoice("voice.explain", 0);
        }
        GameActions2D.BOOT.execute(this);
    }

    private void showView(Node view) {
        Node currentView = viewPy.get();
        if (view != currentView) {
            if (currentView instanceof GameActionProvider actionProvider) {
                actionProvider.unregisterGameActionKeyBindings(keyboard);
            }
            if (currentView instanceof GameEventListener gameEventListener) {
                game().removeGameEventListener(gameEventListener);
            }
            if (view instanceof GameActionProvider actionProvider) {
                actionProvider.registerGameActionKeyBindings(keyboard);
            }
            if (view instanceof GameEventListener gameEventListener) {
                game().addGameEventListener(gameEventListener);
            }
            gameView.setSize(mainScene.getWidth(), mainScene.getHeight());
            sceneRoot.getChildren().set(0, view);
            viewPy.set(view);
        }
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
        gameVariantPy.set(gameVariant());
    }

    @Override
    public void onUnspecifiedChange(GameEvent event) {
        updateGameScene(true);
    }

    @Override
    public void onStopAllSounds(GameEvent event) {
        sound().stopAll();
    }
}