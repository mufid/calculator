// Copyright (c) 2007, Carlo Teubner
// Available under the MIT License (see COPYING).

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class PlotCanvas extends Canvas implements VMConstants {

    private final static int PARPLOT_INIT_POINTS = 64; // initial number of points - must be power of 2
    private final static int PARPLOT_MAX_POINTS = 4096; // max number of points - must be power of 2
    private final static int PARPLOT_MAX_DIST = 15; // max squared distance in pixels between neighbouring points

    private final static int 
        COLOR_BLACK = 0x000000,
        COLOR_WHITE = 0xffffff,
        COLOR_RED   = 0xff0000,
        COLOR_DARK_BLUE = 0x000080,
        COLOR_YELLOW = 0x40FFFF,
        COLOR_LABEL = 0x7070ff;

    private CompiledFunction func, func2;
    private double[] minmax;

    private Image img;
    private int width, height;
    private Font smallFont;

    private Display display;
    private Displayable next;
    
    public PlotCanvas(Display display, Displayable next) {
        this.display = display;
        this.next = next;

        setFullScreenMode(true);
        width  = getWidth();
        height = getHeight();
        smallFont = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    }

    public void plot(Result result) {
        func = result.function;
        func2 = result.function2;
        minmax = result.plotArgs;
        img = Image.createImage(width, height);
        Graphics g = img.getGraphics();
        g.setFont(smallFont);

        switch (result.plotCommand) {
        case PLOT: paintPlot(g); break;
        case MAP: paintMap(g); break;
        case PARPLOT: paintParPlot(g); break;
        }

        display.setCurrent(this);
    }

    protected void paint(Graphics g) {
        g.drawImage(img, 0, 0, Graphics.TOP | Graphics.LEFT);
    }

    private static String doubleToString(double v, double maxError) {
        return Util.doubleToString(Util.shortApprox(v, maxError), 1);
    }

    private void paintPlot(Graphics g) {
        final double xmin = minmax[0], xmax = minmax[1];
        final double xf = (xmax - xmin) / (width - 1);
        double fmin = Double.POSITIVE_INFINITY;
        double fmax = Double.NEGATIVE_INFINITY;
        double[] y = new double[width];

        double x = xmin;
        for (int xp = 0; xp < width; ++xp, x += xf) {
            double v = func.evaluate(x);
            if (isReal(v)) {
                if (v < fmin) {
                    fmin = v;
                }
                if (v > fmax) {
                    fmax = v;
                }
            }
            y[xp] = v;
        }

        double yf, ymin, ymax, offset;
        if (C.cfg.aspectRatio1) {
            yf = (xmax - xmin) / (height - 1);
            double ycenter = 0.5 * (fmax + fmin);
            double ydist = 0.5 * (xmax - xmin) * height / width;
            ymin = ycenter - ydist;
            ymax = ycenter + ydist;
            offset = 0.5 * (xmax - xmin - fmax - fmin);
        } else {
            yf = (fmax - fmin) / (height - 1);
            ymin = fmin;
            ymax = fmax;
            offset = -fmin;
        }

        // The newly-created image is all-white (according to MIDP spec),
        // so there's no need to white-fill it here.
        // We'd need to clear it if we'd reuse the image between plots.
        // g.setColor(COLOR_WHITE);
        // g.fillRect(0, 0, width, height);

        if (C.cfg.axes) {
            g.setGrayScale(180);
            if (xmin <= 0 && 0 <= xmax) {
                int xx = (int) (-xmin / xf + 0.5);
                g.drawLine(xx, 0, xx, height-1);
            }
            if (ymin <= 0 && 0 <= ymax) {
                int yy = (int) (offset / yf + 0.5);
                g.drawLine(0, height - 1 - yy, width-1, height - 1 - yy);
            }
        }

        if (C.cfg.labels) {
            g.setColor(COLOR_LABEL);
            g.drawString(doubleToString(ymin, xf), 0, height-1, Graphics.BOTTOM | Graphics.LEFT);
            g.drawString(doubleToString(ymax, xf), 0, 0, Graphics.TOP | Graphics.LEFT);
        }

        g.setColor(COLOR_BLACK);
        if (fmin == fmax) {
            g.drawLine(0, height/2, width-1, height/2);
        } else {
            int y1 = (int) ((y[0] + offset) / yf + 0.5);
            for (int xp = 1; xp < width; ++xp) {
                int y2 = (int) ((y[xp] + offset) / yf + 0.5);
                if (isReal(y[xp-1]) && isReal(y[xp])) {
                    g.drawLine(xp - 1, height - 1 - y1, xp, height - 1 - y2);
                }
                y1 = y2;
            }
        }
    }

    private void paintMap(Graphics g) {
        int canvasHeight, fontHeight = 0, infoHeight = 0;
        
        if (C.cfg.labels) {
            fontHeight = smallFont.getHeight();
            infoHeight = fontHeight + 2;
            canvasHeight = height - infoHeight;
        } else {
            canvasHeight = height;
        }

        final double xmin = minmax[0], xmax = minmax[1], ymin = minmax[2], ymax = minmax[3];
        final double xf = (xmax - xmin) / (width - 1);
        final double yf = (ymax - ymin) / (canvasHeight - 1);
        float fmin = Float.POSITIVE_INFINITY, fmax = Float.NEGATIVE_INFINITY;

        final int size = width*canvasHeight;

        // We store the function values as floats in an int array.
        // The float precision is enough for plot/map.
        // We use int array in order to reuse it for createRGBImage() later.
        int[] f = new int[size];
        long start = System.currentTimeMillis();
        {   // This block (curly brackets) is used to limit the scope of xval.
            double xval = xmin;
            for (int x = 0; x < width; ++x, xval += xf) {
                double yval = ymin;
                for (int y = 0; y < canvasHeight; ++y, yval += yf) {
                    float v = (float) func.evaluate(xval, yval);
                if (isReal(v)) {
                        if (v < fmin) {
                        fmin = v;
                        } else if (v > fmax) {
                        fmax = v;
                }
                    }
                    f[x + (canvasHeight - 1 - y)*width] = Float.floatToIntBits(v);
                }
            }
            }

        long end = System.currentTimeMillis();
        Log.log("Calculation took " + (end - start) + " ms.");

        start = end;
        final double gf = 255 / (fmax - fmin);
        float v;
        int c;
        for (int i = 0; i < size; ++i) {
            v = Float.intBitsToFloat(f[i]);
            if (Float.isNaN(v)) {
                c = COLOR_RED;
            } else if (Float.NEGATIVE_INFINITY == v) {
                c = COLOR_DARK_BLUE;
            } else if (Float.POSITIVE_INFINITY == v) {
                c = COLOR_YELLOW;
            } else {
                c = (int) ((v - fmin) * gf + 0.5);
                c |= (c << 8) | (c << 16); 
            }
            f[i] = c;
        }
        Image im = Image.createRGBImage(f, width, canvasHeight, false);
        f = null;
        g.drawImage(im, 0, 0, Graphics.TOP | Graphics.LEFT);
        im = null;
        
        if (C.cfg.labels) {
            final int colourBoxWidth = Math.max(fontHeight, 8);
            g.setGrayScale(180);
            g.fillRect(0, canvasHeight, width, infoHeight);
            g.setGrayScale(0);
            g.fillRect(1, canvasHeight + 1, colourBoxWidth, fontHeight);
            g.setGrayScale(0xFF);
            g.fillRect(width / 2 + 1, canvasHeight + 1, colourBoxWidth, fontHeight);
            final int labelWidthPx = width / 2 - colourBoxWidth - 2;
            g.setColor(0x000000FF);
            g.drawString(Util.fitDouble(fmin, smallFont, labelWidthPx), colourBoxWidth + 2, canvasHeight + 1, Graphics.TOP | Graphics.LEFT);
            g.drawString(Util.fitDouble(fmax, smallFont, labelWidthPx), width / 2 + colourBoxWidth + 2, canvasHeight + 1, Graphics.TOP | Graphics.LEFT);
        }

        end = System.currentTimeMillis();
        Log.log("Drawing took " + (end - start) + " ms.");
    }

    private void paintParPlot(Graphics g) {
        final double tmin = minmax[0], tmax = minmax[1];
        final double tf = (tmax - tmin) / PARPLOT_MAX_POINTS;

        double[] fx = new double[PARPLOT_MAX_POINTS + 1],
                 fy = new double[PARPLOT_MAX_POINTS + 1];
        boolean[] present = new boolean[PARPLOT_MAX_POINTS + 1];

        double fxmin = Double.POSITIVE_INFINITY, fxmax = Double.NEGATIVE_INFINITY,
               fymin = Double.POSITIVE_INFINITY, fymax = Double.NEGATIVE_INFINITY;

        int n = PARPLOT_INIT_POINTS;
        final int level = PARPLOT_MAX_POINTS / n;
        double t, x, y;

        /* Initially, evaluate at PARPLOT_INIT_POINTS evenly spaced values of t */
        for (int i = 0; i <= PARPLOT_MAX_POINTS; i += level) {
            t = tmin + i * tf;
            fx[i] = x = func.evaluate(t);
            fy[i] = y = func2.evaluate(t);
            present[i] = true;
            if (isReal(x)) {
                if (x < fxmin) fxmin = x;
                if (x > fxmax) fxmax = x;
            }
            if (isReal(y)) {
                if (y < fymin) fymin = y;
                if (y > fymax) fymax = y;
            }
        }

        PriorityQueueOfInt distantPts = new PriorityQueueOfInt(PARPLOT_INIT_POINTS * 3);

        double xf = (width - 1) / (fxmax - fxmin),
               yf = (height - 1) / (fymax - fymin);

        /* Put too-large inter-point distances on the priority queue distantPts */
        double x1 = fx[0], y1 = fy[0], x2, y2, dx, dy, dist;
        for (int i = level; i <= PARPLOT_MAX_POINTS; i += level) {
            x2 = fx[i];
            y2 = fy[i];
            dx = (x2 - x1) * xf;
            dy = (y2 - y1) * yf;
            dist = dx*dx + dy*dy;
            if (dist > PARPLOT_MAX_DIST)
                distantPts.rawInsert((int) dist, i - level);
            x1 = x2;
            y1 = y2;
        }

        distantPts.repairHeap(); // must call this because we have used rawInsert

        /* Work through distantPts, starting with largest inter-point distance.
           For each pair of distant points, insert a new point in between. */
        int i, j, k;
        while (distantPts.size() > 0 && n <= PARPLOT_MAX_POINTS) {
            i = distantPts.extractMaximumValue();
            for (k = i + 1; !present[k]; ++k) ;
            if (k == i + 1)
                continue;
            j = (i + k) / 2;
            t = tmin + j * tf;
            fx[j] = x = func.evaluate(t);
            fy[j] = y = func2.evaluate(t);
            if (isReal(x)) {
                if (x < fxmin) fxmin = x;
                if (x > fxmax) fxmax = x;
            }
            if (isReal(y)) {
                if (y < fymin) fymin = y;
                if (y > fymax) fymax = y;
            }
            present[j] = true;
            ++n;
            double dx1 = (fx[j] - fx[i]) * xf;
            double dy1 = (fy[j] - fy[i]) * yf;
            dist = dx1*dx1 + dy1*dy1;
            if (dist > PARPLOT_MAX_DIST)
                distantPts.insert((int) dist, i);
            double dx2 = (fx[k] - fx[j]) * xf;
            double dy2 = (fy[k] - fy[j]) * yf;
            dist = dx2*dx2 + dy2*dy2;
            if (dist > PARPLOT_MAX_DIST)
                distantPts.insert((int) dist, j);
        }

        xf = (width - 1) / (fxmax - fxmin);
        yf = (height - 1) / (fymax - fymin);

        double xmin, xmax, ymin, ymax, xoff, yoff;
        if (C.cfg.aspectRatio1) {
            xf = yf = Math.min(xf, yf);
            double xcenter = 0.5 * (fxmin + fxmax),
                   ycenter = 0.5 * (fymin + fymax);
            double xdist = 0.5 * width / xf,
                   ydist = 0.5 * height / yf;
            xmin = xcenter - xdist;
            xmax = xcenter + xdist;
            ymin = ycenter - ydist;
            ymax = ycenter + ydist;
            xoff = -xmin;
            yoff = -ymin;
            /*
            System.out.println("center = (" + xcenter + "," + ycenter + ")");
            System.out.println("dist = (" + xdist + "," + ydist + ")");
            System.out.println("min = (" + xmin + "," + ymin + ")");
            System.out.println("max = (" + xmax + "," + ymax + ")");
            */
        } else {
            xmin = fxmin;
            xmax = fxmax;
            ymin = fymin;
            ymax = fymax;
            xoff = -xmin;
            yoff = -ymin;
        }

        Log.log("Plotting " + n + " points");

        /* Draw axes and labels */
        if (C.cfg.axes) {
            g.setGrayScale(180);
            if (xmin <= 0 && 0 <= xmax) {
                int xx = (int) (xoff * xf + 0.5);
                g.drawLine(xx, 0, xx, height-1);
            }
            if (ymin <= 0 && 0 <= ymax) {
                int yy = (int) (yoff * yf + 0.5);
                g.drawLine(0, height - 1 - yy, width-1, height - 1 - yy);
            }
        }

        if (C.cfg.labels) {
            int fontHeight = smallFont.getHeight();
            int w = width / 2 - smallFont.stringWidth("x=") - 7;
            g.setColor(COLOR_LABEL);
            g.drawString("x=" + Util.fitDouble(xmin, smallFont, w), 0, height - 1 - fontHeight, Graphics.BOTTOM | Graphics.LEFT);
            g.drawString("x=" + Util.fitDouble(xmax, smallFont, w), width - 1, height - 1 - fontHeight, Graphics.BOTTOM | Graphics.RIGHT);
            g.drawString("y=" + Util.fitDouble(ymin, smallFont, w), 0, height - 1, Graphics.BOTTOM | Graphics.LEFT);
            g.drawString("y=" + Util.fitDouble(ymax, smallFont, w), 0, 0, Graphics.TOP | Graphics.LEFT);
        }

        /* Draw lines between the points */
        g.setColor(0x00000000);
        x1 = fx[0];
        y1 = fy[0];
        for (i = 1; i <= PARPLOT_MAX_POINTS; ++i) {
            if (!present[i])
                continue;
            x2 = fx[i];
            y2 = fy[i];
            if (isReal(x1) && isReal(y1) && isReal(x2) && isReal(y2))
                g.drawLine(
                        (int) ((x1 + xoff) * xf + 0.5),
                        height - 1 - (int) ((y1 + yoff) * yf + 0.5),
                        (int) ((x2 + xoff) * xf + 0.5),
                        height - 1 - (int) ((y2 + yoff) * yf + 0.5) );
            x1 = x2;
            y1 = y2;
        }
    }

    private static boolean isReal(double d) {
        return !(Double.isInfinite(d) || Double.isNaN(d));
    }
    
    protected void keyPressed(int keyCode) {
        img = null;
        display.setCurrent(next);
    }

}
