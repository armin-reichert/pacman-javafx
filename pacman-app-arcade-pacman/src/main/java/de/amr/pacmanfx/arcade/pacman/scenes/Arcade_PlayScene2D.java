/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman.model.Arcade_GameController.GameState;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.event.GameStateChangeEvent;
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

    private LevelCompletedAnimation levelCompletedAnimation;
    private long lastMunchingSoundPlayedTick;

    public Arcade_PlayScene2D() {}

    public Optional<LevelCompletedAnimation> levelCompletedAnimation() {
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
     * @return Unscaled scene size in pixels (width, height)
     */
    @Override
    public Vector2i unscaledSize() {
        return context().currentGame().optGameLevel().map(GameLevel::worldMap).map(WorldMap::terrainLayer)
            .map(TerrainLayer::sizeInPixel).orElse(ARCADE_MAP_SIZE_IN_PIXELS);
    }

    @Override
    public Optional<GameUI_ContextMenu> supplyContextMenu(Game game) {
        final var menu = new GameUI_ContextMenu();
        menu.setUI(ui);
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
        context().currentGame().optGameLevel().ifPresent(this::acceptGameLevel);
        Logger.info("2D scene {} entered from 3D scene {}", getClass().getSimpleName(), scene3D.getClass().getSimpleName());
    }

    // Game event handlers

    @Override
    public void onBonusActivated(GameEvent e) {
        // This is the sound in Ms. Pac-Man when the bonus wanders the maze. In Pac-Man, this is a no-op.
        soundManager().loop(SoundID.BONUS_ACTIVE);
    }

    @Override
    public void onBonusEaten(GameEvent e) {
        soundManager().stop(SoundID.BONUS_ACTIVE);
        soundManager().play(SoundID.BONUS_EATEN);
    }

    @Override
    public void onBonusExpires(GameEvent e) {
        soundManager().stop(SoundID.BONUS_ACTIVE);
    }

    @Override
    public void onCreditAdded(GameEvent e) {
        soundManager().play(SoundID.COIN_INSERTED);
    }

    @Override
    public void onGameContinues(GameEvent e) {
        final Game game = e.game();
        game.optGameLevel().ifPresent(this::resetAnimations);
    }

    @Override
    public void onGameStarts(GameEvent e) {
        final Game game = e.game();
        final boolean silent = game.optGameLevel().isPresent() && game.level().isDemoLevel()
            || game.control().state() instanceof TestState;
        if (!silent) {
            soundManager().play(SoundID.GAME_READY);
        }
    }

    @Override
    public void onGameStateChange(GameStateChangeEvent e) {
        if (e.newState() == GameState.LEVEL_COMPLETE) {
            soundManager().stopAll();
            playLevelCompletedAnimation(e.game(), e.game().level());
        }
        else if (e.newState() == GameState.GAME_OVER) {
            soundManager().stopAll();
            soundManager().play(SoundID.GAME_OVER);
            e.game().hud().credit(true);
        }
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        soundManager().play(SoundID.GHOST_EATEN);
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        e.game().optGameLevel().ifPresent(this::acceptGameLevel);
    }

    @Override
    public void onPacDead(GameEvent e) {
        // Trigger end of game state PACMAN_DYING after dying animation has finished
        e.game().control().terminateCurrentGameState();
    }

    @Override
    public void onPacDying(GameEvent e) {
        soundManager().stopSiren();
        soundManager().play(SoundID.PAC_MAN_DEATH);
    }

    @Override
    public void onPacFindsFood(GameEvent e) {
        final long now = ui.clock().tickCount();
        final long passed = now - lastMunchingSoundPlayedTick;
        final byte minDelay = ui.currentConfig().munchingSoundDelay();
        Logger.debug("Pac found food, tick={} passed since last time={}", now, passed);
        if (passed > minDelay || minDelay == 0) {
            soundManager().play(SoundID.PAC_MAN_MUNCHING);
            lastMunchingSoundPlayedTick = now;
        }
    }

    @Override
    public void onPacPowerBegins(GameEvent e) {
        soundManager().stopSiren();
        soundManager().loop(SoundID.PAC_MAN_POWER);
    }

    @Override
    public void onPacPowerEnds(GameEvent e) {
        soundManager().stop(SoundID.PAC_MAN_POWER);
    }

    @Override
    public void onSpecialScoreReached(GameEvent e) {
        soundManager().play(SoundID.EXTRA_LIFE);
    }

    // private

    private void setAutopilot(Game game, boolean usingAutopilot) {
        if (usingAutopilot && game.optGameLevel().isPresent() && !game.level().isDemoLevel()) {
            game.cheatUsedProperty().set(true);
        }
        ui.voicePlayer().play(usingAutopilot ? GameUI.VOICE_AUTOPILOT_ON : GameUI.VOICE_AUTOPILOT_OFF);
        ui.showFlashMessage(ui.translate(usingAutopilot ? "autopilot_on" : "autopilot_off"));
    }

    private void setImmunity(Game game, boolean immune) {
        if (immune && game.optGameLevel().isPresent() && !game.level().isDemoLevel()) {
            game.cheatUsedProperty().set(true);
        }
        ui.voicePlayer().play(immune ? GameUI.VOICE_IMMUNITY_ON : GameUI.VOICE_IMMUNITY_OFF);
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
        game.hud().credit(false).levelCounter(true).show();
        if (demoLevel) {
            game.hud().credit(true).livesCounter(false);
            soundManager().setEnabled(false);
            actionBindings.useAllBindings(ArcadePacMan_UIConfig.DEFAULT_BINDINGS); // insert coin + start game
        } else {
            game.hud().credit(false).livesCounter(true);
            soundManager().setEnabled(true);
            actionBindings.useAllBindings(GameUI.STEERING_BINDINGS);
            actionBindings.useAllBindings(GameUI.CHEAT_BINDINGS);
        }
        actionBindings.activateBindings(GameUI.KEYBOARD);
        Logger.info("Scene {} accepted game level #{}", getClass().getSimpleName(), level.number());
    }

    private void updateHUD(GameLevel level) {
        final Game game = level.game();
        // While Pac-Man is still invisible on level start, one entry more is shown in the lives counter
        final boolean oneExtra = game.control().state() == GameState.STARTING_GAME_OR_LEVEL && !level.pac().isVisible();
        final int livesDisplayed = oneExtra ? game.lifeCount() : game.lifeCount() - 1;
        game.hud().setVisibleLifeCount(Math.clamp(livesDisplayed, 0, game.hud().maxLivesDisplayed()));
    }

    private void updateHuntingSound(GameLevel level) {
        if (!soundManager().isEnabled())
            return;

        final State<Game> gameState = level.game().control().state();
        if (gameState == GameState.HUNTING) {
            final Pac pac = level.pac();
            if (!pac.powerTimer().isRunning()) {
                selectAndPlaySiren(level);
            }
            final boolean ghostReturningToHouse = pac.isAlive()
                && level.ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).findAny().isPresent();
            if (ghostReturningToHouse) {
                if (!soundManager().isPlaying(SoundID.GHOST_RETURNS)) {
                    soundManager().loop(SoundID.GHOST_RETURNS);
                }
            } else {
                soundManager().stop(SoundID.GHOST_RETURNS);
            }
        }
    }

    // Siren numbers are 1, 2, 3, 4, hunting phase index = 0..7
    // TODO: move this logic into game model as it depends on the played game variant
    private void selectAndPlaySiren(GameLevel level) {
        final int sirenNumber = 1 + level.huntingTimer().phaseIndex() / 2;
        soundManager().playSiren(sirenNumber, SIREN_VOLUME);
    }

    private void playLevelCompletedAnimation(Game game, GameLevel level) {
        levelCompletedAnimation = new LevelCompletedAnimation(() -> game.control().terminateCurrentGameState());
        levelCompletedAnimation.play(level);
    }

    private void resetAnimations(GameLevel level) {
        level.pac().optAnimationManager().ifPresent(animationManager -> {
            animationManager.select(Pac.AnimationID.PAC_MUNCHING);
            animationManager.reset();
        });
        level.ghosts().forEach(ghost -> ghost.optAnimationManager().ifPresent(animationManager -> {
            animationManager.select(Ghost.AnimationID.GHOST_NORMAL);
            animationManager.reset();
        }));
    }
}