package de.amr.games.pacman.ui.fx.entities._3d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.util.List;
import java.util.stream.Stream;

import de.amr.games.pacman.model.common.PacManGameModel;
import de.amr.games.pacman.model.world.PacManGameWorld;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D_Assets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Translate;

/**
 * 3D-model for a maze. Creates boxes representing walls from the world map.
 * 
 * @author Armin Reichert
 */
public class Maze3D extends Group {

	private final Box floor;
	private final PhongMaterial wallMaterial = new PhongMaterial();
	private final Group wallGroup = new Group();
	private final Group foodGroup = new Group();
	private int resolution; // 8, 4, 2, 1
	private double wallHeight;

	public Maze3D(double sizeX, double sizeY) {
		resolution = 8;
		wallHeight = 4;
		var floorMaterial = new PhongMaterial();
		var floorColor = Color.rgb(20, 20, 120);
		floorMaterial.setDiffuseColor(floorColor);
		floorMaterial.setSpecularColor(floorColor.brighter());
		floor = new Box(sizeX - 1, sizeY - 1, 0.1);
		floor.getTransforms().add(new Translate(sizeX / 2 - TS / 2, sizeY / 2 - TS / 2, 3.5));
		floor.setMaterial(floorMaterial);
		getChildren().addAll(floor, wallGroup, foodGroup);
	}

	public void setResolution(int resolution) {
		this.resolution = resolution;
	}
	
	public void setWallHeight(double wallHeight) {
		this.wallHeight = wallHeight;
	}

	public void build(PacManGameModel game) {
		buildWalls(game.currentLevel().world);
		final var foodColor = Rendering2D_Assets.getFoodColor(game.variant(), game.currentLevel().mazeNumber);
		createFood(game.currentLevel().world, foodColor);
	}

	private void buildWalls(PacManGameWorld world) {
		var wallBuilder = new WallBuilder3D();
		wallBuilder.setWallMaterial(wallMaterial);
		wallBuilder.setWallHeight(wallHeight);
		List<Node> walls = wallBuilder.build(world, resolution);
		wallGroup.getChildren().setAll(walls);
		wallGroup.setTranslateX(-TS / 2);
		wallGroup.setTranslateY(-TS / 2);
	}

	private void createFood(PacManGameWorld world, Color foodColor) {
		foodGroup.getChildren().clear();
		final var foodMaterial = new PhongMaterial(foodColor);
		world.tiles().filter(world::isFoodTile).forEach(foodTile -> {
			final var pellet = new Sphere(world.isEnergizerTile(foodTile) ? 2.5 : 1);
			pellet.setMaterial(foodMaterial);
			pellet.setTranslateX(foodTile.x * TS);
			pellet.setTranslateY(foodTile.y * TS);
			pellet.setTranslateZ(1);
			pellet.setUserData(foodTile);
			foodGroup.getChildren().add(pellet);
		});
	}

	public void setWallColor(Color color) {
		wallMaterial.setDiffuseColor(color);
		wallMaterial.setSpecularColor(color.brighter());
	}

	public void setFloorTexture(Image floorTexture) {
		((PhongMaterial) floor.getMaterial()).setDiffuseMap(floorTexture);
	}

	public Stream<Node> foodNodes() {
		return foodGroup.getChildren().stream();
	}
}