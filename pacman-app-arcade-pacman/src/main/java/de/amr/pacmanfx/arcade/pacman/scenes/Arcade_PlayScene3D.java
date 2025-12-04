/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.ArcadeActions;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.ui._3d.PlayScene3D;
import de.amr.pacmanfx.ui.action.CheatActions;
import de.amr.pacmanfx.ui.api.GameUI;
import javafx.scene.input.KeyCode;

import static de.amr.pacmanfx.ui.action.CommonGameActions.*;
import static de.amr.pacmanfx.ui.input.Keyboard.bare;

public class Arcade_PlayScene3D extends PlayScene3D {

    public Arcade_PlayScene3D(GameUI ui) {
        super(ui);
    }

    protected void setActionBindings() {
        final Game game = context().currentGame();
        actionBindings.removeBindingsFromKeyboard(ui.keyboard());
        actionBindings.bind(ACTION_PERSPECTIVE_PREVIOUS, GameUI.ACTION_BINDINGS);
        actionBindings.bind(ACTION_PERSPECTIVE_NEXT, GameUI.ACTION_BINDINGS);
        actionBindings.bind(ACTION_TOGGLE_DRAW_MODE, GameUI.ACTION_BINDINGS);
        if (game.optGameLevel().isPresent()) {
            if (game.level().isDemoLevel()) {
                actionBindings.addKeyCombination(ArcadeActions.ACTION_INSERT_COIN, bare(KeyCode.DIGIT5));
                actionBindings.addKeyCombination(ArcadeActions.ACTION_INSERT_COIN, bare(KeyCode.NUMPAD5));
                actionBindings.addKeyCombination(ArcadeActions.ACTION_START_GAME,  bare(KeyCode.DIGIT1));
                actionBindings.addKeyCombination(ArcadeActions.ACTION_START_GAME,  bare(KeyCode.NUMPAD1));
            } else {
                setPlayerSteeringActionBindings();
                actionBindings.bind(CheatActions.ACTION_EAT_ALL_PELLETS, GameUI.ACTION_BINDINGS);
                actionBindings.bind(CheatActions.ACTION_ADD_LIVES, GameUI.ACTION_BINDINGS);
                actionBindings.bind(CheatActions.ACTION_ENTER_NEXT_LEVEL, GameUI.ACTION_BINDINGS);
                actionBindings.bind(CheatActions.ACTION_KILL_GHOSTS, GameUI.ACTION_BINDINGS);
            }
        }
        actionBindings.assignBindingsToKeyboard(ui.keyboard());
    }

    /**
     * Overridden by "Tengen Ms. Pac-Man" subclass to bind to keys representing the Joypad buttons.
     */
    protected void setPlayerSteeringActionBindings() {
        actionBindings.bind(ACTION_STEER_UP,    GameUI.ACTION_BINDINGS);
        actionBindings.bind(ACTION_STEER_DOWN,  GameUI.ACTION_BINDINGS);
        actionBindings.bind(ACTION_STEER_LEFT,  GameUI.ACTION_BINDINGS);
        actionBindings.bind(ACTION_STEER_RIGHT, GameUI.ACTION_BINDINGS);
    }
}
