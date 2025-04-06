/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman_xxl;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.MapSelectionMode;
import de.amr.games.pacman.uilib.Keyboard;
import de.amr.games.pacman.uilib.OptionMenu;
import javafx.animation.AnimationTimer;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.ui.Globals.PY_3D_ENABLED;
import static de.amr.games.pacman.ui.Globals.THE_UI;

public class PacManXXL_OptionMenu extends OptionMenu {

    public static class MenuState {
        GameVariant gameVariant;
        boolean play3D;
        boolean cutScenesEnabled;
        MapSelectionMode mapSelectionMode;
    }

    private final OptionMenu.MenuEntry<GameVariant> entryGameVariant;
    private final OptionMenu.MenuEntry<Boolean> entryPlay3D;
    private final OptionMenu.MenuEntry<Boolean> entryCutScenesEnabled;
    private final OptionMenu.MenuEntry<MapSelectionMode> entryMapSelectionMode;
    private final MenuState state = new MenuState();
    private final AnimationTimer drawingLoop;

    public PacManXXL_OptionMenu(int tilesX, int tilesY) {
        super(tilesX, tilesY);
        setBackgroundFill(Color.web("#0C1568"));
        setTitle("Pac-Man XXL");
        setCommandTexts(
            "SELECT OPTIONS WITH UP AND DOWN",
            "PRESS SPACE TO CHANGE VALUE",
            "PRESS E TO OPEN EDITOR",
            "PRESS ENTER TO START"
        );
        setOnStart(() -> {
            logMenuState();
            if (state.gameVariant == GameVariant.PACMAN_XXL || state.gameVariant == GameVariant.MS_PACMAN_XXL) {
                PY_3D_ENABLED.set(state.play3D);
                GameModel game = THE_GAME_CONTROLLER.game(state.gameVariant);
                game.setCutScenesEnabled(state.cutScenesEnabled);
                game.mapSelector().setMapSelectionMode(state.mapSelectionMode);
                game.mapSelector().loadAllMaps(game);
                THE_UI.selectGameVariant(state.gameVariant);
            } else {
                Logger.error("Game variant {} is not allowed for XXL game", state.gameVariant);
            }
        });

        drawingLoop = new AnimationTimer() {
            @Override
            public void handle(long now) { draw(); }
        };

        entryGameVariant = new OptionMenu.MenuEntry<>("GAME VARIANT",
                List.of(GameVariant.PACMAN_XXL, GameVariant.MS_PACMAN_XXL))
        {
            @Override
            protected void onValueChange(int index) {
                state.gameVariant = selectedValue();
                logMenuState();
            }

            @Override
            public String selectedValueText() {
                return switch (selectedValue()) {
                    case PACMAN_XXL -> "PAC-MAN";
                    case MS_PACMAN_XXL -> "MS.PAC-MAN";
                    default -> "";
                };
            }
        };
        addEntry(entryGameVariant);

        entryPlay3D = new OptionMenu.MenuEntry<>("SCENE DISPLAY",
            List.of(true, false))
        {
            @Override
            protected void onValueChange(int index) {
                state.play3D = selectedValue();
                logMenuState();
            }

            @Override
            public String selectedValueText() {
                return selectedValue() ? "3D" : "2D";
            }
        };
        addEntry(entryPlay3D);

        entryCutScenesEnabled = new OptionMenu.MenuEntry<>("CUTSCENES",
                List.of(true, false))
        {
            @Override
            protected void onValueChange(int index) {
                state.cutScenesEnabled = selectedValue();
                logMenuState();
            }

            @Override
            public String selectedValueText() {
                return selectedValue() ? "ON" : "OFF";
            }
        };
        addEntry(entryCutScenesEnabled);

        entryMapSelectionMode = new OptionMenu.MenuEntry<>("MAP ORDER",
                List.of(MapSelectionMode.CUSTOM_MAPS_FIRST, MapSelectionMode.ALL_RANDOM))
        {
            @Override
            protected void onValueChange(int index) {
                if (enabled) {
                    state.mapSelectionMode = selectedValue();
                }
                logMenuState();
            }

            @Override
            public String selectedValueText() {
                if (!enabled) {
                    return "NO CUSTOM MAPS!";
                }
                return switch (selectedValue()) {
                    case CUSTOM_MAPS_FIRST -> "CUSTOM MAPS FIRST";
                    case ALL_RANDOM -> "RANDOM ORDER";
                    default -> "";
                };
            }
        };
        addEntry(entryMapSelectionMode);
    }

    @Override
    protected void handleKeyPress(KeyEvent e) {
        super.handleKeyPress(e);
        if (Keyboard.naked(KeyCode.E).match(e)) {
            THE_UI.showEditorView();
        }
    }

    public void startDrawingLoop() {
        drawingLoop.start();
        Logger.trace("Menu drawing started");
    }

    public void stopDrawingLoop() {
        drawingLoop.stop();
        Logger.trace("Menu drawing stopped");
    }

    public void setState(
        boolean play3D,
        GameVariant gameVariant,
        boolean cutScenesEnabled,
        MapSelectionMode mapSelectionMode,
        boolean customMapsExist)
    {
        state.play3D = play3D;
        state.gameVariant = gameVariant;
        state.cutScenesEnabled = cutScenesEnabled;
        state.mapSelectionMode = mapSelectionMode;

        entryPlay3D.selectValue(play3D);
        entryGameVariant.selectValue(gameVariant);
        entryCutScenesEnabled.selectValue(cutScenesEnabled);
        entryMapSelectionMode.selectValue(mapSelectionMode);
        entryMapSelectionMode.setEnabled(customMapsExist);

        logMenuState();
    }

    public MenuState state() {
        return state;
    }

    private void logMenuState() {
        Logger.info("Menu state: gameVariant={}, play3D={}, cutScenesEnabled={}, mapSelectionMode={}",
            state.gameVariant, state.play3D, state.cutScenesEnabled, state.mapSelectionMode);
    }
}