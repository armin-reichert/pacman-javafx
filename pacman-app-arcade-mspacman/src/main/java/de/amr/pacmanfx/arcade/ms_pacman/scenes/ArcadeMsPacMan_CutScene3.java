/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_ActorRenderer;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_HUDRenderer;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;

import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GameModel.createMsPacMan;
import static de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_GameModel.createPacMan;
import static de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_PacAnimationManager.PAC_MAN_MUNCHING;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_MUNCHING;

/**
 * Intermission scene 3: "Junior".
 *
 * <p>
 * Pac-Man and Ms. Pac-Man gradually wait for a stork, who flies overhead with a little blue bundle. The stork drops the
 * bundle, which falls to the ground in front of Pac-Man and Ms. Pac-Man, and finally opens up to reveal a tiny Pac-Man.
 * (Played after rounds 9, 13, and 17)
 */
public class ArcadeMsPacMan_CutScene3 extends GameScene2D {

    private static final String MUSIC_ID = "audio.intermission.3";

    private static final int LANE_Y = TS * 24;

    private Pac pacMan;
    private Pac msPacMan;
    private Stork stork;
    private Bag bag;
    private int numBagBounces;

    private Clapperboard clapperboard;

    private ArcadeMsPacMan_HUDRenderer hudRenderer;
    private ArcadeMsPacMan_ActorRenderer actorRenderer;

    public ArcadeMsPacMan_CutScene3(GameUI ui) {
        super(ui);
    }
    
    @Override
    public void doInit() {
        GameUI_Config uiConfig = ui.currentConfig();

        hudRenderer = (ArcadeMsPacMan_HUDRenderer) uiConfig.createHUDRenderer(canvas);
        actorRenderer = (ArcadeMsPacMan_ActorRenderer) uiConfig.createActorRenderer(canvas);

        bindRendererProperties(hudRenderer, actorRenderer);

        var spriteSheet = (ArcadeMsPacMan_SpriteSheet) uiConfig.spriteSheet();

        context().game().hud().scoreVisible(true).levelCounterVisible(true).livesCounterVisible(false);

        pacMan = createPacMan();
        pacMan.setAnimations(uiConfig.createPacAnimations(pacMan));

        msPacMan = createMsPacMan();
        msPacMan.setAnimations(uiConfig.createPacAnimations(msPacMan));

        stork = new Stork(spriteSheet);

        bag = new Bag(spriteSheet);
        bag.setOpen(false);

        clapperboard = new Clapperboard("3", "JUNIOR");
        clapperboard.setPosition(TS(3), TS(10));
        clapperboard.setFont(sceneRenderer.arcadeFontTS());
        clapperboard.startAnimation();

        setSceneState(STATE_CLAPPERBOARD, TickTimer.INDEFINITE);
    }

    @Override
    protected void doEnd() {
        ui.soundManager().stop(MUSIC_ID);
    }

    @Override
    public void update() {
        switch (sceneState) {
            case STATE_CLAPPERBOARD -> updateStateClapperboard();
            case STATE_DELIVER_JUNIOR -> updateStateDeliverJunior();
            case STATE_STORK_LEAVES_SCENE -> updateStateStorkLeavesScene();
            default -> throw new IllegalStateException("Illegal scene state: " + sceneState);
        }
        sceneTimer.doTick();
    }

    @Override
    public Vector2f sizeInPx() {
        return ARCADE_MAP_SIZE_IN_PIXELS;
    }

    @Override
    public void drawHUD() {
        if (hudRenderer != null) {
            hudRenderer.drawHUD(context().game(), context().game().hud(), sizeInPx());
        }
    }

    @Override
    public void drawSceneContent() {
        if (actorRenderer != null) {
            Stream.of(clapperboard, msPacMan, pacMan, stork, bag).forEach(actorRenderer::drawActor);
        }
    }

    // Scene controller state machine

    private static final byte STATE_CLAPPERBOARD = 0;
    private static final byte STATE_DELIVER_JUNIOR = 1;
    private static final byte STATE_STORK_LEAVES_SCENE = 2;

    private byte sceneState;
    private final TickTimer sceneTimer = new TickTimer("MsPacMan_CutScene3");

    private void setSceneState(byte state, long ticks) {
        sceneState = state;
        sceneTimer.reset(ticks);
        sceneTimer.start();
    }

    private void updateStateClapperboard() {
        clapperboard.tick();
        if (sceneTimer.atSecond(1)) {
            ui.soundManager().play(MUSIC_ID);
        } else if (sceneTimer.atSecond(3)) {
            enterStateDeliverJunior();
        }
    }

    private void enterStateDeliverJunior() {
        pacMan.setMoveDir(Direction.RIGHT);
        pacMan.setPosition(TS * 3, LANE_Y - 4);
        pacMan.selectAnimation(PAC_MAN_MUNCHING);
        pacMan.show();

        msPacMan.setMoveDir(Direction.RIGHT);
        msPacMan.setPosition(TS * 5, LANE_Y - 4);
        msPacMan.selectAnimation(ANIM_PAC_MUNCHING);
        msPacMan.show();

        stork.setPosition(TS * 30, TS * 12);
        stork.setVelocity(-0.8f, 0);
        stork.show();
        stork.playAnimation(Stork.ANIM_FLYING);

        bag.setPosition(stork.x() - 14, stork.y() + 3);
        bag.setVelocity(stork.velocity());
        bag.setAcceleration(Vector2f.ZERO);
        bag.show();
        bag.setOpen(false);
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
        if (!bag.isOpen() && bag.y() > LANE_Y) {
            ++numBagBounces;
            if (numBagBounces < 3) {
                bag.setVelocity(-0.2f, -1f / numBagBounces);
                bag.setY(LANE_Y);
            } else {
                bag.setOpen(true);
                bag.setVelocity(Vector2f.ZERO);
                setSceneState(STATE_STORK_LEAVES_SCENE, 3 * Globals.NUM_TICKS_PER_SEC);
            }
        }
    }

    private void updateStateStorkLeavesScene() {
        stork.move();
        if (sceneTimer.hasExpired()) {
            context().gameController().letCurrentGameStateExpire();
        }
    }
}