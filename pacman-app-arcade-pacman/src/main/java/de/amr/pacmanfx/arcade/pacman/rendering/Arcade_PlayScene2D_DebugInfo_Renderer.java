/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.basics.fsm.State;
import de.amr.basics.math.Direction;
import de.amr.pacmanfx.arcade.pacman.flow.Arcade_GameState;
import de.amr.pacmanfx.model.HuntingTimer;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.MovingActor;
import de.amr.pacmanfx.model.world.TerrainLayer;
import de.amr.pacmanfx.ui.d2.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static de.amr.pacmanfx.core.Globals.*;

public class Arcade_PlayScene2D_DebugInfo_Renderer extends BaseDebugInfoRenderer {

    private static final List<Direction> CLOCK_ORDER = List.of(Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT);

    private final List<Actor> actorsInZOrder = new ArrayList<>();

    public Arcade_PlayScene2D_DebugInfo_Renderer(Canvas canvas) {
        super(canvas);
    }

    @Override
    public void draw(GameScene2D scene) {
        drawTileGrid(scene.getUnscaledWidth(), scene.getUnscaledHeight(), Color.LIGHTGRAY);

        scene.context().optCurrentGameLevel().ifPresent(level -> {
            // We assume all ghosts have the same set of special terrain tiles
            level.ghost(RED_GHOST_SHADOW).specialTerrainTiles().forEach(tile -> {
                final double x = scaled(tile.x() * TS);
                final double y = scaled(tile.y() * TS + HTS), size = scaled(TS);
                ctx.setFill(Color.RED);
                ctx.fillRect(x, y, size, 2);
            });

            // Mark intersection tiles
            final TerrainLayer terrain = level.worldMap().terrainLayer();
            terrain.tiles()
                .filter(tile -> tile.y() >= terrain.emptyRowsOverMaze())
                .filter(tile -> tile.y() < terrain.numRows() - terrain.emptyRowsBelowMaze())
                .filter(terrain::isIntersection)
                .forEach(tile -> {
                    final double cx = tile.x() * TS + HTS;
                    final double cy = tile.y() * TS + HTS;
                    for (Direction dir : CLOCK_ORDER) {
                        if (!terrain.isTileBlocked(tile.plus(dir.vector()))) {
                            final double x = cx + dir.vector().x() * HTS;
                            final double y = cy + dir.vector().y() * HTS;
                            ctx.setStroke(Color.WHITE);
                            ctx.setLineWidth(2);
                            ctx.strokeLine(scaled(cx), scaled(cy), scaled(x), scaled(y));
                        }
                    }
            });

            final State<GameModel> state = scene.context().currentGameState();
            final String gameStateText = state.name() + " (Tick %d)".formatted(state.timer().tickCount());
            String huntingPhaseText = "";
            if (state == Arcade_GameState.GAME_LEVEL_PLAYING.state()) {
                final HuntingTimer huntingTimer = level.huntingTimer();
                huntingPhaseText = " %s (Tick %d)".formatted(huntingTimer.phase(), huntingTimer.tickCount());
            }
            ctx.setFill(debugTextFill);
            ctx.setStroke(debugTextStroke);
            ctx.setFont(debugTextFont);
            ctx.fillText("%s%s".formatted(gameStateText, huntingPhaseText), 0, TS(8));

            updateActorDrawingOrder(level);
            actorsInZOrder.stream()
                .filter(MovingActor.class::isInstance)
                .map(MovingActor.class::cast)
                .forEach(this::drawMovingActorInfo);
        });
    }

    private void updateActorDrawingOrder(GameLevel level) {
        actorsInZOrder.clear();
        level.optBonus().ifPresent(actorsInZOrder::add);
        actorsInZOrder.add(level.entities().pac());
        Stream.of(ORANGE_GHOST_POKEY, CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, RED_GHOST_SHADOW)
            .map(level::ghost)
            .forEach(actorsInZOrder::add);
    }
}