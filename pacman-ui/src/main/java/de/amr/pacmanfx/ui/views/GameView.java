/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views;

import de.amr.pacmanfx.core.event.GameEvent;
import de.amr.pacmanfx.engine.QuitHandler;
import de.amr.pacmanfx.engine.action.ActionBindingsRegistry;
import de.amr.pacmanfx.engine.input.Input;
import de.amr.pacmanfx.ui.EngineContext;
import javafx.scene.Node;

import java.util.Optional;
import java.util.function.Supplier;

public interface GameView extends QuitHandler {

    ActionBindingsRegistry<EngineContext> actionBindings();

    void onInput(Input input);

    Node rootPane();

    default Optional<Supplier<String>> optTitleSupplier() {
        return Optional.empty();
    }

    void onEnter();

    void onExit();

    void handleGameEvent(GameEvent gameEvent);
}
