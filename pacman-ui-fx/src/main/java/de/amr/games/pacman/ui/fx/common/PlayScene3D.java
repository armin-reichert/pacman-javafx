package de.amr.games.pacman.ui.fx.common;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.world.PacManGameWorld.HTS;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static de.amr.games.pacman.ui.fx.mspacman.MsPacMan_Constants.getMazeWallColor;

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
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Shape3D;
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

	private static final int wallHeight = TS - 2;

	private static Color ghostColor(int i) {
		return i == 0 ? Color.RED : i == 1 ? Color.PINK : i == 2 ? Color.CYAN : Color.ORANGE;
	}

	private double scaling;

	private final PacManGameController controller;
	private final SubScene subScene;
	private final PerspectiveCamera camera;

	private final TimedSequence<Boolean> energizerBlinking = TimedSequence.pulse().frameDuration(15);
	private TimedSequence<?> playerMunchingAnimation;
	private TimedSequence<?> playerDyingAnimation;
	private final Map<Ghost, TimedSequence<?>> ghostReturningHomeAnimationByGhost = new HashMap<>();
	private final Map<Ghost, TimedSequence<?>> ghostFlashingAnimationByGhost = new HashMap<>();
	private final Map<Ghost, TimedSequence<?>> ghostFrightenedAnimationByGhost = new HashMap<>();
	private final Map<Ghost, TimedSequence<?>> ghostKickingAnimationByGhost = new HashMap<>();

	private final Group root = new Group();
	private final Group tgMaze = new Group();
	private final Group tgFood = new Group();
	private final Group tgPlayer = new Group();
	private final Group[] tgGhosts = new Group[4];
	private final Map<V2i, Node> wallNodes = new HashMap<>();
	private final List<Node> energizerNodes = new ArrayList<>();
	private final List<Node> pelletNodes = new ArrayList<>();
	private final Text txtScore = new Text();
	private final Text txtHiscore = new Text();
	private final Font scoreFont = Font.loadFont(getClass().getResource("/emulogic.ttf").toExternalForm(), TS);

	public PlayScene3D(PacManGameController controller, double height) {
		this.controller = controller;
		double width = GameScene.ASPECT_RATIO * height;
		scaling = width / GameScene.WIDTH_UNSCALED;

		camera = new PerspectiveCamera();
		camera.setTranslateZ(-240);
		camera.setRotationAxis(Rotate.X_AXIS);
		camera.setRotate(30);

		root.getTransforms().add(new Scale(scaling, scaling, scaling));
		subScene = new SubScene(root, width, height);
		subScene.setFill(Color.BLACK);
		subScene.setCamera(camera);

		controller.fsm.addStateEntryListener(PacManGameState.HUNTING, this::onHuntingStateEntry);
		controller.fsm.addStateExitListener(PacManGameState.HUNTING, this::onHuntingStateExit);
		controller.fsm.addStateEntryListener(PacManGameState.LEVEL_COMPLETE, state -> playLevelCompleteAnimation());
		controller.fsm.addStateEntryListener(PacManGameState.LEVEL_STARTING, state -> playLevelStartingAnimation());
	}

	@Override
	public void resize(double width, double height) {
		scaling = width / GameScene.WIDTH_UNSCALED;
		root.getTransforms().clear();
		root.getTransforms().add(new Scale(scaling, scaling, scaling));
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
	public void start() {

		GameModel game = controller.game;
		PacManGameWorld world = game.level.world;

		wallNodes.clear();
		world.tiles().forEach(tile -> {
			if (world.isWall(tile)) {
				Box wallShape = new Box(TS - 2, TS - 2, wallHeight);
				wallShape.setTranslateX(tile.x * TS);
				wallShape.setTranslateY(tile.y * TS);
				PhongMaterial material = new PhongMaterial(
						controller.isPlaying(GameType.PACMAN) ? Color.BLUE : getMazeWallColor(game.level.mazeNumber));
				wallShape.setMaterial(material);
				wallShape.setViewOrder(-tile.y * TS);
				wallNodes.put(tile, wallShape);
			}
		});

		Color foodColor = controller.isPlaying(GameType.PACMAN) ? Color.rgb(250, 185, 176)
				: MsPacMan_Constants.getMazeFoodColor(game.level.mazeNumber);
		energizerNodes.clear();
		world.energizerTiles().forEach(tile -> {
			Sphere energizer = new Sphere(HTS);
			energizer.setMaterial(new PhongMaterial(foodColor));
			energizer.setUserData(tile);
			energizer.setTranslateX(tile.x * TS);
			energizer.setTranslateY(tile.y * TS);
			energizer.setViewOrder(-tile.y * TS);
			energizerNodes.add(energizer);
			tgFood.getChildren().add(energizer);
		});

		pelletNodes.clear();
		world.tiles().filter(world::isFoodTile).filter(tile -> !world.isEnergizerTile(tile)).forEach(tile -> {
			Sphere pellet = new Sphere(1.5);
			pellet.setMaterial(new PhongMaterial(foodColor));
			pellet.setUserData(tile);
			pellet.setTranslateX(tile.x * TS);
			pellet.setTranslateY(tile.y * TS);
			pellet.setViewOrder(-tile.y * TS);
			pelletNodes.add(pellet);
			tgFood.getChildren().add(pellet);
		});

		for (int id = 0; id < 4; ++id) {
			tgGhosts[id] = new Group();
		}

		tgMaze.getChildren().addAll(wallNodes.values());
		tgMaze.getChildren().add(tgFood);
		tgMaze.getChildren().add(tgPlayer);
		tgMaze.getChildren().addAll(tgGhosts);

		root.getChildren().clear();
		root.getChildren().addAll(tgMaze, txtScore, txtHiscore);
	}

	@Override
	public void update() {
		GameModel game = controller.game;

		wallNodes.values().stream().map(wall -> (Shape3D) wall)
				.forEach(wall -> wall.setDrawMode(GlobalSettings.drawWallsAsLines ? DrawMode.LINE : DrawMode.FILL));

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

		txtScore.setFill(Color.YELLOW);
		txtScore.setFont(scoreFont);
		txtScore.setText(String.format("SCORE\n%08dL%03d", game.score, game.levelNumber));
		txtScore.setTranslateX(TS);
		txtScore.setTranslateY(-2 * TS);
		txtScore.setTranslateZ(-2 * TS);
		txtScore.setRotationAxis(Rotate.X_AXIS);
		txtScore.setRotate(camera.getRotate());
		txtScore.setViewOrder(-1000);

		txtHiscore.setFill(Color.YELLOW);
		txtHiscore.setFont(scoreFont);
		txtHiscore.setText(String.format("HI SCORE\n%08dL%03d", game.highscorePoints, game.highscoreLevel));
		txtHiscore.setTranslateX(14 * TS);
		txtHiscore.setTranslateY(-2 * TS);
		txtHiscore.setTranslateZ(-2 * TS);
		txtHiscore.setRotationAxis(Rotate.X_AXIS);
		txtHiscore.setRotate(camera.getRotate());
		txtHiscore.setViewOrder(-1000);
	}

	private void onHuntingStateEntry(PacManGameState state) {
		energizerBlinking.restart();
	}

	private void onHuntingStateExit(PacManGameState state) {
		energizerBlinking.reset();
	}

	private void updatePlayerShape(Pac player) {
		Node shape = player.dead ? (Node) playerDyingAnimation.frame() : (Node) playerMunching(player, player.dir).frame();
		boolean insidePortal = controller.game.level.world.isPortal(player.tile());
		tgPlayer.getChildren().clear();
		tgPlayer.getChildren().add(shape);
		tgPlayer.setVisible(player.visible && !insidePortal);
		tgPlayer.setTranslateX(player.position.x);
		tgPlayer.setTranslateY(player.position.y);
		tgPlayer.setViewOrder(-player.position.y);
	}

	private void updateGhostShape(int id) {
		Ghost ghost = controller.game.ghosts[id];
		Node shape;

		if (ghost.visible && ghost.bounty > 0) {
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
			shape = bountyText;
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

		else if (ghost.is(GhostState.LOCKED) && controller.game.player.powerTimer.isRunning()) {
			shape = (Node) ghostFrightened(ghost, ghost.dir).animate();
		}

		else {
			// default: show ghost in color, alive and kicking
			shape = (Node) ghostKicking(ghost, ghost.wishDir).animate(); // Looks towards wish dir!
		}

		tgGhosts[id].setVisible(ghost.visible);
		tgGhosts[id].setTranslateX(ghost.position.x);
		tgGhosts[id].setTranslateY(ghost.position.y);
		tgGhosts[id].setViewOrder(-ghost.position.y);
		tgGhosts[id].getChildren().clear();
		tgGhosts[id].getChildren().add(shape);
	}

	@Override
	public void end() {
	}

	@Override
	public Camera getCamera() {
		return camera;
	}

	// State change handling

	private void playLevelCompleteAnimation() {
		tgFood.setVisible(false);
		tgPlayer.setVisible(false);
		Arrays.asList(tgGhosts).forEach(ghostShape -> ghostShape.setVisible(false));
		ScaleTransition levelCompleteAnimation = new ScaleTransition(Duration.seconds(5), tgMaze);
		levelCompleteAnimation.setFromZ(1);
		levelCompleteAnimation.setToZ(0);
		levelCompleteAnimation.setDelay(Duration.seconds(2));
		controller.fsm.state.timer.resetSeconds(2 + levelCompleteAnimation.getDuration().toSeconds());
		levelCompleteAnimation.play();
	}

	private void playLevelStartingAnimation() {
		// TODO these should become visible after the animation
		tgPlayer.setVisible(true);
		Arrays.asList(tgGhosts).forEach(ghostShape -> ghostShape.setVisible(true));
		tgFood.setVisible(true);
		ScaleTransition levelStartAnimation = new ScaleTransition(Duration.seconds(5), tgMaze);
		levelStartAnimation.setFromZ(0);
		levelStartAnimation.setToZ(1);
		controller.fsm.state.timer.resetSeconds(levelStartAnimation.getDuration().toSeconds());
		levelStartAnimation.play();
	}

	// Animations

	@Override
	public void initCamera() {
		log("Initialize camera for PlayScene3D");
		camera.setTranslateX(0);
		// TODO how to do that right?
		camera.setTranslateY(subScene.getHeight() * 1.5);
		camera.setTranslateZ(-subScene.getHeight() * 1.5);
		camera.setRotate(36);
	}

	public TimedSequence<?> ghostFlashing(Ghost ghost) {
		if (!ghostFlashingAnimationByGhost.containsKey(ghost)) {
			Sphere s1 = new Sphere(HTS);
			s1.setMaterial(new PhongMaterial(Color.BLUE));
			Sphere s2 = new Sphere(HTS);
			s2.setMaterial(new PhongMaterial(Color.WHITE));
			ghostFlashingAnimationByGhost.put(ghost, TimedSequence.of(s1, s2).frameDuration(10).endless());
		}
		return ghostFlashingAnimationByGhost.get(ghost);
	}

	public TimedSequence<?> ghostFrightened(Ghost ghost, Direction dir) {
		if (!ghostFrightenedAnimationByGhost.containsKey(ghost)) {
			Sphere s = new Sphere(HTS);
			s.setMaterial(new PhongMaterial(Color.BLUE));
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
			Cylinder s = new Cylinder(2, TS);
			s.setMaterial(new PhongMaterial(ghostColor(ghost.id)));
			s.setUserData(ghost);
			ghostReturningHomeAnimationByGhost.put(ghost, TimedSequence.of(s));
		}
		return ghostReturningHomeAnimationByGhost.get(ghost);
	}

	public TimedSequence<?> playerMunching(Pac player, Direction dir) {
		if (playerMunchingAnimation == null) {
			Box box = new Box(TS, TS, TS);
			box.setMaterial(new PhongMaterial(Color.YELLOW));
			box.setUserData(player);
			playerMunchingAnimation = TimedSequence.of(box);
		}
		return playerMunchingAnimation;
	}
}