/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.basics.Identifier;
import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationBuilder;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.pacman.rendering.SpriteID;
import de.amr.pacmanfx.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.ui.GameVariant;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;

import static de.amr.pacmanfx.model.GameModel.RED_GHOST_SHADOW;

/**
 * Second cut scene in Arcade Pac-Man game:<br>
 * Red ghost chases Pac-Man from right to left over screen, at the middle of the screen, a nail
 * is stopping the red ghost, its dress gets stretched and eventually raptures.
 */
public class ArcadePacMan_CutScene2 extends AbstractGameScene2D {

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

    public final int nailX = WorldMap.TS * 14;
    public final int nailY = WorldMap.TS * 19 + 3;
    public int sceneTick;
    public Pac pacMan;
    public Ghost blinky;
    public SpriteAnimation nailDressAnimation;

    public ArcadePacMan_CutScene2(Game game) {
        super(game);
    }

    @Override
    public void onActivate() {
        final GameVariant gameVariant = game().variant();
        final SpriteAnimationContainer spriteAnimationContainer = game().ui().sprites().animations();
        final ArcadePacMan_SpriteSheet spriteSheet = ArcadePacMan_SpriteSheet.instance();

        pacMan = ArcadePacMan_ActorFactory.createPacMan();
        pacMan.setAnimations(gameVariant.createPacAnimations(spriteAnimationContainer));

        blinky = gameVariant.createAnimatedGhost(spriteAnimationContainer, RED_GHOST_SHADOW);

        nailDressAnimation = new SpriteAnimationBuilder()
            .sprites(spriteSheet.sprites(SpriteID.RED_GHOST_STRETCHED))
            .initiallyStopped()
            .build(spriteAnimationContainer);

        sceneTick = -1;
    }

    @Override
    public void onTick(long tick) {
        if (++sceneTick < TICK_ANIMATION_START) {
            return;
        }
        switch (sceneTick) {
            case TICK_ANIMATION_START -> {
                game().ui().sounds().play(PacManGameSoundID.INTERMISSION_2);
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
        gameState().triggerTimeout();
    }

    private void dressRaptures() {
        setNailDressAnimationState(NailDressState.RAPTURED);
        blinky.setX(blinky.x() - 4);
        blinky.animations().select(ArcadePacMan_AnimationID.BLINKY_DAMAGED);
    }

    private void blinkyStopsMoving() {
        blinky.setSpeed(0);
        blinky.animations().stopSelected();
    }

    private void blinkyGetsCaughtOnNail() {
        blinky.setSpeed(0.09f);
        blinkyAnimation(ArcadePacMan_AnimationID.GHOST_NORMAL).setFrameDurationTicks(32);
    }

    private void blinkyStartsRunning() {
        blinky.placeAtTile(28, 20, -3, 0);
        blinky.setMoveDir(Direction.LEFT);
        blinky.setWishDir(Direction.LEFT);
        blinky.setSpeed(1.25f);
        blinky.animations().select(ArcadePacMan_AnimationID.GHOST_NORMAL);
        blinky.animations().playSelected();
        blinky.show();
    }

    private void pacManStartsRunning() {
        pacMan.placeAtTile(28, 20);
        pacMan.setMoveDir(Direction.LEFT);
        pacMan.setSpeed(1.15f);
        pacMan.animations().select(ArcadePacMan_AnimationID.PAC_MUNCHING);
        pacMan.animations().playSelected();
        pacMan.show();
    }

    private void setNailDressAnimationState(NailDressState state) {
        nailDressAnimation.setFrame(state.frame());
    }

    private SpriteAnimation blinkyAnimation(Identifier animationID) {
        return (SpriteAnimation) blinky.animations().animation(animationID);
    }
}