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

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGameStateChangeEvent;
import de.amr.games.pacman.controller.event.ScatterPhaseStartedEvent;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.ui.PacManGameSound;
import de.amr.games.pacman.ui.PacManGameUI;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx._3d.entity.Bonus3D;
import de.amr.games.pacman.ui.fx._3d.entity.Ghost3D;
import de.amr.games.pacman.ui.fx._3d.entity.LevelCounter3D;
import de.amr.games.pacman.ui.fx._3d.entity.LivesCounter3D;
import de.amr.games.pacman.ui.fx._3d.entity.Maze3D;
import de.amr.games.pacman.ui.fx._3d.entity.PacManModel3D;
import de.amr.games.pacman.ui.fx._3d.entity.Player3D;
import de.amr.games.pacman.ui.fx._3d.entity.ScoreNotReally3D;
import de.amr.games.pacman.ui.fx.app.Env;
import de.amr.games.pacman.ui.fx.scene.AbstractGameScene;
import de.amr.games.pacman.ui.fx.scene.ScenesMsPacMan;
import de.amr.games.pacman.ui.fx.scene.ScenesPacMan;
import de.amr.games.pacman.ui.fx.sound.SoundManager;
import de.amr.games.pacman.ui.fx.util.AbstractCameraController;
import de.amr.games.pacman.ui.fx.util.CoordinateSystem;
import javafx.animation.Animation;
import javafx.animation.Animation.Status;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * 3D play scene with sound and animations.
 * 
 * @author Armin Reichert
 */
public class PlayScene3D extends AbstractGameScene {

	private final PacManModel3D model3D;
	private final EnumMap<Perspective, AbstractCameraController> camControllers = new EnumMap<>(Perspective.class);
	private final Image floorImage = new Image(getClass().getResource("/common/escher-texture.jpg").toString());

	private Maze3D maze3D;
	private Player3D player3D;
	private Ghost3D[] ghosts3D;
	private Bonus3D bonus3D;
	private ScoreNotReally3D score3D;
	private LevelCounter3D levelCounter3D;
	private LivesCounter3D livesCounter3D;
	private Animation[] energizerAnimations;
	private Rendering2D rendering2D;

	public PlayScene3D(PacManGameUI ui, PacManModel3D model3D, SoundManager sounds) {
		super(ui, sounds);
		this.model3D = model3D;
		fxSubScene = new SubScene(new Group(), 1, 1, true, SceneAntialiasing.BALANCED);
		var cam = new PerspectiveCamera(true);
		fxSubScene.setCamera(cam);
		fxSubScene.addEventHandler(KeyEvent.KEY_PRESSED, e -> currentCamController().handle(e));
		camControllers.put(Perspective.CAM_FOLLOWING_PLAYER, new Cam_FollowingPlayer(cam));
		camControllers.put(Perspective.CAM_NEAR_PLAYER, new Cam_NearPlayer(cam));
		camControllers.put(Perspective.CAM_TOTAL, new Cam_Total(cam));
		Env.$perspective.addListener(($1, $2, $3) -> currentCamController().reset());
	}

	@Override
	public void init(PacManGameController gameController) {
		super.init(gameController);

		final int width = game.world.numCols() * TS;
		final int height = game.world.numRows() * TS;

		rendering2D = gameController.gameVariant() == GameVariant.MS_PACMAN ? ScenesMsPacMan.RENDERING
				: ScenesPacMan.RENDERING;

		maze3D = new Maze3D(width, height, floorImage);
		maze3D.$wallHeight.bind(Env.$mazeWallHeight);
		maze3D.$resolution.bind(Env.$mazeResolution);
		maze3D.$resolution.addListener((x, y, z) -> buildMazeStructure(game.mazeNumber));
		buildMaze(game.mazeNumber);

		player3D = new Player3D(game.player, model3D.createPacMan());
		ghosts3D = game.ghosts()
				.map(ghost -> new Ghost3D(ghost, model3D.createGhost(), model3D.createGhostEyes(), rendering2D))
				.toArray(Ghost3D[]::new);
		bonus3D = new Bonus3D(rendering2D);
		score3D = new ScoreNotReally3D(rendering2D.getScoreFont(), fxSubScene.getCamera());

		livesCounter3D = new LivesCounter3D(model3D);
		livesCounter3D.setTranslateX(TS);
		livesCounter3D.setTranslateY(TS);
		livesCounter3D.setTranslateZ(-HTS);
		livesCounter3D.setVisible(!gameController.isAttractMode());

		levelCounter3D = new LevelCounter3D(rendering2D);
		levelCounter3D.setRightPosition(t(GameModel.TILES_X - 1), TS);
		levelCounter3D.setTranslateZ(-HTS);
		levelCounter3D.rebuild(game);

		var playground = new Group();
		playground.getChildren().addAll(maze3D, score3D, livesCounter3D, levelCounter3D, player3D, bonus3D);
		playground.getChildren().addAll(ghosts3D);
		playground.setTranslateX(-0.5 * width);
		playground.setTranslateY(-0.5 * height);

		var coordinateSystem = new CoordinateSystem(fxSubScene.getWidth());
		coordinateSystem.visibleProperty().bind(Env.$axesVisible);

		AmbientLight light = new AmbientLight();
		light.setColor(Color.GHOSTWHITE);

		fxSubScene.setRoot(new Group(light, playground, coordinateSystem));
		currentCamController().reset();
	}

	@Override
	public void update() {
		player3D.update();
		Stream.of(ghosts3D).forEach(Ghost3D::update);
		bonus3D.update(game.bonus);
		score3D.update(game, gameController.isAttractMode() ? "GAME OVER!" : null);
		livesCounter3D.setVisibleItems(game.player.lives);
		currentCamController().follow(player3D);
		playDoorAnimation();

		// update food visibility and animations in case of switching between 2D and 3D view
		// TODO: incomplete
		if (gameController.currentStateID == PacManGameState.HUNTING) {
			maze3D.foodNodes().forEach(foodNode -> {
				foodNode.setVisible(!game.isFoodEaten(info(foodNode).tile));
			});
			if (Stream.of(energizerAnimations).anyMatch(animation -> animation.getStatus() != Status.RUNNING)) {
				Stream.of(energizerAnimations).forEach(Animation::play);
			}
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
	public AbstractCameraController currentCamController() {
		if (!camControllers.containsKey(Env.$perspective.get())) {
			// This should not happen:
			Env.$perspective.set(camControllers.keySet().iterator().next());
		}
		return camControllers.get(Env.$perspective.get());
	}

	private void buildMaze(int mazeNumber) {
		buildMazeStructure(mazeNumber);
		maze3D.buildFood(game.world, rendering2D.getFoodColor(mazeNumber));
		energizerAnimations = energizerNodes().map(this::createEnergizerAnimation).toArray(Animation[]::new);
	}

	private void buildMazeStructure(int mazeNumber) {
		maze3D.buildWallsAndDoors(game.world, rendering2D.getMazeSideColor(mazeNumber),
				rendering2D.getMazeTopColor(mazeNumber));
	}

	private Transition createEnergizerAnimation(Node energizer) {
		var animation = new ScaleTransition(Duration.seconds(0.16), energizer);
		animation.setAutoReverse(true);
		animation.setCycleCount(Transition.INDEFINITE);
		animation.setFromX(1.0);
		animation.setFromY(1.0);
		animation.setFromZ(1.0);
		animation.setToX(0.1);
		animation.setToY(0.1);
		animation.setToZ(0.1);
		return animation;
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
			foodNodeAt(e.tile.get()).ifPresent(node -> node.setVisible(false));
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
		sounds.setMuted(gameController.isAttractMode());

		// enter READY
		if (e.newGameState == PacManGameState.READY) {
			sounds.stopAll();
			player3D.reset();
			resetEnergizers();
			sounds.setMuted(gameController.isAttractMode());
			if (!gameController.isGameRunning()) {
				sounds.play(PacManGameSound.GAME_READY);
			}
		}

		// enter HUNTING
		else if (e.newGameState == PacManGameState.HUNTING) {
			playEnergizerAnimations();
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
			buildMaze(game.mazeNumber);
			levelCounter3D.rebuild(game);
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

		// exit HUNTING but not GAME_OVER
		if (e.oldGameState == PacManGameState.HUNTING && e.newGameState != PacManGameState.GHOST_DYING) {
			stopEnergizerAnimations();
			bonus3D.hide();
		}
	}

	private Stream<Node> energizerNodes() {
		return maze3D.foodNodes().filter(node -> info(node).energizer);
	}

	private Optional<Node> foodNodeAt(V2i tile) {
		return maze3D.foodNodes().filter(node -> info(node).tile.equals(tile)).findFirst();
	}

	private void resetEnergizers() {
		energizerNodes().forEach(node -> {
			node.setScaleX(1.0);
			node.setScaleY(1.0);
			node.setScaleZ(1.0);
		});
	}

	private void playEnergizerAnimations() {
		Stream.of(energizerAnimations).forEach(Animation::play);
	}

	private void stopEnergizerAnimations() {
		Stream.of(energizerAnimations).forEach(Animation::stop);
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
				afterSeconds(1, game::hideGuys), //
				pause(1), //
				maze3D.flashingAnimation(game.numFlashes), //
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

	private void playDoorAnimation() {
		boolean open = maze3D.doors()
				.anyMatch(door -> game.ghosts().anyMatch(ghost -> ghost.tile().equals(info(door).tile)));
		maze3D.showDoorsOpen(open);
	}
}