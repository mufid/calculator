// Copyright (c) 2007, Mihai Preda.
// Available under the MIT License (see COPYING).

class CalcConfig {
    static final String
        kAngleKey = "angleUnit",
        kAngleRad = "rad",
        kAngleDeg = "deg",
        kRoundKey = "roundDigits",
        kAxes     = "axes",
        kLabels   = "labels",
        kTrue     = "T",
        kFalse    = "F";
       
    CalcConfig(Store rs, int recId) {
        cfg = new Config(rs, recId);
        angleInRadians = cfg.get(kAngleKey, kAngleRad).equals(kAngleRad);
        roundingDigits = Integer.parseInt(cfg.get(kRoundKey, "1"));
        axes = cfg.get(kAxes, kTrue).equals(kTrue);
        labels = cfg.get(kLabels, kTrue).equals(kTrue);
    }

    void setAngleInRadians(boolean inRad) {
        angleInRadians = inRad;
        cfg.set(kAngleKey, angleInRadians ? kAngleRad : kAngleDeg);
        cfg.save();
    }

    void setRoundingDigits(int nDigits) {
        roundingDigits = nDigits;
        cfg.set(kRoundKey, "" + roundingDigits);
        cfg.save();
    }

    void setAxes(boolean axes) {
        this.axes = axes;
        cfg.set(kAxes, axes ? kTrue : kFalse);
        cfg.save();
    }

    void setLabels(boolean labels) {
        this.labels = labels;
        cfg.set(kLabels, labels ? kTrue : kFalse);
        cfg.save();
    }

    boolean angleInRadians;
    int roundingDigits;
    boolean axes;
    boolean labels;

    private Config cfg;
}
