/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.gamescene.d2;

import de.amr.basics.Named;
import de.amr.basics.math.Vector2f;
import de.amr.basics.timer.TickTimer;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.SpriteAnimationComp;
import de.amr.pacmanfx.core.ecs.comp.WorldNavigationComp;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.ecs.systems.PositionSystem;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostAnimationComp;
import de.amr.pacmanfx.core.entities.pac.comp.PacAnimationComp;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class BaseDebugInfoRenderer extends BaseRenderer implements GameScene2D_Renderer {

    public static final Color DEFAULT_FILL_COLOR = Color.WHITE;
    public static final Color DEFAULT_STROKE_COLOR = Color.GRAY;

    protected Color debugTextFill = DEFAULT_FILL_COLOR;
    protected Color debugTextStroke = DEFAULT_STROKE_COLOR;
    protected Font debugTextFont = Font.font("Sans", 14.0f);

    public BaseDebugInfoRenderer(Canvas canvas) {
        super(canvas);
    }

    @Override
    public void draw(GameScene scene, long tick) {
        final AbstractGameState gameState = scene.game().state();
        final String stateText = "Game State: '%s' (Tick %d of %s)".formatted(
            gameState.name(),
            gameState.timer().tickCount(),
            gameState.timer().durationTicks() == TickTimer.INDEFINITE ? "∞" : String.valueOf(gameState.timer().tickCount())
        );
        ctx.setFill(debugTextFill);
        ctx.setStroke(debugTextStroke);
        ctx.setFont(debugTextFont);
        ctx.fillText(stateText, 0, scaled(3 * WorldMap.TS));

        final CanvasRenderingComp r2D = scene.components().reqComp(CanvasRenderingComp.class);
        drawTileGrid(r2D.unscaledWidth(), r2D.unscaledHeight(), Color.LIGHTGRAY);
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

        if (actor.hasComp(SpriteAnimationComp.class)) {
            if (animController.selectedAnimationID(actor) != null) {
                drawAnimationInfo(animController, actor, bgColor(actor));
            }
        }

        if (actor.hasComp(WorldNavigationComp.class)) {
            final WorldNavigationComp worldNavigation = actor.reqComp(WorldNavigationComp.class);
            if (worldNavigation.wishDir() != null) {
                drawDirectionIndicator(actor);
            }
        }

        final Rectangle2D boundingBox = PositionSystem.boundingBox(actor.pos().asVector2f());
        ctx.save();
        ctx.setStroke(Color.BLACK);
        ctx.setLineWidth(2);
        ctx.strokeRect(scaled(boundingBox.getMinX()), scaled(boundingBox.getMinY()), scaled(boundingBox.getWidth()), scaled(boundingBox.getHeight()));
        ctx.restore();
    }

    record AnimationInfo(Named animationID, int frame, boolean stopped, boolean locked) {}

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
        final int width = 120;
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

        ctx.save();
        ctx.translate(scaled(center.x() + actorOffsetX), scaled(center.y() + actorOffsetY));

        ctx.setFill(Ufx.colorWithOpacity(Color.DARKBLUE, 0.8));
        ctx.fillRect(0, 0, scaled(width), scaled(height));
        ctx.setFill(bgColor);
        ctx.fillRect(0, 0, scaled(1), scaled(height));

        ctx.setFill(debugTextFill);
        ctx.setFont(debugTextFont);
        ctx.fillText(formatAnimationInfo(info), scaled(3), scaled(0.6 * height));

        ctx.restore();
    }

    private void drawDirectionIndicator(GameEntity actor) {
        final WorldNavigationComp worldNavigation = actor.reqComp(WorldNavigationComp.class);

        ctx.save();
        Vector2f center = actor.pos().bodyCenter();
        Vector2f arrowHead = center.plus(worldNavigation.wishDir().vector().scaled(12f)).scaled(scaling());
        Vector2f guyCenter = center.scaled(scaling());
        double radius = scaled(2), diameter = 2 * radius;
        ctx.setStroke(Color.WHITE);
        ctx.setLineWidth(0.5);
        ctx.strokeLine(guyCenter.x(), guyCenter.y(), arrowHead.x(), arrowHead.y());
        ctx.setFill(worldNavigation.isNewTileEntered() ? Color.YELLOW : Color.GREEN);
        ctx.fillOval(arrowHead.x() - radius, arrowHead.y() - radius, diameter, diameter);
        ctx.restore();
    }
}