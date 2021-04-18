package de.amr.games.pacman.ui.fx.scenes.common._3d;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static java.util.function.Predicate.not;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.controller.event.BonusActivatedEvent;
import de.amr.games.pacman.controller.event.BonusEatenEvent;
import de.amr.games.pacman.controller.event.BonusExpiredEvent;
import de.amr.games.pacman.controller.event.ExtraLifeEvent;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGameStateChangedEvent;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.world.PacManGameWorld;
import de.amr.games.pacman.ui.PacManGameSound;
import de.amr.games.pacman.ui.fx.entities._3d.Bonus3D;
import de.amr.games.pacman.ui.fx.entities._3d.Ghost3D;
import de.amr.games.pacman.ui.fx.entities._3d.LevelCounter3D;
import de.amr.games.pacman.ui.fx.entities._3d.Maze3D;
import de.amr.games.pacman.ui.fx.entities._3d.ScoreNotReally3D;
import de.amr.games.pacman.ui.fx.model3D.GianmarcosModel3D;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D_Assets;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D_Impl;
import de.amr.games.pacman.ui.fx.scenes.common.GameScene;
import de.amr.games.pacman.ui.fx.scenes.common._3d.PlaySceneCameras.CameraType;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.animation.TranslateTransition;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * 3D scene displaying the maze and the game play for both, Pac-Man and Ms.
 * Pac-Man games.
 * 
 * @author Armin Reichert
 */
public class PlayScene3D implements GameScene {

	private static final String[] CONGRATS = { "Well done", "Congrats", "Awesome", "You did it", "You're the man*in",
			"WTF" };
	private static final int MAX_LIVES_DISPLAYED = 5;

	private final SubScene fxScene;
	private final PlaySceneCameras cams;

	private PlayScene3DAnimationController animationController;
	private PacManGameController gameController;

	private CoordinateSystem coordSystem;
	private Box floor;
	private Group player;
	private Map<Ghost, Ghost3D> ghosts3D;
	private Maze3D maze;
	private List<Node> energizers;
	private List<Transition> energizerAnimations;
	private List<Node> pellets;
	private Bonus3D bonus3D;
	private ScoreNotReally3D score3D;
	private Group livesCounter3D;
	private LevelCounter3D levelCounter3D;

	public PlayScene3D(PlayScene3DAnimationController animationController) {
		this.animationController = animationController;
		fxScene = new SubScene(new Group(), 800, 600, true, SceneAntialiasing.BALANCED);
		cams = new PlaySceneCameras(fxScene);
		cams.select(CameraType.DYNAMIC);
	}

	@Override
	public PacManGameController getGameController() {
		return gameController;
	}

	@Override
	public void setGameController(PacManGameController gameController) {
		this.gameController = gameController;
		animationController.setGameController(gameController);
	}

	@Override
	public OptionalDouble aspectRatio() {
		return OptionalDouble.empty();
	}

	@Override
	public SubScene getSubScene() {
		return fxScene;
	}

	@Override
	public void stretchTo(double width, double height) {
		// data binding does the job
	}

	@Override
	public Optional<PlaySceneCameras> cams() {
		return Optional.of(cams);
	}

	@Override
	public void init() {
		log("Game scene %s: init", this);

		final GameVariant variant = gameController.gameVariant();
		final GameLevel level = game().currentLevel;
		final PacManGameWorld world = level.world;
		final Rendering2D r2D = Rendering2D_Impl.get(variant);
		final Group root = new Group();

		maze = new Maze3D(world, Rendering2D_Assets.getMazeWallColor(variant, level.mazeNumber));

		floor = new Box(UNSCALED_SCENE_WIDTH, UNSCALED_SCENE_HEIGHT, 0.1);
		PhongMaterial floorMaterial = new PhongMaterial(Color.rgb(0, 0, 80));
		floor.setMaterial(floorMaterial);
		floor.setTranslateX(UNSCALED_SCENE_WIDTH / 2 - 4);
		floor.setTranslateY(UNSCALED_SCENE_HEIGHT / 2 - 4);
		floor.setTranslateZ(3);

		PhongMaterial foodMaterial = new PhongMaterial(Rendering2D_Assets.getFoodColor(variant, level.mazeNumber));
		energizers = world.energizerTiles().map(tile -> createPellet(3, tile, foodMaterial)).collect(Collectors.toList());
		pellets = world.tiles().filter(world::isFoodTile).filter(not(world::isEnergizerTile))//
				.map(tile -> createPellet(1, tile, foodMaterial)).collect(Collectors.toList());

		player = GianmarcosModel3D.IT.createPacMan();
		ghosts3D = game().ghosts().collect(Collectors.toMap(Function.identity(), ghost -> new Ghost3D(ghost, r2D)));
		bonus3D = new Bonus3D(variant, r2D);

		score3D = new ScoreNotReally3D();
		livesCounter3D = createLivesCounter3D(new V2i(2, 1));
		if (gameController.isAttractMode()) {
			score3D.setHiscoreOnly(true);
			livesCounter3D.setVisible(false);
		} else {
			score3D.setHiscoreOnly(false);
			livesCounter3D.setVisible(true);
		}

		levelCounter3D = new LevelCounter3D(r2D);
		levelCounter3D.tileRight = new V2i(25, 1);
		levelCounter3D.update(game());

		AmbientLight ambientLight = new AmbientLight();
		PointLight playerLight = new PointLight();
		playerLight.translateXProperty().bind(player.translateXProperty());
		playerLight.translateYProperty().bind(player.translateYProperty());
		playerLight.lightOnProperty().bind(player.visibleProperty());
		playerLight.setTranslateZ(-4);

		root.getChildren().addAll(maze.getBricks());
		root.getChildren().addAll(floor, score3D, livesCounter3D, levelCounter3D);
		root.getChildren().addAll(energizers);
		root.getChildren().addAll(pellets);
		root.getChildren().addAll(player);
		root.getChildren().addAll(ghosts3D.values());
		root.getChildren().addAll(bonus3D);
		root.getChildren().addAll(ambientLight, playerLight);

		root.setTranslateX(-UNSCALED_SCENE_WIDTH / 2);
		root.setTranslateY(-UNSCALED_SCENE_HEIGHT / 2);

		coordSystem = new CoordinateSystem(fxScene.getWidth());

		fxScene.setRoot(new Group(coordSystem.getNode(), root));
		fxScene.setFill(Color.rgb(0, 0, 0));
	}

	@Override
	public void update() {
		score3D.update(game(), cams.selectedCamera());
		for (int i = 0; i < MAX_LIVES_DISPLAYED; ++i) {
			livesCounter3D.getChildren().get(i).setVisible(i < game().lives);
		}
		energizers.forEach(energizer -> {
			V2i tile = (V2i) energizer.getUserData();
			energizer.setVisible(!game().currentLevel.isFoodRemoved(tile));
		});
		pellets.forEach(pellet -> {
			V2i tile = (V2i) pellet.getUserData();
			pellet.setVisible(!game().currentLevel.isFoodRemoved(tile));
		});
		updatePlayer();
		ghosts3D.values().forEach(Ghost3D::update);
		bonus3D.update(game().bonus);
		cams.updateSelectedCamera(player);
		animationController.update();
	}

	@Override
	public void end() {
		log("Game scene %s: end", this);
	}

	private Sphere createPellet(double r, V2i tile, PhongMaterial material) {
		Sphere s = new Sphere(r);
		s.setMaterial(material);
		s.setTranslateX(tile.x * TS);
		s.setTranslateY(tile.y * TS);
		s.setTranslateZ(1);
		s.setUserData(tile);
		return s;
	}

	private Group createLivesCounter3D(V2i tilePosition) {
		Group livesCounter = new Group();
		for (int i = 0; i < MAX_LIVES_DISPLAYED; ++i) {
			V2i tile = tilePosition.plus(2 * i, 0);
			Group liveIndicator = GianmarcosModel3D.IT.createPacMan();
			liveIndicator.setTranslateX(tile.x * TS);
			liveIndicator.setTranslateY(tile.y * TS);
			liveIndicator.setTranslateZ(0);
			livesCounter.getChildren().add(liveIndicator);
		}
		return livesCounter;
	}

	private void updatePlayer() {
		Pac pac = game().player;
		player.setVisible(pac.visible);
		player.setTranslateX(pac.position.x);
		player.setTranslateY(pac.position.y);
		player.setRotationAxis(Rotate.Z_AXIS);
		player.setRotate(
				pac.dir == Direction.LEFT ? 0 : pac.dir == Direction.UP ? 90 : pac.dir == Direction.RIGHT ? 180 : 270);
	}

	@Override
	public void onGameEvent(PacManGameEvent gameEvent) {
		animationController.onGameEvent(gameEvent);

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

		// enter HUNTING
		if (event.newGameState == PacManGameState.HUNTING) {
			startEnergizerAnimations();
		}

		// exit HUNTING
		if (event.oldGameState == PacManGameState.HUNTING && event.newGameState != PacManGameState.GHOST_DYING) {
			stopEnergizerAnimations();
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

	private void startEnergizerAnimations() {
		if (energizerAnimations == null) {
			energizerAnimations = new ArrayList<>();
			energizers.forEach(energizer -> {
				ScaleTransition pumping = new ScaleTransition(Duration.seconds(0.25), energizer);
				pumping.setAutoReverse(true);
				pumping.setCycleCount(Transition.INDEFINITE);
				pumping.setFromX(0.2);
				pumping.setFromY(0.2);
				pumping.setFromZ(0.2);
				pumping.setToX(1);
				pumping.setToY(1);
				pumping.setToZ(1);
				energizerAnimations.add(pumping);
			});
		}
		energizerAnimations.forEach(Transition::play);
	}

	private void stopEnergizerAnimations() {
		energizerAnimations.forEach(Transition::stop);
		energizers.forEach(energizer -> {
			energizer.setScaleX(1);
			energizer.setScaleY(1);
			energizer.setScaleZ(1);
		});
	}

	private void playAnimationPlayerDying() {

		double savedTranslateX = player.getTranslateX();
		double savedTranslateY = player.getTranslateY();
		double savedTranslateZ = player.getTranslateZ();

		PauseTransition phase1 = new PauseTransition(Duration.seconds(1));
		phase1.setOnFinished(e -> {
			game().ghosts().forEach(ghost -> ghost.visible = false);
			game().player.turnBothTo(Direction.DOWN);
			animationController.sounds.play(PacManGameSound.PACMAN_DEATH);
		});

		TranslateTransition raise = new TranslateTransition(Duration.seconds(0.5), player);
		raise.setFromZ(0);
		raise.setToZ(-10);
		raise.setByZ(1);

		ScaleTransition expand = new ScaleTransition(Duration.seconds(0.5), player);
		expand.setToX(2);
		expand.setToY(2);
		expand.setToZ(2);

		ScaleTransition shrink = new ScaleTransition(Duration.seconds(1), player);
		shrink.setToX(0);
		shrink.setToY(0);
		shrink.setToZ(0);

		SequentialTransition animation = new SequentialTransition(phase1, raise, expand, shrink);
		animation.setOnFinished(e -> {
			player.setScaleX(1);
			player.setScaleY(1);
			player.setScaleZ(1);
			player.setTranslateX(savedTranslateX);
			player.setTranslateY(savedTranslateY);
			player.setTranslateZ(savedTranslateZ);
			game().player.visible = false;
			gameController.stateTimer().forceExpiration();
		});

		animation.play();
	}

	private void playAnimationLevelComplete() {
		gameController.stateTimer().reset();
		PauseTransition phase1 = new PauseTransition(Duration.seconds(2));
		phase1.setDelay(Duration.seconds(1));
		phase1.setOnFinished(e -> {
			game().player.visible = false;
			game().ghosts().forEach(ghost -> ghost.visible = false);
			String congrats = CONGRATS[new Random().nextInt(CONGRATS.length)];
			String message = String.format("%s!\n\nLevel %d complete.", congrats, game().currentLevelNumber);
			gameController.getUI().showFlashMessage(message, 2);
		});
		SequentialTransition animation = new SequentialTransition(phase1, new PauseTransition(Duration.seconds(2)));
		animation.setOnFinished(e -> gameController.stateTimer().forceExpiration());
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
		SequentialTransition animation = new SequentialTransition(phase1, new PauseTransition(Duration.seconds(2)));
		animation.setOnFinished(e -> gameController.stateTimer().forceExpiration());
		animation.play();
	}
}