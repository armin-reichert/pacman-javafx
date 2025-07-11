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
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.ui.GameAction;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.LevelCompletedAnimation;
import de.amr.pacmanfx.ui._2d.SpriteGameRenderer;
import de.amr.pacmanfx.ui.sound.SoundID;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.arcade.pacman.rendering.ArcadePalette.*;
import static de.amr.pacmanfx.controller.GameState.TESTING_LEVELS_MEDIUM;
import static de.amr.pacmanfx.controller.GameState.TESTING_LEVELS_SHORT;
import static de.amr.pacmanfx.ui.PacManGames.*;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.*;
import static de.amr.pacmanfx.ui.PacManGames_UI.*;
import static de.amr.pacmanfx.uilib.Ufx.menuTitleItem;

/**
 * 2D play scene for Arcade game variants.
 */
public class ArcadeCommon_PlayScene2D extends GameScene2D {

    private LevelCompletedAnimation levelCompletedAnimation;

    @Override
    public SpriteGameRenderer gr() {
        return (SpriteGameRenderer) super.gr();
    }

    @Override
    protected void doInit() {
        theGame().hud().showScore(true);
        theGame().hud().showLevelCounter(true);
        theGame().hud().showLivesCounter(true);
        levelCompletedAnimation = new LevelCompletedAnimation(animationManager);
    }

    @Override
    protected void doEnd() {
        if (levelCompletedAnimation != null) {
            animationManager.destroyAnimation(levelCompletedAnimation);
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
            theGame().hud().showLevelCounter(true);
            theGame().hud().showLivesCounter(false);
            bindAction(ACTION_ARCADE_INSERT_COIN, GLOBAL_ACTION_BINDING_MAP);
            updateActionBindings();
        } else {
            theGame().hud().showLevelCounter(true);
            theGame().hud().showLivesCounter(true);
            bindAction(ACTION_STEER_UP, GLOBAL_ACTION_BINDING_MAP);
            bindAction(ACTION_STEER_DOWN, GLOBAL_ACTION_BINDING_MAP);
            bindAction(ACTION_STEER_LEFT, GLOBAL_ACTION_BINDING_MAP);
            bindAction(ACTION_STEER_RIGHT, GLOBAL_ACTION_BINDING_MAP);
            bindAction(ACTION_CHEAT_EAT_ALL_PELLETS, GLOBAL_ACTION_BINDING_MAP);
            bindAction(ACTION_CHEAT_ADD_LIVES, GLOBAL_ACTION_BINDING_MAP);
            bindAction(ACTION_CHEAT_ENTER_NEXT_LEVEL, GLOBAL_ACTION_BINDING_MAP);
            bindAction(ACTION_CHEAT_KILL_GHOSTS, GLOBAL_ACTION_BINDING_MAP);
            updateActionBindings();
        }
        if (gameRenderer == null) { //TODO can this happen at all?
            gameRenderer = theUI().configuration().createGameRenderer(canvas);
            Logger.warn("No game renderer existed for 2D play scene, created one...");
        }
        Logger.info("Scene {} initialized with game level", getClass().getSimpleName());
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        initWithGameLevel(theGameLevel());
    }

    @Override
    public void onSwitch_3D_2D(GameScene scene3D) {
        Logger.info("2D scene {} entered from 3D scene {}", this, scene3D);
        if (optGameLevel().isPresent()) {
            initWithGameLevel(theGameLevel());
        }
    }

    @Override
    public List<MenuItem> supplyContextMenuItems(ContextMenuEvent e, ContextMenu menu) {
        var miAutopilot = new CheckMenuItem(theAssets().text("autopilot"));
        miAutopilot.selectedProperty().bindBidirectional(PY_USING_AUTOPILOT);

        var miImmunity = new CheckMenuItem(theAssets().text("immunity"));
        miImmunity.selectedProperty().bindBidirectional(PY_IMMUNITY);

        var miMuted = new CheckMenuItem(theAssets().text("muted"));
        miMuted.selectedProperty().bindBidirectional(theUI().mutedProperty());

        var miQuit = new MenuItem(theAssets().text("quit"));
        miQuit.setOnAction(ae -> GameAction.executeIfEnabled(theUI(), ACTION_QUIT_GAME_SCENE));

        return List.of(
            menuTitleItem(theAssets().text("pacman")),
            miAutopilot,
            miImmunity,
            new SeparatorMenuItem(),
            miMuted,
            miQuit);
    }

    @Override
    public void onGameContinued(GameEvent e) {
        theGameLevel().showMessage(GameLevel.MESSAGE_READY);
    }

    @Override
    public void onGameStarted(GameEvent e) {
        boolean silent = theGameLevel().isDemoLevel() || theGameState() == TESTING_LEVELS_SHORT || theGameState() == TESTING_LEVELS_MEDIUM;
        if (!silent) {
            theSound().play(SoundID.GAME_READY);
        }
    }

    @Override
    public void update() {
        if (optGameLevel().isEmpty()) {
            // Scene is already updated 2 ticks before the game level gets created!
            Logger.info("Tick {}: Game level not yet created", theClock().tickCount());
            return;
        }
        if (theGameLevel().isDemoLevel()) {
            theSound().setEnabled(false);
        } else {
            theSound().setEnabled(true);
            updateSound(theGameLevel());
        }
        updateHUD();
    }

    private void updateHUD() {
        LivesCounter livesCounter = theGame().hud().livesCounter();
        int numLivesDisplayed = theGame().lifeCount() - 1;
        // As long as Pac-Man is still initially hidden in the maze, he is shown as an entry in the lives counter
        if (theGameState() == GameState.STARTING_GAME && !theGameLevel().pac().isVisible()) {
            numLivesDisplayed += 1;
        }
        livesCounter.setVisibleLifeCount(Math.min(numLivesDisplayed, livesCounter.maxLivesDisplayed()));
        theGame().hud().showCredit(theCoinMechanism().isEmpty());
    }

    private void updateSound(GameLevel gameLevel) {
        final Pac pac = gameLevel.pac();
        //TODO check in simulator when exactly which siren plays
        boolean pacChased = theGameState() == GameState.HUNTING && !pac.powerTimer().isRunning();
        if (pacChased) {
            // siren numbers are 1..4, hunting phase index = 0..7
            int huntingPhase = theGame().huntingTimer().phaseIndex();
            int sirenNumber = 1 + huntingPhase / 2;
            switch (sirenNumber) {
                case 1 -> theSound().playSiren(SoundID.SIREN_1, 1.0);
                case 2 -> theSound().playSiren(SoundID.SIREN_2, 1.0);
                case 3 -> theSound().playSiren(SoundID.SIREN_3, 1.0);
                case 4 -> theSound().playSiren(SoundID.SIREN_4, 1.0);
                default -> throw new IllegalArgumentException("Illegal siren number " + sirenNumber);
            }
        }

        // TODO: how exactly is the munching sound created in the original game?
        if (pac.starvingTicks() > 10) {
            theSound().pause(SoundID.PAC_MAN_MUNCHING);
        }

        //TODO check in simulator when exactly this sound is played
        var ghostReturning = gameLevel.ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).findAny();
        if (ghostReturning.isPresent()
            && (theGameState() == GameState.HUNTING || theGameState() == GameState.GHOST_DYING)) {
            theSound().loop(SoundID.GHOST_RETURNS);
        } else {
            theSound().stop(SoundID.GHOST_RETURNS);
        }
    }

    @Override
    public Vector2f sizeInPx() {
        return optGameLevel().map(GameLevel::worldSizePx).orElse(ARCADE_MAP_SIZE_IN_PIXELS);
    }

    @Override
    public void drawSceneContent() {
        if (optGameLevel().isEmpty())
            return; // Scene is drawn already 2 ticks before level has been created

        gr().applyRenderingHints(theGameLevel());

        // Level < Level message
        boolean highlighted = levelCompletedAnimation != null && levelCompletedAnimation.isHighlighted();
        gr().drawLevel(theGameLevel(), backgroundColor(), highlighted, theGameLevel().blinking().isOn());
        theGameLevel().house().ifPresent(house -> drawLevelMessageCenteredUnderHouse(house, theGameLevel().messageType()));

        // Collect and draw actors in drawing z-order: bonus < Pac-Man < ghosts.
        List<Actor> actorsInDrawingOrder = new ArrayList<>();

        theGameLevel().bonus().map(Bonus::actor).ifPresent(actorsInDrawingOrder::add);
        actorsInDrawingOrder.add(theGameLevel().pac());
        Stream.of(ORANGE_GHOST_POKEY, CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, RED_GHOST_SHADOW).map(theGameLevel()::ghost)
                .forEach(actorsInDrawingOrder::add);

        actorsInDrawingOrder.forEach(actor -> {
            gr().drawActor(actor);
            if (debugInfoVisibleProperty().get() && actor instanceof MovingActor movingActor) {
                gr().drawMovingActorInfo(movingActor);
            }
        });
    }

    private void drawLevelMessageCenteredUnderHouse(House house, byte messageType) {
        Vector2i houseSize = house.sizeInTiles();
        float cx = TS * (house.minTile().x() + houseSize.x() * 0.5f);
        float cy = TS * (house.minTile().y() + houseSize.y() + 1);
        switch (messageType) {
            case GameLevel.MESSAGE_GAME_OVER -> gr().fillTextAtScaledCenter(
                "GAME  OVER", ARCADE_RED, scaledArcadeFont8(), cx, cy);
            case GameLevel.MESSAGE_READY -> gr().fillTextAtScaledCenter(
                "READY!", ARCADE_YELLOW, scaledArcadeFont8(), cx, cy);
            case GameLevel.MESSAGE_TEST -> gr().fillTextAtScaledCenter(
                "TEST    L%02d".formatted(theGameLevel().number()), ARCADE_WHITE, scaledArcadeFont8(), cx, cy);
        }
    }

    @Override
    protected void drawDebugInfo() {
        gr().drawTileGrid(sizeInPx().x(), sizeInPx().y(), Color.LIGHTGRAY);
        if (optGameLevel().isPresent()) {
            // assuming all ghosts have the same set of special terrain tiles
            theGameLevel().ghost(RED_GHOST_SHADOW).specialTerrainTiles().forEach(tile -> {
                double x = scaled(tile.x() * TS), y = scaled(tile.y() * TS + HTS), size = scaled(TS);
                ctx().setFill(Color.RED);
                ctx().fillRect(x, y, size, 2);
            });
            // mark intersection tiles
            theGameLevel().worldMap().tiles().filter(theGameLevel()::isIntersection).forEach(tile -> {
                ctx().setStroke(Color.gray(0.8));
                ctx().setLineWidth(0.5);
                ctx().save();
                double cx = scaled(tile.x() * TS + HTS), cy = scaled(tile.y() * TS + HTS), size = scaled(HTS);
                ctx().translate(cx, cy);
                ctx().rotate(45);
                ctx().strokeRect(-0.5*size, -0.5*size, size, size);
                ctx().restore();
            });
            ctx().setFill(DEBUG_TEXT_FILL);
            ctx().setFont(DEBUG_TEXT_FONT);
            String gameStateText = theGameState().name() + " (Tick %d)".formatted(theGameState().timer().tickCount());
            String huntingPhaseText = "";
            if (theGameState() == GameState.HUNTING) {
                HuntingTimer huntingTimer = theGame().huntingTimer();
                huntingPhaseText = " %s (Tick %d)".formatted(huntingTimer.phase(), huntingTimer.tickCount());
            }
            ctx().fillText("%s%s".formatted(gameStateText, huntingPhaseText), 0, 64);
        }
    }

    @Override
    public void onEnterGameState(GameState state) {
        if (state == GameState.LEVEL_COMPLETE) {
            theSound().stopAll();
            levelCompletedAnimation.setGameLevel(theGameLevel());
            levelCompletedAnimation.setSingleFlashMillis(333);
            levelCompletedAnimation.getOrCreateAnimation().setOnFinished(e -> theGameController().letCurrentGameStateExpire());
            levelCompletedAnimation.playFromStart();
        }
        else if (state == GameState.GAME_OVER) {
            theSound().stopAll();
            theSound().play(SoundID.GAME_OVER);
        }
    }

    @Override
    public void onBonusActivated(GameEvent e) {
        theSound().loop(SoundID.BONUS_ACTIVE);
    }

    @Override
    public void onBonusEaten(GameEvent e) {
        theSound().stop(SoundID.BONUS_ACTIVE);
        theSound().play(SoundID.BONUS_EATEN);
    }

    @Override
    public void onBonusExpired(GameEvent e) {
        theSound().stop(SoundID.BONUS_ACTIVE);
    }

    @Override
    public void onCreditAdded(GameEvent e) {
        theSound().play(SoundID.COIN_INSERTED);
    }

    @Override
    public void onSpecialScoreReached(GameEvent e) {
        int score = e.payload("score");
        Logger.info("Extra life awarded for reaching score {}", score);
        theSound().play(SoundID.EXTRA_LIFE);
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        theSound().play(SoundID.GHOST_EATEN);
    }

    @Override
    public void onPacDead(GameEvent e) {
        theGameController().letCurrentGameStateExpire();
    }

    @Override
    public void onPacDying(GameEvent e) {
        theSound().pauseSiren();
        theSound().play(SoundID.PAC_MAN_DEATH);
    }

    @Override
    public void onPacFoundFood(GameEvent e) {
        theSound().loop(SoundID.PAC_MAN_MUNCHING);
    }

    @Override
    public void onPacGetsPower(GameEvent e) {
        theSound().pauseSiren();
        theSound().loop(SoundID.PAC_MAN_POWER);
    }

    @Override
    public void onPacLostPower(GameEvent e) {
        theSound().pause(SoundID.PAC_MAN_POWER);
    }
}