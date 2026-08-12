/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.session.GameSession;
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

    public Arcade_PlayScene2D(GameAppContext app) {
        super(app);
    }

    @Override
    public Arcade_PlayScene2D playScene() {
        return this;
    }

    @Override
    public void onTick(GameContext game) {
        final GameSession session = game.session();
        session.optLevel().ifPresent(level -> {
            ActorAnimationManager.ensureActorAnimationsCreated(app(), level);
            updateLivesCounter(session, level.entities().pac());
            optSoundEffects().ifPresent(sfx -> sfx.playAmbientGameLevelSound(game(), level));
        });
    }

    @Override
    public void handleQuit(GameAppContext app) {
        onDeactivate();
        // Avoid game over sound being played
        app.ui().sounds().setEnabled(false);
        gameFlow().enterState(game(), CommonGameStateID.GAME_OVER);
    }

    @Override
    public Optional<ContextMenu> optContextMenu() {
        final TranslationManager translations = app().ui().translations();
        final CheatActions cheatActions = app().commonActions().cheatActions();

        final var contextMenu = new ContextMenu();
        addLocalizedTitleItem(contextMenu, translations, "context_menu.pacman");
        addLocalizedCheckBox(contextMenu, translations, game().session().cheats().pacUsingAutopilotProperty(), "context_menu.autopilot").setOnAction(e -> {
            final var checkBox = (CheckMenuItem) e.getSource();
            if (checkBox.isSelected()) {
                cheatActions.actionActivateAutopilot().execute();
            } else {
                cheatActions.actionDeactivateAutopilot().execute();
            }
        });
        addLocalizedCheckBox(contextMenu, translations, game().session().cheats().pacImmuneProperty(), "context_menu.immunity").setOnAction(e -> {
            final var checkBox = (CheckMenuItem) e.getSource();
            if (checkBox.isSelected()) {
                cheatActions.actionActivateImmunity().execute();
            } else {
                cheatActions.actionDeactivateImmunity().execute();
            }
        });
        addSeparator(contextMenu);
        addLocalizedCheckBox(contextMenu, translations, app().ui().viewModel().mutedProperty, "context_menu.muted");
        addLocalizedActionItem(contextMenu, translations, app().commonActions().gameFlowActions().actionQuit(), "context_menu.quit");

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

    // Expose animation to scene renderer
    public Optional<LevelCompletedAnimation> optLevelCompletedAnimation() {
        return Optional.ofNullable(levelCompletedAnimation);
    }

    @Override
    public void acceptGameLevel(GameSession session, GameLevel level) {
        final Vector2i terrainSize = level.worldMap().terrainLayer().sizeInPixel();
        unscaledWidthProperty().set(terrainSize.x());
        unscaledHeightProperty().set(terrainSize.y());
        if (session.isAttractMode()) {
            acceptDemoLevel();
        } else {
            acceptNormalLevel(level);
        }
    }

    private void acceptNormalLevel(GameLevel level) {
        actionBindings().registerAllBindings(app().commonActions().steeringActions().bindings());
        actionBindings().registerAllBindings(app().commonActions().cheatActions().bindings());
        Logger.info(actionBindings());
        app().ui().sounds().setEnabled(true);
        Logger.info("Game scene {} accepted game level #{}", getClass().getSimpleName(), level.number());
    }

    private void acceptDemoLevel() {
        final Arcade_Actions actions = app().getExtensionValue(Arcade_GameExtensions.ACTIONS, Arcade_Actions.class);

        actionBindings().registerAllBindings(actions.gameStartActionBindings());
        Logger.info(actionBindings());
        app().ui().sounds().setEnabled(false);
        Logger.info("Game scene {} accepted demo level", getClass().getSimpleName());
    }

    // While Pac-Man is not yet visible on game/level start, an additional lives symbol more is shown in the counter
    private void updateLivesCounter(GameSession session, Pac pac) {
        final boolean oneMore = CommonGameStateID.GAME_OR_LEVEL_STARTING.hasSameNameAs(session.gameState()) && !pac.isVisible();
        final int livesToDisplay = session.livesCounter().data().numLives() - 1 + (oneMore ? 1 : 0);
        final int livesDisplayed = Math.clamp(livesToDisplay, 0, session.hud().maxLivesShown());
        session.hud().setLivesCount(livesDisplayed);
    }
}