/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.views;

import de.amr.pacmanfx.ui.action.core.ActionBindingsRegistry;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.action.core.QuitHandler;
import de.amr.pacmanfx.ui.input.Input;
import javafx.scene.Node;

import java.util.Optional;
import java.util.function.Supplier;

public interface GameView extends QuitHandler {

    /**
     * Returns the action bindings manager responsible for resolving keyboard input
     * into high-level game actions for this view.
     *
     * @return the action bindings manager associated with this view
     */
    ActionBindingsRegistry actionBindings();

    /**
     * Processes the current keyboard state and executes the matching action, if any.
     * <p>
     * This method delegates to the view's {@link ActionBindingsRegistry} and executes
     * the resolved action via {@link GameAction#execute()}.
     *
     * @param input the user input
     */
    default void onInput(Input input) {
        actionBindings().findActionMatchingPressedKeys(input.keyboard()).ifPresent(GameAction::execute);
    }

    /**
     * Returns the root JavaFX node representing this view.
     * <p>
     * The returned node is inserted into the main scene graph when the view becomes active.
     * Implementations typically construct this node once and reuse it.
     *
     * @return the root node of this view
     */
    Node rootPane();

    /**
     * Provides an optional dynamic title supplier for this view.
     * <p>
     * If present, the supplied string is used as the window title while this view is active.
     * If empty, the application uses its default title.
     *
     * @return an optional supplier for the view's title text
     */
    default Optional<Supplier<String>> optTitleSupplier() {
        return Optional.empty();
    }

    /**
     * Called when the view becomes active.
     * <p>
     * Typical uses include:
     * <ul>
     *   <li>initializing UI state,</li>
     *   <li>starting animations,</li>
     *   <li>registering listeners or bindings.</li>
     * </ul>
     */
    void onEnter();

    /**
     * Called when the view is no longer active.
     * <p>
     * Implementations may use this hook to:
     * <ul>
     *   <li>stop animations,</li>
     *   <li>release temporary resources,</li>
     *   <li>unregister listeners or bindings.</li>
     * </ul>
     */
    void onExit();

    /**
     * Optional per-frame rendering callback.
     * <p>
     * Views that require continuous updates (e.g., animations, HUD updates, debug overlays)
     * may override this method. The default implementation does nothing.
     */
    default void render() {}
}
