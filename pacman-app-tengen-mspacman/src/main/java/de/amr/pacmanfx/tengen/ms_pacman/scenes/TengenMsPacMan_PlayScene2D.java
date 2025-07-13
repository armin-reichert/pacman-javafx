/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.House;
import de.amr.pacmanfx.model.LivesCounter;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengen.ms_pacman.model.MapCategory;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_HUD;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_LevelCounter;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.ColorSchemedSprite;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_GameRenderer;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.CameraControlledView;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Camera;
import javafx.scene.Node;
import javafx.scene.ParallelCamera;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.controller.GameState.TESTING_LEVELS_MEDIUM;
import static de.amr.pacmanfx.controller.GameState.TESTING_LEVELS_SHORT;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.*;
import static de.amr.pacmanfx.ui.GameUI.*;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.*;
import static de.amr.pacmanfx.ui._2d.GameRenderer.fillCanvas;
import static de.amr.pacmanfx.uilib.Ufx.menuTitleItem;

/**
 * Tengen play scene, uses vertical scrolling.
 */
public class TengenMsPacMan_PlayScene2D extends GameScene2D implements CameraControlledView {

    // Width: 32 tiles (NES screen width), height: 42 tiles (BIG maps height) + 2 extra rows
    private static final int UNSCALED_CANVAS_WIDTH  = 32 * TS;
    private static final int UNSCALED_CANVAS_HEIGHT = 44 * TS;

    private static final int MOVING_MESSAGE_DELAY = 120;

    private final ObjectProperty<SceneDisplayMode> displayModeProperty = new SimpleObjectProperty<>(SceneDisplayMode.SCROLLING);

    private final SubScene fxSubScene;
    private final DynamicCamera dynamicCamera = new DynamicCamera();
    private final ParallelCamera fixedCamera  = new ParallelCamera();
    private final Rectangle canvasClipRect = new Rectangle();

    private MessageMovement messageMovement;
    private LevelCompletedAnimation levelCompletedAnimation;

    public TengenMsPacMan_PlayScene2D(GameContext gameContext) {
        super(gameContext);

        dynamicCamera.scalingProperty().bind(scalingProperty());

        // use own canvas, not shared canvas
        setCanvas(new Canvas());
        canvas.widthProperty() .bind(scalingProperty().multiply(UNSCALED_CANVAS_WIDTH));
        canvas.heightProperty().bind(scalingProperty().multiply(UNSCALED_CANVAS_HEIGHT));

        // The maps are only 28 tiles wide. To avoid drawing the actors outside the map when going through portals,
        // 2 tiles wide vertical stripes are clipped at the left and right map border.
        canvasClipRect.xProperty().bind(canvas.translateXProperty().add(scalingProperty().multiply(2 * TS)));
        canvasClipRect.yProperty().bind(canvas.translateYProperty());
        canvasClipRect.widthProperty().bind(canvas.widthProperty().subtract(scalingProperty().multiply(4 * TS)));
        canvasClipRect.heightProperty().bind(canvas.heightProperty());

        var root = new StackPane(canvas);
        root.setBackground(Background.EMPTY);

        fxSubScene = new SubScene(root, 88, 88); // size gets bound to parent scene size when embedded in game view
        fxSubScene.setFill(backgroundColor());
        fxSubScene.cameraProperty().bind(displayModeProperty()
            .map(displayMode -> displayMode == SceneDisplayMode.SCROLLING ? dynamicCamera : fixedCamera));
    }

    @Override
    public List<MenuItem> supplyContextMenuItems(ContextMenuEvent menuEvent, ContextMenu menu) {
        SceneDisplayMode displayMode = PY_TENGEN_PLAY_SCENE_DISPLAY_MODE.get();

        var miScaledToFit = new RadioMenuItem(theUI().theAssets().text("scaled_to_fit"));
        miScaledToFit.setSelected(displayMode == SceneDisplayMode.SCALED_TO_FIT);
        miScaledToFit.setOnAction(e -> PY_TENGEN_PLAY_SCENE_DISPLAY_MODE.set(SceneDisplayMode.SCALED_TO_FIT));

        var miScrolling = new RadioMenuItem(theUI().theAssets().text("scrolling"));
        miScrolling.setSelected(displayMode == SceneDisplayMode.SCROLLING);
        miScrolling.setOnAction(e -> PY_TENGEN_PLAY_SCENE_DISPLAY_MODE.set(SceneDisplayMode.SCROLLING));

        var radio = new ToggleGroup();
        miScaledToFit.setToggleGroup(radio);
        miScrolling.setToggleGroup(radio);
        PY_TENGEN_PLAY_SCENE_DISPLAY_MODE.addListener((py, ov, newMode) ->
            radio.selectToggle(newMode == SceneDisplayMode.SCROLLING ? miScrolling : miScaledToFit));

        var miAutopilot = new CheckMenuItem(theUI().theAssets().text("autopilot"));
        miAutopilot.selectedProperty().bindBidirectional(PY_USING_AUTOPILOT);

        var miImmunity = new CheckMenuItem(theUI().theAssets().text("immunity"));
        miImmunity.selectedProperty().bindBidirectional(PY_IMMUNITY);

        var miMuted = new CheckMenuItem(theUI().theAssets().text("muted"));
        miMuted.selectedProperty().bindBidirectional(theUI().mutedProperty());

        var miQuit = new MenuItem(theUI().theAssets().text("quit"));
        miQuit.setOnAction(e -> ACTION_QUIT_GAME_SCENE.executeIfEnabled(theUI(), gameContext));

        return List.of(
            miScaledToFit,
            miScrolling,
            menuTitleItem(theUI().theAssets().text("pacman")),
            miAutopilot,
            miImmunity,
            new SeparatorMenuItem(),
            miMuted,
            miQuit
        );
    }

    public ObjectProperty<SceneDisplayMode> displayModeProperty() {
        return displayModeProperty;
    }

    private void bindActionsToKeys() {
        if (gameContext.theGameLevel().isDemoLevel()) {
            actionBindings.bind(ACTION_QUIT_DEMO_LEVEL, TENGEN_MS_PACMAN_ACTION_BINDINGS);
        } else {
            actionBindings.bind(ACTION_STEER_UP, TENGEN_MS_PACMAN_ACTION_BINDINGS);
            actionBindings.bind(ACTION_STEER_DOWN, TENGEN_MS_PACMAN_ACTION_BINDINGS);
            actionBindings.bind(ACTION_STEER_LEFT, TENGEN_MS_PACMAN_ACTION_BINDINGS);
            actionBindings.bind(ACTION_STEER_RIGHT, TENGEN_MS_PACMAN_ACTION_BINDINGS);
            actionBindings.bind(ACTION_TOGGLE_DISPLAY_MODE, TENGEN_MS_PACMAN_ACTION_BINDINGS);
            actionBindings.bind(ACTION_TOGGLE_PAC_BOOSTER, TENGEN_MS_PACMAN_ACTION_BINDINGS);
            actionBindings.bind(ACTION_CHEAT_EAT_ALL_PELLETS, GLOBAL_ACTION_BINDINGS);
            actionBindings.bind(ACTION_CHEAT_ADD_LIVES, GLOBAL_ACTION_BINDINGS);
            actionBindings.bind(ACTION_CHEAT_ENTER_NEXT_LEVEL, GLOBAL_ACTION_BINDINGS);
            actionBindings.bind(ACTION_CHEAT_KILL_GHOSTS, GLOBAL_ACTION_BINDINGS);
        }
        actionBindings.update();
    }

    @Override
    public void doInit() {
        gameContext.theGame().hud().showScore(true);
        gameContext.theGame().hud().showLevelCounter(true);
        gameContext.theGame().hud().showLivesCounter(true);

        TengenMsPacMan_UIConfig config = (TengenMsPacMan_UIConfig) theUI().theUIConfiguration();
        setGameRenderer(config.createGameRenderer(canvas()));
        dynamicCamera.moveTop();
        messageMovement = new MessageMovement();
        levelCompletedAnimation = new LevelCompletedAnimation(animationManager);
    }

    @Override
    protected void doEnd() {
        if (levelCompletedAnimation != null) {
            animationManager.destroyAnimation(levelCompletedAnimation);
        }
    }

    @Override
    public void update() {
        gameContext.optGameLevel().ifPresent(level -> {
            if (level.isDemoLevel()) {
                theUI().theSound().setEnabled(false);
            } else {
                messageMovement.update();
                theUI().theSound().setEnabled(true);
                updateSound();
            }
            if (fxSubScene.getCamera() == dynamicCamera) {
                if (gameContext.theGameState() == GameState.HUNTING) {
                    dynamicCamera.setFocussingActor(true);
                }
                dynamicCamera.setVerticalRangeInTiles(level.worldMap().numRows());
                dynamicCamera.update(level.pac());
            }
            updateHUD();
        });
    }

    @Override
    public DoubleProperty viewPortWidthProperty() {
        return fxSubScene.widthProperty();
    }

    @Override
    public DoubleProperty viewPortHeightProperty() {
        return fxSubScene.heightProperty();
    }

    @Override
    public Node viewPort() {
        return fxSubScene;
    }

    @Override
    public Camera camera() {
        return fxSubScene.getCamera();
    }

    @Override
    public Vector2f sizeInPx() {
        return gameContext.optGameLevel().map(GameLevel::worldSizePx).orElse(NES_SIZE_PX);
    }

    @Override
    public void onGameStarted(GameEvent e) {
        boolean silent = gameContext.theGameLevel().isDemoLevel() || gameContext.theGameState() == TESTING_LEVELS_SHORT || gameContext.theGameState() == TESTING_LEVELS_MEDIUM;
        if (!silent) {
            theUI().theSound().play(SoundID.GAME_READY);
        }
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        gameContext.theGame().hud().showLevelCounter(true);
        gameContext.theGame().hud().showLivesCounter(true); // is also visible in demo level!

        bindActionsToKeys();
        TengenMsPacMan_UIConfig config = (TengenMsPacMan_UIConfig) theUI().theUIConfiguration();
        setGameRenderer(config.createGameRenderer(canvas()));
        gr().ensureRenderingHintsAreApplied(gameContext.theGameLevel());
    }

    @Override
    public void onSwitch_3D_2D(GameScene scene3D) {
        gameContext.theGame().hud().showLevelCounter(true);
        gameContext.theGame().hud().showLivesCounter(true); // is also visible in demo level!

        bindActionsToKeys();
        TengenMsPacMan_UIConfig config = (TengenMsPacMan_UIConfig) theUI().theUIConfiguration();
        setGameRenderer(config.createGameRenderer(canvas()));
        gr().ensureRenderingHintsAreApplied(gameContext.theGameLevel());
    }

    @Override
    public void onLevelStarted(GameEvent e) {
        dynamicCamera.setCameraToTopOfScene();
        dynamicCamera.moveBottom();
        dynamicCamera.setIdleTime(90);
    }

    @Override
    public void onEnterGameState(GameState state) {
        switch (state) {
            case HUNTING -> dynamicCamera.setFocussingActor(true);
            case LEVEL_COMPLETE -> {
                theUI().theSound().stopAll();
                levelCompletedAnimation.setGameLevel(gameContext.theGameLevel());
                levelCompletedAnimation.setSingleFlashMillis(333);
                levelCompletedAnimation.getOrCreateAnimation().setOnFinished(e -> gameContext.theGameController().letCurrentGameStateExpire());
                levelCompletedAnimation.playFromStart();
            }
            case GAME_OVER -> {
                var theGame = (TengenMsPacMan_GameModel) gameContext.theGame();
                if (theGame.mapCategory() != MapCategory.ARCADE) {
                    gameContext.theGameLevel().house().ifPresent(house -> {
                        float belowHouse = house.centerPositionUnderHouse().x();
                        messageMovement.start(MOVING_MESSAGE_DELAY, belowHouse, sizeInPx().x());
                    });
                }
                dynamicCamera.moveTop();
            }
            default -> {}
        }
    }

    @Override
    public void onBonusActivated(GameEvent e) {
        theUI().theSound().loop(SoundID.BONUS_ACTIVE);
    }

    @Override
    public void onBonusEaten(GameEvent e) {
        theUI().theSound().stop(SoundID.BONUS_ACTIVE);
        theUI().theSound().play(SoundID.BONUS_EATEN);
    }

    @Override
    public void onBonusExpired(GameEvent e) {
        theUI().theSound().stop(SoundID.BONUS_ACTIVE);
    }

    @Override
    public void onSpecialScoreReached(GameEvent e) {
        int score = e.payload("score");
        Logger.info("Extra life won for reaching score of {}", score);
        theUI().theSound().play(SoundID.EXTRA_LIFE);
    }

    @Override
    public void onGameContinued(GameEvent e) {
        gameContext.optGameLevel().ifPresent(level -> level.showMessage(GameLevel.MESSAGE_READY));
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        theUI().theSound().play(SoundID.GHOST_EATEN);
    }

    @Override
    public void onPacDead(GameEvent e) {
        dynamicCamera.moveTop();
        gameContext.theGameController().letCurrentGameStateExpire();
    }

    @Override
    public void onPacDying(GameEvent e) {
        theUI().theSound().play(SoundID.PAC_MAN_DEATH);
    }

    @Override
    public void onPacFoundFood(GameEvent e) {
        theUI().theSound().loop(SoundID.PAC_MAN_MUNCHING);
    }

    @Override
    public void onPacGetsPower(GameEvent e) {
        theUI().theSound().pauseSiren();
        theUI().theSound().loop(SoundID.PAC_MAN_POWER);
    }

    @Override
    public void onPacLostPower(GameEvent e) {
        theUI().theSound().stop(SoundID.PAC_MAN_POWER);
    }

    private void updateSound() {
        final Pac pac = gameContext.theGameLevel().pac();

        //TODO check in simulator when exactly which siren plays
        boolean pacChased = gameContext.theGameState() == GameState.HUNTING && !pac.powerTimer().isRunning();
        if (pacChased) {
            // siren numbers are 1..4, hunting phase index = 0..7
            int huntingPhase = gameContext.theGame().huntingTimer().phaseIndex();
            int sirenNumber = 1 + huntingPhase / 2;
            switch (sirenNumber) {
                case 1 -> theUI().theSound().playSiren(SoundID.SIREN_1, 1.0);
                case 2 -> theUI().theSound().playSiren(SoundID.SIREN_2, 1.0);
                case 3 -> theUI().theSound().playSiren(SoundID.SIREN_3, 1.0);
                case 4 -> theUI().theSound().playSiren(SoundID.SIREN_4, 1.0);
                default -> throw new IllegalArgumentException("Illegal siren number " + sirenNumber);
            }
        }

        // TODO: how exactly is the munching sound created in the original game?
        if (pac.starvingTicks() > 10) {
            theUI().theSound().pause(SoundID.PAC_MAN_MUNCHING);
        }

        //TODO check in simulator when exactly this sound is played
        var ghostReturningToHouse = gameContext.theGameLevel()
            .ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE)
            .findAny();
        if (ghostReturningToHouse.isPresent()
            && (gameContext.theGameState() == GameState.HUNTING || gameContext.theGameState() == GameState.GHOST_DYING)) {
            theUI().theSound().loop(SoundID.GHOST_RETURNS);
        } else {
            theUI().theSound().stop(SoundID.GHOST_RETURNS);
        }
    }

    // drawing

    @Override
    public TengenMsPacMan_GameRenderer gr() {
        return (TengenMsPacMan_GameRenderer) gameRenderer;
    }

    @Override
    public void draw() {
        fillCanvas(canvas, backgroundColor());
        if (gameContext.optGameLevel().isEmpty()) {
            return;
        }
        gr().ensureRenderingHintsAreApplied(gameContext.theGameLevel());

        // compute current scene scaling
        switch (displayModeProperty.get()) {
            case SCALED_TO_FIT -> { //TODO this code smells
                int tilesY = gameContext.theGameLevel().worldMap().numRows() + 3;
                double camY = scaled((tilesY - 46) * HTS);
                fixedCamera.setTranslateY(camY);
                setScaling(fxSubScene.getHeight() / (tilesY * TS));
            }
            case SCROLLING -> setScaling(fxSubScene.getHeight() / NES_SIZE_PX.y());
        }
        gr().setScaling(scaling());

        ctx().save();
        if (debugInfoVisibleProperty.get()) {
            canvas.setClip(null);
            drawSceneContent();
            drawDebugInfo();
        } else {
            canvas.setClip(canvasClipRect);
            drawSceneContent();
        }
        ctx().restore();

        ctx().save();
        // NES screen is 32 tiles wide but mazes are only 28 tiles wide
        ctx().translate(scaled(2 * TS), 0);
        gr().drawHUD(gameContext, gameContext.theGame().hud(), sizeInPx());
        ctx().restore();
    }

    @Override
    public void drawSceneContent() {
        ctx().save();
        centerOnScreen();

        if (levelCompletedAnimation.isRunning()) {
            if (levelCompletedAnimation.isHighlighted()) {
                // get the current flashing maze "animation frame"
                int frameIndex = levelCompletedAnimation.flashingIndex();
                ColorSchemedSprite flashingMazeSprite = gr().mazeConfig().flashingMazeSprites().get(frameIndex);
                gr().drawLevelWithMaze(gameContext, gameContext.theGameLevel(), flashingMazeSprite.image(), flashingMazeSprite.sprite());
            } else {
                gr().drawLevel(gameContext, gameContext.theGameLevel(), null, false, false);
            }
        }
        else {
            //TODO in the original game, the message is drawn under the maze image but *over* the pellets!
            gr().drawLevelMessage(gameContext.theGameLevel(), currentMessagePosition(), scaledArcadeFont8());
            gr().drawLevel(gameContext, gameContext.theGameLevel(), null, false, false);
        }

        var actorsByZ = new ArrayList<Actor>();
        gameContext.theGameLevel().bonus().map(Bonus::actor).ifPresent(actorsByZ::add);
        actorsByZ.add(gameContext.theGameLevel().pac());
        ghostsByZ().forEach(actorsByZ::add);

        actorsByZ.forEach(gr()::drawActor);

        ctx().restore();
    }

    @Override
    protected void drawDebugInfo() {
        ctx().save();
        gr().drawTileGrid(UNSCALED_CANVAS_WIDTH, UNSCALED_CANVAS_HEIGHT, Color.LIGHTGRAY);
        if (gameContext.optGameLevel().isPresent()) {
            centerOnScreen();
            ctx().setFill(DEBUG_TEXT_FILL);
            ctx().setFont(DEBUG_TEXT_FONT);
            ctx().fillText("%s %d".formatted(gameContext.theGameState(), gameContext.theGameState().timer().tickCount()), 0, scaled(3 * TS));
            gr().drawMovingActorInfo(gameContext.theGameLevel().pac());
            ghostsByZ().forEach(gr()::drawMovingActorInfo);
        }
        ctx().restore();
    }

    // NES screen is 32 tiles wide but mazes are only 28 tiles wide, so indent by 2 tiles to center on available width
    private void centerOnScreen() {
        ctx().translate(scaled(2 * TS), 0);
    }

    private Stream<Ghost> ghostsByZ() {
        return Stream.of(ORANGE_GHOST_POKEY, CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, RED_GHOST_SHADOW).map(gameContext.theGameLevel()::ghost);
    }

    private Vector2f currentMessagePosition() {
        House house = gameContext.theGameLevel().house().orElse(null);
        if (house == null) {
            Logger.error("No house in game level!");
            return Vector2f.ZERO; //TODO
        }
        Vector2f center = house.centerPositionUnderHouse();
        return messageMovement != null && messageMovement.isRunning()
            ? new Vector2f(messageMovement.currentX(), center.y())
            : center;
    }

    private void updateHUD() {
        TengenMsPacMan_GameModel theGame = (TengenMsPacMan_GameModel) gameContext.theGame();
        TengenMsPacMan_HUD hud = theGame.hud();

        TengenMsPacMan_LevelCounter levelCounter = hud.levelCounter();
        //TODO check demo level behavior in emulator. Are there demo levels for non-ARCADE maps at all?
        if (theGame.mapCategory() == MapCategory.ARCADE || gameContext.optGameLevel().isEmpty() || gameContext.theGameLevel().isDemoLevel()) {
            levelCounter.setDisplayedLevelNumber(0); // no level number boxes for ARCADE maps or when level not yet created
        } else {
            levelCounter.setDisplayedLevelNumber(gameContext.theGameLevel().number());
        }

        LivesCounter livesCounter = hud.livesCounter();
        int numLivesDisplayed = gameContext.theGame().lifeCount() - 1;
        // As long as Pac-Man is still initially hidden in the maze, he is shown as an entry in the lives counter
        if (gameContext.theGameState() == GameState.STARTING_GAME && !gameContext.theGameLevel().pac().isVisible()) {
            numLivesDisplayed += 1;
        }
        livesCounter.setVisibleLifeCount(Math.min(numLivesDisplayed, livesCounter.maxLivesDisplayed()));
    }
}