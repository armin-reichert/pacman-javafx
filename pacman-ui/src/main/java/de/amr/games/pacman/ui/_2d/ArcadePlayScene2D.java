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
import de.amr.games.pacman.ui.input.ArcadeKeyBinding;
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
import static de.amr.games.pacman.ui.Globals.THE_UI;
import static de.amr.games.pacman.ui._2d.GlobalProperties2d.PY_AUTOPILOT;
import static de.amr.games.pacman.ui._2d.GlobalProperties2d.PY_IMMUNITY;

/**
 * 2D play scene for Arcade game variants.
 *
 * @author Armin Reichert
 */
public class ArcadePlayScene2D extends GameScene2D {

    private LevelCompleteAnimation levelCompleteAnimation;

    private ArcadeKeyBinding arcadeKeyBinding() { return THE_UI.keyboard().currentArcadeKeyBinding(); }

    @Override
    public void bindGameActions() {}

    @Override
    protected void doInit() {
        game().setScoreVisible(true);
        bindDefaultArcadeControllerActions(THE_UI.keyboard().currentArcadeKeyBinding());
        bindAlternativePlayerControlActions();
        enableActionBindings();
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        if (game().isDemoLevel()) {
            bind(GameActions.INSERT_COIN, arcadeKeyBinding().key(Arcade.Button.COIN));
        } else {
            bindCheatActions();
            bindDefaultArcadeControllerActions(arcadeKeyBinding());
            bindAlternativePlayerControlActions();
        }
        enableActionBindings();

        game().level().ifPresent(level -> {
            gr.setWorldMap(level.worldMap());
            gr.setMessagePosition(centerPositionBelowHouse(level));
        });
    }

    @Override
    public void onGameStarted(GameEvent e) {
        boolean silent = game().isDemoLevel() || gameState() == TESTING_LEVELS || gameState() == TESTING_LEVEL_TEASERS;
        if (!silent) {
            THE_UI.sound().playGameReadySound();
        }
    }

    @Override
    protected void doEnd() {
        THE_UI.sound().stopAll();
        disableActionBindings();
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
            Logger.warn("Tick {}: Game level not yet available", THE_UI.clock().tickCount());
        });
    }

    private void updateSound(GameLevel level) {
        if (gameState() == GameState.HUNTING && !level.powerTimer().isRunning()) {
            int sirenNumber = 1 + game().huntingTimer().phaseIndex() / 2;
            THE_UI.sound().selectSiren(sirenNumber);
            THE_UI.sound().playSiren();
        }
        if (level.pac().starvingTicks() > 8) { // TODO not sure how to do this right
            THE_UI.sound().stopMunchingSound();
        }
        boolean ghostsReturning = level.ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).anyMatch(Ghost::isVisible);
        if (level.pac().isAlive() && ghostsReturning) {
            THE_UI.sound().playGhostReturningHomeSound();
        } else {
            THE_UI.sound().stopGhostReturningHomeSound();
        }
    }

    @Override
    public Vector2f sizeInPx() {
        return levelSizeInTilesOrElse(ARCADE_MAP_SIZE_IN_TILES).scaled(TS).toVector2f();
    }

    @Override
    protected void drawSceneContent() {
        GameLevel level = game().level().orElse(null);

        // Scene is drawn already for 2 ticks before level has been created
        if (level == null) {
            Logger.warn("Tick {}: Game level not yet available, scene content not drawn", THE_UI.clock().tickCount());
            return;
        }

        // Draw maze
        gr.setMazeHighlighted(levelCompleteAnimation != null && levelCompleteAnimation.isInHighlightPhase());
        gr.setBlinking(level.blinking().isOn());
        gr.setWorldMap(level.worldMap());
        gr.drawMaze(level, 0, 3 * TS, backgroundColor());

        if (level.message() != null) {
            drawLevelMessage(level);
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
            int numLivesShown = game().lives() - 1;
            if (gameState() == GameState.STARTING_GAME && !level.pac().isVisible()) {
                numLivesShown += 1;
            }
            gr.drawLivesCounter(numLivesShown, 5, 2 * TS, sizeInPx().y() - 2 * TS);
        } else {
            gr.drawText("CREDIT %2d".formatted(THE_GAME_CONTROLLER.credit),
                Color.web(Arcade.Palette.WHITE), gr.scaledArcadeFont(TS), 2 * TS, sizeInPx().y() - 2);
        }
        gr.drawLevelCounter(sizeInPx().x() - 4 * TS, sizeInPx().y() - 2 * TS);
    }

    private void drawLevelMessage(GameLevel level) {
        switch (level.message()) {
            case GAME_OVER -> {
                String text = "GAME  OVER";
                // this assumes fixed font width of one tile:
                double x = gr.getMessagePosition().x() - (text.length() * HTS);
                gr.setMessagePosition(centerPositionBelowHouse(level));
                gr.drawText(text, Color.web(Arcade.Palette.RED), gr.scaledArcadeFont(TS), x, gr.getMessagePosition().y());
            }
            case READY -> {
                String text = "READY!";
                // this assumes fixed font width of one tile:
                double x = gr.getMessagePosition().x() - (text.length() * HTS);
                gr.setMessagePosition(centerPositionBelowHouse(level));
                gr.drawText(text, Color.web(Arcade.Palette.YELLOW), gr.scaledArcadeFont(TS), x, gr.getMessagePosition().y());
            }
            case TEST_LEVEL -> {
                String text = "TEST    L%03d".formatted(level.number());
                // this assumes fixed font width of one tile:
                double x = gr.getMessagePosition().x() - (text.length() * HTS);
                gr.setMessagePosition(centerPositionBelowHouse(level));
                gr.drawText(text, Color.web(Arcade.Palette.WHITE), gr.scaledArcadeFont(TS), x, gr.getMessagePosition().y());
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
        GraphicsContext g = gr.ctx();
        gr.drawTileGrid(sizeInPx().x(), sizeInPx().y());
        if (THE_GAME_CONTROLLER.isGameVariantSelected(GameVariant.PACMAN)) {
            game().level().ifPresent(level ->
                level.ghosts().forEach(ghost ->
                    ghost.specialTerrainTiles().forEach(tile -> {
                        double x = scaled(tile.x() * TS), y = scaled(tile.y() * TS + HTS), size = scaled(TS);
                        g.setFill(Color.RED);
                        g.fillRect(x, y, size, 2);
                    }))
            );
        }
        g.setFill(Color.YELLOW);
        g.setFont(GameRenderer.DEBUG_FONT);
        String gameStateText = gameState().name() + " (Tick %d)".formatted(gameState().timer().tickCount());
        String huntingPhaseText = "";
        if (gameState() == GameState.HUNTING) {
            HuntingTimer huntingTimer = game().huntingTimer();
            huntingPhaseText = " %s (Tick %d)".formatted(huntingTimer.huntingPhase(), huntingTimer.tickCount());
        }
        g.fillText("%s%s".formatted(gameStateText, huntingPhaseText), 0, 64);
    }

    @Override
    public void onSceneVariantSwitch(GameScene oldScene) {
        Logger.info("{} entered from {}", this, oldScene);
        bindGameActions();
        enableActionBindings();
        if (gr == null) {
            setGameRenderer(THE_UI.configurations().current().createRenderer(canvas));
        }
        game().level().map(GameLevel::worldMap).ifPresent(gr::setWorldMap);
    }

    @Override
    public void onEnterGameState(GameState state) {
        if (state == GameState.GAME_OVER) {
            THE_UI.sound().playGameOverSound();
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
        THE_UI.sound().playBonusBouncingSound();
    }

    @Override
    public void onBonusEaten(GameEvent e) {
        THE_UI.sound().stopBonusBouncingSound();
        THE_UI.sound().playBonusEatenSound();
    }

    @Override
    public void onBonusExpired(GameEvent e) {
        THE_UI.sound().stopBonusBouncingSound();
    }

    @Override
    public void onCreditAdded(GameEvent e) {
        THE_UI.sound().playInsertCoinSound();
    }

    @Override
    public void onExtraLifeWon(GameEvent e) {
        THE_UI.sound().playExtraLifeSound();
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        THE_UI.sound().playGhostEatenSound();
    }

    @Override
    public void onPacDying(GameEvent e) {
        THE_UI.sound().playPacDeathSound();
    }

    @Override
    public void onPacFoundFood(GameEvent e) {
        THE_UI.sound().playMunchingSound();
    }

    @Override
    public void onPacGetsPower(GameEvent e) {
        THE_UI.sound().stopSiren();
        THE_UI.sound().playPacPowerSound();
    }

    @Override
    public void onPacLostPower(GameEvent e) {
        THE_UI.sound().stopPacPowerSound();
    }

    @Override
    public List<MenuItem> supplyContextMenuItems(ContextMenuEvent e) {
        List<MenuItem> items = new ArrayList<>();
        items.add(Ufx.contextMenuTitleItem(THE_UI.assets().text("pacman")));

        var miAutopilot = new CheckMenuItem(THE_UI.assets().text("autopilot"));
        miAutopilot.selectedProperty().bindBidirectional(PY_AUTOPILOT);
        items.add(miAutopilot);

        var miImmunity = new CheckMenuItem(THE_UI.assets().text("immunity"));
        miImmunity.selectedProperty().bindBidirectional(PY_IMMUNITY);
        items.add(miImmunity);

        items.add(new SeparatorMenuItem());

        var miMuted = new CheckMenuItem(THE_UI.assets().text("muted"));
        miMuted.selectedProperty().bindBidirectional(THE_UI.sound().mutedProperty());
        items.add(miMuted);

        var miQuit = new MenuItem(THE_UI.assets().text("quit"));
        miQuit.setOnAction(ae -> GameActions.SHOW_START_PAGE.execute());
        items.add(miQuit);

        return items;
    }
}