package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.input.JoypadKeyBinding;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.BaseCanvasRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.Globals.TS;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_ScenesRenderer extends BaseCanvasRenderer implements SpriteRenderer {

    private final GameUI_Config uiConfig;

    public TengenMsPacMan_ScenesRenderer(Canvas canvas, GameUI_Config uiConfig) {
        super(canvas);
        this.uiConfig = uiConfig;
    }

    @Override
    public SpriteSheet<?> spriteSheet() {
        return uiConfig.spriteSheet();
    }

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
    public void drawHorizontalBar(Color edgeColor, Color innerColor, double width, double height, double topY) {
        requireNonNull(edgeColor);
        requireNonNull(innerColor);
        ctx().save();
        ctx().scale(scaling(), scaling());
        ctx().setFill(edgeColor);
        ctx().fillRect(0, topY, width, height);
        ctx().setFill(innerColor);
        ctx().fillRect(0, topY + 1, width, height - 2);
        ctx().restore();
    }

    public void drawJoypadKeyBinding(JoypadKeyBinding binding) {
        ctx().save();
        requireNonNull(binding);
        ctx().setFont(Font.font("Sans", scaled(TS)));
        ctx().setStroke(Color.WHITE);
        ctx().strokeText(" [SELECT]=%s   [START]=%s   [BUTTON B]=%s   [BUTTON A]=%s".formatted(
            binding.key(JoypadButton.SELECT),
            binding.key(JoypadButton.START),
            binding.key(JoypadButton.B),
            binding.key(JoypadButton.A)
        ), 0, scaled(TS));
        ctx().strokeText(" [UP]=%s   [DOWN]=%s   [LEFT]=%s   [RIGHT]=%s".formatted(
            binding.key(JoypadButton.UP),
            binding.key(JoypadButton.DOWN),
            binding.key(JoypadButton.LEFT),
            binding.key(JoypadButton.RIGHT)
        ), 0, scaled(2*TS));
        ctx().restore();
    }
}
