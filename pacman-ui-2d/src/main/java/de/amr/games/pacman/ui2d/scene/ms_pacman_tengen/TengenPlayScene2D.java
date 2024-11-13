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
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.ms_pacman_tengen.MapCategory;
import de.amr.games.pacman.model.ms_pacman_tengen.TengenMsPacManGame;
import de.amr.games.pacman.ui2d.input.JoypadKeyAdapter;
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
import javafx.scene.layout.Pane;
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
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenGameActions.QUIT_DEMO_LEVEL;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenGameActions.bindDefaultJoypadActions;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameSceneConfig.*;

/**
 * Tengen play scene, uses vertical scrolling.
 *
 * @author Armin Reichert
 */
public class TengenPlayScene2D extends GameScene2D implements CameraControlledGameScene {

    private static final int MOVING_MESSAGE_DELAY = 120;

    private final SubScene fxSubScene;
    private final ParallelCamera camera = new ParallelCamera();
    private final Canvas canvas = new Canvas(NES_RESOLUTION_X, NES_RESOLUTION_Y);
    private final MessageMovement messageMovement = new MessageMovement();
    private final MazeFlashing mazeFlashing = new MazeFlashing();
    private int camDelay;

    public TengenPlayScene2D() {
        canvas.widthProperty() .bind(scalingProperty().map(s -> s.doubleValue() * size().x()));
        canvas.heightProperty().bind(scalingProperty().map(s -> s.doubleValue() * size().y()));
        Pane root = new StackPane(canvas);
        root.setBackground(null);
        fxSubScene = new SubScene(root, 42, 42);
        fxSubScene.setCamera(camera);
    }

    @Override
    public void bindGameActions() {}

    @Override
    public void doInit() {
        context.enableJoypad();
        context.setScoreVisible(true);
    }

    @Override
    protected void doEnd() {
        context.sound().stopAll();
        context.disableJoypad();
    }

    @Override
    public void update() {
        if (context.game().level().isEmpty()) {
            // Scene is already visible for 2 ticks before game level is created
            return;
        }

        if (context.game().isDemoLevel()) {
            context.game().setDemoLevelBehavior();
        }
        else {
            context.level().pac().setUsingAutopilot(PY_AUTOPILOT.get());
            context.level().pac().setImmune(PY_IMMUNITY.get());
            messageMovement.update();
            updatePlaySceneSound();
        }

        if (camDelay > 0) {
            --camDelay;
        }
        else {
            double msPacManY = scaled(context.level().pac().center().y());
            double r = cameraRadius();
            double y = lerp(camera.getTranslateY(), msPacManY - r, 0.02);
            camera.setTranslateY(clamp(y, dontAskItsMagic(-r), dontAskItsMagic(r)));
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
        return camera;
    }

    private void initCamera(int delayTicks) {
        camDelay = delayTicks;
        // set camera at top
        camera.setTranslateY(dontAskItsMagic(-cameraRadius()));
    }

    private double cameraRadius() {
        return 0.5 * scaled(size().y() - 24);
    }

    //TODO replace black magic with science
    private double dontAskItsMagic(double radius) {
        double height = size().y();
        if (height >= 40 * TS) return 0.38 * radius;
        if (height >= 35 * TS) return 0.30 * radius;
        return 0.1 * radius;
    }

    @Override
    public Vector2f size() {
        Vector2i nesSizeInTiles = new Vector2i(NES_TILES_X, NES_TILES_Y);
        //TODO: change map definitions instead of inventing 2 additional rows here?
        return (context == null)
            ? nesSizeInTiles.toVector2f()
            : context.worldSizeInTilesOrElse(nesSizeInTiles).plus(0, 2).scaled((float) TS);
    }

    @Override
    public void onGameStarted(GameEvent e) {
        boolean silent = context.game().isDemoLevel() ||
            context.gameState() == TESTING_LEVEL_BONI ||
            context.gameState() == TESTING_LEVEL_TEASERS;
        if (!silent) {
            context.sound().playGameReadySound();
        }
        initCamera(30);
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        if (context.game().isDemoLevel()) {
            context.level().pac().setImmune(false);
        } else {
            context.level().pac().setUsingAutopilot(PY_AUTOPILOT.get());
            context.level().pac().setImmune(PY_IMMUNITY.get());
        }
        context.updateRenderer();
        context.enableJoypad();
        setKeyBindings();
    }

    @Override
    public void onLevelStarted(GameEvent e) {
        initCamera(90);
    }

    @Override
    public void onSceneVariantSwitch(GameScene oldScene) {
        Logger.info("{} entered from {}", this, oldScene);
        context.updateRenderer();
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
        TengenMsPacManGame game = (TengenMsPacManGame) context.game();
        switch (state) {
            case LEVEL_COMPLETE -> mazeFlashing.init(game);
            case GAME_OVER -> {
                if (game.mapCategory() != MapCategory.ARCADE) {
                    Vector2f belowHouse = centerBelowHouse(context.level().world());
                    messageMovement.start(MOVING_MESSAGE_DELAY, belowHouse.x(), size().x());
                }
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
    public void onPacDied(GameEvent e) {
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

    private void updatePlaySceneSound() {
        GameSound sounds = context.sound();
        if (context.gameState() == GameState.HUNTING && !context.level().powerTimer().isRunning()) {
            HuntingControl huntingControl = context.game().huntingControl();
            int sirenNumber = 1 + huntingControl.phaseIndex() / 2; // TODO check how this works in original game
            sounds.selectSiren(sirenNumber);
            sounds.playSiren();
        }
        if (context.level().pac().starvingTicks() > 8) { // TODO not sure how to do this right
            sounds.stopMunchingSound();
        }
        boolean ghostsReturning = context.level().ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).anyMatch(Ghost::isVisible);
        if (context.level().pac().isAlive() && ghostsReturning) {
            sounds.playGhostReturningHomeSound();
        } else {
            sounds.stopGhostReturningHomeSound();
        }
    }

    // drawing

    @Override
    public void draw(GameRenderer gr) {
        gr.setCanvas(canvas);
        gr.setScaling(scaling());
        gr.setBackgroundColor(backgroundColor());
        gr.clearCanvas();
        drawSceneContent(gr);
    }

    @Override
    protected void drawSceneContent(GameRenderer gr) {
        if (context.isScoreVisible()) {
            gr.drawScores(context);
        }

        if (context.game().level().isEmpty()) {
            Logger.warn("Tick #{}: Cannot draw scene content, game level not yet available!", context.tick());
            return;
        }

        final var game = (TengenMsPacManGame) context.game();
        final GameWorld world = context.level().world();
        final Pac msPacMan = context.level().pac();
        final var r = (TengenMsPacManGameRenderer) gr;

        r.setBlinkingOn(context.level().blinking().isOn());

        Vector2f messageCenterPosition = centerBelowHouse(world);
        r.setMessagePosition(messageMovement.isRunning()
            ? new Vector2f(messageMovement.currentX(), messageCenterPosition.y())
            : messageCenterPosition
        );

        if (Boolean.TRUE.equals(context.gameState().getProperty("mazeFlashing"))) {
            mazeFlashing.update(context.tick());
            r.drawEmptyMap(world.map(), mazeFlashing.currentColorScheme());
        } else {
            r.drawWorld(context, world, 0,  3 * TS);
        }

        r.drawAnimatedEntity(msPacMan);
        ghostsInZOrder().forEach(r::drawAnimatedEntity);

        int livesCounterEntries = game.lives() - 1;
        if (context.gameState() == GameState.STARTING_GAME && !msPacMan.isVisible()) {
            // as long as Pac-Man is invisible when the game is started, one entry more appears in the lives counter
            livesCounterEntries += 1;
        }
        r.drawLivesCounter(livesCounterEntries, 5, size());
        r.setLevelNumberBoxesVisible(!context.game().isDemoLevel() && game.mapCategory() != MapCategory.ARCADE);
        r.drawLevelCounter(context, size());

        // Debug mode info
        if (debugInfoPy.get()) {
            r.drawAnimatedCreatureInfo(msPacMan);
            ghostsInZOrder().forEach(r::drawAnimatedCreatureInfo);
            drawDebugInfo(gr);
        }
    }

    @Override
    protected void drawDebugInfo(GameRenderer gr) {
        gr.drawTileGrid(size());
        gr.ctx().setFill(Color.YELLOW);
        gr.ctx().setFont(Font.font("Sans", FontWeight.BOLD, 24));
        gr.ctx().fillText(String.format("%s %d", context.gameState(), context.gameState().timer().tickCount()), 0, 64);
    }

    private Vector2f centerBelowHouse(GameWorld world) {
        Vector2i houseTopLeft = world.houseTopLeftTile(), houseSize = world.houseSize();
        float x = TS * (houseTopLeft.x() + houseSize.x() * 0.5f);
        float y = TS * (houseTopLeft.y() + houseSize.y() + 1);
        return new Vector2f(x, y);
    }

    private Stream<Ghost> ghostsInZOrder() {
        return Stream.of(ORANGE_GHOST, CYAN_GHOST, PINK_GHOST, RED_GHOST).map(context.level()::ghost);
    }
}