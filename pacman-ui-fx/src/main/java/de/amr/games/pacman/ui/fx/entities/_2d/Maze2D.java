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
 * 2D representation of the maze for a given game level. Implements the flashing animation played
 * when the game level is complete.
 * 
 * @author Armin Reichert
 */
public class Maze2D<RENDERING extends GameRendering2D> implements Renderable2D<RENDERING> {

	private RENDERING rendering;
	private GameLevel gameLevel;
	private V2i tile;
	private Timeline flashingAnimation;
	private boolean flashImage;
	private List<Energizer2D<RENDERING>> energizers2D;
	private TimedSequence<Boolean> energizerBlinking = TimedSequence.pulse().frameDuration(10);

	public Maze2D(GameLevel gameLevel, RENDERING rendering) {
		this.rendering = rendering;
		this.gameLevel = gameLevel;
		KeyFrame changeMazeImage = new KeyFrame(Duration.millis(150), e -> flashImage = !flashImage);
		flashingAnimation = new Timeline(changeMazeImage);
		flashImage = false;
		flashingAnimation.setCycleCount(2 * gameLevel.numFlashes);
		energizers2D = gameLevel.world.energizerTiles().map(energizerTile -> {
			Energizer2D<RENDERING> energizer2D = new Energizer2D<RENDERING>(rendering);
			energizer2D.setTile(energizerTile);
			energizer2D.setBlinkingAnimation(energizerBlinking);
			return energizer2D;
		}).collect(Collectors.toList());
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

	public TimedSequence<Boolean> getEnergizerBlinking() {
		return energizerBlinking;
	}

	@Override
	public void render(GraphicsContext g) {
		if (flashingAnimation.getStatus() == Status.RUNNING) {
			Image image = flashImage ? rendering.getMazeFlashImage(gameLevel.mazeNumber)
					: rendering.getMazeEmptyImage(gameLevel.mazeNumber);
			g.drawImage(image, t(tile.x), t(tile.y));
		} else {
			Image image = rendering.getMazeFullImage(gameLevel.mazeNumber);
			g.drawImage(image, t(tile.x), t(tile.y));
			energizers2D.forEach(energizer2D -> energizer2D.render(g));
			energizerBlinking.animate();
			g.setFill(Color.BLACK);
			gameLevel.world.tiles().filter(gameLevel::isFoodRemoved).forEach(foodTile -> {
				g.fillRect(foodTile.x * TS, foodTile.y * TS, TS, TS);
			});
		}
	}
}