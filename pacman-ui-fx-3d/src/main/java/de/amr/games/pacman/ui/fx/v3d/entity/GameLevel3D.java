/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.entity;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.model.actors.Bonus;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.world.Door;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.SpritesheetMsPacManGame;
import de.amr.games.pacman.ui.fx.rendering2d.pacman.SpritesheetPacManGame;
import de.amr.games.pacman.ui.fx.util.Spritesheet;
import de.amr.games.pacman.ui.fx.util.Theme;
import de.amr.games.pacman.ui.fx.util.Ufx;
import de.amr.games.pacman.ui.fx.v3d.PacManGames3dApp;
import de.amr.games.pacman.ui.fx.v3d.model.Model3D;
import javafx.animation.SequentialTransition;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;

import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;

/**
 * @author Armin Reichert
 */
public class GameLevel3D {

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

	public GameLevel3D(GameLevel level, Theme theme, Spritesheet spritesheet) {
		checkLevelNotNull(level);
		checkNotNull(theme);
		checkNotNull(spritesheet);

		this.level = level;

		var pelletModel3D = theme.<Model3D>get("model3D.pellet");
		var pacModel3D    = theme.<Model3D>get("model3D.pacman");
		var ghostModel3D  = theme.<Model3D>get("model3D.ghost");

		switch (level.game().variant()) {
			case MS_PACMAN -> {
				int mazeNumber    = level.game().mazeNumber(level.number());
				var foodColor     = theme.color("mspacman.maze.foodColor",     mazeNumber - 1);
				var wallBaseColor = theme.color("mspacman.maze.wallBaseColor", mazeNumber - 1);
				var wallTopColor  = theme.color("mspacman.maze.wallTopColor",  mazeNumber - 1);
				var doorColor     = theme.color("mspacman.maze.doorColor");
				world3D           = new World3D(level.world(), theme, pelletModel3D, foodColor, wallBaseColor, wallTopColor, doorColor);
				pac3D             = Pac3D.createMsPacMan3D(pacModel3D, theme, level.pac());
				ghosts3D          = level.ghosts().map(ghost -> createGhost3D(ghost, ghostModel3D, theme)).toArray(Ghost3D[]::new);
				livesCounter3D    = new LivesCounter3D(() -> Pac3D.createMsPacManGroup(pacModel3D, theme), true);
			}
			case PACMAN -> {
				var foodColor     = theme.color("pacman.maze.foodColor");
				var wallBaseColor = theme.color("pacman.maze.wallBaseColor");
				var wallTopColor  = theme.color("pacman.maze.wallTopColor");
				var doorColor     = theme.color("pacman.maze.doorColor");
				world3D           = new World3D(level.world(), theme, pelletModel3D, foodColor, wallBaseColor, wallTopColor, doorColor);
				pac3D             = Pac3D.createPacMan3D(pacModel3D, theme, level.pac());
				ghosts3D          = level.ghosts().map(ghost -> createGhost3D(ghost, ghostModel3D, theme)).toArray(Ghost3D[]::new);
				livesCounter3D    = new LivesCounter3D(() -> Pac3D.createPacManGroup(pacModel3D, theme), false);
			}
			default -> throw new IllegalGameVariantException(level.game().variant());
		}

		pacLight       = createPacLight(pac3D);
		levelCounter3D = new LevelCounter3D();
		scores3D       = new Scores3D(theme.font("font.arcade", 8));

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
		// World must be added *after* the guys. Otherwise, a semi-transparent house is not rendered correctly!
		root.getChildren().add(world3D.getRoot());

		pac3D.lightedPy.bind(PacManGames3dApp.PY_3D_PAC_LIGHT_ENABLED);
		ghosts3D[GameModel.RED_GHOST].drawModePy.bind(PacManGames3dApp.PY_3D_DRAW_MODE);
		ghosts3D[GameModel.PINK_GHOST].drawModePy.bind(PacManGames3dApp.PY_3D_DRAW_MODE);
		ghosts3D[GameModel.CYAN_GHOST].drawModePy.bind(PacManGames3dApp.PY_3D_DRAW_MODE);
		ghosts3D[GameModel.ORANGE_GHOST].drawModePy.bind(PacManGames3dApp.PY_3D_DRAW_MODE);
		world3D.drawModePy.bind(PacManGames3dApp.PY_3D_DRAW_MODE);
		world3D.floorColorPy.bind(PacManGames3dApp.PY_3D_FLOOR_COLOR);
		world3D.floorTexturePy.bind(PacManGames3dApp.PY_3D_FLOOR_TEXTURE);
		world3D.wallHeightPy.bind(PacManGames3dApp.PY_3D_WALL_HEIGHT);
		world3D.wallThicknessPy.bind(PacManGames3dApp.PY_3D_WALL_THICKNESS);
		livesCounter3D.drawModePy.bind(PacManGames3dApp.PY_3D_DRAW_MODE);
	}

	private PointLight createPacLight(Pac3D pac3D) {
		var light = new PointLight();
		light.setColor(Color.rgb(255, 255, 0, 0.75));
		light.setMaxRange(2 * TS);
		light.translateXProperty().bind(pac3D.position().xProperty());
		light.translateYProperty().bind(pac3D.position().yProperty());
		light.setTranslateZ(-10);
		return light;
	}

	public void replaceBonus3D(Bonus bonus, Spritesheet spritesheet) {
		checkNotNull(bonus);
		checkNotNull(spritesheet);

		if (bonus3D != null) {
			root.getChildren().remove(bonus3D.getRoot());
		}
		bonus3D = createBonus3D(bonus, spritesheet);
		root.getChildren().add(bonus3D.getRoot());
	}

	private Ghost3D createGhost3D(Ghost ghost, Model3D ghostModel3D, Theme theme) {
		return new Ghost3D(ghost, ghostModel3D, theme, 8.5);
	}

	private Bonus3D createBonus3D(Bonus bonus, Spritesheet spritesheet) {
		switch (level.game().variant()) {
		case MS_PACMAN: {
			var ss = (SpritesheetMsPacManGame) spritesheet;
			var symbolImage = spritesheet.subImage(ss.bonusSymbolSprite(bonus.symbol()));
			var pointsImage = spritesheet.subImage(ss.bonusValueSprite(bonus.symbol()));
			return new Bonus3D(bonus, symbolImage, pointsImage);
		}
		case PACMAN: {
			var ss = (SpritesheetPacManGame) spritesheet;
			var symbolImage = spritesheet.subImage(ss.bonusSymbolSprite(bonus.symbol()));
			var pointsImage = spritesheet.subImage(ss.bonusValueSprite(bonus.symbol()));
			return new Bonus3D(bonus, symbolImage, pointsImage);
		}
		default:
			throw new IllegalGameVariantException(level.game().variant());
		}
	}

	private void updatePacLight() {
		var pac = level.pac();
		boolean isVisible = pac.isVisible();
		boolean isAlive = !pac.isDead();
		boolean hasPower = pac.powerTimer().isRunning();
		double radius = 0;
		if (pac.powerTimer().duration() > 0) {
			double t = ((double) pac.powerTimer().remaining() / pac.powerTimer().duration());
			radius = t * 6 * TS;
		}
		pacLight.setMaxRange(hasPower ? 2 * TS + radius : 0);
		pacLight.setLightOn(pac3D.lightedPy.get() && isVisible && isAlive && hasPower);
	}

	public void update() {
		pac3D.update();
		Stream.of(ghosts3D).forEach(Ghost3D::update);
		if (bonus3D != null) {
			bonus3D.update(level);
		}
		boolean hideOneLife = level.pac().isVisible() || GameController.it().state() == GameState.GHOST_DYING;
		int numLivesShown = hideOneLife ? level.game().lives() - 1 : level.game().lives();
		livesCounter3D.update(numLivesShown);
		livesCounter3D.getRoot().setVisible(GameController.it().hasCredit());
		scores3D.update(level);
		if (GameController.it().hasCredit()) {
			scores3D.setShowPoints(true);
		} else {
			scores3D.setShowText(Color.RED, "GAME OVER!");
		}
		updatePacLight();
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
		if (eatenAnimation.isPresent() && PacManGames3dApp.PY_3D_ENERGIZER_EXPLODES.get()) {
			new SequentialTransition(delayHiding, eatenAnimation.get()).play();
		} else {
			delayHiding.play();
		}
	}

	private void updateHouseState() {
		boolean isGhostNearHouse = level.ghosts(GhostState.LOCKED, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
				.anyMatch(Ghost::isVisible);
		boolean accessGranted = isAccessGranted(level.ghosts(), level.world().house().door());
		if (accessGranted) {
			world3D.doorWings3D().forEach(DoorWing3D::open);
		}
		world3D.houseLighting().setLightOn(isGhostNearHouse);
	}

	private boolean isAccessGranted(Stream<Ghost> ghosts, Door door) {
		return ghosts.anyMatch(ghost -> ghost.isVisible()
				&& ghost.is(GhostState.RETURNING_TO_HOUSE, GhostState.ENTERING_HOUSE, GhostState.LEAVING_HOUSE)
				&& ghost.position().euclideanDistance(door.entryPosition()) <= 1.5 * TS);
	}

	public GameLevel level() {
		return level;
	}

	public Group root() {
		return root;
	}

	public LivesCounter3D livesCounter3D() {
		return livesCounter3D;
	}

	public LevelCounter3D levelCounter3D() {
		return levelCounter3D;
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

	public Ghost3D ghost3D(byte id) {
		Globals.checkGhostID(id);
		return ghosts3D[id];
	}

	public Bonus3D bonus3D() {
		return bonus3D;
	}

	public Scores3D scores3D() {
		return scores3D;
	}
}