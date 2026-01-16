/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.widgets;

import javafx.animation.AnimationTimer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
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

    public static final int NUM_CLIENT_ACTIONS = 2;

    private final int numTilesX;
    private final int numTilesY;
    private final int textColumn;
    private final int valueColumn;

    private final List<OptionMenuEntry<?>> entries = new ArrayList<>();
    private final BooleanProperty soundEnabled = new SimpleBooleanProperty(true);
    private final FloatProperty scaling = new SimpleFloatProperty(2);

    private KeyCode nextEntryKeyCode = KeyCode.DOWN;
    private KeyCode prevEntryKeyCode = KeyCode.UP;
    private KeyCode nextValueKeyCode = KeyCode.RIGHT;

    private int selectedEntryIndex = 0;
    private String title = "OPTIONS";

    protected final BorderPane root = new BorderPane();
    protected final Canvas canvas = new Canvas();
    protected OptionMenuRenderer renderer;
    protected OptionMenuStyle style;

    private final AnimationTimer drawLoop;

    private final KeyCode[] actionKeyCodes = new KeyCode[NUM_CLIENT_ACTIONS];
    private final String[]  actionTexts = new String[NUM_CLIENT_ACTIONS];

    public OptionMenu(int numTilesX, int numTilesY, int textColumn, int valueColumn) {
        this.numTilesX = numTilesX;
        this.numTilesY = numTilesY;
        this.textColumn = textColumn;
        this.valueColumn = valueColumn;

        canvas.widthProperty() .bind(scaling.multiply(numTilesX * TS));
        canvas.heightProperty().bind(scaling.multiply(numTilesY * TS));

        canvas.focusedProperty().addListener((_, _, _) -> Logger.info("Option menu canvas has focus"));

        renderer = new OptionMenuRenderer(canvas);
        renderer.scalingProperty().bind(scalingProperty());

        root.maxWidthProperty().bind(canvas.widthProperty());
        root.maxHeightProperty().bind(canvas.heightProperty());
        root.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPress);
        root.setCenter(canvas);

        setStyle(OptionMenuStyle.DEFAULT_OPTION_MENU_STYLE);

        drawLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                draw();
            }
        };
    }

    public void startDrawLoop() {
        drawLoop.start();
    }

    public void stopDrawLoop() {
        drawLoop.stop();
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

    public void draw() {
        renderer.drawOptionMenu(this);
    }

    public void logEntryState() {}

    public void setNextEntryKeyCode(KeyCode keyCode) {
        this.nextEntryKeyCode = keyCode;
    }

    public void setPrevEntryKeyCode(KeyCode keyCode) {
        this.prevEntryKeyCode = keyCode;
    }

    public void setNextValueKeyCode(KeyCode keyCode) {
        this.nextValueKeyCode = keyCode;
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
    }

    public int textColumn() {
        return textColumn;
    }

    public int valueColumn() {
        return valueColumn;
    }

    public void defineAction(int num, KeyCode keyCode, String text) {
        validateActionNumber(num);
        actionKeyCodes[num-1] = requireNonNull(keyCode);
        actionTexts[num-1] = requireNonNull(text);
    }

    public KeyCode actionKeyCode(int num) {
        validateActionNumber(num);
        return actionKeyCodes[num-1];
    }

    public String actionText(int num) {
        validateActionNumber(num);
        return actionTexts[num-1];
    }

    private void validateActionNumber(int num) {
        if (num < 1 || num > NUM_CLIENT_ACTIONS) {
            throw new IllegalArgumentException("Illegal client action number: " + num);
        }
    }

    private void handleKeyPress(KeyEvent e) {
        if (e.getCode() == prevEntryKeyCode) {
            selectedPrevEntry();
            e.consume();
        }
        else if (e.getCode() == nextEntryKeyCode) {
            selectNextEntry();
            e.consume();
        }
        else  if (e.getCode() == nextValueKeyCode) {
            selectNextValue();
            e.consume();
        }
    }

    private void selectedPrevEntry() {
        final int prevIndex = selectedEntryIndex > 0 ? selectedEntryIndex - 1 : entries.size() - 1;
        if (entries.get(prevIndex).enabled) {
            selectedEntryIndex = prevIndex;
            playSoundIfPresent(style.entrySelectedSound());
        }
        logEntryState();
    }

    private void selectNextEntry() {
        final int nextIndex = selectedEntryIndex < entries.size() - 1 ? selectedEntryIndex + 1 : 0;
        if (entries.get(nextIndex).enabled) {
            selectedEntryIndex = nextIndex;
            playSoundIfPresent(style.entrySelectedSound());
        }
        logEntryState();
    }

    private void selectNextValue() {
        final OptionMenuEntry<?> entry = entries.get(selectedEntryIndex);
        entry.selectedValueIndex = entry.selectedValueIndex < entry.optionValues.size() - 1
            ? entry.selectedValueIndex + 1 : 0;
        playSoundIfPresent(style.valueSelectedSound());
        entry.onValueSelectionChange();
        logEntryState();
    }
}