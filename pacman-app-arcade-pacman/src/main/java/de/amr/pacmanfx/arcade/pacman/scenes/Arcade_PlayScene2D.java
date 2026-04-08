/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman.model.Arcade_GameState;
import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.test.TestState;
import de.amr.pacmanfx.model.world.TerrainLayer;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.CheatActions;
import de.amr.pacmanfx.ui.action.CommonGameActions;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.d2.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.layout.GameUI_ContextMenu;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import javafx.scene.control.CheckMenuItem;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.pacmanfx.Globals.ARCADE_MAP_SIZE_IN_PIXELS;

/**
 * 2D play scene for Arcade game variants.
 */
public class Arcade_PlayScene2D extends GameScene2D {

    private LevelCompletedAnimation levelCompletedAnimation;

    public Arcade_PlayScene2D() {}

    @Override
    public void update(Game game) {
        game.optGameLevel().ifPresent(level -> {
            updateLivesCounter(game, level.pac());
            soundEffects().ifPresent(sfx -> {
                sfx.setEnabled(!level.isDemoLevel());
                sfx.playLevelRunningSound(level);
            });
        });
    }

    /**
     * Note: Scene is also used in Pac-Man XXL game variant where world map can have non-Arcade size!
     *
     * @return Unscaled scene size in pixels as (width, height)
     */
    @Override
    public Vector2i unscaledSize() {
        return gameContext().game().optGameLevel()
            .map(GameLevel::worldMap)
            .map(WorldMap::terrainLayer)
            .map(TerrainLayer::sizeInPixel)
            .orElse(ARCADE_MAP_SIZE_IN_PIXELS);
    }

    @Override
    public Optional<GameUI_ContextMenu> supplyContextMenu(Game game) {
        final var menu = new GameUI_ContextMenu(ui);
        menu.addLocalizedTitleItem("pacman");
        menu.addLocalizedCheckBox(game.cheating().usingAutopilotProperty(), "autopilot").setOnAction(e -> {
            final var checkBox = (CheckMenuItem) e.getSource();
            if (checkBox.isSelected()) {
                CheatActions.ACTION_ACTIVATE_AUTOPILOT.executeIfEnabled(ui);
            } else {
                CheatActions.ACTION_DEACTIVATE_AUTOPILOT.executeIfEnabled(ui);
            }
        });
        menu.addLocalizedCheckBox(game.cheating().immuneProperty(), "immunity").setOnAction(e -> {
            final var checkBox = (CheckMenuItem) e.getSource();
            if (checkBox.isSelected()) {
                CheatActions.ACTION_ACTIVATE_IMMUNITY.executeIfEnabled(ui);
            } else {
                CheatActions.ACTION_DEACTIVATE_IMMUNITY.executeIfEnabled(ui);
            }
        });
        menu.addSeparator();
        menu.addLocalizedCheckBox(GameUI.PROPERTY_MUTED, "muted");
        menu.addLocalizedActionItem(CommonGameActions.ACTION_QUIT_GAME_SCENE, "quit");
        return Optional.of(menu);
    }

    // Game event handlers

    @Override
    public void onBonusActivated(BonusActivatedEvent e) {
        // This is the sound in Ms. Pac-Man when the bonus wanders the maze. In Pac-Man, this is a no-op.
        soundEffects().ifPresent(GameSoundEffects::playBonusActiveSound);
    }

    @Override
    public void onBonusEaten(BonusEatenEvent e) {
        soundEffects().ifPresent(GameSoundEffects::playBonusEatenSound);
    }

    @Override
    public void onBonusExpired(BonusExpiredEvent e) {
        soundEffects().ifPresent(GameSoundEffects::playBonusExpiredSound);
    }

    @Override
    public void onCreditAdded(CreditAddedEvent e) {
        soundEffects().ifPresent(GameSoundEffects::playCoinInsertedSound);
    }

    @Override
    public void onGameContinues(GameContinuedEvent e) {
        e.game().optGameLevel().ifPresent(this::resetActorAnimations);
    }

    @Override
    public void onGameStarts(GameStartedEvent e) {
        final Game game = e.game();
        final boolean silent = game.isDemoLevelRunning() || game.control().state() instanceof TestState;
        if (!silent) {
            soundEffects().ifPresent(GameSoundEffects::playGameReadySound);
        }
    }

    @Override
    public void onGameStateChange(GameStateChangeEvent e) {
        final Game game = e.game();
        if (e.newState() == Arcade_GameState.LEVEL_COMPLETE) {
            final GameLevel level = game.optGameLevel().orElseThrow();
            soundEffects().ifPresent(GameSoundEffects::stopAll);
            createAndPlayLevelCompletedAnimation(level);
        }
        else if (e.newState() == Arcade_GameState.GAME_OVER) {
            soundEffects().ifPresent(GameSoundEffects::playGameOverSound);
            game.hud().credit(true);
        }
    }

    @Override
    public void onGhostEaten(GhostEatenEvent e) {
        soundEffects().ifPresent(GameSoundEffects::playGhostEatenSound);
    }

    @Override
    public void onLevelCreated(LevelCreatedEvent e) {
        acceptGameLevel(e.level());
    }

    @Override
    public void onPacDead(PacDeadEvent e) {
        // Trigger end of game state PACMAN_DYING after dying animation has finished
        e.game().control().state().expire();
    }

    @Override
    public void onPacDying(PacDyingEvent e) {
        soundEffects().ifPresent(GameSoundEffects::playPacDeadSound);
    }

    @Override
    public void onPacEatsFood(PacEatsFoodEvent e) {
        final long tick = gameContext().clock().tickCount();
        soundEffects().ifPresent(sfx -> sfx.playPacMunchingSound(tick));
    }

    @Override
    public void onPacGetsPower(PacGetsPowerEvent e) {
        soundEffects().ifPresent(GameSoundEffects::playPacPowerSound);
    }

    @Override
    public void onPacLostPower(PacLostPowerEvent e) {
        soundEffects().ifPresent(GameSoundEffects::stopPacPowerSound);
    }

    @Override
    public void onSpecialScoreReached(SpecialScoreReachedEvent e) {
        soundEffects().ifPresent(GameSoundEffects::playExtraLifeSound);
    }

    // others

    // Expose the animation state to the scene renderer
    public Optional<LevelCompletedAnimation.FlashingState> optFlashingState() {
        return Optional.ofNullable(levelCompletedAnimation).flatMap(LevelCompletedAnimation::flashingState);
    }

    /**
     * If the 3D play scene is shown when the game level gets created, the onLevelCreated() method of this
     * scene is not called, so we have to accept the game level again when switching from the 3D scene to this one.
     *
     * @param level game level
     */
    public void acceptGameLevel(GameLevel level) {
        ui.soundManager().setEnabled(!level.isDemoLevel()); //TODO is this needed?
        actionBindings.registerAll(ArcadePacMan_UIConfig.GAME_START_ACTION_BINDINGS);
        actionBindings.registerAll(GameUI.STEERING_ACTION_BINDINGS);
        actionBindings.registerAll(GameUI.CHEAT_ACTION_BINDINGS);
        actionBindings.addAll(GameUI.KEYBOARD);
        Logger.info("Scene {} accepted game level #{}", getClass().getSimpleName(), level.number());
    }

    // Private

    // While Pac-Man is not yet visible on level start, one symbol more is shown in the lives counter
    private void updateLivesCounter(Game game, Pac pac) {
        final int more = game.control().state() == Arcade_GameState.STARTING_GAME_OR_LEVEL
            && !pac.isVisible() ? 1 : 0;
        final int count = Math.clamp(game.lifeCount() - 1 + more, 0, game.hud().maxLivesDisplayed());
        game.hud().setVisibleLifeCount(count);
    }

    private void createAndPlayLevelCompletedAnimation(GameLevel level) {
        levelCompletedAnimation = new LevelCompletedAnimation(level, () -> level.game().control().state().expire());
        levelCompletedAnimation.play();
    }

    private void resetActorAnimations(GameLevel level) {
        level.pac().animations().selectAnimation(Pac.AnimationID.PAC_MUNCHING);
        level.pac().resetAnimation();
        level.ghosts().forEach(ghost -> {
            ghost.animations().selectAnimation(Ghost.AnimationID.GHOST_NORMAL);
            ghost.resetAnimation();
        });
    }
}