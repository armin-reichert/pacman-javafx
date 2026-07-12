/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.common;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.GameEventListener;
import de.amr.pacmanfx.core.state.TimedGameState;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.ui.action.core.ActionBindingsRegistry;
import de.amr.pacmanfx.ui.action.core.QuitHandler;
import de.amr.pacmanfx.ui.game.PacManGamesCollection;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import javafx.scene.SubScene;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.ScrollEvent;

import java.util.Optional;

public interface GameScene extends QuitHandler, Disposable {

    PacManGamesCollection game();

    Input input();

    GameContext gameContext();

    GameModel gameModel();

    TimedGameState gameState();

    ActionBindingsRegistry actionBindings();

    GameEventListener gameEventHandler();

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
     * @param tick the tick count of the global game clock. Note that each game state has its own timer!
     */
    void onTick(long tick);

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
}
