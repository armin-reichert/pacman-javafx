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
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.Globals.theGame;
import static de.amr.pacmanfx.Globals.theGameController;
import static de.amr.pacmanfx.arcade.ArcadePacMan_GameModel.createPac;
import static de.amr.pacmanfx.arcade.ArcadePacMan_GameModel.createRedGhost;
import static de.amr.pacmanfx.arcade.ArcadePacMan_UIConfig.ANIM_BLINKY_DAMAGED;
import static de.amr.pacmanfx.arcade.ArcadePacMan_UIConfig.ANIM_BLINKY_NAIL_DRESS_RAPTURE;
import static de.amr.pacmanfx.lib.UsefulFunctions.tiles_to_px;
import static de.amr.pacmanfx.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_ANY_PAC_MUNCHING;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_GHOST_NORMAL;
import static de.amr.pacmanfx.ui.PacManGamesEnv.*;

public class ArcadePacMan_CutScene2 extends GameScene2D {

    private static final short ANIMATION_START = 120;

    private static final byte NAIL = 0, STRETCHED_S = 1, STRETCHED_M = 2, STRETCHED_L = 3, RAPTURED = 4;

    private int frame;
    private Pac pac;
    private Ghost blinky;
    private SpriteAnimation blinkyNormal;
    private SpriteAnimation blinkyDamaged;
    private SpriteAnimation nailDressRaptureAnimation;
    private MediaPlayer music;

    @Override
    public void doInit() {
        theGame().scoreManager().setScoreVisible(true);
        pac = createPac();
        blinky = createRedGhost();
        blinky.setSpeed(0);
        blinky.hide();
        music = theSound().createSound("intermission");
        ArcadePacMan_SpriteSheet spriteSheet = theUIConfig().current().spriteSheet();
        pac.setAnimations(new ArcadePacMan_PacAnimationMap(spriteSheet));
        var blinkyAnimations = new ArcadePacMan_GhostAnimationMap(spriteSheet, blinky.personality());
        blinkyNormal = blinkyAnimations.animation(ANIM_GHOST_NORMAL);
        nailDressRaptureAnimation = blinkyAnimations.animation(ANIM_BLINKY_NAIL_DRESS_RAPTURE);
        blinkyDamaged = blinkyAnimations.animation(ANIM_BLINKY_DAMAGED);
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
        if (frame < ANIMATION_START) {
            return;
        }
        switch (frame) {
            case ANIMATION_START -> music.play();
            case ANIMATION_START + 1 -> nailDressRaptureAnimation.setFrameIndex(NAIL);
            case ANIMATION_START + 25 -> {
                pac.placeAtTile(28, 20, 0, 0);
                pac.setMoveDir(Direction.LEFT);
                pac.setSpeed(1.15f);
                pac.playAnimation(ANIM_ANY_PAC_MUNCHING);
                pac.show();
            }
            case ANIMATION_START + 111 -> {
                blinky.placeAtTile(28, 20, -3, 0);
                blinky.setMoveAndWishDir(Direction.LEFT);
                blinky.setSpeed(1.25f);
                blinky.playAnimation(ANIM_GHOST_NORMAL);
                blinky.show();
            }
            case ANIMATION_START + 194 -> {
                blinky.setSpeed(0.09f);
                blinkyNormal.setFrameTicks(32);
            }
            case ANIMATION_START + 198 -> nailDressRaptureAnimation.setFrameIndex(STRETCHED_S);
            case ANIMATION_START + 230 -> nailDressRaptureAnimation.setFrameIndex(STRETCHED_M);
            case ANIMATION_START + 262 -> nailDressRaptureAnimation.setFrameIndex(STRETCHED_L);
            case ANIMATION_START + 296 -> {
                blinky.setSpeed(0);
                blinky.stopAnimation();
            }
            case ANIMATION_START + 360 -> {
                nailDressRaptureAnimation.setFrameIndex(RAPTURED);
                blinky.setPosition(blinky.position().minus(5, 0));
                blinky.selectAnimation(ANIM_BLINKY_DAMAGED);
            }
            case ANIMATION_START + 420 -> blinkyDamaged.nextFrame(); // Eyes right-down
            case ANIMATION_START + 508 -> {
                blinky.setVisible(false);
                theGameController().letCurrentGameStateExpire();
            }
            default -> {}
        }
        pac.move();
        blinky.move();
    }

    @Override
    public Vector2f sizeInPx() {
        return ARCADE_MAP_SIZE_IN_PIXELS;
    }

    @Override
    public void drawSceneContent() {
        gr().drawSpriteScaled(nailDressRaptureAnimation.currentSprite(), tiles_to_px(14), tiles_to_px(19) + 3);
        gr().drawActor(blinky);
        gr().drawActor(pac);
        gr().drawLevelCounter(theGame().levelCounter(), sizeInPx());
    }

    @Override
    protected void drawDebugInfo() {
        super.drawDebugInfo();
        String text = frame < ANIMATION_START ? String.format("Wait %d", ANIMATION_START - frame) : String.format("Frame %d", frame);
        gr().fillText(text, Color.YELLOW, DEBUG_TEXT_FONT, tiles_to_px(1), tiles_to_px(5));
    }
}