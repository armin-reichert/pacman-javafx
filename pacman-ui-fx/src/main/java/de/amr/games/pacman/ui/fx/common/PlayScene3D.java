package de.amr.games.pacman.ui.fx.common;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.interactivemesh.jfx.importer.ImportException;
import com.interactivemesh.jfx.importer.obj.ObjModelImporter;

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
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Mesh;
import javafx.scene.shape.MeshView;
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
	private static final Font scoreFont = Font.loadFont(PlayScene3D.class.getResource("/emulogic.ttf").toExternalForm(),
			TS);

	private static Color ghostColor(int id) {
		return id == 0 ? Color.TOMATO : id == 1 ? Color.PINK : id == 2 ? Color.CYAN : Color.ORANGE;
	}

	private final PacManGameController controller;
	private final SubScene subScene;
	private final Group sceneRoot;
	private final PerspectiveCamera staticCamera;
	private final PerspectiveCamera movingCamera;

	private Mesh ghostMeshPrototype;
	private Map<String, MeshView> meshViews;

	private Group tgAxes;
	private Group tgMaze;
	private Group tgPlayer;
	private Map<Ghost, MeshView> ghostMeshViews;
	private Map<V2i, Node> wallNodes;
	private List<Node> energizerNodes;
	private List<Node> pelletNodes;

	private Group tgScore;
	private Text txtScore;
	private Text txtHiscore;

	private final TimedSequence<Boolean> energizerBlinking = TimedSequence.pulse().frameDuration(15);

	public PlayScene3D(PacManGameController controller, double height) {
		this.controller = controller;
		double width = GameScene.ASPECT_RATIO * height;
		staticCamera = new PerspectiveCamera(true);
		movingCamera = new PerspectiveCamera(true);
		sceneRoot = new Group();
		subScene = new SubScene(sceneRoot, width, height);
		subScene.setFill(Color.BLACK);
		subScene.setCamera(staticCamera);
	}

	private void loadMeshes() {
		ObjModelImporter objImporter = new ObjModelImporter();
		try {
			objImporter.read(getClass().getResource("/common/ghost.obj"));
			ghostMeshPrototype = objImporter.getNamedMeshViews().get("Ghost_Sphere.001").getMesh();
			objImporter.read(getClass().getResource("/common/pacman1.obj"));
			meshViews = objImporter.getNamedMeshViews();
			meshViews.forEach((name, view) -> log("Mesh '%s': %s", name, view));
			log("Mesh views loaded successfully!");
		} catch (ImportException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void resize(double width, double height) {
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
	public Camera getStaticCamera() {
		return staticCamera;
	}

	@Override
	public Camera getMovingCamera() {
		return movingCamera;
	}

	@Override
	public void start() {
		controller.setPlayerImmune(true);
		loadMeshes();
		buildScene();
		addStateListeners();
	}

	@Override
	public void update() {
		GameModel game = controller.selectedGame();
		updateScores();
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
			updateGhostNode(ghost);
		}
		if (Env.$useStaticCamera.get()) {
			subScene.setCamera(staticCamera);
		} else {
			subScene.setCamera(movingCamera);
			updateMovingCamera();
		}
	}

	@Override
	public void end() {
		removeStateListeners();
	}

	private void addLights() {
		AmbientLight ambientLight = new AmbientLight(mazeColor());
		ambientLight.setTranslateZ(-100);
		tgMaze.getChildren().add(ambientLight);

		PointLight spot = new PointLight(Color.LIGHTBLUE);
		spot.setTranslateZ(-500);
		tgMaze.getChildren().add(spot);
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

	private void buildScene() {
		final GameModel game = controller.selectedGame();
		final PacManGameWorld world = game.level.world;

		createAxes();
		createScore();

		PhongMaterial wallMaterial = createWallMaterial();
		wallNodes = world.tiles().filter(world::isWall)
				.collect(Collectors.toMap(Function.identity(), tile -> createWallShape(tile, wallMaterial)));

		PhongMaterial foodMaterial = new PhongMaterial(foodColor());
		energizerNodes = world.energizerTiles().map(tile -> createEnergizerShape(tile, foodMaterial))
				.collect(Collectors.toList());
		pelletNodes = world.tiles().filter(world::isFoodTile).filter(tile -> !world.isEnergizerTile(tile))
				.map(tile -> createPelletShape(tile, foodMaterial)).collect(Collectors.toList());

		tgPlayer = new Group();
		ghostMeshViews = game.ghosts().collect(Collectors.toMap(Function.identity(), ghost -> createGhostMeshView(ghost)));

		tgMaze = new Group();
		// center over origin
		tgMaze.setTranslateX(-GameScene.WIDTH_UNSCALED / 2);
		tgMaze.setTranslateY(-GameScene.HEIGHT_UNSCALED / 2);
		tgMaze.getChildren().add(tgScore);
		tgMaze.getChildren().addAll(wallNodes.values());
		tgMaze.getChildren().addAll(energizerNodes);
		tgMaze.getChildren().addAll(pelletNodes);
		tgMaze.getChildren().addAll(tgPlayer);
		tgMaze.getChildren().addAll(ghostMeshViews.values());
		addLights();

		sceneRoot.getChildren().clear();
		sceneRoot.getChildren().addAll(tgMaze, tgAxes);

		staticCamera.setNearClip(0.1);
		staticCamera.setFarClip(10000.0);
		staticCamera.setTranslateX(0);
		staticCamera.setTranslateY(270);
		staticCamera.setTranslateZ(-460);
		staticCamera.setRotationAxis(Rotate.X_AXIS);
		staticCamera.setRotate(30);

		movingCamera.setNearClip(0.1);
		movingCamera.setFarClip(10000.0);
		movingCamera.setTranslateZ(-300);
		movingCamera.setRotationAxis(Rotate.X_AXIS);
		movingCamera.setRotate(30);
	}

	private void updateMovingCamera() {
		double x = Math.min(10.0, lerp(movingCamera.getTranslateX(), tgPlayer.getTranslateX()));
		double y = Math.max(120, lerp(movingCamera.getTranslateY(), tgPlayer.getTranslateY()));
		movingCamera.setTranslateX(x);
		movingCamera.setTranslateY(y);
	}

	private double lerp(double current, double target) {
		return current + (target - current) * 0.02;
	}

	private void createScore() {
		Text txtScoreTitle = new Text("SCORE");
		txtScoreTitle.setFill(Color.WHITE);
		txtScoreTitle.setFont(scoreFont);

		txtScore = new Text();
		txtScore.setFill(Color.YELLOW);
		txtScore.setFont(scoreFont);

		Text txtHiscoreTitle = new Text("HI SCORE");
		txtHiscoreTitle.setFill(Color.WHITE);
		txtHiscoreTitle.setFont(scoreFont);

		txtHiscore = new Text();
		txtHiscore.setFill(Color.YELLOW);
		txtHiscore.setFont(scoreFont);

		GridPane grid = new GridPane();
		grid.setHgap(4 * TS);
		grid.setTranslateY(-2 * TS);
		grid.setTranslateZ(-2 * TS);
		grid.getChildren().clear();
		grid.add(txtScoreTitle, 0, 0);
		grid.add(txtScore, 0, 1);
		grid.add(txtHiscoreTitle, 1, 0);
		grid.add(txtHiscore, 1, 1);

		tgScore = new Group(grid);
	}

	private Node createWallShape(V2i tile, PhongMaterial material) {
		Box b = new Box(TS - 1, TS - 1, WALL_HEIGHT);
		b.setMaterial(material);
		b.setTranslateX(tile.x * TS);
		b.setTranslateY(tile.y * TS);
		b.setViewOrder(-tile.y * TS);
		b.drawModeProperty().bind(Env.$drawMode);
		return b;
	}

	private Node createEnergizerShape(V2i tile, PhongMaterial material) {
		Sphere s = new Sphere(2);
		s.setMaterial(material);
		s.setTranslateX(tile.x * TS);
		s.setTranslateY(tile.y * TS);
		s.setUserData(tile);
		s.setViewOrder(-tile.y * TS - 1);
		return s;
	}

	private Node createPelletShape(V2i tile, PhongMaterial material) {
		Sphere pellet = new Sphere(1);
		pellet.setMaterial(material);
		pellet.setUserData(tile);
		pellet.setTranslateX(tile.x * TS);
		pellet.setTranslateY(tile.y * TS);
		pellet.setViewOrder(-tile.y * TS - 1);
		return pellet;
	}

	private PhongMaterial createWallMaterial() {
		PhongMaterial m = new PhongMaterial();
		Image wallImage = new Image(getClass().getResource("/common/stonewall.png").toExternalForm());
		m.setBumpMap(wallImage);
		m.setDiffuseMap(wallImage);
		return m;
	}

	private void createAxes() {
		Cylinder xAxis = createAxis("X", Color.RED, 300);
		Cylinder yAxis = createAxis("Y", Color.GREEN, 300);
		Cylinder zAxis = createAxis("Z", Color.BLUE, 300);
		xAxis.setRotationAxis(Rotate.Z_AXIS);
		xAxis.setRotate(90);
		zAxis.setRotationAxis(Rotate.X_AXIS);
		zAxis.setRotate(-90);
		tgAxes = new Group(xAxis, yAxis, zAxis);
		tgAxes.visibleProperty().bind(Env.$showAxes);
	}

	private Cylinder createAxis(String label, Color color, double length) {
		Cylinder axis = new Cylinder(0.5, length);
		axis.setMaterial(new PhongMaterial(color));
		return axis;
	}

	private void updateScores() {
		GameModel game = controller.selectedGame();
		txtScore.setText(String.format("%07d L%d", game.score, game.levelNumber));
		txtHiscore.setText(String.format("%07d L%d", game.highscorePoints, game.highscoreLevel));
		// TODO is this the right way or should the score be kept outside the subscene?
		tgScore.setRotationAxis(Rotate.X_AXIS);
		tgScore.setRotate(staticCamera.getRotate());
	}

	private void updatePlayerShape(Pac player) {
		tgPlayer.getChildren().clear();
		tgPlayer.getChildren().add(playerNode(player));
		tgPlayer.setVisible(player.visible);
		tgPlayer.setTranslateX(player.position.x);
		tgPlayer.setTranslateY(player.position.y);
		tgPlayer.setViewOrder(-player.position.y - 2);
	}

	private Node playerNode(Pac player) {
		MeshView body = meshViews.get("Sphere_Sphere.002_Material.001");
//	body.setMaterial(new PhongMaterial(Color.YELLOW));
//	body.setDrawMode(DrawMode.LINE);
		MeshView glasses = meshViews.get("Sphere_Sphere.002_Material.002");
		glasses.setMaterial(new PhongMaterial(Color.rgb(50, 50, 50)));
//	glasses.setDrawMode(DrawMode.LINE);
		Group shape = new Group(body, glasses);
		shape.setScaleX(8 / 3.0);
		shape.setScaleY(8 / 3.0);
		shape.setScaleZ(8 / 3.0);
		shape.setRotationAxis(Rotate.X_AXIS);
		shape.setRotate(90);
		return shape;
	}

	private MeshView createGhostMeshView(Ghost ghost) {
		MeshView meshView = new MeshView(ghostMeshPrototype);
		meshView.setMaterial(new PhongMaterial(ghostColor(ghost.id)));
		meshView.setUserData(ghost);
		return meshView;
	}

	private void updateGhostNode(Ghost ghost) {
		Node ghostNode;
		if (ghost.bounty > 0) {
			ghostNode = ghostBounty(ghost);
		} else if (ghost.is(GhostState.DEAD) || ghost.is(GhostState.ENTERING_HOUSE)) {
			ghostNode = ghostReturningHome(ghost);
		} else {
			ghostNode = ghostColored(ghost);
		}
		// TODO store one group per ghost in scene graph and exchange its child node?
		ghostNode.setVisible(ghost.visible);
		ghostNode.setTranslateX(ghost.position.x);
		ghostNode.setTranslateY(ghost.position.y);
		ghostNode.setViewOrder(-(ghost.position.y + 5));
		ghostNode.setUserData(ghost);
		tgMaze.getChildren().removeIf(node -> node.getUserData() == ghost);
		tgMaze.getChildren().add(ghostNode);
	}

	private Node ghostColored(Ghost ghost) {
		MeshView shape = ghostMeshViews.get(ghost);
		Color color = ghost.is(GhostState.FRIGHTENED) ? Color.CORNFLOWERBLUE : ghostColor(ghost.id);
		PhongMaterial material = (PhongMaterial) shape.getMaterial();
		material.setDiffuseColor(color);
		material.setSpecularColor(color);
//		shape.setDrawMode(DrawMode.LINE);
		shape.getTransforms().clear();
		shape.getTransforms().add(new Scale(4, 4, 4));
		shape.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
		double angle = ghost.dir == Direction.RIGHT ? 0
				: ghost.dir == Direction.DOWN ? 90 : ghost.dir == Direction.LEFT ? 180 : 270;
		shape.getTransforms().add(new Rotate(angle, Rotate.Y_AXIS));
		return shape;
	}

	private Text ghostBounty(Ghost ghost) {
		// TODO why is this text so blurred?
		Text bounty = new Text();
		bounty.setText(String.valueOf(ghost.bounty));
		bounty.setFont(Font.font("Sans", FontWeight.MEDIUM, 8));
		bounty.setFill(Color.CYAN);
		bounty.setRotationAxis(Rotate.X_AXIS);
		bounty.setRotate(staticCamera.getRotate());
		bounty.setTranslateZ(-1.5 * TS);
		return bounty;
	}

	private Group ghostReturningHome(Ghost ghost) {
		PhongMaterial material = new PhongMaterial(ghostColor(ghost.id));
		Sphere[] parts = new Sphere[3];
		for (int i = 0; i < parts.length; ++i) {
			parts[i] = new Sphere(1);
			parts[i].setMaterial(material);
			parts[i].setTranslateX(i * 3);
		}
		Group g = new Group(parts);
		g.setUserData(ghost);
		g.setRotationAxis(Rotate.Z_AXIS);
		g.setRotate(ghost.dir == Direction.UP || ghost.dir == Direction.DOWN ? 90 : 0);
		return g;
	}

	private Color mazeColor() {
		return controller.isPlaying(GameType.PACMAN) ? Color.BLUE
				: MsPacMan_Constants.getMazeWallColor(controller.selectedGame().level.mazeNumber);
	}

	private Color foodColor() {
		return controller.isPlaying(GameType.PACMAN) ? Color.rgb(250, 185, 176)
				: MsPacMan_Constants.getFoodColor(controller.selectedGame().level.mazeNumber);
	}

	private void onHuntingStateEntry(PacManGameState state) {
		energizerBlinking.restart();
	}

	private void onHuntingStateExit(PacManGameState state) {
		energizerBlinking.reset();
	}

	// State change handling

	private void playLevelCompleteAnimation(PacManGameState state) {
		ScaleTransition animation = new ScaleTransition(Duration.seconds(5), tgMaze);
		animation.setDelay(Duration.seconds(2));
		animation.setFromZ(1);
		animation.setToZ(0);
		animation.play();
		controller.state.timer.resetSeconds(7);
	}

	private void playLevelStartingAnimation(PacManGameState state) {
		ScaleTransition animation = new ScaleTransition(Duration.seconds(5), tgMaze);
		animation.setFromZ(0);
		animation.setToZ(1);
		animation.play();
		controller.state.timer.resetSeconds(5);
	}

}