/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.ui._3d.PlayScene3D;
import de.amr.pacmanfx.ui.api.GameUI;
import javafx.scene.input.KeyCode;

import static de.amr.pacmanfx.ui.input.Keyboard.control;

public class Arcade_PlayScene3D extends PlayScene3D {

    public Arcade_PlayScene3D(GameUI ui) {
        super(ui);
    }

    protected void setActionBindings() {
        final Game game = context().currentGame();
        actionBindings.release(GameUI.KEYBOARD);
        actionBindings.useAll(GameUI.PLAY_3D_BINDINGS);
        if (game.optGameLevel().isPresent()) {
            if (game.level().isDemoLevel()) {
                actionBindings.useAll(ArcadePacMan_UIConfig.DEFAULT_BINDINGS);
            } else {
                actionBindings.useAll(GameUI.STEERING_BINDINGS);
                actionBindings.useAll(GameUI.CHEAT_BINDINGS);
            }
        }
        actionBindings.addKeyCombination(actionDroneUp, control(KeyCode.MINUS));
        actionBindings.addKeyCombination(actionDroneDown, control(KeyCode.PLUS));
        actionBindings.attach(GameUI.KEYBOARD);
    }
}