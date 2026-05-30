/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.action;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.ui.input.Keyboard;
import javafx.scene.input.KeyCodeCombination;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface ActionBindingsSet extends Disposable {

    ActionBindingsSet NO_BINDINGS = new EmptyActionBindingsSet();

    Map<KeyCodeCombination, GameAction> bindingMap();

    void logBindings();

    Optional<GameAction> actionMatchingKeyboardState(Keyboard keyboard);

    void setKeyCombination(GameAction action, KeyCodeCombination combination);

    void registerFirstBinding(GameAction action, Set<ActionBinding> bindings);

    void registerAllBindings(Set<ActionBinding> bindings);
}
