/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.arcade.pacman.model.Arcade_GameController;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_PlayScene2D;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.worldmap.TerrainLayer;
import de.amr.pacmanfx.model.GameControl;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.AbstractHuntingTimer;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.MovingActor;
import de.amr.pacmanfx.ui._2d.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;

public class Arcade_PlayScene2D_DebugInfo_Renderer extends BaseDebugInfoRenderer {

    private static final List<Direction> CLOCK_ORDER = List.of(Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT);

    private final List<Actor> actorsInZOrder = new ArrayList<>();

    public Arcade_PlayScene2D_DebugInfo_Renderer(GameScene2D scene, Canvas canvas) {
        super(scene.ui(), canvas);
    }

    @Override
    public void draw(GameScene2D scene) {
        final Arcade_PlayScene2D playScene = (Arcade_PlayScene2D) scene;
        final GameContext gameContext = playScene.context();
        final GameControl gameControl = gameContext.currentGame().control();

        drawTileGrid(playScene.unscaledSize().x(), playScene.unscaledSize().y(), Color.LIGHTGRAY);

        if (gameContext.currentGame().optGameLevel().isPresent()) {
            final GameLevel gameLevel = gameContext.currentGame().level();

            // assuming all ghosts have the same set of special terrain tiles
            gameLevel.ghost(RED_GHOST_SHADOW).specialTerrainTiles().forEach(tile -> {
                double x = scaled(tile.x() * TS), y = scaled(tile.y() * TS + HTS), size = scaled(TS);
                ctx.setFill(Color.RED);
                ctx.fillRect(x, y, size, 2);
            });

            // mark intersection tiles
            TerrainLayer terrainLayer = gameLevel.worldMap().terrainLayer();
            terrainLayer.tiles().filter(terrainLayer::isIntersection).forEach(tile -> {
                double cx = tile.x() * TS + HTS, cy = tile.y() * TS + HTS;
                for (Direction dir : CLOCK_ORDER) {
                    if (!terrainLayer.isTileBlocked(tile.plus(dir.vector()))) {
                        double x = cx + dir.vector().x() * HTS;
                        double y = cy + dir.vector().y() * HTS;
                        ctx.setFill(Color.gray(0.6));
                        ctx.setLineWidth(1);
                        ctx.strokeLine(scaled(cx), scaled(cy), scaled(x), scaled(y));
                    }
                }
            });

            String gameStateText = gameControl.state().name() + " (Tick %d)".formatted(gameControl.state().timer().tickCount());
            String huntingPhaseText = "";
            if (gameControl.state() == Arcade_GameController.GameState.HUNTING) {
                AbstractHuntingTimer huntingTimer = gameLevel.huntingTimer();
                huntingPhaseText = " %s (Tick %d)".formatted(huntingTimer.phase(), huntingTimer.tickCount());
            }
            ctx.setFill(debugTextFill);
            ctx.setStroke(debugTextStroke);
            ctx.setFont(debugTextFont);
            ctx.fillText("%s%s".formatted(gameStateText, huntingPhaseText), 0, TS(8));

            updateActorDrawingOrder(gameLevel);
            actorsInZOrder.stream()
                .filter(MovingActor.class::isInstance)
                .map(MovingActor.class::cast)
                .forEach(this::drawMovingActorInfo);
        }
    }

    private void updateActorDrawingOrder(GameLevel gameLevel) {
        actorsInZOrder.clear();
        gameLevel.optBonus().ifPresent(actorsInZOrder::add);
        actorsInZOrder.add(gameLevel.pac());
        Stream.of(ORANGE_GHOST_POKEY, CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, RED_GHOST_SHADOW)
            .map(gameLevel::ghost)
            .forEach(actorsInZOrder::add);
    }
}