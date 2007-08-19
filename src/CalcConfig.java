// Copyright (c) 2007, Mihai Preda.
// Available under the MIT License (see COPYING).

class CalcConfig {
    private static final String
        ANGLE_KEY    = "angleUnit",
        ANGLE_RAD    = "rad",
        ANGLE_DEG    = "deg",
        ROUND_KEY    = "roundDigits",
        AXES         = "axes",
        LABELS       = "labels",
        ASPECTRATIO1 = "ar1",
        TRUE         = "T",
        FALSE        = "F";

    CalcConfig(Store rs, int recId) {
        cfg = new Config(rs, recId);
        angleInRadians = cfg.get(ANGLE_KEY, ANGLE_RAD).equals(ANGLE_RAD);
        roundingDigits = Integer.parseInt(cfg.get(ROUND_KEY, "1"));
        axes = cfg.get(AXES, TRUE).equals(TRUE);
        labels = cfg.get(LABELS, TRUE).equals(TRUE);
        aspectRatio1 = cfg.get(ASPECTRATIO1, FALSE).equals(TRUE);
    }

    void setAngleInRadians(boolean inRad) {
        angleInRadians = inRad;
        cfg.set(ANGLE_KEY, angleInRadians ? ANGLE_RAD : ANGLE_DEG);
        cfg.save();
    }

    void setRoundingDigits(int nDigits) {
        roundingDigits = nDigits;
        cfg.set(ROUND_KEY, "" + roundingDigits);
        cfg.save();
    }

    void setAxes(boolean axes) {
        this.axes = axes;
        cfg.set(AXES, axes ? TRUE : FALSE);
        cfg.save();
    }

    void setLabels(boolean labels) {
        this.labels = labels;
        cfg.set(LABELS, labels ? TRUE : FALSE);
        cfg.save();
    }

    void setAspectRatio(boolean one) {
        aspectRatio1 = one;
        cfg.set(ASPECTRATIO1, aspectRatio1 ? TRUE : FALSE);
        cfg.save();
    }

    boolean angleInRadians;
    int roundingDigits;
    boolean axes;
    boolean labels;
    boolean aspectRatio1;

    private Config cfg;
}
