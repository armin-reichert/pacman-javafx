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

import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.lib.U;
import de.amr.games.pacman.model.common.GameSound;
import de.amr.games.pacman.ui.fx._3d.animation.Rendering3D;
import de.amr.games.pacman.ui.fx._3d.entity.Bonus3D;
import de.amr.games.pacman.ui.fx._3d.entity.Energizer3D;
import de.amr.games.pacman.ui.fx._3d.entity.Ghost3D;
import de.amr.games.pacman.ui.fx._3d.entity.Pac3D;
import de.amr.games.pacman.ui.fx._3d.entity.Pellet3D;
import de.amr.games.pacman.ui.fx._3d.entity.World3D;
import de.amr.games.pacman.ui.fx._3d.scene.cams.CamDrone;
import de.amr.games.pacman.ui.fx._3d.scene.cams.CamFirstPerson;
import de.amr.games.pacman.ui.fx._3d.scene.cams.CamFollowingPlayer;
import de.amr.games.pacman.ui.fx._3d.scene.cams.CamNearPlayer;
import de.amr.games.pacman.ui.fx._3d.scene.cams.CamTotal;
import de.amr.games.pacman.ui.fx._3d.scene.cams.GameSceneCamera;
import de.amr.games.pacman.ui.fx._3d.scene.cams.Perspective;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.SceneContext;
import de.amr.games.pacman.ui.fx.shell.Actions;
import de.amr.games.pacman.ui.fx.shell.Keyboard;
import de.amr.games.pacman.ui.fx.texts.Texts;
import de.amr.games.pacman.ui.fx.util.CoordSystem;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.SequentialTransition;
import javafx.beans.binding.DoubleExpression;
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

	private final Map<Perspective, GameSceneCamera> cameraMap = new EnumMap<>(Perspective.class);
	private final SubScene fxSubScene;
	private final Group contentRoot = new Group();
	private final AmbientLight light;
	private final CoordSystem coordSystem;

	private SceneContext ctx;
	private World3D world3D;
	private Pac3D pac3D;
	private Ghost3D[] ghosts3D;
	private Bonus3D bonus3D;

	public PlayScene3D() {

		cameraMap.put(Perspective.DRONE, new CamDrone());
		cameraMap.put(Perspective.FIRST_PERSON, new CamFirstPerson());
		cameraMap.put(Perspective.FOLLOWING_PLAYER, new CamFollowingPlayer());
		cameraMap.put(Perspective.NEAR_PLAYER, new CamNearPlayer());
		cameraMap.put(Perspective.TOTAL, new CamTotal());
		Env.perspectivePy.addListener((obs, oldVal, newVal) -> changeCamera(newVal));

		coordSystem = new CoordSystem(1000);
		coordSystem.visibleProperty().bind(Env.axesVisiblePy);

		light = new AmbientLight(Env.lightColorPy.get());
		light.colorProperty().bind(Env.lightColorPy);

		// origin is at center of scene content
		contentRoot.getTransforms().add(new Translate(-DEFAULT_WIDTH / 2, -DEFAULT_HEIGHT / 2));
		// initial size does not matter, subscene is resized automatically
		fxSubScene = new SubScene(new Group(contentRoot, coordSystem, light), 50, 50, true, SceneAntialiasing.BALANCED);
	}

	@Override
	public boolean is3D() {
		return true;
	}

	@Override
	public void init() {
		var content = contentRoot.getChildren();
		content.clear();
		world3D = new World3D(ctx.game(), ctx.model3D(), ctx.r2D());
		// put first, exchanged when new level starts
		content.add(world3D);
		pac3D = new Pac3D(ctx.game().pac, ctx.game().world(), ctx.model3D(), Rendering3D.getPacSkullColor(),
				Rendering3D.getPacEyesColor(), Rendering3D.getPacPalateColor());
		pac3D.reset();
		content.add(pac3D);
		ghosts3D = ctx.game().ghosts().map(ghost -> new Ghost3D(ghost, ctx.model3D(), ctx.r2D())).toArray(Ghost3D[]::new);
		Stream.of(ghosts3D).forEach(content::add);
		bonus3D = new Bonus3D(ctx.game().bonus());
		content.add(bonus3D);

		var fpc = (CamFirstPerson) getCameraForPerspective(Perspective.FIRST_PERSON);
		fpc.setPac(ctx.game().pac);
		changeCamera(Env.perspectivePy.get());
	}

	@Override
	public void onKeyPressed() {
		if (Keyboard.pressed(KeyCode.DIGIT5) && !ctx.game().hasCredit()) {
			Actions.addCredit();
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
	public void updateAndRender() {
		world3D.update(ctx.game());
		pac3D.update();
		Stream.of(ghosts3D).forEach(ghost3D -> ghost3D.update(ctx.game()));
		bonus3D.update();
		getCamera().update(pac3D);
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
	public void setResizeBehavior(DoubleExpression width, DoubleExpression height) {
		fxSubScene.widthProperty().bind(width);
		fxSubScene.heightProperty().bind(height);
	}

	public GameSceneCamera getCameraForPerspective(Perspective perspective) {
		return cameraMap.get(perspective);
	}

	public GameSceneCamera getCamera() {
		return (GameSceneCamera) fxSubScene.getCamera();
	}

	private void changeCamera(Perspective perspective) {
		var oldCamera = fxSubScene.getCamera();
		var camera = cameraMap.get(perspective);
		if (camera != oldCamera) {
			fxSubScene.setCamera(camera);
			fxSubScene.setOnKeyPressed(camera::onKeyPressed);
			fxSubScene.requestFocus();
			camera.reset();
		}
		if (world3D != null && world3D.getScores3D() != null) {
			var scores3D = world3D.getScores3D();
			scores3D.rotationAxisProperty().bind(camera.rotationAxisProperty());
			scores3D.rotateProperty().bind(camera.rotateProperty());
		}
	}

	private void blockGameController() {
		ctx.state().timer().resetIndefinitely();
	}

	private void unblockGameController() {
		ctx.state().timer().expire();
	}

	public void onSwitchFrom2D() {
		var world = ctx.game().world();
		world3D.getFood3D().pellets3D().forEach(shape3D -> shape3D.setVisible(!world.containsEatenFood(shape3D.tile())));
		if (U.oneOf(ctx.state(), GameState.HUNTING, GameState.GHOST_DYING)) {
			world3D.getFood3D().energizers3D().forEach(Energizer3D::startPumping);
		}
	}

	@Override
	public void onPlayerFindsFood(GameEvent e) {
		var world = ctx.game().world();
		// when cheat "eat all pellets" is used, no tile is present in the event
		if (e.tile.isEmpty()) {
			world.tiles()//
					.filter(world::containsEatenFood)//
					.map(world3D.getFood3D()::pelletAt)//
					.filter(Optional::isPresent)//
					.map(Optional::get)//
					.forEach(Pellet3D::eat);
		} else {
			world3D.getFood3D().pelletAt(e.tile.get()).ifPresent(world3D.getFood3D()::eatPellet);
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
					Ufx.pauseSec(0.0, this::blockGameController), //
					pac3D.createDyingAnimation(color), //
					Ufx.pauseSec(2.0, this::unblockGameController) //
			).play();
		});

		case LEVEL_STARTING -> {
			blockGameController();
			world3D = new World3D(game, ctx.model3D(), ctx.r2D());
			contentRoot.getChildren().set(0, world3D);
			changeCamera(Env.perspectivePy.get());
			Actions.showFlashMessage(Texts.message("level_starting", game.level.number));
			Ufx.pauseSec(3, this::unblockGameController).play();
		}

		case LEVEL_COMPLETE -> {
			var message = Texts.TALK_LEVEL_COMPLETE.next() + "%n%n" + Texts.message("level_complete", game.level.number);
			new SequentialTransition( //
					Ufx.pauseSec(0.0, this::blockGameController), //
					Ufx.pauseSec(2.0), //
					world3D.getMaze3D().createMazeFlashingAnimation(game.level.numFlashes), //
					Ufx.pauseSec(1.0, game.pac::hide), //
					Ufx.pauseSec(0.5, () -> Actions.showFlashMessage(2, message)), //
					Ufx.pauseSec(2.0, this::unblockGameController) //
			).play();
		}

		case GAME_OVER -> Actions.showFlashMessage(3, Texts.TALK_GAME_OVER.next());

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