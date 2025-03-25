package de.amr.games.pacman.ui;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui._3d.PacManGamesUI_3D;
import de.amr.games.pacman.ui.input.Keyboard;
import de.amr.games.pacman.ui.sound.GameSound;
import de.amr.games.pacman.uilib.AssetStorage;
import de.amr.games.pacman.uilib.GameClockFX;

import java.util.Map;

public class UIGlobals {

    public static PacManGamesUI createGameUI_2D(Map<GameVariant, GameUIConfiguration> configMap) {
        var ui = new PacManGamesUI();
        ui.loadAssets2D();
        for (var entry : configMap.entrySet()) {
            ui.configure(entry.getKey(), entry.getValue());
        }
        THE_GAME_CONTEXT = ui;
        return ui;
    }

    public static PacManGamesUI_3D createGameUI_3D(Map<GameVariant, GameUIConfiguration> configMap) {
        var ui = new PacManGamesUI_3D();
        ui.loadAssets2D();
        ui.loadAssets3D();
        for (var entry : configMap.entrySet()) {
            ui.configure(entry.getKey(), entry.getValue());
        }
        THE_GAME_CONTEXT = ui;
        return ui;
    }

    public static GameContext THE_GAME_CONTEXT;
    public static final AssetStorage THE_ASSETS = new AssetStorage();
    public static final GameClockFX THE_CLOCK = new GameClockFX();
    public static final Keyboard THE_KEYBOARD = new Keyboard();
    public static final GameSound THE_SOUND = new GameSound();
}
