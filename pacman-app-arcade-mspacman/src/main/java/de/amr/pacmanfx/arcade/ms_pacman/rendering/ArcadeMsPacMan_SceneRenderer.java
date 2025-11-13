/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.arcade.ms_pacman.scenes.Marquee;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.rendering.BaseSpriteRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.api.ArcadePalette.ARCADE_RED;

public class ArcadeMsPacMan_SceneRenderer extends BaseSpriteRenderer {

    private final GameUI_Config uiConfig;

    public ArcadeMsPacMan_SceneRenderer(Canvas canvas, GameUI_Config uiConfig) {
        super(canvas, uiConfig.spriteSheet());
        this.uiConfig = uiConfig;
    }

    /**
     * 6 of the 96 light bulbs are bright in each frame, shifting counter-clockwise every tick.
     * <p>
     * The bulbs on the left border however are switched off every second frame. This is
     * probably a bug in the original Arcade game.
     * </p>
     */
    public void drawMarquee(Marquee marquee) {
        long tick = marquee.timer().tickCount();
        ctx.setFill(marquee.bulbOffColor());
        for (int bulbIndex = 0; bulbIndex < marquee.totalBulbCount(); ++bulbIndex) {
            drawMarqueeBulb(marquee, bulbIndex);
        }
        int firstBrightIndex = (int) (tick % marquee.totalBulbCount());
        ctx.setFill(marquee.bulbOnColor());
        for (int i = 0; i < marquee.brightBulbsCount(); ++i) {
            drawMarqueeBulb(marquee, (firstBrightIndex + i * marquee.brightBulbsDistance()) % marquee.totalBulbCount());
        }
        // simulate bug from original Arcade game
        ctx.setFill(marquee.bulbOffColor());
        for (int bulbIndex = 81; bulbIndex < marquee.totalBulbCount(); bulbIndex += 2) {
            drawMarqueeBulb(marquee, bulbIndex);
        }
    }

    private void drawMarqueeBulb(Marquee marquee, int bulbIndex) {
        final double minX = marquee.x(), minY = marquee.y();
        final double maxX = marquee.x() + marquee.width(), maxY = marquee.y() + marquee.height();
        double x, y;
        if (bulbIndex <= 33) { // lower edge left-to-right
            x = minX + 4 * bulbIndex;
            y = maxY;
        }
        else if (bulbIndex <= 48) { // right edge bottom-to-top
            x = maxX;
            y = 4 * (70 - bulbIndex);
        }
        else if (bulbIndex <= 81) { // upper edge right-to-left
            x = 4 * (marquee.totalBulbCount() - bulbIndex);
            y = minY;
        }
        else { // left edge top-to-bottom
            x = minX;
            y = 4 * (bulbIndex - 59);
        }
        ctx.fillRect(scaled(x), scaled(y), scaled(2), scaled(2));
    }

    public void drawMidwayCopyright(double x, double y) {
        Image logo = uiConfig.assets().image("logo.midway");
        ctx.drawImage(logo, scaled(x), scaled(y + 2), scaled(TS(4) - 2), scaled(TS(4)));
        ctx.setFont(arcadeFont8());
        ctx.setFill(ARCADE_RED);
        ctx.fillText("©", scaled(x + TS(5)), scaled(y + TS(2)) + 2);
        ctx.fillText("MIDWAY MFG CO", scaled(x + TS(7)), scaled(y + TS(2)));
        ctx.fillText("1980/1981", scaled(x + TS(8)), scaled(y + TS(4)));
    }
}