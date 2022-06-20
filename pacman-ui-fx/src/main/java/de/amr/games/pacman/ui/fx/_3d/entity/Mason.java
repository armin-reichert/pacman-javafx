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
import javafx.scene.Group;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

/**
 * @author Armin Reichert
 */
public class Mason {

	private static class BuildDetails {
		double brickSize;
		PhongMaterial baseMaterial;
		PhongMaterial topMaterial;
		MazeStyle mazeStyle;
	}

	public static void erectBuilding(Maze3D maze3D, World world, MazeStyle mazeStyle) {
		var floorPlan = new FloorPlan(maze3D.resolution.get(), world);

		var details = new BuildDetails();
		details.mazeStyle = mazeStyle;
		details.baseMaterial = new PhongMaterial(mazeStyle.wallSideColor);
		details.baseMaterial.setSpecularColor(mazeStyle.wallSideColor.brighter());
		details.topMaterial = new PhongMaterial(mazeStyle.wallTopColor);
		details.brickSize = TS / floorPlan.getResolution();
		maze3D.foundationGroup.getChildren().clear();

		Mason mason = new Mason();
		mason.addFloor(maze3D, world, details);
		mason.addCorners(maze3D, floorPlan, details);
		mason.addHorizontalWalls(maze3D, floorPlan, details);
		mason.addVerticalWalls(maze3D, floorPlan, details);
		mason.addDoors(maze3D, world, details);

		Logging.log("Built 3D maze (resolution=%d, wall height=%.2f)", floorPlan.getResolution(), maze3D.wallHeight.get());
	}

	private void addFloor(Maze3D maze3D, World world, BuildDetails details) {
		var floor = new MazeFloor3D(world.numCols() * TS - 1, world.numRows() * TS - 1, 0.01);
		floor.setTranslateX(0.5 * floor.getWidth());
		floor.setTranslateY(0.5 * floor.getHeight());
		floor.setTranslateZ(0.5 * floor.getDepth());
		floor.texture.set(details.mazeStyle.floorTexture);
		floor.textureColor.set(details.mazeStyle.floorTextureColor);
		floor.solidColor.set(details.mazeStyle.floorSolidColor);
		maze3D.foundationGroup.getChildren().add(floor);
	}

	private void addDoors(Maze3D maze3D, World world, BuildDetails details) {
		var leftDoor = new Door3D(world.ghostHouse().doorTileLeft(), true, details.mazeStyle.doorColor);
		leftDoor.doorHeight.bind(maze3D.wallHeight);
		var rightDoor = new Door3D(world.ghostHouse().doorTileRight(), false, details.mazeStyle.doorColor);
		rightDoor.doorHeight.bind(maze3D.wallHeight);
		maze3D.doorsGroup.getChildren().setAll(leftDoor.getNode(), rightDoor.getNode());
	}

	private void addHorizontalWalls(Maze3D maze3D, FloorPlan floorPlan, BuildDetails details) {
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
						addWall(maze3D, leftX, y, sizeX, 1, details);
						leftX = -1;
					}
				}
				if (x == floorPlan.sizeX() - 1 && leftX != -1) {
					addWall(maze3D, leftX, y, sizeX, 1, details);
					leftX = -1;
				}
			}
			if (y == floorPlan.sizeY() - 1 && leftX != -1) {
				addWall(maze3D, leftX, y, sizeX, 1, details);
			}
		}
	}

	private void addVerticalWalls(Maze3D maze3D, FloorPlan floorPlan, BuildDetails details) {
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
						addWall(maze3D, x, topY, 1, sizeY, details);
						topY = -1;
					}
				}
				if (y == floorPlan.sizeY() - 1 && topY != -1) {
					addWall(maze3D, x, topY, 1, sizeY, details);
					topY = -1;
				}
			}
			if (x == floorPlan.sizeX() - 1 && topY != -1) {
				addWall(maze3D, x, topY, 1, sizeY, details);
			}
		}
	}

	private void addCorners(Maze3D maze3D, FloorPlan floorPlan, BuildDetails details) {
		for (int x = 0; x < floorPlan.sizeX(); ++x) {
			for (int y = 0; y < floorPlan.sizeY(); ++y) {
				if (floorPlan.get(x, y) == FloorPlan.CORNER) {
					addWall(maze3D, x, y, 1, 1, details);
				}
			}
		}
	}

	/**
	 * Adds a wall at given position. A wall consists of a base and a top part which can have different color and
	 * material.
	 * 
	 * @param maze3D     the maze
	 * @param x          x-coordinate of top-left brick
	 * @param y          y-coordinate of top-left brick
	 * @param numBricksX number of bricks in x-direction
	 * @param numBricksY number of bricks in y-direction
	 * @param details    details for building stuff
	 */
	private void addWall(Maze3D maze3D, int x, int y, int numBricksX, int numBricksY, BuildDetails details) {
		Box base = new Box(numBricksX * details.brickSize, numBricksY * details.brickSize, maze3D.wallHeight.get());
		base.depthProperty().bind(maze3D.wallHeight);
		base.setMaterial(details.baseMaterial);
		base.translateZProperty().bind(maze3D.wallHeight.multiply(-0.5));
		base.drawModeProperty().bind(Env.drawMode3D);

		double topHeight = 0.5;
		Box top = new Box(numBricksX * details.brickSize, numBricksY * details.brickSize, topHeight);
		top.setMaterial(details.topMaterial);
		top.translateZProperty()
				.bind(base.translateZProperty().subtract(maze3D.wallHeight.add(topHeight + 0.1).multiply(0.5)));
		top.drawModeProperty().bind(Env.drawMode3D);

		Group wall = new Group(base, top);
		wall.setTranslateX((x + 0.5 * numBricksX) * details.brickSize);
		wall.setTranslateY((y + 0.5 * numBricksY) * details.brickSize);

		maze3D.foundationGroup.getChildren().add(wall);
	}
}