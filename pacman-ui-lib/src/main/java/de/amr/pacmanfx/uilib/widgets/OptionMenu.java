/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.widgets;

import de.amr.pacmanfx.model.world.WorldMap;
import javafx.animation.AnimationTimer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioClip;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class OptionMenu {

    public static final int NUM_CLIENT_ACTIONS = 2;

    public record Raster(int numTilesX, int numTilesY, int textColumn, int valueColumn) {}

    private final Raster raster;

    private final List<OptionMenuEntry<?>> entries = new ArrayList<>();

    private final BooleanProperty soundEnabled = new SimpleBooleanProperty(true);

    private final FloatProperty scaling = new SimpleFloatProperty(2);

    private int selectedEntryIndex = 0;
    private String title = "OPTIONS";

    protected final BorderPane root = new BorderPane();
    protected final Canvas canvas = new Canvas();
    protected OptionMenuRenderer renderer;
    protected OptionMenuStyle style;

    private final AnimationTimer drawLoop;

    private KeyCode keyUp = KeyCode.UP;
    private KeyCode keyDown = KeyCode.DOWN;
    private KeyCode keyNextValue = KeyCode.RIGHT;

    private final KeyCode[] actionKeys = new KeyCode[NUM_CLIENT_ACTIONS];
    private final String[]  actionTexts = new String[NUM_CLIENT_ACTIONS];

    public OptionMenu(Raster raster) {
        this.raster = requireNonNull(raster);

        canvas.widthProperty() .bind(scaling.multiply(raster.numTilesX() * WorldMap.TS));
        canvas.heightProperty().bind(scaling.multiply(raster.numTilesY() * WorldMap.TS));

        canvas.focusedProperty().addListener((_, _, _) -> Logger.debug("Option menu canvas has focus"));

        renderer = new OptionMenuRenderer(canvas);
        renderer.scalingProperty().bind(scalingProperty());

        root.maxWidthProperty().bind(canvas.widthProperty());
        root.maxHeightProperty().bind(canvas.heightProperty());
        root.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressedEvent);
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
            Logger.debug("Focus now on {}", canvas);
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

    public void logMenuState() {}

    public void setKeyNextValue(KeyCode keyCode) {
        this.keyNextValue = requireNonNull(keyCode);
    }

    public Pane rootPane() { return root; }

    public Raster raster() {
        return raster;
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
        root.setBackground(Background.fill(style.backgroundFill()));
        root.setBorder(Border.stroke(style.borderStroke()));
    }

    /**
     * @param n action number (1, 2, ...)
     * @param key action key
     * @param text action text
     */
    public void defineAction(int n, KeyCode key, String text) {
        validateActionNumber(n);
        actionKeys [n-1] = requireNonNull(key);
        actionTexts[n-1] = requireNonNull(text);
    }

    /**
     * @param n action number (1, 2, ...)
     */
    public KeyCode actionKey(int n) {
        validateActionNumber(n);
        return actionKeys[n-1];
    }

    /**
     * @param n action number (1, 2, ...)
     */
    public String actionText(int n) {
        validateActionNumber(n);
        return actionTexts[n-1];
    }

    private void validateActionNumber(int n) {
        if (n < 1 || n > NUM_CLIENT_ACTIONS) {
            throw new IllegalArgumentException("Illegal action number: " + n);
        }
    }

    private void handleKeyPressedEvent(KeyEvent e) {
        if (e.getCode() == keyUp) {
            goUp();
            e.consume();
        }
        else if (e.getCode() == keyDown) {
            goDown();
            e.consume();
        }
        else if (e.getCode() == keyNextValue) {
            nextValue();
            e.consume();
        }
    }

    private void goUp() {
        final int i = selectedEntryIndex > 0 ? selectedEntryIndex - 1 : entries.size() - 1;
        if (entries.get(i).enabled) {
            selectedEntryIndex = i;
            playSoundIfPresent(style.entrySelectedSound());
        }
    }

    private void goDown() {
        final int i = selectedEntryIndex < entries.size() - 1 ? selectedEntryIndex + 1 : 0;
        if (entries.get(i).enabled) {
            selectedEntryIndex = i;
            playSoundIfPresent(style.entrySelectedSound());
        }
    }

    private void nextValue() {
        final OptionMenuEntry<?> entry = entries.get(selectedEntryIndex);
        entry.setNextValue();
        playSoundIfPresent(style.valueSelectedSound());
        logMenuState();
    }
}