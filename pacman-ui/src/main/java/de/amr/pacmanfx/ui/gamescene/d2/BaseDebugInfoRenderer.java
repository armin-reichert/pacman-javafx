/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.d2;

import de.amr.basics.Named;
import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2f;
import de.amr.basics.timer.TickTimer;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.SpriteAnimationComp;
import de.amr.pacmanfx.core.ecs.comp.WorldNavigationComp;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.ecs.systems.PositionSystem;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.House;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostAnimationComp;
import de.amr.pacmanfx.core.entities.pac.comp.PacAnimationComp;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.rules.HuntingTimer;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

public class BaseDebugInfoRenderer extends BaseRenderer implements GameScene2D_Renderer {

    record AnimationInfo(Named animationID, int frame, boolean stopped, boolean locked) {}

    private static final List<Direction> CLOCK_WISE = List.of(Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT);

    protected Color debugTextFill = Color.WHITE;
    protected Color debugTextStroke = Color.GRAY;
    protected Font debugTextFont = Font.font("Sans", 14.0f);

    private final List<GameEntity> actorsInZOrder = new ArrayList<>();
    private final ActorSpriteAnimController animController;
    private final Text dummy = new Text();

    public BaseDebugInfoRenderer(ActorSpriteAnimController animController, Canvas canvas) {
        super(canvas);
        this.animController = requireNonNull(animController);
    }

    private void updateActorZOrder(GameLevel level) {
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

    @Override
    public void draw(GameScene gameScene, long tick) {
        final GameSession session = gameScene.game().session();
        final CanvasRenderingComp r2D = gameScene.components().reqComp(CanvasRenderingComp.class);

        drawTileGrid(r2D.unscaledWidth(), r2D.unscaledHeight(), Color.LIGHTGRAY);
        drawGameStateInfo(gameScene.game());
        session.optLevel().ifPresent(level -> {
//            drawTerrainDebugInfo(level);
            updateActorZOrder(level);
            actorsInZOrder.forEach(actor -> drawMovingActorInfo(animController, actor));
        });
    }

    public void drawGameStateInfo(GameContext game) {
        final AbstractGameState gameState = game.state();
        String text = "Game State: '%s' (Tick %d of %s)".formatted(
            gameState.name(),
            gameState.timer().tickCount(),
            gameState.timer().durationTicks() == TickTimer.INDEFINITE ? "∞" : String.valueOf(gameState.timer().tickCount())
        );
        if (game.session().optLevel().isPresent() && CommonGameStateID.GAME_LEVEL_PLAYING.hasSameNameAs(gameState)) {
            final HuntingTimer ht = game.session().level().huntingTimer();
            text += " %s (Tick %d)".formatted(ht.currentHuntingPhase(), ht.tickCount());
        }
        ctx.setFill(debugTextFill);
        ctx.setFont(debugTextFont);
        ctx.setStroke(debugTextStroke);
        ctx.fillText(text, 0, scaled(24));
    }

    public void drawMovingActorInfo(ActorSpriteAnimController animController, GameEntity actor) {
        if (!actor.isVisible()) {
            return;
        }

        if (actor instanceof Pac pac) {
            String autopilot = pac.cheats().isUsingAutopilot() ? "autopilot" : "";
            String immune = pac.cheats().isImmune() ? "immune" : "";
            String text = "%s\n%s".formatted(autopilot, immune).trim();
            ctx.setFont(debugTextFont);
            ctx.setFill(debugTextFill);
            ctx.fillText(text, scaled(pac.pos().x() - 4), scaled(pac.pos().y() + 16));
        }

        actor.optComp(SpriteAnimationComp.class).ifPresent(_ -> {
            if (animController.selectedAnimationID(actor) != null) {
                drawAnimationInfo(animController, actor, bgColor(actor));
            }
        });

        actor.optComp(WorldNavigationComp.class).ifPresent(navigation -> drawDirectionIndicator(actor, navigation));

        final Rectangle2D boundingBox = PositionSystem.boundingBox(actor.pos().asVector2f());
        ctx.save();
        ctx.setStroke(Color.BLACK);
        ctx.setLineWidth(2);
        ctx.strokeRect(scaled(boundingBox.getMinX()), scaled(boundingBox.getMinY()), scaled(boundingBox.getWidth()), scaled(boundingBox.getHeight()));
        ctx.restore();
    }

    private Color bgColor(GameEntity actor) {
        return switch (actor) {
            case Pac _ -> Color.YELLOW;
            case Ghost ghost -> switch (ghost.personality()) {
                case RED_GHOST_SHADOW -> Color.RED;
                case PINK_GHOST_SPEEDY -> Color.PINK;
                case CYAN_GHOST_BASHFUL -> Color.CYAN;
                case ORANGE_GHOST_POKEY -> Color.ORANGE;
            };
            default -> Ufx.colorWithOpacity(Color.DARKBLUE, 0.8);
        };
    }

    private AnimationInfo animationInfo(Pac pac, ActorSpriteAnimController animController) {
        final PacAnimationComp animation = pac.animation();
        return new AnimationInfo(
            animation.animationID(),
            animController.currentFrame(pac),
            animation.isStopped(),
            animation.isLocked()
        );
    }

    private AnimationInfo animationInfo(Ghost ghost, ActorSpriteAnimController animController) {
        final GhostAnimationComp animation = ghost.animation();
        return new AnimationInfo(
            animation.animationID(),
            animController.currentFrame(ghost),
            animation.isStopped(),
            animation.isLocked()
        );
    }

    private String formatAnimationInfo(AnimationInfo info) {
        String id = info.animationID() != null ? info.animationID().name() : "";
        String stopped = info.stopped() ? "stopped" : "";
        String locked = info.locked() ? "locked" : "";
        return "%s:%d %s %s".formatted(id, info.frame, stopped, locked);
    }

    private void drawAnimationInfo(ActorSpriteAnimController animController, GameEntity actor, Color bgColor) {
        final int padding = 4;
        final int height = 16;
        final int actorOffsetX = -40;
        final int actorOffsetY = -30;
        final Vector2f center = actor.pos().bodyCenter();

        final AnimationInfo info = switch (actor) {
            case Pac pac -> animationInfo(pac, animController);
            case Ghost ghost -> animationInfo(ghost, animController);
            default -> new AnimationInfo(
                animController.selectedAnimationID(actor),
                animController.currentFrame(actor),
                false, false);
        };

        dummy.setFont(debugTextFont);
        dummy.setText(formatAnimationInfo(info));
        double width = dummy.getLayoutBounds().getWidth() + 2 * padding;

        ctx.save();
        ctx.translate(scaled(center.x() + actorOffsetX), scaled(center.y() + actorOffsetY));

        ctx.setFill(Ufx.colorWithOpacity(Color.DARKBLUE, 0.8));
        ctx.fillRect(0, 0, width, scaled(height));
        ctx.setFill(bgColor);
        ctx.fillRect(0, 0, scaled(1), scaled(height));

        ctx.setFill(debugTextFill);
        ctx.setFont(dummy.getFont());
        ctx.fillText(dummy.getText(), scaled(3), scaled(0.6 * height));

        ctx.restore();
    }

    private void drawDirectionIndicator(GameEntity actor, WorldNavigationComp navigation) {
        if (navigation.wishDir() == null) return;

        ctx.save();
        Vector2f center = actor.pos().bodyCenter();
        Vector2f arrowHead = center.plus(navigation.wishDir().vector().scaled(12f)).scaled(scaling());
        Vector2f guyCenter = center.scaled(scaling());
        double radius = scaled(2), diameter = 2 * radius;
        ctx.setStroke(Color.WHITE);
        ctx.setLineWidth(0.5);
        ctx.strokeLine(guyCenter.x(), guyCenter.y(), arrowHead.x(), arrowHead.y());
        ctx.setFill(navigation.isNewTileEntered() ? Color.YELLOW : Color.GREEN);
        ctx.fillOval(arrowHead.x() - radius, arrowHead.y() - radius, diameter, diameter);
        ctx.restore();
    }

    public void drawTerrainDebugInfo(GameLevel level) {
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
                for (Direction dir : CLOCK_WISE) {
                    if (!terrain.isInaccessibleTile(tile.plus(dir.vector()))) {
                        final double x = cx + dir.vector().x() * WorldMap.HTS;
                        final double y = cy + dir.vector().y() * WorldMap.HTS;
                        ctx.setStroke(Color.WHITE);
                        ctx.setLineWidth(2);
                        ctx.strokeLine(scaled(cx), scaled(cy), scaled(x), scaled(y));
                    }
                }
            });
    }

}