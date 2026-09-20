/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.gamescene.introscene;

import de.amr.basics.math.Direction;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_ActorFactory;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.core.entities.props.marquee.Marquee;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.entities.props.textdisplay.TextDisplay;
import de.amr.pacmanfx.core.entities.actor.ghost.GhostState;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.game.GameVariantRuntime;
import de.amr.pacmanfx.ui.assets.GlobalFonts;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.core.entities.props.imagedisplay.ImageDisplay;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static de.amr.pacmanfx.arcade.ms_pacman.gamescene.introscene.ArcadeMsPacMan_IntroScene.*;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static de.amr.pacmanfx.game.GameVariantRenderConfig.renderablePac;
import static de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene.createText;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.*;

public class IntroSceneView {

    static final String MARQUEE_TITLE = "\"MS PAC-MAN\"";

    private Marquee marquee;
    private ImageDisplay copyrightImage;
    private final List<TextDisplay> copyrightTexts = new ArrayList<>();
    private TextDisplay titleText;
    private TextDisplay marqueeText1;
    private TextDisplay marqueeText2;

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
            titleText,
            marquee,
            marqueeText1,
            marqueeText2,
            renderablePac(msPacMan),
            ghosts.stream().map(GameVariantRenderConfig::renderableGhost),
            copyrightImage, copyrightTexts);
    }

    public Pac msPacMan() {
        return msPacMan;
    }

    public List<Ghost> ghosts() {
        return ghosts;
    }

    public void showMarqueeText1(String text, Color color) {
        marqueeText1.data().setText(text);
        marqueeText1.data().setFillColor(color);
        marqueeText1.show();
    }

    public void hideMarqueeText1() {
        marqueeText1.hide();
    }

    public void showMarqueeText2(String text, Color color) {
        marqueeText2.data().setText(text);
        marqueeText2.data().setFillColor(color);
        marqueeText2.show();
    }

    public void placeMarqueeText2(float x, float y) {
        marqueeText2.pos().set(x, y);
    }

    private void createTitleText() {
        titleText = new TextDisplay();
        titleText.data().setText(MARQUEE_TITLE);
        titleText.data().setFillColor(ARCADE_ORANGE);
        titleText.data().setFont(GlobalFonts.ARCADE.font(8));
        titleText.pos().set(TITLE_X, TITLE_Y);
        titleText.show();
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

        marquee.visualization().setBulbOffColor(ARCADE_RED.toString());
        marquee.visualization().setBulbOnColor(ARCADE_WHITE.toString());

        marqueeText1 = new TextDisplay();
        marqueeText1.data().setFont(GlobalFonts.ARCADE.font(TS));
        marqueeText1.pos().set(TITLE_X, TOP_Y + tilesPx(3));

        marqueeText2 = new TextDisplay();
        marqueeText2.data().setFont(GlobalFonts.ARCADE.font(TS));
    }

    private void createCopyright(GameVariantRuntime runtime) {
        final AssetMap assets = runtime.uiConfig().assets();

        copyrightImage = new ImageDisplay();
        copyrightImage.show();
        copyrightImage.pos().set(tilesPx(6), tilesPx(28));
        copyrightImage.image().setImage(assets.image("logo.midway"));

        copyrightTexts.add(createText("©",             ARCADE_RED, 8, 11, 30.125f));
        copyrightTexts.add(createText("MIDWAY MFG CO", ARCADE_RED, 8, 13, 30));
        copyrightTexts.add(createText("1980/1981",     ARCADE_RED, 8, 14, 32));
        copyrightTexts.forEach(TextDisplay::show);
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
