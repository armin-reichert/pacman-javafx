/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.ArcadePalette.ARCADE_ORANGE;
import static de.amr.pacmanfx.ui.ArcadePalette.ARCADE_RED;

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

        final Font font6 = arcadeFont6();
        final Font font8 = arcadeFont8();
        fillText("PUSH START BUTTON", ARCADE_ORANGE, font8, TS(6), TS(16));
        fillText("1 PLAYER ONLY", ARCADE_ORANGE, font8, TS(8), TS(18));
        fillText("ADDITIONAL    AT 10000", ARCADE_ORANGE, font8,TS(2), TS(25));
        drawSprite(spriteSheet().sprite(SpriteID.LIVES_COUNTER_SYMBOL), TS(13), TS(23) + 1, true);
        fillText("PTS", ARCADE_ORANGE, font6, TS(25), TS(25));

        Image logo = scene.ui().currentConfig().assets().image("logo.midway");
        if (logo != null) {
            drawMidwayCopyright(logo, TS * 6, TS * 28);
        }

        if (scene.debugInfoVisible()) {
            debugRenderer.draw(scene);
        }
    }

    public void drawMidwayCopyright(Image logo, double x, double y) {
        ctx.drawImage(logo, scaled(x), scaled(y + 2), scaled(TS(4) - 2), scaled(TS(4)));
        ctx.setFont(arcadeFont8());
        ctx.setFill(ARCADE_RED);
        ctx.fillText("©", scaled(x + TS(5)), scaled(y + TS(2)) + 2);
        ctx.fillText("MIDWAY MFG CO", scaled(x + TS(7)), scaled(y + TS(2)));
        ctx.fillText("1980/1981", scaled(x + TS(8)), scaled(y + TS(4)));
    }
}