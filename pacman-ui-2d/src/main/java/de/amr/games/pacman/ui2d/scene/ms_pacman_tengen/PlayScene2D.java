/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.GameAction;
import de.amr.games.pacman.ui2d.GameAssets2D;
import de.amr.games.pacman.ui2d.GlobalGameActions2D;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.scene.common.GameScene;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui2d.scene.common.ScrollableGameScene;
import de.amr.games.pacman.ui2d.util.Ufx;
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
import static de.amr.games.pacman.ui2d.PacManGames2dApp.PY_AUTOPILOT;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.PY_IMMUNITY;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameSceneConfiguration.NES_SCREEN_HEIGHT;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameSceneConfiguration.NES_SCREEN_WIDTH;

/**
 * Tengen play scene, uses vertical scrolling.
 *
 * @author Armin Reichert
 */
public class PlayScene2D extends GameScene2D implements ScrollableGameScene {

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

    public PlayScene2D() {
        Pane root = new StackPane();
        root.setBackground(Ufx.coloredBackground(Color.BLACK));

        root.getChildren().add(canvas);
        StackPane.setAlignment(canvas, Pos.CENTER);

        fxSubScene = new SubScene(root, 42, 42, true, SceneAntialiasing.BALANCED);
        fxSubScene.setCamera(cam);
    }

    @Override
    public DoubleProperty scrollAreaWidthProperty() {
        return fxSubScene.widthProperty();
    }

    @Override
    public DoubleProperty scrollAreaHeightProperty() {
        return fxSubScene.heightProperty();
    }

    @Override
    public Node scrollArea() {
        return fxSubScene;
    }

    @Override
    public Camera camera() {
        return fxSubScene.getCamera();
    }

    @Override
    public void init() {
        camMaxY = Integer.MIN_VALUE;
        camMinY = Integer.MAX_VALUE;
    }

    @Override
    public void end() {
        context.sounds().stopAll();
        //((TengenMsPacManGame) context.game()).setCanStartGame(true);
    }

    @Override
    public void update() {
        if (context.game().levelNumber() == 0) {
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

        Pac msPacMan = context.game().pac();
        double halfWorldHeight = 0.5 * context.worldSizeTilesOrDefault().y() * TS;
        double targetCameraY = scaling() * (msPacMan.posY() - halfWorldHeight);
        double y = lerp(cam.getTranslateY(), targetCameraY, 0.015);
        double limiter = 0.45 * scaling(); // dependent on aspect of world?
        y = clamp(y, -halfWorldHeight * limiter, halfWorldHeight * limiter);
        cam.setTranslateY(y);

        if ((int)cam.getTranslateY() > camMaxY) {
            camMaxY = (int)cam.getTranslateY();
            Logger.info("camera max: {}", camMaxY);
        }
        if ((int)cam.getTranslateY() < camMinY) {
            camMinY = (int)cam.getTranslateY();
            Logger.info("camera min: {}", camMinY);
        }
    }

    int camMaxY, camMinY;

    @Override
    public void handleInput() {
        context.doFirstCalledAction(actions);
    }

    @Override
    public Vector2f size() {
        return new Vector2f(NES_SCREEN_WIDTH, NES_SCREEN_HEIGHT);
    }

    @Override
    public void draw(GameRenderer renderer) {
        renderer.scalingProperty().set(scaling());

        Vector2f mapSize = context.worldSizeTilesOrDefault().toVector2f().scaled(TS);

        Vector2f canvasSize = mapSize.plus(0, 2*TS).scaled((float)scaling());
        canvas.setWidth(canvasSize.x());
        canvas.setHeight(canvasSize.y());

        renderer.setCanvas(canvas);
        renderer.scalingProperty().set(scaling());
        renderer.setBackgroundColor(backgroundColorPy.get());
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
        if (context.game().world() == null) { // This happens on level start
            Logger.warn("Cannot draw scene content, game world not yet available!");
            return;
        }
        drawLevelMessage(renderer); // READY, GAME_OVER etc.

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

        Vector2f mapSize = context.worldSizeTilesOrDefault().toVector2f().scaled(TS);
        //TODO: this code is ugly
        int numLivesShown = context.game().lives() - 1;
        if (context.gameState() == GameState.READY && !context.game().pac().isVisible()) {
            numLivesShown += 1;
        }
        renderer.drawLivesCounter(numLivesShown, 5, mapSize);
        renderer.drawLevelCounter(context.game().levelNumber(), context.game().isDemoLevel(),
            context.game().levelCounter(), mapSize);
    }

    private Stream<Ghost> ghostsInZOrder() {
        return Stream.of(GameModel.ORANGE_GHOST, GameModel.CYAN_GHOST, GameModel.PINK_GHOST, GameModel.RED_GHOST)
            .map(context.game()::ghost);
    }

    private void drawLevelMessage(GameRenderer renderer) {
        Vector2i houseTopLeftTile = context.game().world().houseTopLeftTile();
        Vector2i houseSize        = context.game().world().houseSize();
        int cx = houseTopLeftTile.x() + houseSize.x() / 2;
        int y = TS * (houseTopLeftTile.y() + houseSize.y() + 1);
        String assetPrefix = GameAssets2D.assetPrefix(context.gameVariant());
        Font font = renderer.scaledArcadeFont(TS);
        if (context.game().isDemoLevel()) {
            String text = "GAME  OVER";
            int x = TS * (cx - text.length() / 2);
            Color color = Color.web(context.game().world().map().colorSchemeOrDefault().stroke());
            renderer.drawText(text, color, font, x, y);
        } else if (context.gameState() == GameState.GAME_OVER) {
            String text = "GAME  OVER";
            int x = TS * (cx - text.length() / 2);
            Color color = context.assets().color(assetPrefix + ".color.game_over_message");
            renderer.drawText(text, color, font, x, y);
        } else if (context.gameState() == GameState.READY) {
            String text = "READY!";
            int x = TS * (cx - text.length() / 2);
            Color color = context.assets().color(assetPrefix + ".color.ready_message");
            renderer.drawText(text, color, font, x, y);
        } else if (context.gameState() == GameState.TESTING_LEVEL_BONI) {
            String text = "TEST    L%02d".formatted(context.game().levelNumber());
            int x = TS * (cx - text.length() / 2);
            renderer.drawText(text, GameAssets2D.ARCADE_PALE, font, x, y);
        }
    }

    @Override
    protected void drawDebugInfo(GameRenderer renderer) {
        renderer.drawTileGrid(size());
        renderer.ctx().setFill(Color.YELLOW);
        renderer.ctx().setFont(Font.font("Sans", FontWeight.BOLD, 24));
        renderer.ctx().fillText(String.format("%s %d", context.gameState(), context.gameState().timer().currentTick()), 0, 64);
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
            case READY, LEVEL_COMPLETE, PACMAN_DYING -> context.sounds().stopAll();
            case GAME_OVER -> {
                context.sounds().stopAll();
                context.sounds().playGameOverSound();
            }
            default -> {}
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
