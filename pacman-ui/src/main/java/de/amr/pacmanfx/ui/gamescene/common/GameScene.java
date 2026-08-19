/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.common;

import de.amr.basics.Disposable;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.ComponentRegistry;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.event.StopAllSoundsEvent;
import de.amr.pacmanfx.core.event.base.DefaultGameEventListener;
import de.amr.pacmanfx.core.event.base.GameEventManager;
import de.amr.pacmanfx.core.event.gameplay.CreditAddedEvent;
import de.amr.pacmanfx.core.gamestate.GameFlowController;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d2.Rendering2DSupport;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import javafx.scene.SubScene;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.ScrollEvent;
import org.tinylog.Logger;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Abstract base class for all game scenes (2D and 3D).
 */
public class GameScene implements GameSceneController, DefaultGameEventListener, Disposable {

    private final GameAppContext app;

    private final ComponentRegistry<GameSceneComponent> componentRegistry = new ComponentRegistry<>();

    protected GameScene(GameAppContext app) {
        this.app = requireNonNull(app);
    }

    public Rendering2DSupport rendering2D() {
        Rendering2DSupport r2D = componentRegistry.optComp(Rendering2DSupport.class).orElse(null);
        if (r2D == null) {
            componentRegistry.setComp(Rendering2DSupport.class, new Rendering2DSupport());
            Logger.info("Added Rendering2DSupport to " + getClass().getSimpleName());
        }
        return componentRegistry.reqComp(Rendering2DSupport.class);
    }

    public ActionBindingsSupport actionBindingsSupport() {
        ActionBindingsSupport actionBindings = componentRegistry.optComp(ActionBindingsSupport.class).orElse(null);
        if (actionBindings == null) {
            componentRegistry.setComp(ActionBindingsSupport.class, new ActionBindingsSupport());
            Logger.info("Added ActionBindingsSupport to " + getClass().getSimpleName());
        }
        return componentRegistry.reqComp(ActionBindingsSupport.class);
    }

    public ComponentRegistry<GameSceneComponent> componentsRegistry() {
        return componentRegistry;
    }

    public GameAppContext app() {
        return app;
    }

    public GameContext game() {
        return app.game();
    }

    public GameEventManager eventManager() {
        return game().eventManager();
    }

    public GameFlowController gameFlow() {
        return game().variant().gameFlow();
    }

    /**
     * Hook method called when the game scene becomes active.
     */
    protected void onActivate() {}

    /**
     * Hook method called when the game scene becomes inactive.
     */
    protected void onDeactivate() {}

    /**
     * If a 3D-variant of this game scene is active when the game level gets created, this method has not yet been called,
     * but it gets called when the 3D->2D scene switch happens.
     */
    public void acceptGameLevel(GameSession session, GameLevel level) {
        final Vector2i size = level.worldMap().terrainLayer().sizeInPixel();
        rendering2D().unscaledWidthProperty().set(size.x());
        rendering2D().unscaledHeightProperty().set(size.y());
    }

    /**
     * Hook called when entering this 2D scene from a 3D scene.
     * Subclasses may override to adjust state or transitions.
     */
    public void onEnteredFrom3DScene() {}

    // --- Interface "Disposable"

    @Override
    public void dispose() {
        componentRegistry.dispose();
    }

    // --- Interface "GameSceneController"

    @Override
    public final void activate() {
        onActivate();
    }

    @Override
    public final void deactivate() {
        onDeactivate();
        componentRegistry.optComp(ActionBindingsSupport.class).ifPresent(comp -> comp.bindingsMap().dispose());
        optSoundEffects().ifPresent(GameSoundEffects::stopAll);
    }

    @Override
    public Optional<SubScene> optSubSceneFX() {
        return Optional.empty();
    }

    @Override
    public Optional<GameSoundEffects> optSoundEffects() {
        return app.gameVariants().currentGameVariant().uiConfig().optSoundEffects();
    }

    @Override
    public void onBeforeEmbedded() {
        //TODO remove this hook method
    }

    @Override
    public void onInput() {
        if (componentRegistry.hasComp(ActionBindingsSupport.class)) {
            componentRegistry.reqComp(ActionBindingsSupport.class)
                .bindingsMap()
                .executeMatchingAction(app());
        }
    }

    @Override
    public void onScroll(ScrollEvent scrollEvent) {
        // Used only by very few subclasses
    }

    @Override
    public Optional<ContextMenu> optContextMenu() {
        return Optional.empty();
    }

    // --- Interface "QuitHandler"

    @Override
    public void handleQuit(GameAppContext ac) {
        Logger.info("Game scene {} quit", getClass().getSimpleName());
        deactivate();
    }

    // --- Interface DefaultGameEventListener

    @Override
    public void onCreditAdded(CreditAddedEvent e) {
        optSoundEffects().ifPresent(GameSoundEffects::playCoinInsertedSound);
    }

    @Override
    public void onStopAllSounds(StopAllSoundsEvent event) {
        optSoundEffects().ifPresent(GameSoundEffects::stopAll);
    }
}
