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
package de.amr.games.pacman.ui.fx.sound.pacman;

import de.amr.games.pacman.ui.GameSounds;
import de.amr.games.pacman.ui.fx.sound.SoundManager;

/**
 * Pac-Man game sounds.
 * 
 * @author Armin Reichert
 */
public class Sounds_PacMan extends SoundManager {

	private static Sounds_PacMan it;

	public static Sounds_PacMan get() {
		if (it == null) {
			it = new Sounds_PacMan();
		}
		return it;
	}

	private Sounds_PacMan() {
		//@formatter:off
		put(GameSounds.CREDIT,          "/pacman/sound/credit.mp3");
		put(GameSounds.EXTRA_LIFE,      "/pacman/sound/extend.mp3");
		put(GameSounds.GAME_READY,      "/pacman/sound/game_start.mp3");
		put(GameSounds.BONUS_EATEN,     "/pacman/sound/eat_fruit.mp3");
		put(GameSounds.PACMAN_MUNCH,    "/pacman/sound/munch_1.wav");
		put(GameSounds.PACMAN_DEATH,    "/pacman/sound/pacman_death.wav");
		put(GameSounds.PACMAN_POWER,    "/pacman/sound/power_pellet.mp3");
		put(GameSounds.GHOST_EATEN,     "/pacman/sound/eat_ghost.mp3");
		put(GameSounds.GHOST_RETURNING, "/pacman/sound/retreating.mp3");
		put(GameSounds.SIREN_1,         "/pacman/sound/siren_1.mp3");
		put(GameSounds.SIREN_2,         "/pacman/sound/siren_2.mp3");
		put(GameSounds.SIREN_3,         "/pacman/sound/siren_3.mp3");
		put(GameSounds.SIREN_4,         "/pacman/sound/siren_4.mp3");
		put(GameSounds.INTERMISSION_1,  "/pacman/sound/intermission.mp3");
		put(GameSounds.INTERMISSION_2,  "/pacman/sound/intermission.mp3");
		put(GameSounds.INTERMISSION_3,  "/pacman/sound/intermission.mp3");
		//@formatter:on
	}
}