/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.common;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.ui2d.GameActions2D;
import de.amr.games.pacman.ui2d.GameAssets2D;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.tinylog.Logger;

import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.model.pacman.PacManArcadeGame.ARCADE_MAP_SIZE_IN_TILES;
import static de.amr.games.pacman.ui2d.GameAssets2D.ARCADE_PALE;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.PY_AUTOPILOT;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.PY_IMMUNITY;
import static de.amr.games.pacman.ui2d.util.KeyInput.only;

/**
 * 2D play scene for all game variants except Tengen Ms. Pac-Man.
 *
 * @author Armin Reichert
 */
public class PlayScene2D extends GameScene2D {

    static final Font DEBUG_STATE_FONT = Font.font("Sans", FontWeight.BOLD, 24);

    @Override
    public void bindGameActions() {
        if (context.game().isDemoLevel()) {
            bind(GameActions2D.ADD_CREDIT, only(KeyCode.DIGIT5), only(KeyCode.NUMPAD5));
        } else {
            GameActions2D.bindCheatActions(this);
            GameActions2D.bindDefaultPlayerControlActions(this);
            GameActions2D.bindFallbackPlayerControlActions(this);
        }
    }

    @Override
    protected void doEnd() {
        context.sound().stopAll();
    }

    @Override
    public void update() {
        if (context.game().currentLevelNumber() == 0) {
            // scene may be visible for 2 ticks before game level has been created
            Logger.warn("Cannot update PlayScene2D: game level not (yet) available");
            return;
        }
        if (context.game().isDemoLevel()) {
            context.setScoreVisible(false);
            context.game().pac().setUsingAutopilot(true);
            context.game().pac().setImmune(false);
        } else {
            context.setScoreVisible(true);
            context.game().pac().setUsingAutopilot(PY_AUTOPILOT.get());
            context.game().pac().setImmune(PY_IMMUNITY.get());
            updatePlaySceneSound();
        }
    }

    private void updatePlaySceneSound() {
        if (context.gameState() == GameState.HUNTING && !context.game().powerTimer().isRunning()) {
            int sirenNumber = 1 + context.game().huntingControl().phaseIndex() / 2;
            context.sound().selectSiren(sirenNumber);
            context.sound().playSiren();
        }
        if (context.game().pac().starvingTicks() > 8) { // TODO not sure how to do this right
            context.sound().stopMunchingSound();
        }
        boolean ghostsReturning = context.game().ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).anyMatch(Ghost::isVisible);
        if (context.game().pac().isAlive() && ghostsReturning) {
            context.sound().playGhostReturningHomeSound();
        } else {
            context.sound().stopGhostReturningHomeSound();
        }
    }

    @Override
    public Vector2f size() {
        return context.worldSizeInTilesOrElse(ARCADE_MAP_SIZE_IN_TILES).scaled(TS).toVector2f();
    }

    @Override
    protected void drawSceneContent(GameRenderer renderer) {
        if (context.game().world() == null) { // This happens on level start
            Logger.warn("Cannot draw scene content, game world not yet available!");
            return;
        }
        drawLevelMessage(renderer); // READY, GAME_OVER etc.

        boolean flashMode = Boolean.TRUE.equals(context.gameState().getProperty("mazeFlashing"));
        renderer.setFlashMode(flashMode);
        renderer.setBlinkingOn(context.game().blinking().isOn());
        renderer.drawWorld(context, context.game().world());

        renderer.drawAnimatedEntity(context.game().pac());
        ghostsInZOrder().forEach(renderer::drawAnimatedEntity);

        if (debugInfoPy.get()) {
            renderer.drawAnimatedCreatureInfo(context.game().pac());
            ghostsInZOrder().forEach(renderer::drawAnimatedCreatureInfo);
        }

        if (context.game().canStartNewGame()) {
            //TODO: this code is ugly
            int numLivesShown = context.game().lives() - 1;
            if (context.gameState() == GameState.STARTING_GAME && !context.game().pac().isVisible()) {
                numLivesShown += 1;
            }
            renderer.drawLivesCounter(numLivesShown, 5, size());
        } else {
            int credit = context.gameController().coinControl().credit();
            renderer.drawText("CREDIT %2d".formatted(credit), ARCADE_PALE, renderer.scaledArcadeFont(TS), 2 * TS, size().y() - 2);
        }
        renderer.drawLevelCounter(context, size());
    }

    private Stream<Ghost> ghostsInZOrder() {
        return Stream.of(GameModel.ORANGE_GHOST, GameModel.CYAN_GHOST, GameModel.PINK_GHOST, GameModel.RED_GHOST)
            .map(context.game()::ghost);
    }

    private void drawLevelMessage(GameRenderer renderer) {
        Vector2i houseTopLeftTile = context.game().world().houseTopLeftTile();
        Vector2i houseSize        = context.game().world().houseSize();
        int cx = houseTopLeftTile.x() + houseSize.x() / 2;
        int y = TS * (houseTopLeftTile.y() + houseSize.y() + 1);
        String assetPrefix = GameAssets2D.assetPrefix(context.gameVariant());
        Font font = renderer.scaledArcadeFont(TS);
        if (context.game().isDemoLevel()) {
            String text = "GAME  OVER";
            int x = TS * (cx - text.length() / 2);
            Color color = context.assets().color(assetPrefix + ".color.game_over_message");
            renderer.drawText(text, color, font, x, y);
        } else if (context.gameState() == GameState.GAME_OVER) {
            String text = "GAME  OVER";
            int x = TS * (cx - text.length() / 2);
            Color color = context.assets().color(assetPrefix + ".color.game_over_message");
            renderer.drawText(text, color, font, x, y);
        } else if (context.gameState() == GameState.STARTING_GAME) {
            String text = "READY!";
            int x = TS * (cx - text.length() / 2);
            Color color = context.assets().color(assetPrefix + ".color.ready_message");
            renderer.drawText(text, color, font, x, y);
        } else if (context.gameState() == GameState.TESTING_LEVEL_BONI) {
            String text = "TEST    L%03d".formatted(context.game().currentLevelNumber());
            int x = TS * (cx - text.length() / 2);
            renderer.drawText(text, GameAssets2D.ARCADE_PALE, font, x, y);
        }
    }

    @Override
    protected void drawDebugInfo(GameRenderer renderer) {
        GraphicsContext g = renderer.ctx();
        renderer.drawTileGrid(size());
        if (context.gameVariant() == GameVariant.PACMAN && context.game().world() != null) {
            context.game().ghosts().forEach(ghost -> {
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
        g.fillText(String.format("%s %d", context.gameState(), context.gameState().timer().currentTick()), 0, 64);
    }

    @Override
    public void onSceneVariantSwitch(GameScene oldScene) {
        Logger.info("{} entered from {}", this, oldScene);
        //TODO check this
        context.updateRenderer();
        bindGameActions();
        registerGameActionKeyBindings(context.keyboard());
    }

    @Override
    public void onEnterGameState(GameState state) {
        if (state == GameState.GAME_OVER) {
            context.sound().playGameOverSound();
        }
    }

    @Override
    public void onBonusEaten(GameEvent e) {
        context.sound().playBonusEatenSound();
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
    public void onLevelCreated(GameEvent e) {
        context.updateRenderer();
    }

    @Override
    public void onPacDied(GameEvent e) {
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