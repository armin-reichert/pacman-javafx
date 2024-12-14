/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.scene;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.ui.action.GameActions2D;
import de.amr.games.pacman.ui.assets.GameSound;
import de.amr.games.pacman.ui.input.ArcadeKeyBinding;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.stream.Stream;

import static de.amr.games.pacman.controller.GameState.TESTING_LEVEL_BONI;
import static de.amr.games.pacman.controller.GameState.TESTING_LEVEL_TEASERS;
import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_TILES;
import static de.amr.games.pacman.ui.GameContext.assetPrefix;
import static de.amr.games.pacman.ui.GlobalProperties.PY_AUTOPILOT;
import static de.amr.games.pacman.ui.GlobalProperties.PY_IMMUNITY;

/**
 * 2D play scene for all game variants except Tengen Ms. Pac-Man.
 *
 * @author Armin Reichert
 */
public class PlayScene2D extends GameScene2D {

    private LevelCompleteAnimation levelCompleteAnimation;

    @Override
    public void bindGameActions() {}

    @Override
    protected void doInit() {
        context.setScoreVisible(true);
        ArcadeKeyBinding arcadeController = context.arcadeKeys();
        GameActions2D.bindDefaultArcadeControllerActions(this, arcadeController);
        GameActions2D.bindFallbackPlayerControlActions(this);
        registerGameActionKeyBindings(context.keyboard());
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        ArcadeKeyBinding arcadeController = context.arcadeKeys();
        if (context.game().isDemoLevel()) {
            bind(GameActions2D.INSERT_COIN, arcadeController.key(Arcade.Button.COIN));
        } else {
            GameActions2D.bindCheatActions(this);
            GameActions2D.bindDefaultArcadeControllerActions(this, arcadeController);
            GameActions2D.bindFallbackPlayerControlActions(this);
        }
        registerGameActionKeyBindings(context.keyboard());
        gr.setWorldMap(context.level().world().map());
    }

    @Override
    public void onGameStarted(GameEvent e) {
        boolean silent = context.game().isDemoLevel() ||
                context.gameState() == TESTING_LEVEL_BONI ||
                context.gameState() == TESTING_LEVEL_TEASERS;
        if (!silent) {
            context.sound().playGameReadySound();
        }
    }

    @Override
    protected void doEnd() {
        context.sound().stopAll();
    }

    @Override
    public void update() {
        if (context.game().level().isEmpty()) {
            // Scene is already visible for 2 ticks before game level has been created
            Logger.warn("Tick {}: Cannot update PlayScene2D: game level not yet available", context.tick());
            return;
        }
        /* TODO: I would like to do this only on level start but when scene view is switched
                 between 2D and 3D, the other scene has to be updated accordingly. */
        if (context.game().isDemoLevel()) {
            context.game().setDemoLevelBehavior();
        }
        else {
            context.level().pac().setUsingAutopilot(PY_AUTOPILOT.get());
            context.level().pac().setImmune(PY_IMMUNITY.get());
            updateSound(context.sound());
        }
        if (context.gameState() == GameState.LEVEL_COMPLETE) {
            levelCompleteAnimation.update();
        }
    }

    private void updateSound(GameSound sound) {
        if (context.gameState() == GameState.HUNTING && !context.level().powerTimer().isRunning()) {
            int sirenNumber = 1 + context.game().huntingControl().phaseIndex() / 2;
            sound.selectSiren(sirenNumber);
            sound.playSiren();
        }
        if (context.level().pac().starvingTicks() > 8) { // TODO not sure how to do this right
            sound.stopMunchingSound();
        }
        boolean ghostsReturning = context.level().ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).anyMatch(Ghost::isVisible);
        if (context.level().pac().isAlive() && ghostsReturning) {
            sound.playGhostReturningHomeSound();
        } else {
            sound.stopGhostReturningHomeSound();
        }
    }

    @Override
    public Vector2f size() {
        return context.worldSizeInTilesOrElse(ARCADE_MAP_SIZE_IN_TILES).scaled(TS).toVector2f();
    }

    @Override
    protected void drawSceneContent() {
        if (context.game().level().isEmpty()) { // This happens on level start
            Logger.warn("Tick {}: Cannot draw scene content: game world not yet available!", context.tick());
            return;
        }
        GameLevel level = context.level();

        //TODO fixme
        gr.setWorldMap(level.world().map());

        //TODO fixme and use more general solution, maybe terrain map property?
        /*
        if (context.gameVariant() == GameVariant.PACMAN_XXL) {
            PacManGameXXLRenderer r = (PacManGameXXLRenderer) gr;
            r.setMessageAnchorPosition(centerBelowHouse(level.world()));
        }
         */

        drawLevelMessage(level.message());

        boolean flashMode = levelCompleteAnimation != null && levelCompleteAnimation.isInHighlightPhase();
        gr.setFlashMode(flashMode);
        gr.setBlinking(level.blinking().isOn());
        gr.drawWorld(level.world(), 0, 3 * TS);
        level.bonus().ifPresent(gr::drawBonus);

        gr.drawAnimatedEntity(level.pac());
        ghostsInZOrder().forEach(gr::drawAnimatedEntity);

        if (debugInfoVisiblePy.get()) {
            gr.drawAnimatedCreatureInfo(level.pac());
            ghostsInZOrder().forEach(gr::drawAnimatedCreatureInfo);
        }

        if (context.game().canStartNewGame()) {
            //TODO: this code is ugly
            int numLivesShown = context.game().lives() - 1;
            if (context.gameState() == GameState.STARTING_GAME && !level.pac().isVisible()) {
                numLivesShown += 1;
            }
            gr.drawLivesCounter(numLivesShown, 5, 2 * TS, size().y() - 2 * TS);
        } else {
            int credit = context.gameController().coinControl().credit();
            gr.drawText("CREDIT %2d".formatted(credit), Color.valueOf(Arcade.Palette.WHITE), gr.scaledArcadeFont(TS), 2 * TS, size().y() - 2);
        }
        gr.drawLevelCounter(context, size().x() - 4 * TS, size().y() - 2 * TS);
    }

    private Vector2f centerBelowHouse(GameWorld world) {
        Vector2i houseTopLeft = world.houseTopLeftTile(), houseSize = world.houseSize();
        float x = TS * (houseTopLeft.x() + houseSize.x() * 0.5f);
        float y = TS * (houseTopLeft.y() + houseSize.y() + 1);
        return new Vector2f(x, y);
    }

    private Stream<Ghost> ghostsInZOrder() {
        return Stream.of(GameModel.ORANGE_GHOST, GameModel.CYAN_GHOST, GameModel.PINK_GHOST, GameModel.RED_GHOST)
            .map(context.level()::ghost);
    }

    private void drawLevelMessage(GameLevel.Message message) {
        if (message != null) {
            String assetPrefix = assetPrefix(context.gameVariant());
            Color color = null;
            String text = null;
            switch (message.type()) {
                case GAME_OVER -> {
                    color = context.assets().color(assetPrefix + ".color.game_over_message");
                    text = "GAME  OVER";
                }
                case READY -> {
                    color = context.assets().color(assetPrefix + ".color.ready_message");
                    text = "READY!";
                }
                case TEST_LEVEL -> {
                    color = Color.valueOf(Arcade.Palette.WHITE);
                    text = "TEST    L%03d".formatted(context.level().number);
                }
            }
            // this assumes fixed width font of one tile size
            double x = gr.getMessageAnchorPosition().x() - (text.length() * HTS);
            gr.drawText(text, color, gr.scaledArcadeFont(TS), x, gr.getMessageAnchorPosition().y());
        }
    }

    @Override
    protected void drawDebugInfo() {
        GraphicsContext g = gr.ctx();
        gr.drawTileGrid(size().x(), size().y());
        if (context.gameVariant() == GameVariant.PACMAN && context.game().level().isPresent()) {
            context.level().ghosts().forEach(ghost -> {
                // Are currently the same for each ghost, but who knows what comes...
                ghost.specialTerrainTiles().forEach(tile -> {
                    g.setFill(Color.RED);
                    double x = scaled(tile.x() * TS), y = scaled(tile.y() * TS + HTS), size = scaled(TS);
                    g.fillRect(x, y, size, 2);
                });
            });
        }
        g.setFill(Color.YELLOW);
        g.setFont(DEBUG_FONT);
        g.fillText(String.format("%s %d", context.gameState(), context.gameState().timer().tickCount()), 0, 64);
    }

    @Override
    public void onSceneVariantSwitch(GameScene oldScene) {
        Logger.info("{} entered from {}", this, oldScene);
        bindGameActions();
        registerGameActionKeyBindings(context.keyboard());
        if (gr == null) {
            setGameRenderer(context.currentGameSceneConfig().createRenderer(canvas));
        }
        gr.setWorldMap(context.level().world().map());
    }

    @Override
    public void onEnterGameState(GameState state) {
        if (state == GameState.GAME_OVER) {
            context.sound().playGameOverSound();
        }
        else if (state == GameState.LEVEL_COMPLETE) {
            levelCompleteAnimation = new LevelCompleteAnimation(context.level().numFlashes(), 10);
            levelCompleteAnimation.setOnHideGhosts(() -> context.level().ghosts().forEach(Ghost::hide));
            levelCompleteAnimation.setOnFinished(() -> state.timer().expire());
            levelCompleteAnimation.start();
        }
    }

    @Override
    public void onBonusActivated(GameEvent e) {
        context.sound().playBonusBouncingSound();
    }

    @Override
    public void onBonusEaten(GameEvent e) {
        context.sound().stopBonusBouncingSound();
        context.sound().playBonusEatenSound();
    }

    @Override
    public void onBonusExpired(GameEvent e) {
        context.sound().stopBonusBouncingSound();
    }

    @Override
    public void onCreditAdded(GameEvent e) {
        context.sound().playInsertCoinSound();
    }

    @Override
    public void onExtraLifeWon(GameEvent e) {
        context.sound().playExtraLifeSound();
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        context.sound().playGhostEatenSound();
    }

    @Override
    public void onPacDying(GameEvent e) {
        context.sound().playPacDeathSound();
    }

    @Override
    public void onPacFoundFood(GameEvent e) {
        context.sound().playMunchingSound();
    }

    @Override
    public void onPacGetsPower(GameEvent e) {
        context.sound().stopSiren();
        context.sound().playPacPowerSound();
    }

    @Override
    public void onPacLostPower(GameEvent e) {
        context.sound().stopPacPowerSound();
    }
}