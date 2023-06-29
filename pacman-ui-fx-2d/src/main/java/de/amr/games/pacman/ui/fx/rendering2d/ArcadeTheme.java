/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.ui.fx.rendering2d.mspacman.SpritesheetMsPacManGame;
import de.amr.games.pacman.ui.fx.rendering2d.pacman.SpritesheetPacManGame;
import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.Theme;
import javafx.scene.paint.Color;

/**
 * @author Armin Reichert
 */
public class ArcadeTheme extends Theme {

	public static final Color RED    = Color.rgb(255, 0, 0);
	public static final Color YELLOW = Color.rgb(255, 255, 0);
	public static final Color PINK   = Color.rgb(252, 181, 255);
	public static final Color CYAN   = Color.rgb(0, 255, 255);
	public static final Color ORANGE = Color.rgb(251, 190, 88);
	public static final Color BLACK  = Color.rgb(0, 0, 0);
	public static final Color BLUE   = Color.rgb(33, 33, 255);
	public static final Color PALE   = Color.rgb(222, 222, 255);
	public static final Color ROSE   = Color.rgb(252, 187, 179);

	public ArcadeTheme(ResourceManager rm) {

		//
		// Ms. Pac-Man game
		//
		
		set("mspacman.startpage.image",        rm.image("graphics/mspacman/wallpaper-midway.png"));
		set("mspacman.helpButton.icon",        rm.image("graphics/icons/help-red-64.png"));
		
		set("mspacman.spritesheet",            new SpritesheetMsPacManGame(rm.image("graphics/mspacman/sprites.png")));
		set("mspacman.icon",                   rm.image("graphics/icons/mspacman.png"));
		set("mspacman.logo.midway",            rm.image("graphics/mspacman/midway.png"));
		set("mspacman.flashingMazes",          rm.image("graphics/mspacman/mazes-flashing.png"));

		set("mspacman.audio.bonus_eaten",      rm.audioClip("sound/mspacman/Fruit.mp3"));
		set("mspacman.audio.credit",           rm.audioClip("sound/mspacman/Credit.mp3"));
		set("mspacman.audio.extra_life",       rm.audioClip("sound/mspacman/ExtraLife.mp3"));
		set("mspacman.audio.game_ready",       rm.audioClip("sound/mspacman/Start.mp3"));
		set("mspacman.audio.game_over",        rm.audioClip("sound/common/game-over.mp3"));
		set("mspacman.audio.ghost_eaten",      rm.audioClip("sound/mspacman/Ghost.mp3"));
		set("mspacman.audio.ghost_returning",  rm.audioClip("sound/mspacman/GhostEyes.mp3"));
		set("mspacman.audio.intermission.1",   rm.audioClip("sound/mspacman/Act1TheyMeet.mp3"));
		set("mspacman.audio.intermission.2",   rm.audioClip("sound/mspacman/Act2TheChase.mp3"));
		set("mspacman.audio.intermission.3",   rm.audioClip("sound/mspacman/Act3Junior.mp3"));
		set("mspacman.audio.level_complete",   rm.audioClip("sound/common/level-complete.mp3"));
		set("mspacman.audio.pacman_death",     rm.audioClip("sound/mspacman/Died.mp3"));
		set("mspacman.audio.pacman_munch",     rm.audioClip("sound/mspacman/Pill.wav"));
		set("mspacman.audio.pacman_power",     rm.audioClip("sound/mspacman/ScaredGhost.mp3"));
		set("mspacman.audio.siren.1",          rm.audioClip("sound/mspacman/GhostNoise1.wav"));
		set("mspacman.audio.siren.2",          rm.audioClip("sound/mspacman/GhostNoise1.wav"));// TODO
		set("mspacman.audio.siren.3",          rm.audioClip("sound/mspacman/GhostNoise1.wav"));// TODO
		set("mspacman.audio.siren.4",          rm.audioClip("sound/mspacman/GhostNoise1.wav"));// TODO
		set("mspacman.audio.sweep",            rm.audioClip("sound/common/sweep.mp3"));

		//
		// Pac-Man game
		//
		
		set("pacman.startpage.image",          rm.image("graphics/pacman/1980-Flyer-USA-Midway-front.jpg"));
		set("pacman.helpButton.icon",          rm.image("graphics/icons/help-blue-64.png"));
		
		set("pacman.spritesheet",              new SpritesheetPacManGame(rm.image("graphics/pacman/sprites.png")));
		set("pacman.icon",                     rm.image("graphics/icons/pacman.png"));
		set("pacman.flashingMaze",             rm.image("graphics/pacman/maze_empty_flashing.png"));
		set("pacman.fullMaze",                 rm.image("graphics/pacman/maze_full.png"));
		set("pacman.emptyMaze",                rm.image("graphics/pacman/maze_empty.png"));
	  
		set("pacman.audio.bonus_eaten",        rm.audioClip("sound/pacman/eat_fruit.mp3"));
		set("pacman.audio.credit",             rm.audioClip("sound/pacman/credit.wav"));
		set("pacman.audio.extra_life",         rm.audioClip("sound/pacman/extend.mp3"));
		set("pacman.audio.game_ready",         rm.audioClip("sound/pacman/game_start.mp3"));
		set("pacman.audio.game_over",          rm.audioClip("sound/common/game-over.mp3"));
		set("pacman.audio.ghost_eaten",        rm.audioClip("sound/pacman/eat_ghost.mp3"));
		set("pacman.audio.ghost_returning",    rm.audioClip("sound/pacman/retreating.mp3"));
		set("pacman.audio.intermission",       rm.audioClip("sound/pacman/intermission.mp3"));
		set("pacman.audio.level_complete",     rm.audioClip("sound/common/level-complete.mp3"));
		set("pacman.audio.pacman_death",       rm.audioClip("sound/pacman/pacman_death.wav"));
		set("pacman.audio.pacman_munch",       rm.audioClip("sound/pacman/doublemunch.wav"));
		set("pacman.audio.pacman_power",       rm.audioClip("sound/pacman/ghost-turn-to-blue.mp3"));
		set("pacman.audio.siren.1",            rm.audioClip("sound/pacman/siren_1.mp3"));
		set("pacman.audio.siren.2",            rm.audioClip("sound/pacman/siren_2.mp3"));
		set("pacman.audio.siren.3",            rm.audioClip("sound/pacman/siren_3.mp3"));
		set("pacman.audio.siren.4",            rm.audioClip("sound/pacman/siren_4.mp3"));
		set("pacman.audio.sweep",              rm.audioClip("sound/common/sweep.mp3"));

		// Common to both games

		set("startpage.button.bgColor",        Color.rgb(0, 155, 252, 0.8));
		set("startpage.button.color",          Color.WHITE);
		set("startpage.button.font",           rm.font("fonts/emulogic.ttf", 30));
		
		set("wallpaper.background",            rm.imageBackground("graphics/pacman_wallpaper.png"));
		set("wallpaper.color",                 Color.rgb(72, 78, 135));

		set("font.arcade",                     rm.font("fonts/emulogic.ttf", 8));
		set("font.handwriting",                rm.font("fonts/Molle-Italic.ttf", 9));
		set("font.monospaced",                 rm.font("fonts/Inconsolata_Condensed-Bold.ttf", 12));
		
		set("voice.explain",                   rm.audioClip("sound/voice/press-key.mp3"));
		set("voice.autopilot.off",             rm.audioClip("sound/voice/autopilot-off.mp3"));
		set("voice.autopilot.on",              rm.audioClip("sound/voice/autopilot-on.mp3"));
		set("voice.immunity.off",              rm.audioClip("sound/voice/immunity-off.mp3"));
		set("voice.immunity.on",               rm.audioClip("sound/voice/immunity-on.mp3"));
	}
}