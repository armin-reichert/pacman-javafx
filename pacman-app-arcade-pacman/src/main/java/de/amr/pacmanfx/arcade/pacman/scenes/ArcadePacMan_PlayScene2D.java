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
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.House;
import de.amr.pacmanfx.model.HuntingTimer;
import de.amr.pacmanfx.model.LivesCounter;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.MovingActor;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.DefaultDebugInfoRenderer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.api.GameScene;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.rendering.GameLevelRenderer;
import de.amr.pacmanfx.uilib.rendering.HUDRenderer;
import javafx.geometry.Point2D;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    private HUDRenderer hudRenderer;
    private GameLevelRenderer gameLevelRenderer;
    private LevelCompletedAnimation levelCompletedAnimation;

    private class PlaySceneDebugInfoRenderer extends DefaultDebugInfoRenderer {

        public PlaySceneDebugInfoRenderer(GameUI ui) {
            super(ui, canvas);
        }

        @Override
        public void drawDebugInfo() {
            drawTileGrid(sizeInPx().x(), sizeInPx().y(), Color.LIGHTGRAY);
            if (context().optGameLevel().isPresent()) {
                // assuming all ghosts have the same set of special terrain tiles
                context().gameLevel().ghost(RED_GHOST_SHADOW).specialTerrainTiles().forEach(tile -> {
                    double x = scaled(tile.x() * TS), y = scaled(tile.y() * TS + HTS), size = scaled(TS);
                    ctx.setFill(Color.RED);
                    ctx.fillRect(x, y, size, 2);
                });

                // mark intersection tiles
                context().gameLevel().worldMap().tiles().filter(context().gameLevel()::isIntersection).forEach(tile -> {
                    double[] xs = new double[4];
                    double[] ys = new double[4];
                    int n = 0;
                    Point2D center = new Point2D(tile.x() * TS + HTS, tile.y() * TS + HTS);
                    for (Direction dir : List.of(Direction.UP, Direction.RIGHT, Direction.DOWN, Direction.LEFT)) {
                        Vector2i neighborTile = tile.plus(dir.vector());
                        if (!context().gameLevel().isTileBlocked(neighborTile)) {
                            xs[n] = center.getX() + dir.vector().x() * HTS;
                            ys[n] = center.getY() + dir.vector().y() * HTS;
                            ++n;
                        }
                    }
                    ctx.setFill(Color.gray(0.6));
                    ctx.setLineWidth(1);
                    for (int i = 0; i < n; ++i) {
                        ctx.strokeLine(scaled(center.getX()), scaled(center.getY()), scaled(xs[i]), scaled(ys[i]));
                    }
                });

                String gameStateText = context().gameState().name() + " (Tick %d)".formatted(context().gameState().timer().tickCount());
                String huntingPhaseText = "";
                if (context().gameState() == GamePlayState.HUNTING) {
                    HuntingTimer huntingTimer = context().game().huntingTimer();
                    huntingPhaseText = " %s (Tick %d)".formatted(huntingTimer.phase(), huntingTimer.tickCount());
                }
                ctx.setFill(debugTextFill);
                ctx.setFont(debugTextFont);
                ctx.fillText("%s%s".formatted(gameStateText, huntingPhaseText), 0, TS(8));
            }
        }
    }

    public ArcadePacMan_PlayScene2D(GameUI ui) {
        super(ui);
    }

    @Override
    protected void doInit() {
        GameUI_Config uiConfig = ui.currentConfig();
        // Can be Pac-Man or Ms.Pac-Man renderer!
        hudRenderer = uiConfig.createHUDRenderer(canvas);
        gameLevelRenderer = uiConfig.createGameLevelRenderer(canvas);
        debugInfoRenderer = new PlaySceneDebugInfoRenderer(ui);
        bindRendererScaling(hudRenderer, gameLevelRenderer, debugInfoRenderer);
        context().game().hudData().credit(false).score(true).levelCounter(true).livesCounter(true);
        levelCompletedAnimation = new LevelCompletedAnimation(animationRegistry);
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
    private void acceptGameLevel(GameLevel gameLevel) {
        if (gameLevel.isDemoLevel()) {
            context().game().hudData().credit(false).levelCounter(true).livesCounter(false);
            actionBindings.assign(ACTION_ARCADE_INSERT_COIN, ui.actionBindings());
            ui.soundManager().setEnabled(false);
        } else {
            context().game().hudData().credit(false).levelCounter(true).livesCounter(true);
            actionBindings.assign(ACTION_STEER_UP,               ui.actionBindings());
            actionBindings.assign(ACTION_STEER_DOWN,             ui.actionBindings());
            actionBindings.assign(ACTION_STEER_LEFT,             ui.actionBindings());
            actionBindings.assign(ACTION_STEER_RIGHT,            ui.actionBindings());
            actionBindings.assign(ACTION_CHEAT_ADD_LIVES,        ui.actionBindings());
            actionBindings.assign(ACTION_CHEAT_EAT_ALL_PELLETS,  ui.actionBindings());
            actionBindings.assign(ACTION_CHEAT_ENTER_NEXT_LEVEL, ui.actionBindings());
            actionBindings.assign(ACTION_CHEAT_KILL_GHOSTS,      ui.actionBindings());
            ui.soundManager().setEnabled(true);
        }
        actionBindings.installBindings(ui.keyboard());
        Logger.info("Scene {} initialized with game level", getClass().getSimpleName());
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        acceptGameLevel(context().gameLevel());
    }

    @Override
    public void onSwitch_3D_2D(GameScene scene3D) {
        Logger.info("{} entered from {}", this, scene3D);
        if (context().optGameLevel().isPresent()) {
            acceptGameLevel(context().gameLevel());
        }
    }

    @Override
    public void onGameContinued(GameEvent e) {
        context().gameLevel().showMessage(GameLevel.MessageType.READY);
    }

    @Override
    public void onGameStarted(GameEvent e) {
        GameState state = context().gameState();
        boolean silent = context().gameLevel().isDemoLevel()
                || state.is(LevelShortTestState.class)
                || state.is(LevelMediumTestState.class);
        if (!silent) {
            ui.soundManager().play(SoundID.GAME_READY);
        }
    }

    @Override
    public void update() {
        // update() is call already 2 ticks before the game level gets created!
        if (context().optGameLevel().isEmpty()) {
            Logger.info("Tick {}: Game level not yet created", ui.clock().tickCount());
            return;
        }
        updateHUD();
        updateSound();
    }

    private void updateHUD() {
        LivesCounter livesCounter = context().game().hudData().theLivesCounter();
        int numLivesDisplayed = context().game().lifeCount() - 1;
        // As long as Pac-Man is still initially hidden in the maze, he is shown as an entry in the lives counter
        if (context().gameState() == GamePlayState.STARTING_GAME && !context().gameLevel().pac().isVisible()) {
            numLivesDisplayed += 1;
        }
        livesCounter.setVisibleLifeCount(Math.min(numLivesDisplayed, livesCounter.maxLivesDisplayed()));
        context().game().hudData().showCredit(context().coinMechanism().isEmpty());
    }

    private void updateSound() {
        if (!ui.soundManager().isEnabled()) return;
        Pac pac = context().gameLevel().pac();
        boolean pacChased = context().gameState() == GamePlayState.HUNTING && !pac.powerTimer().isRunning();
        if (pacChased) {
            // siren numbers are 1..4, hunting phase index = 0..7
            int huntingPhase = context().game().huntingTimer().phaseIndex();
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

        boolean isGhostReturningHome = context().gameLevel().pac().isAlive()
            && context().gameLevel().ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).findAny().isPresent();
        if (isGhostReturningHome) {
            ui.soundManager().loop(SoundID.GHOST_RETURNS);
        } else {
            ui.soundManager().stop(SoundID.GHOST_RETURNS);
        }
    }

    @Override
    public Vector2f sizeInPx() {
        // Note: scene is also used in Pac-Man XXL game variant where world can have any size
        return context().optGameLevel().map(GameLevel::worldSizePx).orElse(ARCADE_MAP_SIZE_IN_PIXELS);
    }

    @Override
    public List<MenuItem> supplyContextMenuItems(ContextMenuEvent contextMenuEvent, ContextMenu contextMenu) {
        var miAutopilot = new CheckMenuItem(ui.assets().translated("autopilot"));
        miAutopilot.selectedProperty().bindBidirectional(context().gameController().propertyUsingAutopilot());

        var miImmunity = new CheckMenuItem(ui.assets().translated("immunity"));
        miImmunity.selectedProperty().bindBidirectional(context().gameController().propertyImmunity());

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
    public void drawHUD() {
        if (hudRenderer != null) {
            hudRenderer.drawHUD(context(), context().game().hudData(), sizeInPx());
        }
    }

    @Override
    public void drawSceneContent() {
        if (context().optGameLevel().isEmpty()) {
            return; // Scene is drawn already 2 ticks before level has been created
        }
        final GameLevel gameLevel = context().gameLevel();

        // Draw game level
        boolean highlighted = levelCompletedAnimation != null && levelCompletedAnimation.isHighlighted();
        gameLevelRenderer.applyLevelSettings(context());
        gameLevelRenderer.drawGameLevel(context(), backgroundColor(), highlighted, gameLevel.blinking().isOn());

        // Draw message if available
        if (gameLevel.messageType() != GameLevel.MessageType.NONE && gameLevel.house().isPresent()) {
            House house = gameLevel.house().get();
            Vector2i houseSize = house.sizeInTiles();
            float cx = TS(house.minTile().x() + houseSize.x() * 0.5f);
            float cy = TS(house.minTile().y() + houseSize.y() + 1);
            switch (gameLevel.messageType()) {
                case GameLevel.MessageType.GAME_OVER -> gameLevelRenderer.fillTextCentered("GAME  OVER",
                        ARCADE_RED, gameLevelRenderer.arcadeFont8(), cx, cy);
                case GameLevel.MessageType.READY -> gameLevelRenderer.fillTextCentered("READY!",
                        ARCADE_YELLOW, gameLevelRenderer.arcadeFont8(), cx, cy);
                case GameLevel.MessageType.TEST -> gameLevelRenderer.fillTextCentered("TEST    L%02d".formatted(gameLevel.number()),
                        ARCADE_WHITE, gameLevelRenderer.arcadeFont8(), cx, cy);
            }
        }

        // Collect actors in drawing z-order: Bonus < Pac-Man < Ghosts in order. TODO: also take ghost state into account!
        actorsInZOrder.clear();
        gameLevel.bonus().ifPresent(actorsInZOrder::add);
        actorsInZOrder.add(gameLevel.pac());
        Stream.of(ORANGE_GHOST_POKEY, CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, RED_GHOST_SHADOW).map(gameLevel::ghost).forEach(actorsInZOrder::add);

        // Draw actors
        actorsInZOrder.forEach(actor -> gameLevelRenderer.drawActor(actor));

        if (isDebugInfoVisible() && debugInfoRenderer instanceof DefaultDebugInfoRenderer infoRenderer) {
            actorsInZOrder.stream()
                .filter(MovingActor.class::isInstance)
                .map(MovingActor.class::cast)
                .forEach(actor -> infoRenderer.drawMovingActorInfo(ctx(), scaling(), actor));
        }
    }

    @Override
    public void onEnterGameState(GameState state) {
        if (state == GamePlayState.LEVEL_COMPLETE) {
            ui.soundManager().stopAll();
            levelCompletedAnimation.setGameLevel(context().gameLevel());
            levelCompletedAnimation.setSingleFlashMillis(333);
            levelCompletedAnimation.getOrCreateAnimationFX().setOnFinished(e -> context().gameController().letCurrentGameStateExpire());
            levelCompletedAnimation.playFromStart();
        }
        else if (state == GamePlayState.GAME_OVER) {
            ui.soundManager().stopAll();
            ui.soundManager().play(SoundID.GAME_OVER);
        }
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
    public void onSpecialScoreReached(GameEvent e) {
        ui.soundManager().play(SoundID.EXTRA_LIFE);
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        ui.soundManager().play(SoundID.GHOST_EATEN);
    }

    @Override
    public void onPacDead(GameEvent e) {
        // triggers exit from state PACMAN_DYING after dying animation has finished
        context().gameController().letCurrentGameStateExpire();
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