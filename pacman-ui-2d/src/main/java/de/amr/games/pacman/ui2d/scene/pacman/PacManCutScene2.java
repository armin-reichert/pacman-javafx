/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.pacman;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.model.actors.Animations;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import de.amr.games.pacman.ui2d.scene.GameScene2D;
import de.amr.games.pacman.ui2d.util.SpriteAnimation;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.t;

/**
 * @author Armin Reichert
 */
public class PacManCutScene2 extends GameScene2D {

    static final short ANIMATION_START = 120;

    private int frame;
    private Pac pac;
    private Ghost blinky;
    private SpriteAnimation blinkyNormal;
    private SpriteAnimation blinkyStretching;
    private SpriteAnimation blinkyDamaged;

    @Override
    public void init() {
        context.setScoreVisible(true);
        pac = new Pac();
        pac.setAnimations(new PacManGamePacAnimations(context.spriteSheet()));
        blinky = Ghost.blinky();
        var blinkyAnimations = new PacManGameGhostAnimations(context.spriteSheet(), blinky.id());
        blinkyNormal = blinkyAnimations.animation(Ghost.ANIM_GHOST_NORMAL);
        blinkyStretching = blinkyAnimations.animation(Ghost.ANIM_BLINKY_STRETCHED);
        blinkyDamaged = blinkyAnimations.animation(Ghost.ANIM_BLINKY_DAMAGED);
        blinky.setAnimations(blinkyAnimations);
        blinky.setSpeed(0);
        blinky.hide();
        frame = -1;
    }

    @Override
    public void end() {
    }

    @Override
    public void update() {
        ++frame;
        if (frame >= ANIMATION_START) {
            pac.move();
            blinky.move();
        }
        switch (frame) {
            case ANIMATION_START -> startMusic();
            case ANIMATION_START + 1 -> blinkyStretching.setFrameIndex(0); // Show nail
            case ANIMATION_START + 25 -> {
                pac.placeAtTile(28, 20, 0, 0);
                pac.setMoveDir(Direction.LEFT);
                pac.setSpeed(1.15f);
                pac.selectAnimation(Pac.ANIM_MUNCHING);
                pac.animations().ifPresent(Animations::startSelected);
                pac.show();
            }
            case ANIMATION_START + 111 -> {
                blinky.placeAtTile(28, 20, -3, 0);
                blinky.setMoveAndWishDir(Direction.LEFT);
                blinky.setSpeed(1.25f);
                blinky.selectAnimation(Ghost.ANIM_GHOST_NORMAL);
                blinky.startAnimation();
                blinky.show();
            }
            case ANIMATION_START + 194 -> {
                blinky.setSpeed(0.09f);
                blinkyNormal.setFrameTicks(32);
            }
            case ANIMATION_START + 198,
                 ANIMATION_START + 226,
                 ANIMATION_START + 248 -> blinkyStretching.nextFrame(); // Stretched S-M-L
            case ANIMATION_START + 328 -> {
                blinky.setSpeed(0);
                blinkyStretching.nextFrame(); // Rapture
            }
            case ANIMATION_START + 329 -> blinky.selectAnimation(Ghost.ANIM_BLINKY_DAMAGED); // Eyes up
            case ANIMATION_START + 389 -> blinkyDamaged.nextFrame(); // Eyes right-down
            case ANIMATION_START + 508 -> {
                blinky.setVisible(false);
                context.gameState().timer().expire();
            }
            default -> {}
        }
    }

    @Override
    public void drawSceneContent(GameWorldRenderer renderer) {
        renderer.drawSpriteScaled(context.spriteSheet(), blinkyStretching.currentSprite(), t(14), t(19) + 3);
        renderer.drawAnimatedEntity(blinky);
        renderer.drawAnimatedEntity(pac);
        drawLevelCounter(renderer, context.worldSizeTilesOrDefault());
    }

    @Override
    protected void drawDebugInfo(GameWorldRenderer renderer) {
        renderer.drawTileGrid(context.worldSizeTilesOrDefault());
        var text = frame < ANIMATION_START ? String.format("Wait %d", ANIMATION_START - frame) : String.format("Frame %d", frame);
        renderer.drawText(text, Color.YELLOW, Font.font("Sans", 16), t(1), t(5));
    }

    private void startMusic() {
        int number = context.gameState() == GameState.INTERMISSION_TEST
            ? GameState.INTERMISSION_TEST.getProperty("intermissionTestNumber")
            : context.game().intermissionNumber(context.game().levelNumber());
        context.sounds().playIntermissionSound(number);
    }
}