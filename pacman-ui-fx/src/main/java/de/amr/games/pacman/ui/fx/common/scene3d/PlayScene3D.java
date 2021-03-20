package de.amr.games.pacman.ui.fx.common.scene3d;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static java.util.function.Predicate.not;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameType;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.ui.fx.common.CameraController;
import de.amr.games.pacman.ui.fx.common.GameScene;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.collections.ObservableList;
import javafx.scene.AmbientLight;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SubScene;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
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

	private final PerspectiveCamera staticCamera;
	private final PerspectiveCamera moveableCamera;
	private final CameraController cameraController;

	private PacManGameController gameController;

	private CoordinateSystem coordSystem;
	private Group tgMaze;
	private Player3D player;
	private Map<Ghost, Ghost3D> ghosts3D;
	private List<Brick3D> bricks;
	private List<Energizer3D> energizers;
	private List<Pellet3D> pellets;
	private ScoreNotReally3D score3D;
	private Group tgLivesCounter;

	public PlayScene3D(Stage stage) {
		staticCamera = new PerspectiveCamera(true);
		moveableCamera = new PerspectiveCamera(true);
		fxScene = new SubScene(new Group(), stage.getScene().getWidth(), stage.getScene().getHeight());
		fxScene.setFill(Color.BLACK);
		useStaticCamera();
		cameraController = new CameraController(staticCamera);
		// TODO why doesn't subscene get key events?
		stage.addEventHandler(KeyEvent.KEY_PRESSED, cameraController::handleKeyEvent);
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

	private void buildSceneGraph() {
		final GameType gameType = gameController.selectedGameType();
		final GameModel game = gameController.selectedGame();

		bricks = game.level.world.tiles()//
				.filter(game.level.world::isWall)//
				.map(tile -> new Brick3D(tile, Assets3D.randomWallMaterial()))//
				.collect(Collectors.toList());

		energizers = game.level.world.energizerTiles()
				.map(tile -> new Energizer3D(tile, Assets3D.foodMaterial(gameType, game.level.mazeNumber)))
				.collect(Collectors.toList());

		pellets = game.level.world.tiles()//
				.filter(game.level.world::isFoodTile)//
				.filter(not(game.level.world::isEnergizerTile))
				.map(tile -> new Pellet3D(tile, Assets3D.foodMaterial(gameType, game.level.mazeNumber)))
				.collect(Collectors.toList());

		player = new Player3D(game.player);
		ghosts3D = game.ghosts().collect(Collectors.toMap(Function.identity(), Ghost3D::new));

		score3D = new ScoreNotReally3D();
		createLivesCounter();

		tgMaze = new Group();
		// center over origin
		tgMaze.getTransforms()
				.add(new Translate(-GameScene.UNSCALED_SCENE_WIDTH / 2, -GameScene.UNSCALED_SCENE_HEIGHT / 2));

		tgMaze.getChildren().addAll(score3D.getNode(), tgLivesCounter);
		tgMaze.getChildren().addAll(bricks.stream().map(Brick3D::getNode).collect(Collectors.toList()));
		tgMaze.getChildren().addAll(energizers.stream().map(Energizer3D::getNode).collect(Collectors.toList()));
		tgMaze.getChildren().addAll(pellets.stream().map(Pellet3D::getNode).collect(Collectors.toList()));
		tgMaze.getChildren().addAll(player.getNode());
		tgMaze.getChildren().addAll(ghosts3D.values().stream().map(Ghost3D::getNode).collect(Collectors.toList()));

		AmbientLight ambientLight = Assets3D.ambientLight(gameType, game.level.mazeNumber);
		tgMaze.getChildren().add(ambientLight);

		PointLight pointLight = new PointLight(Color.AZURE);
		pointLight.setTranslateZ(-500);
		tgMaze.getChildren().add(pointLight);

		coordSystem = new CoordinateSystem(150);

		fxScene.setRoot(new Group(coordSystem.getNode(), tgMaze));
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
		// TODO remove again
		gameController.selectedGame().player.immune = true;
		buildSceneGraph();
	}

	@Override
	public void end() {
		log("Play scene %s: end", this);
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
			double x = Math.min(10, lerp(moveableCamera.getTranslateX(), player.getNode().getTranslateX()));
			double y = Math.max(50, lerp(moveableCamera.getTranslateY(), player.getNode().getTranslateY()));
			moveableCamera.setTranslateX(x);
			moveableCamera.setTranslateY(y);
		}
	}

	private double lerp(double current, double target) {
		return current + (target - current) * 0.02;
	}

	private void createLivesCounter() {
		tgLivesCounter = new Group();
		int counterTileX = 1, counterTileY = 1;
		tgLivesCounter.setViewOrder(-counterTileY * TS);
		for (int i = 0; i < 5; ++i) {
			V2i tile = new V2i(counterTileX + 2 * i, counterTileY);
			Player3D liveIndicator = new Player3D(gameController.selectedGame().player);
			liveIndicator.getNode().setTranslateX(tile.x * TS);
			liveIndicator.getNode().setTranslateY(tile.y * TS);
			liveIndicator.getNode().setTranslateZ(4); // ???
			liveIndicator.getNode().setUserData(tile);
			tgLivesCounter.getChildren().add(liveIndicator.getNode());
		}
	}

	@Override
	public void update() {
		GameModel game = gameController.selectedGame();
		score3D.update(game);
		score3D.getNode().setRotationAxis(Rotate.X_AXIS);
		score3D.getNode().setRotate(getActiveCamera().getRotate());
		updateLivesCounter(); // TODO
		energizers.forEach(energizer3D -> energizer3D.update(game));
		pellets.forEach(pellet3D -> pellet3D.update(game));
		player.update();
		game.ghosts().map(ghosts3D::get).forEach(Ghost3D::update);
		updateCamera();
	}

	private void updateLivesCounter() {
		if (gameController.isAttractMode()) {
			tgLivesCounter.setVisible(false);
			return;
		}
		tgLivesCounter.setVisible(true);
		GameModel game = gameController.selectedGame();
		ObservableList<Node> children = tgLivesCounter.getChildren();
		for (int i = 0; i < children.size(); ++i) {
			Group liveIndicator = (Group) children.get(i);
			V2i tile = (V2i) liveIndicator.getUserData();
			V2i tileBelowIndicator = tile.plus(0, 1);
			if (i < game.lives) {
				liveIndicator.setVisible(true);
				brickAt(tile).ifPresent(brick -> brick.getNode().setVisible(false));
				brickAt(tileBelowIndicator).ifPresent(brick -> brick.getNode().setVisible(false));
			} else {
				liveIndicator.setVisible(false);
				brickAt(tile).ifPresent(brick -> brick.getNode().setVisible(true));
				brickAt(tileBelowIndicator).ifPresent(brick -> brick.getNode().setVisible(true));
			}
		}
	}

	private Optional<Brick3D> brickAt(V2i tile) {
		return bricks.stream().filter(brick -> tile.equals(brick.getTile())).findFirst();
	}

	// State change handlers

	@Override
	public void onGameStateChange(PacManGameState oldState, PacManGameState newState) {
		if (oldState == PacManGameState.HUNTING) {
			energizers.forEach(Energizer3D::stopPumping);
		}
		if (newState == PacManGameState.HUNTING) {
			energizers.forEach(Energizer3D::startPumping);
		}
		if (newState == PacManGameState.LEVEL_COMPLETE) {
			playLevelCompleteAnimation(oldState);
		}
		if (newState == PacManGameState.LEVEL_STARTING) {
			playLevelStartingAnimation(newState);
		}
	}

	private void playLevelCompleteAnimation(PacManGameState state) {
		gameController.state.timer.reset();

		String randomCongrats = CONGRATS[new Random().nextInt(CONGRATS.length)];

		PauseTransition pause = new PauseTransition(Duration.seconds(2));
		pause.setOnFinished(e -> {
			GameModel game = gameController.selectedGame();
			game.player.visible = false;
			game.ghosts().forEach(ghost -> ghost.visible = false);
			gameController.userInterface.showFlashMessage(
					String.format("%s!\n\nLevel %d complete.", randomCongrats, gameController.selectedGame().levelNumber), 3);
		});

		ScaleTransition animation = new ScaleTransition(Duration.seconds(3), tgMaze);
		animation.setFromZ(1);
		animation.setToZ(0);

		SequentialTransition seq = new SequentialTransition(pause, animation);
		seq.setOnFinished(e -> {
			gameController.letCurrentGameStateExpire();
		});
		seq.play();
	}

	private void playLevelStartingAnimation(PacManGameState state) {
		log("%s: play level starting animation", this);
		gameController.state.timer.reset();
		gameController.userInterface.showFlashMessage("Entering Level " + gameController.selectedGame().levelNumber);
		ScaleTransition animation = new ScaleTransition(Duration.seconds(3), tgMaze);
		animation.setDelay(Duration.seconds(2));
		animation.setFromZ(0);
		animation.setToZ(1);
		animation.setOnFinished(e -> {
			gameController.letCurrentGameStateExpire();
		});
		animation.play();
	}
}