package de.amr.games.pacman.ui.fx.scenes.common._3d;

import static de.amr.games.pacman.lib.Logging.log;
import static de.amr.games.pacman.model.world.PacManGameWorld.TS;

import java.util.Map;
import java.util.OptionalDouble;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.lib.V2i;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.model.common.Ghost;
import de.amr.games.pacman.model.world.PacManGameWorld;
import de.amr.games.pacman.ui.fx.Env;
import de.amr.games.pacman.ui.fx.entities._3d.Bonus3D;
import de.amr.games.pacman.ui.fx.entities._3d.CoordinateSystem;
import de.amr.games.pacman.ui.fx.entities._3d.Ghost3D;
import de.amr.games.pacman.ui.fx.entities._3d.LevelCounter3D;
import de.amr.games.pacman.ui.fx.entities._3d.LivesCounter3D;
import de.amr.games.pacman.ui.fx.entities._3d.Maze3D;
import de.amr.games.pacman.ui.fx.entities._3d.Player3D;
import de.amr.games.pacman.ui.fx.entities._3d.ScoreNotReally3D;
import de.amr.games.pacman.ui.fx.model3D.PacManModel3D;
import de.amr.games.pacman.ui.fx.rendering.Rendering2D_Assets;
import de.amr.games.pacman.ui.fx.scenes.common.GameScene;
import de.amr.games.pacman.ui.fx.scenes.mspacman.MsPacManScenes;
import de.amr.games.pacman.ui.fx.scenes.pacman.PacManScenes;
import javafx.scene.AmbientLight;
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
public class PlayScene3DBase implements GameScene {

	static final int PERSPECTIVE_TOTAL = 0;
	static final int PERSPECTIVE_FOLLOWING_PLAYER = 1;
	static final int PERSPECTIVE_NEAR_PLAYER = 2;

	private final SubScene subSceneFX;
	private final PlayScenePerspective[] perspectives;
	private int selectedPerspective;

	protected PacManGameController gameController;

	protected PacManModel3D model3D;
	protected Maze3D maze3D;
	protected Player3D player3D;
	protected Map<Ghost, Ghost3D> ghosts3D;
	protected Bonus3D bonus3D;
	protected ScoreNotReally3D score3D;
	protected LevelCounter3D levelCounter3D;
	protected LivesCounter3D livesCounter3D;

	public PlayScene3DBase() {
		subSceneFX = new SubScene(new Group(), 1, 1, true, SceneAntialiasing.BALANCED);
		subSceneFX.setCamera(new PerspectiveCamera(true));
		subSceneFX.addEventHandler(KeyEvent.KEY_PRESSED, event -> selectedPerspective().handle(event));
		perspectives = new PlayScenePerspective[] { //
				new TotalPerspective(subSceneFX), //
				new FollowingPlayerPerspective(subSceneFX), //
				new NearPlayerPerspective(subSceneFX), //
//				new POVPerspective(this), //
		};
		selectPerspective(PERSPECTIVE_FOLLOWING_PLAYER);
	}

	public void setModel3D(PacManModel3D model3d) {
		this.model3D = model3d;
	}

	@Override
	public void init() {
		log("%s: init", this);

		final var r2D = game().variant() == GameVariant.MS_PACMAN ? MsPacManScenes.RENDERING : PacManScenes.RENDERING;
		final var width = PacManGameWorld.DEFAULT_WIDTH * TS;
		final var height = PacManGameWorld.DEFAULT_HEIGHT * TS;

		maze3D = new Maze3D(width, height);
		maze3D.setFloorTexture(new Image(getClass().getResourceAsStream("/common/escher-texture.jpg")));
		maze3D
				.setWallBaseColor(Rendering2D_Assets.getMazeWallSideColor(game().variant(), game().level().mazeNumber));
		maze3D.setWallTopColor(Rendering2D_Assets.getMazeWallTopColor(game().variant(), game().level().mazeNumber));
		maze3D.$wallHeight.bind(Env.$mazeWallHeight);
		Env.$mazeResolution.addListener((bean, old, newResolution) -> {
			maze3D.buildWalls(game().level().world, newResolution.intValue(), Env.$mazeWallHeight.get());
		});
		buildMaze();

		player3D = new Player3D(game().player(), model3D);
		player3D.setTranslateZ(-3); // TODO

		ghosts3D = game().ghosts().collect(Collectors.toMap(Function.identity(), ghost -> {
			Ghost3D ghost3D = new Ghost3D(ghost, model3D, r2D);
			ghost3D.setTranslateZ(-4); // TODO
			return ghost3D;
		}));

		bonus3D = new Bonus3D(game().variant(), r2D);
		bonus3D.setTranslateZ(-4); // TODO

		score3D = new ScoreNotReally3D();
		score3D.setHiscoreOnly(gameController.isAttractMode());

		livesCounter3D = new LivesCounter3D(model3D);
		livesCounter3D.setTranslateX(TS);
		livesCounter3D.setTranslateY(TS);
		livesCounter3D.setTranslateZ(-4); // TODO
		livesCounter3D.setVisible(!gameController.isAttractMode());

		levelCounter3D = new LevelCounter3D(new V2i(26, 1), r2D);
		levelCounter3D.setTranslateZ(-4); // TODO
		levelCounter3D.rebuild(game());

		var sceneContent = new Group();
		sceneContent.setTranslateX(-0.5 * width);
		sceneContent.setTranslateY(-0.5 * height);
		sceneContent.getChildren().addAll(maze3D, score3D, livesCounter3D, levelCounter3D, player3D, bonus3D);
		sceneContent.getChildren().addAll(ghosts3D.values());

		subSceneFX.setRoot(new Group(new AmbientLight(), sceneContent, new CoordinateSystem(subSceneFX.getWidth())));
	}

	protected void buildMaze() {
		var foodColor = Rendering2D_Assets.getFoodColor(game().variant(), game().level().mazeNumber);
		maze3D.buildWallsAndAddFood(game().level().world, Env.$mazeResolution.get(), Env.$mazeWallHeight.get(),
				foodColor);
	}

	@Override
	public void update() {
		livesCounter3D.setVisibleItems(game().lives());
		player3D.update();
		ghosts3D.values().forEach(Ghost3D::update);
		bonus3D.update(game().bonus());
		// TODO: is this the recommended way to do keep the score in plain view?
		score3D.update(game());
		score3D.setRotationAxis(Rotate.X_AXIS);
		score3D.setRotate(subSceneFX.getCamera().getRotate());
		selectedPerspective().follow(player3D);
	}

	@Override
	public void end() {
		log("%s: end", this);
	}

	public PlayScenePerspective selectedPerspective() {
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
}