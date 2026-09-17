/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.gamescene.introscene;

import de.amr.basics.fsm.State;
import de.amr.basics.fsm.StateMachine;
import de.amr.basics.math.Direction;
import de.amr.basics.math.RectShort;
import de.amr.basics.timer.Pulse;
import de.amr.basics.timer.TickTimer;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_SpriteSheet;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.ecs.systems.MovementSystem;
import de.amr.pacmanfx.core.entities.*;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.entities.ghost.system.GhostAnimationSystem;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.core.rules.CollisionStrategy;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.game.GameVariantRuntime;
import de.amr.pacmanfx.ui.GlobalFonts;
import de.amr.pacmanfx.ui.VoiceID;
import de.amr.pacmanfx.ui.action.core.GameApp;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneCanvasRenderingComp;
import de.amr.pacmanfx.uilib.entities.ImageDisplay;
import javafx.scene.paint.Color;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.pacmanfx.arcade.pacman.rendering.SpriteID.GALLERY_GHOSTS;
import static de.amr.pacmanfx.core.entities.ghost.comp.GhostState.EATEN;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.*;

/**
 * The ghosts are presented one by one, then Pac-Man is chased by the ghosts, turns the cards and hunts the ghosts himself.
 */
public class ArcadePacMan_IntroScene extends AbstractGameScene {

    private static final String TITLE_TEXT = "CHARACTER / NICKNAME";
    private static final String MIDWAY_MFG_CO = "© 1980 MIDWAY MFG.CO.";
    private static final String[] GHOST_NICKNAMES  = { "\"BLINKY\"", "\"PINKY\"", "\"INKY\"", "\"CLYDE\"" };
    private static final String[] GHOST_CHARACTERS = { "SHADOW", "SPEEDY", "BASHFUL", "POKEY" };
    private static final Color[]  GHOST_COLORS     = { ARCADE_RED, ARCADE_PINK, ARCADE_CYAN, ARCADE_ORANGE };

    private static final byte LEFT_TILE_X = 4;
    private static final short ENERGIZER_X = TS * LEFT_TILE_X;
    private static final short ENERGIZER_Y = TS * 20;

    public static final int NUM_GHOSTS = 4;

    // State STARTING
    public static final int TICK_TITLE_VISIBLE           = 3;
    public static final int TICK_START_PRESENTING_GHOSTS = 60;

    // State PRESENTING_GHOSTS
    public static final int TICK_GHOST_SPRITE_VISIBLE    =   0;
    public static final int TICK_GHOST_CHARACTER_VISIBLE =  60;
    public static final int TICK_GHOST_NICKNAME_VISIBLE  =  90;
    public static final int TICK_GHOST_PRESENT_NEXT      = 120;
    public static final int TICK_GHOST_PRESENTATION_END  = 150;

    // State SHOWING_POINTS
    public static final int TICK_SHOW_POINTS_DURATION = 60;

    // State CHASING_PAC_MAN
    public static final float CHASING_SPEED = 1.1f;
    public static final float GHOST_FRIGHTENED_SPEED = 0.5f;

    public static final int TICK_PAC_MAN_APPEARS = 60;
    public static final int TICK_PAC_MAN_REACHES_ENERGIZER = 230;
    public static final int TICK_PAC_MAN_MOVES_AGAIN = TICK_PAC_MAN_REACHES_ENERGIZER + 4;
    public static final int TICK_CHASING_PAC_MAN_END = TICK_PAC_MAN_REACHES_ENERGIZER + 8;

    // State CHASING_GHOSTS
    public static final int GHOST_EATING_TICKS = 50;

    public static final int TICK_CHASING_GHOSTS_END = 270;

    // READY_TO_PLAY
    public static final int TICK_START_DEMO_LEVEL = 60;

    // public access for renderer
    public final StateMachine<ArcadePacMan_IntroScene> flow;
    public Pulse blinking;

    private Pac pacMan;
    private final Ghost[] ghosts = new Ghost[NUM_GHOSTS];
    private GhostPoints points;

    private int numGhostsEaten;
    private int ghostIndex;
    private long lastGhostEatenTick;

    // NEW: Renderables
    private TextDisplay titleText;
    private final ImageDisplay[] ghostImageDisplays = new ImageDisplay[NUM_GHOSTS];
    private final TextDisplay[] ghostNicknameTextDisplays = new TextDisplay[NUM_GHOSTS];
    private final TextDisplay[] ghostCharacterTextDisplays = new TextDisplay[NUM_GHOSTS];


    public ArcadePacMan_IntroScene(GameApp app) {
        super(app);
        setComp(GameSceneCanvasRenderingComp.class, new GameSceneCanvasRenderingComp());
        flow = new StateMachine<>(List.of(SceneState.values()));
    }

    @Override
    public void onActivate() {
        final Arcade_Actions actions = app.variantManager().currentRuntime()
            .extensionValue(Arcade_GameExtensions.ACTIONS, Arcade_Actions.class);

        final var bindingsMap = actionBindings().registry();
        bindingsMap.registerAllBindings(actions.gameStartActionBindings()); // insert coin + start game actions
        bindingsMap.registerAllBindings(app.commonActions().sceneTestActions().bindings()); // actions for starting tests

        flow.restartState(this, SceneState.STARTING);
    }

    @Override
    public void onDeactivate() {
        points = null;
        blinking.stop();
        soundManager().voice().stop();
    }

    @Override
    public void onTick(GameContext game) {
        flow.update(this);
    }

    @Override
    public Stream<Renderable> renderables() {
        return Ufx.streamOf(
            titleText,
            Arrays.stream(ghostImageDisplays).filter(ImageDisplay::isVisible),
            Arrays.stream(ghostCharacterTextDisplays).filter(TextDisplay::isVisible),
            Arrays.stream(ghostNicknameTextDisplays).filter(TextDisplay::isVisible),
            pacMan,
            ghosts,
            points
        );
    }


    private void createTitleText() {
//        fillText("CHARACTER / NICKNAME", ARCADE_WHITE, tilesPx(LEFT_TILE_X + 3), tilesPx(6));

        titleText = new TextDisplay();
        titleText.pos().set(tilesPx(LEFT_TILE_X + 3), tilesPx(6));
        titleText.data().setFillColor(ARCADE_WHITE);
        titleText.data().setFont(GlobalFonts.ARCADE.font(TS));
        titleText.data().setText(TITLE_TEXT);
    }

    private void createGhostGalleryComponents() {
        final var spriteSheet = ArcadePacMan_SpriteSheet.instance();
        final int y = TS * 8;
        for (int i = 0; i < NUM_GHOSTS; ++i) {
            ghostImageDisplays[i] = new ImageDisplay();
            RectShort sprite = spriteSheet.findSpriteSequence(GALLERY_GHOSTS)[i];
            ghostImageDisplays[i].image().setImage(spriteSheet.image(sprite));
            ghostImageDisplays[i].pos().set(TS * 4, y + 3 * i * TS - 1.5f * TS);

            ghostCharacterTextDisplays[i] = new TextDisplay();
            ghostCharacterTextDisplays[i].pos().set(TS * 7, y + 3 * i * TS);
            ghostCharacterTextDisplays[i].data().setText("-" + GHOST_CHARACTERS[i]);
            ghostCharacterTextDisplays[i].data().setFillColor(GHOST_COLORS[i]);
            ghostCharacterTextDisplays[i].data().setFont(GlobalFonts.ARCADE.font(TS));

            ghostNicknameTextDisplays[i] = new TextDisplay();
            ghostNicknameTextDisplays[i].pos().set(TS * 18, y + 3 * i * TS);
            ghostNicknameTextDisplays[i].data().setText(GHOST_NICKNAMES[i]);
            ghostNicknameTextDisplays[i].data().setFillColor(GHOST_COLORS[i]);
            ghostNicknameTextDisplays[i].data().setFont(GlobalFonts.ARCADE.font(TS));
        }
    }

    private void initScene() {
        final GameVariantRuntime variant = app.variantManager().currentRuntime();
        final GameVariantRenderConfig renderConfig = variant.uiConfig().renderConfig();
        final SpriteAnimationContainer animContainer    = variant.spriteAnimContainer();
        final ActorSpriteAnimController animController  = variant.playConfig().systems().actorSpriteAnimController();

        blinking = new Pulse(10, Pulse.State.ON);

        final var actorFactory = ArcadePacMan_ActorFactory.instance();

        pacMan = actorFactory.createPacMan();
        pacMan.spriteAnim().setSpriteAnimations(renderConfig.createPacAnimations(animContainer));
        pacMan.spriteAnim().spriteAnimations().select(CommonSpriteAnimationID.PAC_MOUTH_MOVING);
        pacMan.spriteAnim().spriteAnimations().playSelected();

        ghosts[0] = renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.RED_GHOST_SHADOW);
        ghosts[1] = renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.PINK_GHOST_SPEEDY);
        ghosts[2] = renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.CYAN_GHOST_BASHFUL);
        ghosts[3] = renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.ORANGE_GHOST_POKEY);

        createTitleText();
        createGhostGalleryComponents();

        ghostIndex = 0;
        lastGhostEatenTick = 0;
        numGhostsEaten = 0;

        soundManager().voice().playAfterSec(1, VoiceID.START_HINT.media());
    }

    private void startChasingPacMan(GameContext game) {
        final GameSystems systems = game.playConfig().systems();

        blinking.start();

        pacMan.pos().set(TS * 28, TS * 20);
        pacMan.show();

        systems.navigator().setMoveDir(pacMan, Direction.LEFT);
        systems.navigator().setSpeed(pacMan, CHASING_SPEED);

        for (Ghost ghost : ghosts) {
            ghost.pos().set(pacMan.pos().x() + 16 * ghost.personality().ordinal() + 18, pacMan.pos().y());
            ghost.show();

            systems.navigator().setMoveDir(ghost, Direction.LEFT);
            systems.navigator().setWishDir(ghost, Direction.LEFT);
            systems.navigator().setSpeed(ghost, CHASING_SPEED);
            systems.ghostState().setState(ghost, GhostState.HUNTING_PAC);
        }
    }

    private void chasePacMan(long tick) {
        final GameSystems systems = game().playConfig().systems();
        final MovementSystem motor = systems.motor();
        final GhostAnimationSystem ghostSpriteAnimationSystem = systems.ghostAnimation();

        blinking.triggerPulse();
        motor.move(pacMan);
        for (Ghost ghost : ghosts) {
            motor.move(ghost);
        }

        // "shaking" effect
        final long tick_0_to_5 = tick % 6;
        final Ghost pinkGhost = ghosts[GhostPersonality.PINK_GHOST_SPEEDY.ordinal()];
        final Ghost cyanGhost = ghosts[GhostPersonality.CYAN_GHOST_BASHFUL.ordinal()];
        if (tick_0_to_5 == 2) {
            pinkGhost.pos().setX(pinkGhost.pos().x() + 0.5);
            cyanGhost.pos().setX(cyanGhost.pos().x() - 0.5);
        }
        else if (tick_0_to_5 == 5) {
            pinkGhost.pos().setX(pinkGhost.pos().x() - 0.5);
            cyanGhost.pos().setX(cyanGhost.pos().x() + 0.5);
        }

        for (Ghost ghost : ghosts) {
            ghostSpriteAnimationSystem.update(ghost);
        }
    }

    private void turnCardsStopPacMan(GameContext game) {
        final GameSystems systems = game.playConfig().systems();

        systems.navigator().setSpeed(pacMan, 0);
        systems.actorSpriteAnimController().stopSelected(pacMan);

        for (Ghost ghost : ghosts) {
            systems.navigator().setMoveDir(ghost, Direction.RIGHT);
            systems.navigator().setWishDir(ghost, Direction.RIGHT);
            systems.navigator().setSpeed(ghost, GHOST_FRIGHTENED_SPEED);

            systems.ghostState().setState(ghost, GhostState.FRIGHTENED);

            systems.actorSpriteAnimController().select(ghost, CommonSpriteAnimationID.GHOST_FRIGHTENED);
            systems.actorSpriteAnimController().playSelected(ghost);
        }
    }

    private void turnCardsRestartPacMan(GameSystems systems) {
        systems.navigator().setSpeed(pacMan, CHASING_SPEED);
        systems.actorSpriteAnimController().playSelected(pacMan);
    }

    private void chaseGhosts(GameContext game, long tick) {
        final GameSystems systems = game.playConfig().systems();

        blinking.triggerPulse();
        systems.motor().move(pacMan);
        for (Ghost ghost : ghosts) { systems.motor().move(ghost); }
        edibleGhost().ifPresent(victim -> eatGhostAndStopChasing(game, victim, tick));
        if (tick == lastGhostEatenTick + GHOST_EATING_TICKS) {
            continueChasing(systems);
        }
    }

    private Optional<Ghost> edibleGhost() {
        return Stream.of(ghosts)
            .filter(ghost -> ghost.state().enumValue() != GhostState.EATEN)
            .filter(ghost -> CollisionStrategy.SAME_TILE.collide(ghost, pacMan))
            .findFirst();
    }

    private void eatGhostAndStopChasing(GameContext game, Ghost victim, long tick) {
        final GameSystems systems = game.playConfig().systems();

        victim.state().setEnumValue(GhostState.EATEN);
        victim.hide();

        pacMan.hide();
        systems.navigator().setSpeed(pacMan, 0);

        for (Ghost ghost : ghosts) {
            systems.navigator().setSpeed(ghost, 0);
            systems.actorSpriteAnimController().stopSelected(ghost);
        }

        ++numGhostsEaten;
        points = new GhostPoints(switch (numGhostsEaten) {
            case 1 -> 200;
            case 2 -> 400;
            case 3 -> 800;
            case 4 -> 1600;
            default -> throw new IllegalArgumentException("Illegal eaten ghosts value: " + numGhostsEaten);
        });
        points.pos().set(victim.pos().asVector2f());
        points.show();

        lastGhostEatenTick = tick;
    }

    private void continueChasing(GameSystems systems) {
        pacMan.show();
        systems.navigator().setSpeed(pacMan, CHASING_SPEED);

        for (Ghost ghost : ghosts) {
            if (ghost.state().enumValue() == EATEN) {
                ghost.hide();
                points = null;
            } else {
                ghost.show();
                systems.navigator().setSpeed(ghost, GHOST_FRIGHTENED_SPEED);
                ghost.spriteAnimation().spriteAnimations().playSelected();
            }
        }
    }

    // Scene flow state machine

    public enum SceneState implements State<ArcadePacMan_IntroScene> {

        STARTING {
            @Override
            public void onEnter(ArcadePacMan_IntroScene scene) {
                scene.initScene();
            }

            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                if (timer.tickCount() == TICK_TITLE_VISIBLE) {
                    scene.titleText.show();
                } else if (timer.tickCount() == TICK_START_PRESENTING_GHOSTS) {
                    scene.flow.enterState(scene, PRESENTING_GHOSTS);
                }
            }
        },

        PRESENTING_GHOSTS {
            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                final int t = (int) timer.tickCount();
                if (t > TICK_GHOST_PRESENTATION_END) {
                    return;
                }
                switch (t) {
                    case TICK_GHOST_SPRITE_VISIBLE    -> scene.ghostImageDisplays[scene.ghostIndex].show();
                    case TICK_GHOST_CHARACTER_VISIBLE -> scene.ghostCharacterTextDisplays[scene.ghostIndex].show();
                    case TICK_GHOST_NICKNAME_VISIBLE  -> scene.ghostNicknameTextDisplays[scene.ghostIndex].show();
                    case TICK_GHOST_PRESENT_NEXT      -> presentNextGhost(scene);
                    case TICK_GHOST_PRESENTATION_END  -> scene.flow.enterState(scene, SHOWING_POINTS);
                }
            }

            private void presentNextGhost(ArcadePacMan_IntroScene scene) {
                if (scene.ghostIndex < NUM_GHOSTS - 1) {
                    scene.ghostIndex += 1;
                    timer.resetToIndefiniteDuration();
                }
            }
        },

        SHOWING_POINTS {
            @Override
            public void onEnter(ArcadePacMan_IntroScene scene) {
                scene.blinking.stop();
            }

            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                if (timer.tickCount() == TICK_SHOW_POINTS_DURATION) {
                    scene.flow.enterState(scene, CHASING_PAC_MAN);
                }
            }
        },

        CHASING_PAC_MAN {
            @Override
            public void onEnter(ArcadePacMan_IntroScene scene) {
                timer.restartTicks(TICK_CHASING_PAC_MAN_END);
                scene.pacMan.hide();
            }

            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                final GameSystems systems = scene.game().playConfig().systems();

                final long tick = timer.tickCount();
                if (tick == TICK_PAC_MAN_APPEARS) {
                    scene.startChasingPacMan(scene.game());
                }
                else if (tick == TICK_PAC_MAN_REACHES_ENERGIZER) {
                    scene.turnCardsStopPacMan(scene.game());
                }
                else if (tick == TICK_PAC_MAN_MOVES_AGAIN) {
                    scene.turnCardsRestartPacMan(systems);
                }
                else if (tick == TICK_CHASING_PAC_MAN_END) {
                    scene.flow.enterState(scene, CHASING_GHOSTS);
                    return;
                }
                scene.chasePacMan(tick);
            }
        },

        CHASING_GHOSTS {
            @Override
            public void onEnter(ArcadePacMan_IntroScene scene) {
                final GameSystems systems = scene.game().playConfig().systems();

                timer.restartTicks(TICK_CHASING_GHOSTS_END);

                scene.lastGhostEatenTick = timer.tickCount();
                scene.numGhostsEaten = 0;

                systems.navigator().setMoveDir(scene.pacMan, Direction.RIGHT);
                systems.navigator().setSpeed(scene.pacMan, CHASING_SPEED);
            }

            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                final long tick = timer.tickCount();
                if (tick == TICK_CHASING_GHOSTS_END) {
                    scene.pacMan.hide();
                    scene.flow.enterState(scene, WAIT_FOR_DEMO_LEVEL);
                } else {
                    scene.chaseGhosts(scene.game(), tick);
                }
            }
        },

        WAIT_FOR_DEMO_LEVEL {
            @Override
            public void onEnter(ArcadePacMan_IntroScene context) {
                timer.restartTicks(TICK_START_DEMO_LEVEL);
            }

            @Override
            public void onUpdate(ArcadePacMan_IntroScene scene) {
                final GameContext game = scene.game();

                if (timer.tickCount() == TICK_START_DEMO_LEVEL) {
                    scene.ghosts[GhostPersonality.ORANGE_GHOST_POKEY.ordinal()].hide();
                    scene.flow().enterGameState(game, CommonGameStateID.GAME_OR_LEVEL_STARTING);
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