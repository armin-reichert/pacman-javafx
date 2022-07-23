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

import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.world.FloorPlan;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._3d.animation.RaiseAndLowerWallAnimation;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.util.Duration;

/**
 * 3D-model for a maze. Creates walls/doors using information from the floor plan.
 * 
 * @author Armin Reichert
 */
public class Maze3D extends Group {

	public record MazeColors(Color wallBaseColor, Color wallTopColor, Color doorColor) {
	}

	private record WallData(double brickSize, double wallHeight, PhongMaterial baseMaterial, PhongMaterial topMaterial) {
	}

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	private static final double FLOOR_THICKNESS = 0.1;

	public final IntegerProperty resolutionPy = new SimpleIntegerProperty(4) {
		@Override
		protected void invalidated() {
			build();
		}
	};

	public final DoubleProperty wallHeightPy = new SimpleDoubleProperty(2.0);

	public final DoubleProperty wallThicknessPy = new SimpleDoubleProperty(1.0);

	public final ObjectProperty<Image> floorTexturePy = new SimpleObjectProperty<>() {
		@Override
		protected void invalidated() {
			updateFloorMaterial();
		}
	};

	public final ObjectProperty<Color> floorColorPy = new SimpleObjectProperty<>(Color.BLACK) {
		@Override
		protected void invalidated() {
			updateFloorMaterial();
		}
	};

	public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(DrawMode.FILL);

	private final World world;
	private final MazeColors mazeColors;
	private final Group wallsGroup = new Group();
	private final Group doorsGroup = new Group();
	private Box floor;

	public Maze3D(World world, MazeColors mazeColors) {
		this.world = world;
		this.mazeColors = mazeColors;
		createFloor();
		getChildren().addAll(floor, wallsGroup, doorsGroup);
		build();
	}

	public Animation createMazeFlashingAnimation(int times) {
		return times > 0 ? new RaiseAndLowerWallAnimation(times) : new PauseTransition(Duration.seconds(1));
	}

	private void createFloor() {
		double width = (double) world.numCols() * TS - 1;
		double height = (double) world.numRows() * TS - 1;
		double depth = FLOOR_THICKNESS;
		floor = new Box(width, height, depth);
		floor.setTranslateX(0.5 * width);
		floor.setTranslateY(0.5 * height);
		floor.setTranslateZ(0.5 * depth);
		floor.drawModeProperty().bind(drawModePy);
		updateFloorMaterial();
	}

	private void updateFloorMaterial() {
		var material = coloredMaterial(floorColorPy.get());
		material.setDiffuseMap(floorTexturePy.get());
		floor.setMaterial(material);
	}

	private PhongMaterial coloredMaterial(Color diffuseColor) {
		var material = new PhongMaterial(diffuseColor);
		material.setSpecularColor(diffuseColor.brighter());
		return material;
	}

	private void build() {
		var resolution = resolutionPy.get();
		var floorPlan = new FloorPlan(world, resolution);
		var brickSize = (double) TS / resolution;
		var wallHeight = wallHeightPy.get();
		var wallData = new WallData(brickSize, wallHeight, coloredMaterial(mazeColors.wallBaseColor),
				coloredMaterial(mazeColors.wallTopColor));

		wallsGroup.getChildren().clear();
		addCorners(floorPlan, wallData);
		addHorizontalWalls(floorPlan, wallData);
		addVerticalWalls(floorPlan, wallData);

		var doors = world.ghostHouse().doorTiles().map(this::createDoor).toList();
		doorsGroup.getChildren().setAll(doors);

		LOGGER.info("Built 3D maze (resolution=%d, wall height=%.2f)", floorPlan.getResolution(), wallData.wallHeight);
	}

	public Stream<Door3D> doors() {
		return doorsGroup.getChildren().stream().map(Door3D.class::cast);
	}

	private Door3D createDoor(V2i tile) {
		var door = new Door3D(tile, mazeColors.doorColor);
		door.doorHeightPy.bind(wallHeightPy);
		return door;
	}

	private void addHorizontalWalls(FloorPlan floorPlan, WallData wallData) {
		for (int y = 0; y < floorPlan.sizeY(); ++y) {
			int wallStart = -1;
			int wallSize = 0;
			for (int x = 0; x < floorPlan.sizeX(); ++x) {
				if (floorPlan.get(x, y) == FloorPlan.HWALL) {
					if (wallSize == 0) {
						wallStart = x;
					}
					wallSize++;
				} else if (wallSize > 0) {
					addCompositeWall(wallStart, y, wallSize, 1, wallData, FloorPlan.HWALL);
					wallSize = 0;
				}
			}
			if (wallSize > 0 && y == floorPlan.sizeY() - 1) {
				addCompositeWall(wallStart, y, wallSize, 1, wallData, FloorPlan.HWALL);
			}
		}
	}

	private void addVerticalWalls(FloorPlan floorPlan, WallData wallData) {
		for (int x = 0; x < floorPlan.sizeX(); ++x) {
			int wallStart = -1;
			int wallSize = 0;
			for (int y = 0; y < floorPlan.sizeY(); ++y) {
				if (floorPlan.get(x, y) == FloorPlan.VWALL) {
					if (wallSize == 0) {
						wallStart = y;
					}
					wallSize++;
				} else if (wallSize > 0) {
					addCompositeWall(x, wallStart, 1, wallSize, wallData, FloorPlan.VWALL);
					wallSize = 0;
				}
			}
			if (wallSize > 0 && x == floorPlan.sizeX() - 1) {
				addCompositeWall(x, wallStart, 1, wallSize, wallData, FloorPlan.VWALL);
			}
		}
	}

	private void addCorners(FloorPlan floorPlan, WallData wallData) {
		for (int x = 0; x < floorPlan.sizeX(); ++x) {
			for (int y = 0; y < floorPlan.sizeY(); ++y) {
				if (floorPlan.get(x, y) == FloorPlan.CORNER) {
					addCompositeWall(x, y, 1, 1, wallData, FloorPlan.CORNER);
				}
			}
		}
	}

	private Box horizontalWall(int numBricksX, double brickSize) {
		Box wall = new Box();
		wall.setWidth(numBricksX * brickSize);
		wall.heightProperty().bind(wallThicknessPy);
		return wall;
	}

	private Box verticalWall(int numBricksY, double brickSize) {
		Box wall = new Box();
		wall.widthProperty().bind(wallThicknessPy);
		wall.setHeight(numBricksY * brickSize);
		return wall;
	}

	private Box corner() {
		Box corner = new Box();
		corner.widthProperty().bind(wallThicknessPy);
		corner.heightProperty().bind(wallThicknessPy);
		return corner;
	}

	private void addCompositeWall(int x, int y, int numBricksX, int numBricksY, WallData data, byte type) {
		// without ...+1 there are gaps. why?
		var base = switch (type) {
		case FloorPlan.HWALL -> horizontalWall(numBricksX + 1, data.brickSize);
		case FloorPlan.VWALL -> verticalWall(numBricksY + 1, data.brickSize);
		case FloorPlan.CORNER -> corner();
		default -> throw new IllegalStateException();
		};
		base.depthProperty().bind(wallHeightPy);
		base.translateZProperty().bind(wallHeightPy.multiply(-0.5));
		base.setMaterial(data.baseMaterial);
		base.drawModeProperty().bind(drawModePy);

		var top = switch (type) {
		case FloorPlan.HWALL -> horizontalWall(numBricksX + 1, data.brickSize);
		case FloorPlan.VWALL -> verticalWall(numBricksY + 1, data.brickSize);
		case FloorPlan.CORNER -> corner();
		default -> throw new IllegalStateException();
		};
		double topHeight = 0.1;
		top.setDepth(topHeight);
		top.translateZProperty().bind(base.translateZProperty().subtract(wallHeightPy.add(topHeight + 0.1).multiply(0.5)));
		top.setMaterial(data.topMaterial);
		top.drawModeProperty().bind(drawModePy);

		var wall = new Group(base, top);
		wall.setTranslateX((x + 0.5 * numBricksX) * data.brickSize);
		wall.setTranslateY((y + 0.5 * numBricksY) * data.brickSize);

		wallsGroup.getChildren().add(wall);
	}
}