package de.amr.games.pacman.ui.fx.entities._3d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.util.stream.Stream;

import de.amr.games.pacman.model.world.PacManGameWorld;
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
	private final Group foodGroup = new Group();

	public Maze3D(PacManGameWorld world, Color wallColor, double wallHeight, double sizeX, double sizeY) {
		floor = new Box(sizeX, sizeY, 0.1);
		floor.getTransforms().add(new Translate(sizeX / 2 - TS / 2, sizeY / 2 - TS / 2, 3));
		var material = new PhongMaterial();
		material.setDiffuseColor(Color.rgb(20, 20, 120));
		material.setSpecularColor(Color.rgb(20, 20, 120).brighter());
		floor.setMaterial(material);

		final var wallMaterial = new PhongMaterial(wallColor);
		wallMaterial.setSpecularColor(wallColor.brighter());
		var wallBuilder = new WallBuilder();
		wallBuilder.setWallMaterial(wallMaterial);
		wallBuilder.setWallHeight(wallHeight);

		int resolution = 4;
		world.getWallMap(resolution);
		Group wallRoot = new Group();
		wallRoot.getChildren().setAll(wallBuilder.build(world, resolution));

		getChildren().addAll(floor, wallRoot, foodGroup);
	}

	public void setFloorTexture(Image floorTexture) {
		((PhongMaterial) floor.getMaterial()).setDiffuseMap(floorTexture);
	}

	public Stream<Node> foodNodes() {
		return foodGroup.getChildren().stream();
	}

	public void resetFood(PacManGameWorld world, Color foodColor) {
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
}