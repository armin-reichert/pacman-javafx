/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.uilib;

import javafx.animation.AnimationTimer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.amr.games.pacman.Globals.TS;
import static de.amr.games.pacman.Globals.assertNotNull;

public class OptionMenu implements ResourceManager {

    public record MenuStyle(
        Color backgroundFill,
        Color borderStroke,
        Color titleTextFill,
        Color entryTextFill,
        Color entryValueFill,
        Color entryValueDisabledFill,
        Color hintTextFill
    ) {}

    public static final MenuStyle DEFAULT_STYLE = new MenuStyle(
        Color.web("#0C1568"),
        Color.web("fffeff"),
        Color.web("ff0000"),
        Color.web("bcbe00"),
        Color.web("fffeff"),
        Color.GRAY,
        Color.web("bcbe00")
    );

    private static final float TITLE_FONT_SCALING = 3f;

    protected final int numTiles;
    protected final List<OptionMenuEntry<?>> entries = new ArrayList<>();
    protected final BooleanProperty soundEnabledPy = new SimpleBooleanProperty(true);
    protected final FloatProperty scalingPy = new SimpleFloatProperty(2);

    private int selectedEntryIndex = 0;
    private Runnable actionOnStart;

    private String title = "";
    private String[] commandTexts = new String[0];

    protected final BorderPane root = new BorderPane();
    protected final Canvas canvas = new Canvas();
    protected final GraphicsContext g = canvas.getGraphicsContext2D();

    protected MenuStyle style = DEFAULT_STYLE;
    protected Font textFont;
    protected Font titleFont;
    protected AudioClip entrySelectedSound;
    protected AudioClip valueSelectedSound;

    private final AnimationTimer drawingTimer = new AnimationTimer() {
        @Override
        public void handle(long now) { draw(); }
    };

    public OptionMenu(float height) {
        numTiles = (int) (height / TS);

        setTitle("OPTIONS");
        setCommandTexts(
            "SELECT OPTIONS WITH UP AND DOWN",
            "PRESS SPACE TO CHANGE VALUE",
            "PRESS ENTER TO START"
        );

        textFont = loadFont("fonts/emulogic.ttf", TS);
        titleFont = loadFont("fonts/emulogic.ttf", TITLE_FONT_SCALING * TS);
        entrySelectedSound = loadAudioClip("sounds/menu-select1.wav");
        valueSelectedSound = loadAudioClip("sounds/menu-select2.wav");

        root.setCenter(canvas);
        root.setBorder(Border.stroke(style.borderStroke()));
        root.maxWidthProperty().bind(scalingPy.multiply(height));
        root.maxHeightProperty().bind(scalingPy.multiply(height));

        canvas.widthProperty().bind(scalingPy.multiply(height));
        canvas.heightProperty().bind(scalingPy.multiply(height));

        root.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPress);
    }

    @Override
    public Class<?> resourceRootClass() {
        return OptionMenu.class;
    }

    private void playSound(AudioClip clip) {
        if (soundEnabledPy.get()) {
            clip.play();
        }
    }

    public BooleanProperty soundEnabledProperty() { return soundEnabledPy; }

    public void startDrawing() {
        drawingTimer.start();
        Logger.trace("Menu drawing started");
    }

    public void stopDrawing() {
        drawingTimer.stop();
        Logger.trace("Menu drawing stopped");
    }

    public void draw() {
        g.save();
        g.setFill(style.backgroundFill());
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        g.scale(scalingPy.doubleValue(), scalingPy.doubleValue());

        g.setFont(titleFont);
        drawAtTile((numTiles - TITLE_FONT_SCALING * title.length()) * 0.5f, 6, title, style.titleTextFill());

        g.setFont(textFont);
        for (int i = 0; i < entries.size(); ++i) {
            int y = (12 + 3 * i);
            OptionMenuEntry<?> entry = entries.get(i);
            if (i == selectedEntryIndex) {
                drawAtTile(1, y, "-", style.entryTextFill());
                drawAtTile(1.5f, y, ">", style.entryTextFill());
            }
            drawAtTile(3, y, entry.text, style.entryTextFill());
            drawAtTile(17, y, entry.selectedValueText(), entry.enabled ? style.entryValueFill() : style.entryValueDisabledFill());
        }

        int ty = numTiles - 2 * commandTexts.length;
        for (String commandText : commandTexts) {
            int tx = (numTiles - commandText.length()) / 2;
            drawAtTile(tx, ty, commandText, style.hintTextFill());
            ty += 2;
        }
        g.restore();
    }

    private void drawAtTile(float tileX, float tileY, String text, Color fillColor) {
        g.setFill(fillColor);
        g.fillText(text, tileX * TS, tileY * TS);
    }

    protected void handleKeyPress(KeyEvent e) {
        switch (e.getCode()) {
            case DOWN -> {
                playSound(entrySelectedSound);
                selectedEntryIndex++;
                if (selectedEntryIndex == entries.size()) selectedEntryIndex = 0;
            }
            case UP -> {
                playSound(entrySelectedSound);
                selectedEntryIndex--;
                if (selectedEntryIndex == -1) selectedEntryIndex = entries.size() - 1;
            }
            case SPACE -> {
                playSound(valueSelectedSound);
                OptionMenuEntry<?> entry = entries.get(selectedEntryIndex);
                entry.selectedValueIndex++;
                if (entry.selectedValueIndex == entry.valueList.size()) entry.selectedValueIndex = 0;
                entry.onValueChanged(entry.selectedValueIndex);
            }
            case ENTER -> {
                if (actionOnStart != null) {
                    actionOnStart.run();
                }
            }
        }
    }

    public Node root() { return root; }

    public int numTiles() {
        return numTiles;
    }

    public FloatProperty scalingProperty() { return scalingPy; }

    public void addEntry(OptionMenuEntry<?> entry) {
        entries.add(entry);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setStyle(MenuStyle style) {
        this.style = assertNotNull(style);
    }

    public void setOnStart(Runnable action) {
        this.actionOnStart = action;
    }

    public void setCommandTexts(String... lines) {
        commandTexts = Arrays.copyOf(lines, lines.length);
    }
}