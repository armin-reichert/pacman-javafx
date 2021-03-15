package de.amr.games.pacman.ui.fx.common;

import static de.amr.games.pacman.model.world.PacManGameWorld.HTS;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameType;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.model.world.PacManGameWorld;
import de.amr.games.pacman.ui.animation.TimedSequence;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_Constants;
import javafx.animation.ScaleTransition;
import javafx.scene.AmbientLight;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SubScene;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

/**
 * 3D scene displaying the maze and the game play for both, Pac-Man and Ms. Pac-Man games.
 * 
 * @author Armin Reichert
 */
public class PlayScene3D implements GameScene {

	private static final int WALL_HEIGHT = TS - 2;

	private static Color ghostColor(int id) {
		return id == 0 ? Color.RED : id == 1 ? Color.PINK : id == 2 ? Color.CYAN : Color.ORANGE;
	}

	private final PacManGameController controller;
	private final SubScene subScene;
	private final Group sceneRoot = new Group();
	private final PerspectiveCamera camera = new PerspectiveCamera(true);
	private double scaling;

	private final PointLight spotLight = new PointLight();
	private final AmbientLight ambientLight = new AmbientLight();

	private final Group tgAxes = createAxes();
	private final Group tgMaze = new Group();
	private final Group tgPlayer = new Group();
	private final Group[] tgGhosts = new Group[4];
	private final Map<V2i, Node> wallNodes = new HashMap<>();
	private final List<Node> energizerNodes = new ArrayList<>();
	private final List<Node> pelletNodes = new ArrayList<>();

	private final GridPane scoreLayout = new GridPane();
	private final Text txtScore = new Text();
	private final Text txtHiscore = new Text();
	private final Font scoreFont = Font.loadFont(getClass().getResource("/emulogic.ttf").toExternalForm(), TS);
	private final PhongMaterial wallMaterial = createWallMaterial();

	private final TimedSequence<Node> missingAnimation = createDefaultAnimation();
	private final TimedSequence<Boolean> energizerBlinking = TimedSequence.pulse().frameDuration(15);
	private TimedSequence<?> playerMunchingAnimation;
	private TimedSequence<?> playerDyingAnimation = missingAnimation;
	private final Map<Ghost, TimedSequence<?>> ghostReturningHomeAnimationByGhost = new HashMap<>();
	private final Map<Ghost, TimedSequence<?>> ghostFlashingAnimationByGhost = new HashMap<>();
	private final Map<Ghost, TimedSequence<?>> ghostFrightenedAnimationByGhost = new HashMap<>();
	private final Map<Ghost, TimedSequence<?>> ghostKickingAnimationByGhost = new HashMap<>();

	public PlayScene3D(PacManGameController controller, double height) {
		this.controller = controller;
		double width = GameScene.ASPECT_RATIO * height;
		scaling = width / GameScene.WIDTH_UNSCALED;
		sceneRoot.getTransforms().add(new Scale(scaling, scaling, scaling));
		subScene = new SubScene(sceneRoot, width, height);
		subScene.setFill(Color.BLACK);
		subScene.setCamera(camera);
	}

	@Override
	public void resize(double width, double height) {
		scaling = width / GameScene.WIDTH_UNSCALED;
		sceneRoot.getTransforms().clear();
		sceneRoot.getTransforms().add(new Scale(scaling, scaling, scaling));
		subScene.setWidth(width);
		subScene.setHeight(height);
	}

	@Override
	public OptionalDouble aspectRatio() {
		return OptionalDouble.empty();
	}

	@Override
	public SubScene getSubScene() {
		return subScene;
	}

	@Override
	public Camera getCamera() {
		return camera;
	}

	// Animations

	@Override
	public void initCamera() {
		camera.setTranslateX(0);
		camera.setTranslateY(2110);
		camera.setTranslateZ(-2000);
		camera.setNearClip(0.1);
		camera.setFarClip(10000.0);
		camera.setRotationAxis(Rotate.X_AXIS);
		camera.setRotate(45);
	}

	@Override
	public void start() {
		GameModel game = controller.selectedGame();
		PacManGameWorld world = game.level.world;
		buildScene(game, world);
		addStateListeners();
	}

	@Override
	public void end() {
		removeStateListeners();
	}

	private void addStateListeners() {
		controller.addStateEntryListener(PacManGameState.HUNTING, this::onHuntingStateEntry);
		controller.addStateExitListener(PacManGameState.HUNTING, this::onHuntingStateExit);
		controller.addStateEntryListener(PacManGameState.LEVEL_COMPLETE, this::playLevelCompleteAnimation);
		controller.addStateEntryListener(PacManGameState.LEVEL_STARTING, this::playLevelStartingAnimation);
	}

	private void removeStateListeners() {
		controller.removeStateEntryListener(this::onHuntingStateEntry);
		controller.removeStateExitListener(this::onHuntingStateExit);
		controller.removeStateEntryListener(this::playLevelCompleteAnimation);
		controller.removeStateEntryListener(this::playLevelStartingAnimation);
	}

	public void buildScene(GameModel game, PacManGameWorld world) {

		wallNodes.clear();
		world.tiles().forEach(tile -> {
			if (world.isWall(tile)) {
				Box wallShape = new Box(TS - 1, TS - 1, WALL_HEIGHT);
				wallShape.setTranslateX(tile.x * TS);
				wallShape.setTranslateY(tile.y * TS);
				if (controller.isPlaying(GameType.PACMAN)) {
//					wallMaterial.setDiffuseColor(Color.BLUE);
//					wallMaterial.setSpecularColor(Color.LIGHTBLUE);
				} else {
//					wallMaterial.setDiffuseColor(MsPacMan_Constants.getMazeWallColor(game.level.mazeNumber));
//					wallMaterial.setSpecularColor(MsPacMan_Constants.getMazeWallColor(game.level.mazeNumber));
				}
				wallShape.setMaterial(wallMaterial);
				wallShape.drawModeProperty().bind(Env.$drawMode);
				wallShape.setViewOrder(-tile.y * TS);
				wallNodes.put(tile, wallShape);
			}
		});

		Color foodColor = controller.isPlaying(GameType.PACMAN) ? Color.rgb(250, 185, 176)
				: MsPacMan_Constants.getMazeFoodColor(game.level.mazeNumber);

		PhongMaterial energizerMaterial = new PhongMaterial(foodColor);
		energizerNodes.clear();
		world.energizerTiles().forEach(tile -> {
			Sphere energizer = new Sphere(HTS);
			energizer.setMaterial(energizerMaterial);
			energizer.setUserData(tile);
			energizer.setTranslateX(tile.x * TS);
			energizer.setTranslateY(tile.y * TS);
			energizer.setViewOrder(-tile.y * TS - 0.5);
			energizerNodes.add(energizer);
		});

		PhongMaterial foodMaterial = new PhongMaterial(foodColor);
		pelletNodes.clear();
		world.tiles().filter(world::isFoodTile).filter(tile -> !world.isEnergizerTile(tile)).forEach(tile -> {
			Sphere pellet = new Sphere(1.5);
			pellet.setMaterial(foodMaterial);
			pellet.setUserData(tile);
			pellet.setTranslateX(tile.x * TS);
			pellet.setTranslateY(tile.y * TS);
			pellet.setViewOrder(-tile.y * TS - 0.5);
			pelletNodes.add(pellet);
		});

		for (int id = 0; id < 4; ++id) {
			tgGhosts[id] = new Group();
		}

		Text txtScoreTitle = new Text("SCORE");
		txtScoreTitle.setFill(Color.WHITE);
		txtScoreTitle.setFont(scoreFont);

		txtScore.setFill(Color.YELLOW);
		txtScore.setFont(scoreFont);

		Text txtHiscoreTitle = new Text("HI SCORE");
		txtHiscoreTitle.setFill(Color.WHITE);
		txtHiscoreTitle.setFont(scoreFont);

		txtHiscore.setFill(Color.YELLOW);
		txtHiscore.setFont(scoreFont);

		scoreLayout.setHgap(4 * TS);
		scoreLayout.setTranslateY(-2 * TS);
		scoreLayout.setTranslateZ(-2 * TS);
		scoreLayout.getChildren().clear();
		scoreLayout.add(txtScoreTitle, 0, 0);
		scoreLayout.add(txtScore, 0, 1);
		scoreLayout.add(txtHiscoreTitle, 1, 0);
		scoreLayout.add(txtHiscore, 1, 1);

		tgMaze.getChildren().clear();
		tgMaze.getChildren().addAll(wallNodes.values());
		tgMaze.getChildren().addAll(energizerNodes);
		tgMaze.getChildren().addAll(pelletNodes);
		tgMaze.getChildren().addAll(tgGhosts);
		tgMaze.getChildren().addAll(tgPlayer);
		tgMaze.getChildren().add(scoreLayout);
		tgMaze.setTranslateX(-14 * TS);
		tgMaze.setTranslateY(-18 * TS);

		sceneRoot.getChildren().clear();
		sceneRoot.getChildren().addAll(ambientLight, spotLight, tgMaze, tgAxes);

		initLight();
		initCamera();
	}

	private void initLight() {
		ambientLight.setTranslateZ(-500);
		ambientLight.setColor(mazeColor());

		spotLight.translateXProperty().bind(tgPlayer.translateXProperty());
		spotLight.translateYProperty().bind(tgPlayer.translateYProperty());
		spotLight.setTranslateZ(-2 * TS);
		spotLight.setRotationAxis(Rotate.X_AXIS);
		spotLight.setRotate(180);
		spotLight.setColor(Color.YELLOW);
	}

	private PhongMaterial createWallMaterial() {
		PhongMaterial m = new PhongMaterial();
		Image wallImage = new Image(getClass().getResource("/common/stonewall.png").toExternalForm());
		m.setBumpMap(wallImage);
		m.setDiffuseMap(wallImage);
		return m;
	}

	private Group createAxes() {
		Cylinder xAxis = createAxis("X", Color.RED, 300);
		Cylinder yAxis = createAxis("Y", Color.GREEN, 300);
		Cylinder zAxis = createAxis("Z", Color.BLUE, 300);
		xAxis.setRotationAxis(Rotate.Z_AXIS);
		xAxis.setRotate(90);
		zAxis.setRotationAxis(Rotate.X_AXIS);
		zAxis.setRotate(-90);
		Group g = new Group(xAxis, yAxis, zAxis);
		g.visibleProperty().bind(Env.$showAxes);
		return g;
	}

	private Cylinder createAxis(String label, Color color, double length) {
		Cylinder axis = new Cylinder(0.5, length);
		axis.setMaterial(new PhongMaterial(color));
		return axis;
	}

	@Override
	public void update() {
		GameModel game = controller.selectedGame();

		energizerBlinking.animate();
		energizerNodes.forEach(energizer -> {
			V2i tile = (V2i) energizer.getUserData();
			energizer.setVisible(!game.level.isFoodRemoved(tile) && energizerBlinking.frame());
		});

		pelletNodes.forEach(pellet -> {
			V2i tile = (V2i) pellet.getUserData();
			pellet.setVisible(!game.level.isFoodRemoved(tile));
		});

		updatePlayerShape(game.player);

		for (Ghost ghost : game.ghosts) {
			updateGhostShape(ghost.id);
		}

		updateScores(game);
	}

	public void updateScores(GameModel game) {
		txtScore.setText(String.format("%07d L%d", game.score, game.levelNumber));
		txtHiscore.setText(String.format("%07d L%d", game.highscorePoints, game.highscoreLevel));
		scoreLayout.setRotationAxis(Rotate.X_AXIS);
		scoreLayout.setRotate(camera.getRotate());
	}

	private void updatePlayerShape(Pac player) {
		Node shape = player.dead ? (Node) playerDyingAnimation.frame() : (Node) playerMunching(player, player.dir).frame();
		boolean insidePortal = controller.selectedGame().level.world.isPortal(player.tile());
		tgPlayer.getChildren().clear();
		tgPlayer.getChildren().add(shape);
		tgPlayer.setVisible(player.visible && !insidePortal);
		tgPlayer.setTranslateX(player.position.x);
		tgPlayer.setTranslateY(player.position.y);
		tgPlayer.setViewOrder(-player.position.y - 0.2);
	}

	private void updateGhostShape(int id) {
		Ghost ghost = controller.selectedGame().ghosts[id];
		Node shape;

		if (ghost.bounty > 0) {
			shape = createGhostBountyText(ghost);
		}

		else if (ghost.is(GhostState.DEAD) || ghost.is(GhostState.ENTERING_HOUSE)) {
			shape = (Node) ghostReturningHome(ghost, ghost.dir).animate();
			if (ghost.dir == Direction.DOWN || ghost.dir == Direction.UP) {
				shape.setRotate(0);
			} else {
				shape.setRotate(90);
			}
		}

		else if (ghost.is(GhostState.FRIGHTENED)) {
			shape = (Node) (ghostFlashing(ghost).isRunning() ? ghostFlashing(ghost).frame()
					: ghostFrightened(ghost, ghost.dir).animate());
		}

		else if (ghost.is(GhostState.LOCKED) && controller.selectedGame().player.powerTimer.isRunning()) {
			shape = (Node) ghostFrightened(ghost, ghost.dir).animate();
		}

		else {
			// default: show ghost in color, alive and kicking
			shape = (Node) ghostKicking(ghost, ghost.wishDir).animate(); // Looks towards wish dir!
		}

		tgGhosts[id].setVisible(ghost.visible);
		tgGhosts[id].setTranslateX(ghost.position.x);
		tgGhosts[id].setTranslateY(ghost.position.y);
		tgGhosts[id].setViewOrder(-ghost.position.y - 0.2);
		tgGhosts[id].getChildren().clear();
		tgGhosts[id].getChildren().add(shape);
	}

	private Color mazeColor() {
		if (controller.isPlaying(GameType.PACMAN)) {
			return Color.BLUE;
		}
		return MsPacMan_Constants.getMazeWallColor(controller.selectedGame().level.mazeNumber);
	}

	private void onHuntingStateEntry(PacManGameState state) {
		energizerBlinking.restart();
	}

	private void onHuntingStateExit(PacManGameState state) {
		energizerBlinking.reset();
	}

	private Node createGhostBountyText(Ghost ghost) {
		DropShadow shadow = new DropShadow(0.3, Color.color(0.4, 0.4, 0.4));
		Text bountyText = new Text();
		bountyText.setEffect(shadow);
		bountyText.setCache(true);
		bountyText.setText(String.valueOf(ghost.bounty));
		bountyText.setFont(Font.font("Sans", FontWeight.BOLD, TS));
		bountyText.setFill(Color.CYAN);
		bountyText.setTranslateZ(-1.5 * TS);
		bountyText.setRotationAxis(Rotate.X_AXIS);
		bountyText.setRotate(camera.getRotate());
		return bountyText;
	}

	// State change handling

	private void playLevelCompleteAnimation(PacManGameState state) {
		tgPlayer.setVisible(false);
		Arrays.asList(tgGhosts).forEach(ghostShape -> ghostShape.setVisible(false));
		ScaleTransition levelCompleteAnimation = new ScaleTransition(Duration.seconds(5), tgMaze);
		levelCompleteAnimation.setFromZ(1);
		levelCompleteAnimation.setToZ(0);
		levelCompleteAnimation.setDelay(Duration.seconds(2));
		controller.state.timer.resetSeconds(2 + levelCompleteAnimation.getDuration().toSeconds());
		levelCompleteAnimation.play();
	}

	private void playLevelStartingAnimation(PacManGameState state) {
		tgPlayer.setVisible(true);
		Arrays.asList(tgGhosts).forEach(ghostShape -> ghostShape.setVisible(true));
		ScaleTransition levelStartAnimation = new ScaleTransition(Duration.seconds(5), tgMaze);
		levelStartAnimation.setFromZ(0);
		levelStartAnimation.setToZ(1);
		controller.state.timer.resetSeconds(levelStartAnimation.getDuration().toSeconds());
		levelStartAnimation.play();
	}

	private TimedSequence<Node> createDefaultAnimation() {
		Text text = new Text("Animation?");
		text.setFill(Color.RED);
		text.setFont(Font.font("Sans", FontWeight.BOLD, 12));
		text.setTranslateZ(-40);
		text.setRotationAxis(Rotate.X_AXIS);
		text.setRotate(90);
		return TimedSequence.of(text);
	}

	public TimedSequence<?> ghostFlashing(Ghost ghost) {
		if (!ghostFlashingAnimationByGhost.containsKey(ghost)) {
			Sphere s1 = new Sphere(HTS);
			s1.setMaterial(new PhongMaterial(Color.CORNFLOWERBLUE));
			Sphere s2 = new Sphere(HTS);
			s2.setMaterial(new PhongMaterial(Color.WHITE));
			ghostFlashingAnimationByGhost.put(ghost, TimedSequence.of(s1, s2).frameDuration(10).endless());
		}
		return ghostFlashingAnimationByGhost.get(ghost);
	}

	public TimedSequence<?> ghostFrightened(Ghost ghost, Direction dir) {
		if (!ghostFrightenedAnimationByGhost.containsKey(ghost)) {
			Sphere s = new Sphere(HTS);
			s.setMaterial(new PhongMaterial(Color.CORNFLOWERBLUE));
			s.setUserData(ghost);
			ghostFrightenedAnimationByGhost.put(ghost, TimedSequence.of(s));
		}
		return ghostFrightenedAnimationByGhost.get(ghost);
	}

	public TimedSequence<?> ghostKicking(Ghost ghost, Direction dir) {
		if (!ghostKickingAnimationByGhost.containsKey(ghost)) {
			Sphere s = new Sphere(HTS);
			s.setMaterial(new PhongMaterial(ghostColor(ghost.id)));
			s.setUserData(ghost);
			ghostKickingAnimationByGhost.put(ghost, TimedSequence.of(s));
		}
		return ghostKickingAnimationByGhost.get(ghost);
	}

	public TimedSequence<?> ghostReturningHome(Ghost ghost, Direction dir) {
		if (!ghostReturningHomeAnimationByGhost.containsKey(ghost)) {
			PhongMaterial material = new PhongMaterial(ghostColor(ghost.id));
			Sphere[] parts = new Sphere[4];
			for (int i = 0; i < parts.length; ++i) {
				parts[i] = new Sphere(1);
				parts[i].setMaterial(material);
				parts[i].setTranslateY(i * 3);
			}
			Group g = new Group(parts);
			g.setUserData(ghost);
			ghostReturningHomeAnimationByGhost.put(ghost, TimedSequence.of(g));
		}
		return ghostReturningHomeAnimationByGhost.get(ghost);
	}

	public TimedSequence<?> playerMunching(Pac player, Direction dir) {
		if (playerMunchingAnimation == null) {
			Box box = new Box(TS, TS, TS);
			PhongMaterial m = new PhongMaterial(Color.WHITE);
			Image playerImage = playerImage(player);
			m.setBumpMap(playerImage);
			m.setDiffuseMap(playerImage);
			box.setMaterial(m);
			box.setUserData(player);
			playerMunchingAnimation = TimedSequence.of(box);
		}
		return playerMunchingAnimation;
	}

	private Image playerImage(Pac player) {
		Image spritesheet = new Image(getClass().getResource("/mspacman/graphics/sprites.png").toExternalForm());
		WritableImage img = new WritableImage(16, 16);
		img.getPixelWriter().setPixels(0, 0, 16, 16, spritesheet.getPixelReader(), 472, 0);
		return img;
	}
}