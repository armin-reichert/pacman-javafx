/*
MIT License

Copyright (c) 2023 Armin Reichert

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

package de.amr.games.pacman.ui.fx.app;

import java.util.ResourceBundle;

import org.tinylog.Logger;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.IllegalGameVariantException;
import de.amr.games.pacman.ui.fx.rendering2d.Spritesheet;
import de.amr.games.pacman.ui.fx.sound.AudioClipID;
import de.amr.games.pacman.ui.fx.sound.GameSounds;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.media.AudioClip;
import javafx.scene.text.Font;

public class Game2dAssets extends ResourceManager {

	public Image iconPacManGame;
	public Spritesheet spritesheetPacManGame;
	public Image fullMazePacManGame;
	public Image emptyMazePacManGame;
	public Image flashingMazePacManGame;

	public Image iconMsPacManGame;
	public Spritesheet spritesheetMsPacManGame;
	public Image logoMsPacManGame;
	public Image flashingMazesMsPacManGame;

	public Font arcadeFont;
	public Font handwritingFont;

	public ResourceBundle messages;

	public AudioClip voiceExplainKeys;
	public AudioClip voiceAutopilotOff;
	public AudioClip voiceAutopilotOn;
	public AudioClip voiceImmunityOff;
	public AudioClip voiceImmunityOn;

	//@formatter:off
	private static Object[][] audioClipsMsPacman = { 
		{ AudioClipID.BONUS_EATEN,     "sound/mspacman/Fruit.mp3", 1.0 }, 
		{ AudioClipID.CREDIT,          "sound/mspacman/Credit.mp3", 1.0 }, 
		{ AudioClipID.EXTRA_LIFE,      "sound/mspacman/ExtraLife.mp3", 1.0 }, 
		{ AudioClipID.GAME_READY,      "sound/mspacman/Start.mp3", 1.0 }, 
		{ AudioClipID.GAME_OVER,       "sound/common/game-over.mp3", 1.0 }, 
		{ AudioClipID.GHOST_EATEN,     "sound/mspacman/Ghost.mp3", 1.0 }, 
		{ AudioClipID.GHOST_RETURNING, "sound/mspacman/GhostEyes.mp3", 1.0 }, 
		{ AudioClipID.INTERMISSION_1,  "sound/mspacman/Act1TheyMeet.mp3", 1.0 }, 
		{ AudioClipID.INTERMISSION_2,  "sound/mspacman/Act2TheChase.mp3", 1.0 }, 
		{ AudioClipID.INTERMISSION_3,  "sound/mspacman/Act3Junior.mp3", 1.0 }, 
		{ AudioClipID.LEVEL_COMPLETE,  "sound/common/level-complete.mp3", 1.0 }, 
		{ AudioClipID.PACMAN_DEATH,    "sound/mspacman/Died.mp3", 1.0 }, 
		{ AudioClipID.PACMAN_MUNCH,    "sound/mspacman/Pill.wav", 1.0 }, 
		{ AudioClipID.PACMAN_POWER,    "sound/mspacman/ScaredGhost.mp3", 1.0 }, 
		{ AudioClipID.SIREN_1,         "sound/mspacman/GhostNoise1.wav", 1.0 }, 
		{ AudioClipID.SIREN_2,         "sound/mspacman/GhostNoise1.wav", 1.0 }, // TODO
		{ AudioClipID.SIREN_3,         "sound/mspacman/GhostNoise1.wav", 1.0 }, // TODO
		{ AudioClipID.SIREN_4,         "sound/mspacman/GhostNoise1.wav", 1.0 }, // TODO
		{ AudioClipID.SWEEP,           "sound/common/sweep.mp3", 1.0 }, 
	};

	private static Object[][] audioClipsPacman = { 
		{ AudioClipID.BONUS_EATEN,     "sound/pacman/eat_fruit.mp3", 1.0 }, 
		{ AudioClipID.CREDIT,          "sound/pacman/credit.wav", 1.0 }, 
		{ AudioClipID.EXTRA_LIFE,      "sound/pacman/extend.mp3", 1.0 }, 
		{ AudioClipID.GAME_READY,      "sound/pacman/game_start.mp3", 1.0 }, 
		{ AudioClipID.GAME_OVER,       "sound/common/game-over.mp3", 1.0 }, 
		{ AudioClipID.GHOST_EATEN,     "sound/pacman/eat_ghost.mp3", 1.0 }, 
		{ AudioClipID.GHOST_RETURNING, "sound/pacman/retreating.mp3", 1.0 }, 
		{ AudioClipID.INTERMISSION_1,  "sound/pacman/intermission.mp3", 1.0 }, 
		{ AudioClipID.LEVEL_COMPLETE,  "sound/common/level-complete.mp3", 1.0 }, 
		{ AudioClipID.PACMAN_DEATH,    "sound/pacman/pacman_death.wav", 1.0 }, 
		{ AudioClipID.PACMAN_MUNCH,    "sound/pacman/doublemunch.wav", 1.0 }, 
		{ AudioClipID.PACMAN_POWER,    "sound/pacman/ghost-turn-to-blue.mp3", 1.0 }, 
		{ AudioClipID.SIREN_1,         "sound/pacman/siren_1.mp3", 1.0 }, 
		{ AudioClipID.SIREN_2,         "sound/pacman/siren_2.mp3", 1.0 }, 
		{ AudioClipID.SIREN_3,         "sound/pacman/siren_3.mp3", 1.0 }, 
		{ AudioClipID.SIREN_4,         "sound/pacman/siren_4.mp3", 1.0 }, 
		{ AudioClipID.SWEEP,           "sound/common/sweep.mp3", 1.0 }, 
	};
	//@formatter:on

	public Background wallpaper2D;

	public GameSounds soundsMsPacMan = new GameSounds();
	public GameSounds soundsPacMan = new GameSounds();

	public Game2dAssets() {
		super("/de/amr/games/pacman/ui/fx/", Game2dAssets.class);
	}

	public void load() {

		long start = System.nanoTime();

		arcadeFont = font("fonts/emulogic.ttf", 8);
		handwritingFont = font("fonts/RockSalt-Regular.ttf", 8);

		wallpaper2D = imageBackground("graphics/pacman_wallpaper_gray.png");

		iconMsPacManGame = image("graphics/icons/mspacman.png");
		spritesheetMsPacManGame = new Spritesheet(image("graphics/mspacman/sprites.png"), 16);
		flashingMazesMsPacManGame = image("graphics/mspacman/mazes-flashing.png");
		logoMsPacManGame = image("graphics/mspacman/midway.png");

		iconPacManGame = image("graphics/icons/pacman.png");
		spritesheetPacManGame = new Spritesheet(image("graphics/pacman/sprites.png"), 16);
		fullMazePacManGame = image("graphics/pacman/maze_full.png");
		emptyMazePacManGame = image("graphics/pacman/maze_empty.png");
		flashingMazePacManGame = image("graphics/pacman/maze_empty_flashing.png");

		// Texts
		messages = ResourceBundle.getBundle("de.amr.games.pacman.ui.fx.texts.messages");

		// Sound
		voiceExplainKeys = audioClip("sound/voice/press-key.mp3");
		voiceAutopilotOff = audioClip("sound/voice/autopilot-off.mp3");
		voiceAutopilotOn = audioClip("sound/voice/autopilot-on.mp3");
		voiceImmunityOff = audioClip("sound/voice/immunity-off.mp3");
		voiceImmunityOn = audioClip("sound/voice/immunity-on.mp3");
		soundsMsPacMan.init(audioClipsMsPacman, false);
		soundsPacMan.init(audioClipsPacman, false);

		Logger.info("Loading assets: {} seconds.", (System.nanoTime() - start) / 1e9f);
	}

	public Font font(Font font, double size) {
		return Font.font(font.getFamily(), size);
	}

	/**
	 * @param variant game variant
	 * @return text displayed in READY state
	 */
	public String randomReadyText(GameVariant variant) {
		return "READY!";
	}

	public GameSounds gameSounds(GameVariant variant) {
		return switch (variant) {
		case MS_PACMAN -> soundsMsPacMan;
		case PACMAN -> soundsPacMan;
		default -> throw new IllegalGameVariantException(variant);
		};
	}
}