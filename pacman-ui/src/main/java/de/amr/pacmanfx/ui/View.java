/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.event.GameEventListener;
import javafx.scene.Node;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Represents a visual UI component within the game's view system.
 * <p>
 * A {@code View} encapsulates:
 * <ul>
 *   <li>a root JavaFX node inserted into the scene graph,</li>
 *   <li>its own keyboard action bindings,</li>
 *   <li>lifecycle callbacks for entering and leaving the view,</li>
 *   <li>optional dynamic title text for the main window,</li>
 *   <li>optional per-frame rendering logic.</li>
 * </ul>
 *
 * <h2>Lifecycle</h2>
 * Views follow a simple lifecycle managed by the {@link ViewManager}:
 * <ol>
 *   <li>{@link #onEnter()} is called when the view becomes active.</li>
 *   <li>{@link #render()} may be called periodically while the view is active.</li>
 *   <li>{@link #onExit()} is called when the view is replaced or hidden.</li>
 * </ol>
 *
 * <h2>Input Handling</h2>
 * Each view provides its own {@link ActionBindingsManager}, allowing it to define
 * view-specific keyboard shortcuts. The default {@link #onKeyboardInput(GameUI)}
 * implementation resolves the current key state into a {@link de.amr.pacmanfx.ui.action.GameAction}
 * and executes it if enabled.
 *
 * <h2>Event Handling</h2>
 * As a {@link GameEventListener}, a view may react to game events such as level changes,
 * score updates, or state transitions.
 */
public interface View extends GameEventListener {

    /**
     * Returns the action bindings manager responsible for resolving keyboard input
     * into high-level game actions for this view.
     *
     * @return the action bindings manager associated with this view
     */
    ActionBindingsManager actionBindingsManager();

    /**
     * Processes the current keyboard state and executes the matching action, if any.
     * <p>
     * This method delegates to the view's {@link ActionBindingsManager} and executes
     * the resolved action via {@link de.amr.pacmanfx.ui.action.GameAction#executeIfEnabled(GameUI)}.
     *
     * @param ui the global game UI façade
     */
    default void onKeyboardInput(GameUI ui) {
        actionBindingsManager().findMatchingAction(GameUI.KEYBOARD)
            .ifPresent(gameAction -> gameAction.executeIfEnabled(ui));
    }

    /**
     * Returns the root JavaFX node representing this view.
     * <p>
     * The returned node is inserted into the main scene graph when the view becomes active.
     * Implementations typically construct this node once and reuse it.
     *
     * @return the root node of this view
     */
    Node root();

    /**
     * Provides an optional dynamic title supplier for this view.
     * <p>
     * If present, the supplied string is used as the window title while this view is active.
     * If empty, the application uses its default title.
     *
     * @return an optional supplier for the view's title text
     */
    default Optional<Supplier<String>> titleSupplier() {
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
