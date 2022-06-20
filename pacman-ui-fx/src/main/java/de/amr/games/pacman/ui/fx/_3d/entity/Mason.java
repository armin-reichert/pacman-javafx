/*
MIT License

Copyright (c) 2022 Armin Reichert

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

import static de.amr.games.pacman.model.common.world.World.TS;

import de.amr.games.pacman.lib.Logging;
import de.amr.games.pacman.model.common.world.FloorPlan;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._3d.entity.Maze3D.WallProperties;
import de.amr.games.pacman.ui.fx.app.Env;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

/**
 * @author Armin Reichert
 */
public class Mason {

	private final Group wallsGroup;
	private final Group doorsGroup;

	public Mason(Group wallsGroup, Group doorsGroup) {
		this.wallsGroup = wallsGroup;
		this.doorsGroup = doorsGroup;
	}

	public void erectBuilding(FloorPlan floorPlan, World world, DoubleProperty wallHeight, Color wallBaseColor,
			Color wallTopColor, Color doorColor) {

		wallsGroup.getChildren().clear();

		var floor = new MazeFloor3D(world.numCols() * TS - 1, world.numRows() * TS - 1, 0.01);
		floor.showSolid(Color.rgb(5, 5, 10));
		floor.setTranslateX(0.5 * floor.getWidth());
		floor.setTranslateY(0.5 * floor.getHeight());
		floor.setTranslateZ(0.5 * floor.getDepth());
		wallsGroup.getChildren().add(floor);

		createWalls(floorPlan, wallHeight, wallBaseColor, wallTopColor);
		createDoors(world, wallHeight, doorColor);
	}

	private void createWalls(FloorPlan floorPlan, DoubleProperty wallHeight, Color wallBaseColor, Color wallTopColor) {
		WallProperties wp = new WallProperties();
		wp.wallHeight = wallHeight;
		wp.baseMaterial = new PhongMaterial(wallBaseColor);
		wp.baseMaterial.setSpecularColor(wallBaseColor.brighter());
		wp.topMaterial = new PhongMaterial(wallTopColor);
		wp.brickSize = TS / floorPlan.getResolution();

		addHorizontalWalls(floorPlan, wp);
		addVerticalWalls(floorPlan, wp);
		addCorners(floorPlan, wp);
		Logging.log("Built 3D maze (resolution=%d, wall height=%.2f)", floorPlan.getResolution(), wallHeight.get());
	}

	private void createDoors(World world, DoubleProperty wallHeight, Color doorColor) {
		var leftDoor = new Door3D(world.ghostHouse().doorTileLeft(), true, doorColor);
		leftDoor.doorHeight.bind(wallHeight);
		var rightDoor = new Door3D(world.ghostHouse().doorTileRight(), false, doorColor);
		rightDoor.doorHeight.bind(wallHeight);
		doorsGroup.getChildren().setAll(leftDoor.getNode(), rightDoor.getNode());
	}

	private void addHorizontalWalls(FloorPlan floorPlan, WallProperties wp) {
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
						addWall(leftX, y, sizeX, 1, wp);
						leftX = -1;
					}
				}
				if (x == floorPlan.sizeX() - 1 && leftX != -1) {
					addWall(leftX, y, sizeX, 1, wp);
					leftX = -1;
				}
			}
			if (y == floorPlan.sizeY() - 1 && leftX != -1) {
				addWall(leftX, y, sizeX, 1, wp);
			}
		}
	}

	private void addVerticalWalls(FloorPlan floorPlan, WallProperties wp) {
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
						addWall(x, topY, 1, sizeY, wp);
						topY = -1;
					}
				}
				if (y == floorPlan.sizeY() - 1 && topY != -1) {
					addWall(x, topY, 1, sizeY, wp);
					topY = -1;
				}
			}
			if (x == floorPlan.sizeX() - 1 && topY != -1) {
				addWall(x, topY, 1, sizeY, wp);
			}
		}
	}

	private void addCorners(FloorPlan floorPlan, WallProperties wp) {
		for (int x = 0; x < floorPlan.sizeX(); ++x) {
			for (int y = 0; y < floorPlan.sizeY(); ++y) {
				if (floorPlan.get(x, y) == FloorPlan.CORNER) {
					addWall(x, y, 1, 1, wp);
				}
			}
		}
	}

	/**
	 * Adds a wall at given position. A wall consists of a base and a top part which can have different color and
	 * material.
	 * 
	 * @param x          x-coordinate of top-left brick
	 * @param y          y-coordinate of top-left brick
	 * @param numBricksX number of bricks in x-direction
	 * @param numBricksY number of bricks in y-direction
	 * @param wp         wall properties
	 */
	private void addWall(int x, int y, int numBricksX, int numBricksY, WallProperties wp) {
		Box base = new Box(numBricksX * wp.brickSize, numBricksY * wp.brickSize, wp.wallHeight.get());
		base.depthProperty().bind(wp.wallHeight);
		base.setMaterial(wp.baseMaterial);
		base.translateZProperty().bind(wp.wallHeight.multiply(-0.5));
		base.drawModeProperty().bind(Env.$drawMode3D);

		double topHeight = 0.5;
		Box top = new Box(numBricksX * wp.brickSize, numBricksY * wp.brickSize, topHeight);
		top.setMaterial(wp.topMaterial);
		top.translateZProperty().bind(base.translateZProperty().subtract(wp.wallHeight.add(topHeight + 0.1).multiply(0.5)));
		top.drawModeProperty().bind(Env.$drawMode3D);

		Group wall = new Group(base, top);
		wall.setTranslateX((x + 0.5 * numBricksX) * wp.brickSize);
		wall.setTranslateY((y + 0.5 * numBricksY) * wp.brickSize);

		wallsGroup.getChildren().add(wall);
	}
}