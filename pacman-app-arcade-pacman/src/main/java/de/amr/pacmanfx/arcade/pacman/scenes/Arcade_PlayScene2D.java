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

    public Optional<LevelCompletedAnimation> optLevelCompletedAnimation() {
        return Optional.ofNullable(levelCompletedAnimation);
    }

    @Override
    protected void doInit(Game game) {
        game.hud().credit(false).score(true).levelCounter(true).livesCounter(true).show();
    }

    @Override
    protected void doEnd(Game game) {}

    @Override
    public void update(Game game) {
        game.optGameLevel().ifPresent(level -> {
            updateHUD(level);
            ui.currentConfig().soundEffects().ifPresent(soundEffects -> {
                soundEffects.setEnabled(!level.isDemoLevel());
                soundEffects.playLevelPlayingSound(level);
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
        return gameContext().game().optGameLevel().map(GameLevel::worldMap).map(WorldMap::terrainLayer)
            .map(TerrainLayer::sizeInPixel).orElse(ARCADE_MAP_SIZE_IN_PIXELS);
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
        ui.currentConfig().soundEffects().ifPresent(GameSoundEffects::playBonusActiveSound);
    }

    @Override
    public void onBonusEaten(BonusEatenEvent e) {
        ui.currentConfig().soundEffects().ifPresent(GameSoundEffects::playBonusEatenSound);
    }

    @Override
    public void onBonusExpired(BonusExpiredEvent e) {
        ui.currentConfig().soundEffects().ifPresent(GameSoundEffects::playBonusExpiredSound);
    }

    @Override
    public void onCreditAdded(CreditAddedEvent e) {
        ui.currentConfig().soundEffects().ifPresent(GameSoundEffects::playCoinInsertedSound);
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
            ui.currentConfig().soundEffects().ifPresent(GameSoundEffects::playGameReadySound);
        }
    }

    @Override
    public void onGameStateChange(GameStateChangeEvent e) {
        final Game game = gameContext().game();
        if (e.newState() == Arcade_GameState.LEVEL_COMPLETE) {
            final GameLevel level = game.optGameLevel().orElseThrow();
            ui.currentConfig().soundEffects().ifPresent(GameSoundEffects::stopAll);
            createAndPlayLevelCompletedAnimation(level);
        }
        else if (e.newState() == Arcade_GameState.GAME_OVER) {
            ui.currentConfig().soundEffects().ifPresent(GameSoundEffects::playGameOverSound);
            game.hud().credit(true);
        }
    }

    @Override
    public void onGhostEaten(GhostEatenEvent e) {
        ui.currentConfig().soundEffects().ifPresent(GameSoundEffects::playGhostEatenSound);
    }

    @Override
    public void onLevelCreated(LevelCreatedEvent e) {
        acceptGameLevel(e.level());
    }

    @Override
    public void onPacDead(PacDeadEvent e) {
        // Trigger end of game state PACMAN_DYING after dying animation has finished
        gameContext().game().control().state().expire();
    }

    @Override
    public void onPacDying(PacDyingEvent e) {
        ui.currentConfig().soundEffects().ifPresent(GameSoundEffects::playPacDeadSound);
    }

    @Override
    public void onPacEatsFood(PacEatsFoodEvent e) {
        final long tick = gameContext().clock().tickCount();
        ui.currentConfig().soundEffects().ifPresent(sfx -> sfx.playPacMunchingSound(tick));
    }

    @Override
    public void onPacGetsPower(PacGetsPowerEvent e) {
        ui.currentConfig().soundEffects().ifPresent(GameSoundEffects::playPacPowerSound);
    }

    @Override
    public void onPacLostPower(PacLostPowerEvent e) {
        ui.currentConfig().soundEffects().ifPresent(GameSoundEffects::stopPacPowerSound);
    }

    @Override
    public void onSpecialScoreReached(SpecialScoreReachedEvent e) {
        ui.currentConfig().soundEffects().ifPresent(GameSoundEffects::playExtraLifeSound);
    }

    // private

    /**
     * If the 3D play scene is shown when the game level gets created, the onLevelCreated() method of this
     * scene is not called, so we have to accept the game level again when switching from the 3D scene to this one.
     * @param level game level
     */
    public void acceptGameLevel(GameLevel level) {
        final Game game = level.game();
        final boolean demoLevel = level.isDemoLevel();
        if (demoLevel) {
            game.hud().credit(true).livesCounter(false).levelCounter(true).show();
            ui.soundManager().setEnabled(false);
            actionBindings.registerAllFrom(ArcadePacMan_UIConfig.GAME_START_BINDINGS); // insert coin + start game
        } else {
            game.hud().credit(false).livesCounter(true).levelCounter(true).show();
            ui.soundManager().setEnabled(true);
            actionBindings.registerAllFrom(GameUI.STEERING_BINDINGS);
            actionBindings.registerAllFrom(GameUI.CHEAT_BINDINGS);
        }
        actionBindings.addAll(GameUI.KEYBOARD);
        Logger.info("Scene {} accepted game level #{}", getClass().getSimpleName(), level.number());
    }

    private void updateHUD(GameLevel level) {
        final Game game = level.game();
        // While Pac-Man is still invisible on level start, one Pac symbol more is shown in the lives counter
        final boolean oneExtra = game.control().state() == Arcade_GameState.STARTING_GAME_OR_LEVEL && !level.pac().isVisible();
        final int lifeCountDisplayed = oneExtra ? game.lifeCount() : game.lifeCount() - 1;
        game.hud().setVisibleLifeCount(Math.clamp(lifeCountDisplayed, 0, game.hud().maxLivesDisplayed()));
    }

    private void createAndPlayLevelCompletedAnimation(GameLevel level) {
        levelCompletedAnimation = new LevelCompletedAnimation(level, () -> level.game().control().state().expire());
        levelCompletedAnimation.play();
    }

    private void resetActorAnimations(GameLevel level) {
        level.pac().optAnimationManager().ifPresent(pacAnimations -> {
            pacAnimations.select(Pac.AnimationID.PAC_MUNCHING);
            pacAnimations.reset();
        });
        level.ghosts().forEach(ghost -> ghost.optAnimationManager().ifPresent(ghostAnimations -> {
            ghostAnimations.select(Ghost.AnimationID.GHOST_NORMAL);
            ghostAnimations.reset();
        }));
    }
}