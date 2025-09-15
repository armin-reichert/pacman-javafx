package de.amr.pacmanfx.uilib.rendering;

import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.canvas.Canvas;

public class BaseSpriteRenderer extends BaseCanvasRenderer implements SpriteRenderer {

    private SpriteSheet<?> spriteSheet;
    private boolean imageSmoothing;

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
    public void setImageSmoothing(boolean imageSmoothing) {
        this.imageSmoothing = imageSmoothing;
    }

    @Override
    public boolean imageSmoothing() {
        return imageSmoothing;
    }

    @Override
    public SpriteSheet<?> spriteSheet() {
        return spriteSheet;
    }
}
