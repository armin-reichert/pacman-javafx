/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.world.World;
import de.amr.games.pacman.ui.fx.rendering2d.FoodMapRenderer;
import de.amr.games.pacman.ui.fx.rendering2d.MsPacManGameSpriteSheet;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGameSpriteSheet;
import de.amr.games.pacman.ui.fx.rendering2d.TerrainMapRenderer;
import de.amr.games.pacman.ui.fx.util.Keyboard;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.actors.GhostState.ENTERING_HOUSE;
import static de.amr.games.pacman.model.actors.GhostState.RETURNING_HOME;
import static de.amr.games.pacman.ui.fx.PacManGames2dUI.*;
import static de.amr.games.pacman.ui.fx.rendering2d.TileMapRenderer.getColorFromMap;
import static java.util.function.Predicate.not;

/**
 * @author Armin Reichert
 */
public class PlayScene2D extends GameScene2D {

    static class ModernWorldRenderer {

        public ModernWorldRenderer(DoubleProperty scalingPy) {
            terrainRenderer.scalingPy.bind(scalingPy);
            foodRenderer.scalingPy.bind(scalingPy);
        }

        private final TerrainMapRenderer terrainRenderer = new TerrainMapRenderer();
        private final FoodMapRenderer foodRenderer = new FoodMapRenderer();

        private void draw(GraphicsContext g, World world, boolean flashing, boolean blinkingOn) {
            if (flashing) {
                drawTerrain(g, world, blinkingOn);
            } else {
                drawTerrain(g, world, false);
                drawFood(g, world, blinkingOn);
            }
        }

        public void drawTerrain(GraphicsContext g, World world, boolean hiLighted) {
            var terrainMap = world.map().terrain();
            terrainRenderer.setWallStrokeColor(hiLighted ? Color.WHITE : getColorFromMap(terrainMap, "wall_stroke_color", Color.WHITE));
            terrainRenderer.setWallFillColor(hiLighted ? Color.BLACK : getColorFromMap(terrainMap, "wall_fill_color", Color. GREEN));
            terrainRenderer.setDoorColor(hiLighted ? Color.BLACK : getColorFromMap(terrainMap, "door_color", Color.YELLOW));
            terrainRenderer.drawMap(g, terrainMap);
        }

        public void drawFood(GraphicsContext g, World world, boolean energizersOn) {
            var foodColor = getColorFromMap(world.map().food(), "food_color", Color.ORANGE);
            foodRenderer.setPelletColor(foodColor);
            foodRenderer.setEnergizerColor(foodColor);
            world.tiles().filter(world::hasFoodAt).filter(not(world::isEnergizerTile)).forEach(tile -> foodRenderer.drawPellet(g, tile));
            if (energizersOn) {
                world.energizerTiles().filter(world::hasFoodAt).forEach(tile -> foodRenderer.drawEnergizer(g, tile));
            }
        }

    }

    static class ClassicWorldRenderer {

        private final DoubleProperty scalingPy = new SimpleDoubleProperty(1);
        private PacManGameSpriteSheet spriteSheetPacMan;
        private MsPacManGameSpriteSheet spriteSheetMsPacMan;

        public ClassicWorldRenderer(DoubleProperty scalingPy) {
            this.scalingPy.bind(scalingPy);
        }

        public void setPacManSpriteSheet(PacManGameSpriteSheet spriteSheet) {
            spriteSheetPacMan = spriteSheet;
        }
        public void setMsPacManSpriteSheet(MsPacManGameSpriteSheet spriteSheet) {
            spriteSheetMsPacMan = spriteSheet;
        }

        public void drawPacManWorld(GraphicsContext g, World world, boolean flashing, boolean blinkingOn) {
            if (flashing) {
                g.save();
                g.scale(scalingPy.get(), scalingPy.get());
                if (blinkingOn) {
                    g.drawImage(spriteSheetPacMan.getFlashingMazeImage(), 0, t(3));
                } else {
                    drawSprite(g, spriteSheetPacMan.source(), spriteSheetPacMan.getEmptyMazeSprite(), 0, t(3));
                }
                g.restore();
            } else {
                g.save();
                g.scale(scalingPy.get(), scalingPy.get());
                drawSprite(g, spriteSheetPacMan.source(), spriteSheetPacMan.getFullMazeSprite(), 0, t(3));
                g.restore();
                world.tiles().filter(world::hasEatenFoodAt).forEach(tile -> hideFoodTileContent(g, world, tile));
                if (!blinkingOn) {
                    world.energizerTiles().forEach(tile -> hideFoodTileContent(g, world, tile));
                }
            }
        }

        public void drawMsPacManWorld(GraphicsContext g, World world, int mapNumber, boolean flashing, boolean blinkingOn) {
            double x = 0, y = t(3);
            if (flashing) {
                g.save();
                g.scale(scalingPy.get(), scalingPy.get());
                if (blinkingOn) {
                    var emptyMazeBright = spriteSheetMsPacMan.highlightedMaze(mapNumber);
                    drawSprite(g, spriteSheetMsPacMan.getFlashingMazesImage(), emptyMazeBright, x - 3, y);
                } else {
                    drawSprite(g, spriteSheetMsPacMan.source(), spriteSheetMsPacMan.emptyMaze(mapNumber), x, y);
                }
                g.restore();
            } else {
                g.save();
                g.scale(scalingPy.get(), scalingPy.get());
                drawSprite(g, spriteSheetMsPacMan.source(), spriteSheetMsPacMan.filledMaze(mapNumber), x, y);
                g.restore();
                world.tiles().filter(world::hasEatenFoodAt).forEach(tile -> hideFoodTileContent(g, world, tile));
                if (!blinkingOn) {
                    world.energizerTiles().forEach(tile -> hideFoodTileContent(g, world, tile));
                }
            }
        }

        protected void drawSprite(GraphicsContext g, Image sourceImage, Rectangle2D sprite, double x, double y) {
            if (sprite != null) {
                g.drawImage(sourceImage,
                    sprite.getMinX(), sprite.getMinY(), sprite.getWidth(), sprite.getHeight(),
                    x, y, sprite.getWidth(), sprite.getHeight());
            }
        }

        private void hideFoodTileContent(GraphicsContext g, World world, Vector2i tile) {
            double r = world.isEnergizerTile(tile) ? 4.5 : 2;
            double cx = t(tile.x()) + HTS;
            double cy = t(tile.y()) + HTS;
            double s = scalingPy.get();
            g.setFill(Color.BLACK);
            g.fillRect(s*(cx - r), s*(cy - r), s*(2 * r), s*(2 * r));
        }
    }

    private final ModernWorldRenderer modernWorldRenderer = new ModernWorldRenderer(scalingPy);
    private final ClassicWorldRenderer classicWorldRenderer = new ClassicWorldRenderer(scalingPy);

    @Override
    public boolean isCreditVisible() {
        return !context.gameController().hasCredit() || context.gameState() == GameState.GAME_OVER;
    }

    @Override
    public void init() {
        setScoreVisible(true);
        switch (context.game().variant()) {
            case MS_PACMAN -> classicWorldRenderer.setMsPacManSpriteSheet(context.spriteSheet());
            case PACMAN -> classicWorldRenderer.setPacManSpriteSheet(context.spriteSheet());
        }
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
            case GameVariant.MS_PACMAN ->
                classicWorldRenderer.drawMsPacManWorld(g, game.world(), game.mapNumber(), flashing, blinkingOn);
            case GameVariant.PACMAN -> {
                if (game.mapNumber() == 1 && !PY_USE_ALTERNATE_MAPS.get()) {
                    classicWorldRenderer.drawPacManWorld(g, game.world(), flashing, blinkingOn);
                } else {
                    modernWorldRenderer.draw(g, game.world(), flashing, blinkingOn);
                }
            }
        }
        drawLevelMessage();
        game.bonus().ifPresent(this::drawBonus);
        drawPac(game.pac());
        if (game.powerTimer().isRunning()) {
            game.ghosts().forEach(this::drawGhost);
        } else {
            game.ghosts().toList().reversed().forEach(this::drawGhost);
        }
        if (!isCreditVisible()) {
            int numLivesDisplayed = game.lives() - 1;
            if (context.gameState() == GameState.READY && !game.pac().isVisible()) {
                numLivesDisplayed += 1;
            }
            drawLivesCounter(numLivesDisplayed);
        }
        drawLevelCounter();
    }

    private void drawLevelMessage() {
        var game = context.game();
        if (game.isDemoLevel() || context.gameState() == GameState.GAME_OVER) {
            // "GAME OVER" is drawn in demo mode and when game is over
            drawText("GAME  OVER", Color.RED, sceneFont(8), t(9), t(21));
        } else {
            switch (context.gameState()) {
                case READY      -> drawText("READY!", Color.YELLOW, sceneFont(8), t(11), t(21));
                case LEVEL_TEST -> drawText("TEST    L" + game.levelNumber(), Color.YELLOW, sceneFont(8), t(8.5), t(21));
            }
        }
    }

    @Override
    protected void drawSceneInfo() {
        var game = context.game();
        drawTileGrid(GameModel.ARCADE_MAP_TILES_X, GameModel.ARCADE_MAP_TILES_Y);
        if (game == GameVariant.PACMAN && game.world() != null) {
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