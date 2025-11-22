/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.rendering.Arcade_BootScene2D_Renderer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.HUDRenderer;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.ui._2d.GameScene2D_Renderer.configureRendererForGameScene;

/**
 * The boot screen is showing some strange screen patterns and eventually  a grid.
 * This scene tries to mimic that to a certain degree.
 */
public class Arcade_BootScene2D extends GameScene2D {

    private Arcade_BootScene2D_Renderer sceneRenderer;

    public Arcade_BootScene2D(GameUI ui) {
        super(ui);
    }

    @Override
    protected HUDRenderer hudRenderer() {
        return null;
    }

    @Override
    public Arcade_BootScene2D_Renderer sceneRenderer() {
        return sceneRenderer;
    }

    @Override
    public void doInit() {
        context().game().hud().all(false);
   }

    @Override
    protected void createRenderers(Canvas canvas) {
        final SpriteSheet<?> spriteSheet = ui.currentConfig().spriteSheet();
        sceneRenderer = configureRendererForGameScene(
            new Arcade_BootScene2D_Renderer(this, canvas, spriteSheet), this);
    }

    @Override
    protected void doEnd() {}

    @Override
    public void update() {
        if (context().gameState().timer().atSecond(4)) {
            context().gameController().letCurrentGameStateExpire();
        }
    }

    @Override
    public void draw() {
        sceneRenderer.draw();
    }
}