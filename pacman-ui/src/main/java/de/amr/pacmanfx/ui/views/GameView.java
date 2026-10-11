/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views;

import de.amr.pacmanfx.engine.input.Input;
import de.amr.pacmanfx.engine.runtime.action.ActionBindingsRegistry;
import de.amr.pacmanfx.ui.GameEngineUIContext;
import de.amr.pacmanfx.engine.QuitHandler;
import javafx.scene.Node;

import java.util.Optional;
import java.util.function.Supplier;

import static de.amr.pacmanfx.engine.runtime.action.ActionExecutor.executeMatchingAction;

public interface GameView extends QuitHandler {

    ActionBindingsRegistry<GameEngineUIContext> actionBindings();

    void setEngineContext(GameEngineUIContext engineContext);

    GameEngineUIContext engineContext();

    default void onInput(Input input) {
        executeMatchingAction(actionBindings(), engineContext(), engineContext().input().keyboard());
    }

    Node rootPane();

    default Optional<Supplier<String>> optTitleSupplier() {
        return Optional.empty();
    }

    void onEnter();

    void onExit();
}
