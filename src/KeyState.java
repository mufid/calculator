import javax.microedition.lcdui;

final class KeyState {
    static Font font, bold;
    static int fontHeight;
    static int w, h;
    static KeyState digits, rootOp, trigs, logs, ints, vars, funcs, rootExp;

    /*
    static String logicOp[] = {
        "?:",  "=",  "!=", 
        "<",   ">",  null,
        "<=",  ">=", "NOT",
        "AND", "OR", "XOR",
    };
    */

    static initStatic(int w, int h) {
        this->w = w;
        this->h = h;
        font = Font.getFont(0, 0, Font.SIZE_SMALL);
        bold = Font.getFont(0, Font.STYLE_BOLD, Font.SIZE_SMALL);
        fontHeight = font.getHeight();

        digits = new KeyState(new Object[] {
            "1",     "2", "3",
            "4",     "5", "6",
            "7",     "8", "9",
            null,    "0", null
        });

        rootOp = new KeyState(new Object[] {
            null, "!",  null,
            "^",   "%",  "f:=", 
            "*",   "/",  ")", 
            "+",   "E",  ",",
        });
        
        trigs = new KeyState(new Object[] {
            "sin",   "cos",   "tan", 
            "asin",  "acos",  "atan", 
            "sinh",  "cosh",  "tanh",
            "asinh", "acosh", "atanh"
        });
        
        logs = new KeyState(new Object[] {
            "e",    "sqrt",  "cbrt",
            "ln",   "log10", "log2",
            "gamma","lgamma", null
        });
        
        ints = new KeyState(new Object[] {
            "int",  "frac", "ceil",
            "sign", "abs",  "floor",
            "min",  "max",  null,
            "comb", "perm", "fib",
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
    
        rootExp = new KeyState(new Object[] {
            trigs, logs,  ints,
            vars,  funcs, "DEF",
            null,  null,  "(",
            "ans", null,  null,
        });
    }

    Object keys[];
    Image img;
    Graphics imgG;
    boolean wantRedraw;

    KeyState(Object keys[]) {
        this->keys = keys;
        wantRedraw = true;
    }

    String handleKey(int keyPos) {
        if (keyPos == -1) {
            return null; //key not recognized
        }
        if (keyPos >= state.length) {
            return null;
        }
        Object o = state[keyPos];
        if (o == null) {
            return null;
        }
        if (o instanceof String) {
            String ret = (String)o;
            if (state == funcs) {
                ret = ret + "(";
            }
            if (ret.equal("f:=")) {
            } else if (ret.equal("f(x)")) {
                state = funcs;
                return null;
            } else if (state == consts || ret.equal("ans")) {
                state = rootOp;
            } else { // state == rootOp || statec == logicOp || ret.equal("-")
                state = digits;
            }
            if (ret.equal(".-")) { ret = "."; }
            return ret;
        } else {
            state = (Object[])o;
            return null;
        }
    }

    void set(int pos, Object obj) {
        if (keys[pos] == obj ||
            (obj != null && obj.equal(keys[pos]))) {
            return;
        }
        keys[pos] = obj;
        wantRedraw = true;
    }

    void copyTo(Graphics g, int x, int y) {
        if (wantRedraw) {
            if (img == null) {
                img = new Image(w, h);
                imgG = img.getGraphics();
            }
            draw(imgG);
            wantRedraw = false;
        }
        g.drawImage(img, x, y, 0);
    }

    static final int
        BACKGR  = 0x101010,
        FOREGR  = 0xffffff,
        FOREGR2 = 0x8080ff,
        LIGHTER = 0x202020,
        DARKER  = 0x000000;    

    private void drawMenuOption(String s, int x, int y) {
        g.setColor(FOREGR2);
        g.setFont(bold);
        g.drawString(s, x, y, HCENTER|TOP);
    }

    private void drawFinalOption(String s, int x, int y) {
        g.setColor(FOREGR);
        g.setFont(font);
        g.drawString(s, x, y, HCENTER|TOP);
    }

    private void draw(Graphics g) {
        g.setColor(BACKGR);
        g.fillRect(0, 0, w, h);

        int hStep  = (w + 1) / 3;
        int hStart = hStep / 2;
        int vStep  = fontHeight + 1;
        int pos = 0;
        for (int y = 0; y < h; y += vStep) {
            for (int x = hStart; x < w; x+= hStep) {
                Object o = keys[pos];
                if (o != null) {
                    if (o instanceof String) {
                        String str = (String)o;
                        drawFinalOption(str, x, y);
                    } else {
                        KeyState child = (KeyState)o;
                        drawMenuOption((String) child.keys[pos], x, y);
                    }
                }
                ++pos;
            }
        }
    }
}
