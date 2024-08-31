/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.actors.Animations;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.rendering.PacManGameGhostAnimations;
import de.amr.games.pacman.ui2d.rendering.PacManGamePacAnimations;
import de.amr.games.pacman.ui2d.rendering.PacManGameSpriteSheet;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.t;

/**
 * @author Armin Reichert
 */
public class PacManCutScene1 extends GameScene2D {

    private int initialDelay;
    private int frame;
    private Pac pac;
    private Ghost blinky;

    @Override
    public boolean isCreditVisible() {
        return !context.game().hasCredit();
    }

    @Override
    public void init() {
        super.init();
        frame = -1;
        initialDelay = 120;
        context.setScoreVisible(true);
        pac = new Pac();
        pac.setAnimations(new PacManGamePacAnimations(pac, (PacManGameSpriteSheet) spriteRenderer.spriteSheet()));
        blinky = Ghost.red();
        blinky.setAnimations(new PacManGameGhostAnimations(blinky, (PacManGameSpriteSheet) spriteRenderer.spriteSheet()));
    }

    @Override
    public void update() {
        if (initialDelay > 0) {
            --initialDelay;
            if (initialDelay == 0) {
                context.game().publishGameEvent(GameEventType.INTERMISSION_STARTED);
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
                pac.animations().ifPresent(Animations::startSelected);
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
                pac.animations().ifPresent(Animations::startSelected);
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
        spriteRenderer.drawPac(g, pac);
        spriteRenderer.drawGhost(g, blinky);
        drawLevelCounter(g);
    }

    @Override
    protected void drawSceneInfo() {
        drawTileGrid();
        var text = initialDelay > 0 ? String.format("Wait %d", initialDelay) : String.format("Frame %d", frame);
        spriteRenderer.drawText(g, text, Color.YELLOW, Font.font("Sans", 16), t(1), t(5));
    }
}