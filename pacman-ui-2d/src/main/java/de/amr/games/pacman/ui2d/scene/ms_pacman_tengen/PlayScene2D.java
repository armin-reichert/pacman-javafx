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
import de.amr.games.pacman.ui2d.GameAction;
import de.amr.games.pacman.ui2d.GameAssets2D;
import de.amr.games.pacman.ui2d.GlobalGameActions2D;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.scene.common.GameScene;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui2d.scene.common.ScrollableGameScene2D;
import de.amr.games.pacman.ui2d.util.Ufx;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.tinylog.Logger;

import java.util.List;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.PY_AUTOPILOT;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.PY_IMMUNITY;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameSceneConfiguration.*;

/**
 * @author Armin Reichert
 */
public class PlayScene2D extends GameScene2D implements ScrollableGameScene2D {

    private final List<GameAction> actions = List.of(
        GlobalGameActions2D.CHEAT_EAT_ALL,
        GlobalGameActions2D.CHEAT_ADD_LIVES,
        GlobalGameActions2D.CHEAT_NEXT_LEVEL,
        GlobalGameActions2D.CHEAT_KILL_GHOSTS,
        GlobalGameActions2D.TENGEN_TOGGLE_PAC_BOOSTER,
        GlobalGameActions2D.TENGEN_QUIT_PLAY_SCENE
    );

    private final DoubleProperty availableWidthPy = new SimpleDoubleProperty(224);
    private final DoubleProperty availableHeightPy = new SimpleDoubleProperty(288);

    private final Canvas canvas = new Canvas(NES_SCREEN_WIDTH, NES_SCREEN_HEIGHT);
    private final StackPane root = new StackPane();

    public PlayScene2D() {

        //TODO to see what's going on
        //root.setBackground(Ufx.coloredBackground(Color.BLUE));
        root.setBackground(Ufx.coloredBackground(Color.BLACK));

        canvas.heightProperty().bind(availableHeightPy.multiply(1));
        canvas.widthProperty().bind(canvas.heightProperty().multiply(NES_ASPECT));

        root.getChildren().add(canvas);
        StackPane.setAlignment(canvas, Pos.CENTER);

        SubScene fxSubScene = new SubScene(root, 42, 42, true, SceneAntialiasing.BALANCED);
        fxSubScene.widthProperty().bind(availableWidthPy);
        fxSubScene.heightProperty().bind(availableHeightPy);
    }

    @Override
    public DoubleProperty availableHeightProperty() {
        return availableHeightPy;
    }

    @Override
    public DoubleProperty availableWidthProperty() {
        return availableWidthPy;
    }

    public Parent root() {
        return root;
    }

    @Override
    public Canvas canvas() {
        return canvas;
    }

    @Override
    public void init() {
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

    @Override
    public void handleInput() {
        context.doFirstCalledAction(actions);
    }

    @Override
    public void draw(GameRenderer renderer, Vector2f sceneSize) {
        //TODO use faked scene size until camera is available
        Vector2i worldSizeInTiles = context.worldSizeTilesOrDefault();
        sceneSize = worldSizeInTiles.plus(0, 2).scaled(TS).toVector2f();
        //scalingPy.set(availableHeightPy.get() / NES_SCREEN_HEIGHT);
        scalingPy.set(availableHeightPy.get() / (sceneSize.y()));
        renderer.setCanvas(canvas);
        renderer.scalingProperty().set(scaling());
        renderer.setBackgroundColor(backgroundColorPy.get());
        renderer.clearCanvas();

        double dx = 0.5 * (canvas.getWidth() - worldSizeInTiles.scaled(TS).x() * scaling());
        renderer.ctx().save();
        renderer.ctx().translate(dx, 0);
        if (context.isScoreVisible()) {
            renderer.drawScores(context);
        }
        drawSceneContent(renderer, sceneSize);
        if (debugInfoPy.get()) {
            drawDebugInfo(renderer, sceneSize);
        }
        renderer.ctx().restore();

    }

    @Override
    protected void drawSceneContent(GameRenderer renderer, Vector2f size) {
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

        //TODO: this code looks ugly
        int numLivesShown = context.game().lives() - 1;
        if (context.gameState() == GameState.READY && !context.game().pac().isVisible()) {
            numLivesShown += 1;
        }
        renderer.drawLivesCounter(numLivesShown, 5, size);
        renderer.drawLevelCounter(context.game().levelNumber(), context.game().isDemoLevel(),
            context.game().levelCounter(), size);
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
            String text = "TEST    L%03d".formatted(context.game().levelNumber());
            int x = TS * (cx - text.length() / 2);
            renderer.drawText(text, GameAssets2D.ARCADE_PALE, font, x, y);
        }
    }

    @Override
    protected void drawDebugInfo(GameRenderer renderer, Vector2f sceneSize) {
        renderer.drawTileGrid(sceneSize);
        renderer.ctx().setFill(Color.YELLOW);
        renderer.ctx().setFont(Font.font("Sans", FontWeight.BOLD, 24));
        renderer.ctx().fillText(String.format("%s %d", context.gameState(), context.gameState().timer().currentTick()), 0, 64);
    }

    @Override
    public void onSceneVariantSwitch(GameScene oldScene) {
        //TODO check this
        context.currentGameSceneConfig().renderer().setRendererFor(context.game());
        Logger.info("{} entered from {}", this, oldScene);
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
        context.currentGameSceneConfig().renderer().setRendererFor(context.game());
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
}