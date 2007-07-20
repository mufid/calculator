class CalcConfig {
    static final String
        kAngleKey = "angleUnit",
        kAngleRad = "rad",
        kAngleDeg = "deg",
        kRoundKey = "roundDigits";
       
    CalcConfig(RMS rs, int recId) {
        cfg = new Config(rs, recId);
        angleInRadians = cfg.get(kAngleKey, kAngleRad).equals(kAngleRad);
        roundingDigits = Integer.parseInt(cfg.get(kRoundKey, "1"));
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

    boolean angleInRadians;
    int roundingDigits;

    private Config cfg;
}
