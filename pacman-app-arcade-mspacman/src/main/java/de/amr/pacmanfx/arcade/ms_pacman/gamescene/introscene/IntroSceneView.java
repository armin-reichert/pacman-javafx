/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.gamescene.introscene;

import de.amr.basics.math.Direction;
import de.amr.basics.ui.ecs.system.ActorSpriteAnimController;
import de.amr.basics.ui.entities.props.imagedisplay.ImageView;
import de.amr.basics.ui.entities.props.marquee.Marquee;
import de.amr.basics.ui.entities.props.textdisplay.TextView;
import de.amr.basics.ui.spriteanim.SpriteAnimationContainer;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_ActorFactory;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.pacmanfx.core.entities.world.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.entities.actor.ghost.GhostState;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.game.GameVariantRuntime;
import de.amr.pacmanfx.ui.assets.GlobalFonts;
import de.amr.basics.ui.assets.AssetMap;
import de.amr.pacmanfx.uilib.ArcadeColor;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static de.amr.pacmanfx.arcade.ms_pacman.gamescene.introscene.ArcadeMsPacMan_IntroScene.*;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static de.amr.pacmanfx.game.GameVariantRenderConfig.createEntityView;
import static de.amr.pacmanfx.game.GameVariantRenderConfig.createPacView;
import static de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene.createText;

public class IntroSceneView {

    static final String MARQUEE_TITLE = "\"MS PAC-MAN\"";

    private Marquee marquee;
    private ImageView copyrightImageView;
    private final List<TextView> copyrightTexts = new ArrayList<>();
    private TextView titleTextView;
    private TextView marqueeTextView1;
    private TextView marqueeTextView2;

    private Pac msPacMan;
    private List<Ghost> ghosts;

    public IntroSceneView(GameVariantRuntime runtime) {
        createTitleText();
        createMarquee();
        createMsPacManAndTheGhosts(runtime);
        createCopyright(runtime);
    }

    public Stream<Renderable> renderables() {
        return Ufx.streamOf(
            titleTextView,
            createEntityView(marquee, RenderingLayer.PROPS, 0),
            marqueeTextView1,
            marqueeTextView2,
            createPacView(msPacMan),
            ghosts.stream().map(GameVariantRenderConfig::createGhostView),
            copyrightImageView, copyrightTexts);
    }

    public Pac msPacMan() {
        return msPacMan;
    }

    public List<Ghost> ghosts() {
        return ghosts;
    }

    public void showMarqueeText1(String text, Color color) {
        marqueeTextView1.data().setText(text);
        marqueeTextView1.data().setFillColor(color);
        marqueeTextView1.show();
    }

    public void hideMarqueeText1() {
        marqueeTextView1.hide();
    }

    public void showMarqueeText2(String text, Color color) {
        marqueeTextView2.data().setText(text);
        marqueeTextView2.data().setFillColor(color);
        marqueeTextView2.show();
    }

    public void placeMarqueeText2(float x, float y) {
        marqueeTextView2.pos().set(x, y);
    }

    private void createTitleText() {
        titleTextView = new TextView();
        titleTextView.data().setText(MARQUEE_TITLE);
        titleTextView.data().setFillColor(ArcadeColor.ORANGE.color());
        titleTextView.data().setFont(GlobalFonts.ARCADE.font(8));
        titleTextView.pos().set(TITLE_X, TITLE_Y);
        titleTextView.show();
    }

    private void createMarquee() {
        marquee = new Marquee();
        marquee.pos().set(60, 88);
        marquee.show();

        marquee.layout().setNumBulbsHorizontally(34);
        marquee.layout().setNumBulbsVertically(16);
        marquee.layout().setBulbSize(4);
        marquee.layout().setBrightBulbsCount(6);
        marquee.layout().setBrightBulbsDistance(16);

        marquee.visualization().setBulbOffColor(ArcadeColor.RED.toString());
        marquee.visualization().setBulbOnColor(ArcadeColor.WHITE.toString());

        marqueeTextView1 = new TextView();
        marqueeTextView1.data().setFont(GlobalFonts.ARCADE.font(TS));
        marqueeTextView1.pos().set(TITLE_X, TOP_Y + tilesPx(3));

        marqueeTextView2 = new TextView();
        marqueeTextView2.data().setFont(GlobalFonts.ARCADE.font(TS));
    }

    private void createCopyright(GameVariantRuntime runtime) {
        final AssetMap assets = runtime.uiConfig().assets();

        copyrightImageView = new ImageView();
        copyrightImageView.show();
        copyrightImageView.pos().set(tilesPx(6), tilesPx(28));
        copyrightImageView.image().setImage(assets.image("logo.midway"));

        copyrightTexts.add(createText("©",             ArcadeColor.RED.color(), 8, 11, 30.125f));
        copyrightTexts.add(createText("MIDWAY MFG CO", ArcadeColor.RED.color(), 8, 13, 30));
        copyrightTexts.add(createText("1980/1981",     ArcadeColor.RED.color(), 8, 14, 32));
        copyrightTexts.forEach(TextView::show);
    }

    private void createMsPacManAndTheGhosts(GameVariantRuntime runtime) {
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
}
