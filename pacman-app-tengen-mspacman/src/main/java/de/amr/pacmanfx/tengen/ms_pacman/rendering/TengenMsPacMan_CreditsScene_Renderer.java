package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.tengen.ms_pacman.scenes.TengenMsPacMan_CreditsScene;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.nesColor;
import static de.amr.pacmanfx.tengen.ms_pacman.scenes.TengenMsPacMan_CreditsScene.DISPLAY_SECONDS;

public class TengenMsPacMan_CreditsScene_Renderer extends GameScene2D_Renderer implements TengenMsPacMan_CommonSceneRenderingFunctions {

    private int y;

    public TengenMsPacMan_CreditsScene_Renderer(GameScene2D scene, Canvas canvas, SpriteSheet<?> spriteSheet) {
        super(canvas, spriteSheet);
        createDefaultDebugInfoRenderer(scene, canvas, spriteSheet);
    }

    @Override
    public GameScene2D_Renderer renderer() {
        return this;
    }

    @Override
    public void draw(GameScene2D scene) {
        clearCanvas();

        final TengenMsPacMan_CreditsScene creditsScene = (TengenMsPacMan_CreditsScene) scene;

        drawHorizontalBar(nesColor(0x20), nesColor(0x13), creditsScene.sizeInPx().x(), TS, 20);
        drawHorizontalBar(nesColor(0x20), nesColor(0x13), creditsScene.sizeInPx().x(), TS, 212);
        ctx.setFont(arcadeFont8());
        y = 7 * TS; // important: reset on every draw!
        if (creditsScene.context().gameState().timer().betweenSeconds(0, 0.5 * DISPLAY_SECONDS)) {
            drawOriginalCreditsText();
        } else {
            ctx.setGlobalAlpha(creditsScene.fadeProgress);
            drawJavaFXRemakeCreditsText();
            ctx.setGlobalAlpha(1);
        }

        if (scene.debugInfoVisible()) {
            debugInfoRenderer.draw(scene);
        }
    }

    private void drawOriginalCreditsText() {
        drawText(4, "CREDITS FOR MS PAC-MAN",    0x20,  3);
        drawText(2, "GAME PROGRAMMER:",          0x23,  4);
        drawText(3, "FRANZ LANZINGER",           0x23, 10);
        drawText(2, "SPECIAL THANKS:",           0x23,  4);
        drawText(1, "JEFF YONAN",                0x23, 10);
        drawText(4, "DAVE O'RIVA",               0x23, 10);
        drawText(1, "MS PAC-MAN TM NAMCO LTD",   0x19,  5);
        drawText(1, "©1990 TENGEN INC",          0x19,  7);
        drawText(0, "ALL RIGHTS RESERVED",       0x19,  6);
    }

    private void drawJavaFXRemakeCreditsText() {
        drawText(4, "CREDITS FOR JAVAFX REMAKE", 0x20,  3);
        drawText(2, "GAME PROGRAMMER:",          0x23,  4);
        drawText(3, "ARMIN REICHERT",            0x23, 10);
        drawText(2, "SPECIAL THANKS:",           0x23,  4);
        drawText(1, "@RUSSIANMANSMWC",           0x23, 10);
        drawText(1, "@FLICKY1211",               0x23, 10);
        drawText(3, "ANDYANA JONSEPH",           0x23, 10);
        drawText(1, "GITHUB.COM/ARMIN-REICHERT", 0x19,  3);
        drawText(1, "©2024 MIT LICENSE",         0x19,  6);
        drawText(0, "ALL RIGHTS GRANTED",        0x19,  5);
    }

    private void drawText(int numTiles, String text, int colorIndex, int tilesX) {
        fillText(text, nesColor(colorIndex), TS(tilesX), y);
        y += numTiles * TS;
    }
}