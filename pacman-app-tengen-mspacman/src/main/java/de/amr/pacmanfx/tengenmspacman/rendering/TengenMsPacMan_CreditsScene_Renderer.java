/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.pacmanfx.tengenmspacman.scenes.TengenMsPacMan_CreditsScene;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import de.amr.pacmanfx.uilib.assets.UIPreferences;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.nesColor;
import static de.amr.pacmanfx.tengenmspacman.scenes.TengenMsPacMan_CreditsScene.DISPLAY_SECONDS;

public class TengenMsPacMan_CreditsScene_Renderer extends GameScene2D_Renderer implements TengenMsPacMan_CommonSceneRenderingFunctions {

    private int y;

    public TengenMsPacMan_CreditsScene_Renderer(UIPreferences prefs, GameScene2D scene, Canvas canvas) {
        super(canvas);
        createDefaultDebugInfoRenderer(prefs, scene, canvas);
    }

    @Override
    public GameScene2D_Renderer renderer() {
        return this;
    }

    @Override
    public void draw(GameScene2D scene) {
        clearCanvas();
        drawHorizontalBar(nesColor(0x20), nesColor(0x13), scene.unscaledSize().x(), TS, 20);
        drawHorizontalBar(nesColor(0x20), nesColor(0x13), scene.unscaledSize().x(), TS, 212);
        ctx.setFont(arcadeFont8());
        y = 7 * TS; // important: reset on every draw!
        if (scene.context().currentGame().control().state().timer().betweenSeconds(0, 0.5 * DISPLAY_SECONDS)) {
            drawOriginalCreditsText();
        } else {
            final var creditsScene = (TengenMsPacMan_CreditsScene) scene;
            ctx.setGlobalAlpha(creditsScene.fadeProgress);
            drawJavaFXRemakeCreditsText();
            ctx.setGlobalAlpha(1);
        }
        if (scene.debugInfoVisible()) {
            debugRenderer.draw(scene);
        }
    }

    private void drawOriginalCreditsText() {
        drawTextAndSkip(4, "CREDITS FOR MS PAC-MAN",    0x20,  3);
        drawTextAndSkip(2, "GAME PROGRAMMER:",          0x23,  4);
        drawTextAndSkip(3, "FRANZ LANZINGER",           0x23, 10);
        drawTextAndSkip(2, "SPECIAL THANKS:",           0x23,  4);
        drawTextAndSkip(1, "JEFF YONAN",                0x23, 10);
        drawTextAndSkip(4, "DAVE O'RIVA",               0x23, 10);
        drawTextAndSkip(1, "MS PAC-MAN TM NAMCO LTD",   0x19,  5);
        drawTextAndSkip(1, "©1990 TENGEN INC",          0x19,  7);
        drawTextAndSkip(0, "ALL RIGHTS RESERVED",       0x19,  6);
    }

    private void drawJavaFXRemakeCreditsText() {
        drawTextAndSkip(4, "CREDITS FOR JAVAFX REMAKE", 0x20,  3);
        drawTextAndSkip(2, "GAME PROGRAMMER:",          0x23,  4);
        drawTextAndSkip(3, "ARMIN REICHERT",            0x23, 10);
        drawTextAndSkip(2, "SPECIAL THANKS:",           0x23,  4);
        drawTextAndSkip(1, "@RUSSIANMANSMWC",           0x23, 10);
        drawTextAndSkip(1, "@FLICKY1211",               0x23, 10);
        drawTextAndSkip(3, "ANDYANA JONSEPH",           0x23, 10);
        drawTextAndSkip(1, "GITHUB.COM/ARMIN-REICHERT", 0x19,  3);
        drawTextAndSkip(1, "©2024 MIT LICENSE",         0x19,  6);
        drawTextAndSkip(0, "ALL RIGHTS GRANTED",        0x19,  5);
    }

    private void drawTextAndSkip(int numTiles, String text, int colorIndex, int tilesX) {
        fillText(text, nesColor(colorIndex), TS(tilesX), y);
        y += numTiles * TS;
    }
}