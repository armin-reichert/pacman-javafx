package de.amr.games.pacman.ui.fx.pacman;

import static de.amr.games.pacman.heaven.God.clock;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.lib.Logging;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.ui.fx.PacManGameFXUI;
import de.amr.games.pacman.ui.fx.common.PacManGameScene;
import de.amr.games.pacman.ui.fx.common.PlayScene;

/**
 * The scenes of the Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class Scenes {

	private static IntroScene introScene;
	private static PlayScene<PacManSceneRendering> playScene;
	private static IntermissionScene1 intermissionScene1;
	private static IntermissionScene2 intermissionScene2;
	private static IntermissionScene3 intermissionScene3;

	public static void createScenes(PacManGame game, double width, double height, double scaling) {
		introScene = new IntroScene(game, width, height, scaling);
		playScene = new PlayScene<>(width, height, scaling, game, PacManSceneRendering.IT, PacManGameFXUI.pacManSounds);
		playScene.setFlashMessageSupplier(PacManGameFXUI.flashMessageQ::peek);
		intermissionScene1 = new IntermissionScene1(game, width, height, scaling);
		intermissionScene2 = new IntermissionScene2(game, width, height, scaling);
		intermissionScene3 = new IntermissionScene3(game, width, height, scaling);
		Logging.log("Pac-Man game scenes created at %d", clock.ticksTotal);
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
