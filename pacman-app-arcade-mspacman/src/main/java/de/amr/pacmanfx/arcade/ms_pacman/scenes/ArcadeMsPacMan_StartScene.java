/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_HUDRenderer;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_StartScene_Renderer;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.action.ArcadeActions;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.sound.SoundID;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.ui._2d.GameScene2DRenderer.configureRendererForGameScene;

public class ArcadeMsPacMan_StartScene extends GameScene2D {

    private ArcadeMsPacMan_HUDRenderer hudRenderer;
    private ArcadeMsPacMan_StartScene_Renderer sceneRenderer;

    public ArcadeMsPacMan_StartScene(GameUI ui) {
        super(ui);
    }

    @Override
    protected void createRenderers(Canvas canvas) {
        final GameUI_Config uiConfig = ui.currentConfig();

        hudRenderer = configureRendererForGameScene(
            (ArcadeMsPacMan_HUDRenderer) uiConfig.createHUDRenderer(canvas), this);

        sceneRenderer = configureRendererForGameScene(
            new ArcadeMsPacMan_StartScene_Renderer(this, canvas, uiConfig.spriteSheet()), this);
    }

    @Override
    public ArcadeMsPacMan_HUDRenderer hudRenderer() {
        return hudRenderer;
    }

    @Override
    public ArcadeMsPacMan_StartScene_Renderer sceneRenderer() {
        return sceneRenderer;
    }

    @Override
    public void doInit() {
        context().game().hud().creditVisible(true).scoreVisible(true).levelCounterVisible(true).livesCounterVisible(false);
        actionBindings.bind(ArcadeActions.ACTION_INSERT_COIN, ui.actionBindings());
        actionBindings.bind(ArcadeActions.ACTION_START_GAME, ui.actionBindings());
    }

    @Override
    protected void doEnd() {}

    @Override
    public void update() {}

    @Override
    public void onCreditAdded(GameEvent e) {
        ui.soundManager().play(SoundID.COIN_INSERTED);
    }
}