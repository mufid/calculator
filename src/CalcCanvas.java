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

    protected void paint(Graphics g) {
        g.drawImage(img, 0, 0, 0);
    }

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
        normalFont = Font.getFont(0, 0, Font.SIZE_NORMAL);
        largeFont  = Font.getFont(0, 0, Font.SIZE_LARGE);
        largeBold  = Font.getFont(0, Font.STYLE_BOLD, Fost.SIZE_LARGE);

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
        
        caretPos = 0;
    }
    
    int clipX, clipY, clipW, clipH;
    boolean intersects(int clipX, int clipY, int clipW, int clipH,
                       int x2, int y2, int w2, int h2) {
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

    void paint(Graphics g) {
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
            keypad.paint(g);
        }
        g.setColor(0x101010);
        g.fillRect(0, blank1Y, w, blank1H);
        g.fillRect(0, blank2Y, w, blank2H);
        g.fillRect(0, h, keypadX, keypadH);
        g.fillRect(keypadXEnd, h, w - keypadXEnd, keypadH);
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
            char c = Character.toLower(line[p]);
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

        rootOp[10] = (number && !pastE) ? "E" : null;
        if (id || number) {
            digits[9] = rootOp;
        } else if (atStart) {
            digits[9] = rootExp;
        } else {
            digits[9] = null;
        }
        
        if (acceptDot) {
            digits[11] = ".-";
        } else if (acceptMinus) {
            digits[11] = "-";
        } else {
            digits[11] = null;
        }
        
        int openParens = 0;
        for (int p = pos; p >= 0; --p) {
            if (line[p] == '(') {
                ++openParens;
            } else if (line[p] == ')') {
                --openParens;
            }
            if (openParens > 0) { break; }
        }
        rootOp[8] = (openParens > 0) ? ")" : null;
        state = id ? rootOp : digits;
    }
    
    static final boolean isDigit(char c) {
        return '0' <= c && c <= '9';
    }

    void keyPressed(int key) {
        int keyPos = getKeyPos(key);
        if (keyPos >= 0) {
            String s = handleKey(keyPos);
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
                    Object[] save = state;
                    resetState();
                    if (save == state) {
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
