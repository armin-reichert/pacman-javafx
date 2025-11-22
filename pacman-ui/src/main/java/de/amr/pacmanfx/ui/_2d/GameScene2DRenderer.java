/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui._2d;

import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.BaseSpriteRenderer;
import de.amr.pacmanfx.uilib.rendering.Renderer;
import javafx.scene.canvas.Canvas;

import java.util.ArrayList;
import java.util.List;

public class GameScene2DRenderer extends BaseSpriteRenderer {

    public static <T extends Renderer> T configureRendererForGameScene(T renderer, GameScene2D scene) {
        renderer.backgroundProperty().bind(scene.backgroundProperty());
        renderer.scalingProperty().bind(scene.scalingProperty());
        return renderer;
    }

    protected final GameScene2D scene;
    protected final List<Actor> actorsInZOrder = new ArrayList<>();

    public GameScene2DRenderer(GameScene2D scene, Canvas canvas, SpriteSheet<?> spriteSheet) {
        super(canvas, spriteSheet);
        this.scene = scene;
        configureRendererForGameScene(this, scene);
    }

    @SuppressWarnings("unchecked")
    public <S extends GameScene2D> S scene() {
        return (S) scene;
    }

    public List<Actor> actorsInZOrder() {
        return actorsInZOrder;
    }
}
