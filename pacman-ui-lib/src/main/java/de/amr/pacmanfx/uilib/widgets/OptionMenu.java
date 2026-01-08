/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.widgets;

import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
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
import javafx.scene.text.TextAlignment;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;

public class OptionMenu {

    private final int numTilesX;
    private final int numTilesY;
    private final int textCol;
    private final int valueCol;

    private final List<OptionMenuEntry<?>> entries = new ArrayList<>();
    private final BooleanProperty soundEnabled = new SimpleBooleanProperty(true);
    private final FloatProperty scaling = new SimpleFloatProperty(2);

    private int selectedEntryIndex = 0;
    private String title = "";

    private final BorderPane root = new BorderPane();
    protected final BaseRenderer renderer;
    protected final Canvas canvas;

    protected OptionMenuStyle style = OptionMenuStyle.DEFAULT_OPTION_MENU_STYLE;

    public OptionMenu(int numTilesX, int numTilesY, int textCol, int valueCol) {
        this.numTilesX = numTilesX;
        this.numTilesY = numTilesY;
        this.textCol = textCol;
        this.valueCol = valueCol;

        setTitle("OPTIONS");

        canvas = new Canvas();
        canvas.widthProperty().bind(scaling.multiply(numTilesX*TS));
        canvas.heightProperty().bind(scaling.multiply(numTilesY*TS));

        renderer = new BaseRenderer(canvas);

        root.setCenter(canvas);
        root.setBorder(Border.stroke(style.borderStroke()));
        root.maxWidthProperty().bind(canvas.widthProperty());
        root.maxHeightProperty().bind(canvas.heightProperty());
        root.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeyPress);
    }

    public BaseRenderer renderer() {
        return renderer;
    }

    public void requestFocus() {
        final Canvas canvas = renderer.ctx().getCanvas();
        if (!canvas.isFocused()) {
            canvas.requestFocus();
            Logger.info("Focus now on {}", canvas);
        }
    }

    protected void playSound(AudioClip clip) {
        if (soundEnabled.get()) {
            clip.play();
        }
    }

    public BooleanProperty soundEnabledProperty() { return soundEnabled; }

    public final void draw() {
        final GraphicsContext ctx = renderer.ctx();
        renderer.fillCanvas(style.backgroundFill());

        ctx.save();
        ctx.scale(scaling.get(), scaling.get());

        ctx.setFont(style.titleFont());
        ctx.setFill(style.titleTextFill());
        drawCentered(title, 6 * TS);

        ctx.setFont(style.textFont());
        for (int i = 0; i < entries.size(); ++i) {
            int y = (12 + 3 * i) * TS;
            OptionMenuEntry<?> entry = entries.get(i);
            if (i == selectedEntryIndex) {
                ctx.setFill(style.entryTextFill());
                ctx.fillText("-", (textCol - 2) * TS, y);
                ctx.fillText(">", (textCol - 2) * TS + HTS, y);
            }
            ctx.setFill(style.entryTextFill());
            ctx.fillText(entry.text, textCol * TS, y);
            ctx.setFill(entry.enabled ? style.entryValueFill() : style.entryValueDisabledFill());
            ctx.fillText(entry.getSelectedValueText(), valueCol * TS, y);
        }

        drawUsageInfo();

        ctx.restore();
    }

    protected void drawUsageInfo() {}

    protected void drawCentered(String text, double y) {
        final GraphicsContext ctx = renderer.ctx();
        ctx.save();
        ctx.setTextAlign(TextAlignment.CENTER);
        ctx.fillText(text, (renderer.ctx().getCanvas().getWidth() * 0.5) / scaling.get(), y);
        ctx.restore();
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

    protected void selectedPrevEntry() {
        playSound(style.entrySelectedSound());
        selectedEntryIndex = selectedEntryIndex > 0 ? selectedEntryIndex - 1 : entries.size() - 1;
    }

    protected void selectNextEntry() {
        playSound(style.entrySelectedSound());
        selectedEntryIndex = selectedEntryIndex < entries.size() - 1 ? selectedEntryIndex + 1 : 0;
    }

    protected void selectNextValue() {
        playSound(style.valueSelectedSound());
        final OptionMenuEntry<?> entry = entries.get(selectedEntryIndex);
        entry.selectedValueIndex = entry.selectedValueIndex < entry.optionValues.size() - 1 ? entry.selectedValueIndex + 1 : 0;
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

    public void addEntry(OptionMenuEntry<?> entry) { entries.add(requireNonNull(entry)); }

    public void setTitle(String title) { this.title = requireNonNull(title); }

    public void setStyle(OptionMenuStyle style) {
        this.style = requireNonNull(style);
    }
}