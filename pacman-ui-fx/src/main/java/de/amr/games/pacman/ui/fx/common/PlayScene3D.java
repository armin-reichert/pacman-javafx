package de.amr.games.pacman.ui.fx.common;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.world.PacManGameWorld.HTS;
import static de.amr.games.pacman.world.PacManGameWorld.TS;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.Animation;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.Ghost;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.SubScene;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;

public class PlayScene3D implements GameScene3D {

	public final BooleanProperty cameraEnabledProperty = new SimpleBooleanProperty();

	private final PacManGameController controller;
	private final Group root;
	private final SubScene subScene;
	private final ControllableCamera camera;
	private Scale scale;

	private Sphere playerShape;
	private List<Sphere> ghostShapes = new ArrayList<>();
	private List<Box> maze = new ArrayList<>();
	private List<Sphere> energizers = new ArrayList<>();
	private List<Sphere> pellets = new ArrayList<>();

	private Animation<Boolean> pulse = Animation.pulse().frameDuration(15).run();

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
				Box wall = new Box(TS, TS, TS);
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
		game.level.world.tiles().filter(game.level.world::isFoodTile).forEach(tile -> {
			Sphere ball = new Sphere(1);
			ball.setMaterial(new PhongMaterial(Color.YELLOW));
			ball.setUserData(tile);
			ball.setTranslateX(tile.x * TS);
			ball.setTranslateY(tile.y * TS + 2);
			ball.setTranslateZ(TS - 2);
			pellets.add(ball);
		});

		playerShape = new Sphere(HTS);
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
			energizer.setVisible(!game.level.isFoodRemoved(tile) && pulse.frame());
		});
		pellets.forEach(pellet -> {
			V2i tile = (V2i) pellet.getUserData();
			pellet.setVisible(!game.level.isFoodRemoved(tile));
		});

		playerShape.setTranslateX(game.pac.position.x);
		playerShape.setTranslateY(game.pac.position.y);
		for (Ghost ghost : game.ghosts) {
			Sphere ghostShape = ghostShapes.get(ghost.id);
			ghostShape.setTranslateX(ghost.position.x);
			ghostShape.setTranslateY(ghost.position.y);
			PhongMaterial material = (PhongMaterial) ghostShape.getMaterial();
			switch (ghost.state) {
			case DEAD:
				material.setDiffuseColor(Color.rgb(200, 200, 200, 0.25));
				break;
			case FRIGHTENED:
				material.setDiffuseColor(Color.BLUE);
				break;
			default:
				material.setDiffuseColor(ghostColor(ghost.id));
			}
		}
		computeViewOrder();
		pulse.animate();
	}

	private void computeViewOrder() {
		GameModel game = controller.getGame();
		maze.stream().filter(Objects::nonNull).forEach(wall -> {
			V2i tile = (V2i) wall.getUserData();
			wall.setViewOrder(-tile.y);
		});
		energizers.forEach(energizer -> {
			V2i tile = (V2i) energizer.getUserData();
			energizer.setViewOrder(-tile.y - 0.5);
		});
		pellets.forEach(energizer -> {
			V2i tile = (V2i) energizer.getUserData();
			energizer.setViewOrder(-tile.y - 0.5);
		});
		playerShape.setViewOrder(-game.pac.tile().y - 0.5);
		for (Ghost ghost : game.ghosts) {
			Node ghostShape = ghostShapes.get(ghost.id);
			ghostShape.setViewOrder(-ghost.tile().y - 0.5);
		}
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
}