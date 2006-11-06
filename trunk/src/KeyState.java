import javax.microedition.lcdui.*;

final class KeyState {
    static Font font, bold;
    static int fontHeight;
    static int w, h, cellWidth, cellHeight, yPos, interSpace;
    static KeyState rootOp, trigs, hyps, logs, ints, vars, funcs, rootExp;
    static KeyState keypad, lastPainted;

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
        int available = (sh - CalcCanvas.largeHeight * 6) / 5;
        int size = CalcCanvas.normalHeight < available ? Font.SIZE_MEDIUM : Font.SIZE_SMALL;
        font = Font.getFont(0, 0, size);
        bold = Font.getFont(0, Font.STYLE_BOLD, size);
        fontHeight = font.getHeight();

        int w1 = (sw + 1) / 3;
        int w2 = font.stringWidth("mmmm");
        cellWidth = Math.min(w1, w2);
        cellHeight = fontHeight + 3;

        interSpace = (sw - cellWidth*3 + 3)/6;

        h = cellHeight * 4;
        yPos = sh - h;

        /*
        digits = new KeyState(new Object[] {
            "1",     "2", "3",
            "4",     "5", "6",
            "7",     "8", "9",
            null,    "0", ". -",
        });
        */

        // ! "f:=" "," "hyp"
        
        trigs = new KeyState(new Object[] {
            "sin",   "cos",   "tan", 
            "asin",  "acos",  "atan", 
            "sinh",  "cosh",  "tanh",
            "asinh", "acosh", "atanh",
        });
                
        logs = new KeyState(new Object[] {
            "log10", "ln",  "log2",
            null,    "cbrt",    null,
            null,     null,    null,
            null,     null,    null
        });

        rootOp = new KeyState(new Object[] {
            "(",  ",",  ")",
            "^",  "!",  null, 
            "*",  "/",  "%",
            "+",  "-",  null,
        });

        rootExp = new KeyState(new Object[] {
            trigs,    logs,  ints,
            "\u03c0", "e",  "sqrt",
            vars,      ":=", "ans",
            null,     "E",   ".",
        });
        
        ints = new KeyState(new Object[] {
            "int",  "frac", "abs",
            "floor","ceil", "sign",
            "min",  "max",  "gcd",
            "comb", "perm", "rnd",
        });

        vars = new KeyState(new Object[] {
            "f", "g", "h",
            "x", "y", "z",
            "a", "b", "c",
            "phi", null, null,
        });
        

        /*
        hyps = new KeyState(new Object[] {
            "sinh",  "cosh",  "tanh",
            "asinh", "acosh", "atanh",
            null, null, null,
            null, null, null,
        });
        */

        //keypad = digits;
    }

    Object keys[];
    Image img[] = new Image[3];
    Graphics imgG[] = new Graphics[3];
    boolean wantRedraw[] = {true, true, true};

    KeyState(Object keys[]) {
        this.keys = keys;
        for (int i = 0; i < 3; ++i) {
            img[i] = Image.createImage(cellWidth, cellHeight * 4);
            imgG[i] = img[i].getGraphics();
            imgG[i].setFont(font);
        }
    }

    String handleKey(int keyPos) {
        Object o = keys[keyPos];
        if (o == null) {
            return null;
        }
        if (o instanceof String) {
            String str = (String)o;
            if (str.equals(". -")) { 
                //digits.set(11, "-");
                return "."; 
            }
            //keypad = digits;
            Symbol symbol = Expr.symbols.get(str);
            if (symbol == null) {
                return str;
            }
            if (symbol.isFun) {
                str += "(";
            } else if (symbol.isValue) {
                keypad = rootOp;                
            }
            return str;
        } else {
            keypad = (KeyState)o;
            return null;
        }
    }

    void set(int pos, Object obj) {
        if (keys[pos] == obj ||
            (obj != null && obj.equals(keys[pos]))) {
            return;
        }
        keys[pos] = obj;
        wantRedraw[pos%3] = true;
    }

    static void paint(Graphics g) {
        keypad.doPaint(g);
        lastPainted = keypad;
        //System.out.println("painted");
    }

    void doPaint(Graphics g) {
        int doubleSpace = interSpace + interSpace;
        g.setColor(BACKGR);
        g.fillRect(0, yPos, interSpace, h);
        g.fillRect(interSpace + cellWidth, yPos, doubleSpace, h);
        g.fillRect(3*interSpace + 2*cellWidth, yPos, doubleSpace, h);
        g.fillRect(w - interSpace, yPos, interSpace, h);
        for (int i = 0; i < 3; ++i) {
            paintColumn(g, i);
        }
    }

    void paintColumn(Graphics destG, int col) {
        if (wantRedraw[col]) {
            Graphics g = imgG[col];
            g.setColor(BACKGR);
            g.fillRect(0, 0, cellWidth, h);
            int pos = col;
            for (int y = 1; y < h; y += cellHeight, pos += 3) {
                g.setColor(LIGHTER);
                int bottom = y + cellHeight - 3;
                g.drawLine(0, y, cellWidth, y);
                g.drawLine(0, y, 0, bottom);
                g.setColor(DARKER);
                g.drawLine(cellWidth - 1, y + 1, cellWidth - 1, bottom);
                g.drawLine(1, bottom, cellWidth, bottom);
                Object o = keys[pos];
                if (o != null) {
                    String txt;
                    if (o instanceof String) {
                        txt = (String)o;
                        g.setColor(FOREGR);
                        g.drawString(txt, cellWidth/2, y+1, Graphics.HCENTER|Graphics.TOP);
                    } else {
                        txt = (String) ((KeyState)o).keys[pos];
                        g.setColor(FOREGR2);
                        g.setFont(bold);
                        g.drawString(txt, cellWidth/2, y+1, Graphics.HCENTER|Graphics.TOP);
                        g.setFont(font);
                    }
                }
            }
        }

        int x = interSpace * (col + col + 1) + cellWidth * col;
        destG.drawImage(img[col], x, yPos, 0);
        wantRedraw[col] = false;
    }

    static void repaint(Canvas c) {
        keypad.doRepaint(c);
    }

    void doRepaint(Canvas c) {
        for (int i = 0; i < 3; ++i) {
            if (lastPainted != keypad || wantRedraw[i]) {
                c.repaint(interSpace*(i+i+1)+cellWidth*i, yPos, cellWidth, h);
            }
        }
    }

    static final int
        BACKGR  = 0xffff00, //0xe0e0e0,
        FOREGR  = 0x000000,
        FOREGR2 = 0x0000ff,
        LIGHTER = 0xffffff,
        DARKER  = 0x808000;    
}