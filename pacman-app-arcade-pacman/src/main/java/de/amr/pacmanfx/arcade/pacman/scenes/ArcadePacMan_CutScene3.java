/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import javafx.scene.media.MediaPlayer;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GameModel.createGhost;
import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GameModel.createPac;
import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig.*;
import static de.amr.pacmanfx.lib.UsefulFunctions.tiles_to_px;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_MUNCHING;
import static de.amr.pacmanfx.ui.PacManGames.theSound;
import static de.amr.pacmanfx.ui.PacManGames.theUI;
import static de.amr.pacmanfx.ui.PacManGames_UI.DEBUG_TEXT_FILL;
import static de.amr.pacmanfx.ui.PacManGames_UI.DEBUG_TEXT_FONT;

/**
 * Third cut scene in Arcade Pac-Man game:<br>
 * Red ghost in damaged dress chases Pac-Man from right to left over the screen.
 * After they have disappeared, a naked, shaking ghost runs from left to right over the screen.
 */
public class ArcadePacMan_CutScene3 extends GameScene2D {

    static final short ANIMATION_START = 120;

    private int frame;
    private Pac pac;
    private Ghost blinky;
    private MediaPlayer music;

    @Override
    public void doInit() {
        theGame().setScoreVisible(true);
        //theGame().levelCounter().setPosition(sizeInPx().x() - 4 * TS, sizeInPx().y() - 2 * TS);
        pac = createPac();
        blinky = createGhost(RED_GHOST_SHADOW);
        music = theSound().createSound("intermission", 2);

        pac.setAnimations(theUI().configuration().createPacAnimations(pac));
        blinky.setAnimations(theUI().configuration().createGhostAnimations(blinky));

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
                pac.placeAtTile(29, 20);
                pac.setMoveDir(Direction.LEFT);
                pac.setSpeed(1.25f);
                pac.show();
                pac.playAnimation(ANIM_PAC_MUNCHING);
                blinky.placeAtTile(35, 20);
                blinky.setMoveAndWishDir(Direction.LEFT);
                blinky.setSpeed(1.25f);
                blinky.show();
                blinky.playAnimation(ANIM_BLINKY_PATCHED);
            }
            case ANIMATION_START + 400 -> {
                blinky.placeAtTile(-1, 20);
                blinky.setMoveAndWishDir(Direction.RIGHT);
                blinky.playAnimation(ANIM_BLINKY_NAKED);
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
        gr().drawActor(pac);
        gr().drawActor(blinky);
        gr().drawHUD(theGame());
    }

    @Override
    protected void drawDebugInfo() {
        super.drawDebugInfo();
        String text = frame < ANIMATION_START ? String.format("Wait %d", ANIMATION_START - frame) : String.format("Frame %d", frame);
        gr().fillText(text, DEBUG_TEXT_FILL, DEBUG_TEXT_FONT, tiles_to_px(1), tiles_to_px(5));
    }
}