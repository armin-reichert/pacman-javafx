package de.amr.games.pacman.ui.fx.entities._2d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class Maze2D {

	private final GameLevel gameLevel;
	private final GameRendering2D rendering;
	private V2i tile;
	private Timeline flashingAnimation;
	private boolean flashImage;

	public Maze2D(GameLevel gameLevel, GameRendering2D rendering) {
		this.gameLevel = gameLevel;
		this.rendering = rendering;
		KeyFrame changeMazeImage = new KeyFrame(Duration.millis(150), e -> flashImage = !flashImage);
		flashingAnimation = new Timeline(changeMazeImage);
		flashImage = false;
		flashingAnimation.setCycleCount(2 * gameLevel.numFlashes);
	}

	public void setTile(V2i tile) {
		this.tile = tile;
	}

	public boolean isFlashing() {
		return flashingAnimation.getStatus() == Status.RUNNING;
	}

	public Timeline getFlashingAnimation() {
		return flashingAnimation;
	}

	public void render(GraphicsContext g) {
		if (flashingAnimation.getStatus() == Status.RUNNING) {
			Image image = flashImage ? rendering.getMazeFlashImage(gameLevel.mazeNumber)
					: rendering.getMazeEmptyImage(gameLevel.mazeNumber);
			g.drawImage(image, t(tile.x), t(tile.y));
		} else {
			Image image = rendering.getMazeFullImage(gameLevel.mazeNumber);
			g.drawImage(image, t(tile.x), t(tile.y));
			g.setFill(Color.BLACK);
			gameLevel.world.tiles().filter(gameLevel::isFoodRemoved).forEach(foodTile -> {
				g.fillRect(foodTile.x * TS, foodTile.y * TS, TS, TS);
			});
		}
	}
}