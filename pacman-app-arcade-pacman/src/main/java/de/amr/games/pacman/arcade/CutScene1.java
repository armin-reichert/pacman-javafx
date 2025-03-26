/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui._2d.GameRenderer;
import de.amr.games.pacman.ui._2d.GameScene2D;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.model.actors.ActorAnimations.*;
import static de.amr.games.pacman.ui.GameUI.THE_CONTEXT;
import static de.amr.games.pacman.ui.GameUI.THE_SOUND;

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
        THE_CONTEXT.setScoreVisible(true);
        pac = new Pac();
        blinky = ArcadePacMan_GameModel.blinky();

        music = THE_SOUND.makeSoundLoop("intermission");
        music.setCycleCount(2);

        var spriteSheet = (ArcadePacMan_SpriteSheet) THE_CONTEXT.currentUIConfig().spriteSheet();
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
            case ANIMATION_START + 632 -> THE_GAME_CONTROLLER.state().timer().expire();
            default -> {}
        }
    }

    @Override
    public Vector2f sizeInPx() {
        return ARCADE_MAP_SIZE_IN_PIXELS;
    }

    @Override
    public void drawSceneContent() {
        gr.drawAnimatedActor(pac);
        gr.drawAnimatedActor(blinky);
        gr.drawLevelCounter(sizeInPx().x() - 4 * TS, sizeInPx().y() - 2 * TS);
    }

    @Override
    protected void drawDebugInfo() {
        gr.drawTileGrid(sizeInPx().x(), sizeInPx().y());
        String text = frame < ANIMATION_START ? String.format("Wait %d", ANIMATION_START - frame) : String.format("Frame %d", frame);
        gr.drawText(text, Color.YELLOW, GameRenderer.DEBUG_FONT, tiles2Px(1), tiles2Px(5));
    }
}