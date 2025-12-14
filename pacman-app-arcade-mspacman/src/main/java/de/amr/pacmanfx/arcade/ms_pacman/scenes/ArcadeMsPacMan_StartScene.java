/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_StartScene_Renderer;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.sound.SoundID;
import javafx.scene.canvas.Canvas;

public class ArcadeMsPacMan_StartScene extends GameScene2D {

    private ArcadeMsPacMan_StartScene_Renderer sceneRenderer;

    public ArcadeMsPacMan_StartScene(GameUI ui) {
        super(ui);
    }

    @Override
    protected void createRenderers(Canvas canvas) {
        final GameUI_Config uiConfig = ui.currentConfig();
        sceneRenderer = adaptRenderer(
            new ArcadeMsPacMan_StartScene_Renderer(this, canvas, (ArcadeMsPacMan_SpriteSheet) uiConfig.spriteSheet()));
    }

    @Override
    public ArcadeMsPacMan_StartScene_Renderer sceneRenderer() {
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