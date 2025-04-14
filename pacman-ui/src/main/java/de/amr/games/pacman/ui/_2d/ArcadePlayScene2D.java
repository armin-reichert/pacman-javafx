/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._2d;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.controller.HuntingTimer;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.ui.GameAction;
import de.amr.games.pacman.ui.GameScene;
import de.amr.games.pacman.uilib.Ufx;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.controller.GameState.TESTING_LEVELS;
import static de.amr.games.pacman.controller.GameState.TESTING_LEVEL_TEASERS;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_TILES;
import static de.amr.games.pacman.ui.Globals.*;
import static de.amr.games.pacman.uilib.Keyboard.naked;

/**
 * 2D play scene for Arcade game variants.
 *
 * @author Armin Reichert
 */
public class ArcadePlayScene2D extends GameScene2D {

    private LevelCompleteAnimation levelCompleteAnimation;

    @Override
    protected void doInit() {
        game().scoreVisibleProperty().set(true);
        bindDefaultArcadeActions();
        enableActionBindings(THE_KEYBOARD);
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        if (game().isDemoLevel()) {
            bind(GameAction.INSERT_COIN,  naked(KeyCode.DIGIT5), naked(KeyCode.NUMPAD5));
        } else {
            bindCheatActions();
            bindDefaultArcadeActions();
        }
        enableActionBindings(THE_KEYBOARD);
        game().level().ifPresent(level -> gr.applyMapSettings(level.worldMap()));
    }

    @Override
    public void onGameStarted(GameEvent e) {
        boolean silent = game().isDemoLevel() || gameState() == TESTING_LEVELS || gameState() == TESTING_LEVEL_TEASERS;
        if (!silent) {
            THE_SOUND.playGameReadySound();
        }
    }

    @Override
    protected void doEnd() {
        THE_SOUND.stopAll();
        disableActionBindings(THE_KEYBOARD);
    }

    @Override
    public void update() {
        game().level().ifPresentOrElse(level -> {
            /* TODO: I would like to do this only on level start but when scene view is switched
                between 2D and 3D, the other scene has to be updated accordingly. */
            if (game().isDemoLevel()) {
                game().assignDemoLevelBehavior(level.pac());
            }
            else {
                level.pac().setUsingAutopilot(PY_AUTOPILOT.get());
                level.pac().setImmune(PY_IMMUNITY.get());
                updateSound(level);
            }
            if (gameState() == GameState.LEVEL_COMPLETE) {
                levelCompleteAnimation.update();
            }
        }, () -> { // Scene is already visible 2 ticks before game level is created!
            Logger.warn("Tick {}: Game level not yet available", THE_CLOCK.tickCount());
        });
    }

    private void updateSound(GameLevel level) {
        if (gameState() == GameState.HUNTING && !level.powerTimer().isRunning()) {
            int sirenNumber = 1 + game().huntingTimer().phaseIndex() / 2;
            THE_SOUND.selectSiren(sirenNumber);
            THE_SOUND.playSiren();
        }
        if (level.pac().starvingTicks() > 8) { // TODO not sure how to do this right
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
        Font font = THE_ASSETS.arcadeFontAtSize(scaled(TS));
        gr.setScaling(scaling());
        gr.fillCanvas(backgroundColor());
        if (game().isScoreVisible()) {
            gr.drawScores(game().scoreManager(), Color.web(Arcade.Palette.WHITE), font);
        }
        GameLevel level = game().level().orElse(null);
        // Scene is drawn already for 2 ticks before level has been created
        if (level == null) {
            if (!THE_CLOCK.isPaused()) {
                Logger.warn("Tick {}: Game level not yet available, scene content not drawn", THE_CLOCK.tickCount());
            }
            return;
        }

        // Draw maze
        gr.applyMapSettings(level.worldMap());
        gr.drawMaze(level, 0, 3 * TS, backgroundColor(),
            levelCompleteAnimation != null && levelCompleteAnimation.isInHighlightPhase(),
            level.blinking().isOn());

        if (level.message() != null) {
            drawLevelMessage(level, font, centerPositionBelowHouse(level));
        }

        level.bonus().ifPresent(gr::drawBonus);

        gr.drawAnimatedActor(level.pac());
        ghostsInZOrder(level).forEach(gr::drawAnimatedActor);

        if (debugInfoVisiblePy.get()) {
            gr.drawAnimatedCreatureInfo(level.pac());
            ghostsInZOrder(level).forEach(gr::drawAnimatedCreatureInfo);
        }

        // Draw lives counter or remaining credit
        if (game().canStartNewGame()) {
            //TODO: this code is ugly. Realizes effect that Pac is "picked" from the lives counter when game starts.
            int numLivesShown = game().livesProperty().get() - 1;
            if (gameState() == GameState.STARTING_GAME && !level.pac().isVisible()) {
                numLivesShown += 1;
            }
            gr.drawLivesCounter(numLivesShown, LIVES_COUNTER_MAX, 2 * TS, sizeInPx().y() - 2 * TS);
        } else {
            gr.fillTextAtScaledPosition("CREDIT %2d".formatted(THE_COIN_SLOT.numCoins()),
                Color.web(Arcade.Palette.WHITE), font, 2 * TS, sizeInPx().y() - 2);
        }
        gr.drawLevelCounter(game().levelCounter(), sizeInPx());
    }

    private void drawLevelMessage(GameLevel level, Font font, Vector2f messageCenterPosition) {
        switch (level.message()) {
            case GAME_OVER -> {
                String text = "GAME  OVER";
                // this assumes fixed font width of one tile:
                double x = messageCenterPosition.x() - (text.length() * HTS);
                gr.fillTextAtScaledPosition(text, Color.web(Arcade.Palette.RED), font, x, messageCenterPosition.y());
            }
            case READY -> {
                String text = "READY!";
                // this assumes fixed font width of one tile:
                double x = messageCenterPosition.x() - (text.length() * HTS);
                gr.fillTextAtScaledPosition(text, Color.web(Arcade.Palette.YELLOW), font, x, messageCenterPosition.y());
            }
            case TEST_LEVEL -> {
                String text = "TEST    L%03d".formatted(level.number());
                // this assumes fixed font width of one tile:
                double x = messageCenterPosition.x() - (text.length() * HTS);
                gr.fillTextAtScaledPosition(text, Color.web(Arcade.Palette.WHITE), font, x, messageCenterPosition.y());
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
        return Stream.of(GameModel.ORANGE_GHOST_ID, GameModel.CYAN_GHOST_ID, GameModel.PINK_GHOST_ID, GameModel.RED_GHOST_ID).map(level::ghost);
    }

    @Override
    protected void drawDebugInfo() {
        gr.drawTileGrid(sizeInPx().x(), sizeInPx().y(), Color.LIGHTGRAY);
        if (THE_GAME_CONTROLLER.isGameVariantSelected(GameVariant.PACMAN)) {
            game().level().ifPresent(level ->
                level.ghosts().forEach(ghost ->
                    ghost.specialTerrainTiles().forEach(tile -> {
                        double x = scaled(tile.x() * TS), y = scaled(tile.y() * TS + HTS), size = scaled(TS);
                        gr.ctx().setFill(Color.RED);
                        gr.ctx().fillRect(x, y, size, 2);
                    }))
            );
        }
        gr.ctx().setFill(Color.YELLOW);
        gr.ctx().setFont(DEBUG_TEXT_FONT);
        String gameStateText = gameState().name() + " (Tick %d)".formatted(gameState().timer().tickCount());
        String huntingPhaseText = "";
        if (gameState() == GameState.HUNTING) {
            HuntingTimer huntingTimer = game().huntingTimer();
            huntingPhaseText = " %s (Tick %d)".formatted(huntingTimer.huntingPhase(), huntingTimer.tickCount());
        }
        gr.ctx().fillText("%s%s".formatted(gameStateText, huntingPhaseText), 0, 64);
    }

    @Override
    public void onSceneVariantSwitch(GameScene oldScene) {
        Logger.info("{} entered from {}", this, oldScene);
        bindActions();
        enableActionBindings(THE_KEYBOARD);
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
                levelCompleteAnimation = new LevelCompleteAnimation(level.numFlashes(), 10);
                levelCompleteAnimation.setOnHideGhosts(() -> level.ghosts().forEach(Ghost::hide));
                levelCompleteAnimation.setOnFinished(THE_GAME_CONTROLLER::terminateCurrentState);
                levelCompleteAnimation.start();
            });
        }
    }

    @Override
    public void onBonusActivated(GameEvent e) {
        THE_SOUND.playBonusBouncingSound();
    }

    @Override
    public void onBonusEaten(GameEvent e) {
        THE_SOUND.stopBonusBouncingSound();
        THE_SOUND.playBonusEatenSound();
    }

    @Override
    public void onBonusExpired(GameEvent e) {
        THE_SOUND.stopBonusBouncingSound();
    }

    @Override
    public void onCreditAdded(GameEvent e) {
        THE_SOUND.playInsertCoinSound();
    }

    @Override
    public void onExtraLifeWon(GameEvent e) {
        THE_SOUND.playExtraLifeSound();
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        THE_SOUND.playGhostEatenSound();
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
        miQuit.setOnAction(ae -> GameAction.SHOW_START_VIEW.execute());
        items.add(miQuit);

        return items;
    }
}