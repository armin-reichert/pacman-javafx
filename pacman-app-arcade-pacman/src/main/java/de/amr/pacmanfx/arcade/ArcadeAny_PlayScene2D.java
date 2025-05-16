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
import static de.amr.pacmanfx.ui.PacManGamesEnv.*;

/**
 * 2D play scene for Arcade game variants.
 *
 * @author Armin Reichert
 */
public class ArcadeAny_PlayScene2D extends GameScene2D {

    private FlashingMazeAnimation levelCompleteAnimation;

    @Override
    protected void doInit() {
        theGame().scoreManager().setScoreVisible(true);
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        GameLevel level = theGameLevel();
        gr.applyMapSettings(level.worldMap());
    }

    @Override
    public void onLevelStarted(GameEvent e) {
        if (theGameLevel().isDemoLevel()) {
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
        theGameLevel().showMessage(GameLevel.Message.READY);
    }

    @Override
    public void onGameStarted(GameEvent e) {
        boolean silent = theGameLevel().isDemoLevel() || theGameState() == TESTING_LEVELS || theGameState() == TESTING_LEVEL_TEASERS;
        if (!silent) {
            theSound().playGameReadySound();
        }
    }

    @Override
    protected void doEnd() {
        theSound().stopAll();
        disableActionBindings();
    }

    @Override
    public void update() {
        if (optionalGameLevel().isPresent()) {
            GameLevel level = optionalGameLevel().get();
            /* TODO: Would like to do this only on level start, but when scene is switched between 2D and 3D,
                     the corresponding scene has to be updated accordingly. */
            if (level.isDemoLevel()) {
                theGame().assignDemoLevelBehavior(level.pac());
            }
            else {
                level.pac().setUsingAutopilot(PY_AUTOPILOT.get());
                level.pac().setImmune(PY_IMMUNITY.get());
                updateSound(level);
            }
            if (theGameState() == GameState.LEVEL_COMPLETE) {
                levelCompleteAnimation.tick();
            }
        }
        else {
            // Scene is already active 2 ticks before game level is created!
            Logger.info("Tick {}: Game level not yet available", theClock().tickCount());
        }
    }

    @Override
    public List<MenuItem> supplyContextMenuItems(ContextMenuEvent e) {
        List<MenuItem> items = new ArrayList<>();
        items.add(Ufx.contextMenuTitleItem(theAssets().text("pacman")));

        var miAutopilot = new CheckMenuItem(theAssets().text("autopilot"));
        miAutopilot.selectedProperty().bindBidirectional(PY_AUTOPILOT);
        items.add(miAutopilot);

        var miImmunity = new CheckMenuItem(theAssets().text("immunity"));
        miImmunity.selectedProperty().bindBidirectional(PY_IMMUNITY);
        items.add(miImmunity);

        items.add(new SeparatorMenuItem());

        var miMuted = new CheckMenuItem(theAssets().text("muted"));
        miMuted.selectedProperty().bindBidirectional(theSound().mutedProperty());
        items.add(miMuted);

        var miQuit = new MenuItem(theAssets().text("quit"));
        miQuit.setOnAction(ae -> GameAction.QUIT_GAME_SCENE.execute());
        items.add(miQuit);

        return items;
    }

    private void updateSound(GameLevel level) {
        boolean pacChased = theGameState() == GameState.HUNTING && !level.pac().powerTimer().isRunning();
        if (pacChased) {
            int sirenNumber = 1 + level.huntingTimer().phaseIndex() / 2;
            theSound().selectSiren(sirenNumber);
            theSound().playSiren();
        }
        // TODO: how exactly is the munching sound created in the original game?
        if (level.pac().starvingTicks() > 5) {
            theSound().stopMunchingSound();
        }
        boolean ghostsReturning = level.ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).anyMatch(Ghost::isVisible);
        if (level.pac().isAlive() && ghostsReturning) {
            theSound().playGhostReturningHomeSound();
        } else {
            theSound().stopGhostReturningHomeSound();
        }
    }

    @Override
    public Vector2f sizeInPx() {
        return levelSizeInTilesOrElse(ARCADE_MAP_SIZE_IN_TILES).scaled(TS).toVector2f();
    }

    @Override
    protected void drawSceneContent() {
        if (optionalGameLevel().isEmpty())
            return; // Scene is drawn already 2 ticks before level has been created

        final GameLevel level = theGameLevel();

        gr.applyMapSettings(level.worldMap());

        gr.drawScores(theGame().scoreManager(), ARCADE_WHITE, arcadeFontScaledTS());
        gr.drawMaze(level, 0, 3 * TS, backgroundColor(),
            levelCompleteAnimation != null && levelCompleteAnimation.inHighlightPhase(),
            level.blinking().isOn());
        if (level.message() != null) {
            drawLevelMessage(level.message(), level.number(), centerPositionBelowHouse(level));
        }
        level.bonus().ifPresent(gr::drawBonus);
        gr.drawActor(level.pac());
        ghostsInZOrder(level).forEach(gr::drawActor);
        if (debugInfoVisiblePy.get()) {
            gr.drawAnimatedCreatureInfo(level.pac());
            ghostsInZOrder(level).forEach(gr::drawAnimatedCreatureInfo);
        }
        // Draw either lives counter or missing credit
        if (theGame().canStartNewGame()) {
            // As long as Pac-Man is still invisible on game start, one live more is shown in the counter
            int numLivesDisplayed = theGameState() == GameState.STARTING_GAME && !level.pac().isVisible()
                ? theGame().lifeCount() : theGame().lifeCount() - 1;
            gr.drawLivesCounter(numLivesDisplayed, LIVES_COUNTER_MAX, 2 * TS, sizeInPx().y() - 2 * TS);
        } else {
            gr.fillTextAtScaledPosition("CREDIT %2d".formatted(theCoinMechanism().numCoins()),
                ARCADE_WHITE, arcadeFontScaledTS(), 2 * TS, sizeInPx().y() - 2);
        }
        gr.drawLevelCounter(theGame().levelCounter(), sizeInPx());
    }

    private void drawLevelMessage(GameLevel.Message message, int levelNumber, Vector2f messageCenterPosition) {
        switch (message) {
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
                String text = "TEST    L%03d".formatted(levelNumber);
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
        return Stream.of(ORANGE_GHOST_POKEY, CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, RED_GHOST_SHADOW).map(level::ghost);
    }

    @Override
    protected void drawDebugInfo() {
        gr.drawTileGrid(sizeInPx().x(), sizeInPx().y(), Color.LIGHTGRAY);
        if (theGameController().isSelected(GameVariant.PACMAN)) {
            optionalGameLevel().ifPresent(level -> {
                level.ghosts().forEach(ghost ->
                    ghost.specialTerrainTiles().forEach(tile -> {
                        double x = scaled(tile.x() * TS), y = scaled(tile.y() * TS + HTS), size = scaled(TS);
                        gr.ctx().setFill(Color.RED);
                        gr.ctx().fillRect(x, y, size, 2);
                    })
                );
                gr.ctx().setFill(Color.YELLOW);
                gr.ctx().setFont(DEBUG_TEXT_FONT);
                String gameStateText = theGameState().name() + " (Tick %d)".formatted(theGameState().timer().tickCount());
                String huntingPhaseText = "";
                if (theGameState() == GameState.HUNTING) {
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
            setGameRenderer(theUIConfig().current().createRenderer(canvas));
        }
        optionalGameLevel().map(GameLevel::worldMap).ifPresent(gr::applyMapSettings);
    }

    @Override
    public void onEnterGameState(GameState state) {
        if (state == GameState.GAME_OVER) {
            theSound().playGameOverSound();
        }
        else if (state == GameState.LEVEL_COMPLETE) {
            optionalGameLevel().ifPresent(level -> {
                theSound().stopAll();
                levelCompleteAnimation = new FlashingMazeAnimation(level);
                levelCompleteAnimation.setActionOnFinished(theGameController()::letCurrentGameStateExpire);
                levelCompleteAnimation.start();
            });
        }
    }

    @Override
    public void onBonusActivated(GameEvent e) {
        theSound().playBonusActiveSound();
    }

    @Override
    public void onBonusEaten(GameEvent e) {
        theSound().stopBonusActiveSound();
        theSound().playBonusEatenSound();
    }

    @Override
    public void onBonusExpired(GameEvent e) {
        theSound().stopBonusActiveSound();
    }

    @Override
    public void onCreditAdded(GameEvent e) {
        theSound().playInsertCoinSound();
    }

    @Override
    public void onSpecialScoreReached(GameEvent e) {
        int score = e.payload("score");
        Logger.info("Extra life won for reaching score of {}", score);
        theSound().playExtraLifeSound();
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        theSound().playGhostEatenSound();
    }

    @Override
    public void onPacDead(GameEvent e) {
        theGameController().letCurrentGameStateExpire();
    }

    @Override
    public void onPacDying(GameEvent e) {
        theSound().playPacDeathSound();
    }

    @Override
    public void onPacFoundFood(GameEvent e) {
        theSound().playMunchingSound();
    }

    @Override
    public void onPacGetsPower(GameEvent e) {
        theSound().stopSiren();
        theSound().playPacPowerSound();
    }

    @Override
    public void onPacLostPower(GameEvent e) {
        theSound().stopPacPowerSound();
    }
}