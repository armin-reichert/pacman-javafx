package de.amr.games.pacman.ui.fx.scenes.common._3d;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx.Env;
import de.amr.games.pacman.ui.fx.entities._3d.Bonus3D;
import de.amr.games.pacman.ui.fx.entities._3d.Ghost3D;
import de.amr.games.pacman.ui.fx.entities._3d.LevelCounter3D;
import de.amr.games.pacman.ui.fx.entities._3d.LivesCounter3D;
import de.amr.games.pacman.ui.fx.entities._3d.Maze3D;
import de.amr.games.pacman.ui.fx.entities._3d.Player3D;
import de.amr.games.pacman.ui.fx.entities._3d.ScoreNotReally3D;
import de.amr.games.pacman.ui.fx.model3D.PacManModel3D;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D_Assets;
import de.amr.games.pacman.ui.fx.scenes.common.GameScene;
import de.amr.games.pacman.ui.fx.scenes.common._3d.cams.Cam_FollowingPlayer;
import de.amr.games.pacman.ui.fx.scenes.common._3d.cams.Cam_NearPlayer;
import de.amr.games.pacman.ui.fx.scenes.common._3d.cams.Cam_Total;
import de.amr.games.pacman.ui.fx.scenes.mspacman.MsPacManScenes;
import de.amr.games.pacman.ui.fx.scenes.pacman.PacManScenes;
import de.amr.games.pacman.ui.fx.util.CoordinateSystem;
import javafx.scene.AmbientLight;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.transform.Rotate;

/**
 * 3D-scene displaying the maze and the game play. Used in both game variants.
 * 
 * @author Armin Reichert
 */
public class PlayScene3D_Raw implements GameScene {

	static final int CAM_TOTAL = 0, CAM_FOLLOWING_PLAYER = 1, CAM_NEAR_PLAYER = 2;

	private final SubScene subSceneFX;
	private final PlayScene3DPerspective[] perspectives;
	private int selectedPerspective;

	protected PacManGameController gameController;

	protected final PacManModel3D model3D;
	protected Maze3D maze3D;
	protected Player3D player3D;
	protected List<Ghost3D> ghosts3D;
	protected Bonus3D bonus3D;
	protected ScoreNotReally3D score3D;
	protected LevelCounter3D levelCounter3D;
	protected LivesCounter3D livesCounter3D;

	public PlayScene3D_Raw(PacManModel3D model3D) {
		this.model3D = model3D;
		subSceneFX = new SubScene(new Group(), 1, 1, true, SceneAntialiasing.BALANCED);
		Camera cam = new PerspectiveCamera(true);
		subSceneFX.setCamera(cam);
		subSceneFX.addEventHandler(KeyEvent.KEY_PRESSED, event -> selectedPerspective().handle(event));
		perspectives = new PlayScene3DPerspective[] { //
				new Cam_Total(cam), //
				new Cam_FollowingPlayer(cam), //
				new Cam_NearPlayer(cam), //
//				new POVPerspective(this), //
		};
		selectPerspective(CAM_FOLLOWING_PLAYER);
		Env.$mazeResolution.addListener((resolution, oldValue, newValue) -> buildMazeWalls(newValue.intValue()));
	}

	@Override
	public void init() {
		log("%s: init", this);

		final var r2D = game().variant() == GameVariant.MS_PACMAN ? MsPacManScenes.RENDERING : PacManScenes.RENDERING;
		final var width = game().level().world.numCols() * TS;
		final var height = game().level().world.numRows() * TS;

		maze3D = new Maze3D(width, height);
		maze3D.setFloorTexture(new Image(getClass().getResourceAsStream("/common/escher-texture.jpg")));
		maze3D.setWallBaseColor(Rendering2D_Assets.getMazeWallSideColor(game().variant(), game().level().mazeNumber));
		maze3D.setWallTopColor(Rendering2D_Assets.getMazeWallTopColor(game().variant(), game().level().mazeNumber));
		maze3D.$wallHeight.bind(Env.$mazeWallHeight);
		buildMaze();

		player3D = new Player3D(game().player(), model3D);
		ghosts3D = game().ghosts().map(ghost -> new Ghost3D(ghost, model3D, r2D)).collect(Collectors.toList());
		bonus3D = new Bonus3D(r2D);
		score3D = new ScoreNotReally3D();

		livesCounter3D = new LivesCounter3D(model3D);
		livesCounter3D.setTranslateX(TS);
		livesCounter3D.setTranslateY(TS);
		livesCounter3D.setTranslateZ(-4); // TODO
		livesCounter3D.setVisible(!gameController.isAttractMode());

		levelCounter3D = new LevelCounter3D(r2D);
		levelCounter3D.setRightPosition(26 * TS, TS);
		levelCounter3D.setTranslateZ(-4); // TODO
		levelCounter3D.rebuild(game());

		var playground = new Group(maze3D, score3D, livesCounter3D, levelCounter3D, player3D, bonus3D);
		playground.getChildren().addAll(ghosts3D);
		playground.setTranslateX(-0.5 * width);
		playground.setTranslateY(-0.5 * height);

		var coordinateSystem = new CoordinateSystem(subSceneFX.getWidth());
		coordinateSystem.visibleProperty().bind(Env.$axesVisible);

		subSceneFX.setRoot(new Group(new AmbientLight(), playground, coordinateSystem));
	}

	@Override
	public void update() {
		player3D.update();
		ghosts3D.forEach(Ghost3D::update);
		bonus3D.update(game().bonus());
		score3D.update(game(), gameController.isAttractMode());
		// TODO: is this the recommended way to do keep the score in plain view?
		score3D.setRotationAxis(Rotate.X_AXIS);
		score3D.setRotate(subSceneFX.getCamera().getRotate());
		livesCounter3D.setVisibleItems(game().lives());
		selectedPerspective().follow(player3D);
	}

	@Override
	public void end() {
		log("%s: end", this);
	}

	public PlayScene3DPerspective selectedPerspective() {
		return perspectives[selectedPerspective];
	}

	public void selectPerspective(int index) {
		selectedPerspective = index;
		selectedPerspective().reset();
	}

	public void nextPerspective() {
		selectPerspective((selectedPerspective + 1) % perspectives.length);
	}

	@Override
	public PacManGameController getGameController() {
		return gameController;
	}

	@Override
	public void setGameController(PacManGameController gameController) {
		this.gameController = gameController;
	}

	@Override
	public OptionalDouble aspectRatio() {
		return OptionalDouble.empty();
	}

	@Override
	public SubScene getSubSceneFX() {
		return subSceneFX;
	}

	@Override
	public void resize(double width, double height) {
		// data binding does the job
	}

	protected void buildMaze() {
		var foodColor = Rendering2D_Assets.getFoodColor(game().variant(), game().level().mazeNumber);
		maze3D.buildWallsAndAddFood(game().level().world, Env.$mazeResolution.get(), Env.$mazeWallHeight.get(), foodColor);
	}

	protected void buildMazeWalls(int resolution) {
		maze3D.buildWalls(game().level().world, resolution, Env.$mazeWallHeight.get());
	}
}