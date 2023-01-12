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

import de.amr.games.pacman.lib.math.Vector2f;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.model.common.world.ArcadeGhostHouse;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._2d.rendering.Rendering2D;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;

/**
 * @author Armin Reichert
 */
public class World3D extends Group {

	private final GameLevel level;
	private final Maze3D maze3D;
	private final Food3D food3D;
	private final PointLight houseLighting;
	private final LevelCounter3D levelCounter3D;
	private final LivesCounter3D livesCounter3D;
	private final Scores3D scores3D;

	public World3D(GameLevel level, Rendering2D r2D) {
		this.level = level;

		var width = level.world().numCols() * World.TS;
		var height = level.world().numRows() * World.TS;

		int mazeNumber = level.game().mazeNumber(level.number());
		var mazeColors = new Maze3DColors(//
				r2D.mazeSideColor(mazeNumber), //
				r2D.mazeTopColor(mazeNumber), //
				r2D.ghostHouseDoorColor());

		maze3D = new Maze3D(level.world(), mazeColors);

		houseLighting = new PointLight();
		houseLighting.setColor(Color.GHOSTWHITE);
		houseLighting.setMaxRange(10 * TS);
		houseLighting.setTranslateX(0.5 * width);
		houseLighting.setTranslateY(0.5 * (height - 2 * TS));
		houseLighting.setTranslateZ(-TS);

		var foodColor = r2D.mazeFoodColor(mazeNumber);
		food3D = new Food3D(level.world(), foodColor);

		var levelCounterPos = new Vector2f((level.world().numCols() - 1) * TS, TS);
		levelCounter3D = new LevelCounter3D(level.game().levelCounter(), levelCounterPos,
				symbol -> r2D.spritesheet().region(r2D.bonusSymbolSprite(symbol)));

		livesCounter3D = new LivesCounter3D();
		livesCounter3D.setTranslateX(TS);
		livesCounter3D.setTranslateY(TS);
		livesCounter3D.setTranslateZ(-HTS);
		livesCounter3D().setVisible(level.game().hasCredit());

		var scoreFont = r2D.arcadeFont(TS);
		scores3D = new Scores3D(scoreFont);

		getChildren().add(maze3D);
		getChildren().add(food3D);
		getChildren().add(scores3D);
		getChildren().add(houseLighting);
		getChildren().add(levelCounter3D);
		getChildren().add(livesCounter3D);
	}

	public void update() {
		updateHouseLightingState();
		updateDoorState();
		livesCounter3D.update(level.game().isOneLessLifeDisplayed() ? level.game().lives() - 1 : level.game().lives());
		scores3D.update(level);
		if (level.game().hasCredit()) {
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

	private void updateHouseLightingState() {
		boolean anyGhostInHouse = level.ghosts(GhostState.LOCKED, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
				.count() > 0;
		houseLighting.setLightOn(anyGhostInHouse);
	}

	// should be generalized to work with any ghost house
	private void updateDoorState() {
		if (level.world().ghostHouse() instanceof ArcadeGhostHouse) {
			var accessGranted = isAccessGranted(level.ghosts(), ArcadeGhostHouse.DOOR_CENTER);
			maze3D.doors().forEach(door3D -> door3D.setOpen(accessGranted));
		}
	}

	private boolean isAccessGranted(Stream<Ghost> ghosts, Vector2f doorPosition) {
		return ghosts.anyMatch(ghost -> isAccessGranted(ghost, doorPosition));
	}

	private boolean isAccessGranted(Ghost ghost, Vector2f doorPosition) {
		return ghost.isVisible() && ghost.is(RETURNING_TO_HOUSE, ENTERING_HOUSE, LEAVING_HOUSE)
				&& inDoorDistance(ghost, doorPosition);
	}

	private boolean inDoorDistance(Ghost ghost, Vector2f doorPosition) {
		return ghost.position().euclideanDistance(doorPosition) <= (ghost.is(LEAVING_HOUSE) ? TS : 3 * TS);
	}
}