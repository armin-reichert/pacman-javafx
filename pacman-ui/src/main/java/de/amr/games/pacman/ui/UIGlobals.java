package de.amr.games.pacman.ui;

import de.amr.games.pacman.ui.input.Keyboard;
import de.amr.games.pacman.ui.sound.GameSound;
import de.amr.games.pacman.uilib.AssetStorage;
import de.amr.games.pacman.uilib.GameClockFX;

public class UIGlobals {
    public static GameContext THE_GAME_CONTEXT;
    public static final AssetStorage THE_ASSETS = new AssetStorage();
    public static final GameClockFX THE_CLOCK = new GameClockFX();
    public static final Keyboard THE_KEYBOARD = new Keyboard();
    public static final GameSound THE_SOUND = new GameSound(THE_ASSETS);
}
