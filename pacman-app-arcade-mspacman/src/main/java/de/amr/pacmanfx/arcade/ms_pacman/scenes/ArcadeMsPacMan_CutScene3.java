/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.Globals;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_UIConfig;
import de.amr.pacmanfx.arcade.ms_pacman.model.actors.ArcadeMsPacMan_ActorFactory;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.actors.CommonAnimationID;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.sound.SoundID;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.Globals.TS;

/**
 * Intermission scene 3: "Junior".
 *
 * <p>
 * Pac-Man and Ms. Pac-Man gradually wait for a stork, who flies overhead with a little blue bundle. The stork drops the
 * bundle, which falls to the ground in front of Pac-Man and Ms. Pac-Man, and finally opens up to reveal a tiny Pac-Man.
 * (Played after rounds 9, 13, and 17)
 */
public class ArcadeMsPacMan_CutScene3 extends GameScene2D {

    private static final int LANE_Y = TS * 24;

    private Pac pacMan;
    private Pac msPacMan;
    private Stork stork;
    private Bag bag;
    private int numBagBounces;

    private Clapperboard clapperboard;

    private GameScene2D_Renderer sceneRenderer;

    public ArcadeMsPacMan_CutScene3(GameUI ui) {
        super(ui);
    }

    @Override
    protected void createRenderers(Canvas canvas) {
        sceneRenderer = ui.currentConfig().createGameSceneRenderer(canvas, this);    }

    @Override
    public GameScene2D_Renderer sceneRenderer() {
        return sceneRenderer;
    }

    public Pac pacMan() {
        return pacMan;
    }

    public Pac msPacMan() {
        return msPacMan;
    }

    public Stork stork() {
        return stork;
    }

    public Bag bag() {
        return bag;
    }

    public Clapperboard clapperboard() {
        return clapperboard;
    }

    @Override
    public void doInit(Game game) {
        game.hud().score(true).levelCounter(true).livesCounter(false).show();

        final GameUI_Config uiConfig = ui.currentConfig();
        final var spriteSheet = (ArcadeMsPacMan_SpriteSheet) uiConfig.spriteSheet();

        pacMan = ArcadeMsPacMan_ActorFactory.createPacMan();
        pacMan.setAnimationManager(uiConfig.createPacAnimations());

        msPacMan = ArcadeMsPacMan_ActorFactory.createMsPacMan();
        msPacMan.setAnimationManager(uiConfig.createPacAnimations());

        stork = new Stork(spriteSheet);

        bag = new Bag(spriteSheet);
        bag.setOpen(false);

        clapperboard = new Clapperboard("3", "JUNIOR");
        clapperboard.setPosition(TS(3), TS(10));
        clapperboard.startAnimation();

        setSceneState(SceneState.CLAPPERBOARD, TickTimer.INDEFINITE);
    }

    @Override
    protected void doEnd(Game game) {
    }

    @Override
    public void update(Game game) {
        switch (sceneState) {
            case SceneState.CLAPPERBOARD -> updateStateClapperboard();
            case SceneState.DELIVER_JUNIOR -> updateStateDeliverJunior();
            case SceneState.STORK_LEAVES_SCENE -> updateStateStorkLeavesScene();
            default -> throw new IllegalStateException("Illegal scene state: " + sceneState);
        }
        sceneTimer.doTick();
    }

    // Scene controller state machine

    private enum SceneState { CLAPPERBOARD, DELIVER_JUNIOR, STORK_LEAVES_SCENE }

    private SceneState sceneState;
    private final TickTimer sceneTimer = new TickTimer("MsPacMan_CutScene3");

    private void setSceneState(SceneState state, long ticks) {
        sceneState = state;
        sceneTimer.reset(ticks);
        sceneTimer.start();
    }

    private void updateStateClapperboard() {
        clapperboard.tick();
        if (sceneTimer.atSecond(1)) {
            ui.soundManager().play(SoundID.INTERMISSION_3);
        } else if (sceneTimer.atSecond(3)) {
            enterStateDeliverJunior();
        }
    }

    private void enterStateDeliverJunior() {
        pacMan.setMoveDir(Direction.RIGHT);
        pacMan.setPosition(TS * 3, LANE_Y - 4);
        pacMan.selectAnimation(ArcadeMsPacMan_UIConfig.AnimationID.PAC_MAN_MUNCHING);
        pacMan.show();

        msPacMan.setMoveDir(Direction.RIGHT);
        msPacMan.setPosition(TS * 5, LANE_Y - 4);
        msPacMan.selectAnimation(CommonAnimationID.ANIM_PAC_MUNCHING);
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

        setSceneState(SceneState.DELIVER_JUNIOR, TickTimer.INDEFINITE);
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
                setSceneState(SceneState.STORK_LEAVES_SCENE, 3 * Globals.NUM_TICKS_PER_SEC);
            }
        }
    }

    private void updateStateStorkLeavesScene() {
        stork.move();
        if (sceneTimer.hasExpired()) {
            context().currentGame().control().terminateCurrentGameState();
        }
    }
}