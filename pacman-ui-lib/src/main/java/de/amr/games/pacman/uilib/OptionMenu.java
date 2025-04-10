/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.uilib;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.amr.games.pacman.Globals.TS;
import static de.amr.games.pacman.Globals.assertNotNull;

public class OptionMenu implements ResourceManager {

    private static final int REFRESH_RATE = 30;

    public final OptionMenuStyle DEFAULT_STYLE = new OptionMenuStyle(
            loadFont("fonts/emulogic.ttf", 3 * TS),
            loadFont("fonts/emulogic.ttf", TS),
            Color.web("#0C1568"),
            Color.web("fffeff"),
            Color.web("ffffff"),
            Color.web("bcbe00"),
            Color.web("fffeff"),
            Color.GRAY,
            Color.web("bcbe00")
    );

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

    protected OptionMenuStyle style = DEFAULT_STYLE;
    protected AudioClip entrySelectedSound;
    protected AudioClip valueSelectedSound;

    private final Timeline drawingTimer;

    public OptionMenu(float height) {
        numTiles = (int) (height / TS);

        setTitle("OPTIONS");
        setCommandTexts(
            "SELECT OPTIONS WITH UP AND DOWN",
            "PRESS SPACE TO CHANGE VALUE",
            "PRESS ENTER TO START"
        );

        entrySelectedSound = loadAudioClip("sounds/menu-select1.wav");
        valueSelectedSound = loadAudioClip("sounds/menu-select2.wav");

        root.setCenter(canvas);
        root.setBorder(Border.stroke(style.borderStroke()));
        root.maxWidthProperty().bind(scalingPy.multiply(height));
        root.maxHeightProperty().bind(scalingPy.multiply(height));

        canvas.widthProperty().bind(scalingPy.multiply(height));
        canvas.heightProperty().bind(scalingPy.multiply(height));

        root.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPress);

        drawingTimer = new Timeline(REFRESH_RATE, new KeyFrame(Duration.seconds(1.0 / REFRESH_RATE), e-> draw()));
        drawingTimer.setCycleCount(Animation.INDEFINITE);
    }

    @Override
    public Class<?> resourceRootClass() {
        return OptionMenu.class;
    }

    public void requestFocus() {
        canvas.requestFocus();
    }

    private void playSound(AudioClip clip) {
        if (soundEnabledPy.get()) {
            clip.play();
        }
    }

    public BooleanProperty soundEnabledProperty() { return soundEnabledPy; }

    public void startDrawing() {
        drawingTimer.play();
        Logger.trace("Menu drawing started");
    }

    public void stopDrawing() {
        drawingTimer.stop();
        Logger.trace("Menu drawing stopped");
    }

    private void draw() {
        g.save();
        g.setFill(style.backgroundFill());
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        g.scale(scalingPy.doubleValue(), scalingPy.doubleValue());

        g.setFont(style.titleFont());
        g.setFill(style.titleTextFill());
        drawCentered(title, 6 * TS);

        g.setFont(style.textFont());
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

        g.setFill(style.hintTextFill());
        g.setFont(style.textFont());
        int ty = numTiles - 2 * commandTexts.length;
        for (String commandText : commandTexts) {
            drawCentered(commandText, ty * TS);
            ty += 2;
        }

        g.restore();
    }

    private void drawCentered(String text, double y) {
        g.save();
        g.setTextAlign(TextAlignment.CENTER);
        g.fillText(text, (canvas.getWidth() * 0.5) / scalingPy.get(), y);
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

    public void setStyle(OptionMenuStyle style) {
        this.style = assertNotNull(style);
    }

    public void setOnStart(Runnable action) {
        this.actionOnStart = action;
    }

    public void setCommandTexts(String... lines) {
        commandTexts = Arrays.copyOf(lines, lines.length);
    }
}