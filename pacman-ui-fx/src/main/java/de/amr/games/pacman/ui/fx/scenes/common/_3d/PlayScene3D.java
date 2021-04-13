package de.amr.games.pacman.ui.fx.scenes.common._3d;

import static de.amr.games.pacman.lib.Logging.log;
import static java.util.function.Predicate.not;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.controller.event.BonusActivatedEvent;
import de.amr.games.pacman.controller.event.BonusEatenEvent;
import de.amr.games.pacman.controller.event.BonusExpiredEvent;
import de.amr.games.pacman.controller.event.ExtraLifeEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGameStateChangedEvent;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.ui.PacManGameSound;
import de.amr.games.pacman.ui.fx.entities._3d.Bonus3D;
import de.amr.games.pacman.ui.fx.entities._3d.Energizer3D;
import de.amr.games.pacman.ui.fx.entities._3d.Ghost3D;
import de.amr.games.pacman.ui.fx.entities._3d.LevelCounter3D;
import de.amr.games.pacman.ui.fx.entities._3d.LivesCounter3D;
import de.amr.games.pacman.ui.fx.entities._3d.Maze3D;
import de.amr.games.pacman.ui.fx.entities._3d.Pellet3D;
import de.amr.games.pacman.ui.fx.entities._3d.Player3D;
import de.amr.games.pacman.ui.fx.entities._3d.ScoreNotReally3D;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D;
import de.amr.games.pacman.ui.fx.rendering.GameRendering2D_Assets;
import de.amr.games.pacman.ui.fx.scenes.common.GameScene;
import de.amr.games.pacman.ui.fx.sound.PlaySceneSoundHandler;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.AmbientLight;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

/**
 * 3D scene displaying the maze and the game play for both, Pac-Man and Ms. Pac-Man games.
 * 
 * @author Armin Reichert
 */
public class PlayScene3D implements GameScene {

	private static final String[] CONGRATS = { "Well done", "Congrats", "Awesome", "You did it", "You're the man*in",
			"WTF" };

	private final SubScene fxScene;

	private final List<PerspectiveCamera> cameras = new ArrayList<>();
	private int selectedCamIndex;

	private final SoundManager sounds;
	private PlaySceneSoundHandler playSceneSoundHandler;
	private PacManGameController gameController;

	private AmbientLight ambientLight;
	private PointLight pointLight;
	private CoordinateSystem coordSystem;
	private Box ground;
	private Group tgMaze;
	private Player3D player;
	private Map<Ghost, Ghost3D> ghosts3D;
	private Maze3D maze;
	private List<Energizer3D> energizers;
	private List<Pellet3D> pellets;
	private Bonus3D bonus3D;
	private ScoreNotReally3D score3D;
	private LivesCounter3D livesCounter3D;
	private LevelCounter3D levelCounter3D;

	public PlayScene3D(SoundManager sounds) {
		this.sounds = sounds;
		fxScene = new SubScene(new Group(), 800, 600, true, SceneAntialiasing.BALANCED);
		for (int i = 0; i < 3; ++i) {
			cameras.add(new PerspectiveCamera(true));
		}
		// camera #1 is controllable using the keyboard
		CameraController cameraController = new CameraController(cameras.get(1));
		fxScene.addEventHandler(KeyEvent.KEY_PRESSED, cameraController::handleKeyEvent);
		selectCam(0);
	}

	private void buildSceneGraph() {
		final GameVariant gameVariant = gameController.gameVariant();
		final int mazeNumber = game().currentLevel.mazeNumber;

		maze = new Maze3D(game(), GameRendering2D_Assets.getMazeWallColor(gameVariant, mazeNumber));

		PhongMaterial foodMaterial = new PhongMaterial(GameRendering2D_Assets.getFoodColor(gameVariant, mazeNumber));

		energizers = game().currentLevel.world.energizerTiles()//
				.map(tile -> new Energizer3D(tile, foodMaterial))//
				.collect(Collectors.toList());

		pellets = game().currentLevel.world.tiles()//
				.filter(game().currentLevel.world::isFoodTile)//
				.filter(not(game().currentLevel.world::isEnergizerTile))//
				.map(tile -> new Pellet3D(tile, foodMaterial)).collect(Collectors.toList());

		player = new Player3D(game().player);
		ghosts3D = game().ghosts().collect(
				Collectors.toMap(Function.identity(), ghost -> new Ghost3D(ghost, GameRendering2D.rendering(gameVariant))));

		bonus3D = new Bonus3D(gameVariant, GameRendering2D.rendering(gameVariant));

		score3D = new ScoreNotReally3D();

		livesCounter3D = new LivesCounter3D(game().player, 2, 1);

		levelCounter3D = new LevelCounter3D(GameRendering2D.rendering(gameVariant));
		levelCounter3D.tileRight = new V2i(25, 1);
		levelCounter3D.update(game());

		tgMaze = new Group();
		tgMaze.getTransforms().add(new Translate(-14 * 8, -18 * 8));
		tgMaze.getChildren().addAll(score3D.get(), livesCounter3D.get(), levelCounter3D.get());
		tgMaze.getChildren().addAll(maze.getBricks());
		tgMaze.getChildren().addAll(collect(energizers));
		tgMaze.getChildren().addAll(collect(pellets));
		tgMaze.getChildren().addAll(player.get());
		tgMaze.getChildren().addAll(collect(ghosts3D.values()));
		tgMaze.getChildren().add(bonus3D.get());

		ambientLight = new AmbientLight();

		pointLight = new PointLight();
		pointLight.translateXProperty().bind(player.get().translateXProperty());
		pointLight.translateYProperty().bind(player.get().translateYProperty());
		pointLight.lightOnProperty().bind(player.$visible);
		pointLight.setTranslateZ(-4);

		tgMaze.getChildren().addAll(ambientLight, pointLight);

		ground = new Box(UNSCALED_SCENE_WIDTH * 8, UNSCALED_SCENE_HEIGHT * 8, 0.1);
		ground.setMaterial(new PhongMaterial(Color.rgb(20, 20, 20)));
		ground.setTranslateX(-4);
		ground.setTranslateY(-4);
		ground.setTranslateZ(4);

		coordSystem = new CoordinateSystem(fxScene.getWidth());

		fxScene.setRoot(new Group(coordSystem.getNode(), ground, tgMaze));
		fxScene.setFill(Color.rgb(20, 20, 60));
	}

	@Override
	public PacManGameController getGameController() {
		return gameController;
	}

	@Override
	public void setGameController(PacManGameController gameController) {
		this.gameController = gameController;
		playSceneSoundHandler = new PlaySceneSoundHandler(gameController, sounds);
	}

	@Override
	public OptionalDouble aspectRatio() {
		return OptionalDouble.empty();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "@" + hashCode();
	}

	private Collection<Node> collect(Collection<? extends Supplier<Node>> items) {
		return items.stream().map(Supplier::get).collect(Collectors.toList());
	}

	@Override
	public void stretchTo(double width, double height) {
		// data binding does the job
	}

	@Override
	public SubScene get() {
		return fxScene;
	}

	@Override
	public int numCams() {
		return cameras.size();
	}

	@Override
	public int selectedCamIndex() {
		return selectedCamIndex;
	}

	@Override
	public Optional<Camera> selectedCam() {
		return Optional.of(cameras.get(selectedCamIndex));
	}

	@Override
	public void selectCam(int cameraIndex) {
		selectedCamIndex = cameraIndex;
		PerspectiveCamera cam = cameras.get(selectedCamIndex);
		fxScene.setCamera(cam);
		switch (selectedCamIndex) {
		case 0: // dynamic camera showing most of the board
			cam.setNearClip(0.1);
			cam.setFarClip(10000.0);
			cam.setRotationAxis(Rotate.X_AXIS);
			cam.setRotate(30);
			cam.setTranslateZ(-250);
			break;
		case 1: // static camera, controllable using keyboard
			cam.setNearClip(0.1);
			cam.setFarClip(10000.0);
			cam.setRotationAxis(Rotate.X_AXIS);
			cam.setRotate(30);
			cam.setTranslateX(0);
			cam.setTranslateY(270);
			cam.setTranslateZ(-460);
			break;
		case 2: // dynamic camera near to the player
			cam.setNearClip(0.1);
			cam.setFarClip(10000.0);
			cam.setRotationAxis(Rotate.X_AXIS);
			cam.setRotate(60);
			cam.setTranslateZ(-60);
			break;
		default:
			throw new IllegalArgumentException();
		}
	}

	private void updateCamera() {
		PerspectiveCamera cam = cameras.get(selectedCamIndex);
		switch (selectedCamIndex) {
		case 0:
			cam.setTranslateX(Math.min(10, lerp(cam.getTranslateX(), player.get().getTranslateX())));
			cam.setTranslateY(Math.max(50, lerp(cam.getTranslateY(), player.get().getTranslateY())));
			break;
		case 1:
			break;
		case 2:
			cam.setTranslateX(lerp(cam.getTranslateX(), player.pac.position.x - 100));
			cam.setTranslateY(lerp(cam.getTranslateY(), player.pac.position.y));
			break;
		default:
			throw new IllegalArgumentException();
		}
	}

	private static double lerp(double current, double target) {
		return current + (target - current) * 0.02;
	}

	@Override
	public void start() {
		log("Game scene %s: start", this);
		buildSceneGraph();
	}

	@Override
	public void end() {
		log("Game scene %s: end", this);
	}

	@Override
	public void update() {
		score3D.setHiscoreOnly(gameController.isAttractMode());
		score3D.update(game());
		// TODO use overlay subscene for score etc.?
		selectedCam().ifPresent(camera -> {
			score3D.get().setRotationAxis(Rotate.X_AXIS);
			score3D.get().setRotate(camera.getRotate());
		});
		livesCounter3D.get().setVisible(!gameController.isAttractMode());
		livesCounter3D.update(game());
		energizers.forEach(energizer3D -> energizer3D.update(game()));
		pellets.forEach(pellet3D -> pellet3D.update(game()));
		player.update(game().currentLevel.world);
		game().ghosts().map(ghosts3D::get).forEach(Ghost3D::update);
		bonus3D.update(game().bonus);
		updateCamera();
		playSceneSoundHandler.update();
	}

	@Override
	public void onGameEvent(PacManGameEvent gameEvent) {
		playSceneSoundHandler.onGameEvent(gameEvent);

		if (gameEvent instanceof PacManGameStateChangedEvent) {
			onGameStateChange((PacManGameStateChangedEvent) gameEvent);
		}

		else if (gameEvent instanceof ExtraLifeEvent) {
			gameController.getUI().showFlashMessage("Extra life!");
		}

		else if (gameEvent instanceof BonusActivatedEvent) {
			bonus3D.showSymbol(game().bonus);
		}

		else if (gameEvent instanceof BonusExpiredEvent) {
			bonus3D.hide();
		}

		else if (gameEvent instanceof BonusEatenEvent) {
			bonus3D.showPoints(game().bonus);
		}
	}

	private void onGameStateChange(PacManGameStateChangedEvent event) {

		playSceneSoundHandler.onGameStateChange(event.oldGameState, event.newGameState);

		// enter READY
		if (event.newGameState == PacManGameState.READY) {
		}

		// enter HUNTING
		if (event.newGameState == PacManGameState.HUNTING) {
			energizers.forEach(Energizer3D::startPumping);
		}

		// exit HUNTING
		if (event.oldGameState == PacManGameState.HUNTING && event.newGameState != PacManGameState.GHOST_DYING) {
			energizers.forEach(Energizer3D::stopPumping);
			bonus3D.hide();
		}

		// enter PACMAN_DYING
		if (event.newGameState == PacManGameState.PACMAN_DYING) {
			playAnimationPlayerDying();
		}

		// enter LEVEL_COMPLETE
		if (event.newGameState == PacManGameState.LEVEL_COMPLETE) {
			playAnimationLevelComplete();
		}

		// enter LEVEL_STARTING
		if (event.newGameState == PacManGameState.LEVEL_STARTING) {
			levelCounter3D.update(event.gameModel);
			playAnimationLevelStarting();
		}
	}

	private void playAnimationPlayerDying() {
		PauseTransition phase1 = new PauseTransition(Duration.seconds(1));
		phase1.setOnFinished(e -> {
			game().ghosts().forEach(ghost -> ghost.visible = false);
			sounds.play(PacManGameSound.PACMAN_DEATH);
		});

		ScaleTransition expand = new ScaleTransition(Duration.seconds(1), player.get());
		expand.setToX(1.5);
		expand.setToY(1.5);
		expand.setToZ(1.5);

		ScaleTransition shrink = new ScaleTransition(Duration.seconds(1.5), player.get());
		shrink.setToX(0.1);
		shrink.setToY(0.1);
		shrink.setToZ(0.1);

		SequentialTransition animation = new SequentialTransition(phase1, expand, shrink);
		animation.setOnFinished(e -> {
			player.get().setScaleX(1);
			player.get().setScaleY(1);
			player.get().setScaleZ(1);
			game().player.visible = false;
			gameController.stateTimer().forceExpiration();
		});

		animation.play();
	}

	private void playAnimationLevelComplete() {
		gameController.stateTimer().reset();

		String congrats = CONGRATS[new Random().nextInt(CONGRATS.length)];
		String message = String.format("%s!\n\nLevel %d complete.", congrats, game().currentLevelNumber);
		gameController.getUI().showFlashMessage(message, 2);

		PauseTransition phase1 = new PauseTransition(Duration.seconds(2));
		phase1.setOnFinished(e -> {
			game().player.visible = false;
			game().ghosts().forEach(ghost -> ghost.visible = false);
		});

		PauseTransition phase2 = new PauseTransition(Duration.seconds(2));

		SequentialTransition animation = new SequentialTransition(phase1, phase2);
		animation.setOnFinished(e -> {
			gameController.stateTimer().forceExpiration();
		});
		animation.play();
	}

	private void playAnimationLevelStarting() {
		gameController.stateTimer().reset();
		gameController.getUI().showFlashMessage("Entering Level " + gameController.game().currentLevelNumber);

		PauseTransition phase1 = new PauseTransition(Duration.seconds(2));
		phase1.setOnFinished(e -> {
			game().player.visible = true;
			game().ghosts().forEach(ghost -> ghost.visible = true);
		});

		PauseTransition phase2 = new PauseTransition(Duration.seconds(2));

		SequentialTransition animation = new SequentialTransition(phase1, phase2);
		animation.setOnFinished(e -> gameController.stateTimer().forceExpiration());
		animation.play();
	}
}