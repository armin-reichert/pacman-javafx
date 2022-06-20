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
import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;

import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx._3d.animation.Rendering3D;
import de.amr.games.pacman.ui.fx._3d.model.PacManModel3D;
import de.amr.games.pacman.ui.fx.util.U;
import javafx.scene.Group;
import javafx.scene.paint.Color;

/**
 * @author Armin Reichert
 */
public class World3D extends Group {

	private final Maze3D maze3D;
	private final Scores3D scores3D;
	private final LevelCounter3D levelCounter3D;
	private final LivesCounter3D livesCounter3D;

	public World3D(GameModel game, PacManModel3D model3D, Rendering2D r2D) {

		scores3D = new Scores3D();
		scores3D.setFont(r2D.getArcadeFont());
		if (game.credit > 0) {
			scores3D.setComputeScoreText(true);
		} else {
			scores3D.setComputeScoreText(false);
			scores3D.txtScore.setFill(Color.RED);
			scores3D.txtScore.setText("GAME OVER!");
		}
		getChildren().add(scores3D);

		var wallSideColor = Rendering3D.getMazeSideColor(game.variant, game.level.mazeNumber);
		var wallTopColor = Rendering3D.getMazeTopColor(game.variant, game.level.mazeNumber);
		var doorColor = Rendering3D.getGhostHouseDoorColor(game.variant);
		var foodColor = Rendering3D.getMazeFoodColor(game.variant, game.level.mazeNumber);
		maze3D = new Maze3D(game.level.world, wallSideColor, wallTopColor, doorColor, foodColor);
		getChildren().add(maze3D.getRoot());

		levelCounter3D = new LevelCounter3D(symbol -> r2D.getSpriteImage(r2D.getBonusSymbolSprite(symbol)));
		levelCounter3D.setRightPosition((game.level.world.numCols() - 1) * TS, TS);
		levelCounter3D.update(game.levelCounter);
		getChildren().add(levelCounter3D);

		livesCounter3D = new LivesCounter3D(model3D);
		livesCounter3D.setTranslateX(TS);
		livesCounter3D.setTranslateY(TS);
		livesCounter3D.setTranslateZ(-HTS);
		livesCounter3D.setVisible(game.credit > 0);
		getChildren().add(livesCounter3D);
	}

	public Scores3D getScores3D() {
		return scores3D;
	}

	public Maze3D getMaze3D() {
		return maze3D;
	}

	public void update(GameModel game) {
		scores3D.update(game);
		maze3D.doors().forEach(door3D -> {
			boolean ghostApproaching = game.ghosts() //
					.filter(ghost -> ghost.visible) //
					.filter(ghost -> U.oneOf(ghost.state, GhostState.DEAD, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)) //
					.anyMatch(ghost -> isGhostNearDoor(ghost, door3D));
			door3D.setOpen(ghostApproaching);
		});
		livesCounter3D.update(game.playing ? game.lives - 1 : game.lives);
	}

	private boolean isGhostNearDoor(Ghost ghost, Door3D door3D) {
		double threshold = ghost.is(LEAVING_HOUSE) ? TS : 3 * TS;
		return ghost.position.euclideanDistance(door3D.getCenterPosition()) <= threshold;
	}

}