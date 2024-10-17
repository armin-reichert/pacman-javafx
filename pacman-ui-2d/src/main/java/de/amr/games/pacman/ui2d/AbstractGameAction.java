/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.ui2d.util.KeyInput;
import de.amr.games.pacman.ui2d.util.Keyboard;
import javafx.scene.input.KeyCodeCombination;

public abstract class AbstractGameAction implements GameAction {

    private final KeyInput trigger;

    protected AbstractGameAction(KeyCodeCombination... combinations) {
        trigger = KeyInput.of(combinations);
    }

    @Override
    public KeyInput trigger() {
        return trigger;
    }

    @Override
    public boolean called(Keyboard keyboard) {
        return keyboard.isRegisteredKeyPressed(trigger);
    }

    @Override
    public abstract void execute(GameContext context);
}
