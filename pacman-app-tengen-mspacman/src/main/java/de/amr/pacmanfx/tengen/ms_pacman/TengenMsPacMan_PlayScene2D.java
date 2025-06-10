/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.ui.PacManGames_Actions;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.LevelFinishedAnimation;
import de.amr.pacmanfx.uilib.CameraControlledView;
import de.amr.pacmanfx.uilib.GameScene;
import de.amr.pacmanfx.uilib.Ufx;
import javafx.beans.property.*;
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
import static de.amr.pacmanfx.controller.GameState.TESTING_LEVELS;
import static de.amr.pacmanfx.controller.GameState.TESTING_LEVEL_TEASERS;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_ActionBindings.TENGEN_DEFAULT_ACTION_BINDINGS;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_GameActions.*;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_SpriteSheet.sprite;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.*;
import static de.amr.pacmanfx.ui.PacManGames_Actions.*;
import static de.amr.pacmanfx.ui.PacManGames_Env.*;
import static de.amr.pacmanfx.ui.PacManGames_UI.*;

/**
 * Tengen play scene, uses vertical scrolling.
 */
public class TengenMsPacMan_PlayScene2D extends GameScene2D implements CameraControlledView {

    // NES screen width (32 tiles), BIG map height (42 tiles) + 2 extra tile rows
    private static final Vector2i UNSCALED_CANVAS_SIZE = Vector2i.of(NES_SIZE.x(), 44 * TS);
    private static final int MOVING_MESSAGE_DELAY = 120;

    private final SubScene fxSubScene;
    private final DynamicCamera dynamicCamera = new DynamicCamera();
    private final ParallelCamera fixedCamera  = new ParallelCamera();
    private final ObjectProperty<SceneDisplayMode> displayModePy = new SimpleObjectProperty<>(SceneDisplayMode.SCROLLING);

    private MessageMovement messageMovement;
    private LevelFinishedAnimation levelFinishedAnimation;

    public TengenMsPacMan_PlayScene2D() {
        dynamicCamera.scalingProperty().bind(scalingProperty());

        setCanvas(new Canvas());
        canvas().widthProperty().bind(scalingProperty().multiply(UNSCALED_CANVAS_SIZE.x()));
        canvas().heightProperty().bind(scalingProperty().multiply(UNSCALED_CANVAS_SIZE.y()));

        // The mazes are smaller than the canvas which is as wide as the NES screen.
        // When Pac-Man runs through a portal, it gets drawn outside the maze bounds.
        // To avoid that, clip 2 tiles wide vertical stripes at the left and right maze border.
        var mazeClip = new Rectangle();
        int stripeWidth = 2 * TS;
        mazeClip.xProperty().bind(canvas().translateXProperty().add(scalingProperty().multiply(stripeWidth)));
        mazeClip.yProperty().bind(canvas().translateYProperty());
        mazeClip.widthProperty().bind(canvas().widthProperty().subtract(scalingProperty().multiply(2 * stripeWidth)));
        mazeClip.heightProperty().bind(canvas().heightProperty());
        canvas().setClip(mazeClip);

        var root = new StackPane(canvas());
        root.setBackground(Background.EMPTY);

        fxSubScene = new SubScene(root, 0, 0); // size is bound to parent scene size when embedded in game view
        fxSubScene.setFill(PY_CANVAS_BG_COLOR.get());

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
        miQuit.setOnAction(ae -> PacManGames_Actions.ACTION_QUIT_GAME_SCENE.execute());
        items.add(miQuit);

        return items;
    }

    public ObjectProperty<SceneDisplayMode> displayModeProperty() {
        return displayModePy;
    }

    private void bindActionsToKeys() {
        if (theGameLevel().isDemoLevel()) {
            bindAction(ACTION_QUIT_DEMO_LEVEL, TENGEN_DEFAULT_ACTION_BINDINGS);
        } else {
            bindAction(ACTION_PLAYER_UP, TENGEN_DEFAULT_ACTION_BINDINGS);
            bindAction(ACTION_PLAYER_DOWN, TENGEN_DEFAULT_ACTION_BINDINGS);
            bindAction(ACTION_PLAYER_LEFT, TENGEN_DEFAULT_ACTION_BINDINGS);
            bindAction(ACTION_PLAYER_RIGHT, TENGEN_DEFAULT_ACTION_BINDINGS);
            bindAction(ACTION_TOGGLE_DISPLAY_MODE, TENGEN_DEFAULT_ACTION_BINDINGS);
            bindAction(ACTION_TOGGLE_PAC_BOOSTER, TENGEN_DEFAULT_ACTION_BINDINGS);
            bindAction(ACTION_CHEAT_EAT_ALL_PELLETS);
            bindAction(ACTION_CHEAT_ADD_LIVES);
            bindAction(ACTION_CHEAT_ENTER_NEXT_LEVEL);
            bindAction(ACTION_CHEAT_KILL_GHOSTS);
        }
        updateActionBindings();
    }

    @Override
    public void doInit() {
        theGame().setScoreVisible(true);
        setGameRenderer(theUI().configuration().createRenderer(canvas()));
        dynamicCamera.moveTop();
        messageMovement = new MessageMovement();
    }

    @Override
    protected void doEnd() {
        theSound().stopAll();
    }

    @Override
    public void update() {
        optGameLevel().ifPresent(level -> {
            if (!level.isDemoLevel()) {
                messageMovement.update();
                updateSound(level);
            }
            if (fxSubScene.getCamera() == dynamicCamera) {
                if (theGameState() == GameState.HUNTING) {
                    dynamicCamera.setFocussingActor(true);
                }
                dynamicCamera.setVerticalRangeInTiles(level.worldMap().numRows());
                dynamicCamera.update(level.pac());
            }
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
        if (optGameLevel().isPresent()) {
            int numRows = theGameLevel().worldMap().numRows();
            int numCols = theGameLevel().worldMap().numCols();
            return new Vector2f(numCols * TS, numRows * TS);
        }
        return NES_SIZE.toVector2f();
    }

    @Override
    public void onGameStarted(GameEvent e) {
        boolean silent = theGameLevel().isDemoLevel() || theGameState() == TESTING_LEVELS || theGameState() == TESTING_LEVEL_TEASERS;
        if (!silent) {
            theSound().playGameReadySound();
        }
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        bindActionsToKeys();
        gr().applyRenderingHints(theGameLevel());
    }

    @Override
    public void onLevelStarted(GameEvent e) {
        dynamicCamera.setCameraToTopOfScene();
        dynamicCamera.moveBottom();
        dynamicCamera.setIdleTime(90);
    }

    @Override
    public void onSwitch_3D_2D(GameScene scene3D) {
        bindActionsToKeys();
        gr().applyRenderingHints(theGameLevel());
    }

    @Override
    public void onEnterGameState(GameState state) {
        switch (state) {
            case HUNTING -> dynamicCamera.setFocussingActor(true);
            case LEVEL_COMPLETE -> {
                theSound().stopAll();
                levelFinishedAnimation = new LevelFinishedAnimation(theGameLevel(), 333);
                levelFinishedAnimation.whenFinished(theGameController()::letCurrentGameStateExpire);
                levelFinishedAnimation.play();
            }
            case GAME_OVER -> {
                var tengenGame = (TengenMsPacMan_GameModel) theGame();
                if (tengenGame.mapCategory() != MapCategory.ARCADE) {
                    float belowHouse = centerPosBelowHouse().x();
                    messageMovement.start(MOVING_MESSAGE_DELAY, belowHouse, sizeInPx().x());
                }
                dynamicCamera.moveTop();
            }
            default -> {}
        }
    }

    @Override
    public void onBonusActivated(GameEvent e) {
        theSound().playBonusActiveSound();
    }

    @Override
    public void onBonusEaten(GameEvent e) {
        theSound().stopBonusActiveSound();
        theSound().playBonusEatenSound();
    }

    @Override
    public void onBonusExpired(GameEvent e) {
        theSound().stopBonusActiveSound();
    }

    @Override
    public void onSpecialScoreReached(GameEvent e) {
        int score = e.payload("score");
        Logger.info("Extra life won for reaching score of {}", score);
        theSound().playExtraLifeSound();
    }

    @Override
    public void onGameContinued(GameEvent e) {
        optGameLevel().ifPresent(level -> level.showMessage(GameLevel.MESSAGE_READY));
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        theSound().playGhostEatenSound();
    }

    @Override
    public void onPacDead(GameEvent e) {
        dynamicCamera.moveTop();
        theGameController().letCurrentGameStateExpire();
    }

    @Override
    public void onPacDying(GameEvent e) {
        theSound().playPacDeathSound();
    }

    @Override
    public void onPacFoundFood(GameEvent e) {
        theSound().playMunchingSound();
    }

    @Override
    public void onPacGetsPower(GameEvent e) {
        theSound().stopSiren();
        theSound().playPacPowerSound();
    }

    @Override
    public void onPacLostPower(GameEvent e) {
        theSound().stopPacPowerSound();
    }

    private void updateSound(GameLevel level) {
        if (theGameState() == GameState.HUNTING && !level.pac().powerTimer().isRunning()) {
            int sirenNumber = 1 + theGame().huntingTimer().phaseIndex() / 2; // TODO check how this works in original game
            theSound().selectSiren(sirenNumber);
            theSound().playSiren();
        }
        if (level.pac().starvingTicks() > 5) { // TODO not sure how to do this right
            theSound().stopMunchingSound();
        }
        boolean ghostsReturning = level.ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).anyMatch(Ghost::isVisible);
        if (level.pac().isAlive() && ghostsReturning) {
            theSound().playGhostReturningHomeSound();
        } else {
            theSound().stopGhostReturningHomeSound();
        }
    }

    // drawing

    @Override
    public void draw() {
        gr().fillCanvas(backgroundColor());
        if (optGameLevel().isEmpty()) {
            return;
        }
        if (displayModeProperty().get() == SceneDisplayMode.SCROLLING) {
            setScaling(fxSubScene.getHeight() / NES_SIZE.y());
        } else {
            //TODO this code smells
            int tilesY = theGameLevel().worldMap().numRows() + 3;
            setScaling(fxSubScene.getHeight() / (tilesY * TS));
            double camY = scaled((tilesY - 43) * HTS);
            fixedCamera.setTranslateY(camY);
        }
        gr().setScaling(scaling());
        if (theGame().isScoreVisible()) {
            gr().drawScores(theGame(), scoreColor(), normalArcadeFont());
        }
        drawSceneContent();
        if (debugInfoVisiblePy.get()) {
            drawDebugInfo();
        }
    }

    @Override
    protected void drawSceneContent() {
        final var game = (TengenMsPacMan_GameModel) theGame();
        final var r = (TengenMsPacMan_Renderer2D) gr();
        r.ensureMapSettingsApplied(theGameLevel()); //TODO check this workaround

        r.ctx().save();
        // NES screen width is 32 tiles but mazes are only 28 tiles wide
        double margin = scaled((NES_TILES.x() - theGameLevel().worldMap().numCols()) * HTS);
        r.ctx().translate(margin, 0);

        final int mazeTopY = 3 * TS;
        final boolean flashing = levelFinishedAnimation != null && levelFinishedAnimation.isRunning();
        if (flashing) {
            if (levelFinishedAnimation.isHighlighted()) {
                r.drawHighlightedWorld(theGameLevel(), 0, mazeTopY, levelFinishedAnimation.getFlashingIndex());
            } else {
                r.drawLevel(theGameLevel(), 0, mazeTopY, null, false, false);
                r.drawFood(theGameLevel()); // this also hides the eaten food!
            }
        }
        else {
            r.drawLevel(theGameLevel(), 0, mazeTopY, null, false, false);
            r.drawFood(theGameLevel());
            theGameLevel().bonus().ifPresent(r::drawBonus);
            //TODO in the original game, the message is drawn under the maze image but *over* the pellets!
            r.drawLevelMessage(theGameLevel(), currentMessagePosition(), normalArcadeFont());
        }
        r.drawActor(theGameLevel().pac());
        ghostsInZOrder().forEach(r::drawActor);

        // As long as Pac-Man is still invisible on game start, one live more is shown in the counter
        int numLivesDisplayed = theGameState() == GameState.STARTING_GAME && !theGameLevel().pac().isVisible()
            ? game.lifeCount() : game.lifeCount() - 1;
        r.drawLivesCounter(numLivesDisplayed, LIVES_COUNTER_MAX, 2 * TS, sizeInPx().y() - TS, sprite(SpriteID.LIVES_COUNTER_SYMBOL));

        if (theGameLevel().isDemoLevel() || game.mapCategory() == MapCategory.ARCADE) {
            r.drawLevelCounter(game.levelCounter(), sizeInPx());
        } else {
            r.drawLevelCounterWithLevelNumbers(theGameLevel().number(), game.levelCounter(), sizeInPx());
        }

        r.ctx().restore();
    }

    @Override
    protected void drawDebugInfo() {
        gr().ctx().save();
        if (optGameLevel().isPresent()) {
            // NES screen width is 32 tiles but mazes are only 28 tiles wide
            double margin = scaled((NES_TILES.x() - theGameLevel().worldMap().numCols()) * HTS);
            gr().ctx().translate(margin, 0);
            gr().drawTileGrid(UNSCALED_CANVAS_SIZE.x(), UNSCALED_CANVAS_SIZE.y(), Color.LIGHTGRAY);
            gr().ctx().setFill(Color.YELLOW);
            gr().ctx().setFont(DEBUG_TEXT_FONT);
            gr().ctx().fillText("%s %d".formatted(theGameState(), theGameState().timer().tickCount()), 0, scaled(3 * TS));
            gr().drawAnimatedActorInfo(theGameLevel().pac());
            ghostsInZOrder().forEach(gr()::drawAnimatedActorInfo);
        }
        gr().ctx().restore();
    }

    private Stream<Ghost> ghostsInZOrder() {
        return Stream.of(ORANGE_GHOST_POKEY, CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, RED_GHOST_SHADOW).map(theGameLevel()::ghost);
    }

    private Vector2f currentMessagePosition() {
        Vector2f center = centerPosBelowHouse();
        return messageMovement != null && messageMovement.isRunning()
            ? new Vector2f(messageMovement.currentX(), center.y())
            : center;
    }

    private Vector2f centerPosBelowHouse() {
        return theGameLevel().houseMinTile()
                .plus(0.5f * theGameLevel().houseSizeInTiles().x(), theGameLevel().houseSizeInTiles().y() + 1)
                .scaled(TS);
    }
}