/*
MIT License

Copyright (c) 2021 Armin Reichert

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

import static de.amr.games.pacman.lib.Logging.log;

import de.amr.games.pacman.ui.GameSounds;
import de.amr.games.pacman.ui.fx.sound.SoundMap;

/**
 * Ms. Pac-Man game sounds.
 * 
 * @author Armin Reichert
 */
public class Sounds_MsPacMan extends SoundMap {

	public Sounds_MsPacMan() {
		//@formatter:off
		put(GameSounds.CREDIT,          "/mspacman/sound/Coin Credit.mp3");
		put(GameSounds.EXTRA_LIFE,      "/mspacman/sound/Extra Life.mp3");
		put(GameSounds.GAME_READY,      "/mspacman/sound/Start.mp3");
		put(GameSounds.BONUS_EATEN,     "/mspacman/sound/Fruit.mp3");
		put(GameSounds.PACMAN_MUNCH,    "/mspacman/sound/Ms. Pac Man Pill.mp3");
		put(GameSounds.PACMAN_DEATH,    "/mspacman/sound/Died.mp3");
		put(GameSounds.PACMAN_POWER,    "/mspacman/sound/Scared Ghost.mp3");
		put(GameSounds.GHOST_EATEN,     "/mspacman/sound/Ghost.mp3");
		put(GameSounds.GHOST_RETURNING, "/mspacman/sound/Ghost Eyes.mp3");
		put(GameSounds.SIREN_1,         "/mspacman/sound/Ghost Noise 1.mp3");
		put(GameSounds.SIREN_2,         "/mspacman/sound/Ghost Noise 2.mp3");
		put(GameSounds.SIREN_3,         "/mspacman/sound/Ghost Noise 3.mp3");
		put(GameSounds.SIREN_4,         "/mspacman/sound/Ghost Noise 4.mp3");
		put(GameSounds.INTERMISSION_1,  "/mspacman/sound/They Meet Act 1.mp3");
		put(GameSounds.INTERMISSION_2,  "/mspacman/sound/The Chase Act 2.mp3");
		put(GameSounds.INTERMISSION_3,  "/mspacman/sound/Junior Act 3.mp3");
		//@formatter:on
		log("Ms. Pac-Man sounds loaded");
	}
}