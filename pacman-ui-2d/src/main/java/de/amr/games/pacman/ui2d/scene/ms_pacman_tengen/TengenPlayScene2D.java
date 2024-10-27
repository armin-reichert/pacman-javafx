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
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.GameActions2D;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.scene.common.CameraControlledGameScene;
import de.amr.games.pacman.ui2d.scene.common.GameScene;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui2d.sound.GameSounds;
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
import static de.amr.games.pacman.ui2d.util.Ufx.coloredBackground;

/**
 * Tengen play scene, uses vertical scrolling.
 *
 * @author Armin Reichert
 */
public class TengenPlayScene2D extends GameScene2D implements CameraControlledGameScene {

    private final SubScene fxSubScene;
    private final ParallelCamera cam = new ParallelCamera();
    private final Canvas canvas = new Canvas(NES_RESOLUTION_X, NES_RESOLUTION_Y);
    private int camDelay;

    public TengenPlayScene2D() {
        Pane root = new StackPane(canvas);
        root.setBackground(coloredBackground(Color.TRANSPARENT));
        StackPane.setAlignment(canvas, Pos.CENTER);
        fxSubScene = new SubScene(root, 42, 42);
        fxSubScene.setCamera(cam);
    }

    @Override
    public void bindActions() {
        bindAction(GameActions2D.CHEAT_EAT_ALL,              alt(KeyCode.E));
        bindAction(GameActions2D.CHEAT_ADD_LIVES,            alt(KeyCode.L));
        bindAction(GameActions2D.CHEAT_NEXT_LEVEL,           alt(KeyCode.N));
        bindAction(GameActions2D.CHEAT_KILL_GHOSTS,          alt(KeyCode.X));
        bindAction(GameActions2D.TENGEN_TOGGLE_PAC_BOOSTER,  KeyCode.A);
        bindAction(GameActions2D.TENGEN_SHOW_OPTIONS,        KeyCode.S);
    }

    @Override
    public void doInit() {
        context.setScoreVisible(true);
        canvas.widthProperty().bind(scalingProperty().map(scaling -> scaled(size().x())));
        canvas.heightProperty().bind(scalingProperty().map(scaling -> scaled(size().y())));
    }

    @Override
    protected void doEnd() {
        context.sounds().stopAll();
    }

    @Override
    public void update() {
        if (context.game().currentLevelNumber() == 0) {
            Logger.warn("Cannot update PlayScene2D: no game level available");
            return;
        }
        Pac msPacMan = context.game().pac();
        if (context.game().world() == null || msPacMan == null) {
            //TODO: Can world or Ms. Pac-Man be null here?
            Logger.warn("Cannot update PlayScene2D: no game world available");
            return;
        }
        if (context.game().isDemoLevel()) {
            msPacMan.setUseAutopilot(true);
            msPacMan.setImmune(false);
        } else {
            msPacMan.setUseAutopilot(PY_AUTOPILOT.get());
            msPacMan.setImmune(PY_IMMUNITY.get());
            updatePlaySceneSound();
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
        return 0.5 * scaled(size().y());
    }

    private double dontAskItsMagic(double radius) {
        double height = size().y();
        if (height >= 40 * TS) return 0.38 * radius;
        if (height >= 35 * TS) return 0.30 * radius;
        return 0.18 * radius;
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
        int cx = houseTopLeftTile.x() + houseSize.x() / 2;
        int y = TS * (houseTopLeftTile.y() + houseSize.y() + 1);
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
        renderer.drawLevelCounter(game.currentLevelNumber(), game.isDemoLevel(), game.levelCounter(), size());
    }

    private Stream<Ghost> ghostsInZOrder() {
        return Stream.of(ORANGE_GHOST, CYAN_GHOST, PINK_GHOST, RED_GHOST).map(context.game()::ghost);
    }

    private void drawLevelMessage(GameRenderer renderer, int cx, int y) {
        AssetStorage assets = context.assets();
        GameState state = context.gameState();
        GameModel game = context.game();
        if (game.isDemoLevel()) {
            drawText(renderer, "GAME  OVER", cx, y, Color.web(game.currentMapColorScheme().stroke()));
        } else if (state == GameState.GAME_OVER) {
            drawText(renderer, "GAME  OVER", cx, y, assets.color(assetPrefix(context.gameVariant()) + ".color.game_over_message"));
        } else if (state == GameState.STARTING_GAME) {
            drawText(renderer, "READY!", cx, y, assets.color(assetPrefix(context.gameVariant()) + ".color.ready_message"));
        } else if (state == GameState.TESTING_LEVEL_BONI) {
            drawText(renderer, "TEST L%02d".formatted(game.currentLevelNumber()), cx, y, TENGEN_YELLOW);
        }
    }

    private void drawText(GameRenderer renderer, String text, int cx, int y, Color color) {
        int x = TS * (cx - text.length() / 2);
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
            case STARTING_GAME, LEVEL_COMPLETE, PACMAN_DYING -> context.sounds().stopAll();
            case GAME_OVER -> {
                context.sounds().stopAll();
                context.sounds().playGameOverSound();
            }
            default -> {}
        }
        if (state == GameState.STARTING_GAME) {
            initCamDelay(30);
        }
    }

    @Override
    public void onBonusEaten(GameEvent e) {
        context.sounds().playBonusEatenSound();
    }

    @Override
    public void onExtraLifeWon(GameEvent e) {
        context.sounds().playExtraLifeSound();
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        context.sounds().playGhostEatenSound();
    }

    @Override
    public void onLevelStarted(GameEvent e) {
        context.updateRenderer();
        initCamDelay(90);
    }

    @Override
    public void onPacDied(GameEvent e) {
        context.sounds().playPacDeathSound();
    }

    @Override
    public void onPacFoundFood(GameEvent e) {
        context.sounds().playMunchingSound();
    }

    @Override
    public void onPacGetsPower(GameEvent e) {
        context.sounds().stopSiren();
        context.sounds().playPacPowerSound();
    }

    @Override
    public void onPacLostPower(GameEvent e) {
        context.sounds().stopPacPowerSound();
    }

    private void updatePlaySceneSound() {
        GameSounds sounds = context.sounds();
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