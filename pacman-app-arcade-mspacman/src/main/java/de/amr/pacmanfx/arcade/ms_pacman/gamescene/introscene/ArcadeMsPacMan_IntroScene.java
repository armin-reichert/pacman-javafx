/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.gamescene.introscene;

import de.amr.basics.fsm.State;
import de.amr.basics.fsm.StateMachine;
import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2f;
import de.amr.basics.timer.TickTimer;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.uilib.entities.ImageDisplay;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_ActorFactory;
import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.*;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.game.GameVariantRuntime;
import de.amr.pacmanfx.ui.GlobalFonts;
import de.amr.pacmanfx.ui.VoiceID;
import de.amr.pacmanfx.ui.action.core.GameApp;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneCanvasRenderingComp;
import de.amr.pacmanfx.uilib.assets.AssetMap;
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

    private static final int TITLE_X             = TS * 10;
    private static final int TITLE_Y             = TS * 8;
    private static final int TOP_Y               = TS * 11;
    private static final int GHOST_RAISE_POS_X   = TS * 6 - WorldMap.HTS;
    private static final int MS_PACMAN_END_POS_X = TS * 15 + 2;

    private static final Vector2f PAC_START_POS = new Vector2f(31 * TS, 20 * TS);
    private static final Vector2f GHOST_START_POS = new Vector2f(33.5f * TS, 20 * TS);

    private static final float ACTOR_SPEED = 1.10f;

    private static final String MARQUEE_TITLE = "\"MS PAC-MAN\"";
    private static final String[] GHOST_NAMES = { "BLINKY", "PINKY", "INKY", "SUE" };
    private static final Color[] GHOST_COLORS = { ARCADE_RED, ARCADE_PINK, ARCADE_CYAN, ARCADE_ORANGE };

    private final StateMachine<ArcadeMsPacMan_IntroScene> sceneFlow;

    private Marquee marquee;
    private Pac msPacMan;
    private List<Ghost> ghosts;
    private ImageDisplay copyright;
    private TextDisplay titleText;
    private TextDisplay marqueeText1;
    private TextDisplay marqueeText2;

    public int ghostInSpotlight;

    private int numTicksBeforeRising;

    public ArcadeMsPacMan_IntroScene(GameApp app) {
        super(app);
        setComp(GameSceneCanvasRenderingComp.class, new GameSceneCanvasRenderingComp());
        sceneFlow = new StateMachine<>(List.of(SceneState.values()));
    }

    @Override
    public Stream<Renderable> renderables() {
        return Ufx.streamOf(titleText, marquee, marqueeText1, marqueeText2, msPacMan, ghosts, copyright);
    }

    @Override
    public void onActivate() {
        final Arcade_Actions actions = app.variantManager().currentRuntime()
            .extensionValue(Arcade_GameExtensions.ACTIONS, Arcade_Actions.class);

        final var bindingsMap = actionBindings().registry();
        bindingsMap.registerAllBindings(actions.gameStartActionBindings());
        bindingsMap.registerAllBindings(app.commonActions().sceneTestActions().bindings());

        sceneFlow.restartState(this, SceneState.STARTING);
    }

    @Override
    public void onDeactivate() {
        soundManager().voice().stop();
        actionBindings().registry().dispose();
    }

    @Override
    public void onTick(GameContext game) {
        sceneFlow.update(this);
    }

    private void initScene() {
        final GameVariantRuntime runtime = app.variantManager().currentRuntime();
        final ActorSpriteAnimController animController = runtime.playConfig().systems().actorSpriteAnimController();

        createTitleText();
        createMarqueeTexts();
        createMarquee();
        createMsPacMan(runtime);
        createGhosts(runtime);
        createCopyrightImage(runtime);

        ghostInSpotlight = GhostPersonality.RED_GHOST_SHADOW.ordinal();
        numTicksBeforeRising = 0;

        startAnimations(animController);
        soundManager().voice().playAfterSec(1, VoiceID.START_HINT.media());
    }

    private void createTitleText() {
        titleText = new TextDisplay();
        titleText.data().setText(MARQUEE_TITLE);
        titleText.data().setFillColor(ARCADE_ORANGE);
        titleText.data().setFont(GlobalFonts.ARCADE.font(8));
        titleText.pos().set(TITLE_X, TITLE_Y);
        titleText.show();
    }

    private void createCopyrightImage(GameVariantRuntime runtime) {
        final AssetMap assets = runtime.uiConfig().assets();
        copyright = new ImageDisplay();
        copyright.show();
        copyright.pos().set(tilesPx(6), tilesPx(28));
        copyright.image().setImage(assets.image("logo.midway"));
    }

    private void createMsPacMan(GameVariantRuntime runtime) {
        final var actorFactory = new ArcadeMsPacMan_ActorFactory();
        final GameVariantRenderConfig renderConfig = runtime.uiConfig().renderConfig();
        final SpriteAnimationContainer animContainer = runtime.spriteAnimContainer();
        final GameSystems systems = runtime.playConfig().systems();
        final ActorSpriteAnimController animController = systems.actorSpriteAnimController();
        final WorldNavigationSystem nav = systems.navigator();

        msPacMan = actorFactory.createMsPacMan();
        msPacMan.pos().set(PAC_START_POS);
        nav.setMoveDir(msPacMan, Direction.LEFT);
        nav.setSpeed(msPacMan, ACTOR_SPEED);
        animController.setAnimations(msPacMan, renderConfig.createPacAnimations(animContainer));
        msPacMan.show();
    }

    private void createGhosts(GameVariantRuntime runtime) {
        final GameVariantRenderConfig renderConfig = runtime.uiConfig().renderConfig();
        final SpriteAnimationContainer animContainer = runtime.spriteAnimContainer();
        final GameSystems systems = runtime.playConfig().systems();
        final ActorSpriteAnimController animController = systems.actorSpriteAnimController();
        final WorldNavigationSystem nav = systems.navigator();

        ghosts = List.of(
            renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.RED_GHOST_SHADOW),
            renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.PINK_GHOST_SPEEDY),
            renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.CYAN_GHOST_BASHFUL),
            renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.ORANGE_GHOST_POKEY)
        );

        for (Ghost ghost : ghosts) {
            ghost.pos().set(GHOST_START_POS);
            nav.setMoveDir(ghost, Direction.LEFT);
            nav.setWishDir(ghost, Direction.LEFT);
            nav.setSpeed(ghost, ACTOR_SPEED);
            systems.ghostState().setState(ghost, GhostState.HUNTING_PAC);
            ghost.show();
        }
    }

    private void createMarquee() {
        marquee = new Marquee();
        marquee.pos().set(60, 88);

        marquee.layout().setNumBulbsHorizontally(34);
        marquee.layout().setNumBulbsVertically(16);
        marquee.layout().setBulbSize(4);
        marquee.layout().setBrightBulbsCount(6);
        marquee.layout().setBrightBulbsDistance(16);

        marquee.visualization().setBulbOffColor(ARCADE_RED.toString());
        marquee.visualization().setBulbOnColor(ARCADE_WHITE.toString());

        marquee.show();
    }

    private void createMarqueeTexts() {
        marqueeText1 = new TextDisplay();
        marqueeText1.data().setFont(GlobalFonts.ARCADE.font(TS));
        marqueeText1.pos().set(TITLE_X, TOP_Y + tilesPx(3));
        marqueeText1.show();

        marqueeText2 = new TextDisplay();
        marqueeText2.data().setFont(GlobalFonts.ARCADE.font(TS));
        marqueeText2.show();
    }

    private void updateMarqueeText(SceneState state) {
        switch (state) {
            case GHOSTS_MARCHING_IN -> {
                String ghostName = GHOST_NAMES[ghostInSpotlight];
                Color ghostColor = GHOST_COLORS[ghostInSpotlight];
                if (ghostInSpotlight == GhostPersonality.RED_GHOST_SHADOW.ordinal()) {
                    marqueeText1.data().setText("WITH");
                    marqueeText1.data().setFillColor(ARCADE_WHITE);
                    marqueeText1.show();
                } else {
                    marqueeText1.hide();
                }
                double x = TITLE_X + (ghostName.length() < 4 ? tilesPx(4) : tilesPx(3));
                double y = TOP_Y + tilesPx(6);
                marqueeText2.data().setText(ghostName);
                marqueeText2.data().setFillColor(ghostColor);
                marqueeText2.pos().set(x, y);
            }

            case MS_PACMAN_MARCHING_IN -> {
                marqueeText1.data().setText("STARRING");
                marqueeText1.data().setFillColor(ARCADE_WHITE);
                marqueeText1.show();

                marqueeText2.data().setText("MS PAC-MAN");
                marqueeText2.data().setFillColor(ARCADE_YELLOW);
                marqueeText2.pos().set(TITLE_X, TOP_Y + tilesPx(6));
            }
        }
    }

    private void startAnimations(ActorSpriteAnimController animController) {
        animController.select(msPacMan, CommonSpriteAnimationID.PAC_MOUTH_MOVING);
        animController.playSelected(msPacMan);
        for (Ghost ghost : ghosts) {
            animController.select(ghost, CommonSpriteAnimationID.GHOST_NORMAL);
            animController.playSelected(ghost);
        }
    }

    private boolean letGhostWalkIn() {
        final GameSystems systems = game().playConfig().systems();

        final Ghost ghost = ghosts.get(ghostInSpotlight);
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

    private boolean letMsPacManWalkIn() {
        final GameSystems systems = game().playConfig().systems();
        systems.motor().move(msPacMan);
        if (msPacMan.pos().x() <= MS_PACMAN_END_POS_X) {
            systems.navigator().setSpeed(msPacMan, 0);
            systems.actorSpriteAnimController().resetSelected(msPacMan);
            return true;
        }
        return false;
    }

    // Scene flow state machine

    public enum SceneState implements State<ArcadeMsPacMan_IntroScene> {

        STARTING {
            @Override
            public void onEnter(ArcadeMsPacMan_IntroScene scene) {
                scene.initScene();
                scene.updateMarqueeText(this);
            }

            @Override
            public void onUpdate(ArcadeMsPacMan_IntroScene scene) {
                if (timer.atSecond(1)) {
                    scene.sceneFlow.enterState(scene, GHOSTS_MARCHING_IN);
                }
            }
        },

        GHOSTS_MARCHING_IN {
            @Override
            public void onEnter(ArcadeMsPacMan_IntroScene scene) {
                scene.updateMarqueeText(this);
            }

            @Override
            public void onUpdate(ArcadeMsPacMan_IntroScene scene) {
                final boolean atEndPosition = scene.letGhostWalkIn();
                if (atEndPosition) {
                    if (scene.ghostInSpotlight == GhostPersonality.ORANGE_GHOST_POKEY.ordinal()) {
                        scene.sceneFlow.enterState(scene, MS_PACMAN_MARCHING_IN);
                    } else {
                        ++scene.ghostInSpotlight;
                        scene.updateMarqueeText(this);
                    }
                }
            }
        },

        MS_PACMAN_MARCHING_IN {
            @Override
            public void onEnter(ArcadeMsPacMan_IntroScene scene) {
                scene.updateMarqueeText(this);
            }

            @Override
            public void onUpdate(ArcadeMsPacMan_IntroScene scene) {
                final boolean atEndPosition = scene.letMsPacManWalkIn();
                if (atEndPosition) {
                    scene.sceneFlow.enterState(scene, READY_TO_PLAY);
                }
            }
        },

        READY_TO_PLAY {
            @Override
            public void onUpdate(ArcadeMsPacMan_IntroScene scene) {
                final GameContext game = scene.app.game();
                final boolean canPlay = !game.coinMechanism().isEmpty();
                if (timer.atSecond(2.0) && !canPlay) {
                    scene.flow().enterGameState(game, CommonGameStateID.GAME_OR_LEVEL_STARTING); // play demo level after 2 seconds
                }
                //TODO can this happen at all?
                else if (timer.atSecond(5)) {
                    scene.flow().enterGameState(game, CommonGameStateID.GAME_PREPARATION);
                }
            }
        };

        final TickTimer timer = new TickTimer("Timer-" + name());

        @Override
        public TickTimer timer() {
            return timer;
        }
    }
}