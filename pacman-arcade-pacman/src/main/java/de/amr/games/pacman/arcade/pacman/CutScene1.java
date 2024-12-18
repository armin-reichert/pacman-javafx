/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui.scene.GameScene;
import de.amr.games.pacman.ui.scene.GameScene2D;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.t;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.model.actors.Animations.*;

/**
 * @author Armin Reichert
 */
public class CutScene1 extends GameScene2D {

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

        var spriteSheet = (PacManGameSpriteSheet) context.currentGameConfig().spriteSheet();
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

                pac.placeAtTile(29, 20, 0, 0);
                pac.setMoveDir(Direction.LEFT);
                pac.setSpeed(1.25f);
                pac.selectAnimation(ANIM_PAC_MUNCHING);
                pac.startAnimation();
                pac.show();

                blinky.placeAtTile(32, 20, 0, 0);
                blinky.setMoveAndWishDir(Direction.LEFT);
                blinky.setSpeed(1.3f);
                blinky.selectAnimation(ANIM_GHOST_NORMAL);
                blinky.startAnimation();
                blinky.show();
            }
            case ANIMATION_START + 260 -> {
                blinky.placeAtTile(-2, 20, 4, 0);
                blinky.setMoveAndWishDir(Direction.RIGHT);
                blinky.setSpeed(0.75f);
                blinky.selectAnimation(ANIM_GHOST_FRIGHTENED);
                blinky.startAnimation();
            }
            case ANIMATION_START + 400 -> {
                pac.placeAtTile(-3, 18, 0, 6.5f);
                pac.setMoveDir(Direction.RIGHT);
                pac.selectAnimation(PacAnimations.ANIM_PAC_BIG);
                pac.startAnimation();
            }
            case ANIMATION_START + 632 -> context.gameState().timer().expire();
            default -> {}
        }
    }

    @Override
    public Vector2f size() {
        return ARCADE_MAP_SIZE_IN_PIXELS;
    }

    @Override
    public void drawSceneContent() {
        gr.drawAnimatedEntity(pac);
        gr.drawAnimatedEntity(blinky);
        gr.drawLevelCounter(context, size().x() - 4 * TS, size().y() - 2 * TS);
    }

    @Override
    protected void drawDebugInfo() {
        gr.drawTileGrid(size().x(), size().y());
        var text = frame < ANIMATION_START ? String.format("Wait %d", ANIMATION_START - frame) : String.format("Frame %d", frame);
        gr.drawText(text, Color.YELLOW, GameScene.DEBUG_FONT, t(1), t(5));
    }
}