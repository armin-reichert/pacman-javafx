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
import static de.amr.games.pacman.ui.fx.util.Animations.afterSeconds;
import static java.util.function.Predicate.not;

import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.controller.event.PacManGameEvent;
import de.amr.games.pacman.controller.event.PacManGameStateChangeEvent;
import de.amr.games.pacman.controller.event.ScatterPhaseStartedEvent;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.GhostState;
import de.amr.games.pacman.model.world.PacManGameWorld;
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
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

/**
 * 3D play scene with sound and animations.
 * 
 * @author Armin Reichert
 */
public class PlayScene3D extends AbstractGameScene {

	private static V2i tile(Node node) {
		return (V2i) node.getUserData();
	}

	private static Transition createEnergizerAnimation(Node energizer) {
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

	private final PacManModel3D model3D;
	private final SoundManager sounds;
	private final SubScene fxScene;
	private final EnumMap<Perspective, AbstractCameraController> cameraControllers = new EnumMap<>(Perspective.class);
	private final Image floorImage = new Image(getClass().getResourceAsStream("/common/escher-texture.jpg"));
	private Maze3D maze3D;
	private Player3D player3D;
	private List<Ghost3D> ghosts3D;
	private Bonus3D bonus3D;
	private ScoreNotReally3D score3D;
	private LevelCounter3D levelCounter3D;
	private LivesCounter3D livesCounter3D;
	private List<Transition> energizerAnimations;

	public PlayScene3D(PacManGameUI ui, PacManModel3D model3D, SoundManager sounds) {
		super(ui);
		this.model3D = model3D;
		this.sounds = sounds;
		fxScene = new SubScene(new Group(), 1, 1, true, SceneAntialiasing.BALANCED);
		var cam = new PerspectiveCamera(true);
		fxScene.setCamera(cam);
		fxScene.addEventHandler(KeyEvent.KEY_PRESSED, e -> currentCameraController().handle(e));
		cameraControllers.put(Perspective.CAM_FOLLOWING_PLAYER, new Cam_FollowingPlayer(cam));
		cameraControllers.put(Perspective.CAM_NEAR_PLAYER, new Cam_NearPlayer(cam));
		cameraControllers.put(Perspective.CAM_TOTAL, new Cam_Total(cam));
		Env.$perspective.addListener(($1, $2, $3) -> currentCameraController().reset());
	}

	@Override
	public boolean is3D() {
		return true;
	}

	@Override
	public AbstractCameraController currentCameraController() {
		if (!cameraControllers.containsKey(Env.$perspective.get())) {
			// This should not happen:
			Env.$perspective.set(cameraControllers.keySet().iterator().next());
		}
		return cameraControllers.get(Env.$perspective.get());
	}

	@Override
	public SubScene getSubSceneFX() {
		return fxScene;
	}

	private Rendering2D rendering2D() {
		return gameController.gameVariant() == GameVariant.MS_PACMAN ? ScenesMsPacMan.RENDERING : ScenesPacMan.RENDERING;
	}

	private void buildMaze(PacManGameWorld world, int mazeNumber) {
		buildMazeStructure(world, mazeNumber);
		maze3D.buildFood(world, rendering2D().getFoodColor(mazeNumber));
		energizerAnimations = energizerNodes(world).map(PlayScene3D::createEnergizerAnimation).collect(Collectors.toList());
	}

	private void buildMazeStructure(PacManGameWorld world, int mazeNumber) {
		maze3D.buildWallsAndDoors(world, rendering2D().getMazeSideColor(mazeNumber),
				rendering2D().getMazeTopColor(mazeNumber));
	}

	@Override
	public void init(PacManGameController gameController) {
		super.init(gameController);

		final int width = game.world.numCols() * TS;
		final int height = game.world.numRows() * TS;

		maze3D = new Maze3D(width, height, floorImage);
		maze3D.$wallHeight.bind(Env.$mazeWallHeight);
		maze3D.$resolution.bind(Env.$mazeResolution);
		maze3D.$resolution.addListener((x, y, z) -> buildMazeStructure(game.world, game.mazeNumber));
		buildMaze(game.world, game.mazeNumber);

		player3D = new Player3D(game.player, model3D.createPacMan());
		ghosts3D = game.ghosts()
				.map(ghost -> new Ghost3D(ghost, model3D.createGhost(), model3D.createGhostEyes(), rendering2D()))
				.collect(Collectors.toList());
		bonus3D = new Bonus3D(rendering2D());
		score3D = new ScoreNotReally3D(rendering2D().getScoreFont());
		score3D.setRotationAxis(Rotate.X_AXIS);
		score3D.rotateProperty().bind(fxScene.getCamera().rotateProperty());

		livesCounter3D = new LivesCounter3D(model3D);
		livesCounter3D.setTranslateX(TS);
		livesCounter3D.setTranslateY(TS);
		livesCounter3D.setTranslateZ(-HTS);
		livesCounter3D.setVisible(!gameController.isAttractMode());

		levelCounter3D = new LevelCounter3D(rendering2D());
		levelCounter3D.setRightPosition(26 * TS, TS);
		levelCounter3D.setTranslateZ(-HTS);
		levelCounter3D.rebuild(game);

		var playground = new Group();
		playground.getChildren().addAll(maze3D, score3D, livesCounter3D, levelCounter3D, player3D, bonus3D);
		playground.getChildren().addAll(ghosts3D);
		playground.setTranslateX(-0.5 * width);
		playground.setTranslateY(-0.5 * height);

		var coordinateSystem = new CoordinateSystem(fxScene.getWidth());
		coordinateSystem.visibleProperty().bind(Env.$axesVisible);

		AmbientLight light = new AmbientLight();
		light.setColor(Color.GHOSTWHITE);

		fxScene.setRoot(new Group(light, playground, coordinateSystem));
		currentCameraController().reset();
	}

	@Override
	public void update() {
		player3D.update();
		ghosts3D.forEach(Ghost3D::update);
		bonus3D.update(game.bonus);
		score3D.update(game, gameController.isAttractMode() ? "GAME OVER!" : null);
		livesCounter3D.setVisibleItems(game.player.lives);
		currentCameraController().follow(player3D);
		playDoorAnimation();

		// update food visibility and animations in case of switching between 2D and 3D view
		// TODO: incomplete
		if (gameController.currentStateID == PacManGameState.HUNTING) {
			maze3D.foodNodes().forEach(foodNode -> {
				foodNode.setVisible(!game.isFoodEaten(tile(foodNode)));
			});
			if (energizerAnimations.stream().anyMatch(animation -> animation.getStatus() != Status.RUNNING)) {
				energizerAnimations.forEach(Transition::play);
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
	public void onGameEvent(PacManGameEvent gameEvent) {
		sounds.setMuted(gameController.isAttractMode());
		super.onGameEvent(gameEvent);
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
		ghosts3D.stream() //
				.filter(ghost3D -> ghost3D.ghost.is(GhostState.FRIGHTENED) || ghost3D.ghost.is(GhostState.LOCKED))
				.forEach(Ghost3D::setBlueSkinColor);
	}

	@Override
	public void onPlayerLosingPower(PacManGameEvent e) {
		ghosts3D.stream() //
				.filter(ghost3D -> ghost3D.ghost.is(GhostState.FRIGHTENED)) //
				.forEach(ghost3D -> ghost3D.playFlashingAnimation());
	}

	@Override
	public void onPlayerLostPower(PacManGameEvent e) {
		sounds.stop(PacManGameSound.PACMAN_POWER);
		ghosts3D.forEach(Ghost3D::setNormalSkinColor);
	}

	@Override
	public void onPlayerFoundFood(PacManGameEvent e) {
		if (e.tile.isEmpty()) {
			// this happens when the "eat all pellets except energizers" cheat was triggered
			Predicate<Node> isEnergizer = node -> game.world.isEnergizerTile(tile(node));
			maze3D.foodNodes().filter(not(isEnergizer)).forEach(foodNode -> foodNode.setVisible(false));
		} else {
			foodNodeAt(e.tile.get()).ifPresent(foodNode -> foodNode.setVisible(false));
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
		ghosts3D.get(e.ghost.get().id).setNormalSkinColor();
	}

	@Override
	public void onPacManGameStateChange(PacManGameStateChangeEvent e) {
		sounds.setMuted(gameController.isAttractMode());

		// enter READY
		if (e.newGameState == PacManGameState.READY) {
			sounds.stopAll();
			player3D.reset();
			resetEnergizers(game.world);
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
			ghosts3D.forEach(ghost3D -> ghost3D.setNormalSkinColor());
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
			buildMaze(game.world, game.mazeNumber);
			levelCounter3D.rebuild(game);
			playAnimationLevelStarting();
		}

		// enter LEVEL_COMPLETE
		else if (e.newGameState == PacManGameState.LEVEL_COMPLETE) {
			sounds.stopAll();
			playAnimationLevelComplete();
			ghosts3D.forEach(ghost3D -> ghost3D.setNormalSkinColor());
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

	private Stream<Node> energizerNodes(PacManGameWorld world) {
		return maze3D.foodNodes().filter(node -> world.isEnergizerTile(tile(node)));
	}

	private Optional<Node> foodNodeAt(V2i tile) {
		return maze3D.foodNodes().filter(node -> tile(node).equals(tile)).findFirst();
	}

	private void resetEnergizers(PacManGameWorld world) {
		energizerNodes(world).forEach(node -> {
			node.setScaleX(1.0);
			node.setScaleY(1.0);
			node.setScaleZ(1.0);
		});
	}

	private void playEnergizerAnimations() {
		energizerAnimations.forEach(Animation::play);
	}

	private void stopEnergizerAnimations() {
		energizerAnimations.forEach(Animation::stop);
	}

	private void playAnimationPlayerDying() {
		new SequentialTransition( //
				afterSeconds(1, game::hideGhosts), //
				player3D.dyingAnimation(sounds), //
				afterSeconds(2, this::continueGame) //
		).play();
	}

	private void playAnimationLevelComplete() {
		gameController.stateTimer().setIndefinite().start();
		var hideGuysAndShowMessage = afterSeconds(3, () -> {
			game.player.hide();
			game.hideGhosts();
			var message = Env.LEVEL_COMPLETE_TALK.next() + "\n\n" + Env.message("level_complete", game.levelNumber);
			ui.showFlashMessage(2, message);
		});
		var quitLevel = afterSeconds(2, () -> continueGame());
		new SequentialTransition(hideGuysAndShowMessage, quitLevel).play();
	}

	private void playAnimationLevelStarting() {
		ui.showFlashMessage(1, Env.message("level_starting", game.levelNumber));
		var showGuys = afterSeconds(1, () -> {
			game.player.show();
			game.showGhosts();
		});
		var startLevel = afterSeconds(3, () -> continueGame());
		new SequentialTransition(showGuys, startLevel).play();
	}

	private void playDoorAnimation() {
		boolean open = maze3D.doors().anyMatch(door -> game.ghosts().anyMatch(ghost -> ghost.tile().equals(tile(door))));
		maze3D.showDoorsOpen(open);
	}
}