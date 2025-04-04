/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.ms_pacman;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.actors.Actor2D;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui._2d.GameScene2D;
import de.amr.games.pacman.uilib.SpriteAnimation;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.model.actors.ActorAnimations.ANIM_MR_PACMAN_MUNCHING;
import static de.amr.games.pacman.model.actors.ActorAnimations.ANIM_PAC_MUNCHING;
import static de.amr.games.pacman.ui.Globals.THE_UI;

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
public class CutScene3 extends GameScene2D {

    private static final int LANE_Y = TS * 24;

    private Pac pacMan;
    private Pac msPacMan;
    private Actor2D stork;
    private Actor2D bag;
    private boolean bagOpen;
    private int numBagBounces;

    private MediaPlayer music;
    private ClapperboardAnimation clapAnimation;
    private SpriteAnimation storkAnimation;

    @Override
    public void bindGameActions() {}

    @Override
    public void doInit() {
        game().setScoreVisible(true);

        pacMan = new Pac();
        msPacMan = new Pac();
        stork = new Actor2D();
        bag = new Actor2D();

        music = THE_UI.sound().makeSound("intermission.3");
        var spriteSheet = (ArcadeMsPacMan_SpriteSheet) THE_UI.configurations().current().spriteSheet();
        msPacMan.setAnimations(new PacAnimations(spriteSheet));
        pacMan.setAnimations(new PacAnimations(spriteSheet));

        storkAnimation = spriteSheet.createStorkFlyingAnimation();
        storkAnimation.play();

        clapAnimation = new ClapperboardAnimation("3", "JUNIOR");
        clapAnimation.start();

        setSceneState(STATE_FLAP, TickTimer.INDEFINITE);
    }

    @Override
    protected void doEnd() {
        music.stop();
    }

    @Override
    public void update() {
        updateSceneState();
    }

    @Override
    public Vector2f sizeInPx() {
        return ARCADE_MAP_SIZE_IN_PIXELS;
    }

    @Override
    public void drawSceneContent() {
        gr.setScaling(scaling());
        gr.clearCanvas(backgroundColor());
        if (game().isScoreVisible()) {
            gr.drawScores(Color.web(Arcade.Palette.WHITE), gr.scaledArcadeFont(TS));
        }
        if (gr instanceof ArcadeMsPacMan_GameRenderer r) {
            r.drawClapperBoard(clapAnimation, tiles2Px(3), tiles2Px(10));
        }
        gr.drawAnimatedActor(msPacMan);
        gr.drawAnimatedActor(pacMan);
        gr.drawActorSprite(stork, storkAnimation.currentSprite());
        gr.drawActorSprite(bag, bagOpen ? ArcadeMsPacMan_SpriteSheet.JUNIOR_PAC_SPRITE : ArcadeMsPacMan_SpriteSheet.BLUE_BAG_SPRITE);
        gr.drawLevelCounter(sizeInPx().x() - 4 * TS, sizeInPx().y() - 2 * TS);
    }

    // scene controller state machine

    private static final byte STATE_FLAP = 0;
    private static final byte STATE_DELIVER_JUNIOR = 1;
    private static final byte STATE_STORK_LEAVES_SCENE = 2;

    private byte state;
    private final TickTimer stateTimer = new TickTimer("MsPacManCutScene3");

    private void setSceneState(byte state, long ticks) {
        this.state = state;
        stateTimer.reset(ticks);
        stateTimer.start();
    }

    private void updateSceneState() {
        switch (state) {
            case STATE_FLAP -> updateStateFlap();
            case STATE_DELIVER_JUNIOR -> updateStateDeliverJunior();
            case STATE_STORK_LEAVES_SCENE -> updateStateStorkLeavesScene();
            default -> throw new IllegalStateException("Illegal state: " + state);
        }
        stateTimer.doTick();
    }

    private void updateStateFlap() {
        clapAnimation.tick();
        if (stateTimer.atSecond(1)) {
            music.play();
        } else if (stateTimer.atSecond(3)) {
            enterStateDeliverJunior();
        }
    }

    private void enterStateDeliverJunior() {
        pacMan.setMoveDir(Direction.RIGHT);
        pacMan.setPosition(TS * 3, LANE_Y - 4);
        pacMan.selectAnimation(ANIM_MR_PACMAN_MUNCHING);
        pacMan.show();

        msPacMan.setMoveDir(Direction.RIGHT);
        msPacMan.setPosition(TS * 5, LANE_Y - 4);
        msPacMan.selectAnimation(ANIM_PAC_MUNCHING);
        msPacMan.show();

        stork.setPosition(TS * 30, TS * 12);
        stork.setVelocity(-0.8f, 0);
        stork.show();

        bag.setPosition(stork.position().plus(-14, 3));
        bag.setVelocity(stork.velocity());
        bag.setAcceleration(Vector2f.ZERO);
        bag.show();
        bagOpen = false;
        numBagBounces = 0;

        setSceneState(STATE_DELIVER_JUNIOR, TickTimer.INDEFINITE);
    }

    private void updateStateDeliverJunior() {
        stork.move();
        bag.move();

        // release bag from storks beak?
        if (stork.tile().x() == 20) {
            bag.setAcceleration(0, 0.04f); // gravity
            stork.setVelocity(-1, 0);
        }

        // (closed) bag reaches ground for first time?
        if (!bagOpen && bag.posY() > LANE_Y) {
            ++numBagBounces;
            if (numBagBounces < 3) {
                bag.setVelocity(-0.2f, -1f / numBagBounces);
                bag.setPosY(LANE_Y);
            } else {
                bagOpen = true;
                bag.setVelocity(Vector2f.ZERO);
                setSceneState(STATE_STORK_LEAVES_SCENE, 3 * 60);
            }
        }
    }

    private void updateStateStorkLeavesScene() {
        stork.move();
        if (stateTimer.hasExpired()) {
            THE_GAME_CONTROLLER.terminateCurrentState();
        }
    }
}