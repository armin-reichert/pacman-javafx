/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui2d.scene.GameScene2D;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.model.actors.ActorAnimations.ANIM_PAC_MUNCHING;

/**
 * @author Armin Reichert
 */
public class CutScene3 extends GameScene2D {

    static final short ANIMATION_START = 120;

    private int frame;
    private Pac pac;
    private Ghost blinky;
    private MediaPlayer music;

    @Override
    public void bindGameActions() {}

    @Override
    public void doInit() {
        context.setScoreVisible(true);

        pac = new Pac();
        blinky = Ghost.blinky();

        music = context.sound().makeSound("intermission", 1.0, true);
        music.setCycleCount(2);

        var spriteSheet = (ArcadePacMan_SpriteSheet) context.gameConfiguration().spriteSheet();
        pac.setAnimations(new PacAnimations(spriteSheet));
        blinky.setAnimations(new GhostAnimations(spriteSheet, blinky.id()));

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
            case ANIMATION_START -> {
                music.play();
                pac.centerOverTile(vec_2i(29, 20));
                pac.setMoveDir(Direction.LEFT);
                pac.setSpeed(1.25f);
                pac.show();
                pac.selectAnimation(ANIM_PAC_MUNCHING);
                pac.startAnimation();
                blinky.centerOverTile(vec_2i(35, 20));
                blinky.setMoveAndWishDir(Direction.LEFT);
                blinky.setSpeed(1.25f);
                blinky.show();
                blinky.selectAnimation(GhostAnimations.ANIM_BLINKY_PATCHED);
                blinky.startAnimation();
            }
            case ANIMATION_START + 400 -> {
                blinky.centerOverTile(vec_2i(-1, 20));
                blinky.setMoveAndWishDir(Direction.RIGHT);
                blinky.selectAnimation(GhostAnimations.ANIM_BLINKY_NAKED);
                blinky.startAnimation();
            }
            case ANIMATION_START + 700 -> context.gameState().timer().expire();
            default -> {}
        }
    }

    @Override
    public Vector2f size() {
        return ARCADE_MAP_SIZE_IN_PIXELS;
    }

    @Override
    public void drawSceneContent() {
        gr.drawAnimatedActor(pac);
        gr.drawAnimatedActor(blinky);
        gr.drawLevelCounter(context, size().x() - 4 * TS, size().y() - 2 * TS);
    }

    @Override
    protected void drawDebugInfo() {
        gr.drawTileGrid(size().x(), size().y());
        String text = frame < ANIMATION_START ? String.format("Wait %d", ANIMATION_START - frame) : String.format("Frame %d", frame);
        gr.drawText(text, Color.YELLOW, GameScene.DEBUG_FONT, tiles2Px(1), tiles2Px(5));
    }
}