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

import static de.amr.games.pacman.ui.fx.util.Ufx.pause;

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
import de.amr.games.pacman.model.common.GameSound;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx.Actions;
import de.amr.games.pacman.ui.fx.Env;
import de.amr.games.pacman.ui.fx._3d.animation.SwingingWallsAnimation;
import de.amr.games.pacman.ui.fx._3d.entity.Energizer3D;
import de.amr.games.pacman.ui.fx._3d.entity.GameLevel3D;
import de.amr.games.pacman.ui.fx._3d.entity.Pellet3D;
import de.amr.games.pacman.ui.fx._3d.scene.cams.CamDrone;
import de.amr.games.pacman.ui.fx._3d.scene.cams.CamFollowingPlayer;
import de.amr.games.pacman.ui.fx._3d.scene.cams.CamNearPlayer;
import de.amr.games.pacman.ui.fx._3d.scene.cams.CamTotal;
import de.amr.games.pacman.ui.fx._3d.scene.cams.GameSceneCamera;
import de.amr.games.pacman.ui.fx._3d.scene.cams.Perspective;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.scene.GameSceneContext;
import de.amr.games.pacman.ui.fx.util.Keyboard;
import de.amr.games.pacman.ui.fx.util.Modifier;
import de.amr.games.pacman.ui.fx.util.TextManager;
import de.amr.games.pacman.ui.fx.util.Ufx;
import javafx.animation.SequentialTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.transform.Translate;

/**
 * 3D play scene with sound and animations.
 * 
 * @author Armin Reichert
 */
public class PlayScene3D implements GameScene {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	public final ObjectProperty<Color> floorColorPy = new SimpleObjectProperty<>(this, "floorColor", Color.BLACK);
	public final StringProperty floorTexturePy = new SimpleStringProperty(this, "floorTexture", "none");
	public final IntegerProperty mazeResolutionPy = new SimpleIntegerProperty(this, "mazeResolution", 4);
	public final DoubleProperty mazeWallHeightPy = new SimpleDoubleProperty(this, "mazeWallHeight", 2.5);
	public final DoubleProperty mazeWallThicknessPy = new SimpleDoubleProperty(this, "mazeWallThickness", 1.5);
	public final ObjectProperty<Perspective> perspectivePy = new SimpleObjectProperty<>(this, "perspective",
			Perspective.TOTAL);
	public final BooleanProperty squirtingEffectPy = new SimpleBooleanProperty(this, "squirtingEffect", true);

	private final SubScene fxSubScene;
	private final Group levelContainer = new Group();
	private final CoordSystem coordSystem = new CoordSystem();
	private final AmbientLight ambientLight = new AmbientLight();
	private final Map<Perspective, GameSceneCamera> cameraMap = new EnumMap<>(Perspective.class);

	private GameSceneContext ctx;
	private GameLevel3D level3D;

	public PlayScene3D() {
		var root = new Group(levelContainer, coordSystem, ambientLight);
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
		ctx.level().ifPresent(this::createGameLevel3D);
	}

	@Override
	public void resizeToHeight(float height) {
		// nothing to do
	}

	private void createGameLevel3D(GameLevel level) {
		var width = level.world().numCols() * World.TS;
		var height = level.world().numRows() * World.TS;

		level3D = new GameLevel3D(level, ctx.r2D());
		level3D.drawModePy.bind(Env.drawModePy);
		level3D.pac3DLightedPy.bind(Env.pac3DLightedPy);
		level3D.food3D().squirtingEffectPy.bind(squirtingEffectPy);
		level3D.world3D().floorTexturePy.bind(Bindings.createObjectBinding(
				() -> "none".equals(floorTexturePy.get()) ? null : Ufx.image("graphics/" + floorTexturePy.get()),
				floorTexturePy));
		level3D.world3D().floorColorPy.bind(floorColorPy);
		level3D.world3D().resolutionPy.bind(mazeResolutionPy);
		level3D.world3D().wallHeightPy.bind(mazeWallHeightPy);
		level3D.world3D().wallThicknessPy.bind(mazeWallThicknessPy);

		levelContainer.getChildren().setAll(level3D);
		levelContainer.getTransforms().setAll(new Translate(-0.5 * width, -0.5 * height));

		changeCameraPerspective(perspectivePy.get());
		LOGGER.info("3D game level created.");
	}

	@Override
	public void onKeyPressed() {
		if (Keyboard.pressed(KeyCode.DIGIT5) && !ctx.hasCredit()) {
			Actions.addCredit(); // in demo mode, allow adding credit
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
	public void onTick() {
		ctx.level().ifPresent(level -> {
			level3D.update();
			currentCamera().update(level3D.pac3D());
		});
	}

	@Override
	public boolean is3D() {
		return true;
	}

	@Override
	public GameSceneContext ctx() {
		return ctx;
	}

	@Override
	public void setContext(GameSceneContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public SubScene fxSubScene() {
		return fxSubScene;
	}

	@Override
	public void embedInto(Scene parentScene) {
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
			LOGGER.error("No camera found for perspective %s", newPerspective);
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
			level3D.scores3D().rotationAxisProperty().bind(newCamera.rotationAxisProperty());
			level3D.scores3D().rotateProperty().bind(newCamera.rotateProperty());
		}
	}

	@Override
	public void onSwitchFrom2D() {
		ctx.world().ifPresent(world -> {
			level3D.food3D().pellets3D().forEach(pellet3D -> pellet3D.setVisible(!world.containsEatenFood(pellet3D.tile())));
			if (U.oneOf(ctx.state(), GameState.HUNTING, GameState.GHOST_DYING)) {
				level3D.food3D().energizers3D().forEach(Energizer3D::startPumping);
			}
		});
	}

	@Override
	public void onPlayerFindsFood(GameEvent e) {
		if (e.tile.isEmpty()) {
			// when cheat "eat all pellets" is used, no tile is present in the event.
			// In that case, bring 3D pellets to be in synch with model:
			ctx.world().ifPresent(world -> {
				world.tiles() //
						.filter(world::containsEatenFood) //
						.map(level3D.food3D()::pelletAt) //
						.flatMap(Optional::stream) //
						.forEach(Pellet3D::eat);
			});
		} else {
			level3D.food3D().pelletAt(e.tile.get()).ifPresent(level3D.food3D()::eatPellet);
		}
	}

	@Override
	public void onPlayerGetsExtraLife(GameEvent e) {
		ctx.sounds().play(GameSound.EXTRA_LIFE);
	}

	@Override
	public void onBonusGetsActive(GameEvent e) {
		ctx.level().ifPresent(level -> {
			var sprite = ctx.r2D().bonusSymbolSprite(level.bonus().symbol());
			level3D.bonus3D().showSymbol(ctx.r2D().spritesheet().region(sprite));
		});
	}

	@Override
	public void onBonusGetsEaten(GameEvent e) {
		ctx.level().ifPresent(level -> {
			var sprite = ctx.r2D().bonusValueSprite(level.bonus().symbol());
			level3D.bonus3D().showPoints(ctx.r2D().spritesheet().region(sprite));
			ctx.sounds().play(GameSound.BONUS_EATEN);
		});
	}

	@Override
	public void onBonusExpires(GameEvent e) {
		level3D.bonus3D().setVisible(false);
	}

	@Override
	public void onGameStateChange(GameStateChangeEvent e) {
		switch (e.newGameState) {

		case READY -> {
			ctx.level().ifPresent(level -> {
				level3D.food3D().energizers3D().forEach(Energizer3D::init);
				level3D.pac3D().init(level.world());
				Stream.of(level3D.ghosts3D()).forEach(ghost3D -> ghost3D.init(level));
			});
		}

		case HUNTING -> level3D.food3D().energizers3D().forEach(Energizer3D::startPumping);

		case PACMAN_DYING -> {
			ctx.game().level().ifPresent(level -> {
				level.ghosts().filter(level.pac()::sameTile).findAny().ifPresent(killer -> {
					lockGameState();
					var animation = new SequentialTransition( //
							pause(0.3), //
							level3D.pac3D().createDyingAnimation(ctx.r2D().ghostColor(killer.id())), //
							pause(2.0) //
					);
					animation.setOnFinished(evt -> unlockGameState());
					animation.play();
				});
			});
		}

		case GHOST_DYING -> {
			ctx.level().ifPresent(level -> {
				level.memo().killedGhosts.forEach(killedGhost -> {
					int index = killedGhost.killedIndex();
					var sprite = ctx.r2D().createGhostValueList().frame(index);
					var image = ctx.r2D().spritesheet().region(sprite);
					level3D.ghosts3D()[killedGhost.id()].setNumberImage(image);
				});
			});
		}

		case CHANGING_TO_NEXT_LEVEL -> {
			ctx.level().ifPresent(level -> {
				LOGGER.info("Starting level %d", level.number());
				lockGameState();
				createGameLevel3D(level);
				Actions.showFlashMessage(TextManager.message("level_starting", level.number()));
				pause(3, this::unlockGameState).play();
			});
		}

		case LEVEL_COMPLETE -> {
			ctx.level().ifPresent(level -> {
				lockGameState();
				var message = "%s%n%n%s".formatted(TextManager.TALK_LEVEL_COMPLETE.next(),
						TextManager.message("level_complete", level.number()));
				var animation = new SequentialTransition( //
						pause(1.0), //
						level.params().numFlashes() > 0 ? new SwingingWallsAnimation(level.params().numFlashes()) : pause(1.0), //
						pause(1.0, level.pac()::hide), //
						pause(0.5, () -> Actions.showFlashMessage(2, message)), //
						pause(2.0) //
				);
				animation.setOnFinished(evt -> unlockGameState());
				animation.play();
			});
		}

		case GAME_OVER -> Actions.showFlashMessage(3, TextManager.TALK_GAME_OVER.next());

		default -> { // ignore
		}
		}

		// exit HUNTING
		if (e.oldGameState == GameState.HUNTING && e.newGameState != GameState.GHOST_DYING) {
			level3D.food3D().energizers3D().forEach(Energizer3D::stopPumping);
			level3D.bonus3D().setVisible(false);
		}
	}
}