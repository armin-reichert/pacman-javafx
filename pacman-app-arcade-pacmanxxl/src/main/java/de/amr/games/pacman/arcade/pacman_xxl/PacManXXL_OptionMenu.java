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
import de.amr.games.pacman.uilib.OptionMenuEntry;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.Globals.TS;
import static de.amr.games.pacman.ui.Globals.*;

public class PacManXXL_OptionMenu extends OptionMenu {

    // State
    GameVariant gameVariant;
    boolean play3D;
    boolean cutScenesEnabled;
    MapSelectionMode mapOrder;

    private final OptionMenuEntry<GameVariant> entryGameVariant = new OptionMenuEntry<>("GAME VARIANT",
            GameVariant.PACMAN_XXL, GameVariant.MS_PACMAN_XXL) {

        @Override
        protected void onValueChanged(int index) {
            gameVariant = selectedValue();
            logMenuState();
        }

        @Override
        public String selectedValueText() {
            return switch (gameVariant) {
                case PACMAN_XXL -> "PAC-MAN XXL";
                case MS_PACMAN_XXL -> "MS.PAC-MAN XXL";
                default -> "";
            };
        }
    };

    private final OptionMenuEntry<Boolean> entryPlay3D = new OptionMenuEntry<>("SCENE DISPLAY", true, false) {

        @Override
        protected void onValueChanged(int index) {
            play3D = selectedValue();
            PY_3D_ENABLED.set(play3D);
            logMenuState();
        }

        @Override
        public String selectedValueText() {
            return play3D ? "3D" : "2D";
        }
    };

    private final OptionMenuEntry<Boolean> entryCutScenesEnabled = new OptionMenuEntry<>("CUTSCENES", true, false) {

        @Override
        protected void onValueChanged(int index) {
            cutScenesEnabled = selectedValue();
            logMenuState();
        }

        @Override
        public String selectedValueText() {
            return cutScenesEnabled ? "ON" : "OFF";
        }
    };

    private final OptionMenuEntry<MapSelectionMode> entryMapOrder = new OptionMenuEntry<>("MAP ORDER",
            MapSelectionMode.CUSTOM_MAPS_FIRST, MapSelectionMode.ALL_RANDOM) {

        @Override
        protected void onValueChanged(int index) {
            if (enabled) {
                mapOrder = selectedValue();
            }
            logMenuState();
        }

        @Override
        public String selectedValueText() {
            if (!enabled) {
                return "NO CUSTOM MAPS!";
            }
            return switch (mapOrder) {
                case CUSTOM_MAPS_FIRST -> "CUSTOM MAPS FIRST";
                case ALL_RANDOM -> "RANDOM ORDER";
                case NO_CUSTOM_MAPS -> "NO CUSTOM MAPS";
            };
        }
    };

    public PacManXXL_OptionMenu() {
        super(36 * TS);
        addEntry(entryGameVariant);
        addEntry(entryPlay3D);
        addEntry(entryCutScenesEnabled);
        addEntry(entryMapOrder);
        setBackgroundFill(Color.web("#0C1568"));
        setTitle("Pac-Man XXL");
        setCommandTexts(
            "SELECT OPTIONS WITH UP AND DOWN",
            "PRESS SPACE TO CHANGE VALUE",
            "PRESS E TO OPEN EDITOR",
            "PRESS ENTER TO START"
        );
        setOnStart(() -> {
            Logger.info("Start action from option menu triggered");
            if (gameVariant == GameVariant.PACMAN_XXL || gameVariant == GameVariant.MS_PACMAN_XXL) {
                GameModel game = THE_GAME_CONTROLLER.game(gameVariant);
                game.setCutScenesEnabled(cutScenesEnabled);
                game.mapSelector().setMapSelectionMode(mapOrder);
                game.mapSelector().loadAllMaps(game);
                THE_UI.selectGameVariant(gameVariant);
            } else {
                Logger.error("Game variant {} is not allowed for XXL game", gameVariant);
            }
        });
        soundEnabledProperty().bind(THE_SOUND.mutedProperty().not());
    }

    @Override
    protected void handleKeyPress(KeyEvent e) {
        super.handleKeyPress(e);
        if (Keyboard.naked(KeyCode.E).match(e)) {
            THE_UI.showEditorView();
        }
    }

    public GameVariant gameVariant() {
        return gameVariant;
    }

    public void requestFocus() {
        Logger.info("XXL Option menu canvas requests focus");
        canvas.requestFocus();
    }

    public void setGameVariant(GameVariant gameVariant) {
        this.gameVariant = gameVariant;
        entryGameVariant.selectValue(gameVariant);
    }

    public void setPlay3D(boolean play3D) {
        this.play3D = play3D;
        entryPlay3D.selectValue(play3D);
    }

    public void setCutScenesEnabled(boolean cutScenesEnabled) {
        this.cutScenesEnabled = cutScenesEnabled;
        entryCutScenesEnabled.selectValue(cutScenesEnabled);
    }

    public void setMapOrder(MapSelectionMode mapOrder, boolean customMapsExist) {
        this.mapOrder = mapOrder;
        entryMapOrder.selectValue(mapOrder);
        entryMapOrder.setEnabled(customMapsExist);
    }

    private void logMenuState() {
        Logger.info("Menu state: gameVariant={}, play3D={}, cutScenesEnabled={}, mapOrder={}", gameVariant, play3D, cutScenesEnabled, mapOrder);
    }
}