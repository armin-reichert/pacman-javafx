/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.MsPacManGame;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.MovingBonus;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.GameKeys;
import de.amr.games.pacman.ui2d.PacManGames2dUI;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.actors.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.actors.GhostState.RETURNING_HOME;

/**
 * @author Armin Reichert
 */
public class PlayScene2D extends GameScene2D {

    private static final Pattern PATTERN_MS_PACMAN_MAP = Pattern.compile(".*mspacman_(\\d)\\.world$");

    private static int mapNumber(WorldMap map) {
        Matcher m = PATTERN_MS_PACMAN_MAP.matcher(map.url().toExternalForm());
        if (m.matches()) {
            return Integer.parseInt(m.group(1));
        } else {
            throw new IllegalArgumentException("Could not determine map number for Ms. Pac-Man map URL: " + map.url());
        }
    }

    @Override
    public boolean isCreditVisible() {
        return !context.gameController().hasCredit() || context.gameState() == GameState.GAME_OVER;
    }

    @Override
    public void init() {
        super.init();
        context.setScoreVisible(true);
    }

    @Override
    public void end() {
        context.soundHandler().stopAllSounds();
    }

    @Override
    public void update() {
        context.game().level().ifPresent(level -> {
            context.game().pac().setUseAutopilot(context.game().isDemoLevel() || PacManGames2dUI.PY_AUTOPILOT.get());
            if (!context.game().isDemoLevel()) {
                updateSound();
            }
        });
    }

    @Override
    public void handleKeyboardInput() {
        if (GameKeys.ADD_CREDIT.pressed()) {
            if (!context.gameController().hasCredit()) {
                context.actionHandler().addCredit();
            }
        } else if (GameKeys.CHEAT_EAT_ALL.pressed()) {
            context.actionHandler().cheatEatAllPellets();
        } else if (GameKeys.CHEAT_ADD_LIVES.pressed()) {
            context.actionHandler().cheatAddLives();
        } else if (GameKeys.CHEAT_NEXT_LEVEL.pressed()) {
            context.actionHandler().cheatEnterNextLevel();
        } else if (GameKeys.CHEAT_KILL_GHOSTS.pressed()) {
            context.actionHandler().cheatKillAllEatableGhosts();
        }
    }

    @Override
    public void onGameStateEntry(GameState state) {
        switch (state) {
            case READY, PACMAN_DYING, LEVEL_COMPLETE -> context.soundHandler().stopAllSounds();
            case GAME_OVER -> {
                context.soundHandler().stopAllSounds();
                context.soundHandler().playAudioClip("audio.game_over");
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
        spriteRenderer.setSpriteSheet(context.getSpriteSheet(game.variant()));
        spriteRenderer.setBackgroundColor(canvasBackground());
        switch (game.variant()) {
            case MS_PACMAN -> {
                int mapNumber = mapNumber(game.world().map());
                if (game instanceof MsPacManGame msPacManGame && msPacManGame.blueMazeBug) {
                    vectorRenderer.draw(g, game.world(), flashing, blinkingOn);
                } else {
                    spriteRenderer.drawMsPacManWorld(g, game.world(), mapNumber, flashing, blinkingOn);
                }
                game.bonus().ifPresent(bonus -> spriteRenderer.drawMovingBonus(g, (MovingBonus) bonus));
            }
            case PACMAN -> {
                spriteRenderer.drawPacManWorld(g, game.world(), flashing, blinkingOn);
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
            spriteRenderer.drawLivesCounter(g, numLivesDisplayed);
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
        if (game.isDemoLevel() || context.gameState() == GameState.GAME_OVER) {
            // "GAME OVER" is drawn in demo mode and when game is over
            spriteRenderer.drawText(g, "GAME  OVER", Color.RED, sceneFont(8), t(9), t(21));
        } else {
            switch (context.gameState()) {
                case READY      -> spriteRenderer.drawText(g, "READY!", Color.YELLOW, sceneFont(8), t(11), t(21));
                case LEVEL_TEST -> spriteRenderer.drawText(g, "TEST    L" + game.levelNumber(), Color.YELLOW, sceneFont(8), t(8.5), t(21));
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
            context.soundHandler().ensureSirenStarted(context.game().huntingPhaseIndex() / 2);
        }
    }

    private void updateSound() {
        if (context.game().pac().starvingTicks() > 8) { // TODO not sure
            context.soundHandler().stopAudioClip("audio.pacman_munch");
        }
        if (context.game().pac().isAlive() && context.game().ghosts(RETURNING_HOME, ENTERING_HOUSE).anyMatch(Ghost::isVisible)) {
            context.soundHandler().ensureAudioLoop("audio.ghost_returning");
        } else {
            context.soundHandler().stopAudioClip("audio.ghost_returning");
        }
    }
}