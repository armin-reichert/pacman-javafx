package de.amr.games.pacman.ui.fx.scenes.common._2d;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.controller.event.BonusEatenEvent;
import de.amr.games.pacman.controller.event.PacManGainsPowerEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGameStateChangedEvent;
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
 * 2D scene displaying the maze and the game play for both, Pac-Man and Ms.
 * Pac-Man games.
 * 
 * @author Armin Reichert
 */
public class PlayScene2D<RENDERING extends GameRendering2D> extends AbstractGameScene2D<RENDERING> {

	private Maze2D<RENDERING> maze2D;
	private GameScore2D<RENDERING> score2D;
	private GameScore2D<RENDERING> hiscore2D;
	private LivesCounter2D<RENDERING> livesCounter2D;
	private LevelCounter2D<RENDERING> levelCounter2D;
	private GameStateDisplay2D<RENDERING> gameStateDisplay2D;
	private Player2D<RENDERING> player2D;
	private List<Ghost2D<RENDERING>> ghosts2D;
	private Bonus2D<RENDERING> bonus2D;

	private PlaySceneSoundHandler playSceneSoundHandler;
	private SequentialTransition levelCompleteAnimation;

	public PlayScene2D(RENDERING rendering, SoundManager sounds) {
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

		maze2D = new Maze2D<>(game().currentLevel, rendering);
		maze2D.setLeftUpperCorner(new V2i(0, 3));

		livesCounter2D = new LivesCounter2D<>(rendering);
		livesCounter2D.setLeftUpperCorner(new V2i(2, 34));
		livesCounter2D.setLifeCountSupplier(() -> game().lives);

		levelCounter2D = new LevelCounter2D<>(rendering);
		levelCounter2D.setRightUpperCorner(new V2i(25, 34));
		levelCounter2D.setLevelSymbols(game().levelSymbols);
		levelCounter2D.setLevelNumberSupplier(() -> game().currentLevelNumber);

		score2D = new GameScore2D<>(rendering);
		score2D.setTitle("SCORE");
		score2D.setLeftUpperCorner(new V2i(1, 1));
		score2D.setLevelSupplier(() -> game().currentLevelNumber);
		score2D.setPointsSupplier(() -> game().score);

		hiscore2D = new GameScore2D<>(rendering);
		hiscore2D.setTitle("HI SCORE");
		hiscore2D.setLeftUpperCorner(new V2i(16, 1));
		hiscore2D.setPointsSupplier(() -> game().highscorePoints);
		hiscore2D.setLevelSupplier(() -> game().highscoreLevel);

		gameStateDisplay2D = new GameStateDisplay2D<>(rendering);
		gameStateDisplay2D
				.setStateSupplier(() -> gameController.isAttractMode() ? PacManGameState.GAME_OVER : gameController.state);

		player2D = new Player2D<>(game().player, rendering);
		player2D.getDyingAnimation().delay(120).onStart(() -> {
			game().ghosts().forEach(ghost -> ghost.visible = false);
		});

		ghosts2D = game().ghosts().map(ghost -> new Ghost2D<>(ghost, rendering)).collect(Collectors.toList());

		bonus2D = new Bonus2D<>(rendering);

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

	private void onGameStateChange(PacManGameStateChangedEvent event) {
		playSceneSoundHandler.onGameStateChange(event.oldGameState, event.newGameState);

		// enter HUNTING
		if (event.newGameState == PacManGameState.HUNTING) {
			maze2D.getEnergizerBlinking().restart();
			player2D.getMunchingAnimations().values().forEach(TimedSequence::restart);
			letGhostsKick(true);
		}

		// enter GHOST_DYING
		if (event.newGameState == PacManGameState.GHOST_DYING) {
			game().player.visible = false;
		}

		// exit GHOST_DYING
		if (event.oldGameState == PacManGameState.GHOST_DYING) {
			game().player.visible = true;
		}

		// enter PACMAN_DYING
		if (event.newGameState == PacManGameState.PACMAN_DYING) {
			maze2D.getEnergizerBlinking().reset();
			letGhostsKick(false);
			player2D.getDyingAnimation().restart();
		}

		// enter LEVEL_COMPLETE
		if (event.newGameState == PacManGameState.LEVEL_COMPLETE) {
			game().ghosts().forEach(ghost -> ghost.visible = false);
			gameController.stateTimer().reset();
			levelCompleteAnimation.play();
		}

		// enter LEVEL_STARTING
		if (event.newGameState == PacManGameState.LEVEL_STARTING) {
			maze2D = new Maze2D<>(game().currentLevel, rendering);
			maze2D.setLeftUpperCorner(new V2i(0, 3));
			gameController.stateTimer().reset(1);
			gameController.stateTimer().start();
		}

		// enter GAME_OVER
		if (event.newGameState == PacManGameState.GAME_OVER) {
			maze2D.getEnergizerBlinking().reset();
			letGhostsKick(false);
		}
	}

	@Override
	public void onGameEvent(PacManGameEvent gameEvent) {
		playSceneSoundHandler.onGameEvent(gameEvent);

		if (gameEvent instanceof PacManGameStateChangedEvent) {
			onGameStateChange((PacManGameStateChangedEvent) gameEvent);
		}

		if (gameEvent instanceof PacManGainsPowerEvent) {
			game().ghosts(GhostState.FRIGHTENED).forEach(ghost -> {
				Ghost2D<RENDERING> ghost2D = ghosts2D.get(ghost.id);
				ghost2D.getFlashingAnimation().reset();
				ghost2D.getFrightenedAnimation().restart();
			});
		}

		else if (gameEvent instanceof BonusEatenEvent) {
			if (bonus2D.getJumpAnimation() != null) {
				bonus2D.getJumpAnimation().reset();
			}
		}
	}

	private void letGhostsKick(boolean on) {
		ghosts2D.forEach(
				ghost2D -> ghost2D.getKickingAnimations().values().forEach(on ? TimedSequence::restart : TimedSequence::reset));
	}

	// TODO simplify
	private void handleGhostsFlashing(TickTimerEvent e) {
		if (e.type == TickTimerEvent.Type.HALF_EXPIRED) {
			game().ghosts(GhostState.FRIGHTENED).forEach(ghost -> {
				Ghost2D<RENDERING> ghost2D = ghosts2D.get(ghost.id);
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
		game().ghosts(GhostState.LOCKED)
				.forEach(ghost -> ghosts2D.get(ghost.id).setLooksFrightened(game().player.powerTimer.isRunning()));

		Stream.concat(Stream.of(score2D, hiscore2D, levelCounter2D, maze2D, gameStateDisplay2D, bonus2D, player2D),
				ghosts2D.stream()).forEach(r -> r.render(gc));
	}
}