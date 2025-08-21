package de.amr.pacmanfx.uilib.rendering;

import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.canvas.Canvas;

public class SpriteRenderer extends BaseRenderer implements SpriteRendererMixin {

    private final SpriteSheet<?> spriteSheet;

    public SpriteRenderer(Canvas canvas, SpriteSheet<?> spriteSheet) {
        super(canvas);
        this.spriteSheet = spriteSheet;
    }

    @Override
    public SpriteSheet<?> spriteSheet() {
        return spriteSheet;
    }
}
