/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.ActorAnimationMap;
import de.amr.pacmanfx.model.actors.Animated;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;

import java.util.Optional;

import static de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID.BLUE_BAG;
import static de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID.JUNIOR_PAC;

class Bag extends Actor implements Animated {
    private final SpriteAnimationMap<SpriteID> animationMap;
    private boolean open;

    public Bag(ArcadeMsPacMan_SpriteSheet spriteSheet) {
        animationMap = new SpriteAnimationMap<>(spriteSheet);
        animationMap.setAnimation("junior", SpriteAnimation.build().ofSprite(spriteSheet.sprite(JUNIOR_PAC)).once());
        animationMap.setAnimation("bag", SpriteAnimation.build().ofSprite(spriteSheet.sprite(BLUE_BAG)).once());
    }

    @Override
    public Optional<ActorAnimationMap> animationMap() {
        return Optional.of(animationMap);
    }

    public void setOpen(boolean open) {
        this.open = open;
        animationMap.selectAnimation(open ? "junior" : "bag");
    }

    public boolean isOpen() {
        return open;
    }
}
