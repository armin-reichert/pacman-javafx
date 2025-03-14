/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman_xxl;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.MapSelectionMode;
import de.amr.games.pacman.ui.PacManGamesUI;
import de.amr.games.pacman.ui._3d.GlobalProperties3d;
import de.amr.games.pacman.uilib.OptionMenu;
import javafx.animation.AnimationTimer;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.games.pacman.lib.Globals.TS;

public class PacManXXL_OptionsMenu {

    private static final float UNSCALED_HEIGHT = 36 * TS;
    private static final float RELATIVE_HEIGHT = 0.9f;

    private static class MenuState {
        GameVariant gameVariant;
        boolean play3D;
        boolean cutScenesEnabled;
        MapSelectionMode mapSelectionMode;
    }

    private final OptionMenu menu = new OptionMenu(UNSCALED_HEIGHT);
    private final OptionMenu.MenuEntry<GameVariant> entryGameVariant;
    private final OptionMenu.MenuEntry<Boolean> entryPlay3D;
    private final OptionMenu.MenuEntry<Boolean> entryCutScenesEnabled;
    private final OptionMenu.MenuEntry<MapSelectionMode> entryMapSelectionMode;

    private final AnimationTimer animationTimer;
    private final MenuState state = new MenuState();

    public PacManXXL_OptionsMenu(PacManGamesUI ui) {
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                menu.draw();
            }
        };

        menu.setTitle("  Pac-Man XXL");
        menu.setBackgroundFill(Color.valueOf("#172E73"));
        menu.setBorderStroke(Color.WHITESMOKE);
        menu.setEntryTextFill(Color.YELLOW);
        menu.setEntryValueFill(Color.WHITESMOKE);
        menu.setTitleTextFill(Color.RED);
        menu.setHintTextFill(Color.YELLOW);

        menu.scalingProperty().bind(ui.mainScene().heightProperty().multiply(RELATIVE_HEIGHT).divide(UNSCALED_HEIGHT));

        menu.setOnStart(() -> {
            logMenuState();
            if (state.gameVariant == GameVariant.PACMAN_XXL || state.gameVariant == GameVariant.MS_PACMAN_XXL) {
                GlobalProperties3d.PY_3D_ENABLED.set(state.play3D);
                GameModel game = ui.gameController().game(state.gameVariant);
                game.setCutScenesEnabled(state.cutScenesEnabled);
                game.mapSelector().loadAllMaps(game);
                game.mapSelector().setMapSelectionMode(state.mapSelectionMode);
                ui.selectGameVariant(state.gameVariant);
            } else {
                Logger.error("Game variant {} is not allowed for XXL game", state.gameVariant);
            }
        });

        entryGameVariant = new OptionMenu.MenuEntry<>(
            "GAME VARIANT", List.of(GameVariant.PACMAN_XXL, GameVariant.MS_PACMAN_XXL))
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

        entryPlay3D = new OptionMenu.MenuEntry<>(
            "SCENE DISPLAY",
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

        entryCutScenesEnabled = new OptionMenu.MenuEntry<>(
            "CUTSCENES", List.of(true, false))
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

        entryMapSelectionMode = new OptionMenu.MenuEntry<>(
            "MAP ORDER", List.of(MapSelectionMode.CUSTOM_MAPS_FIRST, MapSelectionMode.ALL_RANDOM))
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

        menu.addEntry(entryGameVariant);
        menu.addEntry(entryPlay3D);
        menu.addEntry(entryCutScenesEnabled);
        menu.addEntry(entryMapSelectionMode);
    }

    public AnimationTimer getAnimationTimer() {
        return animationTimer;
    }

    public Node root() {
        return menu.root();
    }

    public void setState(boolean play3D, GameVariant gameVariant, boolean cutScenesEnabled,
                         MapSelectionMode mapSelectionMode, boolean customMapsExist) {
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

    private void logMenuState() {
        Logger.info("Menu state: gameVariant={}, play3D={}, cutScenesEnabled={}, mapSelectionMode={}",
            state.gameVariant, state.play3D, state.cutScenesEnabled, state.mapSelectionMode);
    }
}