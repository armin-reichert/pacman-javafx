package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.tengen.ms_pacman.scenes.TengenMsPacMan_CreditsScene;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2DRenderer;
import de.amr.pacmanfx.ui.input.JoypadKeyBinding;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.nesColor;
import static de.amr.pacmanfx.tengen.ms_pacman.scenes.TengenMsPacMan_CreditsScene.DISPLAY_SECONDS;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_CreditsScene_Renderer extends GameScene2DRenderer {

    private int y;

    public TengenMsPacMan_CreditsScene_Renderer(GameScene2D scene, Canvas canvas, SpriteSheet<?> spriteSheet) {
        super(scene, canvas, spriteSheet);
    }
    
    public void draw() {
        final TengenMsPacMan_CreditsScene creditsScene = (TengenMsPacMan_CreditsScene) scene();
        drawHorizontalBar(nesColor(0x20), nesColor(0x13), creditsScene.sizeInPx().x(), TS, 20);
        drawHorizontalBar(nesColor(0x20), nesColor(0x13), creditsScene.sizeInPx().x(), TS, 212);
        ctx().setFont(arcadeFont8());
        y = 7 * TS; // important: reset on every draw!
        if (creditsScene.context().gameState().timer().betweenSeconds(0, 0.5 * DISPLAY_SECONDS)) {
            drawOriginalCreditsText();
        } else {
            ctx().setGlobalAlpha(creditsScene.fadeProgress);
            drawRemakeCreditsText();
            ctx().setGlobalAlpha(1);
        }
    }

    /**
     * Draws a vertical bar of given width and height. The top and bottom edges are drawn with the edgeColor and are
     * 1 pixel high.
     *
     * @param edgeColor color of upper and lower edges
     * @param innerColor color of inner area
     * @param width width of the bar
     * @param height height of the bar
     * @param topY top y-position
     */
    public void drawHorizontalBar(Color edgeColor, Color innerColor, double width, double height, double topY) {
        requireNonNull(edgeColor);
        requireNonNull(innerColor);
        ctx.save();
        ctx.scale(scaling(), scaling());
        ctx.setFill(edgeColor);
        ctx.fillRect(0, topY, width, height);
        ctx.setFill(innerColor);
        ctx.fillRect(0, topY + 1, width, height - 2);
        ctx.restore();
    }

    public void drawJoypadKeyBinding(JoypadKeyBinding binding) {
        ctx.save();
        requireNonNull(binding);
        ctx.setFont(Font.font(scaled(6)));
        ctx.setStroke(Color.WHITE);
        ctx.strokeText(" [SELECT]=%s   [START]=%s   [BUTTON B]=%s   [BUTTON A]=%s".formatted(
                binding.key(JoypadButton.SELECT),
                binding.key(JoypadButton.START),
                binding.key(JoypadButton.B),
                binding.key(JoypadButton.A)
        ), 0, scaled(TS));
        ctx.strokeText(" [UP]=%s   [DOWN]=%s   [LEFT]=%s   [RIGHT]=%s".formatted(
                binding.key(JoypadButton.UP),
                binding.key(JoypadButton.DOWN),
                binding.key(JoypadButton.LEFT),
                binding.key(JoypadButton.RIGHT)
        ), 0, scaled(2*TS));
        ctx.restore();
    }

    private void drawOriginalCreditsText() {
        print(4, "CREDITS FOR MS PAC-MAN",    0x20,  3);
        print(2, "GAME PROGRAMMER:",          0x23,  4);
        print(3, "FRANZ LANZINGER",           0x23, 10);
        print(2, "SPECIAL THANKS:",           0x23,  4);
        print(1, "JEFF YONAN",                0x23, 10);
        print(4, "DAVE O'RIVA",               0x23, 10);
        print(1, "MS PAC-MAN TM NAMCO LTD",   0x19,  5);
        print(1, "©1990 TENGEN INC",          0x19,  7);
        print(0, "ALL RIGHTS RESERVED",       0x19,  6);
    }

    private void drawRemakeCreditsText() {
        print(4, "CREDITS FOR JAVAFX REMAKE", 0x20,  3);
        print(2, "GAME PROGRAMMER:",          0x23,  4);
        print(3, "ARMIN REICHERT",            0x23, 10);
        print(2, "SPECIAL THANKS:",           0x23,  4);
        print(1, "@RUSSIANMANSMWC",           0x23, 10);
        print(1, "@FLICKY1211",               0x23, 10);
        print(3, "ANDYANA JONSEPH",           0x23, 10);
        print(1, "GITHUB.COM/ARMIN-REICHERT", 0x19,  3);
        print(1, "©2024 MIT LICENSE",         0x19,  6);
        print(0, "ALL RIGHTS GRANTED",        0x19,  5);
    }

    private void print(int numTiles, String text, int colorIndex, int tilesX) {
        fillText(text, nesColor(colorIndex), TS(tilesX), y);
        y += numTiles * TS;
    }

}
