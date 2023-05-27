/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene;

/**
 * @author Armin Reichert
 */
public class GameSceneConfiguration {

	private final GameSceneChoice bootSceneChoice;
	private final GameSceneChoice introSceneChoice;
	private final GameSceneChoice creditSceneChoice;
	private final GameSceneChoice playSceneChoice;
	private final GameSceneChoice[] cutSceneChoices = new GameSceneChoice[3];

	public GameSceneConfiguration(GameSceneChoice boot, GameSceneChoice intro, GameSceneChoice credit,
			GameSceneChoice play, GameSceneChoice cut1, GameSceneChoice cut2, GameSceneChoice cut3) {
		bootSceneChoice = boot;
		introSceneChoice = intro;
		creditSceneChoice = credit;
		playSceneChoice = play;
		cutSceneChoices[0] = cut1;
		cutSceneChoices[1] = cut2;
		cutSceneChoices[2] = cut3;
	}

	public GameSceneChoice bootSceneChoice() {
		return bootSceneChoice;
	}

	public GameSceneChoice creditSceneChoice() {
		return creditSceneChoice;
	}

	public GameSceneChoice introSceneChoice() {
		return introSceneChoice;
	}

	public GameSceneChoice playSceneChoice() {
		return playSceneChoice;
	}

	public GameSceneChoice cutSceneChoice(int cutSceneNumber) {
		return cutSceneChoices[cutSceneNumber - 1];
	}
}