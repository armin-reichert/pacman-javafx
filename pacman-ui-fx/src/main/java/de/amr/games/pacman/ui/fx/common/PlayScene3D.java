package de.amr.games.pacman.ui.fx.common;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.util.List;
import java.util.Map;
import java.util.Random;
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
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.scene.AmbientLight;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SubScene;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
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
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * 3D scene displaying the maze and the game play for both, Pac-Man and Ms. Pac-Man games.
 * 
 * @author Armin Reichert
 */
public class PlayScene3D implements GameScene {

	private static final Font ARCADE_FONT = Font.loadFont(PlayScene3D.class.getResourceAsStream("/emulogic.ttf"), TS);

	private static Color ghostColor(int id) {
		return id == 0 ? Color.TOMATO : id == 1 ? Color.PINK : id == 2 ? Color.CYAN : Color.ORANGE;
	}

	private final PacManGameController controller;
	private final SubScene subScene;
	private final Group sceneRoot;
	private final PerspectiveCamera staticCamera;
	private final PerspectiveCamera moveableCamera;
	private final CameraController cameraController;

	private Mesh ghostMeshPrototype;
	private Map<String, MeshView> meshViews;

	private Group tgAxes;
	private Group tgMaze;
	private Group tgPlayer;
	private Map<Ghost, Group> tgGhosts;
	private Map<V2i, Node> wallNodes;
	private List<Node> energizerNodes;
	private List<Node> pelletNodes;

	private Group tgScore;
	private Text txtScore;
	private Text txtHiscore;

	private Group tgLivesCounter;

	private Image wallTexture = new Image(getClass().getResource("/common/stone-texture.png").toExternalForm());
	private PhongMaterial wallMaterials[];
	private PhongMaterial livesCounterOn = new PhongMaterial(Color.YELLOW);
	private PhongMaterial livesCounterOff = new PhongMaterial(Color.GRAY);

	private final TimedSequence<Boolean> energizerBlinking = TimedSequence.pulse().frameDuration(15);

	public PlayScene3D(Stage stage, PacManGameController controller, double width, double height) {
		this.controller = controller;
		staticCamera = new PerspectiveCamera(true);
		moveableCamera = new PerspectiveCamera(true);
		sceneRoot = new Group();
		subScene = new SubScene(sceneRoot, width, height);
		subScene.setFill(Color.BLACK);
		useStaticCamera();
		cameraController = new CameraController(staticCamera);
		// TODO why doesn't subscene get key events?
		stage.addEventHandler(KeyEvent.KEY_PRESSED, cameraController::handleKeyEvent);
	}

	private void loadMeshes() {
		ObjModelImporter objImporter = new ObjModelImporter();
		try {
			objImporter.read(getClass().getResource("/common/ghost.obj"));
			ghostMeshPrototype = objImporter.getNamedMeshViews().get("Ghost_Sphere.001").getMesh();
			objImporter.read(getClass().getResource("/common/pacman1.obj"));
			meshViews = objImporter.getNamedMeshViews();
			meshViews.keySet().stream().sorted().forEach(key -> log("Mesh '%s': %s", key, meshViews.get(key)));
			log("Mesh views loaded successfully!");
		} catch (ImportException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setAvailableSize(double width, double height) {
		// data binding does the job
	}

	@Override
	public SubScene getFXSubScene() {
		return subScene;
	}

	@Override
	public Camera getActiveCamera() {
		return subScene.getCamera();
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
		updateLivesCounter();
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
		if (subScene.getCamera() == moveableCamera) {
			updateMoveableCamera();
		}
	}

	@Override
	public void end() {
		removeStateListeners();
	}

	private void buildScene() {
		final GameModel game = controller.selectedGame();
		final PacManGameWorld world = game.level.world;

		createAxes();

		createWallMaterials(10);
		wallNodes = world.tiles().filter(world::isWall).collect(Collectors.toMap(Function.identity(),
				tile -> createWallShape(tile, wallMaterials[new Random().nextInt(wallMaterials.length)])));

		PhongMaterial foodMaterial = new PhongMaterial(foodColor());
		energizerNodes = world.energizerTiles().map(tile -> createEnergizerShape(tile, foodMaterial))
				.collect(Collectors.toList());
		pelletNodes = world.tiles().filter(world::isFoodTile).filter(tile -> !world.isEnergizerTile(tile))
				.map(tile -> createPelletShape(tile, foodMaterial)).collect(Collectors.toList());

		tgPlayer = playerShape(game.player);

		tgGhosts = game.ghosts().collect(Collectors.toMap(Function.identity(), this::createGhostGroup));

		createScore();
		createLivesCounter();

		tgMaze = new Group();

		// center over origin
		tgMaze.setTranslateX(-GameScene.UNSCALED_SCENE_WIDTH / 2);
		tgMaze.setTranslateY(-GameScene.UNSCALED_SCENE_HEIGHT / 2);

		tgMaze.getChildren().addAll(tgScore, tgLivesCounter);
		tgMaze.getChildren().addAll(wallNodes.values());
		tgMaze.getChildren().addAll(energizerNodes);
		tgMaze.getChildren().addAll(pelletNodes);
		tgMaze.getChildren().addAll(tgPlayer);
		tgMaze.getChildren().addAll(tgGhosts.values());

		addLights();

		sceneRoot.getChildren().clear();
		sceneRoot.getChildren().addAll(tgMaze, tgAxes);
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

	@Override
	public void useMoveableCamera(boolean moveable) {
		if (moveable) {
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
		subScene.setCamera(staticCamera);
	}

	private void useMoveableCamera() {
		moveableCamera.setNearClip(0.1);
		moveableCamera.setFarClip(10000.0);
		moveableCamera.setTranslateZ(-250);
		moveableCamera.setRotationAxis(Rotate.X_AXIS);
		moveableCamera.setRotate(30);
		subScene.setCamera(moveableCamera);
	}

	private void updateMoveableCamera() {
		double x = Math.min(10.0, lerp(moveableCamera.getTranslateX(), tgPlayer.getTranslateX()));
		double y = Math.max(120, lerp(moveableCamera.getTranslateY(), tgPlayer.getTranslateY()));
		moveableCamera.setTranslateX(x);
		moveableCamera.setTranslateY(y);
	}

	private double lerp(double current, double target) {
		return current + (target - current) * 0.02;
	}

	private void createScore() {
		Text txtScoreTitle = new Text("SCORE");
		txtScoreTitle.setFill(Color.WHITE);
		txtScoreTitle.setFont(ARCADE_FONT);

		txtScore = new Text();
		txtScore.setFill(Color.YELLOW);
		txtScore.setFont(ARCADE_FONT);

		Text txtHiscoreTitle = new Text("HI SCORE");
		txtHiscoreTitle.setFill(Color.WHITE);
		txtHiscoreTitle.setFont(ARCADE_FONT);

		txtHiscore = new Text();
		txtHiscore.setFill(Color.YELLOW);
		txtHiscore.setFont(ARCADE_FONT);

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

	private void createLivesCounter() {
		tgLivesCounter = new Group();
		int counterTileX = 1, counterTileY = 1;
		tgLivesCounter.setViewOrder(-counterTileY * TS);
		for (int i = 0; i < 5; ++i) {
			V2i lampTile = new V2i(counterTileX + 2 * i, counterTileY);
			Sphere lamp = new Sphere(3);
			lamp.setTranslateX(lampTile.x * TS);
			lamp.setTranslateY(lampTile.y * TS);
			lamp.setTranslateZ(0); // ???
			tgLivesCounter.getChildren().add(lamp);
			wallNodes.remove(lampTile);
		}
	}

	private Node createWallShape(V2i tile, PhongMaterial material) {
		Box block = new Box(TS - 1, TS - 1, TS - 2);
		block.setMaterial(material);
		block.setTranslateX(tile.x * TS);
		block.setTranslateY(tile.y * TS);
		block.setViewOrder(-tile.y * TS);
		block.drawModeProperty().bind(Env.$drawMode);
		return block;
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

	private void createWallMaterials(int n) {
		wallMaterials = new PhongMaterial[n];
		for (int i = 0; i < wallMaterials.length; ++i) {
			PhongMaterial m = new PhongMaterial();
			Image texture = randomSubimage(wallTexture, 128, 128);
			m.setBumpMap(texture);
			m.setDiffuseMap(texture);
			wallMaterials[i] = m;
		}
	}

	private Image randomSubimage(Image src, int w, int h) {
		WritableImage result = new WritableImage(w, h);
		int x = new Random().nextInt((int) src.getWidth() - w);
		int y = new Random().nextInt((int) src.getHeight() - h);
		result.getPixelWriter().setPixels(0, 0, w, h, src.getPixelReader(), x, y);
		return result;
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

	private void updateLivesCounter() {
		if (controller.isAttractMode()) {
			tgLivesCounter.setVisible(false);
			return;
		}
		tgLivesCounter.setVisible(true);
		GameModel game = controller.selectedGame();
		ObservableList<Node> children = tgLivesCounter.getChildren();
		for (int i = 0; i < children.size(); ++i) {
			Sphere ball = (Sphere) children.get(i);
			ball.setMaterial(i < game.lives ? livesCounterOn : livesCounterOff);
		}
	}

	private Group playerShape(Pac player) {
		MeshView body = meshViews.get("Sphere_Sphere.002_Material.001");
		PhongMaterial bodyMaterial = new PhongMaterial(Color.YELLOW);
		bodyMaterial.setDiffuseColor(Color.YELLOW.brighter());
		bodyMaterial.setSpecularPower(100);
		body.setMaterial(bodyMaterial);
		body.setDrawMode(Env.$drawMode.get());
		Translate shift = centerOverOrigin(body);

		MeshView glasses = meshViews.get("Sphere_Sphere.002_Material.002");
		glasses.setMaterial(new PhongMaterial(Color.rgb(50, 50, 50)));
		glasses.setDrawMode(Env.$drawMode.get());
		glasses.getTransforms().add(shift);

		Group shape = new Group(body, glasses);
		shape.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
		scale(shape, TS);
		return shape;
	}

	private Translate centerOverOrigin(Node node) {
		Bounds bounds = node.getBoundsInLocal();
		Translate shift = new Translate(-bounds.getCenterX(), -bounds.getCenterY(), -bounds.getCenterZ());
		node.getTransforms().add(shift);
		return shift;
	}

	private void scale(Node node, double size) {
		Bounds bounds = node.getBoundsInLocal();
		double s1 = size / bounds.getWidth();
		double s2 = size / bounds.getHeight();
		double s3 = size / bounds.getDepth();
		node.getTransforms().add(new Scale(s1, s2, s3));
	}

	private void updatePlayerShape(Pac player) {
		tgPlayer.setVisible(player.visible);
		tgPlayer.setTranslateX(player.position.x);
		tgPlayer.setTranslateY(player.position.y);
		tgPlayer.setViewOrder(-player.position.y - 2);
		tgPlayer.setRotationAxis(Rotate.Z_AXIS);

		// WTF?
//		if (player.dir == Direction.UP) {
//			tgPlayer.setRotate(180);
//		} else if (player.dir == Direction.RIGHT) {
//			tgPlayer.setRotate(-90);
//		} else if (player.dir == Direction.DOWN) {
//			tgPlayer.setRotate(0);
//		} else if (player.dir == Direction.LEFT) {
//			tgPlayer.setRotate(90);
//		}
	}

	private Group createGhostGroup(Ghost ghost) {
		// children: colored ghost, bounty text, ghost returning home
		Group group = new Group(ghostColored(ghost), ghostBounty(ghost), ghostReturningHome(ghost));
		Node selection = group.getChildren().get(0);
		group.setUserData(selection);
		group.getChildren().forEach(child -> child.setVisible(child == selection));
		return group;
	}

	private void updateGhostNode(Ghost ghost) {
		Node oldSelection = (Node) tgGhosts.get(ghost).getUserData();
		ObservableList<Node> children = tgGhosts.get(ghost).getChildren();
		Node newSelection;
		if (ghost.bounty > 0) {
			newSelection = children.get(1);
			((Text) newSelection).setText(ghost.bounty + "");
		} else if (ghost.is(GhostState.DEAD) || ghost.is(GhostState.ENTERING_HOUSE)) {
			newSelection = children.get(2);
			newSelection.setRotationAxis(Rotate.Z_AXIS);
			newSelection.setRotate(ghost.dir == Direction.UP || ghost.dir == Direction.DOWN ? 90 : 0);
		} else {
			newSelection = children.get(0);
			MeshView meshView = (MeshView) newSelection;
			PhongMaterial material = (PhongMaterial) meshView.getMaterial();
			Color color = ghost.is(GhostState.FRIGHTENED) ? Color.CORNFLOWERBLUE : ghostColor(ghost.id);
			material.setDiffuseColor(color);
			material.setSpecularColor(color);
		}
		if (newSelection != oldSelection) {
			tgGhosts.get(ghost).setUserData(newSelection);
			oldSelection.setVisible(false);
			newSelection.setVisible(true);
		}
		tgGhosts.get(ghost).setVisible(ghost.visible);
		tgGhosts.get(ghost).setTranslateX(ghost.position.x);
		tgGhosts.get(ghost).setTranslateY(ghost.position.y);
		tgGhosts.get(ghost).setViewOrder(-(ghost.position.y + 5));
	}

	private MeshView ghostColored(Ghost ghost) {
		PhongMaterial material = new PhongMaterial(ghostColor(ghost.id));
		MeshView shape = new MeshView(ghostMeshPrototype);
		shape.setMaterial(material);
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
		return new Group(parts);
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
		PauseTransition pause = new PauseTransition(Duration.seconds(2));
		pause.setOnFinished(e -> {
			GameModel game = controller.selectedGame();
			game.player.visible = false;
			game.ghosts().forEach(ghost -> ghost.visible = false);
		});

		ScaleTransition animation = new ScaleTransition(Duration.seconds(5), tgMaze);
		animation.setFromZ(1);
		animation.setToZ(0);

		SequentialTransition seq = new SequentialTransition(pause, animation);
		seq.setOnFinished(e -> {
			controller.letCurrentGameStateExpire();
		});
		seq.play();

		// wait until UI signals state expiration
		controller.state.timer.reset();
	}

	private void playLevelStartingAnimation(PacManGameState state) {
		PauseTransition pause = new PauseTransition(Duration.seconds(2));

		ScaleTransition animation = new ScaleTransition(Duration.seconds(5), tgMaze);
		animation.setDelay(Duration.seconds(2));
		animation.setFromZ(0);
		animation.setToZ(1);

		SequentialTransition seq = new SequentialTransition(pause, animation);
		seq.setOnFinished(e -> {
			controller.letCurrentGameStateExpire();
		});
		seq.play();

		// wait until UI signals state expiration
		controller.state.timer.reset();
	}
}