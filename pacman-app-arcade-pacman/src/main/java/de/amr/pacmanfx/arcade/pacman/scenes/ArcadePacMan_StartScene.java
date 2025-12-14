/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_StartScene_Renderer;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.HUD_Renderer;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.sound.SoundID;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.ui._2d.GameScene2D_Renderer.configureRendererForGameScene;

/**
 * Scene shown after credit has been added and where game can be started.
 */
public class ArcadePacMan_StartScene extends GameScene2D {

    private ArcadePacMan_StartScene_Renderer sceneRenderer;
    private HUD_Renderer hudRenderer;

    public ArcadePacMan_StartScene(GameUI ui) {
        super(ui);
    }

    @Override
    protected void createRenderers(Canvas canvas) {
        sceneRenderer = configureRendererForGameScene(
            new ArcadePacMan_StartScene_Renderer(this, canvas), this);

        hudRenderer = configureRendererForGameScene(
            ui.currentConfig().createHUDRenderer(canvas), this);
    }

    @Override
    public HUD_Renderer hudRenderer() {
        return hudRenderer;
    }

    @Override
    public ArcadePacMan_StartScene_Renderer sceneRenderer() {
        return sceneRenderer;
    }

    @Override
    public void doInit(Game game) {
        game.hud().credit(true).score(true).levelCounter(true).livesCounter(false);
        actionBindings.useAll(ArcadePacMan_UIConfig.DEFAULT_BINDINGS);
    }

    @Override
    protected void doEnd(Game game) {
        ui.soundManager().stopVoice();
    }

    @Override
    public void update(Game game) {}

    @Override
    public void onCreditAdded(GameEvent e) {
        ui.soundManager().play(SoundID.COIN_INSERTED);
    }
}