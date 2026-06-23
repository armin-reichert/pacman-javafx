/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.uilib.widgets;

import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.uilib.SettingsLoader;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.animation.AnimationTimer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.AudioClip;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class OptionMenu {

    public static final OptionMenuSettings DEFAULT_SETTINGS = SettingsLoader.load(
        OptionMenu.class.getResource("option-menu.json"),
        OptionMenuSettings.class);

    private static final AudioClip DEFAULT_ENTRY_SELECTION_SOUND;
    private static final AudioClip DEFAULT_VALUE_SELECTION_SOUND;

    static {
        final ResourceManager RM = () -> OptionMenu.class;
        DEFAULT_ENTRY_SELECTION_SOUND = RM.loadAudioClip("sounds/menu-select1.wav");
        DEFAULT_VALUE_SELECTION_SOUND = RM.loadAudioClip("sounds/menu-select2.wav");
    }

    public static final int NUM_CLIENT_ACTIONS = 2;

    public record Layout(int numTilesX, int numTilesY, int textColumn, int valueColumn) {}

    private final Layout layout;

    private final List<OptionMenuEntry<?>> entries = new ArrayList<>();

    private final BooleanProperty soundEnabled = new SimpleBooleanProperty(true);

    private final FloatProperty scaling = new SimpleFloatProperty(2);

    private int selectedEntryIndex = 0;
    private String title = "OPTIONS";

    protected final BorderPane root = new BorderPane();
    protected final Canvas canvas = new Canvas();
    protected OptionMenuRenderer renderer;

    protected OptionMenuSettings settings;

    private final AnimationTimer drawLoop;

    private AudioClip entrySelectedSound = DEFAULT_ENTRY_SELECTION_SOUND;
    private AudioClip valueSelectedSound = DEFAULT_VALUE_SELECTION_SOUND;

    private final KeyCode keyUp = KeyCode.UP;
    private final KeyCode keyDown = KeyCode.DOWN;
    private KeyCode keyNextValue = KeyCode.RIGHT;

    private final KeyCode[] actionKeys = new KeyCode[NUM_CLIENT_ACTIONS];
    private final String[]  actionTexts = new String[NUM_CLIENT_ACTIONS];

    public OptionMenu(Layout layout) {
        this(layout, DEFAULT_SETTINGS);
    }

    public OptionMenu(Layout layout, OptionMenuSettings settings) {
        this.layout = requireNonNull(layout);
        this.settings = requireNonNull(settings);

        canvas.widthProperty() .bind(scaling.multiply(layout.numTilesX() * WorldMap.TS));
        canvas.heightProperty().bind(scaling.multiply(layout.numTilesY() * WorldMap.TS));

        canvas.focusedProperty().addListener((_, _, _) -> Logger.debug("Option menu canvas has focus"));

        renderer = new OptionMenuRenderer(canvas);
        renderer.scalingProperty().bind(scalingProperty());

        root.getStyleClass().add("option-menu");
        root.maxWidthProperty().bind(canvas.widthProperty());
        root.maxHeightProperty().bind(canvas.heightProperty());
        root.addEventFilter(KeyEvent.KEY_PRESSED, this::handleKeyPressedEvent);
        root.setCenter(canvas);

        drawLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                draw();
            }
        };
    }

    public void setEntrySelectedSound(AudioClip entrySelectedSound) {
        this.entrySelectedSound = entrySelectedSound;
    }

    public void setValueSelectedSound(AudioClip valueSelectedSound) {
        this.valueSelectedSound = valueSelectedSound;
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

    public Layout layout() {
        return layout;
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

    public String title() {
        return title;
    }

    public void setTitle(String title) { this.title = requireNonNull(title); }

    public OptionMenuSettings settings() {
        return settings;
    }

/*
    public void setStyle(OptionMenuStyle posture) {
        this.posture = requireNonNull(posture);
        root.setBackground(Background.fill(posture.backgroundFill()));
        root.setBorder(Border.stroke(posture.borderStroke()));
    }
 */

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
        if (entries.get(i).isEnabled()) {
            selectedEntryIndex = i;
            playSoundIfPresent(entrySelectedSound);
        }
    }

    private void goDown() {
        final int i = selectedEntryIndex < entries.size() - 1 ? selectedEntryIndex + 1 : 0;
        if (entries.get(i).isEnabled()) {
            selectedEntryIndex = i;
            playSoundIfPresent(entrySelectedSound);
        }
    }

    private void nextValue() {
        final OptionMenuEntry<?> entry = entries.get(selectedEntryIndex);
        entry.setNextValue();
        playSoundIfPresent(valueSelectedSound);
        logMenuState();
    }
}