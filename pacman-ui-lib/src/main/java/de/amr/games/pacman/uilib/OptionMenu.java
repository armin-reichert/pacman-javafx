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

import static de.amr.games.pacman.Globals.*;

public class OptionMenu {

    public static abstract class MenuEntry<T> {
        protected boolean enabled;
        protected final String text;
        protected List<T> values;
        protected int selectedIndex;

        public MenuEntry(String text, List<T> values) {
            enabled = true;
            this.text = assertNotNull(text);
            assertNotNull(values);
            if (values.isEmpty()) {
                throw new IllegalArgumentException("Menu entry must provide at least one value");
            }
            this.values = new ArrayList<>(values);
            this.selectedIndex = 0;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int selectedIndex() { return selectedIndex; }

        public void setSelectedIndex(int index) {
            this.selectedIndex = index;
        }

        public void selectValue(T value) {
            for (int i = 0; i < values.size(); ++i) {
                if (values.get(i).equals(value)) {
                    selectedIndex = i;
                    return;
                }
            }
            throw new IllegalArgumentException("Cannot select value " + value);
        }

        public T selectedValue() { return values.get(selectedIndex); }

        public String selectedValueText() { return String.valueOf(selectedValue()); }

        protected void onSelect() {}

        protected abstract void onValueChange(int index);
    }

    private final List<OptionMenu.MenuEntry<?>> entries = new ArrayList<>();

    private int selectedEntryIndex = 0;
    private final AudioClip selectEntrySound;
    private final AudioClip selectValueSound;

    private Runnable actionOnStart;

    private String title = "123456789012345";

    private final BorderPane root = new BorderPane();
    protected final Canvas canvas = new Canvas();
    protected final FloatProperty scalingPy = new SimpleFloatProperty(2);

    protected final Font arcadeFont8;
    protected final Font arcadeFont20;

    protected Paint backgroundFill = Color.BLACK;
    protected Paint borderStroke = Color.WHITESMOKE;
    protected Paint titleTextFill = Color.RED;
    protected Paint entryTextFill = Color.YELLOW;
    protected Paint entryValueFill = Color.WHITESMOKE;
    protected Paint entryValueDisabledFill = Color.GRAY;
    protected Paint hintTextFill = Color.YELLOW;

    public OptionMenu(float height) {
        root.setCenter(canvas);
        root.setBorder(Border.stroke(borderStroke));

        ResourceManager rm = () -> OptionMenu.class;
        arcadeFont8 = rm.loadFont("fonts/emulogic.ttf", 8);
        arcadeFont20 = rm.loadFont("fonts/emulogic.ttf", 20);

        selectEntrySound = rm.loadAudioClip("sounds/menu-select1.wav");
        selectValueSound = rm.loadAudioClip("sounds/menu-select2.wav");

        root.maxWidthProperty().bind(scalingPy.multiply(height));
        root.maxHeightProperty().bind(scalingPy.multiply(height));

        canvas.widthProperty().bind(scalingPy.multiply(height));
        canvas.heightProperty().bind(scalingPy.multiply(height));

        root.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPress);
    }

    protected void handleKeyPress(KeyEvent e) {
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
                MenuEntry<?> entry = entries.get(selectedEntryIndex);
                entry.selectedIndex++;
                if (entry.selectedIndex == entry.values.size()) entry.selectedIndex = 0;
                entry.onValueChange(entry.selectedIndex);
                selectValueSound.play();
            }
            case ENTER -> {
                if (actionOnStart != null) {
                    actionOnStart.run();
                }
            }
        }
    }

    public Node root() { return root; }

    public FloatProperty scalingProperty() { return scalingPy; }

    public void addEntry(MenuEntry<?> entry) {
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

    public void setEntryValueDisabledFill(Paint entryValueDisabledFill) {
        this.entryValueDisabledFill = entryValueDisabledFill;
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

        g.setFont(arcadeFont20);
        g.setFill(titleTextFill);
        g.fillText(title, 0, 6 * TS);
        g.setFont(arcadeFont8);

        for (int i = 0; i < entries.size(); ++i) {
            int y = (12 + 3 * i) * TS;
            MenuEntry<?> entry = entries.get(i);
            if (i == selectedEntryIndex) {
                g.setFill(entryTextFill);
                g.fillText("-", TS, y);
                g.fillText(">", TS+HTS, y);
            }
            g.setFill(entryTextFill);
            g.fillText(entry.text, 3 * TS, y);
            g.setFill(entry.enabled ? entryValueFill : entryValueDisabledFill);
            g.fillText(entry.selectedValueText(), 17 * TS, y);
        }

        g.setFill(hintTextFill);
        g.fillText("  SELECT OPTIONS WITH UP AND DOWN   ", 0, 29 * TS);
        g.fillText("   PRESS SPACE TO CHANGE OPTIONS    ", 0, 31 * TS);
        g.fillText("        PRESS ENTER TO START        ", 0, 33 * TS);

        g.restore();
    }
}