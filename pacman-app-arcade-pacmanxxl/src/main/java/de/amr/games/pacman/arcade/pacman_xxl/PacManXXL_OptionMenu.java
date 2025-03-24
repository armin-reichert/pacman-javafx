/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman_xxl;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.MapSelectionMode;
import de.amr.games.pacman.ui.PacManGamesUI;
import de.amr.games.pacman.ui.input.Keyboard;
import de.amr.games.pacman.uilib.OptionMenu;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.Globals.TS;
import static de.amr.games.pacman.ui._3d.GlobalProperties3d.PY_3D_ENABLED;

public class PacManXXL_OptionMenu extends OptionMenu {

    private static final float UNSCALED_HEIGHT = 36 * TS;
    private static final float RELATIVE_HEIGHT = 0.9f;

    private static class MenuState {
        GameVariant gameVariant;
        boolean play3D;
        boolean cutScenesEnabled;
        MapSelectionMode mapSelectionMode;
    }

    private final PacManGamesUI ui;
    private final OptionMenu.MenuEntry<GameVariant> entryGameVariant;
    private final OptionMenu.MenuEntry<Boolean> entryPlay3D;
    private final OptionMenu.MenuEntry<Boolean> entryCutScenesEnabled;
    private final OptionMenu.MenuEntry<MapSelectionMode> entryMapSelectionMode;
    private final MenuState state = new MenuState();
    private final AnimationTimer drawingLoop;

    public PacManXXL_OptionMenu(PacManGamesUI ui) {
        super(UNSCALED_HEIGHT);
        this.ui = ui;

        setBackgroundFill(Color.web("#0C1568"));
        setTitle("  Pac-Man XXL");
        setOnStart(() -> {
            logMenuState();
            if (state.gameVariant == GameVariant.PACMAN_XXL || state.gameVariant == GameVariant.MS_PACMAN_XXL) {
                ui.setGameVariant(state.gameVariant);
                PY_3D_ENABLED.set(state.play3D);
                GameModel game = THE_GAME_CONTROLLER.game(state.gameVariant);
                game.setCutScenesEnabled(state.cutScenesEnabled);
                game.mapSelector().setMapSelectionMode(state.mapSelectionMode);
                game.mapSelector().loadAllMaps(game);
            } else {
                Logger.error("Game variant {} is not allowed for XXL game", state.gameVariant);
            }
        });
        scalingProperty().bind(ui.mainScene().heightProperty().multiply(RELATIVE_HEIGHT).divide(UNSCALED_HEIGHT));

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

        addEntry(entryGameVariant);
        addEntry(entryPlay3D);
        addEntry(entryCutScenesEnabled);
        addEntry(entryMapSelectionMode);
    }

    @Override
    protected void handleKeyPress(KeyEvent e) {
        super.handleKeyPress(e);
        if (Keyboard.naked(KeyCode.E).match(e)) {
            ui.openEditor();
        }
    }

    @Override
    public void draw() {
        super.draw();
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.save();
        g.scale(scalingPy.doubleValue(), scalingPy.doubleValue());
        g.setFont(arcadeFont8);
        g.setFill(hintTextFill);
        g.fillText("      PRESS E TO OPEN EDITOR ", 0, 27 * TS);
        g.restore();
    }

    public void startDrawingLoop() {
        drawingLoop.start();
        Logger.trace("Menu drawing started");
    }

    public void stopDrawingLoop() {
        drawingLoop.stop();
        Logger.trace("Menu drawing stopped");
    }

    public void setState(boolean play3D, GameVariant gameVariant, boolean cutScenesEnabled,
                         MapSelectionMode mapSelectionMode, boolean customMapsExist)
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

    private void logMenuState() {
        Logger.info("Menu state: gameVariant={}, play3D={}, cutScenesEnabled={}, mapSelectionMode={}",
            state.gameVariant, state.play3D, state.cutScenesEnabled, state.mapSelectionMode);
    }
}