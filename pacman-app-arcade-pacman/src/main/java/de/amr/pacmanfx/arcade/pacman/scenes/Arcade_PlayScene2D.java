/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman.model.Arcade_GameModel;
import de.amr.pacmanfx.arcade.pacman.model.Arcade_GameState;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.AppContext;
import de.amr.pacmanfx.ui.AppConstants;
import de.amr.pacmanfx.ui.action.CheatActions;
import de.amr.pacmanfx.ui.action.CommonActions;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.d2.LevelCompletedAnimation;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.pacmanfx.ui.subviews.ContextMenuSupport.*;

/**
 * 2D play scene for Arcade game variants.
 */
public class Arcade_PlayScene2D extends GameScene2D {

    private LevelCompletedAnimation levelCompletedAnimation;

    public Arcade_PlayScene2D(AppContext context) {
        super(context);
        setGameEventHandler(new Arcade_PlayScene2DGameEventHandler(this));
    }

    public LevelCompletedAnimation levelCompletedAnimation() {
        return levelCompletedAnimation;
    }

    @Override
    public void onTick(GameClock clock) {
        final Arcade_GameModel game = context().currentGame();
        game.optGameLevel().ifPresent(level -> {
            updateLivesCounter(level);
            context().currentSoundEffects().ifPresent(sfx -> {
                sfx.setEnabled(!level.isDemoLevel());
                sfx.playLevelRunningSound(level);
            });
        });
    }

    @Override
    public Optional<ContextMenu> supplyContextMenu() {
        final Game game = context().currentGame();
        final var menu = new ContextMenu();
        addLocalizedTitleItem(menu, context.ui().translations(), "pacman");
        addLocalizedCheckBox(menu, context.ui().translations(), game.cheats().usingAutopilotProperty(), "autopilot").setOnAction(e -> {
            final var checkBox = (CheckMenuItem) e.getSource();
            if (checkBox.isSelected()) {
                CheatActions.ACTION_ACTIVATE_AUTOPILOT.executeIfEnabled(context);
            } else {
                CheatActions.ACTION_DEACTIVATE_AUTOPILOT.executeIfEnabled(context);
            }
        });
        addLocalizedCheckBox(menu, context.ui().translations(), game.cheats().immuneProperty(), "immunity").setOnAction(e -> {
            final var checkBox = (CheckMenuItem) e.getSource();
            if (checkBox.isSelected()) {
                CheatActions.ACTION_ACTIVATE_IMMUNITY.executeIfEnabled(context);
            } else {
                CheatActions.ACTION_DEACTIVATE_IMMUNITY.executeIfEnabled(context);
            }
        });
        addSeparator(menu);
        addLocalizedCheckBox(menu, context.ui().translations(), AppConstants.PROPERTY_MUTED, "muted");
        addLocalizedActionItem(menu, context, context.ui().translations(), CommonActions.ACTION_QUIT_GAME_SCENE, "quit");

        return Optional.of(menu);
    }

    @Override
    public void onEnteredFrom3DScene() {
        context().currentGame().optGameLevel().ifPresent(this::acceptGameLevel);
    }

    // Others

    // Expose flashing animation state to renderer
    public Optional<LevelCompletedAnimation.FlashingState> optFlashingState() {
        return Optional.ofNullable(levelCompletedAnimation).flatMap(LevelCompletedAnimation::flashingState);
    }

    /**
     * If the 3D play scene is shown when the game level gets created, the onLevelCreated() method of this
     * scene is not called, so we have to accept the game level again when switching from the 3D scene to this one.
     */
    protected void acceptGameLevel(GameLevel level) {
        actionBindings.registerAllBindings(ArcadePacMan_UIConfig.GAME_START_ACTION_BINDINGS);
        if (!level.isDemoLevel()) {
            actionBindings.registerAllBindings(AppConstants.STEERING_ACTION_BINDINGS);
            actionBindings.registerAllBindings(AppConstants.CHEAT_ACTION_BINDINGS);
        }
        Logger.info(actionBindings);

        context.ui().sounds().setEnabled(!level.isDemoLevel()); //TODO is this needed?
        levelCompletedAnimation = new LevelCompletedAnimation(level, () -> context().currentGameState().expire());

        final Vector2i terrainSize = level.worldMap().terrainLayer().sizeInPixel();
        unscaledWidthProperty().set(terrainSize.x());
        unscaledHeightProperty().set(terrainSize.y());

        Logger.info("Game scene {} accepted game level #{}", getClass().getSimpleName(), level.number());
    }

    // Private

    // While Pac-Man is not yet visible on level start, one symbol more is shown in the lives counter
    private void updateLivesCounter(GameLevel level) {
        final Game game = context().currentGame();
        final int additionalLives = context().currentGameState() == Arcade_GameState.STARTING_GAME_OR_LEVEL
            && !level.entities().pac().isVisible() ? 1 : 0;
        final int count = Math.clamp(game.lifeCount() - 1 + additionalLives, 0, game.hud().maxLivesDisplayed());
        game.hud().setVisibleLifeCount(count);
    }

    protected void resetActorAnimations(GameLevel level) {
        final Pac pac = level.entities().pac();
        pac.animations().select(ArcadePacMan_AnimationID.PAC_MUNCHING);
        pac.animations().resetSelected();
        level.ghosts().forEach(ghost -> {
            ghost.animations().select(ArcadePacMan_AnimationID.GHOST_NORMAL);
            ghost.animations().resetSelected();
        });
    }
}