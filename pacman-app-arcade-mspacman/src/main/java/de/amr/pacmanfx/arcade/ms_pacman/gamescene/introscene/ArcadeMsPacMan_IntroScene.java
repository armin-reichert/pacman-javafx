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
import de.amr.pacmanfx.core.ecs.systems.MovementSystem;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.spriteanim.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.ghost.Ghost;
import de.amr.pacmanfx.core.entities.pac.Pac;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.game.GameVariantRuntime;
import de.amr.pacmanfx.ui.assets.VoiceID;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneCanvasRenderingComp;
import javafx.scene.paint.Color;

import java.util.List;
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

    static final String[] GHOST_NAMES = { "BLINKY", "PINKY", "INKY", "SUE" };
    static final Color[] GHOST_COLORS = { ARCADE_RED, ARCADE_PINK, ARCADE_CYAN, ARCADE_ORANGE };

    static final int TITLE_X             = TS * 10;
     static final int TITLE_Y             = TS * 8;
     static final int TOP_Y               = TS * 11;
     static final int GHOST_RAISE_POS_X   = TS * 6 - WorldMap.HTS;
     static final int MS_PACMAN_END_POS_X = TS * 15 + 2;

     static final Vector2f PAC_START_POS = new Vector2f(31 * TS, 20 * TS);
     static final Vector2f GHOST_START_POS = new Vector2f(33.5f * TS, 20 * TS);

     static final float ACTOR_SPEED = 1.10f;

    private final IntroSceneController flow;
    
    IntroSceneView view;
    
    int ghostInSpotlight;
    int numTicksBeforeRising;

    public ArcadeMsPacMan_IntroScene() {
        setComp(GameSceneCanvasRenderingComp.class, new GameSceneCanvasRenderingComp());
        flow = new IntroSceneController();
    }

    @Override
    public Stream<Renderable> renderables() {
        return view.renderables();
    }

    @Override
    public void onActivate() {
        final GameVariantRuntime runtime = app().variantManager().currentRuntime();

        final Arcade_Actions arcadeActions = runtime.extensionValue(Arcade_GameExtensions.ACTIONS, Arcade_Actions.class);
        actionBindings().registry().registerAllBindings(arcadeActions.gameStartActionBindings());
        actionBindings().registry().registerAllBindings(app().commonActions().sceneTestActions().bindings());

        view = new IntroSceneView(runtime);

        final ActorSpriteAnimController animController = app().variantManager().currentRuntime()
            .playConfig().systems().actorSpriteAnimController();

        ghostInSpotlight = 0;
        numTicksBeforeRising = 0;

        startAnimations(animController, view.msPacMan(), view.ghosts());
        soundManager().voice().playAfterSec(1, VoiceID.START_HINT.media());

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

    void updateMarqueeText(IntroSceneController.SceneState state) {
        switch (state) {
            case GHOSTS_MARCHING_IN -> {
                if (ghostInSpotlight == GhostPersonality.RED_GHOST_SHADOW.ordinal()) {
                    view.showMarqueeText1("WITH", ARCADE_WHITE);
                } else {
                    view.hideMarqueeText1();
                }

                final String ghostName = GHOST_NAMES[ghostInSpotlight];
                final Color ghostColor = GHOST_COLORS[ghostInSpotlight];
                final float x = TITLE_X + (ghostName.length() < 4 ? tilesPx(4) : tilesPx(3));
                final float y = TOP_Y + tilesPx(6);
                view.placeMarqueeText2(x, y);
                view.showMarqueeText2(ghostName, ghostColor);
            }

            case MS_PACMAN_MARCHING_IN -> {
                view.showMarqueeText1("STARRING", ARCADE_WHITE);

                view.placeMarqueeText2(TITLE_X, TOP_Y + tilesPx(6));
                view.showMarqueeText2("MS PAC-MAN", ARCADE_YELLOW);
            }
        }
    }

    void startAnimations(ActorSpriteAnimController animController, Pac msPacMan, List<Ghost> ghosts) {
        animController.select(msPacMan, CommonSpriteAnimationID.PAC_MOUTH_MOVING);
        animController.playSelected(msPacMan);
        for (Ghost ghost : ghosts) {
            animController.select(ghost, CommonSpriteAnimationID.GHOST_NORMAL);
            animController.playSelected(ghost);
        }
    }

    boolean letGhostWalkIn(Ghost ghost) {
        final GameSystems systems = game().playConfig().systems();
        final WorldNavigationSystem nav = systems.navigator();
        final MovementSystem motor = systems.motor();
        final ActorSpriteAnimController animController = systems.actorSpriteAnimController();

        if (ghost.worldNavigation().moveDir() == Direction.LEFT) {
            if (ghost.pos().x() <= GHOST_RAISE_POS_X) {
                ghost.pos().setX(GHOST_RAISE_POS_X);
                nav.setMoveDir(ghost, Direction.UP);
                nav.setWishDir(ghost, Direction.UP);
                numTicksBeforeRising = 2;
            } else {
                motor.move(ghost);
            }
        }
        else if (ghost.worldNavigation().moveDir() == Direction.UP) {
            final int endPositionY = TOP_Y + ghostInSpotlight * 16 + 1;
            if (numTicksBeforeRising > 0) {
                numTicksBeforeRising--;
            }
            else if (ghost.pos().y() <= endPositionY) {
                nav.setSpeed(ghost, 0);
                animController.stopSelected(ghost);
                animController.resetSelected(ghost);
                return true;
            }
            else {
                motor.move(ghost);
            }
        }
        return false;
    }

    boolean letMsPacManWalkIn(Pac msPacMan) {
        final GameSystems systems = game().playConfig().systems();
        final WorldNavigationSystem nav = systems.navigator();
        final MovementSystem motor = systems.motor();
        final ActorSpriteAnimController animController = systems.actorSpriteAnimController();

        motor.move(msPacMan);
        if (msPacMan.pos().x() <= MS_PACMAN_END_POS_X) {
            nav.setSpeed(msPacMan, 0);
            animController.resetSelected(msPacMan);
            return true;
        }
        return false;
    }
}