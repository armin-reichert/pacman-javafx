/*
MIT License

Copyright (c) 2022 Armin Reichert

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

package de.amr.games.pacman.ui.fx.sound.mspacman;

import de.amr.games.pacman.model.common.GameSound;
import de.amr.games.pacman.ui.fx.sound.AbstractGameSounds;

/**
 * @author Armin Reichert
 *
 */
public class MsPacManGameSounds extends AbstractGameSounds {

	public MsPacManGameSounds() {
		//@formatter:off
		add(GameSound.BONUS_EATEN,     "sound/mspacman/Fruit.mp3");
		add(GameSound.CREDIT,          "sound/mspacman/Coin Credit.mp3");
		add(GameSound.EXTRA_LIFE,      "sound/mspacman/Extra Life.mp3");
		add(GameSound.GAME_READY,      "sound/mspacman/Start.mp3");
		add(GameSound.GHOST_EATEN,     "sound/mspacman/Ghost.mp3");
		add(GameSound.GHOST_RETURNING, "sound/mspacman/Ghost Eyes.mp3");
		add(GameSound.INTERMISSION_1,  "sound/mspacman/They Meet Act 1.mp3");
		add(GameSound.INTERMISSION_2,  "sound/mspacman/The Chase Act 2.mp3");
		add(GameSound.INTERMISSION_3,  "sound/mspacman/Junior Act 3.mp3");
		add(GameSound.PACMAN_MUNCH,    "sound/mspacman/Ms. Pac Man Pill.mp3");
		add(GameSound.PACMAN_DEATH,    "sound/mspacman/Died.mp3");
		add(GameSound.PACMAN_POWER,    "sound/mspacman/Scared Ghost.mp3");
		add(GameSound.SIREN_1,         "sound/mspacman/Ghost Noise 1.mp3");
		add(GameSound.SIREN_2,         "sound/mspacman/Ghost Noise 2.mp3");
		add(GameSound.SIREN_3,         "sound/mspacman/Ghost Noise 3.mp3");
		add(GameSound.SIREN_4,         "sound/mspacman/Ghost Noise 4.mp3");
		//@formatter:on
	}
}
