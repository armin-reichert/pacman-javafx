package de.amr.games.pacman.ui.fx.scenes.common._3d;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static java.util.function.Predicate.not;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.world.PacManGameWorld;
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
import de.amr.games.pacman.ui.fx.sound.SoundManager;
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

/**
 * 3D scene displaying the maze and the game play for both, Pac-Man and Ms.
 * Pac-Man games.
 * 
 * @author Armin Reichert
 */
public class PlayScene3D implements GameScene {

	private static final int MAX_LIVES_DISPLAYED = 5;

	private final SubScene fxScene;
	private final PlaySceneCameras cams;

	private PlayScene3DAnimationController animationController;
	private PacManGameController gameController;

	private CoordinateSystem coordSystem;
	private Box floor;
	Group player;
	private Map<Ghost, Ghost3D> ghosts3D;
	private Maze3D maze;
	List<Node> energizers;
	private List<Node> pellets;
	Bonus3D bonus3D;
	private ScoreNotReally3D score3D;
	private Group livesCounter3D;
	LevelCounter3D levelCounter3D;

	public PlayScene3D(SoundManager sounds) {
		this.animationController = new PlayScene3DAnimationController(this, sounds);
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
	public void onGameEvent(PacManGameEvent gameEvent) {
		animationController.onGameEvent(gameEvent);
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

		// TODO we need the exact moment of the direction change
		player.setRotationAxis(Rotate.Z_AXIS);
		double target = rotateZ(pac.dir);
		if (player.getRotate() != target) {
			double next = lerp(player.getRotate(), target, 0.1);
			if (player.getRotate() - 180 > target) {
				next = lerp(player.getRotate(), target + 360, 0.1);
			} else if (player.getRotate() + 180 < target) {
				next = lerp(player.getRotate(), target - 360, 0.1);
			}
			player.setRotate(next);
		}
	}

	private double rotateZ(Direction dir) {
		return dir == Direction.LEFT ? 0 : dir == Direction.UP ? 90 : dir == Direction.RIGHT ? 180 : 270;
	}

	private double lerp(double min, double max, double factor) {
		return min + factor * (max - min);
	}
}