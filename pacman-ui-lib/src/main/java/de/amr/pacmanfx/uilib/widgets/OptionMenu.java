/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.widgets;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.AudioClip;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;

public class OptionMenu {

    private final int numTilesX;
    private final int numTilesY;
    private final int textColumn;
    private final int valueColumn;

    private final List<OptionMenuEntry<?>> entries = new ArrayList<>();
    private final BooleanProperty soundEnabled = new SimpleBooleanProperty(true);
    private final FloatProperty scaling = new SimpleFloatProperty(2);

    private int selectedEntryIndex = 0;
    private String title = "OPTIONS";

    protected final BorderPane root = new BorderPane();
    protected final Canvas canvas = new Canvas();
    protected OptionMenuRenderer renderer;
    protected OptionMenuStyle style;

    public OptionMenu(int numTilesX, int numTilesY, int textColumn, int valueColumn) {
        this.numTilesX = numTilesX;
        this.numTilesY = numTilesY;
        this.textColumn = textColumn;
        this.valueColumn = valueColumn;

        canvas.widthProperty() .bind(scaling.multiply(numTilesX * TS));
        canvas.heightProperty().bind(scaling.multiply(numTilesY * TS));

        renderer = new OptionMenuRenderer(canvas);
        renderer.scalingProperty().bind(scalingProperty());

        root.maxWidthProperty().bind(canvas.widthProperty());
        root.maxHeightProperty().bind(canvas.heightProperty());
        root.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPress);
        root.setCenter(canvas);

        setStyle(OptionMenuStyle.DEFAULT_OPTION_MENU_STYLE);
    }

    public Canvas canvas() {
        return canvas;
    }

    public void setRenderer(OptionMenuRenderer renderer) {
        this.renderer = renderer;
        renderer.scalingProperty().bind(scalingProperty());
    }

    public void requestFocus() {
        final Canvas canvas = renderer.ctx().getCanvas();
        if (!canvas.isFocused()) {
            canvas.requestFocus();
            Logger.info("Focus now on {}", canvas);
        }
    }

    protected void playSoundIfPresent(AudioClip clip) {
        if (clip != null && soundEnabled.get()) {
            clip.play();
        }
    }

    public BooleanProperty soundEnabledProperty() { return soundEnabled; }

    public final void draw() {
        renderer.drawOptionMenu(this);
    }

    protected void handleKeyPress(KeyEvent e) {
        switch (e.getCode()) {
            case UP -> {
                e.consume();
                selectedPrevEntry();
            }
            case DOWN -> {
                e.consume();
                selectNextEntry();
            }
            case SPACE -> {
                e.consume();
                selectNextValue();
            }
        }
    }

    private void selectedPrevEntry() {
        final int prevIndex = selectedEntryIndex > 0 ? selectedEntryIndex - 1 : entries.size() - 1;
        if (entries.get(prevIndex).enabled) {
            selectedEntryIndex = prevIndex;
            playSoundIfPresent(style.entrySelectedSound());
        }
    }

    private void selectNextEntry() {
        final int nextIndex = selectedEntryIndex < entries.size() - 1 ? selectedEntryIndex + 1 : 0;
        if (entries.get(nextIndex).enabled) {
            selectedEntryIndex = nextIndex;
            playSoundIfPresent(style.entrySelectedSound());
        }
    }

    private void selectNextValue() {
        final OptionMenuEntry<?> entry = entries.get(selectedEntryIndex);
        entry.selectedValueIndex = entry.selectedValueIndex < entry.optionValues.size() - 1
            ? entry.selectedValueIndex + 1 : 0;
        playSoundIfPresent(style.valueSelectedSound());
        entry.onValueSelectionChange();
    }

    public Node root() { return root; }

    public int numTilesX() {
        return numTilesX;
    }

    public int numTilesY() {
        return numTilesY;
    }

    public FloatProperty scalingProperty() { return scaling; }

    public float scaling() {
        return scalingProperty().get();
    }

    public List<OptionMenuEntry<?>> entries() {
        return Collections.unmodifiableList(entries);
    }

    public void addEntry(OptionMenuEntry<?> entry) { entries.add(requireNonNull(entry)); }

    public int selectedEntryIndex() {
        return selectedEntryIndex;
    }

    public OptionMenuEntry<?> selectedEntry(){
        return entries.isEmpty() ? null : entries.get(selectedEntryIndex);
    }

    public String title() {
        return title;
    }

    public void setTitle(String title) { this.title = requireNonNull(title); }

    public OptionMenuStyle style() {
        return style;
    }

    public void setStyle(OptionMenuStyle style) {
        this.style = requireNonNull(style);
        root.setBackground(new Background(new BackgroundFill(style.backgroundFill(), null, null)));
        root.setBorder(Border.stroke(style.borderStroke()));
        draw();
    }

    public int textColumn() {
        return textColumn;
    }

    public int valueColumn() {
        return valueColumn;
    }
}