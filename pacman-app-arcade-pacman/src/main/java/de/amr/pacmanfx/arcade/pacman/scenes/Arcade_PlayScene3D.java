/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui._3d.PlayScene3D;
import javafx.scene.input.KeyCode;

import static de.amr.pacmanfx.ui.input.Keyboard.control;

public class Arcade_PlayScene3D extends PlayScene3D {

    public Arcade_PlayScene3D() {}

    @Override
    protected void setActionBindings(GameLevel level) {
        actionBindings.releaseBindings(GameUI.KEYBOARD);
        actionBindings.useAllBindings(GameUI.PLAY_3D_BINDINGS);
        if (level.isDemoLevel()) {
            actionBindings.useAllBindings(ArcadePacMan_UIConfig.DEFAULT_BINDINGS);
        } else {
            actionBindings.useAllBindings(GameUI.STEERING_BINDINGS);
            actionBindings.useAllBindings(GameUI.CHEAT_BINDINGS);
        }
        actionBindings.setKeyCombination(actionDroneClimb, control(KeyCode.MINUS));
        actionBindings.setKeyCombination(actionDroneDescent, control(KeyCode.PLUS));
        actionBindings.activateBindings(GameUI.KEYBOARD);
    }
}