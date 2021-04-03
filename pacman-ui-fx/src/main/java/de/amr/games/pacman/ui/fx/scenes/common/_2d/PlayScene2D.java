package de.amr.games.pacman.ui.fx.scenes.common._2d;

import java.util.List;
import java.util.stream.Collectors;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.controller.event.BonusEatenEvent;
import de.amr.games.pacman.controller.event.ExtraLifeEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.lib.TickTimerEvent;
import de.amr.games.pacman.lib.TimedSequence;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.ui.fx.entities._2d.Bonus2D;
import de.amr.games.pacman.ui.fx.entities._2d.GameScore2D;
import de.amr.games.pacman.ui.fx.entities._2d.GameStateDisplay2D;
import de.amr.games.pacman.ui.fx.entities._2d.Ghost2D;
import de.amr.games.pacman.ui.fx.entities._2d.LevelCounter2D;
import de.amr.games.pacman.ui.fx.entities._2d.LivesCounter2D;
import de.amr.games.pacman.ui.fx.entities._2d.Maze2D;
import de.amr.games.pacman.ui.fx.entities._2d.Player2D;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import de.amr.games.pacman.ui.fx.scenes.common.PlaySceneSoundHandler;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.util.Duration;

/**
 * 2D scene displaying the maze and the game play for both, Pac-Man and Ms. Pac-Man games.
 * 
 * @author Armin Reichert
 */
public class PlayScene2D extends AbstractGameScene2D {

	private Maze2D maze2D;
	private GameScore2D score2D;
	private GameScore2D hiscore2D;
	private LivesCounter2D livesCounter2D;
	private LevelCounter2D levelCounter2D;
	private GameStateDisplay2D gameStateDisplay2D;
	private Player2D player2D;
	private List<Ghost2D> ghosts2D;
	private Bonus2D bonus2D;

	private PlaySceneSoundHandler playSceneSoundHandler;
	private SequentialTransition levelCompleteAnimation;

	public PlayScene2D(GameRendering2D rendering, SoundManager sounds) {
		super(rendering, sounds);
	}

	@Override
	public void setGameController(PacManGameController gameController) {
		super.setGameController(gameController);
		playSceneSoundHandler = new PlaySceneSoundHandler(gameController, sounds);
	}

	@Override
	public void start() {
		super.start();

		maze2D = new Maze2D(game().currentLevel, rendering);
		maze2D.setLeftUpperCorner(new V2i(0, 3));

		livesCounter2D = new LivesCounter2D(() -> game().lives);
		livesCounter2D.setLeftUpperCorner(new V2i(2, 34));
		livesCounter2D.setRendering(rendering);

		levelCounter2D = new LevelCounter2D(() -> game().currentLevelNumber);
		levelCounter2D.setRightUpperCorner(new V2i(25, 34));
		levelCounter2D.setLevelSymbols(game().levelSymbols);
		levelCounter2D.setRendering(rendering);

		score2D = new GameScore2D(() -> game().score, () -> game().currentLevelNumber);
		score2D.setLeftUpperCorner(new V2i(1, 1));
		score2D.setFont(rendering.getScoreFont());

		hiscore2D = new GameScore2D(() -> game().highscorePoints, () -> game().highscoreLevel);
		hiscore2D.setTitle("HI SCORE");
		hiscore2D.setLeftUpperCorner(new V2i(16, 1));
		hiscore2D.setFont(rendering.getScoreFont());

		gameStateDisplay2D = new GameStateDisplay2D(
				() -> gameController.isAttractMode() ? PacManGameState.GAME_OVER : gameController.state);
		gameStateDisplay2D.setFont(rendering.getScoreFont());

		player2D = new Player2D(game().player);
		player2D.setRendering(rendering);
		player2D.getDyingAnimation().delay(120).onStart(() -> {
			game().ghosts().forEach(ghost -> ghost.visible = false);
		});

		ghosts2D = game().ghosts().map(ghost -> new Ghost2D(ghost, () -> game().player.powerTimer.isRunning()))
				.collect(Collectors.toList());
		ghosts2D.forEach(ghost2D -> ghost2D.setRendering(rendering));

		bonus2D = new Bonus2D();
		bonus2D.setRendering(rendering);

		levelCompleteAnimation = new SequentialTransition(maze2D.getFlashingAnimation(),
				new PauseTransition(Duration.seconds(1)));
		levelCompleteAnimation.setDelay(Duration.seconds(2));
		levelCompleteAnimation.setOnFinished(e -> gameController.stateTimer().forceExpiration());

		game().player.powerTimer.addEventListener(this::handleGhostsFlashing);
	}

	@Override
	public void end() {
		game().player.powerTimer.removeEventListener(this::handleGhostsFlashing);
		super.end();
	}

	@Override
	public void update() {
		playSceneSoundHandler.onUpdate();
	}

	@Override
	public void onGameStateChange(PacManGameState oldState, PacManGameState newState) {
		playSceneSoundHandler.onGameStateChange(oldState, newState);

		// enter HUNTING
		if (newState == PacManGameState.HUNTING) {
			maze2D.startEnergizerAnimation();
			player2D.getMunchingAnimations().values().forEach(TimedSequence::restart);
			ghosts2D.forEach(ghost2D -> ghost2D.getKickingAnimations().values().forEach(TimedSequence::restart));
		}

		// enter PACMAN_DYING
		if (newState == PacManGameState.PACMAN_DYING) {
			maze2D.stopEnergizerAnimation();
			ghosts2D.forEach(ghost2D -> ghost2D.getKickingAnimations().values().forEach(TimedSequence::reset));
			player2D.getDyingAnimation().restart();
		}

		// enter LEVEL_COMPLETE
		if (newState == PacManGameState.LEVEL_COMPLETE) {
			game().ghosts().forEach(ghost -> ghost.visible = false);
			gameController.stateTimer().reset();
			levelCompleteAnimation.play();
		}

		// enter LEVEL_STARTING
		if (newState == PacManGameState.LEVEL_STARTING) {
			maze2D = new Maze2D(game().currentLevel, rendering);
			maze2D.setLeftUpperCorner(new V2i(0, 3));
			gameController.stateTimer().reset(1);
			gameController.stateTimer().start();
		}

		// enter GAME_OVER
		if (newState == PacManGameState.GAME_OVER) {
			maze2D.stopEnergizerAnimation();
			ghosts2D.forEach(ghost2D -> ghost2D.getKickingAnimations().values().forEach(TimedSequence::reset));
		}
	}

	@Override
	public void onGameEvent(PacManGameEvent gameEvent) {
		playSceneSoundHandler.onGameEvent(gameEvent);

		if (gameEvent instanceof PacManGainsPowerEvent) {
			ghosts2D.stream().filter(ghost2D -> ghost2D.ghost.is(GhostState.FRIGHTENED)).forEach(ghost2D -> {
				ghost2D.getFlashingAnimation().reset();
				ghost2D.getFrightenedAnimation().restart();
			});
		}

		else if (gameEvent instanceof BonusEatenEvent) {
			if (bonus2D.getJumpAnimation() != null) {
				bonus2D.getJumpAnimation().reset();
			}
		}

		else if (gameEvent instanceof ExtraLifeEvent) {
			gameController.userInterface.showFlashMessage("Extra life!");
		}
	}

	private void handleGhostsFlashing(TickTimerEvent e) {
		if (e.type == TickTimerEvent.Type.HALF_EXPIRED) {
			ghosts2D.stream().filter(ghost2D -> ghost2D.ghost.is(GhostState.FRIGHTENED)).forEach(ghost2D -> {
				TimedSequence<?> flashing = ghost2D.getFlashingAnimation();
				long frameTime = e.ticks / (game().currentLevel.numFlashes * flashing.numFrames());
				flashing.frameDuration(frameTime).repetitions(game().currentLevel.numFlashes).restart();
			});
		}
	}

	@Override
	public void render() {
		if (!gameController.isAttractMode()) {
			livesCounter2D.render(gc);
			score2D.setShowPoints(true);
		} else {
			score2D.setShowPoints(false);
		}
		score2D.render(gc);
		hiscore2D.render(gc);
		levelCounter2D.render(gc);
		maze2D.render(gc);
		gameStateDisplay2D.render(gc);
		bonus2D.render(gc);
		player2D.render(gc);
		ghosts2D.forEach(ghost2D -> ghost2D.render(gc));
	}
}