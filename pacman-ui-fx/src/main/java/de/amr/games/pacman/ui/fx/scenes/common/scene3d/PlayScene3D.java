package de.amr.games.pacman.ui.fx.scenes.common.scene3d;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static java.util.function.Predicate.not;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import de.amr.games.pacman.controller.BonusEatenEvent;
import de.amr.games.pacman.controller.DeadGhostCountChangeEvent;
import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameEvent;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.controller.PacManLostPowerEvent;
import de.amr.games.pacman.controller.ScatterPhaseStartedEvent;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.ui.fx.rendering.standard.Assets2D;
import de.amr.games.pacman.ui.fx.scenes.common.CameraController;
import de.amr.games.pacman.ui.fx.scenes.common.GameScene;
import de.amr.games.pacman.ui.sound.PacManGameSound;
import de.amr.games.pacman.ui.sound.SoundManager;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.AmbientLight;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SubScene;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * 3D scene displaying the maze and the game play for both, Pac-Man and Ms. Pac-Man games.
 * 
 * @author Armin Reichert
 */
public class PlayScene3D implements GameScene {

	private static final String[] CONGRATS = { "Well done", "Congrats", "Awesome", "You did it", "You're the man*in",
			"WTF", "You old cheating bastard" };

	private final SubScene fxScene;

	private final PerspectiveCamera staticCamera = new PerspectiveCamera(true);
	private final PerspectiveCamera moveableCamera = new PerspectiveCamera(true);
	private final CameraController cameraController = new CameraController(staticCamera);

	private PacManGameController gameController;

	private AmbientLight ambientLight = new AmbientLight(Color.AZURE);
	private PointLight pointLight = new PointLight(Color.AZURE);
	private CoordinateSystem coordSystem;
	private Group tgMaze;
	private Player3D player;
	private Map<Ghost, Ghost3D> ghosts3D;
	private Maze3D maze;
	private List<Energizer3D> energizers;
	private List<Pellet3D> pellets;
	private ScoreNotReally3D score3D;
	private LivesCounter3D livesCounter3D;

	public PlayScene3D() {
		fxScene = new SubScene(new Group(), 800, 600);
		// TODO make this work
		fxScene.addEventHandler(KeyEvent.KEY_PRESSED, cameraController::handleKeyEvent);
		useStaticCamera();
	}

	@Override
	public PacManGameController getController() {
		return gameController;
	}

	@Override
	public void setController(PacManGameController controller) {
		this.gameController = controller;
	}

	@Override
	public OptionalDouble aspectRatio() {
		return OptionalDouble.empty();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "@" + hashCode();
	}

	@Override
	public void stopAllSounds() {
		// TODO
	}

	private void buildSceneGraph() {
		final GameVariant gameVariant = gameController.gameVariant();
		final GameModel game = gameController.game();

		fxScene.setFill(Color.rgb(20, 20, 60));

		maze = new Maze3D(game, Assets2D.getMazeWallColor(game.level.mazeNumber));
		PhongMaterial foodMaterial = Assets3D.foodMaterial(gameVariant, game.level.mazeNumber);

		energizers = game.level.world.energizerTiles()//
				.map(tile -> new Energizer3D(tile, foodMaterial))//
				.collect(Collectors.toList());

		pellets = game.level.world.tiles()//
				.filter(game.level.world::isFoodTile)//
				.filter(not(game.level.world::isEnergizerTile))//
				.map(tile -> new Pellet3D(tile, foodMaterial)).collect(Collectors.toList());

		player = new Player3D(game.player);
		ghosts3D = game.ghosts().collect(Collectors.toMap(Function.identity(), Ghost3D::new));

		score3D = new ScoreNotReally3D();
		livesCounter3D = new LivesCounter3D(game.player, 1, 1);

		tgMaze = new Group();

		tgMaze.setTranslateX(-0.5 * game.level.world.numCols() * TS);
		tgMaze.setTranslateY(-0.5 * game.level.world.numRows() * TS);

		tgMaze.getChildren().addAll(score3D.get(), livesCounter3D.get());
		tgMaze.getChildren().addAll(maze.getWalls());
		tgMaze.getChildren().addAll(collect(energizers));
		tgMaze.getChildren().addAll(collect(pellets));
		tgMaze.getChildren().addAll(player.get());
		tgMaze.getChildren().addAll(collect(ghosts3D.values()));
		tgMaze.getChildren().addAll(ambientLight, pointLight);

		coordSystem = new CoordinateSystem(150);
		fxScene.setRoot(new Group(coordSystem.getNode(), tgMaze));
	}

	private Collection<Node> collect(Collection<? extends Supplier<Node>> items) {
		return items.stream().map(Supplier::get).collect(Collectors.toList());
	}

	@Override
	public void setAvailableSize(double width, double height) {
		// data binding does the job
	}

	@Override
	public SubScene getFXSubScene() {
		return fxScene;
	}

	@Override
	public Camera getActiveCamera() {
		return fxScene.getCamera();
	}

	@Override
	public void start() {
		log("Game scene %s: start", this);
		gameController.addGameEventListener(this::onGameEvent);
		// TODO remove again
		gameController.game().player.immune = true;
		buildSceneGraph();
	}

	@Override
	public void end() {
		log("Game scene %s: end", this);
		gameController.removeGameEventListener(this::onGameEvent);
	}

	@Override
	public void useMoveableCamera(boolean on) {
		if (on) {
			useMoveableCamera();
		} else {
			useStaticCamera();
		}
	}

	private void useStaticCamera() {
		staticCamera.setNearClip(0.1);
		staticCamera.setFarClip(10000.0);
		staticCamera.setTranslateX(0);
		staticCamera.setTranslateY(270);
		staticCamera.setTranslateZ(-460);
		staticCamera.setRotationAxis(Rotate.X_AXIS);
		staticCamera.setRotate(30);
		fxScene.setCamera(staticCamera);
	}

	private void useMoveableCamera() {
		moveableCamera.setNearClip(0.1);
		moveableCamera.setFarClip(10000.0);
		moveableCamera.setTranslateZ(-250);
		moveableCamera.setRotationAxis(Rotate.X_AXIS);
		moveableCamera.setRotate(30);
		fxScene.setCamera(moveableCamera);
	}

	private void updateCamera() {
		if (getActiveCamera() == moveableCamera) {
			double x = Math.min(10, lerp(moveableCamera.getTranslateX(), player.get().getTranslateX()));
			double y = Math.max(50, lerp(moveableCamera.getTranslateY(), player.get().getTranslateY()));
			moveableCamera.setTranslateX(x);
			moveableCamera.setTranslateY(y);
		}
	}

	private static double lerp(double current, double target) {
		return current + (target - current) * 0.02;
	}

	@Override
	public void update() {
		GameModel game = gameController.game();
		score3D.update(game);
		score3D.get().setRotationAxis(Rotate.X_AXIS);
		score3D.get().setRotate(getActiveCamera().getRotate());
		livesCounter3D.get().setVisible(!gameController.isAttractMode());
		livesCounter3D.update(game);
		energizers.forEach(energizer3D -> energizer3D.update(game));
		pellets.forEach(pellet3D -> pellet3D.update(game));
		player.update();
		game.ghosts().map(ghosts3D::get).forEach(Ghost3D::update);
		updateCamera();
	}

	@Override
	public void onGameStateChange(PacManGameState oldState, PacManGameState newState) {
		if (newState == PacManGameState.READY) {
			setSceneColor();
			if (!gameController.isGameRunning() && !gameController.isAttractMode()) {
				gameController.stateTimer().resetSeconds(4.5);
				// TODO use FX sound
				Assets2D.SOUND.get(gameController.gameVariant()).play(PacManGameSound.GAME_READY);
			} else {
				gameController.stateTimer().resetSeconds(2);
			}
		}
		if (oldState == PacManGameState.HUNTING) {
			energizers.forEach(Energizer3D::stopPumping);
		}
		if (newState == PacManGameState.HUNTING) {
			energizers.forEach(Energizer3D::startPumping);
		}
		if (newState == PacManGameState.LEVEL_COMPLETE) {
			playLevelCompleteAnimation();
		}
		if (newState == PacManGameState.LEVEL_STARTING) {
			playLevelStartingAnimation();
		}
	}

	private void onGameEvent(PacManGameEvent gameEvent) {
//		updateSound(gameEvent);
	}

	private void updateSound(PacManGameEvent gameEvent) {
		// TODO FX sound
		SoundManager sounds = null;
		if (gameEvent instanceof ScatterPhaseStartedEvent) {
			ScatterPhaseStartedEvent e = (ScatterPhaseStartedEvent) gameEvent;
			if (e.scatterPhase > 0) {
				sounds.stop(PacManGameSound.SIRENS.get((e.scatterPhase - 1) / 2));
			}
			sounds.loopForever(PacManGameSound.SIRENS.get(e.scatterPhase / 2));
		}

		else if (gameEvent instanceof PacManLostPowerEvent) {
			sounds.stop(PacManGameSound.PACMAN_POWER);
		}

		else if (gameEvent instanceof BonusEatenEvent) {
			sounds.play(PacManGameSound.BONUS_EATEN);
		}

		else if (gameEvent instanceof DeadGhostCountChangeEvent) {
			DeadGhostCountChangeEvent e = (DeadGhostCountChangeEvent) gameEvent;
			if (e.oldCount == 0 && e.newCount > 0) {
				sounds.play(PacManGameSound.GHOST_RETURNING_HOME);
			} else if (e.oldCount > 0 && e.newCount == 0) {
				sounds.stop(PacManGameSound.GHOST_RETURNING_HOME);
			}
		}

	}

	private void setSceneColor() {
		if (gameController.gameVariant() == GameVariant.PACMAN) {
			Color color = Color.rgb(20, 20, 60);
			fxScene.setFill(color);
			ambientLight.setColor(Color.AZURE);
		} else {
			Color mazeColor = Assets2D.getMazeWallColor(gameController.game().level.mazeNumber);
//			fxScene.setFill(mazeColor);
			ambientLight.setColor(mazeColor);
		}
	}

	private void playLevelCompleteAnimation() {
		GameModel game = gameController.game();
		game.player.visible = false;
		game.ghosts().forEach(ghost -> ghost.visible = false);
		gameController.userInterface.showFlashMessage(
				String.format("%s!\n\nLevel %d complete.", CONGRATS[new Random().nextInt(CONGRATS.length)], game.levelNumber),
				2);

		gameController.stateTimer().reset();
		PauseTransition pause = new PauseTransition(Duration.seconds(3));
		pause.setOnFinished(e -> gameController.stateTimer().forceExpiration());
		pause.play();
	}

	private void playLevelStartingAnimation() {
		gameController.stateTimer().reset();
		gameController.userInterface.showFlashMessage("Entering Level " + gameController.game().levelNumber);

		ScaleTransition animation = new ScaleTransition(Duration.seconds(3), tgMaze);
		animation.setDelay(Duration.seconds(2));
		animation.setFromZ(0);
		animation.setToZ(1);
		animation.setOnFinished(e -> gameController.stateTimer().forceExpiration());
		animation.play();
	}
}