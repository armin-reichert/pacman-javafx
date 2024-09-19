package de.amr.games.pacman.ui2d.rendering.ms_pacman;

import de.amr.games.pacman.model.actors.MovingBonus;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.t;

/**
 * @author Armin Reichert
 */
public interface MsPacManGameWorldRenderer extends GameWorldRenderer {

    /**
     * Draws a moving bonus entity at its current position (including jump offset).
     *
     * @param g     graphics context
     * @param bonus moving bonus entity
     */
    void drawMovingBonus(GraphicsContext g, MovingBonus bonus);

    void drawClapperBoard(GraphicsContext g, Font font, Color textColor, ClapperboardAnimation animation, double x, double y);

    default void drawMsPacManMidwayCopyright(GraphicsContext g, Image image, double x, double y, Color color, Font font) {
        spriteRenderer().drawImageScaled(g, image, x, y + 2, t(4) - 2, t(4));
        g.setFont(font);
        g.setFill(color);
        g.fillText("©", scaled(x + TS * 5), scaled(y + TS * 2 + 2));
        g.fillText("MIDWAY MFG CO", scaled(x + TS * 7), scaled(y + TS * 2));
        g.fillText("1980/1981", scaled(x + TS * 8), scaled(y + TS * 4));
    }
}