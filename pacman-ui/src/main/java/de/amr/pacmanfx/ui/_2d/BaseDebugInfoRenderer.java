/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.lib.TickTimer;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.GameControl;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.MovingActor;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.Globals.TS;

public class BaseDebugInfoRenderer extends GameScene2D_Renderer {

    protected Color debugTextFill;
    protected Color debugTextStroke;
    protected Font debugTextFont;

    public BaseDebugInfoRenderer(GameUI ui, Canvas canvas) {
        super(canvas);
        debugTextFill   = ui.prefs().getColor("debug_text.fill");
        debugTextStroke = ui.prefs().getColor("debug_text.stroke");
        debugTextFont   = ui.prefs().getFont("debug_text.font");
    }

    @Override
    public void draw(GameScene2D scene) {
        final GameControl gameControl = scene.ui().context().currentGame().control();
        final TickTimer stateTimer = gameControl.state().timer();
        final String stateText = "Game State: '%s' (Tick %d of %s)".formatted(
            gameControl.state().name(),
            stateTimer.tickCount(),
            stateTimer.durationTicks() == TickTimer.INDEFINITE ? "∞" : String.valueOf(stateTimer.tickCount())
        );
        ctx.setFill(debugTextFill);
        ctx.setStroke(debugTextStroke);
        ctx.setFont(debugTextFont);
        ctx.fillText(stateText, 0, scaled(3 * TS));

        final Vector2i size = scene.unscaledSize();
        drawTileGrid(size.x(), size.y(), Color.LIGHTGRAY);
    }

    public void drawMovingActorInfo(MovingActor movingActor) {
        if (!movingActor.isVisible()) {
            return;
        }
        ctx.setFill(Color.FORESTGREEN);
        if (movingActor instanceof Pac pac) {
            String autopilot = pac.isUsingAutopilot() ? "autopilot" : "";
            String immune = pac.isImmune() ? "immune" : "";
            String text = "%s\n%s".formatted(autopilot, immune).trim();
            ctx.setFont(debugTextFont);
            ctx.fillText(text, scaled(pac.x() - 4), scaled(pac.y() + 16));
        }
        movingActor.optAnimationManager()
            .filter(SpriteAnimationManager.class::isInstance)
            .map(SpriteAnimationManager.class::cast)
            .ifPresent(spriteAnimationMap -> {
                Object selectedID = spriteAnimationMap.selectedID();
                if (selectedID != null) {
                    ctx.setFont(debugTextFont);
                    drawAnimationInfo(movingActor, spriteAnimationMap, selectedID);
                }
                if (movingActor.wishDir() != null) {
                    drawDirectionIndicator(movingActor);
                }
            });
    }

    private void drawAnimationInfo(Actor actor, SpriteAnimationManager<?> spriteAnimationMap, Object selectedID) {
        ctx.save();
        String text = "[%s:%d]".formatted(selectedID, spriteAnimationMap.currentAnimation().frameIndex());
        double x = scaled(actor.x() - 4), y = scaled(actor.y() - 4);
        ctx.setFill(debugTextFill);
        ctx.fillText(text, x, y);
        ctx.restore();
    }

    private void drawDirectionIndicator(MovingActor movingActor) {
        ctx.save();
        Vector2f center = movingActor.center();
        Vector2f arrowHead = center.plus(movingActor.wishDir().vector().scaled(12f)).scaled(scaling());
        Vector2f guyCenter = center.scaled(scaling());
        double radius = scaled(2), diameter = 2 * radius;
        ctx.setStroke(Color.WHITE);
        ctx.setLineWidth(0.5);
        ctx.strokeLine(guyCenter.x(), guyCenter.y(), arrowHead.x(), arrowHead.y());
        ctx.setFill(movingActor.isNewTileEntered() ? Color.YELLOW : Color.GREEN);
        ctx.fillOval(arrowHead.x() - radius, arrowHead.y() - radius, diameter, diameter);
        ctx.restore();
    }
}