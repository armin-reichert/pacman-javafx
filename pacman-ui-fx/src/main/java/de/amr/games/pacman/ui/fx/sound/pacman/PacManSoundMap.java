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

import de.amr.games.pacman.ui.fx.sound.common.SoundClip;

/**
 * @author Armin Reichert
 */
public class PacManSoundMap {

	public static final Object[][] DATA = { //
			{ SoundClip.BONUS_EATEN, "sound/pacman/eat_fruit.mp3", 1.0 }, //
			{ SoundClip.CREDIT, "sound/pacman/credit.wav", 1.0 }, //
			{ SoundClip.EXTRA_LIFE, "sound/pacman/extend.mp3", 1.0 }, //
			{ SoundClip.GAME_READY, "sound/pacman/game_start.mp3", 1.0 }, //
			{ SoundClip.GHOST_EATEN, "sound/pacman/eat_ghost.mp3", 1.0 }, //
			{ SoundClip.GHOST_RETURNING, "sound/pacman/retreating.mp3", 1.0 }, //
			{ SoundClip.INTERMISSION_1, "sound/pacman/intermission.mp3", 1.0 }, //
			{ SoundClip.PACMAN_DEATH, "sound/pacman/pacman_death.wav", 0.5 }, //
			{ SoundClip.PACMAN_MUNCH, "sound/pacman/munch_1.wav", 1.0 }, //
			{ SoundClip.PACMAN_POWER, "sound/pacman/power_pellet.mp3", 1.0 }, //
			{ SoundClip.SIREN_1, "sound/pacman/siren_1.mp3", 0.5 }, //
			{ SoundClip.SIREN_2, "sound/pacman/siren_2.mp3", 0.5 }, //
			{ SoundClip.SIREN_3, "sound/pacman/siren_3.mp3", 0.5 }, //
			{ SoundClip.SIREN_4, "sound/pacman/siren_4.mp3", 0.5 }, //
	};
}