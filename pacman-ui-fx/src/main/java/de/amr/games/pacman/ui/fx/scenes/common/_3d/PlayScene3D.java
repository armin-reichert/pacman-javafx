package de.amr.games.pacman.ui.fx.scenes.common._3d;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static de.amr.games.pacman.ui.fx.rendering.Rendering2D_Assets.getMazeWallColor;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.world.PacManGameWorld;
import de.amr.games.pacman.ui.fx.entities._3d.Bonus3D;
import de.amr.games.pacman.ui.fx.entities._3d.Ghost3D;
import de.amr.games.pacman.ui.fx.entities._3d.LevelCounter3D;
import de.amr.games.pacman.ui.fx.entities._3d.Maze3D;
import de.amr.games.pacman.ui.fx.entities._3d.Player3D;
import de.amr.games.pacman.ui.fx.entities._3d.ScoreNotReally3D;
import de.amr.games.pacman.ui.fx.model3D.GianmarcosModel3D;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D;
import de.amr.games.pacman.ui.fx.scenes.common.GameScene;
import de.amr.games.pacman.ui.fx.scenes.common._3d.PlaySceneCameras.CameraType;
import de.amr.games.pacman.ui.fx.scenes.mspacman.MsPacManScenes;
import de.amr.games.pacman.ui.fx.scenes.pacman.PacManScenes;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

/**
 * 3D scene displaying the maze and the game play for both, Pac-Man and Ms. Pac-Man games.
 * 
 * @author Armin Reichert
 */
public class PlayScene3D implements GameScene {

	private static final int MAX_LIVES_DISPLAYED = 5;

	private final SubScene fxScene;
	private final PlaySceneCameras cams;
	private final PlayScene3DAnimationController animationController;
	private PacManGameController gameController;
	private CoordinateSystem coordSystem;

	Maze3D maze;
	Box floor;
	Player3D player3D;
	Map<Ghost, Ghost3D> ghosts3D;
	Bonus3D bonus3D;
	ScoreNotReally3D score3D;
	Group livesCounter3D;
	LevelCounter3D levelCounter3D;

	public PlayScene3D(SoundManager sounds) {
		Group root = new Group();
		fxScene = new SubScene(root, 400, 300, true, SceneAntialiasing.BALANCED);
		cams = new PlaySceneCameras(fxScene);
		cams.select(CameraType.DYNAMIC);
		animationController = new PlayScene3DAnimationController(this, sounds);
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
		log("%s: init", this);

		final GameVariant variant = gameController.gameVariant();
		final Rendering2D r2D = variant == GameVariant.MS_PACMAN ? MsPacManScenes.RENDERING : PacManScenes.RENDERING;
		final GameLevel level = game().currentLevel();
		final PacManGameWorld world = level.world;
		final Group root = new Group();

		maze = new Maze3D();
		maze.createWalls(world, getMazeWallColor(variant, level.mazeNumber));
		maze.resetFood(variant, game());

		var floorMaterial = new PhongMaterial(Color.rgb(20, 20, 100));
		var floorTexture = new Image(getClass().getResourceAsStream("/common/escher-texture.jpg"));
		floorMaterial.setDiffuseMap(floorTexture);
		floor = new Box(UNSCALED_SCENE_WIDTH, UNSCALED_SCENE_HEIGHT, 0.1);
		floor.setMaterial(floorMaterial);
		floor.setTranslateX(UNSCALED_SCENE_WIDTH / 2 - 4);
		floor.setTranslateY(UNSCALED_SCENE_HEIGHT / 2 - 4);
		floor.setTranslateZ(3);

		player3D = new Player3D(game().player());
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

		levelCounter3D = new LevelCounter3D(new V2i(25, 1), r2D);
		levelCounter3D.update(game());

		var ambientLight = new AmbientLight();
		var playerLight = new PointLight();
		playerLight.translateXProperty().bind(player3D.translateXProperty());
		playerLight.translateYProperty().bind(player3D.translateYProperty());
//		playerLight.lightOnProperty().bind(player3D.visibleProperty());
		playerLight.setTranslateZ(-4);

		root.getChildren().addAll(maze, floor, score3D, livesCounter3D, levelCounter3D);
		root.getChildren().addAll(player3D);
		root.getChildren().addAll(ghosts3D.values());
		root.getChildren().addAll(bonus3D);
		root.getChildren().addAll(ambientLight, playerLight);

		root.setTranslateX(-UNSCALED_SCENE_WIDTH / 2);
		root.setTranslateY(-UNSCALED_SCENE_HEIGHT / 2);

		coordSystem = new CoordinateSystem(fxScene.getWidth());

		fxScene.setRoot(new Group(coordSystem.getNode(), root));
		fxScene.setFill(Color.rgb(0, 0, 0));

		animationController.init();
	}

	@Override
	public void onGameEvent(PacManGameEvent gameEvent) {
		animationController.onGameEvent(gameEvent);
	}

	@Override
	public void update() {
		score3D.update(game(), cams.selectedCamera());
		for (int i = 0; i < MAX_LIVES_DISPLAYED; ++i) {
			livesCounter3D.getChildren().get(i).setVisible(i < game().lives());
		}
		player3D.update();
		ghosts3D.values().forEach(Ghost3D::update);
		bonus3D.update(game().bonus());

		cams.updateSelectedCamera(player3D);
		animationController.update();
	}

	@Override
	public void end() {
		log("%s: end", this);
	}

	private Group createLivesCounter3D(V2i tilePosition) {
		Group livesCounter = new Group();
		for (int i = 0; i < MAX_LIVES_DISPLAYED; ++i) {
			V2i tile = tilePosition.plus(2 * i, 0);
			Group liveIndicator = GianmarcosModel3D.createPacMan();
			liveIndicator.setTranslateX(tile.x * TS);
			liveIndicator.setTranslateY(tile.y * TS);
			liveIndicator.setTranslateZ(0);
			livesCounter.getChildren().add(liveIndicator);
		}
		return livesCounter;
	}
}