/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.maps.editor.TileMapEditor;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.pacman_xxl.PacManXXLGame;
import de.amr.games.pacman.ui2d.dashboard.InfoBoxCustomMaps;
import de.amr.games.pacman.ui2d.input.ArcadeKeyAdapter;
import de.amr.games.pacman.ui2d.input.JoypadKeyAdapter;
import de.amr.games.pacman.ui2d.input.Keyboard;
import de.amr.games.pacman.ui2d.page.*;
import de.amr.games.pacman.ui2d.scene.common.GameScene;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui2d.scene.common.GameSceneConfig;
import de.amr.games.pacman.ui2d.sound.GameSound;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import de.amr.games.pacman.ui2d.util.FlashMessageView;
import de.amr.games.pacman.ui2d.util.GameClockFX;
import de.amr.games.pacman.ui2d.util.Picker;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.text.MessageFormat;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.model.pacman.PacManArcadeGame.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.ui2d.GameAssets2D.assetPrefix;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.*;
import static de.amr.games.pacman.ui2d.util.Ufx.createIcon;

/**
 * User interface for all Pac-Man game variants (2D only).
 *
 * @author Armin Reichert
 */
public class PacManGamesUI implements GameEventListener, GameContext {

    public static final ArcadeKeyAdapter ARCADE_CURSOR_KEYS = new ArcadeKeyAdapter.Definition(
        new KeyCodeCombination(KeyCode.DIGIT5),
        new KeyCodeCombination(KeyCode.DIGIT1),
        new KeyCodeCombination(KeyCode.UP),
        new KeyCodeCombination(KeyCode.DOWN),
        new KeyCodeCombination(KeyCode.LEFT),
        new KeyCodeCombination(KeyCode.RIGHT)
    );

    // My current bindings, might be crap
    public static final JoypadKeyAdapter JOYPAD_CURSOR_KEYS = new JoypadKeyAdapter.Definition(
        new KeyCodeCombination(KeyCode.SPACE),
        new KeyCodeCombination(KeyCode.ENTER),
        new KeyCodeCombination(KeyCode.B),
        new KeyCodeCombination(KeyCode.N),
        new KeyCodeCombination(KeyCode.UP),
        new KeyCodeCombination(KeyCode.DOWN),
        new KeyCodeCombination(KeyCode.LEFT),
        new KeyCodeCombination(KeyCode.RIGHT)
    );

    // Mesen emulator key set #2
    public static final JoypadKeyAdapter JOYPAD_WASD = new JoypadKeyAdapter.Definition(
        new KeyCodeCombination(KeyCode.U),
        new KeyCodeCombination(KeyCode.I),
        new KeyCodeCombination(KeyCode.J),
        new KeyCodeCombination(KeyCode.K),
        new KeyCodeCombination(KeyCode.W),
        new KeyCodeCombination(KeyCode.S),
        new KeyCodeCombination(KeyCode.A),
        new KeyCodeCombination(KeyCode.D)
    );

    protected final Keyboard keyboard = new Keyboard();

    protected final GameClockFX clock = new GameClockFX();

    protected final AssetStorage assets = new AssetStorage();

    protected final GameSound gameSound = new GameSound();

    protected final ObjectProperty<GameScene> gameScenePy = new SimpleObjectProperty<>(this, "gameScene");

    protected final ObjectProperty<GameVariant> gameVariantPy = new SimpleObjectProperty<>(this, "gameVariant") {
        @Override
        protected void invalidated() {
            handleGameVariantChange(get());
        }
    };

    protected final Map<GameVariant, GameSceneConfig> gameSceneConfigByVariant = new EnumMap<>(GameVariant.class);

    protected final FlashMessageView flashMessageLayer = new FlashMessageView();
    protected final StackPane sceneRoot = new StackPane();
    protected Stage stage;
    protected StartPage startPage;
    protected GamePage gamePage;
    protected EditorPage editorPage;
    protected Page currentPage;

    protected boolean scoreVisible;
    protected boolean signatureShown; //TODO make this work again for all intro screens
    protected Picker<String> pickerGameOver;
    protected Picker<String> pickerLevelComplete;

    protected JoypadKeyAdapter joypad = JOYPAD_CURSOR_KEYS;
    protected ArcadeKeyAdapter arcade = ARCADE_CURSOR_KEYS;

    public PacManGamesUI() {}

    public void loadAssets() {
        GameAssets2D.addTo(assets);
        sound().setAssets(assets);
        pickerGameOver = Picker.fromBundle(assets.bundles().getFirst(), "game.over");
        pickerLevelComplete = Picker.fromBundle(assets.bundles().getFirst(), "level.complete");
    }

    public void setGameSceneConfig(GameVariant variant, GameSceneConfig gameSceneConfig) {
        gameSceneConfigByVariant.put(variant, gameSceneConfig);
        gameSceneConfig.initGameScenes(this);
        //TODO check this
        gameSceneConfig.gameScenes().forEach(gameScene -> {
            if (gameScene instanceof GameScene2D gameScene2D) {
                gameScene2D.debugInfoPy.bind(PY_DEBUG_INFO_VISIBLE);
            }
        });
    }

    /**
     * Called from application start method (on JavaFX application thread).

     * @param stage primary stage (window)
     * @param initialSize initial UI size
     */
    public void createAndStart(Stage stage, Dimension2D initialSize) {
        this.stage = checkNotNull(stage);
        checkNotNull(initialSize);

        Scene mainScene = createMainScene(initialSize);
        stage.setScene(mainScene);

        startPage = new StartPage(this);
        startPage.gameVariantPy.bind(gameVariantPy);

        gamePage = createGamePage(mainScene);

        clock.setPauseableCallback(this::runIfNotPausedOnEveryTick);
        clock.setPermanentCallback(this::runOnEveryTick);

        // init game variant property
        gameVariantPy.set(currentGameVariant());

        //TODO This doesn't fit for Tengen screen resolution
        stage.setMinWidth(ARCADE_MAP_SIZE_IN_PIXELS.x() * 1.25);
        stage.setMinHeight(ARCADE_MAP_SIZE_IN_PIXELS.y() * 1.25);
        stage.titleProperty().bind(stageTitleBinding());
        stage.centerOnScreen();
        stage.setOnShowing(e-> selectStartPage());
        stage.show();
    }

    private void addMyselfAsGameListener(GameModel game) {
        game.addGameEventListener(this);
        game.addGameEventListener(gamePage.dashboardLayer().getPip());
    }

    /**
     * Executed on clock tick if game is not paused.
     */
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

    /**
     * Executed on clock tick even if game is paused.
     */
    protected void runOnEveryTick() {
        try {
            if (currentPage == gamePage) {
                currentGameScene().ifPresent(gameScene -> {
                    if (gameScene instanceof  GameScene2D gameScene2D) {
                        gameScene2D.draw(currentGameSceneConfig().renderer());
                    }
                });
                gamePage.updateDashboard();
                flashMessageLayer.update();
            } else {
                Logger.warn("Should not happen: Cannot handle tick when not on game page");
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

    protected Scene createMainScene(Dimension2D size) {
        Scene mainScene = new Scene(sceneRoot, size.getWidth(), size.getHeight());

        var mutedIcon = createIcon(assets.get("icon.mute"), 48, sound().mutedProperty());
        StackPane.setAlignment(mutedIcon, Pos.BOTTOM_RIGHT);
        var pauseIcon = createIcon(assets.get("icon.pause"), 64, clock.pausedPy);
        StackPane.setAlignment(pauseIcon, Pos.CENTER);
        sceneRoot.getChildren().addAll(new Pane(), flashMessageLayer, pauseIcon, mutedIcon);

        mainScene.addEventFilter(KeyEvent.KEY_PRESSED, keyboard::onKeyPressed);
        mainScene.addEventFilter(KeyEvent.KEY_RELEASED, keyboard::onKeyReleased);
        // Global keyboard shortcuts
        mainScene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.F11) {
                stage.setFullScreen(true);
            } else if (e.getCode() == KeyCode.M && e.isAltDown()) {
                sound().toggleMuted();
            } else {
                currentPage.handleInput(this);
            }
        });
        mainScene.setOnContextMenuRequested(e -> {
            currentPage.handleContextMenuRequest(e);
            e.consume();
        });
        ChangeListener<Number> sizeListener = (py,ov,nv) -> {
            if (currentPage != null) {
                currentPage.setSize(mainScene.getWidth(), mainScene.getHeight());
            }
        };
        mainScene.widthProperty().addListener(sizeListener);
        mainScene.heightProperty().addListener(sizeListener);
        return mainScene;
    }

    protected GamePage createGamePage(Scene parentScene) {
        var gamePage = new GamePage(this, parentScene);
        gamePage.gameScenePy.bind(gameScenePy);
        return gamePage;
    }

    protected void handleGameVariantChange(GameVariant variant) {
        gameController().selectGame(variant);
        gameController().restart(GameState.BOOT);
        Logger.info("Selected game variant: {}", variant);

        GameModel game = gameController().gameModel(variant);
        addMyselfAsGameListener(game);

        // TODO: Not sure if this belongs here
        if (variant == GameVariant.PACMAN_XXL) {
            // We cannot use data binding to the game model classes because the game models are in project
            // "pacman-core" which has no dependency to JavaFX data binding.
            PacManXXLGame xxlGame = (PacManXXLGame) game;
            xxlGame.setMapSelectionMode(PY_MAP_SELECTION_MODE.get());
            PY_MAP_SELECTION_MODE.addListener((py, ov, selectionMode) -> xxlGame.setMapSelectionMode(selectionMode));
        }

        String assetPrefix = assetPrefix(variant);
        sceneRoot.setBackground(assets.get(assetPrefix + ".scene_background"));
        Image icon = assets.image(assetPrefix + ".icon");
        if (icon != null) {
            stage.getIcons().setAll(icon);
        }
        try {
            sound().init(variant);
        } catch (Exception x) {
            Logger.error(x);
        }
        gamePage.gameCanvasContainer().decorationEnabledPy.set(currentGameVariant() != GameVariant.MS_PACMAN_TENGEN);
    }

    protected StringBinding stageTitleBinding() {
        return Bindings.createStringBinding(
            () -> {
                String gameVariantPart = "app.title." + assetPrefix(gameVariantPy.get());
                String pausedPart = clock.pausedPy.get() ? ".paused" : "";
                String scalingPart = "";
                // Just in case:
                if (currentGameScene().isPresent() && currentGameScene().get() instanceof GameScene2D gameScene2D) {
                    scalingPart = " (%.2fx)".formatted(gameScene2D.scaling());
                }
                return locText(gameVariantPart + pausedPart + scalingPart, "2D");
            },
            clock.pausedPy, gameVariantPy, gameScenePy, gamePage.heightProperty());
    }

    private String displayName(GameScene gameScene) {
        String text = gameScene != null ? gameScene.getClass().getSimpleName() : "NO GAME SCENE";
        text += String.format(" (%s)", currentGameVariant());
        return text;
    }

    private void configureGameScene2D(GameScene2D gameScene2D) {
        var gameSceneConfig = currentGameSceneConfig();
        gamePage.setWorldRenderer(gameSceneConfig.renderer());
        gameScene2D.backgroundColorProperty().bind(PY_CANVAS_BG_COLOR);
    }

    protected void updateGameScene(boolean reloadCurrent) {
        GameScene currentGameScene = gameScenePy.get();
        GameScene nextGameScene = currentGameSceneConfig().selectGameScene(this);
        boolean sceneChanging = nextGameScene != currentGameScene;
        if (reloadCurrent || sceneChanging) {
            if (currentGameScene != null) {
                currentGameScene.end();
                Logger.info("Game scene ended: {}", displayName(currentGameScene));
            }
            if (nextGameScene != null) {
                if (nextGameScene instanceof GameScene2D gameScene2D) {
                    configureGameScene2D(gameScene2D);
                }
                nextGameScene.init();
                if (is2D3DSwitch(currentGameScene, nextGameScene)) {
                    nextGameScene.onSceneVariantSwitch(currentGameScene);
                }
            }
            if (sceneChanging) {
                gameScenePy.set(nextGameScene);
                Logger.info("Game scene changed to: {}", displayName(gameScenePy.get()));
            } else {
                Logger.info("Game scene reloaded: {}", displayName(currentGameScene));
            }
        }
    }

    private boolean is2D3DSwitch(GameScene oldGameScene, GameScene newGameScene) {
        var cfg = currentGameSceneConfig();
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
    public ArcadeKeyAdapter arcade() {
        return arcade;
    }

    @Override
    public JoypadKeyAdapter joypad() {
        return joypad;
    }

    @Override
    public void nextJoypad() {
        // TODO should work for any number
        if (joypad == JOYPAD_WASD) {
            joypad = JOYPAD_CURSOR_KEYS;
        } else {
            joypad = JOYPAD_WASD;
        }
    }

    @Override
    public void enableJoypad() {
        Logger.info("Enable joypad");
        joypad.register(keyboard);
    }

    @Override
    public void disableJoypad() {
        Logger.info("Disable joypad");
        joypad.unregister(keyboard);
    }

    @Override
    public void ifGameActionRun(GameActionProvider actionProvider) {
        actionProvider.firstMatchedAction(keyboard()).filter(gameAction -> gameAction.isEnabled(this))
            .ifPresent(action -> action.execute(this));
    }

    @Override
    public void ifGameActionRunElse(GameActionProvider actionProvider, Runnable defaultAction) {
       actionProvider.firstMatchedAction(keyboard()).filter(gameAction -> gameAction.isEnabled(this))
           .ifPresentOrElse(action -> action.execute(this), defaultAction);
    }

    @Override
    public String locText(String keyOrPattern, Object... args) {
        checkNotNull(keyOrPattern);
        for (var bundle : assets.bundles()) {
            if (bundle.containsKey(keyOrPattern)) {
                return MessageFormat.format(bundle.getString(keyOrPattern), args);
            }
        }
        Logger.error("Missing localized text for key {}", keyOrPattern);
        return null;
    }

    @Override
    public String locGameOverMessage() {
        return pickerGameOver.next();
    }

    @Override
    public String locLevelCompleteMessage() {
        return pickerLevelComplete.next() + "\n\n" + locText("level_complete", level().number);
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
    public GamePage gamePage() {
        return gamePage;
    }

    @Override
    public ObjectProperty<GameScene> gameSceneProperty() {
        return gameScenePy;
    }

    @Override
    public GameSceneConfig gameSceneConfig(GameVariant variant) {
        return gameSceneConfigByVariant.get(variant);
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
        return currentGameSceneConfig().gameSceneHasID(currentGameScene().get(), sceneID);
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
    public EditorPage getOrCreateEditorPage() {
        if (editorPage == null) {
            editorPage = new EditorPage(stage, this, game().customMapDir());
            editorPage.setCloseAction(this::closeEditor);
        }
        return editorPage;
    }

    private void closeEditor(TileMapEditor editor) {
        editor.stop();
        editor.showSaveConfirmationDialog(editor::showSaveDialog, () -> stage.titleProperty().bind(stageTitleBinding()));
        game().updateCustomMaps();
        gameClock().setTargetFrameRate(GameModel.TICKS_PER_SECOND);
        gameController().restart(GameState.BOOT);
        selectStartPage();
    }

    @Override
    public void selectGameVariant(GameVariant variant) {
        gameVariantPy.set(variant);
    }

    @Override
    public void selectPage(Page page) {
        if (page != currentPage) {
            if (currentPage != null) {
                currentPage.unregisterGameActionKeyBindings(keyboard());
            }
            page.registerGameActionKeyBindings(keyboard());
            currentPage = page;
            currentPage.setSize(stage.getScene().getWidth(), stage.getScene().getHeight());
            sceneRoot.getChildren().set(0, currentPage.rootPane());
            currentPage.rootPane().requestFocus();
            currentPage.onPageSelected();
        }
    }

    @Override
    public void selectStartPage() {
        clock.stop();
        gameSceneProperty().set(null);
        //TODO check this
        gamePage.dashboardLayer().hideDashboard();
        selectPage(startPage);
    }

    @Override
    public void selectGamePage() {
        selectPage(gamePage);
        clock.start();
        if (currentGameVariant() != GameVariant.MS_PACMAN_TENGEN) {
            sound().playVoice("voice.explain", 0);
        }
    }

    @Override
    public void showFlashMessageSec(double seconds, String message, Object... args) {
        flashMessageLayer.showMessage(String.format(message, args), seconds);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // GameEventListener interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public void onGameEvent(GameEvent event) {
        Logger.trace("Received: {}", event);
        // call event specific hook method:
        GameEventListener.super.onGameEvent(event);
        if (currentPage == gamePage) {
            updateGameScene(false);
            // dispatch event to current game scene if any
            currentGameScene().ifPresent(gameScene -> gameScene.onGameEvent(event));
        }
    }

    @Override
    public void onCustomMapsChanged(GameEvent e) {
        //TODO find a cleaner solution
        gamePage.dashboardLayer().dashboardEntries().stream()
            .map(DashboardLayer.DashboardEntry::infoBox)
            .filter(infoBox -> infoBox instanceof InfoBoxCustomMaps)
            .findFirst()
            .ifPresent(infoBox -> ((InfoBoxCustomMaps)infoBox).updateTableView());
        Logger.info("Custom maps table updated");
    }

    @Override
    public void onGameVariantChanged(GameEvent event) {
        gameVariantPy.set(currentGameVariant());
    }

    @Override
    public void onUnspecifiedChange(GameEvent event) {
        updateGameScene(true);
    }

    @Override
    public void onLevelCreated(GameEvent event) {
        currentGameSceneConfig().createActorAnimations(game());
        Logger.info("Actor animations created. ({} level #{})", currentGameVariant(), level().number);
        sound().setEnabled(!game().isDemoLevel());
        Logger.info("Sounds {}", sound().isEnabled() ? "enabled" : "disabled");
        // size of game scene have changed, so re-embed
        currentGameScene().ifPresent(gamePage::embedGameScene);
    }

    @Override
    public void onStopAllSounds(GameEvent event) {
        sound().stopAll();
    }
}