package de.amr.games.pacman.ui.fx.mspacman;

import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.model.guys.GameEntity;
import de.amr.games.pacman.ui.fx.PacManGameFXUI;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class Flap extends GameEntity {

	static final MsPacMan_SceneRendering rendering = PacManGameFXUI.MS_PACMAN_RENDERING;

	public int sceneNumber = 1;
	public String sceneTitle = "Scene #" + sceneNumber;
	public Animation<Rectangle2D> animation = rendering.getFlapAnim();
	public Font font = Font.font(rendering.getScoreFont().getName(), FontWeight.THIN, 8);

	public void draw(GraphicsContext g) {
		if (visible) {
			animation.animate();
			rendering.drawRegion(g, animation.frame(), position.x, position.y);
			g.setFont(font);
			g.setFill(Color.rgb(222, 222, 225, 0.8));
			g.fillText(sceneNumber + "", position.x + 20, position.y + 30);
			if (animation.isRunning()) {
				g.setFont(rendering.getScoreFont());
				g.fillText(sceneTitle, position.x + 40, position.y + 20);
			}
		}
	}
}