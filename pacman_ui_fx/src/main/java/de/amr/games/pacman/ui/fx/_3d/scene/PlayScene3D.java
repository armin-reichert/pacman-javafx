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
import de.amr.games.pacman.model.common.GameSound;
import de.amr.games.pacman.model.common.world.World;
import de.amr.games.pacman.ui.fx._3d.animation.SwingingWallsAnimation;
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
import javafx.scene.shape.DrawMode;
import javafx.scene.transform.Translate;

/**
 * 3D play scene with sound and animations.
 * 
 * @author Armin Reichert
 */
public class PlayScene3D implements GameScene {

	private static final Logger LOGGER = LogManager.getFormatterLogger();

	public final ObjectProperty<DrawMode> drawModePy = new SimpleObjectProperty<>(this, "drawMode", DrawMode.FILL);
	public final ObjectProperty<Color> floorColorPy = new SimpleObjectProperty<>(this, "floorColor", Color.BLACK);
	public final StringProperty floorTexturePy = new SimpleStringProperty(this, "floorTexture", "none");
	public final IntegerProperty mazeResolutionPy = new SimpleIntegerProperty(this, "mazeResolution", 4);
	public final DoubleProperty mazeWallHeightPy = new SimpleDoubleProperty(this, "mazeWallHeight", 2.5);
	public final DoubleProperty mazeWallThicknessPy = new SimpleDoubleProperty(this, "mazeWallThickness", 1.5);
	public final BooleanProperty pac3DLightedPy = new SimpleBooleanProperty(this, "pac3DLighted", false);
	public final ObjectProperty<Perspective> perspectivePy = new SimpleObjectProperty<>(this, "perspective",
			Perspective.TOTAL);
	public final BooleanProperty squirtingEffectPy = new SimpleBooleanProperty(this, "squirtingEffect", true);

	private final SubScene fxSubScene;
	private final Group content = new Group();
	private final CoordSystem coordSystem = new CoordSystem();
	private final AmbientLight ambientLight = new AmbientLight();
	private final Map<Perspective, GameSceneCamera> cameraMap = new EnumMap<>(Perspective.class);

	private SceneContext ctx;
	private World3D world3D;
	private Pac3D pac3D;
	private Ghost3D[] ghosts3D;
	private Bonus3D bonus3D;

	public PlayScene3D() {
		var root = new Group(content, coordSystem, ambientLight);
		// initial scene size is irrelevant
		fxSubScene = new SubScene(root, 42, 42, true, SceneAntialiasing.BALANCED);
		cameraMap.put(Perspective.DRONE, new CamDrone());
		cameraMap.put(Perspective.FOLLOWING_PLAYER, new CamFollowingPlayer());
		cameraMap.put(Perspective.NEAR_PLAYER, new CamNearPlayer());
		cameraMap.put(Perspective.TOTAL, new CamTotal());
		perspectivePy.addListener((py, oldVal, newPerspective) -> changeCamera(newPerspective));
	}

	public CoordSystem coordSystem() {
		return coordSystem;
	}

	public AmbientLight ambientLight() {
		return ambientLight;
	}

	@Override
	public boolean is3D() {
		return true;
	}

	@Override
	public void onKeyPressed() {
		if (Keyboard.pressed(KeyCode.DIGIT5) && !ctx.hasCredit()) {
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

	private void createWorld3D() {
		world3D = new World3D(ctx.game(), ctx.r2D());
		world3D.food3D().squirtingEffectPy.bind(squirtingEffectPy);
		world3D.maze3D().drawModePy.bind(drawModePy);
		world3D.maze3D().floorTexturePy.bind(Bindings.createObjectBinding(
				() -> "none".equals(floorTexturePy.get()) ? null : Ufx.image("graphics/" + floorTexturePy.get()),
				floorTexturePy));
		world3D.maze3D().floorColorPy.bind(floorColorPy);
		world3D.maze3D().resolutionPy.bind(mazeResolutionPy);
		world3D.maze3D().wallHeightPy.bind(mazeWallHeightPy);
		world3D.maze3D().wallThicknessPy.bind(mazeWallThicknessPy);
		LOGGER.info("3D world created for game level %d", ctx.game().level().number());
	}

	private void createPac3D() {
		pac3D = new Pac3D(ctx.game().pac());
		pac3D.init(ctx.world());
		pac3D.lightOnPy.bind(pac3DLightedPy);
		LOGGER.info("3D %s created", ctx.game().pac().name());
	}

	private void createGhosts3D() {
		ghosts3D = ctx.game().ghosts().map(ghost -> new Ghost3D(ghost, ctx.r2D().ghostColorScheme(ghost.id)))
				.toArray(Ghost3D[]::new);
		for (var ghost3D : ghosts3D) {
			ghost3D.init(ctx.game());
			ghost3D.drawModePy.bind(drawModePy);
		}
		LOGGER.info("3D ghosts created");
	}

	private void createBonus3D() {
		bonus3D = new Bonus3D();
	}

	@Override
	public void init() {
		createWorld3D();
		createPac3D();
		createGhosts3D();
		createBonus3D();
		content.getChildren().clear();
		content.getChildren().add(world3D); // always child #0, gets exchanged on level change
		content.getChildren().add(pac3D);
		content.getChildren().addAll(ghosts3D);
		content.getChildren().add(bonus3D);
		var width = ctx.world().numCols() * World.TS;
		var height = ctx.world().numRows() * World.TS;
		content.getTransforms().setAll(new Translate(-0.5 * width, -0.5 * height));
		changeCamera(perspectivePy.get());
	}

	@Override
	public void onTick() {
		world3D.update(ctx.game());
		pac3D.update(ctx.world());
		Stream.of(ghosts3D).forEach(ghost3D -> ghost3D.update(ctx.game()));
		bonus3D.update(ctx.game().bonus());
		currentCamera().update(pac3D);
	}

	@Override
	public SceneContext ctx() {
		return ctx;
	}

	@Override
	public void setContext(SceneContext ctx) {
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

	public GameSceneCamera getCamera(Perspective perspective) {
		return cameraMap.get(perspective);
	}

	public GameSceneCamera currentCamera() {
		return (GameSceneCamera) fxSubScene.getCamera();
	}

	public void changeCamera(Perspective newPerspective) {
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
		if (world3D != null && world3D.scores3D() != null) {
			world3D.scores3D().rotationAxisProperty().bind(newCamera.rotationAxisProperty());
			world3D.scores3D().rotateProperty().bind(newCamera.rotateProperty());
		}
	}

	@Override
	public void onSwitchFrom2D() {
		var world = ctx.world();
		world3D.food3D().pellets3D().forEach(pellet3D -> pellet3D.setVisible(!world.containsEatenFood(pellet3D.tile())));
		if (U.oneOf(ctx.state(), GameState.HUNTING, GameState.GHOST_DYING)) {
			world3D.food3D().energizers3D().forEach(Energizer3D::startPumping);
		}
	}

	@Override
	public void onPlayerFindsFood(GameEvent e) {
		if (e.tile.isEmpty()) {
			// when cheat "eat all pellets" is used, no tile is present in the event. In that case, bring 3D pellets to be in
			// synch with model:
			ctx.world().tiles() //
					.filter(ctx.world()::containsEatenFood) //
					.map(world3D.food3D()::pelletAt) //
					.flatMap(Optional::stream) //
					.forEach(Pellet3D::eat);
		} else {
			world3D.food3D().pelletAt(e.tile.get()).ifPresent(world3D.food3D()::eatPellet);
		}
	}

	@Override
	public void onPlayerGetsExtraLife(GameEvent e) {
		ctx.sounds().play(GameSound.EXTRA_LIFE);
	}

	@Override
	public void onBonusGetsActive(GameEvent e) {
		var sprite = ctx.r2D().bonusSymbolSprite(ctx.game().bonus().index());
		bonus3D.showSymbol(ctx.r2D().spritesheet().region(sprite));
	}

	@Override
	public void onBonusGetsEaten(GameEvent e) {
		var sprite = ctx.r2D().bonusValueSprite(ctx.game().bonus().index());
		bonus3D.showPoints(ctx.r2D().spritesheet().region(sprite));
		ctx.sounds().play(GameSound.BONUS_EATEN);
	}

	@Override
	public void onBonusExpires(GameEvent e) {
		bonus3D.setVisible(false);
	}

	@Override
	public void onGameStateChange(GameStateChangeEvent e) {
		switch (e.newGameState) {

		case READY -> {
			world3D.food3D().energizers3D().forEach(Energizer3D::init);
			pac3D.init(ctx.world());
			Stream.of(ghosts3D).forEach(ghost3D -> ghost3D.init(ctx.game()));
		}

		case HUNTING -> world3D.food3D().energizers3D().forEach(Energizer3D::startPumping);

		case PACMAN_DYING -> ctx.game().ghosts().filter(ctx.game().pac()::sameTile).findAny().ifPresent(killer -> {
			lockGameState();
			var animation = new SequentialTransition( //
					pause(0.3), //
					pac3D.createDyingAnimation(ctx.r2D().ghostColor(killer.id)), //
					pause(2.0) //
			);
			animation.setOnFinished(evt -> unlockGameState());
			animation.play();
		});

		case GHOST_DYING -> {
			ctx.game().memo.killedGhosts.forEach(killedGhost -> {
				int index = killedGhost.killedIndex;
				var sprite = ctx.r2D().createGhostValueList().frame(index);
				var image = ctx.r2D().spritesheet().region(sprite);
				ghosts3D[killedGhost.id].setNumberImage(image);
			});
		}

		case LEVEL_STARTING -> {
			LOGGER.info("Starting level %d", ctx.game().level().number());
			lockGameState();
			createWorld3D();
			content.getChildren().set(0, world3D);
			changeCamera(perspectivePy.get());
			Actions.showFlashMessage(TextManager.message("level_starting", ctx.game().level().number()));
			pause(3, this::unlockGameState).play();
		}

		case LEVEL_COMPLETE -> {
			lockGameState();
			var message = "%s%n%n%s".formatted(TextManager.TALK_LEVEL_COMPLETE.next(),
					TextManager.message("level_complete", ctx.game().level().number()));
			var animation = new SequentialTransition( //
					pause(1.0), //
					ctx.game().level().numFlashes() > 0 ? new SwingingWallsAnimation(ctx.game().level().numFlashes())
							: pause(1.0), //
					pause(1.0, ctx.game().pac()::hide), //
					pause(0.5, () -> Actions.showFlashMessage(2, message)), //
					pause(2.0) //
			);
			animation.setOnFinished(evt -> unlockGameState());
			animation.play();
		}

		case GAME_OVER -> Actions.showFlashMessage(3, TextManager.TALK_GAME_OVER.next());

		default -> { // ignore
		}
		}

		// exit HUNTING
		if (e.oldGameState == GameState.HUNTING && e.newGameState != GameState.GHOST_DYING) {
			world3D.food3D().energizers3D().forEach(Energizer3D::stopPumping);
			bonus3D.setVisible(false);
		}
	}
}