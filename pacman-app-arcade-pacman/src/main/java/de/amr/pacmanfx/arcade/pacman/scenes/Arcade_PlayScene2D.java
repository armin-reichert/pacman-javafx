/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman.model.Arcade_GameController.GameState;
import de.amr.pacmanfx.arcade.pacman.rendering.Arcade_PlayScene2D_Renderer;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.event.GameStateChangeEvent;
import de.amr.pacmanfx.lib.fsm.StateMachine.State;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.HUD;
import de.amr.pacmanfx.model.MessageType;
import de.amr.pacmanfx.model.actors.CommonAnimationID;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.test.TestState;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.HUD_Renderer;
import de.amr.pacmanfx.ui._2d.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.api.GameScene;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.sound.SoundID;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.ContextMenuEvent;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.pacmanfx.Globals.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.ui.action.CommonGameActions.ACTION_QUIT_GAME_SCENE;
import static de.amr.pacmanfx.ui.api.GameUI.PROPERTY_MUTED;
import static de.amr.pacmanfx.uilib.Ufx.createContextMenuTitle;

/**
 * 2D play scene for Arcade game variants.
 *
 * <p>
 * TODO: Currently, scene instances are permanently stored in the UI configuration and no garbage collection occurs!
 * </p>
 */
public class Arcade_PlayScene2D extends GameScene2D {

    private Arcade_PlayScene2D_Renderer renderer;
    private HUD_Renderer hudRenderer;
    private LevelCompletedAnimation levelCompletedAnimation;

    public Arcade_PlayScene2D(GameUI ui) {
        super(ui);
    }

    public LevelCompletedAnimation levelCompletedAnimation() {
        return levelCompletedAnimation;
    }

    @Override
    protected void createRenderers(Canvas canvas) {
        final GameUI_Config uiConfig = ui.currentConfig();
        hudRenderer = adaptRenderer(uiConfig.createHUDRenderer(canvas));
        renderer = adaptRenderer(new Arcade_PlayScene2D_Renderer(this, canvas, uiConfig.spriteSheet()));
    }

    @Override
    public HUD_Renderer hudRenderer() {
        return hudRenderer;
    }

    @Override
    public Arcade_PlayScene2D_Renderer sceneRenderer() {
        return renderer;
    }

    @Override
    protected void doInit(Game game) {
        game.hud().creditVisible(false).scoreVisible(true).levelCounterVisible(true).livesCounterVisible(true);
    }

    @Override
    protected void doEnd(Game game) {
        if (levelCompletedAnimation != null) {
            levelCompletedAnimation.dispose();
            levelCompletedAnimation = null;
        }
    }

    @Override
    public void update(Game game) {
        game.optGameLevel().ifPresent(level -> {
            updateHUD(level);
            updateHuntingSound(level);
        });
    }

    @Override
    public Vector2i unscaledSize() {
        // Note: scene is also used in Pac-Man XXL game variant where world can have any size!
        return context().currentGame().optGameLevel().map(level -> level.worldMap().terrainLayer().sizeInPixel())
            .orElse(ARCADE_MAP_SIZE_IN_PIXELS);
    }

    @Override
    public List<MenuItem> supplyContextMenuItems(ContextMenuEvent contextMenuEvent, ContextMenu contextMenu) {
        final Game game = context().currentGame();

        var miAutopilot = new CheckMenuItem(ui.globalAssets().translated("autopilot"));
        miAutopilot.selectedProperty().bindBidirectional(game.usingAutopilotProperty());
        miAutopilot.setOnAction(e -> {
            final boolean usingAutopilot = miAutopilot.isSelected();
            if (usingAutopilot && game.optGameLevel().isPresent() && !game.level().isDemoLevel()) {
                game.cheatUsedProperty().set(true);
            }
            ui.soundManager().playVoiceAfterSec(0, usingAutopilot ? SoundID.VOICE_AUTOPILOT_ON : SoundID.VOICE_AUTOPILOT_OFF);
            ui.showFlashMessage(ui.globalAssets().translated(usingAutopilot ? "autopilot_on" : "autopilot_off"));

        });

        var miImmunity = new CheckMenuItem(ui.globalAssets().translated("immunity"));
        miImmunity.selectedProperty().bindBidirectional(game.immuneProperty());
        miImmunity.setOnAction(e -> {
            final boolean immune = miImmunity.isSelected();
            if (immune && game.optGameLevel().isPresent() && !game.level().isDemoLevel()) {
                game.cheatUsedProperty().set(true);
            }
            ui.soundManager().playVoiceAfterSec(0, immune ? SoundID.VOICE_IMMUNITY_ON : SoundID.VOICE_IMMUNITY_OFF);
            ui.showFlashMessage(ui.globalAssets().translated(immune ? "player_immunity_on" : "player_immunity_off"));
        });

        var miMuted = new CheckMenuItem(ui.globalAssets().translated("muted"));
        miMuted.selectedProperty().bindBidirectional(PROPERTY_MUTED);

        var miQuit = new MenuItem(ui.globalAssets().translated("quit"));
        miQuit.setOnAction(e -> ACTION_QUIT_GAME_SCENE.executeIfEnabled(ui));

        return List.of(
            createContextMenuTitle(ui.preferences(), ui.globalAssets().translated("pacman")),
            miAutopilot,
            miImmunity,
            new SeparatorMenuItem(),
            miMuted,
            miQuit);
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
        ui.soundManager().loop(SoundID.ACTIVE);
    }

    @Override
    public void onBonusEaten(GameEvent e) {
        ui.soundManager().stop(SoundID.ACTIVE);
        ui.soundManager().play(SoundID.BONUS_EATEN);
    }

    @Override
    public void onBonusExpires(GameEvent e) {
        ui.soundManager().stop(SoundID.ACTIVE);
    }

    @Override
    public void onCreditAdded(GameEvent e) {
        ui.soundManager().play(SoundID.COIN_INSERTED);
    }

    @Override
    public void onGameContinues(GameEvent e) {
        final Game game = context().currentGame();
        game.optGameLevel().ifPresent(level -> {
            resetAnimations(level);
            game.showLevelMessage(MessageType.READY);
        });
    }

    @Override
    public void onGameStarts(GameEvent e) {
        final Game game = context().currentGame();
        final boolean silent = game.optGameLevel().isPresent() && game.level().isDemoLevel()
            || game.control().state() instanceof TestState;
        if (!silent) {
            ui.soundManager().play(SoundID.GAME_READY);
        }
    }

    @Override
    public void onGameStateChange(GameStateChangeEvent e) {
        if (e.newState() == GameState.LEVEL_COMPLETE) {
            ui.soundManager().stopAll();
            playLevelCompletedAnimation(context().currentGame().level());
        }
        else if (e.newState() == GameState.GAME_OVER) {
            ui.soundManager().stopAll();
            ui.soundManager().play(SoundID.GAME_OVER);
        }
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        ui.soundManager().play(SoundID.GHOST_EATEN);
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        context().currentGame().optGameLevel().ifPresent(this::acceptGameLevel);
    }

    @Override
    public void onPacDead(GameEvent e) {
        // Trigger end of game state PACMAN_DYING after dying animation has finished
        context().currentGame().control().terminateCurrentGameState();
    }

    @Override
    public void onPacDying(GameEvent e) {
        ui.soundManager().stopSiren();
        ui.soundManager().play(SoundID.PAC_MAN_DEATH);
    }

    private long lastMunchingSoundPlayedTick;

    @Override
    public void onPacFindsFood(GameEvent e) {
        final long now = ui.clock().tickCount();
        final long passed = now - lastMunchingSoundPlayedTick;
        Logger.debug("Pac found food, tick={} passed since last time={}", now, passed);
        byte minDelay = ui.currentConfig().munchingSoundDelay();
        if (passed > minDelay  || minDelay == 0) {
            ui.soundManager().play(SoundID.PAC_MAN_MUNCHING);
            lastMunchingSoundPlayedTick = now;
        }
    }

    @Override
    public void onPacPowerBegins(GameEvent e) {
        ui.soundManager().stopSiren();
        ui.soundManager().loop(SoundID.PAC_MAN_POWER);
    }

    @Override
    public void onPacPowerEnds(GameEvent e) {
        ui.soundManager().stop(SoundID.PAC_MAN_POWER);
    }

    @Override
    public void onSpecialScoreReached(GameEvent e) {
        ui.soundManager().play(SoundID.EXTRA_LIFE);
    }

    // private

    /**
     * If the 3D play scene is shown when the game level gets created, the onLevelCreated() method of this
     *  scene is not called, so we have to accept the game level again when switching from the 3D scene to this one.
     */
    private void acceptGameLevel(GameLevel level) {
        final Game game = context().currentGame();
        if (level.isDemoLevel()) {
            game.hud().creditVisible(false).levelCounterVisible(true).livesCounterVisible(false);
            actionBindings.useAll(ArcadePacMan_UIConfig.DEFAULT_BINDINGS);
            actionBindings.attach(GameUI.KEYBOARD);
            ui.soundManager().setEnabled(false);
        } else {
            game.hud().creditVisible(false).levelCounterVisible(true).livesCounterVisible(true);
            actionBindings.useAll(GameUI.STEERING_BINDINGS);
            actionBindings.useAll(GameUI.CHEAT_BINDINGS);
            actionBindings.attach(GameUI.KEYBOARD);
            ui.soundManager().setEnabled(true);
        }
        Logger.info("Scene {} accepted game level", getClass().getSimpleName());
    }

    private void updateHUD(GameLevel level) {
        final Game game = context().currentGame();
        final HUD hud = game.hud();
        // While Pac-Man is still invisible on level start, one entry more is shown in the lives counter
        // TODO: This is still not 100% correct
        final boolean oneExtra = game.control().state() == GameState.STARTING_GAME_OR_LEVEL && !level.pac().isVisible();
        final int livesDisplayed = oneExtra ? game.lifeCount() : game.lifeCount() - 1;
        hud.setVisibleLifeCount(Math.min(livesDisplayed, hud.maxLivesDisplayed()));
        hud.showCredit(context().coinMechanism().isEmpty());
    }

    private void updateHuntingSound(GameLevel level) {
        if (!ui.soundManager().isEnabled())
            return;

        final State<Game> gameState = level.game().control().state();
        if (gameState == GameState.HUNTING) {
            final Pac pac = level.pac();
            if (!pac.powerTimer().isRunning()) {
                selectAndPlaySiren(level);
            }
            final boolean ghostReturns = pac.isAlive()
                    && level.ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).findAny().isPresent();
            if (ghostReturns) {
                if (!ui.soundManager().isPlaying(SoundID.GHOST_RETURNS)) {
                    ui.soundManager().loop(SoundID.GHOST_RETURNS);
                }
            } else {
                ui.soundManager().stop(SoundID.GHOST_RETURNS);
            }
        }
    }

    // Siren numbers are 1, 2, 3, 4, hunting phase index = 0..7
    //TODO move this logic into game model as it depends on the currently played game variant
    private int selectSirenNumber(int huntingPhase) {
        return 1 + huntingPhase / 2;
    }

    private void selectAndPlaySiren(GameLevel level) {
        //TODO fix volume in audio file
        final float volume = 0.33f;
        final int sirenNumber = selectSirenNumber(level.huntingTimer().phaseIndex());
        final SoundID sirenID = switch (sirenNumber) {
            case 1 -> SoundID.SIREN_1;
            case 2 -> SoundID.SIREN_2;
            case 3 -> SoundID.SIREN_3;
            case 4 -> SoundID.SIREN_4;
            default -> throw new IllegalArgumentException("Illegal siren number " + sirenNumber);
        };
        ui.soundManager().playSiren(sirenID, volume);
    }

    private void playLevelCompletedAnimation(GameLevel level) {
        levelCompletedAnimation = new LevelCompletedAnimation(animationRegistry, level);
        levelCompletedAnimation.getOrCreateAnimationFX().setOnFinished(
            e -> level.game().control().terminateCurrentGameState());
        levelCompletedAnimation.playFromStart();
    }

    private void resetAnimations(GameLevel level) {
        level.pac().optAnimationManager().ifPresent(animationManager -> {
            animationManager.select(CommonAnimationID.ANIM_PAC_MUNCHING);
            animationManager.reset();
        });
        level.ghosts().forEach(ghost -> ghost.optAnimationManager().ifPresent(animationManager -> {
            animationManager.select(CommonAnimationID.ANIM_GHOST_NORMAL);
            animationManager.reset();
        }));
    }
}