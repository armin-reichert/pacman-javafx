/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_PlayScene2D;
import de.amr.pacmanfx.controller.GamePlayState;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.model.HuntingTimer;
import de.amr.pacmanfx.model.actors.MovingActor;
import de.amr.pacmanfx.ui._2d.DefaultDebugInfoRenderer;
import de.amr.pacmanfx.ui.api.GameUI;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import java.util.List;

import static de.amr.pacmanfx.Globals.*;

public class Arcade_PlayScene2DDebugInfoRenderer extends DefaultDebugInfoRenderer {

    private static final List<Direction> CLOCK_ORDER = List.of(Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT);

    private final Arcade_PlayScene2D scene2D;

    public Arcade_PlayScene2DDebugInfoRenderer(GameUI ui, Arcade_PlayScene2D scene2D, Canvas canvas) {
        super(ui, canvas);
        this.scene2D = scene2D;
    }

    @Override
    public void drawDebugInfo() {
        GameContext gameContext = scene2D.context();
        drawTileGrid(scene2D.sizeInPx().x(), scene2D.sizeInPx().y(), Color.LIGHTGRAY);
        if (gameContext.optGameLevel().isPresent()) {
            // assuming all ghosts have the same set of special terrain tiles
            gameContext.gameLevel().ghost(RED_GHOST_SHADOW).specialTerrainTiles().forEach(tile -> {
                double x = scaled(tile.x() * TS), y = scaled(tile.y() * TS + HTS), size = scaled(TS);
                ctx.setFill(Color.RED);
                ctx.fillRect(x, y, size, 2);
            });

            // mark intersection tiles
            gameContext.gameLevel().tiles().filter(gameContext.gameLevel()::isIntersection).forEach(tile -> {
                double cx = tile.x() * TS + HTS, cy = tile.y() * TS + HTS;
                for (Direction dir : CLOCK_ORDER) {
                    if (!gameContext.gameLevel().isTileBlocked(tile.plus(dir.vector()))) {
                        double x = cx + dir.vector().x() * HTS;
                        double y = cy + dir.vector().y() * HTS;
                        ctx.setFill(Color.gray(0.6));
                        ctx.setLineWidth(1);
                        ctx.strokeLine(scaled(cx), scaled(cy), scaled(x), scaled(y));
                    }
                }
            });

            String gameStateText = gameContext.gameState().name() + " (Tick %d)".formatted(gameContext.gameState().timer().tickCount());
            String huntingPhaseText = "";
            if (gameContext.gameState() == GamePlayState.HUNTING) {
                HuntingTimer huntingTimer = gameContext.game().huntingTimer();
                huntingPhaseText = " %s (Tick %d)".formatted(huntingTimer.phase(), huntingTimer.tickCount());
            }
            ctx.setFill(debugTextFill);
            ctx.setStroke(debugTextStroke);
            ctx.setFont(debugTextFont);
            ctx.fillText("%s%s".formatted(gameStateText, huntingPhaseText), 0, TS(8));
        }
        scene2D.actorsInZOrder().stream()
            .filter(MovingActor.class::isInstance)
            .map(MovingActor.class::cast)
            .forEach(actor -> drawMovingActorInfo(ctx(), scaling(), actor));
    }
}
