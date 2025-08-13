/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.actors.ActorAnimationMap;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GameModel.createGhost;
import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GameModel.createPac;
import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig.*;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_GHOST_NORMAL;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_MUNCHING;

/**
 * Second cut scene in Arcade Pac-Man game:<br>
 * Red ghost chases Pac-Man from right to left over screen, at the middle of the screen, a nail
 * is stopping the red ghost, its dress gets stretched and eventually raptures.
 */
public class ArcadePacMan_CutScene2 extends GameScene2D {

    private static final String MUSIC_ID = "audio.intermission";
    private static final short ANIMATION_START = 120;

    private static final byte NAIL = 0, STRETCHED_S = 1, STRETCHED_M = 2, STRETCHED_L = 3, RAPTURED = 4;

    private int frame;
    private Pac pac;
    private Ghost blinky;
    private SpriteAnimation blinkyNormal;
    private SpriteAnimation blinkyDamaged;
    private SpriteAnimation nailDressRaptureAnimation;

    public ArcadePacMan_CutScene2(GameUI ui) {
        super(ui);
    }

    @Override
    public void doInit() {
        frame = -1;

        pac = createPac(null);
        pac.setAnimations(ui.currentConfig().createPacAnimations(pac));

        blinky = createGhost(null, RED_GHOST_SHADOW);
        blinky.setSpeed(0);
        blinky.hide();

        ActorAnimationMap blinkyAnimations = ui.currentConfig().createGhostAnimations(blinky);
        blinky.setAnimations(blinkyAnimations);
        blinkyNormal = (SpriteAnimation) blinkyAnimations.animation(ANIM_GHOST_NORMAL);
        nailDressRaptureAnimation = (SpriteAnimation) blinkyAnimations.animation(ANIM_BLINKY_NAIL_DRESS_RAPTURE);
        blinkyDamaged = (SpriteAnimation) blinkyAnimations.animation(ANIM_BLINKY_DAMAGED);

        actorsInZOrder.add(pac);
        actorsInZOrder.add(blinky);

        gameContext().game().theHUD().credit(false).score(true).levelCounter(true).livesCounter(false);
    }

    @Override
    protected void doEnd() {
        ui.soundManager().stop(MUSIC_ID);
        Logger.info("{} ends", getClass().getSimpleName());
    }

    @Override
    public void update() {
        ++frame;
        if (frame < ANIMATION_START) {
            return;
        }
        switch (frame) {
            case ANIMATION_START -> ui.soundManager().play(MUSIC_ID);
            case ANIMATION_START + 1 -> nailDressRaptureAnimation.setFrameIndex(NAIL);
            case ANIMATION_START + 25 -> {
                pac.placeAtTile(28, 20);
                pac.setMoveDir(Direction.LEFT);
                pac.setSpeed(1.15f);
                pac.playAnimation(ANIM_PAC_MUNCHING);
                pac.show();
            }
            case ANIMATION_START + 111 -> {
                blinky.placeAtTile(28, 20, -3, 0);
                blinky.setMoveDir(Direction.LEFT);
                blinky.setWishDir(Direction.LEFT);
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
                blinky.setX(blinky.x() - 4);
                blinky.selectAnimation(ANIM_BLINKY_DAMAGED);
            }
            case ANIMATION_START + 420 -> blinkyDamaged.nextFrame(); // Eyes right-down
            case ANIMATION_START + 508 -> {
                blinky.setVisible(false);
                gameContext().gameController().letCurrentGameStateExpire();
            }
            default -> {}
        }
        pac.move();
        blinky.move();
    }

    @Override
    public Vector2f sizeInPx() { return ARCADE_MAP_SIZE_IN_PIXELS; }

    @Override
    public void drawSceneContent() {
        gameRenderer.drawSpriteScaled(nailDressRaptureAnimation.currentSprite(), TS(14), TS(19) + 3);
        gameRenderer.drawActors(actorsInZOrder);
    }

    @Override
    protected void drawDebugInfo() {
        super.drawDebugInfo();
        String text = frame < ANIMATION_START ? String.format("Wait %d", ANIMATION_START - frame) : String.format("Frame %d", frame);
        gameRenderer.fillTextAtScaledPosition(text, debugTextFill, debugTextFont, TS(1), TS(5));
    }
}