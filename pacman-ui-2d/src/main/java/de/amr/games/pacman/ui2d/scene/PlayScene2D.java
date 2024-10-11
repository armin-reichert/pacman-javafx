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
import de.amr.games.pacman.ui2d.GameAction2D;
import de.amr.games.pacman.ui2d.GameAssets2D;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import de.amr.games.pacman.ui2d.scene.tengen.TengenGameWorldRenderer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.tinylog.Logger;

import java.util.List;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.PY_AUTOPILOT;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.PY_IMMUNITY;

/**
 * @author Armin Reichert
 */
public class PlayScene2D extends GameScene2D {

    private static final List<GameAction> GAME_ACTIONS = List.of(
        GameAction2D.CHEAT_EAT_ALL,
        GameAction2D.CHEAT_ADD_LIVES,
        GameAction2D.CHEAT_NEXT_LEVEL,
        GameAction2D.CHEAT_KILL_GHOSTS
    );

    private static final List<GameAction> TENGEN_GAME_ACTIONS = List.of(
        GameAction2D.TENGEN_TOGGLE_PAC_BOOSTER,
        GameAction2D.TENGEN_QUIT_PLAY_SCENE
    );

    @Override
    public void init() {
    }

    @Override
    public void end() {
        context.sounds().stopAll();
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
            context.sounds().selectSiren(sirenNumber);
            context.sounds().playSiren();
        }
        if (context.game().pac().starvingTicks() > 8) { // TODO not sure how to do this right
            context.sounds().stopMunchingSound();
        }
        boolean ghostsReturning = context.game().ghosts(GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE).anyMatch(Ghost::isVisible);
        if (context.game().pac().isAlive() && ghostsReturning) {
            context.sounds().playGhostReturningHomeSound();
        } else {
            context.sounds().stopGhostReturningHomeSound();
        }
    }

    @Override
    public void handleInput() {
        if (context.gameVariant() == GameVariant.MS_PACMAN_TENGEN) {
            context.execFirstCalledActionOrElse(TENGEN_GAME_ACTIONS.stream(),
                () -> context.execFirstCalledAction(GAME_ACTIONS));
        } else {
            // add credit is only allowed in demo level
            if (context.isActionCalled(GameAction2D.ADD_CREDIT) && context.game().isDemoLevel()) {
                GameAction2D.ADD_CREDIT.execute(context);
            } else {
                context.execFirstCalledAction(GAME_ACTIONS);
            }
        }
    }

    @Override
    protected void drawSceneContent(GameWorldRenderer renderer) {
        if (context.game().world() == null) { // This happens on level start
            Logger.warn("Cannot draw scene content, game world not yet available!");
            return;
        }

        drawLevelMessage(renderer); // READY, GAME_OVER etc.

        boolean flashMode = Boolean.TRUE.equals(context.gameState().getProperty("mazeFlashing"));
        renderer.setFlashMode(flashMode);
        renderer.setBlinkingOn(context.game().blinking().isOn());
        renderer.drawWorld(context.spriteSheet(), context, context.game().world());

        renderer.drawAnimatedEntity(context.game().pac());
        ghostsInZOrder().forEach(renderer::drawAnimatedEntity);

        // Debug mode info
        if (debugInfoPy.get()) {
            renderer.drawAnimatedCreatureInfo(context.game().pac());
            ghostsInZOrder().forEach(renderer::drawAnimatedCreatureInfo);
        }

        boolean showCredit = switch (context.gameVariant()) {
            case MS_PACMAN_TENGEN -> false;
            default -> !context.game().hasCredit() || context.gameState() == GameState.GAME_OVER;
        };

        Vector2i worldSize = context.worldSizeTilesOrDefault();
        if (showCredit) {
            drawCredit(renderer, worldSize);
        } else {
            //TODO: this code looks ugly
            int numLivesShown = context.game().lives() - 1;
            if (context.gameState() == GameState.READY && !context.game().pac().isVisible()) {
                numLivesShown += 1;
            }
            renderer.drawLivesCounter(context.spriteSheet(), numLivesShown, 5, worldSize);
        }
        drawLevelCounter(renderer, worldSize);
        //TODO Hack: Tengen has those boxes displaying the level number
        if (context.gameVariant() == GameVariant.MS_PACMAN_TENGEN) {
            TengenGameWorldRenderer tr = (TengenGameWorldRenderer) renderer;
            tr.drawLevelNumberBoxes(context.spriteSheet(), context.game().levelNumber(), worldSize);
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
        String assetPrefix = GameAssets2D.assetPrefix(context.gameVariant());
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
        } else if (context.gameState() == GameState.TESTING_LEVELS_AND_BONUSES) {
            String text = "TEST    L%03d".formatted(context.game().levelNumber());
            int x = TS * (cx - text.length() / 2);
            renderer.drawText(text, GameAssets2D.PALETTE_PALE, font, x, y);
        }
    }

    @Override
    protected void drawDebugInfo(GameWorldRenderer renderer) {
        renderer.drawTileGrid(context.worldSizeTilesOrDefault());
        if (context.gameVariant() == GameVariant.PACMAN && context.game().world() != null) {
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
            case READY, LEVEL_COMPLETE, PACMAN_DYING -> context.sounds().stopAll();
            case GAME_OVER -> {
                context.sounds().stopAll();
                context.sounds().playGameOverSound();
            }
            default -> {}
        }
    }

    @Override
    public void onBonusEaten(GameEvent e) {
        context.sounds().playBonusEatenSound();
    }

    @Override
    public void onExtraLifeWon(GameEvent e) {
        context.sounds().playExtraLifeSound();
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        context.sounds().playGhostEatenSound();
    }

    @Override
    public void onLevelCreated(GameEvent e) {
        context.attachRendererToCurrentMap(context.renderer());
    }

    @Override
    public void onPacDied(GameEvent e) {
        context.sounds().playPacDeathSound();
    }

    @Override
    public void onPacFoundFood(GameEvent e) {
        context.sounds().playMunchingSound();
    }

    @Override
    public void onPacGetsPower(GameEvent e) {
        context.sounds().stopSiren();
        context.sounds().playPacPowerSound();
    }

    @Override
    public void onPacLostPower(GameEvent e) {
        context.sounds().stopPacPowerSound();
    }
}