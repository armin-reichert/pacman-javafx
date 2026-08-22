/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.entities.House;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.rules.HuntingTimerStrategy;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui.gamescene.d2.Rendering2DSupport;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static java.util.Objects.requireNonNull;

public class Arcade_PlayScene2D_DebugInfo_Renderer extends BaseDebugInfoRenderer {

    private static final List<Direction> CLOCK_ORDER = List.of(Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT);

    private final ActorSpriteAnimController animSystem;
    private final List<GameEntity> actorsInZOrder = new ArrayList<>();

    public Arcade_PlayScene2D_DebugInfo_Renderer(ActorSpriteAnimController animSystem, Canvas canvas) {
        super(canvas);
        this.animSystem = requireNonNull(animSystem);
    }

    public ActorSpriteAnimController animSystem() {
        return animSystem;
    }

    @Override
    public void draw(GameScene gameScene, long tick) {
        final Rendering2DSupport r2D = gameScene.componentsRegistry().reqComp(Rendering2DSupport.class);

        drawTileGrid(r2D.unscaledWidth(), r2D.unscaledHeight(), Color.LIGHTGRAY);

        final GameSession session = gameScene.game().session();
        session.optLevel().ifPresent(level -> {
            // We assume all ghosts have the same set of special terrain tiles
            level.entities().ghost(GhostPersonality.RED_GHOST_SHADOW).worldInfo().specialTerrainTiles().forEach(tile -> {
                final double x = scaled(tile.x() * WorldMap.TS);
                final double y = scaled(tile.y() * WorldMap.TS + WorldMap.HTS), size = scaled(WorldMap.TS);
                ctx.setFill(Color.RED);
                ctx.fillRect(x, y, size, 2);
            });

            // Mark intersection tiles
            final TerrainLayer terrain = level.worldMap().terrainLayer();
            final House house = level.entities().house();
            terrain.tiles()
                .filter(tile -> tile.y() >= terrain.emptyRowsOverMaze())
                .filter(tile -> tile.y() < terrain.numRows() - terrain.emptyRowsBelowMaze())
                .filter(tile -> terrain.isRealIntersectionTile(tile, house::contains))
                .forEach(tile -> {
                    final double cx = tile.x() * WorldMap.TS + WorldMap.HTS;
                    final double cy = tile.y() * WorldMap.TS + WorldMap.HTS;
                    for (Direction dir : CLOCK_ORDER) {
                        if (!terrain.isInaccessibleTerrainTile(tile.plus(dir.vector()))) {
                            final double x = cx + dir.vector().x() * WorldMap.HTS;
                            final double y = cy + dir.vector().y() * WorldMap.HTS;
                            ctx.setStroke(Color.WHITE);
                            ctx.setLineWidth(2);
                            ctx.strokeLine(scaled(cx), scaled(cy), scaled(x), scaled(y));
                        }
                    }
            });

            final GameState state = gameScene.game().state();
            final String gameStateText = state.name() + " (Tick %d)".formatted(state.timer().tickCount());
            String huntingPhaseText = "";
            if (CommonGameStateID.GAME_LEVEL_PLAYING.hasSameNameAs(state)) {
                final HuntingTimerStrategy huntingRules = level.huntingTimerStrategy();
                huntingPhaseText = " %s (Tick %d)".formatted(huntingRules.currentHuntingPhase(), huntingRules.tickCount());
            }
            ctx.setFill(debugTextFill);
            ctx.setStroke(debugTextStroke);
            ctx.setFont(debugTextFont);
            ctx.fillText("%s%s".formatted(gameStateText, huntingPhaseText), 0, tilesPx(8));

            updateActorDrawingOrder(level);
            actorsInZOrder.forEach(actor -> drawMovingActorInfo(animSystem, actor));
        });
    }

    private void updateActorDrawingOrder(GameLevel level) {
        actorsInZOrder.clear();
        level.entities().optBonus().ifPresent(actorsInZOrder::add);
        actorsInZOrder.add(level.entities().pac());
        Stream.of(
            GhostPersonality.ORANGE_GHOST_POKEY,
            GhostPersonality.CYAN_GHOST_BASHFUL,
            GhostPersonality.PINK_GHOST_SPEEDY,
            GhostPersonality.RED_GHOST_SHADOW
            ).map(level.entities()::ghost).forEach(actorsInZOrder::add);
    }
}