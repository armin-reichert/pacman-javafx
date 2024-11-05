/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.pacman;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.pacman.PacManArcadeGame;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.t;
import static de.amr.games.pacman.model.pacman.PacManArcadeGame.ARCADE_MAP_SIZE_IN_PIXELS;

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

        var spriteSheet = (PacManGameSpriteSheet) context.currentGameSceneConfig().spriteSheet();
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
                pac.selectAnimation(GameModel.ANIM_PAC_MUNCHING);
                pac.startAnimation();
                pac.show();

                blinky.placeAtTile(32, 20, 0, 0);
                blinky.setMoveAndWishDir(Direction.LEFT);
                blinky.setSpeed(1.3f);
                blinky.selectAnimation(GameModel.ANIM_GHOST_NORMAL);
                blinky.startAnimation();
                blinky.show();
            }
            case ANIMATION_START + 260 -> {
                blinky.placeAtTile(-2, 20, 4, 0);
                blinky.setMoveAndWishDir(Direction.RIGHT);
                blinky.setSpeed(0.75f);
                blinky.selectAnimation(GameModel.ANIM_GHOST_FRIGHTENED);
                blinky.startAnimation();
            }
            case ANIMATION_START + 400 -> {
                pac.placeAtTile(-3, 18, 0, 6.5f);
                pac.setMoveDir(Direction.RIGHT);
                pac.selectAnimation(PacManArcadeGame.ANIM_PAC_BIG);
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
    public void drawSceneContent(GameRenderer renderer) {
        renderer.drawAnimatedEntity(pac);
        renderer.drawAnimatedEntity(blinky);
        renderer.drawLevelCounter(context, size());
    }

    @Override
    protected void drawDebugInfo(GameRenderer renderer) {
        renderer.drawTileGrid(size());
        var text = frame < ANIMATION_START ? String.format("Wait %d", ANIMATION_START - frame) : String.format("Frame %d", frame);
        renderer.drawText(text, Color.YELLOW, Font.font("Sans", 16), t(1), t(5));
    }
}