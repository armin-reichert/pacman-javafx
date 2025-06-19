/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.lib.DirectoryWatchdog;
import de.amr.pacmanfx.ui.input.Joypad;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.ui.sound.PacManGames_Sound;
import de.amr.pacmanfx.uilib.GameClock;

/**
 * Global environment accessible from every class in the UI layer.
 */
public class PacManGames {
    // must be created before UI is instantiated
    private static final Keyboard theKeyboard = new Keyboard();
    private static final Joypad theJoypad = new Joypad(theKeyboard);

    // package-private to grant access from UI builder
    static PacManGames_UI_Impl theUI;

    public static PacManGames_Assets theAssets() { return PacManGames_UI_Impl.ASSETS; }
    public static GameClock          theClock() { return PacManGames_UI_Impl.GAME_CLOCK; }
    public static Joypad             theJoypad() { return theJoypad; }
    public static Keyboard           theKeyboard() { return theKeyboard; }
    public static PacManGames_Sound  theSound() { return PacManGames_UI_Impl.SOUND_MANAGER; }
    public static PacManGames_UI     theUI() { return theUI; }
    public static DirectoryWatchdog  theWatchdog() { return PacManGames_UI_Impl.WATCHDOG; }
}