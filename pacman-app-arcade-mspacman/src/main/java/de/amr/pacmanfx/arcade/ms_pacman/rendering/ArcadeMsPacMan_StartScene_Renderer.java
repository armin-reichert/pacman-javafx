/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.SceneCanvasRenderingComp;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static de.amr.pacmanfx.ui.gamescene.d2.BaseGameSceneDebugInfoRenderer.createDefaultSceneDebugRenderer;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_ORANGE;

public class ArcadeMsPacMan_StartScene_Renderer extends BaseRenderer implements SpriteRenderer {

    private final CopyrightRenderer copyrightRenderer;
    private final Image copyrightImage;

    public ArcadeMsPacMan_StartScene_Renderer(GameVariantRenderConfig renderConfig, GameScene gameScene, Canvas canvas) {
        super(canvas);

        copyrightImage = renderConfig.assets().image("logo.midway");

        final SceneCanvasRenderingComp r2D = gameScene.reqComp(SceneCanvasRenderingComp.class);
        copyrightRenderer = r2D.configureRenderer(new CopyrightRenderer(canvas));
        setDebugInfoRenderer(createDefaultSceneDebugRenderer(gameScene, canvas));
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return ArcadeMsPacMan_SpriteSheet.instance();
    }

    @Override
    public void render(Object r, long tick) {
        final Font arcade6 = Ufx.deriveFont(GlobalAssets.Fonts.ARCADE.font(), scaled(6));
        final Font arcade8 = Ufx.deriveFont(GlobalAssets.Fonts.ARCADE.font(), scaled(8));
        final double STS = scaled(TS);

        ctx.setFill(ARCADE_ORANGE);
        ctx.setFont(arcade8);
        ctx.fillText("PUSH START BUTTON",      STS*6, STS*16);
        ctx.fillText("1 PLAYER ONLY",          STS*8, STS*18);
        ctx.fillText("ADDITIONAL    AT 10000", STS*2, STS*25);
        ctx.setFont(arcade6);
        ctx.fillText("PTS", STS*25, STS*25);
        drawSprite(spriteSheet().findSprite(SpriteID.LIVES_COUNTER_SYMBOL), tilesPx(13), tilesPx(23) + 1, true);
        copyrightRenderer.drawCopyright(copyrightImage, tilesPx(6), tilesPx(28));
    }
}