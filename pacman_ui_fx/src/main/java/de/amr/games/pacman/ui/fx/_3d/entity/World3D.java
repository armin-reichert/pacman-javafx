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
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.model.common.world.ArcadeGhostHouse;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._2d.rendering.Rendering2D;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * @author Armin Reichert
 */
public class World3D extends Group {

	private final Maze3D maze3D;
	private final Food3D food3D;
	private final PointLight houseLighting;
	private final LevelCounter3D levelCounter3D;
	private final LivesCounter3D livesCounter3D;
	private final Scores3D scores3D;

	public World3D(GameModel game, Rendering2D r2D) {
		var width = game.level.world().numCols() * World.TS;
		var height = game.level.world().numRows() * World.TS;

		var mazeColors = new Maze3DColors(//
				r2D.getMazeSideColor(game.level.mazeNumber()), //
				r2D.getMazeTopColor(game.level.mazeNumber()), //
				r2D.getGhostHouseDoorColor());

		maze3D = new Maze3D(game.level.world(), mazeColors);

		houseLighting = new PointLight();
		houseLighting.setColor(Color.GHOSTWHITE);
		houseLighting.setMaxRange(10 * TS);
		houseLighting.setTranslateX(0.5 * width);
		houseLighting.setTranslateY(0.5 * (height - 2 * TS));
		houseLighting.setTranslateZ(-TS);

		var foodColor = r2D.getMazeFoodColor(game.level.mazeNumber());
		food3D = new Food3D(game.level.world(), foodColor);

		levelCounter3D = new LevelCounter3D(game.levelCounter,
				symbol -> r2D.spritesheet().region(r2D.bonusSymbolSprite(symbol)));
		levelCounter3D.setRightPosition((game.level.world().numCols() - 1) * TS, TS);

		livesCounter3D = new LivesCounter3D();
		livesCounter3D.setTranslateX(TS);
		livesCounter3D.setTranslateY(TS);
		livesCounter3D.setTranslateZ(-HTS);
		livesCounter3D().setVisible(game.hasCredit());

		var scoreFont = Font.font(r2D.arcadeFont().getFamily(), TS);
		scores3D = new Scores3D(scoreFont);

		getChildren().add(maze3D);
		getChildren().add(food3D);
		getChildren().add(scores3D);
		getChildren().add(houseLighting);
		getChildren().add(levelCounter3D);
		getChildren().add(livesCounter3D);
	}

	public void update(GameModel game) {
		scores3D.update(game);
		updateHouseLightingState(game);
		updateDoorState(game);
		livesCounter3D.update(game.livesOneLessShown ? game.lives - 1 : game.lives);
		if (game.hasCredit()) {
			scores3D.setShowPoints(true);
		} else {
			scores3D.setShowText(Color.RED, "GAME OVER!");
		}
	}

	public Scores3D scores3D() {
		return scores3D;
	}

	public LivesCounter3D livesCounter3D() {
		return livesCounter3D;
	}

	public LevelCounter3D levelCounter3D() {
		return levelCounter3D;
	}

	public Maze3D maze3D() {
		return maze3D;
	}

	public Food3D food3D() {
		return food3D;
	}

	private void updateHouseLightingState(GameModel game) {
		boolean anyGhostInHouse = game.ghosts(GhostState.LOCKED, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
				.count() > 0;
		houseLighting.setLightOn(anyGhostInHouse);
	}

	// should be generalized to work with any ghost house
	private void updateDoorState(GameModel game) {
		if (game.level.world().ghostHouse() instanceof ArcadeGhostHouse) {
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
		return ghost.position().euclideanDistance(doorPosition) <= (ghost.is(LEAVING_HOUSE) ? TS : 3 * TS);
	}
}