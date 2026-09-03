/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.arcade.ms_pacman.scenes.ArcadeMsPacMan_StartScene;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.BaseGameSceneDebugInfoRenderer;
import de.amr.pacmanfx.ui.gamescene.d2.CanvasRenderingComp;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static de.amr.pacmanfx.ui.gamescene.d2.BaseGameSceneDebugInfoRenderer.createDefaultSceneDebugRenderer;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_ORANGE;

public class ArcadeMsPacMan_StartScene_Renderer extends BaseRenderer implements SpriteRenderer {

    private final CopyrightRenderer copyrightRenderer;
    private final BaseGameSceneDebugInfoRenderer debugRenderer;

    private final Image copyrightImage;

    public ArcadeMsPacMan_StartScene_Renderer(GameVariantRenderConfig renderConfig, GameScene gameScene, Canvas canvas) {
        super(canvas);

        final CanvasRenderingComp r2D = gameScene.reqComp(CanvasRenderingComp.class);

        copyrightImage = renderConfig.assets().image("logo.midway");
        copyrightRenderer = r2D.configureRenderer(new CopyrightRenderer(canvas));
        debugRenderer = createDefaultSceneDebugRenderer(gameScene, canvas);
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return ArcadeMsPacMan_SpriteSheet.instance();
    }

    @Override
    public void render(Object r, long tick) {
        if ((!(r instanceof ArcadeMsPacMan_StartScene startScene))) {
            return;
        }

        final double STS = scaled(TS);

        ctx.setFill(ARCADE_ORANGE);
        ctx.setFont(arcadeFont8());
        ctx.fillText("PUSH START BUTTON",      STS*6, STS*16);
        ctx.fillText("1 PLAYER ONLY",          STS*8, STS*18);
        ctx.fillText("ADDITIONAL    AT 10000", STS*2, STS*25);
        ctx.setFont(arcadeFont6());
        ctx.fillText("PTS", STS*25, STS*25);
        drawSprite(spriteSheet().findSprite(SpriteID.LIVES_COUNTER_SYMBOL), tilesPx(13), tilesPx(23) + 1, true);
        copyrightRenderer.drawCopyright(copyrightImage, tilesPx(6), tilesPx(28));

        if (startScene.viewModel().debugModeOnProperty().get()) {
            debugRenderer.render(startScene, tick);
        }
    }
}