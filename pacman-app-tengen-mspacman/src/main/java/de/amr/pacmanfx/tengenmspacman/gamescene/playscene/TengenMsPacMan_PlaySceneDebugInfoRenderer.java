/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamescene.playscene;

import de.amr.basics.ui.ecs.system.ActorSpriteAnimController;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.common.GameSceneDebugView;
import de.amr.pacmanfx.ui.gamescene.d2.BaseGameSceneDebugInfoRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import static de.amr.basics.TileDimension.TS;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_WIDTH;

public class TengenMsPacMan_PlaySceneDebugInfoRenderer extends BaseGameSceneDebugInfoRenderer {

    public TengenMsPacMan_PlaySceneDebugInfoRenderer(ActorSpriteAnimController animController, Canvas canvas) {
        super(animController, canvas);
    }

    @Override
    public void render(Renderable r, long tick) {
        switch (r) {
            case GameSceneDebugView(GameScene gameScene) -> {
                if (gameScene instanceof AbstractGameScene abstractGameScene) {
                    draw(abstractGameScene);
                }
            }
            default -> {}
        }
    }

    public void draw(AbstractGameScene playScene) {
        final GameContext game = playScene.game();
        final GameSession session = game.session();
        final AbstractGameState gameState = game.state();

        ctx.save();
        ctx.getCanvas().setClip(null);
        drawDebugGrid(NES_SCREEN_WIDTH, playScene.view2D().unscaledHeight(), Color.LIGHTGRAY);
        ctx.restore();

        ctx.save();
        ctx.translate(scaled(TengenMsPacMan_PlayScene2D.OFFSET.x()), 0);
        ctx.setFill(debugTextFill);
        ctx.setFont(debugTextFont);
        ctx.fillText("%s %d".formatted(gameState.name(), gameState.timer().tickCount()), 0, scaled(3 * TS));
        session.optLevel().ifPresent(level -> {
            drawMovingActorInfo(animController, level.entitySet().pac());
            level.entitySet().ghosts().forEach(ghost -> drawMovingActorInfo(animController, ghost));
        });
        ctx.restore();
    }
}
