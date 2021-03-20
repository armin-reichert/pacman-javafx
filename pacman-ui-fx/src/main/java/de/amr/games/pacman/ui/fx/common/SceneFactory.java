package de.amr.games.pacman.ui.fx.common;

import static de.amr.games.pacman.model.common.GameType.MS_PACMAN;
import static de.amr.games.pacman.model.common.GameType.PACMAN;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameType;
import de.amr.games.pacman.ui.fx.common.scene2d.PlayScene2D;
import de.amr.games.pacman.ui.fx.common.scene3d.PlayScene3D;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_IntermissionScene1;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_IntermissionScene2;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_IntermissionScene3;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_IntroScene;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_PlayScene;
import de.amr.games.pacman.ui.fx.pacman.PacMan_IntermissionScene1;
import de.amr.games.pacman.ui.fx.pacman.PacMan_IntermissionScene2;
import de.amr.games.pacman.ui.fx.pacman.PacMan_IntermissionScene3;
import de.amr.games.pacman.ui.fx.pacman.PacMan_IntroScene;
import de.amr.games.pacman.ui.fx.pacman.PacMan_PlayScene;
import javafx.stage.Stage;

/**
 * Controls scene selection and serves as scene factory.
 * 
 * @author Armin Reichert
 */
public class SceneFactory {

	private static final Class<?> SCENE_CLASSES[][][] = new Class<?>[2][5][2];

	static {
		//@formatter:off
		SCENE_CLASSES[MS_PACMAN.ordinal()][0][0] = MsPacMan_IntroScene.class;
		SCENE_CLASSES[MS_PACMAN.ordinal()][0][1] = MsPacMan_IntroScene.class;
		SCENE_CLASSES[MS_PACMAN.ordinal()][1][0] = MsPacMan_IntermissionScene1.class;
		SCENE_CLASSES[MS_PACMAN.ordinal()][1][1] = MsPacMan_IntermissionScene1.class;
		SCENE_CLASSES[MS_PACMAN.ordinal()][2][0] = MsPacMan_IntermissionScene2.class;
		SCENE_CLASSES[MS_PACMAN.ordinal()][2][1] = MsPacMan_IntermissionScene2.class;
		SCENE_CLASSES[MS_PACMAN.ordinal()][3][0] = MsPacMan_IntermissionScene3.class;
		SCENE_CLASSES[MS_PACMAN.ordinal()][3][1] = MsPacMan_IntermissionScene3.class;
		SCENE_CLASSES[MS_PACMAN.ordinal()][4][0] = PlayScene2D.class;
		SCENE_CLASSES[MS_PACMAN.ordinal()][4][1] = PlayScene3D.class;

		SCENE_CLASSES[PACMAN.ordinal()]   [0][0] = PacMan_IntroScene.class;
		SCENE_CLASSES[PACMAN.ordinal()]   [0][1] = PacMan_IntroScene.class;
		SCENE_CLASSES[PACMAN.ordinal()]   [1][0] = PacMan_IntermissionScene1.class;
		SCENE_CLASSES[PACMAN.ordinal()]   [1][1] = PacMan_IntermissionScene1.class;
		SCENE_CLASSES[PACMAN.ordinal()]   [2][0] = PacMan_IntermissionScene2.class;
		SCENE_CLASSES[PACMAN.ordinal()]   [2][1] = PacMan_IntermissionScene2.class;
		SCENE_CLASSES[PACMAN.ordinal()]   [3][0] = PacMan_IntermissionScene3.class;
		SCENE_CLASSES[PACMAN.ordinal()]   [3][1] = PacMan_IntermissionScene3.class;
		SCENE_CLASSES[PACMAN.ordinal()]   [4][0] = PlayScene2D.class;
		SCENE_CLASSES[PACMAN.ordinal()]   [4][1] = PlayScene3D.class;
		//@formatter:on
	}

	public static GameScene createGameScene(Stage stage, GameType gameType, PacManGameState gameState, GameModel game,
			boolean _3D) {
		switch (gameType) {
		case MS_PACMAN:
			switch (sceneIndex(gameState, game)) {
			case 0:
				return new MsPacMan_IntroScene();
			case 1:
				return new MsPacMan_IntermissionScene1();
			case 2:
				return new MsPacMan_IntermissionScene2();
			case 3:
				return new MsPacMan_IntermissionScene3();
			case 4:
				return _3D ? new PlayScene3D(stage) : new MsPacMan_PlayScene();
			default:
				break;
			}
		case PACMAN:
			switch (sceneIndex(gameState, game)) {
			case 0:
				return new PacMan_IntroScene();
			case 1:
				return new PacMan_IntermissionScene1();
			case 2:
				return new PacMan_IntermissionScene2();
			case 3:
				return new PacMan_IntermissionScene3();
			case 4:
				return _3D ? new PlayScene3D(stage) : new PacMan_PlayScene();
			default:
				break;
			}
		default:
			break;
		}
		// all hope is lost
		throw new IllegalStateException();
	}

	public static boolean isSuitableScene(GameScene gameScene, GameType gameType, PacManGameState gameState,
			GameModel game, boolean _3D) {
		return gameScene != null && gameScene.getClass().equals(sceneClass(gameType, gameState, game, _3D));
	}

	public static boolean hasDifferentSceneFor3D(GameType gameType, PacManGameState gameState, GameModel game) {
		return sceneClass(gameType, gameState, game, false) != sceneClass(gameType, gameState, game, true);
	}

	private static Class<?> sceneClass(GameType gameType, PacManGameState gameState, GameModel game, boolean _3D) {
		return SCENE_CLASSES[gameType.ordinal()][sceneIndex(gameState, game)][_3D ? 1 : 0];
	}

	private static int sceneIndex(PacManGameState gameState, GameModel game) {
		return gameState == PacManGameState.INTRO ? 0
				: gameState == PacManGameState.INTERMISSION ? game.intermissionNumber : 4;
	}
}