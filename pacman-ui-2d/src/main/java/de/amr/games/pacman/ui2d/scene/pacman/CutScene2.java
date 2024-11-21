/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.pacman;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.actors.Animations;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import de.amr.games.pacman.ui2d.util.SpriteAnimation;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.t;
import static de.amr.games.pacman.model.actors.Animations.*;
import static de.amr.games.pacman.model.pacman.PacManGame.ARCADE_MAP_SIZE_IN_PIXELS;

/**
 * @author Armin Reichert
 */
public class CutScene2 extends GameScene2D {

    static final short ANIMATION_START = 120;

    private int frame;
    private Pac pac;
    private Ghost blinky;
    private SpriteAnimation blinkyNormal;
    private SpriteAnimation blinkyStretching;
    private SpriteAnimation blinkyDamaged;
    private MediaPlayer music;

    @Override
    public void bindGameActions() {}

    @Override
    public void doInit() {
        context.setScoreVisible(true);

        pac = new Pac();
        blinky = Ghost.blinky();
        blinky.setSpeed(0);
        blinky.hide();

        music = context.sound().makeSound("intermission", 1.0, false);
        music.setCycleCount(1);

        var spriteSheet = (PacManGameSpriteSheet) context.currentGameSceneConfig().spriteSheet();
        pac.setAnimations(new PacAnimations(spriteSheet));

        var blinkyAnimations = new GhostAnimations(spriteSheet, blinky.id());
        blinkyNormal = blinkyAnimations.animation(ANIM_GHOST_NORMAL);
        blinkyStretching = blinkyAnimations.animation(ANIM_BLINKY_STRETCHED);
        blinkyDamaged = blinkyAnimations.animation(GhostAnimations.ANIM_BLINKY_DAMAGED);
        blinky.setAnimations(blinkyAnimations);

        frame = -1;
    }

    @Override
    protected void doEnd() {
        music.stop();
    }

    @Override
    public void update() {
        ++frame;
        if (frame >= ANIMATION_START) {
            pac.move();
            blinky.move();
        }
        switch (frame) {
            case ANIMATION_START -> music.play();
            case ANIMATION_START + 1 -> blinkyStretching.setFrameIndex(0); // Show nail
            case ANIMATION_START + 25 -> {
                pac.placeAtTile(28, 20, 0, 0);
                pac.setMoveDir(Direction.LEFT);
                pac.setSpeed(1.15f);
                pac.selectAnimation(ANIM_PAC_MUNCHING);
                pac.animations().ifPresent(Animations::startCurrentAnimation);
                pac.show();
            }
            case ANIMATION_START + 111 -> {
                blinky.placeAtTile(28, 20, -3, 0);
                blinky.setMoveAndWishDir(Direction.LEFT);
                blinky.setSpeed(1.25f);
                blinky.selectAnimation(ANIM_GHOST_NORMAL);
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
            case ANIMATION_START + 329 -> blinky.selectAnimation(GhostAnimations.ANIM_BLINKY_DAMAGED); // Eyes up
            case ANIMATION_START + 389 -> blinkyDamaged.nextFrame(); // Eyes right-down
            case ANIMATION_START + 508 -> {
                blinky.setVisible(false);
                context.gameState().timer().expire();
            }
            default -> {}
        }
    }

    @Override
    public Vector2f size() {
        return ARCADE_MAP_SIZE_IN_PIXELS;
    }

    @Override
    public void drawSceneContent(GameRenderer renderer) {
        renderer.drawSpriteScaled(blinkyStretching.currentSprite(), t(14), t(19) + 3);
        renderer.drawAnimatedEntity(blinky);
        renderer.drawAnimatedEntity(pac);
        renderer.drawLevelCounter(context, size());
    }

    @Override
    protected void drawDebugInfo(GameRenderer renderer) {
        renderer.drawTileGrid(size());
        var text = frame < ANIMATION_START ? String.format("Wait %d", ANIMATION_START - frame) : String.format("Frame %d", frame);
        renderer.drawText(text, Color.YELLOW, Font.font("Sans", 16), t(1), t(5));
    }
}