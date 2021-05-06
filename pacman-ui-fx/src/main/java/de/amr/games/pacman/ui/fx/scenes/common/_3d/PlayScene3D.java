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
import de.amr.games.pacman.model.common.GameModel;
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
import de.amr.games.pacman.ui.fx.scenes.common.GameScene;
import de.amr.games.pacman.ui.fx.scenes.mspacman.MsPacManScenes;
import de.amr.games.pacman.ui.fx.scenes.pacman.PacManScenes;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import javafx.scene.AmbientLight;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.transform.Rotate;

/**
 * 3D-scene displaying the maze and the game play. Used in both game variants.
 * 
 * @author Armin Reichert
 */
public class PlayScene3D implements GameScene {

	static final int PERSPECTIVE_TOTAL = 0;
	static final int PERSPECTIVE_FOLLOWING_PLAYER = 1;
	static final int PERSPECTIVE_NEAR_PLAYER = 2;

	static final int LIVES_COUNTER_MAX = 5;

	private final SubScene subSceneFX;
	private final PlayScene3DAnimationController animationController;
	private final PlayScenePerspective[] perspectives;

	private PacManGameController gameController;
	private int selectedPerspective;

	// these are exposed to animation controller:
	Maze3D maze;
	Player3D player3D;
	Map<Ghost, Ghost3D> ghosts3D;
	Bonus3D bonus3D;
	ScoreNotReally3D score3D;
	Group livesCounter3D;
	LevelCounter3D levelCounter3D;

	public PlayScene3D(SoundManager sounds) {
		animationController = new PlayScene3DAnimationController(this, sounds);
		subSceneFX = new SubScene(new Group(), 1, 1, true, SceneAntialiasing.BALANCED);
		Camera camera = new PerspectiveCamera(true);
		subSceneFX.setCamera(camera);
		perspectives = new PlayScenePerspective[] { new TotalPerspective(subSceneFX),
				new FollowingPlayerPerspective(camera), new NearPlayerPerspective(camera) };
		selectPerspective(PERSPECTIVE_FOLLOWING_PLAYER);
	}

	public Optional<PlayScenePerspective> selectedPerspective() {
		if (selectedPerspective >= 0 && selectedPerspective < perspectives.length) {
			return Optional.of(perspectives[selectedPerspective]);
		}
		return Optional.empty();
	}

	public void selectPerspective(int index) {
		selectedPerspective = index;
		selectedPerspective().ifPresent(PlayScenePerspective::reset);
	}

	public void nextPerspective() {
		int next = selectedPerspective + 1;
		if (next == perspectives.length) {
			next = 0;
		}
		selectPerspective(next);
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
	public SubScene getSubSceneFX() {
		return subSceneFX;
	}

	@Override
	public void resize(double width, double height) {
		// data binding does the job
	}

	@Override
	public void init() {
		log("%s: init", this);

		final var variant = gameController.gameVariant();
		final var r2D = variant == GameVariant.MS_PACMAN ? MsPacManScenes.RENDERING : PacManScenes.RENDERING;
		final var level = game().currentLevel();
		final var world = level.world;

		final var wallColor = getMazeWallColor(variant, level.mazeNumber);
		final var wallMaterial = new PhongMaterial(wallColor);
		wallMaterial.setSpecularColor(wallColor.brighter());
		final var floorTexture = new Image(getClass().getResourceAsStream("/common/escher-texture.jpg"));
		maze = new Maze3D(world, wallMaterial, 2.5, floorTexture, PacManGameWorld.DEFAULT_WIDTH * TS,
				PacManGameWorld.DEFAULT_HEIGHT * TS);
		maze.resetFood(variant, level);

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

		final var ambientLight = new AmbientLight();
		final var playerLight = new PointLight();
		playerLight.translateXProperty().bind(player3D.translateXProperty());
		playerLight.translateYProperty().bind(player3D.translateYProperty());
		playerLight.setTranslateZ(-4);

		final var content = new Group();
		content.getChildren().addAll(maze, score3D, livesCounter3D, levelCounter3D, player3D, bonus3D);
		content.getChildren().addAll(ghosts3D.values());
		content.getChildren().addAll(ambientLight, playerLight);

		content.setTranslateX(-0.5 * PacManGameWorld.DEFAULT_WIDTH * TS);
		content.setTranslateY(-0.5 * PacManGameWorld.DEFAULT_HEIGHT * TS);

		final var sceneRoot = new Group(content, new CoordinateSystem(subSceneFX.getWidth()).getNode());
		subSceneFX.setRoot(sceneRoot);

		animationController.init();
	}

	@Override
	public void onGameEvent(PacManGameEvent gameEvent) {
		animationController.onGameEvent(gameEvent);
	}

	@Override
	public void update() {
		score3D.update(game());
		updateLivesCounter3D(game());
		player3D.update();
		ghosts3D.values().forEach(Ghost3D::update);
		bonus3D.update(game().bonus());
		selectedPerspective().ifPresent(camera -> {
			// Keep score text in plain sight to viewer.
			// TODO: is this the recommended way to do this?
			score3D.setRotationAxis(Rotate.X_AXIS);
			score3D.setRotate(camera.camera().getRotate());
			camera.follow(player3D);
		});
		animationController.update();
	}

	@Override
	public void end() {
		log("%s: end", this);
	}

	private Group createLivesCounter3D(V2i tilePosition) {
		Group livesCounter = new Group();
		for (int i = 0; i < LIVES_COUNTER_MAX; ++i) {
			V2i tile = tilePosition.plus(2 * i, 0);
			Group liveIndicator = GianmarcosModel3D.createPacMan();
			liveIndicator.setTranslateX(tile.x * TS);
			liveIndicator.setTranslateY(tile.y * TS);
			liveIndicator.setTranslateZ(0);
			livesCounter.getChildren().add(liveIndicator);
		}
		return livesCounter;
	}

	private void updateLivesCounter3D(GameModel game) {
		for (int i = 0; i < LIVES_COUNTER_MAX; ++i) {
			livesCounter3D.getChildren().get(i).setVisible(i < game.lives());
		}
	}
}