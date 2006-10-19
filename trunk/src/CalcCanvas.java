import javax.microedition.lcdui.*;

class CalcCanvas extends Canvas {
    Image img;
    Graphics g;
    int screenHeight, screenWidth;
    int answerY, inputY, historyY;
    int helpHeight;
    Font smallFont, normalFont, largeFont;
    int smallFontHeight, normalFontHeight, largeFontHeight;

    protected void paint(Graphics g) {
        g.drawImage(img, 0, 0, 0);
    }

    CalcCanvas() {
        setFullScreenMode(true);
        screenHeight = getHeight();
        screenWidth  = getWidth();
        img = Image.createImage(screenWidth, screenHeight);
        g = img.getGraphics();

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
