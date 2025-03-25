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
import de.amr.games.pacman.ui.GameScene;
import de.amr.games.pacman.ui.sound.GameSound;
import de.amr.games.pacman.uilib.Ufx;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.controller.GameState.TESTING_LEVELS;
import static de.amr.games.pacman.controller.GameState.TESTING_LEVEL_TEASERS;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_TILES;
import static de.amr.games.pacman.ui.UIGlobals.*;
import static de.amr.games.pacman.ui._2d.GlobalProperties2d.PY_AUTOPILOT;
import static de.amr.games.pacman.ui._2d.GlobalProperties2d.PY_IMMUNITY;

/**
 * 2D play scene for all game variants except Tengen Ms. Pac-Man.
 *
 * @author Armin Reichert
 */
public class ArcadePlayScene2D extends GameScene2D {

    private LevelCompleteAnimation levelCompleteAnimation;

    @Override
    public void bindGameActions() {}

    @Override
    protected void doInit() {
        THE_GAME_CONTEXT.setScoreVisible(true);
        GameActions2D.bindDefaultArcadeControllerActions(this, THE_GAME_CONTEXT.arcadeKeys());
        GameActions2D.bindFallbackPlayerControlActions(this);
        registerGameActionKeyBindings();
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        if (THE_GAME_CONTROLLER.game().isDemoLevel()) {
            bind(GameActions2D.INSERT_COIN, THE_GAME_CONTEXT.arcadeKeys().key(Arcade.Button.COIN));
        } else {
            GameActions2D.bindCheatActions(this);
            GameActions2D.bindDefaultArcadeControllerActions(this, THE_GAME_CONTEXT.arcadeKeys());
            GameActions2D.bindFallbackPlayerControlActions(this);
        }
        registerGameActionKeyBindings();
        THE_GAME_CONTROLLER.game().level().ifPresent(level -> {
            gr.setWorldMap(level.worldMap());
            gr.setMessagePosition(centerPositionBelowHouse(level));
        });
    }

    @Override
    public void onGameStarted(GameEvent e) {
        boolean silent = THE_GAME_CONTROLLER.game().isDemoLevel() ||
                THE_GAME_CONTROLLER.state() == TESTING_LEVELS ||
                THE_GAME_CONTROLLER.state() == TESTING_LEVEL_TEASERS;
        if (!silent) {
            THE_SOUND.playGameReadySound();
        }
    }

    @Override
    protected void doEnd() {
        THE_SOUND.stopAll();
    }

    @Override
    public void update() {
        THE_GAME_CONTROLLER.game().level().ifPresentOrElse(level -> {
            /* TODO: I would like to do this only on level start but when scene view is switched
                between 2D and 3D, the other scene has to be updated accordingly. */
            if (THE_GAME_CONTROLLER.game().isDemoLevel()) {
                THE_GAME_CONTROLLER.game().assignDemoLevelBehavior(level);
            }
            else {
                level.pac().setUsingAutopilot(PY_AUTOPILOT.get());
                level.pac().setImmune(PY_IMMUNITY.get());
                updateSound(level, THE_SOUND);
            }
            if (THE_GAME_CONTROLLER.state() == GameState.LEVEL_COMPLETE) {
                levelCompleteAnimation.update();
            }
        }, () -> { // Scene is already visible 2 ticks before game level is created!
            Logger.warn("Tick {}: Game level not yet available", THE_CLOCK.tickCount());
        });
    }

    private void updateSound(GameLevel level, GameSound sound) {
        if (THE_GAME_CONTROLLER.state() == GameState.HUNTING && !level.powerTimer().isRunning()) {
            int sirenNumber = 1 + THE_GAME_CONTROLLER.game().huntingTimer().phaseIndex() / 2;
            sound.selectSiren(sirenNumber);
            sound.playSiren();
        }
        if (level.pac().starvingTicks() > 8) { // TODO not sure how to do this right
            sound.stopMunchingSound();
        }
        boolean ghostsReturning = level.ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).anyMatch(Ghost::isVisible);
        if (level.pac().isAlive() && ghostsReturning) {
            sound.playGhostReturningHomeSound();
        } else {
            sound.stopGhostReturningHomeSound();
        }
    }

    @Override
    public Vector2f sizeInPx() {
        return THE_GAME_CONTEXT.worldSizeInTilesOrElse(ARCADE_MAP_SIZE_IN_TILES).scaled(TS).toVector2f();
    }

    @Override
    protected void drawSceneContent() {
        GameLevel level = THE_GAME_CONTROLLER.game().level().orElse(null);
        if (level == null) { // This happens on level start
            Logger.warn("Tick {}: Cannot draw scene content: Game level not yet available!", THE_CLOCK.tickCount());
            return;
        }

        // Draw message
        if (level.message() != null) {
            Color color = null;
            String text = null;
            switch (level.message()) {
                case GAME_OVER -> {
                    color = Color.web(Arcade.Palette.RED);
                    text = "GAME  OVER";
                }
                case READY -> {
                    color = Color.web(Arcade.Palette.YELLOW);
                    text = "READY!";
                }
                case TEST_LEVEL -> {
                    color = Color.web(Arcade.Palette.WHITE);
                    text = "TEST    L%03d".formatted(level.number());
                }
            }
            gr.setMessagePosition(centerPositionBelowHouse(level));
            // this assumes fixed width font of one tile size:
            double x = gr.getMessagePosition().x() - (text.length() * HTS);
            gr.drawText(text, color, gr.scaledArcadeFont(TS), x, gr.getMessagePosition().y());
        }

        // Draw maze
        boolean highlighted = levelCompleteAnimation != null && levelCompleteAnimation.isInHighlightPhase();
        gr.setMazeHighlighted(highlighted);
        gr.setBlinking(level.blinking().isOn());
        gr.setWorldMap(level.worldMap()); //TODO fixme: avoid calling this in every frame
        gr.drawGameLevel(level, 0, 3 * TS);

        // Draw bonus
        level.bonus().ifPresent(gr::drawBonus);

        // Draw guys and debug info if activated
        gr.drawAnimatedActor(level.pac());
        ghostsInZOrder(level).forEach(gr::drawAnimatedActor);
        if (debugInfoVisiblePy.get()) {
            gr.drawAnimatedCreatureInfo(level.pac());
            ghostsInZOrder(level).forEach(gr::drawAnimatedCreatureInfo);
        }

        // Draw lives counter or remaining credit
        if (THE_GAME_CONTROLLER.game().canStartNewGame()) {
            //TODO: this code is ugly
            int numLivesShown = THE_GAME_CONTROLLER.game().lives() - 1;
            if (THE_GAME_CONTROLLER.state() == GameState.STARTING_GAME && !level.pac().isVisible()) {
                numLivesShown += 1;
            }
            gr.drawLivesCounter(numLivesShown, 5, 2 * TS, sizeInPx().y() - 2 * TS);
        } else {
            int credit = THE_GAME_CONTROLLER.credit;
            gr.drawText("CREDIT %2d".formatted(credit), Color.web(Arcade.Palette.WHITE), gr.scaledArcadeFont(TS),
                    2 * TS, sizeInPx().y() - 2);
        }

        // Draw level counter
        gr.drawLevelCounter(sizeInPx().x() - 4 * TS, sizeInPx().y() - 2 * TS);
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
        GraphicsContext g = gr.ctx();
        gr.drawTileGrid(sizeInPx().x(), sizeInPx().y());

        if (THE_GAME_CONTEXT.gameVariant() == GameVariant.PACMAN) {
            THE_GAME_CONTROLLER.game().level().ifPresent(level -> {
                level.ghosts().forEach(ghost -> {
                    // Are currently the same for each ghost, but who knows what comes...
                    ghost.specialTerrainTiles().forEach(tile -> {
                        g.setFill(Color.RED);
                        double x = scaled(tile.x() * TS), y = scaled(tile.y() * TS + HTS), size = scaled(TS);
                        g.fillRect(x, y, size, 2);
                    });
                });
            });
        }
        g.setFill(Color.YELLOW);
        g.setFont(GameRenderer.DEBUG_FONT);
        String gameStateText = THE_GAME_CONTROLLER.state().name() + " (Tick %d)".formatted(THE_GAME_CONTROLLER.state().timer().tickCount());
        String scatterChaseText = "";
        if (THE_GAME_CONTROLLER.state() == GameState.HUNTING) {
            HuntingTimer huntingTimer = THE_GAME_CONTROLLER.game().huntingTimer();
            scatterChaseText = " %s (Tick %d)".formatted(huntingTimer.huntingPhase(), huntingTimer.tickCount());
        }
        g.fillText("%s%s".formatted(gameStateText, scatterChaseText), 0, 64);
    }

    @Override
    public void onSceneVariantSwitch(GameScene oldScene) {
        Logger.info("{} entered from {}", this, oldScene);
        bindGameActions();
        registerGameActionKeyBindings();
        if (gr == null) {
            setGameRenderer(THE_GAME_CONTEXT.gameConfiguration().createRenderer(canvas));
        }
        THE_GAME_CONTROLLER.game().level().map(GameLevel::worldMap).ifPresent(gr::setWorldMap);
    }

    @Override
    public void onEnterGameState(GameState state) {
        if (state == GameState.GAME_OVER) {
            THE_SOUND.playGameOverSound();
        }
        else if (state == GameState.LEVEL_COMPLETE) {
            THE_GAME_CONTROLLER.game().level().ifPresent(level -> {
                levelCompleteAnimation = new LevelCompleteAnimation(level.numFlashes(), 10);
                levelCompleteAnimation.setOnHideGhosts(() -> level.ghosts().forEach(Ghost::hide));
                levelCompleteAnimation.setOnFinished(() -> state.timer().expire());
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
        items.add(Ufx.contextMenuTitleItem(THE_ASSETS.localizedText("pacman")));

        var miAutopilot = new CheckMenuItem(THE_ASSETS.localizedText("autopilot"));
        miAutopilot.selectedProperty().bindBidirectional(PY_AUTOPILOT);
        items.add(miAutopilot);

        var miImmunity = new CheckMenuItem(THE_ASSETS.localizedText("immunity"));
        miImmunity.selectedProperty().bindBidirectional(PY_IMMUNITY);
        items.add(miImmunity);

        items.add(new SeparatorMenuItem());

        var miMuted = new CheckMenuItem(THE_ASSETS.localizedText("muted"));
        miMuted.selectedProperty().bindBidirectional(THE_SOUND.mutedProperty());
        items.add(miMuted);

        var miQuit = new MenuItem(THE_ASSETS.localizedText("quit"));
        miQuit.setOnAction(ae -> GameActions2D.SHOW_START_PAGE.execute());
        items.add(miQuit);

        return items;
    }
}