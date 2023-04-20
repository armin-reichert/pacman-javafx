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
package de.amr.games.pacman.ui.fx._3d.scene;

import static de.amr.games.pacman.lib.Globals.HTS;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.lib.U;
import de.amr.games.pacman.model.common.GameLevel;
import de.amr.games.pacman.model.common.IllegalGameVariantException;
import de.amr.games.pacman.model.common.Validator;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.model.mspacman.MsPacManDemoLevel;
import de.amr.games.pacman.model.pacman.PacManDemoLevel;
import de.amr.games.pacman.ui.fx._2d.rendering.SpritesheetRenderer;
import de.amr.games.pacman.ui.fx._3d.animation.SwingingWallsAnimation;
import de.amr.games.pacman.ui.fx._3d.entity.Eatable3D;
import de.amr.games.pacman.ui.fx._3d.entity.Energizer3D;
import de.amr.games.pacman.ui.fx._3d.entity.GameLevel3D;
import de.amr.games.pacman.ui.fx._3d.entity.Text3D;
import de.amr.games.pacman.ui.fx.app.Actions;
import de.amr.games.pacman.ui.fx.app.AppResources;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.app.Keys;
import de.amr.games.pacman.ui.fx.app.ResourceMgr;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameSceneContext;
import de.amr.games.pacman.ui.fx.sound.SoundClipID;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

/**
 * 3D play scene with sound and animations.
 * 
 * @author Armin Reichert
 */
public class PlayScene3D implements GameScene {

	private static final Logger LOG = LogManager.getFormatterLogger();

	private final ObjectProperty<Perspective> perspectivePy = new SimpleObjectProperty<>(this, "perspective",
			Perspective.TOTAL) {
		@Override
		protected void invalidated() {
			updateCamera();
		}
	};

	private final GameSceneContext context;
	private final SubScene fxSubScene;

	private final Group root = new Group();
	private final Map<Perspective, CameraController> camControllerMap = new EnumMap<>(Perspective.class);
	private final Text3D readyMessageText3D = new Text3D();
	private GameLevel3D level3D;
	private CameraController camController;

	public PlayScene3D(GameController gameController) {
		Validator.checkNotNull(gameController);

		context = new GameSceneContext(gameController);

		camControllerMap.put(Perspective.DRONE, new CamDrone());
		camControllerMap.put(Perspective.FOLLOWING_PLAYER, new CamFollowingPlayer());
		camControllerMap.put(Perspective.NEAR_PLAYER, new CamNearPlayer());
		camControllerMap.put(Perspective.TOTAL, new CamTotal());

		var coordSystem = new CoordSystem();
		coordSystem.visibleProperty().bind(Env.d3_axesVisiblePy);

		var ambientLight = new AmbientLight();
		ambientLight.colorProperty().bind(Env.d3_lightColorPy);

		root.getChildren().addAll(new Group() /* placeholder for 3D level */, coordSystem, ambientLight,
				readyMessageText3D.getRoot());

		// initial scene size is irrelevant
		fxSubScene = new SubScene(root, 42, 42, true, SceneAntialiasing.BALANCED);
		fxSubScene.setCamera(new PerspectiveCamera(true));
	}

	@Override
	public GameSceneContext context() {
		return context;
	}

	@Override
	public SubScene fxSubScene() {
		return fxSubScene;
	}

	@Override
	public void init() {
		context.level().ifPresent(level -> {
			resetReadyMessageText3D();
			replaceGameLevel3D(level);
			perspectivePy.bind(Env.d3_perspectivePy);
		});
	}

	@Override
	public void update() {
		context.level().ifPresent(level -> {
			level3D.update();
			camController.update(fxSubScene.getCamera(), level3D.pac3D());
			updateSound(level);
		});
	}

	@Override
	public void end() {
		context.level().ifPresent(level -> {
			context.sounds().stopAll();
			perspectivePy.unbind();
		});
	}

	@Override
	public void onEmbedIntoParentScene(Scene parentScene) {
		fxSubScene.widthProperty().bind(parentScene.widthProperty());
		fxSubScene.heightProperty().bind(parentScene.heightProperty());
	}

	@Override
	public void onParentSceneResize(Scene parentScene) {
		// nothing to do
	}

	private void updateCamera() {
		var perspective = perspectivePy.get();
		if (perspective == null) {
			LOG.error("No camera perspective specified");
			return;
		}
		var perspectiveController = camControllerMap.get(perspective);
		if (perspectiveController == null) {
			LOG.error("No camera found for perspective %s", perspective);
			return;
		}
		if (perspectiveController != camController) {
			camController = perspectiveController;
			fxSubScene.requestFocus();
			perspectiveController.reset(fxSubScene.getCamera());
			LOG.info("Perspective changed to %s (%s)", perspective, this);
		}
	}

	private void replaceGameLevel3D(GameLevel level) {
		var centerX = level.world().numCols() * HTS;
		var centerY = level.world().numRows() * HTS;
		level3D = new GameLevel3D(level, context.rendering2D(), context.rendering2D().pacManColors(),
				context.rendering2D().msPacManColors(), context.rendering2D().ghostColors());
		level3D.getRoot().getTransforms().setAll(new Translate(-centerX, -centerY));
		if (Env.d3_floorTextureRandomPy.get()) {
			Env.d3_floorTexturePy.set(AppResources.randomTextureKey());
		}
		// keep the scores rotated such that the viewer always sees them frontally
		level3D.scores3D().getRoot().rotationAxisProperty().bind(fxSubScene.getCamera().rotationAxisProperty());
		level3D.scores3D().getRoot().rotateProperty().bind(fxSubScene.getCamera().rotateProperty());
		root.getChildren().set(0, level3D.getRoot());
		LOG.info("3D game level created.");
	}

	private void resetReadyMessageText3D() {
		readyMessageText3D.beginBatch();
		readyMessageText3D.setBgColor(Color.CORNFLOWERBLUE);
		readyMessageText3D.setTextColor(Color.YELLOW);
		readyMessageText3D.setFont(context.rendering2D().screenFont(6));
		readyMessageText3D.setText("");
		readyMessageText3D.endBatch();
		readyMessageText3D.translate(0, 16, -4.5);
		readyMessageText3D.rotate(Rotate.X_AXIS, 90);
	}

	@Override
	public void handleKeyboardInput() {
		if (Keyboard.pressed(Keys.ADD_CREDIT) && !context.hasCredit()) {
			Actions.addCredit(); // in demo mode, allow adding credit
		} else if (Keyboard.pressed(Keys.PREV_CAMERA)) {
			Actions.selectPrevPerspective();
		} else if (Keyboard.pressed(Keys.NEXT_CAMERA)) {
			Actions.selectNextPerspective();
		} else if (Keyboard.pressed(Keys.CHEAT_EAT_ALL)) {
			Actions.cheatEatAllPellets();
		} else if (Keyboard.pressed(Keys.CHEAT_ADD_LIVES)) {
			Actions.cheatAddLives(3);
		} else if (Keyboard.pressed(Keys.CHEAT_NEXT_LEVEL)) {
			Actions.cheatEnterNextLevel();
		} else if (Keyboard.pressed(Keys.CHEAT_KILL_GHOSTS)) {
			Actions.cheatKillAllEatableGhosts();
		}
	}

	@Override
	public boolean is3D() {
		return true;
	}

	public CameraController currentCamController() {
		return camController;
	}

	public String camInfo() {
		var cam = fxSubScene.getCamera();
		return "x=%.0f y=%.0f z=%.0f rot=%.0f".formatted(cam.getTranslateX(), cam.getTranslateY(), cam.getTranslateZ(),
				cam.getRotate());
	}

	@Override
	public void onSceneVariantSwitch() {
		context.level().ifPresent(level -> {
			level3D.world3D().eatables3D()
					.forEach(eatable3D -> eatable3D.getRoot().setVisible(!level.world().containsEatenFood(eatable3D.tile())));
			if (U.oneOf(context.state(), GameState.HUNTING, GameState.GHOST_DYING)) {
				level3D.world3D().energizers3D().forEach(Energizer3D::startPumping);
			}
			context.sounds().ensureSirenStarted(level.huntingPhase() / 2);
		});
	}

	@Override
	public void onPlayerFindsFood(GameEvent e) {
		if (e.tile.isEmpty()) {
			// when cheat "eat all pellets" is used, no tile is present in the event.
			// In that case, bring 3D pellets to be in synch with model:
			context.world().ifPresent(world -> {
				world.tiles() //
						.filter(world::containsEatenFood) //
						.map(level3D.world3D()::eatableAt) //
						.flatMap(Optional::stream) //
						.forEach(Eatable3D::eat);
			});
		} else {
			var eatable = level3D.world3D().eatableAt(e.tile.get());
			eatable.ifPresent(level3D::eat);
		}
	}

	@Override
	public void onBonusGetsActive(GameEvent e) {
		level3D.bonus3D().showEdible();
	}

	@Override
	public void onBonusGetsEaten(GameEvent e) {
		level3D.bonus3D().showEaten();
	}

	@Override
	public void onBonusExpires(GameEvent e) {
		level3D.bonus3D().hide();
	}

	@Override
	public void onPlayerGetsPower(GameEvent e) {
		level3D.pac3D().onGetsPower();
	}

	@Override
	public void onPlayerLosesPower(GameEvent e) {
		level3D.pac3D().onLosesPower();
	}

	@Override
	public void onGameStateChange(GameStateChangeEvent e) {
		switch (e.newGameState) {

		case READY -> {
			context.level().ifPresent(level -> {
				level3D.pac3D().init(level);
				Stream.of(level3D.ghosts3D()).forEach(ghost3D -> ghost3D.init(level));
				if (Env.d3_foodOscillationEnabledPy.get()) {
					level3D.world3D().foodOscillation().play();
				}
				readyMessageText3D.setVisible(true);
				var readyMessage = U.inPercentOfCases(40) ? AppResources.randomReadyText(context.gameVariant()) : "READY!";
				readyMessageText3D.setText(readyMessage);
			});
		}

		case HUNTING -> {
			level3D.getLivesCounter3D().startAnimation();
			level3D.world3D().energizers3D().forEach(Energizer3D::startPumping);
		}

		case PACMAN_DYING -> {
			context.level().ifPresent(level -> {
				level3D.world3D().foodOscillation().stop();
				switch (level.game().variant()) {
				case MS_PACMAN -> level3D.pac3D().createMsPacManDyingAnimation();
				case PACMAN -> level3D.pac3D().createPacManDyingAnimation();
				default -> throw new IllegalGameVariantException(level.game().variant());
				}
				lockStateAndPlayAfterSeconds(1.0, level3D.pac3D().dyingAnimation());
			});
		}

		case GHOST_DYING -> {
			context.level().ifPresent(level -> {
				level.memo().killedGhosts.forEach(killedGhost -> {
					var ghost3D = level3D.ghosts3D()[killedGhost.id()];
					int index = killedGhost.killedIndex();
					// TODO make this work for all renderers
					if (context.rendering2D() instanceof SpritesheetRenderer sgr) {
						ghost3D.setNumberImage(sgr.image(sgr.ghostValueRegion(index)));
					}
				});
			});
		}

		case CHANGING_TO_NEXT_LEVEL -> {
			context.level().ifPresent(level -> {
//				Actions.showFlashMessage(AppResources.message("level_starting", level.number()));
				lockGameState();
				replaceGameLevel3D(level);
				updateCamera();
				waitSeconds(3.0);
			});
		}

		case LEVEL_COMPLETE -> {
			context.level().ifPresent(level -> {
				level3D.getLivesCounter3D().stopAnimation();
				level3D.world3D().foodOscillation().stop();
				// if cheat has been used to complete level, 3D food might still exist
				level3D.world3D().eatables3D().forEach(level3D::eat);
				var message = AppResources.pickLevelCompleteMessage(level.number());
				lockStateAndPlayAfterSeconds(1.0 //
						, createLevelCompleteAnimation(level) //
						, Ufx.afterSeconds(0.5, level.pac()::hide) //
						, Ufx.afterSeconds(0.5, () -> Actions.showFlashMessageSeconds(2, message)) //
						, createLevelChangeAnimation(level));
			});
		}

		case GAME_OVER -> {
			level3D.world3D().foodOscillation().stop();
			level3D.getLivesCounter3D().stopAnimation();
			Actions.showFlashMessageSeconds(3, AppResources.pickGameOverMessage());
		}

		default -> {
			// ignore
		}

		}

		// on state exit
		switch (e.oldGameState) {
		case READY -> {
			readyMessageText3D.setVisible(false);
		}
		case HUNTING -> {
			if (e.newGameState != GameState.GHOST_DYING) {
				level3D.world3D().energizers3D().forEach(Energizer3D::stopPumping);
				level3D.bonus3D().hide();
			}
		}
		default -> {
			// ignore
		}
		}
	}

	private Animation createLevelChangeAnimation(GameLevel level) {
		if (level.intermissionNumber != 0) {
			return Ufx.pause(0); // no level change animation if intermission scene follows
		}
		var perspectiveToRestore = Env.d3_perspectivePy.get();
		return new SequentialTransition(//
				Ufx.afterSeconds(1.0, () -> Env.d3_perspectivePy.set(Perspective.TOTAL)), //
				Ufx.afterSeconds(0.1, () -> ResourceMgr.audioClip("sound/common/sweep.wav").play()), //
				createLevelRotateAnimation(), //
				Ufx.afterSeconds(0.5, () -> Env.d3_perspectivePy.set(perspectiveToRestore)) //
		);
	}

	private Animation createLevelRotateAnimation() {
		var rotateAnimation = new RotateTransition();
		rotateAnimation.setNode(level3D.getRoot());
		rotateAnimation.setDuration(Duration.seconds(1.5));
		// TODO rotation does not work as expected
		rotateAnimation.setAxis(U.RND.nextBoolean() ? Rotate.X_AXIS : Rotate.Z_AXIS);
		rotateAnimation.setFromAngle(0);
		rotateAnimation.setToAngle(360);
		return rotateAnimation;
	}

	private Animation createLevelCompleteAnimation(GameLevel level) {
		if (level.numFlashes == 0) {
			return Ufx.pause(1.0);
		}
		double wallHeight = Env.d3_mazeWallHeightPy.get();
		var animation = new SwingingWallsAnimation(level.numFlashes);
		animation.setOnFinished(e -> Env.d3_mazeWallHeightPy.set(wallHeight));
		return animation;
	}

	private void lockStateAndPlayAfterSeconds(double afterSeconds, Animation... animations) {
		lockGameState();
		var animation = new SequentialTransition();
		if (afterSeconds > 0) {
			animation.getChildren().add(Ufx.pause(afterSeconds));
		}
		animation.getChildren().addAll(animations);
		animation.setOnFinished(e -> unlockGameState());
		animation.play();
	}

	private void waitSeconds(double seconds) {
		lockGameState();
		var pause = Ufx.pause(seconds);
		pause.setOnFinished(e -> unlockGameState());
		pause.play();
	}

	// TODO this is copy-pasta from 2D play scene
	private void updateSound(GameLevel level) {
		if (level instanceof PacManDemoLevel || level instanceof MsPacManDemoLevel) {
			return; // TODO maybe mark level as silent?
		}
		if (level.pac().starvingTicks() > 10) {
			context.sounds().stop(SoundClipID.PACMAN_MUNCH);
		}
		if (!level.pacKilled() && level.ghosts(GhostState.RETURNING_TO_HOUSE, GhostState.ENTERING_HOUSE)
				.filter(Ghost::isVisible).count() > 0) {
			context.sounds().ensureLoop(SoundClipID.GHOST_RETURNING, AudioClip.INDEFINITE);
		} else {
			context.sounds().stop(SoundClipID.GHOST_RETURNING);
		}
	}
}