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

import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.world.PacManGameWorld;
import de.amr.games.pacman.ui.fx.Env;
import de.amr.games.pacman.ui.fx._2d.rendering.common.Rendering2D;
import de.amr.games.pacman.ui.fx._3d.entity.Bonus3D;
import de.amr.games.pacman.ui.fx._3d.entity.Ghost3D;
import de.amr.games.pacman.ui.fx._3d.entity.LevelCounter3D;
import de.amr.games.pacman.ui.fx._3d.entity.LivesCounter3D;
import de.amr.games.pacman.ui.fx._3d.entity.Maze3D;
import de.amr.games.pacman.ui.fx._3d.entity.PacManModel3D;
import de.amr.games.pacman.ui.fx._3d.entity.Player3D;
import de.amr.games.pacman.ui.fx._3d.entity.ScoreNotReally3D;
import de.amr.games.pacman.ui.fx.scene.AbstractGameScene;
import de.amr.games.pacman.ui.fx.scene.ScenesMsPacMan;
import de.amr.games.pacman.ui.fx.scene.ScenesPacMan;
import de.amr.games.pacman.ui.fx.util.AbstractCameraController;
import de.amr.games.pacman.ui.fx.util.CoordinateSystem;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.transform.Rotate;

/**
 * 3D-scene displaying the maze and the game play. Each game variant has an instance of this class.
 * 
 * <p>
 * The scene is a JavaFX subscene of the game's main scene.
 * 
 * @see ScenesPacMan
 * 
 * @author Armin Reichert
 */
public class PlayScene3DNaked extends AbstractGameScene {

	protected final PacManModel3D model3D;
	protected final SubScene fxScene;
	protected final EnumMap<Perspective, AbstractCameraController> cameraControllers = new EnumMap<>(Perspective.class);
	protected final Image floorImage = new Image(getClass().getResourceAsStream("/common/escher-texture.jpg"));

	protected Maze3D maze3D;
	protected Player3D player3D;
	protected List<Ghost3D> ghosts3D;
	protected Bonus3D bonus3D;
	protected ScoreNotReally3D score3D;
	protected LevelCounter3D levelCounter3D;
	protected LivesCounter3D livesCounter3D;

	public PlayScene3DNaked(PacManModel3D model3D) {
		this.model3D = model3D;
		var cam = new PerspectiveCamera(true);
		fxScene = new SubScene(new Group(), 1, 1, true, SceneAntialiasing.BALANCED);
		fxScene.setCamera(cam);
		fxScene.addEventHandler(KeyEvent.KEY_PRESSED, e -> currentCameraController().handle(e));
		cameraControllers.put(Perspective.CAM_FOLLOWING_PLAYER, new Cam_FollowingPlayer(cam));
		cameraControllers.put(Perspective.CAM_NEAR_PLAYER, new Cam_NearPlayer(cam));
		cameraControllers.put(Perspective.CAM_TOTAL, new Cam_Total(cam));
		Env.$perspective.addListener(($1, $2, $3) -> currentCameraController().reset());
	}

	@Override
	public void init(PacManGameController gameController) {
		super.init(gameController);

		final int width = game.world.numCols() * TS;
		final int height = game.world.numRows() * TS;

		maze3D = new Maze3D(width, height, floorImage);
		maze3D.$wallHeight.bind(Env.$mazeWallHeight);
		maze3D.$resolution.bind(Env.$mazeResolution);
		maze3D.$resolution.addListener((x, y, z) -> buildMazeWithoutFood(game.world, game.mazeNumber));
		buildMaze(game.world, game.mazeNumber);

		player3D = new Player3D(game.player, model3D.createPacMan());
		ghosts3D = game.ghosts()
				.map(ghost -> new Ghost3D(ghost, model3D.createGhost(), model3D.createGhostEyes(), rendering2D()))
				.collect(Collectors.toList());
		bonus3D = new Bonus3D(rendering2D());
		score3D = new ScoreNotReally3D(rendering2D().getScoreFont());

		livesCounter3D = new LivesCounter3D(model3D);
		livesCounter3D.setTranslateX(TS);
		livesCounter3D.setTranslateY(TS);
		livesCounter3D.setTranslateZ(-4); // TODO
		livesCounter3D.setVisible(!gameController.isAttractMode());

		levelCounter3D = new LevelCounter3D(rendering2D());
		levelCounter3D.setRightPosition(26 * TS, TS);
		levelCounter3D.setTranslateZ(-4); // TODO
		levelCounter3D.rebuild(game);

		var playground = new Group(maze3D, score3D, livesCounter3D, levelCounter3D, player3D, bonus3D);
		playground.getChildren().addAll(ghosts3D);
		playground.setTranslateX(-0.5 * width);
		playground.setTranslateY(-0.5 * height);

		var coordinateSystem = new CoordinateSystem(fxScene.getWidth());
		coordinateSystem.visibleProperty().bind(Env.$axesVisible);

		fxScene.setRoot(new Group(new AmbientLight(), playground, coordinateSystem));
		currentCameraController().reset();
	}

	@Override
	public void update() {
		player3D.update();
		ghosts3D.forEach(Ghost3D::update);
		bonus3D.update(game.bonus);
		score3D.update(game, gameController.isAttractMode() ? "GAME OVER!" : null);
		// TODO: is this the recommended way to do keep the score in plain view?
		score3D.setRotationAxis(Rotate.X_AXIS);
		score3D.setRotate(fxScene.getCamera().getRotate());
		livesCounter3D.setVisibleItems(game.player.lives);
		currentCameraController().follow(player3D);
	}

	protected static V2i tile(Node node) {
		return (V2i) node.getUserData();
	}

	/**
	 * Builds the maze content including the food. Overwritten by subclass to also build energizer animations.
	 */
	protected void buildMaze(PacManGameWorld world, int mazeNumber) {
		buildMazeWithoutFood(world, mazeNumber);
		maze3D.buildFood(world, rendering2D().getFoodColor(mazeNumber));
	}

	/**
	 * Builds the maze content without the food. Used when floorplan resolution is changed.
	 */
	protected void buildMazeWithoutFood(PacManGameWorld world, int mazeNumber) {
		maze3D.buildWallsAndDoors(world, rendering2D().getMazeSideColor(mazeNumber),
				rendering2D().getMazeTopColor(mazeNumber));
	}

	/**
	 * @return 2D-rendering for current game variant
	 */
	protected Rendering2D rendering2D() {
		return gameController.gameVariant() == GameVariant.MS_PACMAN ? ScenesMsPacMan.RENDERING : ScenesPacMan.RENDERING;
	}

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
}