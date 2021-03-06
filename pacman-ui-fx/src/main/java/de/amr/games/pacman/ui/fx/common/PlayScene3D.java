package de.amr.games.pacman.ui.fx.common;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.world.PacManGameWorld.HTS;
import static de.amr.games.pacman.world.PacManGameWorld.TS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;

public class PlayScene3D
		implements GameScene3D, PacManGameAnimations, GhostAnimations, MazeAnimations, PlayerAnimations {

	private final PacManGameController controller;
	private final Group root;
	private final SubScene subScene;
	private final PerspectiveCamera camera;
	private Scale scale;

	private Shape3D playerShape;
	private List<Shape3D> ghostShapes = new ArrayList<>();
	private List<Box> walls = new ArrayList<>();
	private List<Shape3D> energizers = new ArrayList<>();
	private List<Shape3D> pellets = new ArrayList<>();

	public PlayScene3D(PacManGameController controller, double height) {
		this.controller = controller;
		double width = GameScene.ASPECT_RATIO * height;
		double s = width / GameScene.WIDTH_UNSCALED;
		scale = new Scale(s, s, s);

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
		log("Start PlayScene3D");
		GameModel game = controller.getGame();

		walls.clear();
		game.level.world.tiles().forEach(tile -> {
			if (game.level.world.isWall(tile)) {
				Box wall = new Box(TS - 1, TS - 1, TS - 1);
				if (controller.isPlaying(GameType.PACMAN)) {
					wall.setMaterial(new PhongMaterial(Color.BLUE));
				} else {
					wall.setMaterial(new PhongMaterial(MsPacMan_Constants.getMazeWallColor(game.level.mazeNumber - 1)));
				}
//				wall.setDrawMode(DrawMode.LINE);
				wall.setTranslateX(tile.x * TS);
				wall.setTranslateY(tile.y * TS);
				wall.setUserData(tile);
				walls.add(wall);
			}
		});

		energizers.clear();
		game.level.world.tiles().filter(game.level.world::isEnergizerTile).forEach(tile -> {
			Sphere ball = new Sphere(HTS - 1);
			ball.setMaterial(new PhongMaterial(Color.YELLOW));
			ball.setUserData(tile);
			ball.setTranslateX(tile.x * TS);
			ball.setTranslateY(tile.y * TS);
			ball.setTranslateZ(-HTS);
			energizers.add(ball);
		});

		pellets.clear();
		game.level.world.tiles().filter(game.level.world::isFoodTile)
				.filter(tile -> !game.level.world.isEnergizerTile(tile)).forEach(tile -> {
					Sphere ball = new Sphere(1.5);
					ball.setMaterial(new PhongMaterial(Color.YELLOW));
					ball.setUserData(tile);
					ball.setTranslateX(tile.x * TS);
					ball.setTranslateY(tile.y * TS);
					ball.setTranslateZ(-HTS);
					pellets.add(ball);
				});

		playerShape = (Shape3D) playerMunching(game.pac, game.pac.dir).frame();

		ghostShapes.clear();
		for (Ghost ghost : game.ghosts) {
			ghostShapes.add(ghostShape(ghost, game.pac.powerTicksLeft > 0));
		}

		root.getChildren().clear();
		root.getChildren().addAll(walls.stream().filter(Objects::nonNull).toArray(Node[]::new));
		root.getChildren().addAll(playerShape);
		root.getChildren().addAll(ghostShapes);
		root.getChildren().addAll(energizers);
		root.getChildren().addAll(pellets);
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

		playerShape = (Shape3D) playerMunching(game.pac, game.pac.dir).frame();
		playerShape.setTranslateX(game.pac.position.x);
		playerShape.setTranslateY(game.pac.position.y);
		playerShape.setTranslateZ(-HTS);
		playerShape.setVisible(game.pac.visible);

		root.getChildren().removeAll(ghostShapes);
		for (Ghost ghost : game.ghosts) {
			Shape3D shape = ghostShape(ghost, game.pac.powerTicksLeft > 0);
			ghostShapes.set(ghost.id, shape);
			shape.setTranslateX(ghost.position.x);
			shape.setTranslateY(ghost.position.y);
			shape.setTranslateZ(-HTS);
			shape.setVisible(ghost.visible);
		}
		root.getChildren().addAll(ghostShapes);

		computeViewOrder();
	}

	private void computeViewOrder() {
		GameModel game = controller.getGame();
		walls.stream().filter(Objects::nonNull).forEach(wall -> {
			V2i tile = (V2i) wall.getUserData();
			wall.setViewOrder(-tile.y);
		});
		energizers.forEach(energizer -> {
			V2i tile = (V2i) energizer.getUserData();
			energizer.setViewOrder(-tile.y - 0.2);
		});
		pellets.forEach(energizer -> {
			V2i tile = (V2i) energizer.getUserData();
			energizer.setViewOrder(-tile.y - 0.2);
		});
		for (Ghost ghost : game.ghosts) {
			Node ghostShape = ghostShapes.get(ghost.id);
			ghostShape.setViewOrder(-ghost.tile().y - 0.3);
		}
		playerShape.setViewOrder(-game.pac.tile().y - 0.4);
	}

	@Override
	public void end() {
	}

	@Override
	public Camera getCamera() {
		return camera;
	}

	// Animations

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

	private Shape3D ghostShape(Ghost ghost, boolean frightened) {
//		if (ghost.bounty > 0) {
//			return assets.numberSprites.get(ghost.bounty);
//		}
		if (ghost.is(GhostState.DEAD) || ghost.is(GhostState.ENTERING_HOUSE)) {
			Shape3D shape = (Shape3D) ghostReturningHome(ghost, ghost.dir).animate();
			if (ghost.dir == Direction.DOWN || ghost.dir == Direction.UP) {
				shape.setRotate(0);
			} else {
				shape.setRotate(90);
			}
			return shape;
		}
		if (ghost.is(GhostState.FRIGHTENED)) {
			return (Shape3D) (ghostFlashing(ghost).isRunning() ? ghostFlashing(ghost).frame()
					: ghostFrightened(ghost, ghost.dir).animate());
		}
		if (ghost.is(GhostState.LOCKED) && frightened) {
			return (Shape3D) ghostFrightened(ghost, ghost.dir).animate();
		}
		return (Shape3D) ghostKicking(ghost, ghost.wishDir).animate(); // Looks towards wish dir!
	}

	private Map<Ghost, Animation<?>> ghostFlashingAnimation = new HashMap<>();

	@Override
	public Animation<?> ghostFlashing(Ghost ghost) {
		if (!ghostFlashingAnimation.containsKey(ghost)) {
			Sphere s1 = new Sphere(HTS);
			s1.setMaterial(new PhongMaterial(Color.BLUE));
			Sphere s2 = new Sphere(HTS);
			s2.setMaterial(new PhongMaterial(Color.WHITE));
			ghostFlashingAnimation.put(ghost, Animation.of(s1, s2).frameDuration(5).endless());
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