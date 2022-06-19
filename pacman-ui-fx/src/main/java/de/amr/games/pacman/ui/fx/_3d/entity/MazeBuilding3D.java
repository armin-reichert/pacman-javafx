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

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.common.world.World.TS;

import java.util.stream.Stream;

import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.world.FloorPlan;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx.app.Env;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

/**
 * 3D-model for a maze. Creates walls/doors using information from the floor plan.
 * 
 * @author Armin Reichert
 */
public class MazeBuilding3D {

	public static class WallProperties {
		double brickSize;
		PhongMaterial baseMaterial;
		PhongMaterial topMaterial;
	}

	public final DoubleProperty wallHeight = new SimpleDoubleProperty(1.0);
	public final IntegerProperty resolution = new SimpleIntegerProperty(8);
	public final BooleanProperty floorHasTexture = new SimpleBooleanProperty(false);

	private final World world;
	private final Group root = new Group();
	private final Group wallsGroup = new Group();
	private final Group doorsGroup = new Group();

	private Image floorTexture;
	private Color floorTextureColor = Color.BLUE;
	private Color floorSolidColor = Color.GREEN;

	public MazeBuilding3D(V2d unscaledSize, World world) {
		this.world = world;
		var floor = new MazeFloor3D(unscaledSize.x - 1, unscaledSize.y - 1, 0.01);
		floor.showSolid(Color.rgb(5, 5, 10));
		floor.setTranslateX(0.5 * floor.getWidth());
		floor.setTranslateY(0.5 * floor.getHeight());
		floor.setTranslateZ(0.5 * floor.getDepth());
		floorHasTexture.addListener((obs, oldVal, newVal) -> {
			if (newVal.booleanValue()) {
				floor.showTextured(floorTexture, floorTextureColor);
			} else {
				floor.showSolid(floorSolidColor);
			}
		});
		root.getChildren().add(floor);
		root.getChildren().addAll(wallsGroup, doorsGroup);
	}

	public Group getRoot() {
		return root;
	}

	public void setFloorSolidColor(Color floorSolidColor) {
		this.floorSolidColor = floorSolidColor;
	}

	public void setFloorTexture(Image floorTexture) {
		this.floorTexture = floorTexture;
	}

	public void setFloorTextureColor(Color floorTextureColor) {
		this.floorTextureColor = floorTextureColor;
	}

	public Stream<Door3D> doors() {
		return doorsGroup.getChildren().stream().map(Node::getUserData).map(Door3D.class::cast);
	}

	public void erect(Color wallBaseColor, Color wallTopColor, Color doorColor) {
		createWallsAndDoors(wallBaseColor, wallTopColor, doorColor);
		resolution.addListener((obs, oldVal, newVal) -> createWallsAndDoors(wallBaseColor, wallTopColor, doorColor));
	}

	/**
	 * Creates the walls and doors according to the current resolution.
	 * 
	 * @param world         the game world
	 * @param wallBaseColor color of wall at base
	 * @param wallTopColor  color of wall at top
	 * @param doorColor     door color
	 */
	private void createWallsAndDoors(Color wallBaseColor, Color wallTopColor, Color doorColor) {
		WallProperties wp = new WallProperties();
		wp.baseMaterial = new PhongMaterial(wallBaseColor);
		wp.baseMaterial.setSpecularColor(wallBaseColor.brighter());
		wp.topMaterial = new PhongMaterial(wallTopColor);
		wp.brickSize = TS / resolution.get();

		var floorPlan = new FloorPlan(resolution.get(), world);
		wallsGroup.getChildren().clear();
		addHorizontalWalls(floorPlan, wallsGroup, wp);
		addVerticalWalls(floorPlan, wallsGroup, wp);
		addCorners(floorPlan, wallsGroup, wp);

		var leftDoor = new Door3D(world.ghostHouse().doorTileLeft(), true, doorColor);
		leftDoor.doorHeight.bind(wallHeight);
		var rightDoor = new Door3D(world.ghostHouse().doorTileRight(), false, doorColor);
		rightDoor.doorHeight.bind(wallHeight);
		doorsGroup.getChildren().setAll(leftDoor.getNode(), rightDoor.getNode());

		log("Built 3D maze (resolution=%d, wall height=%.2f)", resolution.get(), wallHeight.get());
	}

	private void addHorizontalWalls(FloorPlan floorPlan, Group wallsGroup, WallProperties wp) {
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

	private void addVerticalWalls(FloorPlan floorPlan, Group wallsGroup, WallProperties wp) {
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

	private void addCorners(FloorPlan floorPlan, Group wallsGroup, WallProperties wp) {
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
		Box base = new Box(numBricksX * wp.brickSize, numBricksY * wp.brickSize, wallHeight.get());
		base.depthProperty().bind(wallHeight);
		base.setMaterial(wp.baseMaterial);
		base.translateZProperty().bind(wallHeight.multiply(-0.5));
		base.drawModeProperty().bind(Env.$drawMode3D);

		double topHeight = 0.5;
		Box top = new Box(numBricksX * wp.brickSize, numBricksY * wp.brickSize, topHeight);
		top.setMaterial(wp.topMaterial);
		top.translateZProperty().bind(base.translateZProperty().subtract(wallHeight.add(topHeight + 0.1).multiply(0.5)));
		top.drawModeProperty().bind(Env.$drawMode3D);

		Group wall = new Group(base, top);
		wall.setTranslateX((x + 0.5 * numBricksX) * wp.brickSize);
		wall.setTranslateY((y + 0.5 * numBricksY) * wp.brickSize);

		wallsGroup.getChildren().add(wall);
	}
}