/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.GameClock;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman.model.Arcade_GameModel;
import de.amr.pacmanfx.arcade.pacman.model.Arcade_GameState;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUIConstants;
import de.amr.pacmanfx.ui.action.CheatActions;
import de.amr.pacmanfx.ui.action.CommonActions;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.d2.LevelCompletedAnimation;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.pacmanfx.ui.layout.ContextMenuSupport.*;

/**
 * 2D play scene for Arcade game variants.
 */
public class Arcade_PlayScene2D extends GameScene2D {

    private LevelCompletedAnimation levelCompletedAnimation;

    public Arcade_PlayScene2D(GameUI ui) {
        super(ui);
        setGameEventHandler(new Arcade_PlayScene2DGameEventHandler(this));
    }

    public LevelCompletedAnimation levelCompletedAnimation() {
        return levelCompletedAnimation;
    }

    @Override
    public void onTick(GameClock clock) {
        final Arcade_GameModel game = gameContext().game();
        game.optGameLevel().ifPresent(level -> {
            updateLivesCounter(level);
            soundEffects().ifPresent(sfx -> {
                sfx.setEnabled(!level.isDemoLevel());
                sfx.playLevelRunningSound(level);
            });
        });
    }

    @Override
    public Optional<ContextMenu> supplyContextMenu() {
        final Game game = gameContext().game();
        final var menu = new ContextMenu();
        addLocalizedTitleItem(menu, ui.translator(), "pacman");
        addLocalizedCheckBox(menu, ui.translator(), game.cheats().usingAutopilotProperty(), "autopilot").setOnAction(e -> {
            final var checkBox = (CheckMenuItem) e.getSource();
            if (checkBox.isSelected()) {
                CheatActions.ACTION_ACTIVATE_AUTOPILOT.executeIfEnabled(ui);
            } else {
                CheatActions.ACTION_DEACTIVATE_AUTOPILOT.executeIfEnabled(ui);
            }
        });
        addLocalizedCheckBox(menu, ui.translator(), game.cheats().immuneProperty(), "immunity").setOnAction(e -> {
            final var checkBox = (CheckMenuItem) e.getSource();
            if (checkBox.isSelected()) {
                CheatActions.ACTION_ACTIVATE_IMMUNITY.executeIfEnabled(ui);
            } else {
                CheatActions.ACTION_DEACTIVATE_IMMUNITY.executeIfEnabled(ui);
            }
        });
        addSeparator(menu);
        addLocalizedCheckBox(menu, ui.translator(), GameUIConstants.PROPERTY_MUTED, "muted");
        addLocalizedActionItem(menu, ui, ui.translator(), CommonActions.ACTION_QUIT_GAME_SCENE, "quit");

        return Optional.of(menu);
    }

    @Override
    public void onEnteredFrom3DScene() {
        gameContext().game().optGameLevel().ifPresent(this::acceptGameLevel);
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
        actionBindings.addAll(ArcadePacMan_UIConfig.GAME_START_ACTION_BINDINGS);
        if (!level.isDemoLevel()) {
            actionBindings.addAll(GameUIConstants.STEERING_ACTION_BINDINGS);
            actionBindings.addAll(GameUIConstants.CHEAT_ACTION_BINDINGS);
        }
        actionBindings.register();

        ui.soundManager().setEnabled(!level.isDemoLevel()); //TODO is this needed?
        levelCompletedAnimation = new LevelCompletedAnimation(level, () -> gameContext().game().flow().state().expire());

        final Vector2i terrainSize = level.worldMap().terrainLayer().sizeInPixel();
        unscaledWidthProperty().set(terrainSize.x());
        unscaledHeightProperty().set(terrainSize.y());

        Logger.info("Game scene {} accepted game level #{}", getClass().getSimpleName(), level.number());
    }

    // Private

    // While Pac-Man is not yet visible on level start, one symbol more is shown in the lives counter
    private void updateLivesCounter(GameLevel level) {
        final Game game = level.game();
        final int additionalLives = level.game().flow().state() == Arcade_GameState.STARTING_GAME_OR_LEVEL
            && !level.pac().isVisible() ? 1 : 0;
        final int count = Math.clamp(game.lifeCount() - 1 + additionalLives, 0, game.hud().maxLivesDisplayed());
        game.hud().setVisibleLifeCount(count);
    }

    protected void resetActorAnimations(GameLevel level) {
        level.pac().animationManager().select(ArcadePacMan_AnimationID.PAC_MUNCHING);
        level.pac().animationManager().resetSelected();
        level.ghosts().forEach(ghost -> {
            ghost.animationManager().select(ArcadePacMan_AnimationID.GHOST_NORMAL);
            ghost.animationManager().resetSelected();
        });
    }
}