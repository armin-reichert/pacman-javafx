/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman_xxl;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.MapSelectionMode;
import de.amr.games.pacman.ui.StartPage;
import de.amr.games.pacman.uilib.assets.ResourceManager;
import de.amr.games.pacman.uilib.input.Keyboard;
import de.amr.games.pacman.uilib.widgets.Flyer;
import de.amr.games.pacman.uilib.widgets.OptionMenu;
import de.amr.games.pacman.uilib.widgets.OptionMenuEntry;
import de.amr.games.pacman.uilib.widgets.OptionMenuStyle;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.Globals.TS;
import static de.amr.games.pacman.ui.Globals.*;
import static java.util.Objects.requireNonNull;

/**
 * Displays an option menu where the game variant to be played and other options can be set.
 */
public class PacManXXL_StartPage implements StartPage {

    private static class GameOptionMenu extends OptionMenu {

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

        GameOptionMenu() {
            super(42, 36, 6, 20);

            addEntry(entryGameVariant);
            addEntry(entryPlay3D);
            addEntry(entryCutScenesEnabled);
            addEntry(entryMapOrder);

            ResourceManager rm = this::getClass;
            setStyle(new OptionMenuStyle(
                rm.loadFont("fonts/emulogic.ttf", 3 * TS),
                rm.loadFont("fonts/emulogic.ttf", TS),
                OptionMenu.DEFAULT_STYLE.backgroundFill(),
                OptionMenu.DEFAULT_STYLE.borderStroke(),
                Color.RED, // DEFAULT_STYLE.titleTextFill(),
                OptionMenu.DEFAULT_STYLE.entryTextFill(),
                OptionMenu.DEFAULT_STYLE.entryValueFill(),
                OptionMenu.DEFAULT_STYLE.entryValueDisabledFill(),
                OptionMenu.DEFAULT_STYLE.hintTextFill(),
                OptionMenu.DEFAULT_STYLE.entrySelectedSound(),
                OptionMenu.DEFAULT_STYLE.valueSelectedSound()
            ));
            setTitle("Pac-Man XXL");
            setCommandTexts(
                "SELECT OPTIONS WITH UP AND DOWN",
                "PRESS SPACE TO CHANGE VALUE",
                "PRESS E TO OPEN EDITOR",
                "PRESS ENTER TO START"
            );
        }

        private void setPlay3D(boolean play3D) {
            this.play3D = play3D;
            entryPlay3D.selectValue(play3D);
        }

        private void setCutScenesEnabled(boolean cutScenesEnabled) {
            this.cutScenesEnabled = cutScenesEnabled;
            entryCutScenesEnabled.selectValue(cutScenesEnabled);
        }

        private void setMapOrder(MapSelectionMode mapOrder, boolean customMapsExist) {
            this.mapOrder = requireNonNull(mapOrder);
            entryMapOrder.selectValue(mapOrder);
            entryMapOrder.setEnabled(customMapsExist);
        }

        private void logMenuState() {
            Logger.info("gameVariant={} play3D={} cutScenesEnabled={} mapOrder={}", gameVariant, play3D, cutScenesEnabled, mapOrder);
        }

        @Override
        protected void handleKeyPress(KeyEvent e) {
            if (Keyboard.naked(KeyCode.E).match(e)) {
                THE_UI.showEditorView();
            } else {
                super.handleKeyPress(e);
            }
        }

        @Override
        public void draw() {
            super.draw();
            drawActorAnimation();
        }

        private void drawActorAnimation() {

        }
    }

    private final StackPane root = new StackPane();
    private final GameOptionMenu menu;

    public PacManXXL_StartPage() {
        ResourceManager rm = this::getClass;
        Flyer flyer = new Flyer(rm.loadImage("graphics/pacman_xxl_startpage.jpg"));
        flyer.setPageLayout(0, Flyer.LayoutMode.FILL);
        flyer.selectPage(0);

        menu = new GameOptionMenu();
        // scale menu to take 90% of start page height
        menu.scalingProperty().bind(root.heightProperty().multiply(0.9).divide(menu.numTilesY() * TS));
        menu.soundEnabledProperty().bind(THE_SOUND.mutedProperty().not());
        menu.setOnStart(() -> {
            if (menu.gameVariant == GameVariant.PACMAN_XXL || menu.gameVariant == GameVariant.MS_PACMAN_XXL) {
                GameModel game = THE_GAME_CONTROLLER.game(menu.gameVariant);
                game.cutScenesEnabledProperty().set(menu.cutScenesEnabled);
                game.mapSelector().setMapSelectionMode(menu.mapOrder);
                game.mapSelector().loadAllMaps(game);
                THE_UI.selectGameVariant(menu.gameVariant);
            } else {
                Logger.error("Game variant {} is not allowed for XXL game", menu.gameVariant);
            }
        });

        root.setBackground(Background.fill(Color.BLACK));
        root.getChildren().addAll(flyer, menu.root());
    }

    @Override
    public void onEnter() {
        initMenuState();
        menu.startDrawing();
    }

    @Override
    public GameVariant currentGameVariant() {
        return menu.gameVariant;
    }

    @Override
    public Region layoutRoot() {
        return root;
    }

    @Override
    public void requestFocus() {
        menu.requestFocus();
    }

    @Override
    public void onExit() {
        menu.stopDrawing();
    }

    private void initMenuState() {
        if (menu.gameVariant == null) {
            menu.gameVariant = GameVariant.PACMAN_XXL;
            menu.entryGameVariant.selectValue(menu.gameVariant);
        }
        GameModel game = THE_GAME_CONTROLLER.game(menu.gameVariant);
        menu.setPlay3D(PY_3D_ENABLED.get());
        menu.setCutScenesEnabled(game.cutScenesEnabledProperty().get());
        game.mapSelector().loadAllMaps(game);
        menu.setMapOrder(game.mapSelector().mapSelectionMode(), !game.mapSelector().customMaps().isEmpty());
        menu.logMenuState();
        Logger.info("Option menu initialized");
    }
}