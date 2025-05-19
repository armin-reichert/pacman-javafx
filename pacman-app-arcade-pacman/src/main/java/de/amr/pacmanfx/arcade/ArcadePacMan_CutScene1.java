/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.theGame;
import static de.amr.pacmanfx.Globals.theGameController;
import static de.amr.pacmanfx.arcade.ArcadePacMan_GameModel.createPac;
import static de.amr.pacmanfx.arcade.ArcadePacMan_GameModel.createRedGhost;
import static de.amr.pacmanfx.arcade.ArcadePacMan_UIConfig.ANIM_BIG_PAC_MAN;
import static de.amr.pacmanfx.lib.UsefulFunctions.tiles_to_px;
import static de.amr.pacmanfx.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.*;
import static de.amr.pacmanfx.ui.GameAssets.ARCADE_WHITE;
import static de.amr.pacmanfx.ui.PacManGamesEnv.*;

/**
 * @author Armin Reichert
 */
public class ArcadePacMan_CutScene1 extends GameScene2D {

    private static final short ANIMATION_START = 120;

    private int frame;
    private Pac pac;
    private Ghost blinky;
    private MediaPlayer music;

    @Override
    public void doInit() {
        theGame().scoreManager().setScoreVisible(true);
        pac = createPac();
        blinky = createRedGhost();
        music = theSound().createSound("intermission", 2);
        ArcadePacMan_SpriteSheet spriteSheet = theUIConfig().current().spriteSheet();
        pac.setAnimations(new ArcadePacMan_PacAnimationSet(spriteSheet));
        blinky.setAnimations(new ArcadePacMan_GhostAnimationSet(spriteSheet, blinky.personality()));
        frame = -1;
    }

    @Override
    protected void doEnd() {
        music.stop();
    }

    @Override
    public void update() {
        ++frame;
        if (frame == ANIMATION_START) {
            music.play();

            pac.placeAtTile(29, 20, 0, 0);
            pac.setMoveDir(Direction.LEFT);
            pac.setSpeed(1.25f);
            pac.playAnimation(ANIM_ANY_PAC_MUNCHING);
            pac.show();

            blinky.placeAtTile(32, 20, 0, 0);
            blinky.setMoveAndWishDir(Direction.LEFT);
            blinky.setSpeed(1.3f);
            blinky.playAnimation(ANIM_GHOST_NORMAL);
            blinky.show();
        }
        else if (frame == ANIMATION_START + 260) {
            blinky.placeAtTile(-2, 20, 4, 0);
            blinky.setMoveAndWishDir(Direction.RIGHT);
            blinky.setSpeed(0.75f);
            blinky.playAnimation(ANIM_GHOST_FRIGHTENED);
        }
        else if (frame == ANIMATION_START + 400) {
            pac.placeAtTile(-3, 18, 0, 6.5f);
            pac.setMoveDir(Direction.RIGHT);
            pac.playAnimation(ANIM_BIG_PAC_MAN);
        }
        else if (frame == ANIMATION_START + 632) {
             theGameController().letCurrentGameStateExpire();
        }
        if (frame >= ANIMATION_START) {
            pac.move();
            blinky.move();
        }
    }

    @Override
    public Vector2f sizeInPx() {
        return ARCADE_MAP_SIZE_IN_PIXELS;
    }

    @Override
    public void drawSceneContent() {
        gr.drawScores(theGame().scoreManager(), ARCADE_WHITE, arcadeFontScaledTS());
        gr.drawActor(pac);
        gr.drawActor(blinky);
        gr.drawLevelCounter(theGame().levelCounter(), sizeInPx());
    }

    @Override
    protected void drawDebugInfo() {
        super.drawDebugInfo();
        String text = frame < ANIMATION_START ? String.format("Wait %d", ANIMATION_START - frame) : String.format("Frame %d", frame);
        gr.fillTextAtScaledPosition(text, Color.YELLOW, DEBUG_TEXT_FONT, tiles_to_px(1), tiles_to_px(5));
    }
}