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

import static de.amr.games.pacman.model.common.actors.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.model.common.world.World.TS;

import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._3d.animation.RaiseAndLowerWallAnimation;
import de.amr.games.pacman.ui.fx._3d.animation.Rendering3D;
import de.amr.games.pacman.ui.fx.util.U;
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
public class World3D extends Group {

	public final Maze3D maze3D;
	private final World world;
	private final Group foodGroup = new Group();

	/**
	 * @param gameVariant the game variant
	 * @param world       the world
	 * @param mazeNumber  the maze number (1..6)
	 * @param foodColor   food color in this maze
	 */
	public World3D(GameVariant gameVariant, World world, int mazeNumber, Color foodColor) {
		this.world = world;
		maze3D = new Maze3D(world);
		var wallSideColor = Rendering3D.getMazeSideColor(gameVariant, mazeNumber);
		var wallTopColor = Rendering3D.getMazeTopColor(gameVariant, mazeNumber);
		var doorColor = Rendering3D.getGhostHouseDoorColor(gameVariant);
		maze3D.erect(wallSideColor, wallTopColor, doorColor);
		maze3D.resolution.addListener((obs, oldVal, newVal) -> maze3D.erect(wallSideColor, wallTopColor, doorColor));

		var meatBall = new PhongMaterial(foodColor);
		world.tiles() //
				.filter(world::isFoodTile) //
				.map(tile -> world.isEnergizerTile(tile) //
						? new Energizer3D(tile, meatBall, 3.0)
						: new Pellet3D(tile, meatBall, 1.0))
				.forEach(foodGroup.getChildren()::add);

		getChildren().addAll(maze3D.getRoot(), foodGroup);
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
		maze3D.doors().forEach(door3D -> {
			boolean ghostApproaching = game.ghosts() //
					.filter(ghost -> ghost.visible) //
					.filter(ghost -> U.oneOf(ghost.state, GhostState.DEAD, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)) //
					.anyMatch(ghost -> isGhostNearDoor(ghost, door3D));
			door3D.setOpen(ghostApproaching);
		});
	}

	private boolean isGhostNearDoor(Ghost ghost, Door3D door3D) {
		double threshold = ghost.is(LEAVING_HOUSE) ? TS : 3 * TS;
		return ghost.position.euclideanDistance(door3D.getCenterPosition()) <= threshold;
	}

	public Animation createMazeFlashingAnimation(int times) {
		return times > 0 ? new RaiseAndLowerWallAnimation(times) : new PauseTransition(Duration.seconds(1));
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
}