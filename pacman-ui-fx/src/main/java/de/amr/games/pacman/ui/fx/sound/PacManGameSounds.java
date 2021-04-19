package de.amr.games.pacman.ui.fx.sound;

import java.net.URL;
import java.util.EnumMap;
import java.util.Map;

import de.amr.games.pacman.ui.PacManGameSound;

public class PacManGameSounds {

	private static final Map<PacManGameSound, URL> pacManSounds = new EnumMap<>(PacManGameSound.class);
	private static final Map<PacManGameSound, URL> msPacManSounds = new EnumMap<>(PacManGameSound.class);

	private static URL url(String path) {
		return PacManGameSounds.class.getResource(path);
	}

	public static URL mrPacManSoundURL(PacManGameSound sound) {
		return pacManSounds.get(sound);
	}

	public static URL msPacManSoundURL(PacManGameSound sound) {
		return msPacManSounds.get(sound);
	}

	static {
		//@formatter:off
		pacManSounds.put(PacManGameSound.CREDIT,             url("/pacman/sound/credit.mp3"));
		pacManSounds.put(PacManGameSound.EXTRA_LIFE,         url("/pacman/sound/extend.mp3"));
		pacManSounds.put(PacManGameSound.GAME_READY,         url("/pacman/sound/game_start.mp3"));
		pacManSounds.put(PacManGameSound.BONUS_EATEN,        url("/pacman/sound/eat_fruit.mp3"));
		pacManSounds.put(PacManGameSound.PACMAN_MUNCH,       url("/pacman/sound/munch_1.wav"));
		pacManSounds.put(PacManGameSound.PACMAN_DEATH,       url("/pacman/sound/death_1.mp3"));
		pacManSounds.put(PacManGameSound.PACMAN_POWER,       url("/pacman/sound/power_pellet.mp3"));
		pacManSounds.put(PacManGameSound.GHOST_EATEN,        url("/pacman/sound/eat_ghost.mp3"));
		pacManSounds.put(PacManGameSound.GHOST_RETURNING_HOME,         url("/pacman/sound/retreating.mp3"));
		pacManSounds.put(PacManGameSound.GHOST_SIREN_1,      url("/pacman/sound/siren_1.mp3"));
		pacManSounds.put(PacManGameSound.GHOST_SIREN_2,      url("/pacman/sound/siren_2.mp3"));
		pacManSounds.put(PacManGameSound.GHOST_SIREN_3,      url("/pacman/sound/siren_3.mp3"));
		pacManSounds.put(PacManGameSound.GHOST_SIREN_4,      url("/pacman/sound/siren_4.mp3"));
		pacManSounds.put(PacManGameSound.INTERMISSION_1,     url("/pacman/sound/intermission.mp3"));
		pacManSounds.put(PacManGameSound.INTERMISSION_2,     url("/pacman/sound/intermission.mp3"));
		pacManSounds.put(PacManGameSound.INTERMISSION_3,     url("/pacman/sound/intermission.mp3"));

		msPacManSounds.put(PacManGameSound.CREDIT,           url("/mspacman/sound/Coin Credit.mp3"));
		msPacManSounds.put(PacManGameSound.EXTRA_LIFE,       url("/mspacman/sound/Extra Life.mp3"));
		msPacManSounds.put(PacManGameSound.GAME_READY,       url("/mspacman/sound/Start.mp3"));
		msPacManSounds.put(PacManGameSound.BONUS_EATEN,      url("/mspacman/sound/Fruit.mp3"));
		msPacManSounds.put(PacManGameSound.PACMAN_MUNCH,     url("/mspacman/sound/Ms. Pac Man Pill.mp3"));
		msPacManSounds.put(PacManGameSound.PACMAN_DEATH,     url("/mspacman/sound/Died.mp3"));
		msPacManSounds.put(PacManGameSound.PACMAN_POWER,     url("/mspacman/sound/Scared Ghost.mp3"));
		msPacManSounds.put(PacManGameSound.GHOST_EATEN,      url("/mspacman/sound/Ghost.mp3"));
		msPacManSounds.put(PacManGameSound.GHOST_RETURNING_HOME,       url("/mspacman/sound/Ghost Eyes.mp3"));
		msPacManSounds.put(PacManGameSound.GHOST_SIREN_1,    url("/mspacman/sound/Ghost Noise 1.mp3"));
		msPacManSounds.put(PacManGameSound.GHOST_SIREN_2,    url("/mspacman/sound/Ghost Noise 2.mp3"));
		msPacManSounds.put(PacManGameSound.GHOST_SIREN_3,    url("/mspacman/sound/Ghost Noise 3.mp3"));
		msPacManSounds.put(PacManGameSound.GHOST_SIREN_4,    url("/mspacman/sound/Ghost Noise 4.mp3"));
		msPacManSounds.put(PacManGameSound.INTERMISSION_1,   url("/mspacman/sound/They Meet Act 1.mp3"));
		msPacManSounds.put(PacManGameSound.INTERMISSION_2,   url("/mspacman/sound/The Chase Act 2.mp3"));
		msPacManSounds.put(PacManGameSound.INTERMISSION_3,   url("/mspacman/sound/Junior Act 3.mp3"));
		//@formatter:on
	}
}