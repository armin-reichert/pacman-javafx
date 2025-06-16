/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.ui.input.Joypad;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.ui.sound.PacManGames_SoundManager;
import de.amr.pacmanfx.uilib.GameClock;
import javafx.application.Application;
import org.tinylog.Logger;

import java.io.File;

import static java.util.Objects.requireNonNull;

/**
 * Global environment accessible from every class in the UI layer.
 */
public class PacManGames_Env {

    // must be created before UI is instantiated
    private static final Keyboard theKeyboard = new Keyboard();
    private static final Joypad theJoypad = new Joypad(theKeyboard);

    // package-private to grant access from UI builder
    static PacManGames_Assets theAssets;
    static GameClock theClock;
    static PacManGames_SoundManager theSound;
    static PacManGames_UI theUI;

    public static PacManGames_Assets theAssets() { return theAssets; }
    public static GameClock theClock() { return theClock; }
    public static Keyboard theKeyboard() { return theKeyboard; }
    public static Joypad theJoypad() { return theJoypad; }
    public static PacManGames_SoundManager theSound() { return theSound; }
    public static PacManGames_UI theUI() { return theUI; }

}