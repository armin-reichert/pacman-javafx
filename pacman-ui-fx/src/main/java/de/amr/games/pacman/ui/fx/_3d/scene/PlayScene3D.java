/*
MIT License

Copyright (c) 2021 Armin Reichert

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
import static de.amr.games.pacman.ui.fx.shell.FlashMessageView.showFlashMessage;

import java.util.stream.Stream;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.controller.event.DefaultGameEventHandler;
import de.amr.games.pacman.controller.event.GameEvent;
import de.amr.games.pacman.controller.event.GameStateChangeEvent;
import de.amr.games.pacman.controller.event.ScatterPhaseStartedEvent;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.ui.GameSound;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx._3d.entity.Bonus3D;
import de.amr.games.pacman.ui.fx._3d.entity.Ghost3D;
import de.amr.games.pacman.ui.fx._3d.entity.LevelCounter3D;
import de.amr.games.pacman.ui.fx._3d.entity.LivesCounter3D;
import de.amr.games.pacman.ui.fx._3d.entity.Maze3D;
import de.amr.games.pacman.ui.fx._3d.entity.Pac3D;
import de.amr.games.pacman.ui.fx._3d.entity.Score3D;
import de.amr.games.pacman.ui.fx._3d.model.PacManModel3D;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.scene.GameScene;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import de.amr.games.pacman.ui.fx.util.CoordinateSystem;
import de.amr.games.pacman.ui.fx.util.U;
import javafx.animation.Animation;
import javafx.animation.SequentialTransition;
import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.scene.AmbientLight;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.image.Image;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.transform.Translate;

/**
 * 3D play scene with sound and animations.
 * 
 * @author Armin Reichert
 */
public class PlayScene3D extends DefaultGameEventHandler implements GameScene {

	private final GameController gc;
	private final SubScene fxSubScene;
	private final PacManModel3D model3D;
	private final Image floorTexture = U.image("/common/escher-texture.jpg");
	private final Color floorColorWithTexture = Color.DARKBLUE;
	private final Color floorColorNoTexture = Color.rgb(30, 30, 30);
	private final CoordinateSystem coordSystem;

	private GameModel game;
	private Rendering2D r2D;
	private CameraController camController;
	private Pac3D player3D;
	private Maze3D maze3D;
	private Ghost3D[] ghosts3D;
	private Bonus3D bonus3D;
	private Score3D score3D;
	private LevelCounter3D levelCounter3D;
	private LivesCounter3D livesCounter3D;

	public PlayScene3D(GameController gc, PacManModel3D model3D) {
		this.gc = gc;
		this.model3D = model3D;
		fxSubScene = new SubScene(new Group(), 1, 1, true, SceneAntialiasing.BALANCED);
		fxSubScene.setCamera(new PerspectiveCamera(true));
		coordSystem = new CoordinateSystem(1000);
		coordSystem.visibleProperty().bind(Env.$axesVisible);
	}

	@Override
	public void setContext(GameModel game, Rendering2D r2d) {
		this.game = game;
		this.r2D = r2d;
	}

	@Override
	public SubScene getFXSubScene() {
		return fxSubScene;
	}

	@Override
	public void resizeFXSubScene(double height) {
	}

	public CameraController getCamController() {
		return camController;
	}

	@Override
	public void init() {
		final int width = game.world.numCols() * TS;
		final int height = game.world.numRows() * TS;

		maze3D = new Maze3D(width, height);
		maze3D.createWallsAndDoors(game.world, r2D.getMazeSideColor(game.mazeNumber), r2D.getMazeTopColor(game.mazeNumber));
		maze3D.createFood(game.world, r2D.getFoodColor(game.mazeNumber));

		player3D = new Pac3D(game.player, model3D);
		ghosts3D = game.ghosts().map(ghost -> new Ghost3D(ghost, model3D, r2D)).toArray(Ghost3D[]::new);
		bonus3D = new Bonus3D(r2D);

		score3D = new Score3D();
		score3D.setFont(r2D.getArcadeFont());
		score3D.setComputeScoreText(!gc.attractMode);
		if (gc.attractMode) {
			score3D.txtScore.setFill(Color.RED);
			score3D.txtScore.setText("GAME OVER!");
		}

		livesCounter3D = new LivesCounter3D(model3D);
		livesCounter3D.getTransforms().add(new Translate(TS, TS, -HTS));
		livesCounter3D.setVisible(!gc.attractMode);

		levelCounter3D = new LevelCounter3D(r2D, width - TS, TS);
		levelCounter3D.update(game);

		var world3D = new Group(maze3D, score3D, livesCounter3D, levelCounter3D, player3D, bonus3D);
		world3D.getChildren().addAll(ghosts3D);
		world3D.getTransforms().add(new Translate(-width / 2, -height / 2)); // center at origin

		fxSubScene.setRoot(new Group(new AmbientLight(Color.GHOSTWHITE), world3D, coordSystem));

		onPerspectiveChange(null, null, Env.$perspective.get());
		onUseMazeFloorTextureChange(null, null, Env.$useMazeFloorTexture.getValue());

		SoundManager.get().setMuted(gc.attractMode);

		maze3D.$wallHeight.bind(Env.$mazeWallHeight);
		maze3D.$resolution.bind(Env.$mazeResolution);
		maze3D.$resolution.addListener(this::onMazeResolutionChange);

		Env.$perspective.addListener(this::onPerspectiveChange);
		Env.$useMazeFloorTexture.addListener(this::onUseMazeFloorTextureChange);
	}

	@Override
	public void end() {
		SoundManager.get().setMuted(false);

		maze3D.$wallHeight.unbind();
		maze3D.$resolution.unbind();
		maze3D.$resolution.removeListener(this::onMazeResolutionChange);

		Env.$perspective.removeListener(this::onPerspectiveChange);
		Env.$useMazeFloorTexture.removeListener(this::onUseMazeFloorTextureChange);
	}

	@Override
	public void update() {
		maze3D.update(game);
		player3D.update();
		Stream.of(ghosts3D).forEach(Ghost3D::update);
		bonus3D.update(game.bonus);
		score3D.update(game.score, game.levelNumber, game.highscorePoints, game.highscoreLevel);
		livesCounter3D.update(game.player.lives);
		camController.update();

		// keep in sync with 2D scene in case user toggles between 2D and 3D
		maze3D.pellets().forEach(pellet -> pellet.setVisible(!game.world.isFoodEaten(pellet.tile)));
		if (gc.state == GameState.HUNTING || gc.state == GameState.GHOST_DYING) {
			maze3D.energizerAnimations().forEach(Animation::play);
		}
		if (SoundManager.get().getClip(GameSound.PACMAN_MUNCH).isPlaying() && game.player.starvingTicks > 10) {
			SoundManager.get().stop(GameSound.PACMAN_MUNCH);
		}
		int scatterPhase = game.huntingPhase % 2;
		GameSound siren = GameSound.SIRENS.get(scatterPhase);
		if (gc.state == GameState.HUNTING && !SoundManager.get().getClip(siren).isPlaying()) {
			SoundManager.get().loop(siren, Animation.INDEFINITE);
		}
	}

	private void onMazeResolutionChange(ObservableValue<? extends Number> property, Number oldValue, Number newValue) {
		if (!oldValue.equals(newValue)) {
			maze3D.createWallsAndDoors(game.world, r2D.getMazeSideColor(game.mazeNumber),
					r2D.getMazeTopColor(game.mazeNumber));
		}
	}

	private void onPerspectiveChange(Observable $perspective, Perspective oldPerspective, Perspective newPerspective) {
		setPerspective(newPerspective);
	}

	public void setPerspective(Perspective perspective) {
		Camera cam = fxSubScene.getCamera();
		camController = switch (perspective) {
		case CAM_DRONE -> new Cam_Drone(cam, player3D);
		case CAM_FOLLOWING_PLAYER -> new Cam_FollowingPlayer(cam, player3D);
		case CAM_NEAR_PLAYER -> new Cam_NearPlayer(cam, player3D);
		case CAM_TOTAL -> new Cam_Total(cam);
		};
		camController.reset();
		fxSubScene.setOnKeyPressed(camController);
		fxSubScene.requestFocus();
		if (score3D != null) {
			// keep the score in plain sight
			score3D.rotationAxisProperty().bind(camController.cam().rotationAxisProperty());
			score3D.rotateProperty().bind(camController.cam().rotateProperty());
		}
	}

	private void onUseMazeFloorTextureChange(Observable $useMazeFloorTexture, Boolean oldValue, Boolean newValue) {
		if (newValue) {
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
	public void onScatterPhaseStarted(ScatterPhaseStartedEvent e) {
		if (e.scatterPhase > 0) {
			SoundManager.get().stop(GameSound.SIRENS.get(e.scatterPhase - 1));
		}
		GameSound siren = GameSound.SIRENS.get(e.scatterPhase);
		if (!SoundManager.get().getClip(siren).isPlaying())
			SoundManager.get().loop(siren, Animation.INDEFINITE);
	}

	@Override
	public void onPlayerGainsPower(GameEvent e) {
		SoundManager.get().loop(GameSound.PACMAN_POWER, Animation.INDEFINITE);
		Stream.of(ghosts3D) //
				.filter(ghost3D -> ghost3D.creature.is(GhostState.FRIGHTENED) || ghost3D.creature.is(GhostState.LOCKED))
				.forEach(Ghost3D::setFrightenedSkinColor);
	}

	@Override
	public void onPlayerLosingPower(GameEvent e) {
		Stream.of(ghosts3D) //
				.filter(ghost3D -> ghost3D.creature.is(GhostState.FRIGHTENED)) //
				.forEach(Ghost3D::playFlashingAnimation);
	}

	@Override
	public void onPlayerLostPower(GameEvent e) {
		SoundManager.get().stop(GameSound.PACMAN_POWER);
		Stream.of(ghosts3D).forEach(Ghost3D::setNormalSkinColor);
	}

	@Override
	public void onPlayerFoundFood(GameEvent e) {
		// when cheat "eat all pellets" is used, no tile is present
		e.tile.ifPresent(tile -> {
			maze3D.pelletAt(tile).ifPresent(maze3D::hidePellet);
			AudioClip munching = SoundManager.get().getClip(GameSound.PACMAN_MUNCH);
			if (!munching.isPlaying()) {
				SoundManager.get().loop(GameSound.PACMAN_MUNCH, Animation.INDEFINITE);
			}
		});
	}

	@Override
	public void onBonusActivated(GameEvent e) {
		bonus3D.showSymbol(game.bonus.symbol);
	}

	@Override
	public void onBonusEaten(GameEvent e) {
		bonus3D.showPoints(game.bonus.points);
		SoundManager.get().play(GameSound.BONUS_EATEN);
	}

	@Override
	public void onBonusExpired(GameEvent e) {
		bonus3D.setVisible(false);
	}

	@Override
	public void onExtraLife(GameEvent e) {
		showFlashMessage(1.5, Env.message("extra_life"));
		SoundManager.get().play(GameSound.EXTRA_LIFE);
	}

	@Override
	public void onGhostReturnsHome(GameEvent e) {
		SoundManager.get().play(GameSound.GHOST_RETURNING);
	}

	@Override
	public void onGhostEntersHouse(GameEvent e) {
		if (game.ghosts(GhostState.DEAD).count() == 0) {
			SoundManager.get().stop(GameSound.GHOST_RETURNING);
		}
	}

	@Override
	public void onGhostRevived(GameEvent e) {
		Ghost ghost = e.ghost.get();
		ghosts3D[ghost.id].playRevivalAnimation();
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public void onGameStateChange(GameStateChangeEvent e) {
		switch (e.newGameState) {
		case READY -> {
			maze3D.reset();
			player3D.reset();
			Stream.of(ghosts3D).forEach(Ghost3D::reset);
			SoundManager.get().stopAll();
			SoundManager.get().setMuted(gc.attractMode);
			if (!gc.gameRunning) {
				SoundManager.get().play(GameSound.GAME_READY);
			}
		}
		case HUNTING -> {
			maze3D.energizerAnimations().forEach(Animation::play);
		}
		case PACMAN_DYING -> {
			SoundManager.get().stopAll();
			Stream.of(ghosts3D).forEach(Ghost3D::setNormalSkinColor);
			Color killerColor = r2D.getGhostColor(
					Stream.of(game.ghosts).filter(ghost -> ghost.tile().equals(game.player.tile())).findAny().get().id);
			new SequentialTransition( //
					U.afterSec(1.0, game::hideGhosts), //
					player3D.dyingAnimation(killerColor), //
					U.afterSec(2.0, () -> gc.stateTimer().expire()) //
			).play();
		}
		case GHOST_DYING -> {
			SoundManager.get().play(GameSound.GHOST_EATEN);
		}
		case LEVEL_STARTING -> {
			// TODO: This is not executed at the *first* level. Maybe I should change the state machine to make a transition
			// from READY to LEVEL_STARTING when the game starts?
			maze3D.createWallsAndDoors(game.world, r2D.getMazeSideColor(game.mazeNumber),
					r2D.getMazeTopColor(game.mazeNumber));
			maze3D.createFood(game.world, r2D.getFoodColor(game.mazeNumber));
			maze3D.energizerAnimations().forEach(Animation::stop);
			levelCounter3D.update(game);
			var message = Env.message("level_starting", game.levelNumber);
			showFlashMessage(1, message);
			U.afterSec(3, () -> gc.stateTimer().expire()).play();
		}
		case LEVEL_COMPLETE -> {
			Stream.of(ghosts3D).forEach(Ghost3D::setNormalSkinColor);
			var message = Env.LEVEL_COMPLETE_TALK.next() + "\n\n" + Env.message("level_complete", game.levelNumber);
			new SequentialTransition( //
					U.pause(2.0), //
					maze3D.createMazeFlashingAnimation(game.numFlashes), //
					U.afterSec(1.0, () -> game.player.hide()), //
					U.afterSec(0.5, () -> showFlashMessage(2, message)), //
					U.afterSec(2.0, () -> gc.stateTimer().expire()) //
			).play();
		}
		case GAME_OVER -> {
			showFlashMessage(3, Env.GAME_OVER_TALK.next());
		}
		}

		// exit HUNTING
		if (e.oldGameState == GameState.HUNTING && e.newGameState != GameState.GHOST_DYING) {
			maze3D.energizerAnimations().forEach(Animation::stop);
			bonus3D.setVisible(false);
			SoundManager.get().stopAll();
		}
	}
}