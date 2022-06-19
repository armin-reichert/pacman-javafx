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

import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._3d.animation.RaiseAndLowerWallAnimation;
import de.amr.games.pacman.ui.fx._3d.animation.Rendering3D;
import javafx.animation.Animation;
import javafx.animation.PauseTransition;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.util.Duration;

/**
 * @author Armin Reichert
 */
public class Maze3D extends Group {

	private static final double ENERGIZER_RADIUS = 3.0;
	private static final double PELLET_RADIUS = 1.0;

	private final Group foodGroup = new Group();
	public MazeBuilding3D mazeBuilding;

	/**
	 * @param gameVariant  the game variant
	 * @param world        the world
	 * @param mazeNumber   the maze number (1..6)
	 * @param unscaledSize unscaled size
	 * @param foodColor    food color in this maze
	 */
	public Maze3D(GameVariant gameVariant, World world, int mazeNumber, V2d unscaledSize, Color foodColor) {
		mazeBuilding = new MazeBuilding3D(unscaledSize);
		mazeBuilding.build(world, //
				Rendering3D.getMazeSideColor(gameVariant, mazeNumber), //
				Rendering3D.getMazeTopColor(gameVariant, mazeNumber), //
				Rendering3D.getGhostHouseDoorColor(gameVariant));

		var material = new PhongMaterial(foodColor);
		world.tiles() //
				.filter(world::isFoodTile) //
				.map(tile -> world.isEnergizerTile(tile) //
						? new Energizer3D(tile, material, ENERGIZER_RADIUS)
						: new Pellet3D(tile, material, PELLET_RADIUS))
				.forEach(foodGroup.getChildren()::add);

		getChildren().addAll(mazeBuilding.getRoot(), foodGroup);
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
		mazeBuilding.doors().forEach(door -> door.update(game));
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

	public void validateFoodNodes(World world) {
		foodNodes().forEach(foodNode -> foodNode.setVisible(!world.containsEatenFood(tile(foodNode))));

	}

	public Stream<Animation> energizerAnimations() {
		return energizers().map(energizer -> energizer.animation);
	}

	public Animation createMazeFlashingAnimation(int times) {
		return times > 0 ? new RaiseAndLowerWallAnimation(times) : new PauseTransition(Duration.seconds(1));
	}
}