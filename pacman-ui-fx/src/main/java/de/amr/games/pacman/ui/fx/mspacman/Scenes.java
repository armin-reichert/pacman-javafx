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

	private static IntroScene introScene;
	private static PlayScene<MsPacManSceneRendering> playScene;
	private static IntermissionScene1 intermissionScene1;
	private static IntermissionScene2 intermissionScene2;
	private static IntermissionScene3 intermissionScene3;

	public static void createScenes(MsPacManGame game, double width, double height, double scaling) {
		introScene = new IntroScene(game, width, height, scaling);
		playScene = new PlayScene<>(width, height, scaling, game, MsPacManSceneRendering.IT, PacManGameFXUI.msPacManSounds);
		intermissionScene1 = new IntermissionScene1(game, width, height, scaling);
		intermissionScene2 = new IntermissionScene2(game, width, height, scaling);
		intermissionScene3 = new IntermissionScene3(game, width, height, scaling);
	}

	public static PacManGameScene selectScene(PacManGameModel game) {
		if (game.state == PacManGameState.INTRO) {
			return introScene;
		}
		if (game.state == PacManGameState.INTERMISSION) {
			if (game.intermissionNumber == 1) {
				return intermissionScene1;
			}
			if (game.intermissionNumber == 2) {
				return intermissionScene2;
			}
			if (game.intermissionNumber == 3) {
				return intermissionScene3;
			}
		}
		return playScene;
	}
}