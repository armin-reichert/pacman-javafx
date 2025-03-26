/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui;

import de.amr.games.pacman.ui.input.Keyboard;
import de.amr.games.pacman.ui.sound.GameSound;
import de.amr.games.pacman.uilib.GameClockFX;

public class UIGlobals {
    public static GameUI THE_UI;
    public static final GameAssets THE_ASSETS = new GameAssets();
    public static final GameClockFX THE_CLOCK = new GameClockFX();
    public static final Keyboard THE_KEYBOARD = new Keyboard();
    public static final GameSound THE_SOUND = new GameSound();
}