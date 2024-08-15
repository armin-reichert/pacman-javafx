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
import de.amr.games.pacman.model.MsPacManGame;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.MovingBonus;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.ActionHandler;
import de.amr.games.pacman.ui2d.GameKey;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.tinylog.Logger;

import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.ui2d.GameParameters.PY_AUTOPILOT;

/**
 * @author Armin Reichert
 */
public class PlayScene2D extends GameScene2D {

    @Override
    public boolean isCreditVisible() {
        return !context.gameController().hasCredit() || context.gameState() == GameState.GAME_OVER;
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
        } else {
            context.setScoreVisible(true);
            context.game().pac().setUseAutopilot(PY_AUTOPILOT.get());
            GameSounds.updatePlaySceneSound();
        }
    }

    @Override
    public void handleKeyboardInput(ActionHandler actions) {
        if (GameKey.ADD_CREDIT.pressed() && context.game().isDemoLevel()) {
            actions.addCredit();
        } else if (GameKey.CHEAT_EAT_ALL.pressed()) {
            actions.cheatEatAllPellets();
        } else if (GameKey.CHEAT_ADD_LIVES.pressed()) {
            actions.cheatAddLives();
        } else if (GameKey.CHEAT_NEXT_LEVEL.pressed()) {
            actions.cheatEnterNextLevel();
        } else if (GameKey.CHEAT_KILL_GHOSTS.pressed()) {
            actions.cheatKillAllEatableGhosts();
        }
    }

    @Override
    public void onGameStateEntry(GameState state) {
        switch (state) {
            case READY, PACMAN_DYING, LEVEL_COMPLETE -> GameSounds.stopAll();
            case GAME_OVER -> {
                GameSounds.stopAll();
                GameSounds.playGameOverSound();
            }
            default -> {}
        }
    }

    @Override
    protected void drawSceneContent() {
        var game = context.game();
        if (game.world() == null) {
            return;
        }
        boolean flashing = Boolean.TRUE.equals(context.gameState().getProperty("mazeFlashing"));
        boolean blinkingOn = game.blinking().isOn();
        spriteRenderer.setSpriteSheet(context.spriteSheet(game.variant()));
        switch (game.variant()) {
            case MS_PACMAN -> {
                MsPacManGame msPacManGame = (MsPacManGame) game;
                if (msPacManGame.blueMazeBug) {
                    vectorRenderer.draw(g, game.world(), flashing, blinkingOn);
                } else {
                    int mapNumber = msPacManGame.currentMapNumber();
                    spriteRenderer.drawMsPacManWorld(g, game.world(), mapNumber, flashing, blinkingOn);
                }
                game.bonus().ifPresent(bonus -> spriteRenderer.drawMovingBonus(g, (MovingBonus) bonus));
            }
            case PACMAN -> {
                spriteRenderer.drawPacManWorld(g, game.world(), 0, 3, flashing, blinkingOn);
                game.bonus().ifPresent(bonus -> spriteRenderer.drawStaticBonus(g, bonus));
            }
            case PACMAN_XXL -> {
                vectorRenderer.draw(g, game.world(), flashing, blinkingOn);
                game.bonus().ifPresent(bonus -> spriteRenderer.drawStaticBonus(g, bonus));
            }
        }
        drawLevelMessage();
        if (game.powerTimer().isRunning()) {
            Stream.of(GameModel.ORANGE_GHOST, GameModel.CYAN_GHOST, GameModel.PINK_GHOST, GameModel.RED_GHOST)
                .map(game::ghost).forEach(this::drawGhost);
            drawPac(game.pac());
        } else {
            drawPac(game.pac());
            Stream.of(GameModel.ORANGE_GHOST, GameModel.CYAN_GHOST, GameModel.PINK_GHOST, GameModel.RED_GHOST)
                .map(game::ghost).forEach(this::drawGhost);
        }
        if (!isCreditVisible()) {
            int numLivesDisplayed = game.lives() - 1;
            if (context.gameState() == GameState.READY && !game.pac().isVisible()) {
                numLivesDisplayed += 1;
            }
            spriteRenderer.drawLivesCounter(g, numLivesDisplayed, context.game().world().map().terrain().numRows() - 2);
        }
        drawLevelCounter(g);
    }

    private void drawPac(Pac pac) {
        spriteRenderer.drawPac(g, pac);
        if (infoVisiblePy.get()) {
            spriteRenderer.drawPacInfo(g, pac);
        }
    }

    private void drawGhost(Ghost ghost) {
        spriteRenderer.drawGhost(g, ghost);
        if (infoVisiblePy.get()) {
            spriteRenderer.drawGhostInfo(g, ghost);
        }
    }

    private void drawLevelMessage() {
        var game = context.game();
        Vector2i houseOrigin = game.world().houseTopLeftTile();
        Vector2i houseSize = game.world().houseSize();
        int centerTileX = houseOrigin.x() + houseSize.x() / 2;
        int tileY = houseOrigin.y() + houseSize.y() + 1;
        if (game.isDemoLevel() || context.gameState() == GameState.GAME_OVER) {
            // "GAME OVER" is drawn in demo mode and when game is over
            spriteRenderer.drawText(g, "GAME  OVER", Color.RED, sceneFont(8), t(centerTileX - 5), t(tileY));
        } else {
            switch (context.gameState()) {
                case READY      -> spriteRenderer.drawText(g, "READY!", Color.YELLOW, sceneFont(8), t(centerTileX - 3), t(tileY));
                case LEVEL_TEST -> spriteRenderer.drawText(g, "TEST    L" + game.levelNumber(), Color.YELLOW, sceneFont(8), t(8.5), t(tileY));
            }
        }
    }

    @Override
    protected void drawSceneInfo() {
        var game = context.game();
        drawTileGrid();
        if (game.variant() == GameVariant.PACMAN && game.world() != null) {
            game.world().forbiddenPassages().forEach((tile, directions) -> {
                // TODO indicate direction
                g.setFill(Color.RED);
                g.fillOval(s(t(tile.x())), s(t(tile.y() - 1)), s(TS), s(TS));
                g.setFill(Color.WHITE);
                g.fillRect(s(t(tile.x()) + 1), s(t(tile.y()) - HTS - 1), s(TS - 2), s(2));
            });
        }
        g.setFill(Color.YELLOW);
        g.setFont(Font.font("Sans", FontWeight.BOLD, 24));
        g.fillText(String.format("%s %d", context.gameState(), context.gameState().timer().tick()), 0, 64);
    }

    @Override
    public void onSceneVariantSwitch(GameScene oldScene) {
        Logger.info("{} entered from {}", this, oldScene);
        if (!context.game().isDemoLevel()) {
            GameSounds.playHuntingSound();
        }
    }

    @Override
    public void onHuntingPhaseStarted(GameEvent event) {
        if (!context.game().isDemoLevel()) {
            GameSounds.playHuntingSound();
        }
    }

    @Override
    public void onBonusEaten(GameEvent e) {
        if (!context.game().isDemoLevel()) {
            GameSounds.playBonusEatenSound();
        }
    }

    @Override
    public void onExtraLifeWon(GameEvent e) {
        if (!context.game().isDemoLevel()) {
            GameSounds.playExtraLifeSound();
        }
    }

    @Override
    public void onGhostEaten(GameEvent e) {
        if (!context.game().isDemoLevel()) {
            GameSounds.playGhostEatenSound();
        }
    }

    @Override
    public void onPacDied(GameEvent event) {
        if (!context.game().isDemoLevel()) {
            GameSounds.playPacManDeathSound();
            GameSounds.stopMunchingSound();
        }
    }

    @Override
    public void onPacFoundFood(GameEvent event) {
        if (!context.game().isDemoLevel()) {
            GameSounds.playMunchingSound();
        }
    }

    @Override
    public void onPacGetsPower(GameEvent event) {
        if (!context.game().isDemoLevel()) {
            GameSounds.stopSiren();
            GameSounds.playPowerSound();
        }
    }

    @Override
    public void onPacLostPower(GameEvent event) {
        if (!context.game().isDemoLevel()) {
            GameSounds.stopPowerSound();
            GameSounds.playHuntingSound();
        }
    }
}