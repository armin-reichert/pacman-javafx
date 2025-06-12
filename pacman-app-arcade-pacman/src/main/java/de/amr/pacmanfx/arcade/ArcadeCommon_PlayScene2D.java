/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade;

import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.RectArea;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.HuntingTimer;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.ui.PacManGames_Action;
import de.amr.pacmanfx.ui.PacManGames_ActionBinding;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.LevelFinishedAnimation;
import de.amr.pacmanfx.ui._2d.SpriteGameRenderer;
import de.amr.pacmanfx.ui._2d.VectorGraphicsGameRenderer;
import de.amr.pacmanfx.uilib.GameScene;
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
import static de.amr.pacmanfx.arcade.ArcadePacMan_UIConfig.ARCADE_MAP_SIZE_IN_TILES;
import static de.amr.pacmanfx.arcade.ArcadePalette.*;
import static de.amr.pacmanfx.controller.GameState.TESTING_LEVELS;
import static de.amr.pacmanfx.controller.GameState.TESTING_LEVEL_TEASERS;
import static de.amr.pacmanfx.ui.PacManGames_Action.*;
import static de.amr.pacmanfx.ui.PacManGames_Env.*;
import static de.amr.pacmanfx.ui.PacManGames_UI.*;

/**
 * 2D play scene for Arcade game variants.
 */
public class ArcadeCommon_PlayScene2D extends GameScene2D implements PacManGames_ActionBinding {

    private RectArea livesCounterSprite;
    private LevelFinishedAnimation levelFinishedAnimation;

    public void setLivesCounterSprite(RectArea livesCounterSprite) {
        this.livesCounterSprite = livesCounterSprite;
    }

    @Override
    protected void doInit() {
        theGame().setScoreVisible(true);
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        gr().applyRenderingHints(theGameLevel());
        if (theGameLevel().isDemoLevel()) {
            bindAction(ACTION_ARCADE_INSERT_COIN, COMMON_ACTION_BINDINGS);
        } else {
            bindAction(ACTION_STEER_UP, COMMON_ACTION_BINDINGS);
            bindAction(ACTION_STEER_DOWN, COMMON_ACTION_BINDINGS);
            bindAction(ACTION_STEER_LEFT, COMMON_ACTION_BINDINGS);
            bindAction(ACTION_STEER_RIGHT, COMMON_ACTION_BINDINGS);
            bindAction(ACTION_CHEAT_EAT_ALL_PELLETS, COMMON_ACTION_BINDINGS);
            bindAction(ACTION_CHEAT_ADD_LIVES, COMMON_ACTION_BINDINGS);
            bindAction(ACTION_CHEAT_ENTER_NEXT_LEVEL, COMMON_ACTION_BINDINGS);
            bindAction(ACTION_CHEAT_KILL_GHOSTS, COMMON_ACTION_BINDINGS);
        }
        updateActionBindings();
    }

    @Override
    public void onGameContinued(GameEvent e) {
        theGameLevel().showMessage(GameLevel.MESSAGE_READY);
    }

    @Override
    public void onGameStarted(GameEvent e) {
        boolean silent = theGameLevel().isDemoLevel() || theGameState() == TESTING_LEVELS || theGameState() == TESTING_LEVEL_TEASERS;
        if (!silent) {
            theSound().playGameReadySound();
        }
    }

    @Override
    public void update() {
        if (optGameLevel().isPresent()) {
            if (!theGameLevel().isDemoLevel()) {
                updateSound();
            }
        } else {
            // Scene is already active 2 ticks before game level is created!
            Logger.info("Tick {}: Game level not yet available", theClock().tickCount());
        }
    }

    @Override
    public List<MenuItem> supplyContextMenuItems(ContextMenuEvent e) {
        List<MenuItem> items = new ArrayList<>();
        items.add(Ufx.contextMenuTitleItem(theAssets().text("pacman")));

        var miAutopilot = new CheckMenuItem(theAssets().text("autopilot"));
        miAutopilot.selectedProperty().bindBidirectional(PY_USING_AUTOPILOT);
        items.add(miAutopilot);

        var miImmunity = new CheckMenuItem(theAssets().text("immunity"));
        miImmunity.selectedProperty().bindBidirectional(PY_IMMUNITY);
        items.add(miImmunity);

        items.add(new SeparatorMenuItem());

        var miMuted = new CheckMenuItem(theAssets().text("muted"));
        miMuted.selectedProperty().bindBidirectional(theSound().mutedProperty());
        items.add(miMuted);

        var miQuit = new MenuItem(theAssets().text("quit"));
        miQuit.setOnAction(ae -> PacManGames_Action.ACTION_QUIT_GAME_SCENE.execute());
        items.add(miQuit);

        return items;
    }

    private void updateSound() {
        boolean pacChased = theGameState() == GameState.HUNTING && !theGameLevel().pac().powerTimer().isRunning();
        if (pacChased) {
            int sirenNumber = 1 + theGame().huntingTimer().phaseIndex() / 2;
            theSound().selectSiren(sirenNumber);
            theSound().playSiren();
        }
        // TODO: how exactly is the munching sound created in the original game?
        if (theGameLevel().pac().starvingTicks() > 5) {
            theSound().stopMunchingSound();
        }
        boolean ghostsReturning = theGameLevel().ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).anyMatch(Ghost::isVisible);
        if (theGameLevel().pac().isAlive() && ghostsReturning) {
            theSound().playGhostReturningHomeSound();
        } else {
            theSound().stopGhostReturningHomeSound();
        }
    }

    @Override
    public Vector2f sizeInPx() {
        if (optGameLevel().isPresent()) {
            int numRows = theGameLevel().worldMap().numRows();
            int numCols = theGameLevel().worldMap().numCols();
            return new Vector2f(numCols * TS, numRows * TS);
        }
        return ARCADE_MAP_SIZE_IN_TILES.scaled(TS).toVector2f();
    }

    @Override
    protected void drawSceneContent() {
        if (optGameLevel().isEmpty())
            return; // Scene is drawn already 2 ticks before level has been created

        gr().applyRenderingHints(theGameLevel());

        //TODO: check this
        if (gr() instanceof VectorGraphicsGameRenderer vr) {
            vr.setBackgroundColor(PY_CANVAS_BG_COLOR.get());
        }

        boolean highlighted = levelFinishedAnimation != null
            && levelFinishedAnimation.isRunning() && levelFinishedAnimation.isHighlighted();
        gr().drawLevel(theGameLevel(), 0, 3 * TS, backgroundColor(), highlighted, theGameLevel().blinking().isOn());

        if (theGameLevel().message() != GameLevel.MESSAGE_NONE) {
            drawLevelMessage();
        }

        // Use correct z-order: bonus, Pac-Man, ghosts in order
        theGameLevel().bonus().ifPresent(gr()::drawBonus);
        gr().drawActor(theGameLevel().pac());
        Stream.of(ORANGE_GHOST_POKEY, CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, RED_GHOST_SHADOW)
                .map(theGameLevel()::ghost).forEach(ghost -> gr().drawActor(ghost));

        if (debugInfoVisibleProperty().get()) {
            gr().drawAnimatedActorInfo(theGameLevel().pac());
            theGameLevel().ghosts().forEach(gr()::drawAnimatedActorInfo);
        }

        // Draw either lives counter or credit text
        if (theGame().canStartNewGame()) {
            // As long as Pac-Man is still invisible on game start, one live more is shown in the counter
            int numLivesDisplayed = theGameState() == GameState.STARTING_GAME && !theGameLevel().pac().isVisible()
                ? theGame().lifeCount() : theGame().lifeCount() - 1;
            gr().drawLivesCounter(numLivesDisplayed, LIVES_COUNTER_MAX, 2 * TS, sizeInPx().y() - 2 * TS,
                    livesCounterSprite);
        } else {
            gr().fillText("CREDIT %2d".formatted(theCoinMechanism().numCoins()),
                scoreColor(), arcadeFont8(), 2 * TS, sizeInPx().y() - 2);
        }
        gr().drawLevelCounter(theGame().levelCounter(), sizeInPx());
    }

    private void drawLevelMessage() {
        Vector2i houseMinTile = theGameLevel().houseMinTile(), houseSize = theGameLevel().houseSizeInTiles();
        float cx = TS * (houseMinTile.x() + houseSize.x() * 0.5f);
        float cy = TS * (houseMinTile.y() + houseSize.y() + 1);
        switch (theGameLevel().message()) {
            case GameLevel.MESSAGE_GAME_OVER -> gr().fillTextAtCenter("GAME  OVER", ARCADE_RED, arcadeFont8(), cx, cy);
            case GameLevel.MESSAGE_READY -> gr().fillTextAtCenter("READY!", ARCADE_YELLOW, arcadeFont8(), cx, cy);
            case GameLevel.MESSAGE_TEST -> gr().fillTextAtCenter("TEST    L%02d".formatted(theGameLevel().number()),
                    ARCADE_WHITE, arcadeFont8(), cx, cy);
        }
    }

    @Override
    protected void drawDebugInfo() {
        gr().drawTileGrid(sizeInPx().x(), sizeInPx().y(), Color.LIGHTGRAY);
        optGameLevel().ifPresent(level -> {
            // assume all ghosts have the same special tiles
            level.ghost(RED_GHOST_SHADOW).specialTerrainTiles().forEach(tile -> {
                double x = scaled(tile.x() * TS), y = scaled(tile.y() * TS + HTS), size = scaled(TS);
                ctx().setFill(Color.RED);
                ctx().fillRect(x, y, size, 2);
            });
            level.worldMap().tiles().filter(level::isIntersection).forEach(tile -> {
                ctx().setStroke(Color.gray(0.8));
                ctx().setLineWidth(0.5);
                ctx().save();
                double cx = scaled(tile.x() * TS + HTS), cy = scaled(tile.y() * TS + HTS), size = scaled(HTS);
                ctx().translate(cx, cy);
                ctx().rotate(45);
                ctx().strokeRect(-0.5*size, -0.5*size, size, size);
                ctx().restore();
            });
            ctx().setFill(Color.YELLOW);
            ctx().setFont(DEBUG_TEXT_FONT);
            String gameStateText = theGameState().name() + " (Tick %d)".formatted(theGameState().timer().tickCount());
            String huntingPhaseText = "";
            if (theGameState() == GameState.HUNTING) {
                HuntingTimer huntingTimer = theGame().huntingTimer();
                huntingPhaseText = " %s (Tick %d)".formatted(huntingTimer.phase(), huntingTimer.tickCount());
            }
            ctx().fillText("%s%s".formatted(gameStateText, huntingPhaseText), 0, 64);
        });
    }

    @Override
    public void onSwitch_3D_2D(GameScene scene3D) {
        Logger.info("2D scene {} entered from 3D scene {}", this, scene3D);
        bindAction(ACTION_STEER_UP, COMMON_ACTION_BINDINGS);
        bindAction(ACTION_STEER_DOWN, COMMON_ACTION_BINDINGS);
        bindAction(ACTION_STEER_LEFT, COMMON_ACTION_BINDINGS);
        bindAction(ACTION_STEER_RIGHT, COMMON_ACTION_BINDINGS);
        bindAction(ACTION_CHEAT_EAT_ALL_PELLETS, COMMON_ACTION_BINDINGS);
        bindAction(ACTION_CHEAT_ADD_LIVES, COMMON_ACTION_BINDINGS);
        bindAction(ACTION_CHEAT_ENTER_NEXT_LEVEL, COMMON_ACTION_BINDINGS);
        bindAction(ACTION_CHEAT_KILL_GHOSTS, COMMON_ACTION_BINDINGS);
        updateActionBindings();
        if (gr() == null) { //TODO check if this can happen
            Logger.warn("No game renderer was existing when switching to 2D scene");
            setGameRenderer((SpriteGameRenderer) theUI().configuration().createRenderer(canvas()));
        }
        optGameLevel().ifPresent(gr()::applyRenderingHints);
    }

    @Override
    public void onEnterGameState(GameState state) {
        if (state == GameState.GAME_OVER) {
            theSound().playGameOverSound();
        }
        else if (state == GameState.LEVEL_COMPLETE) {
            theSound().stopAll();
            levelFinishedAnimation = new LevelFinishedAnimation(theGameLevel(), 333);
            levelFinishedAnimation.setOnFinished(theGameController()::letCurrentGameStateExpire);
            levelFinishedAnimation.play();
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