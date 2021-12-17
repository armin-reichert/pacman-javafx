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

import de.amr.games.pacman.model.world.FloorPlan;
import de.amr.games.pacman.model.world.PacManGameWorld;
import de.amr.games.pacman.ui.fx.Env;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

/**
 * Creates the maze structure.
 * 
 * @author Armin Reichert
 */
public class Maze3DBuilder {

	public DoubleProperty $wallHeight = new SimpleDoubleProperty(2.0);

	private final Maze3D maze3D;
	private FloorPlan floorPlan;
	private List<Node> parts;
	private PhongMaterial wallBaseMaterial;
	private PhongMaterial wallTopMaterial;

	public Maze3DBuilder(Maze3D maze3D) {
		this.maze3D = maze3D;
		wallBaseMaterial = new PhongMaterial();
		wallTopMaterial = new PhongMaterial();
	}

	public void build(PacManGameWorld world, int resolution) {
		floorPlan = FloorPlan.build(resolution, world);
		double stoneSize = TS / resolution;
		createWalls(world, stoneSize);
		createDoors(world, stoneSize);
	}

	public List<Node> getParts() {
		return Collections.unmodifiableList(parts);
	}

	public void setBaseMaterial(PhongMaterial material) {
		this.wallBaseMaterial = material;
	}

	public void setTopMaterial(PhongMaterial material) {
		this.wallTopMaterial = material;
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

	private void createDoors(PacManGameWorld world, double stoneSize) {
		PhongMaterial doorMaterial = new PhongMaterial(Maze3D.DOOR_COLOR_CLOSED);
		world.ghostHouse().doorTiles().forEach(tile -> {
			Box door = new Box(TS - 1, 1, HTS);
			door.setMaterial(doorMaterial);
			door.setTranslateX(tile.x * TS + HTS);
			door.setTranslateY(tile.y * TS + HTS);
			door.setTranslateZ(-HTS / 2);
			door.setUserData(tile);
			door.drawModeProperty().bind(Env.$drawMode3D);
			maze3D.addDoor(door);
			parts.add(door);
		});
	}

	private void createWalls(PacManGameWorld world, double stoneSize) {
		parts = new ArrayList<>();
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