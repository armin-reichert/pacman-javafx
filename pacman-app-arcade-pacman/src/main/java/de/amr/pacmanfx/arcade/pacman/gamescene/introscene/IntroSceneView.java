/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.gamescene.introscene;

import de.amr.basics.ecs.GameEntity;
import de.amr.basics.math.RectShort;
import de.amr.basics.timer.Pulse;
import de.amr.basics.ui.ecs.system.ActorSpriteAnimController;
import de.amr.basics.ui.entities.props.ghostpoints.GhostPoints;
import de.amr.basics.ui.entities.props.imagedisplay.ImageView;
import de.amr.basics.ui.entities.props.textdisplay.TextView;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.spriteanim.CommonSpriteAnimationID;
import de.amr.basics.ui.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.pacman.rendering.SpriteID;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.assets.GlobalFonts;
import de.amr.pacmanfx.ui.rendering.RenderableFactory;
import de.amr.pacmanfx.uilib.ArcadeColor;
import javafx.scene.image.Image;

import java.util.Arrays;
import java.util.stream.Stream;

import static de.amr.pacmanfx.arcade.pacman.rendering.SpriteID.GALLERY_GHOSTS;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.*;
import static de.amr.pacmanfx.ui.rendering.RenderableFactory.createPropView;

public class IntroSceneView {

    public static final int NUM_GHOSTS = 4;

    private static final String   TITLE_TEXT = "CHARACTER / NICKNAME";
    private static final String   MIDWAY_MFG_CO = "© 1980 MIDWAY MFG.CO.";

    private static final String[] GHOST_NICKNAMES  = { "\"BLINKY\"", "\"PINKY\"", "\"INKY\"", "\"CLYDE\"" };
    private static final String[] GHOST_CHARACTERS = { "-SHADOW", "-SPEEDY", "-BASHFUL", "-POKEY" };
    private static final ArcadeColor[] GHOST_COLORS = { ArcadeColor.RED, ArcadeColor.PINK, ArcadeColor.CYAN, ArcadeColor.ORANGE };

    private static final int LEFT_TILE_X = 4;
    private static final int TARGET_ENERGIZER_CENTER_X = TS * LEFT_TILE_X + 2;
    private static final int TARGET_ENERGIZER_CENTER_Y = TS * 20 + 2;

    final Pulse pulse = new Pulse(10, Pulse.State.ON);

    final TextView titleTextView;

    // Ghost presentation
    final ImageView[] ghostImageViews;
    final TextView[]  ghostNicknameDisplays;
    final TextView[]  ghostCharacterDisplays;

    // Chase animation
    ImageView targetEnergizer;
    GhostPoints points;
    Pac pacMan;
    Ghost[] ghosts;

    // Points display
    ImageView pointsEnergizer;
    final ImageView pellet;
    final TextView text10;
    final TextView text10Pts;
    final TextView text50;
    final TextView text50Pts;
    final TextView copyrightText;

    public IntroSceneView() {
        titleTextView = new TextView();

        // Ghost gallery
        ghostImageViews = new ImageView[NUM_GHOSTS];
        ghostNicknameDisplays = new TextView[NUM_GHOSTS];
        ghostCharacterDisplays = new TextView[NUM_GHOSTS];
        for (int i = 0; i < NUM_GHOSTS; ++i) {
            ghostImageViews[i] = new ImageView();
            ghostCharacterDisplays[i] = new TextView();
            ghostNicknameDisplays[i] = new TextView();
        }

        final ArcadePacMan_SpriteSheet spriteSheet = ArcadePacMan_SpriteSheet.instance();
        final Image pelletImage = spriteSheet.createImage(SpriteID.PELLET);

        pellet = new ImageView();
        pellet.image().setImage(pelletImage);
        pellet.pos().set(tilesPx(10) + HTS, tilesPx(24) + HTS);

        text10 = new TextView();
        text10Pts = new TextView();
        text50 = new TextView();
        text50Pts = new TextView();
        copyrightText = new TextView();

        initTitleText();
        initGhostGallery();
        initPointsExplanation();
        initCopyrightText();
    }

    public void createAndShowPointsEnergizer() {
        final ArcadePacMan_SpriteSheet spriteSheet = ArcadePacMan_SpriteSheet.instance();
        final Image energizerImage = spriteSheet.createImage(SpriteID.ENERGIZER);
        pointsEnergizer = new ImageView();
        pointsEnergizer.image().setImage(energizerImage);
        pointsEnergizer.pos().set(tilesPx(LEFT_TILE_X + 6), tilesPx(26));
    }

    public void removePointsEnergizer() {
        pointsEnergizer = null;
    }

    public void createAndShowTargetEnergizer() {
        final ArcadePacMan_SpriteSheet spriteSheet = ArcadePacMan_SpriteSheet.instance();
        final Image energizerImage = spriteSheet.createImage(SpriteID.ENERGIZER);
        targetEnergizer = new ImageView();
        targetEnergizer.image().setImage(energizerImage);
        targetEnergizer.pos().set(TARGET_ENERGIZER_CENTER_X, TARGET_ENERGIZER_CENTER_Y);
    }

    public void removeTargetEnergizer() {
        targetEnergizer = null;
    }

    public void createPacManAndGhosts(GameVariantRenderConfig renderConfig, ActorSpriteAnimController animController, SpriteAnimationContainer animContainer) {
        final var actorFactory = ArcadePacMan_ActorFactory.instance();

        pacMan = actorFactory.createPacMan();
        pacMan.spriteAnim().setSpriteAnimations(renderConfig.createPacAnimations(animContainer));
        pacMan.spriteAnim().spriteAnimations().select(CommonSpriteAnimationID.PAC_MOUTH_MOVING);
        pacMan.spriteAnim().spriteAnimations().playSelected();

        ghosts = new Ghost[] {
            renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.RED_GHOST_SHADOW),
            renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.PINK_GHOST_SPEEDY),
            renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.CYAN_GHOST_BASHFUL),
            renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.ORANGE_GHOST_POKEY)
        };
    }

    public Stream<Renderable> renderables() {
        return Renderable.createRenderableStream(
            createPropView(titleTextView),
            visibleEntities(ghostImageViews).map(RenderableFactory::createPropView),
            visibleEntities(ghostCharacterDisplays).map(RenderableFactory::createPropView),
            visibleEntities(ghostNicknameDisplays).map(RenderableFactory::createPropView),
            targetEnergizer != null ? createPropView(targetEnergizer) : null,
            createPropView(text10),
            createPropView(text10Pts),
            createPropView(text50),
            createPropView(text50Pts),
            createPropView(pellet),
            pointsEnergizer != null ? createPropView(pointsEnergizer) : null,
            copyrightText.isVisible() ? createPropView(copyrightText) : null,
            pacMan.isVisible() ? createPropView(pacMan) : null,
            visibleEntities(ghosts).map(RenderableFactory::createPropView),
            points != null && points.isVisible() ? createPropView(points) : null
        );
    }

    private Stream<GameEntity> visibleEntities(GameEntity[] entityArray) {
        return Arrays.stream(entityArray).filter(GameEntity::isVisible);
    }

    public void hideEverything() {
        for (int i = 0; i < NUM_GHOSTS; ++i) {
            ghostImageViews[i].hide();
            ghostCharacterDisplays[i].hide();
            ghostNicknameDisplays[i].hide();
        }

        if (pacMan != null) {
            pacMan.hide();
        }

        if (ghosts != null) {
            for (int i = 0; i < NUM_GHOSTS; ++i) {
                ghosts[i].hide();
            }
        }
        targetEnergizer = null;
        points = null; // points for killed ghost

        pellet.hide();
        text10.hide();
        text10Pts.hide();

        pointsEnergizer = null;
        text50.hide();
        text50Pts.hide();

        copyrightText.hide();
    }

    private void initTitleText() {
        titleTextView.data().setText(TITLE_TEXT);
        titleTextView.data().setFillColor(ArcadeColor.WHITE.color());
        titleTextView.data().setFont(GlobalFonts.ARCADE.font(TS));
        titleTextView.pos().set(tilesPx(LEFT_TILE_X + 3), tilesPx(6));
    }

    private void initGhostGallery() {
        final var spriteSheet = ArcadePacMan_SpriteSheet.instance();
        final int y = TS * 8;

        for (int i = 0; i < NUM_GHOSTS; ++i) {
            final int offsetY = 3 * i * TS;

            final ImageView imageView = ghostImageViews[i];
            final RectShort sprite = spriteSheet.findSpriteSequence(GALLERY_GHOSTS)[i];
            imageView.image().setImage(spriteSheet.createImage(sprite));
            imageView.pos().set(TS * 4, y + offsetY - 1.5f * TS);

            final TextView characterDisplay = ghostCharacterDisplays[i];
            characterDisplay.data().setText(GHOST_CHARACTERS[i]);
            characterDisplay.data().setFillColor(GHOST_COLORS[i].color());
            characterDisplay.data().setFont(GlobalFonts.ARCADE.font(TS));
            characterDisplay.pos().set(TS * 7, y + offsetY);

            final TextView nicknameDisplay = ghostNicknameDisplays[i];
            nicknameDisplay.data().setText(GHOST_NICKNAMES[i]);
            nicknameDisplay.data().setFillColor(GHOST_COLORS[i].color());
            nicknameDisplay.data().setFont(GlobalFonts.ARCADE.font(TS));
            nicknameDisplay.pos().set(TS * 18, y + offsetY);
        }
    }

    private void initPointsExplanation() {
        text10.data().setText("10");
        text10.data().setFillColor(ArcadeColor.WHITE.color());
        text10.data().setFont(GlobalFonts.ARCADE.font(TS));
        text10.pos().set(tilesPx(LEFT_TILE_X + 8), tilesPx(25));

        text10Pts.data().setText("PTS");
        text10Pts.data().setFillColor(ArcadeColor.WHITE.color());
        text10Pts.data().setFont(GlobalFonts.ARCADE.font(6));
        text10Pts.pos().set(tilesPx(LEFT_TILE_X + 11), tilesPx(25));

        text50.data().setText("50");
        text50.data().setFillColor(ArcadeColor.WHITE.color());
        text50.data().setFont(GlobalFonts.ARCADE.font(TS));
        text50.pos().set(tilesPx(LEFT_TILE_X + 8), tilesPx(27));

        text50Pts.data().setText("PTS");
        text50Pts.data().setFillColor(ArcadeColor.WHITE.color());
        text50Pts.data().setFont(GlobalFonts.ARCADE.font(6));
        text50Pts.pos().set(tilesPx(LEFT_TILE_X + 11), tilesPx(27));
    }

    private void initCopyrightText() {
        copyrightText.data().setText(MIDWAY_MFG_CO);
        copyrightText.data().setFillColor(ArcadeColor.PINK.color());
        copyrightText.data().setFont(GlobalFonts.ARCADE.font(TS));
        copyrightText.pos().set(tilesPx(4), tilesPx(32));
    }
}
