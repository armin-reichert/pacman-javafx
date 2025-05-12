/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.GameVariant;
import de.amr.pacmanfx.model.HuntingTimer;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.ui.GameAction;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui._2d.FlashingMazeAnimation;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.uilib.Ufx;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.controller.GameState.TESTING_LEVELS;
import static de.amr.pacmanfx.controller.GameState.TESTING_LEVEL_TEASERS;
import static de.amr.pacmanfx.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_TILES;
import static de.amr.pacmanfx.ui.GameAssets.*;
import static de.amr.pacmanfx.ui.PacManGamesEnvironment.*;

/**
 * 2D play scene for Arcade game variants.
 *
 * @author Armin Reichert
 */
public class ArcadeAny_PlayScene2D extends GameScene2D {

    private FlashingMazeAnimation levelCompleteAnimation;

    @Override
    protected void doInit() {
        game().scoreManager().setScoreVisible(true);
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        GameLevel level = game().level().orElseThrow();
        gr.applyMapSettings(level.worldMap());
    }

    @Override
    public void onLevelStarted(GameEvent e) {
        GameLevel level = game().level().orElseThrow();
        if (level.isDemoLevel()) {
            bindArcadeInsertCoinAction();
            enableActionBindings();
        } else {
            bindPlayerActions();
            bindCheatActions();
            enableActionBindings();
        }
    }

    @Override
    public void onGameContinued(GameEvent e) {
        game().level().ifPresent(level -> level.showMessage(GameLevel.Message.READY));
    }

    @Override
    public void onGameStarted(GameEvent e) {
        GameLevel level = game().level().orElseThrow();
        boolean silent = level.isDemoLevel() || gameState() == TESTING_LEVELS || gameState() == TESTING_LEVEL_TEASERS;
        if (!silent) {
            THE_SOUND.playGameReadySound();
        }
    }

    @Override
    protected void doEnd() {
        THE_SOUND.stopAll();
        disableActionBindings();
    }

    @Override
    public void update() {
        if (game().level().isPresent()) {
            GameLevel level = game().level().get();
            /* TODO: Would like to do this only on level start, but when scene is switched between 2D and 3D,
                     the corresponding scene has to be updated accordingly. */
            if (level.isDemoLevel()) {
                game().assignDemoLevelBehavior(level.pac());
            }
            else {
                level.pac().setUsingAutopilot(PY_AUTOPILOT.get());
                level.pac().setImmune(PY_IMMUNITY.get());
                updateSound(level);
            }
            if (gameState() == GameState.LEVEL_COMPLETE) {
                levelCompleteAnimation.tick();
            }
        }
        else {
            // Scene is already visible 2 ticks before game level is created!
            Logger.info("Tick {}: Game level not yet available", THE_CLOCK.tickCount());
        }
    }

    @Override
    public List<MenuItem> supplyContextMenuItems(ContextMenuEvent e) {
        List<MenuItem> items = new ArrayList<>();
        items.add(Ufx.contextMenuTitleItem(THE_ASSETS.text("pacman")));

        var miAutopilot = new CheckMenuItem(THE_ASSETS.text("autopilot"));
        miAutopilot.selectedProperty().bindBidirectional(PY_AUTOPILOT);
        items.add(miAutopilot);

        var miImmunity = new CheckMenuItem(THE_ASSETS.text("immunity"));
        miImmunity.selectedProperty().bindBidirectional(PY_IMMUNITY);
        items.add(miImmunity);

        items.add(new SeparatorMenuItem());

        var miMuted = new CheckMenuItem(THE_ASSETS.text("muted"));
        miMuted.selectedProperty().bindBidirectional(THE_SOUND.mutedProperty());
        items.add(miMuted);

        var miQuit = new MenuItem(THE_ASSETS.text("quit"));
        miQuit.setOnAction(ae -> GameAction.QUIT_GAME_SCENE.execute());
        items.add(miQuit);

        return items;
    }

    private void updateSound(GameLevel level) {
        boolean pacChased = gameState() == GameState.HUNTING && !level.pac().powerTimer().isRunning();
        if (pacChased) {
            int sirenNumber = 1 + level.huntingTimer().phaseIndex() / 2;
            THE_SOUND.selectSiren(sirenNumber);
            THE_SOUND.playSiren();
        }
        // TODO: how exactly is the munching sound created in the original game?
        if (level.pac().starvingTicks() > 5) {
            THE_SOUND.stopMunchingSound();
        }
        boolean ghostsReturning = level.ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).anyMatch(Ghost::isVisible);
        if (level.pac().isAlive() && ghostsReturning) {
            THE_SOUND.playGhostReturningHomeSound();
        } else {
            THE_SOUND.stopGhostReturningHomeSound();
        }
    }

    @Override
    public Vector2f sizeInPx() {
        return levelSizeInTilesOrElse(ARCADE_MAP_SIZE_IN_TILES).scaled(TS).toVector2f();
    }

    @Override
    protected void drawSceneContent() {
        final GameLevel level = game().level().orElse(null);
        if (level == null) return; // Scene is drawn already 2 ticks before level has been created

        gr.applyMapSettings(level.worldMap());

        gr.drawScores(game().scoreManager(), ARCADE_WHITE, arcadeFontScaledTS());
        gr.drawMaze(level, 0, 3 * TS, backgroundColor(),
            levelCompleteAnimation != null && levelCompleteAnimation.inHighlightPhase(),
            level.blinking().isOn());
        if (level.message() != null) {
            drawLevelMessage(level, centerPositionBelowHouse(level));
        }
        level.bonus().ifPresent(gr::drawBonus);
        gr.drawActor(level.pac());
        ghostsInZOrder(level).forEach(gr::drawActor);
        if (debugInfoVisiblePy.get()) {
            gr.drawAnimatedCreatureInfo(level.pac());
            ghostsInZOrder(level).forEach(gr::drawAnimatedCreatureInfo);
        }
        // Draw either lives counter or missing credit
        if (game().canStartNewGame()) {
            // As long as Pac-Man is still invisible on game start, one live more is shown in the counter
            int numLivesDisplayed = gameState() == GameState.STARTING_GAME && !level.pac().isVisible()
                ? game().lifeCount() : game().lifeCount() - 1;
            gr.drawLivesCounter(numLivesDisplayed, LIVES_COUNTER_MAX, 2 * TS, sizeInPx().y() - 2 * TS);
        } else {
            gr.fillTextAtScaledPosition("CREDIT %2d".formatted(THE_COIN_MECHANISM.numCoins()),
                ARCADE_WHITE, arcadeFontScaledTS(), 2 * TS, sizeInPx().y() - 2);
        }
        gr.drawLevelCounter(game().levelCounter(), sizeInPx());
    }

    private void drawLevelMessage(GameLevel level, Vector2f messageCenterPosition) {
        switch (level.message()) {
            case GAME_OVER -> {
                String text = "GAME  OVER";
                // this assumes fixed font width of one tile:
                double x = messageCenterPosition.x() - (text.length() * HTS);
                gr.fillTextAtScaledPosition(text, ARCADE_RED, arcadeFontScaledTS(), x, messageCenterPosition.y());
            }
            case READY -> {
                String text = "READY!";
                // this assumes fixed font width of one tile:
                double x = messageCenterPosition.x() - (text.length() * HTS);
                gr.fillTextAtScaledPosition(text, ARCADE_YELLOW, arcadeFontScaledTS(), x, messageCenterPosition.y());
            }
            case TEST_LEVEL -> {
                String text = "TEST    L%03d".formatted(level.number());
                // this assumes fixed font width of one tile:
                double x = messageCenterPosition.x() - (text.length() * HTS);
                gr.fillTextAtScaledPosition(text, ARCADE_WHITE, arcadeFontScaledTS(), x, messageCenterPosition.y());
            }
        }
    }

    private Vector2f centerPositionBelowHouse(GameLevel level) {
        Vector2i houseTopLeft = level.houseMinTile(), houseSize = level.houseSizeInTiles();
        float x = TS * (houseTopLeft.x() + houseSize.x() * 0.5f);
        float y = TS * (houseTopLeft.y() + houseSize.y() + 1);
        return new Vector2f(x, y);
    }

    private Stream<Ghost> ghostsInZOrder(GameLevel level) {
        return Stream.of(ORANGE_GHOST_ID, CYAN_GHOST_ID, PINK_GHOST_ID, RED_GHOST_ID).map(level::ghost);
    }

    @Override
    protected void drawDebugInfo() {
        gr.drawTileGrid(sizeInPx().x(), sizeInPx().y(), Color.LIGHTGRAY);
        if (THE_GAME_CONTROLLER.isSelected(GameVariant.PACMAN)) {
            game().level().ifPresent(level -> {
                level.ghosts().forEach(ghost ->
                    ghost.specialTerrainTiles().forEach(tile -> {
                        double x = scaled(tile.x() * TS), y = scaled(tile.y() * TS + HTS), size = scaled(TS);
                        gr.ctx().setFill(Color.RED);
                        gr.ctx().fillRect(x, y, size, 2);
                    })
                );
                gr.ctx().setFill(Color.YELLOW);
                gr.ctx().setFont(DEBUG_TEXT_FONT);
                String gameStateText = gameState().name() + " (Tick %d)".formatted(gameState().timer().tickCount());
                String huntingPhaseText = "";
                if (gameState() == GameState.HUNTING) {
                    HuntingTimer huntingTimer = level.huntingTimer();
                    huntingPhaseText = " %s (Tick %d)".formatted(huntingTimer.phase(), huntingTimer.tickCount());
                }
                gr.ctx().fillText("%s%s".formatted(gameStateText, huntingPhaseText), 0, 64);
            });
        }
    }

    @Override
    public void onSceneVariantSwitch(GameScene oldScene) {
        Logger.info("{} entered from {}", this, oldScene);
        bindActions();
        bindPlayerActions();
        enableActionBindings();
        if (gr == null) {
            setGameRenderer(THE_UI_CONFIGS.current().createRenderer(canvas));
        }
        game().level().map(GameLevel::worldMap).ifPresent(gr::applyMapSettings);
    }

    @Override
    public void onEnterGameState(GameState state) {
        if (state == GameState.GAME_OVER) {
            THE_SOUND.playGameOverSound();
        }
        else if (state == GameState.LEVEL_COMPLETE) {
            game().level().ifPresent(level -> {
                THE_SOUND.stopAll();
                levelCompleteAnimation = new FlashingMazeAnimation(level);
                levelCompleteAnimation.setActionOnFinished(THE_GAME_CONTROLLER::letCurrentStateExpire);
                levelCompleteAnimation.start();
            });
        }
    }

    @Override
    public void onBonusActivated(GameEvent e) {
        THE_SOUND.playBonusActiveSound();
    }

    @Override
    public void onBonusEaten(GameEvent e) {
        THE_SOUND.stopBonusActiveSound();
        THE_SOUND.playBonusEatenSound();
    }

    @Override
    public void onBonusExpired(GameEvent e) {
        THE_SOUND.stopBonusActiveSound();
    }

    @Override
    public void onCreditAdded(GameEvent e) {
        THE_SOUND.playInsertCoinSound();
    }

    @Override
    public void onSpecialScoreReached(GameEvent e) {
        int score = e.payload("score");
        Logger.info("Extra life won for reaching score of {}", score);
        THE_SOUND.playExtraLifeSound();
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        THE_SOUND.playGhostEatenSound();
    }

    @Override
    public void onPacDead(GameEvent e) {
        THE_GAME_CONTROLLER.letCurrentStateExpire();
    }

    @Override
    public void onPacDying(GameEvent e) {
        THE_SOUND.playPacDeathSound();
    }

    @Override
    public void onPacFoundFood(GameEvent e) {
        THE_SOUND.playMunchingSound();
    }

    @Override
    public void onPacGetsPower(GameEvent e) {
        THE_SOUND.stopSiren();
        THE_SOUND.playPacPowerSound();
    }

    @Override
    public void onPacLostPower(GameEvent e) {
        THE_SOUND.stopPacPowerSound();
    }
}