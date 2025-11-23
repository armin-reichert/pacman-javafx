/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.BaseSpriteRenderer;
import de.amr.pacmanfx.uilib.rendering.Renderer;
import javafx.scene.canvas.Canvas;

public abstract class GameScene2D_Renderer extends BaseSpriteRenderer {

    public static <T extends Renderer> T configureRendererForGameScene(T renderer, GameScene2D scene) {
        renderer.backgroundProperty().bind(scene.backgroundProperty());
        renderer.scalingProperty().bind(scene.scalingProperty());
        return renderer;
    }

    protected final GameScene2D scene;
    protected BaseDebugInfoRenderer debugInfoRenderer;

    public GameScene2D_Renderer(GameScene2D scene, Canvas canvas, SpriteSheet<?> spriteSheet) {
        super(canvas, spriteSheet);
        this.scene = scene;
    }

    public abstract void draw();

    @SuppressWarnings("unchecked")
    public <S extends GameScene2D> S scene() {
        return (S) scene;
    }

    protected void createDefaultDebugInfoRenderer(Canvas canvas, SpriteSheet<?> spriteSheet) {
        debugInfoRenderer = configureRendererForGameScene(new BaseDebugInfoRenderer(scene, canvas, spriteSheet), scene);
    }
}