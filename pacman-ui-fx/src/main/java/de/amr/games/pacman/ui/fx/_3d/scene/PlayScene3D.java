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
package de.amr.games.pacman.ui.fx._3d.scene;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.lib.U;
import de.amr.games.pacman.ui.fx._3d.animation.Rendering3D;
import de.amr.games.pacman.ui.fx._3d.entity.Bonus3D;
import de.amr.games.pacman.ui.fx._3d.entity.Energizer3D;
import de.amr.games.pacman.ui.fx._3d.entity.Ghost3D;
import de.amr.games.pacman.ui.fx._3d.entity.Pac3D;
import de.amr.games.pacman.ui.fx._3d.entity.World3D;
import de.amr.games.pacman.ui.fx._3d.scene.cams.CamDrone;
import de.amr.games.pacman.ui.fx._3d.scene.cams.CamFollowingPlayer;
import de.amr.games.pacman.ui.fx._3d.scene.cams.CamNearPlayer;
import de.amr.games.pacman.ui.fx._3d.scene.cams.CamTotal;
import de.amr.games.pacman.ui.fx._3d.scene.cams.GameSceneCamera;
import de.amr.games.pacman.ui.fx._3d.scene.cams.Perspective;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.app.Talk;
import de.amr.games.pacman.ui.fx.shell.Actions;
import de.amr.games.pacman.ui.fx.shell.Keyboard;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.SequentialTransition;
import javafx.scene.input.KeyCode;

/**
 * 3D play scene with sound and animations.
 * 
 * @author Armin Reichert
 */
public class PlayScene3D extends GameScene3D {

	private final Map<Perspective, GameSceneCamera> cameraMap = new EnumMap<>(Perspective.class);

	private World3D world3D;
	private Pac3D pac3D;
	private Ghost3D[] ghosts3D;
	private Bonus3D bonus3D;

	public PlayScene3D() {
		cameraMap.put(Perspective.DRONE, new CamDrone());
		cameraMap.put(Perspective.FOLLOWING_PLAYER, new CamFollowingPlayer());
		cameraMap.put(Perspective.NEAR_PLAYER, new CamNearPlayer());
		cameraMap.put(Perspective.TOTAL, new CamTotal());
		Env.perspective.addListener((obs, oldVal, newVal) -> setPerspective(newVal));
	}

	@Override
	public void init() {
		content().clear();
		world3D = new World3D($.game, $.model3D, $.r2D);
		// put first, exchanged when new level starts
		content().add(world3D);
		pac3D = new Pac3D($.game.pac, $.game.world(), $.model3D, Rendering3D.getPacSkullColor(),
				Rendering3D.getPacEyesColor(), Rendering3D.getPacPalateColor());
		content().add(pac3D);
		ghosts3D = $.game.ghosts().map(ghost -> new Ghost3D(ghost, $.model3D, $.r2D)).toArray(Ghost3D[]::new);
		Stream.of(ghosts3D).forEach(content()::add);
		bonus3D = new Bonus3D();
		content().add(bonus3D);
		setPerspective(Env.perspective.get());
	}

	public void setPerspective(Perspective perspective) {
		var camera = cameraMap.get(perspective);
		camera.reset();
		setCamera(camera);
		if (world3D != null && world3D.getScores3D() != null) {
			var scores3D = world3D.getScores3D();
			scores3D.rotationAxisProperty().bind(camera.rotationAxisProperty());
			scores3D.rotateProperty().bind(camera.rotateProperty());
		}
	}

	@Override
	public void onKeyPressed() {
		if (Keyboard.pressed(KeyCode.DIGIT5) && $.game.credit == 0) {
			$.gameState().addCredit($.game);
		} else if (Keyboard.pressed(Keyboard.ALT, KeyCode.LEFT)) {
			Actions.selectPrevPerspective();
		} else if (Keyboard.pressed(Keyboard.ALT, KeyCode.RIGHT)) {
			Actions.selectNextPerspective();
		} else if (Keyboard.pressed(Keyboard.ALT, KeyCode.E)) {
			Actions.cheatEatAllPellets();
		} else if (Keyboard.pressed(Keyboard.ALT, KeyCode.L)) {
			Actions.addLives(3);
		} else if (Keyboard.pressed(Keyboard.ALT, KeyCode.N)) {
			Actions.cheatEnterNextLevel();
		} else if (Keyboard.pressed(Keyboard.ALT, KeyCode.X)) {
			Actions.cheatKillAllEatableGhosts();
		}
	}

	@Override
	public void update() {
		world3D.update($.game);
		pac3D.update($.game.world());
		Stream.of(ghosts3D).forEach(ghost3D -> ghost3D.update($.game));
		bonus3D.update($.game.bonus());
		getCamera().update(pac3D);
	}

	public void onSwitchFrom2D() {
		var maze3D = world3D.getMaze3D();
		maze3D.validateFoodShapes();
		if (U.oneOf($.gameState(), GameState.HUNTING, GameState.GHOST_DYING)) {
			maze3D.energizers().forEach(Energizer3D::startBlinking);
		}
	}

	@Override
	public void onPlayerFindsFood(GameEvent e) {
		var maze3D = world3D.getMaze3D();
		if (e.tile.isEmpty()) {
			// when cheat "eat all pellets" is used, no tile is present in the event
			$.game.level.world.tiles().filter($.game.level.world::containsEatenFood)
					.forEach(tile -> maze3D.foodAt(tile).ifPresent(maze3D::eatFood));
		} else {
			maze3D.foodAt(e.tile.get()).ifPresent(maze3D::eatFood);
		}
	}

	@Override
	public void onBonusGetsActive(GameEvent e) {
		bonus3D.showSymbol($.game.bonus(), $.r2D);
	}

	@Override
	public void onBonusGetsEaten(GameEvent e) {
		bonus3D.showPoints($.game.bonus(), $.r2D);
	}

	@Override
	public void onBonusExpires(GameEvent e) {
		bonus3D.setVisible(false);
	}

	@Override
	public void onGameStateChange(GameStateChangeEvent e) {
		var maze3D = world3D.getMaze3D();
		switch (e.newGameState) {
		case READY -> {
			maze3D.reset();
			pac3D.reset($.game.world());
			Stream.of(ghosts3D).forEach(ghost3D -> ghost3D.reset($.game));
		}
		case HUNTING -> maze3D.energizers().forEach(Energizer3D::startBlinking);
		case PACMAN_DYING -> {
			blockGameController();
			$.game.ghosts().filter(ghost -> ghost.sameTile($.game.pac)).findAny()
					.ifPresent(killer -> new SequentialTransition( //
							pac3D.createDyingAnimation($.r2D.getGhostColor(killer.id)), //
							Ufx.pauseSec(2.0, this::unblockGameController) //
					).play());
		}
		case LEVEL_STARTING -> {
			blockGameController();
			world3D = new World3D($.game, $.model3D, $.r2D);
			content().set(0, world3D);
			setPerspective(Env.perspective.get());
			Actions.showFlashMessage(Talk.message("level_starting", $.game.level.number));
			Ufx.pauseSec(3, this::unblockGameController).play();
		}
		case LEVEL_COMPLETE -> {
			blockGameController();
			var message = Talk.LEVEL_COMPLETE_TALK.next() + "%n%n" + Talk.message("level_complete", $.game.level.number);
			new SequentialTransition( //
					Ufx.pauseSec(2.0), //
					maze3D.createMazeFlashingAnimation($.game.level.numFlashes), //
					Ufx.pauseSec(1.0, $.game.pac::hide), //
					Ufx.pauseSec(0.5, () -> Actions.showFlashMessage(2, message)), //
					Ufx.pauseSec(2.0, this::unblockGameController) //
			).play();
		}
		case GAME_OVER -> Actions.showFlashMessage(3, Talk.GAME_OVER_TALK.next());
		default -> { // ignore
		}
		}

		// exit HUNTING
		if (e.oldGameState == GameState.HUNTING && e.newGameState != GameState.GHOST_DYING) {
			maze3D.energizers().forEach(Energizer3D::stopBlinking);
			bonus3D.setVisible(false);
		}
	}
}