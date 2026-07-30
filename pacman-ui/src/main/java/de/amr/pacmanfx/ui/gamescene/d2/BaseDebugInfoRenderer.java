/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.gamescene.d2;

import de.amr.basics.math.Vector2f;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.model.actors.GameEntity;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.component.world.WorldNavigation;
import de.amr.pacmanfx.core.model.systems.common.WorldNavigationSystem;
import de.amr.pacmanfx.core.model.systems.spriteanim.SpriteAnimSystem;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.core.state.GameState;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

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
    public void draw(AbstractGameScene2D scene, long tick) {
        final GameState gameState = scene.gameState();
        final String stateText = "Game State: '%s' (Tick %d of %s)".formatted(
            gameState.name(),
            gameState.timer().tickCount(),
            gameState.timer().durationTicks() == TickTimer.INDEFINITE ? "∞" : String.valueOf(gameState.timer().tickCount())
        );
        ctx.setFill(debugTextFill);
        ctx.setStroke(debugTextStroke);
        ctx.setFont(debugTextFont);
        ctx.fillText(stateText, 0, scaled(3 * WorldMap.TS));

        drawTileGrid(scene.unscaledWidth(), scene.unscaledHeight(), Color.LIGHTGRAY);
    }

    public void drawMovingActorInfo(SpriteAnimSystem animSystem, GameEntity actor) {
        if (!actor.visibility().isVisible()) {
            return;
        }

        final WorldNavigation worldNavigation = actor.requireComponent(WorldNavigation.class);

        ctx.setFill(Color.FORESTGREEN);
        if (actor instanceof Pac pac) {
            String autopilot = pac.cheats().isUsingAutopilot() ? "autopilot" : "";
            String immune = pac.cheats().isImmune() ? "immune" : "";
            String text = "%s\n%s".formatted(autopilot, immune).trim();
            ctx.setFont(debugTextFont);
            ctx.fillText(text, scaled(pac.position().x - 4), scaled(pac.position().y + 16));
        }
        Object animationID = animSystem.selectedAnimationID(actor);
        if (animationID != null) {
            ctx.setFont(debugTextFont);
            drawAnimationInfo(animSystem, actor, animationID);
        }
        if (worldNavigation.wishDir() != null) {
            drawDirectionIndicator(actor);
        }
    }

    private void drawAnimationInfo(SpriteAnimSystem animSystem, GameEntity actor, Object selectedID) {
        ctx.save();
        String text = "[%s:%d]".formatted(selectedID, animSystem.currentFrame(actor));
        double x = scaled(actor.position().x - 4), y = scaled(actor.position().y - 4);
        ctx.setFill(debugTextFill);
        ctx.fillText(text, x, y);
        ctx.restore();
    }

    private void drawDirectionIndicator(GameEntity actor) {
        final WorldNavigation worldNavigation = actor.requireComponent(WorldNavigation.class);

        ctx.save();
        Vector2f center = WorldNavigationSystem.computeCenter(actor);
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