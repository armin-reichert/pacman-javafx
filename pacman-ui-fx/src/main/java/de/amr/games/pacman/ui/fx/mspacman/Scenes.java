package de.amr.games.pacman.ui.fx.mspacman;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.model.MsPacManGame;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.ui.fx.PacManGameFXUI;
import de.amr.games.pacman.ui.fx.common.PacManGameScene;
import de.amr.games.pacman.ui.fx.common.PlayScene;

/**
 * The scenes of the Ms. Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class Scenes {

	private static final PacManGameScene[] scenes = new PacManGameScene[5];

	public static void createScenes(MsPacManGame game, double width, double height, double scaling) {
		scenes[0] = new IntroScene(game, width, height, scaling);
		scenes[1] = new IntermissionScene1(game, width, height, scaling);
		scenes[2] = new IntermissionScene2(game, width, height, scaling);
		scenes[3] = new IntermissionScene3(game, width, height, scaling);
		scenes[4] = new PlayScene<>(width, height, scaling, game, MsPacManSceneRendering.IT, PacManGameFXUI.msPacManSounds);
	}

	public static PacManGameScene selectScene(PacManGameModel game) {
		if (game.state == PacManGameState.INTRO) {
			return scenes[0];
		}
		if (game.state == PacManGameState.INTERMISSION) {
			return scenes[game.intermissionNumber];
		}
		return scenes[4];
	}
}