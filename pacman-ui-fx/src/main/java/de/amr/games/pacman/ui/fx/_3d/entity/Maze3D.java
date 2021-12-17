/*
MIT License

Copyright (c) 2021 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package de.amr.games.pacman.ui.fx._3d.entity;

import static de.amr.games.pacman.model.world.PacManGameWorld.HTS;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.Logging;
import de.amr.games.pacman.model.world.FloorPlan;
import de.amr.games.pacman.model.world.PacManGameWorld;
import de.amr.games.pacman.ui.fx.Env;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Translate;

/**
 * 3D-model for a maze. Creates walls using information from the world map / floor plan.
 * 
 * @author Armin Reichert
 */
public class Maze3D extends Group {

	public final DoubleProperty $wallHeight = new SimpleDoubleProperty(2.0);
	public final IntegerProperty $resolution = new SimpleIntegerProperty(8);

	private double sizeX;
	private double sizeY;
	private final List<Node> parts = new ArrayList<>();
	private Box floor;
	private double floorSizeZ = 0.1;
	private Color floorColor = Color.rgb(20, 20, 120);
	private double energizerSize = 2.5;
	private double pelletSize = 1;
	private PhongMaterial wallBaseMaterial = new PhongMaterial();
	private PhongMaterial wallTopMaterial = new PhongMaterial();
	private Group componentsGroup = new Group();
	private Group foodGroup = new Group();
	private final List<Box> doors = new ArrayList<>();
	private Color doorClosedColor = Color.PINK;
	private Color doorOpenColor = Color.TRANSPARENT;

	/**
	 * Creates the 3D representation of the maze without walls and doors.
	 * 
	 * @param world the game world
	 * @param sizeX maze x-size in units
	 * @param sizeY maze y-size in units
	 */
	public Maze3D(PacManGameWorld world, double sizeX, double sizeY) {
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		$resolution.addListener((x, y, z) -> {
			build(world);
		});
		createFloor();
		componentsGroup.setTranslateX(-TS / 2);
		componentsGroup.setTranslateY(-TS / 2);
		getChildren().addAll(floor, componentsGroup, foodGroup);
	}

	/**
	 * Creates the walls and doors according to the current resolution.
	 * 
	 * @param world the game world
	 */
	public void build(PacManGameWorld world) {
		int res = $resolution.get();
		FloorPlan floorPlan = FloorPlan.build(res, world);
		double stoneSize = TS / res;
		parts.clear();
		doors.clear();
		addWalls(floorPlan, world, stoneSize);
		addDoors(world, stoneSize);
		componentsGroup.getChildren().setAll(parts);
		Logging.log("Rebuild 3D maze with resolution %d (stone size %.2f)", res, stoneSize);
	}

	public void buildWithFood(PacManGameWorld world, Color foodColor) {
		build(world);
		foodGroup.getChildren().clear();
		final var foodMaterial = new PhongMaterial(foodColor);
		world.tiles().filter(world::isFoodTile).forEach(foodTile -> {
			double radius = world.isEnergizerTile(foodTile) ? energizerSize : pelletSize;
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

	private void createFloor() {
		floor = new Box(sizeX - 1, sizeY - 1, floorSizeZ);
		floor.drawModeProperty().bind(Env.$drawMode3D);
		floor.getTransforms().add(new Translate(sizeX / 2 - TS / 2, sizeY / 2 - TS / 2, -0.5 * floorSizeZ + 0.1));
		var floorMaterial = new PhongMaterial(floorColor);
		floorMaterial.setSpecularColor(floorColor.brighter());
		floor.setMaterial(floorMaterial);
	}

	public Color getDoorClosedColor() {
		return doorClosedColor;
	}

	public Color getDoorOpenColor() {
		return doorOpenColor;
	}

	public List<Box> getDoors() {
		return Collections.unmodifiableList(doors);
	}

	public Stream<Node> foodNodes() {
		return foodGroup.getChildren().stream();
	}

	public void showDoorsOpen(boolean open) {
		Color doorColor = open ? doorOpenColor : doorClosedColor;
		PhongMaterial material = new PhongMaterial(doorColor);
		for (Box door : doors) {
			door.setMaterial(material);
		}
	}

	/**
	 * Adds a wall at given position. A wall consists of a base and a top part which can have different
	 * color and material.
	 * 
	 * @param leftX      x-coordinate of top-left stone
	 * @param topY       y-coordinate of top-left stone
	 * @param numStonesX number of stones in x-direction
	 * @param numStonesY number of stones in y-direction
	 * @param stoneSize  size of a single stone
	 * @return pair of walls (base, top)
	 */
	private List<Box> addWall(int leftX, int topY, int numStonesX, int numStonesY, double stoneSize) {
		Box wallBase = new Box(numStonesX * stoneSize, numStonesY * stoneSize, $wallHeight.get());
		wallBase.depthProperty().bind($wallHeight);
		wallBase.setMaterial(wallBaseMaterial);
		wallBase.setTranslateX((leftX + 0.5 * numStonesX) * stoneSize);
		wallBase.setTranslateY((topY + 0.5 * numStonesY) * stoneSize);
		wallBase.translateZProperty().bind($wallHeight.multiply(-0.5));
		wallBase.drawModeProperty().bind(Env.$drawMode3D);
		parts.add(wallBase);

		double topHeight = 0.5;
		Box wallTop = new Box(numStonesX * stoneSize, numStonesY * stoneSize, topHeight);
		wallTop.setMaterial(wallTopMaterial);
		wallTop.setTranslateX(leftX * stoneSize + numStonesX * 0.5 * stoneSize);
		wallTop.setTranslateY(topY * stoneSize + numStonesY * 0.5 * stoneSize);
		wallTop.translateZProperty()
				.bind(wallBase.translateZProperty().subtract($wallHeight.add(topHeight + 0.1).multiply(0.5)));
		wallTop.drawModeProperty().bind(Env.$drawMode3D);
		parts.add(wallTop);

		return Arrays.asList(wallBase, wallTop);
	}

	// TODO I need a half cylinder or a special corner shape for smooth corners
	private void addCorner(int x, int y, double blockSize) {
		addWall(x, y, 1, 1, blockSize);
	}

	private void addDoors(PacManGameWorld world, double stoneSize) {
		PhongMaterial doorMaterial = new PhongMaterial(doorClosedColor);
		world.ghostHouse().doorTiles().forEach(tile -> {
			Box door = new Box(TS - 1, 1, HTS);
			door.setMaterial(doorMaterial);
			door.setTranslateX(tile.x * TS + HTS);
			door.setTranslateY(tile.y * TS + HTS);
			door.setTranslateZ(-HTS / 2);
			door.setUserData(tile);
			door.drawModeProperty().bind(Env.$drawMode3D);
			doors.add(door);
			parts.add(door);
		});
	}

	private void addWalls(FloorPlan floorPlan, PacManGameWorld world, double stoneSize) {
		// horizontal
		for (int y = 0; y < floorPlan.sizeY(); ++y) {
			int leftX = -1;
			int sizeX = 0;
			for (int x = 0; x < floorPlan.sizeX(); ++x) {
				if (floorPlan.get(x, y) == FloorPlan.HWALL) {
					if (leftX == -1) {
						leftX = x;
						sizeX = 1;
					} else {
						sizeX++;
					}
				} else {
					if (leftX != -1) {
						addWall(leftX, y, sizeX, 1, stoneSize);
						leftX = -1;
					}
				}
				if (x == floorPlan.sizeX() - 1 && leftX != -1) {
					addWall(leftX, y, sizeX, 1, stoneSize);
					leftX = -1;
				}
			}
			if (y == floorPlan.sizeY() - 1 && leftX != -1) {
				addWall(leftX, y, sizeX, 1, stoneSize);
				leftX = -1;
			}
		}

		// vertical
		for (int x = 0; x < floorPlan.sizeX(); ++x) {
			int topY = -1;
			int sizeY = 0;
			for (int y = 0; y < floorPlan.sizeY(); ++y) {
				if (floorPlan.get(x, y) == FloorPlan.VWALL) {
					if (topY == -1) {
						topY = y;
						sizeY = 1;
					} else {
						sizeY++;
					}
				} else {
					if (topY != -1) {
						addWall(x, topY, 1, sizeY, stoneSize);
						topY = -1;
					}
				}
				if (y == floorPlan.sizeY() - 1 && topY != -1) {
					addWall(x, topY, 1, sizeY, stoneSize);
					topY = -1;
				}
			}
			if (x == floorPlan.sizeX() - 1 && topY != -1) {
				addWall(x, topY, 1, sizeY, stoneSize);
				topY = -1;
			}
		}

		// corners
		for (int y = 0; y < floorPlan.sizeY(); ++y) {
			for (int x = 0; x < floorPlan.sizeX(); ++x) {
				if (floorPlan.get(x, y) == FloorPlan.CORNER) {
					addCorner(x, y, stoneSize);
				}
			}
		}
	}
}