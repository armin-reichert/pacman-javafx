/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.ArcadeActions;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_StartScene_Renderer;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.ui._2d.HUD_Renderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;

import static de.amr.pacmanfx.ui._2d.GameScene2D_Renderer.configureRendererForGameScene;
import static de.amr.pacmanfx.ui.input.Keyboard.bare;

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
    public void doInit() {
        context().currentGame().hud().creditVisible(true).scoreVisible(true).levelCounterVisible(true).livesCounterVisible(false);
        actionBindings.addKeyCombination(ArcadeActions.ACTION_INSERT_COIN, bare(KeyCode.DIGIT5));
        actionBindings.addKeyCombination(ArcadeActions.ACTION_INSERT_COIN, bare(KeyCode.NUMPAD5));
        actionBindings.addKeyCombination(ArcadeActions.ACTION_START_GAME, bare(KeyCode.DIGIT1));
        actionBindings.addKeyCombination(ArcadeActions.ACTION_START_GAME, bare(KeyCode.NUMPAD1));
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