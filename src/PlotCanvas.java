// Copyright (c) 2007, Carlo Teubner
// Available under the MIT License (see COPYING).

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class PlotCanvas extends Canvas implements VMConstants {

    private CompiledFunction func;
    private double[] minmax;

    private Image img;
    private int width, height;
    
    private Display display;
    private Displayable next;
    
    public PlotCanvas(Display display) {
        this.display = display;
        setFullScreenMode(true);
    }

    public void init(Displayable next) {
        this.next = next;        
    }

    public void plot(int plotCommand, CompiledFunction function, double[] plotArgs) {
        func = function;
        minmax = plotArgs;
        width = getWidth();
        height = getHeight();

        img = Image.createImage(width, height);
        Graphics g = img.getGraphics();
        switch (plotCommand)
        {
        case PLOT: paintPlot(g); break;
        case MAP: paintMap(g); break;
        }

        display.setCurrent(this);
    }

    protected void paint(Graphics g) {
        g.drawImage(img, 0, 0, Graphics.TOP | Graphics.LEFT);
    }

    private void paintPlot(Graphics g) {
        double xmin = minmax[0], xmax = minmax[1];
        double xf = (xmax - xmin) / (width - 1);
        double ymin = Double.POSITIVE_INFINITY;
        double ymax = Double.NEGATIVE_INFINITY;
        double[] y = new double[width];

        for (int xp = 0; xp < width; ++xp) {
            double v = func.evaluate(xmin + xp * xf);
            if (isReal(v)) {
                if (v < ymin)
                    ymin = v;
                if (v > ymax)
                    ymax = v;
            }
            y[xp] = v;
        }
    
        double yf = (ymax - ymin) / (height - 1);

        g.setColor(0x00FFFFFF);
        g.fillRect(0, 0, width, height);

        g.setGrayScale(180);
        if (xmin <= 0 && 0 <= xmax) {
            int xx = (int) (-xmin / xf + 0.499);
            g.drawLine(xx, 0, xx, height-1);
        }
        if (ymin <= 0 && 0 <= ymax) {
            int yy = (int) (-ymin / yf + 0.499);
            g.drawLine(0, height - 1 - yy, width-1, height - 1 - yy);
        }

        g.setColor(0x000000FF);
        g.setFont(Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL));
        // XXX Mihai: format(ymin, xf)?
        g.drawString(Double.toString(ymin), 0, height-1, Graphics.BOTTOM | Graphics.LEFT);
        g.drawString(Double.toString(ymax), 0, 0, Graphics.TOP | Graphics.LEFT);

        g.setColor(0x00000000);
        if (ymin == ymax)
            g.drawLine(0, height/2, width-1, height/2);
        else {
            int y1 = (int) ((y[0] - ymin) / yf + 0.499);
            for (int xp = 1; xp < width; ++xp) {
                int y2 = (int) ((y[xp] - ymin) / yf + 0.499);
                if (isReal(y[xp-1]) && isReal(y[xp]))
                    g.drawLine(xp - 1, height - 1 - y1, xp, height - 1 - y2);
                y1 = y2;
            }
        }
    }
    
    private void paintMap(Graphics g) {
        long start, end;

        Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        g.setFont(font);
        int fontHeight = font.getHeight();
        int infoHeight = fontHeight + 2;
        int canvasHeight = height - infoHeight;
        int colourBoxWidth = Math.max(fontHeight, 8);
        g.setGrayScale(180);
        g.fillRect(0, canvasHeight, width, infoHeight);
        g.setGrayScale(0);
        g.fillRect(1, canvasHeight + 1, colourBoxWidth, fontHeight);
        g.setGrayScale(0xFF);
        g.fillRect(width / 2 + 1, canvasHeight + 1, colourBoxWidth, fontHeight);

        final double xmin = minmax[0], xmax = minmax[1], ymin = minmax[2], ymax = minmax[3];
        final double xf = (xmax - xmin) / (width - 1);
        final double yf = (ymax - ymin) / (canvasHeight - 1);
        double fmin = Double.POSITIVE_INFINITY, fmax = Double.NEGATIVE_INFINITY;

        final int size = width*canvasHeight;
        double[] f = new double[size];
        start = System.currentTimeMillis();
        for (int x = 0; x < width; ++x)
            for (int y = 0; y < canvasHeight; ++y) {
                double v = func.evaluate(xmin + x * xf, ymin + y * yf);
                if (isReal(v)) {
                    if (v < fmin)
                        fmin = v;
                    if (v > fmax)
                        fmax = v;
                }
                f[x + (canvasHeight - 1 - y)*width] = v;
            }
        end = System.currentTimeMillis();
        Log.log("Calculation took " + (end - start) + " ms.");

        /* The following straightforward method of going from the array of function values (f)
           to the array of colour values (rgb) is quite memory-intensive. This cost could be
           reduced by doing the following instead:

           int[] f1 = new int[size];
           double[] f2 = new int[size/2];

           put the first half of the function values into f1, using MoreMath.HI and MoreMath.LO;
           put the second half of the function values into f2;
           convert f1 and f2 to colour values, which we store in f1(!);
           f2 = null;
           ... Image.createRGBImage(f1, ...);
           f1 = null;
           
           If the speed penalty for the above is significant, it could be a fallback in case
           of an OutOfMemoryError.
           
           A more extreme memory-saving, speed-decreasing measure would be to evaluate our function
           twice at each pixel, once purely to determine fmin, fmax, and once to populate rgb.
         */

        double gf = 255 / (fmax - fmin);
        start = System.currentTimeMillis();
        int[] rgb = new int[size];
        for (int i = 0; i < size; ++i) {
            double v = f[i];
            int c;
            if (Double.isNaN(v))
                c = 0xFF0000;
            else if (Double.NEGATIVE_INFINITY == v)
                c = 0x000080;
            else if (Double.POSITIVE_INFINITY == v)
                c = 0x40FFFF;
            else {
                c = (int) ((v - fmin) * gf + 0.499);
                c |= (c << 8) | (c << 16); 
            }
            rgb[i] = c;
        }
        f = null;
        Image im = Image.createRGBImage(rgb, width, canvasHeight, false);
        rgb = null;
        g.drawImage(im, 0, 0, Graphics.TOP | Graphics.LEFT);
        im = null;
        int labelWidthPx = width / 2 - colourBoxWidth - 2;
        String labelMin = Double.toString(fmin), labelMax = Double.toString(fmax); // XXX use Util.doubleToString
        labelMin = labelMin.substring(0, CalcCanvas.fitWidth(font, labelWidthPx, labelMin));
        labelMax = labelMax.substring(0, CalcCanvas.fitWidth(font, labelWidthPx, labelMax));        
        g.setColor(0x000000FF);
        g.drawString(labelMin, colourBoxWidth + 2, canvasHeight + 1, Graphics.TOP | Graphics.LEFT);
        g.drawString(labelMax, width / 2 + colourBoxWidth + 2, canvasHeight + 1, Graphics.TOP | Graphics.LEFT);
        end = System.currentTimeMillis();
        Log.log("Drawing took " + (end - start) + " ms.");
    }

    private static boolean isReal(double d) {
        return !(Double.isInfinite(d) || Double.isNaN(d));
    }
    
    protected void keyPressed(int keyCode) {
        img = null;
        display.setCurrent(next);
    }

}
