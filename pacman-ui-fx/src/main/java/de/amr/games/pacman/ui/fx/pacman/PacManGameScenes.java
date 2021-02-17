package de.amr.games.pacman.ui.fx.pacman;

import static de.amr.games.pacman.heaven.God.clock;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.lib.Logging;
import de.amr.games.pacman.model.PacManGame;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.PacManGameSoundAssets;
import de.amr.games.pacman.sound.PacManGameSoundManager;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.fx.common.PacManGameScene;
import de.amr.games.pacman.ui.fx.common.PlayScene;

/**
 * The scenes of the Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class PacManGameScenes {

	public static final PacManSceneRendering rendering = new PacManSceneRendering();
	public static final SoundManager soundManager = new PacManGameSoundManager(PacManGameSoundAssets::getPacManSoundURL);

	private IntroScene introScene;
	private PlayScene<PacManSceneRendering> playScene;
	private IntermissionScene1 intermissionScene1;
	private IntermissionScene2 intermissionScene2;
	private IntermissionScene3 intermissionScene3;

	public void createScenes(PacManGame game, double width, double height, double scaling) {
		introScene = new IntroScene(game, width, height, scaling);
		playScene = new PlayScene<>(game, width, height, scaling, rendering, soundManager);
		intermissionScene1 = new IntermissionScene1(game, width, height, scaling);
		intermissionScene2 = new IntermissionScene2(game, width, height, scaling);
		intermissionScene3 = new IntermissionScene3(game, width, height, scaling);
		Logging.log("Pac-Man game scenes created at %d", clock.ticksTotal);
	}

	public PacManGameScene selectScene(PacManGameModel game) {
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
