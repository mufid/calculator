import javax.microedition.lcdui.*;
import java.util.*;

class CalcCanvas extends Canvas {
    Image img, cursorImg;
    Graphics imgG, cursorG;
    int w, h, screenH;
    int topH, editY, editH;
    int blank1Y, blank1H, blank2Y, blank2H;
    int blank3Y, blank3H, blank4X;
    int keypadX, keypadW, keypadH, keypadXEnd;

    Font smallFont, normalFont, largeFont, largeBold;
    int smallHeight, normalHeight, largeHeight;
    Timer cursorBlink = new Timer();

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

        img  = Image.createImage(w, h);
        imgG = img.getGraphics();

        imgG.setColor(0xe0e0e0);
        imgG.fillRect(0, 0, w, h);

        smallFont  = Font.getFont(0, 0, Font.SIZE_SMALL);
        normalFont = Font.getFont(0, 0, Font.SIZE_MEDIUM);
        largeFont  = Font.getFont(0, 0, Font.SIZE_LARGE);
        largeBold  = Font.getFont(0, Font.STYLE_BOLD, Font.SIZE_LARGE);

        smallHeight  = smallFont.getHeight();
        normalHeight = normalFont.getHeight();
        largeHeight  = largeFont.getHeight();

        cursorImg = Image.createImage(2, largeHeight);
        cursorG   = cursorImg.getGraphics();

        topH = largeHeight;
        blank1Y = topH;
        blank1H = 3;
        editY = blank1Y + blank1H;
        editH = largeHeight;
        blank2Y = editY + editH;
        blank2H = h - blank2Y;
        
        cursorBlink.schedule(new TimerTask() {
                public void run() {
                    drawCursor = !drawCursor;
                    repaint(0, editY, w, editH);
                }
            }, 5000, 5000);
        resetState();
    }
    
    int clipX, clipY, clipW, clipH;
    boolean intersects(int x2, int y2, int w2, int h2) {
        return !(clipX + clipW <= x2 || x2 + w2 <= clipX ||
                 clipY + clipH <= y2 || y2 + h2 <= clipY);
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

    char buf[] = new char[256];
    boolean drawCursor = true;

    void paintEdit(Graphics g) {
        g.translate(0, editY);
        g.setColor(0xffffff);
        g.fillRect(0, 0, w, editH);
        g.setColor(0);
        g.setFont(largeFont);
        int len = line.length();
        line.getChars(0, len, buf, 0);
        int cursorX = largeFont.charsWidth(buf, 0, pos + 1);
        g.drawChars(buf, 0, len, 0, 0, 0);

        if (drawCursor) {
            cursorG.setColor(0);
            cursorG.fillRect(0, 0, 2, largeHeight);
            if (pos + 1 < len) {
                cursorG.setColor(0xffff00);
                cursorG.drawChar(buf[pos + 1], 0, 0, 0);
            }
            g.drawImage(cursorImg, cursorX, 0, 0);
        }
        //drawCursor = true;

        g.translate(0, -editY);
    }

    protected void paint(Graphics g) {
        g.drawImage(img, 0, 0, 0);
        KeyState.paint(g);
        g.setColor(0xe0e0e0);
        g.fillRect(0, h, keypadX, keypadH);
        g.fillRect(keypadXEnd, h, w - keypadXEnd, keypadH);
        
        /*
        if (clipY < h) {
            int smallH = Math.min(clipH, h - clipY);
            g.drawRegion(img, clipX, clipY, clipW, smallH, 0, clipX, clipY, 0);
        }
        */

        /*
        System.out.println("Clip " + clipX + " " + clipY + " " +
                           clipW + " " + clipH);
        */

        /*
        if (intersects(keypadX, h, keypadW, keypadH)) {
            KeyState.paint(g);
        }

        g.fillRect(0, blank1Y, w, blank1H);
        g.fillRect(0, blank2Y, w, blank2H);
        if (KeyState.needPaint()) {
            repaint(keypadX, h, keypadW, keypadH);
        }
        */
    }

    StringBuffer line = new StringBuffer();
    int  pos = -1;
    
    boolean startsWithLetter(int p) {
        for (int i = p; i >= 0; --i) {
            if (isLetterAt(i)) {
                return true;
            }
            if (!isDigitAt(i)) {
                return false;
            }
        }
        return false;
    }
    
    int prevFlexPoint(int pos) {
        if (pos == -1) {
            return pos;
        }
        int p = pos;
        if (p >= 0 && line.charAt(p) == '(') --p;
        while (p >= 0 && isDigitAt(p)) --p;
        if (p >= 0 && isLetterAt(p)) {
            while (p >= 0 && (isLetterAt(p) || isDigitAt(p))) --p;
            return p;
        }
        return pos - 1;
    }

    int nextFlexPoint(int pos) {
        int len = line.length();
        if (pos >= len - 1) {
            return pos;
        }
        int p = pos + 1;
        if (isLetterAt(p)) {
            ++p;
            while (p < len && (isLetterAt(p) || isDigitAt(p))) ++p;
            if (p < len && line.charAt(p) == '(') ++p;
            return p - 1;
        }
        return pos + 1;
    }

    void resetState() {
        int p = pos;
        boolean number = false;
        while (p >= 0 && isDigitAt(p)) {
            number = true;
            --p;
        }
        boolean pastE = false;
        if (p >= 0 && line.charAt(p) == 'E') {
            pastE = true;
            --p;
        } else if (p >= 1 && line.charAt(p) == '-' && line.charAt(p-1) == 'E') {
            pastE = true;
            p -= 2;
        }
        while (p >= 0 && isDigitAt(p)) {
            number = true;
            --p;
        }
        boolean id = false;
        boolean acceptDot = !pastE;
        if (p >= 0) {
            char c = Character.toLowerCase(line.charAt(p));
            if (('a' <= c && c <= 'z') || c == ')' || c == '!') {
                id = true;
                number = false;
                pastE = false;
                acceptDot = false;
            } else if (c == '.') {
                acceptDot = false;
                if (p > 0 && isDigitAt(p - 1)) {
                    number = true;
                }
            }
        }
        boolean acceptMinus = !pastE || line.charAt(pos) == 'E';
        if (pastE && (line.charAt(pos) == 'E' || line.charAt(pos) == '-')) {
            number = false;
        }
        boolean atStart = acceptDot && (pos == -1 || !isDigitAt(pos));

        KeyState.rootOp.set(10, (number && !pastE) ? "E" : null);
        if (id || number) {
            KeyState.digits.set(9, KeyState.rootOp);
        } else if (atStart) {
            KeyState.digits.set(9, KeyState.rootExp);
        } else {
            KeyState.digits.set(9, null);
        }
        
        if (acceptDot) {
            KeyState.digits.set(11, ". -");
        } else if (acceptMinus) {
            KeyState.digits.set(11, "-");
        } else {
            KeyState.digits.set(11, null);
        }
        
        int openParens = 0;
        for (int i = pos; i >= 0; --i) {
            if (line.charAt(i) == '(') {
                ++openParens;
            } else if (line.charAt(i) == ')') {
                --openParens;
            }
            if (openParens > 0) { break; }
        }
        KeyState.rootOp.set(8, (openParens > 0) ? ")" : null);
        KeyState.keypad = id ? KeyState.rootOp : KeyState.digits;
    }
    
    static final boolean isDigit(char c) {
        return '0' <= c && c <= '9';
    }

    final boolean isDigitAt(int p) {
        return isDigit(line.charAt(p));
    }

    final boolean isLetterAt(int p) {
        char c = line.charAt(p);
        return ('a' <= c && c <= 'z') || c == '_';
    }

    void delFromLine() {
        int prev = prevFlexPoint(pos);
        line.delete(prev + 1, pos + 1);
        pos = prev;
    }

    void insertIntoLine(String s) {
        line.insert(pos + 1, s);
        pos += s.length();
    }

    void movePrev() {
        int prev = prevFlexPoint(pos);
        pos = prev;
    }

    void moveNext() {
        int next = nextFlexPoint(pos);
        pos = next;
    }

    protected void keyPressed(int key) {
        boolean redrawEdit = false;
        int keyPos = getKeyPos(key);
        if (keyPos >= 0) {
            String s = KeyState.keypad.handleKey(keyPos);
            if (s != null) {
                if (pos >= 0 && line.charAt(pos) == '.' && 
                    s.length() == 1 && !isDigit(s.charAt(0))) {
                    delFromLine();
                }
                insertIntoLine(s);
                redrawEdit = true;
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
                        movePrev();
                    }
                    redrawEdit = true;
                    break;
                    
                case Canvas.RIGHT:
                    if (pos < line.length()) {
                        moveNext();
                    }
                    redrawEdit = true;
                    break;
                    
                case Canvas.UP:
                    break;
                    
                case Canvas.DOWN:
                    break;
                    
                case Canvas.FIRE:
                    break;
                    
                }
            } else {
                if (key == -8 || key == -11) { //delete
                    KeyState save = KeyState.keypad;
                    resetState();
                    if (save == KeyState.keypad) {
                        delFromLine();
                    }
                    redrawEdit = true;
                }
            }
        }
        if (redrawEdit) {
            drawCursor = true;
            repaint(0, editY, w, editH);
            resetState();
        } else if (KeyState.needPaint()) {
            repaint(keypadX, h, keypadW, keypadH);
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
