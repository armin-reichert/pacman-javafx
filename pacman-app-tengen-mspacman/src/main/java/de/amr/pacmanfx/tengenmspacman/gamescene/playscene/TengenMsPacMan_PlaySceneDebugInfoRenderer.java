/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamescene.playscene;


import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.Renderable;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.ui.gamescene.d2.BaseGameSceneDebugInfoRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_WIDTH;

public class TengenMsPacMan_PlaySceneDebugInfoRenderer extends BaseGameSceneDebugInfoRenderer {

    public TengenMsPacMan_PlaySceneDebugInfoRenderer(ActorSpriteAnimController animController, Canvas canvas) {
        super(animController, canvas);
    }

    @Override
    public void render(Renderable r, long tick) {
        if (!(r instanceof TengenMsPacMan_PlayScene2D playScene)) {
            return;
        }

        final GameContext game = playScene.game();
        final GameSession session = game.session();
        final AbstractGameState gameState = game.state();

        drawTileGrid(NES_SCREEN_WIDTH, playScene.canvasHeightUnscaled(), Color.LIGHTGRAY);

        ctx.save();
        ctx.translate(scaled(TengenMsPacMan_PlayScene2D_Renderer.CONTENT_INDENT), 0);
        ctx.setFill(debugTextFill);
        ctx.setFont(debugTextFont);
        ctx.fillText("%s %d".formatted(gameState.name(), gameState.timer().tickCount()), 0, scaled(3 * WorldMap.TS));
        session.optLevel().ifPresent(level -> {
            drawMovingActorInfo(animController, level.entities().pac());
            level.entities().ghosts().forEach(ghost -> drawMovingActorInfo(animController, ghost));
        });
        ctx.fillText("Camera y=%.2f".formatted(playScene.dynamicCamera().getTranslateY()), scaled(11 * WorldMap.TS), scaled(15 * WorldMap.TS));
        ctx.restore();
    }
}
