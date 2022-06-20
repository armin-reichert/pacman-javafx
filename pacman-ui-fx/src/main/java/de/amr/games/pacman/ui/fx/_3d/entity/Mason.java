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
import de.amr.games.pacman.ui.fx._3d.entity.Maze3D.MazeStyle;
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

	private static class BuildingDetails {
		DoubleProperty wallHeight;
		double brickSize;
		PhongMaterial baseMaterial;
		PhongMaterial topMaterial;
		Color doorColor;
	}

	private final Group foundationGroup;
	private final Group doorsGroup;

	public Mason(Group foundationGroup, Group doorsGroup) {
		this.foundationGroup = foundationGroup;
		this.doorsGroup = doorsGroup;
	}

	public void erectBuilding(World world, int resolution, DoubleProperty wallHeight, MazeStyle features,
			boolean floorTextureVisible) {
		foundationGroup.getChildren().clear();

		var floorPlan = new FloorPlan(resolution, world);

		var floor = new MazeFloor3D(world.numCols() * TS - 1, world.numRows() * TS - 1, 0.01);
		floor.setTranslateX(0.5 * floor.getWidth());
		floor.setTranslateY(0.5 * floor.getHeight());
		floor.setTranslateZ(0.5 * floor.getDepth());
		if (floorTextureVisible) {
			floor.showTextured(features.floorTexture, features.floorTextureColor);
		} else {
			floor.showSolid(features.floorSolidColor);
		}
		foundationGroup.getChildren().add(floor);

		BuildingDetails details = new BuildingDetails();
		details.wallHeight = wallHeight;
		details.baseMaterial = new PhongMaterial(features.wallSideColor);
		details.baseMaterial.setSpecularColor(features.wallSideColor.brighter());
		details.topMaterial = new PhongMaterial(features.wallTopColor);
		details.brickSize = TS / floorPlan.getResolution();
		details.doorColor = features.doorColor;

		addCorners(floorPlan, details);
		addHorizontalWalls(floorPlan, details);
		addVerticalWalls(floorPlan, details);
		addDoors(world, details);
		Logging.log("Built 3D maze (resolution=%d, wall height=%.2f)", floorPlan.getResolution(), details.wallHeight.get());
	}

	private void addDoors(World world, BuildingDetails details) {
		var leftDoor = new Door3D(world.ghostHouse().doorTileLeft(), true, details.doorColor);
		leftDoor.doorHeight.bind(details.wallHeight);
		var rightDoor = new Door3D(world.ghostHouse().doorTileRight(), false, details.doorColor);
		rightDoor.doorHeight.bind(details.wallHeight);
		doorsGroup.getChildren().setAll(leftDoor.getNode(), rightDoor.getNode());
	}

	private void addHorizontalWalls(FloorPlan floorPlan, BuildingDetails details) {
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
						addWall(leftX, y, sizeX, 1, details);
						leftX = -1;
					}
				}
				if (x == floorPlan.sizeX() - 1 && leftX != -1) {
					addWall(leftX, y, sizeX, 1, details);
					leftX = -1;
				}
			}
			if (y == floorPlan.sizeY() - 1 && leftX != -1) {
				addWall(leftX, y, sizeX, 1, details);
			}
		}
	}

	private void addVerticalWalls(FloorPlan floorPlan, BuildingDetails details) {
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
						addWall(x, topY, 1, sizeY, details);
						topY = -1;
					}
				}
				if (y == floorPlan.sizeY() - 1 && topY != -1) {
					addWall(x, topY, 1, sizeY, details);
					topY = -1;
				}
			}
			if (x == floorPlan.sizeX() - 1 && topY != -1) {
				addWall(x, topY, 1, sizeY, details);
			}
		}
	}

	private void addCorners(FloorPlan floorPlan, BuildingDetails details) {
		for (int x = 0; x < floorPlan.sizeX(); ++x) {
			for (int y = 0; y < floorPlan.sizeY(); ++y) {
				if (floorPlan.get(x, y) == FloorPlan.CORNER) {
					addWall(x, y, 1, 1, details);
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
	 * @param details    details for building stuff
	 */
	private void addWall(int x, int y, int numBricksX, int numBricksY, BuildingDetails details) {
		Box base = new Box(numBricksX * details.brickSize, numBricksY * details.brickSize, details.wallHeight.get());
		base.depthProperty().bind(details.wallHeight);
		base.setMaterial(details.baseMaterial);
		base.translateZProperty().bind(details.wallHeight.multiply(-0.5));
		base.drawModeProperty().bind(Env.$drawMode3D);

		double topHeight = 0.5;
		Box top = new Box(numBricksX * details.brickSize, numBricksY * details.brickSize, topHeight);
		top.setMaterial(details.topMaterial);
		top.translateZProperty()
				.bind(base.translateZProperty().subtract(details.wallHeight.add(topHeight + 0.1).multiply(0.5)));
		top.drawModeProperty().bind(Env.$drawMode3D);

		Group wall = new Group(base, top);
		wall.setTranslateX((x + 0.5 * numBricksX) * details.brickSize);
		wall.setTranslateY((y + 0.5 * numBricksY) * details.brickSize);

		foundationGroup.getChildren().add(wall);
	}
}