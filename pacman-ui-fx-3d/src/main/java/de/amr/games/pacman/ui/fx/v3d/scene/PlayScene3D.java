/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.scene;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.lib.Globals;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.ui.fx.app.PacManGames2dApp;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.rendering2d.mspacman.SpritesheetMsPacManGame;
import de.amr.games.pacman.ui.fx.rendering2d.pacman.SpritesheetPacManGame;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameSceneContext;
import de.amr.games.pacman.ui.fx.v3d.animation.SinusCurveAnimation;
import de.amr.games.pacman.ui.fx.v3d.app.ActionHandler3D;
import de.amr.games.pacman.ui.fx.v3d.app.PacManGames3dApp;
import de.amr.games.pacman.ui.fx.v3d.entity.*;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.SequentialTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui.fx.util.Ufx.actionAfterSeconds;
import static de.amr.games.pacman.ui.fx.util.Ufx.pauseSeconds;

/**
 * 3D play scene.
 *
 * <p>Provides different camera perspectives that can be selected sequentially using keys <code>Alt+LEFT</code>
 *  and <code>Alt+RIGHT</code>.</p>
 * 
 * @author Armin Reichert
 */
public class PlayScene3D implements GameScene {

	public final ObjectProperty<Perspective> perspectivePy = new SimpleObjectProperty<>(this, "perspective") {
		@Override
		protected void invalidated() {
			updateCamera(get());
		}
	};

	private final Map<Perspective, CameraController> camControllerMap = new EnumMap<>(Perspective.class);
	private GameSceneContext context;
	private boolean scoreVisible;
	private boolean creditVisible;
	private final BorderPane root;
	private final SubScene fxSubScene;
	private final Group subSceneRoot;
	private final Text3D readyMessageText3D = new Text3D();
	private GameLevel3D level3D;


	public PlayScene3D() {
		camControllerMap.put(Perspective.DRONE,            new CamDrone());
		camControllerMap.put(Perspective.FOLLOWING_PLAYER, new CamFollowingPlayer());
		camControllerMap.put(Perspective.NEAR_PLAYER,      new CamNearPlayer());
		camControllerMap.put(Perspective.TOTAL,            new CamTotal());

		var coordSystem = new CoordSystem();
		coordSystem.visibleProperty().bind(PacManGames3dApp.PY_3D_AXES_VISIBLE);

		var ambientLight = new AmbientLight();
		ambientLight.colorProperty().bind(PacManGames3dApp.PY_3D_LIGHT_COLOR);

		// initial sub-scene size is irrelevant, gets bound to main scene size in init method
		subSceneRoot = new Group(new Text("<3D game level>"), coordSystem, ambientLight, readyMessageText3D.getRoot());
		fxSubScene = new SubScene(subSceneRoot, 42, 42, true, SceneAntialiasing.BALANCED);
		fxSubScene.setCamera(new PerspectiveCamera(true));
		root = new BorderPane(fxSubScene);
	}

	@Override
	public void setContext(GameSceneContext context) {
		this.context = context;
	}

	@Override
	public GameSceneContext context() {
		return context;
	}

	@Override
	public void init() {
		setCreditVisible(false);
		setScoreVisible(true);
		resetReadyMessageText3D();
		perspectivePy.bind(PacManGames3dApp.PY_3D_PERSPECTIVE);
		game().level().ifPresent(this::replaceGameLevel3D);
		Logger.info("3D play scene initialized.");
	}

	@Override
	public void update() {
		if (level3D == null) {
			return;
		}
		level3D.update();
		currentCamController().update(fxSubScene.getCamera(), level3D.pac3D());
		updateSound();
	}

	@Override
	public boolean isCreditVisible() {
		return creditVisible;
	}

	@Override
	public void setCreditVisible(boolean creditVisible) {
		this.creditVisible = creditVisible;
	}

	@Override
	public boolean isScoreVisible() {
		return scoreVisible;
	}

	@Override
	public void setScoreVisible(boolean scoreVisible) {
		this.scoreVisible = scoreVisible;
	}

	@Override
	public BorderPane root() {
		return root;
	}

	public CameraController currentCamController() {
		return camControllerMap.getOrDefault(perspectivePy.get(), camControllerMap.get(Perspective.TOTAL));
	}

	@Override
	public void end() {
		perspectivePy.unbind();
	}

	public void bindSize(ReadOnlyDoubleProperty widthPy, ReadOnlyDoubleProperty heightPy) {
		fxSubScene.widthProperty().bind(widthPy);
		fxSubScene.heightProperty().bind(heightPy);
	}

	private void updateCamera(Perspective perspective) {
		currentCamController().reset(fxSubScene.getCamera());
		fxSubScene.requestFocus();
		Logger.info("Perspective is {} ({})", perspective, this);
	}

	private void replaceGameLevel3D(GameLevel level) {
		if (level.number() > 1 && level3D != null && level3D.level().number() == level.number()) {
			Logger.info("3D game level up-to-date");
			return;
		}

		level3D = new GameLevel3D(level, context.theme(), context.spritesheet());

		// center over origin
		var centerX = level.world().numCols() * HTS;
		var centerY = level.world().numRows() * HTS;
		level3D.root().setTranslateX(-centerX);
		level3D.root().setTranslateY(-centerY);

		// keep the scores rotated such that the viewer always sees them frontally
		level3D.scores3D().getRoot().rotationAxisProperty().bind(fxSubScene.getCamera().rotationAxisProperty());
		level3D.scores3D().getRoot().rotateProperty().bind(fxSubScene.getCamera().rotateProperty());

		// replace initial placeholder or previous 3D level
		subSceneRoot.getChildren().set(0, level3D.root());

		if (state() == GameState.LEVEL_TEST) {
			readyMessageText3D.setText("LEVEL %s TEST".formatted(level.number()));
		}

		if (PacManGames3dApp.PY_3D_FLOOR_TEXTURE_RND.get()) {
			var names = new String[] { "hexagon", "knobs", "plastic", "wood" };
			PacManGames3dApp.PY_3D_FLOOR_TEXTURE.set(names[randomInt(0, names.length)]);
		}
		Logger.info("3D game level {} created.", level.number());
	}

	private void resetReadyMessageText3D() {
		readyMessageText3D.beginBatch();
		readyMessageText3D.setBgColor(Color.CORNFLOWERBLUE);
		readyMessageText3D.setTextColor(Color.YELLOW);
		readyMessageText3D.setFont(context.theme().font("font.arcade", 6));
		readyMessageText3D.setText("");
		readyMessageText3D.endBatch();
		readyMessageText3D.translate(0, 16, -4.5);
		readyMessageText3D.rotate(Rotate.X_AXIS, 90);
	}

	@Override
	public void handleKeyboardInput() {
			if (Keyboard.pressed(PacManGames2dApp.KEY_ADD_CREDIT) && !GameController.it().hasCredit()) {
				context.actionHandler().addCredit();
			} else if (Keyboard.pressed(PacManGames3dApp.KEY_PREV_PERSPECTIVE)) {
				((ActionHandler3D) context.actionHandler()).selectPrevPerspective();
			} else if (Keyboard.pressed(PacManGames3dApp.KEY_NEXT_PERSPECTIVE)) {
				((ActionHandler3D) context.actionHandler()).selectNextPerspective();
			} else if (Keyboard.pressed(PacManGames2dApp.KEY_CHEAT_EAT_ALL)) {
				context.actionHandler().cheatEatAllPellets();
			} else if (Keyboard.pressed(PacManGames2dApp.KEY_CHEAT_ADD_LIVES)) {
				context.actionHandler().cheatAddLives();
			} else if (Keyboard.pressed(PacManGames2dApp.KEY_CHEAT_NEXT_LEVEL)) {
				context.actionHandler().cheatEnterNextLevel();
			} else if (Keyboard.pressed(PacManGames2dApp.KEY_CHEAT_KILL_GHOSTS)) {
				context.actionHandler().cheatKillAllEatableGhosts();
			}
	}

	@Override
	public boolean is3D() {
		return true;
	}

	public String camInfo() {
		var cam = fxSubScene.getCamera();
		return "x=%.0f y=%.0f z=%.0f rot=%.0f".formatted(cam.getTranslateX(), cam.getTranslateY(), cam.getTranslateZ(),
				cam.getRotate());
	}

	@Override
	public void onSceneVariantSwitch() {
		game().level().ifPresent(level -> {
			level3D.world3D().eatables3D().forEach(
					eatable3D -> eatable3D.getRoot().setVisible(!level.world().hasEatenFoodAt(eatable3D.tile())));
			if (Globals.oneOf(state(), GameState.HUNTING, GameState.GHOST_DYING)) {
				level3D.world3D().energizers3D().forEach(Energizer3D::startPumping);
			}
			if (!level.isDemoLevel()) {
				context.soundHandler().ensureSirenStarted(level.game().variant(), level.huntingPhase() / 2);
			}
		});
	}

	@Override
	public void onPacFoundFood(GameEvent e) {
		// When cheat "eat all pellets" has been used, no tile is present in the event.
		// In that case, ensure that the 3D pellets are in sync with the model.
		if (e.tile().isEmpty()) {
			world().ifPresent(world -> world.tiles()
					.filter(world::hasEatenFoodAt)
					.map(level3D.world3D()::eatableAt)
					.flatMap(Optional::stream)
					.forEach(Eatable3D::eaten));
		} else {
			var tile = e.tile().get();
			level3D.world3D().eatableAt(tile).ifPresent(level3D::eat);
		}
	}

	@Override
	public void onBonusActivated(GameEvent e) {
		game().level().ifPresent(level -> {
			level.bonus().ifPresent(bonus -> level3D.replaceBonus3D(bonus, context.spritesheet()));
			level3D.bonus3D().showEdible();
		});
	}

	@Override
	public void onBonusEaten(GameEvent e) {
		if (level3D.bonus3D() != null) {
			level3D.bonus3D().showEaten();
		}
	}

	@Override
	public void onBonusExpired(GameEvent e) {
		if (level3D.bonus3D() != null) {
			level3D.bonus3D().hide();
		}
	}

	@Override
	public void onPacGetsPower(GameEvent e) {
		level3D.pac3D().walkingAnimation().setPowerWalking(true);
	}

	@Override
	public void onPacLostPower(GameEvent e) {
		level3D.pac3D().walkingAnimation().setPowerWalking(false);
	}

	@Override
	public void onGameStateChange(GameStateChangeEvent e) {
		switch (e.newState) {

		case READY -> {
			level3D.pac3D().init();
			Stream.of(level3D.ghosts3D()).forEach(Ghost3D::init);
			var msg = "READY!";
			if (!PacManGames3dApp.PY_WOKE_PUSSY.get() && inPercentOfCases(5)) {
				msg = PacManGames3dApp.pickFunnyReadyMessage(game().variant());
			}
			readyMessageText3D.setText(msg);
			readyMessageText3D.setVisible(true);
		}

		case HUNTING -> {
			level3D.livesCounter3D().startAnimation();
			level3D.world3D().energizers3D().forEach(Energizer3D::startPumping);
		}

		case PACMAN_DYING -> {
			level3D.world3D().foodOscillation().stop();
			lockStateAndPlayAfterSeconds(1.0, level3D.pac3D().dyingAnimation().animation());
		}

		case GHOST_DYING -> {
			game().level().map(GameLevel::thisFrame).ifPresent(thisFrame -> {
				switch (game().variant()) {
				case MS_PACMAN -> {
					var ss = (SpritesheetMsPacManGame) context.spritesheet();
					thisFrame.killedGhosts.forEach(ghost -> {
						var numberImage = ss.subImage(ss.ghostNumberSprites()[ghost.killedIndex()]);
						level3D.ghost3D(ghost.id()).setNumberImage(numberImage);
					});
				}
				case PACMAN -> {
					var ss = (SpritesheetPacManGame) context.spritesheet();
					thisFrame.killedGhosts.forEach(ghost -> {
						var numberImage = ss.subImage(ss.ghostNumberSprites()[ghost.killedIndex()]);
						level3D.ghost3D(ghost.id()).setNumberImage(numberImage);
					});
				}
				default -> throw new IllegalGameVariantException(game().variant());
				}
			});
		}

		case CHANGING_TO_NEXT_LEVEL -> {
			game().level().ifPresent(level -> {
				state().timer().resetIndefinitely();
				replaceGameLevel3D(level);
				updateCamera(perspectivePy.get());
				keepGameStateForSeconds(3);
			});
		}

		case LEVEL_COMPLETE -> {
			game().level().ifPresent(level -> {
				level3D.livesCounter3D().stopAnimation();
				level3D.world3D().foodOscillation().stop();
				// if cheat has been used to complete level, 3D food might still exist
				level3D.world3D().eatables3D().forEach(level3D::eat);
				// level complete animation is always played
				var levelCompleteAnimation = createLevelCompleteAnimation(level);
				// level change animation is played only if no intermission scene follows
				var levelChangeAnimation = level.intermissionNumber == 0 ? createLevelChangeAnimation()	: pauseSeconds(0);
				//@formatter:off
				lockStateAndPlayAfterSeconds(1.0, 
					levelCompleteAnimation, 
					actionAfterSeconds(1.0, () -> {
						level.pac().hide();
						level3D.livesCounter3D().lightOnPy.set(false);
						// play sound / flash msg only if no intermission scene follows
						if (level.intermissionNumber == 0) {
							context.soundHandler().audioClip(level.game().variant(), "audio.level_complete").play();
							context.actionHandler().showFlashMessageSeconds(2,
									PacManGames3dApp.pickLevelCompleteMessage(level.number()));
						}
					}),
					levelChangeAnimation,
					actionAfterSeconds(0, () -> level3D.livesCounter3D().lightOnPy.set(true))
				);
				//@formatter:on
			});
		}

		case GAME_OVER -> {
			game().level().ifPresent(level -> {
				level3D.world3D().foodOscillation().stop();
				level3D.livesCounter3D().stopAnimation();
				context.actionHandler().showFlashMessageSeconds(3, PacManGames3dApp.pickGameOverMessage());
				context.soundHandler().audioClip(level.game().variant(), "audio.game_over").play();
				keepGameStateForSeconds(3);
			});
		}

		default -> {
			// ignore
		}

		}

		// on state exit
		if (e.oldState == null) {
			return;
		}
		switch (e.oldState) {
		case READY -> {
			readyMessageText3D.setVisible(false);
		}
		case HUNTING -> {
			if (e.newState != GameState.GHOST_DYING) {
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
		var rotation = new RotateTransition(Duration.seconds(1.5), level3D.root());
		rotation.setAxis(RND.nextBoolean() ? Rotate.X_AXIS : Rotate.Z_AXIS);
		rotation.setFromAngle(0);
		rotation.setToAngle(360);
		rotation.setInterpolator(Interpolator.LINEAR);

		return new SequentialTransition(
			actionAfterSeconds(1.0, () -> {
				perspectivePy.unbind();
				perspectivePy.set(Perspective.TOTAL);
			}),
			rotation,
			actionAfterSeconds(0.5, () -> clip("audio.sweep").play()),
			actionAfterSeconds(0.5, () -> perspectivePy.bind(PacManGames3dApp.PY_3D_PERSPECTIVE))
		);
	}

	private Animation createLevelCompleteAnimation(GameLevel level) {
		if (level.numFlashes == 0) {
			return pauseSeconds(1.0);
		}
		double wallHeight = PacManGames3dApp.PY_3D_WALL_HEIGHT.get();
		var animation = new SinusCurveAnimation(level.numFlashes);
		animation.setAmplitude(wallHeight);
		animation.elongationPy.set(level3D.world3D().wallHeightPy.get());
		level3D.world3D().wallHeightPy.bind(animation.elongationPy);
		animation.setOnFinished(e -> {
			level3D.world3D().wallHeightPy.bind(PacManGames3dApp.PY_3D_WALL_HEIGHT);
			PacManGames3dApp.PY_3D_WALL_HEIGHT.set(wallHeight);
		});
		return animation;
	}

	private void updateSound() {
		game().level().ifPresent(level -> {
			if (level.isDemoLevel()) {
				return;
			}
			if (level.pac().starvingTicks() > 8) { // TODO not sure how this is done in Arcade game
				clip("audio.pacman_munch").stop();
			}
			if (!level.thisFrame().pacKilled && level.ghosts(GhostState.RETURNING_TO_HOUSE, GhostState.ENTERING_HOUSE)
					.anyMatch(Ghost::isVisible)) {
				context.soundHandler().ensureLoopEndless(clip("audio.ghost_returning"));
			} else {
				clip("audio.ghost_returning").stop();
			}
		});
	}

	private AudioClip clip(String key) {
		return context.soundHandler().audioClip(game().variant(), key);
	}

	/**
	 * Locks the current game state, waits given seconds, plays given animations and unlocks the state when the animations
	 * have finished.
	 */
	private void lockStateAndPlayAfterSeconds(double afterSeconds, Animation... animations) {
		state().timer().resetIndefinitely();
		var animationSequence = new SequentialTransition(animations);
		if (afterSeconds > 0) {
			animationSequence.setDelay(Duration.seconds(afterSeconds));
		}
		animationSequence.setOnFinished(e -> state().timer().expire());
		animationSequence.play();
	}

	/**
	 * Keeps the current game state for given number of seconds, then forces the state timer to expire.
	 * 
	 * @param seconds seconds to keep game state
	 */
	private void keepGameStateForSeconds(double seconds) {
		state().timer().resetIndefinitely();
		actionAfterSeconds(seconds, () -> state().timer().expire()).play();
	}

	@Override
	public void onLevelStarted(GameEvent e) {
		game().level().ifPresent(level -> {
			switch (game().variant()) {
				case MS_PACMAN -> {
					var ss = (SpritesheetMsPacManGame) context.spritesheet();
					var images = game().levelCounter().stream().map(ss::bonusSymbolSprite).map(ss::subImage).toArray(Image[]::new);
					level3D.levelCounter3D().update(images);
				}
				case PACMAN -> {
					var ss = (SpritesheetPacManGame) context.spritesheet();
					var images = game().levelCounter().stream().map(ss::bonusSymbolSprite).map(ss::subImage).toArray(Image[]::new);
					level3D.levelCounter3D().update(images);
				}
				default -> throw new IllegalGameVariantException(game().variant());
			}
		});
	}
}