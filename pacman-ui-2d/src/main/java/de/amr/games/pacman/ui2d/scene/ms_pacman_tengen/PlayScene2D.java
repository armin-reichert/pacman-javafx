/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.controller.HuntingControl;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.nes.NES;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.ms_pacman_tengen.MapCategory;
import de.amr.games.pacman.model.ms_pacman_tengen.MsPacManGameTengen;
import de.amr.games.pacman.ui2d.input.Keyboard;
import de.amr.games.pacman.ui2d.scene.common.CameraControlledView;
import de.amr.games.pacman.ui2d.scene.common.GameScene;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui2d.sound.GameSound;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Camera;
import javafx.scene.Node;
import javafx.scene.ParallelCamera;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import org.tinylog.Logger;

import java.util.stream.Stream;

import static de.amr.games.pacman.controller.GameState.TESTING_LEVEL_BONI;
import static de.amr.games.pacman.controller.GameState.TESTING_LEVEL_TEASERS;
import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.GameModel.*;
import static de.amr.games.pacman.ui2d.GameActions2D.bindCheatActions;
import static de.amr.games.pacman.ui2d.GameActions2D.bindFallbackPlayerControlActions;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.PY_AUTOPILOT;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.PY_IMMUNITY;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.MsPacManGameTengenActions.QUIT_DEMO_LEVEL;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.MsPacManGameTengenActions.setDefaultJoypadBinding;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.MsPacManGameTengenSceneConfig.*;

/**
 * Tengen play scene, uses vertical scrolling.
 *
 * @author Armin Reichert
 */
public class PlayScene2D extends GameScene2D implements CameraControlledView {

    // (NES screen width, BIG map height (42 tiles) + 2 extra tile rows)
    private static final Vector2i UNSCALED_CANVAS_SIZE = v2i(NES_SIZE.x(), 44 * TS);

    private static final int MOVING_MESSAGE_DELAY = 120;

    private static class MovingCamera extends ParallelCamera {
        private static final float CAM_SPEED = 0.03f;

        private final DoubleProperty scalingPy = new SimpleDoubleProperty(1.0);

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

        public DoubleProperty scalingProperty() {
            return scalingPy;
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
            setTranslateY(clamp(y, camMinY(), camMaxY()));
            Logger.debug("Camera: y={0.00} target={} top={} bottom={}", getTranslateY(), targetY, camMinY(), camMaxY());
        }
    }

    private final SubScene fxSubScene;
    private final Canvas canvas;
    private final MovingCamera movingCamera;
    private final ParallelCamera fixedCamera;
    private final ObjectProperty<SceneDisplayMode> displayModePy = new SimpleObjectProperty<>(SceneDisplayMode.SCROLLING);

    private MessageMovement messageMovement;
    private LevelCompleteAnimationTengen levelCompleteAnimation;

    public PlayScene2D() {
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
        root.setBackground(null);

        fxSubScene = new SubScene(root, 42, 42);
        fxSubScene.setFill(nesPaletteColor(0x0f));

        movingCamera = new MovingCamera();
        movingCamera.scalingProperty().bind(scalingProperty());

        fixedCamera = new ParallelCamera();

        fxSubScene.cameraProperty().bind(displayModeProperty()
            .map(mode -> mode == SceneDisplayMode.SCROLLING ? movingCamera : fixedCamera));
    }

    public ObjectProperty<SceneDisplayMode> displayModeProperty() {
        return displayModePy;
    }

    protected void updateScaling() {
        SceneDisplayMode displayMode = displayModePy.get();
        double unscaledHeight = displayMode == SceneDisplayMode.SCROLLING ? NES_SIZE.y() : size().y() + 3*TS;
        setScaling(viewPortHeightProperty().get() / unscaledHeight);
    }

    private void updateCameraPosition(double scaling) {
        int worldTilesY = context.game().level().map(level -> level.world().map().terrain().numRows()).orElse(NES_TILES.y());
        double dy = scaling * (worldTilesY - 43) * HTS;
        fixedCamera.setTranslateY(dy);
    }

    @Override
    public void bindGameActions() {
        bind(MsPacManGameTengenActions.TOGGLE_DISPLAY_MODE, Keyboard.alt(KeyCode.C));
    }

    @Override
    public void doInit() {
        setCanvas(canvas); // do not use common canvas from game page
        messageMovement = new MessageMovement();
        context.enableJoypad();
        context.setScoreVisible(true);
        movingCamera.focusTopOfScene();
    }

    @Override
    protected void doEnd() {
        context.sound().stopAll();
        context.disableJoypad();
    }

    @Override
    public void update() {
        context.game().level().ifPresent(level -> {
            if (context.game().isDemoLevel()) {
                context.game().setDemoLevelBehavior();
            }
            else {
                level.pac().setUsingAutopilot(PY_AUTOPILOT.get());
                level.pac().setImmune(PY_IMMUNITY.get());
                messageMovement.update();
                updateSound();
            }
            if (context.gameState() == GameState.LEVEL_COMPLETE) {
                levelCompleteAnimation.update();
            }
            if (fxSubScene.getCamera() == movingCamera) {
                if (context.gameState() == GameState.HUNTING) {
                    movingCamera.focusPlayer(true);
                }
                movingCamera.setVerticalRangeTiles(level.world().map().terrain().numRows());
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
    public Vector2f size() {
        return context.worldSizeInTilesOrElse(NES_TILES).toVector2f().scaled(TS);
    }

    @Override
    public void onGameStarted(GameEvent e) {
        boolean silent = context.game().isDemoLevel() ||
            context.gameState() == TESTING_LEVEL_BONI ||
            context.gameState() == TESTING_LEVEL_TEASERS;
        if (!silent) {
            context.sound().playGameReadySound();
        }
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        context.enableJoypad();
        setKeyBindings();

        GameLevel level = context.level();
        if (context.game().isDemoLevel()) {
            level.pac().setImmune(false);
        } else {
            level.pac().setUsingAutopilot(PY_AUTOPILOT.get());
            level.pac().setImmune(PY_IMMUNITY.get());
        }
        gr.setWorldMap(level.world().map());
    }

    @Override
    public void onLevelStarted(GameEvent e) {
        movingCamera.setCameraToTopOfScene();
        movingCamera.focusBottomOfScene();
        movingCamera.setIdleTicks(90);
    }

    @Override
    public void onSceneVariantSwitch(GameScene oldScene) {
        Logger.info("{} entered from {}", this, oldScene);
        context.enableJoypad();
        setKeyBindings();
    }

    private void setKeyBindings() {
        if (context.game().isDemoLevel()) {
            bind(QUIT_DEMO_LEVEL, context.joypadKeys().key(NES.JoypadButton.BTN_START));
        } else {
            setDefaultJoypadBinding(this, context.joypadKeys());
            bindFallbackPlayerControlActions(this);
            bindCheatActions(this);
        }
        registerGameActionKeyBindings(context.keyboard());
    }

    @Override
    public void onEnterGameState(GameState state) {
        switch (state) {
            case HUNTING -> movingCamera.focusPlayer(true);
            case LEVEL_COMPLETE -> {
                levelCompleteAnimation = new LevelCompleteAnimationTengen(
                    context.level().world().map(), context.level().numFlashes(), 10);
                levelCompleteAnimation.setOnHideGhosts(() -> context.level().ghosts().forEach(Ghost::hide));
                levelCompleteAnimation.setOnFinished(() -> state.timer().expire());
                levelCompleteAnimation.start();
            }
            case GAME_OVER -> {
                var game = (MsPacManGameTengen) context.game();
                if (game.mapCategory() != MapCategory.ARCADE) {
                    float belowHouse = centerPosBelowHouse(context.level().world()).x();
                    messageMovement.start(MOVING_MESSAGE_DELAY, belowHouse, size().x());
                }
                movingCamera.focusTopOfScene();
            }
            default -> {}
        }
    }

    @Override
    public void onBonusActivated(GameEvent e) {
        context.sound().playBonusBouncingSound();
    }

    @Override
    public void onBonusEaten(GameEvent e) {
        context.sound().stopBonusBouncingSound();
        context.sound().playBonusEatenSound();
    }

    @Override
    public void onBonusExpired(GameEvent e) {
        context.sound().stopBonusBouncingSound();
    }

    @Override
    public void onExtraLifeWon(GameEvent e) {
        context.sound().playExtraLifeSound();
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        context.sound().playGhostEatenSound();
    }

    @Override
    public void onPacDead(GameEvent e) {
        movingCamera.focusTopOfScene();
    }

    @Override
    public void onPacDying(GameEvent e) {
        context.sound().playPacDeathSound();
    }

    @Override
    public void onPacFoundFood(GameEvent e) {
        context.sound().playMunchingSound();
    }

    @Override
    public void onPacGetsPower(GameEvent e) {
        context.sound().stopSiren();
        context.sound().playPacPowerSound();
    }

    @Override
    public void onPacLostPower(GameEvent e) {
        context.sound().stopPacPowerSound();
    }

    private void updateSound() {
        GameLevel level = context.level();
        GameSound sound = context.sound();
        if (context.gameState() == GameState.HUNTING && !level.powerTimer().isRunning()) {
            HuntingControl huntingControl = context.game().huntingControl();
            int sirenNumber = 1 + huntingControl.phaseIndex() / 2; // TODO check how this works in original game
            sound.selectSiren(sirenNumber);
            sound.playSiren();
        }
        if (level.pac().starvingTicks() > 8) { // TODO not sure how to do this right
            sound.stopMunchingSound();
        }
        boolean ghostsReturning = level.ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).anyMatch(Ghost::isVisible);
        if (level.pac().isAlive() && ghostsReturning) {
            sound.playGhostReturningHomeSound();
        } else {
            sound.stopGhostReturningHomeSound();
        }
    }

    // drawing

    @Override
    public void draw() {
        // do this here because it should be run also when game is paused
        updateScaling();
        updateCameraPosition(scaling());

        var r = (MsPacManGameTengenRenderer) gr;
        r.setScaling(scaling());
        r.clearCanvas();
        context.game().level().ifPresent(level -> {
            r.ctx().save();
            r.ctx().translate(scaled(2 * TS), 0);
            drawSceneContent();
            r.ctx().restore();
        });
    }

    @Override
    protected void drawSceneContent() {
        var r = (MsPacManGameTengenRenderer) gr;

        MsPacManGameTengen game = (MsPacManGameTengen) context.game();
        GameLevel level = context.level();
        GameWorld world = level.world();
        Pac msPacMan = level.pac();

        if (context.isScoreVisible()) {
            r.drawScores(context);
        }
        Vector2f messageCenterPosition = centerPosBelowHouse(world);
        if (messageMovement != null) {
            r.setMessageAnchorPosition(messageMovement.isRunning()
                    ? new Vector2f(messageMovement.currentX(), messageCenterPosition.y())
                    : messageCenterPosition
            );
        } else {
            r.setMessageAnchorPosition(messageCenterPosition);
        }

        r.setBlinking(level.blinking().isOn());
        boolean flashing = levelCompleteAnimation != null && levelCompleteAnimation.isFlashing();
        if (flashing && levelCompleteAnimation.isInHighlightPhase()) {
            //TODO support multi-color flashing again
            //r.drawEmptyMap(world.map(), levelCompleteAnimation.currentFillColor(), levelCompleteAnimation.currentStrokeColor());
            r.drawWorldHighlighted(context, world, 0, 3 * TS);
        } else {
            r.drawWorld(context, world, 0,  3 * TS);
        }

        context.level().bonus().ifPresent(r::drawBonus);

        r.drawAnimatedEntity(msPacMan);
        ghostsInZOrder(level).forEach(r::drawAnimatedEntity);

        int livesCounterEntries = game.lives() - 1;
        if (context.gameState() == GameState.STARTING_GAME && !msPacMan.isVisible()) {
            // as long as Pac-Man is invisible when the game is started, one entry more appears in the lives counter
            livesCounterEntries += 1;
        }
        r.drawLivesCounter(livesCounterEntries, 5, 2 * TS, size().y() - TS);
        r.setLevelNumberBoxesVisible(!game.isDemoLevel() && game.mapCategory() != MapCategory.ARCADE);
        r.drawLevelCounter(context, size().x() - 2 * TS, size().y() - TS);

        if (debugInfoVisiblePy.get()) {
            r.drawAnimatedCreatureInfo(msPacMan);
            ghostsInZOrder(level).forEach(r::drawAnimatedCreatureInfo);
            drawDebugInfo();
        }
    }

    @Override
    protected void drawDebugInfo() {
        gr.drawTileGrid(canvas.getWidth(), canvas.getHeight());
        gr.ctx().setFill(Color.WHITE);
        gr.ctx().setFont(DEBUG_FONT);
        GameState state = context().gameState();
        gr.ctx().fillText("%s %d".formatted(state, state.timer().tickCount()), 0, scaled(3 * TS));
    }

    private Vector2f centerPosBelowHouse(GameWorld world) {
        return world.houseTopLeftTile().plus(0.5f * world.houseSize().x(), world.houseSize().y() + 1).scaled(TS);
    }

    private Stream<Ghost> ghostsInZOrder(GameLevel level) {
        return Stream.of(ORANGE_GHOST, CYAN_GHOST, PINK_GHOST, RED_GHOST).map(level::ghost);
    }
}