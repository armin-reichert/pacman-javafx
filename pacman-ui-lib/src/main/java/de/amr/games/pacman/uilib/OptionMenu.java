/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.uilib;

import javafx.animation.AnimationTimer;
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
import javafx.scene.text.Font;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.amr.games.pacman.Globals.HTS;
import static de.amr.games.pacman.Globals.TS;

public class OptionMenu {

    private static final float TITLE_FONT_SCALING = 3f;

    private final int tilesX;
    private final int tilesY;

    private final List<OptionMenuEntry<?>> entries = new ArrayList<>();

    private int selectedEntryIndex = 0;
    private final AudioClip selectEntrySound;
    private final AudioClip selectValueSound;

    private Runnable actionOnStart;

    private String title = "";
    private String[] commandTexts = new String[0];

    private final BorderPane root = new BorderPane();
    protected final Canvas canvas = new Canvas();
    protected final FloatProperty scalingPy = new SimpleFloatProperty(2);

    protected final Font textFont;
    protected final Font titleFont;

    protected Color backgroundFill = Color.BLACK;
    protected Color borderStroke = Color.web("fffeff");
    protected Color titleTextFill = Color.web("b53120");
    protected Color entryTextFill = Color.web("bcbe00");
    protected Color entryValueFill = Color.web("fffeff");
    protected Color entryValueDisabledFill = Color.GRAY;
    protected Color hintTextFill = Color.web("bcbe00");

    private final AnimationTimer drawingTimer = new AnimationTimer() {
        @Override
        public void handle(long now) { draw(); }
    };

    public OptionMenu(int tilesX, int tilesY) {
        this.tilesX = tilesX;
        this.tilesY = tilesY;
        float height = tilesY * TS;

        setTitle("OPTIONS");
        setCommandTexts(
            "SELECT OPTIONS WITH UP AND DOWN",
            "PRESS SPACE TO CHANGE VALUE",
            "PRESS ENTER TO START"
        );

        root.setCenter(canvas);
        root.setBorder(Border.stroke(borderStroke));

        ResourceManager rm = () -> OptionMenu.class;
        textFont = rm.loadFont("fonts/emulogic.ttf", TS);
        titleFont = rm.loadFont("fonts/emulogic.ttf", TITLE_FONT_SCALING * TS);

        selectEntrySound = rm.loadAudioClip("sounds/menu-select1.wav");
        selectValueSound = rm.loadAudioClip("sounds/menu-select2.wav");

        root.maxWidthProperty().bind(scalingPy.multiply(height));
        root.maxHeightProperty().bind(scalingPy.multiply(height));

        canvas.widthProperty().bind(scalingPy.multiply(height));
        canvas.heightProperty().bind(scalingPy.multiply(height));

        canvas.focusedProperty().addListener((py, ov, nv) -> {
            Logger.info("Canvas has focus: {}" , nv);
        });
        root.borderProperty().bind(canvas.focusedProperty()
                .map(hasFocus -> hasFocus ? Border.stroke(Color.LIGHTBLUE) : Border.stroke(borderStroke)));

        root.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPress);
    }

    public void startDrawing() {
        drawingTimer.start();
        Logger.trace("Menu drawing started");
    }

    public void stopDrawing() {
        drawingTimer.stop();
        Logger.trace("Menu drawing stopped");
    }


    public void draw() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(backgroundFill);
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        g.save();
        g.scale(scalingPy.doubleValue(), scalingPy.doubleValue());

        float x = (tilesX - TITLE_FONT_SCALING * title.length()) * 0.5f * TS;
        g.setFont(titleFont);
        g.setFill(titleTextFill);
        g.fillText(title, x, 6 * TS);
        g.setFont(textFont);

        for (int i = 0; i < entries.size(); ++i) {
            int y = (12 + 3 * i) * TS;
            OptionMenuEntry<?> entry = entries.get(i);
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
        int line = tilesY - 2 * commandTexts.length;
        for (String commandText : commandTexts) {
             int ox = (tilesX - commandText.length()) / 2;
            g.fillText(commandText, ox * TS, line * TS);
            line += 2;
        }
        g.restore();
    }

    protected void handleKeyPress(KeyEvent e) {
        switch (e.getCode()) {
            case DOWN -> {
                selectEntrySound.play();
                selectedEntryIndex++;
                if (selectedEntryIndex == entries.size()) selectedEntryIndex = 0;
            }
            case UP -> {
                selectEntrySound.play();
                selectedEntryIndex--;
                if (selectedEntryIndex == -1) selectedEntryIndex = entries.size() - 1;
            }
            case SPACE -> {
                selectValueSound.play();
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

    public Canvas canvas() {
        return canvas;
    }

    public FloatProperty scalingProperty() { return scalingPy; }

    public void addEntry(OptionMenuEntry<?> entry) {
        entries.add(entry);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setBackgroundFill(Color backgroundFill) {
        this.backgroundFill = backgroundFill;
    }

    public void setBorderStroke(Color borderStroke) {
        this.borderStroke = borderStroke;
    }

    public void setEntryTextFill(Color entryTextFill) {
        this.entryTextFill = entryTextFill;
    }

    public void setEntryValueFill(Color entryValueFill) {
        this.entryValueFill = entryValueFill;
    }

    public void setEntryValueDisabledFill(Color entryValueDisabledFill) {
        this.entryValueDisabledFill = entryValueDisabledFill;
    }

    public void setHintTextFill(Color hintTextFill) {
        this.hintTextFill = hintTextFill;
    }

    public void setTitleTextFill(Color titleTextFill) {
        this.titleTextFill = titleTextFill;
    }

    public void setOnStart(Runnable action) {
        this.actionOnStart = action;
    }

    public void setCommandTexts(String... lines) {
        commandTexts = Arrays.copyOf(lines, lines.length);
    }

    public int tilesX() {
        return tilesX;
    }

    public int tilesY() {
        return tilesY;
    }
}