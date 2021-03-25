package de.amr.games.pacman.ui.fx.sound;

import static de.amr.games.pacman.model.common.GameVariant.MS_PACMAN;
import static de.amr.games.pacman.model.common.GameVariant.PACMAN;

import java.util.EnumMap;

import de.amr.games.pacman.model.common.GameVariant;
import de.amr.games.pacman.ui.sound.SoundManager;

public class SoundAssets {

	public static final EnumMap<GameVariant, SoundManager> SOUND = new EnumMap<>(GameVariant.class);

	static {
		SOUND.put(MS_PACMAN, new PacManGameSoundManager(PacManGameSounds::msPacManSoundURL));
		SOUND.put(PACMAN, new PacManGameSoundManager(PacManGameSounds::mrPacManSoundURL));
	}

	public static SoundManager get(GameVariant gameVariant) {
		return SOUND.get(gameVariant);
	}

}
