/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.ui.fx.util.Keyboard;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.actors.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.actors.GhostState.RETURNING_HOME;
import static de.amr.games.pacman.ui.fx.PacManGames2dUI.*;

/**
 * @author Armin Reichert
 */
public class PlayScene2D extends GameScene2D {

    @Override
    public boolean isCreditVisible() {
        return !context.gameController().hasCredit() || context.gameState() == GameState.GAME_OVER;
    }

    @Override
    public void init() {
        setScoreVisible(true);
    }

    @Override
    public void update() {
        context.game().level().ifPresent(level -> {
            context.game().pac().setUseAutopilot(context.game().isDemoLevel() || PY_USE_AUTOPILOT.get());
            if (!context.game().isDemoLevel()) {
                updateSound();
            }
        });
    }

    @Override
    public void handleKeyboardInput() {
        if (Keyboard.pressed(KEYS_ADD_CREDIT)) {
            if (!context.gameController().hasCredit()) {
                context.actionHandler().addCredit();
            }
        } else if (Keyboard.pressed(KEY_CHEAT_EAT_ALL)) {
            context.actionHandler().cheatEatAllPellets();
        } else if (Keyboard.pressed(KEY_CHEAT_ADD_LIVES)) {
            context.actionHandler().cheatAddLives();
        } else if (Keyboard.pressed(KEY_CHEAT_NEXT_LEVEL)) {
            context.actionHandler().cheatEnterNextLevel();
        } else if (Keyboard.pressed(KEY_CHEAT_KILL_GHOSTS)) {
            context.actionHandler().cheatKillAllEatableGhosts();
        }
    }

    @Override
    public void onGameStateEntry(GameState state) {
        switch (state) {
            case READY, PACMAN_DYING, LEVEL_COMPLETE -> context.stopAllSounds();
            case GAME_OVER -> {
                context.stopAllSounds();
                context.playAudioClip("audio.game_over");
            }
            default -> {}
        }
    }

    @Override
    protected void drawSceneContent() {
        var game = context.game();
        if (game.level().isEmpty()) {
            return;
        }
        boolean flashing = Boolean.TRUE.equals(context.gameState().getProperty("mazeFlashing"));
        boolean blinkingOn = game.blinking().isOn();
        switch (game.variant()) {
            case MS_PACMAN ->
                classicRenderer.drawMsPacManWorld(g, game.world(), game.mapNumber(), flashing, blinkingOn);
            case PACMAN ->
                classicRenderer.drawPacManWorld(g, game.world(), flashing, blinkingOn);
            case PACMAN_XXL ->
                modernRenderer.draw(g, game.world(), flashing, blinkingOn);
        }
        drawLevelMessage();
        game.bonus().ifPresent(bonus -> classicRenderer.drawBonus(g, context.game().variant(), bonus));
        classicRenderer.drawPac(g, context.game().variant(), game.pac());
        if (infoVisiblePy.get()) {
            classicRenderer.drawPacInfo(g, game.pac());
        }
        if (game.powerTimer().isRunning()) {
            game.ghosts().forEach(ghost -> {
                classicRenderer.drawGhost(g, context.game().variant(), ghost);
                if (infoVisiblePy.get()) {
                    classicRenderer.drawGhostInfo(g, ghost);
                }
            });
        } else {
            game.ghosts().toList().reversed().forEach(ghost -> {
                classicRenderer.drawGhost(g, context.game().variant(), ghost);
                if (infoVisiblePy.get()) {
                    classicRenderer.drawGhostInfo(g, ghost);
                }
            });
        }
        if (!isCreditVisible()) {
            int numLivesDisplayed = game.lives() - 1;
            if (context.gameState() == GameState.READY && !game.pac().isVisible()) {
                numLivesDisplayed += 1;
            }
            classicRenderer.drawLivesCounter(g, context.game().variant(), numLivesDisplayed);
        }
        classicRenderer.drawLevelCounter(g, context.game().variant(), context.game().levelCounter());
    }

    private void drawLevelMessage() {
        var game = context.game();
        if (game.isDemoLevel() || context.gameState() == GameState.GAME_OVER) {
            // "GAME OVER" is drawn in demo mode and when game is over
            classicRenderer.drawText(g, "GAME  OVER", Color.RED, sceneFont(8), t(9), t(21));
        } else {
            switch (context.gameState()) {
                case READY      -> classicRenderer.drawText(g, "READY!", Color.YELLOW, sceneFont(8), t(11), t(21));
                case LEVEL_TEST -> classicRenderer.drawText(g, "TEST    L" + game.levelNumber(), Color.YELLOW, sceneFont(8), t(8.5), t(21));
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
    public void onSceneVariantSwitch() {
        if (!context.game().isDemoLevel() && context.gameState() == GameState.HUNTING) {
            context.ensureSirenStarted(context.game().huntingPhaseIndex() / 2);
        }
    }

    private void updateSound() {
        if (context.game().pac().starvingTicks() > 8) { // TODO not sure
            context.stopAudioClip("audio.pacman_munch");
        }
        if (context.game().pac().isAlive() && context.game().ghosts(RETURNING_HOME, ENTERING_HOUSE).anyMatch(Ghost::isVisible)) {
            context.ensureAudioLoop("audio.ghost_returning");
        } else {
            context.stopAudioClip("audio.ghost_returning");
        }
    }
}