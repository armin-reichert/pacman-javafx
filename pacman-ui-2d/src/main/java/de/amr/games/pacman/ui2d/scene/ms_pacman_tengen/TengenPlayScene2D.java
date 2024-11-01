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
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.ms_pacman_tengen.MapCategory;
import de.amr.games.pacman.model.ms_pacman_tengen.TengenMsPacManGame;
import de.amr.games.pacman.ui2d.GameActions2D;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.scene.common.CameraControlledGameScene;
import de.amr.games.pacman.ui2d.scene.common.GameScene;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui2d.sound.GameSound;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.Camera;
import javafx.scene.Node;
import javafx.scene.ParallelCamera;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.tinylog.Logger;

import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.GameModel.*;
import static de.amr.games.pacman.ui2d.GameAssets2D.assetPrefix;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.PY_AUTOPILOT;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.PY_IMMUNITY;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameRenderer.TENGEN_YELLOW;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameSceneConfiguration.*;
import static de.amr.games.pacman.ui2d.util.KeyInput.alt;
import static de.amr.games.pacman.ui2d.util.KeyInput.control;
import static de.amr.games.pacman.ui2d.util.NES_Controller.DEFAULT_CONTROLLER;
import static de.amr.games.pacman.ui2d.util.Ufx.coloredBackground;

/**
 * Tengen play scene, uses vertical scrolling.
 *
 * @author Armin Reichert
 */
public class TengenPlayScene2D extends GameScene2D implements CameraControlledGameScene {

    static final int MESSAGE_ANIMATION_DELAY = 120; // TODO how long?
    static final double MESSAGE_SPEED = 1;  // TODO how fast?

    private final SubScene fxSubScene;
    private final ParallelCamera cam = new ParallelCamera();
    private final Canvas canvas = new Canvas(NES_RESOLUTION_X, NES_RESOLUTION_Y);
    private int camDelay;
    private final GameOverMessageAnimation gameOverMessageAnimation = new GameOverMessageAnimation();

    private static class GameOverMessageAnimation {
        private double startX;
        private double rightBorderX;
        private double speed;
        private double currentX;
        private boolean wrapped;
        private long delay;

        void start(double startX, double rightBorderX, double speed) {
            this.startX = startX;
            this.rightBorderX = rightBorderX;
            this.speed = speed;
            currentX = startX;
            wrapped = false;
            delay = MESSAGE_ANIMATION_DELAY;
        }

        void update() {
            if (delay > 0) {
                --delay;
                return;
            }
            currentX += speed;
            if (currentX > rightBorderX) {
                currentX = 0;
                wrapped = true;
            }
            if (wrapped && currentX >= startX) {
                speed = 0;
                currentX = startX;
            }
        }
    }

    public TengenPlayScene2D() {
        Pane root = new StackPane(canvas);
        root.setBackground(coloredBackground(Color.TRANSPARENT));
        StackPane.setAlignment(canvas, Pos.CENTER);
        fxSubScene = new SubScene(root, 42, 42);
        fxSubScene.setCamera(cam);
    }

    @Override
    public void defineGameActionKeyBindings() {
        bindAction(GameActions2D.CHEAT_EAT_ALL,              alt(KeyCode.E));
        bindAction(GameActions2D.CHEAT_ADD_LIVES,            alt(KeyCode.L));
        bindAction(GameActions2D.CHEAT_NEXT_LEVEL,           alt(KeyCode.N));
        bindAction(GameActions2D.CHEAT_KILL_GHOSTS,          alt(KeyCode.X));

        bindAction(GameActions2D.TENGEN_TOGGLE_PAC_BOOSTER, DEFAULT_CONTROLLER.a(), DEFAULT_CONTROLLER.b());
        bindAction(GameActions2D.TENGEN_QUIT_DEMO_LEVEL,    DEFAULT_CONTROLLER.start());
        bindAction(GameActions2D.PLAYER_UP,                 DEFAULT_CONTROLLER.up(),    control(KeyCode.UP));
        bindAction(GameActions2D.PLAYER_DOWN,               DEFAULT_CONTROLLER.down(),  control(KeyCode.DOWN));
        bindAction(GameActions2D.PLAYER_LEFT,               DEFAULT_CONTROLLER.left(),  control(KeyCode.LEFT));
        bindAction(GameActions2D.PLAYER_RIGHT,              DEFAULT_CONTROLLER.right(), control(KeyCode.RIGHT));
    }

    @Override
    public void doInit() {
        context.plugIn_NES_Controller();
        context.setScoreVisible(true);
        canvas.widthProperty().bind(scalingProperty().map(scaling -> scaled(size().x())));
        canvas.heightProperty().bind(scalingProperty().map(scaling -> scaled(size().y())));
    }

    @Override
    protected void doEnd() {
        context.sound().stopAll();
        context.plugOut_NES_Controller();
    }

    @Override
    public void update() {
        TengenMsPacManGame game = (TengenMsPacManGame) context.game();
        if (game.currentLevelNumber() == 0) {
            Logger.warn("Cannot update PlayScene2D: no game level available");
            return;
        }
        Pac msPacMan = game.pac();
        if (game.world() == null || msPacMan == null) {
            //TODO: Can world or Ms. Pac-Man be null here?
            Logger.warn("Cannot update PlayScene2D: no game world available");
            return;
        }
        if (game.isDemoLevel()) {
            msPacMan.setUsingAutopilot(true);
            msPacMan.setImmune(false);
        } else {
            msPacMan.setUsingAutopilot(PY_AUTOPILOT.get());
            msPacMan.setImmune(PY_IMMUNITY.get());
            updatePlaySceneSound();
            if (context.gameState() == GameState.GAME_OVER && game.mapCategory() != MapCategory.ARCADE) {
                // only non-Arcade maps have moving "Game Over" text
                gameOverMessageAnimation.update();
            }
        }
        if (camDelay > 0) {
            --camDelay;
        }
        else {
            double msPacManY = scaled(msPacMan.center().y());
            double r = cameraRadius();
            double y = lerp(cam.getTranslateY(), msPacManY - r, 0.02);
            cam.setTranslateY(clamp(y, dontAskItsMagic(-r), dontAskItsMagic(r)));
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

    private void initCamDelay(int ticks) {
        camDelay = ticks;
        cam.setTranslateY(dontAskItsMagic(-cameraRadius()));
    }

    private double cameraRadius() {
        return 0.5 * scaled(size().y() - 24);
    }

    private double dontAskItsMagic(double radius) {
        double height = size().y();
        if (height >= 40 * TS) return 0.38 * radius;
        if (height >= 35 * TS) return 0.30 * radius;
        return 0.1 * radius;
    }

    @Override
    public Vector2f size() {
        Vector2i sizeInTiles = context.worldSizeInTilesOrElse(new Vector2i(NES_TILES_X, NES_TILES_Y));
        return sizeInTiles.plus(0, 2).scaled(TS).toVector2f(); //TODO: change maps instead of inventing rows?
    }

    @Override
    public void draw(GameRenderer renderer) {
        renderer.ctx().setImageSmoothing(false);
        renderer.setCanvas(canvas);
        renderer.scalingProperty().set(scaling());
        renderer.setBackgroundColor(backgroundColor());
        renderer.clearCanvas();
        if (context.isScoreVisible()) {
            renderer.drawScores(context);
        }
        drawSceneContent(renderer);
        if (debugInfoPy.get()) {
            drawDebugInfo(renderer);
        }
    }

    @Override
    protected void drawSceneContent(GameRenderer renderer) {
        GameState state = context.gameState();
        GameModel game = context.game();
        GameWorld world = game.world();
        Pac msPacMan = game.pac();

        if (world == null) { // This happens on level start
            Logger.warn("Cannot draw scene content, game world not yet available!");
            return;
        }

        // Draw level message centered under ghost house
        Vector2i houseTopLeftTile = world.houseTopLeftTile();
        Vector2i houseSize        = world.houseSize();
        double cx = TS * (world.houseTopLeftTile().x() + world.houseSize().x() * 0.5);
        double y = TS * (houseTopLeftTile.y() + houseSize.y() + 1);
        drawLevelMessage(renderer, cx, y); // READY, GAME_OVER etc.

        boolean flashMode = Boolean.TRUE.equals(state.getProperty("mazeFlashing"));
        renderer.setFlashMode(flashMode);
        renderer.setBlinkingOn(game.blinking().isOn());
        renderer.drawWorld(context, world);

        renderer.drawAnimatedEntity(msPacMan);
        ghostsInZOrder().forEach(renderer::drawAnimatedEntity);

        // Debug mode info
        if (debugInfoPy.get()) {
            renderer.drawAnimatedCreatureInfo(msPacMan);
            ghostsInZOrder().forEach(renderer::drawAnimatedCreatureInfo);
        }

        int livesCounterEntries = game.lives() - 1;
        if (state == GameState.STARTING_GAME && !msPacMan.isVisible()) {
            // as long as Pac-Man is invisible when the game is started, one entry more appears in the lives counter
            livesCounterEntries += 1;
        }
        renderer.drawLivesCounter(livesCounterEntries, 5, size());
        renderer.drawLevelCounter(context, size());
    }

    private Stream<Ghost> ghostsInZOrder() {
        return Stream.of(ORANGE_GHOST, CYAN_GHOST, PINK_GHOST, RED_GHOST).map(context.game()::ghost);
    }

    private void drawLevelMessage(GameRenderer renderer, double cx, double y) {
        AssetStorage assets = context.assets();
        String assetPrefix = assetPrefix(GameVariant.MS_PACMAN_TENGEN);
        GameState state = context.gameState();
        GameModel game = context.game();
        if (game.isDemoLevel()) {
            Color color = Color.web(game.currentMapColorScheme().get("stroke"));
            drawText(renderer, "GAME  OVER", cx, y, color);
        } else if (state == GameState.GAME_OVER) {
            Color color = assets.color(assetPrefix + ".color.game_over_message");
            drawText(renderer, "GAME  OVER", gameOverMessageAnimation.currentX, y, color);
        } else if (state == GameState.STARTING_GAME) {
            Color color = assets.color(assetPrefix + ".color.ready_message");
            drawText(renderer, "READY!", cx, y, color);
        } else if (state == GameState.TESTING_LEVEL_BONI) {
            drawText(renderer, "TEST L%02d".formatted(game.currentLevelNumber()), cx, y, TENGEN_YELLOW);
        }
    }

    private void drawText(GameRenderer renderer, String text, double cx, double y, Color color) {
        double x = (cx - text.length() * 0.5 * TS);
        renderer.drawText(text, color, renderer.scaledArcadeFont(TS), x, y);
    }

    @Override
    protected void drawDebugInfo(GameRenderer renderer) {
        renderer.drawTileGrid(size());
        renderer.ctx().setFill(Color.YELLOW);
        renderer.ctx().setFont(Font.font("Sans", FontWeight.BOLD, 24));
        renderer.ctx().fillText(String.format("%s %d", context.gameState(), context.gameState().timer().currentTick()), 0, 64);
        renderer.ctx().setFill(Color.GREEN);
        renderer.ctx().fillRect(0, 0, renderer.canvas().getWidth(), 2);
    }

    @Override
    public void onSceneVariantSwitch(GameScene oldScene) {
        Logger.info("{} entered from {}", this, oldScene);
        //TODO check this
        context.updateRenderer();
    }

    @Override
    public void onEnterGameState(GameState state) {
        switch (state) {
            //TOD check this. GameState can publish an event to stop all sounds.
            case STARTING_GAME, LEVEL_COMPLETE, PACMAN_DYING -> context.sound().stopAll();
            case GAME_OVER -> {
                context.sound().stopAll();
                GameWorld world = context.game().world();
                double houseCenterX = TS * (world.houseTopLeftTile().x() + 0.5 * world.houseSize().x());
                gameOverMessageAnimation.start(houseCenterX, size().x(), MESSAGE_SPEED);
            }
            default -> {}
        }
        if (state == GameState.STARTING_GAME) {
            initCamDelay(30);
        }
    }

    @Override
    public void onBonusEaten(GameEvent e) {
        context.sound().playBonusEatenSound();
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
    public void onLevelStarted(GameEvent e) {
        context.updateRenderer();
        initCamDelay(90);
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
        if (context.gameState() == GameState.HUNTING && !context.game().powerTimer().isRunning()) {
            HuntingControl huntingControl = context.game().huntingControl();
            int sirenNumber = 1 + huntingControl.phaseIndex() / 2; // TODO check how this works in original game
            sounds.selectSiren(sirenNumber);
            sounds.playSiren();
        }
        if (context.game().pac().starvingTicks() > 8) { // TODO not sure how to do this right
            sounds.stopMunchingSound();
        }
        boolean ghostsReturning = context.game().ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).anyMatch(Ghost::isVisible);
        if (context.game().pac().isAlive() && ghostsReturning) {
            sounds.playGhostReturningHomeSound();
        } else {
            sounds.stopGhostReturningHomeSound();
        }
    }
}