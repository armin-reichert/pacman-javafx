package de.amr.games.pacman.arcade.pacman_xxl;

import de.amr.games.pacman.model.CustomMapSelectionMode;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.GameRenderer;
import de.amr.games.pacman.ui2d.PacManGamesUI;
import de.amr.games.pacman.uilib.ResourceManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

import static de.amr.games.pacman.lib.Globals.TS;

public class BootMenu extends BorderPane {

    private static abstract class MenuEntry {
        String label;
        List<String> options;
        int selectedOptionIndex;
        void onSelected() {}
        abstract void onOptionSelected();
    }

    private final PacManGamesUI ui;
    private final Canvas canvas = new Canvas();
    private float scaling = 2;
    private Font arcadeFont;

    private final List<MenuEntry> entries = new ArrayList<>();
    private int selectedEntryIndex = 0;

    public BootMenu(PacManGamesUI ui) {
        this.ui = ui;

        ResourceManager rm = () -> GameRenderer.class;
        arcadeFont = rm.loadFont("fonts/emulogic.ttf", 8);

        canvas.setWidth(scaling * 36 * TS);
        canvas.setHeight(scaling * 36 * TS);
        setCenter(canvas);

        {
            var entry = new MenuEntry() {
                {
                    label = "GAME VARIANT";
                    options = List.of("PAC-MAN", "MS. PAC-MAN");
                    selectedOptionIndex = 0;
                }
                @Override
                void onOptionSelected() {
                    switch (selectedOptionIndex) {
                        case 0 -> ui.selectGameVariant(GameVariant.PACMAN);
                        case 1 -> ui.selectGameVariant(GameVariant.MS_PACMAN);
                    }
                }
            };
            entries.add(entry);
        }
        {
            var entry = new MenuEntry() {
                {
                    label = "CUT SCENES";
                    options = List.of("ENABLED", "DISABLED");
                    selectedOptionIndex = 1;
                }
                @Override
                void onOptionSelected() {
                }
            };
            entries.add(entry);
        }
        {
            var entry = new MenuEntry() {
                {
                    label = "CUSTOM MAPS";
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
            };
            entries.add(entry);
        }

        ui.getMainScene().addEventHandler(KeyEvent.KEY_PRESSED, e -> {
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
            }
        });

        // initialize
        for (MenuEntry entry : entries) {
            entry.onOptionSelected();
        }

        Timeline loop = new Timeline(new KeyFrame(Duration.millis(16), e -> {
            drawContent();
        }));
        loop.setCycleCount(Animation.INDEFINITE);
        loop.play();
    }

    private void drawContent() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.save();
        g.scale(scaling, scaling);
        g.setFont(arcadeFont);

        g.setFill(Color.BLACK);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        for (int i = 0; i < entries.size(); ++i) {
            int y =  (4 + 3*i) * TS;
            MenuEntry entry = entries.get(i);
            if (i == selectedEntryIndex) {
                g.setFill(Color.YELLOW);
                g.fillText("->", TS, y);
            }
            g.setFill(Color.YELLOW);
            g.fillText(entry.label, 3 * TS, y);
            g.setFill(Color.WHITE);
            g.fillText(entry.options.get(entry.selectedOptionIndex), 16 * TS, y);
        }
        g.restore();
    }
}