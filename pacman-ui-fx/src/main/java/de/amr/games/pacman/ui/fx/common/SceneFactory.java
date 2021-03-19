package de.amr.games.pacman.ui.fx.common;

import static de.amr.games.pacman.model.common.GameType.MS_PACMAN;
import static de.amr.games.pacman.model.common.GameType.PACMAN;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Objects;

import de.amr.games.pacman.controller.PacManGameController;
import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.model.common.GameType;
import de.amr.games.pacman.ui.fx.common.scene2d.PlayScene2D;
import de.amr.games.pacman.ui.fx.common.scene3d.PlayScene3D;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_IntermissionScene1;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_IntermissionScene2;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_IntermissionScene3;
import de.amr.games.pacman.ui.fx.mspacman.MsPacMan_IntroScene;
import de.amr.games.pacman.ui.fx.pacman.PacMan_IntermissionScene1;
import de.amr.games.pacman.ui.fx.pacman.PacMan_IntermissionScene2;
import de.amr.games.pacman.ui.fx.pacman.PacMan_IntermissionScene3;
import de.amr.games.pacman.ui.fx.pacman.PacMan_IntroScene;
import de.amr.games.pacman.ui.fx.rendering.PacManGameRendering2D;
import de.amr.games.pacman.ui.fx.rendering.standard.MsPacMan_StandardRendering;
import de.amr.games.pacman.ui.fx.rendering.standard.PacMan_StandardRendering;
import de.amr.games.pacman.ui.fx.sound.PacManGameSoundManager;
import de.amr.games.pacman.ui.fx.sound.PacManGameSounds;
import de.amr.games.pacman.ui.sound.SoundManager;
import javafx.stage.Stage;

/**
 * Controls scene selection and serves as scene factory.
 * 
 * @author Armin Reichert
 */
public class SceneFactory {

	private static final EnumMap<GameType, PacManGameRendering2D> RENDERING_2D = new EnumMap<>(GameType.class);

	static {
		RENDERING_2D.put(MS_PACMAN, new MsPacMan_StandardRendering());
		RENDERING_2D.put(PACMAN, new PacMan_StandardRendering());
	}

	public static final EnumMap<GameType, SoundManager> SOUND = new EnumMap<>(GameType.class);

	static {
		SOUND.put(MS_PACMAN, new PacManGameSoundManager(PacManGameSounds::msPacManSoundURL));
		SOUND.put(PACMAN, new PacManGameSoundManager(PacManGameSounds::mrPacManSoundURL));
	}

	private static final Class<?> SCENE_CLASSES[][][] = new Class<?>[2][5][2];

	static {
		//@formatter:off
		SCENE_CLASSES[MS_PACMAN.ordinal()][0][0] = MsPacMan_IntroScene.class;
		SCENE_CLASSES[MS_PACMAN.ordinal()][1][0] = MsPacMan_IntermissionScene1.class;
		SCENE_CLASSES[MS_PACMAN.ordinal()][2][0] = MsPacMan_IntermissionScene2.class;
		SCENE_CLASSES[MS_PACMAN.ordinal()][3][0] = MsPacMan_IntermissionScene3.class;
		SCENE_CLASSES[MS_PACMAN.ordinal()][4][0] = PlayScene2D.class;
		SCENE_CLASSES[MS_PACMAN.ordinal()][4][1] = PlayScene3D.class;

		SCENE_CLASSES[PACMAN.ordinal()]   [0][0] = PacMan_IntroScene.class;
		SCENE_CLASSES[PACMAN.ordinal()]   [1][0] = PacMan_IntermissionScene1.class;
		SCENE_CLASSES[PACMAN.ordinal()]   [2][0] = PacMan_IntermissionScene2.class;
		SCENE_CLASSES[PACMAN.ordinal()]   [3][0] = PacMan_IntermissionScene3.class;
		SCENE_CLASSES[PACMAN.ordinal()]   [4][0] = PlayScene2D.class;
		SCENE_CLASSES[PACMAN.ordinal()]   [4][1] = PlayScene3D.class;
		//@formatter:on
	}

	public static GameScene createGameScene(Stage stage, PacManGameController controller, boolean version3D) {
		int sceneIndex = sceneIndex(controller);
		switch (controller.selectedGameType()) {
		case MS_PACMAN:
			switch (sceneIndex) {
			case 0:
				return new MsPacMan_IntroScene(controller, RENDERING_2D.get(MS_PACMAN), SOUND.get(MS_PACMAN));
			case 1:
				return new MsPacMan_IntermissionScene1(controller, RENDERING_2D.get(MS_PACMAN), SOUND.get(MS_PACMAN));
			case 2:
				return new MsPacMan_IntermissionScene2(controller, RENDERING_2D.get(MS_PACMAN), SOUND.get(MS_PACMAN));
			case 3:
				return new MsPacMan_IntermissionScene3(controller, RENDERING_2D.get(MS_PACMAN), SOUND.get(MS_PACMAN));
			case 4:
				return version3D ? new PlayScene3D(stage, controller)
						: new PlayScene2D(controller, RENDERING_2D.get(MS_PACMAN), SOUND.get(MS_PACMAN));
			default:
				break;
			}
		case PACMAN:
			switch (sceneIndex) {
			case 0:
				return new PacMan_IntroScene(controller, RENDERING_2D.get(PACMAN), SOUND.get(PACMAN));
			case 1:
				return new PacMan_IntermissionScene1(controller, RENDERING_2D.get(PACMAN), SOUND.get(PACMAN));
			case 2:
				return new PacMan_IntermissionScene2(controller, RENDERING_2D.get(PACMAN), SOUND.get(PACMAN));
			case 3:
				return new PacMan_IntermissionScene3(controller, RENDERING_2D.get(PACMAN), SOUND.get(PACMAN));
			case 4:
				return version3D ? new PlayScene3D(stage, controller)
						: new PlayScene2D(controller, RENDERING_2D.get(PACMAN), SOUND.get(PACMAN));
			default:
				break;
			}
		default:
			break;
		}
		// all hope is lost
		throw new IllegalStateException();
	}

	public static boolean isSuitableScene(GameScene gameSceneOrNull, PacManGameController controller, boolean use3D) {
		if (gameSceneOrNull == null) {
			return false;
		}
		int gameIndex = controller.selectedGameType().ordinal();
		Class<?> sceneClass = SCENE_CLASSES[gameIndex][sceneIndex(controller)][use3D ? 1 : 0];
		return gameSceneOrNull.getClass().equals(sceneClass);
	}

	public static boolean is2DAnd3DVersionAvailable(PacManGameController controller) {
		int gameIndex = controller.selectedGameType().ordinal();
		return Arrays.stream(SCENE_CLASSES[gameIndex][sceneIndex(controller)]).filter(Objects::nonNull).count() > 1;
	}

	private static int sceneIndex(PacManGameController controller) {
		return controller.state == PacManGameState.INTRO ? 0
				: controller.state == PacManGameState.INTERMISSION ? controller.selectedGame().intermissionNumber : 4;
	}
}