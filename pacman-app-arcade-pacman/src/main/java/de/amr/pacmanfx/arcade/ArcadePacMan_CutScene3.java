/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.model.actors.Animations;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.theGame;
import static de.amr.pacmanfx.Globals.theGameController;
import static de.amr.pacmanfx.arcade.ArcadePacMan_GameModel.createPac;
import static de.amr.pacmanfx.arcade.ArcadePacMan_GameModel.createRedGhost;
import static de.amr.pacmanfx.lib.UsefulFunctions.tiles_to_px;
import static de.amr.pacmanfx.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.ui.GameAssets.ARCADE_WHITE;
import static de.amr.pacmanfx.ui.PacManGamesEnv.*;

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
        theGame().scoreManager().setScoreVisible(true);
        pac = createPac();
        blinky = createRedGhost();
        music = theSound().createSound("intermission", 2);
        var spriteSheet = (ArcadePacMan_SpriteSheet) theUIConfig().current().spriteSheet();
        pac.setAnimations(new ArcadePacMan_PacAnimations(spriteSheet));
        blinky.setAnimations(new ArcadePacMan_GhostAnimations(spriteSheet, blinky.personality()));
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
                pac.selectAnimation(Animations.ANY_PAC_MUNCHING);
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
            case ANIMATION_START + 700 -> theGameController().letCurrentGameStateExpire();
            default -> {}
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