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
package de.amr.games.pacman.ui.fx.v3d.scene;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.RND;
import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.lib.Globals.inPercentOfCases;
import static de.amr.games.pacman.lib.Globals.oneOf;
import static de.amr.games.pacman.lib.Globals.randomInt;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import javafx.scene.layout.BorderPane;
import org.tinylog.Logger;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.ui.fx.app.PacManGames2d;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameSceneContext;
import de.amr.games.pacman.ui.fx.util.Ufx;
import de.amr.games.pacman.ui.fx.v3d.animation.SinusCurveAnimation;
import de.amr.games.pacman.ui.fx.v3d.app.PacManGames3d;
import de.amr.games.pacman.ui.fx.v3d.entity.Eatable3D;
import de.amr.games.pacman.ui.fx.v3d.entity.Energizer3D;
import de.amr.games.pacman.ui.fx.v3d.entity.GameLevel3D;
import de.amr.games.pacman.ui.fx.v3d.entity.Text3D;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
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
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * 3D play scene with sound and animations.
 * 
 * @author Armin Reichert
 */
public class PlayScene3D implements GameScene {

	private final ObjectProperty<Perspective> perspectivePy = new SimpleObjectProperty<>(this, "perspective",
			Perspective.TOTAL) {
		@Override
		protected void invalidated() {
			updateCamera();
		}
	};

	private GameSceneContext context;
	private final BorderPane subSceneContainer;
	private final SubScene fxSubScene;
	private final Group root;
	private final Text3D readyMessageText3D = new Text3D();
	private GameLevel3D level3D;

	private final Map<Perspective, CameraController> camControllerMap = new EnumMap<>(Perspective.class);
	private CameraController camController;

	public PlayScene3D() {
		camControllerMap.put(Perspective.DRONE, new CamDrone());
		camControllerMap.put(Perspective.FOLLOWING_PLAYER, new CamFollowingPlayer());
		camControllerMap.put(Perspective.NEAR_PLAYER, new CamNearPlayer());
		camControllerMap.put(Perspective.TOTAL, new CamTotal());

		var coordSystem = new CoordSystem();
		coordSystem.visibleProperty().bind(PacManGames3d.PY_3D_AXES_VISIBLE);

		var ambientLight = new AmbientLight();
		ambientLight.colorProperty().bind(PacManGames3d.PY_3D_LIGHT_COLOR);

		root = new Group(new Text("<3D game level>"), coordSystem, ambientLight, readyMessageText3D.getRoot());

		// initial scene size is irrelevant, will be bound to main scene size
		fxSubScene = new SubScene(root, 42, 42, true, SceneAntialiasing.BALANCED);
		fxSubScene.setCamera(new PerspectiveCamera(true));

		subSceneContainer = new BorderPane(fxSubScene);
	}

	@Override
	public void setContext(GameSceneContext context) {
		checkNotNull(context);
		this.context = context;
	}

	@Override
	public GameSceneContext context() {
		return context;
	}

	@Override
	public BorderPane sceneContainer() {
		return subSceneContainer;
	}

	@Override
	public void init() {
		context.setCreditVisible(false);
		context.setScoreVisible(true);
		resetReadyMessageText3D();
		perspectivePy.bind(PacManGames3d.PY_3D_PERSPECTIVE);
		context.level().ifPresent(this::replaceGameLevel3D);
		Logger.info("Initialized 3D play scene");
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
		context.ui().stopAllSounds();
		perspectivePy.unbind();
	}

	@Override
	public void setParentScene(Scene parentScene) {
		fxSubScene.widthProperty().bind(parentScene.widthProperty());
		fxSubScene.heightProperty().bind(parentScene.heightProperty());
	}

	private void updateCamera() {
		var perspective = perspectivePy.get();
		if (perspective == null) {
			Logger.error("No camera perspective specified");
			return;
		}
		var perspectiveController = camControllerMap.get(perspective);
		if (perspectiveController == null) {
			Logger.error("No camera found for perspective {}", perspective);
			return;
		}
		if (perspectiveController != camController) {
			camController = perspectiveController;
			fxSubScene.requestFocus();
			perspectiveController.reset(fxSubScene.getCamera());
			Logger.info("Perspective changed to {} ({})", perspective, this);
		}
	}

	private void replaceGameLevel3D(GameLevel level) {
		if (level.number() > 1 && level3D != null && level3D.level().number() == level.number()) {
			Logger.info("3D game level up-to-date");
			return;
		}

		level3D = new GameLevel3D(level, context.ui().theme(), context.renderer());

		// center over origin
		var centerX = level.world().numCols() * HTS;
		var centerY = level.world().numRows() * HTS;
		level3D.getRoot().setTranslateX(-centerX);
		level3D.getRoot().setTranslateY(-centerY);

		// keep the scores rotated such that the viewer always sees them frontally
		level3D.scores3D().getRoot().rotationAxisProperty().bind(fxSubScene.getCamera().rotationAxisProperty());
		level3D.scores3D().getRoot().rotateProperty().bind(fxSubScene.getCamera().rotateProperty());

		// replace initial placeholder or previous 3D level
		root.getChildren().set(0, level3D.getRoot());

		if (context.state() == GameState.LEVEL_TEST) {
			readyMessageText3D.setText("LEVEL %s TEST".formatted(level.number()));
		}

		if (PacManGames3d.PY_3D_FLOOR_TEXTURE_RND.get()) {
			var names = new String[] { "hexagon", "knobs", "plastic", "wood" };
			PacManGames3d.PY_3D_FLOOR_TEXTURE.set(names[randomInt(0, names.length)]);
		}
		Logger.info("3D game level {} created.", level.number());
	}

	private void resetReadyMessageText3D() {
		readyMessageText3D.beginBatch();
		readyMessageText3D.setBgColor(Color.CORNFLOWERBLUE);
		readyMessageText3D.setTextColor(Color.YELLOW);
		readyMessageText3D.setFont(context.ui().theme().font("font.arcade", 6));
		readyMessageText3D.setText("");
		readyMessageText3D.endBatch();
		readyMessageText3D.translate(0, 16, -4.5);
		readyMessageText3D.rotate(Rotate.X_AXIS, 90);
	}

	@Override
	public void handleKeyboardInput() {
		if (Keyboard.pressed(PacManGames2d.KEY_ADD_CREDIT) && !context.hasCredit()) {
			PacManGames2d.ui.addCredit(); // in demo mode, allow adding credit
		} else if (Keyboard.pressed(PacManGames3d.KEY_PREV_PERSPECTIVE)) {
			PacManGames3d.ui.selectPrevPerspective();
		} else if (Keyboard.pressed(PacManGames3d.KEY_NEXT_PERSPECTIVE)) {
			PacManGames3d.ui.selectNextPerspective();
		} else if (Keyboard.pressed(PacManGames2d.KEY_CHEAT_EAT_ALL)) {
			PacManGames2d.ui.cheatEatAllPellets();
		} else if (Keyboard.pressed(PacManGames2d.KEY_CHEAT_ADD_LIVES)) {
			PacManGames2d.ui.cheatAddLives();
		} else if (Keyboard.pressed(PacManGames2d.KEY_CHEAT_NEXT_LEVEL)) {
			PacManGames2d.ui.cheatEnterNextLevel();
		} else if (Keyboard.pressed(PacManGames2d.KEY_CHEAT_KILL_GHOSTS)) {
			PacManGames2d.ui.cheatKillAllEatableGhosts();
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
			if (oneOf(context.state(), GameState.HUNTING, GameState.GHOST_DYING)) {
				level3D.world3D().energizers3D().forEach(Energizer3D::startPumping);
			}
			if (!level.isDemoLevel()) {
				context.ui().ensureSirenStarted(level.huntingPhase() / 2);
			}
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
		context.level().ifPresent(level -> {
			boolean moving = context.gameVariant() == GameVariant.MS_PACMAN;
			level.bonusManagement().getBonus().ifPresent(bonus -> {
				level3D.replaceBonus3D(bonus, context.renderer(), moving);
			});
			level3D.bonus3D().showEdible();
		});
	}

	@Override
	public void onBonusGetsEaten(GameEvent e) {
		if (level3D.bonus3D() != null) {
			level3D.bonus3D().showEaten();
		}
	}

	@Override
	public void onBonusExpires(GameEvent e) {
		if (level3D.bonus3D() != null) {
			level3D.bonus3D().hide();
		}
	}

	@Override
	public void onPlayerGetsPower(GameEvent e) {
		level3D.pac3D().walkingAnimation().setPowerWalking(true);
	}

	@Override
	public void onPlayerLosesPower(GameEvent e) {
		level3D.pac3D().walkingAnimation().setPowerWalking(false);
	}

	@Override
	public void onGameStateChange(GameStateChangeEvent e) {
		switch (e.newGameState) {

		case READY -> {
			context.level().ifPresent(level -> {
				level3D.pac3D().init(level);
				Stream.of(level3D.ghosts3D()).forEach(ghost3D -> ghost3D.init(level));
//				if (Game3d.d3_foodOscillationPy.get()) {
//					level3D.world3D().foodOscillation().play();
//				}
				readyMessageText3D.setVisible(true);
				var readyMessage = inPercentOfCases(40) ? PacManGames3d.pickFunnyReadyMessage(context.gameVariant()) : "READY!";
				readyMessageText3D.setText(readyMessage);
			});
		}

		case HUNTING -> {
			level3D.livesCounter3D().startAnimation();
			level3D.world3D().energizers3D().forEach(Energizer3D::startPumping);
		}

		case PACMAN_DYING -> {
			context.level().ifPresent(level -> {
				level3D.world3D().foodOscillation().stop();
				Logger.info("Play dying animation for {}", level3D.pac3D());
				lockStateAndPlayAfterSeconds(1.0, level3D.pac3D().dyingAnimation().animation());
			});
		}

		case GHOST_DYING -> {
			context.level().ifPresent(level -> {
				level.memo().killedGhosts.forEach(killedGhost -> {
					var ghost3D = level3D.ghosts3D()[killedGhost.id()];
					int index = killedGhost.killedIndex();
					var r = context.renderer();
					ghost3D.setNumberImage(r.spritesheet().subImage(r.ghostValueSprite(index)));
				});
			});
		}

		case CHANGING_TO_NEXT_LEVEL -> {
			context.level().ifPresent(level -> {
				lockGameState();
				replaceGameLevel3D(level);
				updateCamera();
				waitSeconds(3.0);
			});
		}

		case LEVEL_COMPLETE -> {
			context.level().ifPresent(level -> {
				level3D.livesCounter3D().stopAnimation();
				level3D.world3D().foodOscillation().stop();
				// if cheat has been used to complete level, 3D food might still exist
				level3D.world3D().eatables3D().forEach(level3D::eat);
				// level complete animation is always played
				var levelCompleteAnimation = createLevelCompleteAnimation(level);
				// level change animation is played only if no intermission scene follows
				var levelChangeAnimation = level.intermissionNumber == 0 ? createLevelChangeAnimation() : Ufx.pauseSeconds(0);
				//@formatter:off
				lockStateAndPlayAfterSeconds(1.0, 
					levelCompleteAnimation, 
					Ufx.actionAfterSeconds(1.0, () -> {
						level.pac().hide();
						level3D.livesCounter3D().lightOnPy.set(false);
						// play sound / flash msg only if no intermission scene follows
						if (level.intermissionNumber == 0) {
							context.ui().playLevelCompleteSound();
							PacManGames3d.ui.showFlashMessageSeconds(2, PacManGames3d.pickLevelCompleteMessage(level.number()));
						}
					}),
					levelChangeAnimation,
					Ufx.actionAfterSeconds(0, () -> level3D.livesCounter3D().lightOnPy.set(true))
				);
				//@formatter:on
			});
		}

		case GAME_OVER -> {
			level3D.world3D().foodOscillation().stop();
			level3D.livesCounter3D().stopAnimation();
			PacManGames3d.ui.showFlashMessageSeconds(3, PacManGames3d.pickGameOverMessage());
			context.ui().playGameOverSound();
			waitSeconds(3);
		}

		default -> {
			// ignore
		}

		}

		// on state exit
		if (e.oldGameState == null) {
			return;
		}
		switch (e.oldGameState) {
		case READY -> {
			readyMessageText3D.setVisible(false);
		}
		case HUNTING -> {
			if (e.newGameState != GameState.GHOST_DYING) {
				level3D.world3D().energizers3D().forEach(Energizer3D::stopPumping);
				if (level3D.bonus3D() != null) {
					level3D.bonus3D().hide();
				}
			}
		}
		default -> {
			// ignore
		}
		}
	}

	private Animation createLevelChangeAnimation() {
		var rotation = new RotateTransition(Duration.seconds(1.5), level3D.getRoot());
		rotation.setAxis(RND.nextBoolean() ? Rotate.X_AXIS : Rotate.Z_AXIS);
		rotation.setFromAngle(0);
		rotation.setToAngle(360);
		rotation.setInterpolator(Interpolator.LINEAR);
		//@formatter:off
		return new SequentialTransition(
			Ufx.actionAfterSeconds(1.0, () -> {
				perspectivePy.unbind();
				perspectivePy.set(Perspective.TOTAL);
			}),
			rotation,
			Ufx.actionAfterSeconds(0.5, () -> context.ui().theme().audioClip("audio.sweep").play()),
			Ufx.actionAfterSeconds(0.5, () -> perspectivePy.bind(PacManGames3d.PY_3D_PERSPECTIVE))
		);
		//@formatter:on
	}

	private Animation createLevelCompleteAnimation(GameLevel level) {
		if (level.numFlashes == 0) {
			return Ufx.pauseSeconds(1.0);
		}
		double wallHeight = PacManGames3d.PY_3D_WALL_HEIGHT.get();
		var animation = new SinusCurveAnimation(level.numFlashes);
		animation.setAmplitude(wallHeight);
		animation.elongationPy.set(level3D.world3D().wallHeightPy.get());
		level3D.world3D().wallHeightPy.bind(animation.elongationPy);
		animation.setOnFinished(e -> {
			level3D.world3D().wallHeightPy.bind(PacManGames3d.PY_3D_WALL_HEIGHT);
			PacManGames3d.PY_3D_WALL_HEIGHT.set(wallHeight);
		});
		return animation;
	}

	private void updateSound(GameLevel level) {
		if (level.isDemoLevel()) {
			return;
		}
		if (level.pac().starvingTicks() > 8) { // TODO not sure
			context.ui().stopMunchingSound();
		}
		if (!level.pacKilled() && level.ghosts(GhostState.RETURNING_TO_HOUSE, GhostState.ENTERING_HOUSE)
				.filter(Ghost::isVisible).count() > 0) {
			context.ui().loopGhostReturningSound();
		} else {
			context.ui().stopGhostReturningSound();
		}
	}
}