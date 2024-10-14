/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.pacman_xxl.PacManXXLGame;
import de.amr.games.pacman.ui2d.dashboard.InfoBoxCustomMaps;
import de.amr.games.pacman.ui2d.page.EditorPage;
import de.amr.games.pacman.ui2d.page.GamePage;
import de.amr.games.pacman.ui2d.page.Page;
import de.amr.games.pacman.ui2d.page.StartPage;
import de.amr.games.pacman.ui2d.scene.common.GameScene;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui2d.scene.common.GameSceneConfiguration;
import de.amr.games.pacman.ui2d.scene.common.GameSceneID;
import de.amr.games.pacman.ui2d.sound.GameSounds;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import de.amr.games.pacman.ui2d.util.FlashMessageView;
import de.amr.games.pacman.ui2d.util.GameClockFX;
import de.amr.games.pacman.ui2d.util.Keyboard;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Dimension2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.text.MessageFormat;
import java.time.LocalTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.games.pacman.controller.GameState.TESTING_LEVEL_BONI;
import static de.amr.games.pacman.controller.GameState.TESTING_LEVEL_TEASERS;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui2d.GameAssets2D.assetPrefix;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.*;
import static de.amr.games.pacman.ui2d.util.Ufx.createIcon;

/**
 * 2D user interface for all Pac-Man game variants.
 *
 * @author Armin Reichert
 */
public class PacManGames2dUI implements GameEventListener, GameContext {

    protected static final Keyboard KEYBOARD = new Keyboard();

    /**
     * The order here is used by the start page!
     */
    public static List<GameVariant> GAME_VARIANTS_IN_ORDER = List.of(
        GameVariant.PACMAN, GameVariant.PACMAN_XXL, GameVariant.MS_PACMAN, GameVariant.MS_PACMAN_TENGEN
    );

    private static final GameSounds SOUNDS = new GameSounds();

    public final ObjectProperty<GameScene> gameScenePy = new SimpleObjectProperty<>(this, "gameScene");

    public final ObjectProperty<GameVariant> gameVariantPy = new SimpleObjectProperty<>(this, "gameVariant") {
        @Override
        protected void invalidated() {
            handleGameVariantChange(get());
        }
    };

    protected final AssetStorage assets;
    protected final FlashMessageView flashMessageLayer;
    protected final GameClockFX clock;
    protected final Map<GameVariant, GameSceneConfiguration> gameSceneConfigByVariant;
    protected final StackPane sceneRoot;
    protected Stage stage;
    protected StartPage startPage;
    protected GamePage gamePage;
    protected EditorPage editorPage;
    protected Page currentPage;
    protected boolean scoreVisible;
    protected boolean signatureShown;

    public PacManGames2dUI() {
        assets = new AssetStorage();
        clock = new GameClockFX();
        flashMessageLayer = new FlashMessageView();
        sceneRoot = new StackPane();
        gameSceneConfigByVariant = new EnumMap<>(GameVariant.class);
    }

    public void loadAssets() {
        GameAssets2D.addTo(assets);
        sounds().setAssets(assets);
    }

    protected void bindActionsToKeys() {
        for (GameAction action : GameAction2D.values()) {
            KEYBOARD.register(action.trigger());
        }
    }

    public void setGameSceneConfiguration(GameVariant variant, GameSceneConfiguration gameSceneConfiguration) {
        gameSceneConfigByVariant.put(variant, gameSceneConfiguration);
        gameSceneConfiguration.gameScenes().forEach(gameScene -> {
            if (gameScene instanceof GameScene2D gameScene2D) {
                gameScene2D.setGameContext(this);
                gameScene2D.debugInfoPy.bind(PY_DEBUG_INFO);
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

        var mutedIcon = createIcon(assets.get("icon.mute"), 48, sounds().mutedProperty());
        StackPane.setAlignment(mutedIcon, Pos.BOTTOM_RIGHT);
        var pauseIcon = createIcon(assets.get("icon.pause"), 64, clock.pausedPy);
        StackPane.setAlignment(pauseIcon, Pos.CENTER);
        sceneRoot.getChildren().addAll(new Pane(), flashMessageLayer, pauseIcon, mutedIcon);
        Scene mainScene = createMainScene(initialSize);
        stage.setScene(mainScene);

        startPage = new StartPage(this);
        startPage.gameVariantPy.bind(gameVariantPy);

        gamePage = createGamePage(mainScene);

        clock.setPauseableCallback(this::runIfNotPausedOnEveryTick);
        clock.setPermanentCallback(this::runOnEveryTick);

        // attach game event listeners to game model
        for (var variant : GameVariant.values()) {
            GameModel game = GameController.it().gameModel(variant);
            game.addGameEventListener(this);
            // TODO: find better way for this:
            game.addGameEventListener(gamePage.dashboardLayer().getPip());
        }

        // init game variant property
        gameVariantPy.set(gameVariant());

        // TODO: Not sure if this belongs here
        // The game models are in project "pacman-core" which has no dependency toJavaFX,
        // therefore we cannot use data binding to the model classes.
        PacManXXLGame xxlGame = gameController().gameModel(GameVariant.PACMAN_XXL);
        xxlGame.setMapSelectionMode(PY_MAP_SELECTION_MODE.get());
        PY_MAP_SELECTION_MODE.addListener((py,ov,selectionMode) -> xxlGame.setMapSelectionMode(selectionMode));

        bindActionsToKeys();

        stage.setMinWidth(GameModel.ARCADE_MAP_SIZE_X * 1.25);
        stage.setMinHeight(GameModel.ARCADE_MAP_SIZE_Y * 1.25);
        stage.titleProperty().bind(stageTitleBinding());
        stage.centerOnScreen();
        stage.setOnShowing(e-> selectStartPage());
        stage.show();
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
                // 2D scenes only:
                currentGameScene().ifPresent(gameScene -> gameScene.draw(currentGameSceneConfig().renderer()));
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
        mainScene.addEventFilter(KeyEvent.KEY_PRESSED, KEYBOARD::onKeyPressed);
        mainScene.addEventFilter(KeyEvent.KEY_RELEASED, KEYBOARD::onKeyReleased);
        mainScene.setOnKeyPressed(e -> {
            if (GameAction2D.FULLSCREEN.called(KEYBOARD)) {
                stage.setFullScreen(true);
            } else if (GameAction2D.MUTE.called(KEYBOARD)) {
                sounds().toggleMuted();
            } else {
                currentPage.handleInput();
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
        String assetPrefix = assetPrefix(variant);
        sceneRoot.setBackground(assets.get(assetPrefix + ".scene_background"));
        boolean gameCanvasDecorated = variant != GameVariant.MS_PACMAN_TENGEN;
        PY_GAME_CANVAS_DECORATED.set(gameCanvasDecorated);
        Image icon = assets.image(assetPrefix + ".icon");
        if (icon != null) {
            stage.getIcons().setAll(icon);
        }
        sounds().init(variant);
    }

    protected ObservableValue<String> stageTitleBinding() {
        return Bindings.createStringBinding(
            () -> {
                String gameVariantPart = "app.title." + assetPrefix(gameVariantPy.get());
                String pausedPart = clock.pausedPy.get() ? ".paused" : "";
                return locText(gameVariantPart + pausedPart, "2D");
            },
            clock.pausedPy, gameVariantPy);
    }

    private String displayName(GameScene gameScene) {
        String text = gameScene != null ? gameScene.getClass().getSimpleName() : "NO GAME SCENE";
        text += String.format(" (%s)", gameVariant());
        return text;
    }

    private void configureGameScene2D(GameScene2D gameScene2D) {
        var gameSceneConfig = currentGameSceneConfig();
        gamePage.setWorldRenderer(gameSceneConfig.renderer());
        gameScene2D.backgroundColorPy.bind(PY_CANVAS_BG_COLOR);
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
        return KEYBOARD;
    }

    @Override
    public boolean isActionCalled(GameAction action) {
        return action.called(KEYBOARD);
    }

    @Override
    public void doFirstCalledAction(Stream<GameAction> actions) {
        actions.filter(this::isActionCalled).findFirst().ifPresent(action -> action.execute(this));
    }

    @Override
    public void doFirstCalledActionOrElse(Stream<GameAction> actions, Runnable defaultAction) {
        actions.filter(this::isActionCalled).findFirst().ifPresentOrElse(action -> action.execute(this), defaultAction);
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
    public GameSounds sounds() {
        return SOUNDS;
    }

    @Override
    public GameClockFX gameClock() {
        return clock;
    }

    @Override
    public ObjectProperty<GameVariant> gameVariantProperty() {
        return gameVariantPy;
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
    public GameSceneConfiguration gameSceneConfig(GameVariant variant) {
        return gameSceneConfigByVariant.get(variant);
    }

    @Override
    public Optional<GameScene> currentGameScene() {
        return Optional.ofNullable(gameScenePy.get());
    }

    @Override
    public boolean currentGameSceneHasID(GameSceneID sceneID) {
        if (currentGameScene().isEmpty()) {
            return false;
        }
        return currentGameSceneConfig().gameSceneHasID(currentGameScene().get(), sceneID);
    }

    @Override
    public void updateGameScene(boolean reloadCurrent) {
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
            }
            if (sceneChanging) {
                gameScenePy.set(nextGameScene);
                Logger.info("Game scene changed to: {}", displayName(gameScenePy.get()));
            } else {
                Logger.info("Game scene reloaded: {}", displayName(currentGameScene));
            }
        }
    }

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
            editorPage.setCloseAction(editor -> {
                editor.stop();
                editor.showSaveConfirmationDialog(editor::showSaveDialog, () -> stage.titleProperty().bind(stageTitleBinding()));
                game().updateCustomMaps();
                GameAction2D.BOOT.execute(this);
                selectStartPage();
            });
        }
        return editorPage;
    }

    @Override
    public void selectGameVariant(GameVariant variant) {
        gameController().selectGame(variant);
        gameController().restart(GameState.BOOT);
        Logger.info("Selected game variant: {}", variant);
    }

    @Override
    public void selectPage(Page page) {
        if (page != currentPage) {
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
        //TODO check this
        gamePage.dashboardLayer().hideDashboard();
        selectPage(startPage);
    }

    @Override
    public void selectGamePage() {
        selectPage(gamePage);
        clock.start();
    }

    @Override
    public void showFlashMessage(String message, Object... args) {
        showFlashMessageSeconds(1, message, args);
    }

    @Override
    public void showFlashMessageSeconds(double seconds, String message, Object... args) {
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
        gamePage.dashboardLayer().getInfoBoxes()
            .filter(infoBox -> infoBox instanceof InfoBoxCustomMaps)
            .findFirst()
            .ifPresent(infoBox -> ((InfoBoxCustomMaps)infoBox).updateTableView());
        Logger.info("Custom maps table updated");
    }

    @Override
    public void onGameVariantChanged(GameEvent event) {
        gameVariantPy.set(event.game.variant());
    }

    @Override
    public void onUnspecifiedChange(GameEvent event) {
        updateGameScene(true);
    }

    @Override
    public void onLevelCreated(GameEvent event) {
        currentGameSceneConfig().createActorAnimations(game());
        Logger.info("Actor animations created. ({} level #{})", gameVariant(), game().levelNumber());
        if (game().isDemoLevel()) {
            sounds().setEnabled(false);
        } else {
            game().pac().setManualSteering(new KeyboardPacSteering(KEYBOARD));
            sounds().setEnabled(true);
        }
        //TODO use data binding?
        gamePage.adaptCanvasSizeToCurrentWorld();
    }

    @Override
    public void onLevelStarted(GameEvent event) {
        int hourOfDay = LocalTime.now().getHour();
        PY_NIGHT_MODE.set(hourOfDay >= 21 || hourOfDay <= 4);
        if (gameState() != TESTING_LEVEL_BONI && gameState() != TESTING_LEVEL_TEASERS
                && !game().isDemoLevel()
                && game().levelNumber() == 1) {
            sounds().playGameReadySound();
        }
    }

    @Override
    public void onStopAllSounds(GameEvent event) {
        sounds().stopAll();
    }
}