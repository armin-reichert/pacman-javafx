/*
MIT License

Copyright (c) 2021-2023 Armin Reichert

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
package de.amr.games.pacman.ui.fx.v3d.entity;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.checkLevelNotNull;
import static de.amr.games.pacman.lib.Globals.checkNotNull;

import java.util.stream.Stream;

import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.world.Door;
import de.amr.games.pacman.ui.fx.app.Game2d;
import de.amr.games.pacman.ui.fx.rendering2d.GhostColoring;
import de.amr.games.pacman.ui.fx.rendering2d.MazeColoring;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManColoring;
import de.amr.games.pacman.ui.fx.rendering2d.PacManColoring;
import de.amr.games.pacman.ui.fx.rendering2d.Rendering2D;
import de.amr.games.pacman.ui.fx.util.Ufx;
import de.amr.games.pacman.ui.fx.v3d.app.Game3d;
import javafx.animation.SequentialTransition;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

/**
 * @author Armin Reichert
 */
public class GameLevel3D {

	private static PointLight createPacLight(Pac3D pac3D) {
		var light = new PointLight();
		light.setColor(Color.rgb(255, 255, 0, 0.25));
		light.setMaxRange(2 * TS);
		light.translateXProperty().bind(pac3D.position().xProperty());
		light.translateYProperty().bind(pac3D.position().yProperty());
		light.setTranslateZ(-10);
		return light;
	}

	private final GameLevel level;
	private final Group root = new Group();
	private final World3D world3D;
	private final Pac3D pac3D;
	private final PointLight pacLight;
	private final Ghost3D[] ghosts3D;
	private final LevelCounter3D levelCounter3D;
	private final LivesCounter3D livesCounter3D;
	private final Scores3D scores3D;
	private Bonus3D bonus3D;

	public GameLevel3D(GameLevel level, Rendering2D r2D, MazeColoring mazeColors, PacManColoring pacManColors,
			MsPacManColoring msPacManColors, GhostColoring[] ghostColors) {

		checkLevelNotNull(level);
		checkNotNull(r2D);
		checkNotNull(mazeColors);
		checkNotNull(pacManColors);
		checkNotNull(msPacManColors);
		checkNotNull(ghostColors);

		this.level = level;
		boolean msPacMan = level.game().variant() == GameVariant.MS_PACMAN;

		world3D = new World3D(level.world(), mazeColors, Game3d.assets.pelletModel3D);
		pac3D = msPacMan ? Pac3D.createMsPacMan3D(Game3d.assets.pacModel3D, level.pac(), msPacManColors)
				: Pac3D.createPacMan3D(Game3d.assets.pacModel3D, level.pac(), pacManColors);
		pacLight = createPacLight(pac3D);
		ghosts3D = level.ghosts().map(ghost -> createGhost3D(ghost, ghostColors[ghost.id()])).toArray(Ghost3D[]::new);
		levelCounter3D = createLevelCounter3D(r2D);
		livesCounter3D = msPacMan ? LivesCounter3D.of(msPacManColors) : LivesCounter3D.of(pacManColors);
		scores3D = new Scores3D(Game2d.assets.arcadeFont);

		scores3D.setPosition(TS, -3 * TS, -3 * TS);
		livesCounter3D.setPosition(2 * TS, 2 * TS, 0);
		levelCounter3D.setRightPosition((level.world().numCols() - 2) * TS, 2 * TS, -HTS);

		root.getChildren().add(scores3D.getRoot());
		root.getChildren().add(levelCounter3D.getRoot());
		root.getChildren().add(livesCounter3D.getRoot());
		root.getChildren().add(pac3D.getRoot());
		root.getChildren().add(pacLight);
		root.getChildren().add(ghosts3D[0].getRoot());
		root.getChildren().add(ghosts3D[1].getRoot());
		root.getChildren().add(ghosts3D[2].getRoot());
		root.getChildren().add(ghosts3D[3].getRoot());
		// Adding world (ghosthouse) *after* the guys if mandatory to get semi-transparent ghosthouse rendered correctly!
		root.getChildren().add(world3D.getRoot());

		pac3D.lightedPy.bind(Game3d.PY_3D_PAC_LIGHT_ENABLED);
		ghosts3D[GameModel.RED_GHOST].drawModePy.bind(Game3d.PY_3D_DRAW_MODE);
		ghosts3D[GameModel.PINK_GHOST].drawModePy.bind(Game3d.PY_3D_DRAW_MODE);
		ghosts3D[GameModel.CYAN_GHOST].drawModePy.bind(Game3d.PY_3D_DRAW_MODE);
		ghosts3D[GameModel.ORANGE_GHOST].drawModePy.bind(Game3d.PY_3D_DRAW_MODE);
		world3D.drawModePy.bind(Game3d.PY_3D_DRAW_MODE);
		world3D.floorColorPy.bind(Game3d.PY_3D_FLOOR_COLOR);
		world3D.floorTexturePy.bind(Game3d.PY_3D_FLOOR_TEXTURE);
		world3D.wallHeightPy.bind(Game3d.PY_3D_WALL_HEIGHT);
		world3D.wallThicknessPy.bind(Game3d.PY_3D_WALL_THICKNESS);
		livesCounter3D.drawModePy.bind(Game3d.PY_3D_DRAW_MODE);
	}

	public void replaceBonus3D(Bonus bonus, Rendering2D r2D, boolean moving) {
		if (bonus3D != null) {
			root.getChildren().remove(bonus3D.getRoot());
		}
		bonus3D = createBonus3D(bonus, r2D, moving);
		root.getChildren().add(bonus3D.getRoot());
	}

	private Ghost3D createGhost3D(Ghost ghost, GhostColoring colors) {
		return new Ghost3D(ghost, colors, Game3d.assets.ghostModel3D, 8.5);
	}

	private Bonus3D createBonus3D(Bonus bonus, Rendering2D r2D, boolean moving) {
		var symbolImage = r2D.image(r2D.bonusSymbolRegion(bonus.symbol()));
		var pointsImage = r2D.image(r2D.bonusValueRegion(bonus.symbol()));
		return new Bonus3D(bonus, symbolImage, pointsImage, moving);
	}

	private LevelCounter3D createLevelCounter3D(Rendering2D r2D) {
		var symbolImages = level.game().levelCounter().stream().map(r2D::bonusSymbolRegion).map(r2D::image)
				.toArray(Image[]::new);
		return new LevelCounter3D(symbolImages);
	}

	private void updatePacLight() {
		var pac = level.pac();
		boolean isVisible = pac.isVisible();
		boolean isAlive = !pac.isDead();
		boolean hasPower = pac.powerTimer().isRunning();
		var maxRange = pac.isPowerFading(level) ? 4 : 8;
		pacLight.setLightOn(pac3D.lightedPy.get() && isVisible && isAlive && hasPower);
		pacLight.setMaxRange(hasPower ? maxRange * TS : 0);
	}

	public void update() {
		pac3D.update(level);
		updatePacLight();
		Stream.of(ghosts3D).forEach(ghost3D -> ghost3D.update(level));
		if (bonus3D != null) {
			bonus3D.update(level);
		}
		// TODO get rid of this
		int numLivesShown = level.game().isOneLessLifeDisplayed() ? level.game().lives() - 1 : level.game().lives();
		livesCounter3D.update(numLivesShown);
		livesCounter3D.getRoot().setVisible(level.game().hasCredit());
		scores3D.update(level);
		if (level.game().hasCredit()) {
			scores3D.setShowPoints(true);
		} else {
			scores3D.setShowText(Color.RED, "GAME OVER!");
		}
		updateHouseState();
	}

	public void eat(Eatable3D eatable3D) {
		checkNotNull(eatable3D);

		if (eatable3D instanceof Energizer3D energizer3D) {
			energizer3D.stopPumping();
		}
		// Delay hiding of pellet for some milliseconds because in case the player approaches the pellet from the right,
		// the pellet disappears too early (collision by same tile in game model is too simplistic).
		var delayHiding = Ufx.actionAfterSeconds(0.05, () -> eatable3D.getRoot().setVisible(false));
		var eatenAnimation = eatable3D.getEatenAnimation();
		if (eatenAnimation.isPresent() && Game3d.PY_3D_ENERGIZER_EXPLODES.get()) {
			new SequentialTransition(delayHiding, eatenAnimation.get()).play();
		} else {
			delayHiding.play();
		}
	}

	private void updateHouseState() {
		boolean isGhostNearHouse = level.ghosts(GhostState.LOCKED, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
				.anyMatch(Ghost::isVisible);
		boolean accessGranted = isAccessGranted(level.ghosts(), level.world().house().door());
		world3D.houseLighting().setLightOn(isGhostNearHouse);
		world3D.doorWings3D().forEach(door3D -> door3D.setOpen(accessGranted));
	}

	private boolean isAccessGranted(Stream<Ghost> ghosts, Door door) {
		return ghosts.anyMatch(ghost -> ghost.isVisible()
				&& ghost.is(GhostState.RETURNING_TO_HOUSE, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
				&& ghost.position().euclideanDistance(door.entryPosition()) <= 1.5 * TS);
	}

	public GameLevel level() {
		return level;
	}

	public Group getRoot() {
		return root;
	}

	public LivesCounter3D livesCounter3D() {
		return livesCounter3D;
	}

	public World3D world3D() {
		return world3D;
	}

	public Pac3D pac3D() {
		return pac3D;
	}

	public Ghost3D[] ghosts3D() {
		return ghosts3D;
	}

	public Bonus3D bonus3D() {
		return bonus3D;
	}

	public Scores3D scores3D() {
		return scores3D;
	}
}