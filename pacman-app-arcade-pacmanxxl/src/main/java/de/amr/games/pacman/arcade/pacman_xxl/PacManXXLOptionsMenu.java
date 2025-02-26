package de.amr.games.pacman.arcade.pacman_xxl;

import de.amr.games.pacman.model.CustomMapSelectionMode;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.GameRenderer;
import de.amr.games.pacman.ui2d.PacManGamesUI;
import de.amr.games.pacman.uilib.ResourceManager;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;

public class PacManXXLOptionsMenu extends BorderPane {

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
    private final Font arcadeFont8;
    private final Font arcadeFont12;

    private final List<MenuEntry> entries = new ArrayList<>();
    private int selectedEntryIndex = 0;

    public PacManXXLOptionsMenu(PacManGamesUI ui) {
        ResourceManager rm = () -> GameRenderer.class;
        arcadeFont8 = rm.loadFont("fonts/emulogic.ttf", 8);
        arcadeFont12 = rm.loadFont("fonts/emulogic.ttf", 12);

        double unscaledHeight = 36 * TS;
        scalingPy.bind(ui.getMainScene().heightProperty().divide(unscaledHeight));
        canvas.widthProperty().bind(scalingPy.multiply(unscaledHeight));
        canvas.heightProperty().bind(scalingPy.multiply(unscaledHeight));
        setCenter(canvas);

        entries.add(new MenuEntry("GAME VARIANT") {
            {
                options = List.of("PAC-MAN", "MS.PAC-MAN (TODO)");
                selectedOptionIndex = 0;
            }
            @Override
            void onOptionSelected() {
                switch (selectedOptionIndex) {
                    case 0 -> ui.selectGameVariant(GameVariant.PACMAN_XXL);
                    case 1 -> ui.selectGameVariant(GameVariant.MS_PACMAN);
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

        Timeline loop = new Timeline(new KeyFrame(Duration.millis(1000.0/ 60), e -> draw()));
        loop.setCycleCount(Animation.INDEFINITE);
        loop.play();
    }

    private void draw() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.save();
        g.scale(scalingPy.doubleValue(), scalingPy.doubleValue());
        g.setFill(Color.BLACK);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        g.setFill(Color.WHITE);
        g.fillRect(0, 2*TS, getWidth(), TS);
        g.setFill(Color.CORNFLOWERBLUE);
        g.fillRect(0, 2*TS+1, getWidth(), TS-2);
        drawContent(g);
        g.setFill(Color.WHITE);
        g.fillRect(0, 34*TS, getWidth(), TS);
        g.setFill(Color.CORNFLOWERBLUE);
        g.fillRect(0, 34*TS+1, getWidth(), TS-2);
        g.restore();
    }

    private void drawContent(GraphicsContext g) {
        g.setFont(arcadeFont12);
        g.setFill(Color.grayRgb(200));
        g.fillText("PAC-MAN XXL OPTIONS", 4 * TS, 8 * TS);
        g.setFont(arcadeFont8);
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
        g.fillText("   PRESS SPACE TO CHANGE OPTIONS    ", 0, 23 * TS);
        g.fillText("  CHOOSE OPTIONS WITH UP AND DOWN   ", 0, 25 * TS);
        g.fillText("     PRESS ENTER TO START GAME      ", 0, 27 * TS);
    }
}