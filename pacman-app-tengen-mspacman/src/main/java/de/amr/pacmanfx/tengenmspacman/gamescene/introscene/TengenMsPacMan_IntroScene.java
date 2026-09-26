/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamescene.introscene;

import de.amr.basics.ecs.GameEntity;
import de.amr.basics.ecs.system.MovementSystem;
import de.amr.basics.fsm.State;
import de.amr.basics.fsm.StateMachine;
import de.amr.basics.math.Direction;
import de.amr.basics.timer.TickTimer;
import de.amr.basics.ui.ecs.system.ActorSpriteAnimController;
import de.amr.basics.ui.entities.props.imagedisplay.ImageView;
import de.amr.basics.ui.entities.props.marquee.Marquee;
import de.amr.basics.ui.entities.props.textdisplay.TextView;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.spriteanim.CommonSpriteAnimationID;
import de.amr.basics.ui.spriteanim.SpriteAnimationContainer;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.core.entities.actor.ghost.GhostState;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.entities.world.WorldNavigationSystem;
import de.amr.pacmanfx.core.gamestate.GameFlow;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.game.GameVariantRuntime;
import de.amr.pacmanfx.game.GameVariantUIConfig;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameExtension;
import de.amr.pacmanfx.tengenmspacman.gamestate.Tengen_GameState;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_ActorFactory;
import de.amr.pacmanfx.tengenmspacman.rendering.NES_Palette;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_RenderConfig;
import de.amr.pacmanfx.tengenmspacman.sprites.SpriteID;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui.assets.GlobalFonts;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneCanvasRenderingComp;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneView;
import de.amr.pacmanfx.ui.rendering.GameEntityViewBuilder;
import de.amr.pacmanfx.uilib.entities3d.ghost.comp.GhostSettings;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import java.util.List;
import java.util.stream.Stream;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay.gameOptions;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_HEIGHT;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_WIDTH;
import static de.amr.pacmanfx.ui.rendering.GameEntityViewBuilder.propView;

public class TengenMsPacMan_IntroScene extends AbstractGameScene {

    public static final String TENGEN_PRESENTS = "TENGEN PRESENTS";
    public static final String PRESS_START = "PRESS START";

    // Footer
    public static final String NAMCO_LTD = "MS PAC-MAN TM NAMCO LTD";
    public static final String TENGEN_INC = "©1990 TENGEN INC";
    public static final String ALL_RIGHTS_RESERVED = "ALL RIGHTS RESERVED";

    // Marquee
    public static final String MARQUEE_TITLE_TEXT = "\"MS PAC-MAN\"";
    public static final String WITH = "WITH";
    public static final String STARRING = "STARRING";
    public static final String MS_PAC_MAN = "MS PAC-MAN";

    // Anchor point for everything
    public static final int ANCHOR_X = 76, ANCHOR_Y = 64;

    public static final int ACTOR_Y = ANCHOR_Y + 72;
    public static final int GHOST_STOP_X = ANCHOR_X - 18;
    public static final int MS_PAC_MAN_STOP_X = ANCHOR_X + 62;
    public static final float SPEED = 2.2f; //TODO check exact speed


    public TengenMsPacMan_SpriteSheet spriteSheet;
    public Color[] ghostColors;

    // First sub-scene (Tengen presents)
    private List<GameEntity> tengenPresentsContent;
    private TextView tengenPresentsTextView;
    private TextView pressStartTextView;

    // Second sub-scene (marquee)
    private List<GameEntity> marqueeContent;
    private TextView marqueeTitleTextView;
    private TextView marqueeTextView1;
    private TextView marqueeTextView2;
    private Marquee marquee;
    private Pac msPacMan;
    private List<Ghost> ghosts;

    public int ghostIndex;
    private int waitBeforeRising;
    public boolean dark;

    public final StateMachine<TengenMsPacMan_IntroScene> flow;

    public TengenMsPacMan_IntroScene() {
        setComp(GameSceneCanvasRenderingComp.class, new GameSceneCanvasRenderingComp());
        reqCanvasRendering().unscaledWidthProperty().set(NES_SCREEN_WIDTH);
        reqCanvasRendering().unscaledHeightProperty().set(NES_SCREEN_HEIGHT);

        flow = new StateMachine<>(List.of(SceneState.values()));
    }

    public Ghost currentGhost() {
        return ghosts.get(ghostIndex);
    }

    @Override
    public Stream<Renderable> renderables() {
        return switch (flow.state()) {
            case SceneState.PRESENTING_GAME
                -> dark? Stream.empty() : GameEntityViewBuilder.streamOfPropViews(tengenPresentsContent);

            case SceneState.SHOWING_MARQUEE,
                 SceneState.GHOSTS_MARCHING_IN,
                 SceneState.MS_PACMAN_MARCHING_IN -> Ufx.streamOf(
                //TODO replace by renderables:
                new GameSceneView(this),
                propView(marqueeTitleTextView),
                propView(marquee),
                propView(marqueeTextView1),
                propView(marqueeTextView2),
                propView(msPacMan),
                ghosts.stream().map(GameEntityViewBuilder::propView)
            );
            default -> Stream.empty();
        };
    }

    @Override
    public void onActivate() {
        final GameVariantUIConfig variantConfig = app().variantManager().currentRuntime().uiConfig();

        game().session().setHudVisible(false);

        spriteSheet = TengenMsPacMan_SpriteSheet.instance();

        final var actions = app().variantManager().currentRuntime()
            .extensionValue(TengenMsPacMan_GameExtension.EXT_ACTIONS, TengenMsPacMan_Actions.class);

        final var bindingsMap = actionBindings().registry();
        bindingsMap.selectAnyMatchingBinding(actions.actionEnterStartScreen(), actions.localBindings());
        bindingsMap.selectAnyMatchingBinding(actions.actionToggleJoypadBindingsDisplayed(), actions.localBindings());

        final List<GhostSettings> ghostSettings = variantConfig.worldSettings().ghosts();
        ghostColors = Stream.of(
                GhostPersonality.RED_GHOST_SHADOW,
                GhostPersonality.PINK_GHOST_SPEEDY,
                GhostPersonality.CYAN_GHOST_BASHFUL,
                GhostPersonality.ORANGE_GHOST_POKEY)
            .map(personality -> ghostSettings.get(personality.ordinal()).colors().normal().dressColor())
            .toArray(Color[]::new);

        createTengenPresentsSubSceneContent();
        createMarqueeSubSceneContent();

        flow.restartState(this, SceneState.PRESENTING_GAME);
    }

    @Override
    public void onTick(GameContext game) {
        flow.update(this);
    }

    // --- private

    private void createTengenPresentsSubSceneContent() {
        tengenPresentsTextView = new TextView();
        tengenPresentsTextView.pos().set(8 * TS, ANCHOR_Y - TS);
        tengenPresentsTextView.data().setText(TENGEN_PRESENTS);
        tengenPresentsTextView.data().setFillColor(Color.WHITE); // TODO animate
        tengenPresentsTextView.data().setFont(GlobalFonts.ARCADE.font(TS));
        tengenPresentsTextView.show();

        final ImageView titleImageView = new ImageView();
        titleImageView.pos().set(7 * TS, ANCHOR_Y);
        titleImageView.image().setImage(spriteSheet.createImage(SpriteID.LARGE_MS_PAC_MAN_TEXT));
        titleImageView.show();

        pressStartTextView = new TextView();
        pressStartTextView.pos().set(10 * TS, ANCHOR_Y + 9 * TS);
        pressStartTextView.data().setText(PRESS_START);
        pressStartTextView.data().setFillColor(NES_Palette.color(0x20)); // TODO animate
        pressStartTextView.data().setFont(GlobalFonts.ARCADE.font(TS));
        pressStartTextView.show();

        final TextView footer1 = new TextView();
        footer1.pos().set(5 * TS, ANCHOR_Y + 15 * TS);
        footer1.data().setText(NAMCO_LTD);
        footer1.data().setFillColor(NES_Palette.color(0x25));
        footer1.data().setFont(GlobalFonts.ARCADE.font(TS));
        footer1.show();

        final TextView footer2 = new TextView();
        footer2.pos().set(7 * TS, ANCHOR_Y + 16 * TS);
        footer2.data().setText(TENGEN_INC);
        footer2.data().setFillColor(NES_Palette.color(0x25));
        footer2.data().setFont(GlobalFonts.ARCADE.font(TS));
        footer2.show();

        final TextView footer3 = new TextView();
        footer3.pos().set(6 * TS, ANCHOR_Y + 17 * TS);
        footer3.data().setText(ALL_RIGHTS_RESERVED);
        footer3.data().setFillColor(NES_Palette.color(0x25));
        footer3.data().setFont(GlobalFonts.ARCADE.font(TS));
        footer3.show();

        tengenPresentsContent = List.of(tengenPresentsTextView, titleImageView, pressStartTextView, footer1, footer2, footer3);
    }

    private void createMarqueeSubSceneContent() {
        marqueeTitleTextView = new TextView();
        marqueeTitleTextView.pos().set(ANCHOR_X + 20, ANCHOR_Y - 18);
        marqueeTitleTextView.data().setFillColor(NES_Palette.color(0x28));
        marqueeTitleTextView.data().setFont(GlobalFonts.ARCADE.font(TS));
        marqueeTitleTextView.data().setText(MARQUEE_TITLE_TEXT);
        marqueeTitleTextView.show();

        marquee = new Marquee();
        marquee.pos().set(ANCHOR_X, ANCHOR_Y);

        marquee.layout().setNumBulbsHorizontally(34);
        marquee.layout().setNumBulbsVertically(16);
        marquee.layout().setBulbSize(4);
        marquee.layout().setBrightBulbsCount(6);
        marquee.layout().setBrightBulbsDistance(16);

        marquee.visualization().setBulbOnColor(NES_Palette.rgb(0x20));
        marquee.visualization().setBulbOffColor(NES_Palette.rgb(0x15));

        marqueeTextView1 = new TextView();
        marqueeTextView1.data().setFillColor(Color.WHITE); //TODO
        marqueeTextView1.data().setFont(GlobalFonts.ARCADE.font(TS));
        marqueeTextView1.data().setText("Text 1 in Marquee");
        marqueeTextView1.show();

        marqueeTextView2 = new TextView();
        marqueeTextView2.data().setFillColor(Color.WHITE); //TODO
        marqueeTextView2.data().setFont(GlobalFonts.ARCADE.font(TS));
        marqueeTextView1.data().setText("Text 2 in Marquee");

        marqueeTextView2.show();

        final var actorFactory = TengenMsPacMan_ActorFactory.instance();

        final GameVariantRuntime variant = app().variantManager().currentRuntime();
        final GameVariantRenderConfig renderConfig = variant.uiConfig().renderConfig();
        final SpriteAnimationContainer animContainer    = variant.spriteAnimContainer();
        final ActorSpriteAnimController animController  = variant.playConfig().systems().actorSpriteAnimController();

        msPacMan = actorFactory.createMsPacMan();
        animController.setAnimations(msPacMan, renderConfig.createPacAnimations(animContainer));

        ghosts = List.of(
            renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.RED_GHOST_SHADOW),
            renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.CYAN_GHOST_BASHFUL),
            renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.PINK_GHOST_SPEEDY),
            renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.ORANGE_GHOST_POKEY)
        );
    }

    // --- State machine ---

    public enum SceneState implements State<TengenMsPacMan_IntroScene> {

        PRESENTING_GAME {

            @Override
            public void onEnter(TengenMsPacMan_IntroScene scene) {
                timer.restartTicks(TickTimer.INDEFINITE);
                scene.dark = false;
                scene.marquee.hide();
                scene.msPacMan.hide();
                scene.ghosts.forEach(Ghost::hide);
            }

            @Override
            public void onUpdate(TengenMsPacMan_IntroScene scene) {
                if (timer.atSecond(7.8)) {
                    scene.dark = true;
                } else if (timer.atSecond(9)) {
                    scene.dark = false;
                    scene.flow.enterState(scene, SHOWING_MARQUEE);
                }
                final long stateTick = timer().tickCount();
                final boolean bright = stateTick % 60 < 30; // 0.5s dark, 0.5s bright

                scene.tengenPresentsTextView.data().setFillColor(TengenMsPacMan_RenderConfig.shadeOfBlue(stateTick));
                if (bright) {
                    scene.pressStartTextView.show();
                } else {
                    scene.pressStartTextView.hide();
                }
            }
        },

        SHOWING_MARQUEE {
            @Override
            public void onEnter(TengenMsPacMan_IntroScene scene) {
                final GameVariantRuntime variant = scene.app().variantManager().currentRuntime();
                final ActorSpriteAnimController animController = variant.playConfig().systems().actorSpriteAnimController();
                final GameSystems systems = variant.playConfig().systems();
                final WorldNavigationSystem nav = systems.navigator();

                timer.restartTicks(TickTimer.INDEFINITE);

                scene.marquee.show();

                scene.msPacMan.pos().set(TS * 33, ACTOR_Y);
                scene.msPacMan.show();

                nav.setMoveDir(scene.msPacMan, Direction.LEFT);
                nav.setSpeed(scene.msPacMan, SPEED);

                animController.select(scene.msPacMan, CommonSpriteAnimationID.PAC_MOUTH_MOVING);
                animController.playSelected(scene.msPacMan);

                for (Ghost ghost : scene.ghosts) {
                    ghost.pos().set(TS * 33, ACTOR_Y);
                    ghost.show();

                    nav.setMoveDir(ghost, Direction.LEFT);
                    nav.setWishDir(ghost, Direction.LEFT);
                    nav.setSpeed(ghost, SPEED);

                    animController.playSelected(ghost);
                    systems.ghostState().setState(ghost, GhostState.HUNTING_PAC);
                }
                scene.ghostIndex = 0;
            }

            @Override
            public void onUpdate(TengenMsPacMan_IntroScene scene) {
                if (timer.atSecond(1)) {
                    scene.flow.enterState(scene, GHOSTS_MARCHING_IN);
                }
            }
        },

        GHOSTS_MARCHING_IN {
            @Override
            public void onEnter(TengenMsPacMan_IntroScene scene) {
                timer.restartTicks(TickTimer.INDEFINITE);
                scene.waitBeforeRising = 0;
            }

            @Override
            public void onUpdate(TengenMsPacMan_IntroScene scene) {

                final Ghost currentGhost = scene.currentGhost();
                final int personalityIndex = currentGhost.personality().ordinal();
                final Color ghostColor = scene.ghostColors[personalityIndex];
                if (scene.ghostIndex == 0) {
                    scene.marqueeTextView1.data().setText(WITH);
                    scene.marqueeTextView1.data().setFillColor(NES_Palette.color(0x20));
                    scene.marqueeTextView1.pos().set(ANCHOR_X + 12, ANCHOR_Y + 23);
                    scene.marqueeTextView1.show();
                } else {
                    scene.marqueeTextView1.hide();
                }
                scene.marqueeTextView2.data().setText(currentGhost.name().toUpperCase());
                scene.marqueeTextView2.data().setFillColor(ghostColor);
                scene.marqueeTextView2.pos().set(ANCHOR_X + 44, ANCHOR_Y + 41);

                boolean reachedEndPosition = letGhostMarchIn(scene);
                if (reachedEndPosition) {
                    if (scene.ghostIndex == 3) {
                        scene.flow.enterState(scene, MS_PACMAN_MARCHING_IN);
                    } else {
                        ++scene.ghostIndex;
                    }
                }
            }

            @Override
            public void onExit(TengenMsPacMan_IntroScene scene) {
                scene.marqueeTextView1.hide();
                scene.marqueeTextView2.hide();
            }

            boolean letGhostMarchIn(TengenMsPacMan_IntroScene scene) {
                final GameSystems systems = scene.game().playConfig().systems();
                final MovementSystem motor = systems.motor();
                final WorldNavigationSystem nav = systems.navigator();

                final Ghost ghost = scene.ghosts.get(scene.ghostIndex);
                if (ghost.worldNavigation().moveDir() == Direction.LEFT) {
                    if (ghost.pos().x() <= GHOST_STOP_X) {
                        ghost.pos().setX(GHOST_STOP_X);
                        nav.setMoveDir(ghost, Direction.UP);
                        nav.setWishDir(ghost, Direction.UP);
                        scene.waitBeforeRising = 2;
                    } else {
                        motor.move(ghost);
                        Logger.debug("{} moves {} x={}", ghost.name(), ghost.worldNavigation().moveDir(), ghost.pos().x());
                    }
                }
                else if (ghost.worldNavigation().moveDir() == Direction.UP) {
                    final int endPositionY = ANCHOR_Y + scene.ghostIndex * 16;
                    if (scene.waitBeforeRising > 0) {
                        scene.waitBeforeRising--;
                    }
                    else if (ghost.pos().y() <= endPositionY) {
                        nav.setSpeed(ghost, 0);
                        nav.setMoveDir(ghost, Direction.RIGHT);
                        nav.setWishDir(ghost, Direction.RIGHT);
                        return true;
                    }
                    else {
                        motor.move(ghost);
                        Logger.debug("{} moves {}", ghost.name(), ghost.worldNavigation().moveDir());
                    }
                }
                return false;
            }
        },

        MS_PACMAN_MARCHING_IN {
            @Override
            public void onEnter(TengenMsPacMan_IntroScene scene) {
                timer.restartTicks(TickTimer.INDEFINITE);
            }

            @Override
            public void onUpdate(TengenMsPacMan_IntroScene scene) {
                final GameContext game = scene.game();
                final GameFlow flow = game.playConfig().gameFlow();
                final GameSystems systems = game.playConfig().systems();
                final ActorSpriteAnimController animController = systems.actorSpriteAnimController();
                final MovementSystem motor = systems.motor();
                final WorldNavigationSystem nav = systems.navigator();

                scene.marqueeTextView1.pos().set(ANCHOR_X + 12, ANCHOR_Y + 22);
                scene.marqueeTextView1.data().setText(STARRING);
                scene.marqueeTextView1.data().setFillColor(NES_Palette.color(0x20));
                scene.marqueeTextView1.show();

                scene.marqueeTextView2.pos().set(ANCHOR_X + 28, ANCHOR_Y + 38);
                scene.marqueeTextView2.data().setText(MS_PAC_MAN);
                scene.marqueeTextView2.data().setFillColor(NES_Palette.color(0x28));
                scene.marqueeTextView2.show();

                motor.move(scene.msPacMan);
                if (scene.msPacMan.pos().x() <= MS_PAC_MAN_STOP_X) {
                    nav.setSpeed(scene.msPacMan, 0);
                    animController.resetSelected(scene.msPacMan);
                }
                if (timer.atSecond(8)) {
                    // start demo level or show options
                    if (gameOptions(game.session()).areInitial()) {
                        gameOptions(game.session()).setCanStartNewGame(false); // TODO check this
                        flow.restartState(game, Tengen_GameState.GAME_OR_LEVEL_STARTING.state());
                    } else {
                        flow.enterState(game, Tengen_GameState.GAME_PREPARATION.state());
                    }
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