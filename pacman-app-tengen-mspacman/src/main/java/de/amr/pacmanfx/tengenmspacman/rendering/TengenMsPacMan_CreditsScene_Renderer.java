/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.Renderable;
import de.amr.pacmanfx.tengenmspacman.gamescene.TengenMsPacMan_CreditsScene;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.SceneCanvasRenderingComp;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_SceneRendererUtils.drawHorizontalBar;
import static de.amr.pacmanfx.ui.gamescene.d2.BaseGameSceneDebugInfoRenderer.createDefaultSceneDebugRenderer;

public class TengenMsPacMan_CreditsScene_Renderer extends BaseRenderer {

    record Line(String text, int paletteIndex, int column, int skipTiles) {}

    private static final Line[] ORIGINAL_AUTHORS_LINES = {
        new Line("CREDITS FOR MS PAC-MAN",  0x20,  3, 4),
        new Line("GAME PROGRAMMER:",        0x23,  4, 2),
        new Line("FRANZ LANZINGER",         0x23, 10, 3),
        new Line("SPECIAL THANKS:",         0x23,  4, 2),
        new Line("JEFF YONAN",              0x23, 10, 1),
        new Line("DAVE O'RIVA",             0x23, 10, 4),
        new Line("MS PAC-MAN TM NAMCO LTD", 0x19,  5, 1),
        new Line("©1990 TENGEN INC",        0x19,  7, 1),
        new Line("ALL RIGHTS RESERVED",     0x19,  6, 1),
    };

    private static final Line[] REMAKE_AUTHORS_LINES = {
        new Line("CREDITS FOR JAVAFX REMAKE",  0x20,  3, 4),
        new Line("GAME PROGRAMMER:",           0x23,  4, 2),
        new Line("ARMIN REICHERT",             0x23, 10, 3),
        new Line("SPECIAL THANKS:",            0x23,  4, 2),
        new Line("@RUSSIANMANSMWC",            0x23, 10, 1),
        new Line("@FLICKY1211",                0x23, 10, 1),
        new Line("ANDYANA JONSEPH",            0x23, 10, 3),
        new Line("GITHUB.COM/ARMIN-REICHERT",  0x19,  3, 1),
        new Line("©2021 MIT LICENSE",          0x19,  6, 1),
        new Line("ALL RIGHTS GRANTED",         0x19,  5, 1),
    };

    private class Pen {
        private double y;

        public void setY(double y) {
            this.y = y;
        }

        public void drawLines(Line[] lines) {
            for (var line : lines) {
                drawLine(line);
            }
        }

        public void drawLine(Line line) {
            fillText(line.text(), NES_Palette.color(line.paletteIndex()), tilesPx(line.column()), y);
            y += tilesPx(line.skipTiles());
        }
    }

    private final Pen pen = new Pen();

    public TengenMsPacMan_CreditsScene_Renderer(GameScene gameScene, Canvas canvas) {
        super(canvas);
        setDebugInfoRenderer(createDefaultSceneDebugRenderer(gameScene, canvas));
    }

    @Override
    public void render(Renderable r, long tick) {
        if (!(r instanceof TengenMsPacMan_CreditsScene creditsScene)) {
            return;
        }

        final SceneCanvasRenderingComp canvasRendering = creditsScene.reqComp(SceneCanvasRenderingComp.class);

        final int width = canvasRendering.unscaledWidth();
        drawHorizontalBar(ctx, scaling(),NES_Palette.color(0x20), NES_Palette.color(0x13), width, TS, 20);
        drawHorizontalBar(ctx, scaling(),NES_Palette.color(0x20), NES_Palette.color(0x13), width, TS, 212);

        final Font arcade8 = Ufx.deriveFont(GlobalAssets.Fonts.ARCADE.font(), scaled(8));
        ctx.setFont(arcade8);
        switch (creditsScene.displayMode) {
            case ORIGINAL_AUTHORS -> {
                pen.setY(tilesPx(7));
                pen.drawLines(ORIGINAL_AUTHORS_LINES);
            }
            case REMAKE_AUTHORS -> {
                ctx.save();
                ctx.setGlobalAlpha(creditsScene.fadeProgress);
                pen.setY(tilesPx(7));
                pen.drawLines(REMAKE_AUTHORS_LINES);
                ctx.restore();
            }
        }
    }
}