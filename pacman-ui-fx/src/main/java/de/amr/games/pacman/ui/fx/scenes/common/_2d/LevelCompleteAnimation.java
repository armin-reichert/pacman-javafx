package de.amr.games.pacman.ui.fx.scenes.common._2d;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.common.AbstractGameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.animation.TimedSequence;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.util.Duration;

class LevelCompleteAnimation {

	private final SequentialTransition sequence;
	private final Timeline flashing;
	private TimedSequence<?> mazeFlashing; // TODO get rid og this
	private int imageIndex;

	public LevelCompleteAnimation(PacManGameController gameController, int numFlashes) {
		GameVariant variant = gameController.gameVariant();
		AbstractGameModel game = gameController.game();
		GameRendering2D rendering = variant == GameVariant.MS_PACMAN ? GameRendering2D.RENDERING_MS_PACMAN
				: GameRendering2D.RENDERING_PACMAN;

		mazeFlashing = rendering.mazeFlashing(game.currentLevel.mazeNumber);
		imageIndex = 1;

		flashing = new Timeline(new KeyFrame(Duration.millis(150), e -> {
			imageIndex = (imageIndex + 1) % 2;
		}));
		flashing.setCycleCount(2 * numFlashes);

		PauseTransition start = new PauseTransition(Duration.seconds(2));
		start.setOnFinished(e -> game.player.visible = false);
		PauseTransition end = new PauseTransition(Duration.seconds(1));
		sequence = new SequentialTransition(start, flashing, end);
		sequence.setOnFinished(e -> gameController.stateTimer().forceExpiration());
	}

	public Image getCurrentMazeImage() {
		return (Image) mazeFlashing.frame(imageIndex);
	}

	public void play() {
		sequence.playFromStart();
	}

	public boolean isRunning() {
		return sequence.getStatus() == Status.RUNNING;
	}

	public Duration getTotalDuration() {
		return sequence.getTotalDuration();
	}
}