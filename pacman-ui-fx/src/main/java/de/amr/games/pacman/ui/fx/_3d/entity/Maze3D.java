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

import static de.amr.games.pacman.model.common.actors.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.model.common.world.World.TS;

import java.util.Optional;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.model.common.world.FloorPlan;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._3d.animation.RaiseAndLowerWallAnimation;
import de.amr.games.pacman.ui.fx.app.Env;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.PauseTransition;
import javafx.animation.Transition;
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
import javafx.scene.shape.Shape3D;
import javafx.util.Duration;

/**
 * 3D-model for a maze. Creates walls/doors using information from the floor plan.
 * 
 * @author Armin Reichert
 */
public class Maze3D extends Group {

	private static final Logger logger = LogManager.getFormatterLogger();

	public static class MazeStyle {
		public Color wallSideColor;
		public Color wallTopColor;
		public Color doorColor;
		public Color foodColor;
	}

	private static class WallData {
		double brickSize;
		double height;
		PhongMaterial baseMaterial;
		PhongMaterial topMaterial;
	}

	private static class FoodDisappearTransition extends Transition {

		private Shape3D foodShape;
		private PhongMaterial foodMaterial;
		private Color foodColor;

		public FoodDisappearTransition(Shape3D foodShape, Color foodColor) {
			setCycleDuration(Duration.seconds(0.6));
			setOnFinished(e -> foodShape.setVisible(false));
			setInterpolator(Interpolator.EASE_BOTH);
			this.foodShape = foodShape;
			this.foodColor = foodColor;
			foodMaterial = new PhongMaterial(foodColor);
			foodShape.setMaterial(foodMaterial);
		}

		@Override
		protected void interpolate(double t) {
			var color = foodColor.interpolate(Color.LIGHTGREY, t);
			foodMaterial.setDiffuseColor(color);
			foodMaterial.setSpecularColor(color.brighter());
			foodShape.setScaleX((1 - t));
			foodShape.setScaleY((1 - t));
			foodShape.setScaleZ((1 - t));
		}
	}

	public final IntegerProperty resolution = new SimpleIntegerProperty(8);
	public final DoubleProperty wallHeight = new SimpleDoubleProperty(1.0);

	private final World world;
	private final Group foundationGroup = new Group();
	private final Group wallsGroup = new Group();
	private final Group doorsGroup = new Group();
	private final Group foodGroup = new Group();
	private final MazeStyle mazeStyle;

	public Maze3D(World world, MazeStyle mazeStyle) {
		this.world = world;
		this.mazeStyle = mazeStyle;
		getChildren().addAll(foundationGroup, foodGroup);
		foundationGroup.getChildren().addAll(createFloor(), wallsGroup, doorsGroup);
		build(mazeStyle);
		addFood(world, mazeStyle.foodColor);
		resolution.addListener((obs, oldVal, newVal) -> build(mazeStyle));
	}

	private Node createFloor() {
		double width = (double) world.numCols() * TS;
		double height = (double) world.numRows() * TS;
		double depth = 0.05;
		var floor = new Box(width - 1, height - 1, depth);
		floor.setTranslateX(0.5 * width);
		floor.setTranslateY(0.5 * height);
		floor.setTranslateZ(0.5 * depth);
		floor.drawModeProperty().bind(Env.drawMode3D);
		return floor;
	}

	private Box getFloor() {
		return (Box) foundationGroup.getChildren().get(0);
	}

	public void build(MazeStyle mazeStyle) {
		var floorPlan = new FloorPlan(resolution.get(), world);

		var wallData = new WallData();
		wallData.baseMaterial = new PhongMaterial(mazeStyle.wallSideColor);
		wallData.baseMaterial.setSpecularColor(mazeStyle.wallSideColor.brighter());
		wallData.topMaterial = new PhongMaterial(mazeStyle.wallTopColor);
		wallData.brickSize = (double) TS / floorPlan.getResolution();
		wallData.height = wallHeight.get();

		wallsGroup.getChildren().clear();
		addCorners(floorPlan, wallData);
		addHorizontalWalls(floorPlan, wallData);
		addVerticalWalls(floorPlan, wallData);

		doorsGroup.getChildren().clear();
		var leftDoor = createDoor(world.ghostHouse().doorTileLeft(), mazeStyle.doorColor);
		var rightDoor = createDoor(world.ghostHouse().doorTileRight(), mazeStyle.doorColor);
		doorsGroup.getChildren().setAll(leftDoor, rightDoor);

		logger.info("Built 3D maze (resolution=%d, wall height=%.2f)", floorPlan.getResolution(), wallData.height);
	}

	public void setFloorTexture(Image texture, Color color) {
		var mat = new PhongMaterial();
		mat.setDiffuseColor(color);
		mat.setSpecularColor(color.brighter());
		if (texture != null) {
			mat.setDiffuseMap(texture);
		}
		getFloor().setMaterial(mat);
	}

	public World getWorld() {
		return world;
	}

	public Stream<Door3D> doors() {
		return doorsGroup.getChildren().stream().map(Node::getUserData).map(Door3D.class::cast);
	}

	public Animation createMazeFlashingAnimation(int times) {
		return times > 0 ? new RaiseAndLowerWallAnimation(times) : new PauseTransition(Duration.seconds(1));
	}

	public void reset() {
		energizerAnimations().forEach(Animation::stop);
		energizers().forEach(node -> {
			node.setScaleX(1.0);
			node.setScaleY(1.0);
			node.setScaleZ(1.0);
		});
	}

	public void updateDoorState(Stream<Ghost> ghosts, V2d doorsCenter) {
		doors().findFirst().ifPresent(firstDoor3D -> {
			boolean openDoors = isAnyGhostGettingAccess(ghosts, doorsCenter);
			doors().forEach(door3D -> door3D.setOpen(openDoors));
		});
	}

	private boolean isAnyGhostGettingAccess(Stream<Ghost> ghosts, V2d centerPosition) {
		return ghosts //
				.filter(ghost -> ghost.visible) //
				.filter(ghost -> ghost.is(GhostState.DEAD, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)) //
				.anyMatch(ghost -> isGhostGettingAccess(ghost, centerPosition));
	}

	private boolean isGhostGettingAccess(Ghost ghost, V2d doorCenter) {
		return ghost.position.euclideanDistance(doorCenter) <= (ghost.is(LEAVING_HOUSE) ? TS : 3 * TS);
	}

	private void addFood(World world, Color foodColor) {
		var meatBall = new PhongMaterial(foodColor);
		world.tiles() //
				.filter(world::isFoodTile) //
				.map(tile -> world.isEnergizerTile(tile) //
						? new Energizer3D(tile, meatBall, 3.0)
						: new Pellet3D(tile, meatBall, 1.0))
				.forEach(foodGroup.getChildren()::add);
	}

	public Stream<Animation> energizerAnimations() {
		return energizers().map(Energizer3D::animation);
	}

	public Optional<Shape3D> foodAt(V2i tile) {
		return foodShapes().filter(food -> tile(food).equals(tile)).findFirst();
	}

	public void hideFood(Shape3D foodShape) {
		if (foodShape instanceof Energizer3D) {
			var energizer = (Energizer3D) foodShape;
			energizer.animation().stop();
			foodShape.setVisible(false);
			return;
		}
		new FoodDisappearTransition(foodShape, mazeStyle.foodColor).play();
	}

	public void validateFoodShapes() {
		foodShapes().forEach(food -> food.setVisible(!world.containsEatenFood(tile(food))));
	}

	private Stream<Shape3D> foodShapes() {
		return foodGroup.getChildren().stream().map(Shape3D.class::cast);
	}

	private V2i tile(Node foodNode) {
		return (V2i) foodNode.getUserData();
	}

	private Stream<Energizer3D> energizers() {
		return foodShapes().filter(Energizer3D.class::isInstance).map(Energizer3D.class::cast);
	}

	// -------------------------------------------------------------------------------------------

	private Door3D createDoor(V2i tile, Color color) {
		var door = new Door3D(tile, color);
		door.doorHeight.bind(wallHeight);
		return door;
	}

	private void addHorizontalWalls(FloorPlan floorPlan, WallData wallData) {
		for (int y = 0; y < floorPlan.sizeY(); ++y) {
			int wallStart = -1;
			int wallSize = 0;
			for (int x = 0; x < floorPlan.sizeX(); ++x) {
				if (floorPlan.get(x, y) == FloorPlan.HWALL) {
					if (wallSize > 0) {
						wallSize++;
					} else {
						wallStart = x;
						wallSize = 1;
					}
				} else if (wallSize > 0) {
					addWall(wallStart, y, wallSize, 1, wallData);
					wallSize = 0;
				}
			}
			if (wallSize > 0 && y == floorPlan.sizeY() - 1) {
				addWall(wallStart, y, wallSize, 1, wallData);
			}
		}
	}

	private void addVerticalWalls(FloorPlan floorPlan, WallData wallData) {
		for (int x = 0; x < floorPlan.sizeX(); ++x) {
			int wallStart = -1;
			int wallSize = 0;
			for (int y = 0; y < floorPlan.sizeY(); ++y) {
				if (floorPlan.get(x, y) == FloorPlan.VWALL) {
					if (wallSize > 0) {
						wallSize++;
					} else {
						wallStart = y;
						wallSize = 1;
					}
				} else if (wallSize > 0) {
					addWall(x, wallStart, 1, wallSize, wallData);
					wallSize = 0;
				}
			}
			if (wallSize > 0 && x == floorPlan.sizeX() - 1) {
				addWall(x, wallStart, 1, wallSize, wallData);
			}
		}
	}

	private void addCorners(FloorPlan floorPlan, WallData wallData) {
		for (int x = 0; x < floorPlan.sizeX(); ++x) {
			for (int y = 0; y < floorPlan.sizeY(); ++y) {
				if (floorPlan.get(x, y) == FloorPlan.CORNER) {
					addWall(x, y, 1, 1, wallData);
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
	 * @param data       data on how the wall look like
	 */
	private void addWall(int x, int y, int numBricksX, int numBricksY, WallData data) {
		Box base = new Box();
		base.setWidth(numBricksX * data.brickSize);
		base.setHeight(numBricksY * data.brickSize);
		base.depthProperty().bind(wallHeight);
		base.translateZProperty().bind(wallHeight.multiply(-0.5));
		base.setMaterial(data.baseMaterial);
		base.drawModeProperty().bind(Env.drawMode3D);

		double topHeight = 0.1;
		Box top = new Box();
		top.setWidth(numBricksX * data.brickSize);
		top.setHeight(numBricksY * data.brickSize);
		top.setDepth(topHeight);
		top.translateZProperty().bind(base.translateZProperty().subtract(wallHeight.add(topHeight + 0.1).multiply(0.5)));
		top.setMaterial(data.topMaterial);
		top.drawModeProperty().bind(Env.drawMode3D);

		var wall = new Group(base, top);
		wall.setTranslateX((x + 0.5 * numBricksX) * data.brickSize);
		wall.setTranslateY((y + 0.5 * numBricksY) * data.brickSize);

		wallsGroup.getChildren().add(wall);
	}
}