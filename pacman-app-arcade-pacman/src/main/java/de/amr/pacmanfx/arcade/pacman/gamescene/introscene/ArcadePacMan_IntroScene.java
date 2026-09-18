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
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
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
import static de.amr.pacmanfx.core.model.world.map.WorldMap.*;
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
    private static final int ENERGIZER_CENTER_X = TS * LEFT_TILE_X + HTS;
    private static final int ENERGIZER_CENTER_Y = TS * 20 + HTS;

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

    private final StateMachine<ArcadePacMan_IntroScene> flow;
    private final Pulse blinking = new Pulse(10, Pulse.State.ON);

    private int numGhostsEaten;
    private int ghostIndex;
    private long lastGhostEatenTick;

    // Renderables
    private Pac pacMan;
    private final Ghost[] ghosts = new Ghost[NUM_GHOSTS];
    private GhostPoints points;
    private TextDisplay titleText;
    private final ImageDisplay[] ghostImageDisplays = new ImageDisplay[NUM_GHOSTS];
    private final TextDisplay[] ghostNicknameTextDisplays = new TextDisplay[NUM_GHOSTS];
    private final TextDisplay[] ghostCharacterTextDisplays = new TextDisplay[NUM_GHOSTS];
    private final BlinkingEnergizer targetEnergizer = new BlinkingEnergizer();
    private final BlinkingEnergizer energizer = new BlinkingEnergizer();
    private final Pellet pellet = new Pellet();
    private final TextDisplay text10 = new TextDisplay();
    private final TextDisplay text10Pts = new TextDisplay();
    private final TextDisplay text50 = new TextDisplay();
    private final TextDisplay text50Pts = new TextDisplay();
    private final TextDisplay copyrightText = new TextDisplay();

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
            targetEnergizer,
            points,
            text10,
            text10Pts,
            text50,
            text50Pts,
            pellet,
            energizer,
            copyrightText
        );
    }

    private void createTitleText() {
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
        final var actorFactory = ArcadePacMan_ActorFactory.instance();

        createPacMan(actorFactory, renderConfig, animContainer);
        createGhosts(renderConfig, animController, animContainer);
        createTitleText();
        createGhostGalleryComponents();
        createPointsTexts();
        createCopyrightText();

        targetEnergizer.setPulse(blinking);
        targetEnergizer.pos().set(ENERGIZER_CENTER_X, ENERGIZER_CENTER_Y);
        targetEnergizer.hide();

        energizer.setPulse(blinking);
        energizer.pos().set(tilesPx(LEFT_TILE_X + 6) + HTS, tilesPx(26) + HTS);
        energizer.hide();

        pellet.pos().set(tilesPx(LEFT_TILE_X + 6) + HTS, tilesPx(24) + 4);
        pellet.hide();

        ghostIndex = 0;
        lastGhostEatenTick = 0;
        numGhostsEaten = 0;

        soundManager().voice().playAfterSec(1, VoiceID.START_HINT.media());
    }

    private void createGhosts(GameVariantRenderConfig renderConfig, ActorSpriteAnimController animController, SpriteAnimationContainer animContainer) {
        ghosts[0] = renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.RED_GHOST_SHADOW);
        ghosts[1] = renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.PINK_GHOST_SPEEDY);
        ghosts[2] = renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.CYAN_GHOST_BASHFUL);
        ghosts[3] = renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.ORANGE_GHOST_POKEY);
    }

    private void createPacMan(ArcadePacMan_ActorFactory actorFactory, GameVariantRenderConfig renderConfig, SpriteAnimationContainer animContainer) {
        pacMan = actorFactory.createPacMan();
        pacMan.spriteAnim().setSpriteAnimations(renderConfig.createPacAnimations(animContainer));
        pacMan.spriteAnim().spriteAnimations().select(CommonSpriteAnimationID.PAC_MOUTH_MOVING);
        pacMan.spriteAnim().spriteAnimations().playSelected();
    }

    private void createCopyrightText() {
        copyrightText.data().setText(MIDWAY_MFG_CO);
        copyrightText.data().setFont(GlobalFonts.ARCADE.font(TS));
        copyrightText.data().setFillColor(ARCADE_PINK);
        copyrightText.pos().set(tilesPx(4), tilesPx(32));
        copyrightText.hide();
    }

    private void createPointsTexts() {
        text10.data().setText("10");
        text10.pos().set(tilesPx(LEFT_TILE_X + 8), tilesPx(25));
        text10.data().setFillColor(ARCADE_WHITE);
        text10.data().setFont(GlobalFonts.ARCADE.font(TS));
        text10.hide();

        text10Pts.data().setText("PTS");
        text10Pts.pos().set(tilesPx(LEFT_TILE_X + 11), tilesPx(25));
        text10Pts.data().setFillColor(ARCADE_WHITE);
        text10Pts.data().setFont(GlobalFonts.ARCADE.font(6));
        text10Pts.hide();

        text50.data().setText("50");
        text50.pos().set(tilesPx(LEFT_TILE_X + 8), tilesPx(27));
        text50.data().setFillColor(ARCADE_WHITE);
        text50.data().setFont(GlobalFonts.ARCADE.font(TS));
        text50.hide();

        text50Pts.data().setText("PTS");
        text50Pts.pos().set(tilesPx(LEFT_TILE_X + 11), tilesPx(27));
        text50Pts.data().setFillColor(ARCADE_WHITE);
        text50Pts.data().setFont(GlobalFonts.ARCADE.font(6));
        text50Pts.hide();
    }

    private void startChasingPacMan(GameContext game) {
        final GameSystems systems = game.playConfig().systems();
        final WorldNavigationSystem nav = systems.navigator();

        blinking.start();

        pacMan.pos().set(TS * 28, TS * 20);
        nav.setMoveDir(pacMan, Direction.LEFT);
        nav.setSpeed(pacMan, CHASING_SPEED);
        pacMan.show();

        for (Ghost ghost : ghosts) {
            ghost.pos().set(pacMan.pos().x() + 16 * ghost.personality().ordinal() + 18, pacMan.pos().y());
            nav.setMoveDir(ghost, Direction.LEFT);
            nav.setWishDir(ghost, Direction.LEFT);
            nav.setSpeed(ghost, CHASING_SPEED);
            systems.ghostState().setState(ghost, GhostState.HUNTING_PAC);
            ghost.show();
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
        final WorldNavigationSystem nav = systems.navigator();
        final ActorSpriteAnimController animController = systems.actorSpriteAnimController();

        nav.setSpeed(pacMan, 0);
        systems.actorSpriteAnimController().stopSelected(pacMan);

        for (Ghost ghost : ghosts) {
            nav.setMoveDir(ghost, Direction.RIGHT);
            nav.setWishDir(ghost, Direction.RIGHT);
            nav.setSpeed(ghost, GHOST_FRIGHTENED_SPEED);
            systems.ghostState().setState(ghost, GhostState.FRIGHTENED);
            animController.select(ghost, CommonSpriteAnimationID.GHOST_FRIGHTENED);
            animController.playSelected(ghost);
        }
    }

    private void turnCardsRestartPacMan(GameSystems systems) {
        systems.navigator().setSpeed(pacMan, CHASING_SPEED);
        systems.actorSpriteAnimController().playSelected(pacMan);
    }

    private void chaseGhosts(GameContext game, long tick) {
        final GameSystems systems = game.playConfig().systems();
        final MovementSystem motor = systems.motor();

        blinking.triggerPulse();

        motor.move(pacMan);
        for (Ghost ghost : ghosts) {
            motor.move(ghost);
        }

        findNextEdibleGhost().ifPresent(victim -> eatGhostAndStopChasing(game, victim, tick));
        if (tick == lastGhostEatenTick + GHOST_EATING_TICKS) {
            continueChasing(systems);
        }
    }

    private Optional<Ghost> findNextEdibleGhost() {
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
                scene.blinking.stopAndReset();
                scene.energizer.show();
                scene.pellet.show();
                scene.text10.show();
                scene.text10Pts.show();
                scene.text50.show();
                scene.text50Pts.show();
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
                scene.targetEnergizer.show();
                scene.copyrightText.show();
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
                    scene.targetEnergizer.hide();
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