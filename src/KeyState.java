import javax.microedition.lcdui.*;

final class KeyState {
    static Font font;
    static int fontHeight;
    static int w, h, cellWidth, cellHeight, yPos;
    static int stepW, singleSpace;
    static KeyState rootOp, trigs, hyps, logs, ints, vars, funcs, rootExp;
    static KeyState keypad, lastPainted;

    private static final int 
        BACKGR[] = {CalcCanvas.BACKGR, CalcCanvas.BACKGR, 0xffff00},
        DARKER[] = {0x808080,  0x808080, 0x808080};
    private static final int FOREGR  = 0x000000, LIGHTER = 0xffffff;
    
    /*
    static String logicOp[] = {
        "?:",  "=",  "!=", 
        "<",   ">",  null,
        "<=",  ">=", "NOT",
        "AND", "OR", "XOR",
    };
    */

    static void init(int sw, int sh) {
        w = sw;
        boolean isSmall = sh <= 160;
        int size = isSmall ? Font.SIZE_SMALL : Font.SIZE_MEDIUM;
        font = Font.getFont(0, 0, size);
        fontHeight = font.getHeight();

        stepW = sw/3;
        int w2 = font.stringWidth("mmmm");
        cellWidth = Math.min(stepW, w2);
        singleSpace = (stepW - cellWidth)/2;

        cellHeight = fontHeight + (isSmall ? 2 : 3);
        h = cellHeight * 4 + 1;
        yPos = sh - h;
        
        trigs = new KeyState(new Object[] {
            "asinh", "acosh", "atanh",
            "sinh",  "cosh",  "tanh",
            "sin",   "cos",   "tan", 
            "asin",  "acos",  "atan", 
        });
                
        logs = new KeyState(new Object[] {
            null,  null,   null,
            null,  null,   null,
            "lg",  "ln",   "lb",
            "cbrt", null,  null,
        });

        ints = new KeyState(new Object[] {
            "min",   "max",  "gcd",
            "floor", "ceil", "sign",
            "int",   "frac", "abs",
            "C",     "P",    "rnd",
        });

        vars = new KeyState(new Object[] {
            "x",  "y",  "z",
            "h",  "m",  "n",
            "g",  "c",  "d",
            "f",  "a",  "b",
        });

        rootOp = new KeyState(new Object[] {
            "(",  ",",  ")",
            null, "!",  "^", 
            "%",  "/",  "*",
            null, "-",  "+",
        });

        rootExp = new KeyState(new Object[] {
            "sqrt",  "ans",  "E",
            "pi",    "e",    null,
            trigs,   logs,   ints,
            ".",     vars,   ":=",
        });

        //"\u03c0" == pi
    }

    Object keys[];
    Image img = null;
    Graphics g;

    KeyState(Object keys[]) {
        this.keys = keys;
    }

    private static final String base[] = {"1", "2", "3", "4", "5", "6", "7", "8", "9"};
    static String handleKey(int pos) {
        if (keypad == null) {
            switch (pos) {
            case 9: keypad = rootExp; return null;
            case 10: return "0";
            case 11: keypad = rootOp; return null;
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
        //System.out.println("painted");
    }

    void doPaint(Graphics destG) {
        if (img == null) {
            img = Image.createImage(w, h);
            g = img.getGraphics();
            g.setFont(font);
            g.setColor(BACKGR[0]);
            g.fillRect(0, 0, w, h);
            String txt = null;
            Object o;
            int colIndex;
            for (int x=singleSpace, col=0; col < 3; ++col, x+=stepW) {
                for (int pos=col, y=1; y < h; y += cellHeight, pos += 3) {
                    //txt = null;
                    colIndex = 0;
                    if ((o = keys[pos]) != null) {
                        if (o instanceof String) {
                            txt = (String)o;
                            colIndex = 1;
                        } else {
                            txt = (String) ((KeyState)o).keys[pos];
                            colIndex = 2;
                        }
                    }

                    int bottom = y + fontHeight;
                    g.setColor(LIGHTER);
                    g.drawLine(x, y, x+cellWidth-1, y);
                    g.drawLine(x, y, x, bottom);
                    g.setColor(DARKER[colIndex]);
                    g.drawLine(x+cellWidth-1, y + 1, x+cellWidth-1, bottom);
                    g.drawLine(x+1, bottom, x+cellWidth-1, bottom);
                    g.setColor(BACKGR[colIndex]);
                    g.fillRect(x+1, y+1, cellWidth-2, bottom-y-1);
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
