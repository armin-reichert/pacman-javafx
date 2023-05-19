/*
MIT License

Copyright (c) 2023 Armin Reichert

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
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

	public boolean isPlayScene(GameScene gameScene) {
		return playSceneChoice().includes(gameScene);
	}
}