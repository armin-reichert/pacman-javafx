/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.pacman.rendering.SpriteID;
import de.amr.pacmanfx.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.sound.SoundID;

import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel.createPacMan;

/**
 * Second cut scene in Arcade Pac-Man game:<br>
 * Red ghost chases Pac-Man from right to left over screen, at the middle of the screen, a nail
 * is stopping the red ghost, its dress gets stretched and eventually raptures.
 */
public class ArcadePacMan_CutScene2 extends GameScene2D {

    public enum NailDressState {
        NAIL(0), STRETCHED_SMALL(1), STRETCHED_MEDIUM(2), STRETCHED_LARGE(3), RAPTURED(4);

        NailDressState(int frame) {
            this.frame = (byte) frame;
        }

        public byte frame() {
            return frame;
        }

        private final byte frame;
    }

    public static final int TICK_ANIMATION_START = 120;
    public static final int TICK_PAC_MAN_STARTS_RUNNING = TICK_ANIMATION_START + 25;
    public static final int TICK_BLINKY_STARTS_RUNNING  = TICK_ANIMATION_START + 111;
    public static final int TICK_BLINKY_GETS_CAUGHT     = TICK_ANIMATION_START + 194;
    public static final int TICK_DRESS_STRETCHED_SMALL  = TICK_ANIMATION_START + 198;
    public static final int TICK_DRESS_STRETCHED_MEDIUM = TICK_ANIMATION_START + 230;
    public static final int TICK_DRESS_STRETCHED_LARGE  = TICK_ANIMATION_START + 262;
    public static final int TICK_BLINKY_STOPS_MOVING    = TICK_ANIMATION_START + 296;
    public static final int TICK_DRESS_RAPTURES         = TICK_ANIMATION_START + 360;
    public static final int TICK_BLINK_INSPECTS_DAMAGE  = TICK_ANIMATION_START + 420;
    public static final int TICK_ANIMATION_ENDS         = TICK_ANIMATION_START + 508;

    public final int nailX = TS * 14;
    public final int nailY = TS * 19 + 3;
    public int sceneTick;
    public Pac pacMan;
    public Ghost blinky;
    public SpriteAnimation nailDressAnimation;

    public ArcadePacMan_CutScene2(GameUI ui) {
        super(ui);
    }

    @Override
    public void onSceneStart() {
        final UIConfig uiConfig = ui.currentConfig();
        pacMan = createPacMan();
        pacMan.setAnimations(uiConfig.createPacAnimations());
        blinky = uiConfig.createGhostWithAnimations(RED_GHOST_SHADOW);
        nailDressAnimation = SpriteAnimationBuilder.builder()
            .sprites(ArcadePacMan_SpriteSheet.instance().sprites(SpriteID.RED_GHOST_STRETCHED))
            .initiallyStopped()
            .build(SpriteAnimationContainer.instance());
        sceneTick = -1;
    }

    @Override
    public void onTick(long tick) {
        if (++sceneTick < TICK_ANIMATION_START) {
            return;
        }
        switch (sceneTick) {
            case TICK_ANIMATION_START -> {
                ui.soundManager().play(SoundID.INTERMISSION_2);
                setNailDressAnimationState(NailDressState.NAIL);
            }
            case TICK_PAC_MAN_STARTS_RUNNING -> pacManStartsRunning();
            case TICK_BLINKY_STARTS_RUNNING -> blinkyStartsRunning();
            case TICK_BLINKY_GETS_CAUGHT -> blinkyGetsCaughtOnNail();
            case TICK_DRESS_STRETCHED_SMALL -> setNailDressAnimationState(NailDressState.STRETCHED_SMALL);
            case TICK_DRESS_STRETCHED_MEDIUM -> setNailDressAnimationState(NailDressState.STRETCHED_MEDIUM);
            case TICK_DRESS_STRETCHED_LARGE -> setNailDressAnimationState(NailDressState.STRETCHED_LARGE);
            case TICK_BLINKY_STOPS_MOVING -> blinkyStopsMoving();
            case TICK_DRESS_RAPTURES -> dressRaptures();
            case TICK_BLINK_INSPECTS_DAMAGE -> blinkyLooksDownToInspectDamage();
            case TICK_ANIMATION_ENDS -> animationEnds();
        }
        pacMan.move();
        blinky.move();
    }

    private void blinkyLooksDownToInspectDamage() {
        blinkyAnimation(ArcadePacMan_AnimationID.BLINKY_DAMAGED).advanceFrame();
    }

    private void animationEnds() {
        blinky.setVisible(false);
        gameContext().game().flow().state().expire();
    }

    private void dressRaptures() {
        setNailDressAnimationState(NailDressState.RAPTURED);
        blinky.setX(blinky.x() - 4);
        blinky.selectAnimation(ArcadePacMan_AnimationID.BLINKY_DAMAGED);
    }

    private void blinkyStopsMoving() {
        blinky.setSpeed(0);
        blinky.stopAnimation();
    }

    private void blinkyGetsCaughtOnNail() {
        blinky.setSpeed(0.09f);
        blinkyAnimation(ArcadePacMan_AnimationID.GHOST_NORMAL).setFrameTicks(32);
    }

    private void blinkyStartsRunning() {
        blinky.placeAtTile(28, 20, -3, 0);
        blinky.setMoveDir(Direction.LEFT);
        blinky.setWishDir(Direction.LEFT);
        blinky.setSpeed(1.25f);
        blinky.selectAnimation(ArcadePacMan_AnimationID.GHOST_NORMAL);
        blinky.playAnimation();
        blinky.show();
    }

    private void pacManStartsRunning() {
        pacMan.placeAtTile(28, 20);
        pacMan.setMoveDir(Direction.LEFT);
        pacMan.setSpeed(1.15f);
        pacMan.selectAnimation(ArcadePacMan_AnimationID.PAC_MUNCHING);
        pacMan.playAnimation();
        pacMan.show();
    }

    private void setNailDressAnimationState(NailDressState state) {
        nailDressAnimation.setCurrentFrameIndex(state.frame());
    }

    private SpriteAnimation blinkyAnimation(Object animationID) {
        return (SpriteAnimation) blinky.animations().animation(animationID);
    }
}