package de.amr.pacmanfx.ui.gamescene.common;

import de.amr.basics.Disposable;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.base.GameEventListener;
import de.amr.pacmanfx.core.gamestate.GameFlow;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.ui.action.core.GameApp;
import de.amr.pacmanfx.ui.action.core.QuitHandler;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.ui.vm.GameViewModel;
import javafx.scene.SubScene;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.ScrollEvent;

import java.util.Optional;
import java.util.stream.Stream;

public interface GameScene extends Disposable, QuitHandler {

    GameApp app();

    default GameViewModel viewModel() {
        return app().ui().viewModel();
    }

    default GameContext game() {
        return app().game();
    }

    default GameFlow gameFlow() {
        return game().playConfig().gameFlow();
    }

    default Optional<GameEventListener> optGameEventHandler() {
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
    default void onEnteredFrom3DScene() {
    }

    //TODO remove this hook method
    default void onBeforeEmbedded() {}

    default void onScroll(ScrollEvent scrollEvent) {
        // Used only by very few subclasses
    }

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
     * Executes the first matching action.
     */
    void onInput();

    default Optional<ContextMenu> optContextMenu() {
        return Optional.empty();
    }

    default Optional<SubScene> optSubSceneFX() {
        return Optional.empty();
    }

    default SoundManager soundManager() {
        return app().ui().soundManager();
    }

    default Optional<GameSoundEffects> optSoundEffects() {
        return app().variantManager().currentRuntime().uiConfig().optSoundEffects();
    }
}