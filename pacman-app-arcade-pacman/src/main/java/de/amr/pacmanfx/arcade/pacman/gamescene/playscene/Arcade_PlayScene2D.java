/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.gamescene.playscene;

import de.amr.basics.InfoMap;
import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.Pulse;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.event.base.GameEventListener;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.basics.rendering.Renderable;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.CheatActions;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.d2.ActorAnimationManager;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneCanvasRenderingComp;
import de.amr.pacmanfx.ui.gamescene.d2.LevelCompletedAnimation;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import de.amr.pacmanfx.uilib.rendering.LevelRenderInfoKey;
import de.amr.pacmanfx.uilib.rendering.RenderableGameLevel;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import org.tinylog.Logger;

import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.pacmanfx.ui.views.ContextMenuSupport.*;

/**
 * 2D play scene for Arcade game variants.
 */
public class Arcade_PlayScene2D extends AbstractGameScene {

    private final GameEventHandler eventHandler = new GameEventHandler(this);

    private LevelCompletedAnimation levelCompletedAnimation;

    public Arcade_PlayScene2D() {
        // Add 2D canvas rendering capability
        setComp(GameSceneCanvasRenderingComp.class, new GameSceneCanvasRenderingComp());
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

        final GameVariantRenderConfig renderConfig = app().variantManager().currentRuntime().uiConfig().renderConfig();
        return Ufx.streamOf(
            createRenderableLevel(level),
            level.entitySet().all().map(renderConfig::renderable)
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
        final TranslationManager translations = app().ui().translationManager();
        final CheatActions cheatActions = app().commonActions().cheatActions();

        final var contextMenu = new ContextMenu();
        addLocalizedTitleItem(contextMenu, translations, "context_menu.pacman");
        addLocalizedCheckBox(contextMenu, translations, game().session().cheats().pacUsingAutopilotProperty(), "context_menu.autopilot").setOnAction(e -> {
            final var checkBox = (CheckMenuItem) e.getSource();
            if (checkBox.isSelected()) {
                app().runAction(cheatActions.actionActivateAutopilot());
            } else {
                app().runAction(cheatActions.actionDeactivateAutopilot());
            }
        });
        addLocalizedCheckBox(contextMenu, translations, game().session().cheats().pacImmuneProperty(), "context_menu.immunity").setOnAction(e -> {
            final var checkBox = (CheckMenuItem) e.getSource();
            if (checkBox.isSelected()) {
                app().runAction(cheatActions.actionActivateImmunity());
            } else {
                app().runAction(cheatActions.actionDeactivateImmunity());
            }
        });
        addSeparator(contextMenu);
        addLocalizedCheckBox(contextMenu, translations, viewModel().muteProperty(), "context_menu.muted");
        addLocalizedActionItem(app(), contextMenu, translations, app().commonActions().gameFlowActions().actionQuit(), "context_menu.quit");

        return Optional.of(contextMenu);
    }

    @Override
    public void onEnteredFrom3DScene() {
        final GameSession session = game().session();
        session.optLevel().ifPresent(level -> acceptGameLevel(session, level));
    }

    @Override
    public void acceptGameLevel(GameSession session, GameLevel level) {
        optCanvasRendering().ifPresent(rendering -> {
            final Vector2i terrainSize = level.worldMap().terrainLayer().sizeInPixel();
            rendering.unscaledWidthProperty().set(terrainSize.x());
            rendering.unscaledHeightProperty().set(terrainSize.y());
        });

        final var bindingsRegistry = actionBindings().registry();
        if (session.isAttractMode()) {
            final Arcade_Actions actions = app().variantManager().currentRuntime()
                .extensionValue(Arcade_GameExtensions.ACTIONS, Arcade_Actions.class);
            bindingsRegistry.registerAllBindings(actions.gameStartActionBindings());
            Logger.info("Game scene {} accepted demo level", getClass().getSimpleName());
            soundManager().setEnabled(false);
        } else {
            bindingsRegistry.registerAllBindings(app().commonActions().steeringActions().bindings());
            bindingsRegistry.registerAllBindings(app().commonActions().cheatActions().bindings());
            Logger.info("Game scene {} accepted level #{}", getClass().getSimpleName(), level.number());
            soundManager().setEnabled(true);
        }
        Logger.info(bindingsRegistry);
        ActorAnimationManager.ensureActorAnimationsCreated(app(), level);
    }

    private RenderableGameLevel createRenderableLevel(GameLevel level) {
        final var renderInfo = new InfoMap();
        renderInfo.put(LevelRenderInfoKey.ENERGIZERS_SHOWN, level.heartbeat().state() == Pulse.State.ON);
        renderInfo.put(LevelRenderInfoKey.SHOW_EMPTY_MAZE, level.food().remainingFoodCount() == 0);
        updateFlashingRenderInfo(renderInfo);
        return new RenderableGameLevel(level, renderInfo);
    }

    private void updateFlashingRenderInfo(InfoMap renderInfo) {
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
    }
}