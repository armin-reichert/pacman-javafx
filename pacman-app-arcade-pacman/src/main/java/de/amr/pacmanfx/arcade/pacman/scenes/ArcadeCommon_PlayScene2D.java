/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.House;
import de.amr.pacmanfx.model.HuntingTimer;
import de.amr.pacmanfx.model.LivesCounter;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.MovingActor;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.sound.SoundID;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.List;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.controller.GameState.TESTING_LEVELS_MEDIUM;
import static de.amr.pacmanfx.controller.GameState.TESTING_LEVELS_SHORT;
import static de.amr.pacmanfx.ui.GameUI.GLOBAL_ACTION_BINDINGS;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.*;
import static de.amr.pacmanfx.ui._2d.ArcadePalette.*;

/**
 * 2D play scene for Arcade game variants.
 * <p>
 * TODO: Currently the instance of this scene is permanently stored in the UI configuration and lives as long as the
 *       game, so no garbage collection occurs!
 */
public class ArcadeCommon_PlayScene2D extends GameScene2D {

    private LevelCompletedAnimation levelCompletedAnimation;

    public ArcadeCommon_PlayScene2D(GameUI ui) {
        super(ui);
    }

    @Override
    protected void doInit() {
        gameContext().theGame().theHUD().credit(false).score(true).levelCounter(true).livesCounter(true);
        levelCompletedAnimation = new LevelCompletedAnimation(animationManager);
        gameRenderer = ui.theConfiguration().createGameRenderer(canvas);
    }

    @Override
    protected void doEnd() {
        if (levelCompletedAnimation != null) {
            levelCompletedAnimation.dispose();
            levelCompletedAnimation = null;
        }
    }

    /*
      Note: If the corresponding 3D scene is displayed when the game level gets created,
      the onLevelCreated() handler of this scene is not called!
      So we have to initialize the scene also with the game level when switching from the 3D scene.
     */
    private void initWithGameLevel(GameLevel gameLevel) {
        if (gameLevel.isDemoLevel()) {
            gameContext().theGame().theHUD().credit(false).levelCounter(true).livesCounter(false);
            actionBindings.bind(ACTION_ARCADE_INSERT_COIN, GLOBAL_ACTION_BINDINGS);
            actionBindings.updateKeyboard();
        } else {
            gameContext().theGame().theHUD().credit(false).levelCounter(true).livesCounter(true);
            actionBindings.bind(ACTION_STEER_UP, GLOBAL_ACTION_BINDINGS);
            actionBindings.bind(ACTION_STEER_DOWN, GLOBAL_ACTION_BINDINGS);
            actionBindings.bind(ACTION_STEER_LEFT, GLOBAL_ACTION_BINDINGS);
            actionBindings.bind(ACTION_STEER_RIGHT, GLOBAL_ACTION_BINDINGS);
            actionBindings.bind(ACTION_CHEAT_EAT_ALL_PELLETS, GLOBAL_ACTION_BINDINGS);
            actionBindings.bind(ACTION_CHEAT_ADD_LIVES, GLOBAL_ACTION_BINDINGS);
            actionBindings.bind(ACTION_CHEAT_ENTER_NEXT_LEVEL, GLOBAL_ACTION_BINDINGS);
            actionBindings.bind(ACTION_CHEAT_KILL_GHOSTS, GLOBAL_ACTION_BINDINGS);
            actionBindings.updateKeyboard();
        }
        if (gameRenderer == null) { //TODO can this happen at all?
            gameRenderer = ui.theConfiguration().createGameRenderer(canvas);
            Logger.warn("No game renderer existed for 2D play scene, created one...");
        }
        Logger.info("Scene {} initialized with game level", getClass().getSimpleName());
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        initWithGameLevel(gameContext().theGameLevel());
    }

    @Override
    public void onSwitch_3D_2D(GameScene scene3D) {
        Logger.info("2D scene {} entered from 3D scene {}", this, scene3D);
        if (gameContext().optGameLevel().isPresent()) {
            initWithGameLevel(gameContext().theGameLevel());
        }
    }

    @Override
    public List<MenuItem> supplyContextMenuItems(ContextMenuEvent contextMenuEvent, ContextMenu contextMenu) {
        var miAutopilot = new CheckMenuItem(ui.theAssets().text("autopilot"));
        miAutopilot.selectedProperty().bindBidirectional(gameContext().theGameController().propertyUsingAutopilot());

        var miImmunity = new CheckMenuItem(ui.theAssets().text("immunity"));
        miImmunity.selectedProperty().bindBidirectional(gameContext().theGameController().propertyImmunity());

        var miMuted = new CheckMenuItem(ui.theAssets().text("muted"));
        miMuted.selectedProperty().bindBidirectional(ui.propertyMuted());

        var miQuit = new MenuItem(ui.theAssets().text("quit"));
        miQuit.setOnAction(e -> ACTION_QUIT_GAME_SCENE.executeIfEnabled(ui));

        return List.of(
            ui.createContextMenuTitleItem(ui.theAssets().text("pacman")),
            miAutopilot,
            miImmunity,
            new SeparatorMenuItem(),
            miMuted,
            miQuit);
    }

    @Override
    public void onGameContinued(GameEvent e) {
        gameContext().theGameLevel().showMessage(GameLevel.MESSAGE_READY);
    }

    @Override
    public void onGameStarted(GameEvent e) {
        boolean silent = gameContext().theGameLevel().isDemoLevel()
                || gameContext().theGameState() == TESTING_LEVELS_SHORT
                || gameContext().theGameState() == TESTING_LEVELS_MEDIUM;
        if (!silent) {
            ui.theSound().play(SoundID.GAME_READY);
        }
    }

    @Override
    public void update() {
        if (gameContext().optGameLevel().isEmpty()) {
            // Scene is already updated 2 ticks before the game level gets created!
            Logger.info("Tick {}: Game level not yet created", ui.theGameClock().tickCount());
            return;
        }
        if (gameContext().theGameLevel().isDemoLevel()) {
            ui.theSound().setEnabled(false);
        } else {
            ui.theSound().setEnabled(true);
            updateSound(gameContext().theGameLevel());
        }
        updateHUD();
    }

    private void updateHUD() {
        LivesCounter livesCounter = gameContext().theGame().theHUD().theLivesCounter();
        int numLivesDisplayed = gameContext().theGame().lifeCount() - 1;
        // As long as Pac-Man is still initially hidden in the maze, he is shown as an entry in the lives counter
        if (gameContext().theGameState() == GameState.STARTING_GAME && !gameContext().theGameLevel().pac().isVisible()) {
            numLivesDisplayed += 1;
        }
        livesCounter.setVisibleLifeCount(Math.min(numLivesDisplayed, livesCounter.maxLivesDisplayed()));
        gameContext().theGame().theHUD().showCredit(gameContext().theCoinMechanism().isEmpty());
    }

    private void updateSound(GameLevel gameLevel) {
        final Pac pac = gameLevel.pac();
        //TODO check in simulator when exactly which siren plays
        boolean pacChased = gameContext().theGameState() == GameState.HUNTING && !pac.powerTimer().isRunning();
        if (pacChased) {
            // siren numbers are 1..4, hunting phase index = 0..7
            int huntingPhase = gameContext().theGame().huntingTimer().phaseIndex();
            int sirenNumber = 1 + huntingPhase / 2;
            switch (sirenNumber) {
                case 1 -> ui.theSound().playSiren(SoundID.SIREN_1, 1.0);
                case 2 -> ui.theSound().playSiren(SoundID.SIREN_2, 1.0);
                case 3 -> ui.theSound().playSiren(SoundID.SIREN_3, 1.0);
                case 4 -> ui.theSound().playSiren(SoundID.SIREN_4, 1.0);
                default -> throw new IllegalArgumentException("Illegal siren number " + sirenNumber);
            }
        }

        // TODO: how exactly is the munching sound created in the original game?
        if (pac.starvingTicks() > 10) {
            ui.theSound().pause(SoundID.PAC_MAN_MUNCHING);
        }

        //TODO check in simulator when exactly this sound is played
        var ghostReturning = gameLevel.ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).findAny();
        if (ghostReturning.isPresent()
            && (gameContext().theGameState() == GameState.HUNTING || gameContext().theGameState() == GameState.GHOST_DYING)) {
            ui.theSound().loop(SoundID.GHOST_RETURNS);
        } else {
            ui.theSound().stop(SoundID.GHOST_RETURNS);
        }
    }

    @Override
    public Vector2f sizeInPx() {
        // Note: scene is also used in Pac-Man XXL game variant were world can have arbitrary size
        return gameContext().optGameLevel().map(GameLevel::worldSizePx).orElse(ARCADE_MAP_SIZE_IN_PIXELS);
    }

    @Override
    public void drawSceneContent() {
        if (gameContext().optGameLevel().isEmpty()) {
            return; // Scene is drawn already 2 ticks before level has been created
        }

        final GameLevel gameLevel = gameContext().theGameLevel();
        gameRenderer.applyRenderingHints(gameLevel);

        // Level < Level message
        boolean highlighted = levelCompletedAnimation != null && levelCompletedAnimation.isHighlighted();
        gameRenderer.drawLevel(
            gameContext(),
            gameLevel,
            backgroundColor(),
            highlighted,
            gameLevel.blinking().isOn(),
            ui.theGameClock().tickCount()
        );
        gameLevel.house().ifPresent(house -> drawLevelMessageCenteredUnderHouse(house, gameLevel.messageType()));

        // Collect actors in drawing z-order: Bonus < Pac-Man < Ghosts in order
        // TODO: also take ghost state into account!
        actorsInZOrder.clear();
        gameLevel.bonus().ifPresent(actorsInZOrder::add);
        actorsInZOrder.add(gameLevel.pac());
        Stream.of(ORANGE_GHOST_POKEY, CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, RED_GHOST_SHADOW).map(gameLevel::ghost)
                .forEach(actorsInZOrder::add);

        gameRenderer.drawActors(actorsInZOrder);

        if (isDebugInfoVisible()) {
            actorsInZOrder.forEach(actor -> {
                if (actor instanceof MovingActor movingActor) {
                    gameRenderer.drawMovingActorInfo(movingActor);
                }
            });
        }
    }

    private void drawLevelMessageCenteredUnderHouse(House house, byte messageType) {
        Vector2i houseSize = house.sizeInTiles();
        float cx = TS * (house.minTile().x() + houseSize.x() * 0.5f);
        float cy = TS * (house.minTile().y() + houseSize.y() + 1);
        switch (messageType) {
            case GameLevel.MESSAGE_GAME_OVER -> gameRenderer.fillTextAtScaledCenter(
                "GAME  OVER", ARCADE_RED, scaledArcadeFont8(), cx, cy);
            case GameLevel.MESSAGE_READY -> gameRenderer.fillTextAtScaledCenter(
                "READY!", ARCADE_YELLOW, scaledArcadeFont8(), cx, cy);
            case GameLevel.MESSAGE_TEST -> gameRenderer.fillTextAtScaledCenter(
                "TEST    L%02d".formatted(gameContext().theGameLevel().number()), ARCADE_WHITE, scaledArcadeFont8(), cx, cy);
        }
    }

    @Override
    protected void drawDebugInfo() {
        gameRenderer.drawTileGrid(sizeInPx().x(), sizeInPx().y(), Color.LIGHTGRAY);
        if (gameContext().optGameLevel().isPresent()) {
            // assuming all ghosts have the same set of special terrain tiles
            gameContext().theGameLevel().ghost(RED_GHOST_SHADOW).specialTerrainTiles().forEach(tile -> {
                double x = scaled(tile.x() * TS), y = scaled(tile.y() * TS + HTS), size = scaled(TS);
                ctx().setFill(Color.RED);
                ctx().fillRect(x, y, size, 2);
            });
            // mark intersection tiles
            gameContext().theGameLevel().worldMap().tiles().filter(gameContext().theGameLevel()::isIntersection).forEach(tile -> {
                ctx().setStroke(Color.gray(0.8));
                ctx().setLineWidth(0.5);
                ctx().save();
                double cx = scaled(tile.x() * TS + HTS), cy = scaled(tile.y() * TS + HTS), size = scaled(HTS);
                ctx().translate(cx, cy);
                ctx().rotate(45);
                ctx().strokeRect(-0.5*size, -0.5*size, size, size);
                ctx().restore();
            });
            ctx().setFill(debugTextFill);
            ctx().setFont(debugTextFont);
            String gameStateText = gameContext().theGameState().name() + " (Tick %d)".formatted(gameContext().theGameState().timer().tickCount());
            String huntingPhaseText = "";
            if (gameContext().theGameState() == GameState.HUNTING) {
                HuntingTimer huntingTimer = gameContext().theGame().huntingTimer();
                huntingPhaseText = " %s (Tick %d)".formatted(huntingTimer.phase(), huntingTimer.tickCount());
            }
            ctx().fillText("%s%s".formatted(gameStateText, huntingPhaseText), 0, 64);
        }
    }

    @Override
    public void onEnterGameState(GameState state) {
        if (state == GameState.LEVEL_COMPLETE) {
            ui.theSound().stopAll();
            levelCompletedAnimation.setGameLevel(gameContext().theGameLevel());
            levelCompletedAnimation.setSingleFlashMillis(333);
            levelCompletedAnimation.getOrCreateAnimation().setOnFinished(e -> gameContext().theGameController().letCurrentGameStateExpire());
            levelCompletedAnimation.playFromStart();
        }
        else if (state == GameState.GAME_OVER) {
            ui.theSound().stopAll();
            ui.theSound().play(SoundID.GAME_OVER);
        }
    }

    @Override
    public void onBonusActivated(GameEvent e) {
        ui.theSound().loop(SoundID.BONUS_ACTIVE);
    }

    @Override
    public void onBonusEaten(GameEvent e) {
        ui.theSound().stop(SoundID.BONUS_ACTIVE);
        ui.theSound().play(SoundID.BONUS_EATEN);
    }

    @Override
    public void onBonusExpired(GameEvent e) {
        ui.theSound().stop(SoundID.BONUS_ACTIVE);
    }

    @Override
    public void onCreditAdded(GameEvent e) {
        ui.theSound().play(SoundID.COIN_INSERTED);
    }

    @Override
    public void onSpecialScoreReached(GameEvent e) {
        int score = e.payload("score");
        Logger.info("Extra life awarded for reaching score {}", score);
        ui.theSound().play(SoundID.EXTRA_LIFE);
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        ui.theSound().play(SoundID.GHOST_EATEN);
    }

    @Override
    public void onPacDead(GameEvent e) {
        gameContext().theGameController().letCurrentGameStateExpire();
    }

    @Override
    public void onPacDying(GameEvent e) {
        ui.theSound().pauseSiren();
        ui.theSound().play(SoundID.PAC_MAN_DEATH);
    }

    @Override
    public void onPacFoundFood(GameEvent e) {
        ui.theSound().loop(SoundID.PAC_MAN_MUNCHING);
    }

    @Override
    public void onPacGetsPower(GameEvent e) {
        ui.theSound().pauseSiren();
        ui.theSound().loop(SoundID.PAC_MAN_POWER);
    }

    @Override
    public void onPacLostPower(GameEvent e) {
        ui.theSound().pause(SoundID.PAC_MAN_POWER);
    }
}