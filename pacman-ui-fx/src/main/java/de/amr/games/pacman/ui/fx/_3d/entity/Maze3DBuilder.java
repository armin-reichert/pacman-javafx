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

	public List<Node> getParts() {
		return Collections.unmodifiableList(parts);
	}

	public void setBaseMaterial(PhongMaterial material) {
		this.wallBaseMaterial = material;
	}

	public void setTopMaterial(PhongMaterial material) {
		this.wallTopMaterial = material;
	}

	public List<Node> build(PacManGameWorld world, int resolution) {
		floorPlan = FloorPlan.build(resolution, world);
		double blockSize = TS / resolution;
		createWalls(world, blockSize);
		createDoors(world, blockSize);
		return getParts();
	}

	private List<Box> addBlock(int leftX, int topY, int numBlocksX, int numBlocksY, double blockSize) {
		Box base = new Box(numBlocksX * blockSize, numBlocksY * blockSize, $wallHeight.get());
		base.depthProperty().bind($wallHeight);
		base.setMaterial(wallBaseMaterial);
		base.setTranslateX(leftX * blockSize + numBlocksX * 0.5 * blockSize);
		base.setTranslateY(topY * blockSize + numBlocksY * 0.5 * blockSize);
		base.translateZProperty().bind($wallHeight.multiply(-0.5));
		base.drawModeProperty().bind(Env.$drawMode3D);
		parts.add(base);

		double topHeight = 0.5;
		Box top = new Box(numBlocksX * blockSize, numBlocksY * blockSize, topHeight);
		top.setMaterial(wallTopMaterial);
		top.setTranslateX(leftX * blockSize + numBlocksX * 0.5 * blockSize);
		top.setTranslateY(topY * blockSize + numBlocksY * 0.5 * blockSize);
		top.translateZProperty()
				.bind(base.translateZProperty().subtract($wallHeight.add(topHeight + 0.1).multiply(0.5)));
		top.drawModeProperty().bind(Env.$drawMode3D);
		parts.add(top);

		return Arrays.asList(base, top);
	}

	// TODO I need a half cylinder or a special corner shape for smooth corners
	private void addCorner(int x, int y, double blockSize) {
		addBlock(x, y, 1, 1, blockSize);
	}

	private void createDoors(PacManGameWorld world, double blockSize) {
		PhongMaterial doorMaterial = new PhongMaterial(Maze3D.DOOR_COLOR_CLOSED);
		world.ghostHouse().doorTiles().forEach(tile -> {
			Box door = new Box(TS - 1, 1, 1);
			door.setMaterial(doorMaterial);
			door.setTranslateX(tile.x * TS + TS / 2);
			door.setTranslateY(tile.y * TS + TS / 2);
			door.setTranslateZ(-4);
			door.setUserData(tile);
			door.drawModeProperty().bind(Env.$drawMode3D);
			maze3D.addDoor(door);
			parts.add(door);
		});
	}

	private void createWalls(PacManGameWorld world, double blockSize) {
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
						addBlock(leftX, y, sizeX, 1, blockSize);
						leftX = -1;
					}
				}
				if (x == floorPlan.sizeX() - 1 && leftX != -1) {
					addBlock(leftX, y, sizeX, 1, blockSize);
					leftX = -1;
				}
			}
			if (y == floorPlan.sizeY() - 1 && leftX != -1) {
				addBlock(leftX, y, sizeX, 1, blockSize);
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
						addBlock(x, topY, 1, sizeY, blockSize);
						topY = -1;
					}
				}
				if (y == floorPlan.sizeY() - 1 && topY != -1) {
					addBlock(x, topY, 1, sizeY, blockSize);
					topY = -1;
				}
			}
			if (x == floorPlan.sizeX() - 1 && topY != -1) {
				addBlock(x, topY, 1, sizeY, blockSize);
				topY = -1;
			}
		}

		// corners
		for (int y = 0; y < floorPlan.sizeY(); ++y) {
			for (int x = 0; x < floorPlan.sizeX(); ++x) {
				if (floorPlan.get(x, y) == FloorPlan.CORNER) {
					addCorner(x, y, blockSize);
				}
			}
		}
	}
}