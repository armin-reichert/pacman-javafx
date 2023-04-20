/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.lib.math.Vector2i;
import de.amr.games.pacman.model.common.world.FloorPlan;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._2d.rendering.MazeColoring;
import de.amr.games.pacman.ui.fx._3d.Model3D;
import de.amr.games.pacman.ui.fx._3d.animation.FoodOscillation;
import de.amr.games.pacman.ui.fx._3d.animation.Squirting;
import de.amr.games.pacman.ui.fx.app.AppResources;
import de.amr.games.pacman.ui.fx.app.ResourceMgr;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Translate;

/**
 * 3D-model for the world in a game level. Creates walls/doors using information from the floor plan.
 * 
 * @author Armin Reichert
 */
public class World3D {

	private static final Logger LOG = LogManager.getFormatterLogger();

	private static final int MAZE_RESOLUTION = 4; // 1, 2, 4, 8 are allowed values
	private static final double FLOOR_THICKNESS = 0.25;

	private static class WallData {
		byte type;
		int x;
		int y;
		int numBricksX;
		int numBricksY;
		float brickSize;
		PhongMaterial baseMaterial;
		PhongMaterial topMaterial;
		PhongMaterial houseMaterial;
	}

	public final DoubleProperty wallHeightPy = new SimpleDoubleProperty(this, "wallHeight", 2.0);

	public final DoubleProperty wallThicknessPy = new SimpleDoubleProperty(this, "wallThickness", 1.0);

	public final ObjectProperty<String> floorTexturePy = new SimpleObjectProperty<>(this, "floorTexture",
			AppResources.KEY_NO_TEXTURE) {
		@Override
		protected void invalidated() {
			LOG.trace("Floor texture change detected");
			updateFloorMaterial(floor());
		}
	};

	public final ObjectProperty<Color> floorColorPy = new SimpleObjectProperty<>(this, "floorColor", Color.BLACK) {
		@Override
		protected void invalidated() {
			LOG.trace("Floor color change detected");
			updateFloorMaterial(floor());
		}
	};

	public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);

	private final Model3D pelletModel3D;
	private final World world;
	private final MazeColoring mazeColoring;
	private final Group root = new Group();
	private final Group floorGroup = new Group();
	private final Group wallsGroup = new Group();
	private final List<DoorWing3D> doorWings3D = new ArrayList<>();
	private final Group doorGroup = new Group();
	private final PointLight houseLight;
	private final Group foodGroup = new Group();
	private final FoodOscillation foodOscillation;

	public World3D(World world, MazeColoring mazeColoring, Model3D pelletModel3D) {
		requireNonNull(world);
		requireNonNull(mazeColoring);
		requireNonNull(pelletModel3D);

		this.world = world;
		this.mazeColoring = mazeColoring;
		this.pelletModel3D = pelletModel3D;
		this.houseLight = createGhostHouseLight();
		this.foodOscillation = new FoodOscillation(foodGroup);

		buildFloor();
		buildWorld(MAZE_RESOLUTION);
		addFood();

		root.getChildren().addAll(floorGroup, wallsGroup, doorGroup, houseLight, foodGroup);
	}

	private PointLight createGhostHouseLight() {
		var light = new PointLight();
		light.setColor(mazeColoring.wallBaseColor());
		light.setMaxRange(3 * TS);
		var center = world.house().seatPositions().get(1);
		light.setTranslateX(center.x() + HTS);
		light.setTranslateY(center.y());
		light.setTranslateZ(-TS);
		return light;
	}

	public Node getRoot() {
		return root;
	}

	public PointLight houseLighting() {
		return houseLight;
	}

	public FoodOscillation foodOscillation() {
		return foodOscillation;
	}

	private void buildFloor() {
		var sizeX = world.numCols() * TS - 1;
		var sizeY = world.numRows() * TS - 1;
		var sizeZ = FLOOR_THICKNESS;
		var floor = new Box(sizeX, sizeY, sizeZ);
		floor.drawModeProperty().bind(drawModePy);
		floorGroup.getChildren().add(floor);
		floorGroup.getTransforms().add(new Translate(0.5 * sizeX, 0.5 * sizeY, 0.5 * sizeZ));
		updateFloorMaterial(floor);
	}

	private Box floor() {
		return (Box) floorGroup.getChildren().get(0);
	}

	private void updateFloorMaterial(Box floor) {
		String key = floorTexturePy.get();
		var texture = AppResources.texture(key);
		if (texture == null) {
			texture = ResourceMgr.coloredMaterial(floorColorPy.get());
		}
		floor.setMaterial(texture);
	}

	private WallData createWallData(int resolution) {
		var wallData = new WallData();
		wallData.brickSize = (float) TS / resolution;
		wallData.baseMaterial = ResourceMgr.coloredMaterial(mazeColoring.wallBaseColor());
		wallData.topMaterial = ResourceMgr.coloredMaterial(mazeColoring.wallTopColor());
		wallData.houseMaterial = ResourceMgr.coloredMaterial(ResourceMgr.color(mazeColoring.wallBaseColor(), 0.25));
		return wallData;
	}

	private void buildWorld(int resolution) {
		LOG.info("Build 3D world...");
		var floorPlan = new FloorPlan(world, resolution);
		wallsGroup.getChildren().clear();
		addCorners(floorPlan, createWallData(resolution));
		addHorizontalWalls(floorPlan, createWallData(resolution));
		addVerticalWalls(floorPlan, createWallData(resolution));
		addHouseDoor();
		LOG.info("Done building 3D world (resolution=%d, wall height=%.2f)", floorPlan.getResolution(), wallHeightPy.get());
	}

//	private void transformMaze() {
//		if (world instanceof MapBasedWorld mapWorld) {
//			wallsGroup.getChildren().forEach(wall -> {
//				WallData data = (WallData) wall.getUserData();
//				LOG.info("Wall data type is %d", data.type);
//				var tile = tileFromFloorPlanCoord(data.x, data.y);
//				switch (data.type) {
//				case FloorPlan.HWALL -> {
//					var tileAbove = tile.plus(Direction.UP.vector());
//					if (mapWorld.isWall(tileAbove)) {
//						wall.getTransforms().add(new Translate(0, -2));
//					} else {
////						wall.getTransforms().add(new Translate(0, 2));
//					}
//				}
//				case FloorPlan.VWALL -> {
//
//				}
//				case FloorPlan.CORNER -> {
//
//				}
//				}
//			});
//		}
//	}
//
//	private Vector2i tileFromFloorPlanCoord(int fx, int fy) {
//		return new Vector2i(fx / MAZE_RESOLUTION, fy / MAZE_RESOLUTION);
//	}

	public Stream<DoorWing3D> doorWings3D() {
		return doorWings3D.stream().map(DoorWing3D.class::cast);
	}

	private void addHouseDoor() {
		addDoorWing(world.house().door().leftWing());
		addDoorWing(world.house().door().rightWing());
	}

	private void addDoorWing(Vector2i tile) {
		var wing3D = new DoorWing3D(tile, mazeColoring.houseDoorColor());
		wing3D.doorHeightPy.set(6.0);
		wing3D.getRoot().drawModeProperty().bind(drawModePy);
		doorWings3D.add(wing3D);
		doorGroup.getChildren().add(wing3D.getRoot());
	}

	private void addHorizontalWalls(FloorPlan floorPlan, WallData wallData) {
		wallData.type = FloorPlan.HWALL;
		wallData.numBricksY = 1;
		for (int y = 0; y < floorPlan.sizeY(); ++y) {
			wallData.x = -1;
			wallData.y = y;
			wallData.numBricksX = 0;
			for (int x = 0; x < floorPlan.sizeX(); ++x) {
				if (floorPlan.get(x, y) == FloorPlan.HWALL) {
					if (wallData.numBricksX == 0) {
						wallData.x = x;
					}
					wallData.numBricksX++;
				} else if (wallData.numBricksX > 0) {
					addCompositeWall(floorPlan, wallData);
					wallData.numBricksX = 0;
				}
			}
			if (wallData.numBricksX > 0 && y == floorPlan.sizeY() - 1) {
				addCompositeWall(floorPlan, wallData);
			}
		}
	}

	private void addVerticalWalls(FloorPlan floorPlan, WallData wallData) {
		wallData.type = FloorPlan.VWALL;
		wallData.numBricksX = 1;
		for (int x = 0; x < floorPlan.sizeX(); ++x) {
			wallData.x = x;
			wallData.y = -1;
			wallData.numBricksY = 0;
			for (int y = 0; y < floorPlan.sizeY(); ++y) {
				if (floorPlan.get(x, y) == FloorPlan.VWALL) {
					if (wallData.numBricksY == 0) {
						wallData.y = y;
					}
					wallData.numBricksY++;
				} else if (wallData.numBricksY > 0) {
					addCompositeWall(floorPlan, wallData);
					wallData.numBricksY = 0;
				}
			}
			if (wallData.numBricksY > 0 && x == floorPlan.sizeX() - 1) {
				addCompositeWall(floorPlan, wallData);
			}
		}
	}

	private void addCorners(FloorPlan floorPlan, WallData wallData) {
		wallData.type = FloorPlan.CORNER;
		wallData.numBricksX = 1;
		wallData.numBricksY = 1;
		for (int x = 0; x < floorPlan.sizeX(); ++x) {
			for (int y = 0; y < floorPlan.sizeY(); ++y) {
				if (floorPlan.get(x, y) == FloorPlan.CORNER) {
					wallData.x = x;
					wallData.y = y;
					addCompositeWall(floorPlan, wallData);
				}
			}
		}
	}

	private void addCompositeWall(FloorPlan floorPlan, WallData wallData) {
		final double topHeight = 0.5;
		final double ghostHouseHeight = 9.0;
		final Vector2i tile = floorPlan.tile(wallData.x, wallData.y);
		final boolean ghostHouseWall = world.house().contains(tile);

		var base = switch (wallData.type) {
		case FloorPlan.HWALL -> horizontalWall(wallData);
		case FloorPlan.VWALL -> verticalWall(wallData);
		case FloorPlan.CORNER -> corner();
		default -> throw new IllegalStateException();
		};
		if (ghostHouseWall) {
			base.setDepth(ghostHouseHeight);
			base.setTranslateZ(-ghostHouseHeight / 2);
			base.setMaterial(wallData.houseMaterial);
		} else {
			base.depthProperty().bind(wallHeightPy);
			base.translateZProperty().bind(wallHeightPy.multiply(-0.5));
			base.setMaterial(wallData.baseMaterial);
		}

		var top = switch (wallData.type) {
		case FloorPlan.HWALL -> horizontalWall(wallData);
		case FloorPlan.VWALL -> verticalWall(wallData);
		case FloorPlan.CORNER -> corner();
		default -> throw new IllegalStateException();
		};
		top.setMaterial(wallData.topMaterial);
		if (ghostHouseWall) {
			top.setDepth(topHeight);
			top.setTranslateZ(-ghostHouseHeight - 0.2);
		} else {
			top.setDepth(topHeight);
			top.translateZProperty()
					.bind(base.translateZProperty().subtract(wallHeightPy.add(topHeight + 0.1).multiply(0.5)));
		}

		var wall = new Group(base, top);
		wall.setTranslateX((wallData.x + 0.5 * wallData.numBricksX) * wallData.brickSize);
		wall.setTranslateY((wallData.y + 0.5 * wallData.numBricksY) * wallData.brickSize);
		wall.setUserData(wallData);

		wallsGroup.getChildren().add(wall);
	}

	private Box horizontalWall(WallData wallData) {
		Box wall = new Box();
		// without ...+1 there are gaps. why?
		wall.setWidth((wallData.numBricksX + 1) * wallData.brickSize);
		wall.heightProperty().bind(wallThicknessPy);
		wall.drawModeProperty().bind(drawModePy);
		return wall;
	}

	private Box verticalWall(WallData wallData) {
		Box wall = new Box();
		wall.widthProperty().bind(wallThicknessPy);
		// without ...+1 there are gaps. why?
		wall.setHeight((wallData.numBricksY + 1) * wallData.brickSize);
		wall.drawModeProperty().bind(drawModePy);
		return wall;
	}

	private Box corner() {
		Box corner = new Box();
		corner.widthProperty().bind(wallThicknessPy);
		corner.heightProperty().bind(wallThicknessPy);
		corner.drawModeProperty().bind(drawModePy);
		return corner;
	}

	// Food

	private void addFood() {
		var foodMaterial = ResourceMgr.coloredMaterial(mazeColoring.foodColor());
		world.tiles().filter(world::containsFood).forEach(tile -> {
			var food3D = world.isEnergizerTile(tile)//
					? createEnergizer3D(tile, foodMaterial)//
					: createNormalPellet3D(tile, foodMaterial);
			foodGroup.getChildren().add(food3D.getRoot());
		});
	}

	private Pellet3D createNormalPellet3D(Vector2i tile, PhongMaterial material) {
		var pellet3D = new Pellet3D(pelletModel3D, 1.0);
		pellet3D.getRoot().setMaterial(material);
		pellet3D.placeAtTile(tile);
		return pellet3D;
	}

	private Energizer3D createEnergizer3D(Vector2i tile, PhongMaterial material) {
		var energizer3D = new Energizer3D(3.5);
		energizer3D.getRoot().setMaterial(material);
		energizer3D.placeAtTile(tile);
		var squirting = new Squirting(root) {
			@Override
			protected boolean reachesEndPosition(Drop drop) {
				return drop.getTranslateZ() >= -1 && world.insideBounds(drop.getTranslateX(), drop.getTranslateY());
			}
		};
		squirting.setOrigin(energizer3D.getRoot());
		squirting.setDropCountMin(15);
		squirting.setDropCountMax(45);
		squirting.setDropMaterial(ResourceMgr.coloredMaterial(mazeColoring.foodColor().desaturate()));
		energizer3D.setEatenAnimation(squirting);
		return energizer3D;
	}

	/**
	 * @return all 3D pellets, including energizers
	 */
	public Stream<Eatable3D> eatables3D() {
		return foodGroup.getChildren().stream().map(Node::getUserData).map(Eatable3D.class::cast);
	}

	public Stream<Energizer3D> energizers3D() {
		return eatables3D().filter(Energizer3D.class::isInstance).map(Energizer3D.class::cast);
	}

	public Optional<Eatable3D> eatableAt(Vector2i tile) {
		requireNonNull(tile);
		return eatables3D().filter(eatable -> eatable.tile().equals(tile)).findFirst();
	}

	public void logFood() {
		LOG.info("Food: %d energizers, %d pellets total", energizers3D().count(), eatables3D().count());
		eatables3D().forEach(LOG::info);
	}
}