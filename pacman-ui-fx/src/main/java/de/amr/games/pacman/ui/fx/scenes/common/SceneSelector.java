package de.amr.games.pacman.ui.fx.scenes.common;

import de.amr.games.pacman.controller.PacManGameState;
import de.amr.games.pacman.model.common.PacManGameModel;

/**
 * Scene selector.
 * 
 * @author Armin Reichert
 */
public class SceneSelector {

	public static GameScene getScene(PacManGameModel game, PacManGameState state, boolean _3D) {
		GameScene[][] scenes;
		switch (game.variant()) {
		case MS_PACMAN:
			scenes = de.amr.games.pacman.ui.fx.scenes.mspacman.Scenes.SCENES;
			break;
		case PACMAN:
			scenes = de.amr.games.pacman.ui.fx.scenes.pacman.Scenes.SCENES;
			break;
		default:
			throw new IllegalStateException();
		}

		int sceneIndex;
		switch (state) {
		case INTRO:
			sceneIndex = 0;
			break;
		case INTERMISSION:
			sceneIndex = game.intermissionAfterLevel(game.level().number).getAsInt();
			break;
		default:
			sceneIndex = 4;
			break;
		}

		return scenes[sceneIndex][_3D ? 1 : 0];
	}
}