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

import static de.amr.games.pacman.model.common.world.World.HTS;
import static de.amr.games.pacman.model.common.world.World.TS;

import java.util.EnumMap;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.common.GameController;
import de.amr.games.pacman.controller.common.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.actors.Ghost;
import de.amr.games.pacman.model.common.actors.GhostState;
import de.amr.games.pacman.model.common.world.ArcadeWorld;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx._2d.rendering.mspacman.Spritesheet_MsPacMan;
import de.amr.games.pacman.ui.fx._2d.rendering.pacman.Spritesheet_PacMan;
import de.amr.games.pacman.ui.fx._3d.animation.Rendering3D;
import de.amr.games.pacman.ui.fx._3d.entity.Bonus3D;
import de.amr.games.pacman.ui.fx._3d.entity.Ghost3D;
import de.amr.games.pacman.ui.fx._3d.entity.Ghost3D.AnimationMode;
import de.amr.games.pacman.ui.fx._3d.entity.LevelCounter3D;
import de.amr.games.pacman.ui.fx._3d.entity.LivesCounter3D;
import de.amr.games.pacman.ui.fx._3d.entity.Maze3D;
import de.amr.games.pacman.ui.fx._3d.entity.Pac3D;
import de.amr.games.pacman.ui.fx._3d.entity.Scores3D;
import de.amr.games.pacman.ui.fx._3d.model.GianmarcosModel3D;
import de.amr.games.pacman.ui.fx._3d.model.PacManModel3D;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.shell.Actions;
import de.amr.games.pacman.ui.fx.shell.Keyboard;
import de.amr.games.pacman.ui.fx.util.CoordinateAxes;
import de.amr.games.pacman.ui.fx.util.U;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.transform.Translate;

/**
 * 3D play scene with sound and animations.
 * 
 * @author Armin Reichert
 */
public class PlayScene3D implements GameScene {

	private final SubScene fxSubScene;
	private final AmbientLight light = new AmbientLight(Color.GHOSTWHITE);
	private final Image floorTexture = U.image("/common/escher-texture.jpg");
	private final Color floorColorWithTexture = Color.DARKBLUE;
	private final Color floorColorWithoutTexture = Color.rgb(5, 5, 10);
	private final SimpleObjectProperty<Perspective> $perspective = new SimpleObjectProperty<>();
	private final SimpleBooleanProperty $useMazeFloorTexture = new SimpleBooleanProperty();
	private final EnumMap<Perspective, PlaySceneCamera> cameras = new EnumMap<>(Perspective.class);

	// scene context
	private GameController gameController;
	private GameModel game;
	private PacManModel3D model3D;
	private Rendering2D r2D;

	private Group root;
	private Pac3D player3D;
	private Maze3D maze3D;
	private Ghost3D[] ghosts3D;
	private Bonus3D bonus3D;
	private Scores3D scores3D;
	private LevelCounter3D levelCounter3D;
	private LivesCounter3D livesCounter3D;

	public PlayScene3D() {
		cameras.put(Perspective.CAM_DRONE, new Cam_Drone());
		cameras.put(Perspective.CAM_FOLLOWING_PLAYER, new Cam_FollowingPlayer());
		cameras.put(Perspective.CAM_NEAR_PLAYER, new Cam_NearPlayer());
		cameras.put(Perspective.CAM_TOTAL, new Cam_Total());

		var axes = new CoordinateAxes(1000);
		axes.visibleProperty().bind(Env.$axesVisible);

		// first child is placeholder for scene content (world3D)
		root = new Group(new Group(), axes, light);
		// width and height of subscene are defined using data binding, see class GameScenes
		fxSubScene = new SubScene(root, 1, 1, true, SceneAntialiasing.BALANCED);

		$perspective.bind(Env.$perspective);
		$perspective.addListener(($perspective, oldPerspective, newPerspective) -> setCameraPerspective(newPerspective));
		$useMazeFloorTexture.bind(Env.$useMazeFloorTexture);
		$useMazeFloorTexture.addListener(($useMazeFloorTexture, oldValue, newValue) -> setUseMazeFloorTexture(newValue));
	}

	@Override
	public SubScene getFXSubScene() {
		return fxSubScene;
	}

	public PlaySceneCamera getCamera() {
		return (PlaySceneCamera) fxSubScene.getCamera();
	}

	@Override
	public void setParent(Scene parent) {
		fxSubScene.widthProperty().bind(parent.widthProperty());
		fxSubScene.heightProperty().bind(parent.heightProperty());
	}

	@Override
	public void setSceneContext(GameController gameController) {
		this.gameController = gameController;
		this.game = gameController.game();
		r2D = switch (game.variant) {
		case MS_PACMAN -> Spritesheet_MsPacMan.get();
		case PACMAN -> Spritesheet_PacMan.get();
		};
		model3D = GianmarcosModel3D.get();
	}

	@Override
	public void init() {
		boolean hasCredit = game.credit > 0;

		buildMaze3D();

		scores3D = new Scores3D();
		scores3D.setFont(r2D.getArcadeFont());
		if (!hasCredit) {
			scores3D.setComputeScoreText(false);
			scores3D.txtScore.setFill(Color.RED);
			scores3D.txtScore.setText("GAME OVER!");
		} else {
			scores3D.setComputeScoreText(true);
		}

		livesCounter3D = new LivesCounter3D(model3D);
		livesCounter3D.getTransforms().add(new Translate(TS, TS, -HTS));
		livesCounter3D.setVisible(hasCredit);

		levelCounter3D = new LevelCounter3D(ArcadeWorld.SIZE.x - TS, TS, r2D);
		levelCounter3D.update(game);

		player3D = new Pac3D(game.level.world, game.pac, model3D);
		ghosts3D = game.ghosts().map(ghost -> new Ghost3D(ghost, model3D, r2D)).toArray(Ghost3D[]::new);
		bonus3D = new Bonus3D(r2D);

		var world3D = new Group();
		world3D.setTranslateX(-ArcadeWorld.SIZE.x / 2);
		world3D.setTranslateY(-ArcadeWorld.SIZE.y / 2);
		world3D.getChildren().addAll(maze3D, scores3D, livesCounter3D, levelCounter3D, player3D, bonus3D);
		world3D.getChildren().addAll(ghosts3D);

		root.getChildren().set(0, world3D);

		setCameraPerspective($perspective.get());
		setUseMazeFloorTexture($useMazeFloorTexture.get());
	}

	@Override
	public void end() {
		// Note: property bindings are garbage collected, no need to explicitly unbind them here
		maze3D.$resolution.removeListener(this::onMazeResolutionChange);
	}

	@Override
	public void onKeyPressed() {
		if (Keyboard.pressed(Keyboard.ALT, KeyCode.LEFT)) {
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
	public void update() {
		maze3D.update(game);
		player3D.update();
		Stream.of(game.ghosts).forEach(this::updateGhost3D);
		bonus3D.update(game.bonus());
		scores3D.update(game);
		livesCounter3D.update(game.playing ? game.lives - 1 : game.lives);
		getCamera().update(player3D);
	}

	private void updateGhost3D(Ghost ghost) {
		var ghost3D = ghosts3D[ghost.id];
		if (ghost.killIndex != -1) {
			ghost3D.setAnimationMode(AnimationMode.NUMBER);
		} else if (ghost.is(GhostState.DEAD) || ghost.is(GhostState.ENTERING_HOUSE)) {
			ghost3D.setAnimationMode(AnimationMode.EYES);
		} else if (game.pac.hasPower() && !ghost.is(GhostState.LEAVING_HOUSE)) {
			ghost3D.setAnimationMode(AnimationMode.FRIGHTENED);
		} else {
			ghost3D.setAnimationMode(AnimationMode.COLORED);
		}
		ghost3D.update();
	}

	public void onSwitchFrom2D() {
		if (game.pac.hasPower()) {
			Stream.of(ghosts3D) //
					.filter(ghost3D -> ghost3D.ghost.is(GhostState.FRIGHTENED) || ghost3D.ghost.is(GhostState.LOCKED))
					.forEach(ghost3D -> ghost3D.setAnimationMode(AnimationMode.FRIGHTENED));
		}
		maze3D.foodNodes()
				.forEach(foodNode -> foodNode.setVisible(!game.level.world.containsEatenFood(maze3D.tile(foodNode))));
		if (gameController.state() == GameState.HUNTING || gameController.state() == GameState.GHOST_DYING) {
			maze3D.energizerAnimations().forEach(Animation::play);
		}
	}

	private void onMazeResolutionChange(ObservableValue<? extends Number> property, Number oldValue, Number newValue) {
		if (!oldValue.equals(newValue)) {
			maze3D.createWallsAndDoors(game.level.world, //
					Rendering3D.getMazeSideColor(game.variant, game.level.mazeNumber), //
					Rendering3D.getMazeTopColor(game.variant, game.level.mazeNumber), //
					Rendering3D.getGhostHouseDoorColor(game.variant, game.level.mazeNumber));
		}
	}

	private void setCameraPerspective(Perspective perspective) {
		var camera = cameras.get(perspective);
		fxSubScene.setCamera(camera);
		fxSubScene.setOnKeyPressed(camera::onKeyPressed);
		fxSubScene.requestFocus();
		if (scores3D != null) {
			// keep the score in plain sight
			scores3D.rotationAxisProperty().bind(camera.rotationAxisProperty());
			scores3D.rotateProperty().bind(camera.rotateProperty());
		}
		camera.reset();
	}

	private void setUseMazeFloorTexture(boolean useTexture) {
		if (useTexture) {
			maze3D.getFloor().setTexture(floorTexture);
			maze3D.getFloor().setColor(floorColorWithTexture);
		} else {
			maze3D.getFloor().setTexture(null);
			maze3D.getFloor().setColor(floorColorWithoutTexture);
		}
	}

	@Override
	public boolean is3D() {
		return true;
	}

	@Override
	public void onPlayerGetsPower(GameEvent e) {
		Stream.of(ghosts3D) //
				.filter(ghost3D -> ghost3D.ghost.is(GhostState.FRIGHTENED) || ghost3D.ghost.is(GhostState.LOCKED))
				.forEach(ghost3D -> ghost3D.setAnimationMode(AnimationMode.FRIGHTENED));
	}

	@Override
	public void onPlayerStartsLosingPower(GameEvent e) {
		Stream.of(ghosts3D) //
				.filter(ghost3D -> ghost3D.ghost.is(GhostState.FRIGHTENED) || ghost3D.ghost.is(GhostState.LOCKED)) //
				.forEach(Ghost3D::playFlashingAnimation);
	}

	@Override
	public void onPlayerLosesPower(GameEvent e) {
		Stream.of(ghosts3D).forEach(ghost3D -> ghost3D.setAnimationMode(AnimationMode.COLORED));
	}

	@Override
	public void onPlayerFindsFood(GameEvent e) {
		// when cheat "eat all pellets" is used, no tile is present
		if (!e.tile.isPresent()) {
			game.level.world.tiles().filter(game.level.world::containsEatenFood)
					.forEach(tile -> maze3D.foodAt(tile).ifPresent(maze3D::hideFood));
		} else {
			maze3D.foodAt(e.tile.get()).ifPresent(maze3D::hideFood);
		}
	}

	@Override
	public void onBonusGetsActive(GameEvent e) {
		bonus3D.showSymbol(game.bonus());
	}

	@Override
	public void onBonusGetsEaten(GameEvent e) {
		bonus3D.showPoints(game.bonus());
	}

	@Override
	public void onBonusExpires(GameEvent e) {
		bonus3D.setVisible(false);
	}

	@Override
	public void onGhostStartsLeavingHouse(GameEvent e) {
		e.ghost.ifPresent(ghost -> ghosts3D[ghost.id].playRevivalAnimation());
	}

	@Override
	public void onGameStateChange(GameStateChangeEvent e) {
		switch (e.newGameState) {
		default -> {
		}
		case READY -> {
			maze3D.reset();
			player3D.reset();
			Stream.of(ghosts3D).forEach(ghost3D -> ghost3D.reset());
		}
		case HUNTING -> {
			maze3D.energizerAnimations().forEach(Animation::play);
		}
		case PACMAN_DYING -> {
			blockGameController();
			Stream.of(ghosts3D).forEach(ghost3D -> ghost3D.setAnimationMode(AnimationMode.COLORED));
			var killer = game.ghosts().filter(ghost -> ghost.sameTile(game.pac)).findAny().get();
			var killerColor = r2D.getGhostColor(killer.id);
			new SequentialTransition( //
					player3D.dyingAnimation(killerColor, game.credit == 0), //
					U.pauseSec(2.0, () -> unblockGameController()) //
			).play();
		}
		case LEVEL_STARTING -> {
			blockGameController();
			buildMazeContent(game.level.mazeNumber);
			levelCounter3D.update(game);
			Actions.showFlashMessage(Env.message("level_starting", game.level.number));
			U.pauseSec(3, () -> unblockGameController()).play();
		}
		case LEVEL_COMPLETE -> {
			blockGameController();
			Stream.of(ghosts3D).forEach(ghost3D -> ghost3D.setAnimationMode(AnimationMode.COLORED));
			var message = Env.LEVEL_COMPLETE_TALK.next() + "\n\n" + Env.message("level_complete", game.level.number);
			new SequentialTransition( //
					U.pauseSec(2.0), //
					maze3D.createMazeFlashingAnimation(game.level.numFlashes), //
					U.pauseSec(1.0, () -> game.pac.hide()), //
					U.pauseSec(0.5, () -> Actions.showFlashMessage(2, message)), //
					U.pauseSec(2.0, () -> unblockGameController()) //
			).play();
		}
		case GAME_OVER -> {
			Actions.showFlashMessage(3, Env.GAME_OVER_TALK.next());
		}
		}

		// exit HUNTING
		if (e.oldGameState == GameState.HUNTING && e.newGameState != GameState.GHOST_DYING) {
			maze3D.energizerAnimations().forEach(Animation::stop);
			bonus3D.setVisible(false);
		}
	}

	private void blockGameController() {
		gameController.state().timer().resetIndefinitely();
	}

	private void unblockGameController() {
		gameController.state().timer().expire();
	}

	private void buildMaze3D() {
		maze3D = new Maze3D(ArcadeWorld.SIZE.x, ArcadeWorld.SIZE.y);
		maze3D.$wallHeight.bind(Env.$mazeWallHeight);
		maze3D.$resolution.bind(Env.$mazeResolution);
		maze3D.$resolution.addListener(this::onMazeResolutionChange);
		buildMazeContent(game.level.mazeNumber);
	}

	private void buildMazeContent(int mazeNumber) {
		maze3D.createWallsAndDoors(game.level.world, //
				Rendering3D.getMazeSideColor(game.variant, mazeNumber), //
				Rendering3D.getMazeTopColor(game.variant, mazeNumber), //
				Rendering3D.getGhostHouseDoorColor(game.variant, mazeNumber));
		maze3D.createFood(game.level.world, r2D.getFoodColor(mazeNumber));
	}
}