/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.ui2d.GameAction;
import de.amr.games.pacman.ui2d.GameSounds;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.tinylog.Logger;

import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.PY_AUTOPILOT;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.PY_IMMUNITY;

/**
 * @author Armin Reichert
 */
public class PlayScene2D extends GameScene2D {

    @Override
    public boolean isCreditVisible() {
        return !context.game().hasCredit() || context.gameState() == GameState.GAME_OVER;
    }

    @Override
    public void end() {
        GameSounds.stopAll();
    }

    @Override
    public void update() {
        if (context.game().level().isEmpty()) {
            Logger.warn("Cannot update PlayScene2D: no game level exists");
            return;
        }
        if (context.game().isDemoLevel()) {
            context.game().pac().setUseAutopilot(true);
            context.game().pac().setImmune(false);
        } else {
            context.setScoreVisible(true);
            context.game().pac().setUseAutopilot(PY_AUTOPILOT.get());
            context.game().pac().setImmune(PY_IMMUNITY.get());
            updatePlaySceneSound();
        }
    }

    private void updatePlaySceneSound() {
        if (context.gameState() == GameState.HUNTING && !context.game().powerTimer().isRunning()) {
            int sirenNumber = 1 + context.game().huntingPhaseIndex() / 2;
            GameSounds.selectSiren(sirenNumber);
            GameSounds.playSiren();
        }
        if (context.game().pac().starvingTicks() > 8) { // TODO not sure how to do this right
            GameSounds.stopMunchingSound();
        }
        boolean ghostsReturning = context.game().ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).anyMatch(Ghost::isVisible);
        if (context.game().pac().isAlive() && ghostsReturning) {
            GameSounds.playGhostReturningHomeSound();
        } else {
            GameSounds.stopGhostReturningHomeSound();
        }
    }

    @Override
    public void handleInput() {
        if (GameAction.ADD_CREDIT.triggered() && context.game().isDemoLevel()) {
            context.addCredit();
        } else if (GameAction.CHEAT_EAT_ALL.triggered()) {
            context.cheatEatAllPellets();
        } else if (GameAction.CHEAT_ADD_LIVES.triggered()) {
            context.cheatAddLives();
        } else if (GameAction.CHEAT_NEXT_LEVEL.triggered()) {
            context.cheatEnterNextLevel();
        } else if (GameAction.CHEAT_KILL_GHOSTS.triggered()) {
            context.cheatKillAllEatableGhosts();
        }
    }

    @Override
    protected void drawSceneContent(GameWorldRenderer renderer) {
        if (context.game().world() == null) { // This happens on level start
            Logger.warn("Cannot draw scene content, game world not yet available!");
            return;
        }

        boolean flashMode = Boolean.TRUE.equals(context.gameState().getProperty("mazeFlashing"));
        renderer.setFlashMode(flashMode);
        renderer.setBlinkingOn(context.game().blinking().isOn());
        renderer.drawWorld(context.spriteSheet(), context, context.game().world());

        drawLevelMessage(renderer); // READY, GAME_OVER etc.

        renderer.drawAnimatedEntity(context.game().pac());
        ghostsInZOrder().forEach(renderer::drawAnimatedEntity);

        // Debug mode info
        if (debugInfoPy.get()) {
            renderer.drawAnimatedCreatureInfo(context.game().pac());
            ghostsInZOrder().forEach(renderer::drawAnimatedCreatureInfo);
        }

        if (!isCreditVisible()) {
            //TODO check this
            int numLivesDisplayed = context.game().lives() - 1;
            if (context.gameState() == GameState.READY && !context.game().pac().isVisible()) {
                numLivesDisplayed += 1;
            }
            renderer.drawLivesCounter(context.spriteSheet(), numLivesDisplayed, (context.worldSizeTilesOrDefault().y() - 2) * TS - 1);
        }
    }

    private Stream<Ghost> ghostsInZOrder() {
        return Stream.of(GameModel.ORANGE_GHOST, GameModel.CYAN_GHOST, GameModel.PINK_GHOST, GameModel.RED_GHOST)
            .map(context.game()::ghost);
    }

    private void drawLevelMessage(GameWorldRenderer renderer) {
        Vector2i houseOrigin = context.game().world().houseTopLeftTile();
        Vector2i houseSize = context.game().world().houseSize();
        int centerTileX = houseOrigin.x() + houseSize.x() / 2;
        double msgY = t(houseOrigin.y() + houseSize.y() + 1);
        // "GAME OVER" is drawn in demo mode and when game is over:
        if (context.game().isDemoLevel() || context.gameState() == GameState.GAME_OVER) {
            renderer.drawText("GAME  OVER", Color.RED, renderer.scaledArcadeFont(8), t(centerTileX - 5), msgY);
        } else {
            switch (context.gameState()) {
                case READY      -> renderer.drawText("READY!", Color.YELLOW, renderer.scaledArcadeFont(8), t(centerTileX - 3), msgY);
                case LEVEL_TEST -> renderer.drawText("TEST    L" + context.game().levelNumber(),
                    Color.YELLOW, renderer.scaledArcadeFont(8), t(8.5), msgY);
            }
        }
    }

    @Override
    protected void drawDebugInfo(GameWorldRenderer renderer) {
        Vector2i worldSize = context.worldSizeTilesOrDefault();
        renderer.drawTileGrid(worldSize.x(), worldSize.y());
        if (context.game().variant() == GameVariant.PACMAN && context.game().world() != null) {
            context.game().ghosts().forEach(ghost -> {
                // Are currently the same for each ghost, but who knows what comes...
                ghost.cannotMoveUpTiles().forEach(tile -> {
                    renderer.ctx().setFill(Color.RED);
                    renderer.ctx().fillOval(scaled(t(tile.x())), scaled(t(tile.y() - 1)), scaled(TS), scaled(TS));
                    renderer.ctx().setFill(Color.WHITE);
                    renderer.ctx().fillRect(scaled(t(tile.x()) + 1), scaled(t(tile.y()) - HTS - 1), scaled(TS - 2), scaled(2));
                });
            });
        }
        renderer.ctx().setFill(Color.YELLOW);
        renderer.ctx().setFont(Font.font("Sans", FontWeight.BOLD, 24));
        renderer.ctx().fillText(String.format("%s %d", context.gameState(), context.gameState().timer().currentTick()), 0, 64);
    }

    @Override
    public void onSceneVariantSwitch(GameScene oldScene) {
        int mapNumber = context.game().currentMapNumber();
        context.renderer().selectMap(context.game().world().map(), mapNumber, context.spriteSheet());
        Logger.info("{} entered from {}", this, oldScene);
    }

    @Override
    public void onGameStateEntry(GameState state) {
        switch (state) {
            case READY, LEVEL_COMPLETE, PACMAN_DYING -> GameSounds.stopAll();
            case GAME_OVER -> {
                GameSounds.stopAll();
                GameSounds.playGameOverSound();
            }
            default -> {}
        }
    }

    @Override
    public void onBonusEaten(GameEvent e) {
        GameSounds.playBonusEatenSound();
    }

    @Override
    public void onExtraLifeWon(GameEvent e) {
        GameSounds.playExtraLifeSound();
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        GameSounds.playGhostEatenSound();
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        int mapNumber = e.game.currentMapNumber();
        context.renderer().selectMap(e.game.world().map(), mapNumber, context.spriteSheet());
    }

    @Override
    public void onPacDied(GameEvent e) {
        GameSounds.playPacDeathSound();
    }

    @Override
    public void onPacFoundFood(GameEvent e) {
        GameSounds.playMunchingSound();
    }

    @Override
    public void onPacGetsPower(GameEvent e) {
        GameSounds.stopSiren();
        GameSounds.playPacPowerSound();
    }

    @Override
    public void onPacLostPower(GameEvent e) {
        GameSounds.stopPacPowerSound();
    }
}