/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.ActorAnimationMap;
import de.amr.pacmanfx.model.actors.AnimatedActor;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_GameRenderer;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui.PacManGames_UIConfig;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;
import javafx.scene.media.MediaPlayer;

import java.util.Optional;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_MUNCHING;
import static de.amr.pacmanfx.tengen.ms_pacman.rendering.SpriteID.STORK;
import static de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel.createMsPacMan;
import static de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel.createPacMan;
import static de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_PacAnimationMap.ANIM_PAC_MAN_MUNCHING;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE;
import static de.amr.pacmanfx.ui.PacManGames.*;
import static de.amr.pacmanfx.ui.PacManGames_UI.ACTION_LET_GAME_STATE_EXPIRE;

/**
 * Intermission scene 3: "Junior".
 *
 * <p>
 * Pac-Man and Ms. Pac-Man gradually wait for a stork, who flies overhead with a little blue bundle. The stork drops the
 * bundle, which falls to the ground in front of Pac-Man and Ms. Pac-Man, and finally opens up to reveal a tiny Pac-Man.
 * (Played after rounds 9, 13, and 17)
 *
 * @author Armin Reichert
 */
public class TengenMsPacMan_CutScene3 extends GameScene2D {

    public static class Stork extends Actor implements AnimatedActor {
        private final SpriteAnimationMap animationMap;
        private boolean bagReleasedFromBeak;

        public Stork(TengenMsPacMan_SpriteSheet spriteSheet) {
            animationMap = new SpriteAnimationMap(spriteSheet);
            animationMap.setAnimation("flying",
                SpriteAnimation.build()
                    .of(spriteSheet.spriteSeq(STORK)).frameTicks(8).forever());
        }

        public void setBagReleasedFromBeak(boolean released) {
            bagReleasedFromBeak = released;
        }

        public boolean isBagReleasedFromBeak() {
            return bagReleasedFromBeak;
        }

        @Override
        public Optional<ActorAnimationMap> animations() { return Optional.of(animationMap); }
    }

    public static class Bag extends Actor implements AnimatedActor {
        private final SpriteAnimationMap animationMap;
        private boolean open;

        public Bag(TengenMsPacMan_SpriteSheet spriteSheet) {
            animationMap = new SpriteAnimationMap(spriteSheet);
            animationMap.setAnimation("junior", SpriteAnimation.build().ofSprite(spriteSheet.sprite(SpriteID.JUNIOR_PAC)).once());
            animationMap.setAnimation("bag",    SpriteAnimation.build().ofSprite(spriteSheet.sprite(SpriteID.BLUE_BAG)).once());
            setOpen(false);
        }

        @Override
        public Optional<ActorAnimationMap> animations() { return Optional.of(animationMap); }

        public void setOpen(boolean open) {
            this.open = open;
            animationMap.selectAnimation(open ? "junior" : "bag");
        }

        public boolean isOpen() {
            return open;
        }
    }

    private static final int GROUND_Y = TS * 24;
    private static final int RIGHT_BORDER = TS * 30;

    private MediaPlayer music;
    private Clapperboard clapperboard;
    private Pac pacMan;
    private Pac msPacMan;
    private Stork stork;
    private Bag flyingBag;

    private boolean darkness;
    private int t;

    @Override
    public void doInit() {
        t = -1;
        darkness = false;
        theGame().setScoreVisible(false);
        theGame().levelCounter().setPosition(sizeInPx().minus(6 * TS, 3 * TS));

        bindActionToKeyCombination(ACTION_LET_GAME_STATE_EXPIRE, theJoypad().key(JoypadButton.START));
        music = theSound().createSound("intermission.3");

        PacManGames_UIConfig config = theUI().configuration();
        var spriteSheet = (TengenMsPacMan_SpriteSheet) config.spriteSheet();

        clapperboard = new Clapperboard(spriteSheet, 3, "JUNIOR");
        clapperboard.setPosition(3 * TS, 10 * TS);
        clapperboard.setFont(arcadeFont8());
        msPacMan = createMsPacMan();
        msPacMan.setAnimations(config.createPacAnimations(msPacMan));
        pacMan = createPacMan();
        pacMan.setAnimations(config.createPacAnimations(pacMan));
        stork = new Stork(spriteSheet);
        flyingBag = new Bag(spriteSheet);
    }

    @Override
    protected void doEnd() {
        music.stop();
    }

    @Override
    public void update() {
        t += 1;
        if (t == 0) {
            clapperboard.show();
            clapperboard.startAnimation();
            music.play();
        }
        else if (t == 130) {
            pacMan.setMoveDir(Direction.RIGHT);
            pacMan.setPosition(TS * 3, GROUND_Y - 4);
            pacMan.setSpeed(0);
            pacMan.selectAnimation(ANIM_PAC_MAN_MUNCHING);
            pacMan.show();

            msPacMan.setMoveDir(Direction.RIGHT);
            msPacMan.setPosition(TS * 5, GROUND_Y - 4);
            msPacMan.setSpeed(0);
            msPacMan.selectAnimation(ANIM_PAC_MUNCHING);
            msPacMan.show();

            stork.setPosition(RIGHT_BORDER, TS * 7);
            stork.setVelocity(-0.8f, 0);
            stork.setBagReleasedFromBeak(false);
            stork.playAnimation("flying");
            stork.show();
        }
        else if (t == 240) {
            // stork releases bag, bag starts falling
            stork.setVelocity(-1f, 0); // faster, no bag to carry!
            stork.setBagReleasedFromBeak(true);
            flyingBag.setPosition(stork.x() - 15, stork.y() + 8);
            flyingBag.setVelocity(-0.5f, 0);
            flyingBag.setAcceleration(0, 0.1f);
            flyingBag.show();
        }
        else if (t == 320) {
            // reaches ground, starts bouncing
            flyingBag.setVelocity(-0.5f, flyingBag.velocity().y());
        }
        else if (t == 380) {
            flyingBag.setOpen(true);
            flyingBag.setVelocity(Vector2f.ZERO);
            flyingBag.setAcceleration(Vector2f.ZERO);
        }
        else if (t == 640) {
            darkness = true;
        }
        else if (t == 660) {
            theGameController().letCurrentGameStateExpire();
            return;
        }

        stork.move();

        if (!flyingBag.isOpen()) {
            flyingBag.move();
            Vector2f velocity = flyingBag.velocity();
            if (flyingBag.y() > GROUND_Y) {
                flyingBag.setY(GROUND_Y);
                flyingBag.setVelocity(0.9f * velocity.x(), -0.3f * velocity.y());
            }
        }

        clapperboard.tick();
    }

    @Override
    public Vector2f sizeInPx() {
        return NES_SIZE.toVector2f();
    }

    @Override
    public TengenMsPacMan_GameRenderer gr() {
        return (TengenMsPacMan_GameRenderer) gameRenderer;
    }

    @Override
    public void drawSceneContent() {
        if (!darkness) {
            gr().drawVerticalSceneBorders();
            gr().drawActor(clapperboard);
            gr().drawActor(stork);
            gr().drawActor(flyingBag);
            gr().drawActor(msPacMan);
            gr().drawActor(pacMan);
            gr().drawActor(theGame().levelCounter());
        }
    }
}