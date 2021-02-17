package de.amr.games.pacman.ui.fx.mspacman;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.model.MsPacManGame;
import de.amr.games.pacman.model.PacManGameModel;
import de.amr.games.pacman.sound.PacManGameSoundAssets;
import de.amr.games.pacman.sound.PacManGameSoundManager;
import de.amr.games.pacman.sound.SoundManager;
import de.amr.games.pacman.ui.fx.common.PacManGameScene;
import de.amr.games.pacman.ui.fx.common.PlayScene;

/**
 * The scenes of the Ms. Pac-Man game.
 * 
 * @author Armin Reichert
 */
public class MsPacManGameScenes {

	public static final SoundManager soundManager = new PacManGameSoundManager(
			PacManGameSoundAssets::getMsPacManSoundURL);

	private IntroScene introScene;
	private PlayScene<MsPacManSceneRendering> playScene;
	private IntermissionScene1 intermissionScene1;
	private IntermissionScene2 intermissionScene2;
	private IntermissionScene3 intermissionScene3;

	public void createScenes(MsPacManGame game, double width, double height, double scaling) {
		introScene = new IntroScene(game, width, height, scaling);
		playScene = new PlayScene<>(game, width, height, scaling);
		playScene.setRendering(new MsPacManSceneRendering(playScene.gc()));
		intermissionScene1 = new IntermissionScene1(game, width, height, scaling);
		intermissionScene2 = new IntermissionScene2(game, width, height, scaling);
		intermissionScene3 = new IntermissionScene3(game, width, height, scaling);
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