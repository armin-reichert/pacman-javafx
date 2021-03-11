package de.amr.games.pacman.ui.fx.common;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.world.PacManGameWorld.HTS;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static de.amr.games.pacman.ui.fx.mspacman.MsPacMan_Constants.getMazeWallColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Stream;

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
import de.amr.games.pacman.ui.animation.Animation;
import de.amr.games.pacman.ui.animation.GhostAnimations;
import de.amr.games.pacman.ui.animation.MazeAnimations;
import de.amr.games.pacman.ui.animation.PacManGameAnimations;
import de.amr.games.pacman.ui.animation.PlayerAnimations;
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
public class PlayScene3D implements GameScene, PacManGameAnimations, GhostAnimations, MazeAnimations, PlayerAnimations {

	private static final int wallHeight = TS - 2;

	private static Color ghostColor(int i) {
		return i == 0 ? Color.RED : i == 1 ? Color.PINK : i == 2 ? Color.CYAN : Color.ORANGE;
	}

	private final PacManGameController controller;
	private final Group root;
	private final SubScene subScene;
	private final PerspectiveCamera camera;
	private final Animation<?> defaultAnimation;

	private double scaling;

	private Group maze;
	private Group food;
	private ScaleTransition levelChangeAnimation;
	private Node playerShape;
	private List<Node> ghostShapes = new ArrayList<>();
	private Map<V2i, Node> walls = new HashMap<>();
	private List<Node> energizers = new ArrayList<>();
	private List<Node> pellets = new ArrayList<>();
	private Text scoreDisplay = new Text();
	private Text hiscoreDisplay = new Text();

	private Font scoreFont;

	public PlayScene3D(PacManGameController controller, double height) {
		this.controller = controller;
		double width = GameScene.ASPECT_RATIO * height;
		scaling = width / GameScene.WIDTH_UNSCALED;

		scoreFont = Font.loadFont(getClass().getResource("/emulogic.ttf").toExternalForm(), TS);

		root = new Group();
		root.getTransforms().add(new Scale(scaling, scaling, scaling));

		camera = new PerspectiveCamera();
		camera.setTranslateZ(-240);
		camera.setRotationAxis(Rotate.X_AXIS);
		camera.setRotate(30);

		subScene = new SubScene(root, width, height);
		subScene.setFill(Color.BLACK);
		subScene.setCamera(camera);

		// default for not yet implemented animations
		Text text = new Text();
		text.setText("MISSING ANIMATION");
		text.setFill(Color.RED);
		text.setFont(Font.font("Sans", FontWeight.BOLD, 6));
		text.setRotationAxis(Rotate.X_AXIS);
		text.setRotate(camera.getRotate());
		defaultAnimation = Animation.of(text);

		controller.fsm.addStateEntryListener(PacManGameState.CHANGING_LEVEL, this::onLeavingLevel);
		controller.fsm.addStateExitListener(PacManGameState.CHANGING_LEVEL, this::onEnteringLevel);
	}

	@Override
	public Optional<PacManGameAnimations> animations() {
		return Optional.of(this);
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
		root.getChildren().clear();

		maze = new Group();
		walls.clear();
		world.tiles().forEach(tile -> {
			if (world.isWall(tile)) {
				Box wall = new Box(TS - 2, TS - 2, wallHeight);
				wall.setTranslateX(tile.x * TS);
				wall.setTranslateY(tile.y * TS);
				PhongMaterial material = new PhongMaterial(
						controller.isPlaying(GameType.PACMAN) ? Color.BLUE : getMazeWallColor(game.level.mazeNumber));
				wall.setMaterial(material);
				wall.setViewOrder(-tile.y * TS);
				walls.put(tile, wall);
				maze.getChildren().add(wall);
			}
		});

		food = new Group();
		Color foodColor = controller.isPlaying(GameType.PACMAN) ? Color.rgb(250, 185, 176)
				: MsPacMan_Constants.getMazeFoodColor(game.level.mazeNumber);
		energizers.clear();
		world.energizerTiles().forEach(tile -> {
			Sphere energizer = new Sphere(HTS);
			energizer.setMaterial(new PhongMaterial(foodColor));
			energizer.setUserData(tile);
			energizer.setTranslateX(tile.x * TS);
			energizer.setTranslateY(tile.y * TS);
			energizer.setViewOrder(-tile.y * TS);
			energizers.add(energizer);
			food.getChildren().add(energizer);
		});

		pellets.clear();
		world.tiles().filter(world::isFoodTile).filter(tile -> !world.isEnergizerTile(tile)).forEach(tile -> {
			Sphere pellet = new Sphere(1.5);
			pellet.setMaterial(new PhongMaterial(foodColor));
			pellet.setUserData(tile);
			pellet.setTranslateX(tile.x * TS);
			pellet.setTranslateY(tile.y * TS);
			pellet.setTranslateZ(-HTS);
			pellet.setViewOrder(-tile.y * TS);
			pellets.add(pellet);
			food.getChildren().add(pellet);
		});

		maze.getChildren().addAll(food);

		playerShape = (Node) playerMunching(game.player, game.player.dir).frame();
		playerShape.setViewOrder(-game.player.position.y);
		maze.getChildren().add(playerShape);

		ghostShapes.clear();
		for (Ghost ghost : game.ghosts) {
			Node ghostShape = ghostShape(ghost, game.player.powerTimer.isRunning());
			ghostShapes.add(ghostShape);
			ghostShape.setViewOrder(-ghost.position.y);
			maze.getChildren().add(ghostShape);
		}

		scoreDisplay.setViewOrder(-1000);
		hiscoreDisplay.setViewOrder(-1000);

		root.getChildren().clear();
		root.getChildren().add(maze);
		root.getChildren().addAll(scoreDisplay, hiscoreDisplay);
	}

	@Override
	public void update() {
		GameModel game = controller.game;

		walls.values().stream().map(wall -> (Shape3D) wall)
				.forEach(wall -> wall.setDrawMode(GlobalSettings.drawWallsAsLines ? DrawMode.LINE : DrawMode.FILL));

		energizers.forEach(energizer -> {
			V2i tile = (V2i) energizer.getUserData();
			energizer.setVisible(!game.level.isFoodRemoved(tile) && energizerBlinking.frame());
		});

		pellets.forEach(pellet -> {
			V2i tile = (V2i) pellet.getUserData();
			pellet.setVisible(!game.level.isFoodRemoved(tile));
		});

		maze.getChildren().remove(playerShape);
		playerShape = playerShape(game.player);
		playerShape.setViewOrder(-game.player.position.y);
		maze.getChildren().add(playerShape);

		maze.getChildren().removeAll(ghostShapes);
		for (Ghost ghost : game.ghosts) {
			Node ghostShape = ghostShape(ghost, game.player.powerTimer.isRunning());
			ghostShape.setViewOrder(-ghost.position.y);
			ghostShapes.set(ghost.id, ghostShape);
			maze.getChildren().add(ghostShape);
		}

		scoreDisplay.setFill(Color.YELLOW);
		scoreDisplay.setFont(scoreFont);
		scoreDisplay.setText(String.format("SCORE\n%08dL%03d", game.score, game.levelNumber));
		scoreDisplay.setTranslateX(TS);
		scoreDisplay.setTranslateY(-2 * TS);
		scoreDisplay.setTranslateZ(-2 * TS);
		scoreDisplay.setRotationAxis(Rotate.X_AXIS);
		scoreDisplay.setRotate(camera.getRotate());

		hiscoreDisplay.setFill(Color.YELLOW);
		hiscoreDisplay.setFont(scoreFont);
		hiscoreDisplay.setText(String.format("HI SCORE\n%08dL%03d", game.highscorePoints, game.highscoreLevel));
		hiscoreDisplay.setTranslateX(14 * TS);
		hiscoreDisplay.setTranslateY(-2 * TS);
		hiscoreDisplay.setTranslateZ(-2 * TS);
		hiscoreDisplay.setRotationAxis(Rotate.X_AXIS);
		hiscoreDisplay.setRotate(camera.getRotate());
	}

	@Override
	public void end() {
	}

	@Override
	public Camera getCamera() {
		return camera;
	}

	// State change handling

	private void onLeavingLevel(PacManGameState state) {
		food.setVisible(false);
		levelChangeAnimation = new ScaleTransition(Duration.seconds(3), maze);
		levelChangeAnimation.setFromZ(1);
		levelChangeAnimation.setToZ(0);
		levelChangeAnimation.setDelay(Duration.seconds(2));
		levelChangeAnimation.play();
	}

	private void onEnteringLevel(PacManGameState state) {
		food.setVisible(true);
		levelChangeAnimation = new ScaleTransition(Duration.seconds(3), maze);
		levelChangeAnimation.setFromZ(0);
		levelChangeAnimation.setToZ(1);
		levelChangeAnimation.play();
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

	@Override
	public Animation<?> flapFlapping() {
		return defaultAnimation;
	}

	@Override
	public Animation<?> storkFlying() {
		return defaultAnimation;
	}

	@Override
	public GhostAnimations ghostAnimations() {
		return this;
	}

	@Override
	public MazeAnimations mazeAnimations() {
		return this;
	}

	@Override
	public PlayerAnimations playerAnimations() {
		return this;
	}

	private Animation<Boolean> energizerBlinking = Animation.pulse().frameDuration(15).run();

	@Override
	public Animation<Boolean> energizerBlinking() {
		return energizerBlinking;
	}

	private Node ghostShape(Ghost ghost, boolean frightened) {
		if (ghost.visible && ghost.bounty > 0) {
			DropShadow shadow = new DropShadow(0.3, Color.color(0.4, 0.4, 0.4));
			Text bountyText = new Text();
			bountyText.setEffect(shadow);
			bountyText.setCache(true);
			bountyText.setText(String.valueOf(ghost.bounty));
			bountyText.setFont(Font.font("Sans", FontWeight.BOLD, TS));
			bountyText.setFill(Color.CYAN);
			bountyText.setTranslateX(ghost.position.x);
			bountyText.setTranslateY(ghost.position.y);
			bountyText.setTranslateZ(-1.5 * TS);
			bountyText.setRotationAxis(Rotate.X_AXIS);
			bountyText.setRotate(camera.getRotate());
			return bountyText;
		}

		Node shape;
		if (ghost.is(GhostState.DEAD) || ghost.is(GhostState.ENTERING_HOUSE)) {
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

		else if (ghost.is(GhostState.LOCKED) && frightened) {
			shape = (Node) ghostFrightened(ghost, ghost.dir).animate();
		}

		else {
			// default: show ghost in color, alive and kicking
			shape = (Node) ghostKicking(ghost, ghost.wishDir).animate(); // Looks towards wish dir!
		}

		shape.setVisible(ghost.visible);
		shape.setTranslateX(ghost.position.x);
		shape.setTranslateY(ghost.position.y);
		return shape;
	}

	private Map<Ghost, Animation<?>> ghostFlashingAnimationByGhost = new HashMap<>();

	@Override
	public Animation<?> ghostFlashing(Ghost ghost) {
		if (!ghostFlashingAnimationByGhost.containsKey(ghost)) {
			Sphere s1 = new Sphere(HTS);
			s1.setMaterial(new PhongMaterial(Color.BLUE));
			Sphere s2 = new Sphere(HTS);
			s2.setMaterial(new PhongMaterial(Color.WHITE));
			ghostFlashingAnimationByGhost.put(ghost, Animation.of(s1, s2).frameDuration(10).endless());
		}
		return ghostFlashingAnimationByGhost.get(ghost);
	}

	private Map<Ghost, Animation<?>> ghostFrightenedAnimationByGhost = new HashMap<>();

	@Override
	public Animation<?> ghostFrightened(Ghost ghost, Direction dir) {
		if (!ghostFrightenedAnimationByGhost.containsKey(ghost)) {
			Sphere s = new Sphere(HTS);
			s.setMaterial(new PhongMaterial(Color.BLUE));
			s.setUserData(ghost);
			ghostFrightenedAnimationByGhost.put(ghost, Animation.of(s));
		}
		return ghostFrightenedAnimationByGhost.get(ghost);
	}

	private Map<Ghost, Animation<?>> ghostKickingAnimationByGhost = new HashMap<>();

	@Override
	public Animation<?> ghostKicking(Ghost ghost, Direction dir) {
		if (!ghostKickingAnimationByGhost.containsKey(ghost)) {
			Sphere s = new Sphere(HTS);
			s.setMaterial(new PhongMaterial(ghostColor(ghost.id)));
			s.setUserData(ghost);
			ghostKickingAnimationByGhost.put(ghost, Animation.of(s));
		}
		return ghostKickingAnimationByGhost.get(ghost);
	}

	private Map<Ghost, Animation<?>> ghostReturningHomeAnimationByGhost = new HashMap<>();

	@Override
	public Animation<?> ghostReturningHome(Ghost ghost, Direction dir) {
		if (!ghostReturningHomeAnimationByGhost.containsKey(ghost)) {
			Cylinder s = new Cylinder(2, TS);
			s.setMaterial(new PhongMaterial(ghostColor(ghost.id)));
			s.setUserData(ghost);
			ghostReturningHomeAnimationByGhost.put(ghost, Animation.of(s));
		}
		return ghostReturningHomeAnimationByGhost.get(ghost);
	}

	@Override
	public Animation<?> mazeFlashing(int mazeNumber) {
		// TODO implement this method
		return defaultAnimation;
	}

	@Override
	public Stream<Animation<?>> mazeFlashings() {
		// TODO implement this method
		return Stream.empty();
	}

	private Node playerShape(Pac player) {
		Node shape = null;
		if (player.dead) {
			shape = (Node) playerDying().frame();
		} else {
			shape = (Node) playerMunching(player, player.dir).frame();
		}
		shape.setVisible(player.visible);
		shape.setTranslateX(player.position.x);
		shape.setTranslateY(player.position.y);
		return shape;
	}

	@Override
	public Animation<?> playerDying() {
		// TODO implement this method
		return defaultAnimation;
	}

	private Animation<?> playerMunchingAnimation;

	@Override
	public Animation<?> playerMunching(Pac player, Direction dir) {
		if (playerMunchingAnimation == null) {
			Box box = new Box(TS, TS, TS);
			box.setMaterial(new PhongMaterial(Color.YELLOW));
			box.setUserData(player);
			playerMunchingAnimation = Animation.of(box);
		}
		return playerMunchingAnimation;
	}

	@Override
	public Animation<?> spouseMunching(Pac spouse, Direction dir) {
		// used in intermission scenes where both, Pac-Man and Ms. Pac-Man, appear
		return defaultAnimation;
	}
}