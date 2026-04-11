/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.d2.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRendererMixin;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_ORANGE;

public class ArcadeMsPacMan_StartScene_Renderer extends BaseRenderer implements GameScene2D_Renderer, SpriteRendererMixin {

    private final CopyrightRenderer copyrightRenderer;
    private final BaseDebugInfoRenderer debugRenderer;

    public ArcadeMsPacMan_StartScene_Renderer(GameScene2D scene, Canvas canvas) {
        super(canvas);
        copyrightRenderer = scene.adaptRenderer(new CopyrightRenderer(canvas,
            scene.ui().currentConfig().assets().image("logo.midway")));
        debugRenderer = GameScene2D_Renderer.createDefaultSceneDebugRenderer(scene, canvas);
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return ArcadeMsPacMan_SpriteSheet.instance();
    }

    @Override
    public void draw(GameScene2D scene) {
        clearCanvas();

        final double STS = scaled(TS);
        final RectShort livesCounterSprite = spriteSheet().sprite(SpriteID.LIVES_COUNTER_SYMBOL);

        ctx.setFill(ARCADE_ORANGE);
        ctx.setFont(arcadeFont8());
        ctx.fillText("PUSH START BUTTON",      STS*6, STS*16);
        ctx.fillText("1 PLAYER ONLY",          STS*8, STS*18);
        ctx.fillText("ADDITIONAL    AT 10000", STS*2, STS*25);
        ctx.setFont(arcadeFont6());
        ctx.fillText("PTS", STS*25, STS*25);
        drawSprite(livesCounterSprite, TS(13), TS(23) + 1, true);

        copyrightRenderer.drawCopyright(TS(6), TS(28));

        if (GameUI.PROPERTY_DEBUG_INFO_VISIBLE.get()) {
            debugRenderer.draw(scene);
        }
    }
}