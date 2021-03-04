package de.amr.games.pacman.ui.fx.common;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.world.PacManGameWorld.HTS;
import static de.amr.games.pacman.world.PacManGameWorld.TS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.Pac;
import de.amr.games.pacman.ui.animation.GhostAnimations;
import de.amr.games.pacman.ui.animation.MazeAnimations;
import de.amr.games.pacman.ui.animation.PacManGameAnimations;
import de.amr.games.pacman.ui.animation.PlayerAnimations;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;

public class PlayScene3D
		implements GameScene3D, PacManGameAnimations, GhostAnimations, MazeAnimations, PlayerAnimations {

	public final BooleanProperty cameraEnabledProperty = new SimpleBooleanProperty();

	private final PacManGameController controller;
	private final Group root;
	private final SubScene subScene;
	private final ControllableCamera camera;
	private Scale scale;

	private Shape3D playerShape;
	private List<Shape3D> ghostShapes = new ArrayList<>();
	private List<Box> maze = new ArrayList<>();
	private List<Shape3D> energizers = new ArrayList<>();
	private List<Shape3D> pellets = new ArrayList<>();

	public PlayScene3D(PacManGameController controller, double height) {
		this.controller = controller;
		double width = ContainerScene2D.ASPECT_RATIO * height;
		double s = width / ContainerScene2D.WIDTH_UNSCALED;
		scale = new Scale(s, s, s);

		root = new Group();
		root.getTransforms().add(scale);

		camera = new ControllableCamera();
		camera.setTranslateZ(-240);
		camera.setRotationAxis(Rotate.X_AXIS);
		camera.setRotate(30);

		subScene = new SubScene(root, width, height);
		subScene.setFill(Color.BLACK);
		subScene.setCamera(camera);
	}

	@Override
	public void resize(double width, double height) {
		double s = width / ContainerScene2D.WIDTH_UNSCALED;
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
		root.getChildren().removeIf(node -> {
			return node == playerShape || ghostShapes.contains(node);
		});
		GameModel game = controller.getGame();

		maze.clear();
		game.level.world.tiles().forEach(tile -> {
			if (game.level.world.isWall(tile)) {
				Box wall = new Box(TS, TS, 4);
				wall.setMaterial(new PhongMaterial(Color.BLUE));
//				wall.setDrawMode(DrawMode.LINE);
				wall.setTranslateX(tile.x * TS);
				wall.setTranslateY(tile.y * TS);
				wall.setUserData(tile);
				maze.add(wall);
			}
		});

		energizers.clear();
		game.level.world.tiles().filter(game.level.world::isEnergizerTile).forEach(tile -> {
			Sphere ball = new Sphere(HTS);
			ball.setMaterial(new PhongMaterial(Color.YELLOW));
			ball.setUserData(tile);
			ball.setTranslateX(tile.x * TS);
			ball.setTranslateY(tile.y * TS);
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
					ball.setTranslateZ(-2);
					pellets.add(ball);
				});

		playerShape = new Box(TS, TS, TS);
		playerShape.setMaterial(new PhongMaterial(Color.YELLOW));
		playerShape.setUserData(game.pac);

		ghostShapes.clear();
		for (Ghost ghost : game.ghosts) {
			Sphere ghostShape = new Sphere(HTS);
			ghostShape.setUserData(ghost);
			ghostShape.setMaterial(new PhongMaterial(ghostColor(ghost.id)));
			ghostShapes.add(ghostShape);
		}

		root.getChildren().clear();
		root.getChildren().addAll(playerShape);
		root.getChildren().addAll(ghostShapes);
		root.getChildren().addAll(energizers);
		root.getChildren().addAll(pellets);
		root.getChildren().addAll(maze.stream().filter(Objects::nonNull).toArray(Node[]::new));
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

		playerShape.setTranslateX(game.pac.position.x);
		playerShape.setTranslateY(game.pac.position.y);

		root.getChildren().removeAll(ghostShapes);
		for (Ghost ghost : game.ghosts) {
			switch (ghost.state) {
			case DEAD:
				ghostShapes.set(ghost.id, (Shape3D) ghostReturningHome(ghost, ghost.dir).frame());
				break;
			case FRIGHTENED:
				ghostShapes.set(ghost.id, (Shape3D) ghostFrightened(ghost, ghost.dir).frame());
				break;
			default:
				ghostShapes.set(ghost.id, (Shape3D) ghostKicking(ghost, ghost.dir).frame());
			}
		}
		for (Ghost ghost : game.ghosts) {
			Shape3D ghostShape = ghostShapes.get(ghost.id);
			ghostShape.setTranslateX(ghost.position.x);
			ghostShape.setTranslateY(ghost.position.y);
		}
		root.getChildren().addAll(ghostShapes);

		computeViewOrder();
	}

	private void computeViewOrder() {
		GameModel game = controller.getGame();
		maze.stream().filter(Objects::nonNull).forEach(wall -> {
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
	public void enableCamera(boolean state) {
		cameraEnabledProperty.set(state);
	}

	@Override
	public boolean isCameraEnabled() {
		return cameraEnabledProperty.get();
	}

	@Override
	public Optional<ControllableCamera> getCamera() {
		return Optional.of(camera);
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

	@Override
	public Animation<?> ghostFlashing() {
		// TODO implement this method
		return NO_ANIMATION;
	}

	private Map<Ghost, Animation<?>> ghostFrightenedAnimation = new HashMap<>();

	@Override
	public Animation<?> ghostFrightened(Ghost ghost, Direction dir) {
		if (!ghostFrightenedAnimation.containsKey(ghost)) {
			Sphere s = new Sphere(HTS);
			s.setMaterial(new PhongMaterial(Color.BLUE));
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
			ghostKickingAnimation.put(ghost, Animation.of(s));
		}
		return ghostKickingAnimation.get(ghost);
	}

	private Map<Ghost, Animation<?>> ghostReturningHomeAnimation = new HashMap<>();

	@Override
	public Animation<?> ghostReturningHome(Ghost ghost, Direction dir) {
		if (!ghostReturningHomeAnimation.containsKey(ghost)) {
			Sphere s = new Sphere(2);
			s.setMaterial(new PhongMaterial(Color.GRAY));
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

	@Override
	public Animation<?> playerMunching(Pac player, Direction dir) {
		// TODO implement this method
		return NO_ANIMATION;
	}

	@Override
	public Animation<?> spouseMunching(Pac spouse, Direction dir) {
		// TODO implement this method
		return null;
	}

}