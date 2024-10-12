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
import de.amr.games.pacman.model.pacmanxxl.PacManXXLGame;
import de.amr.games.pacman.ui2d.dashboard.InfoBoxCustomMaps;
import de.amr.games.pacman.ui2d.page.EditorPage;
import de.amr.games.pacman.ui2d.page.GamePage;
import de.amr.games.pacman.ui2d.page.Page;
import de.amr.games.pacman.ui2d.page.StartPage;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui2d.scene.GameScene2D;
import de.amr.games.pacman.ui2d.scene.GameSceneID;
import de.amr.games.pacman.ui2d.scene.ms_pacman.MsPacManGameRenderer;
import de.amr.games.pacman.ui2d.scene.ms_pacman.MsPacManGameGhostAnimations;
import de.amr.games.pacman.ui2d.scene.ms_pacman.MsPacManGamePacAnimations;
import de.amr.games.pacman.ui2d.scene.pacman.PacManGameRenderer;
import de.amr.games.pacman.ui2d.scene.pacman.PacManGameGhostAnimations;
import de.amr.games.pacman.ui2d.scene.pacman.PacManGamePacAnimations;
import de.amr.games.pacman.ui2d.scene.pacman_xxl.PacManXXLGameRenderer;
import de.amr.games.pacman.ui2d.scene.tengen.TengenMsPacManGameRenderer;
import de.amr.games.pacman.ui2d.scene.tengen.TengenMsPacManGameGhostAnimations;
import de.amr.games.pacman.ui2d.scene.tengen.TengenMsPacManGamePacAnimations;
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
import javafx.scene.image.ImageView;
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
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui2d.GameAssets2D.assetPrefix;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.*;

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

    public static GameWorldRenderer createRenderer(GameVariant variant, AssetStorage assets) {
        return switch (variant) {
            case MS_PACMAN -> new MsPacManGameRenderer(assets);
            case MS_PACMAN_TENGEN -> new TengenMsPacManGameRenderer(assets);
            case PACMAN -> new PacManGameRenderer(assets);
            case PACMAN_XXL -> new PacManXXLGameRenderer(assets);
        };
    }

    private static void createActorAnimations(GameModel game, GameSpriteSheet spriteSheet) {
        switch (game.variant()) {
            case MS_PACMAN -> {
                game.pac().setAnimations(new MsPacManGamePacAnimations(spriteSheet));
                game.ghosts().forEach(ghost -> ghost.setAnimations(new MsPacManGameGhostAnimations(spriteSheet, ghost.id())));
            }
            case MS_PACMAN_TENGEN -> {
                game.pac().setAnimations(new TengenMsPacManGamePacAnimations(spriteSheet));
                game.ghosts().forEach(ghost -> ghost.setAnimations(new TengenMsPacManGameGhostAnimations(spriteSheet, ghost.id())));
            }
            case PACMAN, PACMAN_XXL -> {
                game.pac().setAnimations(new PacManGamePacAnimations(spriteSheet));
                game.ghosts().forEach(ghost -> ghost.setAnimations(new PacManGameGhostAnimations(spriteSheet, ghost.id())));
            }
            default -> throw new IllegalArgumentException("Unsupported game variant: " + game.variant());
        }
    }

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
    protected final StackPane sceneRoot;
    protected final Map<GameVariant, Map<GameSceneID, GameScene>> gameSceneMap;

    protected Stage stage;
    protected StartPage startPage;
    protected GamePage gamePage;
    protected EditorPage editorPage;
    protected Page currentPage;
    protected GameWorldRenderer worldRenderer;
    protected boolean scoreVisible;

    public PacManGames2dUI() {
        assets = new AssetStorage();
        clock = new GameClockFX();
        flashMessageLayer = new FlashMessageView();
        sceneRoot = new StackPane();
        gameSceneMap = new EnumMap<>(GameVariant.class);
    }

    public void loadAssets() {
        GameAssets2D.addTo(assets);
        sounds().setAssets(assets);
    }

    protected void registerActions() {
        for (GameAction action : GameAction2D.values()) {
            KEYBOARD.register(action.trigger());
        }
    }

    public void setGameScenes(GameVariant variant, Map<GameSceneID, GameScene> gameScenes) {
        gameSceneMap.put(variant, gameScenes);
        gameScenes.values().forEach(gameScene -> {
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

        sceneRoot.getChildren().addAll(new Pane(), flashMessageLayer, createMutedIcon());
        Scene mainScene = createMainScene(initialSize);
        stage.setScene(mainScene);

        startPage = new StartPage(this);
        startPage.gameVariantPy.bind(gameVariantPy);

        gamePage = createGamePage(mainScene);

        clock.setPauseableCallback(this::onNonPausedTick);
        clock.setPermanentCallback(this::onEveryTick);

        // attach game event listeners to game model
        for (var variant : GameVariant.values()) {
            GameModel game = GameController.it().gameModel(variant);
            game.addGameEventListener(this);
            // TODO: find better way for this:
            game.addGameEventListener(gamePage.dashboardLayer().getPip());
        }

        // init game variant property
        gameVariantPy.set(gameVariant());

        //TODO: Not sure if this belongs here:
        PacManXXLGame xxlGame = gameController().gameModel(GameVariant.PACMAN_XXL);
        xxlGame.setMapSelectionMode(PY_MAP_SELECTION_MODE.get());
        PY_MAP_SELECTION_MODE.addListener((py,ov,selectionMode) -> xxlGame.setMapSelectionMode(selectionMode));

        registerActions();

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
    protected void onNonPausedTick() {
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
    protected void onEveryTick() {
        try {
            if (currentPage == gamePage) {
                // 2D scenes only:
                currentGameScene().ifPresent(gameScene -> gameScene.draw(worldRenderer));
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

    public StackPane rootPane() {
        return sceneRoot;
    }

    protected GamePage createGamePage(Scene parentScene) {
        var gamePage = new GamePage(this, parentScene);
        gamePage.gameScenePy.bind(gameScenePy);
        return gamePage;
    }

    private void handleGameVariantChange(GameVariant variant) {
        String assetPrefix = assetPrefix(variant);
        sceneRoot.setBackground(assets.get(assetPrefix + ".scene_background"));
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

    protected ImageView createMutedIcon() {
        var icon = new ImageView(assets.<Image>get("icon.mute"));
        icon.setFitWidth(48);
        icon.setPreserveRatio(true);
        icon.visibleProperty().bind(sounds().mutedProperty());
        StackPane.setAlignment(icon, Pos.BOTTOM_RIGHT);
        return icon;
    }

    private String displayName(GameScene gameScene) {
        String text = gameScene != null ? gameScene.getClass().getSimpleName() : "NO GAME SCENE";
        text += String.format(" (%s)", gameVariant());
        return text;
    }

    private void configureGameScene2D(GameScene2D gameScene2D) {
        worldRenderer = createRenderer(gameVariant(), assets);
        gamePage.setWorldRenderer(worldRenderer);
        gameScene2D.backgroundColorPy.bind(PY_CANVAS_BG_COLOR);
    }

    protected GameScene gameSceneForCurrentGameState() {
        GameSceneID sceneID = switch (gameState()) {
            case BOOT               -> GameSceneID.BOOT_SCENE;
            case STARTING           -> GameSceneID.START_SCENE;
            case INTRO              -> GameSceneID.INTRO_SCENE;
            case INTERMISSION       -> cutSceneID(game().intermissionNumber(game().levelNumber()));
            case TESTING_CUT_SCENES -> cutSceneID(gameState().<Integer>getProperty("intermissionTestNumber"));
            default                 -> GameSceneID.PLAY_SCENE;
        };
        return gameScene(gameVariant(), sceneID);
    }

    private GameSceneID cutSceneID(int number) {
        return switch (number) {
            case 1 -> GameSceneID.CUT_SCENE_1;
            case 2 -> GameSceneID.CUT_SCENE_2;
            case 3 -> GameSceneID.CUT_SCENE_3;
            default -> null;
        };
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
    public void execFirstCalledAction(Stream<GameAction> actions) {
        actions.filter(this::isActionCalled).findFirst().ifPresent(action -> action.execute(this));
    }

    @Override
    public void execFirstCalledActionOrElse(Stream<GameAction> actions, Runnable defaultAction) {
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
    public ObjectProperty<GameScene> gameSceneProperty() {
        return gameScenePy;
    }

    @Override
    public Optional<GameScene> currentGameScene() {
        return Optional.ofNullable(gameScenePy.get());
    }

    @Override
    public boolean currentGameSceneIs(GameSceneID sceneID) {
        return currentGameScene().isPresent() && hasID(currentGameScene().get(), sceneID);
    }


    @Override
    public void updateGameScene(boolean reloadCurrent) {
        GameScene currentGameScene = gameScenePy.get();
        GameScene nextGameScene = gameSceneForCurrentGameState();
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
            if (currentGameSceneIs(GameSceneID.INTRO_SCENE)) {
                gamePage.showSignature();
            } else {
                gamePage.hideSignature();
            }
        }
    }

    @Override
    public AssetStorage assets() {
        return assets;
    }

    @Override
    public GameSpriteSheet spriteSheet(GameVariant variant) {
        return assets.get(assetPrefix(variant) + ".spritesheet");
    }

    @Override
    public GameWorldRenderer renderer() {
        return worldRenderer;
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

    // -----------------------------------------------------------------------------------------------------------------
    // GameScene related
    // -----------------------------------------------------------------------------------------------------------------

    protected boolean hasID(GameScene gameScene, GameSceneID sceneID) {
        return gameScene(gameVariant(), sceneID) == gameScene;
    }

    //TODO maybe return an Optional?
    protected GameScene gameScene(GameVariant variant, GameSceneID sceneID) {
        GameScene gameScene = gameSceneMap.get(variant).get(sceneID);
        if (gameScene != null) {
            return gameScene;
        }
        throw new IllegalStateException(
            String.format("No game scene found for ID %s in game variant %s", sceneID, variant));
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
        createActorAnimations(game(), spriteSheet());
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
        if (gameState() != TESTING_LEVEL_BONI && !game().isDemoLevel() && game().levelNumber() == 1) {
            sounds().playGameReadySound();
        }
    }

    @Override
    public void onStopAllSounds(GameEvent event) {
        sounds().stopAll();
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Actions
    // -----------------------------------------------------------------------------------------------------------------

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
    public GamePage gamePage() {
        return gamePage;
    }

    @Override
    public void showFlashMessage(String message, Object... args) {
        showFlashMessageSeconds(1, message, args);
    }

    @Override
    public void showFlashMessageSeconds(double seconds, String message, Object... args) {
        flashMessageLayer.showMessage(String.format(message, args), seconds);
    }
}