package de.amr.pacmanfx.ui._3d;

import javafx.animation.Animation;
import org.tinylog.Logger;

import java.util.List;

public interface AnimationProvider3D {

    List<Animation> animations();

     default void stopAnimations() {
        animations().forEach(animation -> {
            try {
                animation.stop();
            } catch (IllegalStateException x) {
                Logger.warn("Animation could not be stopped (probably embedded in another one)");
            }
        });
    }
}
