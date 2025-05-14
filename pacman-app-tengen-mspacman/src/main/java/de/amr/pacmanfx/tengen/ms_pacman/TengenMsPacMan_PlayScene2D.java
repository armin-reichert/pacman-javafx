/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.GameAction;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui._2d.FlashingMazeAnimation;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.uilib.CameraControlledView;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.input.Keyboard;
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
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.controller.GameState.TESTING_LEVELS;
import static de.amr.pacmanfx.controller.GameState.TESTING_LEVEL_TEASERS;
import static de.amr.pacmanfx.lib.UsefulFunctions.lerp;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_GameAction.QUIT_DEMO_LEVEL;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.*;
import static de.amr.pacmanfx.ui.PacManGamesEnv.*;
import static de.amr.pacmanfx.uilib.input.Keyboard.control;

/**
 * Tengen play scene, uses vertical scrolling.
 *
 * @author Armin Reichert
 */
public class TengenMsPacMan_PlayScene2D extends GameScene2D implements CameraControlledView {

    // (NES screen width, BIG map height (42 tiles) + 2 extra tile rows)
    private static final Vector2i UNSCALED_CANVAS_SIZE = Vector2i.of(NES_SIZE.x(), 44 * TS);
    private static final float CAM_SPEED = 0.03f;
    private static final int MOVING_MESSAGE_DELAY = 120;

    private class MovingCamera extends ParallelCamera {
        private int idleTicks;
        private int verticalRangeTiles;
        private double targetY;
        private boolean focusPlayer;

        public void setIdleTicks(int ticks) {
            idleTicks = ticks;
        }

        public void setVerticalRangeTiles(int numTiles) {
            verticalRangeTiles = numTiles;
        }

        public void setCameraToTopOfScene() {
            setTranslateY(camMinY());
        }

        public void focusTopOfScene() {
            targetY = camMinY();
            focusPlayer = false;
        }

        public void focusBottomOfScene() {
            targetY = camMaxY();
            focusPlayer = false;
        }

        public double camMinY() {
            return scalingPy.get() * (-9 * TS);
        }

        public double camMaxY() {
            return scalingPy.get() * (verticalRangeTiles - 35) * TS;
        }

        public void focusPlayer(boolean focus) {
            focusPlayer = focus;
        }

        public void update(Pac pac) {
            if (idleTicks > 0) {
                --idleTicks;
                return;
            }
            if (focusPlayer) {
                double frac = (double) pac.tile().y() / verticalRangeTiles;
                if (frac < 0.4) { frac = 0; } else if (frac > 0.6) { frac = 1.0; }
                targetY = lerp(camMinY(), camMaxY(), frac);
            }
            double y = lerp(getTranslateY(), targetY, CAM_SPEED);
            setTranslateY(Math.clamp(y, camMinY(), camMaxY()));
            Logger.debug("Camera: y={0.00} target={} top={} bottom={}", getTranslateY(), targetY, camMinY(), camMaxY());
        }
    }

    private final SubScene fxSubScene;
    private final MovingCamera movingCamera;
    private final ParallelCamera fixedCamera;
    private final ObjectProperty<SceneDisplayMode> displayModePy = new SimpleObjectProperty<>(SceneDisplayMode.SCROLLING);

    private MessageMovement messageMovement;
    private FlashingMazeAnimation levelCompleteAnimation;

    public TengenMsPacMan_PlayScene2D() {
        movingCamera = new MovingCamera();
        fixedCamera = new ParallelCamera();

        canvas = new Canvas();
        canvas.widthProperty().bind(scalingProperty().multiply(UNSCALED_CANVAS_SIZE.x()));
        canvas.heightProperty().bind(scalingProperty().multiply(UNSCALED_CANVAS_SIZE.y()));

        // maze is drawn centered inside canvas: clip left and right vertical stripes (2 tiles wide each)
        var clip = new Rectangle();
        int stripeWidth = 2 * TS;
        clip.xProperty().bind(canvas.translateXProperty().add(scalingProperty().multiply(stripeWidth)));
        clip.yProperty().bind(canvas.translateYProperty());
        clip.widthProperty().bind(canvas.widthProperty().subtract(scalingPy.multiply(2 * stripeWidth)));
        clip.heightProperty().bind(canvas.heightProperty());
        canvas.setClip(clip);

        var root = new StackPane(canvas);
        root.setBackground(Background.EMPTY);

        fxSubScene = new SubScene(root, 42, 42);
        fxSubScene.setFill(PY_CANVAS_BG_COLOR.get());
        fxSubScene.cameraProperty().bind(displayModeProperty()
            .map(mode -> mode == SceneDisplayMode.SCROLLING ? movingCamera : fixedCamera));
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
        miAutopilot.selectedProperty().bindBidirectional(PY_AUTOPILOT);
        items.add(miAutopilot);

        var miImmunity = new CheckMenuItem(theAssets().text("immunity"));
        miImmunity.selectedProperty().bindBidirectional(PY_IMMUNITY);
        items.add(miImmunity);

        items.add(new SeparatorMenuItem());

        var miMuted = new CheckMenuItem(theAssets().text("muted"));
        miMuted.selectedProperty().bindBidirectional(theSound().mutedProperty());
        items.add(miMuted);

        var miQuit = new MenuItem(theAssets().text("quit"));
        miQuit.setOnAction(ae -> GameAction.QUIT_GAME_SCENE.execute());
        items.add(miQuit);

        return items;
    }

    @Override
    public void setCanvas(Canvas canvas) { /* ignore */ }

    public ObjectProperty<SceneDisplayMode> displayModeProperty() {
        return displayModePy;
    }

    protected void updateScaling() {
        SceneDisplayMode displayMode = displayModePy.get();
        double unscaledHeight = displayMode == SceneDisplayMode.SCROLLING ? NES_SIZE.y() : sizeInPx().y() + 3*TS;
        setScaling(viewPortHeightProperty().get() / unscaledHeight);
    }

    private void updateCameraPosition(double scaling) {
        int worldTilesY = optionalGameLevel().map(level -> level.worldMap().numRows()).orElse(NES_TILES.y());
        double dy = scaling * (worldTilesY - 43) * HTS;
        fixedCamera.setTranslateY(dy);
    }

    private void setJoypadKeyBindings(GameLevel level) {
        if (level.isDemoLevel()) {
            bind(QUIT_DEMO_LEVEL, theJoypad().key(JoypadButton.START));
        } else {
            bind(GameAction.PLAYER_UP,    theJoypad().key(JoypadButton.UP),    control(KeyCode.UP));
            bind(GameAction.PLAYER_DOWN,  theJoypad().key(JoypadButton.DOWN),  control(KeyCode.DOWN));
            bind(GameAction.PLAYER_LEFT,  theJoypad().key(JoypadButton.LEFT),  control(KeyCode.LEFT));
            bind(GameAction.PLAYER_RIGHT, theJoypad().key(JoypadButton.RIGHT), control(KeyCode.RIGHT));
            bind(TengenMsPacMan_GameAction.TOGGLE_PAC_BOOSTER, theJoypad().key(JoypadButton.A), theJoypad().key(JoypadButton.B));
            bindCheatActions();
        }
        enableActionBindings();
    }

    @Override
    public void bindActions() {
        bind(TengenMsPacMan_GameAction.TOGGLE_DISPLAY_MODE, Keyboard.alt(KeyCode.C));
    }

    @Override
    public void doInit() {
        messageMovement = new MessageMovement();
        theGame().scoreManager().setScoreVisible(true);
        setGameRenderer(theUIConfig().current().createRenderer(canvas));
        movingCamera.focusTopOfScene();
    }

    @Override
    protected void doEnd() {
        theSound().stopAll();
    }

    @Override
    public void update() {
        optionalGameLevel().ifPresent(level -> {
            if (level.isDemoLevel()) {
                theGame().assignDemoLevelBehavior(level.pac());
            }
            else {
                level.pac().setUsingAutopilot(PY_AUTOPILOT.get());
                level.pac().setImmune(PY_IMMUNITY.get());
                messageMovement.update();
                updateSound(level);
            }
            if (theGameState() == GameState.LEVEL_COMPLETE) {
                levelCompleteAnimation.tick();
            }
            if (fxSubScene.getCamera() == movingCamera) {
                if (theGameState() == GameState.HUNTING) {
                    movingCamera.focusPlayer(true);
                }
                movingCamera.setVerticalRangeTiles(level.worldMap().numRows());
                movingCamera.update(level.pac());
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
        return levelSizeInTilesOrElse(NES_TILES).toVector2f().scaled(TS);
    }

    @Override
    public void onGameStarted(GameEvent e) {
        GameLevel level = requiredGameLevel();
        boolean silent = level.isDemoLevel() || theGameState() == TESTING_LEVELS || theGameState() == TESTING_LEVEL_TEASERS;
        if (!silent) {
            theSound().playGameReadySound();
        }
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        GameLevel level = requiredGameLevel();
        setJoypadKeyBindings(level);
        if (level.isDemoLevel()) {
            level.pac().setImmune(false);
        } else {
            level.pac().setUsingAutopilot(PY_AUTOPILOT.get());
            level.pac().setImmune(PY_IMMUNITY.get());
        }
        gr.applyMapSettings(level.worldMap());
    }

    @Override
    public void onLevelStarted(GameEvent e) {
        movingCamera.setCameraToTopOfScene();
        movingCamera.focusBottomOfScene();
        movingCamera.setIdleTicks(90);
    }

    @Override
    public void onSceneVariantSwitch(GameScene oldScene) {
        GameLevel level = requiredGameLevel();
        setJoypadKeyBindings(level);
        gr.applyMapSettings(level.worldMap());
    }

    @Override
    public void onEnterGameState(GameState state) {
        switch (state) {
            case HUNTING -> movingCamera.focusPlayer(true);

            case LEVEL_COMPLETE ->
                optionalGameLevel().ifPresent(level -> {
                    theSound().stopAll();
                    levelCompleteAnimation = new FlashingMazeAnimation(level);
                    levelCompleteAnimation.setActionOnFinished(theGameController()::letCurrentStateExpire);
                    levelCompleteAnimation.start();
                });

            case GAME_OVER ->
                optionalGameLevel().ifPresent(level -> {
                    var tengenGame = (TengenMsPacMan_GameModel) theGame();
                    if (tengenGame.mapCategory() != MapCategory.ARCADE) {
                        float belowHouse = centerPosBelowHouse(level).x();
                        messageMovement.start(MOVING_MESSAGE_DELAY, belowHouse, sizeInPx().x());
                    }
                    movingCamera.focusTopOfScene();
                });

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
        optionalGameLevel().ifPresent(level -> level.showMessage(GameLevel.Message.READY));
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        theSound().playGhostEatenSound();
    }

    @Override
    public void onPacDead(GameEvent e) {
        movingCamera.focusTopOfScene();
        theGameController().letCurrentStateExpire();
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
            int sirenNumber = 1 + level.huntingTimer().phaseIndex() / 2; // TODO check how this works in original game
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
        final var tr = (TengenMsPacMan_Renderer2D) gr;
        tr.fillCanvas(tr.backgroundColorProperty().get());
        updateScaling();
        updateCameraPosition(scaling());
        drawSceneContent();
    }

    @Override
    protected void drawSceneContent() {
        final GameLevel level = optionalGameLevel().orElse(null);
        if (level == null) {
            // Scene is drawn already 2 ticks before level has been created
            Logger.warn("Tick {}: Game level not yet available, scene content not drawn", theClock().tickCount());
            return;
        }
        final var tengenGame = (TengenMsPacMan_GameModel) theGame();
        final var tr = (TengenMsPacMan_Renderer2D) gr;
        final int mazeTopY = 3 * TS;

        tr.ctx().save();

        // Tengen screen width is 32 tiles, each maze is 28 tiles wide, so reserve 2 tiles on each side
        tr.ctx().translate(scaled(2 * TS), 0);

        tr.setScaling(scaling());

        tr.drawScores(tengenGame.scoreManager(), nesPaletteColor(0x20), arcadeFontScaledTS());

        final boolean flashing = levelCompleteAnimation != null && levelCompleteAnimation.inFlashingPhase();
        if (flashing) {
            if (levelCompleteAnimation.inHighlightPhase()) {
                tr.drawHighlightedWorld(level, 0, mazeTopY, levelCompleteAnimation.flashingIndex());
            } else {
                tr.drawWorld(level, 0, mazeTopY);
                tr.drawFood(level); // this also hides eaten food!
            }
        }
        else {
            tr.drawWorld(level, 0, mazeTopY);
            tr.drawFood(level);
            level.bonus().ifPresent(tr::drawBonus);
            //TODO in the original game, the message is drawn under the maze image but *over* the pellets!
            tr.drawLevelMessage(level, level.isDemoLevel(), currentMessagePosition(level), arcadeFontScaledTS());
            tr.drawActor(level.pac());
            ghostsInZOrder(level).forEach(tr::drawActor);
        }

        // As long as Pac-Man is still invisible on game start, one live more is shown in the counter
        int numLivesDisplayed = theGameState() == GameState.STARTING_GAME && !level.pac().isVisible()
            ? tengenGame.lifeCount() : tengenGame.lifeCount() - 1;
        tr.drawLivesCounter(numLivesDisplayed, LIVES_COUNTER_MAX, 2 * TS, sizeInPx().y() - TS);

        if (level.isDemoLevel() || tengenGame.mapCategory() == MapCategory.ARCADE) {
            tr.drawLevelCounter(tengenGame.levelCounter(), sizeInPx());
        } else {
            tr.drawLevelCounterWithLevelNumbers(level.number(), tengenGame.levelCounter(), sizeInPx());
        }

        if (debugInfoVisiblePy.get()) {
            tr.drawAnimatedCreatureInfo(level.pac());
            ghostsInZOrder(level).forEach(tr::drawAnimatedCreatureInfo);
            drawDebugInfo();
        }

        tr.ctx().restore();
    }

    private Stream<Ghost> ghostsInZOrder(GameLevel level) {
        return Stream.of(ORANGE_GHOST_POKEY, CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, RED_GHOST_SHADOW).map(level::ghost);
    }

    private Vector2f currentMessagePosition(GameLevel level) {
        Vector2f center = centerPosBelowHouse(level);
        if (messageMovement != null && messageMovement.isRunning()) {
            return new Vector2f(messageMovement.currentX(), center.y());
        }
        return center;
    }

    private Vector2f centerPosBelowHouse(GameLevel level) {
        return level.houseMinTile().plus(0.5f * level.houseSizeInTiles().x(), level.houseSizeInTiles().y() + 1).scaled(TS);
    }
}