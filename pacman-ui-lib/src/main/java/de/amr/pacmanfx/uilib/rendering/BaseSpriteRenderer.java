package de.amr.pacmanfx.uilib.rendering;

import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.canvas.Canvas;

public class BaseSpriteRenderer extends BaseRenderer implements SpriteRenderer {

    private final SpriteSheet<?> spriteSheet;

    public BaseSpriteRenderer(Canvas canvas, SpriteSheet<?> spriteSheet) {
        super(canvas);
        this.spriteSheet = spriteSheet;
    }

    @Override
    public SpriteSheet<?> spriteSheet() {
        return spriteSheet;
    }
}
