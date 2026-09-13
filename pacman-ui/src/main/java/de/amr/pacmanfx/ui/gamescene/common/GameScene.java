/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.common;

import de.amr.basics.Composition;
import de.amr.basics.Disposable;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.ecs.comp.RenderingLayer;
import de.amr.pacmanfx.core.event.StopAllSoundsEvent;
import de.amr.pacmanfx.core.event.base.DefaultGameEventListener;
import de.amr.pacmanfx.core.event.base.GameEventManager;
import de.amr.pacmanfx.core.event.gameplay.CreditAddedEvent;
import de.amr.pacmanfx.core.gamestate.GameFlowController;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d2.SceneCanvasRenderingComp;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.ui.vm.GameViewModel;
import javafx.scene.SubScene;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.ScrollEvent;

import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Abstract base class for all game scenes (2D and 3D).
 */
public abstract class GameScene extends Composition<GameSceneComponent>
    implements GameSceneController, DefaultGameEventListener, Disposable, Renderable
{
    //TODO Should a game scene really be a renderable itself or only produce renderables?

    private final GameAppContext app;

    protected GameScene(GameAppContext app) {
        this.app = requireNonNull(app);
    }

    @Override
    public RenderingLayer layer() {
        return RenderingLayer.SCENE;
    }

    /**
     * @return the renderables produced by this game scene
     */
    public abstract Stream<Renderable> renderables();

    public Optional<SceneCanvasRenderingComp> optCanvasRendering() {
        return optComp(SceneCanvasRenderingComp.class);
    }

    public SceneCanvasRenderingComp reqCanvasRendering() {
        return reqComp(SceneCanvasRenderingComp.class);
    }

    public boolean wantsClearCanvas() {
        return true;
    }

    public ActionBindingsComp actionBindingsSupport() {
        ActionBindingsComp actionBindings = optComp(ActionBindingsComp.class).orElse(null);
        if (actionBindings == null) {
            setComp(ActionBindingsComp.class, new ActionBindingsComp(this));
        }
        return reqComp(ActionBindingsComp.class);
    }

    public GameAppContext app() {
        return app;
    }

    public GameEventManager eventManager() {
        return game().eventManager();
    }

    public GameFlowController flow() {
        return game().variantPlayConfig().gameFlow();
    }

    public GameContext game() {
        return app.game();
    }

    public GameViewModel viewModel() {
        return app.ui().viewModel();
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
        optCanvasRendering().ifPresent(canvasRendering -> {
            final Vector2i terrainSize = level.worldMap().terrainLayer().sizeInPixel();
            canvasRendering.unscaledWidthProperty().set(terrainSize.x());
            canvasRendering.unscaledHeightProperty().set(terrainSize.y());
        });
    }

    /**
     * Hook called when entering this 2D scene from a 3D scene.
     * Subclasses may override to adjust state or transitions.
     */
    public void onEnteredFrom3DScene() {}

    // --- Interface "GameSceneController"

    @Override
    public final void activate() {
        onActivate();
    }

    @Override
    public final void deactivate() {
        onDeactivate();
        optComp(ActionBindingsComp.class).ifPresent(comp -> comp.registry().dispose());
        optSoundEffects().ifPresent(GameSoundEffects::stopAll);
    }

    @Override
    public Optional<SubScene> optSubSceneFX() {
        return Optional.empty();
    }

    @Override
    public SoundManager soundManager() {
        return app.ui().soundManager();
    }

    @Override
    public Optional<GameSoundEffects> optSoundEffects() {
        return app.variantManager().currentVariantConfig().uiConfig().optSoundEffects();
    }

    @Override
    public void onBeforeEmbedded() {
        //TODO remove this hook method
    }

    @Override
    public void onInput() {
        if (hasComp(ActionBindingsComp.class)) {
            reqComp(ActionBindingsComp.class)
                .registry()
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
    public void onQuit() {
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
