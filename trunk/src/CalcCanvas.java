import javax.microedition.lcdui.*;

class CalcCanvas extends Canvas {
    Image img;
    Graphics g;
    int w, h, screenH;
    int topH, editY, editH;
    int blank1Y, blank1H, blank2Y, blank2H;
    int blank3Y, blank3H, blank4X;
    int keypadX, keypadW, keypadH, keypadXEnd;
    

    Font smallFont, normalFont, largeFont, largeBold;
    int smallHeight, normalHeight, largeHeight;

    CalcCanvas() {
        setFullScreenMode(true);
        w = getWidth();
        screenH = getHeight();

        KeyState.init(w, screenH);
        keypadX = KeyState.xPos;
        keypadW = KeyState.w;
        keypadH = KeyState.h;
        keypadXEnd = keypadX + keypadW;
        h = screenH - keypadH;

        img = Image.createImage(w, h);
        g = img.getGraphics();

        smallFont  = Font.getFont(0, 0, Font.SIZE_SMALL);
        normalFont = Font.getFont(0, 0, Font.SIZE_MEDIUM);
        largeFont  = Font.getFont(0, 0, Font.SIZE_LARGE);
        largeBold  = Font.getFont(0, Font.STYLE_BOLD, Font.SIZE_LARGE);

        smallHeight  = smallFont.getHeight();
        normalHeight = normalFont.getHeight();
        largeHeight  = largeFont.getHeight();

        topH = largeHeight;
        blank1Y = topH;
        blank1H = 3;
        editY = blank1Y + blank1H;
        editH = largeHeight;
        blank2Y = editY + editH;
        blank2H = h - blank2Y;
        
        //caretPos = 0;
    }
    
    int clipX, clipY, clipW, clipH;
    boolean intersects(int x2, int y2, int w2, int h2) {
        return !((clipX + clipW <= x2 || x2 + w2 <= clipX) &&
                 (clipY + clipH <= y2 || y2 + h2 <= clipY));
    }
    /*
    void clear(Graphics g, int x, int y, int w, int h) {
        if (intersects(x, y, w, h)) {
            g.fillRect(x, y, w, h);
        }
    }
    */

    void paintTop(Graphics g) {
        g.setColor(0);
        g.fillRect(0, 0, w, topH);
        g.setFont(largeBold);
        g.setColor(0xffffff);
        g.drawString("0.31415E1", w, 0, Graphics.TOP|Graphics.RIGHT);
    }

    void paintEdit(Graphics g) {
        g.setColor(0xffffff);
        g.fillRect(0, editY, w, editH);
        g.setColor(0);
        g.setFont(largeFont);
        g.drawString("asin(-1)", 0, editY, 0);
    }

    protected void paint(Graphics g) {
        clipX = g.getClipX();
        clipY = g.getClipY();
        clipW = g.getClipWidth();
        clipH = g.getClipHeight();
        
        if (clipX < topH) {
            paintTop(g);
        }
        if (intersects(0, editY, w, editH)) {
            paintEdit(g);
        }
        if (intersects(keypadX, h, keypadW, keypadH)) {
            //keypad.paint(g);
            KeyState.digits.paint(g);
        }
        g.setColor(0xe0e0e0);
        g.fillRect(0, blank1Y, w, blank1H);
        g.fillRect(0, blank2Y, w, blank2H);
        g.fillRect(0, h, keypadX, keypadH);
        g.fillRect(keypadXEnd, h, w - keypadXEnd, keypadH);
    }

    KeyState keypad = KeyState.digits;
    char line[] = new char[256];
    int lineSize = 256;
    int  last = -1;
    int  pos = 0;
    
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
    
    void resetState() {
        int p = pos;
        boolean number = false;
        while (p >= 0 && isDigit(line[p])) {
            number = true;
            --p;
        }
        boolean pastE = false;
        if (p >= 0 && line[p] == 'E') {
            pastE = true;
            --p;
        } else if (p >= 1 && line[p] == '-' && line[p-1] == 'E') {
            pastE = true;
            p -= 2;
        }
        while (p >= 0 && isDigit(line[p])) {
            number = true;
            --p;
        }
        boolean id = false;
        boolean acceptDot = !pastE;
        if (p >= 0) {
            char c = Character.toLowerCase(line[p]);
            if (('a' <= c && c <= 'z') || c == ')' || c == '!') {
                id = true;
                number = false;
                pastE = false;
                acceptDot = false;
            } else if (c == '.') {
                acceptDot = false;
                if (p > 0 && isDigit(line[p - 1])) {
                    number = true;
                }
            }
        }
        boolean acceptMinus = !pastE || line[pos] == 'E';
        if (pastE && (line[pos] == 'E' || line[pos] == '-')) {
            number = false;
        }
        boolean atStart = acceptDot && (pos == -1 || !isDigit(line[pos]));

        KeyState.rootOp.set(10, (number && !pastE) ? "E" : null);
        if (id || number) {
            KeyState.digits.set(9, KeyState.rootOp);
        } else if (atStart) {
            KeyState.digits.set(9, KeyState.rootExp);
        } else {
            KeyState.digits.set(9, null);
        }
        
        if (acceptDot) {
            KeyState.digits.set(11, ".-");
        } else if (acceptMinus) {
            KeyState.digits.set(11, "-");
        } else {
            KeyState.digits.set(11, null);
        }
        
        int openParens = 0;
        for (int i = pos; i >= 0; --i) {
            if (line[i] == '(') {
                ++openParens;
            } else if (line[i] == ')') {
                --openParens;
            }
            if (openParens > 0) { break; }
        }
        KeyState.rootOp.set(8, (openParens > 0) ? ")" : null);
        keypad = id ? KeyState.rootOp : KeyState.digits;
    }
    
    static final boolean isDigit(char c) {
        return '0' <= c && c <= '9';
    }

    void delFromLine() {
    }

    void insertIntoLine(String s) {
    }

    protected void keyPressed(int key) {
        int keyPos = getKeyPos(key);
        if (keyPos >= 0) {
            String s = keypad.handleKey(keyPos);
            if (s != null) {
                if (pos >= 0 && line[pos] == '.' && 
                    s.length() == 1 && !isDigit(s.charAt(0))) {
                    delFromLine();
                }
                insertIntoLine(s);
            }
        } else {
            int action = 0;
            try {
                action = getGameAction(key);
            } catch (IllegalArgumentException e) {
            }
            
            if (action != 0) {
                switch (action) {
                case Canvas.LEFT: 
                    if (pos >= 0) {
                        --pos;
                        resetState();
                    }
                case Canvas.RIGHT:
                    if (pos < lineSize - 1) {
                        ++pos;
                        resetState();
                    }
                case Canvas.UP:
                case Canvas.DOWN:
                case Canvas.FIRE:
                }
            } else {
                if (key == -8 || key == -11) {
                    KeyState save = keypad;
                    resetState();
                    if (save == keypad) {
                        delFromLine();
                        resetState();
                    }
                }
            }
        }
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
}
