package de.amr.games.pacman.lib.tilemap;

import java.util.Set;
import java.util.stream.Stream;

/**
 * The encoding depends on the byte values in interface {@link TileEncoding}!
 */
public enum ObstacleType {
    CROSS_SHAPE(
        "dfdgegfdfege",
        "dcfdgecgfcdfegce",
        "dfbdgbegfdbfebge",
        "dcfbdgbecgfcdbfebgce"
    ),

    F_SHAPE(
        "dcgfcdbfebgdbfeb",
        "dgbefbdgbecgfceb",
        "dcgfcdbfebgcdbfeb",
        "dgbecfbdgbecgfceb",
        "dgbecgfcdbecgfceb", "dcfbdgbfcedcfbgce",
        "dcgfcdbecgfcdbfeb", "dcgbfebgcedcfbgce"
    ),

    H_SHAPE(
        "dgefdgbfegdfeb",
        "dcgfdegfcedfge",
        "dgbecfbdgbfebgcdbfeb",
        "dcgfcdbecgfcedcfbgce"
    ),

    L_SHAPE(
        "dgegfceb", "dcgfdfeb", "dcgbfege", "dfdgbfce", // 8 segments
        "dcgbfegce", "dcfdgbfce", "dcgfcdfeb", "dgecgfceb", // 9 segments
        "dgbegfceb", "dcgfdbfeb", "dcgbfebge", "dfbdgbfce", // 9 segments
        "dcgbfebgce", "dcfbdgbfce", "dgbecgfceb", "dcgfcdbfeb" // 10 segment
    ),

    LL_SHAPE("dcgbecgbfebgcebgce"),

    LL_SHAPE_MIRRORED("dcfbdcfbdgbfcdbfce"),

    LLL_SHAPE("dcgbecgbecgbfebgcebgcebgce"),

    LLL_SHAPE_MIRRORED("dcfbdcfbdcfbdgbfcdbfcdbfce"),

    O_SHAPE("dgfe", "dcgfce", "dgbfeb", "dcgbfceb"),

    S_SHAPE(
        "dcfdgbfcdfeb",
        "dgecgbfegceb",
        "dcgbegfcebge",
        "dfbdcgfdbfce",
        "dcfbdgbfcdbfeb",
        "dgbecgbfebgceb",
        "dcgbecgfcebgce",
        "dcfbdcgfcdbfce"
    ),

    T_SHAPE(
        "dgbecgfcdbfeb",
        "dcfbdgbfebgce",
        "dcgfcdbfebgce",
        "dcfbdgbecgfce"
    ),

    T_SHAPE_TWO_ROWS("dcgbecgfcdbfceb"),

    U_SHAPE(
        "dcgbfcedfge", "dcgfdegfceb", "dcgbfegdfeb", "dgefdgbfceb",
        "dcgbfcedcfgce", "dcgfcdecgfceb", "dcgbfebgdbfeb", "dgbefbdgbfceb",
        "dcgbfcedcfbgce", "dcgfcdbecgfceb", "dcgbfebgcdbfeb", "dgbecfbdgbfceb"
    ),

    ANY;

    ObstacleType(String... encodingStrings) {
        encodings = Set.of(encodingStrings);
    }

    private final Set<String> encodings;

    public Stream<String> encodings() {
        return encodings.stream();
    }

    public boolean matches(String encoding) {
        return encodings.contains(encoding);
    }
}
