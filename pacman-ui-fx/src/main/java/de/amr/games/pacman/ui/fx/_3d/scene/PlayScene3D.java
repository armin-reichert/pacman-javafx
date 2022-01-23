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

import static de.amr.games.pacman.model.world.PacManGameWorld.HTS;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;
import static de.amr.games.pacman.model.world.PacManGameWorld.t;
import static de.amr.games.pacman.ui.fx._3d.entity.Maze3D.NodeInfo.info;
import static de.amr.games.pacman.ui.fx.util.Animations.afterSeconds;
import static de.amr.games.pacman.ui.fx.util.Animations.pause;

import java.util.EnumMap;
import java.util.Optional;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGameStateChangeEvent;
import de.amr.games.pacman.controller.event.ScatterPhaseStartedEvent;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.ui.PacManGameSound;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx._3d.entity.Bonus3D;
import de.amr.games.pacman.ui.fx._3d.entity.Ghost3D;
import de.amr.games.pacman.ui.fx._3d.entity.LevelCounter3D;
import de.amr.games.pacman.ui.fx._3d.entity.LivesCounter3D;
import de.amr.games.pacman.ui.fx._3d.entity.Maze3D;
import de.amr.games.pacman.ui.fx._3d.entity.Player3D;
import de.amr.games.pacman.ui.fx._3d.entity.ScoreNotReally3D;
import de.amr.games.pacman.ui.fx._3d.model.PacManModel3D;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.scene.AbstractGameScene;
import de.amr.games.pacman.ui.fx.scene.ScenesMsPacMan;
import de.amr.games.pacman.ui.fx.scene.ScenesPacMan;
import de.amr.games.pacman.ui.fx.shell.PacManGameUI_JavaFX;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import de.amr.games.pacman.ui.fx.util.CoordinateSystem;
import javafx.animation.SequentialTransition;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

/**
 * 3D play scene with sound and animations.
 * 
 * @author Armin Reichert
 */
public class PlayScene3D extends AbstractGameScene {

	final PacManModel3D model3D;
	final EnumMap<Perspective, PlayScene3DCameraController> cams = new EnumMap<>(Perspective.class);
	final Image floorImage = new Image(getClass().getResource("/common/escher-texture.jpg").toString());
	final AmbientLight ambientLight = new AmbientLight(Color.GHOSTWHITE);
	final CoordinateSystem coordSystem = new CoordinateSystem(1000);

	Group playground;
	Maze3D maze3D;
	Player3D player3D;
	Ghost3D[] ghosts3D;
	Bonus3D bonus3D;
	ScoreNotReally3D score3D;
	LevelCounter3D levelCounter3D;
	LivesCounter3D livesCounter3D;
	Rendering2D r2D;

	public PlayScene3D(PacManGameUI_JavaFX ui, PacManModel3D model3D, SoundManager sounds) {
		super(ui, sounds);
		this.model3D = model3D;
		coordSystem.visibleProperty().bind(Env.$axesVisible);
		Env.$perspective.addListener(($1, $2, $3) -> camController().ifPresent(PlayScene3DCameraController::reset));
	}

	@Override
	public void createFXSubScene(Scene parentScene) {
		fxSubScene = new SubScene(new Group(), 400, 300, true, SceneAntialiasing.BALANCED);
		fxSubScene.widthProperty().bind(parentScene.widthProperty());
		fxSubScene.heightProperty().bind(parentScene.heightProperty());
		var cam = new PerspectiveCamera(true);
		fxSubScene.setCamera(cam);
		fxSubScene.addEventHandler(KeyEvent.KEY_PRESSED, e -> camController().ifPresent(cc -> cc.handle(e)));
		cams.clear();
		cams.put(Perspective.CAM_FOLLOWING_PLAYER, new Cam_FollowingPlayer(cam));
		cams.put(Perspective.CAM_NEAR_PLAYER, new Cam_NearPlayer(cam));
		cams.put(Perspective.CAM_TOTAL, new Cam_Total(cam));
	}

	@Override
	public void init(Scene parentScene) {
		super.init(parentScene);

		final int width = game.world.numCols() * TS;
		final int height = game.world.numRows() * TS;

		r2D = gameController.gameVariant == GameVariant.MS_PACMAN //
				? ScenesMsPacMan.RENDERING
				: ScenesPacMan.RENDERING;

		maze3D = new Maze3D(width, height, floorImage);
		maze3D.$wallHeight.bind(Env.$mazeWallHeight);
		maze3D.$resolution.bind(Env.$mazeResolution);
		maze3D.$resolution.addListener((x, y, z) -> buildMaze(game.mazeNumber, false));
		buildMaze(game.mazeNumber, true);

		player3D = new Player3D(game.player, model3D.createPacMan());

		ghosts3D = game.ghosts().map(ghost -> new Ghost3D(ghost, model3D.createGhost(), model3D.createGhostEyes(), r2D))
				.toArray(Ghost3D[]::new);

		bonus3D = new Bonus3D(r2D);

		score3D = new ScoreNotReally3D(r2D.getScoreFont());
		// TODO: maybe this is not the best solution to keep the score display in plain view
		score3D.setRotationAxis(Rotate.X_AXIS);
		score3D.rotateProperty().bind(fxSubScene.getCamera().rotateProperty());

		livesCounter3D = new LivesCounter3D(model3D);
		livesCounter3D.getTransforms().add(new Translate(TS, TS, -HTS));
		livesCounter3D.setVisible(!gameController.attractMode);

		levelCounter3D = new LevelCounter3D(r2D);
		levelCounter3D.setRightPosition(t(GameModel.TILES_X - 1), TS);
		levelCounter3D.init(game);

		playground = new Group();
		playground.getTransforms().add(new Translate(-0.5 * width, -0.5 * height)); // center at origin
		playground.getChildren().addAll(maze3D, score3D, livesCounter3D, levelCounter3D, player3D, bonus3D);
		playground.getChildren().addAll(ghosts3D);

		fxSubScene.setRoot(new Group(ambientLight, playground, coordSystem));
		camController().ifPresent(PlayScene3DCameraController::reset);
	}

	@Override
	public void update() {
		maze3D.updateGhostHouseDoorState(game);
		player3D.update();
		Stream.of(ghosts3D).forEach(Ghost3D::update);
		bonus3D.update(game.bonus);
		score3D.update(game, gameController.attractMode ? "GAME OVER!" : null);
		livesCounter3D.setVisibleItems(game.player.lives);
		camController().ifPresent(camController -> camController.update(this));

		sounds.setMuted(gameController.attractMode); // TODO check this

		// Update food visibility and start animations and audio in case of switching between 2D and 3D scene
		// TODO: still incomplete
		if (gameController.currentStateID == PacManGameState.HUNTING) {
			maze3D.foodNodes().forEach(foodNode -> {
				foodNode.setVisible(!game.isFoodEaten(info(foodNode).tile));
			});
			maze3D.startEnergizerAnimations();
			AudioClip munching = sounds.getClip(PacManGameSound.PACMAN_MUNCH);
			if (munching.isPlaying()) {
				if (game.player.starvingTicks > 10) {
					sounds.stop(PacManGameSound.PACMAN_MUNCH);
				}
			}
		}
	}

	@Override
	public boolean is3D() {
		return true;
	}

	@Override
	public Optional<PlayScene3DCameraController> camController() {
		if (!cams.containsKey(Env.$perspective.get())) {
			return Optional.empty();
		}
		return Optional.of(cams.get(Env.$perspective.get()));
	}

	private void buildMaze(int mazeNumber, boolean withFood) {
		maze3D.buildWallsAndDoors(game.world, r2D.getMazeSideColor(mazeNumber), r2D.getMazeTopColor(mazeNumber));
		if (withFood) {
			maze3D.buildFood(game.world, r2D.getFoodColor(mazeNumber));
		}
	}

	@Override
	public void onScatterPhaseStarted(ScatterPhaseStartedEvent e) {
		if (e.scatterPhase > 0) {
			sounds.stop(PacManGameSound.SIRENS.get(e.scatterPhase - 1));
		}
		PacManGameSound siren = PacManGameSound.SIRENS.get(e.scatterPhase);
		if (!sounds.getClip(siren).isPlaying())
			sounds.loop(siren, Integer.MAX_VALUE);
	}

	@Override
	public void onPlayerGainsPower(PacManGameEvent e) {
		sounds.loop(PacManGameSound.PACMAN_POWER, Integer.MAX_VALUE);
		Stream.of(ghosts3D) //
				.filter(ghost3D -> ghost3D.ghost.is(GhostState.FRIGHTENED) || ghost3D.ghost.is(GhostState.LOCKED))
				.forEach(Ghost3D::setBlueSkinColor);
	}

	@Override
	public void onPlayerLosingPower(PacManGameEvent e) {
		Stream.of(ghosts3D) //
				.filter(ghost3D -> ghost3D.ghost.is(GhostState.FRIGHTENED)) //
				.forEach(ghost3D -> ghost3D.playFlashingAnimation());
	}

	@Override
	public void onPlayerLostPower(PacManGameEvent e) {
		sounds.stop(PacManGameSound.PACMAN_POWER);
		Stream.of(ghosts3D).forEach(Ghost3D::setNormalSkinColor);
	}

	@Override
	public void onPlayerFoundFood(PacManGameEvent e) {
		if (e.tile.isEmpty()) { // happens when using the "eat all pellets except energizers" cheat
			maze3D.foodNodes().filter(node -> !info(node).energizer).forEach(node -> node.setVisible(false));
		} else {
			maze3D.foodNodeAt(e.tile.get()).ifPresent(node -> node.setVisible(false));
			AudioClip munching = sounds.getClip(PacManGameSound.PACMAN_MUNCH);
			if (!munching.isPlaying()) {
				sounds.loop(PacManGameSound.PACMAN_MUNCH, Integer.MAX_VALUE);
			}
		}
	}

	@Override
	public void onBonusActivated(PacManGameEvent e) {
		bonus3D.showSymbol(game.bonus);
	}

	@Override
	public void onBonusEaten(PacManGameEvent e) {
		bonus3D.showPoints(game.bonus);
		sounds.play(PacManGameSound.BONUS_EATEN);
	}

	@Override
	public void onBonusExpired(PacManGameEvent e) {
		bonus3D.hide();
	}

	@Override
	public void onExtraLife(PacManGameEvent e) {
		ui.showFlashMessage(1.5, Env.message("extra_life"));
		sounds.play(PacManGameSound.EXTRA_LIFE);
	}

	@Override
	public void onGhostReturnsHome(PacManGameEvent e) {
		sounds.play(PacManGameSound.GHOST_RETURNING);
	}

	@Override
	public void onGhostEntersHouse(PacManGameEvent e) {
		if (game.ghosts(GhostState.DEAD).count() == 0) {
			sounds.stop(PacManGameSound.GHOST_RETURNING);
		}
	}

	@Override
	public void onGhostLeavingHouse(PacManGameEvent e) {
		ghosts3D[e.ghost.get().id].setNormalSkinColor();
	}

	@Override
	public void onPacManGameStateChange(PacManGameStateChangeEvent e) {
		sounds.setMuted(gameController.attractMode); // TODO check this

		// enter READY
		if (e.newGameState == PacManGameState.READY) {
			sounds.stopAll();
			player3D.reset();
			maze3D.resetEnergizerSize();
			sounds.setMuted(gameController.attractMode);
			if (!gameController.gameRunning) {
				sounds.play(PacManGameSound.GAME_READY);
			}
		}

		// enter HUNTING
		else if (e.newGameState == PacManGameState.HUNTING) {
			maze3D.playEnergizerAnimations();
		}

		// enter PACMAN_DYING
		else if (e.newGameState == PacManGameState.PACMAN_DYING) {
			Stream.of(ghosts3D).forEach(ghost3D -> ghost3D.setNormalSkinColor());
			sounds.stopAll();
			gameController.stateTimer().setIndefinite().start();
			playAnimationPlayerDying();
		}

		// enter GHOST_DYING
		else if (e.newGameState == PacManGameState.GHOST_DYING) {
			sounds.play(PacManGameSound.GHOST_EATEN);
		}

		// enter LEVEL_STARTING
		else if (e.newGameState == PacManGameState.LEVEL_STARTING) {
			buildMaze(game.mazeNumber, true);
			levelCounter3D.init(game);
			playAnimationLevelStarting();
		}

		// enter LEVEL_COMPLETE
		else if (e.newGameState == PacManGameState.LEVEL_COMPLETE) {
			sounds.stopAll();
			Stream.of(ghosts3D).forEach(ghost3D -> ghost3D.setNormalSkinColor());
			playAnimationLevelComplete();
		}

		// enter GAME_OVER
		else if (e.newGameState == PacManGameState.GAME_OVER) {
			sounds.stopAll();
			ui.showFlashMessage(3, Env.GAME_OVER_TALK.next());
		}

		// exit HUNTING
		if (e.oldGameState == PacManGameState.HUNTING && e.newGameState != PacManGameState.GHOST_DYING) {
			maze3D.stopEnergizerAnimations();
			bonus3D.hide();
		}
	}

	private void playAnimationPlayerDying() {
		var animation = new SequentialTransition( //
				afterSeconds(1, game::hideGhosts), //
				player3D.dyingAnimation(sounds), //
				pause(2) //
		);
		animation.setOnFinished(e -> continueGame());
		animation.play();
	}

	private void playAnimationLevelComplete() {
		var message = Env.LEVEL_COMPLETE_TALK.next() + "\n\n" + Env.message("level_complete", game.levelNumber);
		var animation = new SequentialTransition( //
				pause(1), //
				maze3D.flashingAnimation(game.numFlashes), //
				afterSeconds(1, () -> game.player.hide()), //
				afterSeconds(1, () -> ui.showFlashMessage(2, message)) //
		);
		animation.setOnFinished(e -> continueGame());
		animation.play();
	}

	private void playAnimationLevelStarting() {
		var message = Env.message("level_starting", game.levelNumber);
		ui.showFlashMessage(1, message);
		afterSeconds(3, this::continueGame).play();
	}
}