/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman.model.Arcade_GameController.GameState;
import de.amr.pacmanfx.event.*;
import de.amr.pacmanfx.lib.fsm.StateMachine.State;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.test.TestState;
import de.amr.pacmanfx.model.world.TerrainLayer;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_Resources;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.action.CommonGameActions;
import de.amr.pacmanfx.ui.layout.GameUI_ContextMenu;
import de.amr.pacmanfx.ui.sound.SoundID;
import javafx.scene.control.CheckMenuItem;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.pacmanfx.Globals.ARCADE_MAP_SIZE_IN_PIXELS;

/**
 * 2D play scene for Arcade game variants.
 */
public class Arcade_PlayScene2D extends GameScene2D {

    //TODO fix volume in audio file
    public static final float SIREN_VOLUME = 0.33f;

    private long lastMunchingSoundPlayedTick;
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
            updateHuntingSound(level);
        });
    }

    /**
     * Note: Scene is also used in Pac-Man XXL game variant where world map can have non-Arcade size!
     *
     * @return Unscaled scene size in pixels as (width, height)
     */
    @Override
    public Vector2i unscaledSize() {
        return gameContext().currentGame().optGameLevel().map(GameLevel::worldMap).map(WorldMap::terrainLayer)
            .map(TerrainLayer::sizeInPixel).orElse(ARCADE_MAP_SIZE_IN_PIXELS);
    }

    @Override
    public Optional<GameUI_ContextMenu> supplyContextMenu(Game game) {
        final var menu = new GameUI_ContextMenu(ui);
        menu.addLocalizedTitleItem("pacman");
        menu.addLocalizedCheckBox(game.usingAutopilotProperty(), "autopilot").setOnAction(e -> {
            final var checkBox = (CheckMenuItem) e.getSource();
            setAutopilot(game, checkBox.isSelected());
        });
        menu.addLocalizedCheckBox(game.immuneProperty(), "immunity").setOnAction(e -> {
            final var checkBox = (CheckMenuItem) e.getSource();
            setImmunity(game, checkBox.isSelected());
        });
        menu.addSeparator();
        menu.addLocalizedCheckBox(GameUI.PROPERTY_MUTED, "muted");
        menu.addLocalizedActionItem(CommonGameActions.ACTION_QUIT_GAME_SCENE, "quit");
        return Optional.of(menu);
    }

    @Override
    public void onSwitch_3D_2D(GameScene scene3D) {
        gameContext().currentGame().optGameLevel().ifPresent(this::acceptGameLevel);
        Logger.info("2D scene {} entered from 3D scene {}", getClass().getSimpleName(), scene3D.getClass().getSimpleName());
    }

    // Game event handlers

    @Override
    public void onBonusActivated(BonusActivatedEvent e) {
        // This is the sound in Ms. Pac-Man when the bonus wanders the maze. In Pac-Man, this is a no-op.
        ui.soundManager().loop(SoundID.BONUS_ACTIVE);
    }

    @Override
    public void onBonusEaten(BonusEatenEvent e) {
        ui.soundManager().stop(SoundID.BONUS_ACTIVE);
        ui.soundManager().play(SoundID.BONUS_EATEN);
    }

    @Override
    public void onBonusExpired(BonusExpiredEvent e) {
        ui.soundManager().stop(SoundID.BONUS_ACTIVE);
    }

    @Override
    public void onCreditAdded(CreditAddedEvent e) {
        ui.soundManager().play(SoundID.COIN_INSERTED);
    }

    @Override
    public void onGameContinues(GameContinuedEvent e) {
        e.game().optGameLevel().ifPresent(this::resetActorAnimations);
    }

    @Override
    public void onGameStarts(GameStartedEvent e) {
        final Game game = e.game();
        final boolean silent = game.optGameLevel().isPresent() && game.level().isDemoLevel()
            || game.control().state() instanceof TestState;
        if (!silent) {
            ui.soundManager().play(SoundID.GAME_READY);
        }
    }

    @Override
    public void onGameStateChange(GameStateChangeEvent e) {
        final Game game = gameContext().currentGame();
        if (e.newState() == GameState.LEVEL_COMPLETE) {
            ui.soundManager().stopAll();
            createAndPlayLevelCompletedAnimation(game.level());
        }
        else if (e.newState() == GameState.GAME_OVER) {
            ui.soundManager().stopAll();
            ui.soundManager().play(SoundID.GAME_OVER);
            game.hud().credit(true);
        }
    }

    @Override
    public void onGhostEaten(GhostEatenEvent e) {
        ui.soundManager().play(SoundID.GHOST_EATEN);
    }

    @Override
    public void onLevelCreated(LevelCreatedEvent e) {
        acceptGameLevel(e.level());
    }

    @Override
    public void onPacDead(PacDeadEvent e) {
        // Trigger end of game state PACMAN_DYING after dying animation has finished
        gameContext.currentGame().control().terminateGameState();
    }

    @Override
    public void onPacDying(PacDyingEvent e) {
        ui.soundManager().stopSiren();
        ui.soundManager().play(SoundID.PAC_MAN_DEATH);
    }

    @Override
    public void onPacEatsFood(PacEatsFoodEvent e) {
        final long tick = ui.clock().tickCount();
        final long passed = tick - lastMunchingSoundPlayedTick;
        final byte minDelay = ui.currentConfig().munchingSoundDelay();
        Logger.debug("Pac found food, tick={} passed since last time={}", tick, passed);
        if (passed > minDelay || minDelay == 0) {
            ui.soundManager().play(SoundID.PAC_MAN_MUNCHING);
            lastMunchingSoundPlayedTick = tick;
        }
    }

    @Override
    public void onPacGetsPower(PacGetsPowerEvent e) {
        ui.soundManager().stopSiren();
        ui.soundManager().loop(SoundID.PAC_MAN_POWER);
    }

    @Override
    public void onPacLostPower(PacLostPowerEvent e) {
        ui.soundManager().stop(SoundID.PAC_MAN_POWER);
    }

    @Override
    public void onSpecialScoreReached(SpecialScoreReachedEvent e) {
        ui.soundManager().play(SoundID.EXTRA_LIFE);
    }

    // private

    private void setAutopilot(Game game, boolean usingAutopilot) {
        if (usingAutopilot && game.optGameLevel().isPresent() && !game.level().isDemoLevel()) {
            game.raiseCheatFlag();
        }
        ui.voicePlayer().play(usingAutopilot ? GameUI_Resources.VOICE_AUTOPILOT_ON : GameUI_Resources.VOICE_AUTOPILOT_OFF);
        ui.showFlashMessage(ui.translate(usingAutopilot ? "autopilot_on" : "autopilot_off"));
    }

    private void setImmunity(Game game, boolean immune) {
        if (immune && game.optGameLevel().isPresent() && !game.level().isDemoLevel()) {
            game.raiseCheatFlag();
        }
        ui.voicePlayer().play(immune ? GameUI_Resources.VOICE_IMMUNITY_ON : GameUI_Resources.VOICE_IMMUNITY_OFF);
        ui.showFlashMessage(ui.translate(immune ? "player_immunity_on" : "player_immunity_off"));
    }

    /**
     * If the 3D play scene is shown when the game level gets created, the onLevelCreated() method of this
     * scene is not called, so we have to accept the game level again when switching from the 3D scene to this one.
     * @param level game level
     */
    private void acceptGameLevel(GameLevel level) {
        final Game game = level.game();
        final boolean demoLevel = level.isDemoLevel();
        if (demoLevel) {
            game.hud().credit(true).livesCounter(false).levelCounter(true).show();
            ui.soundManager().setEnabled(false);
            actionBindings.registerAllBindingsFrom(ArcadePacMan_UIConfig.DEFAULT_BINDINGS); // insert coin + start game
        } else {
            game.hud().credit(false).livesCounter(true).levelCounter(true).show();
            ui.soundManager().setEnabled(true);
            actionBindings.registerAllBindingsFrom(GameUI.STEERING_BINDINGS);
            actionBindings.registerAllBindingsFrom(GameUI.CHEAT_BINDINGS);
        }
        actionBindings.activateBindings(GameUI.KEYBOARD);
        Logger.info("Scene {} accepted game level #{}", getClass().getSimpleName(), level.number());
    }

    private void updateHUD(GameLevel level) {
        final Game game = level.game();
        // While Pac-Man is still invisible on level start, one Pac symbol more is shown in the lives counter
        final boolean oneExtra = game.control().state() == GameState.STARTING_GAME_OR_LEVEL && !level.pac().isVisible();
        final int lifeCountDisplayed = oneExtra ? game.lifeCount() : game.lifeCount() - 1;
        game.hud().setVisibleLifeCount(Math.clamp(lifeCountDisplayed, 0, game.hud().maxLivesDisplayed()));
    }

    private void updateHuntingSound(GameLevel level) {
        if (!ui.soundManager().isEnabled())
            return;

        final State<Game> gameState = level.game().control().state();
        if (gameState == GameState.HUNTING) {
            final Pac pac = level.pac();
            if (!pac.powerTimer().isRunning()) {
                final int huntingPhase = level.huntingTimer().phaseIndex();
                selectAndPlaySiren(huntingPhase);
            }
            final boolean ghostReturningToHouse = pac.isAlive()
                && level.ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).findAny().isPresent();
            if (ghostReturningToHouse) {
                if (!ui.soundManager().isPlaying(SoundID.GHOST_RETURNS)) {
                    ui.soundManager().loop(SoundID.GHOST_RETURNS);
                }
            } else {
                ui.soundManager().stop(SoundID.GHOST_RETURNS);
            }
        }
    }

    // Each (scatter, chasing) hunting phase pair uses another siren. Sirens are numbered 1, 2, 3, 4.
    // (0, 1) -> 1, (2, 3) -> 2, (4, 5) -> 3, (6, 7) -> 4
    private void selectAndPlaySiren(int huntingPhase) {
        final int sirenNumber = 1 + huntingPhase / 2;
        ui.soundManager().playSiren(sirenNumber, SIREN_VOLUME);
    }

    private void createAndPlayLevelCompletedAnimation(GameLevel level) {
        levelCompletedAnimation = new LevelCompletedAnimation(level, () -> level.game().control().terminateGameState());
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