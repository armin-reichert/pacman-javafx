/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views;

import de.amr.pacmanfx.engine.input.Input;
import de.amr.pacmanfx.engine.runtime.action.ActionBindingsRegistry;
import de.amr.pacmanfx.engine.runtime.action.GameEngineContext;
import de.amr.pacmanfx.engine.QuitHandler;
import javafx.scene.Node;

import java.util.Optional;
import java.util.function.Supplier;

public interface GameView extends QuitHandler {

    ActionBindingsRegistry<GameEngineContext> actionBindings();

    void setEngineContext(GameEngineContext engineContext);

    GameEngineContext engineContext();

    default void onInput(Input input) {
        engineContext().executeMatchingAction(actionBindings());
    }

    Node rootPane();

    default Optional<Supplier<String>> optTitleSupplier() {
        return Optional.empty();
    }

    void onEnter();

    void onExit();
}
