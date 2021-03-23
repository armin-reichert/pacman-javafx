package de.amr.games.pacman.ui.fx.scenes.common.scene3d;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static java.util.function.Predicate.not;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.ui.fx.rendering.standard.Assets2D;
import de.amr.games.pacman.ui.fx.scenes.common.CameraController;
import de.amr.games.pacman.ui.fx.scenes.common.GameScene;
import de.amr.games.pacman.ui.sound.PacManGameSound;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.AmbientLight;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SubScene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.transform.Rotate;
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

	private AmbientLight ambientLight = new AmbientLight(Color.AZURE);
	private PointLight pointLight = new PointLight(Color.AZURE);
	private CoordinateSystem coordSystem;
	private Group tgMaze;
	private Player3D player;
	private Map<Ghost, Ghost3D> ghosts3D;
	private List<Brick3D> bricks;
	private List<Energizer3D> energizers;
	private List<Pellet3D> pellets;
	private ScoreNotReally3D score3D;
	private LivesCounter3D livesCounter3D;

	public PlayScene3D(Stage stage) {
		staticCamera = new PerspectiveCamera(true);
		moveableCamera = new PerspectiveCamera(true);
		fxScene = new SubScene(new Group(), stage.getScene().getWidth(), stage.getScene().getHeight());
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
		final GameVariant gameVariant = gameController.gameVariant();
		final GameModel game = gameController.game();

		fxScene.setFill(Color.rgb(20, 20, 60));
		buildWalls(Assets2D.getMazeWallBorderColor(game.level.mazeNumber));

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
		tgMaze.setTranslateX(-GameScene.UNSCALED_SCENE_WIDTH / 2);
		tgMaze.setTranslateY(-GameScene.UNSCALED_SCENE_HEIGHT / 2);

		tgMaze.getChildren().addAll(score3D.get(), livesCounter3D.get());
		tgMaze.getChildren().addAll(collect(bricks));
		tgMaze.getChildren().addAll(collect(energizers));
		tgMaze.getChildren().addAll(collect(pellets));
		tgMaze.getChildren().addAll(player.get());
		tgMaze.getChildren().addAll(collect(ghosts3D.values()));
		tgMaze.getChildren().addAll(ambientLight, pointLight);

		coordSystem = new CoordinateSystem(150);
		fxScene.setRoot(new Group(coordSystem.getNode(), tgMaze));
	}

	private static class Tile9th {

		@Override
		public int hashCode() {
			return Objects.hash(i, tile);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Tile9th other = (Tile9th) obj;
			return i == other.i && Objects.equals(tile, other.tile);
		}

		private final double x;
		private final double y;
		private final V2i tile;
		private final int i;

		public Tile9th(double x, double y, V2i tile, int i) {
			this.x = x;
			this.y = y;
			this.tile = tile;
			this.i = i;
		}
	}

	private void buildWalls(Color wallColor) {
		GameModel game = gameController.game();

		List<Tile9th> smallBricks = new ArrayList<>();
		game.level.world.tiles().filter(game.level.world::isWall).forEach(tile -> {
			double w = 8.0 / 3, h = 8.0 / 3, d = TS / 2;
			double bx = tile.x * TS - w, by = tile.y * TS - h;
			List<Tile9th> small = new ArrayList<>();
			//@formatter:off
			small.add(new Tile9th(bx,     by,     tile, 0));
			small.add(new Tile9th(bx+w,   by,     tile, 1));
			small.add(new Tile9th(bx+2*w, by,     tile, 2));
			small.add(new Tile9th(bx,     by+h,   tile, 3));
			small.add(new Tile9th(bx+w,   by+h,   tile, 4));
			small.add(new Tile9th(bx+2*w, by+h,   tile, 5));
			small.add(new Tile9th(bx,     by+2*h, tile, 6));
			small.add(new Tile9th(bx+w,   by+2*h, tile, 7));
			small.add(new Tile9th(bx+2*w, by+2*h, tile, 8));
			//@formatter:on
			smallBricks.addAll(small);
		});

		List<Tile9th> inside = new ArrayList<>();
		for (Tile9th b : smallBricks) {
			V2i north = b.tile.plus(0, b.i < 3 ? -1 : 0);
			V2i east = b.tile.plus(b.i == 2 || b.i == 5 || b.i == 8 ? 1 : 0, 0);
			V2i south = b.tile.plus(0, b.i > 5 ? 1 : 0);
			V2i west = b.tile.plus(b.i == 0 || b.i == 3 || b.i == 6 ? -1 : 0, 0);
			if (game.level.world.isWall(north) && game.level.world.isWall(east) && game.level.world.isWall(south)
					&& game.level.world.isWall(west)) {
				inside.add(b);
			}
		}
		smallBricks.removeAll(inside);

		PhongMaterial wallMaterial = new PhongMaterial(wallColor);
		Image texture = Assets3D.randomArea(Assets3D.WALL_TEXTURE, 64, 64);
		wallMaterial.setBumpMap(texture);
		wallMaterial.setDiffuseMap(texture);
		bricks = smallBricks.stream().map(t -> new Brick3D(t.x, t.y, 2.5, 2.5, 2, wallMaterial, t.tile))
				.collect(Collectors.toList());
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
		// TODO remove again
		gameController.game().player.immune = true;
		buildSceneGraph();
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
		livesCounter3D.get().setVisible(gameController.isPlaying());
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
			if (gameController.isPlayingRequested()) {
				gameController.timer().resetSeconds(4.5);
				// TODO use FX sound
				Assets2D.SOUND.get(gameController.gameVariant()).play(PacManGameSound.GAME_READY);
			} else {
				gameController.timer().resetSeconds(2);
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

		gameController.timer().reset();
		PauseTransition pause = new PauseTransition(Duration.seconds(3));
		pause.setOnFinished(e -> gameController.letCurrentGameStateExpire());
		pause.play();
	}

	private void playLevelStartingAnimation() {
		gameController.timer().reset();
		gameController.userInterface.showFlashMessage("Entering Level " + gameController.game().levelNumber);

		ScaleTransition animation = new ScaleTransition(Duration.seconds(3), tgMaze);
		animation.setDelay(Duration.seconds(2));
		animation.setFromZ(0);
		animation.setToZ(1);
		animation.setOnFinished(e -> gameController.letCurrentGameStateExpire());
		animation.play();
	}
}