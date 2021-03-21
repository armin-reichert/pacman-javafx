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
import javafx.scene.paint.Color;

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

	public static Color getFoodColor(int mazeNumber) {
		switch (mazeNumber) {
		case 1:
			return Color.rgb(222, 222, 255);
		case 2:
			return Color.rgb(255, 255, 0);
		case 3:
			return Color.rgb(255, 0, 0);
		case 4:
			return Color.rgb(222, 222, 255);
		case 5:
			return Color.rgb(0, 255, 255);
		case 6:
			return Color.rgb(222, 222, 255);
		default:
			return Color.WHITE;
		}
	}

	public static Color getMazeWallColor(int mazeNumber) {
		switch (mazeNumber) {
		case 1:
			return Color.rgb(255, 183, 174);
		case 2:
			return Color.rgb(71, 183, 255);
		case 3:
			return Color.rgb(222, 151, 81);
		case 4:
			return Color.rgb(33, 33, 255);
		case 5:
			return Color.rgb(255, 183, 255);
		case 6:
			return Color.rgb(255, 183, 174);
		default:
			return Color.WHITE;
		}
	}

	public static Color getMazeWallBorderColor(int mazeIndex) {
		switch (mazeIndex) {
		case 0:
			return Color.rgb(255, 0, 0);
		case 1:
			return Color.rgb(222, 222, 255);
		case 2:
			return Color.rgb(222, 222, 255);
		case 3:
			return Color.rgb(255, 183, 81);
		case 4:
			return Color.rgb(255, 255, 0);
		case 5:
			return Color.rgb(255, 0, 0);
		default:
			return Color.WHITE;
		}
	}
}
