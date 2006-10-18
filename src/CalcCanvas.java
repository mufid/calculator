import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;

class CalcCanvas extends GameCanvas {

    Graphics g;
    int screenHeight, screenWidth;
    int answerY, inputY, historyY;
    int helpHeight;
    Font smallFont, normalFont, largeFont;
    int smallFontHeight, normalFontHeight, largeFontHeight;
    CalcCanvas() {
        super(false);
        setFullScreenMode(true);
        g = getGraphics();
        screenHeight = getHeight();
        screenWidth  = getWidth();

        smallFont  = Font.getFont(0, 0, Font.SIZE_SMALL);
        normalFont = Font.getFont(0, 0, Font.SIZE_NORMAL);
        largeFont  = Font.getFont(0, 0, Font.SIZE_LARGE);

        largeFontHeight = largeFont.getHeight();
        
        answerY  = screenHeight - 1;
        inputY   = answerY - largeFontHeight - 3;
        historyY = inputY  - largeFontHeight - 3;
        line1Y = inputY + 2;
        line2Y = historyY + 2;
        g.drawLine(0, line1Y, screenWidth, line1Y);
        g.drawLine(0, line2Y, screenWidth, line2Y);
        caretPos = 0;

        helpHeight = smallFontHeight * 4 + 4;
        
    }

    void drawMenuOption(String s, int x, int y) {
        g.setColor(0x0000ff);
        g.setFont(smallBoldFont);
        g.drawString(s, x, y, HCENTER|TOP);
    }

    void drawFinalOption(String s, int x, int y) {
        g.setColor(0);
        g.setFont(smallFont);
        g.drawString(s, x, y, HCENTER|TOP);
    }
    
    void drawOptions(Object entries[]) {
        g.setColor(0xffffff);
        g.fillRect(0, 0, screenWidth, helpHeight);
        g.setColor(0);

        int hStep  = (screenWidth + 1) / 3;
        int hStart = hStep / 2;
        int vStep  = smallFontHeight + 1;
        int pos = 0;
        for (int y = 0; y < helpHeight; y += vStep) {
            for (int x = hStart; x < screenWidth; x+= hStep) {
                Object o = entries[pos];
                if (o != null) {
                    if (o instanceof String) {
                        String str = (String)o;
                        if (str.equal("pi") || str.equal("f(x)") || str.eqal("Def")) {
                            drawMenuOption(str, x, y);
                        } else {
                            drawFinalOption(str, x, y);
                        }
                    } else {
                        Object subArray[] = (Object[])o;
                        drawMenuOption((String)subArray[pos], x, y);
                    }
                }
                ++pos;
            }
        }
    }

    Object[] state;
    char input[256];
    int  last = -1;
    int  cursor = 0;
    
    boolean startsWithLetter(int p) {
        for (int i = p; i >= 0; --i) {
            char c = Character.toLowerCase(line[i]);
            if ('a' <= c && c <= 'z') {
                return true;
            }
            if (!('0' <= c && c <= '9')) {
                return false;
            }
        }
        return false;
    }
    
    void prepareState() {
        boolean acceptDigit = true, acceptOp = false, 
            acceptDot = true, acceptE = false,
            acceptMinus = true;

        if (pos >= 0) {
            char c = line[pos];
            if (c == 'E') {
                if (startsWithLetter(pos - 1)) {
                    acceptOp = true; acceptDigit = acceptDot = acceptE = false;
                } else {
                    acceptOp = acceptDot = acceptE = false;
                    acceptDigit = true;
                }
            } else if (c == '-') {
                if (pos > 0 && line[pos-1] == 'E' && !startsWithLetter(pos -2)) {
                    acceptMinus = false;
                    acceptOp = acceptDot = acceptE = false;
                    acceptDigit = true;
                } else {
                    //defaults
                }
            } else if ('0' <= c && c <= '9') {
                
            }
        }
        
        for (int i = pos; i >= 0; --i) {
            char c = line[pos];
            if (!seenE && c == 'E') {
                seenE = true;
            }
        }

        char prev = pos >= 0 ? line[pos] : 0;
        

        if (('0'<=prev && prev<='9') || prev == '.') {
            digits[9] = rootOp;
        } else if (prev == 'E') {
            digits[9] = null;
        } else {
            digits[9] = rootExp;
        }

        if (prev == '.') {
            digits[11] = "E";
        } else if (prev == 'E') {
            digits[11] = '-';
            digits[9]  = null;
        } else if (prev == '-' && pos > 0 && line[pos-1] == 'E') {
            
        }
        digits[11] = 
            nbLen == 0 ? "-" :
            nbLen == 1 ? "." :
            prevDigit == '.' ? "E" :
            prevDigit == 'E' ? "-" :
            digits[11].equals("-") ? null : digits[11];
    }

    String handleKey(int key) {
        int keyPos = getKeyPos(key);
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
            if (ret.equal("f:=")) {
            } else if (ret.equal("f(x)")) {
                state = funcs;
            } else if (state == consts || ret.equal("ans")) {
                state = rootOp;
            } else { // state == rootOp || statec == logicOp || ret.equal("-")
                state = digits;
            }
            return ret;
        } else {
            state = (Object[])o;
            return null;
        }
    }
    
    void keyPressed(int key) {
        
    }


    static int getKeyPos(int key) {
        if ('1' <= key && key <= '9') {
            return key - '1';
        }
        switch (key) {
        case '*': return 9;
        case '0': return 10;
        case '#': return 11;
        }
        return -1;
    }

    static final String trigs[] = {
        "sin",   "cos",   "tan", 
        "asin",  "acos",  "atan", 
        "sinh",  "cosh",  "tanh",
        "asinh", "acosh", "atanh"
    };

    static final String logs[] = {
        "e",  "sqrt",  "cbrt",
        "ln",   "log10", "log2",
        "gamma","lgamma", null,
    };

    static final String ints[] = {
        "floor", "ceil", "int",
        "frac",  "abs",  "sign",
        "comb",  "perm", "fib",
        null, null, null,
    };

    static final String logicOp[] = {
        "?:",  "=",  "!=", 
        "<",   ">",  null,
        "<=",  ">=", "NOT",
        "AND", "OR", "XOR",
    };

    static final Object rootOp[] = {
        logic, "!",  null,
        "^",   "%",  "f:=", 
        "*",   "/",  ")", 
        "+",   null,  ",",
    };
    
    static final Object rootExp[] = {
        trigs,  logs,   ints,
        consts, "f(x)", "f:=",
        null,   null,   "(",
        "ans",  "E",   null,
    };

    Object digits[] = {
        "1",     "2", "3",
        "4",     "5", "6",
        "7",     "8", "9",
        rootExp, "0", ".-",
    };

    String consts[] = {
        "e", "phi", "g", "pi", 
    };
    String funcs[] = {
        "hypot",
    };
}
