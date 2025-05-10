/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.ms_pacman;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.actors.Actor;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.actors.PacAnimations;
import de.amr.games.pacman.ui._2d.GameScene2D;
import de.amr.games.pacman.uilib.animation.SpriteAnimation;
import javafx.scene.media.MediaPlayer;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.arcade.ms_pacman.ArcadeMsPacMan_GameModel.createMsPacMan;
import static de.amr.games.pacman.arcade.ms_pacman.ArcadeMsPacMan_GameModel.createPacMan;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.ui.GameAssets.ARCADE_WHITE;
import static de.amr.games.pacman.ui.Globals.THE_SOUND;
import static de.amr.games.pacman.ui.Globals.THE_UI_CONFIGS;

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
public class ArcadeMsPacMan_CutScene3 extends GameScene2D {

    private static final int LANE_Y = TS * 24;

    private Pac pacMan;
    private Pac msPacMan;
    private Actor stork;
    private Actor bag;
    private boolean bagOpen;
    private int numBagBounces;

    private MediaPlayer music;
    private ClapperboardAnimation clapperboardAnimation;
    private SpriteAnimation storkAnimation;

    @Override
    public void doInit() {
        game().scoreManager().setScoreVisible(true);

        pacMan = createPacMan();
        msPacMan = createMsPacMan();
        stork = new Actor();
        bag = new Actor();

        ArcadeMsPacMan_SpriteSheet  spriteSheet = THE_UI_CONFIGS.current().spriteSheet();
        msPacMan.setAnimations(new ArcadeMsPacMan_PacAnimations(spriteSheet));
        pacMan.setAnimations(new ArcadeMsPacMan_PacAnimations(spriteSheet));

        storkAnimation = spriteSheet.createStorkFlyingAnimation();
        storkAnimation.play();

        clapperboardAnimation = new ClapperboardAnimation("3", "JUNIOR");
        clapperboardAnimation.start();

        music = THE_SOUND.createSound("intermission.3");
        setSceneState(STATE_CLAPPERBOARD, TickTimer.INDEFINITE);
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
        gr.drawScores(game().scoreManager(), ARCADE_WHITE, arcadeFontScaledTS());
        if (gr instanceof ArcadeMsPacMan_GameRenderer r) {
            // Note: in Ms. Pac-Man XXL another renderer is used!
            r.drawClapperBoard(clapperboardAnimation, tiles_to_px(3), tiles_to_px(10), arcadeFontScaledTS());
        }
        gr.drawAnimatedActor(msPacMan);
        gr.drawAnimatedActor(pacMan);
        gr.drawActorSprite(stork, storkAnimation.currentSprite());
        gr.drawActorSprite(bag, bagOpen ? ArcadeMsPacMan_SpriteSheet.JUNIOR_PAC_SPRITE : ArcadeMsPacMan_SpriteSheet.BLUE_BAG_SPRITE);
        gr.drawLevelCounter(game().levelCounter(), sizeInPx());
    }

    // Scene controller state machine

    private static final byte STATE_CLAPPERBOARD = 0;
    private static final byte STATE_DELIVER_JUNIOR = 1;
    private static final byte STATE_STORK_LEAVES_SCENE = 2;

    private byte sceneState;
    private final TickTimer sceneTimer = new TickTimer("MsPacManCutScene3");

    private void setSceneState(byte state, long ticks) {
        this.sceneState = state;
        sceneTimer.reset(ticks);
        sceneTimer.start();
    }

    private void updateSceneState() {
        switch (sceneState) {
            case STATE_CLAPPERBOARD -> updateStateClapperboard();
            case STATE_DELIVER_JUNIOR -> updateStateDeliverJunior();
            case STATE_STORK_LEAVES_SCENE -> updateStateStorkLeavesScene();
            default -> throw new IllegalStateException("Illegal state: " + sceneState);
        }
        sceneTimer.doTick();
    }

    private void updateStateClapperboard() {
        clapperboardAnimation.tick();
        if (sceneTimer.atSecond(1)) {
            music.play();
        } else if (sceneTimer.atSecond(3)) {
            enterStateDeliverJunior();
        }
    }

    private void enterStateDeliverJunior() {
        pacMan.setMoveDir(Direction.RIGHT);
        pacMan.setPosition(TS * 3, LANE_Y - 4);
        pacMan.selectAnimation(ArcadeMsPacMan_PacAnimations.ANIM_PAC_MAN_MUNCHING);
        pacMan.show();

        msPacMan.setMoveDir(Direction.RIGHT);
        msPacMan.setPosition(TS * 5, LANE_Y - 4);
        msPacMan.selectAnimation(PacAnimations.ANIM_MUNCHING);
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
        if (sceneTimer.hasExpired()) {
            THE_GAME_CONTROLLER.letCurrentStateExpire();
        }
    }
}