/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes.playscene;

import de.amr.basics.InfoMap;
import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.Pulse;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.HUD;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.ui.action.CheatActions;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.ActorAnimationManager;
import de.amr.pacmanfx.ui.gamescene.d2.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneCanvasRenderingComp;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import de.amr.pacmanfx.uilib.rendering.LevelRenderInfoKey;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import org.tinylog.Logger;

import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.pacmanfx.ui.views.ContextMenuSupport.*;

/**
 * 2D play scene for Arcade game variants.
 */
public class Arcade_PlayScene2D extends GameScene implements Arcade_PlayScene2D_GameEventHandler {

    private LevelCompletedAnimation levelCompletedAnimation;

    public Arcade_PlayScene2D(GameAppContext app) {
        super(app);
        // Add 2D canvas rendering capability
        setComp(GameSceneCanvasRenderingComp.class, new GameSceneCanvasRenderingComp());
    }

    @Override
    public Stream<Renderable> renderables() {
        final GameLevel level = game().session().optLevel().orElse(null);
        if (level == null) {
            return Stream.empty();
        }
        return Ufx.streamOf(
            createLevelRenderable(level),
            level.visibleRenderables()
        );
    }

    @Override
    public Arcade_PlayScene2D theGameScene() {
        return this;
    }

    @Override
    protected void onActivate() {
        final HUD hud = game().session().hud();
        hud.levelCounter().show();
        hud.livesCounter().show();
        game().session().setHudVisible(true);
    }

    @Override
    public void onTick(GameContext game) {
        game.session().optLevel().ifPresent(level -> {
            optSoundEffects().ifPresent(sfx -> sfx.playAmbientGameLevelSound(game(), level));
        });
    }

    @Override
    public void onQuit() {
        onDeactivate();
        // Avoid game over sound being played
        soundManager().setEnabled(false);
        flow().enterGameState(game(), CommonGameStateID.GAME_OVER);
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

    public void setLevelCompletedAnimation(LevelCompletedAnimation levelCompletedAnimation) {
        this.levelCompletedAnimation = levelCompletedAnimation;
    }

    @Override
    public void acceptGameLevel(GameSession session, GameLevel level) {
        optCanvasRendering().ifPresent(rendering -> {
            final Vector2i terrainSize = level.worldMap().terrainLayer().sizeInPixel();
            rendering.unscaledWidthProperty().set(terrainSize.x());
            rendering.unscaledHeightProperty().set(terrainSize.y());
        });

        if (session.isAttractMode()) {
            acceptDemoLevel();
        } else {
            acceptNormalLevel(level);
        }
        ActorAnimationManager.ensureActorAnimationsCreated(app(), level);
    }

    private void acceptNormalLevel(GameLevel level) {
        final var bindingsMap = actionBindingsSupport().registry();
        bindingsMap.registerAllBindings(app().commonActions().steeringActions().bindings());
        bindingsMap.registerAllBindings(app().commonActions().cheatActions().bindings());

        soundManager().setEnabled(true);

        Logger.info("Game scene {} accepted level #{}", getClass().getSimpleName(), level.number());
        Logger.info(bindingsMap);
    }

    private void acceptDemoLevel() {
        final Arcade_Actions actions = app().currentGameVariantUIConfig()
            .extensionValue(Arcade_GameExtensions.ACTIONS, Arcade_Actions.class);

        final var bindingsMap = actionBindingsSupport().registry();
        bindingsMap.registerAllBindings(actions.gameStartActionBindings());

        soundManager().setEnabled(false);

        Logger.info("Game scene {} accepted demo level", getClass().getSimpleName());
        Logger.info(bindingsMap);
    }

    private Renderable createLevelRenderable(GameLevel level) {
        final var info = new InfoMap();

        info.put(LevelRenderInfoKey.ENERGIZERS_SHOWN,
            level.heartbeat().state() == Pulse.State.ON);

        info.put(LevelRenderInfoKey.SHOW_EMPTY_MAZE,
            level.food().remainingFoodCount() == 0);

        // TODO: This does not belong here
        //       In Arcade Pac-Man, a dedicated image is used for painting the bright empty maze while flashing
        final AssetMap assets = app().currentGameVariantUIConfig().assets();
        if (assets.containsAsset("maze.bright")) {
            info.put(LevelRenderInfoKey.BRIGHT_MAZE_IMAGE, assets.image("maze.bright"));
        }

        info.put(LevelRenderInfoKey.SHOW_BRIGHT_MAZE, false);
        info.put(LevelRenderInfoKey.MAZE_IS_FLASHING, false);
        if (levelCompletedAnimation != null) {
            levelCompletedAnimation.flashingState().ifPresent(flashing -> {
                info.put(LevelRenderInfoKey.SHOW_BRIGHT_MAZE, flashing.isHighlighted());
                info.put(LevelRenderInfoKey.MAZE_IS_FLASHING, flashing.isFlashing());
            });
        }

        return new GameLevelRenderable(level, info);
    }
}