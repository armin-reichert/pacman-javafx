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
package de.amr.games.pacman.ui.fx.sound;

import java.net.URL;
import java.util.EnumMap;
import java.util.Map;

import de.amr.games.pacman.ui.PacManGameSound;

/**
 * Resource paths for the sounds.
 * 
 * @author Armin Reichert
 */
public class PacManGameSounds {

	private static URL url(String path) {
		return PacManGameSounds.class.getResource(path);
	}

	private static final Map<PacManGameSound, URL> pacMan = new EnumMap<>(PacManGameSound.class);

	static {
		//@formatter:off
		pacMan.put(PacManGameSound.CREDIT,             url("/pacman/sound/credit.mp3"));
		pacMan.put(PacManGameSound.EXTRA_LIFE,         url("/pacman/sound/extend.mp3"));
		pacMan.put(PacManGameSound.GAME_READY,         url("/pacman/sound/game_start.mp3"));
		pacMan.put(PacManGameSound.BONUS_EATEN,        url("/pacman/sound/eat_fruit.mp3"));
		pacMan.put(PacManGameSound.PACMAN_MUNCH,       url("/pacman/sound/munch_1.wav"));
		pacMan.put(PacManGameSound.PACMAN_DEATH,       url("/pacman/sound/pacman_death.wav"));
		pacMan.put(PacManGameSound.PACMAN_POWER,       url("/pacman/sound/power_pellet.mp3"));
		pacMan.put(PacManGameSound.GHOST_EATEN,        url("/pacman/sound/eat_ghost.mp3"));
		pacMan.put(PacManGameSound.GHOST_RETURNING_HOME,         url("/pacman/sound/retreating.mp3"));
		pacMan.put(PacManGameSound.GHOST_SIREN_1,      url("/pacman/sound/siren_1.mp3"));
		pacMan.put(PacManGameSound.GHOST_SIREN_2,      url("/pacman/sound/siren_2.mp3"));
		pacMan.put(PacManGameSound.GHOST_SIREN_3,      url("/pacman/sound/siren_3.mp3"));
		pacMan.put(PacManGameSound.GHOST_SIREN_4,      url("/pacman/sound/siren_4.mp3"));
		pacMan.put(PacManGameSound.INTERMISSION_1,     url("/pacman/sound/intermission.mp3"));
		pacMan.put(PacManGameSound.INTERMISSION_2,     url("/pacman/sound/intermission.mp3"));
		pacMan.put(PacManGameSound.INTERMISSION_3,     url("/pacman/sound/intermission.mp3"));
	}
	
	public static URL pacManSoundURL(PacManGameSound sound) {
		return pacMan.get(sound);
	}

	private static final Map<PacManGameSound, URL> msPacMan = new EnumMap<>(PacManGameSound.class);

	static {
		//@formatter:off
		msPacMan.put(PacManGameSound.CREDIT,           url("/mspacman/sound/Coin Credit.mp3"));
		msPacMan.put(PacManGameSound.EXTRA_LIFE,       url("/mspacman/sound/Extra Life.mp3"));
		msPacMan.put(PacManGameSound.GAME_READY,       url("/mspacman/sound/Start.mp3"));
		msPacMan.put(PacManGameSound.BONUS_EATEN,      url("/mspacman/sound/Fruit.mp3"));
		msPacMan.put(PacManGameSound.PACMAN_MUNCH,     url("/mspacman/sound/Ms. Pac Man Pill.mp3"));
		msPacMan.put(PacManGameSound.PACMAN_DEATH,     url("/mspacman/sound/Died.mp3"));
		msPacMan.put(PacManGameSound.PACMAN_POWER,     url("/mspacman/sound/Scared Ghost.mp3"));
		msPacMan.put(PacManGameSound.GHOST_EATEN,      url("/mspacman/sound/Ghost.mp3"));
		msPacMan.put(PacManGameSound.GHOST_RETURNING_HOME,       url("/mspacman/sound/Ghost Eyes.mp3"));
		msPacMan.put(PacManGameSound.GHOST_SIREN_1,    url("/mspacman/sound/Ghost Noise 1.mp3"));
		msPacMan.put(PacManGameSound.GHOST_SIREN_2,    url("/mspacman/sound/Ghost Noise 2.mp3"));
		msPacMan.put(PacManGameSound.GHOST_SIREN_3,    url("/mspacman/sound/Ghost Noise 3.mp3"));
		msPacMan.put(PacManGameSound.GHOST_SIREN_4,    url("/mspacman/sound/Ghost Noise 4.mp3"));
		msPacMan.put(PacManGameSound.INTERMISSION_1,   url("/mspacman/sound/They Meet Act 1.mp3"));
		msPacMan.put(PacManGameSound.INTERMISSION_2,   url("/mspacman/sound/The Chase Act 2.mp3"));
		msPacMan.put(PacManGameSound.INTERMISSION_3,   url("/mspacman/sound/Junior Act 3.mp3"));
		//@formatter:on
	}

	public static URL msPacManSoundURL(PacManGameSound sound) {
		return msPacMan.get(sound);
	}
}