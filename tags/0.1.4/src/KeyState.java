// Copyright (c) 2006-2007 Mihai Preda, Carlo Teubner.
// Available under the MIT License (see COPYING).

import javax.microedition.lcdui.*;

class KeyState implements VMConstants {
    static Font font;
    static int fontHeight;
    static int w, h, cellWidth, cellHeight, yPos;
    static int stepW, singleSpace;
    static KeyState rootOp, trigs, logs, ints, vars, plots, rootExp;
    static KeyState keypad, lastPainted;

    private static final int BACKGR = CalcCanvas.BACKGR, DARKER = 0x808080, SUBMENU = 0x20f000;
    private static final int FOREGR  = 0x000000, LIGHTER = 0xffffff;
    private static final int TRIANGLE_SIZE = 6;

    static void init(int sw, int sh, boolean isSmallScreen, Font fnt) {
        w = sw;
        font = fnt;
        fontHeight = font.getHeight();

        stepW = sw/3;
        int w2 = font.stringWidth("mmmm");
        cellWidth = Math.min(stepW, w2);
        singleSpace = (stepW - cellWidth)/2;

        cellHeight = fontHeight + (isSmallScreen ? 3 : 4);
        h = cellHeight * 4 + 1;
        yPos = sh - h;
        
        trigs = new KeyState(new Object[] {
            "asinh", "acosh", "atanh",
            "sinh",  "cosh",  "tanh",
            "asin",  "acos",  "atan", 
            "sin",   "cos",   "tan", 
        });
                
        logs = new KeyState(new Object[] {
            null,  null,   null,
            null,  null,   null,
            "cbrt", null,  null,
            "lg",  "ln",   "lb",
        });

        ints = new KeyState(new Object[] {
            "min",   "max",  "gcd",
            "floor", "ceil", "sign",
            "int",   "frac", "abs",
            "C",     "P",    "rnd",
        });

        vars = new KeyState(new Object[] {
            null, null, null,
            "f",  "a",  "b",
            "g",  "c",  "d",
            "h",  "m",  "n",
        }) {
          void init() {
              keys[0] = keys[1] = keys[2] = null;
              changed = true;
              StringWrapper pre = Calc.self.calcCanvas.preCursorLine();
              if (Lexer.isAssignment(pre)) {
                  keys[0] = "x";
                  keys[1] = "y";
                  keys[2] = "z";
                  return;
              }
              switch (Lexer.getFunctionPlotCommand(pre)) {
              case PLOT:
                  keys[0] = "x";
                  break;
              case MAP:
                  keys[0] = "x";
                  keys[1] = "y";
                  break;
              case PARPLOT:
                  keys[0] = "t";
                  break;
              }
          }
        };

        plots = new KeyState(new Object[] {
            null,   null,   null,
            "plot", null,   null,
            "map",   null,   null,
            "par",   null,   null,
        });

        rootOp = new KeyState(new Object[] {
            "(",  ",",  ")",
            "^",  "!",  null, 
            "*",  "/",  "%",
            "+",  "-",  null,
        });

        rootExp = new KeyState(new Object[] {
            "sqrt",  "ans",  "E",
            plots,    vars,   ":=",
            Calc.cfg.piString, "e", ints,
            trigs,   logs,   ".",
        });
    }

    Object keys[];
    Image img = null;
    boolean changed;
    Graphics g;

    KeyState(Object keys[]) {
        this.keys = keys;
    }

    void init() { }

    private static final String base[] = {"1", "2", "3", "4", "5", "6", "7", "8", "9"};
    static String handleKey(int pos) {
        if (keypad == null) {
            switch (pos) {
            case 9: keypad = rootOp; return null;
            case 10: return "0";
            case 11: keypad = rootExp; return null;
            default: return (0 <= pos && pos <= 8) ? base[pos] : null;
            }
        }
        return keypad.intHandleKey(pos);
    }
    
    private String intHandleKey(int keyPos) {
        Object o = keys[keyPos];
        if (o instanceof String) {
            if (o != null) {
                keypad = null;
            }
            return (String)o;
        } else {
            keypad = (KeyState)o;
            if (keypad != null)
                keypad.init();
            return null;
        }
    }

    /*
    void set(int pos, Object obj) {
        if (keys[pos] == obj ||
            (obj != null && obj.equals(keys[pos]))) {
            return;
        }
        keys[pos] = obj;
        wantRedraw[pos%3] = true;
    }
    */

    static int getH() {
        return keypad == null ? 0 : h;
    }

    static void paint(Graphics destG) {
        if (keypad != null) {
            keypad.doPaint(destG);
        }
        lastPainted = keypad;
        //Log.log("painted");
    }

    void doPaint(Graphics destG) {
        if (img == null || changed) {
            changed = false;
            img = Image.createImage(w, h);
            g = img.getGraphics();
            g.setFont(font);
            g.setColor(BACKGR);
            g.fillRect(0, 0, w, h);
            String txt = null;
            Object o;
            final int triangleSize = Math.min(TRIANGLE_SIZE, fontHeight);
            for (int x=singleSpace, col=0; col < 3; ++col, x+=stepW) {
                for (int pos=col, y=1; y < h; y += cellHeight, pos += 3) {
                    boolean submenu = false;
                    if ((o = keys[pos]) != null) {
                        if (o instanceof String) {
                            txt = (String)o;
                        } else {
                            txt = (String) ((KeyState)o).keys[pos];
                            submenu = true;
                        }
                    }

                    int bottom = y + fontHeight + 1;
                    g.setColor(LIGHTER);
                    g.drawLine(x, y, x+cellWidth-1, y);
                    g.drawLine(x, y, x, bottom);
                    g.setColor(DARKER);
                    g.drawLine(x+cellWidth-1, y + 1, x+cellWidth-1, bottom);
                    g.drawLine(x+1, bottom, x+cellWidth-1, bottom);
                    if (submenu) {
                        g.setColor(SUBMENU);
                        g.fillTriangle(x+cellWidth-1, bottom, x+cellWidth-2-triangleSize, bottom, x+cellWidth-1, bottom-triangleSize-1);
                    }
                    if (o != null) {
                        g.setColor(FOREGR);
                        g.drawString(txt, x+cellWidth/2, y+1, Graphics.HCENTER|Graphics.TOP);
                    }
                }
            }                      
        }
        destG.drawImage(img, 0, yPos, 0);
    }

    static void repaint(Canvas c) {
        if (lastPainted != keypad) {
            c.repaint(0, yPos-3, w, h+3);
        }
    }
}
