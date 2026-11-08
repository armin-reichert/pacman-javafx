/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.engine.gamescene;

import de.amr.basics.Disposable;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.base.GameEventListener;
import de.amr.pacmanfx.engine.QuitHandler;
import javafx.scene.SubScene;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ScrollEvent;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface GameScene extends Disposable, QuitHandler {

    default List<MenuItem> contextMenuItems() {
        return List.of();
    }

    default Optional<GameEventListener> optGameEventHandler() {
        return Optional.empty();
    }

    default Optional<SubScene> optSubSceneFX() {
        return Optional.empty();
    }

    /**
     * @return the renderables produced by this game scene
     */
    Stream<Renderable> renderables();

    /**
     * Hook called when entering this 2D scene from a 3D scene.
     * Subclasses may override to adjust state or transitions.
     */
    default void onEnteredFrom3DScene() {}

    default void onBeforeEmbedded(GameContext game) {}

    default void onScroll(ScrollEvent scrollEvent) {}

    /**
     * Called on each simulation frame.
     *
     * @param game the current game context
     */
    void onTick(GameContext game);

    /**
     * Called when a key combination is pressed inside this scene.
     */
    void onInput();
}