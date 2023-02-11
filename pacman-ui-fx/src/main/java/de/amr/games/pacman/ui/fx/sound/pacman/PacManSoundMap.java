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

package de.amr.games.pacman.ui.fx.sound.pacman;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import de.amr.games.pacman.ui.fx.sound.common.GameSound;

/**
 * @author Armin Reichert
 */
public class PacManSoundMap {

	private static final Map<GameSound, String> MAP = new EnumMap<>(GameSound.class);

	static {
		//@formatter:off
		MAP.put(GameSound.BONUS_EATEN,        "sound/pacman/eat_fruit.mp3");
		MAP.put(GameSound.CREDIT,             "sound/pacman/credit.wav");
		MAP.put(GameSound.EXTRA_LIFE,         "sound/pacman/extend.mp3");
		MAP.put(GameSound.GAME_READY,         "sound/pacman/game_start.mp3");
		MAP.put(GameSound.GHOST_EATEN,        "sound/pacman/eat_ghost.mp3");
		MAP.put(GameSound.GHOST_RETURNING,    "sound/pacman/retreating.mp3");
		MAP.put(GameSound.INTERMISSION_1,     "sound/pacman/intermission.mp3");
		MAP.put(GameSound.PACMAN_DEATH,       "sound/pacman/pacman_death.wav");
		MAP.put(GameSound.PACMAN_MUNCH,       "sound/pacman/munch_1.wav");
		MAP.put(GameSound.PACMAN_POWER,       "sound/pacman/power_pellet.mp3");
		MAP.put(GameSound.SIREN_1,            "sound/pacman/siren_1.mp3");
		MAP.put(GameSound.SIREN_2,            "sound/pacman/siren_2.mp3");
		MAP.put(GameSound.SIREN_3,            "sound/pacman/siren_3.mp3");
		MAP.put(GameSound.SIREN_4,            "sound/pacman/siren_4.mp3");
		//@formatter:on
	}

	public static Map<GameSound, String> map() {
		return Collections.unmodifiableMap(MAP);
	}
}