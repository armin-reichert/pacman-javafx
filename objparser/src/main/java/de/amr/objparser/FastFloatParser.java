package de.amr.objparser;

public final class FastFloatParser {

    public static float parse(String s, int start, int end) {
        boolean neg = false;
        int i = start;

        if (i < end) {
            char c = s.charAt(i);
            if (c == '-') {
                neg = true;
                i++;
            } else if (c == '+') i++;
        }

        double val = 0.0;

        while (i < end) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') break;
            val = val * 10 + (c - '0');
            i++;
        }

        if (i < end && s.charAt(i) == '.') {
            i++;
            double factor = 0.1;
            while (i < end) {
                char c = s.charAt(i);
                if (c < '0' || c > '9') break;
                val += (c - '0') * factor;
                factor *= 0.1;
                i++;
            }
        }

        if (i < end && (s.charAt(i) == 'e' || s.charAt(i) == 'E')) {
            i++;
            boolean expNeg = false;
            if (i < end && s.charAt(i) == '-') {
                expNeg = true;
                i++;
            } else if (i < end && s.charAt(i) == '+') i++;

            int exp = 0;
            while (i < end) {
                char c = s.charAt(i);
                if (c < '0' || c > '9') break;
                exp = exp * 10 + (c - '0');
                i++;
            }

            val = val * Math.pow(10, expNeg ? -exp : exp);
        }

        return neg ? (float) -val : (float) val;
    }
}
