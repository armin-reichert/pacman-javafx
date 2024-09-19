/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.maps.editor.TileMapEditor;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.pacmanxxl.PacManXXLGame;
import de.amr.games.pacman.ui2d.dashboard.InfoBoxCustomMaps;
import de.amr.games.pacman.ui2d.page.EditorPage;
import de.amr.games.pacman.ui2d.page.GamePage;
import de.amr.games.pacman.ui2d.page.Page;
import de.amr.games.pacman.ui2d.page.StartPage;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import de.amr.games.pacman.ui2d.rendering.ms_pacman.MsPacManArcadeGameWorldRenderer;
import de.amr.games.pacman.ui2d.rendering.ms_pacman.MsPacManGameGhostAnimations;
import de.amr.games.pacman.ui2d.rendering.ms_pacman.MsPacManGamePacAnimations;
import de.amr.games.pacman.ui2d.rendering.ms_pacman.MsPacManGameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.pacman.PacManArcadeGameWorldRenderer;
import de.amr.games.pacman.ui2d.rendering.pacman.PacManGameGhostAnimations;
import de.amr.games.pacman.ui2d.rendering.pacman.PacManGamePacAnimations;
import de.amr.games.pacman.ui2d.rendering.pacman.PacManGameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.pacman_xxl.PacManXXLGameWorldRenderer;
import de.amr.games.pacman.ui2d.rendering.tengen.TengenMsPacManGameWorldRenderer;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui2d.scene.GameScene2D;
import de.amr.games.pacman.ui2d.scene.GameSceneID;
import de.amr.games.pacman.ui2d.util.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import javafx.scene.media.AudioClip;
import javafx.stage.Stage;
import org.tinylog.Logger;

import java.text.MessageFormat;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static de.amr.games.pacman.controller.GameState.INTRO;
import static de.amr.games.pacman.controller.GameState.LEVEL_TEST;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.model.actors.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.actors.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.*;
import static de.amr.games.pacman.ui2d.util.Ufx.toggle;
import static java.util.function.Predicate.not;

/**
 * 2D user interface for all Pac-Man game variants.
 *
 * @author Armin Reichert
 */
public class PacManGames2dUI implements GameEventListener, GameContext {

    /**
     * The order here is used by the start page!
     */
    public static List<GameVariant> GAME_VARIANTS_IN_ORDER = List.of(
        GameVariant.PACMAN,
        GameVariant.PACMAN_XXL,
        GameVariant.MS_PACMAN,
        GameVariant.MS_PACMAN_TENGEN
    );

    public final ObjectProperty<GameScene> gameScenePy = new SimpleObjectProperty<>(this, "gameScene");

    public final ObjectProperty<GameVariant> gameVariantPy = new SimpleObjectProperty<>(this, "gameVariant") {
        @Override
        protected void invalidated() {
            Logger.info("Game variant changed to: {}", get());
            handleGameVariantChange(get());
        }
    };

    public final BooleanProperty scoreVisiblePy = new SimpleBooleanProperty(this, "scoreVisible");

    protected final Dimension2D initialSize;
    protected final AssetStorage assets = new AssetStorage();
    protected Map<GameVariant, Map<GameSceneID, GameScene>> gameSceneMap;
    protected final FlashMessageView messageView = new FlashMessageView();
    protected final GameClockFX clock = new GameClockFX();
    protected final StackPane sceneRoot = new StackPane();
    protected Stage stage;
    protected StartPage startPage;
    protected GamePage gamePage;
    protected EditorPage editorPage;
    protected Page currentPage;

    public PacManGames2dUI(Dimension2D initialSize) {
        this.initialSize = checkNotNull(initialSize);
    }

    public void setGameScenes(Map<GameVariant, Map<GameSceneID, GameScene>> gameSceneMap) {
        this.gameSceneMap = checkNotNull(gameSceneMap);
        for (GameVariant variant : GameVariant.values()) {
            gameSceneMap.get(variant).values().forEach(gameScene -> {
                if (gameScene instanceof GameScene2D gameScene2D) {
                    gameScene2D.setGameContext(this);
                    gameScene2D.debugInfoPy.bind(PY_DEBUG_INFO);
                }
            });
        }
    }

    /**
     * Called from application start method (on JavaFX application thread).

     * @param stage primary stage (window)
     */
    public void createAndStart(Stage stage) {
        this.stage = checkNotNull(stage);

        sceneRoot.getChildren().addAll(new Pane(), messageView, createMutedIcon());
        stage.setScene(createMainScene(initialSize));

        startPage = new StartPage(this);
        startPage.gameVariantPy.bind(gameVariantPy);

        gamePage = createGamePage(stage.getScene());

        clock.setPauseableCallback(this::onNonPausedClockTick);
        clock.setPermanentCallback(this::onEveryClockTick);

        // start the whole machinery
        for (var variant : GameVariant.values()) {
            GameController.it().gameModel(variant).addGameEventListener(this);
            // TODO: find better way
            GameController.it().gameModel(variant).addGameEventListener(gamePage.dashboardLayer().getPip());
        }

        // Touch all game actions such that they get bound to keyboard
        for (var gameAction : GameAction.values()) {
            Logger.info("Game Action: {} => {}", gameAction, gameAction.trigger());
        }

        // select game variant of current game model
        gameVariantPy.set(game().variant());
        GameSounds.gameVariantProperty().bind(gameVariantPy);

        // Not sure where this belongs
        PacManXXLGame xxlGame = gameController().gameModel(GameVariant.PACMAN_XXL);
        PY_MAP_SELECTION_MODE.addListener((py,ov,nv) -> {
            xxlGame.loadCustomMaps();
            xxlGame.setMapSelectionMode(nv);
        });
        xxlGame.setMapSelectionMode(PY_MAP_SELECTION_MODE.get());

        stage.titleProperty().bind(stageTitleBinding());
        //TODO this does not work yet correctly
        Dimension2D minSize = DecoratedCanvas.computeSize(GameModel.ARCADE_MAP_SIZE_X, GameModel.ARCADE_MAP_SIZE_Y, 1);
        stage.setMinWidth(minSize.getWidth());
        stage.setMinHeight(minSize.getHeight());
        stage.centerOnScreen();
        stage.setOnShowing(e-> selectStartPage());
        stage.show();
    }

    /**
     * Executed on every clock tick if game is not paused.
     */
    protected void onNonPausedClockTick() {
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
     * Executed on every clock tick even if game is paused.
     */
    protected void onEveryClockTick() {
        try {
            if (currentPage == gamePage) {
                currentGameScene()
                    .filter(GameScene2D.class::isInstance).map(GameScene2D.class::cast)
                    .ifPresent(GameScene2D::draw);
                gamePage.updateDashboard();
                messageView.update();
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
        mainScene.addEventFilter(KeyEvent.KEY_PRESSED, Keyboard::onKeyPressed);
        mainScene.addEventFilter(KeyEvent.KEY_RELEASED, Keyboard::onKeyReleased);
        mainScene.setOnKeyPressed(e -> {
            if (GameAction.FULLSCREEN.triggered()) {
                stage.setFullScreen(true);
            } else if (GameAction.MUTE.triggered()) {
                GameSounds.toggleMuted();
            } else {
                currentPage.handleKeyboardInput();
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
        Image icon = assets.image(assetPrefix(variant) + ".icon");
        if (icon != null) {
            stage.getIcons().setAll(icon);
        }
        if (variant == GameVariant.PACMAN_XXL) {
            updateCustomMaps();
        }
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
        icon.visibleProperty().bind(GameSounds.mutedProperty());
        StackPane.setAlignment(icon, Pos.BOTTOM_RIGHT);
        return icon;
    }

    protected void selectPage(Page page) {
        if (page != currentPage) {
            currentPage = page;
            currentPage.setSize(stage.getScene().getWidth(), stage.getScene().getHeight());
            sceneRoot.getChildren().set(0, currentPage.rootPane());
            currentPage.rootPane().requestFocus();
            currentPage.onSelected();
        }
    }

    private String displayName(GameScene gameScene) {
        String text = gameScene != null ? gameScene.getClass().getSimpleName() : "NO GAME SCENE";
        text += String.format(" (%s)", game().variant());
        return text;
    }

    protected void updateGameScene(boolean reloadCurrent) {
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

    private void configureGameScene2D(GameScene2D gameScene2D) {
        GameWorldRenderer renderer = switch (game().variant()) {
            case MS_PACMAN -> new MsPacManArcadeGameWorldRenderer(assets);
            case MS_PACMAN_TENGEN -> new TengenMsPacManGameWorldRenderer(assets);
            case PACMAN -> new PacManArcadeGameWorldRenderer(assets);
            case PACMAN_XXL -> new PacManXXLGameWorldRenderer(assets);
        };
        renderer.scalingProperty().bind(gameScene2D.scalingPy);
        renderer.backgroundColorProperty().bind(gameScene2D.backgroundColorPy);
        gameScene2D.setRenderer(renderer);
        gameScene2D.backgroundColorPy.bind(PY_CANVAS_COLOR);
    }

    protected GameScene gameSceneForCurrentGameState() {
        GameVariant variant = game().variant();
        return switch (gameState()) {
            case BOOT -> gameScene(variant, GameSceneID.BOOT_SCENE);
            case CREDIT -> gameScene(variant, GameSceneID.CREDIT_SCENE);
            case INTRO -> gameScene(variant, GameSceneID.INTRO_SCENE);
            case INTERMISSION -> gameScene(variant, GameSceneID.valueOf(
                "CUT_SCENE_" + game().intermissionNumber(game().levelNumber())));
            case INTERMISSION_TEST -> gameScene(variant, GameSceneID.valueOf(
                "CUT_SCENE_" + gameState().<Integer>getProperty("intermissionTestNumber")));
            default -> gameScene(variant, GameSceneID.PLAY_SCENE);
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
    public GameClockFX gameClock() {
        return clock;
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
    public AssetStorage assets() {
        return assets;
    }

    @Override
    public SpriteSheet spriteSheet(GameVariant variant) {
        return switch(variant) {
            case MS_PACMAN        -> assets.get("ms_pacman.spritesheet");
            case PACMAN           -> assets.get("pacman.spritesheet");
            case PACMAN_XXL       -> assets.get("pacman_xxl.spritesheet");
            case MS_PACMAN_TENGEN -> assets.get("tengen.spritesheet.tmp"); //TODO
        };
    }

    @Override
    public boolean isScoreVisible() {
        return scoreVisiblePy.get();
    }

    @Override
    public void setScoreVisible(boolean visible) {
        scoreVisiblePy.set(visible);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // GameScene related
    // -----------------------------------------------------------------------------------------------------------------

    protected boolean hasID(GameScene gameScene, GameSceneID sceneID) {
        return gameScene(game().variant(), sceneID) == gameScene;
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
    public void onGameVariantChanged(GameEvent event) {
        gameVariantPy.set(event.game.variant());
    }

    @Override
    public void onUnspecifiedChange(GameEvent event) {
        updateGameScene(true);
    }

    @Override
    public void onLevelCreated(GameEvent event) {
        // Found no better point in time to create and assign the sprite animations to the guys
        GameModel game = event.game;
        switch (game.variant()) {
            case MS_PACMAN -> {
                var ss = (MsPacManGameSpriteSheet) spriteSheet(game.variant());
                game.pac().setAnimations(new MsPacManGamePacAnimations(game.pac(), ss));
                game.ghosts().forEach(ghost -> ghost.setAnimations(new MsPacManGameGhostAnimations(ghost, ss)));
                Logger.info("Created Ms. Pac-Man game creature animations for level #{}", game.levelNumber());
            }
            case MS_PACMAN_TENGEN -> {
                var ss = (MsPacManGameSpriteSheet) spriteSheet(game.variant());
                game.pac().setAnimations(new MsPacManGamePacAnimations(game.pac(), ss));
                game.ghosts().forEach(ghost -> ghost.setAnimations(new MsPacManGameGhostAnimations(ghost, ss)));
                Logger.info("Created Ms. Pac-Man Tengen game creature animations for level #{}", game.levelNumber());
            }
            case PACMAN, PACMAN_XXL -> {
                var ss = (PacManGameSpriteSheet) spriteSheet(game.variant());
                game.pac().setAnimations(new PacManGamePacAnimations(game.pac(), ss));
                game.ghosts().forEach(ghost -> ghost.setAnimations(new PacManGameGhostAnimations(ghost, ss)));
                Logger.info("Created Pac-Man game creature animations for level #{}", game.levelNumber());
            }
            default -> throw new IllegalArgumentException("Unsupported game variant: " + game.variant());
        }
        if (!game.isDemoLevel()) {
            game.pac().setManualSteering(new KeyboardPacSteering());
        }
        GameSounds.enabledProperty().set(!game.isDemoLevel());
        //TODO better place than here?
        gamePage.adaptCanvasSizeToCurrentWorld();
    }

    @Override
    public void onLevelStarted(GameEvent event) {
        if (gameState() == LEVEL_TEST || game().isDemoLevel() || game().levelNumber() > 1) {
            return;
        }
        GameSounds.playGameReadySound();
        LocalTime now = LocalTime.now();
        PY_NIGHT_MODE.set(now.getHour() >= 21 || now.getHour() <= 5);
    }

    @Override
    public void onStopAllSounds(GameEvent event) {
        GameSounds.stopAll();
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Actions
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public void selectStartPage() {
        clock.stop();
        selectPage(startPage);
    }

    @Override
    public void selectGamePage() {
        selectPage(gamePage);
        clock.start();
    }

    @Override
    public void openMapEditor() {
        if (game().variant() != GameVariant.PACMAN_XXL) {
            showFlashMessageSeconds(3, "Map editor is not available in this game variant");
            return;
        }
        currentGameScene().ifPresent(GameScene::end);
        GameSounds.stopAll();
        clock.stop();
        if (editorPage == null) {
            var xxlGame = (PacManXXLGame) game();
            editorPage = new EditorPage(stage, this, xxlGame.customMapDir());
            editorPage.setCloseAction(this::quitMapEditor);
        }
        editorPage.startEditor(game().world().map());
        selectPage(editorPage);
    }

    @Override
    public void showFlashMessage(String message, Object... args) {
        showFlashMessageSeconds(1, message, args);
    }

    @Override
    public void showFlashMessageSeconds(double seconds, String message, Object... args) {
        messageView.showMessage(String.format(message, args), seconds);
    }

    @Override
    public void startGame() {
        if (game().hasCredit()) {
            GameSounds.stopVoice();
            if (gameState() == GameState.INTRO || gameState() == GameState.CREDIT) {
                gameController().changeState(GameState.READY);
            } else {
                Logger.error("Cannot start playing when in game state {}", gameState());
            }
        }
    }

    @Override
    public void quitMapEditor(TileMapEditor editor) {
        editor.showSaveConfirmationDialog(editor::showSaveDialog, () -> stage.titleProperty().bind(stageTitleBinding()));
        editor.stop();
        updateCustomMaps();
        reboot();
        selectStartPage();
    }

    @Override
    public void updateCustomMaps() {
        PacManXXLGame xxlGame = gameController().gameModel(GameVariant.PACMAN_XXL);
        xxlGame.loadCustomMaps();
        Logger.info("Custom maps: {}", xxlGame.customMapsSortedByFile());
        /* TODO: Find better solution
        This is total crap! But the "custom map" collection lives inside the model which is
        JavaFX-unaware, there is no observable FX collection where the infobox could register a
        change listener. */
        for (var infoBox : gamePage.dashboardLayer().getInfoBoxes()) {
            if (infoBox instanceof InfoBoxCustomMaps customMapsInfoBox) {
                customMapsInfoBox.updateTableView();
                break;
            }
        }
    }

    @Override
    public void startCutscenesTest() {
        if (gameState() == GameState.INTRO) {
            gameController().changeState(GameState.INTERMISSION_TEST);
        } else {
            Logger.error("Intermission test can only be started from intro screen");
        }
        showFlashMessage("Cut scenes");
    }

    @Override
    public void restartIntro() {
        GameSounds.stopAll();
        currentGameScene().ifPresent(GameScene::end);
        if (gameState() == LEVEL_TEST) {
            gameState().onExit(game()); //TODO exit other states too?
        }
        clock.setTargetFrameRate(GameModel.FPS);
        gameController().restart(INTRO);
    }

    @Override
    public void reboot() {
        GameSounds.stopAll();
        currentGameScene().ifPresent(GameScene::end);
        game().removeWorld();
        clock.setTargetFrameRate(GameModel.FPS);
        gameController().restart(GameState.BOOT);
    }

    /**
     * Adds credit (simulates insertion of a coin) and switches to the credit scene.
     */
    @Override
    public void addCredit() {
        GameSounds.enabledProperty().set(true); // in demo mode, sound is disabled
        GameSounds.playCreditSound();
        if (!game().isPlaying()) {
            boolean coinInserted = game().insertCoin();
            if (coinInserted) {
                game().publishGameEvent(GameEventType.CREDIT_ADDED);
            }
            if (gameState() != GameState.CREDIT) {
                gameController().changeState(GameState.CREDIT);
            }
        }
    }

    @Override
    public void togglePaused() {
        toggle(clock.pausedPy);
        if (clock.isPaused()) {
            assets().audioClips().forEach(AudioClip::stop);
            GameSounds.stopSiren();
        }
        Logger.info("Game variant ({}) {}", game(), clock.isPaused() ? "paused" : "resumed");
    }

    @Override
    public void toggleDashboard() {
        gamePage.toggleDashboard();
    }

    @Override
    public void toggleDrawMode() {
        // not supported in 2D UI
    }

    @Override
    public void togglePipVisible() {
        toggle(PY_PIP_ON);
        if (!currentGameSceneIs(GameSceneID.PLAY_SCENE_3D)) {
            showFlashMessage(locText(PY_PIP_ON.get() ? "pip_on" : "pip_off"));
        }
    }

    @Override
    public void doSimulationSteps(int numSteps) {
        if (clock.isPaused()) {
            if (numSteps == 1) {
                clock.makeStep(true);
            } else {
                clock.makeSteps(numSteps, true);
            }
        }
    }

    @Override
    public void changeSimulationSpeed(int delta) {
        double newRate = clock.getTargetFrameRate() + delta;
        if (newRate > 0) {
            clock.setTargetFrameRate(newRate);
            showFlashMessageSeconds(0.75, newRate + "Hz");
        }
    }

    @Override
    public void resetSimulationSpeed() {
        clock.setTargetFrameRate(GameModel.FPS);
        showFlashMessageSeconds(0.75, clock.getTargetFrameRate() + "Hz");
    }

    @Override
    public void selectNextGameVariant() {
        int nextIndex = GAME_VARIANTS_IN_ORDER.indexOf(game().variant()) + 1;
        selectGameVariant(GAME_VARIANTS_IN_ORDER.get(nextIndex == GAME_VARIANTS_IN_ORDER.size() ? 0 : nextIndex));
    }

    @Override
    public void selectPrevGameVariant() {
        int prevIndex = GAME_VARIANTS_IN_ORDER.indexOf(game().variant()) - 1;
        selectGameVariant(GAME_VARIANTS_IN_ORDER.get(prevIndex < 0 ? GAME_VARIANTS_IN_ORDER.size() - 1 : prevIndex));
    }

    private void selectGameVariant(GameVariant variant) {
        gameController().selectGame(variant);
        gameController().restart(GameState.BOOT);
        Logger.info("Selected game variant: {}", variant);
    }

    @Override
    public void selectNext3DPerspective() {
        // not supported in 2D UI
    }

    @Override
    public void selectPrev3DPerspective() {
        // not supported in 2D UI
    }

    @Override
    public void toggleAutopilot() {
        toggle(PY_AUTOPILOT);
        boolean auto = PY_AUTOPILOT.get();
        showFlashMessage(locText(auto ? "autopilot_on" : "autopilot_off"));
        GameSounds.playVoice(auto ? "voice.autopilot.on" : "voice.autopilot.off", 0);
    }

    @Override
    public void toggle2D3D() {
        // not supported in 2D UI
    }

    @Override
    public void toggleImmunity() {
        toggle(PY_IMMUNITY);
        showFlashMessage(locText(PY_IMMUNITY.get() ? "player_immunity_on" : "player_immunity_off"));
        GameSounds.playVoice(PY_IMMUNITY.get() ? "voice.immunity.on" : "voice.immunity.off", 0);
    }

    @Override
    public void startLevelTestMode() {
        if (gameState() == GameState.INTRO) {
            gameController().restart(GameState.LEVEL_TEST);
            showFlashMessageSeconds(3, "Level TEST MODE");
        }
    }

    @Override
    public void cheatAddLives() {
        game().addLives(3);
        showFlashMessage(locText("cheat_add_lives", game().lives()));
    }

    @Override
    public void cheatEatAllPellets() {
        if (game().isPlaying() && gameState() == GameState.HUNTING) {
            GameWorld world = game().world();
            world.map().food().tiles().filter(not(world::isEnergizerPosition)).forEach(world::eatFoodAt);
            game().publishGameEvent(GameEventType.PAC_FOUND_FOOD);
        }
    }

    @Override
    public void cheatKillAllEatableGhosts() {
        if (game().isPlaying() && gameState() == GameState.HUNTING) {
            game().victims().clear();
            game().ghosts(FRIGHTENED, HUNTING_PAC).forEach(game()::killGhost);
            gameController().changeState(GameState.GHOST_DYING);
        }
    }

    @Override
    public void cheatEnterNextLevel() {
        if (game().isPlaying() && gameState() == GameState.HUNTING) {
            gameController().changeState(GameState.LEVEL_COMPLETE);
        }
    }
}