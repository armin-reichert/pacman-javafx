/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.House;
import de.amr.pacmanfx.model.LivesCounter;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengen.ms_pacman.model.MapCategory;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_HUD;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_LevelCounter;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.ColorSchemedSprite;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_GameRenderer;
import de.amr.pacmanfx.ui.ActionBindingSupport;
import de.amr.pacmanfx.ui.GameAction;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.CameraControlledView;
import de.amr.pacmanfx.uilib.Ufx;
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
import static de.amr.pacmanfx.ui.PacManGames.*;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.*;
import static de.amr.pacmanfx.ui.PacManGames_UI.*;

/**
 * Tengen play scene, uses vertical scrolling.
 */
public class TengenMsPacMan_PlayScene2D extends GameScene2D implements ActionBindingSupport, CameraControlledView {

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

    public TengenMsPacMan_PlayScene2D() {
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
    public List<MenuItem> supplyContextMenuItems(ContextMenuEvent e) {
        List<MenuItem> items = new ArrayList<>();
        // Switching scene display mode
        var miScaledToFit = new RadioMenuItem(theAssets().text("scaled_to_fit"));
        miScaledToFit.selectedProperty().addListener(
            (py,ov,nv) -> PY_TENGEN_PLAY_SCENE_DISPLAY_MODE.set(nv? SceneDisplayMode.SCALED_TO_FIT:SceneDisplayMode.SCROLLING));
        PY_TENGEN_PLAY_SCENE_DISPLAY_MODE.addListener((py, ov, nv) -> miScaledToFit.setSelected(nv == SceneDisplayMode.SCALED_TO_FIT));
        items.add(miScaledToFit);

        var miScrolling = new RadioMenuItem(theAssets().text("scrolling"));
        miScrolling.selectedProperty().addListener(
            (py,ov,nv) -> PY_TENGEN_PLAY_SCENE_DISPLAY_MODE.set(nv? SceneDisplayMode.SCROLLING:SceneDisplayMode.SCALED_TO_FIT));
        PY_TENGEN_PLAY_SCENE_DISPLAY_MODE.addListener((py, ov, nv) -> miScrolling.setSelected(nv == SceneDisplayMode.SCROLLING));
        items.add(miScrolling);

        ToggleGroup exclusion = new ToggleGroup();
        miScaledToFit.setToggleGroup(exclusion);
        miScrolling.setToggleGroup(exclusion);
        if (PY_TENGEN_PLAY_SCENE_DISPLAY_MODE.get() == SceneDisplayMode.SCALED_TO_FIT) {
            miScaledToFit.setSelected(true);
        } else {
            miScrolling.setSelected(true);
        }
        items.add(Ufx.contextMenuTitleItem(theAssets().text("pacman")));

        var miAutopilot = new CheckMenuItem(theAssets().text("autopilot"));
        miAutopilot.selectedProperty().bindBidirectional(PY_USING_AUTOPILOT);
        items.add(miAutopilot);

        var miImmunity = new CheckMenuItem(theAssets().text("immunity"));
        miImmunity.selectedProperty().bindBidirectional(PY_IMMUNITY);
        items.add(miImmunity);

        items.add(new SeparatorMenuItem());

        var miMuted = new CheckMenuItem(theAssets().text("muted"));
        miMuted.selectedProperty().bindBidirectional(theSound().mutedProperty());
        items.add(miMuted);

        var miQuit = new MenuItem(theAssets().text("quit"));
        miQuit.setOnAction(ae -> GameAction.executeIfEnabled(theUI(), ACTION_QUIT_GAME_SCENE));
        items.add(miQuit);

        return items;
    }

    public ObjectProperty<SceneDisplayMode> displayModeProperty() {
        return displayModeProperty;
    }

    private void bindActionsToKeys() {
        if (theGameLevel().isDemoLevel()) {
            bindAction(ACTION_QUIT_DEMO_LEVEL, TENGEN_ACTION_BINDINGS);
        } else {
            bindAction(ACTION_STEER_UP,               TENGEN_ACTION_BINDINGS);
            bindAction(ACTION_STEER_DOWN,             TENGEN_ACTION_BINDINGS);
            bindAction(ACTION_STEER_LEFT,             TENGEN_ACTION_BINDINGS);
            bindAction(ACTION_STEER_RIGHT,            TENGEN_ACTION_BINDINGS);
            bindAction(ACTION_TOGGLE_DISPLAY_MODE,    TENGEN_ACTION_BINDINGS);
            bindAction(ACTION_TOGGLE_PAC_BOOSTER,     TENGEN_ACTION_BINDINGS);
            bindAction(ACTION_CHEAT_EAT_ALL_PELLETS, GLOBAL_ACTION_BINDINGS);
            bindAction(ACTION_CHEAT_ADD_LIVES, GLOBAL_ACTION_BINDINGS);
            bindAction(ACTION_CHEAT_ENTER_NEXT_LEVEL, GLOBAL_ACTION_BINDINGS);
            bindAction(ACTION_CHEAT_KILL_GHOSTS, GLOBAL_ACTION_BINDINGS);
        }
        updateActionBindings();
    }

    @Override
    public void doInit() {
        theGame().hud().showScore(true);
        theGame().hud().showLevelCounter(true);
        theGame().hud().showLivesCounter(true);

        TengenMsPacMan_UIConfig config = (TengenMsPacMan_UIConfig) theUI().configuration();
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
        optGameLevel().ifPresent(level -> {
            if (level.isDemoLevel()) {
                theSound().setEnabled(false);
            } else {
                messageMovement.update();
                theSound().setEnabled(true);
                updateSound(level);
            }
            if (fxSubScene.getCamera() == dynamicCamera) {
                if (theGameState() == GameState.HUNTING) {
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
        return optGameLevel().map(GameLevel::worldSizePx).orElse(NES_SIZE_PX);
    }

    @Override
    public void onGameStarted(GameEvent e) {
        boolean silent = theGameLevel().isDemoLevel() || theGameState() == TESTING_LEVELS_SHORT || theGameState() == TESTING_LEVELS_MEDIUM;
        if (!silent) {
            theSound().play(SoundID.GAME_READY);
        }
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        theGame().hud().showLevelCounter(true);
        theGame().hud().showLivesCounter(true); // is also visible in demo level!

        bindActionsToKeys();
        TengenMsPacMan_UIConfig config = (TengenMsPacMan_UIConfig) theUI().configuration();
        setGameRenderer(config.createGameRenderer(canvas()));
        gr().ensureRenderingHintsAreApplied(theGameLevel());
    }

    @Override
    public void onSwitch_3D_2D(GameScene scene3D) {
        theGame().hud().showLevelCounter(true);
        theGame().hud().showLivesCounter(true); // is also visible in demo level!

        bindActionsToKeys();
        TengenMsPacMan_UIConfig config = (TengenMsPacMan_UIConfig) theUI().configuration();
        setGameRenderer(config.createGameRenderer(canvas()));
        gr().ensureRenderingHintsAreApplied(theGameLevel());
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
                theSound().stopAll();
                levelCompletedAnimation.setGameLevel(theGameLevel());
                levelCompletedAnimation.setSingleFlashMillis(333);
                levelCompletedAnimation.getOrCreateAnimation().setOnFinished(e -> theGameController().letCurrentGameStateExpire());
                levelCompletedAnimation.playFromStart();
            }
            case GAME_OVER -> {
                var theGame = (TengenMsPacMan_GameModel) theGame();
                if (theGame.mapCategory() != MapCategory.ARCADE) {
                    theGameLevel().house().ifPresent(house -> {
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
        theSound().play(SoundID.BONUS_BOUNCING);
    }

    @Override
    public void onBonusEaten(GameEvent e) {
        theSound().stop(SoundID.BONUS_BOUNCING);
        theSound().play(SoundID.BONUS_EATEN);
    }

    @Override
    public void onBonusExpired(GameEvent e) {
        theSound().stop(SoundID.BONUS_BOUNCING);
    }

    @Override
    public void onSpecialScoreReached(GameEvent e) {
        int score = e.payload("score");
        Logger.info("Extra life won for reaching score of {}", score);
        theSound().play(SoundID.EXTRA_LIFE);
    }

    @Override
    public void onGameContinued(GameEvent e) {
        optGameLevel().ifPresent(level -> level.showMessage(GameLevel.MESSAGE_READY));
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        theSound().play(SoundID.GHOST_EATEN);
    }

    @Override
    public void onPacDead(GameEvent e) {
        dynamicCamera.moveTop();
        theGameController().letCurrentGameStateExpire();
    }

    @Override
    public void onPacDying(GameEvent e) {
        theSound().play(SoundID.PAC_MAN_DEATH);
    }

    @Override
    public void onPacFoundFood(GameEvent e) {
        theSound().play(SoundID.PAC_MAN_MUNCHING);
    }

    @Override
    public void onPacGetsPower(GameEvent e) {
        theSound().pauseSiren();
        theSound().play(SoundID.PAC_MAN_POWER);
    }

    @Override
    public void onPacLostPower(GameEvent e) {
        theSound().stop(SoundID.PAC_MAN_POWER);
    }

    private void updateSound(GameLevel level) {
        if (theGameState() == GameState.HUNTING && !level.pac().powerTimer().isRunning()) {
            int sirenNumber = 1 + theGame().huntingTimer().phaseIndex() / 2; // TODO check how this works in original game
            theSound().selectSiren(sirenNumber);
            theSound().playSiren();
        }
        if (level.pac().starvingTicks() > 10) { // TODO not sure how to do this right
            theSound().pause(SoundID.PAC_MAN_MUNCHING);
        }
        boolean ghostsReturning = level.ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).anyMatch(Ghost::isVisible);
        if (level.pac().isAlive() && ghostsReturning) {
            theSound().play(SoundID.GHOST_RETURNS);
        } else {
            theSound().stop(SoundID.GHOST_RETURNS);
        }
    }

    // drawing

    @Override
    public TengenMsPacMan_GameRenderer gr() {
        return (TengenMsPacMan_GameRenderer) gameRenderer;
    }

    @Override
    public void draw() {
        gr().fillCanvas(backgroundColor());
        if (optGameLevel().isEmpty()) {
            return;
        }

        // game level exists from here
        gr().ensureRenderingHintsAreApplied(theGameLevel());

        // compute current scene scaling
        switch (displayModeProperty.get()) {
            case SCALED_TO_FIT -> { //TODO this code smells
                int tilesY = theGameLevel().worldMap().numRows() + 3;
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
        gr().drawHUD(theGame().hud());
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
                gr().drawLevelWithMaze(theGameLevel(), flashingMazeSprite.image(), flashingMazeSprite.sprite());
            } else {
                gr().drawLevel(theGameLevel(), null, false, false);
            }
        }
        else {
            //TODO in the original game, the message is drawn under the maze image but *over* the pellets!
            gr().drawLevelMessage(theGameLevel(), currentMessagePosition(), scaledArcadeFont8());
            gr().drawLevel(theGameLevel(), null, false, false);
        }

        var actorsByZ = new ArrayList<Actor>();
        theGameLevel().bonus().map(Bonus::actor).ifPresent(actorsByZ::add);
        actorsByZ.add(theGameLevel().pac());
        ghostsByZ().forEach(actorsByZ::add);

        actorsByZ.forEach(gr()::drawActor);

        ctx().restore();
    }

    @Override
    protected void drawDebugInfo() {
        ctx().save();
        gr().drawTileGrid(UNSCALED_CANVAS_WIDTH, UNSCALED_CANVAS_HEIGHT, Color.LIGHTGRAY);
        if (optGameLevel().isPresent()) {
            centerOnScreen();
            ctx().setFill(DEBUG_TEXT_FILL);
            ctx().setFont(DEBUG_TEXT_FONT);
            ctx().fillText("%s %d".formatted(theGameState(), theGameState().timer().tickCount()), 0, scaled(3 * TS));
            gr().drawMovingActorInfo(theGameLevel().pac());
            ghostsByZ().forEach(gr()::drawMovingActorInfo);
        }
        ctx().restore();
    }

    // NES screen is 32 tiles wide but mazes are only 28 tiles wide, so indent by 2 tiles to center on available width
    private void centerOnScreen() {
        ctx().translate(scaled(2 * TS), 0);
    }

    private Stream<Ghost> ghostsByZ() {
        return Stream.of(ORANGE_GHOST_POKEY, CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, RED_GHOST_SHADOW).map(theGameLevel()::ghost);
    }

    private Vector2f currentMessagePosition() {
        House house = theGameLevel().house().orElse(null);
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
        TengenMsPacMan_GameModel theGame = (TengenMsPacMan_GameModel) theGame();
        TengenMsPacMan_HUD hud = theGame.hud();

        TengenMsPacMan_LevelCounter levelCounter = hud.levelCounter();
        //TODO check demo level behavior in emulator. Are there demo levels for non-ARCADE maps at all?
        if (theGame.mapCategory() == MapCategory.ARCADE || optGameLevel().isEmpty() || theGameLevel().isDemoLevel()) {
            levelCounter.setDisplayedLevelNumber(0); // no level number boxes for ARCADE maps or when level not yet created
        } else {
            levelCounter.setDisplayedLevelNumber(theGameLevel().number());
        }

        LivesCounter livesCounter = hud.livesCounter();
        int numLivesDisplayed = theGame().lifeCount() - 1;
        // As long as Pac-Man is still initially hidden in the maze, he is shown as an entry in the lives counter
        if (theGameState() == GameState.STARTING_GAME && !theGameLevel().pac().isVisible()) {
            numLivesDisplayed += 1;
        }
        livesCounter.setVisibleLifeCount(Math.min(numLivesDisplayed, livesCounter.maxLivesDisplayed()));
    }
}