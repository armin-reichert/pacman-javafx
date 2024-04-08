/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.scene2d;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameModels;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.world.ArcadeWorld;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGameGhostAnimations;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGamePacAnimations;
import de.amr.games.pacman.ui.fx.rendering2d.PacManGameSpriteSheet;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.t;

/**
 * @author Armin Reichert
 */
public class PacManCutscene1 extends GameScene2D {

    private int initialDelay;
    private int frame;
    private Pac pac;
    private Ghost blinky;

    @Override
    public boolean isCreditVisible() {
        return !context.gameController().hasCredit();
    }

    @Override
    public void init() {
        frame = -1;
        initialDelay = 120;
        setScoreVisible(true);

        var ss = context.<PacManGameSpriteSheet>spriteSheet();
        pac = new Pac("Pac-Man");
        pac.setAnimations(new PacManGamePacAnimations(pac, ss));
        blinky = new Ghost(GameModel.RED_GHOST, "Blinky");
        blinky.setAnimations(new PacManGameGhostAnimations(blinky, ss));
    }

    @Override
    public void update() {
        if (initialDelay > 0) {
            --initialDelay;
            if (initialDelay == 0) {
                GameModels.PACMAN.publishGameEvent(GameEventType.INTERMISSION_STARTED);
            }
            return;
        }

        if (context.gameState().timer().hasExpired()) {
            return;
        }

        switch (frame) {
            case 0 -> {
                pac.placeAtTile(29, 20, 0, 0);
                pac.setMoveDir(Direction.LEFT);
                pac.setSpeed(1.25f);
                pac.selectAnimation(Pac.ANIM_MUNCHING);
                pac.startAnimation();
                pac.show();

                blinky.placeAtTile(32, 20, 0, 0);
                blinky.setMoveAndWishDir(Direction.LEFT);
                blinky.setSpeed(1.3f);
                blinky.selectAnimation(Ghost.ANIM_GHOST_NORMAL);
                blinky.startAnimation();
                blinky.show();
            }
            case 260 -> {
                blinky.placeAtTile(-2, 20, 4, 0);
                blinky.setMoveAndWishDir(Direction.RIGHT);
                blinky.setSpeed(0.75f);
                blinky.selectAnimation(Ghost.ANIM_GHOST_FRIGHTENED);
                blinky.startAnimation();
            }
            case 400 -> {
                pac.placeAtTile(-3, 18, 0, 6.5f);
                pac.setMoveDir(Direction.RIGHT);
                pac.selectAnimation(Pac.ANIM_BIG_PACMAN);
                pac.startAnimation();
            }
            case 632 -> context.gameState().timer().expire();
            default -> {
                pac.move();
                blinky.move();
            }
        }
        ++frame;
    }

    @Override
    public void drawSceneContent() {
        drawPac(pac);
        drawGhost(blinky);
        drawLevelCounter();
    }

    @Override
    protected void drawSceneInfo() {
        drawTileGrid(ArcadeWorld.TILES_X, ArcadeWorld.TILES_Y);
        var text = initialDelay > 0 ? String.format("Wait %d", initialDelay) : String.format("Frame %d", frame);
        drawText(text, Color.YELLOW, Font.font("Sans", 16), t(1), t(5));
    }
}