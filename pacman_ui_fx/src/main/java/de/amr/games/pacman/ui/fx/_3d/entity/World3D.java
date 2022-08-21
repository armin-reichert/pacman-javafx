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

import static de.amr.games.pacman.model.common.actors.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.common.actors.GhostState.LEAVING_HOUSE;
import static de.amr.games.pacman.model.common.actors.GhostState.RETURNING_TO_HOUSE;
import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;

import java.util.stream.Stream;

import de.amr.games.pacman.lib.V2d;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.world.ArcadeGhostHouse;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx._3d.animation.Rendering3D;
import de.amr.games.pacman.ui.fx._3d.entity.Maze3D.MazeColors;
import de.amr.games.pacman.ui.fx._3d.model.Model3D;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.beans.binding.Bindings;
import javafx.scene.Group;
import javafx.scene.paint.Color;

/**
 * @author Armin Reichert
 */
public class World3D extends Group {

	private final Maze3D maze3D;
	private final Food3D food3D;
	private final Scores3D scores3D;
	private final LevelCounter3D levelCounter3D;
	private final LivesCounter3D livesCounter3D;

	public World3D(GameModel game, Model3D model3D, Rendering2D r2D) {
		scores3D = new Scores3D();
		scores3D.setFont(r2D.getArcadeFont());
		if (game.hasCredit()) {
			scores3D.setComputeScoreText(true);
		} else {
			scores3D.setComputeScoreText(false);
			scores3D.txtScore.setFill(Color.RED);
			scores3D.txtScore.setText("GAME OVER!");
		}
		getChildren().add(scores3D);

		var mazeColors = new MazeColors(//
				Rendering3D.getMazeSideColor(game.variant, game.level.mazeNumber()), //
				Rendering3D.getMazeTopColor(game.variant, game.level.mazeNumber()), //
				Rendering3D.getGhostHouseDoorColor(game.variant));

		maze3D = new Maze3D(game.level.world(), mazeColors);
		maze3D.drawModePy.bind(Env.drawModePy);
		maze3D.floorTexturePy.bind(Bindings.createObjectBinding(
				() -> "none".equals(Env.floorTexturePy.get()) ? null : Ufx.image("graphics/" + Env.floorTexturePy.get()),
				Env.floorTexturePy));
		maze3D.floorColorPy.bind(Env.floorColorPy);
		maze3D.resolutionPy.bind(Env.mazeResolutionPy);
		maze3D.wallHeightPy.bind(Env.mazeWallHeightPy);
		maze3D.wallThicknessPy.bind(Env.mazeWallThicknessPy);
		getChildren().add(maze3D);

		var foodColor = Rendering3D.getMazeFoodColor(game.variant, game.level.mazeNumber());
		food3D = new Food3D(game.variant, game.world(), foodColor);
		food3D.squirtingPy.bind(Env.squirtingPy);
		getChildren().add(food3D);

		levelCounter3D = new LevelCounter3D(symbol -> r2D.getSpriteImage(r2D.getBonusSymbolSprite(symbol)));
		levelCounter3D.setRightPosition((game.level.world().numCols() - 1) * TS, TS);
		levelCounter3D.update(game.levelCounter);
		getChildren().add(levelCounter3D);

		livesCounter3D = new LivesCounter3D(model3D);
		livesCounter3D.setTranslateX(TS);
		livesCounter3D.setTranslateY(TS);
		livesCounter3D.setTranslateZ(-HTS);
		livesCounter3D.setVisible(game.hasCredit());
		getChildren().add(livesCounter3D);
	}

	public Scores3D getScores3D() {
		return scores3D;
	}

	public Maze3D getMaze3D() {
		return maze3D;
	}

	public Food3D getFood3D() {
		return food3D;
	}

	public void reset() {
		food3D.resetAnimations();
	}

	public void update(GameModel game) {
		scores3D.update(game);
		updateDoorState(game);
		livesCounter3D.update(game.livesOneLessShown ? game.lives - 1 : game.lives);
	}

	// should be generalized to work with any ghost house
	private void updateDoorState(GameModel game) {
		if (game.world().ghostHouse() instanceof ArcadeGhostHouse) {
			var accessGranted = isAccessGranted(game.theGhosts, ArcadeGhostHouse.DOOR_CENTER);
			maze3D.doors().forEach(door3D -> door3D.setOpen(accessGranted));
		}
	}

	private boolean isAccessGranted(Ghost[] ghosts, V2d doorPosition) {
		return Stream.of(ghosts).anyMatch(ghost -> isAccessGranted(ghost, doorPosition));
	}

	private boolean isAccessGranted(Ghost ghost, V2d doorPosition) {
		return ghost.isVisible() && ghost.is(RETURNING_TO_HOUSE, ENTERING_HOUSE, LEAVING_HOUSE)
				&& inDoorDistance(ghost, doorPosition);
	}

	private boolean inDoorDistance(Ghost ghost, V2d doorPosition) {
		return ghost.getPosition().euclideanDistance(doorPosition) <= (ghost.is(LEAVING_HOUSE) ? TS : 3 * TS);
	}
}