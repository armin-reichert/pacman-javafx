package de.amr.games.pacman.ui.fx.common;

import static de.amr.games.pacman.model.common.GameType.MS_PACMAN;
import static de.amr.games.pacman.model.common.GameType.PACMAN;

import java.util.Arrays;
import java.util.EnumMap;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.model.common.GameModel;
import de.amr.games.pacman.model.common.GameType;
import de.amr.games.pacman.model.common.PacManGameState;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_IntermissionScene1;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_IntermissionScene2;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_IntermissionScene3;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_IntroScene;
import de.amr.games.pacman.ui.fx.pacman.PacMan_IntermissionScene1;
import de.amr.games.pacman.ui.fx.pacman.PacMan_IntermissionScene2;
import de.amr.games.pacman.ui.fx.pacman.PacMan_IntermissionScene3;
import de.amr.games.pacman.ui.fx.pacman.PacMan_IntroScene;
import de.amr.games.pacman.ui.fx.rendering.FXRendering;
import de.amr.games.pacman.ui.fx.rendering.standard.MsPacMan_StandardRendering;
import de.amr.games.pacman.ui.fx.rendering.standard.PacMan_StandardRendering;
import de.amr.games.pacman.ui.sound.SoundManager;

/**
 * Controls scene selection and serves as scene factory.
 * 
 * @author Armin Reichert
 */
public class SceneController {

	public static final EnumMap<GameType, FXRendering> RENDERING_2D = new EnumMap<>(GameType.class);
	static {
		RENDERING_2D.put(MS_PACMAN, new MsPacMan_StandardRendering());
		RENDERING_2D.put(PACMAN, new PacMan_StandardRendering());
	}

	public static final EnumMap<GameType, SoundManager> SOUND = new EnumMap<>(GameType.class);
	static {
		SOUND.put(MS_PACMAN, new PacManGameSoundManager(PacManGameSounds::msPacManSoundURL));
		SOUND.put(PACMAN, new PacManGameSoundManager(PacManGameSounds::mrPacManSoundURL));
	}

	private static final Class<?> SCENES[/* game type */][/* scene index */][/* 2D, 3D */] = {
		//@formatter:off
		{
			{ MsPacMan_IntroScene.class, null },
			{ MsPacMan_IntermissionScene1.class, null },
			{ MsPacMan_IntermissionScene2.class, null },
			{ MsPacMan_IntermissionScene3.class, null },
			{ PlayScene2D.class, PlayScene3D.class },
		},
		{
			{ PacMan_IntroScene.class, null },
			{ PacMan_IntermissionScene1.class, null },
			{ PacMan_IntermissionScene2.class, null },
			{ PacMan_IntermissionScene3.class, null },
			{ PlayScene2D.class, PlayScene3D.class },
		},
		//@formatter:on
	};

	private static int sceneIndex(GameModel game) {
		return game.state == PacManGameState.INTRO ? 0
				: game.state == PacManGameState.INTERMISSION ? game.intermissionNumber : 4;
	}

	public static boolean is2DAnd3DVersionAvailable(GameType gameType, GameModel game) {
		return SCENES[gameType.ordinal()][sceneIndex(game)].length > 1;
	}

	public static boolean isSuitableScene(GameScene gameScene, GameType gameType, GameModel game) {
		Class<?> sceneClasses[] = SCENES[gameType.ordinal()][sceneIndex(game)];
		return Arrays.asList(sceneClasses).contains(gameScene.getClass());
	}

	public static GameScene createGameScene(PacManGameController controller, double height, boolean version3D) {
		GameType gameType = controller.isPlaying(PACMAN) ? PACMAN : MS_PACMAN;
		GameModel game = controller.getGame();
		switch (gameType) {
		case MS_PACMAN: {
			int sceneIndex = sceneIndex(game);
			if (sceneIndex == 0) {
				return new MsPacMan_IntroScene(controller, RENDERING_2D.get(MS_PACMAN), SOUND.get(MS_PACMAN));
			} else if (sceneIndex == 1) {
				return new MsPacMan_IntermissionScene1(controller, RENDERING_2D.get(MS_PACMAN), SOUND.get(MS_PACMAN));
			} else if (sceneIndex == 2) {
				return new MsPacMan_IntermissionScene2(controller, RENDERING_2D.get(MS_PACMAN), SOUND.get(MS_PACMAN));
			} else if (sceneIndex == 3) {
				return new MsPacMan_IntermissionScene3(controller, RENDERING_2D.get(MS_PACMAN), SOUND.get(MS_PACMAN));
			} else if (sceneIndex == 4) {
				return version3D ? new PlayScene3D(controller, height)
						: new PlayScene2D(controller, RENDERING_2D.get(MS_PACMAN), SOUND.get(MS_PACMAN));
			}
			break;
		}
		case PACMAN: {
			int sceneIndex = sceneIndex(game);
			if (sceneIndex == 0) {
				return new PacMan_IntroScene(controller, RENDERING_2D.get(PACMAN), SOUND.get(PACMAN));
			} else if (sceneIndex == 1) {
				return new PacMan_IntermissionScene1(controller, RENDERING_2D.get(PACMAN), SOUND.get(PACMAN));
			} else if (sceneIndex == 2) {
				return new PacMan_IntermissionScene2(controller, RENDERING_2D.get(PACMAN), SOUND.get(PACMAN));
			} else if (sceneIndex == 3) {
				return new PacMan_IntermissionScene3(controller, RENDERING_2D.get(PACMAN), SOUND.get(PACMAN));
			} else if (sceneIndex == 4) {
				return version3D ? new PlayScene3D(controller, height)
						: new PlayScene2D(controller, RENDERING_2D.get(PACMAN), SOUND.get(PACMAN));
			}
			break;
		}

		default:
			break;
		}
		// all hope is lost
		throw new IllegalStateException();
	}
}