package de.amr.games.pacman.ui.fx.common.scene2d;

import static de.amr.games.pacman.model.common.GameType.MS_PACMAN;
import static de.amr.games.pacman.model.common.GameType.PACMAN;

import java.util.EnumMap;

import de.amr.games.pacman.model.common.GameType;
import de.amr.games.pacman.ui.fx.rendering.PacManGameRendering2D;
import de.amr.games.pacman.ui.fx.rendering.standard.MsPacMan_StandardRendering;
import de.amr.games.pacman.ui.fx.rendering.standard.PacMan_StandardRendering;
import de.amr.games.pacman.ui.fx.sound.PacManGameSoundManager;
import de.amr.games.pacman.ui.fx.sound.PacManGameSounds;
import de.amr.games.pacman.ui.sound.SoundManager;

public class Assets2D {

	public static final EnumMap<GameType, PacManGameRendering2D> RENDERING_2D = new EnumMap<>(GameType.class);

	static {
		RENDERING_2D.put(MS_PACMAN, new MsPacMan_StandardRendering());
		RENDERING_2D.put(PACMAN, new PacMan_StandardRendering());
	}

	public static final EnumMap<GameType, SoundManager> SOUND = new EnumMap<>(GameType.class);

	static {
		SOUND.put(MS_PACMAN, new PacManGameSoundManager(PacManGameSounds::msPacManSoundURL));
		SOUND.put(PACMAN, new PacManGameSoundManager(PacManGameSounds::mrPacManSoundURL));
	}

}
