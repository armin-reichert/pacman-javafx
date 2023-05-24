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

package de.amr.games.pacman.ui.fx.rendering2d;

import de.amr.games.pacman.ui.fx.util.ResourceManager;
import de.amr.games.pacman.ui.fx.util.Spritesheet;
import javafx.scene.paint.Color;

/**
 * @author Armin Reichert
 */
public class ArcadeTheme extends Theme {

	//@formatter:off
	public static final Color RED = Color.rgb(255, 0, 0);
	public static final Color YELLOW = Color.rgb(255, 255, 0);
	public static final Color PINK = Color.rgb(252, 181, 255);
	public static final Color CYAN = Color.rgb(0, 255, 255);
	public static final Color ORANGE = Color.rgb(251, 190, 88);
	public static final Color BLACK = Color.rgb(0, 0, 0);
	public static final Color BLUE = Color.rgb(33, 33, 255);
	public static final Color PALE = Color.rgb(222, 222, 255);
	public static final Color ROSE = Color.rgb(252, 187, 179);

	public ArcadeTheme(ResourceManager rm) {
	  
		// Ms. Pac-Man game
		
		set("mspacman.icon",                   rm.image("graphics/icons/mspacman.png"));
		set("mspacman.logo.midway",            rm.image("graphics/mspacman/midway.png"));
		set("mspacman.spritesheet",            new Spritesheet(rm.image("graphics/mspacman/sprites.png"), 16));
		set("mspacman.flashingMazes",          rm.image("graphics/mspacman/mazes-flashing.png"));

		add("mspacman.maze.foodColor",         Color.rgb(222, 222, 255));
		add("mspacman.maze.foodColor",         Color.rgb(255, 255, 0));
		add("mspacman.maze.foodColor",         Color.rgb(255, 0, 0));
		add("mspacman.maze.foodColor",         Color.rgb(222, 222, 255));
		add("mspacman.maze.foodColor",         Color.rgb(0, 255, 255));
		add("mspacman.maze.foodColor",         Color.rgb(222, 222, 255));

		add("mspacman.maze.wallBaseColor",     Color.rgb(255,   0,   0));
		add("mspacman.maze.wallBaseColor",     Color.rgb(222, 222, 255));
		add("mspacman.maze.wallBaseColor",     Color.rgb(222, 222, 255));
		add("mspacman.maze.wallBaseColor",     Color.rgb(255, 183,  81));
		add("mspacman.maze.wallBaseColor",     Color.rgb(255, 255,   0));
		add("mspacman.maze.wallBaseColor",     Color.rgb(255,   0,   0));

		add("mspacman.maze.wallTopColor",      Color.rgb(255, 183, 174));
		add("mspacman.maze.wallTopColor",      Color.rgb( 71, 183, 255));
		add("mspacman.maze.wallTopColor",      Color.rgb(222, 151,  81));
		add("mspacman.maze.wallTopColor",      Color.rgb(222, 151,  81));
		add("mspacman.maze.wallTopColor",      Color.rgb(222, 151,  81));
		add("mspacman.maze.wallTopColor",      Color.rgb(222, 151,  81));

		set("mspacman.color.head",             Color.rgb(255, 255, 0));
		set("mspacman.color.palate",           Color.rgb(191, 79, 61));
		set("mspacman.color.eyes",             Color.rgb(33, 33, 33));
		set("mspacman.color.boobs",            Color.rgb(255, 255, 0).deriveColor(0, 1.0, 0.96, 1.0));
		set("mspacman.color.hairbow",          Color.rgb(255, 0, 0));
		set("mspacman.color.hairbow.pearls",   Color.rgb(33, 33, 255));
		
		set("mspacman.maze.doorColor",         Color.rgb(255, 183, 255));
		
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

		// Pac-Man game
		
		set("pacman.icon",                     rm.image("graphics/icons/pacman.png"));
		set("pacman.spritesheet",              new Spritesheet(rm.image("graphics/pacman/sprites.png"), 16));
		set("pacman.flashingMaze",             rm.image("graphics/pacman/maze_empty_flashing.png"));
		set("pacman.fullMaze",                 rm.image("graphics/pacman/maze_full.png"));
		set("pacman.emptyMaze",                rm.image("graphics/pacman/maze_empty.png"));
	  
		set("pacman.maze.foodColor",           Color.rgb(254, 189, 180));
		set("pacman.maze.wallBaseColor",       Color.rgb(33, 33, 255).brighter());
		set("pacman.maze.wallTopColor",        Color.rgb(33, 33, 255).darker());
		set("pacman.maze.doorColor",	         Color.rgb(252, 181, 255));
		
		set("pacman.color.head",               Color.rgb(255, 255, 0));
		set("pacman.color.palate",             Color.rgb(191, 79, 61));
		set("pacman.color.eyes",               Color.rgb(33, 33, 33));
		
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

		// Common to both games

		set("wallpaper.background",            rm.imageBackground("graphics/pacman_wallpaper.png"));
		set("wallpaper.color",                 Color.rgb(72, 78, 135));

		set("font.arcade",                     rm.font("fonts/emulogic.ttf", 8));
		set("font.handwriting",                rm.font("fonts/RockSalt-Regular.ttf", 9));
		set("font.monospaced",                 rm.font("fonts/Inconsolata_Condensed-Bold.ttf", 12));
		
		set("ghost.0.color.normal.dress",      RED);
		set("ghost.0.color.normal.eyeballs",   PALE);
		set("ghost.0.color.normal.pupils",     BLUE);

		set("ghost.1.color.normal.dress",      PINK);
		set("ghost.1.color.normal.eyeballs",   PALE);
		set("ghost.1.color.normal.pupils",     BLUE);
		
		set("ghost.2.color.normal.dress",      CYAN);
		set("ghost.2.color.normal.eyeballs",   PALE);
		set("ghost.2.color.normal.pupils",     BLUE);
		
		set("ghost.3.color.normal.dress",      ORANGE);
		set("ghost.3.color.normal.eyeballs",   PALE);
		set("ghost.3.color.normal.pupils",     BLUE);
		
		set("ghost.color.frightened.dress",    BLUE);
		set("ghost.color.frightened.eyeballs", ROSE);
		set("ghost.color.frightened.pupils",   ROSE);
		
		set("ghost.color.flashing.dress",      PALE);
		set("ghost.color.flashing.eyeballs",   ROSE);
		set("ghost.color.flashing.pupils",     RED);
		
		set("audio.sweep",                     rm.audioClip("sound/common/sweep.mp3"));

	}
	//@formatter:on
}