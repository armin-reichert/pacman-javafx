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

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.world.World.HTS;
import static de.amr.games.pacman.model.world.World.TS;
import static de.amr.games.pacman.ui.fx._3d.entity.Maze3D.NodeInfo.info;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.model.world.FloorPlan;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.app.Env;
import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.ScaleTransition;
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
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

/**
 * 3D-model for a maze. Creates walls/doors using information from the floor plan.
 * 
 * @author Armin Reichert
 */
public class Maze3D extends Group {

	public static class NodeInfo {

		public final boolean energizer;
		public final V2i tile;

		public NodeInfo(boolean energizer, V2i tile) {
			this.energizer = energizer;
			this.tile = tile;
		}

		public static NodeInfo info(Node node) {
			return (NodeInfo) node.getUserData();
		}
	}

	public final DoubleProperty $wallHeight = new SimpleDoubleProperty(2.0);
	public final IntegerProperty $resolution = new SimpleIntegerProperty(8);

	private final Box floor;
	private final Group wallsGroup = new Group();
	private final Group doorsGroup = new Group();
	private final Group foodGroup = new Group();

	private double energizerRadius = 2.5;
	private double pelletRadius = 1;
	private Color floorColor = Color.rgb(20, 20, 120);
	private Color doorClosedColor = Color.PINK;
	private Color doorOpenColor = Color.TRANSPARENT;

	private Animation[] energizerAnimations;

	/**
	 * Creates the 3D-maze without walls, doors or food.
	 * 
	 * @param sizeX maze x-size
	 * @param sizeY maze y-size
	 */
	public Maze3D(double sizeX, double sizeY, Image floorImage) {
		var sizeZ = 0.1;
		var floorMaterial = new PhongMaterial(floorColor);
		floorMaterial.setSpecularColor(floorColor.brighter());
		floorMaterial.setDiffuseMap(floorImage);
		floor = new Box(sizeX - 1, sizeY - 1, sizeZ);
		floor.setMaterial(floorMaterial);
		floor.getTransforms().add(new Translate(0.5 * sizeX, 0.5 * sizeY, 0.5 * sizeZ));
		floor.drawModeProperty().bind(Env.$drawMode3D);
		Group wallsAndDoors = new Group(wallsGroup, doorsGroup);
		getChildren().addAll(floor, wallsAndDoors, foodGroup);
	}

	/**
	 * Creates the walls and doors according to the current resolution.
	 * 
	 * @param world         the game world
	 * @param wallBaseColor color of wall at base
	 * @param wallTopColor  color of wall at top
	 */
	public void buildWallsAndDoors(World world, Color wallBaseColor, Color wallTopColor) {
		int stoneSize = TS / $resolution.get();
		FloorPlan floorPlan = new FloorPlan($resolution.get(), world);
		rebuildWalls(floorPlan, world, stoneSize, wallBaseColor, wallTopColor);
		rebuildDoors(world, stoneSize);
		log("Rebuilt 3D maze: resolution=%d (stone size=%d), wall height=%.2f", $resolution.get(), stoneSize,
				$wallHeight.get());
	}

	/**
	 * Creates the pellets/food and the energizer animations.
	 * 
	 * @param world     the game world
	 * @param foodColor color of pellets
	 */
	public void buildFood(World world, Color foodColor) {
		var material = new PhongMaterial(foodColor);
		var pellets = world.tiles().filter(world::isFoodTile)
				.map(tile -> createPellet(tile, world.isEnergizerTile(tile), material)).collect(Collectors.toList());
		foodGroup.getChildren().setAll(pellets);
		energizerAnimations = energizerNodes().map(this::createEnergizerAnimation).toArray(Animation[]::new);
	}

	private Animation createEnergizerAnimation(Node energizer) {
		var animation = new ScaleTransition(Duration.seconds(1.0 / 6), energizer);
		animation.setAutoReverse(true);
		animation.setCycleCount(Transition.INDEFINITE);
		animation.setFromX(1.0);
		animation.setFromY(1.0);
		animation.setFromZ(1.0);
		animation.setToX(0.1);
		animation.setToY(0.1);
		animation.setToZ(0.1);
		return animation;
	}

	public void startEnergizerAnimations() {
		if (Stream.of(energizerAnimations).anyMatch(animation -> animation.getStatus() != Status.RUNNING)) {
			Stream.of(energizerAnimations).forEach(Animation::play);
		}
	}

	public void playEnergizerAnimations() {
		Stream.of(energizerAnimations).forEach(Animation::play);
	}

	public void stopEnergizerAnimations() {
		Stream.of(energizerAnimations).forEach(Animation::stop);
	}

	public Animation flashingAnimation(int times) {
		return new Transition() {

			private double startWallHeight;

			{
				startWallHeight = Env.$mazeWallHeight.get();
				setCycleDuration(Duration.seconds(0.25));
				setCycleCount(2 * times);
				setAutoReverse(true);
				setOnFinished(e -> Env.$mazeWallHeight.set(startWallHeight));
			}

			@Override
			protected void interpolate(double t) {
				Env.$mazeWallHeight.set(Math.cos(t * Math.PI / 2) * startWallHeight);
			}
		};
	}

	private Sphere createPellet(V2i tile, boolean energizer, PhongMaterial material) {
		var pellet = new Sphere(energizer ? energizerRadius : pelletRadius);
		pellet.setMaterial(material);
		pellet.setTranslateX(tile.x * TS + HTS);
		pellet.setTranslateY(tile.y * TS + HTS);
		pellet.setTranslateZ(-3);
		pellet.setUserData(new NodeInfo(energizer, tile));
		return pellet;
	}

	public Stream<Shape3D> doors() {
		return doorsGroup.getChildren().stream().map(node -> (Shape3D) node);
	}

	public void updateGhostHouseDoorState(GameModel game) {
		boolean open = doors().anyMatch(door -> isAnyGhostPassingDoor(game, door));
		PhongMaterial material = new PhongMaterial(open ? doorOpenColor : doorClosedColor);
		doors().forEach(door -> door.setMaterial(material));
	}

	private boolean isAnyGhostPassingDoor(GameModel game, Shape3D door) {
		return game.ghosts().filter(ghost -> ghost.is(GhostState.ENTERING_HOUSE) || ghost.is(GhostState.LEAVING_HOUSE))
				.anyMatch(ghost -> isNodeAtTile(door, ghost.tile()) || isNodeAtTile(door, ghost.tile().plus(0, 1)));
	}

	public Stream<Node> foodNodes() {
		return foodGroup.getChildren().stream();
	}

	public Optional<Node> foodNodeAt(V2i tile) {
		return foodNodes().filter(node -> isNodeAtTile(node, tile)).findFirst();
	}

	public Stream<Node> energizerNodes() {
		return foodNodes().filter(node -> info(node).energizer);
	}

	public Optional<Node> energizerNodeAt(V2i tile) {
		return energizerNodes().filter(node -> isNodeAtTile(node, tile)).findFirst();
	}

	private boolean isNodeAtTile(Node node, V2i tile) {
		return info(node).tile.equals(tile);
	}

	public void resetEnergizerSize() {
		energizerNodes().forEach(node -> {
			node.setScaleX(1.0);
			node.setScaleY(1.0);
			node.setScaleZ(1.0);
		});
	}

	/**
	 * Adds a wall at given position. A wall consists of a base and a top part which can have different color and
	 * material.
	 * 
	 * @param leftX      x-coordinate of top-left stone
	 * @param topY       y-coordinate of top-left stone
	 * @param numStonesX number of stones in x-direction
	 * @param numStonesY number of stones in y-direction
	 * @param stoneSize  size of a single stone
	 * @return pair of walls (base, top)
	 */
	private Group addWall(int leftX, int topY, int numStonesX, int numStonesY, double stoneSize,
			PhongMaterial wallBaseMaterial, PhongMaterial wallTopMaterial) {

		Box base = new Box(numStonesX * stoneSize, numStonesY * stoneSize, $wallHeight.get());
		base.depthProperty().bind($wallHeight);
		base.setMaterial(wallBaseMaterial);
		base.translateZProperty().bind($wallHeight.multiply(-0.5));
		base.drawModeProperty().bind(Env.$drawMode3D);

		double topHeight = 0.5;
		Box top = new Box(numStonesX * stoneSize, numStonesY * stoneSize, topHeight);
		top.setMaterial(wallTopMaterial);
		top.translateZProperty().bind(base.translateZProperty().subtract($wallHeight.add(topHeight + 0.1).multiply(0.5)));
		top.drawModeProperty().bind(Env.$drawMode3D);

		Group wall = new Group(base, top);
		wall.setTranslateX((leftX + 0.5 * numStonesX) * stoneSize);
		wall.setTranslateY((topY + 0.5 * numStonesY) * stoneSize);
		wallsGroup.getChildren().add(wall);
		return wall;
	}

	private Group addCorner(int x, int y, double blockSize, PhongMaterial wallBaseMaterial,
			PhongMaterial wallTopMaterial) {
		return addWall(x, y, 1, 1, blockSize, wallBaseMaterial, wallTopMaterial);
	}

	private void rebuildDoors(World world, double stoneSize) {
		doorsGroup.getChildren().clear();
		PhongMaterial doorMaterial = new PhongMaterial(doorClosedColor);
		world.ghostHouse().doorTiles().forEach(tile -> {
			Box door = new Box(TS - 1, 1, HTS);
			door.setMaterial(doorMaterial);
			door.setTranslateX(tile.x * TS + HTS);
			door.setTranslateY(tile.y * TS + HTS);
			door.setTranslateZ(-HTS / 2);
			NodeInfo info = new NodeInfo(false, tile);
			door.setUserData(info);
			door.drawModeProperty().bind(Env.$drawMode3D);
			doorsGroup.getChildren().add(door);
		});
	}

	private void rebuildWalls(FloorPlan floorPlan, World world, double stoneSize, Color wallBaseColor,
			Color wallTopColor) {

		var wallBaseMaterial = new PhongMaterial(wallBaseColor);
		wallBaseMaterial.setSpecularColor(wallBaseColor.brighter());
		var wallTopMaterial = new PhongMaterial(wallTopColor);
		wallTopMaterial.setDiffuseColor(wallTopColor);

		wallsGroup.getChildren().clear();

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
						addWall(leftX, y, sizeX, 1, stoneSize, wallBaseMaterial, wallTopMaterial);
						leftX = -1;
					}
				}
				if (x == floorPlan.sizeX() - 1 && leftX != -1) {
					addWall(leftX, y, sizeX, 1, stoneSize, wallBaseMaterial, wallTopMaterial);
					leftX = -1;
				}
			}
			if (y == floorPlan.sizeY() - 1 && leftX != -1) {
				addWall(leftX, y, sizeX, 1, stoneSize, wallBaseMaterial, wallTopMaterial);
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
						addWall(x, topY, 1, sizeY, stoneSize, wallBaseMaterial, wallTopMaterial);
						topY = -1;
					}
				}
				if (y == floorPlan.sizeY() - 1 && topY != -1) {
					addWall(x, topY, 1, sizeY, stoneSize, wallBaseMaterial, wallTopMaterial);
					topY = -1;
				}
			}
			if (x == floorPlan.sizeX() - 1 && topY != -1) {
				addWall(x, topY, 1, sizeY, stoneSize, wallBaseMaterial, wallTopMaterial);
				topY = -1;
			}
		}

		// corners
		for (int y = 0; y < floorPlan.sizeY(); ++y) {
			for (int x = 0; x < floorPlan.sizeX(); ++x) {
				if (floorPlan.get(x, y) == FloorPlan.CORNER) {
					addCorner(x, y, stoneSize, wallBaseMaterial, wallTopMaterial);
				}
			}
		}
	}
}