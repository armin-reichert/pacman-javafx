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
import de.amr.games.pacman.ui.fx._3d.entity.Text3D;
import de.amr.games.pacman.ui.fx._3d.scene.cams.CamDrone;
import de.amr.games.pacman.ui.fx._3d.scene.cams.CamFollowingPlayer;
import de.amr.games.pacman.ui.fx._3d.scene.cams.CamNearPlayer;
import de.amr.games.pacman.ui.fx._3d.scene.cams.CamTotal;
import de.amr.games.pacman.ui.fx._3d.scene.cams.GameSceneCamera;
import de.amr.games.pacman.ui.fx._3d.scene.cams.Perspective;
import de.amr.games.pacman.ui.fx.app.Actions;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.app.Keys;
import de.amr.games.pacman.ui.fx.app.ResourceMgr;
import de.amr.games.pacman.ui.fx.input.Keyboard;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.sound.SoundClipID;
import de.amr.games.pacman.ui.fx.sound.SoundHandler;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.SequentialTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
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
			Perspective.TOTAL);

	private final Group root = new Group();
	private final Map<Perspective, GameSceneCamera> cameraMap = new EnumMap<>(Perspective.class);
	private GameLevel3D level3D;
	private Text3D infoText3D;

	public PlayScene3D(GameController gameController) {
		super(gameController);

		cameraMap.put(Perspective.DRONE, new CamDrone());
		cameraMap.put(Perspective.FOLLOWING_PLAYER, new CamFollowingPlayer());
		cameraMap.put(Perspective.NEAR_PLAYER, new CamNearPlayer());
		cameraMap.put(Perspective.TOTAL, new CamTotal());
		perspectivePy.addListener((property, oldVal, newVal) -> changeCameraPerspective(newVal));

		var coordSystem = new CoordSystem();
		coordSystem.visibleProperty().bind(Env.d3axesVisiblePy);

		var ambientLight = new AmbientLight();
		ambientLight.colorProperty().bind(Env.d3lightColorPy);

		infoText3D = new Text3D(72, 8);

		root.getChildren().addAll(new Group() /* placeholder for 3D level */, coordSystem, ambientLight, infoText3D);

		// initial scene size is irrelevant
		fxSubScene = new SubScene(root, 42, 42, true, SceneAntialiasing.BALANCED);
	}

	@Override
	public void init() {
		context.level().ifPresent(this::replaceGameLevel3D);
		initInfoText();
		infoText3D.setVisible(false);
		perspectivePy.bind(Env.d3perspectivePy);
	}

	@Override
	public void update() {
		context.level().ifPresent(level -> {
			level3D.update();
			currentCamera().update(level3D.pac3D().getRoot());
			updateSound(level);
		});
	}

	private void initInfoText() {
		infoText3D.setBgColor(Color.BLACK);
		infoText3D.setColor(Color.WHITE);
		infoText3D.setFont(context.rendering2D().screenFont(8));
		infoText3D.setTranslateX(0);
		infoText3D.setTranslateY(20);
		infoText3D.setTranslateZ(-6);
		infoText3D.setRotationAxis(Rotate.X_AXIS);
		infoText3D.setRotate(90);
	}

	@Override
	public void end() {
		context.sounds().stopAll();
		perspectivePy.unbind();
	}

	@Override
	public void render() {
		// nothing to do
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
		var width = level.world().numCols() * World.TS;
		var height = level.world().numRows() * World.TS;
		level3D = new GameLevel3D(level, context.rendering2D());
		level3D.getRoot().getTransforms().setAll(new Translate(-0.5 * width, -0.5 * height));
		root.getChildren().set(0, level3D.getRoot());
		changeCameraPerspective(perspectivePy.get());
		LOG.trace("3D game level created.");
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

	public GameSceneCamera getCamera(Perspective perspective) {
		return cameraMap.get(perspective);
	}

	public GameSceneCamera currentCamera() {
		return (GameSceneCamera) fxSubScene.getCamera();
	}

	private void changeCameraPerspective(Perspective newPerspective) {
		var newCamera = getCamera(newPerspective);
		if (newCamera == null) {
			LOG.error("No camera found for perspective %s", newPerspective);
			return;
		}
		if (newCamera != fxSubScene.getCamera()) {
			fxSubScene.setCamera(newCamera);
			fxSubScene.setOnKeyPressed(newCamera::onKeyPressed);
			fxSubScene.requestFocus();
			newCamera.reset();
		}
		// this rotates the scores such that the viewer always sees them frontally
		if (level3D != null && level3D.scores3D() != null) {
			level3D.scores3D().getRoot().rotationAxisProperty().bind(newCamera.rotationAxisProperty());
			level3D.scores3D().getRoot().rotateProperty().bind(newCamera.rotateProperty());
		}
		LOG.trace("Perspective changed to %s (%s)", newPerspective, this);
	}

	@Override
	public void onSceneVariantSwitch() {
		context.level().ifPresent(level -> {
			level3D.eatables3D()
					.forEach(eatable3D -> eatable3D.getRoot().setVisible(!level.world().containsEatenFood(eatable3D.tile())));
			if (U.oneOf(context.state(), GameState.HUNTING, GameState.GHOST_DYING)) {
				level3D.energizers3D().forEach(Energizer3D::startPumping);
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
						.map(level3D::eatableAt) //
						.flatMap(Optional::stream) //
						.forEach(Eatable3D::eat);
			});
		} else {
			var eatable = level3D.eatableAt(e.tile.get());
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
			context.level().ifPresent(level -> {
				level3D.pac3D().init();
				Stream.of(level3D.ghosts3D()).forEach(ghost3D -> ghost3D.init(level));
				if (Env.d3foodOscillationEnabledPy.get()) {
					level3D.foodOscillation().play();
				}
			});
		}

		case HUNTING -> level3D.energizers3D().forEach(Energizer3D::startPumping);

		case PACMAN_DYING -> {
			context.game().level().ifPresent(level -> {
				level.ghosts().filter(level.pac()::sameTile).findAny().ifPresent(killer -> {
					lockGameState();
					level3D.foodOscillation().stop();
					var animation = new SequentialTransition(Ufx.pause(1.3), level3D.pac3D().createDyingAnimation());
					animation.setOnFinished(evt -> unlockGameState());
					animation.play();
				});
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
			lockGameState();
			context.level().ifPresent(level -> {
				LOG.trace("Starting level %d", level.number());
				replaceGameLevel3D(level);
				Actions.showFlashMessage(ResourceMgr.message("level_starting", level.number()));
			});
			afterSeconds(3, this::unlockGameState).play();
		}

		case LEVEL_COMPLETE -> {
			context.level().ifPresent(level -> {
				lockGameState();
				level3D.foodOscillation().stop();
				var message = "%s%n%n%s".formatted(ResourceMgr.getLevelCompleteMessage(),
						ResourceMgr.message("level_complete", level.number()));
				var animation = new SequentialTransition( //
						Ufx.pause(1.0), //
						level.numFlashes > 0 ? new SwingingWallsAnimation(level.numFlashes) : Ufx.pause(1.0), //
						afterSeconds(1.0, level.pac()::hide), //
						afterSeconds(0.5, () -> Actions.showFlashMessageSeconds(2, message)), //
						Ufx.pause(2.0) //
				);
				animation.setOnFinished(evt -> unlockGameState());
				animation.play();
			});
		}

		case GAME_OVER -> {
			level3D.foodOscillation().stop();
			Actions.showFlashMessageSeconds(3, ResourceMgr.getGameOverMessage());
		}

		default -> { // ignore
		}
		}

		// exit HUNTING
		if (e.oldGameState == GameState.HUNTING && e.newGameState != GameState.GHOST_DYING) {
			level3D.energizers3D().forEach(Energizer3D::stopPumping);
			level3D.bonus3D().hide();
		}
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