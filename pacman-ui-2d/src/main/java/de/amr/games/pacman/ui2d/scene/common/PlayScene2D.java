/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.common;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.ui2d.GameActions2D;
import de.amr.games.pacman.ui2d.input.ArcadeKeyAdapter;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.scene.pacman_xxl.PacManGameXXLRenderer;
import de.amr.games.pacman.ui2d.sound.GameSound;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.tinylog.Logger;

import java.util.stream.Stream;

import static de.amr.games.pacman.controller.GameState.TESTING_LEVEL_BONI;
import static de.amr.games.pacman.controller.GameState.TESTING_LEVEL_TEASERS;
import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.pacman.PacManGame.ARCADE_MAP_SIZE_IN_TILES;
import static de.amr.games.pacman.ui2d.GameActions2D.*;
import static de.amr.games.pacman.ui2d.GameAssets2D.assetPrefix;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.PY_AUTOPILOT;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.PY_IMMUNITY;

/**
 * 2D play scene for all game variants except Tengen Ms. Pac-Man.
 *
 * @author Armin Reichert
 */
public class PlayScene2D extends GameScene2D {

    static final Font DEBUG_STATE_FONT = Font.font("Sans", FontWeight.BOLD, 24);

    private LevelCompleteAnimation levelCompleteAnimation;

    @Override
    public void bindGameActions() {}

    @Override
    protected void doInit() {
        context.setScoreVisible(true);
        ArcadeKeyAdapter arcadeController = context.arcade();
        bindDefaultArcadeControllerActions(this, arcadeController);
        bindFallbackPlayerControlActions(this);
        registerGameActionKeyBindings(context.keyboard());
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        ArcadeKeyAdapter arcadeController = context.arcade();
        if (context.game().isDemoLevel()) {
            bind(GameActions2D.ADD_CREDIT, arcadeController.keyCombination(Arcade.Controls.COIN));
        } else {
            bindCheatActions(this);
            bindDefaultArcadeControllerActions(this, arcadeController);
            bindFallbackPlayerControlActions(this);
        }
        registerGameActionKeyBindings(context.keyboard());
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
    protected void drawSceneContent(GameRenderer renderer) {
        if (context.game().level().isEmpty()) { // This happens on level start
            Logger.warn("Tick {}: Cannot draw scene content: game world not yet available!", context.tick());
            return;
        }

        //TODO use more general solution, maybe terrain map property?
        if (context.gameVariant() == GameVariant.PACMAN_XXL) {
            PacManGameXXLRenderer r = (PacManGameXXLRenderer) renderer;
            r.setMessageAnchorPosition(centerBelowHouse(context.level().world()));
        }
        drawLevelMessage(renderer); // READY, GAME_OVER etc.

        boolean flashMode = levelCompleteAnimation != null && levelCompleteAnimation.isHighlightMaze();
        renderer.setFlashMode(flashMode);
        renderer.setBlinking(context.level().blinking().isOn());
        renderer.drawWorld(context, context.level().world(), 0, 3 * TS);

        renderer.drawAnimatedEntity(context.level().pac());
        ghostsInZOrder().forEach(renderer::drawAnimatedEntity);

        if (debugInfoVisiblePy.get()) {
            renderer.drawAnimatedCreatureInfo(context.level().pac());
            ghostsInZOrder().forEach(renderer::drawAnimatedCreatureInfo);
        }

        if (context.game().canStartNewGame()) {
            //TODO: this code is ugly
            int numLivesShown = context.game().lives() - 1;
            if (context.gameState() == GameState.STARTING_GAME && !context.level().pac().isVisible()) {
                numLivesShown += 1;
            }
            renderer.drawLivesCounter(numLivesShown, 5, 2 * TS, size().y() - 2 * TS);
        } else {
            int credit = context.gameController().coinControl().credit();
            renderer.drawText("CREDIT %2d".formatted(credit), Color.valueOf(Arcade.Palette.WHITE), renderer.scaledArcadeFont(TS), 2 * TS, size().y() - 2);
        }
        renderer.drawLevelCounter(context, size().x() - 4 * TS, size().y() - 2 * TS);
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

    private void drawLevelMessage(GameRenderer renderer) {
        Color color = null;
        String text = null;
        if (context.game().isDemoLevel() || context.gameState() == GameState.GAME_OVER) {
            text = "GAME  OVER";
            String assetPrefix = assetPrefix(context.gameVariant());
            color = context.assets().color(assetPrefix + ".color.game_over_message");
        }
        else if (context.gameState() == GameState.STARTING_GAME) {
            text = "READY!";
            String assetPrefix = assetPrefix(context.gameVariant());
            color = context.assets().color(assetPrefix + ".color.ready_message");
        }
        else if (context.gameState() == GameState.TESTING_LEVEL_BONI) {
            text = "TEST    L%03d".formatted(context.level().number);
            color = Color.valueOf(Arcade.Palette.WHITE);
        }
        if (text != null) {
            float x = renderer.getMessageAnchorPosition().x(), y = renderer.getMessageAnchorPosition().y();
            double mx = x - (text.length() * HTS); // TODO this assumes fixed width font of one tile size
            Font font = renderer.scaledArcadeFont(TS);
            renderer.drawText(text, color, font, mx, y);
        }
    }

    @Override
    protected void drawDebugInfo(GameRenderer renderer) {
        GraphicsContext g = renderer.ctx();
        renderer.drawTileGrid(size().x(), size().y());
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
        g.setFont(DEBUG_STATE_FONT);
        g.fillText(String.format("%s %d", context.gameState(), context.gameState().timer().tickCount()), 0, 64);
    }

    @Override
    public void onSceneVariantSwitch(GameScene oldScene) {
        Logger.info("{} entered from {}", this, oldScene);
        bindGameActions();
        registerGameActionKeyBindings(context.keyboard());
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