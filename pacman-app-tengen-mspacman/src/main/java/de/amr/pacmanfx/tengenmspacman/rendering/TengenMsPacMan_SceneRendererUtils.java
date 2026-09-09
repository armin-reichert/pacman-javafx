/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.ui.input.JoypadButton;
import de.amr.pacmanfx.ui.input.JoypadKeyBinding;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static java.util.Objects.requireNonNull;

public final class TengenMsPacMan_SceneRendererUtils {

    private TengenMsPacMan_SceneRendererUtils() {}

    public static void drawHorizontalBar(GraphicsContext ctx, double scaling, Color edgeColor, Color innerColor, double width, double height, double topY) {
        requireNonNull(edgeColor);
        requireNonNull(innerColor);
        ctx.save();
        ctx.scale(scaling, scaling);
        ctx.setFill(edgeColor);
        ctx.fillRect(0, topY, width, height);
        ctx.setFill(innerColor);
        ctx.fillRect(0, topY + 1, width, height - 2);
        ctx.restore();
    }

    public static void drawJoypadKeyBinding(GraphicsContext ctx, double scaling, JoypadKeyBinding binding) {
        ctx.save();
        requireNonNull(binding);
        ctx.setFont(Font.font(scaling*6));
        ctx.setStroke(Color.WHITE);
        ctx.strokeText(" [SELECT]=%s   [START]=%s   [BUTTON B]=%s   [BUTTON A]=%s".formatted(
            binding.key(JoypadButton.SELECT),
            binding.key(JoypadButton.START),
            binding.key(JoypadButton.B),
            binding.key(JoypadButton.A)
        ), 0, scaling* WorldMap.TS);
        ctx.strokeText(" [UP]=%s   [DOWN]=%s   [LEFT]=%s   [RIGHT]=%s".formatted(
            binding.key(JoypadButton.UP),
            binding.key(JoypadButton.DOWN),
            binding.key(JoypadButton.LEFT),
            binding.key(JoypadButton.RIGHT)
        ), 0, scaling*(2*WorldMap.TS));
        ctx.restore();
    }
}