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
import de.amr.games.pacman.ui2d.GameAssets2D;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.tinylog.Logger;

import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.PY_AUTOPILOT;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.PY_IMMUNITY;
import static de.amr.games.pacman.ui2d.PacManGames2dUI.SOUNDS;

/**
 * @author Armin Reichert
 */
public class PlayScene2D extends GameScene2D {

    @Override
    public boolean isCreditVisible() {
        return !context.game().hasCredit() || context.gameState() == GameState.GAME_OVER;
    }

    @Override
    public void init() {
    }

    @Override
    public void end() {
        SOUNDS.stopAll();
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
            SOUNDS.selectSiren(sirenNumber);
            SOUNDS.playSiren();
        }
        if (context.game().pac().starvingTicks() > 8) { // TODO not sure how to do this right
            SOUNDS.stopMunchingSound();
        }
        boolean ghostsReturning = context.game().ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).anyMatch(Ghost::isVisible);
        if (context.game().pac().isAlive() && ghostsReturning) {
            SOUNDS.playGhostReturningHomeSound();
        } else {
            SOUNDS.stopGhostReturningHomeSound();
        }
    }

    @Override
    public void handleInput() {
        if (GameAction.ADD_CREDIT.called() && context.game().isDemoLevel()) {
            context.addCredit();
        } else if (GameAction.CHEAT_EAT_ALL.called()) {
            GameAction.CHEAT_EAT_ALL.execute(context);
        } else if (GameAction.CHEAT_ADD_LIVES.called()) {
            GameAction.CHEAT_ADD_LIVES.execute(context);
        } else if (GameAction.CHEAT_NEXT_LEVEL.called()) {
            GameAction.CHEAT_NEXT_LEVEL.execute(context);
        } else if (GameAction.CHEAT_KILL_GHOSTS.called()) {
            GameAction.CHEAT_KILL_GHOSTS.execute(context);
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
            //TODO: this looks ugly
            int numLivesShown = context.game().lives() - 1;
            if (context.gameState() == GameState.READY && !context.game().pac().isVisible()) {
                numLivesShown += 1;
            }
            renderer.drawLivesCounter(context.spriteSheet(), numLivesShown, 5, context.worldSizeTilesOrDefault());
        }
    }

    private Stream<Ghost> ghostsInZOrder() {
        return Stream.of(GameModel.ORANGE_GHOST, GameModel.CYAN_GHOST, GameModel.PINK_GHOST, GameModel.RED_GHOST)
            .map(context.game()::ghost);
    }

    private void drawLevelMessage(GameWorldRenderer renderer) {
        Vector2i houseTopLeftTile = context.game().world().houseTopLeftTile();
        Vector2i houseSize        = context.game().world().houseSize();
        int cx = houseTopLeftTile.x() + houseSize.x() / 2;
        int y = TS * (houseTopLeftTile.y() + houseSize.y() + 1);
        String assetPrefix = GameAssets2D.assetPrefix(context.game().variant());
        Font font = renderer.scaledArcadeFont(TS);
        if (context.gameState() == GameState.GAME_OVER || context.game().isDemoLevel()) {
            String text = "GAME  OVER";
            int x = TS * (cx - text.length() / 2);
            Color color = context.assets().color(assetPrefix + ".color.game_over_message");
            renderer.drawText(text, color, font, x, y);
        } else if (context.gameState() == GameState.READY) {
            String text = "READY!";
            int x = TS * (cx - text.length() / 2);
            Color color = context.assets().color(assetPrefix + ".color.ready_message");
            renderer.drawText(text, color, font, x, y);
        } else if (context.gameState() == GameState.LEVEL_TEST) {
            String text = "TEST    L%03d".formatted(context.game().levelNumber());
            int x = TS * (cx - text.length() / 2);
            renderer.drawText(text, GameAssets2D.PALETTE_PALE, font, x, y);
        }
    }

    @Override
    protected void drawDebugInfo(GameWorldRenderer renderer) {
        renderer.drawTileGrid(context.worldSizeTilesOrDefault());
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
        context.attachRendererToCurrentMap(context.renderer());
        Logger.info("{} entered from {}", this, oldScene);
    }

    @Override
    public void onGameStateEntry(GameState state) {
        switch (state) {
            case READY, LEVEL_COMPLETE, PACMAN_DYING -> SOUNDS.stopAll();
            case GAME_OVER -> {
                SOUNDS.stopAll();
                SOUNDS.playGameOverSound();
            }
            default -> {}
        }
    }

    @Override
    public void onBonusEaten(GameEvent e) {
        SOUNDS.playBonusEatenSound();
    }

    @Override
    public void onExtraLifeWon(GameEvent e) {
        SOUNDS.playExtraLifeSound();
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        SOUNDS.playGhostEatenSound();
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        context.attachRendererToCurrentMap(context.renderer());
    }

    @Override
    public void onPacDied(GameEvent e) {
        SOUNDS.playPacDeathSound();
    }

    @Override
    public void onPacFoundFood(GameEvent e) {
        SOUNDS.playMunchingSound();
    }

    @Override
    public void onPacGetsPower(GameEvent e) {
        SOUNDS.stopSiren();
        SOUNDS.playPacPowerSound();
    }

    @Override
    public void onPacLostPower(GameEvent e) {
        SOUNDS.stopPacPowerSound();
    }
}