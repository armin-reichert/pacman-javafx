/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.gamescene.playscene;

import de.amr.basics.InfoMap;
import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.Pulse;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.entities.world.Energizer;
import de.amr.pacmanfx.core.event.base.GameEventListener;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.CheatActions;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.d2.ActorAnimationSystem;
import de.amr.pacmanfx.ui.gamescene.d2.GameLevelView;
import de.amr.pacmanfx.ui.gamescene.d2.GenericLevelRenderer;
import de.amr.pacmanfx.ui.gamescene.d2.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.rendering.GameEntityViewBuilder;
import de.amr.pacmanfx.ui.rendering.RenderingUtil;
import de.amr.pacmanfx.uilib.view2d.LevelRenderInfoKey;
import de.amr.pacmanfx.uilib.view2d.TerrainMapColoring;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.pacmanfx.ui.views.ContextMenuSupport.*;

/**
 * 2D play scene for Arcade game variants.
 */
public class Arcade_PlayScene2D extends AbstractGameScene {

    private final PlaySceneGameEventHandler eventHandler = new PlaySceneGameEventHandler(this);

    private LevelCompletedAnimation levelCompletedAnimation;

    public Arcade_PlayScene2D() {
        view2D().setUnscaledWidth(WorldMap.ARCADE_MAP_SIZE_IN_PIXELS.x());
        view2D().setUnscaledHeight(WorldMap.ARCADE_MAP_SIZE_IN_PIXELS.y());
    }

    public LevelCompletedAnimation levelCompletedAnimation() {
        return levelCompletedAnimation;
    }

    @Override
    public Optional<GameEventListener> optGameEventHandler() {
        return Optional.of(eventHandler);
    }

    @Override
    public Stream<Renderable> renderables() {
        final GameLevel level = game().session().optLevel().orElse(null);
        if (level == null) {
            return Stream.empty();
        }

        final GameVariantRenderConfig renderConfig = engine().gameVariantManager().currentRuntime().uiConfig().renderConfig();

        // Only available for generic level renderer in XXL game variants
        final Color pelletColor = RenderingUtil.findPelletColor(level.worldMap());
        final InfoMap energizerRenderInfo = InfoMap.create();
        if (pelletColor != null) {
            energizerRenderInfo.put(GenericLevelRenderer.RenderInfoKey.PELLET_COLOR, pelletColor);
        }

        return Ufx.streamOf(
            createRenderableLevel(level),

            level.entitySet().all().map(entity -> {
                if (entity instanceof Energizer energizer) {
                    return GameEntityViewBuilder.builder()
                        .entity(energizer)
                        .layer(RenderingLayer.LEVEL)
                        .renderInfo(energizerRenderInfo)
                        .build();
                } else {
                    return renderConfig.createEntityView(entity);
                }
            })
        );
    }

    @Override
    protected void onActivate() {
        levelCompletedAnimation = new LevelCompletedAnimation();
        levelCompletedAnimation.setOnFinished(() -> game().state().triggerTimeout());
    }

    @Override
    public void onTick(GameContext game) {
        game.session().optLevel().ifPresent(
            level -> optSoundEffects().ifPresent(sfx -> sfx.playAmbientGameLevelSound(game(), level)));
    }

    @Override
    public void onQuit() {
        onDeactivate();
        // Avoid game over sound being played
        soundManager().setEnabled(false);
        gameFlow().enterGameState(game(), CommonGameStateID.GAME_OVER);
    }

    @Override
    public Optional<ContextMenu> optContextMenu() {
        final CheatActions cheatActions = engine().commonActions().cheatActions();

        final var contextMenu = new ContextMenu();
        addLocalizedTitleItem(contextMenu, engine().translationManager(), "context_menu.pacman");
        addLocalizedCheckBox(contextMenu, engine().translationManager(), game().session().cheats().pacUsingAutopilotProperty(), "context_menu.autopilot").setOnAction(e -> {
            final var checkBox = (CheckMenuItem) e.getSource();
            if (checkBox.isSelected()) {
                engine().runAction(cheatActions.actionActivateAutopilot());
            } else {
                engine().runAction(cheatActions.actionDeactivateAutopilot());
            }
        });
        addLocalizedCheckBox(contextMenu, engine().translationManager(), game().session().cheats().pacImmuneProperty(), "context_menu.immunity").setOnAction(e -> {
            final var checkBox = (CheckMenuItem) e.getSource();
            if (checkBox.isSelected()) {
                engine().runAction(cheatActions.actionActivateImmunity());
            } else {
                engine().runAction(cheatActions.actionDeactivateImmunity());
            }
        });
        addSeparator(contextMenu);
        addLocalizedCheckBox(contextMenu, engine().translationManager(), viewModel().muteProperty(), "context_menu.muted");
        addLocalizedActionItem(engine(), contextMenu, engine().translationManager(), engine().commonActions().gameFlowActions().actionQuit(), "context_menu.quit");

        return Optional.of(contextMenu);
    }

    @Override
    public void onEnteredFrom3DScene() {
        final GameSession session = game().session();
        session.optLevel().ifPresent(level -> onAcceptGameLevel(session, level));
    }

    @Override
    public void onAcceptGameLevel(GameSession session, GameLevel level) {
        // Custom maps can have arbitrary sizes, so adapt scene size here
        final Vector2i terrainSize = level.worldMap().terrainLayer().sizeInPixel();
        view2D().unscaledWidthProperty().set(terrainSize.x());
        view2D().unscaledHeightProperty().set(terrainSize.y());

        // Action bindings (demo level, normal level)
        final var bindingsRegistry = actionBindings().registry();
        if (session.isAttractMode()) {
            final Arcade_Actions actions = engine().gameVariantManager().currentRuntime()
                .extensionValue(Arcade_GameExtensions.ACTIONS, Arcade_Actions.class);
            bindingsRegistry.registerAllBindings(actions.gameStartActionBindings());
            Logger.info("Game scene {} accepted demo level", getClass().getSimpleName());
            soundManager().setEnabled(false);
        }
        else {
            bindingsRegistry.registerAllBindings(engine().commonActions().steeringActions().bindings());
            bindingsRegistry.registerAllBindings(engine().commonActions().cheatActions().bindings());
            Logger.info("Game scene {} accepted level #{}", getClass().getSimpleName(), level.number());
            soundManager().setEnabled(true);
        }
        Logger.info(bindingsRegistry);

        // TODO check this
        ActorAnimationSystem.ensureActorAnimationsCreated(engine().gameVariantManager().currentRuntime(), level);
    }

    private GameLevelView createRenderableLevel(GameLevel level) {
        final var renderInfo = InfoMap.create();
        renderInfo.put(LevelRenderInfoKey.ENERGIZERS_SHOWN, level.heartbeat().state() == Pulse.State.ON);
        renderInfo.put(LevelRenderInfoKey.SHOW_EMPTY_MAZE, level.food().remainingFoodCount() == 0);
        boolean showBrightMaze = false;
        boolean mazeIsFlashing = false;
        if (levelCompletedAnimation != null) {
            final var flashingState = levelCompletedAnimation.optFlashingState().orElse(null);
            if (flashingState != null) {
                showBrightMaze = flashingState.isHighlighted();
                mazeIsFlashing = flashingState.isFlashing();
            }
        }
        renderInfo.put(LevelRenderInfoKey.SHOW_BRIGHT_MAZE, showBrightMaze);
        renderInfo.put(LevelRenderInfoKey.MAZE_IS_FLASHING, mazeIsFlashing);

        final TerrainMapColoring terrainMapColoring = RenderingUtil.findMapColoring(engine().ui().viewModel(), level.worldMap());
        if (terrainMapColoring != null) {
            // Only available for generic level renderer in XXL game variants
            renderInfo.put(GenericLevelRenderer.RenderInfoKey.TERRAIN_MAP_COLORING, terrainMapColoring);
        }

        return new GameLevelView(level, renderInfo, RenderingLayer.LEVEL, 0, Vector2f.ZERO);
    }
}