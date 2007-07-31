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

        double[] x = new double[1];
        for (int xp = 0; xp < width; ++xp) {
            x[0] = xmin + xp * xf;
            double v = func.evaluate(x);
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

        double xmin = minmax[0], xmax = minmax[1], ymin = minmax[2], ymax = minmax[3];
        double xf = (xmax - xmin) / (width - 1);
        double yf = (ymax - ymin) / (canvasHeight - 1);
        double fmin = Double.POSITIVE_INFINITY, fmax = Double.NEGATIVE_INFINITY;

        double[] f = new double[width*height];
        double[] xy = new double[2];
        start = System.currentTimeMillis();
        for (int x = 0; x < width; ++x)
            for (int y = 0; y < canvasHeight; ++y) {
                xy[0] = xmin + x * xf;
                xy[1] = ymin + y * yf;
                double v = func.evaluate(xy);
                if (isReal(v)) {
                    if (v < fmin)
                        fmin = v;
                    if (v > fmax)
                        fmax = v;
                }
                f[x + y*width] = v;
            }
        end = System.currentTimeMillis();
        Log.log("Calculation took " + (end - start) + " ms.");

        double gf = 255 / (fmax - fmin);
        start = System.currentTimeMillis();
        int labelWidthPx = width / 2 - colourBoxWidth - 2;
        String labelMin = Double.toString(fmin), labelMax = Double.toString(fmax);
        labelMin = labelMin.substring(0, CalcCanvas.fitWidth(font, labelWidthPx, labelMin));
        labelMax = labelMax.substring(0, CalcCanvas.fitWidth(font, labelWidthPx, labelMax));        
        g.setColor(0x000000FF);
        g.drawString(labelMin, colourBoxWidth + 2, canvasHeight + 1, Graphics.TOP | Graphics.LEFT);
        g.drawString(labelMax, width / 2 + colourBoxWidth + 2, canvasHeight + 1, Graphics.TOP | Graphics.LEFT);
        for (int x = 0; x < width; ++x)
            for (int y = 0; y < canvasHeight; ++y) {
                double v = f[x + y*width];
                if (Double.isNaN(v))
                    g.setColor(0xFF0000);
                else if (Double.NEGATIVE_INFINITY == v)
                    g.setColor(0x000080);
                else if (Double.POSITIVE_INFINITY == v)
                    g.setColor(0x40FFFF);
                else                
                    g.setGrayScale((int) ((v - fmin) * gf + 0.499));
                g.drawLine(x, canvasHeight - 1 - y, x, canvasHeight - 1 - y);
            }
        end = System.currentTimeMillis();
        Log.log("Drawing took " + (end - start) + " ms.");
    }

    private static boolean isReal(double d) {
        return !(Double.isInfinite(d) || Double.isNaN(d));
    }
    
    protected void keyPressed(int keyCode) {
        display.setCurrent(next);
    }

}
