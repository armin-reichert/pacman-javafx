/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.controller.GamePlayState;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.controller.teststates.LevelMediumTestState;
import de.amr.pacmanfx.controller.teststates.LevelShortTestState;
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
import de.amr.pacmanfx.ui._2d.DebugInfoRenderer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.api.GameScene;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.sound.SoundID;
import javafx.scene.canvas.GraphicsContext;
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
import static de.amr.pacmanfx.ui.CommonGameActions.*;
import static de.amr.pacmanfx.ui._2d.ArcadePalette.*;
import static de.amr.pacmanfx.ui.api.GameUI_Properties.PROPERTY_MUTED;
import static de.amr.pacmanfx.uilib.Ufx.createContextMenuTitle;

/**
 * 2D play scene for Arcade game variants.
 * <p>
 * TODO: Currently the instance of this scene is permanently stored in the UI configuration and lives as long as the
 *       game, so no garbage collection occurs!
 */
public class ArcadePacMan_PlayScene2D extends GameScene2D {

    private LevelCompletedAnimation levelCompletedAnimation;

    public ArcadePacMan_PlayScene2D(GameUI ui) {
        super(ui);
    }

    @Override
    protected void doInit() {
        gameContext().game().hudData().credit(false).score(true).levelCounter(true).livesCounter(true);
        levelCompletedAnimation = new LevelCompletedAnimation(animationRegistry);
        setGameRenderer(ui.currentConfig().createGameRenderer(canvas));
        setHudRenderer(ui.currentConfig().createHUDRenderer(canvas, scaling));
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
            gameContext().game().hudData().credit(false).levelCounter(true).livesCounter(false);
            actionBindings.assign(ACTION_ARCADE_INSERT_COIN, ui.actionBindings());
            actionBindings.installBindings(ui.keyboard());
        } else {
            gameContext().game().hudData().credit(false).levelCounter(true).livesCounter(true);
            actionBindings.assign(ACTION_STEER_UP, ui.actionBindings());
            actionBindings.assign(ACTION_STEER_DOWN, ui.actionBindings());
            actionBindings.assign(ACTION_STEER_LEFT, ui.actionBindings());
            actionBindings.assign(ACTION_STEER_RIGHT, ui.actionBindings());
            actionBindings.assign(ACTION_CHEAT_EAT_ALL_PELLETS, ui.actionBindings());
            actionBindings.assign(ACTION_CHEAT_ADD_LIVES, ui.actionBindings());
            actionBindings.assign(ACTION_CHEAT_ENTER_NEXT_LEVEL, ui.actionBindings());
            actionBindings.assign(ACTION_CHEAT_KILL_GHOSTS, ui.actionBindings());
            actionBindings.installBindings(ui.keyboard());
        }
        if (gameRenderer == null) { //TODO can this happen at all?
            gameRenderer = ui.currentConfig().createGameRenderer(canvas);
            Logger.warn("No game renderer existed for 2D play scene, created one...");
        }
        Logger.info("Scene {} initialized with game level", getClass().getSimpleName());
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        initWithGameLevel(gameContext().gameLevel());
    }

    @Override
    public void onSwitch_3D_2D(GameScene scene3D) {
        Logger.info("2D scene {} entered from 3D scene {}", this, scene3D);
        if (gameContext().optGameLevel().isPresent()) {
            initWithGameLevel(gameContext().gameLevel());
        }
    }

    @Override
    public List<MenuItem> supplyContextMenuItems(ContextMenuEvent contextMenuEvent, ContextMenu contextMenu) {
        var miAutopilot = new CheckMenuItem(ui.assets().translated("autopilot"));
        miAutopilot.selectedProperty().bindBidirectional(gameContext().gameController().propertyUsingAutopilot());

        var miImmunity = new CheckMenuItem(ui.assets().translated("immunity"));
        miImmunity.selectedProperty().bindBidirectional(gameContext().gameController().propertyImmunity());

        var miMuted = new CheckMenuItem(ui.assets().translated("muted"));
        miMuted.selectedProperty().bindBidirectional(PROPERTY_MUTED);

        var miQuit = new MenuItem(ui.assets().translated("quit"));
        miQuit.setOnAction(e -> ACTION_QUIT_GAME_SCENE.executeIfEnabled(ui));

        return List.of(
            createContextMenuTitle("pacman", ui.uiPreferences(), ui.assets()),
            miAutopilot,
            miImmunity,
            new SeparatorMenuItem(),
            miMuted,
            miQuit);
    }

    @Override
    public void onGameContinued(GameEvent e) {
        gameContext().gameLevel().showMessage(GameLevel.MESSAGE_READY);
    }

    @Override
    public void onGameStarted(GameEvent e) {
        GameState state = gameContext().gameState();
        boolean silent = gameContext().gameLevel().isDemoLevel()
                || state.is(LevelShortTestState.class)
                || state.is(LevelMediumTestState.class);
        if (!silent) {
            ui.soundManager().play(SoundID.GAME_READY);
        }
    }

    @Override
    public void update() {
        if (gameContext().optGameLevel().isEmpty()) {
            // Scene is already updated 2 ticks before the game level gets created!
            Logger.info("Tick {}: Game level not yet created", ui.clock().tickCount());
            return;
        }
        ui.soundManager().setEnabled(!gameContext().gameLevel().isDemoLevel());
        updateHUD();
        updateSound();
    }

    private void updateHUD() {
        LivesCounter livesCounter = gameContext().game().hudData().theLivesCounter();
        int numLivesDisplayed = gameContext().game().lifeCount() - 1;
        // As long as Pac-Man is still initially hidden in the maze, he is shown as an entry in the lives counter
        if (gameContext().gameState() == GamePlayState.STARTING_GAME && !gameContext().gameLevel().pac().isVisible()) {
            numLivesDisplayed += 1;
        }
        livesCounter.setVisibleLifeCount(Math.min(numLivesDisplayed, livesCounter.maxLivesDisplayed()));
        gameContext().game().hudData().showCredit(gameContext().coinMechanism().isEmpty());
    }

    private void updateSound() {
        if (!ui.soundManager().isEnabled()) return;
        Pac pac = gameContext().gameLevel().pac();
        boolean pacChased = gameContext().gameState() == GamePlayState.HUNTING && !pac.powerTimer().isRunning();
        if (pacChased) {
            // siren numbers are 1..4, hunting phase index = 0..7
            int huntingPhase = gameContext().game().huntingTimer().phaseIndex();
            int sirenNumber = 1 + huntingPhase / 2;
            switch (sirenNumber) {
                case 1 -> ui.soundManager().playSiren(SoundID.SIREN_1, 1.0);
                case 2 -> ui.soundManager().playSiren(SoundID.SIREN_2, 1.0);
                case 3 -> ui.soundManager().playSiren(SoundID.SIREN_3, 1.0);
                case 4 -> ui.soundManager().playSiren(SoundID.SIREN_4, 1.0);
                default -> throw new IllegalArgumentException("Illegal siren number " + sirenNumber);
            }
        }

        // TODO Still not sure how to do this right
        if (pac.starvingTicks() >= 10 && !ui.soundManager().isPaused(SoundID.PAC_MAN_MUNCHING)) {
            ui.soundManager().pause(SoundID.PAC_MAN_MUNCHING);
        }

        boolean isGhostReturningHome = gameContext().gameLevel().pac().isAlive()
            && gameContext().gameLevel().ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).findAny().isPresent();
        if (isGhostReturningHome) {
            ui.soundManager().loop(SoundID.GHOST_RETURNS);
        } else {
            ui.soundManager().stop(SoundID.GHOST_RETURNS);
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

        final GameLevel gameLevel = gameContext().gameLevel();
        gameRenderer.applyLevelSettings(gameContext());

        // Draw game level
        boolean highlighted = levelCompletedAnimation != null && levelCompletedAnimation.isHighlighted();
        gameRenderer.drawGameLevel(gameContext(), backgroundColor(), highlighted, gameLevel.blinking().isOn());

        // Draw message if available
        if (gameLevel.messageType() != GameLevel.MESSAGE_NONE && gameLevel.house().isPresent()) {
            House house = gameLevel.house().get();
            Vector2i houseSize = house.sizeInTiles();
            float cx = TS(house.minTile().x() + houseSize.x() * 0.5f);
            float cy = TS(house.minTile().y() + houseSize.y() + 1);
            switch (gameLevel.messageType()) {
                case GameLevel.MESSAGE_GAME_OVER -> gameRenderer.fillTextCentered("GAME  OVER", ARCADE_RED, scaledArcadeFont8(), cx, cy);
                case GameLevel.MESSAGE_READY     -> gameRenderer.fillTextCentered("READY!", ARCADE_YELLOW, scaledArcadeFont8(), cx, cy);
                case GameLevel.MESSAGE_TEST      -> gameRenderer.fillTextCentered("TEST    L%02d".formatted(gameLevel.number()), ARCADE_WHITE, scaledArcadeFont8(), cx, cy);
            }
        }

        // Collect actors in drawing z-order: Bonus < Pac-Man < Ghosts in order. TODO: also take ghost state into account!
        actorsInZOrder.clear();
        gameLevel.bonus().ifPresent(actorsInZOrder::add);
        actorsInZOrder.add(gameLevel.pac());
        Stream.of(ORANGE_GHOST_POKEY, CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, RED_GHOST_SHADOW).map(gameLevel::ghost).forEach(actorsInZOrder::add);

        // Draw actors
        actorsInZOrder.forEach(actor -> gameRenderer.drawActor(actor));

        if (isDebugInfoVisible()) {
            actorsInZOrder.forEach(actor -> {
                if (actor instanceof MovingActor movingActor && gameRenderer instanceof DebugInfoRenderer debugInfoRenderer) {
                    debugInfoRenderer.drawMovingActorInfo(gameRenderer.ctx(), scaling(), movingActor);
                }
            });
        }
    }

    @Override
    protected void drawDebugInfo() {
        GraphicsContext ctx = gameRenderer.ctx();
        gameRenderer.drawTileGrid(sizeInPx().x(), sizeInPx().y(), Color.LIGHTGRAY);
        if (gameContext().optGameLevel().isPresent()) {
            // assuming all ghosts have the same set of special terrain tiles
            gameContext().gameLevel().ghost(RED_GHOST_SHADOW).specialTerrainTiles().forEach(tile -> {
                double x = scaled(tile.x() * TS), y = scaled(tile.y() * TS + HTS), size = scaled(TS);
                ctx.setFill(Color.RED);
                ctx.fillRect(x, y, size, 2);
            });
            // mark intersection tiles
            gameContext().gameLevel().worldMap().tiles().filter(gameContext().gameLevel()::isIntersection).forEach(tile -> {
                ctx.setStroke(Color.gray(0.8));
                ctx.setLineWidth(0.5);
                ctx.save();
                double cx = scaled(tile.x() * TS + HTS), cy = scaled(tile.y() * TS + HTS), size = scaled(HTS);
                ctx.translate(cx, cy);
                ctx.rotate(45);
                ctx.strokeRect(-0.5*size, -0.5*size, size, size);
                ctx.restore();
            });
            ctx.setFill(debugTextFill);
            ctx.setFont(debugTextFont);
            String gameStateText = gameContext().gameState().name() + " (Tick %d)".formatted(gameContext().gameState().timer().tickCount());
            String huntingPhaseText = "";
            if (gameContext().gameState() == GamePlayState.HUNTING) {
                HuntingTimer huntingTimer = gameContext().game().huntingTimer();
                huntingPhaseText = " %s (Tick %d)".formatted(huntingTimer.phase(), huntingTimer.tickCount());
            }
            ctx.fillText("%s%s".formatted(gameStateText, huntingPhaseText), 0, 64);
        }
    }

    @Override
    public void onEnterGameState(GameState state) {
        if (state == GamePlayState.LEVEL_COMPLETE) {
            ui.soundManager().stopAll();
            levelCompletedAnimation.setGameLevel(gameContext().gameLevel());
            levelCompletedAnimation.setSingleFlashMillis(333);
            levelCompletedAnimation.getOrCreateAnimationFX().setOnFinished(e -> gameContext().gameController().letCurrentGameStateExpire());
            levelCompletedAnimation.playFromStart();
        }
        else if (state == GamePlayState.GAME_OVER) {
            ui.soundManager().stopAll();
            ui.soundManager().play(SoundID.GAME_OVER);
        }
    }

    @Override
    public void onBonusActivated(GameEvent e) {
        ui.soundManager().loop(SoundID.BONUS_ACTIVE);
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
    public void onSpecialScoreReached(GameEvent e) {
        ui.soundManager().play(SoundID.EXTRA_LIFE);
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        ui.soundManager().play(SoundID.GHOST_EATEN);
    }

    @Override
    public void onPacDead(GameEvent e) {
        gameContext().gameController().letCurrentGameStateExpire();
    }

    @Override
    public void onPacDying(GameEvent e) {
        ui.soundManager().pauseSiren();
        ui.soundManager().play(SoundID.PAC_MAN_DEATH);
    }

    @Override
    public void onPacFoundFood(GameEvent e) {
        ui.soundManager().loop(SoundID.PAC_MAN_MUNCHING);
    }

    @Override
    public void onPacGetsPower(GameEvent e) {
        ui.soundManager().pauseSiren();
        ui.soundManager().loop(SoundID.PAC_MAN_POWER);
    }

    @Override
    public void onPacLostPower(GameEvent e) {
        ui.soundManager().pause(SoundID.PAC_MAN_POWER);
    }
}