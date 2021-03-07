package de.amr.games.pacman.ui.fx.common;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.world.PacManGameWorld.HTS;
import static de.amr.games.pacman.world.PacManGameWorld.TS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameType;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.ui.animation.GhostAnimations;
import de.amr.games.pacman.ui.animation.MazeAnimations;
import de.amr.games.pacman.ui.animation.PacManGameAnimations;
import de.amr.games.pacman.ui.animation.PlayerAnimations;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_Constants;
import de.amr.games.pacman.world.PacManGameWorld;
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
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;

public class PlayScene3D
		implements GameScene3D, PacManGameAnimations, GhostAnimations, MazeAnimations, PlayerAnimations {

	private final PacManGameController controller;
	private final Group root;
	private final SubScene subScene;
	private final PerspectiveCamera camera;
	private Scale scale;

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
		double s = width / GameScene.WIDTH_UNSCALED;
		scale = new Scale(s, s, s);

		scoreFont = Font.loadFont(getClass().getResource("/emulogic.ttf").toExternalForm(), TS);

		root = new Group();
		root.getTransforms().add(scale);

		camera = new PerspectiveCamera();
		camera.setTranslateZ(-240);
		camera.setRotationAxis(Rotate.X_AXIS);
		camera.setRotate(30);

		subScene = new SubScene(root, width, height);
		subScene.setFill(Color.BLACK);
		subScene.setCamera(camera);
	}

	@Override
	public void resize(double width, double height) {
		double s = width / GameScene.WIDTH_UNSCALED;
		scale = new Scale(s, s, s);
		root.getTransforms().clear();
		root.getTransforms().add(scale);
		subScene.setWidth(width);
		subScene.setHeight(height);
	}

	@Override
	public SubScene getSubScene() {
		return subScene;
	}

	@Override
	public void start() {

		GameModel game = controller.getGame();
		PacManGameWorld world = game.level.world;

		walls.clear();
		world.tiles().forEach(tile -> {
			if (world.isWall(tile)) {
				Box wall = new Box(TS - 2, TS - 2, TS - 2);
				if (controller.isPlaying(GameType.PACMAN)) {
					wall.setMaterial(new PhongMaterial(Color.BLUE));
				} else {
					wall.setMaterial(new PhongMaterial(MsPacMan_Constants.getMazeWallColor(game.level.mazeNumber - 1)));
				}
//				wall.setDrawMode(DrawMode.LINE);
				wall.setTranslateX(tile.x * TS);
				wall.setTranslateY(tile.y * TS);
				wall.setUserData(tile);
				walls.put(tile, wall);
			}
		});

		energizers.clear();
		world.tiles().filter(world::isEnergizerTile).forEach(tile -> {
			Sphere ball = new Sphere(HTS);
			ball.setMaterial(new PhongMaterial(Color.YELLOW));
			ball.setUserData(tile);
			ball.setTranslateX(tile.x * TS);
			ball.setTranslateY(tile.y * TS);
			ball.setTranslateZ(-HTS / 2);
			energizers.add(ball);
		});

		pellets.clear();
		world.tiles().filter(world::isFoodTile).filter(tile -> !world.isEnergizerTile(tile)).forEach(tile -> {
			Sphere ball = new Sphere(1.5);
			ball.setMaterial(new PhongMaterial(Color.YELLOW));
			ball.setUserData(tile);
			ball.setTranslateX(tile.x * TS);
			ball.setTranslateY(tile.y * TS);
			ball.setTranslateZ(-1.5);
			pellets.add(ball);
		});

		playerShape = (Node) playerMunching(game.pac, game.pac.dir).frame();

		ghostShapes.clear();
		for (Ghost ghost : game.ghosts) {
			ghostShapes.add(ghostShape(ghost, game.pac.powerTicksLeft > 0));
		}

		root.getChildren().clear();
		root.getChildren().addAll(walls.values());
		root.getChildren().addAll(ghostShapes);
		root.getChildren().addAll(energizers);
		root.getChildren().addAll(pellets);
		root.getChildren().add(playerShape);
		root.getChildren().addAll(scoreDisplay, hiscoreDisplay);
	}

	private Color ghostColor(int i) {
		return i == 0 ? Color.RED : i == 1 ? Color.PINK : i == 2 ? Color.CYAN : Color.ORANGE;
	}

	@Override
	public void update() {
		GameModel game = controller.getGame();

		energizers.forEach(energizer -> {
			V2i tile = (V2i) energizer.getUserData();
			energizer.setVisible(!game.level.isFoodRemoved(tile) && energizerBlinking.frame());
		});

		pellets.forEach(pellet -> {
			V2i tile = (V2i) pellet.getUserData();
			pellet.setVisible(!game.level.isFoodRemoved(tile));
		});

		playerShape = (Node) playerMunching(game.pac, game.pac.dir).frame();
		playerShape.setTranslateX(game.pac.position.x);
		playerShape.setTranslateY(game.pac.position.y);
		playerShape.setTranslateZ(-HTS);
		playerShape.setVisible(game.pac.visible);

		root.getChildren().removeAll(ghostShapes);
		for (Ghost ghost : game.ghosts) {
			Node shape = ghostShape(ghost, game.pac.powerTicksLeft > 0);
			ghostShapes.set(ghost.id, shape);
			shape.setVisible(ghost.visible);
		}
		root.getChildren().addAll(ghostShapes);

		scoreDisplay.setFill(Color.WHITE);
		scoreDisplay.setFont(scoreFont);
		scoreDisplay.setText(String.format("SCORE\n%08d", game.score));
		scoreDisplay.setTranslateX(TS);
		scoreDisplay.setTranslateY(-2 * TS);
		scoreDisplay.setTranslateZ(-2 * TS);
		scoreDisplay.setRotationAxis(Rotate.X_AXIS);
		scoreDisplay.setRotate(camera.getRotate());

		hiscoreDisplay.setFill(Color.WHITE);
		hiscoreDisplay.setFont(scoreFont);
		hiscoreDisplay.setText(String.format("HI SCORE\n%08d", game.highscorePoints));
		hiscoreDisplay.setTranslateX(18 * TS);
		hiscoreDisplay.setTranslateY(-2 * TS);
		hiscoreDisplay.setTranslateZ(-2 * TS);
		hiscoreDisplay.setRotationAxis(Rotate.X_AXIS);
		hiscoreDisplay.setRotate(camera.getRotate());

		computeViewOrder();
	}

	private void computeViewOrder() {
		GameModel game = controller.getGame();
		walls.values().forEach(wall -> {
			V2i tile = (V2i) wall.getUserData();
			wall.setViewOrder(-tile.y * TS);
		});
		energizers.forEach(energizer -> {
			V2i tile = (V2i) energizer.getUserData();
			energizer.setViewOrder(-tile.y * TS - 0.1);
		});
		pellets.forEach(pellet -> {
			V2i tile = (V2i) pellet.getUserData();
			pellet.setViewOrder(-tile.y * TS - 0.1);
		});
		for (Ghost ghost : game.ghosts) {
			Node ghostShape = ghostShapes.get(ghost.id);
			ghostShape.setViewOrder(-ghost.position.y - 0.5);
		}
		playerShape.setViewOrder(-game.pac.position.y - 0.5);

		scoreDisplay.setViewOrder(-1000);
		hiscoreDisplay.setViewOrder(-1000);
	}

	@Override
	public void end() {
	}

	@Override
	public Camera getCamera() {
		return camera;
	}

	// Animations

	@Override
	public void initCamera() {
		log("Init camera for PlayScene3D");
		camera.setTranslateX(0);
		// TODO how to do that right?
		camera.setTranslateY(subScene.getHeight() * 1.5);
		camera.setTranslateZ(-subScene.getHeight() * 1.5);
		camera.setRotate(36);
	}

	private final Animation<?> NO_ANIMATION = Animation.of(new Object());

	@Override
	public Animation<?> flapFlapping() {
		return NO_ANIMATION;
	}

	@Override
	public Animation<?> storkFlying() {
		return NO_ANIMATION;
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
		if (ghost.bounty > 0) {
			DropShadow shadow = new DropShadow(0.3, Color.color(0.4, 0.4, 0.4));
			Text bountyText = new Text();
			bountyText.setEffect(shadow);
			bountyText.setCache(true);
			bountyText.setText(String.valueOf(ghost.bounty));
			bountyText.setFont(Font.font("Sans", FontWeight.BOLD, TS));
			bountyText.setFill(Color.YELLOW);
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

		shape.setTranslateX(ghost.position.x);
		shape.setTranslateY(ghost.position.y);
		shape.setTranslateZ(-HTS);
		return shape;
	}

	private Map<Ghost, Animation<?>> ghostFlashingAnimation = new HashMap<>();

	@Override
	public Animation<?> ghostFlashing(Ghost ghost) {
		if (!ghostFlashingAnimation.containsKey(ghost)) {
			Sphere s1 = new Sphere(HTS);
			s1.setMaterial(new PhongMaterial(Color.BLUE));
			Sphere s2 = new Sphere(HTS);
			s2.setMaterial(new PhongMaterial(Color.WHITE));
			ghostFlashingAnimation.put(ghost, Animation.of(s1, s2).frameDuration(10).endless());
		}
		return ghostFlashingAnimation.get(ghost);
	}

	private Map<Ghost, Animation<?>> ghostFrightenedAnimation = new HashMap<>();

	@Override
	public Animation<?> ghostFrightened(Ghost ghost, Direction dir) {
		if (!ghostFrightenedAnimation.containsKey(ghost)) {
			Sphere s = new Sphere(HTS);
			s.setMaterial(new PhongMaterial(Color.BLUE));
			s.setUserData(ghost);
			ghostFrightenedAnimation.put(ghost, Animation.of(s));
		}
		return ghostFrightenedAnimation.get(ghost);
	}

	private Map<Ghost, Animation<?>> ghostKickingAnimation = new HashMap<>();

	@Override
	public Animation<?> ghostKicking(Ghost ghost, Direction dir) {
		if (!ghostKickingAnimation.containsKey(ghost)) {
			Sphere s = new Sphere(HTS);
			s.setMaterial(new PhongMaterial(ghostColor(ghost.id)));
			s.setUserData(ghost);
			ghostKickingAnimation.put(ghost, Animation.of(s));
		}
		return ghostKickingAnimation.get(ghost);
	}

	private Map<Ghost, Animation<?>> ghostReturningHomeAnimation = new HashMap<>();

	@Override
	public Animation<?> ghostReturningHome(Ghost ghost, Direction dir) {
		if (!ghostReturningHomeAnimation.containsKey(ghost)) {
			Cylinder s = new Cylinder(2, TS);
			s.setMaterial(new PhongMaterial(ghostColor(ghost.id)));
			s.setUserData(ghost);
			ghostReturningHomeAnimation.put(ghost, Animation.of(s));
		}
		return ghostReturningHomeAnimation.get(ghost);
	}

	@Override
	public Animation<?> mazeFlashing(int mazeNumber) {
		// TODO implement this method
		return NO_ANIMATION;
	}

	@Override
	public Stream<Animation<?>> mazeFlashings() {
		// TODO implement this method
		return Stream.empty();
	}

	@Override
	public Animation<?> playerDying() {
		// TODO implement this method
		return NO_ANIMATION;
	}

	private Animation<?> playerMunching;

	@Override
	public Animation<?> playerMunching(Pac player, Direction dir) {
		if (playerMunching == null) {
			Box box = new Box(TS, TS, TS);
			box.setMaterial(new PhongMaterial(Color.YELLOW));
			box.setUserData(player);
			playerMunching = Animation.of(box);
		}
		return playerMunching;
	}

	@Override
	public Animation<?> spouseMunching(Pac spouse, Direction dir) {
		// TODO implement this method
		return null;
	}
}