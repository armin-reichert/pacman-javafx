/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.game.GameVariantConfig;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.gamescene.d2.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRendererMixin;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

import static de.amr.pacmanfx.core.model.world.WorldMap.TS;
import static de.amr.pacmanfx.core.model.world.WorldMap.tilesPx;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_ORANGE;

public class ArcadeMsPacMan_StartScene_Renderer extends BaseRenderer implements GameScene2D_Renderer, SpriteRendererMixin {

    private final CopyrightRenderer copyrightRenderer;
    private final BaseDebugInfoRenderer debugRenderer;

    private final Image copyrightImage;

    public ArcadeMsPacMan_StartScene_Renderer(GameVariantConfig gameVariant, AbstractGameScene2D scene, Canvas canvas) {
        super(canvas);

        copyrightImage = gameVariant.assets().image("logo.midway");

        copyrightRenderer = scene.configureRenderer(new CopyrightRenderer(canvas));
        debugRenderer = GameScene2D_Renderer.createDefaultSceneDebugRenderer(scene, canvas);
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return ArcadeMsPacMan_SpriteSheet.instance();
    }

    @Override
    public void draw(AbstractGameScene2D scene, long tick) {
        final double STS = scaled(TS);

        clearCanvas();
        ctx.setFill(ARCADE_ORANGE);
        ctx.setFont(arcadeFont8());
        ctx.fillText("PUSH START BUTTON",      STS*6, STS*16);
        ctx.fillText("1 PLAYER ONLY",          STS*8, STS*18);
        ctx.fillText("ADDITIONAL    AT 10000", STS*2, STS*25);
        ctx.setFont(arcadeFont6());
        ctx.fillText("PTS", STS*25, STS*25);
        drawSprite(spriteSheet().sprite(SpriteID.LIVES_COUNTER_SYMBOL), tilesPx(13), tilesPx(23) + 1, true);
        copyrightRenderer.drawCopyright(copyrightImage, tilesPx(6), tilesPx(28));
        if (scene.game().ui().viewModel().debugModeOnProperty.get()) {
            debugRenderer.draw(scene, tick);
        }
    }
}