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

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.ui.Globals.PY_3D_ENABLED;
import static de.amr.games.pacman.ui.Globals.THE_UI;

public class PacManXXL_OptionMenu extends OptionMenu {

    // State
    GameVariant gameVariant;
    boolean play3D;
    boolean cutScenesEnabled;
    MapSelectionMode mapOrder;

    private final MenuEntry<GameVariant> entryGameVariant = new MenuEntry<>("GAME VARIANT",
            GameVariant.PACMAN_XXL, GameVariant.MS_PACMAN_XXL) {

        @Override
        protected void onValueChanged(int index) {
            gameVariant = selectedValue();
            logMenuState();
        }

        @Override
        public String selectedValueText() {
            return switch (selectedValue()) {
                case PACMAN_XXL -> "PAC-MAN XXL";
                case MS_PACMAN_XXL -> "MS.PAC-MAN XXL";
                default -> "";
            };
        }
    };

    private final MenuEntry<Boolean> entryPlay3D = new MenuEntry<>("SCENE DISPLAY", true, false) {

        @Override
        protected void onValueChanged(int index) {
            play3D = selectedValue();
            PY_3D_ENABLED.set(play3D);
            logMenuState();
        }

        @Override
        public String selectedValueText() {
            return selectedValue() ? "3D" : "2D";
        }
    };

    private final MenuEntry<Boolean> entryCutScenesEnabled = new MenuEntry<>("CUTSCENES", true, false) {

        @Override
        protected void onValueChanged(int index) {
            cutScenesEnabled = selectedValue();
            logMenuState();
        }

        @Override
        public String selectedValueText() {
            return selectedValue() ? "ON" : "OFF";
        }
    };

    private final MenuEntry<MapSelectionMode> entryMapOrder = new MenuEntry<>("MAP ORDER",
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
            return switch (selectedValue()) {
                case CUSTOM_MAPS_FIRST -> "CUSTOM MAPS FIRST";
                case ALL_RANDOM -> "RANDOM ORDER";
                default -> "";
            };
        }
    };

    private final AnimationTimer drawingLoop = new AnimationTimer() {
        @Override
        public void handle(long now) { draw(); }
    };

    public PacManXXL_OptionMenu(int tilesX, int tilesY) {
        super(tilesX, tilesY);
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
        this.play3D = play3D;
        this.gameVariant = gameVariant;
        this.cutScenesEnabled = cutScenesEnabled;
        this.mapOrder = mapSelectionMode;

        entryPlay3D.selectValue(play3D);
        entryGameVariant.selectValue(gameVariant);
        entryCutScenesEnabled.selectValue(cutScenesEnabled);
        entryMapOrder.selectValue(mapSelectionMode);
        entryMapOrder.setEnabled(customMapsExist);

        logMenuState();
    }

    private void logMenuState() {
        Logger.info("Menu state: gameVariant={}, play3D={}, cutScenesEnabled={}, mapOrder={}", gameVariant, play3D, cutScenesEnabled, mapOrder);
    }
}