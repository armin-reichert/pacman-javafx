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
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.scene.common.CameraControlledGameScene;
import de.amr.games.pacman.ui2d.scene.common.GameScene;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui2d.sound.GameSound;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Camera;
import javafx.scene.Node;
import javafx.scene.ParallelCamera;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.MsPacManGameTengenActions.bindDefaultJoypadActions;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.MsPacManGameTengenSceneConfig.*;

/**
 * Tengen play scene, uses vertical scrolling.
 *
 * @author Armin Reichert
 */
public class PlayScene2D extends GameScene2D implements CameraControlledGameScene {

    private static final Vector2i CANVAS_SIZE = NES_SIZE.plus(0, 14 * TS);
    private static final float CAM_SPEED = 0.03f;
    private static final int MOVING_MESSAGE_DELAY = 120;

    private final SubScene fxSubScene;
    private final CameraControl cameraControl = new CameraControl();
    private final Canvas canvas;

    private MessageMovement messageMovement;
    private MazeFlashing mazeFlashing;
    private int camDelay;

    private class CameraControl {
        private double cameraTargetY;
        private boolean focusPlayer;

        public void setCameraToTopOfScene() {
            camera().setTranslateY(camMinY());
        }

        public void focusTopOfScene() {
            cameraTargetY = camMinY();
            focusPlayer = false;
        }

        public void focusBottomOfScene() {
            cameraTargetY = camMaxY();
            focusPlayer = false;
        }

        public double camMinY() {
            return scaled(-9 * TS);
        }

        public double camMaxY() {
            return scaled((mapTilesY() - 35) * TS);
        }

        public void focusPlayer(boolean focus) {
            focusPlayer = focus;
        }

        public void update() {
            if (focusPlayer) {
                double frac = (double) context.level().pac().tile().y() / mapTilesY();
                if (frac < 0.4) { frac = 0; } else if (frac > 0.6) { frac = 1.0; }
                cameraTargetY = lerp(camMinY(), camMaxY(), frac);
            }
            double y = lerp(camera().getTranslateY(), cameraTargetY, CAM_SPEED);
            camera().setTranslateY(clamp(y, camMinY(), camMaxY()));
            Logger.debug("Camera: y={0.00} target={} top={} bottom={}", camera().getTranslateY(), cameraTargetY, camMinY(), camMaxY());
        }
    }

    public PlayScene2D() {
        canvas = new Canvas();
        canvas.widthProperty().bind(scalingProperty().multiply(CANVAS_SIZE.x()));
        canvas.heightProperty().bind(scalingProperty().multiply(CANVAS_SIZE.y()));
        var root = new StackPane(canvas);
        root.setBackground(null);
        fxSubScene = new SubScene(root, 42, 42);
        fxSubScene.setFill(nesPaletteColor(0x0f));
        fxSubScene.setCamera(new ParallelCamera());
    }

    @Override
    public void bindGameActions() {}

    @Override
    public void doInit() {
        mazeFlashing = new MazeFlashing();
        messageMovement = new MessageMovement();
        cameraControl.focusTopOfScene();
        context.enableJoypad();
        context.setScoreVisible(true);
        setCanvas(canvas); // do not use common canvas from game page
    }

    @Override
    protected void doEnd() {
        context.sound().stopAll();
        context.disableJoypad();
    }

    @Override
    public void update() {
        if (context.game().level().isEmpty()) {
            return; // Scene is already visible for 2 ticks before game level gets created
        }
        if (context.game().isDemoLevel()) {
            context.game().setDemoLevelBehavior();
        }
        else { // TODO: add/remove listener to global properties instead?
            context.level().pac().setUsingAutopilot(PY_AUTOPILOT.get());
            context.level().pac().setImmune(PY_IMMUNITY.get());
            messageMovement.update();
            updateSound();
        }
        //TODO hack: in case we are switching from 3D scene, focusPlayer might be false even if it should be true
        if (context.gameState() == GameState.HUNTING) {
            cameraControl.focusPlayer(true);
        }
        if (camDelay > 0) {
            --camDelay;
        } else {
            cameraControl.update();
        }
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
        mazeFlashing.init(level.mapConfig(), level.numFlashes());
    }

    @Override
    public void onLevelStarted(GameEvent e) {
        cameraControl.setCameraToTopOfScene();
        cameraControl.focusBottomOfScene();
        camDelay = 90;
    }

    private int mapTilesY() {
        return context.level().world().map().terrain().numRows();
    }

    @Override
    public void onSceneVariantSwitch(GameScene oldScene) {
        Logger.info("{} entered from {}", this, oldScene);
        context.enableJoypad();
        setKeyBindings();
        //TODO what else?
    }

    private void setKeyBindings() {
        if (context.game().isDemoLevel()) {
            bind(QUIT_DEMO_LEVEL, context.joypad().keyCombination(NES.Joypad.START));
        } else {
            bindDefaultJoypadActions(this, context.joypad());
            bindFallbackPlayerControlActions(this);
            bindCheatActions(this);
        }
        registerGameActionKeyBindings(context.keyboard());
    }

    @Override
    public void onEnterGameState(GameState state) {
        switch (state) {
            case HUNTING -> cameraControl.focusPlayer(true);
            case LEVEL_COMPLETE -> mazeFlashing.init(context.level().mapConfig(), context.level().numFlashes());
            case GAME_OVER -> {
                var game = (MsPacManGameTengen) context.game();
                if (game.mapCategory() != MapCategory.ARCADE) {
                    float belowHouse = centerBelowHouse(context.level().world()).x();
                    messageMovement.start(MOVING_MESSAGE_DELAY, belowHouse, size().x());
                }
                cameraControl.focusTopOfScene();
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
        cameraControl.focusTopOfScene();
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
        long start = System.nanoTime();
        context.game().level().ifPresent(level -> gr.update(level.mapConfig()));
        long duration = System.nanoTime() - start;
        Logger.debug(() -> "Update renderer took %.3f millis".formatted(duration * 1e-6));

        gr.setScaling(scaling());
        gr.setBackgroundColor(backgroundColor());
        gr.clearCanvas();
        drawSceneContent(gr);
    }

    @Override
    protected void drawSceneContent(GameRenderer gr) {
        if (context.game().level().isEmpty()) {
            Logger.warn("Tick #{}: Cannot draw scene content, game level not yet available!", context.tick());
            return;
        }

        final var game = (MsPacManGameTengen) context.game();
        final GameWorld world = context.level().world();
        final Pac msPacMan = context.level().pac();
        final var r = (MsPacManGameTengenRenderer) gr;

        r.setBlinking(context.level().blinking().isOn());

        gr.ctx().save();
        gr.ctx().translate(scaled(2 * TS), 0);

        if (context.isScoreVisible()) {
            gr.drawScores(context);
        }
        Vector2f messageCenterPosition = centerBelowHouse(world);
        r.setMessageAnchorPosition(messageMovement.isRunning()
            ? new Vector2f(messageMovement.currentX(), messageCenterPosition.y())
            : messageCenterPosition
        );

        if (Boolean.TRUE.equals(context.gameState().getProperty("mazeFlashing"))) {
            mazeFlashing.update(context.tick());
            r.drawEmptyMap(world.map(), mazeFlashing.currentColorMap());
        } else {
            r.drawWorld(context, world, 0,  3 * TS);
        }

        r.drawAnimatedEntity(msPacMan);
        ghostsInZOrder(context.level()).forEach(r::drawAnimatedEntity);

        int livesCounterEntries = game.lives() - 1;
        if (context.gameState() == GameState.STARTING_GAME && !msPacMan.isVisible()) {
            // as long as Pac-Man is invisible when the game is started, one entry more appears in the lives counter
            livesCounterEntries += 1;
        }
        r.drawLivesCounter(livesCounterEntries, 5, 2 * TS, size().y() - TS);
        r.setLevelNumberBoxesVisible(!context.game().isDemoLevel() && game.mapCategory() != MapCategory.ARCADE);
        r.drawLevelCounter(context, size());

        gr.ctx().restore();

        // Debug mode info
        if (debugInfoVisiblePy.get()) {
            r.drawAnimatedCreatureInfo(msPacMan);
            ghostsInZOrder(context.level()).forEach(r::drawAnimatedCreatureInfo);
            drawDebugInfo(gr);
        }
    }

    @Override
    protected void drawDebugInfo(GameRenderer gr) {
        gr.drawTileGrid(Vector2f.fromDouble(canvas.getWidth(), canvas.getHeight()));
        gr.ctx().setFill(Color.YELLOW);
        gr.ctx().setFont(Font.font("Sans", FontWeight.BOLD, 24));
        gr.ctx().fillText(String.format("%s %d", context.gameState(), context.gameState().timer().tickCount()), 0, 64);
        gr.ctx().fillText("Camera target=%.2f position=%.2f Scene width=%.0f height=%.0f".formatted(
            cameraControl.cameraTargetY, camera().getTranslateY(), scaled(size().x()), scaled(size().y()) ),
            scaled(20), scaled(0.5 * size().y() + 20));
    }

    private Vector2f centerBelowHouse(GameWorld world) {
        Vector2i houseTopLeft = world.houseTopLeftTile(), houseSize = world.houseSize();
        float x = TS * (houseTopLeft.x() + houseSize.x() * 0.5f);
        float y = TS * (houseTopLeft.y() + houseSize.y() + 1);
        return new Vector2f(x, y);
    }

    private Stream<Ghost> ghostsInZOrder(GameLevel level) {
        return Stream.of(ORANGE_GHOST, CYAN_GHOST, PINK_GHOST, RED_GHOST).map(level::ghost);
    }
}