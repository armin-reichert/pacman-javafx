package de.amr.pacmanfx.uilib.rendering;

import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.canvas.Canvas;

public class BaseSpriteRenderer extends BaseRenderer implements SpriteRenderer {

    private SpriteSheet<?> spriteSheet;

    public BaseSpriteRenderer(Canvas canvas, SpriteSheet<?> spriteSheet) {
        super(canvas);
        this.spriteSheet = spriteSheet;
    }

    public BaseSpriteRenderer(Canvas canvas) {
        super(canvas);
    }

    public void setSpriteSheet(SpriteSheet<?> spriteSheet) {
        this.spriteSheet = spriteSheet;
    }

    @Override
    public SpriteSheet<?> spriteSheet() {
        return spriteSheet;
    }
}
