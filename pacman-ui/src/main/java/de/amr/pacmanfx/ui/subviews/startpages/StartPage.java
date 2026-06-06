/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.subviews.startpages;

import de.amr.pacmanfx.ui.app.AppContext;
import javafx.scene.layout.Pane;
import org.tinylog.Logger;

/**
 * Represents a single start page in the application's start‑view system.
 * <p>
 * A {@code StartPage} encapsulates:
 * <ul>
 *   <li>its visual root node,</li>
 *   <li>initialization logic executed once when the page is created,</li>
 *   <li>lifecycle callbacks for entering and leaving the page,</li>
 *   <li>a human‑readable title used by navigation components.</li>
 * </ul>
 *
 * <h2>Lifecycle</h2>
 * <ol>
 *   <li>{@link #init(AppContext)} is called once when the page is constructed or first registered.</li>
 *   <li>{@link #onEnterStartPage(AppContext)} is invoked each time the page becomes visible.</li>
 *   <li>{@link #onExitStartPage(AppContext)} is invoked when the page is left.</li>
 * </ol>
 *
 * Implementations should be lightweight and avoid long‑running operations in lifecycle methods,
 * as these are typically executed on the JavaFX Application Thread.
 */
public interface StartPage {

    /**
     * Returns the root layout node representing this start page.
     * <p>
     * The returned {@link Pane} is inserted into the start‑view container.
     * Implementations should create this node once and reuse it.
     *
     * @return the root JavaFX layout node for this page
     */
    Pane rootPane();

    /**
     * Performs one‑time initialization of the start page.
     * <p>
     * This method is called exactly once, typically during application startup or
     * when the page is first registered with the {@link AppContext}.
     * Implementations may use this hook to:
     * <ul>
     *   <li>construct UI controls,</li>
     *   <li>bind properties to global UI state,</li>
     *   <li>register event handlers,</li>
     *   <li>load resources needed by the page.</li>
     * </ul>
     *
     * @param context the global UI façade providing access to shared services
     */
    void init(AppContext context);

    /**
     * Called whenever this start page becomes the active page.
     * <p>
     * Typical uses include:
     * <ul>
     *   <li>refreshing dynamic content,</li>
     *   <li>starting animations,</li>
     *   <li>resetting UI state specific to this page.</li>
     * </ul>
     *
     * @param context the global UI façade
     */
    void onEnterStartPage(AppContext context);

    /**
     * Called when the user navigates away from this start page.
     * <p>
     * The default implementation logs the transition. Implementations may override
     * this method to stop animations, release temporary resources, or persist state.
     *
     * @param context the global UI façade
     */
    default void onExitStartPage(AppContext context) {
        Logger.info("Exit start page {}", this);
    }

    /**
     * Returns the human‑readable title of this start page.
     * <p>
     * Titles are used by navigation components, menus, or debugging tools.
     *
     * @return the page title
     */
    String title();
}
