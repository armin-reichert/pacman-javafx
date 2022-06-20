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
import java.util.stream.Stream;

import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.ui.fx._3d.entity.Bonus3D;
import de.amr.games.pacman.ui.fx._3d.entity.Ghost3D;
import de.amr.games.pacman.ui.fx._3d.entity.Ghost3D.AnimationMode;
import de.amr.games.pacman.ui.fx._3d.entity.Pac3D;
import de.amr.games.pacman.ui.fx._3d.entity.World3D;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.app.Talk;
import de.amr.games.pacman.ui.fx.shell.Actions;
import de.amr.games.pacman.ui.fx.shell.Keyboard;
import de.amr.games.pacman.ui.fx.util.U;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

/**
 * 3D play scene with sound and animations.
 * 
 * @author Armin Reichert
 */
public class PlayScene3D extends GameScene3D {

	private final EnumMap<Perspective, GameSceneCamera> cameras = new EnumMap<>(Perspective.class);

	private World3D world3D;
	private Pac3D pac3D;
	private Ghost3D[] ghosts3D;
	private Bonus3D bonus3D;

	public PlayScene3D() {
		createPerspectives();
	}

	@Override
	public void init() {
		sceneContent.getChildren().clear();
		createWorld3D();
		// must be at fixed child position it is exchanged when new level starts!
		sceneContent.getChildren().add(world3D.getRoot());
		pac3D = new Pac3D($.game.pac, $.model3D);
		sceneContent.getChildren().add(pac3D);
		ghosts3D = $.game.ghosts().map(ghost -> new Ghost3D(ghost, $.model3D, $.r2D)).toArray(Ghost3D[]::new);
		sceneContent.getChildren().addAll(ghosts3D);
		bonus3D = new Bonus3D();
		sceneContent.getChildren().add(bonus3D);
		setPerspective(Env.$perspective.get());
	}

	public void createWorld3D() {
		world3D = new World3D($.game, $.model3D, $.r2D);
		var maze3D = world3D.getMaze3D();
		maze3D.setFloorSolidColor(Color.rgb(5, 5, 10));
		maze3D.setFloorTexture(U.image("/common/escher-texture.jpg"));
		maze3D.setFloorTextureColor(Color.rgb(51, 0, 102));
		maze3D.wallHeight.bind(Env.$mazeWallHeight);
		maze3D.resolution.bind(Env.$mazeResolution);
		maze3D.floorHasTexture.bind(Env.$mazeFloorHasTexture);
	}

	private void createPerspectives() {
		cameras.put(Perspective.CAM_DRONE, new CamDrone());
		cameras.put(Perspective.CAM_FOLLOWING_PLAYER, new CamFollowingPlayer());
		cameras.put(Perspective.CAM_NEAR_PLAYER, new CamNearPlayer());
		cameras.put(Perspective.CAM_TOTAL, new CamTotal());
		Env.$perspective.addListener((obs, oldVal, newVal) -> setPerspective(newVal));
	}

	private void setPerspective(Perspective psp) {
		var camera = cameras.get(psp);
		camera.reset();
		setCamera(camera);
		var scores3D = world3D.getScores3D();
		if (scores3D != null) {
			// keep the score in plain sight
			scores3D.rotationAxisProperty().bind(camera.rotationAxisProperty());
			scores3D.rotateProperty().bind(camera.rotateProperty());
		}
	}

	@Override
	public void onKeyPressed() {
		if (Keyboard.pressed(Keyboard.ALT, KeyCode.LEFT)) {
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
		pac3D.update();
		Stream.of($.game.ghosts).forEach(this::updateGhost3D);
		bonus3D.update($.game.bonus());
		getCamera().update(pac3D);
	}

	private void updateGhost3D(Ghost ghost) {
		var ghost3D = ghosts3D[ghost.id];
		if (ghost.killIndex != -1) {
			ghost3D.setAnimationMode(AnimationMode.NUMBER);
		} else if (ghost.is(GhostState.DEAD) || ghost.is(GhostState.ENTERING_HOUSE)) {
			ghost3D.setAnimationMode(AnimationMode.EYES);
		} else if ($.game.pac.hasPower() && !ghost.is(GhostState.LEAVING_HOUSE)) {
			ghost3D.setAnimationMode(AnimationMode.FRIGHTENED);
		} else {
			ghost3D.setAnimationMode(AnimationMode.COLORED);
		}
		ghost3D.update();
	}

	public void onSwitchFrom2D() {
		var maze3D = world3D.getMaze3D();
		if ($.game.pac.hasPower()) {
			Stream.of(ghosts3D) //
					.filter(ghost3D -> U.oneOf(ghost3D.ghost.state, GhostState.FRIGHTENED, GhostState.LOCKED))
					.forEach(ghost3D -> ghost3D.setAnimationMode(AnimationMode.FRIGHTENED));
		}
		maze3D.validateFoodNodes();
		if (U.oneOf($.gameState(), GameState.HUNTING, GameState.GHOST_DYING)) {
			maze3D.energizerAnimations().forEach(Animation::play);
		}
	}

	@Override
	public void onPlayerGetsPower(GameEvent e) {
		Stream.of(ghosts3D) //
				.filter(ghost3D -> U.oneOf(ghost3D.ghost.state, GhostState.FRIGHTENED, GhostState.LOCKED))
				.forEach(ghost3D -> ghost3D.setAnimationMode(AnimationMode.FRIGHTENED));
	}

	@Override
	public void onPlayerStartsLosingPower(GameEvent e) {
		Stream.of(ghosts3D) //
				.filter(ghost3D -> U.oneOf(ghost3D.ghost.state, GhostState.FRIGHTENED, GhostState.LOCKED))
				.forEach(Ghost3D::playFlashingAnimation);
	}

	@Override
	public void onPlayerLosesPower(GameEvent e) {
		Stream.of(ghosts3D).forEach(ghost3D -> ghost3D.setAnimationMode(AnimationMode.COLORED));
	}

	@Override
	public void onPlayerFindsFood(GameEvent e) {
		var maze3D = world3D.getMaze3D();
		// when cheat "eat all pellets" is used, no tile is present
		if (!e.tile.isPresent()) {
			$.game.level.world.tiles().filter($.game.level.world::containsEatenFood)
					.forEach(tile -> maze3D.foodAt(tile).ifPresent(maze3D::hideFood));
		} else {
			maze3D.foodAt(e.tile.get()).ifPresent(maze3D::hideFood);
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
	public void onGhostStartsLeavingHouse(GameEvent e) {
		e.ghost.ifPresent(ghost -> ghosts3D[ghost.id].playRevivalAnimation());
	}

	@Override
	public void onGameStateChange(GameStateChangeEvent e) {
		var maze3D = world3D.getMaze3D();
		switch (e.newGameState) {
		case READY -> {
			maze3D.reset();
			pac3D.reset();
			Stream.of(ghosts3D).forEach(Ghost3D::reset);
		}
		case HUNTING -> maze3D.energizerAnimations().forEach(Animation::play);
		case PACMAN_DYING -> {
			blockGameController();
			Stream.of(ghosts3D).forEach(ghost3D -> ghost3D.setAnimationMode(AnimationMode.COLORED));
			$.game.ghosts().filter(ghost -> ghost.sameTile($.game.pac)).findAny()
					.ifPresent(killer -> new SequentialTransition( //
							pac3D.dyingAnimation($.r2D.getGhostColor(killer.id)), //
							U.pauseSec(2.0, this::unblockGameController) //
					).play());
		}
		case LEVEL_STARTING -> {
			blockGameController();
			createWorld3D();
			sceneContent.getChildren().set(0, world3D.getRoot());
			Actions.showFlashMessage(Talk.message("level_starting", $.game.level.number));
			U.pauseSec(3, this::unblockGameController).play();
		}
		case LEVEL_COMPLETE -> {
			blockGameController();
			Stream.of(ghosts3D).forEach(ghost3D -> ghost3D.setAnimationMode(AnimationMode.COLORED));
			var message = Talk.LEVEL_COMPLETE_TALK.next() + "\n\n" + Talk.message("level_complete", $.game.level.number);
			new SequentialTransition( //
					U.pauseSec(2.0), //
					maze3D.createMazeFlashingAnimation($.game.level.numFlashes), //
					U.pauseSec(1.0, $.game.pac::hide), //
					U.pauseSec(0.5, () -> Actions.showFlashMessage(2, message)), //
					U.pauseSec(2.0, this::unblockGameController) //
			).play();
		}
		case GAME_OVER -> Actions.showFlashMessage(3, Talk.GAME_OVER_TALK.next());
		default -> { // ignore
		}
		}

		// exit HUNTING
		if (e.oldGameState == GameState.HUNTING && e.newGameState != GameState.GHOST_DYING) {
			maze3D.energizerAnimations().forEach(Animation::stop);
			bonus3D.setVisible(false);
		}
	}
}