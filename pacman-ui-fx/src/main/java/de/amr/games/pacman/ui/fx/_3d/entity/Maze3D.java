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

import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.world.FloorPlan;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._3d.animation.RaiseAndLowerWallAnimation;
import de.amr.games.pacman.ui.fx.app.Env;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
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
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

/**
 * 3D-model for a maze. Creates walls/doors using information from the floor plan.
 * 
 * @author Armin Reichert
 */
public class Maze3D extends Group {

	public static class PelletInfo {
		public final V2i tile;
		public final boolean energizer;
		public final Animation animation;

		private PelletInfo(V2i tile, boolean energizer, Animation animation) {
			this.tile = tile;
			this.energizer = energizer;
			this.animation = animation;
		}
	}

	public static PelletInfo pelletInfo(Node node) {
		return (PelletInfo) node.getUserData();
	}

	private static Node makePellet(V2i tile, PhongMaterial pelletMaterial) {
		var pellet = new Sphere(1);
		pellet.setMaterial(pelletMaterial);
		pellet.setTranslateX(tile.x * TS + HTS);
		pellet.setTranslateY(tile.y * TS + HTS);
		pellet.setTranslateZ(-3);
		pellet.setUserData(new PelletInfo(tile, false, null));
		return pellet;
	}

	private static Node makeEnergizer(V2i tile, PhongMaterial pelletMaterial) {
		var pellet = new Sphere(2.5);
		pellet.setMaterial(pelletMaterial);
		pellet.setTranslateX(tile.x * TS + HTS);
		pellet.setTranslateY(tile.y * TS + HTS);
		pellet.setTranslateZ(-3);
		pellet.setUserData(new PelletInfo(tile, true, createEnergizerAnimation(pellet)));
		return pellet;
	}

	private static Animation createEnergizerAnimation(Node pellet) {
		var anim = new ScaleTransition(Duration.seconds(1.0 / 6), pellet);
		anim.setAutoReverse(true);
		anim.setCycleCount(Transition.INDEFINITE);
		anim.setFromX(1.0);
		anim.setFromY(1.0);
		anim.setFromZ(1.0);
		anim.setToX(0.1);
		anim.setToY(0.1);
		anim.setToZ(0.1);
		return anim;
	}

	public final DoubleProperty $wallHeight = new SimpleDoubleProperty(2.0);
	public final IntegerProperty $resolution = new SimpleIntegerProperty(8);

	private final Group wallsGroup = new Group();
	private final Group doorsGroup = new Group();
	private final Group foodGroup = new Group();

	/**
	 * Creates the 3D-maze base structure (without walls, doors, food).
	 * 
	 * @param mazeWidth    maze width in units
	 * @param mazeHeight   maze height in units
	 * @param floorTexture floor texture
	 * @param floorColor   floor color
	 */
	public Maze3D(double mazeWidth, double mazeHeight, Image floorTexture, Color floorColor) {
		var floorMaterial = new PhongMaterial(floorColor);
		floorMaterial.setSpecularColor(floorColor.brighter());
		floorMaterial.setDiffuseMap(floorTexture);
		Box floor = new Box(mazeWidth - 1, mazeHeight - 1, 0.01);
		floor.setMaterial(floorMaterial);
		floor.getTransforms().add(new Translate(0.5 * floor.getWidth(), 0.5 * floor.getHeight(), 0.5 * floor.getDepth()));
		floor.drawModeProperty().bind(Env.$drawMode3D);
		getChildren().addAll(floor, new Group(wallsGroup, doorsGroup), foodGroup);
	}

	public void reset() {
		energizerNodes().forEach(node -> {
			node.setScaleX(1.0);
			node.setScaleY(1.0);
			node.setScaleZ(1.0);
		});
	}

	public void update(GameModel game) {
		doors().forEach(door -> door.updateState(game));
	}

	public Stream<Door3D> doors() {
		return doorsGroup.getChildren().stream().map(node -> (Door3D) node);
	}

	public Stream<Node> foodNodes() {
		return foodGroup.getChildren().stream();
	}

	public Stream<Node> energizerNodes() {
		return foodNodes().filter(node -> pelletInfo(node).energizer);
	}

	public Optional<Node> foodAt(V2i tile) {
		return foodNodes().filter(node -> pelletInfo(node).tile.equals(tile)).findFirst();
	}

	public void hideFood(Node node) {
		node.setVisible(false);
		PelletInfo info = pelletInfo(node);
		if (info.animation != null) {
			info.animation.stop();
		}
	}

	/**
	 * Creates the walls and doors according to the current resolution.
	 * 
	 * @param world         the game world
	 * @param wallBaseColor color of wall at base
	 * @param wallTopColor  color of wall at top
	 */
	public void buildStructure(World world, Color wallBaseColor, Color wallTopColor) {
		wallsGroup.getChildren().clear();
		addWalls(new FloorPlan($resolution.get(), world), world, TS / $resolution.get(), wallBaseColor, wallTopColor);
		doorsGroup.getChildren().clear();
		for (V2i doorTile : world.ghostHouse().doorTiles) {
			doorsGroup.getChildren().add(new Door3D(doorTile));
		}
	}

	/**
	 * Creates the pellets/food and the energizer animations.
	 * 
	 * @param world       the game world
	 * @param pelletColor color of pellets
	 */
	public void buildFood(World world, Color pelletColor) {
		var material = new PhongMaterial(pelletColor);
		var pellets = world.tiles().filter(world::isFoodTile)
				.map(tile -> world.isEnergizerTile(tile) ? makeEnergizer(tile, material) : makePellet(tile, material))
				.collect(Collectors.toList());
		foodGroup.getChildren().setAll(pellets);
	}

	public Stream<Animation> energizerAnimations() {
		return energizerNodes().map(node -> pelletInfo(node).animation).filter(Objects::nonNull);
	}

	public Animation createMazeFlashingAnimation(int times) {
		return times > 0 ? new RaiseAndLowerWallAnimation(times) : new PauseTransition(Duration.seconds(1));
	}

	/**
	 * Adds a wall at given position. A wall consists of a base and a top part which can have different color and
	 * material.
	 * 
	 * @param leftX      x-coordinate of top-left brick
	 * @param topY       y-coordinate of top-left brick
	 * @param numBricksX number of bricks in x-direction
	 * @param numBricksY number of bricks in y-direction
	 * @param brickSize  size of a single brick
	 */
	private void addWall(int leftX, int topY, int numBricksX, int numBricksY, double brickSize,
			PhongMaterial baseMaterial, PhongMaterial topMaterial) {

		Box base = new Box(numBricksX * brickSize, numBricksY * brickSize, $wallHeight.get());
		base.depthProperty().bind($wallHeight);
		base.setMaterial(baseMaterial);
		base.translateZProperty().bind($wallHeight.multiply(-0.5));
		base.drawModeProperty().bind(Env.$drawMode3D);

		double topHeight = 0.5;
		Box top = new Box(numBricksX * brickSize, numBricksY * brickSize, topHeight);
		top.setMaterial(topMaterial);
		top.translateZProperty().bind(base.translateZProperty().subtract($wallHeight.add(topHeight + 0.1).multiply(0.5)));
		top.drawModeProperty().bind(Env.$drawMode3D);

		Group wall = new Group(base, top);
		wall.setTranslateX((leftX + 0.5 * numBricksX) * brickSize);
		wall.setTranslateY((topY + 0.5 * numBricksY) * brickSize);

		wallsGroup.getChildren().add(wall);
	}

	private void addWalls(FloorPlan floorPlan, World world, double brickSize, Color wallBaseColor, Color wallTopColor) {
		var baseMaterial = new PhongMaterial(wallBaseColor);
		baseMaterial.setSpecularColor(wallBaseColor.brighter());

		var topMaterial = new PhongMaterial(wallTopColor);
		topMaterial.setDiffuseColor(wallTopColor);

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
						addWall(leftX, y, sizeX, 1, brickSize, baseMaterial, topMaterial);
						leftX = -1;
					}
				}
				if (x == floorPlan.sizeX() - 1 && leftX != -1) {
					addWall(leftX, y, sizeX, 1, brickSize, baseMaterial, topMaterial);
					leftX = -1;
				}
			}
			if (y == floorPlan.sizeY() - 1 && leftX != -1) {
				addWall(leftX, y, sizeX, 1, brickSize, baseMaterial, topMaterial);
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
						addWall(x, topY, 1, sizeY, brickSize, baseMaterial, topMaterial);
						topY = -1;
					}
				}
				if (y == floorPlan.sizeY() - 1 && topY != -1) {
					addWall(x, topY, 1, sizeY, brickSize, baseMaterial, topMaterial);
					topY = -1;
				}
			}
			if (x == floorPlan.sizeX() - 1 && topY != -1) {
				addWall(x, topY, 1, sizeY, brickSize, baseMaterial, topMaterial);
				topY = -1;
			}
		}

		// corners
		for (int y = 0; y < floorPlan.sizeY(); ++y) {
			for (int x = 0; x < floorPlan.sizeX(); ++x) {
				if (floorPlan.get(x, y) == FloorPlan.CORNER) {
					addWall(x, y, 1, 1, brickSize, baseMaterial, topMaterial);
				}
			}
		}
	}
}