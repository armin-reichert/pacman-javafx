/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.GameAction;
import de.amr.games.pacman.ui2d.GameAssets2D;
import de.amr.games.pacman.ui2d.GlobalGameActions2D;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.scene.common.CameraControlledGameScene;
import de.amr.games.pacman.ui2d.scene.common.GameScene;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.tinylog.Logger;

import java.util.List;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.GameModel.*;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.*;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameSceneConfiguration.*;
import static de.amr.games.pacman.ui2d.util.Ufx.coloredBackground;

/**
 * Tengen play scene, uses vertical scrolling.
 *
 * @author Armin Reichert
 */
public class TengenPlayScene2D extends GameScene2D implements CameraControlledGameScene {

    static final double RADIUS_FACTOR = 0.4;

    private final List<GameAction> actions = List.of(
        GlobalGameActions2D.CHEAT_EAT_ALL,
        GlobalGameActions2D.CHEAT_ADD_LIVES,
        GlobalGameActions2D.CHEAT_NEXT_LEVEL,
        GlobalGameActions2D.CHEAT_KILL_GHOSTS,
        GlobalGameActions2D.TENGEN_TOGGLE_PAC_BOOSTER,
        GlobalGameActions2D.TENGEN_QUIT_PLAY_SCENE
    );

    private final SubScene fxSubScene;
    private final ParallelCamera cam = new ParallelCamera();
    private final Canvas canvas = new Canvas(NES_SCREEN_WIDTH, NES_SCREEN_HEIGHT);
    private int camDelay;

    public TengenPlayScene2D() {
        Pane root = new StackPane(canvas);
        root.setBackground(coloredBackground(Color.TRANSPARENT));
        StackPane.setAlignment(canvas, Pos.CENTER);
        fxSubScene = new SubScene(root, 42, 42, true, SceneAntialiasing.BALANCED);
        fxSubScene.setCamera(cam);
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
    public void init() {
        canvas.widthProperty().bind(Bindings.createDoubleBinding(() -> scaled(size().x()), scalingProperty()));
        canvas.heightProperty().bind(Bindings.createDoubleBinding(() -> scaled(size().y()), scalingProperty()));
    }

    @Override
    public void end() {
        context.sounds().stopAll();
        //((TengenMsPacManGame) context.game()).setCanStartGame(true);
    }

    @Override
    public void update() {
        if (context.game().currentLevelNumber() == 0) {
            Logger.warn("Cannot update PlayScene2D: no game level available");
            return;
        }
        context.setScoreVisible(true);
        if (context.game().isDemoLevel()) {
            context.game().pac().setUseAutopilot(true);
            context.game().pac().setImmune(false);
        } else {
            context.game().pac().setUseAutopilot(PY_AUTOPILOT.get());
            context.game().pac().setImmune(PY_IMMUNITY.get());
            updatePlaySceneSound();
        }
        updateCamera();
    }

    private double cameraRadius() {
        return 0.5 * scaled(size().y());
    }

    private void updateCamera() {
        GameWorld world = context.game().world();
        Pac msPacMan = context.game().pac();
        if (world == null || msPacMan == null) {
            return;
        }
        if (camDelay > 0) {
            --camDelay;
            return;
        }
        double pacPositionY = scaled(msPacMan.posY());
        double radius = cameraRadius();
        double y = lerp(cam.getTranslateY(), pacPositionY - radius , 0.02);
        y = clamp(y, -RADIUS_FACTOR * radius, RADIUS_FACTOR * radius);
        cam.setTranslateY(y);
    }

    private void initCamDelay(int ticks) {
        camDelay = ticks;
        cam.setTranslateY(-RADIUS_FACTOR * cameraRadius());
    }

    @Override
    public void handleInput() {
        context.doFirstCalledAction(actions);
    }

    @Override
    public Vector2f size() {
        Vector2i worldSizeInTiles = context.worldSizeInTiles(context.game().world(), new Vector2i(NES_TILES_X, NES_TILES_Y));
        return worldSizeInTiles.plus(0, 2).scaled(TS).toVector2f(); // maybe change all maps to have 4 empty rows under maze?
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
            drawCameraPosition(renderer);
        }
    }

    private void drawCameraPosition(GameRenderer renderer) {
        Color color = Color.YELLOW;
        Font font = Font.font("Sans", FontWeight.BLACK, (int)scaled(6));
        String text = "Cam Y: %.2f (radius=%.0f)".formatted(cam.getTranslateY(), cameraRadius());
        renderer.drawText(text, color, font, 0, 30);
        renderer.drawText(text, color, font, 0, 0.5 * size().y());
        renderer.drawText(text, color, font, 0, size().y() - 30);
    }

    @Override
    protected void drawSceneContent(GameRenderer renderer) {
        if (context.game().world() == null) { // This happens on level start
            Logger.warn("Cannot draw scene content, game world not yet available!");
            return;
        }

        // Draw level message centered under ghost house
        Vector2i houseTopLeftTile = context.game().world().houseTopLeftTile();
        Vector2i houseSize        = context.game().world().houseSize();
        int cx = houseTopLeftTile.x() + houseSize.x() / 2;
        int y = TS * (houseTopLeftTile.y() + houseSize.y() + 1);
        drawLevelMessage(renderer, cx, y); // READY, GAME_OVER etc.

        boolean flashMode = Boolean.TRUE.equals(context.gameState().getProperty("mazeFlashing"));
        renderer.setFlashMode(flashMode);
        renderer.setBlinkingOn(context.game().blinking().isOn());
        renderer.drawWorld(context, context.game().world());

        renderer.drawAnimatedEntity(context.game().pac());
        ghostsInZOrder().forEach(renderer::drawAnimatedEntity);

        // Debug mode info
        if (debugInfoPy.get()) {
            renderer.drawAnimatedCreatureInfo(context.game().pac());
            ghostsInZOrder().forEach(renderer::drawAnimatedCreatureInfo);
        }

        int livesCounterEntries = context.game().lives() - 1;
        if (context.gameState() == GameState.STARTING_GAME && !context.game().pac().isVisible()) {
            // as long as Pac-Man is invisible when the game is started, one entry more appears in the lives counter
            livesCounterEntries += 1;
        }
        renderer.drawLivesCounter(livesCounterEntries, 5, size());
        renderer.drawLevelCounter(context.game().currentLevelNumber(), context.game().isDemoLevel(), context.game().levelCounter(), size());
    }

    private Stream<Ghost> ghostsInZOrder() {
        return Stream.of(ORANGE_GHOST, CYAN_GHOST, PINK_GHOST, RED_GHOST).map(context.game()::ghost);
    }

    private void drawLevelMessage(GameRenderer renderer, int cx, int y) {
        String assetPrefix = GameAssets2D.assetPrefix(context.gameVariant());
        if (context.game().isDemoLevel()) {
            Color color = Color.web(context.game().currentMapColorScheme().stroke());
            drawText(renderer, "GAME  OVER", cx, y, color);
        } else if (context.gameState() == GameState.GAME_OVER) {
            Color color = context.assets().color(assetPrefix + ".color.game_over_message");
            drawText(renderer, "GAME  OVER", cx, y, color);
        } else if (context.gameState() == GameState.STARTING_GAME) {
            Color color = context.assets().color(assetPrefix + ".color.ready_message");
            drawText(renderer, "READY!", cx, y, color);
        } else if (context.gameState() == GameState.TESTING_LEVEL_BONI) {
            drawText(renderer, "TEST    L%02d".formatted(context.game().currentLevelNumber()), cx, y, GameAssets2D.ARCADE_PALE);
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
    public void onGameStateEntry(GameState state) {
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
    public void onLevelCreated(GameEvent e) {
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
        if (context.gameState() == GameState.HUNTING && !context.game().powerTimer().isRunning()) {
            int sirenNumber = 1 + context.game().huntingControl().phaseIndex() / 2;
            context.sounds().selectSiren(sirenNumber);
            context.sounds().playSiren();
        }
        if (context.game().pac().starvingTicks() > 8) { // TODO not sure how to do this right
            context.sounds().stopMunchingSound();
        }
        boolean ghostsReturning = context.game().ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).anyMatch(Ghost::isVisible);
        if (context.game().pac().isAlive() && ghostsReturning) {
            context.sounds().playGhostReturningHomeSound();
        } else {
            context.sounds().stopGhostReturningHomeSound();
        }
    }
}
