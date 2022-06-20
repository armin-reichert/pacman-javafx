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

import de.amr.games.pacman.lib.Logging;
import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.model.common.world.FloorPlan;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._3d.animation.RaiseAndLowerWallAnimation;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.util.U;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
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
import javafx.util.Duration;

/**
 * 3D-model for a maze. Creates walls/doors using information from the floor plan.
 * 
 * @author Armin Reichert
 */
public class Maze3D {

	public static class MazeStyle {
		public Color wallSideColor;
		public Color wallTopColor;
		public Color doorColor;
		public Image floorTexture;
		public Color floorTextureColor;
		public Color floorSolidColor;
		public Color foodColor;
	}

	public final IntegerProperty resolution = new SimpleIntegerProperty(8);
	public final DoubleProperty wallHeight = new SimpleDoubleProperty(1.0);
	public final BooleanProperty floorTextureVisible = new SimpleBooleanProperty(false);

	private final World world;
	private final Group root = new Group();
	private final Group foundationGroup = new Group();
	private final Group doorsGroup = new Group();
	private final Group foodGroup = new Group();

	private Floor3D floor;

	public Maze3D(World world, MazeStyle style) {
		this.world = world;
		root.getChildren().addAll(foundationGroup, doorsGroup, foodGroup);
		build(style);
		addFood(world, style.foodColor);
		resolution.addListener((obs, oldVal, newVal) -> build(style));
		floor.textureVisible.bind(floorTextureVisible);
	}

	public World getWorld() {
		return world;
	}

	public Group getRoot() {
		return root;
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

	public void updateDoorState(Stream<Ghost> ghosts) {
		doors().findFirst().ifPresent(firstDoor3D -> {
			var centerPosition = firstDoor3D.getCenterPosition();
			boolean openDoors = isAnyGhostGettingAccess(ghosts, centerPosition);
			doors().forEach(door3D -> door3D.setOpen(openDoors));
		});
	}

	private boolean isAnyGhostGettingAccess(Stream<Ghost> ghosts, V2d centerPosition) {
		return ghosts //
				.filter(ghost -> ghost.visible) //
				.filter(ghost -> U.oneOf(ghost.state, GhostState.DEAD, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)) //
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

	public Optional<Node> foodAt(V2i tile) {
		return foodNodes().filter(food -> tile(food).equals(tile)).findFirst();
	}

	public void hideFood(Node foodNode) {
		foodNode.setVisible(false);
		if (foodNode instanceof Energizer3D) {
			var energizer = (Energizer3D) foodNode;
			energizer.animation().stop();
		}
	}

	public void validateFoodNodes() {
		foodNodes().forEach(foodNode -> foodNode.setVisible(!world.containsEatenFood(tile(foodNode))));
	}

	private Stream<Node> foodNodes() {
		return foodGroup.getChildren().stream();
	}

	private V2i tile(Node foodNode) {
		return (V2i) foodNode.getUserData();
	}

	private Stream<Energizer3D> energizers() {
		return foodNodes().filter(Energizer3D.class::isInstance).map(Energizer3D.class::cast);
	}

	// -------------

	private static class BuildDetails {
		double brickSize;
		PhongMaterial baseMaterial;
		PhongMaterial topMaterial;
		MazeStyle mazeStyle;
	}

	public void build(MazeStyle mazeStyle) {
		var floorPlan = new FloorPlan(resolution.get(), world);

		var details = new BuildDetails();
		details.mazeStyle = mazeStyle;
		details.baseMaterial = new PhongMaterial(mazeStyle.wallSideColor);
		details.baseMaterial.setSpecularColor(mazeStyle.wallSideColor.brighter());
		details.topMaterial = new PhongMaterial(mazeStyle.wallTopColor);
		details.brickSize = TS / floorPlan.getResolution();

		foundationGroup.getChildren().clear();

		addFloor(details);
		addCorners(floorPlan, details);
		addHorizontalWalls(floorPlan, details);
		addVerticalWalls(floorPlan, details);
		addDoors(details);

		Logging.log("Built 3D maze (resolution=%d, wall height=%.2f)", floorPlan.getResolution(), wallHeight.get());
	}

	private void addFloor(BuildDetails details) {
		double width = world.numCols() * TS;
		double height = world.numRows() * TS;
		double depth = 0.05;
		floor = new Floor3D(width - 1, height - 1, depth);
		floor.getRoot().setTranslateX(0.5 * width);
		floor.getRoot().setTranslateY(0.5 * height);
		floor.getRoot().setTranslateZ(0.5 * depth);
		floor.texture.set(details.mazeStyle.floorTexture);
		floor.textureColor.set(details.mazeStyle.floorTextureColor);
		floor.solidColor.set(details.mazeStyle.floorSolidColor);
		foundationGroup.getChildren().add(floor.getRoot());
	}

	private void addDoors(BuildDetails details) {
		var leftDoor = new Door3D(world.ghostHouse().doorTileLeft(), true, details.mazeStyle.doorColor);
		leftDoor.doorHeight.bind(wallHeight);
		var rightDoor = new Door3D(world.ghostHouse().doorTileRight(), false, details.mazeStyle.doorColor);
		rightDoor.doorHeight.bind(wallHeight);
		doorsGroup.getChildren().setAll(leftDoor.getNode(), rightDoor.getNode());
	}

	private void addHorizontalWalls(FloorPlan floorPlan, BuildDetails details) {
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

	private void addVerticalWalls(FloorPlan floorPlan, BuildDetails details) {
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

	private void addCorners(FloorPlan floorPlan, BuildDetails details) {
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
	 * @param maze3D     the maze
	 * @param x          x-coordinate of top-left brick
	 * @param y          y-coordinate of top-left brick
	 * @param numBricksX number of bricks in x-direction
	 * @param numBricksY number of bricks in y-direction
	 * @param details    details for building stuff
	 */
	private void addWall(int x, int y, int numBricksX, int numBricksY, BuildDetails details) {
		Box base = new Box(numBricksX * details.brickSize, numBricksY * details.brickSize, wallHeight.get());
		base.depthProperty().bind(wallHeight);
		base.setMaterial(details.baseMaterial);
		base.translateZProperty().bind(wallHeight.multiply(-0.5));
		base.drawModeProperty().bind(Env.drawMode3D);

		double topHeight = 0.5;
		Box top = new Box(numBricksX * details.brickSize, numBricksY * details.brickSize, topHeight);
		top.setMaterial(details.topMaterial);
		top.translateZProperty().bind(base.translateZProperty().subtract(wallHeight.add(topHeight + 0.1).multiply(0.5)));
		top.drawModeProperty().bind(Env.drawMode3D);

		Group wall = new Group(base, top);
		wall.setTranslateX((x + 0.5 * numBricksX) * details.brickSize);
		wall.setTranslateY((y + 0.5 * numBricksY) * details.brickSize);

		foundationGroup.getChildren().add(wall);
	}
}