/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameEventListener;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.mapeditor.TileMapEditor;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.pacmanxxl.PacManXXLGameModel;
import de.amr.games.pacman.ui2d.page.EditorPage;
import de.amr.games.pacman.ui2d.page.GamePage;
import de.amr.games.pacman.ui2d.page.Page;
import de.amr.games.pacman.ui2d.page.StartPage;
import de.amr.games.pacman.ui2d.rendering.*;
import de.amr.games.pacman.ui2d.scene.GameScene;
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
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.tinylog.Logger;

import java.text.MessageFormat;
import java.time.LocalTime;
import java.util.*;

import static de.amr.games.pacman.controller.GameState.INTRO;
import static de.amr.games.pacman.controller.GameState.LEVEL_TEST;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.model.actors.GhostState.FRIGHTENED;
import static de.amr.games.pacman.model.actors.GhostState.HUNTING_PAC;
import static de.amr.games.pacman.ui2d.GameParameters.*;
import static de.amr.games.pacman.ui2d.util.Ufx.toggle;
import static java.util.function.Predicate.not;

/**
 * 2D-only user interface for Pac-Man and Ms. Pac-Man games. No 3D scenes.
 *
 * @author Armin Reichert
 */
public class PacManGames2dUI implements GameEventListener, GameContext, ActionHandler {

    public final ObjectProperty<GameScene> gameScenePy = new SimpleObjectProperty<>(this, "gameScene") {
        @Override
        protected void invalidated() {
            gamePage.setGameScene(get());
        }
    };

    public final ObjectProperty<GameVariant> gameVariantPy = new SimpleObjectProperty<>(this, "gameVariant") {
        @Override
        protected void invalidated() {
            Logger.info("Game variant changed to: {}", get());
            handleGameVariantChange(get());
        }
    };

    public final BooleanProperty scoreVisiblePy = new SimpleBooleanProperty(this, "scoreVisible");

    protected final AssetMap assets = new AssetMap();
    protected final FlashMessageView messageView = new FlashMessageView();
    protected final GameClockFX clock = new GameClockFX();
    protected final StackPane layout = new StackPane();
    protected Stage stage;
    protected StartPage startPage;
    protected GamePage gamePage;
    protected EditorPage editorPage;
    protected Page currentPage;
    protected Map<GameVariant, Map<GameSceneID, GameScene>> gameScenesForVariant = new EnumMap<>(GameVariant.class);

    public void createLayout(Stage stage, Dimension2D size) {
        this.stage = checkNotNull(stage);

        // Touch all game keys such that they get registered with keyboard
        for (var gameKey : GameKey.values()) {
            Logger.debug("Game key '{}' registered", gameKey);
        }

        // first child will be replaced by page
        layout.getChildren().addAll(new Pane(), messageView, createMutedIcon());

        Scene mainScene = new Scene(layout, size.getWidth(), size.getHeight());
        stage.setScene(mainScene);

        initMainScene(mainScene);

        startPage = new StartPage(this);
        startPage.gameVariantPy.bind(gameVariantPy);

        gamePage = createGamePage(mainScene);
        gamePage.sign(assets.font("font.monospaced", 9), locText("app.signature"));
    }

    public void setGameScenes(Map<GameVariant, Map<GameSceneID, GameScene>> gameScenesForVariant) {
        this.gameScenesForVariant = gameScenesForVariant;
    }

    public StackPane layout() {
        return layout;
    }

    public void start() {

        // Configure game clock
        clock.setPauseableCallback(() -> {
            try {
                gameController().update();
                currentGameScene().ifPresent(GameScene::update);
            } catch (Exception x) {
                clock.stop();
                Logger.error("Game update caused an error, game clock stopped!");
                Logger.error(x);
            }
        });
        clock.setContinousCallback(() -> {
            try {
                if (currentPage == gamePage) {
                    messageView.update();
                    gamePage.render();
                }
            } catch (Exception x) {
                clock.stop();
                Logger.error("Game page rendering caused an error, game clock stopped!");
                Logger.error(x);
            }
        });
        gameController().setClock(clock);

        // select game variant of current game model
        gameVariantPy.set(game().variant());
        GameSounds.gameVariantProperty().bind(gameVariantPy);

        stage.titleProperty().bind(stageTitleBinding());
        //TODO this does not work yet correctly
        Dimension2D minSize = DecoratedCanvas.computeSize(GameModel.ARCADE_MAP_SIZE_X, GameModel.ARCADE_MAP_SIZE_Y, 1);
        stage.setMinWidth(minSize.getWidth());
        stage.setMinHeight(minSize.getHeight());
        stage.centerOnScreen();
        stage.setOnShowing(e-> selectStartPage());
        stage.show();
    }

    protected void initMainScene(Scene mainScene) {
        mainScene.addEventFilter(KeyEvent.KEY_PRESSED, Keyboard::onKeyPressed);
        mainScene.addEventFilter(KeyEvent.KEY_RELEASED, Keyboard::onKeyReleased);
        mainScene.setOnKeyPressed(e -> {
            if (GameKey.FULLSCREEN.pressed()) {
                stage.setFullScreen(true);
            } else if (GameKey.MUTE.pressed()) {
                GameSounds.toggleMuted();
            } else {
                currentPage.handleKeyboardInput(this);
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
    }

    protected GamePage createGamePage(Scene mainScene) {
        return new GamePage(this, mainScene);
    }

    private void handleGameVariantChange(GameVariant variant) {
        stage.getIcons().setAll(assets.image(variant.resourceKey() + ".icon"));
        if (variant == GameVariant.PACMAN_XXL) {
            updateCustomMaps();
        }
    }

    protected ObservableValue<String> stageTitleBinding() {
        return Bindings.createStringBinding(
            () -> {
                String gameVariantPart = "app.title." + gameVariantPy.get().resourceKey();
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
            layout.getChildren().set(0, currentPage.rootPane());
            currentPage.rootPane().requestFocus();
            currentPage.onSelected();
        }
    }

    protected void updateGameScene(boolean reloadCurrentScene) {
        if (currentPage == startPage) {
            return; // no game scene on start page
        }
        GameScene sceneToDisplay = gameSceneForCurrentGameState();
        GameScene currentScene = gameScenePy.get();
        if (reloadCurrentScene || sceneToDisplay != currentScene) {
            Logger.info("updateGameScene: {}/{} reload={}", currentPage, sceneToDisplay.getClass().getSimpleName(), reloadCurrentScene);
            if (currentScene != null) {
                currentScene.end();
            }
            sceneToDisplay.init();
            gameScenePy.set(sceneToDisplay);
            if (sceneToDisplay == currentScene) {
                Logger.info("Game scene has been reloaded {}", gameScenePy.get());
            } else {
                Logger.info("Game scene changed to {}/{}", currentPage, gameScenePy.get());
            }
        }
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
    public ActionHandler actionHandler() {
        return this;
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
    public AssetMap assets() {
        return assets;
    }

    @Override
    public GameSpriteSheet spriteSheet(GameVariant variant) {
        var rk = switch (variant) {
            case MS_PACMAN -> variant.resourceKey();
            case PACMAN, PACMAN_XXL -> GameVariant.PACMAN.resourceKey();
        };
        return assets.get(rk + ".spritesheet");
    }

    @Override
    public boolean isScoreVisible() {
        return scoreVisiblePy.get();
    }

    @Override
    public void setScoreVisible(boolean visible) {
        scoreVisiblePy.set(visible);
    }

    public void showSignature() {
        gamePage.signature().show(2, 3);
    }

    public void hideSignature() {
        gamePage.signature().hide();
    }

    // -----------------------------------------------------------------------------------------------------------------
    // GameScene related
    // -----------------------------------------------------------------------------------------------------------------

    protected boolean hasID(GameScene gameScene, GameSceneID sceneID) {
        return gameScene(game().variant(), sceneID) == gameScene;
    }

    protected GameScene gameScene(GameVariant variant, GameSceneID sceneID) {
        return gameScenesForVariant.get(variant).get(sceneID);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // GameEventListener interface implementation
    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public void onGameEvent(GameEvent event) {
        Logger.trace("Received: {}", event);
        // call event specific hook method:
        GameEventListener.super.onGameEvent(event);
        updateGameScene(false);
        // dispatch event to current game scene if any
        currentGameScene().ifPresent(gameScene -> gameScene.onGameEvent(event));
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
    public void onIntermissionStarted(GameEvent event) {
        if (gameState() == GameState.INTERMISSION_TEST) {
            //TODO this is ugly
            int number = GameState.INTERMISSION_TEST.getProperty("intermissionTestNumber");
            GameSounds.playIntermissionSound(number);
        } else {
            GameSounds.playIntermissionSound(game().intermissionNumber(game().levelNumber()));
        }
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
            case PACMAN, PACMAN_XXL -> {
                var ss = (PacManGameSpriteSheet) spriteSheet(game.variant());
                game.pac().setAnimations(new PacManGamePacAnimations(game.pac(), ss));
                game.ghosts().forEach(ghost -> ghost.setAnimations(new PacManGameGhostAnimations(ghost, ss)));
                Logger.info("Created Pac-Man game creature animations for level #{}", game.levelNumber());
            }
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
    // ActionHandler interface implementation
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
    public void selectEditorPage() {
        clock.stop();
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
        if (gameController().hasCredit()) {
            GameSounds.stopVoice();
            if (gameState() == GameState.INTRO || gameState() == GameState.CREDIT) {
                gameController().changeState(GameState.READY);
            } else {
                Logger.error("Cannot start playing when in game state {}", gameState());
            }
        }
        PY_IMMUNITY.set(gameController().isPacImmune());
    }

    @Override
    public void openMapEditor() {
        if (game().variant() != GameVariant.PACMAN_XXL) {
            showFlashMessageSeconds(3, "Map editor is not available in this game variant");
            return;
        }
        if (editorPage == null) {
            editorPage = new EditorPage(stage, this);
            editorPage.setCloseAction(this::quitMapEditor);
        }
        GameSounds.stopAll();
        currentGameScene().ifPresent(GameScene::end);
        stage.titleProperty().bind(editorPage.editor().titlePy);
        if (game().world() != null) {
            editorPage.editor().setMap(game().world().map());
        }
        editorPage.startEditor();
        selectEditorPage();
    }

    @Override
    public void quitMapEditor(TileMapEditor editor) {
        editor.showConfirmation(editor::saveMapFileAs, () -> stage.titleProperty().bind(stageTitleBinding()));
        editor.stop();
        updateCustomMaps();
        reboot();
        selectStartPage();
    }

    @Override
    public void updateCustomMaps() {
        PacManXXLGameModel xxlGame = gameController().gameModel(GameVariant.PACMAN_XXL);
        xxlGame.loadCustomMaps();
        Logger.info("Custom maps: {}", xxlGame.customMaps());
        //TODO dashboard should be updated too!
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
            boolean added = gameController().changeCredit(1);
            if (added) {
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
        gamePage.dashboard().toggleVisibility();
    }

    @Override
    public void toggleDrawMode() {
        // not supported in 2D UI
    }

    @Override
    public void togglePipVisible() {
        toggle(PY_PIP_ON);
        showFlashMessage(locText(PY_PIP_ON.get() ? "pip_on" : "pip_off"));
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
        var all = Arrays.asList(GameVariant.PACMAN, GameVariant.MS_PACMAN, GameVariant.PACMAN_XXL);
        var current = all.indexOf(game().variant());
        var next = current < all.size() - 1 ? all.get(current + 1) : all.get(0);
        selectGameVariant(next);
    }

    @Override
    public void selectPrevGameVariant() {
        var all = Arrays.asList(GameVariant.PACMAN, GameVariant.MS_PACMAN, GameVariant.PACMAN_XXL);
        var current = all.indexOf(game().variant());
        var prev = current > 0 ? all.get(current - 1) : all.get(all.size() - 1);
        selectGameVariant(prev);
    }

    private void selectGameVariant(GameVariant variant) {
        gameController().selectGameVariant(variant);
        gameController().restart(GameState.BOOT);
    }

    @Override
    public void selectNextPerspective() {
        // not supported in 2D UI
    }

    @Override
    public void selectPrevPerspective() {
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

    // -----------------------------------------------------------------------------------------------------------------
    // Assets
    // -----------------------------------------------------------------------------------------------------------------

    public void loadAssets(ResourceManager rm, boolean log) {

        assets.addBundle(ResourceBundle.getBundle("de.amr.games.pacman.ui2d.texts.messages", rm.rootClass().getModule()));

        // Dashboard

        assets.set("image.armin1970",                 rm.loadImage("graphics/armin.jpg"));
        assets.set("icon.mute",                       rm.loadImage("graphics/icons/mute.png"));
        assets.set("icon.play",                       rm.loadImage("graphics/icons/play.png"));
        assets.set("icon.stop",                       rm.loadImage("graphics/icons/stop.png"));
        assets.set("icon.step",                       rm.loadImage("graphics/icons/step.png"));

        assets.set("infobox.min_label_width",         140);
        assets.set("infobox.min_col_width",           200);
        assets.set("infobox.text_color",              Color.WHITE);
        assets.set("infobox.label_font",              Font.font("Sans", 12));
        assets.set("infobox.text_font",               rm.loadFont("fonts/SplineSansMono-Regular.ttf", 12));

        //
        // Common to all game variants
        //

        assets.set("palette.black",                   Color.rgb(0, 0, 0));
        assets.set("palette.red",                     Color.rgb(255, 0, 0));
        assets.set("palette.yellow",                  Color.rgb(255, 255, 0));
        assets.set("palette.pink",                    Color.rgb(252, 181, 255));
        assets.set("palette.cyan",                    Color.rgb(0, 255, 255));
        assets.set("palette.orange",                  Color.rgb(251, 190, 88));
        assets.set("palette.blue",                    Color.rgb(33, 33, 255));
        assets.set("palette.pale",                    Color.rgb(222, 222, 255));
        assets.set("palette.rose",                    Color.rgb(252, 187, 179));

        assets.set("startpage.arrow.left",            rm.loadImage("graphics/icons/arrow-left.png"));
        assets.set("startpage.arrow.right",           rm.loadImage("graphics/icons/arrow-right.png"));
        assets.set("startpage.button.bgColor",        Color.rgb(0, 155, 252, 0.8));
        assets.set("startpage.button.color",          Color.WHITE);
        assets.set("startpage.button.font",           rm.loadFont("fonts/emulogic.ttf", 30));

        assets.set("wallpaper.background",            Ufx.imageBackground(rm.loadImage("graphics/pacman_wallpaper.png")));
        assets.set("wallpaper.color",                 Color.rgb(72, 78, 135));

        assets.set("font.arcade",                     rm.loadFont("fonts/emulogic.ttf", 8));
        assets.set("font.handwriting",                rm.loadFont("fonts/Molle-Italic.ttf", 9));
        assets.set("font.monospaced",                 rm.loadFont("fonts/Inconsolata_Condensed-Bold.ttf", 12));

        assets.set("voice.explain",                   rm.url("sound/voice/press-key.mp3"));
        assets.set("voice.autopilot.off",             rm.url("sound/voice/autopilot-off.mp3"));
        assets.set("voice.autopilot.on",              rm.url("sound/voice/autopilot-on.mp3"));
        assets.set("voice.immunity.off",              rm.url("sound/voice/immunity-off.mp3"));
        assets.set("voice.immunity.on",               rm.url("sound/voice/immunity-on.mp3"));

        //
        // Ms. Pac-Man game
        //

        assets.set("ms_pacman.spritesheet",           new MsPacManGameSpriteSheet());
        assets.set("ms_pacman.startpage.image",       rm.loadImage("graphics/mspacman/mspacman_flyer.png"));
        assets.set("ms_pacman.startpage.image1",      rm.loadImage("graphics/mspacman/mspacman_flyer1.jpg"));
        assets.set("ms_pacman.startpage.image2",      rm.loadImage("graphics/mspacman/mspacman_flyer2.jpg"));
        assets.set("ms_pacman.helpButton.icon",       rm.loadImage("graphics/icons/help-red-64.png"));
        assets.set("ms_pacman.icon",                  rm.loadImage("graphics/icons/mspacman.png"));
        assets.set("ms_pacman.logo.midway",           rm.loadImage("graphics/mspacman/midway_logo.png"));

        // Clips
        assets.set("ms_pacman.audio.bonus_eaten",     rm.loadAudioClip("sound/mspacman/Fruit.mp3"));
        assets.set("ms_pacman.audio.credit",          rm.loadAudioClip("sound/mspacman/Credit.mp3"));
        assets.set("ms_pacman.audio.extra_life",      rm.loadAudioClip("sound/mspacman/ExtraLife.mp3"));
        assets.set("ms_pacman.audio.ghost_eaten",     rm.loadAudioClip("sound/mspacman/Ghost.mp3"));
        assets.set("ms_pacman.audio.sweep",           rm.loadAudioClip("sound/common/sweep.mp3"));

        // Audio played by MediaPlayer
        assets.set("ms_pacman.audio.game_ready",      rm.url("sound/mspacman/Start.mp3"));
        assets.set("ms_pacman.audio.game_over",       rm.url("sound/common/game-over.mp3"));
        assets.set("ms_pacman.audio.intermission.1",  rm.url("sound/mspacman/Act1TheyMeet.mp3"));
        assets.set("ms_pacman.audio.intermission.2",  rm.url("sound/mspacman/Act2TheChase.mp3"));
        assets.set("ms_pacman.audio.intermission.3",  rm.url("sound/mspacman/Act3Junior.mp3"));
        assets.set("ms_pacman.audio.level_complete",  rm.url("sound/common/level-complete.mp3"));
        assets.set("ms_pacman.audio.pacman_death",    rm.url("sound/mspacman/Died.mp3"));
        assets.set("ms_pacman.audio.pacman_munch",    rm.url("sound/mspacman/Pill.wav"));
        assets.set("ms_pacman.audio.pacman_power",    rm.url("sound/mspacman/ScaredGhost.mp3"));
        assets.set("ms_pacman.audio.siren.1",         rm.url("sound/mspacman/GhostNoise1.wav"));
        assets.set("ms_pacman.audio.siren.2",         rm.url("sound/mspacman/GhostNoise1.wav"));// TODO
        assets.set("ms_pacman.audio.siren.3",         rm.url("sound/mspacman/GhostNoise1.wav"));// TODO
        assets.set("ms_pacman.audio.siren.4",         rm.url("sound/mspacman/GhostNoise1.wav"));// TODO
        assets.set("ms_pacman.audio.ghost_returning", rm.url("sound/mspacman/GhostEyes.mp3"));

        //
        // Pac-Man game
        //

        assets.set("pacman.spritesheet",              new PacManGameSpriteSheet());
        assets.set("pacman.startpage.image1",         rm.loadImage("graphics/pacman/pacman_flyer.png"));
        assets.set("pacman.startpage.image2",         rm.loadImage("graphics/pacman/pacman_flyer2.jpg"));
        assets.set("pacman.helpButton.icon",          rm.loadImage("graphics/icons/help-blue-64.png"));
        assets.set("pacman.icon",                     rm.loadImage("graphics/icons/pacman.png"));

        // Clips
        assets.set("pacman.audio.bonus_eaten",        rm.loadAudioClip("sound/pacman/eat_fruit.mp3"));
        assets.set("pacman.audio.credit",             rm.loadAudioClip("sound/pacman/credit.wav"));
        assets.set("pacman.audio.extra_life",         rm.loadAudioClip("sound/pacman/extend.mp3"));
        assets.set("pacman.audio.ghost_eaten",        rm.loadAudioClip("sound/pacman/eat_ghost.mp3"));
        assets.set("pacman.audio.sweep",              rm.loadAudioClip("sound/common/sweep.mp3"));

        // Media player sounds
        assets.set("pacman.audio.game_ready",         rm.url("sound/pacman/game_start.mp3"));
        assets.set("pacman.audio.game_over",          rm.url("sound/common/game-over.mp3"));
        assets.set("pacman.audio.intermission",       rm.url("sound/pacman/intermission.mp3"));
        assets.set("pacman.audio.pacman_death",       rm.url("sound/pacman/pacman_death.wav"));
        assets.set("pacman.audio.pacman_munch",       rm.url("sound/pacman/doublemunch.wav"));
        assets.set("pacman.audio.pacman_power",       rm.url("sound/pacman/ghost-turn-to-blue.mp3"));
        assets.set("pacman.audio.level_complete",     rm.url("sound/common/level-complete.mp3"));
        assets.set("pacman.audio.siren.1",            rm.url("sound/pacman/siren_1.mp3"));
        assets.set("pacman.audio.siren.2",            rm.url("sound/pacman/siren_2.mp3"));
        assets.set("pacman.audio.siren.3",            rm.url("sound/pacman/siren_3.mp3"));
        assets.set("pacman.audio.siren.4",            rm.url("sound/pacman/siren_4.mp3"));
        assets.set("pacman.audio.ghost_returning",    rm.url("sound/pacman/retreating.mp3"));

        //
        // Pac-Man XXL
        //
        assets.set("pacman_xxl.icon",                 rm.loadImage("graphics/icons/pacman.png"));
        assets.set("pacman_xxl.helpButton.icon",      rm.loadImage("graphics/icons/help-blue-64.png"));
        assets.set("pacman_xxl.startpage.image",      rm.loadImage("graphics/pacman_xxl/pacman_xxl_logo.png"));

        if (log) {
            Logger.info("Assets loaded: {}", assets.summary(List.of(
                new Pair<>(Image.class, "images"),
                new Pair<>(Font.class, "fonts"),
                new Pair<>(Color.class, "colors"),
                new Pair<>(AudioClip.class, "audio clips")
            )));
        }

        GameSounds.setAssets(assets);
    }
}