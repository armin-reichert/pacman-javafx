/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.common;

import de.amr.basics.Disposable;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.base.GameEventListener;
import de.amr.pacmanfx.core.gamestate.GameFlow;
import de.amr.pacmanfx.engine.GameVariantRuntime;
import de.amr.pacmanfx.ui.action.core.PacManGamesEngine;
import de.amr.pacmanfx.ui.action.core.QuitHandler;
import de.amr.pacmanfx.ui.sound.PacManGameSoundEffects;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.ui.viewmodel.GameViewModel;
import javafx.scene.SubScene;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.ScrollEvent;

import java.util.Optional;
import java.util.stream.Stream;

public interface GameScene extends Disposable, QuitHandler {

    default GameContext game() {
        return engine().currentGame();
    }

    default GameFlow gameFlow() {
        return game().playConfig().gameFlow();
    }

    default GameVariantRuntime runtime() {
        return engine().gameVariantManager().currentRuntime();
    }

    default GameViewModel viewModel() {
        return engine().ui().viewModel();
    }

    default Optional<ContextMenu> optContextMenu() {
        return Optional.empty();
    }

    default Optional<GameEventListener> optGameEventHandler() {
        return Optional.empty();
    }

    default Optional<SubScene> optSubSceneFX() {
        return Optional.empty();
    }

    default SoundManager soundManager() {
        return engine().soundManager();
    }

    default Optional<PacManGameSoundEffects> optSoundEffects() {
        return runtime().uiConfig().optSoundEffects();
    }

    /**
     * @return the renderables produced by this game scene
     */
    Stream<Renderable> renderables();

    void setEngine(PacManGamesEngine engine);

    PacManGamesEngine engine();

    /**
     * Hook called when entering this 2D scene from a 3D scene.
     * Subclasses may override to adjust state or transitions.
     */
    default void onEnteredFrom3DScene() {}

    default void onBeforeEmbedded() {}

    default void onScroll(ScrollEvent scrollEvent) {}

    /**
     * Activates the scene and assigns keyboard bindings.
     */
    void activate();

    /**
     * Called when the scene is deactivated.
     * Subclasses must:<br/>
     * - unbind all properties<br/>
     * - remove all listeners<br/>
     * - stop all timers<br/>
     * - release all UI references (canvas, subscene, etc.)
     */
    void deactivate();

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