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

import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._3d.animation.Rendering3D;
import de.amr.games.pacman.ui.fx.util.U;
import javafx.scene.Group;

/**
 * @author Armin Reichert
 */
public class World3D extends Group {

	private final Maze3D maze3D;

	/**
	 * @param gameVariant the game variant
	 * @param world       the world
	 * @param mazeNumber  the maze number (1..6)
	 */
	public World3D(GameVariant gameVariant, World world, int mazeNumber) {
		var wallSideColor = Rendering3D.getMazeSideColor(gameVariant, mazeNumber);
		var wallTopColor = Rendering3D.getMazeTopColor(gameVariant, mazeNumber);
		var doorColor = Rendering3D.getGhostHouseDoorColor(gameVariant);
		var foodColor = Rendering3D.getMazeFoodColor(gameVariant, mazeNumber);
		maze3D = new Maze3D(world, wallSideColor, wallTopColor, doorColor, foodColor);
		getChildren().addAll(maze3D.getRoot());
	}

	public Maze3D getMaze3D() {
		return maze3D;
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

}