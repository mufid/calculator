import javax.microedition.lcdui.*;

final class KeyState {
    static Font font, bold;
    static int fontHeight;
    static int w, h, cellWidth, cellHeight, xPos, yPos;
    static KeyState digits, rootOp, trigs, hyps, logs, ints, vars, funcs, rootExp;
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
        font = Font.getFont(0, 0, Font.SIZE_SMALL);
        bold = Font.getFont(0, Font.STYLE_BOLD, Font.SIZE_SMALL);
        fontHeight = font.getHeight();

        int w1 = (sw + 1) / 3;
        int w2 = font.stringWidth("mmmmm");
        cellWidth = Math.min(w1, w2);
        cellHeight = fontHeight + 1;

        w = cellWidth  * 3;
        h = cellHeight * 4;

        xPos = (sw - w) / 2;
        yPos = sh - h;

        digits = new KeyState(new Object[] {
            "1",     "2", "3",
            "4",     "5", "6",
            "7",     "8", "9",
            null,    "0", ". -",
        });

        // ! "f:=" "," "hyp"
        
        trigs = new KeyState(new Object[] {
            "sin",   "cos",   "tan", 
            "asin",  "acos",  "atan", 
            null,    null,    null,
            null,   null,    null,
        });
                
        logs = new KeyState(new Object[] {
            "log10", "ln",  "log2",
            null,    null,    null,
            null,     null,    null,
            null,     null,    null
        });

        rootOp = new KeyState(new Object[] {
            null, null, null,
            "^", "%",  ")", 
            "*", "/",  null, 
            "+", "E",  ",",
        });

        rootExp = new KeyState(new Object[] {
            trigs,    logs,  null,
            "\u03c0",  "e",  "(",
            null, null, null,
            "ans", "\u221a", "\u221b",
        });
        
        /*
        ints = new KeyState(new Object[] {
            "int",  "frac", "ceil",
            "sign", "abs",  "floor",
            "min",  "max",  null,
            null, null, null,
            //"comb", "perm", "fib",
        });

        hyps = new KeyState(new Object[] {
            "sinh",  "cosh",  "tanh",
            "asinh", "acosh", "atanh",
            null, null, null,
            null, null, null,
        });

        vars = new KeyState(new Object[] {
            "a",    "b",    "c",
            "pi",   "e",    "phi",
            "M",    "ans1", "ans2",
            null,   null,   null,
        });

        funcs = new KeyState(new Object[] {
            "f",     "g",    "h",
            "hypot", "dist", null,
            null, null, null,
            null, null, null,
        });
        */

        keypad = digits;
    }

    Object keys[];
    Image img;
    Graphics imgG;
    boolean wantRedraw;

    KeyState(Object keys[]) {
        this.keys = keys;
        wantRedraw = true;
    }

    String handleKey(int keyPos) {
        Object o = keys[keyPos];
        if (o == null) {
            return null;
        }
        if (o instanceof String) {
            String str = (String)o;
            if (str.equals(". -")) { 
                digits.set(11, "-");
                return "."; 
            }
            keypad = digits;
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
        wantRedraw = true;
    }

    static boolean needPaint() {
        boolean ret = (lastPainted != keypad) ||
            keypad.wantRedraw;
        //System.out.println("needPaint " + ret);
        return ret;
    }

    static void paint(Graphics g) {
        keypad.doPaint(g);
        lastPainted = keypad;
        System.out.println("painted");
    }

    void doPaint(Graphics g) {
        if (wantRedraw) {
            if (img == null) {
                System.out.println("img " + w + "x" + h);
                img = Image.createImage(w, h);
                imgG = img.getGraphics();
            }
            draw(imgG);
            wantRedraw = false;
        }
        g.drawImage(img, xPos, yPos, 0);
    }

    static final int
        BACKGR  = 0x101010,
        FOREGR  = 0xffffff,
        FOREGR2 = 0x8080ff,
        LIGHTER = 0x202020,
        DARKER  = 0x000000;    

    private void drawMenuOption(Graphics g, String s, int x, int y) {
        g.setColor(FOREGR2);
        g.setFont(bold);
        g.drawString(s, x, y, Graphics.HCENTER|Graphics.TOP);
    }

    private void drawFinalOption(Graphics g, String s, int x, int y) {
        g.setColor(FOREGR);
        g.setFont(font);
        g.drawString(s, x, y, Graphics.HCENTER|Graphics.TOP);
    }

    private void draw(Graphics g) {
        g.setColor(BACKGR);
        g.fillRect(0, 0, w, h);
        int pos = 0;
        for (int y = 0; y < h; y += cellHeight) {
            for (int x = cellWidth / 2; x < w; x+= cellWidth) {
                Object o = keys[pos];
                if (o != null) {
                    if (o instanceof String) {
                        String str = (String)o;
                        drawFinalOption(g, str, x, y);
                    } else {
                        KeyState child = (KeyState)o;
                        drawMenuOption(g, (String) child.keys[pos], x, y);
                    }
                }
                ++pos;
            }
        }
    }
}
