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
import de.amr.games.pacman.event.GameEventAdapter;
import de.amr.games.pacman.event.GameStateChangeEvent;
import de.amr.games.pacman.lib.V2i;
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
import de.amr.games.pacman.ui.fx._3d.entity.LevelCounter3D;
import de.amr.games.pacman.ui.fx._3d.entity.LivesCounter3D;
import de.amr.games.pacman.ui.fx._3d.entity.Maze3D;
import de.amr.games.pacman.ui.fx._3d.entity.Pac3D;
import de.amr.games.pacman.ui.fx._3d.entity.Score3D;
import de.amr.games.pacman.ui.fx._3d.model.GianmarcosModel3D;
import de.amr.games.pacman.ui.fx._3d.model.PacManModel3D;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.shell.Actions;
import de.amr.games.pacman.ui.fx.shell.Keyboard;
import de.amr.games.pacman.ui.fx.sound.PlaySceneSounds;
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
public class PlayScene3D extends GameEventAdapter implements GameScene, Rendering3D {

	private final SubScene fxSubScene;
	private final AmbientLight light = new AmbientLight(Color.GHOSTWHITE);
	private final Image floorTexture = U.image("/common/escher-texture.jpg");
	private final Color floorColorWithTexture = Color.DARKBLUE;
	private final Color floorColorNoTexture = Color.rgb(30, 30, 30);
	private final SimpleObjectProperty<Perspective> $perspective = new SimpleObjectProperty<>();
	private final SimpleBooleanProperty $useMazeFloorTexture = new SimpleBooleanProperty();
	private final EnumMap<Perspective, PlaySceneCamera> cameras = new EnumMap<>(Perspective.class);

	// scene context
	private GameController gameController;
	private GameModel game;
	private PacManModel3D model3D;
	private Rendering2D r2D;

	private Pac3D player3D;
	private Maze3D maze3D;
	private Ghost3D[] ghosts3D;
	private Bonus3D bonus3D;
	private Score3D score3D;
	private LevelCounter3D levelCounter3D;
	private LivesCounter3D livesCounter3D;

	public PlayScene3D() {
		cameras.put(Perspective.CAM_DRONE, new Cam_Drone());
		cameras.put(Perspective.CAM_FOLLOWING_PLAYER, new Cam_FollowingPlayer());
		cameras.put(Perspective.CAM_NEAR_PLAYER, new Cam_NearPlayer());
		cameras.put(Perspective.CAM_TOTAL, new Cam_Total());

		var axes = new CoordinateAxes(1000);
		axes.visibleProperty().bind(Env.$axesVisible);

		// first child is placeholder for scene content
		var root = new Group(new Group(), axes, light);
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
		boolean hasCredit = gameController.credit() > 0;

		maze3D = new Maze3D(ArcadeWorld.SIZE.x, ArcadeWorld.SIZE.y);
		maze3D.$wallHeight.bind(Env.$mazeWallHeight);
		maze3D.$resolution.bind(Env.$mazeResolution);
		maze3D.$resolution.addListener(this::onMazeResolutionChange);
		int mazeNumber = r2D.mazeNumber(game.level.number);
		maze3D.createWallsAndDoors(game.level.world, //
				getMazeSideColor(game.variant, mazeNumber), //
				getMazeTopColor(game.variant, mazeNumber), //
				getGhostHouseDoorColor(game.variant, mazeNumber));
		maze3D.createFood(game.level.world, r2D.getFoodColor(mazeNumber));

		player3D = new Pac3D(game.level.world, game.pac, model3D);
		ghosts3D = game.ghosts().map(ghost -> new Ghost3D(ghost, model3D, r2D)).toArray(Ghost3D[]::new);
		bonus3D = new Bonus3D(r2D);

		score3D = new Score3D();
		score3D.setFont(r2D.getArcadeFont());
		if (!hasCredit) {
			score3D.setComputeScoreText(false);
			score3D.txtScore.setFill(Color.RED);
			score3D.txtScore.setText("GAME OVER!");
		} else {
			score3D.setComputeScoreText(true);
		}

		livesCounter3D = new LivesCounter3D(model3D);
		livesCounter3D.getTransforms().add(new Translate(TS, TS, -HTS));
		livesCounter3D.setVisible(hasCredit);

		levelCounter3D = new LevelCounter3D(ArcadeWorld.SIZE.x - TS, TS, r2D);
		levelCounter3D.update(game);

		var world3D = new Group(maze3D, score3D, livesCounter3D, levelCounter3D, player3D, bonus3D);
		world3D.getChildren().addAll(ghosts3D);
		world3D.setTranslateX(-ArcadeWorld.SIZE.x / 2);
		world3D.setTranslateY(-ArcadeWorld.SIZE.y / 2);

		Group root = (Group) fxSubScene.getRoot();
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
	public void update() {
		maze3D.update(game);
		player3D.update();
		Stream.of(ghosts3D).forEach(Ghost3D::update);
		bonus3D.update(game.bonus());
		score3D.update(game.scores.gameScore().points, game.level.number, game.scores.highScore().points,
				game.scores.highScore().levelNumber);
		livesCounter3D.update(game.lives);
		getCamera().update(player3D);
		PlaySceneSounds.update(gameController.state());
	}

	@Override
	public void onKeyPressed() {
		if (Keyboard.pressed(Keyboard.ALT, KeyCode.LEFT)) {
			Actions.selectPrevPerspective();
		} else if (Keyboard.pressed(Keyboard.ALT, KeyCode.RIGHT)) {
			Actions.selectNextPerspective();
		}
	}

	public void onSwitchFrom2DScene() {
		if (game.pac.hasPower()) {
			Stream.of(ghosts3D) //
					.filter(ghost3D -> ghost3D.ghost.is(GhostState.FRIGHTENED) || ghost3D.ghost.is(GhostState.LOCKED))
					.filter(ghost3D -> !ghost3D.isLooksFrightened()) //
					.forEach(Ghost3D::setFrightenedLook);
		}
		maze3D.foodNodes()
				.forEach(foodNode -> foodNode.setVisible(!game.level.world.containsEatenFood(maze3D.tile(foodNode))));
		if (gameController.state() == GameState.HUNTING || gameController.state() == GameState.GHOST_DYING) {
			maze3D.energizerAnimations().forEach(Animation::play);
		}
	}

	private void onMazeResolutionChange(ObservableValue<? extends Number> property, Number oldValue, Number newValue) {
		if (!oldValue.equals(newValue)) {
			int mazeNumber = r2D.mazeNumber(game.level.number);
			maze3D.createWallsAndDoors(game.level.world, //
					getMazeSideColor(game.variant, mazeNumber), //
					getMazeTopColor(game.variant, mazeNumber), //
					getGhostHouseDoorColor(game.variant, mazeNumber));
		}
	}

	private void setCameraPerspective(Perspective perspective) {
		var camera = cameras.get(perspective);
		fxSubScene.setCamera(camera);
		fxSubScene.setOnKeyPressed(camera::onKeyPressed);
		fxSubScene.requestFocus();
		if (score3D != null) {
			// keep the score in plain sight
			score3D.rotationAxisProperty().bind(camera.rotationAxisProperty());
			score3D.rotateProperty().bind(camera.rotateProperty());
		}
		camera.reset();
	}

	private void setUseMazeFloorTexture(Boolean use) {
		if (use) {
			maze3D.getFloor().setTexture(floorTexture);
			maze3D.getFloor().setColor(floorColorWithTexture);
		} else {
			maze3D.getFloor().setTexture(null);
			maze3D.getFloor().setColor(floorColorNoTexture);
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
				.forEach(Ghost3D::setFrightenedLook);
	}

	@Override
	public void onPlayerStartsLosingPower(GameEvent e) {
		Stream.of(ghosts3D) //
				.filter(ghost3D -> ghost3D.ghost.is(GhostState.FRIGHTENED) || ghost3D.ghost.is(GhostState.LOCKED)) //
				.forEach(Ghost3D::playFlashingAnimation);
	}

	@Override
	public void onPlayerLosesPower(GameEvent e) {
		Stream.of(ghosts3D).forEach(Ghost3D::setNormalLook);
	}

	@Override
	public void onPlayerFindsFood(GameEvent e) {
		// when cheat "eat all pellets" is used, no tile is present
		if (!e.tile.isPresent()) {
			game.level.world.tiles().filter(game.level.world::containsEatenFood)
					.forEach(tile -> maze3D.foodAt(tile).ifPresent(maze3D::hideFood));
		} else {
			V2i tile = e.tile.get();
			maze3D.foodAt(tile).ifPresent(maze3D::hideFood);
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

	@SuppressWarnings("incomplete-switch")
	@Override
	public void onGameStateChange(GameStateChangeEvent e) {
		switch (e.newGameState) {
		case READY -> {
			maze3D.reset();
			player3D.reset();
			Stream.of(ghosts3D).forEach(Ghost3D::reset);
		}
		case HUNTING -> {
			maze3D.energizerAnimations().forEach(Animation::play);
		}
		case PACMAN_DYING -> {
			Stream.of(ghosts3D).forEach(Ghost3D::setNormalLook);
			var killer = game.ghosts().filter(ghost -> ghost.sameTile(game.pac)).findAny().get();
			var killerColor = r2D.getGhostColor(killer.id);
			new SequentialTransition( //
					U.pauseSec(1.0, () -> game.ghosts().forEach(Ghost::hide)), //
					player3D.dyingAnimation(killerColor, gameController.credit() == 0), //
					U.pauseSec(2.0, () -> gameController.state().timer().expire()) //
			).play();
		}
		case LEVEL_STARTING -> {
			// TODO: This is not executed at the *first* level. Maybe I should change the state machine to make a transition
			// from READY to LEVEL_STARTING when the game starts?
			int mazeNumber = r2D.mazeNumber(game.level.number);
			maze3D.createWallsAndDoors(game.level.world, //
					getMazeSideColor(game.variant, mazeNumber), //
					getMazeTopColor(game.variant, mazeNumber), //
					getGhostHouseDoorColor(game.variant, mazeNumber));
			maze3D.createFood(game.level.world, r2D.getFoodColor(mazeNumber));
			levelCounter3D.update(game);
			Actions.showFlashMessage(Env.message("level_starting", game.level.number));
			U.pauseSec(3, () -> gameController.state().timer().expire()).play();
		}
		case LEVEL_COMPLETE -> {
			Stream.of(ghosts3D).forEach(Ghost3D::setNormalLook);
			var message = Env.LEVEL_COMPLETE_TALK.next() + "\n\n" + Env.message("level_complete", game.level.number);
			new SequentialTransition( //
					U.pauseSec(2.0), //
					maze3D.createMazeFlashingAnimation(game.level.numFlashes), //
					U.pauseSec(1.0, () -> game.pac.hide()), //
					U.pauseSec(0.5, () -> Actions.showFlashMessage(2, message)), //
					U.pauseSec(2.0, () -> gameController.state().timer().expire()) //
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
}