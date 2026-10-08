/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.gamescene.startscene;

import de.amr.basics.ui.assets.AssetMap;
import de.amr.basics.ui.entities.props.imagedisplay.ImageView;
import de.amr.basics.ui.entities.props.textview.TextView;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.ui.assets.GlobalFonts;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.rendering.GameEntityViewBuilder;
import de.amr.basics.ui.assets.ArcadeColor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static de.amr.basics.TileDimension.TS;
import static de.amr.basics.TileDimension.tilesPx;
import static de.amr.pacmanfx.ui.rendering.GameEntityViewBuilder.propView;

public class ArcadeMsPacMan_StartScene extends AbstractGameScene {

    private final List<TextView> texts = List.of(
        TextView.create("PUSH START BUTTON",      ArcadeColor.ORANGE.color(), GlobalFonts.ARCADE.font(8),  6, 16),
        TextView.create("1 PLAYER ONLY",          ArcadeColor.ORANGE.color(), GlobalFonts.ARCADE.font(8),  8, 18),
        TextView.create("ADDITIONAL    AT 10000", ArcadeColor.ORANGE.color(), GlobalFonts.ARCADE.font(8),  2, 25),
        TextView.create("PTS",                    ArcadeColor.ORANGE.color(), GlobalFonts.ARCADE.font(6), 25, 25)
    );

    private final ImageView msPacManImageView;
    private final ImageView copyrightImageView;
    private final List<TextView> copyrightTexts = new ArrayList<>();

    public ArcadeMsPacMan_StartScene() {
        msPacManImageView = new ImageView();
        msPacManImageView.image().setImage(ArcadeMsPacMan_SpriteSheet.instance().createImage(SpriteID.LIVES_COUNTER_SYMBOL));
        msPacManImageView.pos().set(13 * TS, 23.5 * TS);
        msPacManImageView.show();

        copyrightImageView = new ImageView();
        copyrightImageView.show();
        copyrightImageView.pos().set(tilesPx(6), tilesPx(28));

        copyrightTexts.add(TextView.create("©",             ArcadeColor.RED.color(), GlobalFonts.ARCADE.font(8), 11, 30.125f));
        copyrightTexts.add(TextView.create("MIDWAY MFG CO", ArcadeColor.RED.color(), GlobalFonts.ARCADE.font(8), 13, 30));
        copyrightTexts.add(TextView.create("1980/1981",     ArcadeColor.RED.color(), GlobalFonts.ARCADE.font(8), 14, 32));
        copyrightTexts.forEach(TextView::show);
    }

    @Override
    public Stream<Renderable> renderables() {
        return Ufx.streamOf(
            texts.stream().map(GameEntityViewBuilder::propView),
            propView(msPacManImageView),
            propView(copyrightImageView),
            copyrightTexts.stream().map(GameEntityViewBuilder::propView)
        );
    }

    @Override
    protected void onEngineConnected() {
        final AssetMap assets = engine().gameVariantManager().currentRuntime().uiConfig().assets();
        copyrightImageView.image().setImage(assets.image("logo.midway"));
    }

    @Override
    public void onActivate() {
        // Bind "insert coin" + "start game" actions
        final Arcade_Actions actions = engine().gameVariantManager().currentRuntime()
            .extensionValue(Arcade_GameExtensions.ACTIONS, Arcade_Actions.class);

        actionBindings().registry().registerAllBindings(actions.gameStartActionBindings());
    }

    @Override
    public void onDeactivate() {
        soundManager().voice().stop();
        actionBindings().registry().dispose();
    }

    @Override
    public void onTick(GameContext game) {}
}