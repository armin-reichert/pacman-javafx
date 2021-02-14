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

	public final SoundManager soundManager = new PacManGameSoundManager(PacManGameSoundAssets::getPacManSoundURL);;

	private PacManGameScene introScene;
	private PacManGameScene playScene;
	private PacManGameScene intermissionScene1;
	private PacManGameScene intermissionScene2;
	private PacManGameScene intermissionScene3;

	public static PlayScene<PacManSceneRendering> createPlayScene(PacManGameModel game, double width, double height,
			double scaling) {
		PlayScene<PacManSceneRendering> scene = new PlayScene<>(game, width, height, scaling);
		scene.setRendering(new PacManSceneRendering(scene.gc()));
		return scene;
	}

	public void createScenes(PacManGame game, double sizeX, double sizeY, double scaling) {
		introScene = new IntroScene(game, sizeX, sizeY, scaling);
		playScene = createPlayScene(game, sizeX, sizeY, scaling);
		intermissionScene1 = new IntermissionScene1(game, soundManager, sizeX, sizeY, scaling);
		intermissionScene2 = new IntermissionScene2(game, soundManager, sizeX, sizeY, scaling);
		intermissionScene3 = new IntermissionScene3(game, soundManager, sizeX, sizeY, scaling);
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
