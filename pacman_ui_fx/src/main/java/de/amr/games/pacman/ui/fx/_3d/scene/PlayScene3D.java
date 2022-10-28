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
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.lib.U;
import de.amr.games.pacman.model.common.GameSound;
import de.amr.games.pacman.ui.fx.Env;
import de.amr.games.pacman.ui.fx._3d.animation.Rendering3D;
import de.amr.games.pacman.ui.fx._3d.entity.Bonus3D;
import de.amr.games.pacman.ui.fx._3d.entity.Energizer3D;
import de.amr.games.pacman.ui.fx._3d.entity.Ghost3D;
import de.amr.games.pacman.ui.fx._3d.entity.Pac3D;
import de.amr.games.pacman.ui.fx._3d.entity.Pellet3D;
import de.amr.games.pacman.ui.fx._3d.entity.World3D;
import de.amr.games.pacman.ui.fx._3d.scene.cams.CamDrone;
import de.amr.games.pacman.ui.fx._3d.scene.cams.CamFollowingPlayer;
import de.amr.games.pacman.ui.fx._3d.scene.cams.CamNearPlayer;
import de.amr.games.pacman.ui.fx._3d.scene.cams.CamTotal;
import de.amr.games.pacman.ui.fx._3d.scene.cams.GameSceneCamera;
import de.amr.games.pacman.ui.fx._3d.scene.cams.Perspective;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.SceneContext;
import de.amr.games.pacman.ui.fx.shell.Actions;
import de.amr.games.pacman.ui.fx.util.CoordSystem;
import de.amr.games.pacman.ui.fx.util.Keyboard;
import de.amr.games.pacman.ui.fx.util.Modifier;
import de.amr.games.pacman.ui.fx.util.TextManager;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.SequentialTransition;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.KeyCode;
import javafx.scene.transform.Translate;

/**
 * 3D play scene with sound and animations.
 * 
 * @author Armin Reichert
 */
public class PlayScene3D implements GameScene {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	private final SubScene fxSubScene;
	private final Group content = new Group();
	private final Map<Perspective, GameSceneCamera> cameraMap = new EnumMap<>(Perspective.class);

	private SceneContext ctx;
	private World3D world3D;
	private Pac3D pac3D;
	private Ghost3D[] ghosts3D;
	private Bonus3D bonus3D;

	public PlayScene3D() {
		var coordSystem = new CoordSystem();
		coordSystem.visibleProperty().bind(Env.axesVisiblePy);

		var light = new AmbientLight();
		light.colorProperty().bind(Env.lightColorPy);

		var root = new Group(content, coordSystem, light);

		// initial scene size is irrelevant as it is resized automatically
		fxSubScene = new SubScene(root, 1, 1, true, SceneAntialiasing.BALANCED);

		// center scene content over origin
		content.getTransforms().add(new Translate(-DEFAULT_SIZE.x() / 2, -DEFAULT_SIZE.y() / 2));

		createCameras();
	}

	private void createCameras() {
		cameraMap.put(Perspective.DRONE, new CamDrone());
		cameraMap.put(Perspective.FOLLOWING_PLAYER, new CamFollowingPlayer());
		cameraMap.put(Perspective.NEAR_PLAYER, new CamNearPlayer());
		cameraMap.put(Perspective.TOTAL, new CamTotal());
		Env.perspectivePy.addListener((obs, oldVal, newPerspective) -> changeCamera(newPerspective));
	}

	@Override
	public boolean is3D() {
		return true;
	}

	@Override
	public void init() {
		final var game = ctx.game();
		world3D = new World3D(game, ctx.model3D(), ctx.r2D());
		pac3D = new Pac3D(game.pac, game.world(), ctx.model3D(), Rendering3D.getPacSkullColor(),
				Rendering3D.getPacEyesColor(), Rendering3D.getPacPalateColor());
		pac3D.reset();
		ghosts3D = game.ghosts().map(ghost -> new Ghost3D(ghost, ctx.model3D(), ctx.r2D())).toArray(Ghost3D[]::new);
		bonus3D = new Bonus3D(game.bonus());

		content.getChildren().clear();
		// put world first in content list, will get exchanged everytime a new level starts
		content.getChildren().add(world3D);
		content.getChildren().add(pac3D);
		content.getChildren().addAll(ghosts3D);
		content.getChildren().add(bonus3D);

		changeCamera(Env.perspectivePy.get());
	}

	@Override
	public void onKeyPressed() {
		if (Keyboard.pressed(KeyCode.DIGIT5) && !ctx.game().hasCredit()) {
			// when in attract mode, allow adding credit
			Actions.addCredit();
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.LEFT)) {
			Actions.selectPrevPerspective();
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.RIGHT)) {
			Actions.selectNextPerspective();
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.E)) {
			Actions.cheatEatAllPellets();
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.L)) {
			Actions.cheatAddLives(3);
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.N)) {
			Actions.cheatEnterNextLevel();
		} else if (Keyboard.pressed(Modifier.ALT, KeyCode.X)) {
			Actions.cheatKillAllEatableGhosts();
		}
	}

	@Override
	public void resize(double height) {
		// nothing to do
	}

	@Override
	public void updateAndRender() {
		world3D.update(ctx.game());
		pac3D.update();
		Stream.of(ghosts3D).forEach(ghost3D -> ghost3D.update(ctx.game()));
		bonus3D.update();
		currentCamera().update(pac3D);
	}

	@Override
	public SceneContext getSceneContext() {
		return ctx;
	}

	@Override
	public void setSceneContext(SceneContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public SubScene getFXSubScene() {
		return fxSubScene;
	}

	@Override
	public void setResizeBehavior(ObservableDoubleValue width, ObservableDoubleValue height) {
		fxSubScene.widthProperty().bind(width);
		fxSubScene.heightProperty().bind(height);
	}

	public GameSceneCamera getCamera(Perspective perspective) {
		return cameraMap.get(perspective);
	}

	public GameSceneCamera currentCamera() {
		return (GameSceneCamera) fxSubScene.getCamera();
	}

	private void changeCamera(Perspective newPerspective) {
		var newCamera = getCamera(newPerspective);
		if (newCamera == null) {
			LOGGER.error("No camera found for perspective %s", newPerspective);
			return;
		}
		if (newCamera != currentCamera()) {
			fxSubScene.setCamera(newCamera);
			fxSubScene.setOnKeyPressed(newCamera::onKeyPressed);
			fxSubScene.requestFocus();
			newCamera.reset();
		}
		if (world3D != null && world3D.getScores3D() != null) {
			var scores3D = world3D.getScores3D();
			scores3D.rotationAxisProperty().bind(newCamera.rotationAxisProperty());
			scores3D.rotateProperty().bind(newCamera.rotateProperty());
		}
	}

	public void onSwitchFrom2D() {
		var world = ctx.game().world();
		world3D.getFood3D().pellets3D().forEach(pellet3D -> pellet3D.setVisible(!world.containsEatenFood(pellet3D.tile())));
		if (U.oneOf(ctx.state(), GameState.HUNTING, GameState.GHOST_DYING)) {
			world3D.getFood3D().energizers3D().forEach(Energizer3D::startPumping);
		}
	}

	@Override
	public void onPlayerFindsFood(GameEvent e) {
		var food3D = world3D.getFood3D();
		if (e.tile.isEmpty()) {
			// when cheat "eat all pellets" is used, no tile is present in the event
			// remove 3D pellets to be in synch with model:
			var world = ctx.game().world();
			world.tiles()//
					.filter(world::containsEatenFood)//
					.map(food3D::pelletAt)//
					.filter(Optional::isPresent)//
					.map(Optional::get)//
					.forEach(Pellet3D::eat);
		} else {
			var tile = e.tile.get();
			food3D.pelletAt(tile).ifPresent(food3D::eatPellet);
		}
	}

	@Override
	public void onPlayerGetsExtraLife(GameEvent e) {
		ctx.sounds().play(GameSound.EXTRA_LIFE);
	}

	@Override
	public void onBonusGetsActive(GameEvent e) {
		bonus3D.showSymbol(ctx.r2D());
	}

	@Override
	public void onBonusGetsEaten(GameEvent e) {
		bonus3D.showPoints(ctx.r2D());
		ctx.sounds().play(GameSound.BONUS_EATEN);
	}

	@Override
	public void onBonusExpires(GameEvent e) {
		bonus3D.setVisible(false);
	}

	@Override
	public void onGameStateChange(GameStateChangeEvent e) {
		var game = ctx.game();

		switch (e.newGameState) {

		case READY -> {
			world3D.reset();
			pac3D.reset();
			Stream.of(ghosts3D).forEach(ghost3D -> ghost3D.reset(ctx.game()));
		}

		case HUNTING -> world3D.getFood3D().energizers3D().forEach(Energizer3D::startPumping);

		case PACMAN_DYING -> game.ghosts().filter(game.pac::sameTile).findAny().ifPresent(killer -> {
			var color = ctx.r2D().getGhostColor(killer.id);
			new SequentialTransition( //
					Ufx.pauseSec(0.0, this::lockGameState), //
					pac3D.createDyingAnimation(color), //
					Ufx.pauseSec(2.0, this::unlockGameState) //
			).play();
		});

		case LEVEL_STARTING -> {
			lockGameState();
			world3D = new World3D(game, ctx.model3D(), ctx.r2D());
			content.getChildren().set(0, world3D);
			changeCamera(Env.perspectivePy.get());
			Actions.showFlashMessage(TextManager.message("level_starting", game.level.number()));
			Ufx.pauseSec(3, this::unlockGameState).play();
		}

		case LEVEL_COMPLETE -> {
			var message = TextManager.TALK_LEVEL_COMPLETE.next() + "%n%n"
					+ TextManager.message("level_complete", game.level.number());
			new SequentialTransition( //
					Ufx.pauseSec(0.0, this::lockGameState), //
					Ufx.pauseSec(2.0), //
					world3D.getMaze3D().createMazeFlashingAnimation(game.level.numFlashes()), //
					Ufx.pauseSec(1.0, game.pac::hide), //
					Ufx.pauseSec(0.5, () -> Actions.showFlashMessage(2, message)), //
					Ufx.pauseSec(2.0, this::unlockGameState) //
			).play();
		}

		case GAME_OVER -> Actions.showFlashMessage(3, TextManager.TALK_GAME_OVER.next());

		default -> {
			// ignore
		}

		}

		// exit HUNTING
		if (e.oldGameState == GameState.HUNTING && e.newGameState != GameState.GHOST_DYING) {
			world3D.getFood3D().energizers3D().forEach(Energizer3D::stopPumping);
			bonus3D.setVisible(false);
		}
	}
}