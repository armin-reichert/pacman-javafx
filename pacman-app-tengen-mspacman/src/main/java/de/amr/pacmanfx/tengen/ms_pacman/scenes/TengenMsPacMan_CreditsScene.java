/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_CreditsScene_Renderer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.action.ActionBinding;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.rendering.HUD_Renderer;
import javafx.scene.canvas.Canvas;

import java.util.Set;

import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Actions.ACTION_ENTER_START_SCREEN;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE_PX;
import static de.amr.pacmanfx.ui._2d.GameScene2D_Renderer.configureRendererForGameScene;

/**
 * Gives credit to the people that helped in making the game, original and remake authors.
 */
public class TengenMsPacMan_CreditsScene extends GameScene2D {

    public static final float DISPLAY_SECONDS = 16;

    public float fadeProgress = 0;

    private TengenMsPacMan_CreditsScene_Renderer sceneRenderer;

    public TengenMsPacMan_CreditsScene(GameUI ui) {
        super(ui);
    }

    @Override
    protected void createRenderers(Canvas canvas) {
        final GameUI_Config uiConfig = ui.currentConfig();

        sceneRenderer = configureRendererForGameScene(
            new TengenMsPacMan_CreditsScene_Renderer(this, canvas, uiConfig.spriteSheet()), this);
    }

    @Override
    protected HUD_Renderer hudRenderer() {
        return null;
    }

    @Override
    public TengenMsPacMan_CreditsScene_Renderer sceneRenderer() {
        return sceneRenderer;
    }

    @Override
    protected void doInit() {
        context().game().hud().creditVisible(false).scoreVisible(false).levelCounterVisible(false).livesCounterVisible(false);

        Set<ActionBinding> tengenActionBindings = ui.<TengenMsPacMan_UIConfig>currentConfig().tengenActionBindings();
        actionBindings.bind(ACTION_ENTER_START_SCREEN, tengenActionBindings);
    }

    @Override
    protected void doEnd() {}

    @Override
    public void update() {
        if (context().gameState().timer().atSecond(DISPLAY_SECONDS)) {
            context().game().stateMachine().letCurrentStateExpire();
            return;
        }
        if (context().gameState().timer().betweenSeconds(0.5 * DISPLAY_SECONDS, DISPLAY_SECONDS)) {
            fadeProgress = Math.min(fadeProgress + 0.005f, 1f); // Clamp to 1.0
        }
    }

    @Override
    public Vector2i sizeInPx() { return NES_SIZE_PX; }
}