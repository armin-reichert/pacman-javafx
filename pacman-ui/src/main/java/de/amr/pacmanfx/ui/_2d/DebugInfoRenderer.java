/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.MovingActor;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public interface DebugInfoRenderer {

    default void drawMovingActorInfo(GraphicsContext ctx, double scaling, MovingActor movingActor) {
        if (!movingActor.isVisible()) {
            return;
        }
        if (movingActor instanceof Pac pac) {
            String autopilot = pac.isUsingAutopilot() ? "autopilot" : "";
            String immune = pac.isImmune() ? "immune" : "";
            String text = "%s\n%s".formatted(autopilot, immune).trim();
            ctx.setFill(Color.WHITE);
            ctx.setFont(Font.font("Monospaced", scaling * (6)));
            ctx.fillText(text, scaling * (pac.x() - 4), scaling * (pac.y() + 16));
        }
        movingActor.animations()
                .filter(SpriteAnimationManager.class::isInstance)
                .map(SpriteAnimationManager.class::cast)
                .ifPresent(spriteAnimationMap -> {
                    String selectedID = spriteAnimationMap.selectedID();
                    if (selectedID != null) {
                        drawAnimationInfo(ctx, scaling, movingActor, spriteAnimationMap, selectedID);
                    }
                    if (movingActor.wishDir() != null) {
                        drawDirectionIndicator(ctx, scaling, movingActor);
                    }
                });
    }

    private void drawAnimationInfo(GraphicsContext ctx, double scaling, Actor actor, SpriteAnimationManager<?> spriteAnimationMap, String selectedID) {
        ctx.save();
        String text = "[%s:%d]".formatted(selectedID, spriteAnimationMap.currentAnimation().frameIndex());
        double x = scaling * (actor.x() - 4), y = scaling * (actor.y() - 4);
        ctx.setFill(Color.WHITE);
        ctx.setFont(Font.font("Sans", scaling * (7)));
        ctx.fillText(text, x, y);
        ctx.setStroke(Color.GRAY);
        ctx.strokeText(text, x, y);
        ctx.restore();
    }

    private void drawDirectionIndicator(GraphicsContext ctx, double scaling, MovingActor movingActor) {
        ctx.save();
        Vector2f center = movingActor.center();
        Vector2f arrowHead = center.plus(movingActor.wishDir().vector().scaled(12f)).scaled(scaling);
        Vector2f guyCenter = center.scaled(scaling);
        double radius = scaling * 2, diameter = 2 * radius;
        ctx.setStroke(Color.WHITE);
        ctx.setLineWidth(0.5);
        ctx.strokeLine(guyCenter.x(), guyCenter.y(), arrowHead.x(), arrowHead.y());
        ctx.setFill(movingActor.isNewTileEntered() ? Color.YELLOW : Color.GREEN);
        ctx.fillOval(arrowHead.x() - radius, arrowHead.y() - radius, diameter, diameter);
        ctx.restore();
    }
}
