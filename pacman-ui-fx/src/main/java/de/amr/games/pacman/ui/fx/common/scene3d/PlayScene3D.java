package de.amr.games.pacman.ui.fx.common.scene3d;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static java.util.function.Predicate.not;

import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameType;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.ui.animation.TimedSequence;
import de.amr.games.pacman.ui.fx.common.CameraController;
import de.amr.games.pacman.ui.fx.common.Env;
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
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
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

	private final SubScene fxScene;

	private final PerspectiveCamera staticCamera;
	private final PerspectiveCamera moveableCamera;
	private final CameraController cameraController;

	private PacManGameController controller;

	private Group tgAxes;
	private Group tgMaze;
	private Group tgPlayer;
	private Map<Ghost, Group> tgGhosts;
	private List<Node> wallNodes;
	private List<Node> energizerNodes;
	private List<Node> pelletNodes;
	private Group tgScore;
	private Text txtScore;
	private Text txtHiscore;
	private Group tgLivesCounter;

	private final TimedSequence<Boolean> energizerBlinking = TimedSequence.pulse().frameDuration(15);

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
		this.controller = controller;
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
		final GameType gameType = controller.selectedGameType();
		final GameModel game = controller.selectedGame();

		wallNodes = game.level.world.tiles()//
				.filter(game.level.world::isWall)//
				.map(tile -> createWallShape(tile, Assets.randomWallMaterial()))//
				.collect(Collectors.toList());

		energizerNodes = game.level.world.energizerTiles()
				.map(tile -> createEnergizerShape(tile, Assets.foodMaterial(gameType, game.level.mazeNumber)))
				.collect(Collectors.toList());

		pelletNodes = game.level.world.tiles()//
				.filter(game.level.world::isFoodTile)//
				.filter(not(game.level.world::isEnergizerTile))
				.map(tile -> createPelletShape(tile, Assets.foodMaterial(gameType, game.level.mazeNumber)))
				.collect(Collectors.toList());

		tgPlayer = Assets.createPlayerShape();

		tgGhosts = game.ghosts().collect(Collectors.toMap(Function.identity(), this::createGhostShape));

		createScore();
		createLivesCounter();

		tgMaze = new Group();
		// center over origin
		tgMaze.getTransforms()
				.add(new Translate(-GameScene.UNSCALED_SCENE_WIDTH / 2, -GameScene.UNSCALED_SCENE_HEIGHT / 2));

		tgMaze.getChildren().addAll(tgScore, tgLivesCounter);
		tgMaze.getChildren().addAll(wallNodes);
		tgMaze.getChildren().addAll(energizerNodes);
		tgMaze.getChildren().addAll(pelletNodes);
		tgMaze.getChildren().addAll(tgPlayer);
		tgMaze.getChildren().addAll(tgGhosts.values());

		AmbientLight ambientLight = Assets.ambientLight(gameType, game.level.mazeNumber);
		tgMaze.getChildren().add(ambientLight);

		PointLight pointLight = new PointLight(Color.AZURE);
		pointLight.setTranslateZ(-500);
		tgMaze.getChildren().add(pointLight);

		createAxes();

		fxScene.setRoot(new Group(tgAxes, tgMaze));
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
		// TODO remove
		controller.setPlayerImmune(true);
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

	private void updateMoveableCamera() {
		double x = Math.min(10, lerp(moveableCamera.getTranslateX(), tgPlayer.getTranslateX()));
		double y = Math.max(50, lerp(moveableCamera.getTranslateY(), tgPlayer.getTranslateY()));
		moveableCamera.setTranslateX(x);
		moveableCamera.setTranslateY(y);
	}

	private double lerp(double current, double target) {
		return current + (target - current) * 0.02;
	}

	private void createAxes() {
		int len = 100;
		Sphere origin = new Sphere(2);
		origin.setMaterial(new PhongMaterial(Color.BISQUE));
		tgAxes = new Group(origin);
		tgAxes.visibleProperty().bind(Env.$showAxes);

		Cylinder posX = createYAxis(Color.RED.brighter(), len);
		posX.getTransforms().add(new Translate(len / 2, 0, 0));
		posX.getTransforms().add(new Rotate(90, Rotate.Z_AXIS));
		tgAxes.getChildren().add(posX);

		Cylinder posY = createYAxis(Color.GREEN.brighter(), len);
		posY.getTransforms().add(new Translate(0, len / 2, 0));
		tgAxes.getChildren().add(posY);

		Cylinder negZ = createYAxis(Color.BLUE.brighter(), len);
		posY.getTransforms().add(new Translate(0, -len / 2, 0));
		negZ.getTransforms().add(new Rotate(-90, Rotate.X_AXIS));
		tgAxes.getChildren().add(negZ);
	}

	private Cylinder createYAxis(Color color, double height) {
		Cylinder axis = new Cylinder(1, height);
		axis.setMaterial(new PhongMaterial(color));
		return axis;
	}

	private void createScore() {
		Font font = Assets.ARCADE_FONT;

		Text txtScoreTitle = new Text("SCORE");
		txtScoreTitle.setFill(Color.WHITE);
		txtScoreTitle.setFont(font);

		txtScore = new Text();
		txtScore.setFill(Color.YELLOW);
		txtScore.setFont(font);

		Text txtHiscoreTitle = new Text("HI SCORE");
		txtHiscoreTitle.setFill(Color.WHITE);
		txtHiscoreTitle.setFont(font);

		txtHiscore = new Text();
		txtHiscore.setFill(Color.YELLOW);
		txtHiscore.setFont(font);

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
			V2i tile = new V2i(counterTileX + 2 * i, counterTileY);
			Group liveIndicator = Assets.createPlayerShape();
			liveIndicator.setTranslateX(tile.x * TS);
			liveIndicator.setTranslateY(tile.y * TS);
			liveIndicator.setTranslateZ(4); // ???
			liveIndicator.setUserData(tile);
			tgLivesCounter.getChildren().add(liveIndicator);
		}
	}

	private Node createWallShape(V2i tile, PhongMaterial material) {
		Box block = new Box(TS - 1, TS - 1, TS - 2);
		block.setMaterial(material);
		block.setTranslateX(tile.x * TS);
		block.setTranslateY(tile.y * TS);
		block.setViewOrder(-tile.y * TS);
		block.setUserData(tile);
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
		// TODO this is inefficient
		pelletNodes.forEach(pellet -> {
			V2i tile = (V2i) pellet.getUserData();
			pellet.setVisible(!game.level.isFoodRemoved(tile));
		});
		updatePlayerShape(game.player);
		for (Ghost ghost : game.ghosts) {
			updateGhostShape(ghost);
		}
		if (getActiveCamera() == moveableCamera) {
			updateMoveableCamera();
		}
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
			Group liveIndicator = (Group) children.get(i);
			V2i tile = (V2i) liveIndicator.getUserData();
			V2i tileBelowIndicator = tile.plus(0, 1);
			if (i < game.lives) {
				liveIndicator.setVisible(true);
				wallAt(tile).setVisible(false);
				wallAt(tileBelowIndicator).setVisible(false);
			} else {
				liveIndicator.setVisible(false);
				wallAt(tile).setVisible(true);
				wallAt(tileBelowIndicator).setVisible(true);
			}
		}
	}

	private Node wallAt(V2i tile) {
		return wallNodes.stream().filter(wall -> tile.equals(wall.getUserData())).findFirst().get();
	}

	private void updatePlayerShape(Pac player) {
		tgPlayer.setVisible(player.visible);
		tgPlayer.setTranslateX(player.position.x);
		tgPlayer.setTranslateY(player.position.y);
		tgPlayer.setViewOrder(-player.position.y - 2);
	}

	private Group createGhostShape(Ghost ghost) {
		Group ghostColoredShape = new Group(Assets.createGhostMeshView(ghost.id));
		ghostColoredShape.getTransforms().add(new Rotate(90, Rotate.X_AXIS));
		Group group = new Group(ghostColoredShape, createGhostBountyShape(ghost), createGhostReturningHomeShape(ghost));
		Node selection = group.getChildren().get(0);
		group.setUserData(selection);
		group.getChildren().forEach(child -> child.setVisible(child == selection));
		return group;
	}

	private void updateGhostShape(Ghost ghost) {
		Group oldSelection = (Group) tgGhosts.get(ghost).getUserData();
		ObservableList<Node> children = tgGhosts.get(ghost).getChildren();
		Group newSelection;
		if (ghost.bounty > 0) {
			newSelection = (Group) children.get(1);
			Text text = (Text) newSelection.getChildren().get(0);
			text.setText(ghost.bounty + "");
		} else if (ghost.is(GhostState.DEAD) || ghost.is(GhostState.ENTERING_HOUSE)) {
			newSelection = (Group) children.get(2);
			newSelection.setRotationAxis(Rotate.Z_AXIS);
			newSelection.setRotate(ghost.dir == Direction.UP || ghost.dir == Direction.DOWN ? 90 : 0);
		} else {
			newSelection = (Group) children.get(0);
			MeshView meshView = (MeshView) newSelection.getChildren().get(0);
			PhongMaterial material = (PhongMaterial) meshView.getMaterial();
			Color color = ghost.is(GhostState.FRIGHTENED) ? Color.CORNFLOWERBLUE : Assets.ghostColor(ghost.id);
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

	private Group createGhostBountyShape(Ghost ghost) {
		// TODO why is this text so blurred?
		Text bounty = new Text();
		bounty.setText(String.valueOf(ghost.bounty));
		bounty.setFont(Font.font("Sans", FontWeight.MEDIUM, 8));
		bounty.setFill(Color.CYAN);
		bounty.setRotationAxis(Rotate.X_AXIS);
		bounty.setRotate(staticCamera.getRotate());
		bounty.setTranslateZ(-1.5 * TS);
		return new Group(bounty);
	}

	private Group createGhostReturningHomeShape(Ghost ghost) {
		PhongMaterial skin = Assets.ghostSkin(ghost.id);
		Sphere[] parts = new Sphere[3];
		for (int i = 0; i < parts.length; ++i) {
			parts[i] = new Sphere(1);
			parts[i].setMaterial(skin);
			parts[i].setTranslateX(i * 3);
		}
		return new Group(parts);
	}

	// State change handlers

	@Override
	public void onGameStateChange(PacManGameState oldState, PacManGameState newState) {
		if (oldState == PacManGameState.HUNTING) {
			energizerBlinking.reset();
		}
		if (newState == PacManGameState.HUNTING) {
			energizerBlinking.restart();
		}
		if (newState == PacManGameState.LEVEL_COMPLETE) {
			playLevelCompleteAnimation(oldState);
		}
		if (newState == PacManGameState.LEVEL_STARTING) {
			playLevelStartingAnimation(newState);
		}
	}

	private void playLevelCompleteAnimation(PacManGameState state) {
		log("%s: play level complete animation", this);
		controller.state.timer.reset();
		String[] congrats = { "Well done", "Congrats", "You did it", "You're the man*in", "WTF", "Man, man, man" };
		String randomCongrats = congrats[new Random().nextInt(congrats.length)];
		controller.userInterface.showFlashMessage(
				String.format("%s!\n\nLevel %d complete.", randomCongrats, controller.selectedGame().levelNumber), 3);
		PauseTransition pause = new PauseTransition(Duration.seconds(2));
		pause.setOnFinished(e -> {
			GameModel game = controller.selectedGame();
			game.player.visible = false;
			game.ghosts().forEach(ghost -> ghost.visible = false);
		});

		ScaleTransition animation = new ScaleTransition(Duration.seconds(3), tgMaze);
		animation.setFromZ(1);
		animation.setToZ(0);

		SequentialTransition seq = new SequentialTransition(pause, animation);
		seq.setOnFinished(e -> {
			controller.letCurrentGameStateExpire();
		});
		seq.play();
	}

	private void playLevelStartingAnimation(PacManGameState state) {
		log("%s: play level starting animation", this);
		controller.state.timer.reset();
		controller.userInterface.showFlashMessage("Entering Level " + controller.selectedGame().levelNumber);
		ScaleTransition animation = new ScaleTransition(Duration.seconds(3), tgMaze);
		animation.setDelay(Duration.seconds(2));
		animation.setFromZ(0);
		animation.setToZ(1);
		animation.setOnFinished(e -> {
			controller.letCurrentGameStateExpire();
		});
		animation.play();
	}
}