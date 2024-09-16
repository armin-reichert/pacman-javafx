/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.maps.rendering.FoodMapRenderer;
import de.amr.games.pacman.maps.rendering.TerrainMapRenderer;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.MovingBonus;
import de.amr.games.pacman.ui2d.GameAction;
import de.amr.games.pacman.ui2d.GameSounds;
import de.amr.games.pacman.ui2d.rendering.ms_pacman.MsPacManGameWorldRenderer;
import de.amr.games.pacman.ui2d.rendering.pacman.PacManGameWorldRenderer;
import de.amr.games.pacman.ui2d.rendering.pacman_xxl.PacManXXLGameWorldRenderer;
import de.amr.games.pacman.ui2d.rendering.tengen.TengenMsPacManGameWorldRenderer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.tinylog.Logger;

import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.maps.editor.TileMapUtil.getColorFromMap;
import static de.amr.games.pacman.model.GameWorld.*;
import static de.amr.games.pacman.ui2d.PacManGames2dApp.*;
import static java.util.function.Predicate.not;

/**
 * @author Armin Reichert
 */
public class PlayScene2D extends GameScene2D {

    private PacManGameWorldRenderer pacManGameWorldRenderer;
    private MsPacManGameWorldRenderer msPacManGameWorldRenderer;
    private PacManXXLGameWorldRenderer pacManXXLGameWorldRenderer;
    private TengenMsPacManGameWorldRenderer tengenMsPacManGameWorldRenderer;

    private final TerrainMapRenderer terrainRenderer = new TerrainMapRenderer();
    private final FoodMapRenderer foodRenderer = new FoodMapRenderer();

    @Override
    public boolean isCreditVisible() {
        return !context.game().hasCredit() || context.gameState() == GameState.GAME_OVER;
    }

    @Override
    public void init() {
        backgroundColorPy.bind(PY_CANVAS_COLOR);

        terrainRenderer.setMapBackgroundColor(backgroundColorPy.get());
        terrainRenderer.scalingPy.bind(scalingPy);

        foodRenderer.scalingPy.bind(scalingPy);

        spriteRenderer.scalingPy.bind(scalingPy);
        spriteRenderer.backgroundColorPy.bind(backgroundColorPy);
        spriteRenderer.setSpriteSheet(context.spriteSheet(context.game().variant()));

        pacManGameWorldRenderer = new PacManGameWorldRenderer(context.assets());
        pacManGameWorldRenderer.scalingProperty().bind(scalingPy);
        pacManGameWorldRenderer.backgroundColorProperty().bind(PY_CANVAS_COLOR);

        msPacManGameWorldRenderer = new MsPacManGameWorldRenderer(context.assets());
        msPacManGameWorldRenderer.scalingProperty().bind(scalingPy);
        msPacManGameWorldRenderer.backgroundColorProperty().bind(PY_CANVAS_COLOR);

        pacManXXLGameWorldRenderer = new PacManXXLGameWorldRenderer(context.assets());
        pacManXXLGameWorldRenderer.scalingProperty().bind(scalingPy);
        pacManXXLGameWorldRenderer.backgroundColorProperty().bind(PY_CANVAS_COLOR);

        tengenMsPacManGameWorldRenderer = new TengenMsPacManGameWorldRenderer(context.assets());
        tengenMsPacManGameWorldRenderer.scalingProperty().bind(scalingPy);
        tengenMsPacManGameWorldRenderer.backgroundColorProperty().bind(PY_CANVAS_COLOR);

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
    public void handleUserInput() {
        if (GameAction.ADD_CREDIT.requested() && context.game().isDemoLevel()) {
            context.addCredit();
        } else if (GameAction.CHEAT_EAT_ALL.requested()) {
            context.cheatEatAllPellets();
        } else if (GameAction.CHEAT_ADD_LIVES.requested()) {
            context.cheatAddLives();
        } else if (GameAction.CHEAT_NEXT_LEVEL.requested()) {
            context.cheatEnterNextLevel();
        } else if (GameAction.CHEAT_KILL_GHOSTS.requested()) {
            context.cheatKillAllEatableGhosts();
        }
    }

    @Override
    public void onGameStateEntry(GameState state) {
        switch (state) {
            case READY, PACMAN_DYING -> GameSounds.stopAll();
            case GAME_OVER -> {
                GameSounds.stopAll();
                GameSounds.playGameOverSound();
            }
            case LEVEL_COMPLETE -> {
                GameSounds.stopAll();
                // if cheat has been used to complete level, food might still exist, so eat it:
                GameWorld world = context.game().world();
                world.map().food().tiles().forEach(world::eatFoodAt);
            }
            default -> {}
        }
    }

    @Override
    protected void drawSceneContent() {
        GameModel game = context.game();
        if (game.world() == null) {
            // This happens for one frame
            Logger.warn("Cannot draw scene content, game world not yet available!");
            return;
        }
        boolean flashMode = Boolean.TRUE.equals(context.gameState().getProperty("mazeFlashing"));
        boolean blinkingOn = game.blinking().isOn();
        switch (game.variant()) {
            case MS_PACMAN -> {
                msPacManGameWorldRenderer.setFlashMode(flashMode);
                msPacManGameWorldRenderer.setBlinkingOn(blinkingOn);
                msPacManGameWorldRenderer.drawWorld(g, context, game.world());
            }
            case MS_PACMAN_TENGEN -> {
                tengenMsPacManGameWorldRenderer.setFlashMode(flashMode);
                tengenMsPacManGameWorldRenderer.setBlinkingOn(blinkingOn);
                tengenMsPacManGameWorldRenderer.drawWorld(g, context, game.world());
                //drawWorld(flashMode, blinkingOn);
                //game.bonus().ifPresent(bonus -> spriteRenderer.drawMovingBonus(g, (MovingBonus) bonus));
            }
            case PACMAN -> {
                pacManGameWorldRenderer.setFlashMode(flashMode);
                pacManGameWorldRenderer.setBlinkingOn(blinkingOn);
                pacManGameWorldRenderer.drawWorld(g, context, game.world());
            }
            case PACMAN_XXL -> {
                pacManXXLGameWorldRenderer.setFlashMode(flashMode);
                pacManXXLGameWorldRenderer.setBlinkingOn(blinkingOn);
                pacManXXLGameWorldRenderer.drawWorld(g, context, game.world());
            }
            default -> throw new IllegalArgumentException("Unsupported game variant: " + game.variant());
        }
        drawLevelMessage();

        spriteRenderer.drawPac(g, game.pac());
        ghostsInZOrder().forEach(ghost -> spriteRenderer.drawGhost(g, ghost));
        if (infoVisiblePy.get()) {
            spriteRenderer.drawPacInfo(g, game.pac());
            ghostsInZOrder().forEach(ghost -> spriteRenderer.drawGhostInfo(g, ghost));
        }

        if (!isCreditVisible()) {
            int numLivesDisplayed = game.lives() - 1;
            if (context.gameState() == GameState.READY && !game.pac().isVisible()) {
                numLivesDisplayed += 1;
            }
            spriteRenderer.drawLivesCounter(g, numLivesDisplayed, game.world().map().terrain().numRows() - 2);
        }
        drawLevelCounter(g);
    }

    private Stream<Ghost> ghostsInZOrder() {
        return Stream.of(GameModel.ORANGE_GHOST, GameModel.CYAN_GHOST, GameModel.PINK_GHOST, GameModel.RED_GHOST)
            .map(context.game()::ghost);
    }

    private void drawLevelMessage() {
        GameModel game = context.game();
        Vector2i houseOrigin = game.world().houseTopLeftTile(), houseSize = game.world().houseSize();
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
        GameModel game = context.game();
        drawTileGrid();
        if (game.variant() == GameVariant.PACMAN && game.world() != null) {
            game.ghosts().forEach(ghost -> {
                // Are currently the same for each ghost, but who knows what comes...
                ghost.cannotMoveUpTiles().forEach(tile -> {
                    g.setFill(Color.RED);
                    g.fillOval(s(t(tile.x())), s(t(tile.y() - 1)), s(TS), s(TS));
                    g.setFill(Color.WHITE);
                    g.fillRect(s(t(tile.x()) + 1), s(t(tile.y()) - HTS - 1), s(TS - 2), s(2));
                });
            });
        }
        g.setFill(Color.YELLOW);
        g.setFont(Font.font("Sans", FontWeight.BOLD, 24));
        g.fillText(String.format("%s %d", context.gameState(), context.gameState().timer().currentTick()), 0, 64);
    }

    @Override
    public void onSceneVariantSwitch(GameScene oldScene) {
        Logger.info("{} entered from {}", this, oldScene);
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
    public void onPacDied(GameEvent event) {
        GameSounds.playPacDeathSound();
    }

    @Override
    public void onPacFoundFood(GameEvent event) {
        GameSounds.playMunchingSound();
    }

    @Override
    public void onPacGetsPower(GameEvent event) {
        GameSounds.stopSiren();
        GameSounds.playPacPowerSound();
    }

    @Override
    public void onPacLostPower(GameEvent event) {
        GameSounds.stopPacPowerSound();
    }
}