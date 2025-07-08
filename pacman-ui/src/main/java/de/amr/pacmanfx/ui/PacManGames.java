/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.lib.DirectoryWatchdog;
import de.amr.pacmanfx.ui.input.Joypad;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.GameClock;

public interface PacManGames {
    static PacManGames_Assets theAssets()   { return PacManGames_UI_Impl.ASSETS; }
    static GameClock          theClock()    { return PacManGames_UI_Impl.GAME_CLOCK; }
    static Joypad             theJoypad()   { return PacManGames_UI_Impl.JOYPAD; }
    static Keyboard           theKeyboard() { return PacManGames_UI_Impl.KEYBOARD; }
    static SoundManager       theSound()    { return PacManGames_UI_Impl.SOUND_MANAGER; }
    static PacManGames_UI     theUI()       { return PacManGames_UI_Impl.THE_ONE; }
    static DirectoryWatchdog  theWatchdog() { return PacManGames_UI_Impl.WATCHDOG; }
}