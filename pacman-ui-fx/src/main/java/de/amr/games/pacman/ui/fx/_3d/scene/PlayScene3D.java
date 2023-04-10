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

import static de.amr.games.pacman.ui.fx.util.Ufx.afterSeconds;

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
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.model.mspacman.MsPacManGameDemoLevel;
import de.amr.games.pacman.model.pacman.PacManGameDemoLevel;
import de.amr.games.pacman.ui.fx._2d.rendering.common.SpritesheetRenderer;
import de.amr.games.pacman.ui.fx._3d.animation.SwingingWallsAnimation;
import de.amr.games.pacman.ui.fx._3d.entity.Eatable3D;
import de.amr.games.pacman.ui.fx._3d.entity.Energizer3D;
import de.amr.games.pacman.ui.fx._3d.entity.GameLevel3D;
import de.amr.games.pacman.ui.fx._3d.entity.Ghost3D;
import de.amr.games.pacman.ui.fx._3d.entity.Text3D;
import de.amr.games.pacman.ui.fx.app.Actions;
import de.amr.games.pacman.ui.fx.app.AppResources;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.app.Keys;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.sound.SoundClipID;
import de.amr.games.pacman.ui.fx.sound.SoundHandler;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.Animation;
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

/**
 * 3D play scene with sound and animations.
 * 
 * @author Armin Reichert
 */
public class PlayScene3D extends GameScene {

	private static final Logger LOG = LogManager.getFormatterLogger();

	private final ObjectProperty<Perspective> perspectivePy = new SimpleObjectProperty<>(this, "perspective",
			Perspective.TOTAL) {
		@Override
		protected void invalidated() {
			updateCameraToPerspective(get());
		}
	};

	private final Group root = new Group();
	private final Map<Perspective, CameraController> cameraControllerMap = new EnumMap<>(Perspective.class);
	private final Text3D infoText3D = new Text3D();
	private GameLevel3D level3D;
	private CameraController currentCamController;

	public PlayScene3D(GameController gameController) {
		super(gameController);

		cameraControllerMap.put(Perspective.DRONE, new CamDrone());
		cameraControllerMap.put(Perspective.FOLLOWING_PLAYER, new CamFollowingPlayer());
		cameraControllerMap.put(Perspective.NEAR_PLAYER, new CamNearPlayer());
		cameraControllerMap.put(Perspective.TOTAL, new CamTotal());
		currentCamController = cameraControllerMap.get(Env.d3_perspectivePy.get());

		var coordSystem = new CoordSystem();
		coordSystem.visibleProperty().bind(Env.d3_axesVisiblePy);

		var ambientLight = new AmbientLight();
		ambientLight.colorProperty().bind(Env.d3_lightColorPy);

		root.getChildren().addAll(new Group() /* placeholder for 3D level */, coordSystem, ambientLight,
				infoText3D.getRoot());

		// initial scene size is irrelevant
		fxSubScene = new SubScene(root, 42, 42, true, SceneAntialiasing.BALANCED);
		fxSubScene.setCamera(new PerspectiveCamera(true));
	}

	@Override
	public void init() {
		context.level().ifPresent(level -> {
			replaceGameLevel3D(level);
			resetInfoText();
			updateCameraToPerspective(perspectivePy.get());
		});
		perspectivePy.bind(Env.d3_perspectivePy);
	}

	@Override
	public void update() {
		context.level().ifPresent(level -> {
			level3D.update();
			currentCamController.update(fxSubScene.getCamera(), level3D.pac3D().getRoot());
			updateSound(level);
		});
	}

	@Override
	public void end() {
		context.sounds().stopAll();
		perspectivePy.unbind();
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

	private void replaceGameLevel3D(GameLevel level) {
		var centerX = level.world().numCols() * World.HTS;
		var centerY = level.world().numRows() * World.HTS;
		level3D = new GameLevel3D(level, context.rendering2D());
		level3D.getRoot().getTransforms().setAll(new Translate(-centerX, -centerY));
		root.getChildren().set(0, level3D.getRoot());
		LOG.info("3D game level created.");
	}

	private void resetInfoText() {
		infoText3D.beginBatch();
		infoText3D.setBgColor(Color.CORNFLOWERBLUE);
		infoText3D.setTextColor(Color.YELLOW);
		infoText3D.setFont(context.rendering2D().screenFont(6));
		infoText3D.setText("");
		infoText3D.endBatch();
		infoText3D.translate(0, 16, -4.5);
		infoText3D.rotate(Rotate.X_AXIS, 90);
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
		return currentCamController;
	}

	public String camInfo() {
		var cam = fxSubScene.getCamera();
		return "x=%.0f y=%.0f z=%.0f rot=%.0f".formatted(cam.getTranslateX(), cam.getTranslateY(), cam.getTranslateZ(),
				cam.getRotate());
	}

	private void updateCameraToPerspective(Perspective perspective) {
		if (perspective == null) {
			LOG.error("No camera perspective specified");
			return;
		}
		var camController = cameraControllerMap.get(perspective);
		if (camController == null) {
			LOG.error("No camera found for perspective %s", perspective);
			return;
		}
		if (camController != currentCamController) {
			currentCamController = camController;
			fxSubScene.requestFocus();
			camController.reset(fxSubScene.getCamera());
			LOG.info("Perspective changed to %s (%s)", perspective, this);
		}
		// rotate the scores such that the viewer sees them frontally
		if (level3D != null && level3D.scores3D() != null) {
			level3D.scores3D().getRoot().rotationAxisProperty().bind(fxSubScene.getCamera().rotationAxisProperty());
			level3D.scores3D().getRoot().rotateProperty().bind(fxSubScene.getCamera().rotateProperty());
		}
	}

	@Override
	public void onSceneVariantSwitch() {
		context.level().ifPresent(level -> {
			level3D.world3D().eatables3D()
					.forEach(eatable3D -> eatable3D.getRoot().setVisible(!level.world().containsEatenFood(eatable3D.tile())));
			if (U.oneOf(context.state(), GameState.HUNTING, GameState.GHOST_DYING)) {
				level3D.world3D().energizers3D().forEach(Energizer3D::startPumping);
			}
			var sounds = SoundHandler.sounds(context.game());
			sounds.ensureSirenStarted(level.huntingPhase() / 2);
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
		level3D.bonus3D().showSymbol();
	}

	@Override
	public void onBonusGetsEaten(GameEvent e) {
		level3D.bonus3D().showPoints();
	}

	@Override
	public void onBonusExpires(GameEvent e) {
		level3D.bonus3D().hide();
	}

	@Override
	public void onGameStateChange(GameStateChangeEvent e) {
		switch (e.newGameState) {

		case READY -> {
			level3D.pac3D().init();
			Stream.of(level3D.ghosts3D()).forEach(Ghost3D::init);
			if (Env.d3_foodOscillationEnabledPy.get()) {
				level3D.foodOscillation().play();
			}
			infoText3D.setVisible(true);
			var readyText = U.RND.nextInt(100) % 5 == 0 ? AppResources.randomReadyText() : "READY!";
			infoText3D.setText(readyText);
		}

		case HUNTING -> {
			level3D.world3D().energizers3D().forEach(Energizer3D::startPumping);
		}

		case PACMAN_DYING -> {
			level3D.foodOscillation().stop();
			lockAndPlay(1.0, level3D.pac3D().createDyingAnimation());
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
				Actions.showFlashMessage(AppResources.message("level_starting", level.number()));
				lockGameState();
				replaceGameLevel3D(level);
				updateCameraToPerspective(perspectivePy.get());
				afterSeconds(3, this::unlockGameState).play();
			});
		}

		case LEVEL_COMPLETE -> {
			context.level().ifPresent(level -> {
				level3D.foodOscillation().stop();
				// if cheat has been used to complete level, 3D food might still exist
				level3D.world3D().eatables3D().forEach(level3D::eat);
				var message = AppResources.pickLevelCompleteMessage(level.number());
				lockAndPlay(2.0, //
						level.numFlashes > 0 ? new SwingingWallsAnimation(level.numFlashes) : Ufx.pause(1.0), //
						afterSeconds(1.0, level.pac()::hide), //
						afterSeconds(0.5, () -> Actions.showFlashMessageSeconds(2, message)), //
						Ufx.pause(2.0) //
				);
			});
		}

		case GAME_OVER -> {
			level3D.foodOscillation().stop();
			Actions.showFlashMessageSeconds(3, AppResources.pickGameOverMessage());
		}

		default -> {
			// ignore
		}

		}

		// on state exit
		switch (e.oldGameState) {
		case READY -> {
			infoText3D.setVisible(false);
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

	private void lockAndPlay(double afterSeconds, Animation... animations) {
		lockGameState();
		var animation = new SequentialTransition();
		if (afterSeconds > 0) {
			animation.getChildren().add(Ufx.pause(afterSeconds));
		}
		animation.getChildren().addAll(animations);
		animation.setOnFinished(e -> unlockGameState());
		animation.play();
	}

	// TODO this is copy-pasta from 2D play scene
	private void updateSound(GameLevel level) {
		if (level instanceof PacManGameDemoLevel || level instanceof MsPacManGameDemoLevel) {
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