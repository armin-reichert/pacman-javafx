package de.amr.games.pacman.ui2d.rendering;

import de.amr.games.pacman.model.actors.MovingBonus;
import de.amr.games.pacman.ui2d.rendering.ms_pacman.ClapperboardAnimation;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * @author Armin Reichert
 */
public interface MsPacManGameWorldRenderer extends GameWorldRenderer {

    /**
     * Draws a moving bonus entity at its current position (including jump offset).
     *
     * @param g     graphics context
     * @param movingBonus moving bonus entity
     */
    void drawMovingBonus(GraphicsContext g, MovingBonus movingBonus);

    void drawClapperBoard(GraphicsContext g, Font font, Color textColor, ClapperboardAnimation animation, double x, double y);

}
