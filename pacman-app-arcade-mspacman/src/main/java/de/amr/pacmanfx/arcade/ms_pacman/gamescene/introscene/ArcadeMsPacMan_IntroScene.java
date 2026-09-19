/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.gamescene.introscene;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.game.GameVariantRuntime;
import de.amr.pacmanfx.ui.VoiceID;
import de.amr.pacmanfx.ui.action.core.GameApp;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneCanvasRenderingComp;
import javafx.scene.paint.Color;

import java.util.stream.Stream;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.*;

/**
 * Intro scene of the Ms. Pac-Man game.
 * <p>
 * The ghosts and Ms. Pac-Man are introduced on a billboard and are marching in one after another.
 */
public class ArcadeMsPacMan_IntroScene extends AbstractGameScene {

     static final int TITLE_X             = TS * 10;
     static final int TITLE_Y             = TS * 8;
     static final int TOP_Y               = TS * 11;
     static final int GHOST_RAISE_POS_X   = TS * 6 - WorldMap.HTS;
     static final int MS_PACMAN_END_POS_X = TS * 15 + 2;

     static final Vector2f PAC_START_POS = new Vector2f(31 * TS, 20 * TS);
     static final Vector2f GHOST_START_POS = new Vector2f(33.5f * TS, 20 * TS);

     static final float ACTOR_SPEED = 1.10f;

     static final String MARQUEE_TITLE = "\"MS PAC-MAN\"";
     static final String[] GHOST_NAMES = { "BLINKY", "PINKY", "INKY", "SUE" };
     static final Color[] GHOST_COLORS = { ARCADE_RED, ARCADE_PINK, ARCADE_CYAN, ARCADE_ORANGE };

    private final IntroSceneController flow;
    
    IntroSceneView view;
    
    int ghostInSpotlight;
    int numTicksBeforeRising;

    public ArcadeMsPacMan_IntroScene(GameApp app) {
        super(app);
        setComp(GameSceneCanvasRenderingComp.class, new GameSceneCanvasRenderingComp());
        flow = new IntroSceneController();
    }

    @Override
    public Stream<Renderable> renderables() {
        return view.renderables();
    }

    @Override
    public void onActivate() {
        final GameVariantRuntime runtime = app.variantManager().currentRuntime();

        final Arcade_Actions actions = runtime.extensionValue(
            Arcade_GameExtensions.ACTIONS, Arcade_Actions.class);

        final var bindingsMap = actionBindings().registry();
        bindingsMap.registerAllBindings(actions.gameStartActionBindings());
        bindingsMap.registerAllBindings(app.commonActions().sceneTestActions().bindings());

        view = new IntroSceneView(runtime);
        flow.restartState(this, IntroSceneController.SceneState.STARTING);
    }

    @Override
    public void onDeactivate() {
        soundManager().voice().stop();
        actionBindings().registry().dispose();
    }

    @Override
    public void onTick(GameContext game) {
        flow.update(this);
    }

    void initScene() {
        final GameVariantRuntime runtime = app.variantManager().currentRuntime();
        final ActorSpriteAnimController animController = runtime.playConfig().systems().actorSpriteAnimController();

        ghostInSpotlight = GhostPersonality.RED_GHOST_SHADOW.ordinal();
        numTicksBeforeRising = 0;

        startAnimations(animController);
        soundManager().voice().playAfterSec(1, VoiceID.START_HINT.media());
    }

    void updateMarqueeText(IntroSceneController.SceneState state) {
        switch (state) {
            case GHOSTS_MARCHING_IN -> {
                String ghostName = GHOST_NAMES[ghostInSpotlight];
                Color ghostColor = GHOST_COLORS[ghostInSpotlight];
                if (ghostInSpotlight == GhostPersonality.RED_GHOST_SHADOW.ordinal()) {
                    view.marqueeText1.data().setText("WITH");
                    view.marqueeText1.data().setFillColor(ARCADE_WHITE);
                    view.marqueeText1.show();
                } else {
                    view.marqueeText1.hide();
                }
                double x = TITLE_X + (ghostName.length() < 4 ? tilesPx(4) : tilesPx(3));
                double y = TOP_Y + tilesPx(6);
                view.marqueeText2.data().setText(ghostName);
                view.marqueeText2.data().setFillColor(ghostColor);
                view.marqueeText2.pos().set(x, y);
            }

            case MS_PACMAN_MARCHING_IN -> {
                view.marqueeText1.data().setText("STARRING");
                view.marqueeText1.data().setFillColor(ARCADE_WHITE);
                view.marqueeText1.show();

                view.marqueeText2.data().setText("MS PAC-MAN");
                view.marqueeText2.data().setFillColor(ARCADE_YELLOW);
                view.marqueeText2.pos().set(TITLE_X, TOP_Y + tilesPx(6));
            }
        }
    }

    void startAnimations(ActorSpriteAnimController animController) {
        animController.select(view.msPacMan, CommonSpriteAnimationID.PAC_MOUTH_MOVING);
        animController.playSelected(view.msPacMan);
        for (Ghost ghost : view.ghosts) {
            animController.select(ghost, CommonSpriteAnimationID.GHOST_NORMAL);
            animController.playSelected(ghost);
        }
    }

    boolean letGhostWalkIn() {
        final GameSystems systems = game().playConfig().systems();

        final Ghost ghost = view.ghosts.get(ghostInSpotlight);
        if (ghost.worldNavigation().moveDir() == Direction.LEFT) {
            if (ghost.pos().x() <= GHOST_RAISE_POS_X) {
                ghost.pos().setX(GHOST_RAISE_POS_X);
                systems.navigator().setMoveDir(ghost, Direction.UP);
                systems.navigator().setWishDir(ghost, Direction.UP);
                numTicksBeforeRising = 2;
            } else {
                systems.motor().move(ghost);
            }
        }
        else if (ghost.worldNavigation().moveDir() == Direction.UP) {
            final int endPositionY = TOP_Y + ghostInSpotlight * 16 + 1;
            if (numTicksBeforeRising > 0) {
                numTicksBeforeRising--;
            }
            else if (ghost.pos().y() <= endPositionY) {
                systems.navigator().setSpeed(ghost, 0);
                systems.actorSpriteAnimController().stopSelected(ghost);
                systems.actorSpriteAnimController().resetSelected(ghost);
                return true;
            }
            else {
                systems.motor().move(ghost);
            }
        }
        return false;
    }

    boolean letMsPacManWalkIn() {
        final GameSystems systems = game().playConfig().systems();
        systems.motor().move(view.msPacMan);
        if (view.msPacMan.pos().x() <= MS_PACMAN_END_POS_X) {
            systems.navigator().setSpeed(view.msPacMan, 0);
            systems.actorSpriteAnimController().resetSelected(view.msPacMan);
            return true;
        }
        return false;
    }
}