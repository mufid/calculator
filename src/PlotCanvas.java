// Copyright (c) 2007, Carlo Teubner

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public class PlotCanvas extends Canvas {
    
    private Symbol func;
    private SymbolTable symtbl;
    private double xmin, xmax;
    
    private Display display;
    private Displayable next;
    
    public PlotCanvas(Display display) {
        this.display = display;
    }

    public void init(Displayable next) {
        this.next = next;        
    }

    public void plot(Symbol func, SymbolTable symtbl, double xmin, double xmax) {
        this.func = func;
        this.symtbl = symtbl;
        this.xmin = xmin;
        this.xmax = xmax;
        display.setCurrent(this);
    }
    
    protected void paint(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        double xf = (xmax - xmin) / (width - 1);
        double ymin = Double.POSITIVE_INFINITY, ymax = Double.NEGATIVE_INFINITY;
        double[] y = new double[width];

        double[] x = new double[1];
        for (int xp = 0; xp < width; ++xp) {
            x[0] = xmin + xp * xf;
            double v = func.eval(symtbl, x);
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
        g.drawString(Double.toString(ymin), 0, height-1, Graphics.BOTTOM | Graphics.LEFT);
        g.drawString(Double.toString(ymax), 0, 0, Graphics.TOP | Graphics.LEFT);
        
        g.setColor(0x00000000);
        if (ymin == ymax)
            g.drawLine(0, height/2, width-1, height/2);
        else {
            int y1 = (int) (((y[0] - ymin) / yf) + 0.499);
            for (int xp = 1; xp < width; ++xp) {
                int y2 = (int) (((y[xp] - ymin) / yf) + 0.499);
                if (isReal(y[xp-1]) && isReal(y[xp]))
                    g.drawLine(xp - 1, height - 1 - y1, xp, height - 1 - y2);
                y1 = y2;
            }
        }
    }

    private static boolean isReal(double d) {
        return !(Double.isInfinite(d) || Double.isNaN(d));
    }
    
    protected void keyPressed(int keyCode) {
        display.setCurrent(next);
    }
}
