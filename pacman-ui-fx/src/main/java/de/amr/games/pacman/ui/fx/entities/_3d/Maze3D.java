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

/**
 * 3D-model for a maze. Creates boxes representing walls from the world map.
 * 
 * @author Armin Reichert
 */
public class Maze3D extends Group {

	private Box floor;
	private Group wallRoot = new Group();
	private Group foodRoot = new Group();

	public Maze3D(PacManGameWorld world, Color wallColor, double unscaledWidth, double unscaledHeight) {
		createFloor(unscaledWidth, unscaledHeight);
		var material = new PhongMaterial(wallColor);
		material.setSpecularColor(wallColor.brighter());
		var wallBuilder = new WallBuilder();
		wallBuilder.setWallMaterial(material);
		wallBuilder.setWallSizeZ(2.5);
		wallRoot.getChildren().setAll(wallBuilder.build(world));
		getChildren().addAll(floor, wallRoot, foodRoot);
	}

	private void createFloor(double unscaledWidth, double unscaledHeight) {
		floor = new Box(unscaledWidth, unscaledHeight, 0.1);
		var material = new PhongMaterial();
		var texture = new Image(getClass().getResourceAsStream("/common/escher-texture.jpg"));
		material.setDiffuseMap(texture);
		material.setDiffuseColor(Color.rgb(30, 30, 120));
		material.setSpecularColor(Color.rgb(60, 60, 240));
		floor.setMaterial(material);
		floor.setTranslateX(unscaledWidth / 2 - TS / 2);
		floor.setTranslateY(unscaledHeight / 2 - TS / 2);
		floor.setTranslateZ(3);
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