import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.ui._3d.GhostAppearance;
import de.amr.pacmanfx.ui._3d.GhostAppearanceSelector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestGhostAppearanceSelector {

    private static final Object[][] EXPECTED_LEAVING_HOUSE = {
            // power, fading, killed => appearance
            {false, false, false, GhostAppearance.NORMAL},
            {false, false, true,  GhostAppearance.NORMAL},
            {false, true,  false, GhostAppearance.NORMAL},
            {false, true,  true,  GhostAppearance.NORMAL},
            {true,  false, false, GhostAppearance.FRIGHTENED},
            {true,  false, true,  GhostAppearance.NORMAL},
            {true,  true,  false, GhostAppearance.FLASHING},
            {true,  true,  true,  GhostAppearance.NORMAL},
    };

    @Test
    public void testLeavingHouse() {
        for (Object[] row : EXPECTED_LEAVING_HOUSE) {
            boolean powerActive = (boolean)row[0], powerFading = (boolean)row[1], killed = (boolean)row[2];
            assertEquals(row[3],
                    GhostAppearanceSelector.selectAppearance(GhostState.LEAVING_HOUSE,
                            powerActive, powerFading, killed),
                    "powerActive=%s powerFading=%s killed=%s".formatted(powerActive, powerFading, killed)
                    );
        }
    }
}
