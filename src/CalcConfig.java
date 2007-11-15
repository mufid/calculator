// Copyright (c) 2007 Mihai Preda, Carlo Teubner.
// Available under the MIT License (see COPYING).

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import org.javia.lib.*;

class CalcConfig {
    private static final String
        ANGLE_KEY    = "angleUnit",
        ANGLE_RAD    = "rad",
        ANGLE_DEG    = "deg",
        ROUND_KEY    = "roundDigits",
        AXES         = "axes",
        LABELS       = "labels",
        ASPECTRATIO1 = "ar1",
        PISYMBOL     = "pi",
        TRUE         = "T",
        FALSE        = "F";

    CalcConfig(Store rs, int recId) {
        cfg = new Config(rs, recId);
        angleInRadians = cfg.get(ANGLE_KEY, ANGLE_RAD).equals(ANGLE_RAD);
        updateTrigFactor();
        roundingDigits = Integer.parseInt(cfg.get(ROUND_KEY, "1"));
        axes = cfg.get(AXES, TRUE).equals(TRUE);
        labels = cfg.get(LABELS, TRUE).equals(TRUE);
        aspectRatio1 = cfg.get(ASPECTRATIO1, FALSE).equals(TRUE);
    }

    void setAngleInRadians(boolean inRad) {
        angleInRadians = inRad;
        updateTrigFactor();
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

    private void updateTrigFactor() {
        trigFactor = angleInRadians ? 1. : (180. / Math.PI);        
    }

    // Does a pixel-by-pixel comparison of pi and rho glyphs, in each of the fonts used by CalcCanvas.
    // If they are unequal, we assume the pi glyph is supported, otherwise we assume it is not.
    // We then set piString accordingly to either "\u03c0" or "pi".
    void initPiSymbol(Font[] fonts)
    {
        boolean piSymbol;
        final String piEntry = cfg.get(PISYMBOL, "NA");
        if (piEntry.equals(TRUE))
            piSymbol = true;
        else if (piEntry.equals(FALSE))
            piSymbol = false;
        else {
            piSymbol = true;
            for (int i = 0; i < fonts.length; ++i) {
                final Font font = fonts[i]; 
                final int height = font.getHeight(),
                          width  = font.charWidth('\u03c0');
                if (width != font.charWidth('\u03c1')) {
                    continue;
                }
                if (width <= 0) {
                    piSymbol = false;
                    break;
                }
                final int size = width * height;
                Image im = Image.createImage(width, height);
                Graphics gr = im.getGraphics();
                gr.setColor(0);
                gr.drawChar('\u03c0', 0, 0, Graphics.TOP | Graphics.LEFT);
                int[] rgb1 = new int[size];
                im.getRGB(rgb1, 0, width, 0, 0, width, height);
                gr.setColor(0xFFFFFF);
                gr.fillRect(0, 0, width, height);
                gr.setColor(0);
                gr.drawChar('\u03c1', 0, 0, Graphics.TOP | Graphics.LEFT);
                int[] rgb2 = new int[size];
                im.getRGB(rgb2, 0, width, 0, 0, width, height);
                boolean difference = false;
                for (int j = 0; j < size; ++j) {
                    if (rgb1[j] != rgb2[j]) {
                        difference = true;
                        break;
                    }
                }
                if (!difference) {
                    piSymbol = false;
                    break;
                }
            }
            cfg.set(PISYMBOL, piSymbol ? TRUE : FALSE);
            cfg.save();
        }
        piString = piSymbol ? "\u03c0" : "pi";
    }

/*
    private void printChar(int[] rgb, int width) {
        for (int i = 0; i < rgb.length; ++i) {
            System.out.print((rgb[i] & 0xffffff) == 0 ? '#' : ' ');
            if ((i + 1) % width == 0)
                System.out.println();
        }
        System.out.println();
    }
*/

    boolean angleInRadians;
    double trigFactor;
    int roundingDigits;
    boolean axes;
    boolean labels;
    boolean aspectRatio1;
    String piString;

    private Config cfg;
}
