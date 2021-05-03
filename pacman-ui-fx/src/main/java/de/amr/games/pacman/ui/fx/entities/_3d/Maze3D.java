package de.amr.games.pacman.ui.fx.entities._3d;

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static de.amr.games.pacman.ui.fx.rendering.Rendering2D_Assets.getFoodColor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
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

	private Group wallRoot = new Group();
	private Group foodRoot = new Group();

	public Maze3D(PacManGameWorld world, PhongMaterial wallMaterial, Image floorTexture, double sizeX, double sizeY) {
		createFloor(sizeX, sizeY, floorTexture);
		var wallBuilder = new WallBuilder();
		wallBuilder.setWallMaterial(wallMaterial);
		wallBuilder.setWallHeight(2.5);
		wallRoot.getChildren().setAll(wallBuilder.build(world));
		getChildren().addAll(wallRoot, foodRoot);
	}

	private void createFloor(double sizeX, double sizeY, Image floorTexture) {
		Box floor = new Box(sizeX, sizeY, 0.1);
		floor.getTransforms().add(new Translate(sizeX / 2 - TS / 2, sizeY / 2 - TS / 2, 3));
		var material = new PhongMaterial();
		material.setDiffuseMap(floorTexture);
		material.setDiffuseColor(Color.rgb(30, 30, 120));
		material.setSpecularColor(Color.rgb(60, 60, 240));
		floor.setMaterial(material);
		getChildren().add(floor);
	}

	public Stream<Node> foodNodes() {
		return foodRoot.getChildren().stream();
	}

	public void resetFood(GameVariant variant, GameModel game) {
		GameLevel level = game.currentLevel();
		PacManGameWorld world = level.world;
		var foodMaterial = new PhongMaterial(getFoodColor(variant, level.mazeNumber));
		List<Node> foodNodes = world.tiles().filter(world::isFoodTile)//
				.map(tile -> createPellet(world.isEnergizerTile(tile) ? 2.5 : 1, tile, foodMaterial))
				.collect(Collectors.toList());
		foodRoot.getChildren().setAll(foodNodes);
	}

	private Sphere createPellet(double r, V2i tile, PhongMaterial material) {
		var s = new Sphere(r);
		s.setMaterial(material);
		s.setTranslateX(tile.x * TS);
		s.setTranslateY(tile.y * TS);
		s.setTranslateZ(1);
		s.setUserData(tile);
		return s;
	}
}