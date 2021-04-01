package de.amr.games.pacman.ui.fx.entities._2d;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Maze2D {

	private V2i tile;
	private int mazeNumber;
	private boolean flashing;
	private GameRendering2D rendering;

	public void render(GraphicsContext g) {
		Object sprite = flashing ? rendering.getMazeFlashingAnimation(mazeNumber).animate()
				: rendering.getMazeSprite(mazeNumber);
		if (sprite instanceof Rectangle2D) {
			Rectangle2D r = (Rectangle2D) sprite;
			g.drawImage(rendering.spritesheet, r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight(), t(tile.x), t(tile.y),
					r.getWidth(), r.getHeight());

		} else if (sprite instanceof Image) {
			g.drawImage((Image) sprite, t(tile.x), t(tile.y));
		}
	}

	public void setTile(V2i tile) {
		this.tile = tile;
	}

	public void setMazeNumber(int mazeNumber) {
		this.mazeNumber = mazeNumber;
	}

	public void setFlashing(boolean flashing) {
		this.flashing = flashing;
	}

	public void setRendering(GameRendering2D rendering) {
		this.rendering = rendering;
	}
}