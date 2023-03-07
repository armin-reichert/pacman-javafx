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
import de.amr.games.pacman.ui.fx._2d.rendering.common.SpritesheetGameRenderer;
import de.amr.games.pacman.ui.fx._3d.animation.SwingingWallsAnimation;
import de.amr.games.pacman.ui.fx._3d.entity.Eatable3D;
import de.amr.games.pacman.ui.fx._3d.entity.Energizer3D;
import de.amr.games.pacman.ui.fx._3d.entity.GameLevel3D;
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
import de.amr.games.pacman.ui.fx.scene.GameSceneContext;
import de.amr.games.pacman.ui.fx.sound.common.SoundClipID;
import de.amr.games.pacman.ui.fx.sound.common.SoundHandler;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.SequentialTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.transform.Translate;

/**
 * 3D play scene with sound and animations.
 * 
 * @author Armin Reichert
 */
public class PlayScene3D implements GameScene {

	private static final Logger LOG = LogManager.getFormatterLogger();

	public final ObjectProperty<Color> floorColorPy = new SimpleObjectProperty<>(this, "floorColor", Color.BLACK);
	public final StringProperty floorTexturePy = new SimpleStringProperty(this, "floorTexture", Env.ThreeD.NO_TEXTURE);
	public final DoubleProperty mazeWallHeightPy = new SimpleDoubleProperty(this, "mazeWallHeight", 2.5);
	public final DoubleProperty mazeWallThicknessPy = new SimpleDoubleProperty(this, "mazeWallThickness", 1.5);
	public final ObjectProperty<Perspective> perspectivePy = new SimpleObjectProperty<>(this, "perspective",
			Perspective.TOTAL);
	public final BooleanProperty squirtingEffectPy = new SimpleBooleanProperty(this, "squirtingEffect", true);

	private final SubScene fxSubScene;
	private final Group root;
	private final CoordSystem coordSystem = new CoordSystem();
	private final AmbientLight ambientLight = new AmbientLight();
	private final Map<Perspective, GameSceneCamera> cameraMap = new EnumMap<>(Perspective.class);

	private GameSceneContext context;
	private GameLevel3D level3D;

	public PlayScene3D() {
		root = new Group(new Group() /* placeholder for level3D */, coordSystem, ambientLight);
		// initial scene size is irrelevant
		fxSubScene = new SubScene(root, 42, 42, true, SceneAntialiasing.BALANCED);
		cameraMap.put(Perspective.DRONE, new CamDrone());
		cameraMap.put(Perspective.FOLLOWING_PLAYER, new CamFollowingPlayer());
		cameraMap.put(Perspective.NEAR_PLAYER, new CamNearPlayer());
		cameraMap.put(Perspective.TOTAL, new CamTotal());
		perspectivePy.addListener((property, oldVal, newVal) -> changeCameraPerspective(newVal));
	}

	@Override
	public void init() {
		context.level().ifPresent(this::replaceGameLevel3D);
	}

	@Override
	public void update() {
		context.level().ifPresent(level -> {
			level3D.update();
			currentCamera().update(level3D.pac3D().getRoot());
			updateSound(level);
		});
	}

	@Override
	public void end() {
		context.level().ifPresent(level -> SoundHandler.sounds(level.game()).stopAll());
	}

	@Override
	public void draw() {
		// nothing to do
	}

	@Override
	public void resizeToHeight(double height) {
		// nothing to do
	}

	private void replaceGameLevel3D(GameLevel level) {
		var width = level.world().numCols() * World.TS;
		var height = level.world().numRows() * World.TS;

		level3D = new GameLevel3D(level, context.r2D());
		level3D.getRoot().getTransforms().setAll(new Translate(-0.5 * width, -0.5 * height));
		level3D.drawModePy.bind(Env.ThreeD.drawModePy);
		level3D.eatenAnimationEnabledPy.bind(squirtingEffectPy);
		level3D.world3D().floorColorPy.bind(floorColorPy);
		var textureBinding = Bindings.createObjectBinding( //
				() -> Env.ThreeD.NO_TEXTURE.equals(floorTexturePy.get()) //
						? null
						: ResourceMgr.image("graphics/" + floorTexturePy.get()),
				floorTexturePy);
		level3D.world3D().floorTexturePy.bind(textureBinding);
		level3D.world3D().wallHeightPy.bind(mazeWallHeightPy);
		level3D.world3D().wallThicknessPy.bind(mazeWallThicknessPy);

		root.getChildren().set(0, level3D.getRoot());
		changeCameraPerspective(perspectivePy.get());
		LOG.info("3D game level created.");
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

	@Override
	public GameSceneContext context() {
		return context;
	}

	@Override
	public void setContext(GameSceneContext context) {
		this.context = context;
	}

	@Override
	public SubScene fxSubScene() {
		return fxSubScene;
	}

	@Override
	public void onEmbed(Scene parentScene) {
		fxSubScene.widthProperty().bind(parentScene.widthProperty());
		fxSubScene.heightProperty().bind(parentScene.heightProperty());
	}

	public CoordSystem coordSystem() {
		return coordSystem;
	}

	public AmbientLight ambientLight() {
		return ambientLight;
	}

	public GameSceneCamera getCamera(Perspective perspective) {
		return cameraMap.get(perspective);
	}

	public GameSceneCamera currentCamera() {
		return (GameSceneCamera) fxSubScene.getCamera();
	}

	public void changeCameraPerspective(Perspective newPerspective) {
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
	}

	@Override
	public void onSwitchFrom2D() {
		context.world().ifPresent(world -> {
			level3D.eatables3D()
					.forEach(eatable3D -> eatable3D.getRoot().setVisible(!world.containsEatenFood(eatable3D.tile())));
			if (U.oneOf(context.state(), GameState.HUNTING, GameState.GHOST_DYING)) {
				level3D.energizers3D().forEach(Energizer3D::startPumping);
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
				level3D.energizers3D().forEach(Energizer3D::init);
				level3D.pac3D().init();
				Stream.of(level3D.ghosts3D()).forEach(ghost3D -> ghost3D.init(level));
			});
		}

		case HUNTING -> level3D.energizers3D().forEach(Energizer3D::startPumping);

		case PACMAN_DYING -> {
			context.game().level().ifPresent(level -> {
				level.ghosts().filter(level.pac()::sameTile).findAny().ifPresent(killer -> {
					lockGameState();
					var animation = new SequentialTransition( //
							Ufx.pause(0.2), //
							level3D.pac3D().createDyingAnimation(context.r2D().ghostColoring(killer.id()).normalDress()), //
							Ufx.pause(2.0) //
					);
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
					if (context.r2D() instanceof SpritesheetGameRenderer sgr) {
						ghost3D.setNumberImage(sgr.image(sgr.ghostValueRegion(index)));
					}
				});
			});
		}

		case CHANGING_TO_NEXT_LEVEL -> {
			lockGameState();
			context.level().ifPresent(level -> {
				LOG.info("Starting level %d", level.number());
				replaceGameLevel3D(level);
				Actions.showFlashMessage(ResourceMgr.message("level_starting", level.number()));
			});
			afterSeconds(3, this::unlockGameState).play();
		}

		case LEVEL_COMPLETE -> {
			context.level().ifPresent(level -> {
				lockGameState();
				var message = "%s%n%n%s".formatted(ResourceMgr.getLevelCompleteMessage(),
						ResourceMgr.message("level_complete", level.number()));
				var animation = new SequentialTransition( //
						Ufx.pause(1.0), //
						level.params().numFlashes() > 0 ? new SwingingWallsAnimation(level.params().numFlashes()) : Ufx.pause(1.0), //
						afterSeconds(1.0, level.pac()::hide), //
						afterSeconds(0.5, () -> Actions.showFlashMessageSeconds(2, message)), //
						Ufx.pause(2.0) //
				);
				animation.setOnFinished(evt -> unlockGameState());
				animation.play();
			});
		}

		case GAME_OVER -> Actions.showFlashMessageSeconds(3, ResourceMgr.getGameOverMessage());

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
		var sounds = SoundHandler.sounds(level.game());
		if (level.pac().starvingTicks() > 10) {
			sounds.stop(SoundClipID.PACMAN_MUNCH);
		}
		if (level.ghosts(GhostState.RETURNING_TO_HOUSE, GhostState.ENTERING_HOUSE).filter(Ghost::isVisible).count() > 0) {
			sounds.ensureLoop(SoundClipID.GHOST_RETURNING, AudioClip.INDEFINITE);
		} else {
			sounds.stop(SoundClipID.GHOST_RETURNING);
		}
	}
}