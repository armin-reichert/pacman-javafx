/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.gamescene.cutscenes;


class CutScene2TimingComp extends CutSceneTimingComp {

    final int TICK_PAC_MAN_STARTS_RUNNING;
    final int TICK_BLINKY_STARTS_RUNNING;
    final int TICK_BLINKY_GETS_CAUGHT;
    final int TICK_DRESS_STRETCHED_SMALL;
    final int TICK_DRESS_STRETCHED_MEDIUM;
    final int TICK_DRESS_STRETCHED_LARGE;
    final int TICK_BLINKY_STOPS_MOVING;
    final int TICK_DRESS_RAPTURES;
    final int TICK_BLINK_INSPECTS_DAMAGE;
    final int TICK_ANIMATION_ENDS;

    public CutScene2TimingComp(int animationStartTick) {
        super(120);
        TICK_PAC_MAN_STARTS_RUNNING = animationStartTick + 25;
        TICK_BLINKY_STARTS_RUNNING = animationStartTick + 111;
        TICK_BLINKY_GETS_CAUGHT = animationStartTick + 194;
        TICK_DRESS_STRETCHED_SMALL = animationStartTick + 198;
        TICK_DRESS_STRETCHED_MEDIUM = animationStartTick + 230;
        TICK_DRESS_STRETCHED_LARGE = animationStartTick + 262;
        TICK_BLINKY_STOPS_MOVING = animationStartTick + 296;
        TICK_DRESS_RAPTURES = animationStartTick + 360;
        TICK_BLINK_INSPECTS_DAMAGE = animationStartTick + 420;
        TICK_ANIMATION_ENDS = animationStartTick + 508;
    }
}
