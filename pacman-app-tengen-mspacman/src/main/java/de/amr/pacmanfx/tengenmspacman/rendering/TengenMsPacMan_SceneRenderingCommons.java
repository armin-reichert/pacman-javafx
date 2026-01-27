/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.input.JoypadKeyBinding;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;

public interface TengenMsPacMan_SceneRenderingCommons {

    GameScene2D_Renderer renderer();

    /**
     * Draws a vertical bar of given width and height. The top and bottom edges are drawn with the edgeColor and are
     * 1 pixel high.
     *
     * @param edgeColor color of upper and lower edges
     * @param innerColor color of inner area
     * @param width width of the bar
     * @param height height of the bar
     * @param topY top y-position
     */
    default void drawHorizontalBar(Color edgeColor, Color innerColor, double width, double height, double topY) {
        requireNonNull(edgeColor);
        requireNonNull(innerColor);
        final GraphicsContext ctx = renderer().ctx();
        final double scaling = renderer().scaling();
        ctx.save();
        ctx.scale(scaling, scaling);
        ctx.setFill(edgeColor);
        ctx.fillRect(0, topY, width, height);
        ctx.setFill(innerColor);
        ctx.fillRect(0, topY + 1, width, height - 2);
        ctx.restore();
    }

    default void drawJoypadKeyBinding(JoypadKeyBinding binding) {
        final GraphicsContext ctx = renderer().ctx();
        final double scaling = renderer().scaling();
        ctx.save();
        requireNonNull(binding);
        ctx.setFont(Font.font(scaling*6));
        ctx.setStroke(Color.WHITE);
        ctx.strokeText(" [SELECT]=%s   [START]=%s   [BUTTON B]=%s   [BUTTON A]=%s".formatted(
            binding.key(JoypadButton.SELECT),
            binding.key(JoypadButton.START),
            binding.key(JoypadButton.B),
            binding.key(JoypadButton.A)
        ), 0, scaling*TS);
        ctx.strokeText(" [UP]=%s   [DOWN]=%s   [LEFT]=%s   [RIGHT]=%s".formatted(
            binding.key(JoypadButton.UP),
            binding.key(JoypadButton.DOWN),
            binding.key(JoypadButton.LEFT),
            binding.key(JoypadButton.RIGHT)
        ), 0, scaling*(2*TS));
        ctx.restore();
    }
}