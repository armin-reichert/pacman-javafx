/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.gamescene.introscene;

import de.amr.basics.math.RectShort;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.basics.timer.Pulse;
import de.amr.basics.ui.ecs.GameEntity;
import de.amr.basics.ui.ecs.systems.ActorSpriteAnimController;
import de.amr.basics.ui.entities.props.imagedisplay.ImageView;
import de.amr.basics.ui.entities.props.textdisplay.TextView;
import de.amr.basics.ui.spriteanim.CommonSpriteAnimationID;
import de.amr.basics.ui.spriteanim.SpriteAnimationContainer;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_SpriteSheet;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.basics.ui.entities.props.ghostpoints.GhostPoints;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.assets.GlobalFonts;
import de.amr.pacmanfx.uilib.ArcadeColor;

import java.util.Arrays;
import java.util.stream.Stream;

import static de.amr.pacmanfx.arcade.pacman.rendering.SpriteID.GALLERY_GHOSTS;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.*;
import static de.amr.pacmanfx.game.GameVariantRenderConfig.createEntityView;
import static de.amr.pacmanfx.game.GameVariantRenderConfig.createPacView;

public class IntroSceneView {

    public static final int NUM_GHOSTS = 4;

    private static final String   TITLE_TEXT = "CHARACTER / NICKNAME";
    private static final String   MIDWAY_MFG_CO = "© 1980 MIDWAY MFG.CO.";

    private static final String[] GHOST_NICKNAMES  = { "\"BLINKY\"", "\"PINKY\"", "\"INKY\"", "\"CLYDE\"" };
    private static final String[] GHOST_CHARACTERS = { "-SHADOW", "-SPEEDY", "-BASHFUL", "-POKEY" };
    private static final ArcadeColor[] GHOST_COLORS = { ArcadeColor.RED, ArcadeColor.PINK, ArcadeColor.CYAN, ArcadeColor.ORANGE };

    private static final int LEFT_TILE_X = 4;
    private static final int ENERGIZER_CENTER_X = TS * LEFT_TILE_X + HTS;
    private static final int ENERGIZER_CENTER_Y = TS * 20 + HTS;

    final Pulse pulse = new Pulse(10, Pulse.State.ON);

    final TextView titleTextView;

    // Ghost presentation
    final ImageView[] ghostImageViews;
    final TextView[]  ghostNicknameDisplays;
    final TextView[]  ghostCharacterDisplays;

    // Chase animation
    final Energizer targetEnergizer;
    GhostPoints points;
    Pac pacMan;
    Ghost[] ghosts;

    // Points display
    final Energizer energizer;
    final Pellet pellet;
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

        // Chase animation
        targetEnergizer = new Energizer();

        // Points display
        energizer = new Energizer();
        pellet = new Pellet();
        text10 = new TextView();
        text10Pts = new TextView();
        text50 = new TextView();
        text50Pts = new TextView();
        copyrightText = new TextView();

        initTitleText();
        initGhostGallery();
        initTargetEnergizer();
        initPointsExplanation();
        initCopyrightText();
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
        return Ufx.streamOf(
            titleTextView,
            Arrays.stream(ghostImageViews).filter(GameEntity::isVisible),
            Arrays.stream(ghostCharacterDisplays).filter(GameEntity::isVisible),
            Arrays.stream(ghostNicknameDisplays).filter(GameEntity::isVisible),
            targetEnergizer.isVisible() ? targetEnergizer :null,
            text10,
            text10Pts,
            text50,
            text50Pts,
            pellet.isVisible() ? pellet : null,
            energizer.isVisible() ? energizer : null,
            copyrightText.isVisible() ? copyrightText : null,
            pacMan.isVisible() ? createPacView(pacMan) : null,
            Arrays.stream(ghosts).filter(Ghost::isVisible).map(GameVariantRenderConfig::createGhostView),
            points != null && points.isVisible() ? createEntityView(points, RenderingLayer.PROPS, 0) : null
        );
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
        targetEnergizer.hide();
        points = null; // points for killed ghost

        pellet.hide();
        text10.hide();
        text10Pts.hide();

        energizer.hide();
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
        pellet.pos().set(tilesPx(LEFT_TILE_X + 6) + HTS, tilesPx(24) + 4);

        energizer.setPulse(pulse);
        energizer.pos().set(tilesPx(LEFT_TILE_X + 6) + HTS, tilesPx(26) + HTS);

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

    private void initTargetEnergizer() {
        targetEnergizer.setPulse(pulse);
        targetEnergizer.pos().set(ENERGIZER_CENTER_X, ENERGIZER_CENTER_Y);
    }

    private void initCopyrightText() {
        copyrightText.data().setText(MIDWAY_MFG_CO);
        copyrightText.data().setFillColor(ArcadeColor.PINK.color());
        copyrightText.data().setFont(GlobalFonts.ARCADE.font(TS));
        copyrightText.pos().set(tilesPx(4), tilesPx(32));
    }
}
