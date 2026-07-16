/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.state.GameState;
import de.amr.pacmanfx.core.state.GameStateID;
import de.amr.pacmanfx.ui.action.CheatActions;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.gamescene.d2.ActorAnimationManager;
import de.amr.pacmanfx.ui.gamescene.d2.LevelCompletedAnimation;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.pacmanfx.ui.views.ContextMenuSupport.*;

/**
 * 2D play scene for Arcade game variants.
 */
public class Arcade_PlayScene2D extends AbstractGameScene2D
    implements Arcade_PlayScene2D_GameEventHandler
{
    private LevelCompletedAnimation levelCompletedAnimation;

    public Arcade_PlayScene2D(GameAppContext actionContext) {
        super(actionContext);
    }

    @Override
    public Arcade_PlayScene2D gameScene() {
        return this;
    }

    @Override
    public void onTick(GameContext gameContext) {
        gameContext().model().optLevel().ifPresent(level -> {
            ActorAnimationManager.ensureActorAnimationsCreated(actionContext(), level);
            updateLivesCounter(gameState(), gameModel(), level.entities().pac());
            optSoundEffects().ifPresent(sfx -> sfx.playAmbientGameLevelSound(gameContext(), level));
        });
    }

    @Override
    public void handleQuit(GameAppContext actionContext) {
        onDeactivate();
        gameContext().flow().enterState(gameContext(), GameStateID.GAME_OVER);
    }

    @Override
    public Optional<ContextMenu> optContextMenu() {
        final TranslationManager translations = actionContext().ui().translations();
        final CheatActions cheatActions = actionContext().commonActions().cheatActions();

        final var contextMenu = new ContextMenu();
        addLocalizedTitleItem(contextMenu, translations, "context_menu.pacman");
        addLocalizedCheckBox(contextMenu, translations, gameContext().cheats().pacUsingAutopilotProperty(), "context_menu.autopilot").setOnAction(e -> {
            final var checkBox = (CheckMenuItem) e.getSource();
            if (checkBox.isSelected()) {
                cheatActions.actionActivateAutopilot().execute();
            } else {
                cheatActions.actionDeactivateAutopilot().execute();
            }
        });
        addLocalizedCheckBox(contextMenu, translations, gameContext().cheats().pacImmuneProperty(), "context_menu.immunity").setOnAction(e -> {
            final var checkBox = (CheckMenuItem) e.getSource();
            if (checkBox.isSelected()) {
                cheatActions.actionActivateImmunity().execute();
            } else {
                cheatActions.actionDeactivateImmunity().execute();
            }
        });
        addSeparator(contextMenu);
        addLocalizedCheckBox(contextMenu, translations, actionContext().ui().viewModel().mutedProperty, "context_menu.muted");
        addLocalizedActionItem(contextMenu, translations, actionContext().commonActions().gameFlowActions().actionQuit(), "context_menu.quit");

        return Optional.of(contextMenu);
    }

    @Override
    public void onEnteredFrom3DScene() {
        gameContext().model().optLevel().ifPresent(this::acceptGameLevel);
    }

    public void setLevelCompletedAnimation(LevelCompletedAnimation levelCompletedAnimation) {
        this.levelCompletedAnimation = levelCompletedAnimation;
    }

    // Expose animation to scene renderer
    public Optional<LevelCompletedAnimation> optLevelCompletedAnimation() {
        return Optional.ofNullable(levelCompletedAnimation);
    }

    @Override
    public void acceptGameLevel(GameLevel level) {
        final Vector2i terrainSize = level.worldMap().terrainLayer().sizeInPixel();
        unscaledWidthProperty().set(terrainSize.x());
        unscaledHeightProperty().set(terrainSize.y());
        if (level.isDemoLevel()) {
            acceptDemoLevel();
        } else {
            acceptNormalLevel(level);
        }
    }

    private void acceptNormalLevel(GameLevel level) {
        actionBindings().registerAllBindings(actionContext().commonActions().steeringActions().bindings());
        actionBindings().registerAllBindings(actionContext().commonActions().cheatActions().bindings());
        Logger.info(actionBindings());
        actionContext().ui().sounds().setEnabled(true);
        Logger.info("Game scene {} accepted game level #{}", getClass().getSimpleName(), level.number());
    }

    private void acceptDemoLevel() {
        final Arcade_Actions actions = actionContext().getExtensionValue(Arcade_GameExtensions.ACTIONS, Arcade_Actions.class);

        actionBindings().registerAllBindings(actions.gameStartActionBindings());
        Logger.info(actionBindings());
        actionContext().ui().sounds().setEnabled(false);
        Logger.info("Game scene {} accepted demo level", getClass().getSimpleName());
    }

    // While Pac-Man is not yet visible on game/level start, an additional lives symbol more is shown in the counter
    private void updateLivesCounter(GameState state, GameModel model, Pac pac) {
        final boolean oneMore = GameStateID.GAME_OR_LEVEL_STARTING.identifies(state) && !pac.isVisible();
        final int livesToDisplay = model.lives().count() - 1 + (oneMore ? 1 : 0);
        final int livesDisplayed = Math.clamp(livesToDisplay, 0, model.hudState().maxLivesShown());
        model.hudState().setLivesCount(livesDisplayed);
    }
}