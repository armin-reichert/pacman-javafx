/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.uilib.widgets;

import de.amr.games.pacman.uilib.assets.ResourceManager;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static de.amr.games.pacman.Globals.HTS;
import static de.amr.games.pacman.Globals.TS;
import static java.util.Objects.requireNonNull;

public class OptionMenu {

    private static final ResourceManager RESOURCE_MGR = () -> OptionMenu.class;

    private static final int REFRESH_RATE = 60;

    public static final OptionMenuStyle DEFAULT_STYLE = new OptionMenuStyle(
        Font.font("sans", FontWeight.BLACK, 30),
        Font.font("sans", FontWeight.BOLD, 12),
        Color.web("0c1568"),
        Color.web("fffeff"),
        Color.web("fffeff"),
        Color.web("bcbe00"),
        Color.web("fffeff"),
        Color.GRAY,
        Color.web("bcbe00"),
        RESOURCE_MGR.loadAudioClip("/de/amr/games/pacman/uilib/sounds/menu-select1.wav"),
        RESOURCE_MGR.loadAudioClip("/de/amr/games/pacman/uilib/sounds/menu-select2.wav")
    );

    private final int numTilesX;
    private final int numTilesY;
    private final int textCol;
    private final int valueCol;

    private final List<OptionMenuEntry<?>> entries = new ArrayList<>();
    private final BooleanProperty soundEnabledPy = new SimpleBooleanProperty(true);
    protected final FloatProperty scalingPy = new SimpleFloatProperty(2);

    private int selectedEntryIndex = 0;
    private String title = "";
    private String[] commandTexts = new String[0];

    private final BorderPane root = new BorderPane();
    protected final Canvas canvas = new Canvas();
    protected final GraphicsContext g = canvas.getGraphicsContext2D();

    private OptionMenuStyle style = DEFAULT_STYLE;
    private final Timeline animation;

    public OptionMenu(int numTilesX, int numTilesY, int textCol, int valueCol) {
        this.numTilesX = numTilesX;
        this.numTilesY = numTilesY;
        this.textCol = textCol;
        this.valueCol = valueCol;

        setTitle("OPTIONS");
        setCommandTexts(
            "SELECT OPTIONS WITH UP AND DOWN",
            "PRESS SPACE TO CHANGE VALUE"
        );

        canvas.widthProperty().bind(scalingPy.multiply(numTilesX*TS));
        canvas.heightProperty().bind(scalingPy.multiply(numTilesY*TS));

        root.setCenter(canvas);
        root.setBorder(Border.stroke(style.borderStroke()));
        root.maxWidthProperty().bind(canvas.widthProperty());
        root.maxHeightProperty().bind(canvas.heightProperty());
        root.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPress);

        animation = new Timeline(REFRESH_RATE,
            new KeyFrame(Duration.seconds(1.0 / REFRESH_RATE), e-> animationStep()));
        animation.setCycleCount(Animation.INDEFINITE);
    }

    protected void animationStep() {
        draw();
    }

    public void requestFocus() {
        canvas.requestFocus();
    }

    protected void playSound(AudioClip clip) {
        if (soundEnabledPy.get()) {
            clip.play();
        }
    }

    public BooleanProperty soundEnabledProperty() { return soundEnabledPy; }

    public void startAnimation() {
        animation.play();
        Logger.trace("Menu animation started");
    }

    public void stopAnimation() {
        animation.stop();
        Logger.trace("Menu animation stopped");
    }

    public void draw() {
        g.setFill(style.backgroundFill());
        g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        g.save();
        g.scale(scalingPy.get(), scalingPy.get());

        g.setFont(style.titleFont());
        g.setFill(style.titleTextFill());
        drawCentered(title, 6 * TS);

        g.setFont(style.textFont());
        for (int i = 0; i < entries.size(); ++i) {
            int y = (12 + 3 * i) * TS;
            OptionMenuEntry<?> entry = entries.get(i);
            if (i == selectedEntryIndex) {
                g.setFill(style.entryTextFill());
                g.fillText("-", (textCol - 2) * TS, y);
                g.fillText(">", (textCol - 2) * TS + HTS, y);
            }
            g.setFill(style.entryTextFill());
            g.fillText(entry.text, textCol * TS, y);
            g.setFill(entry.enabled ? style.entryValueFill() : style.entryValueDisabledFill());
            g.fillText(entry.selectedValueText(), valueCol * TS, y);
        }

        int ty = numTilesY - 2 * commandTexts.length;
        for (String commandText : commandTexts) {
            g.setFill(style.hintTextFill());
            g.setFont(style.textFont());
            drawCentered(commandText, ty * TS);
            ty += 2;
        }
        g.restore();
    }

    protected void drawCentered(String text, double y) {
        g.save();
        g.setTextAlign(TextAlignment.CENTER);
        g.fillText(text, (canvas.getWidth() * 0.5) / scalingPy.get(), y);
        g.restore();
    }

    protected void handleKeyPress(KeyEvent e) {
        switch (e.getCode()) {
            case DOWN -> {
                playSound(style.entrySelectedSound());
                selectedEntryIndex++;
                if (selectedEntryIndex == entries.size()) selectedEntryIndex = 0;
            }
            case UP -> {
                playSound(style.entrySelectedSound());
                selectedEntryIndex--;
                if (selectedEntryIndex == -1) selectedEntryIndex = entries.size() - 1;
            }
            case SPACE -> {
                playSound(style.valueSelectedSound());
                OptionMenuEntry<?> entry = entries.get(selectedEntryIndex);
                entry.selectedValueIndex++;
                if (entry.selectedValueIndex == entry.valueList.size()) entry.selectedValueIndex = 0;
                entry.onValueChanged(entry.selectedValueIndex);
            }
        }
    }

    public Node root() { return root; }

    public int numTilesX() {
        return numTilesX;
    }

    public int numTilesY() {
        return numTilesY;
    }

    public FloatProperty scalingProperty() { return scalingPy; }

    public void addEntry(OptionMenuEntry<?> entry) { entries.add(requireNonNull(entry)); }

    public void setTitle(String title) { this.title = requireNonNull(title); }

    public void setStyle(OptionMenuStyle style) {
        this.style = requireNonNull(style);
    }

    public void setCommandTexts(String... lines) {
        commandTexts = Arrays.copyOf(lines, lines.length);
    }
}