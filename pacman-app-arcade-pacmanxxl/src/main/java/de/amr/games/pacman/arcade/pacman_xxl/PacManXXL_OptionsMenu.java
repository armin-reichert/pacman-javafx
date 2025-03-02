/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman_xxl;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.MapSelectionMode;
import de.amr.games.pacman.ui2d.GameRenderer;
import de.amr.games.pacman.ui2d.PacManGamesUI;
import de.amr.games.pacman.ui2d.scene.GameConfiguration;
import de.amr.games.pacman.uilib.ResourceManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;

public class PacManXXL_OptionsMenu {

    private static final double HEIGHT_FRACTION = 0.85;
    private static final double UNSCALED_HEIGHT = 36 * TS;
    private static final Color BACKGROUND_COLOR = Color.valueOf("#172E73");
    private static final Color BORDER_COLOR = Color.grayRgb(200);

    private static abstract class MenuEntry {
        final String label;
        List<String> options;
        int valueIndex;
        MenuEntry(String label, List<String> options) {
            this.label = label;
            this.options = new ArrayList<>(options);
        }
        void onSelect() {}
        abstract void onValueChange();
    }

    private final BorderPane root = new BorderPane();
    private final Canvas canvas = new Canvas();
    private final FloatProperty scalingPy = new SimpleFloatProperty(2);
    private final Font arcadeFontNormal;
    private final Font arcadeFontBig;

    private final MenuEntry entryGameVariant;
    private final MenuEntry entryCutScenesEnabled;
    private final MenuEntry entryMapSelectionMode;
    private final List<MenuEntry> entries;
    private int selectedEntryIndex = 0;

    // state
    private static class MenuState {
        private GameVariant gameVariant;
        private boolean cutScenesEnabled;
        private MapSelectionMode mapSelectionMode;
    }

    private final MenuState menuState = new MenuState();

    public PacManXXL_OptionsMenu(PacManGamesUI ui) {
        root.setCenter(canvas);
        root.setBorder(Border.stroke(BORDER_COLOR));

        ResourceManager rm = () -> GameRenderer.class;
        arcadeFontNormal = rm.loadFont("fonts/emulogic.ttf", 8);
        arcadeFontBig = rm.loadFont("fonts/emulogic.ttf", 20);

        scalingPy.bind(ui.getMainScene().heightProperty().multiply(HEIGHT_FRACTION).divide(UNSCALED_HEIGHT));

        root.maxWidthProperty().bind(scalingPy.multiply(UNSCALED_HEIGHT));
        root.maxHeightProperty().bind(scalingPy.multiply(UNSCALED_HEIGHT));

        canvas.widthProperty().bind(scalingPy.multiply(UNSCALED_HEIGHT));
        canvas.heightProperty().bind(scalingPy.multiply(UNSCALED_HEIGHT));

        entryGameVariant = new MenuEntry("GAME VARIANT", List.of("PAC-MAN", "MS.PAC-MAN")) {
            @Override
            void onValueChange() {
                switch (valueIndex) {
                    case 0 -> menuState.gameVariant = GameVariant.PACMAN_XXL;
                    case 1 -> menuState.gameVariant = GameVariant.MS_PACMAN_XXL;
                    default -> menuSelectionFailed();
                }
                Logger.info("menuState.gameVariant={}", menuState.gameVariant);
            }
        };

        entryCutScenesEnabled = new MenuEntry("CUT SCENES", List.of("ENABLED", "DISABLED")) {
            @Override
            void onValueChange() {
                menuState.cutScenesEnabled = (valueIndex == 0);
                Logger.info("menuState.cutScenesEnabled={}", menuState.cutScenesEnabled);
            }
        };

        entryMapSelectionMode = new MenuEntry("CUSTOM MAPS", List.of("CUSTOM-MAPS FIRST", "ALL MAPS RANDOMLY")) {
            @Override
            void onValueChange() {
                switch (valueIndex) {
                    case 0 -> menuState.mapSelectionMode = MapSelectionMode.CUSTOM_MAPS_FIRST;
                    case 1 -> menuState.mapSelectionMode = MapSelectionMode.ALL_RANDOM;
                    default -> menuSelectionFailed();
                }
                Logger.info("menuState.mapSelectionMode={}", menuState.mapSelectionMode);
            }
        };

        entries = Arrays.asList(entryGameVariant, entryCutScenesEnabled, entryMapSelectionMode);

        root.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            switch (e.getCode()) {
                case DOWN -> {
                    selectedEntryIndex++;
                    if (selectedEntryIndex == entries.size()) selectedEntryIndex = 0;
                    entries.get(selectedEntryIndex).onSelect();
                    ui.sound().playClipIfEnabled("option.selection_changed", 1);
                }
                case UP -> {
                    selectedEntryIndex--;
                    if (selectedEntryIndex == -1) selectedEntryIndex = entries.size() - 1;
                    entries.get(selectedEntryIndex).onSelect();
                    ui.sound().playClipIfEnabled("option.selection_changed", 1);
                }
                case SPACE -> {
                    MenuEntry entry = entries.get(selectedEntryIndex);
                    entry.valueIndex++;
                    if (entry.valueIndex == entry.options.size()) entry.valueIndex = 0;
                    entry.onValueChange();
                    ui.sound().playClipIfEnabled("option.value_changed", 1);
                }
                case ENTER -> startConfiguredGame(ui);
            }
        });

        Timeline loop = new Timeline(new KeyFrame(Duration.millis(1000.0/ 60), e -> draw()));
        loop.setCycleCount(Animation.INDEFINITE);
        loop.play();
    }

    public BorderPane root() {
        return root;
    }

    private void menuSelectionFailed() {
        throw new IllegalArgumentException("Menu Selection failed");
    }

    public void setMenuState(GameVariant gameVariant, boolean cutScenesEnabled, MapSelectionMode mapSelectionMode) {
        menuState.gameVariant = gameVariant;
        menuState.cutScenesEnabled = cutScenesEnabled;
        menuState.mapSelectionMode = mapSelectionMode;

        entryGameVariant.valueIndex = switch (gameVariant) {
            case PACMAN_XXL -> 0;
            case MS_PACMAN_XXL -> 1;
            default -> throw new IllegalArgumentException();
        };

        entryCutScenesEnabled.valueIndex = cutScenesEnabled ? 0 : 1;

        entryMapSelectionMode.valueIndex = switch (mapSelectionMode) {
            case CUSTOM_MAPS_FIRST -> 0;
            case ALL_RANDOM -> 1;
            case NO_CUSTOM_MAPS -> 1; // TODO
        };
        logMenuState();
    }

    private void logMenuState() {
        Logger.info("Menu state: gameVariant={}, cutScenesEnabled={}, mapSelectionMode={}",
                menuState.gameVariant, menuState.cutScenesEnabled, menuState.mapSelectionMode);
    }

    private void startConfiguredGame(PacManGamesUI ui) {
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
    }

    private void draw() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(BACKGROUND_COLOR);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        g.save();
        g.scale(scalingPy.doubleValue(), scalingPy.doubleValue());

        g.setFont(arcadeFontBig);
        g.setFill(Color.RED);
        g.fillText("PAC-MAN XXL", 4 * TS, 6 * TS);
        g.setFont(arcadeFontNormal);

        for (int i = 0; i < entries.size(); ++i) {
            int y = (12 + 3 * i) * TS;
            MenuEntry entry = entries.get(i);
            if (i == selectedEntryIndex) {
                g.setFill(Color.YELLOW);
                g.fillText("-", TS, y);
                g.fillText(">", TS+HTS, y);
            }
            g.setFill(Color.YELLOW);
            g.fillText(entry.label, 3 * TS, y);
            g.setFill(Color.WHITE);
            g.fillText(entry.options.get(entry.valueIndex), 17 * TS, y);
        }

        g.setFill(Color.YELLOW);
        g.fillText("   PRESS SPACE TO CHANGE OPTIONS    ", 0, 29 * TS);
        g.fillText("  CHOOSE OPTIONS WITH UP AND DOWN   ", 0, 31 * TS);
        g.fillText("     PRESS ENTER TO START GAME      ", 0, 33 * TS);

        g.restore();
    }
}