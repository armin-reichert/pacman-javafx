/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.uilib;

import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;

public class OptionMenu {

    private static final int WIDTH_IN_TILES = 36; // TODO
    private static final float UNSCALED_HEIGHT = 36 * TS;

    public static abstract class MenuEntry {
        protected final String label;
        protected List<String> options;
        protected int valueIndex;

        public MenuEntry(String label, List<String> options) {
            this.label = label;
            this.options = new ArrayList<>(options);
        }

        public int getValueIndex() {
            return valueIndex;
        }

        public void setValueIndex(int valueIndex) {
            this.valueIndex = valueIndex;
        }

        protected void onSelect() {}
        protected abstract void onValueChange();
    }

    private final List<OptionMenu.MenuEntry> entries = new ArrayList<>();

    private int selectedEntryIndex = 0;
    private Runnable actionOnStart;

    private String title = "<Add title>";

    private final BorderPane root = new BorderPane();
    private final Canvas canvas = new Canvas();
    private final FloatProperty scalingPy = new SimpleFloatProperty(2);

    private final Font arcadeFontNormal;
    private final Font arcadeFontBig;

    private Paint backgroundFill = Color.BLACK;
    private Paint borderStroke = Color.LIGHTGRAY;
    private Paint titleTextFill = Color.GREEN;
    private Paint entryTextFill = Color.YELLOW;
    private Paint entryValueFill = Color.WHITESMOKE;
    private Paint hintTextFill = Color.YELLOW;

    public OptionMenu(float height) {
        root.setCenter(canvas);
        root.setBorder(Border.stroke(borderStroke));

        ResourceManager rm = () -> ResourceManager.class;
        arcadeFontNormal = rm.loadFont("fonts/emulogic.ttf", 8);
        arcadeFontBig = rm.loadFont("fonts/emulogic.ttf", 20);

        AudioClip selectEntrySound = rm.loadAudioClip("sounds/menu-select1.wav");
        AudioClip selectValueSound = rm.loadAudioClip("sounds/menu-select2.wav");

        root.maxWidthProperty().bind(scalingPy.multiply(height));
        root.maxHeightProperty().bind(scalingPy.multiply(height));

        canvas.widthProperty().bind(scalingPy.multiply(height));
        canvas.heightProperty().bind(scalingPy.multiply(height));

        root.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            switch (e.getCode()) {
                case DOWN -> {
                    selectedEntryIndex++;
                    if (selectedEntryIndex == entries.size()) selectedEntryIndex = 0;
                    entries.get(selectedEntryIndex).onSelect();
                    selectEntrySound.play();
                }
                case UP -> {
                    selectedEntryIndex--;
                    if (selectedEntryIndex == -1) selectedEntryIndex = entries.size() - 1;
                    entries.get(selectedEntryIndex).onSelect();
                    selectEntrySound.play();
                }
                case SPACE -> {
                    MenuEntry entry = entries.get(selectedEntryIndex);
                    entry.valueIndex++;
                    if (entry.valueIndex == entry.options.size()) entry.valueIndex = 0;
                    entry.onValueChange();
                    selectValueSound.play();
                }
                case ENTER -> {
                    if (actionOnStart != null) {
                        actionOnStart.run();
                    }
                }
            }
        });
    }

    public Node root() { return root; }

    public FloatProperty scalingProperty() { return scalingPy; }
    public void addEntry(MenuEntry entry) {
        entries.add(entry);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setBackgroundFill(Paint backgroundFill) {
        this.backgroundFill = backgroundFill;
    }

    public void setBorderStroke(Paint borderStroke) {
        this.borderStroke = borderStroke;
    }

    public void setEntryTextFill(Paint entryTextFill) {
        this.entryTextFill = entryTextFill;
    }

    public void setEntryValueFill(Paint entryValueFill) {
        this.entryValueFill = entryValueFill;
    }

    public void setHintTextFill(Paint hintTextFill) {
        this.hintTextFill = hintTextFill;
    }

    public void setTitleTextFill(Paint titleTextFill) {
        this.titleTextFill = titleTextFill;
    }

    public void setOnStart(Runnable action) {
        this.actionOnStart = action;
    }

    public void draw() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(backgroundFill);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        g.save();
        g.scale(scalingPy.doubleValue(), scalingPy.doubleValue());

        int titleX = (WIDTH_IN_TILES - title.length()) / 2;
        g.setFont(arcadeFontBig);
        g.setFill(titleTextFill);
        g.fillText(title, 4 * TS, 6 * TS);
        g.setFont(arcadeFontNormal);

        for (int i = 0; i < entries.size(); ++i) {
            int y = (12 + 3 * i) * TS;
            MenuEntry entry = entries.get(i);
            if (i == selectedEntryIndex) {
                g.setFill(entryTextFill);
                g.fillText("-", TS, y);
                g.fillText(">", TS+HTS, y);
            }
            g.setFill(entryTextFill);
            g.fillText(entry.label, 3 * TS, y);
            g.setFill(entryValueFill);
            g.fillText(entry.options.get(entry.valueIndex), 17 * TS, y);
        }

        g.setFill(hintTextFill);
        g.fillText("   PRESS SPACE TO CHANGE OPTIONS    ", 0, 29 * TS);
        g.fillText("  CHOOSE OPTIONS WITH UP AND DOWN   ", 0, 31 * TS);
        g.fillText("          PRESS ENTER TO START      ", 0, 33 * TS);

        g.restore();
    }



}
