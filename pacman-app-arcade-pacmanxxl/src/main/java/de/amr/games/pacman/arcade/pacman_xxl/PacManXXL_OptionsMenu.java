/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman_xxl;

import de.amr.games.pacman.model.CustomMapSelectionMode;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;

public class PacManXXL_OptionsMenu extends BorderPane {

    private static final double HEIGHT_FRACTION = 0.85;
    private static final double UNSCALED_HEIGHT = 36 * TS;

    private static abstract class MenuEntry {
        final String label;
        List<String> options;
        int selectedOptionIndex;
        void onSelected() {}
        abstract void onOptionSelected();
        MenuEntry(String label) {
            this.label = label;
        }
    }

    private final Canvas canvas = new Canvas();
    private final FloatProperty scalingPy = new SimpleFloatProperty(2);
    private final Font arcadeFontNormal;
    private final Font arcadeFontBig;

    private final List<MenuEntry> entries = new ArrayList<>();
    private int selectedEntryIndex = 0;

    public PacManXXL_OptionsMenu(PacManGamesUI ui) {
        setBackground(Background.EMPTY);

        ResourceManager rm = () -> GameRenderer.class;
        arcadeFontNormal = rm.loadFont("fonts/emulogic.ttf", 8);
        arcadeFontBig = rm.loadFont("fonts/emulogic.ttf", 20);

        scalingPy.bind(ui.getMainScene().heightProperty().multiply(HEIGHT_FRACTION).divide(UNSCALED_HEIGHT));

        canvas.widthProperty().bind(scalingPy.multiply(UNSCALED_HEIGHT));
        canvas.heightProperty().bind(scalingPy.multiply(UNSCALED_HEIGHT));

        setCenter(canvas);

        entries.add(new MenuEntry("GAME VARIANT") {
            {
                options = List.of("PAC-MAN", "MS.PAC-MAN");
                selectedOptionIndex = 0;
            }
            @Override
            void onOptionSelected() {
                switch (selectedOptionIndex) {
                    case 0 -> { // Pac-Man
                        GameModel pacManGame = ui.gameController().gameModel(GameVariant.PACMAN);
                        GameConfiguration pacManGameConfig = new PacManXXL_PacMan_GameConfig3D(ui.assets());
                        ui.gameController().setGameModel(GameVariant.PACMAN_XXL, pacManGame);
                        ui.setGameConfiguration(GameVariant.PACMAN_XXL, pacManGameConfig);
                        // clear sounds first such that they are replaced with Pac-Man sounds
                        ui.sound().clearSounds(GameVariant.PACMAN_XXL);
                        ui.sound().useSoundsForGameVariant(GameVariant.PACMAN_XXL, pacManGameConfig.assetKeyPrefix());
                    }
                    case 1 -> { // Ms. Pac-Man
                        GameModel msPacManGame = ui.gameController().gameModel(GameVariant.MS_PACMAN);
                        GameConfiguration msPacManGameConfig = new PacManXXL_MsPacMan_GameConfig3D(ui.assets());
                        ui.gameController().setGameModel(GameVariant.PACMAN_XXL, msPacManGame);
                        ui.setGameConfiguration(GameVariant.PACMAN_XXL, msPacManGameConfig);
                        // clear sounds first such that they are replaced with Ms. Pac-Man sounds
                        ui.sound().clearSounds(GameVariant.PACMAN_XXL);
                        ui.sound().useSoundsForGameVariant(GameVariant.PACMAN_XXL, msPacManGameConfig.assetKeyPrefix());
                    }
                }
            }
        });
        entries.add(new MenuEntry("CUT SCENES") {
            {
                options = List.of("ENABLED", "DISABLED");
                selectedOptionIndex = 1;
            }
            @Override
            void onOptionSelected() {}
        });
        entries.add(new MenuEntry("CUSTOM MAPS") {
            {
                options = List.of("CUSTOM-MAPS FIRST", "ALL MAPS RANDOMLY");
                selectedOptionIndex = 1;
            }
            @Override
            void onOptionSelected() {
                switch (selectedOptionIndex) {
                    case 0 -> ui.game().setMapSelectionMode(CustomMapSelectionMode.CUSTOM_MAPS_FIRST);
                    case 1 -> ui.game().setMapSelectionMode(CustomMapSelectionMode.ALL_RANDOM);
                }
            }
        });

        addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            switch (e.getCode()) {
                case DOWN -> {
                    selectedEntryIndex++;
                    if (selectedEntryIndex == entries.size()) selectedEntryIndex = 0;
                    entries.get(selectedEntryIndex).onSelected();
                }
                case UP -> {
                    selectedEntryIndex--;
                    if (selectedEntryIndex == -1) selectedEntryIndex = entries.size() - 1;
                    entries.get(selectedEntryIndex).onSelected();
                }
                case SPACE -> {
                    MenuEntry entry = entries.get(selectedEntryIndex);
                    entry.selectedOptionIndex++;
                    if (entry.selectedOptionIndex == entry.options.size()) entry.selectedOptionIndex = 0;
                    entry.onOptionSelected();
                }
                case ENTER -> ui.selectGamePage();
            }
        });

        Timeline loop = new Timeline(new KeyFrame(Duration.millis(1000.0/ 60), e -> draw()));
        loop.setCycleCount(Animation.INDEFINITE);
        loop.play();
    }

    private void draw() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.save();

        g.setFill(Color.grayRgb(200));
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        int d = 3;
        g.setFill(Color.rgb(30, 30, 200));
        g.fillRect(d, d, canvas.getWidth() - 2*d, canvas.getHeight() - 2*d);

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
            g.fillText(entry.options.get(entry.selectedOptionIndex), 17 * TS, y);
        }

        g.setFill(Color.YELLOW);
        g.fillText("   PRESS SPACE TO CHANGE OPTIONS    ", 0, 29 * TS);
        g.fillText("  CHOOSE OPTIONS WITH UP AND DOWN   ", 0, 31 * TS);
        g.fillText("     PRESS ENTER TO START GAME      ", 0, 33 * TS);

        g.restore();
    }
}