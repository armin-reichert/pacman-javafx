/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;

import static de.amr.pacmanfx.uilib.animation.SpriteAnimation.buildAnimation;

public class Stork extends Actor {

    public enum AnimationID { FLYING }

    public static class StorkAnimations extends SpriteAnimationManager<SpriteID> {

        public StorkAnimations() {
            super(ArcadeMsPacMan_SpriteSheet.instance());
        }

        @Override
        protected SpriteAnimation createAnimation(Object animationID) {
            if (animationID.equals(AnimationID.FLYING)) {
                return buildAnimation().sprites(spriteSheet.sprites(SpriteID.STORK)).ticksPerFrame(8).repeated();
            }
            throw new IllegalArgumentException("Illegal animation ID: " + animationID);
        }
    }

    public Stork() {
        setAnimationManager(new StorkAnimations());
    }
}