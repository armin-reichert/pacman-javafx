/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d2;

import de.amr.pacmanfx.lib.TickTimer;
import de.amr.pacmanfx.lib.fsm.State;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.MovingActor;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.Globals.TS;

public class BaseDebugInfoRenderer extends BaseRenderer implements GameScene2D_Renderer {

    public static final Color DEFAULT_FILL_COLOR = Color.WHITE;
    public static final Color DEFAULT_STROKE_COLOR = Color.GRAY;
    public static final Font DEFAULT_FONT = Font.font("Sans", 16.0f);

    protected Color debugTextFill = DEFAULT_FILL_COLOR;
    protected Color debugTextStroke = DEFAULT_STROKE_COLOR;
    protected Font debugTextFont = DEFAULT_FONT;

    public BaseDebugInfoRenderer(Canvas canvas) {
        super(canvas);
    }

    @Override
    public void draw(GameScene2D scene) {
        final State<Game> state = scene.ui().gameContext().game().control().state();
        final String stateText = "Game State: '%s' (Tick %d of %s)".formatted(
            state.name(),
            state.timer().tickCount(),
            state.timer().durationTicks() == TickTimer.INDEFINITE ? "∞" : String.valueOf(state.timer().tickCount())
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
        if (movingActor.animations() instanceof SpriteAnimationMap<?> spriteAnimations) {
            Object animationID = spriteAnimations.selectedAnimationID();
            if (animationID != null) {
                ctx.setFont(debugTextFont);
                drawAnimationInfo(movingActor, spriteAnimations, animationID);
            }
            if (movingActor.wishDir() != null) {
                drawDirectionIndicator(movingActor);
            }

        }
    }

    private void drawAnimationInfo(Actor actor, SpriteAnimationMap<?> spriteAnimationMap, Object selectedID) {
        ctx.save();
        String text = "[%s:%d]".formatted(selectedID, spriteAnimationMap.currentAnimation().currentFrame());
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