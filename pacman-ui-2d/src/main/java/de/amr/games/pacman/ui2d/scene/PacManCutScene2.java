/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.actors.Animations;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.rendering.PacManGameGhostAnimations;
import de.amr.games.pacman.ui2d.rendering.PacManGamePacAnimations;
import de.amr.games.pacman.ui2d.rendering.PacManGameSpriteSheet;
import de.amr.games.pacman.ui2d.util.SpriteAnimation;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.t;

/**
 * @author Armin Reichert
 */
public class PacManCutScene2 extends GameScene2D {

    private int initialDelay;
    private int frame;
    private Pac pac;
    private Ghost blinky;
    private SpriteAnimation blinkyNormal;
    private SpriteAnimation blinkyStretching;
    private SpriteAnimation blinkyDamaged;

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
        var blinkyAnimations = new PacManGameGhostAnimations(blinky, (PacManGameSpriteSheet) spriteRenderer.spriteSheet());
        blinkyNormal = blinkyAnimations.animation(Ghost.ANIM_GHOST_NORMAL);
        blinkyStretching = blinkyAnimations.animation(Ghost.ANIM_BLINKY_STRETCHED);
        blinkyDamaged = blinkyAnimations.animation(Ghost.ANIM_BLINKY_DAMAGED);
        blinky.setAnimations(blinkyAnimations);
        blinky.setSpeed(0);
        blinky.hide();
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

        switch (++frame) {
            case 1 -> blinkyStretching.setFrameIndex(0); // Show nail
            case 25 -> {
                pac.placeAtTile(28, 20, 0, 0);
                pac.setMoveDir(Direction.LEFT);
                pac.setSpeed(1.15f);
                pac.selectAnimation(Pac.ANIM_MUNCHING);
                pac.animations().ifPresent(Animations::startSelected);
                pac.show();
            }
            case 111 -> {
                blinky.placeAtTile(28, 20, -3, 0);
                blinky.setMoveAndWishDir(Direction.LEFT);
                blinky.setSpeed(1.25f);
                blinky.selectAnimation(Ghost.ANIM_GHOST_NORMAL);
                blinky.startAnimation();
                blinky.show();
            }
            case 194 -> {
                blinky.setSpeed(0.09f);
                blinkyNormal.setFrameTicks(32);
            }
            case 198, 226, 248 -> blinkyStretching.nextFrame(); // Stretched S-M-L
            case 328 -> {
                blinky.setSpeed(0);
                blinkyStretching.nextFrame(); // Rapture
            }
            case 329 -> blinky.selectAnimation(Ghost.ANIM_BLINKY_DAMAGED); // Eyes up
            case 389 -> blinkyDamaged.nextFrame(); // Eyes right-down
            case 508 -> {
                blinky.setVisible(false);
                context.gameState().timer().expire();
            }
            default -> {
            }
        }

        blinky.move();
        pac.move();
    }

    @Override
    public void drawSceneContent() {
        spriteRenderer.drawSpriteScaled(g, blinkyStretching.currentSprite(), t(14), t(19) + 3);
        spriteRenderer.drawGhost(g,blinky);
        spriteRenderer.drawPac(g, pac);
        drawLevelCounter(g);
    }

    @Override
    protected void drawSceneInfo() {
        drawTileGrid();
        var text = initialDelay > 0 ? String.format("Wait %d", initialDelay) : String.format("Frame %d", frame);
        spriteRenderer.drawText(g, text, Color.YELLOW, Font.font("Sans", 16), t(1), t(5));
    }
}