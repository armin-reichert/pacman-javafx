/*
MIT License

Copyright (c) 2021-22 Armin Reichert

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

import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._3d.animation.RaiseAndLowerWallAnimation;
import de.amr.games.pacman.ui.fx._3d.animation.Rendering3D;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.util.U;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

/**
 * 3D-model for a maze. Creates walls/doors using information from the floor plan.
 * 
 * @author Armin Reichert
 */
public class Maze3D extends Group {

	private static final double ENERGIZER_RADIUS = 3.0;
	private static final double PELLET_RADIUS = 1.0;

	public final MazeBuilding3D mazeBuilding = new MazeBuilding3D();

	private final MazeFloor3D floor;
	private final Group wallsGroup = new Group();
	private final Group doorsGroup = new Group();
	private final Group foodGroup = new Group();

	/**
	 * Creates the 3D-maze base structure (without walls, doors, food).
	 * 
	 * @param gameVariant  the game variant
	 * @param world        the world
	 * @param mazeNumber   the maze number (1..6)
	 * @param unscaledSize unscaled size
	 * @param foodColor    food color in this maze
	 */
	public Maze3D(GameVariant gameVariant, World world, int mazeNumber, V2d unscaledSize, Color foodColor) {
		floor = new MazeFloor3D(unscaledSize.x - 1, unscaledSize.y - 1, 0.01);
		floor.showSolid(Color.rgb(5, 5, 10));
		floor.getTransforms().add(new Translate(0.5 * floor.getWidth(), 0.5 * floor.getHeight(), 0.5 * floor.getDepth()));
		build(gameVariant, world, mazeNumber, foodColor);
		Env.$useMazeFloorTexture.addListener((obs, oldVal, newVal) -> {
			if (newVal.booleanValue()) {
				floor.showTextured(U.image("/common/escher-texture.jpg"), Color.DARKBLUE);
			} else {
				floor.showSolid(Color.rgb(5, 5, 10));
			}
		});
		mazeBuilding.resolution.addListener((obs, oldVal, newVal) -> createWallsAndDoors(world, //
				Rendering3D.getMazeSideColor(gameVariant, mazeNumber), //
				Rendering3D.getMazeTopColor(gameVariant, mazeNumber), //
				Rendering3D.getGhostHouseDoorColor(gameVariant))//
		);
		getChildren().addAll(floor, wallsGroup, doorsGroup, foodGroup);
	}

	public void build(GameVariant gameVariant, World world, int mazeNumber, Color foodColor) {
		createWallsAndDoors(world, //
				Rendering3D.getMazeSideColor(gameVariant, mazeNumber), //
				Rendering3D.getMazeTopColor(gameVariant, mazeNumber), //
				Rendering3D.getGhostHouseDoorColor(gameVariant));
		createFood(world, foodColor);
	}

	public void reset() {
		energizerAnimations().forEach(Animation::stop);
		energizers().forEach(node -> {
			node.setScaleX(1.0);
			node.setScaleY(1.0);
			node.setScaleZ(1.0);
		});
	}

	public void update(GameModel game) {
		doors().forEach(door -> door.update(game));
	}

	public MazeFloor3D getFloor() {
		return floor;
	}

	public Stream<Door3D> doors() {
		return doorsGroup.getChildren().stream().map(Door3D.class::cast);
	}

	public Stream<Node> foodNodes() {
		return foodGroup.getChildren().stream();
	}

	public Optional<Node> foodAt(V2i tile) {
		return foodNodes().filter(food -> tile(food).equals(tile)).findFirst();
	}

	public V2i tile(Node foodNode) {
		return (V2i) foodNode.getUserData();
	}

	public Stream<Energizer3D> energizers() {
		return foodNodes().filter(Energizer3D.class::isInstance).map(Energizer3D.class::cast);
	}

	public void hideFood(Node foodNode) {
		foodNode.setVisible(false);
		if (foodNode instanceof Energizer3D) {
			var energizer = (Energizer3D) foodNode;
			energizer.animation.stop();
		}
	}

	/**
	 * Creates the walls and doors according to the current resolution.
	 * 
	 * @param world         the game world
	 * @param wallBaseColor color of wall at base
	 * @param wallTopColor  color of wall at top
	 * @param doorColor     door color
	 */
	public void createWallsAndDoors(World world, Color wallBaseColor, Color wallTopColor, Color doorColor) {
		mazeBuilding.buildWalls(world, wallsGroup, wallBaseColor, wallTopColor);
		var leftDoor = new Door3D(world.ghostHouse().doorTileLeft(), true, doorColor);
		var rightDoor = new Door3D(world.ghostHouse().doorTileRight(), false, doorColor);
		doorsGroup.getChildren().setAll(leftDoor, rightDoor);
		log("Built 3D maze (resolution=%d, wall height=%.2f)", mazeBuilding.resolution.get(),
				mazeBuilding.wallHeight.get());
	}

	/**
	 * Creates the pellets/food and the energizer animations.
	 * 
	 * @param world       the game world
	 * @param pelletColor color of pellets
	 */
	public void createFood(World world, Color pelletColor) {
		var material = new PhongMaterial(pelletColor);
		foodGroup.getChildren().clear();
		world.tiles() //
				.filter(world::isFoodTile) //
				.map(tile -> world.isEnergizerTile(tile) //
						? new Energizer3D(tile, material, ENERGIZER_RADIUS)
						: new Pellet3D(tile, material, PELLET_RADIUS))
				.forEach(foodGroup.getChildren()::add);
	}

	public Stream<Animation> energizerAnimations() {
		return energizers().map(energizer -> energizer.animation);
	}

	public Animation createMazeFlashingAnimation(int times) {
		return times > 0 ? new RaiseAndLowerWallAnimation(times) : new PauseTransition(Duration.seconds(1));
	}
}