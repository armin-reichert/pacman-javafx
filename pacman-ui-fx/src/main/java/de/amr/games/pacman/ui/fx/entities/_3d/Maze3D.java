package de.amr.games.pacman.ui.fx.entities._3d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.util.stream.Stream;

import de.amr.games.pacman.model.world.PacManGameWorld;
import de.amr.games.pacman.ui.fx.Env;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
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

	public final DoubleProperty $wallHeight = new SimpleDoubleProperty(2.0);

	private final Box floor;
	private final double floorSizeZ = 0.1;
	private final PhongMaterial wallBaseMaterial = new PhongMaterial();
	private final PhongMaterial wallTopMaterial = new PhongMaterial();
	private final Group wallGroup = new Group();
	private final Group foodGroup = new Group();

	public Maze3D(double mazeSizeX, double mazeSizeY) {
		floor = new Box(mazeSizeX - 1, mazeSizeY - 1, floorSizeZ);
		floor.drawModeProperty().bind(Env.$drawMode3D);
		floor.getTransforms().add(new Translate(mazeSizeX / 2 - TS / 2, mazeSizeY / 2 - TS / 2, -0.5 * floorSizeZ + 0.1));
		var floorColor = Color.rgb(20, 20, 120);
		var floorMaterial = new PhongMaterial(floorColor);
		floorMaterial.setSpecularColor(floorColor.brighter());
		floor.setMaterial(floorMaterial);
		getChildren().addAll(floor, wallGroup, foodGroup);
	}

	public Stream<Node> foodNodes() {
		return foodGroup.getChildren().stream();
	}

	public void buildWalls(PacManGameWorld world, int resolution, double wallHeight) {
		var wallBuilder = new Maze3DBuilder();
		wallBuilder.$wallHeight.bind($wallHeight);
		wallBuilder.setBaseMaterial(wallBaseMaterial);
		wallBuilder.setTopMaterial(wallTopMaterial);
		wallGroup.setTranslateX(-TS / 2);
		wallGroup.setTranslateY(-TS / 2);
		wallGroup.getChildren().setAll(wallBuilder.build(world, resolution));
	}

	public void buildWallsAndAddFood(PacManGameWorld world, int resolution, double wallHeight, Color foodColor) {
		buildWalls(world, resolution, wallHeight);
		foodGroup.getChildren().clear();
		final var foodMaterial = new PhongMaterial(foodColor);
		world.tiles().filter(world::isFoodTile).forEach(foodTile -> {
			double radius = world.isEnergizerTile(foodTile) ? 2.5 : 1.0;
			final var pellet = new Sphere(radius);
			pellet.setMaterial(foodMaterial);
			pellet.setTranslateX(foodTile.x * TS);
			pellet.setTranslateY(foodTile.y * TS);
			pellet.setTranslateZ(-3);
			pellet.setUserData(foodTile);
			foodGroup.getChildren().add(pellet);
		});
	}

	public void setWallBaseColor(Color color) {
		wallBaseMaterial.setDiffuseColor(color);
		wallBaseMaterial.setSpecularColor(color.brighter());
	}

	public void setWallTopColor(Color color) {
		wallTopMaterial.setDiffuseColor(color);
		wallTopMaterial.setSpecularColor(color); // TODO not sure about this
	}

	public void setFloorTexture(Image floorTexture) {
		((PhongMaterial) floor.getMaterial()).setDiffuseMap(floorTexture);
	}
}