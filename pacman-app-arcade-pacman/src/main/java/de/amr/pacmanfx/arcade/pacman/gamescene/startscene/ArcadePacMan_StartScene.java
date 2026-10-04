/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.gamescene.startscene;

import de.amr.basics.ui.assets.ArcadeColor;
import de.amr.basics.ui.entities.props.textview.TextView;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.ui.assets.GlobalFonts;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.rendering.GameEntityViewBuilder;

import java.util.List;
import java.util.stream.Stream;

/**
 * Scene shown after credit has been added and where game can be started.
 */
public class ArcadePacMan_StartScene extends AbstractGameScene {

    private final List<TextView> texts = List.of(
        TextView.create("PUSH START BUTTON",       ArcadeColor.ORANGE.color(), GlobalFonts.ARCADE.font(8),  6, 17),
        TextView.create("1 PLAYER ONLY",           ArcadeColor.CYAN.color(),   GlobalFonts.ARCADE.font(8),  8, 21),
        TextView.create("BONUS PAC-MAN FOR 10000", ArcadeColor.ROSE.color(),   GlobalFonts.ARCADE.font(8),  1, 25),
        TextView.create("PTS",                     ArcadeColor.ROSE.color(),   GlobalFonts.ARCADE.font(6), 25, 25),
        TextView.create("© 1980 MIDWAY MFG.CO.",   ArcadeColor.PINK.color(),   GlobalFonts.ARCADE.font(8),  4, 29)
    );

    public ArcadePacMan_StartScene() {}

    @Override
    public Stream<Renderable> renderables() {
        return texts.stream().map(GameEntityViewBuilder::propView);
    }

    @Override
    public void onActivate() {
        final Arcade_Actions actions = app().variantManager().currentRuntime()
            .extensionValue(Arcade_GameExtensions.ACTIONS, Arcade_Actions.class);

        final var bindingsMap = actionBindings().registry();
        bindingsMap.registerAllBindings(actions.gameStartActionBindings());
    }

    @Override
    public void onDeactivate() {
        soundManager().voice().stop();
    }

    @Override
    public void onTick(GameContext game) {}
}