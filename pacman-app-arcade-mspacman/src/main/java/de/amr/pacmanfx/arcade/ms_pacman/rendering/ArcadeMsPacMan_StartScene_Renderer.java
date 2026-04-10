/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_ORANGE;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_RED;

public class ArcadeMsPacMan_StartScene_Renderer extends GameScene2D_Renderer implements SpriteRenderer {

    public ArcadeMsPacMan_StartScene_Renderer(GameScene2D scene, Canvas canvas) {
        super(canvas);
        createDefaultDebugInfoRenderer(scene, canvas);
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
        drawMidwayCopyright(scene.ui().currentConfig().assets(), TS * 6, TS * 28);

        if (GameUI.PROPERTY_DEBUG_INFO_VISIBLE.get()) {
            debugRenderer.draw(scene);
        }
    }

    public void drawMidwayCopyright(AssetMap assets, double x, double y) {
        final Image logo = assets.image("logo.midway");
        ctx.setFont(arcadeFont8());
        ctx.setFill(ARCADE_RED);
        ctx.drawImage(logo, scaled(x), scaled(y + 2), scaled(TS(4) - 2), scaled(TS(4)));
        ctx.fillText("©",             scaled(x + TS(5)), scaled(y + TS(2)) + 2);
        ctx.fillText("MIDWAY MFG CO", scaled(x + TS(7)), scaled(y + TS(2)));
        ctx.fillText("1980/1981",     scaled(x + TS(8)), scaled(y + TS(4)));
    }
}