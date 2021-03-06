package de.amr.games.pacman.ui.fx.entities._2d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.util.List;
import java.util.stream.Collectors;

import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D;
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
public class Maze2D implements Renderable2D {

	private final int x;
	private final int y;
	private final Rendering2D rendering;
	private GameLevel gameLevel;
	private Timeline flashingAnimation;
	private boolean bright;
	private List<Energizer2D> energizers2D;
	private TimedSequence<Boolean> energizerBlinking = TimedSequence.pulse().frameDuration(10);

	public Maze2D(V2i leftUpperCorner, Rendering2D rendering) {
		x = t(leftUpperCorner.x);
		y = t(leftUpperCorner.y);
		this.rendering = rendering;
		KeyFrame switchImage = new KeyFrame(Duration.millis(150), e -> bright = !bright);
		flashingAnimation = new Timeline(switchImage);
		bright = false;
	}

	public void setGameLevel(GameLevel gameLevel) {
		this.gameLevel = gameLevel;
		energizers2D = gameLevel.world.energizerTiles().map(energizerTile -> {
			Energizer2D energizer2D = new Energizer2D();
			energizer2D.setTile(energizerTile);
			energizer2D.setBlinkingAnimation(energizerBlinking);
			return energizer2D;
		}).collect(Collectors.toList());
		flashingAnimation.setCycleCount(2 * gameLevel.numFlashes);
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
			Image image = bright ? rendering.getMazeFlashImage(gameLevel.mazeNumber)
					: rendering.getMazeEmptyImage(gameLevel.mazeNumber);
			g.drawImage(image, x, y);
		} else {
			Image image = rendering.getMazeFullImage(gameLevel.mazeNumber);
			g.drawImage(image, x, y);
			energizers2D.forEach(energizer2D -> energizer2D.render(g));
			energizerBlinking.animate();
			g.setFill(Color.BLACK);
			gameLevel.world.tiles().filter(gameLevel::isFoodRemoved).forEach(emptyFoodTile -> {
				g.fillRect(t(emptyFoodTile.x), t(emptyFoodTile.y), TS, TS);
			});
		}
	}
}