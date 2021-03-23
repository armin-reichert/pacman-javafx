package de.amr.games.pacman.ui.fx.scenes.common;

import static de.amr.games.pacman.model.common.GameVariant.MS_PACMAN;
import static de.amr.games.pacman.model.common.GameVariant.PACMAN;

import java.lang.reflect.InvocationTargetException;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.fx.scenes.common.scene3d.PlayScene3D;
import de.amr.games.pacman.ui.fx.scenes.mspacman.MsPacMan_IntermissionScene1;
import de.amr.games.pacman.ui.fx.scenes.mspacman.MsPacMan_IntermissionScene2;
import de.amr.games.pacman.ui.fx.scenes.mspacman.MsPacMan_IntermissionScene3;
import de.amr.games.pacman.ui.fx.scenes.mspacman.MsPacMan_IntroScene;
import de.amr.games.pacman.ui.fx.scenes.mspacman.MsPacMan_PlayScene;
import de.amr.games.pacman.ui.fx.scenes.pacman.PacMan_IntermissionScene1;
import de.amr.games.pacman.ui.fx.scenes.pacman.PacMan_IntermissionScene2;
import de.amr.games.pacman.ui.fx.scenes.pacman.PacMan_IntermissionScene3;
import de.amr.games.pacman.ui.fx.scenes.pacman.PacMan_IntroScene;
import de.amr.games.pacman.ui.fx.scenes.pacman.PacMan_PlayScene;

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
		SCENE_CLASSES[MS_PACMAN.ordinal()][4][0] = MsPacMan_PlayScene.class;
		SCENE_CLASSES[MS_PACMAN.ordinal()][4][1] = PlayScene3D.class;

		SCENE_CLASSES[PACMAN.ordinal()]   [0][0] = PacMan_IntroScene.class;
		SCENE_CLASSES[PACMAN.ordinal()]   [0][1] = PacMan_IntroScene.class;
		SCENE_CLASSES[PACMAN.ordinal()]   [1][0] = PacMan_IntermissionScene1.class;
		SCENE_CLASSES[PACMAN.ordinal()]   [1][1] = PacMan_IntermissionScene1.class;
		SCENE_CLASSES[PACMAN.ordinal()]   [2][0] = PacMan_IntermissionScene2.class;
		SCENE_CLASSES[PACMAN.ordinal()]   [2][1] = PacMan_IntermissionScene2.class;
		SCENE_CLASSES[PACMAN.ordinal()]   [3][0] = PacMan_IntermissionScene3.class;
		SCENE_CLASSES[PACMAN.ordinal()]   [3][1] = PacMan_IntermissionScene3.class;
		SCENE_CLASSES[PACMAN.ordinal()]   [4][0] = PacMan_PlayScene.class;
		SCENE_CLASSES[PACMAN.ordinal()]   [4][1] = PlayScene3D.class;
		//@formatter:on
	}

	public static GameScene createGameScene(GameVariant gameType, PacManGameState gameState, GameModel game,
			boolean _3D) {
		try {
			return (GameScene) sceneClass(gameType, gameState, game, _3D).getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException x) {
			throw new RuntimeException(x);
		}
	}

	public static boolean isSuitableScene(GameScene gameScene, GameVariant gameType, PacManGameState gameState,
			GameModel game, boolean _3D) {
		return gameScene != null && gameScene.getClass().equals(sceneClass(gameType, gameState, game, _3D));
	}

	public static boolean hasDifferentSceneFor3D(GameVariant gameType, PacManGameState gameState, GameModel game) {
		return sceneClass(gameType, gameState, game, false) != sceneClass(gameType, gameState, game, true);
	}

	private static Class<?> sceneClass(GameVariant gameType, PacManGameState gameState, GameModel game, boolean _3D) {
		return SCENE_CLASSES[gameType.ordinal()][sceneIndex(gameState, game)][_3D ? 1 : 0];
	}

	private static int sceneIndex(PacManGameState gameState, GameModel game) {
		return gameState == PacManGameState.INTRO ? 0
				: gameState == PacManGameState.INTERMISSION ? game.intermissionNumber : 4;
	}
}