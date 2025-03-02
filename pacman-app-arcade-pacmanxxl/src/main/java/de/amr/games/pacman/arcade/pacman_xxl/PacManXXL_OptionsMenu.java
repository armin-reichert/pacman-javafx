/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman_xxl;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.MapSelectionMode;
import de.amr.games.pacman.ui2d.PacManGamesUI;
import de.amr.games.pacman.uilib.OptionMenu;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.games.pacman.lib.Globals.TS;

public class PacManXXL_OptionsMenu {

    private static final float UNSCALED_HEIGHT = 36 * TS;
    private static final float RELATIVE_HEIGHT = 0.85f;

    private final OptionMenu.MenuEntry entryGameVariant;
    private final OptionMenu.MenuEntry entryCutScenesEnabled;
    private final OptionMenu.MenuEntry entryMapSelectionMode;

    private static class MenuState {
        private GameVariant gameVariant;
        private boolean cutScenesEnabled;
        private MapSelectionMode mapSelectionMode;
    }

    private final MenuState menuState = new MenuState();
    private final OptionMenu menu = new OptionMenu(UNSCALED_HEIGHT);

    public PacManXXL_OptionsMenu(PacManGamesUI ui) {

        menu.setTitle("Pac-Man XXL");
        menu.setBackgroundFill(Color.valueOf("#172E73"));
        menu.setBorderStroke(Color.WHITESMOKE);
        menu.setEntryTextFill(Color.YELLOW);
        menu.setEntryValueFill(Color.WHITESMOKE);
        menu.setTitleTextFill(Color.RED);
        menu.setHintTextFill(Color.YELLOW);

        menu.scalingProperty().bind(ui.getMainScene().heightProperty().multiply(RELATIVE_HEIGHT).divide(UNSCALED_HEIGHT));

        menu.setOnStart(() -> {
            logMenuState();
            if (menuState.gameVariant == GameVariant.PACMAN_XXL || menuState.gameVariant == GameVariant.MS_PACMAN_XXL) {
                GameModel game = ui.gameController().gameModel(menuState.gameVariant);
                game.setCutScenesEnabled(menuState.cutScenesEnabled);
                game.mapSelector().loadAllMaps(game);
                game.mapSelector().setMapSelectionMode(menuState.mapSelectionMode);
                ui.selectGameVariant(menuState.gameVariant);
            } else {
                Logger.error("Game variant {} is not allowed for XXL game", menuState.gameVariant);
            }
        });

        entryGameVariant = new OptionMenu.MenuEntry(
                "GAME VARIANT",
                List.of("PAC-MAN", "MS.PAC-MAN")) {

            @Override
            protected void onValueChange() {
                switch (valueIndex) {
                    case 0 -> menuState.gameVariant = GameVariant.PACMAN_XXL;
                    case 1 -> menuState.gameVariant = GameVariant.MS_PACMAN_XXL;
                    default -> throw new IllegalArgumentException("Menu Selection failed");
                }
                Logger.info("menuState.gameVariant={}", menuState.gameVariant);
            }
        };

        entryCutScenesEnabled = new OptionMenu.MenuEntry(
                "CUT SCENES",
                List.of("ENABLED", "DISABLED")) {

            @Override
            protected void onValueChange() {
                menuState.cutScenesEnabled = (valueIndex == 0);
                Logger.info("menuState.cutScenesEnabled={}", menuState.cutScenesEnabled);
            }
        };

        entryMapSelectionMode = new OptionMenu.MenuEntry(
                "CUSTOM MAPS",
                List.of("CUSTOM-MAPS FIRST", "ALL MAPS RANDOMLY")) {

            @Override
            protected void onValueChange() {
                switch (valueIndex) {
                    case 0 -> menuState.mapSelectionMode = MapSelectionMode.CUSTOM_MAPS_FIRST;
                    case 1 -> menuState.mapSelectionMode = MapSelectionMode.ALL_RANDOM;
                    default -> throw new IllegalArgumentException("Menu Selection failed");
                }
                Logger.info("menuState.mapSelectionMode={}", menuState.mapSelectionMode);
            }
        };

        menu.addEntry(entryGameVariant);
        menu.addEntry(entryCutScenesEnabled);
        menu.addEntry(entryMapSelectionMode);

        Timeline loop = new Timeline(new KeyFrame(Duration.millis(1000.0/ 60), e -> menu.draw()));
        loop.setCycleCount(Animation.INDEFINITE);
        loop.play();
    }

    public Node root() {
        return menu.root();
    }

    public void setMenuState(GameVariant gameVariant, boolean cutScenesEnabled, MapSelectionMode mapSelectionMode) {
        menuState.gameVariant = gameVariant;
        menuState.cutScenesEnabled = cutScenesEnabled;
        menuState.mapSelectionMode = mapSelectionMode;

        entryGameVariant.setValueIndex(switch (gameVariant) {
            case PACMAN_XXL -> 0;
            case MS_PACMAN_XXL -> 1;
            default -> throw new IllegalArgumentException();
        });

        entryCutScenesEnabled.setValueIndex(cutScenesEnabled ? 0 : 1);

        entryMapSelectionMode.setValueIndex(switch (mapSelectionMode) {
            case CUSTOM_MAPS_FIRST -> 0;
            case ALL_RANDOM -> 1;
            case NO_CUSTOM_MAPS -> 1; // TODO
        });
        logMenuState();
    }

    private void logMenuState() {
        Logger.info("Menu state: gameVariant={}, cutScenesEnabled={}, mapSelectionMode={}",
                menuState.gameVariant, menuState.cutScenesEnabled, menuState.mapSelectionMode);
    }
}