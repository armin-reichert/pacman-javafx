package de.amr.games.pacman.ui.fx.scenes.common._2d;

import static de.amr.games.pacman.model.world.PacManGameWorld.t;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGameStateChangedEvent;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.ui.fx.entities._2d.Bonus2D;
import de.amr.games.pacman.ui.fx.entities._2d.GameScore2D;
import de.amr.games.pacman.ui.fx.entities._2d.Ghost2D;
import de.amr.games.pacman.ui.fx.entities._2d.LivesCounter2D;
import de.amr.games.pacman.ui.fx.entities._2d.Maze2D;
import de.amr.games.pacman.ui.fx.entities._2d.Player2D;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.scene.paint.Color;

/**
 * 2D scene displaying the maze and the game play for both, Pac-Man and Ms.
 * Pac-Man games.
 * 
 * @param <RENDERING> Type of rendering, Pac-Man or Ms. Pac-Man rendering.
 * 
 * @author Armin Reichert
 */
public class PlayScene2D<RENDERING extends Rendering2D> extends AbstractGameScene2D<RENDERING> {

	public Maze2D<RENDERING> maze2D;
	public GameScore2D<RENDERING> score2D;
	public GameScore2D<RENDERING> hiscore2D;
	public LivesCounter2D<RENDERING> livesCounter2D;
	public Player2D<RENDERING> player2D;
	public List<Ghost2D<RENDERING>> ghosts2D;
	public Bonus2D<RENDERING> bonus2D;

	private final PlayScene2DAnimationController animationController;

	public PlayScene2D(RENDERING rendering, SoundManager sounds) {
		super(UNSCALED_SCENE_WIDTH, UNSCALED_SCENE_HEIGHT, rendering, sounds);
		animationController = new PlayScene2DAnimationController(this, sounds);
	}

	@Override
	public void setGameController(PacManGameController gameController) {
		super.setGameController(gameController);
		animationController.setGameController(gameController);
	}

	@Override
	public void start() {
		super.start();

		maze2D = new Maze2D<>(new V2i(0, 3), rendering);
		maze2D.setGameLevel(game().currentLevel);

		livesCounter2D = new LivesCounter2D<>(new V2i(2, 34), rendering);
		livesCounter2D.lives = game().lives;

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

		player2D = new Player2D<>(game().player, rendering);
		player2D.getDyingAnimation().delay(120).onStart(() -> {
			game().ghosts().forEach(ghost -> ghost.visible = false);
		});

		ghosts2D = game().ghosts().map(ghost -> new Ghost2D<>(ghost, rendering)).collect(Collectors.toList());

		bonus2D = new Bonus2D<>(rendering);

		game().player.powerTimer.addEventListener(animationController::handleGhostsFlashing);
		animationController.init();
	}

	@Override
	public void end() {
		game().player.powerTimer.removeEventListener(animationController::handleGhostsFlashing);
		super.end();
	}

	@Override
	public void update() {
		livesCounter2D.lives = game().lives;
		animationController.update();
	}

	private void onGameStateChange(PacManGameStateChangedEvent event) {
		if (event.newGameState == PacManGameState.LEVEL_STARTING) {
			maze2D.setGameLevel(event.gameModel.currentLevel);
			// wait 1 second
			gameController.stateTimer().reset(60);
			gameController.stateTimer().start();
			animationController.init();
		}
	}

	@Override
	public void onGameEvent(PacManGameEvent gameEvent) {
		if (gameEvent instanceof PacManGameStateChangedEvent) {
			onGameStateChange((PacManGameStateChangedEvent) gameEvent);
		}
		animationController.onGameEvent(gameEvent);
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

		Stream.concat(Stream.of(score2D, hiscore2D, maze2D, bonus2D, player2D), ghosts2D.stream())
				.forEach(r -> r.render(gc));
		renderGameState();
		renderLevelCounter(new V2i(25, 34));
	}

	private void renderGameState() {
		PacManGameState state = gameController.isAttractMode() ? PacManGameState.GAME_OVER : gameController.state;
		if (state == PacManGameState.GAME_OVER) {
			gc.setFont(rendering.getScoreFont());
			gc.setFill(Color.RED);
			gc.fillText("GAME", t(9), t(21));
			gc.fillText("OVER!", t(15), t(21));
		} else if (state == PacManGameState.READY) {
			gc.setFont(rendering.getScoreFont());
			gc.setFill(Color.YELLOW);
			gc.fillText("READY!", t(11), t(21));
		}
	}
}