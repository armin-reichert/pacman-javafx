/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.gamescene.playscene;

import de.amr.basics.ValueMap;
import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.Pulse;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.SessionKey;
import de.amr.pacmanfx.core.entities.world.Energizer;
import de.amr.pacmanfx.core.event.base.GameEventListener;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.ui.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.CheatActions;
import de.amr.pacmanfx.ui.action.CommonGameActions;
import de.amr.pacmanfx.ui.gamescene.animation.ActorAnimationSystem;
import de.amr.pacmanfx.ui.gamescene.animation.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.rendering.GameEntityViewBuilder;
import de.amr.pacmanfx.ui.rendering.GameLevelView;
import de.amr.pacmanfx.ui.rendering.GenericLevelRenderer;
import de.amr.pacmanfx.ui.rendering.RenderingUtil;
import de.amr.pacmanfx.ui.sound.PacManGameSoundEffects;
import de.amr.pacmanfx.uilib.view2d.LevelRenderInfoKey;
import de.amr.pacmanfx.uilib.view2d.TerrainMapColoring;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.pacmanfx.engine.action.ActionExecutor.runAction;
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
        final GameContext game = engine().optCurrentGame().orElse(null);
        if (game == null) {
            return Stream.empty();
        }
        final GameLevel level = game.session().optLevel().orElse(null);
        if (level == null) {
            return Stream.empty();
        }

        final GameVariantRenderConfig renderConfig = engine.uiConfig(runtime()).renderConfig();

        // Only available for generic level renderer in XXL game variants
        final Color pelletColor = RenderingUtil.findPelletColor(level.worldMap()).orElse(null);
        final ValueMap energizerRenderInfo = pelletColor != null
            ? ValueMap.singletonMap(GenericLevelRenderer.RenderInfoKey.PELLET_COLOR, pelletColor)
            : ValueMap.EMPTY_IMMUTABLE_MAP;

        return Ufx.streamOf(
            createRenderableLevel(level),

            level.entitySet().stream().map(entity -> {
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
        engine().optCurrentGame().ifPresent(game -> {
            levelCompletedAnimation = new LevelCompletedAnimation();
            levelCompletedAnimation.setOnFinished(() -> game.state().triggerTimeout());
        });
    }

    @Override
    public void onTick(GameContext game) {
        game.session().optLevel().ifPresent(
            level -> optComponent(PacManGameSoundEffects.class).ifPresent(sfx -> sfx.playAmbientGameLevelSound(game, level)));
    }

    @Override
    public void onQuit() {
        engine().optCurrentGame().ifPresent(game -> {
            onDeactivate();
            // Avoid game over sound being played
            engine().soundManager().setEnabled(false);
            runtime().gameFlow().enterGameState(game, CommonGameStateID.GAME_OVER);
        });
    }

    @Override
    public Optional<ContextMenu> optContextMenu() {
        final CheatActions cheatActions = CommonGameActions.instance().cheatActions();

        final var contextMenu = new ContextMenu();

        engine().optCurrentGame().ifPresent(game -> {
            addLocalizedTitleItem(contextMenu, engine().translationManager(), "context_menu.pacman");
            addLocalizedCheckBox(contextMenu, engine().translationManager(),
                game.session().cheats().pacUsingAutopilotProperty(), "context_menu.autopilot").setOnAction(e -> {
                final var checkBox = (CheckMenuItem) e.getSource();
                if (checkBox.isSelected()) {
                    runAction(cheatActions.actionActivateAutopilot(), engine());
                } else {
                    runAction(cheatActions.actionDeactivateAutopilot(), engine());
                }
            });
            addLocalizedCheckBox(contextMenu, engine().translationManager(),
                game.session().cheats().pacImmuneProperty(), "context_menu.immunity").setOnAction(e -> {
                final var checkBox = (CheckMenuItem) e.getSource();
                if (checkBox.isSelected()) {
                    runAction(cheatActions.actionActivateImmunity(), engine());
                } else {
                    runAction(cheatActions.actionDeactivateImmunity(), engine());
                }
            });
            addSeparator(contextMenu);
            addLocalizedCheckBox(contextMenu, engine().translationManager(), engine.ui().viewModel().muteProperty(), "context_menu.muted");
            addLocalizedActionItem(engine(), contextMenu, engine().translationManager(), CommonGameActions.instance().gameFlowActions().actionQuit(), "context_menu.quit");

        });

        return Optional.of(contextMenu);
    }

    @Override
    public void onEnteredFrom3DScene() {
        engine().optCurrentGame().ifPresent(game -> {
            final GameSession session = game.session();
            session.optLevel().ifPresent(level -> onAcceptGameLevel(session, level));
        });
    }

    @Override
    public void onAcceptGameLevel(GameSession session, GameLevel level) {
        // Custom maps can have arbitrary sizes, so adapt scene size here
        final Vector2i terrainSize = level.worldMap().terrainLayer().sizeInPixel();
        view2D().unscaledWidthProperty().set(terrainSize.x());
        view2D().unscaledHeightProperty().set(terrainSize.y());

        // Action bindings (demo level, normal level)
        final var bindingsRegistry = actionBindingsRegistry();
        if (session.booleanValue(SessionKey.DEMO_LEVEL_MODE)) {
            bindingsRegistry.registerAllBindings(Arcade_Actions.instance().gameStartActionBindings());
            Logger.info("Game scene {} accepted demo level", getClass().getSimpleName());
            engine().soundManager().setEnabled(false);
        }
        else {
            bindingsRegistry.registerAllBindings(CommonGameActions.instance().steeringActions().bindings());
            bindingsRegistry.registerAllBindings(CommonGameActions.instance().cheatActions().bindings());
            Logger.info("Game scene {} accepted level #{}", getClass().getSimpleName(), level.number());
            engine().soundManager().setEnabled(true);
        }
        Logger.info(bindingsRegistry);

        // TODO check this
        ActorAnimationSystem.ensureActorAnimationsCreated(engine, runtime(), engine.spriteAnimationSystem(), level);
    }

    private GameLevelView createRenderableLevel(GameLevel level) {
        final var renderInfo = ValueMap.create();
        renderInfo.setValue(LevelRenderInfoKey.ENERGIZERS_SHOWN, level.heartbeat().state() == Pulse.State.ON);
        renderInfo.setValue(LevelRenderInfoKey.SHOW_EMPTY_MAZE, level.food().remainingFoodCount() == 0);
        boolean showBrightMaze = false;
        boolean mazeIsFlashing = false;
        if (levelCompletedAnimation != null) {
            final var flashingState = levelCompletedAnimation.optFlashingState().orElse(null);
            if (flashingState != null) {
                showBrightMaze = flashingState.isHighlighted();
                mazeIsFlashing = flashingState.isFlashing();
            }
        }
        renderInfo.setValue(LevelRenderInfoKey.SHOW_BRIGHT_MAZE, showBrightMaze);
        renderInfo.setValue(LevelRenderInfoKey.MAZE_IS_FLASHING, mazeIsFlashing);

        final TerrainMapColoring terrainMapColoring = RenderingUtil.findMapColoring(engine.ui().viewModel(), level.worldMap());
        if (terrainMapColoring != null) {
            // Only available for generic level renderer in XXL game variants
            renderInfo.setValue(GenericLevelRenderer.RenderInfoKey.TERRAIN_MAP_COLORING, terrainMapColoring);
        }

        return new GameLevelView(level, renderInfo, RenderingLayer.LEVEL, 0, Vector2f.ZERO);
    }
}