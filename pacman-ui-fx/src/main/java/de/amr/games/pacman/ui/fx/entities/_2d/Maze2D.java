package de.amr.games.pacman.ui.fx.entities._2d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.util.List;
import java.util.stream.Collectors;

import de.amr.games.pacman.lib.TimedSequence;
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

/**
 * 2D UI of the maze for a given game level. Implements the flashing animation played when the game
 * level is complete.
 * 
 * @author Armin Reichert
 */
public class Maze2D {

	private final GameLevel gameLevel;
	private final GameRendering2D rendering;
	private V2i tile;
	private Timeline flashingAnimation;
	private boolean flashImage;
	private List<Energizer2D> energizers2D;
	private TimedSequence<Boolean> blinking = TimedSequence.pulse().frameDuration(10);

	public Maze2D(GameLevel gameLevel, GameRendering2D rendering) {
		this.gameLevel = gameLevel;
		this.rendering = rendering;
		KeyFrame changeMazeImage = new KeyFrame(Duration.millis(150), e -> flashImage = !flashImage);
		flashingAnimation = new Timeline(changeMazeImage);
		flashImage = false;
		flashingAnimation.setCycleCount(2 * gameLevel.numFlashes);
		energizers2D = gameLevel.world.energizerTiles().map(energizerTile -> new Energizer2D(energizerTile, blinking))
				.collect(Collectors.toList());
	}

	public void setLeftUpperCorner(V2i tile) {
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
			energizers2D.forEach(energizer2D -> energizer2D.render(g));
			blinking.animate();
			g.setFill(Color.BLACK);
			gameLevel.world.tiles().filter(gameLevel::isFoodRemoved).forEach(foodTile -> {
				g.fillRect(foodTile.x * TS, foodTile.y * TS, TS, TS);
			});
		}
	}

	public void startEnergizerAnimation() {
		energizers2D.forEach(energizer2D -> energizer2D.getBlinkingAnimation().restart());
	}

	public void stopEnergizerAnimation() {
		energizers2D.forEach(energizer2D -> energizer2D.getBlinkingAnimation().reset());
	}
}