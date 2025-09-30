import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.uilib.model3D.GhostAppearance;
import de.amr.pacmanfx.uilib.model3D.MutableGhost3D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestGhostAppearanceSelector {

    record AppearanceResult(boolean power, boolean fading, boolean killed, GhostAppearance appearance) {}

    private static final AppearanceResult[] GHOST_LEAVING_HOUSE_APPEARANCES = {
        new AppearanceResult(false, false, false, GhostAppearance.NORMAL),
        new AppearanceResult(false, false, true,  GhostAppearance.NORMAL),
        new AppearanceResult(false, true,  false, GhostAppearance.NORMAL),
        new AppearanceResult(false, true,  true,  GhostAppearance.NORMAL),
        new AppearanceResult(true,  false, false, GhostAppearance.FRIGHTENED),
        new AppearanceResult(true,  false, true,  GhostAppearance.NORMAL),
        new AppearanceResult(true,  true,  false, GhostAppearance.FLASHING),
        new AppearanceResult(true,  true,  true,  GhostAppearance.NORMAL),
    };

    @Test
    public void testLeavingHouse() {
        for (AppearanceResult r : GHOST_LEAVING_HOUSE_APPEARANCES) {
            GhostAppearance appearance = MutableGhost3D.selectAppearance(GhostState.LEAVING_HOUSE, r.power, r.fading, r.killed);
            assertEquals(r.appearance, appearance, "powerActive=%s powerFading=%s killed=%s".formatted(r.power, r.fading, r.killed));
        }
    }
}