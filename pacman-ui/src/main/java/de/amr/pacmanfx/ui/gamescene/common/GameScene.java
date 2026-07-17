/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.common;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.gameplay.FrameContext;
import de.amr.pacmanfx.core.state.GameState;
import de.amr.pacmanfx.ui.action.core.ActionBindingsRegistry;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.action.core.QuitHandler;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import javafx.scene.SubScene;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.ScrollEvent;

import java.util.Optional;

public interface GameScene extends GameSceneGameEventHandler, QuitHandler, Disposable {

    Input input();

    GameAppContext appContext();

    GameContext gameContext();

    GameModel gameModel();

    GameState gameState();

    ActionBindingsRegistry actionBindings();

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

    /** Called when the scene is embedded into the UI. */
    void onBeforeEmbedded();

    /**
     * Called when a key combination is pressed inside this scene.
     * Executes the first matching action.
     */
    void onInput();

    /**
     * Called when a scroll event occurs inside this scene.
     */
    void onScroll(ScrollEvent scrollEvent);

    /**
     * Called every game tick.
     *
     * @param frame the context of the current frame)
     */
    void onTick(FrameContext frame);

    /**
     * @return the JavaFX subscene used for this scene.
     */
    Optional<SubScene> optSubSceneFX();

    /**
     * @return the sound effects used for this scene.
     */
    Optional<GameSoundEffects> optSoundEffects();

    /**
     * @return optional context menu contributed by this scene
     */
    Optional<ContextMenu> optContextMenu();

    @Override
    default GameScene gameScene() {
        return this;
    }
}
