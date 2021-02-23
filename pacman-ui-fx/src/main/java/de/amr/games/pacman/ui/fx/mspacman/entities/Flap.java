package de.amr.games.pacman.ui.fx.mspacman.entities;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.model.guys.GameEntity;
import de.amr.games.pacman.ui.fx.rendering.FXRendering;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * The flap used in the intermission scenes.
 * 
 * @author Armin Reichert
 */
public class Flap extends GameEntity {

	private final int sceneNumber;
	private final String sceneTitle;

	public Flap(int number, String title) {
		sceneNumber = number;
		sceneTitle = title;
		animation = Animation.of( //
				new Rectangle2D(456, 208, 32, 32), //
				new Rectangle2D(488, 208, 32, 32), //
				new Rectangle2D(520, 208, 32, 32), //
				new Rectangle2D(488, 208, 32, 32), //
				new Rectangle2D(456, 208, 32, 32)//
		).repetitions(1).frameDuration(4);
	}

	public void draw(GraphicsContext g, FXRendering rendering) {
		if (visible) {
			rendering.drawSprite(g, (Rectangle2D) animation.animate(), position.x, position.y);
			g.setFont(Font.font(rendering.getScoreFont().getName(), FontWeight.THIN, 8));
			g.setFill(Color.rgb(222, 222, 225, 0.75));
			g.fillText(sceneNumber + "", position.x + 20, position.y + 30);
			g.setFont(rendering.getScoreFont());
			g.fillText(sceneTitle, position.x + 40, position.y + 20);
		}
	}
}