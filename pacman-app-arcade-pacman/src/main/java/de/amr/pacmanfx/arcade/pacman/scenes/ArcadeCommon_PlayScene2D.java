/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.House;
import de.amr.pacmanfx.model.HuntingTimer;
import de.amr.pacmanfx.model.LivesCounter;
import de.amr.pacmanfx.model.actors.*;
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
import static de.amr.pacmanfx.controller.GameState.TESTING_LEVELS_MEDIUM;
import static de.amr.pacmanfx.controller.GameState.TESTING_LEVELS_SHORT;
import static de.amr.pacmanfx.ui.GameUI.*;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.*;
import static de.amr.pacmanfx.ui._2d.ArcadePalette.*;
import static de.amr.pacmanfx.uilib.Ufx.menuTitleItem;

/**
 * 2D play scene for Arcade game variants.
 * <p>
 * TODO: Currently the instance of this scene is permanently stored in the UI configuration and lives as long as the
 *       game, so no garbage collection occurs!
 */
public class ArcadeCommon_PlayScene2D extends GameScene2D {

    private final List<Actor> actorsInDrawingOrder = new ArrayList<>();
    private LevelCompletedAnimation levelCompletedAnimation;

    public ArcadeCommon_PlayScene2D(GameContext gameContext) {
        super(gameContext);
    }

    @Override
    public void destroy() {
        if (levelCompletedAnimation != null) {
            animationManager.destroyAnimation(levelCompletedAnimation);
            levelCompletedAnimation = null;
        }
        actorsInDrawingOrder.clear();
        if (gameRenderer != null) {
            gameRenderer.destroy();
            gameRenderer = null;
        }
    }

    @Override
    protected void doInit() {
        gameContext.theGame().hud().showScore(true);
        gameContext.theGame().hud().showLevelCounter(true);
        gameContext.theGame().hud().showLivesCounter(true);
        levelCompletedAnimation = new LevelCompletedAnimation(animationManager);
        gameRenderer = theUI().theUIConfiguration().createGameRenderer(canvas);
    }

    @Override
    protected void doEnd() {
    }

    @Override
    public SpriteGameRenderer gr() {
        return (SpriteGameRenderer) gameRenderer;
    }

    /*
      Note: If the corresponding 3D scene is displayed when the game level gets created,
      the onLevelCreated() handler of this scene is not called!
      So we have to initialize the scene also with the game level when switching from the 3D scene.
     */
    private void initWithGameLevel(GameLevel gameLevel) {
        if (gameLevel.isDemoLevel()) {
            gameContext.theGame().hud().showLevelCounter(true);
            gameContext.theGame().hud().showLivesCounter(false);
            actionBindings.bind(ACTION_ARCADE_INSERT_COIN, GLOBAL_ACTION_BINDINGS);
            actionBindings.update();
        } else {
            gameContext.theGame().hud().showLevelCounter(true);
            gameContext.theGame().hud().showLivesCounter(true);
            actionBindings.bind(ACTION_STEER_UP, GLOBAL_ACTION_BINDINGS);
            actionBindings.bind(ACTION_STEER_DOWN, GLOBAL_ACTION_BINDINGS);
            actionBindings.bind(ACTION_STEER_LEFT, GLOBAL_ACTION_BINDINGS);
            actionBindings.bind(ACTION_STEER_RIGHT, GLOBAL_ACTION_BINDINGS);
            actionBindings.bind(ACTION_CHEAT_EAT_ALL_PELLETS, GLOBAL_ACTION_BINDINGS);
            actionBindings.bind(ACTION_CHEAT_ADD_LIVES, GLOBAL_ACTION_BINDINGS);
            actionBindings.bind(ACTION_CHEAT_ENTER_NEXT_LEVEL, GLOBAL_ACTION_BINDINGS);
            actionBindings.bind(ACTION_CHEAT_KILL_GHOSTS, GLOBAL_ACTION_BINDINGS);
            actionBindings.update();
        }
        if (gameRenderer == null) { //TODO can this happen at all?
            gameRenderer = theUI().theUIConfiguration().createGameRenderer(canvas);
            Logger.warn("No game renderer existed for 2D play scene, created one...");
        }
        Logger.info("Scene {} initialized with game level", getClass().getSimpleName());
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        initWithGameLevel(gameContext.theGameLevel());
    }

    @Override
    public void onSwitch_3D_2D(GameScene scene3D) {
        Logger.info("2D scene {} entered from 3D scene {}", this, scene3D);
        if (gameContext.optGameLevel().isPresent()) {
            initWithGameLevel(gameContext.theGameLevel());
        }
    }

    @Override
    public List<MenuItem> supplyContextMenuItems(ContextMenuEvent contextMenuEvent, ContextMenu contextMenu) {
        var miAutopilot = new CheckMenuItem(theUI().theAssets().text("autopilot"));
        miAutopilot.selectedProperty().bindBidirectional(theGameContext().propertyUsingAutopilot());

        var miImmunity = new CheckMenuItem(theUI().theAssets().text("immunity"));
        miImmunity.selectedProperty().bindBidirectional(theGameContext().propertyImmunity());

        var miMuted = new CheckMenuItem(theUI().theAssets().text("muted"));
        miMuted.selectedProperty().bindBidirectional(theUI().mutedProperty());

        var miQuit = new MenuItem(theUI().theAssets().text("quit"));
        miQuit.setOnAction(e -> ACTION_QUIT_GAME_SCENE.executeIfEnabled(theUI()));

        return List.of(
            menuTitleItem(theUI().theAssets().text("pacman")),
            miAutopilot,
            miImmunity,
            new SeparatorMenuItem(),
            miMuted,
            miQuit);
    }

    @Override
    public void onGameContinued(GameEvent e) {
        gameContext.theGameLevel().showMessage(GameLevel.MESSAGE_READY);
    }

    @Override
    public void onGameStarted(GameEvent e) {
        boolean silent = gameContext.theGameLevel().isDemoLevel() || gameContext.theGameState() == TESTING_LEVELS_SHORT || gameContext.theGameState() == TESTING_LEVELS_MEDIUM;
        if (!silent) {
            theUI().theSound().play(SoundID.GAME_READY);
        }
    }

    @Override
    public void update() {
        if (gameContext.optGameLevel().isEmpty()) {
            // Scene is already updated 2 ticks before the game level gets created!
            Logger.info("Tick {}: Game level not yet created", theUI().theGameClock().tickCount());
            return;
        }
        if (gameContext.theGameLevel().isDemoLevel()) {
            theUI().theSound().setEnabled(false);
        } else {
            theUI().theSound().setEnabled(true);
            updateSound(gameContext.theGameLevel());
        }
        updateHUD();
    }

    private void updateHUD() {
        LivesCounter livesCounter = gameContext.theGame().hud().livesCounter();
        int numLivesDisplayed = gameContext.theGame().lifeCount() - 1;
        // As long as Pac-Man is still initially hidden in the maze, he is shown as an entry in the lives counter
        if (gameContext.theGameState() == GameState.STARTING_GAME && !gameContext.theGameLevel().pac().isVisible()) {
            numLivesDisplayed += 1;
        }
        livesCounter.setVisibleLifeCount(Math.min(numLivesDisplayed, livesCounter.maxLivesDisplayed()));
        gameContext.theGame().hud().showCredit(gameContext.theCoinMechanism().isEmpty());
    }

    private void updateSound(GameLevel gameLevel) {
        final Pac pac = gameLevel.pac();
        //TODO check in simulator when exactly which siren plays
        boolean pacChased = gameContext.theGameState() == GameState.HUNTING && !pac.powerTimer().isRunning();
        if (pacChased) {
            // siren numbers are 1..4, hunting phase index = 0..7
            int huntingPhase = gameContext.theGame().huntingTimer().phaseIndex();
            int sirenNumber = 1 + huntingPhase / 2;
            switch (sirenNumber) {
                case 1 -> theUI().theSound().playSiren(SoundID.SIREN_1, 1.0);
                case 2 -> theUI().theSound().playSiren(SoundID.SIREN_2, 1.0);
                case 3 -> theUI().theSound().playSiren(SoundID.SIREN_3, 1.0);
                case 4 -> theUI().theSound().playSiren(SoundID.SIREN_4, 1.0);
                default -> throw new IllegalArgumentException("Illegal siren number " + sirenNumber);
            }
        }

        // TODO: how exactly is the munching sound created in the original game?
        if (pac.starvingTicks() > 10) {
            theUI().theSound().pause(SoundID.PAC_MAN_MUNCHING);
        }

        //TODO check in simulator when exactly this sound is played
        var ghostReturning = gameLevel.ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).findAny();
        if (ghostReturning.isPresent()
            && (gameContext.theGameState() == GameState.HUNTING || gameContext.theGameState() == GameState.GHOST_DYING)) {
            theUI().theSound().loop(SoundID.GHOST_RETURNS);
        } else {
            theUI().theSound().stop(SoundID.GHOST_RETURNS);
        }
    }

    @Override
    public Vector2f sizeInPx() {
        // Note: scene is also used in Pac-Man XXL game variant were world can have arbitrary size
        return gameContext.optGameLevel().map(GameLevel::worldSizePx).orElse(ARCADE_MAP_SIZE_IN_PIXELS);
    }

    @Override
    public void drawSceneContent() {
        if (gameContext.optGameLevel().isEmpty())
            return; // Scene is drawn already 2 ticks before level has been created

        gr().applyRenderingHints(gameContext.theGameLevel());

        // Level < Level message
        boolean highlighted = levelCompletedAnimation != null && levelCompletedAnimation.isHighlighted();
        gr().drawLevel(gameContext, gameContext.theGameLevel(), backgroundColor(), highlighted, gameContext.theGameLevel().blinking().isOn());
        gameContext.theGameLevel().house().ifPresent(house -> drawLevelMessageCenteredUnderHouse(house, gameContext.theGameLevel().messageType()));

        // Collect and draw actors in drawing z-order: bonus < Pac-Man < ghosts.
        actorsInDrawingOrder.clear();
        gameContext.theGameLevel().bonus().map(Bonus::actor).ifPresent(actorsInDrawingOrder::add);
        actorsInDrawingOrder.add(gameContext.theGameLevel().pac());
        Stream.of(ORANGE_GHOST_POKEY, CYAN_GHOST_BASHFUL, PINK_GHOST_SPEEDY, RED_GHOST_SHADOW).map(gameContext.theGameLevel()::ghost)
                .forEach(actorsInDrawingOrder::add);
        actorsInDrawingOrder.forEach(actor -> {
            gr().drawActor(actor);
            if (debugInfoVisibleProperty().get() && actor instanceof MovingActor movingActor) {
                gr().drawMovingActorInfo(movingActor);
            }
        });
        actorsInDrawingOrder.clear();
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
                "TEST    L%02d".formatted(gameContext.theGameLevel().number()), ARCADE_WHITE, scaledArcadeFont8(), cx, cy);
        }
    }

    @Override
    protected void drawDebugInfo() {
        gr().drawTileGrid(sizeInPx().x(), sizeInPx().y(), Color.LIGHTGRAY);
        if (gameContext.optGameLevel().isPresent()) {
            // assuming all ghosts have the same set of special terrain tiles
            gameContext.theGameLevel().ghost(RED_GHOST_SHADOW).specialTerrainTiles().forEach(tile -> {
                double x = scaled(tile.x() * TS), y = scaled(tile.y() * TS + HTS), size = scaled(TS);
                ctx().setFill(Color.RED);
                ctx().fillRect(x, y, size, 2);
            });
            // mark intersection tiles
            gameContext.theGameLevel().worldMap().tiles().filter(gameContext.theGameLevel()::isIntersection).forEach(tile -> {
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
            String gameStateText = gameContext.theGameState().name() + " (Tick %d)".formatted(gameContext.theGameState().timer().tickCount());
            String huntingPhaseText = "";
            if (gameContext.theGameState() == GameState.HUNTING) {
                HuntingTimer huntingTimer = gameContext.theGame().huntingTimer();
                huntingPhaseText = " %s (Tick %d)".formatted(huntingTimer.phase(), huntingTimer.tickCount());
            }
            ctx().fillText("%s%s".formatted(gameStateText, huntingPhaseText), 0, 64);
        }
    }

    @Override
    public void onEnterGameState(GameState state) {
        if (state == GameState.LEVEL_COMPLETE) {
            theUI().theSound().stopAll();
            levelCompletedAnimation.setGameLevel(gameContext.theGameLevel());
            levelCompletedAnimation.setSingleFlashMillis(333);
            levelCompletedAnimation.getOrCreateAnimation().setOnFinished(e -> gameContext.theGameController().letCurrentGameStateExpire());
            levelCompletedAnimation.playFromStart();
        }
        else if (state == GameState.GAME_OVER) {
            theUI().theSound().stopAll();
            theUI().theSound().play(SoundID.GAME_OVER);
        }
    }

    @Override
    public void onBonusActivated(GameEvent e) {
        theUI().theSound().loop(SoundID.BONUS_ACTIVE);
    }

    @Override
    public void onBonusEaten(GameEvent e) {
        theUI().theSound().stop(SoundID.BONUS_ACTIVE);
        theUI().theSound().play(SoundID.BONUS_EATEN);
    }

    @Override
    public void onBonusExpired(GameEvent e) {
        theUI().theSound().stop(SoundID.BONUS_ACTIVE);
    }

    @Override
    public void onCreditAdded(GameEvent e) {
        theUI().theSound().play(SoundID.COIN_INSERTED);
    }

    @Override
    public void onSpecialScoreReached(GameEvent e) {
        int score = e.payload("score");
        Logger.info("Extra life awarded for reaching score {}", score);
        theUI().theSound().play(SoundID.EXTRA_LIFE);
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        theUI().theSound().play(SoundID.GHOST_EATEN);
    }

    @Override
    public void onPacDead(GameEvent e) {
        gameContext.theGameController().letCurrentGameStateExpire();
    }

    @Override
    public void onPacDying(GameEvent e) {
        theUI().theSound().pauseSiren();
        theUI().theSound().play(SoundID.PAC_MAN_DEATH);
    }

    @Override
    public void onPacFoundFood(GameEvent e) {
        theUI().theSound().loop(SoundID.PAC_MAN_MUNCHING);
    }

    @Override
    public void onPacGetsPower(GameEvent e) {
        theUI().theSound().pauseSiren();
        theUI().theSound().loop(SoundID.PAC_MAN_POWER);
    }

    @Override
    public void onPacLostPower(GameEvent e) {
        theUI().theSound().pause(SoundID.PAC_MAN_POWER);
    }
}