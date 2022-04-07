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

import static de.amr.games.pacman.lib.Logging.log;

import de.amr.games.pacman.ui.GameSound;
import de.amr.games.pacman.ui.fx.sound.SoundMap;

/**
 * Pac-Man game sounds.
 * 
 * @author Armin Reichert
 */
public class Sounds_PacMan extends SoundMap {

	public Sounds_PacMan() {
		//@formatter:off
		put(GameSound.CREDIT,          "/pacman/sound/credit.mp3");
		put(GameSound.EXTRA_LIFE,      "/pacman/sound/extend.mp3");
		put(GameSound.GAME_READY,      "/pacman/sound/game_start.mp3");
		put(GameSound.BONUS_EATEN,     "/pacman/sound/eat_fruit.mp3");
		put(GameSound.PACMAN_MUNCH,    "/pacman/sound/munch_1.wav");
		put(GameSound.PACMAN_DEATH,    "/pacman/sound/pacman_death.wav");
		put(GameSound.PACMAN_POWER,    "/pacman/sound/power_pellet.mp3");
		put(GameSound.GHOST_EATEN,     "/pacman/sound/eat_ghost.mp3");
		put(GameSound.GHOST_RETURNING, "/pacman/sound/retreating.mp3");
		put(GameSound.SIREN_1,         "/pacman/sound/siren_1.mp3");
		put(GameSound.SIREN_2,         "/pacman/sound/siren_2.mp3");
		put(GameSound.SIREN_3,         "/pacman/sound/siren_3.mp3");
		put(GameSound.SIREN_4,         "/pacman/sound/siren_4.mp3");
		put(GameSound.INTERMISSION_1,  "/pacman/sound/intermission.mp3");
		put(GameSound.INTERMISSION_2,  "/pacman/sound/intermission.mp3");
		put(GameSound.INTERMISSION_3,  "/pacman/sound/intermission.mp3");
		//@formatter:on
		log("Pac-Man sounds loaded");

	}
}