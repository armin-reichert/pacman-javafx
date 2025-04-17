/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui._2d.GameScene2D;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.model.actors.ActorAnimations.ANIM_PAC_MUNCHING;
import static de.amr.games.pacman.ui.Globals.*;

/**
 * @author Armin Reichert
 */
public class ArcadePacMan_CutScene3 extends GameScene2D {

    static final short ANIMATION_START = 120;

    private int frame;
    private Pac pac;
    private Ghost blinky;
    private MediaPlayer music;

    @Override
    public void doInit() {
        game().scoreVisibleProperty().set(true);

        pac = new Pac();
        blinky = ArcadePacMan_GameModel.blinky();

        music = THE_SOUND.makeSoundLoop("intermission");
        music.setCycleCount(2);

        var spriteSheet = (ArcadePacMan_SpriteSheet) THE_UI_CONFIGS.current().spriteSheet();
        pac.setAnimations(new ArcadePacMan_PacAnimations(spriteSheet));
        blinky.setAnimations(new ArcadePacMan_GhostAnimations(spriteSheet, blinky.id()));

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
                pac.centerOverTile(Vector2i.of(29, 20));
                pac.setMoveDir(Direction.LEFT);
                pac.setSpeed(1.25f);
                pac.show();
                pac.selectAnimation(ANIM_PAC_MUNCHING);
                pac.startAnimation();
                blinky.centerOverTile(Vector2i.of(35, 20));
                blinky.setMoveAndWishDir(Direction.LEFT);
                blinky.setSpeed(1.25f);
                blinky.show();
                blinky.selectAnimation(ArcadePacMan_GhostAnimations.ANIM_BLINKY_PATCHED);
                blinky.startAnimation();
            }
            case ANIMATION_START + 400 -> {
                blinky.centerOverTile(Vector2i.of(-1, 20));
                blinky.setMoveAndWishDir(Direction.RIGHT);
                blinky.selectAnimation(ArcadePacMan_GhostAnimations.ANIM_BLINKY_NAKED);
                blinky.startAnimation();
            }
            case ANIMATION_START + 700 -> THE_GAME_CONTROLLER.terminateCurrentState();
            default -> {}
        }
    }

    @Override
    public Vector2f sizeInPx() {
        return ARCADE_MAP_SIZE_IN_PIXELS;
    }

    @Override
    public void drawSceneContent() {
        gr.setScaling(scaling());
        gr.fillCanvas(backgroundColor());
        if (game().isScoreVisible()) {
            Font font = THE_ASSETS.arcadeFontAtSize(scaled(TS));
            gr.drawScores(game().scoreManager(), Color.web(Arcade.Palette.WHITE), font);
        }
        gr.drawAnimatedActor(pac);
        gr.drawAnimatedActor(blinky);
        gr.drawLevelCounter(game().levelCounter(), sizeInPx());
    }

    @Override
    protected void drawDebugInfo() {
        super.drawDebugInfo();
        String text = frame < ANIMATION_START ? String.format("Wait %d", ANIMATION_START - frame) : String.format("Frame %d", frame);
        gr.fillTextAtScaledPosition(text, Color.YELLOW, DEBUG_TEXT_FONT, tiles_to_px(1), tiles_to_px(5));
    }
}