/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.pacmanfx.tengenmspacman.gamescene.TengenMsPacMan_CreditsScene;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.gamescene.d2.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.model.world.WorldMap.TS;

public class TengenMsPacMan_CreditsScene_Renderer extends BaseRenderer implements GameScene2D_Renderer, TengenMsPacMan_SceneRendererMixin {

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
            fillText(line.text(), NES_Palette.color(line.paletteIndex()), TS(line.column()), y);
            y += TS(line.skipTiles());
        }
    }

    private final BaseDebugInfoRenderer debugRenderer;
    private final Pen pen = new Pen();

    public TengenMsPacMan_CreditsScene_Renderer(AbstractGameScene2D scene, Canvas canvas) {
        super(canvas);
        debugRenderer = GameScene2D_Renderer.createDefaultSceneDebugRenderer(scene, canvas);
    }

    @Override
    public GameScene2D_Renderer renderer() {
        return this;
    }

    @Override
    public void draw(AbstractGameScene2D scene) {
        clearCanvas();
        if (!(scene instanceof TengenMsPacMan_CreditsScene creditsScene)) {
            return;
        }
        drawHorizontalBar(NES_Palette.color(0x20), NES_Palette.color(0x13), scene.unscaledWidth(), TS, 20);
        drawHorizontalBar(NES_Palette.color(0x20), NES_Palette.color(0x13), scene.unscaledWidth(), TS, 212);

        ctx.setFont(arcadeFont8());
        switch (creditsScene.displayMode) {
            case ORIGINAL_AUTHORS -> {
                pen.setY(TS(7));
                pen.drawLines(ORIGINAL_AUTHORS_LINES);
            }
            case REMAKE_AUTHORS -> {
                ctx.save();
                ctx.setGlobalAlpha(creditsScene.fadeProgress);
                pen.setY(TS(7));
                pen.drawLines(REMAKE_AUTHORS_LINES);
                ctx.restore();
            }
        }

        if (scene.game().ui().viewModel().debugModeOnProperty.get()) {
            debugRenderer.draw(scene);
        }
    }
}