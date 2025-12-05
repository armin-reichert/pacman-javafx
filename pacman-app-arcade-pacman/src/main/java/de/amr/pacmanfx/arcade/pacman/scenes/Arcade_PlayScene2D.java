/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.ArcadeActions;
import de.amr.pacmanfx.arcade.pacman.model.Arcade_GameController;
import de.amr.pacmanfx.arcade.pacman.rendering.Arcade_PlayScene2D_Renderer;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.MessageType;
import de.amr.pacmanfx.model.actors.CommonAnimationID;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.test.TestState;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.HUD_Renderer;
import de.amr.pacmanfx.ui._2d.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.action.CheatActions;
import de.amr.pacmanfx.ui.action.CommonGameActions;
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
import javafx.scene.input.KeyCode;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.pacmanfx.Globals.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.ui.action.CommonGameActions.ACTION_QUIT_GAME_SCENE;
import static de.amr.pacmanfx.ui.api.GameUI.PROPERTY_MUTED;
import static de.amr.pacmanfx.ui.input.Keyboard.bare;
import static de.amr.pacmanfx.uilib.Ufx.createContextMenuTitle;

/**
 * 2D play scene for Arcade game variants.
 * <p>
 * TODO: Currently the instance of this scene is permanently stored in the UI configuration and lives as long as the
 *       game, so no garbage collection occurs!
 */
public class Arcade_PlayScene2D extends GameScene2D {

    private Arcade_PlayScene2D_Renderer sceneRenderer;
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

        hudRenderer = configureRenderer(uiConfig.createHUDRenderer(canvas));

        sceneRenderer = configureRenderer(
            new Arcade_PlayScene2D_Renderer(this, canvas, uiConfig.spriteSheet()));
    }

    @Override
    public HUD_Renderer hudRenderer() {
        return hudRenderer;
    }

    @Override
    public Arcade_PlayScene2D_Renderer sceneRenderer() {
        return sceneRenderer;
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

    /*
      If the corresponding 3D scene is shown when the game level gets created, the onLevelCreated() method of this
      scene is not called, so we have to apply the game level to the scene again when switching from 3D to 2D.
     */
    private void acceptGameLevel(GameLevel level) {
        if (level.isDemoLevel()) {
            context().currentGame().hud().creditVisible(false).levelCounterVisible(true).livesCounterVisible(false);
            actionBindings.addKeyCombination(ArcadeActions.ACTION_INSERT_COIN, bare(KeyCode.DIGIT5));
            actionBindings.addKeyCombination(ArcadeActions.ACTION_INSERT_COIN, bare(KeyCode.NUMPAD5));
            actionBindings.addKeyCombination(ArcadeActions.ACTION_START_GAME,  bare(KeyCode.DIGIT1));
            actionBindings.addKeyCombination(ArcadeActions.ACTION_START_GAME,  bare(KeyCode.NUMPAD1));
            ui.soundManager().setEnabled(false);
        } else {
            context().currentGame().hud().creditVisible(false).levelCounterVisible(true).livesCounterVisible(true);
            actionBindings.bind(CommonGameActions.ACTION_STEER_UP,    GameUI.ACTION_BINDINGS);
            actionBindings.bind(CommonGameActions.ACTION_STEER_DOWN,  GameUI.ACTION_BINDINGS);
            actionBindings.bind(CommonGameActions.ACTION_STEER_LEFT,  GameUI.ACTION_BINDINGS);
            actionBindings.bind(CommonGameActions.ACTION_STEER_RIGHT, GameUI.ACTION_BINDINGS);
            actionBindings.bind(CheatActions.ACTION_ADD_LIVES,        GameUI.ACTION_BINDINGS);
            actionBindings.bind(CheatActions.ACTION_EAT_ALL_PELLETS,  GameUI.ACTION_BINDINGS);
            actionBindings.bind(CheatActions.ACTION_ENTER_NEXT_LEVEL, GameUI.ACTION_BINDINGS);
            actionBindings.bind(CheatActions.ACTION_KILL_GHOSTS,      GameUI.ACTION_BINDINGS);
            ui.soundManager().setEnabled(true);
        }
        actionBindings.assignBindingsToKeyboard(ui.keyboard());

        Logger.info("Scene {} initialized with game level", getClass().getSimpleName());
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        acceptGameLevel(context().currentGame().level());
    }

    @Override
    public void onSwitch_3D_2D(GameScene scene3D) {
        Logger.info("{} entered from {}", this, scene3D);
        if (context().currentGame().optGameLevel().isPresent()) {
            acceptGameLevel(context().currentGame().level());
        }
    }

    @Override
    public void onGameContinued(GameEvent e) {
        final Game game = context().currentGame();
        game.optGameLevel().ifPresent(level -> {
            resetAnimations(level);
            game.showLevelMessage(MessageType.READY);
        });
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

    @Override
    public void onGameStarted(GameEvent e) {
        StateMachine.State<Game> state = context().currentGame().control().state();
        boolean silent = context().currentGame().level().isDemoLevel() || state instanceof TestState;
        if (!silent) {
            ui.soundManager().play(SoundID.GAME_READY);
        }
    }

    @Override
    public void update(Game game) {
        game.optGameLevel().ifPresent(level -> {
            updateHUD(level);
            updateSound(level);
        });
    }

    private void updateHUD(GameLevel level) {
        final Game game = context().currentGame();
        // While Pac-Man is still invisible on level start, one entry more is shown in the lives counter
        boolean oneMore = game.control().state() == Arcade_GameController.GameState.STARTING_GAME_OR_LEVEL
            && !level.pac().isVisible();
        int numLivesDisplayed = game.lifeCount() - 1;
        if (oneMore) numLivesDisplayed += 1;
        game.hud().setVisibleLifeCount(Math.min(numLivesDisplayed, game.hud().maxLivesDisplayed()));
        //TODO this is wrong in level test state
        game.hud().showCredit(context().coinMechanism().isEmpty());
    }

    @Override
    public Vector2i unscaledSize() {
        // Note: scene is also used in Pac-Man XXL game variant where world can have any size
        return context().currentGame().optGameLevel().map(level -> level.worldMap().terrainLayer().sizeInPixel())
            .orElse(ARCADE_MAP_SIZE_IN_PIXELS);
    }

    @Override
    public List<MenuItem> supplyContextMenuItems(ContextMenuEvent contextMenuEvent, ContextMenu contextMenu) {
        final Game game = context().currentGame();

        var miAutopilot = new CheckMenuItem(ui.assets().translated("autopilot"));
        miAutopilot.selectedProperty().bindBidirectional(game.usingAutopilotProperty());

        var miImmunity = new CheckMenuItem(ui.assets().translated("immunity"));
        miImmunity.selectedProperty().bindBidirectional(game.immuneProperty());
        miImmunity.setOnAction(e -> {
            final boolean immune = miImmunity.isSelected();
            if (immune && game.optGameLevel().isPresent() && !game.level().isDemoLevel()) {
                game.cheatUsedProperty().set(true);
            }
            ui.soundManager().playVoiceAfterSec(0, immune ? SoundID.VOICE_IMMUNITY_ON : SoundID.VOICE_IMMUNITY_OFF);
            ui.showFlashMessage(ui.assets().translated(immune ? "player_immunity_on" : "player_immunity_off"));
        });

        var miMuted = new CheckMenuItem(ui.assets().translated("muted"));
        miMuted.selectedProperty().bindBidirectional(PROPERTY_MUTED);

        var miQuit = new MenuItem(ui.assets().translated("quit"));
        miQuit.setOnAction(e -> ACTION_QUIT_GAME_SCENE.executeIfEnabled(ui));

        return List.of(
            createContextMenuTitle(ui.preferences(), ui.assets().translated("pacman")),
            miAutopilot,
            miImmunity,
            new SeparatorMenuItem(),
            miMuted,
            miQuit);
    }

    @Override
    public void onEnterGameState(StateMachine.State<Game> state) {
        if (state == Arcade_GameController.GameState.LEVEL_COMPLETE) {
            ui.soundManager().stopAll();
            playLevelCompletedAnimation();
        }
        else if (state == Arcade_GameController.GameState.GAME_OVER) {
            ui.soundManager().stopAll();
            ui.soundManager().play(SoundID.GAME_OVER);
        }
    }

    private void playLevelCompletedAnimation() {
        levelCompletedAnimation = new LevelCompletedAnimation(animationRegistry, context().currentGame().level());
        levelCompletedAnimation.getOrCreateAnimationFX().setOnFinished(
            e -> context().currentGame().control().terminateCurrentGameState());
        levelCompletedAnimation.playFromStart();
    }

    @Override
    public void onBonusActivated(GameEvent e) {
        ui.soundManager().loop(SoundID.BONUS_ACTIVE); // no-op if that sound does not exist
    }

    @Override
    public void onBonusEaten(GameEvent e) {
        ui.soundManager().stop(SoundID.BONUS_ACTIVE);
        ui.soundManager().play(SoundID.BONUS_EATEN);
    }

    @Override
    public void onBonusExpired(GameEvent e) {
        ui.soundManager().stop(SoundID.BONUS_ACTIVE);
    }

    @Override
    public void onCreditAdded(GameEvent e) {
        ui.soundManager().play(SoundID.COIN_INSERTED);
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        ui.soundManager().play(SoundID.GHOST_EATEN);
    }

    @Override
    public void onPacDead(GameEvent e) {
        // triggers exit from state PACMAN_DYING after dying animation has finished
        context().currentGame().control().terminateCurrentGameState();
    }

    @Override
    public void onPacDying(GameEvent e) {
        ui.soundManager().pauseSiren();
        ui.soundManager().play(SoundID.PAC_MAN_DEATH);
    }

    @Override
    public void onPacFoundFood(GameEvent e) {
        int eatenFoodCount = context().currentGame().level().worldMap().foodLayer().eatenFoodCount();
        if (ui.currentConfig().munchingSoundPlayed(eatenFoodCount)) {
            ui.soundManager().play(SoundID.PAC_MAN_MUNCHING);
        }
    }

    @Override
    public void onPacPowerStarts(GameEvent e) {
        ui.soundManager().pauseSiren();
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

    private void updateSound(GameLevel level) {
        if (!ui.soundManager().isEnabled()) return;

        final Pac pac = level.pac();
        final boolean pacChased = context().currentGame().control().state() == Arcade_GameController.GameState.HUNTING
            && !pac.powerTimer().isRunning();

        if (pacChased) {
            selectAndPlaySiren();
        }

        final boolean ghostReturningHome = pac.isAlive()
            && level.ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).findAny().isPresent();
        if (ghostReturningHome) {
            ui.soundManager().loop(SoundID.GHOST_RETURNS);
        } else {
            ui.soundManager().stop(SoundID.GHOST_RETURNS);
        }
    }

    //TODO move this logic into game model as it depends on the currently played game variant
    private int selectSirenNumber(int huntingPhase) {
        // siren numbers are 1..4, hunting phase index = 0..7
        return 1 + huntingPhase / 2;
    }

    //TODO fix volume in audio file
    private void selectAndPlaySiren() {
        final float volume = 0.33f;
        final int sirenNumber = selectSirenNumber(context().currentGame().level().huntingTimer().phaseIndex());
        final SoundID sirenID = switch (sirenNumber) {
            case 1 -> SoundID.SIREN_1;
            case 2 -> SoundID.SIREN_2;
            case 3 -> SoundID.SIREN_3;
            case 4 -> SoundID.SIREN_4;
            default -> throw new IllegalArgumentException("Illegal siren number " + sirenNumber);
        };
        ui.soundManager().playSiren(sirenID, volume);
    }
}