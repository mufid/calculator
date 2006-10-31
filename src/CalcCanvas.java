import javax.microedition.lcdui.*;
import java.util.*;

class CalcCanvas extends Canvas {
    static Font smallFont, normalFont, largeFont, largeBold;
    static int smallHeight, normalHeight, largeHeight;    
    static {
        smallFont  = Font.getFont(0, 0, Font.SIZE_SMALL);
        normalFont = Font.getFont(0, 0, Font.SIZE_MEDIUM);
        largeFont  = Font.getFont(0, 0, Font.SIZE_LARGE);
        largeBold  = Font.getFont(0, Font.STYLE_BOLD, Font.SIZE_LARGE);

        smallHeight  = smallFont.getHeight();
        normalHeight = normalFont.getHeight();
        largeHeight  = largeFont.getHeight();
    }


    Image img, cursorImg;
    Graphics imgG, cursorG;
    int w, h, screenH;

    int resultY, resultH;

    int blank3Y, blank3H, blank4X;
    int keypadH;
    int cursorX, cursorY, cursorW = 2, cursorH;

    Timer cursorBlink = new Timer();
    Expr expr = new Expr();
    String result = "";
    Font font;

    void setFont(Font afont) {
        font = afont;
        int height = afont.getHeight();
        editH = resultH = height + 1;
        resultY = h - resultH;
        editY   = resultY - 1 - editH;
    }

    CalcCanvas() {
        setFullScreenMode(true);
        w = getWidth();
        screenH = getHeight();

        KeyState.init(w, screenH);
        keypadH = KeyState.h;
        h = screenH - keypadH;

        setFont(h > largeHeight * 4 ? largeFont : normalFont);

        cursorX = 0;
        cursorY = editY;
        cursorH = largeHeight;
        cursorImg = Image.createImage(cursorW, cursorH);
        cursorG   = cursorImg.getGraphics();

        img  = Image.createImage(w, h);
        imgG = img.getGraphics();

        editG = img.getGraphics();
        editG.translate(0, editY);
        editG.setFont(font);

        imgG.setColor(0xe0e0ff);
        imgG.fillRect(0, 0, w, h);

        imgG.setColor(0xffffff);
        imgG.fillRect(0, editY, w, editH);

        imgG.setColor(0);
        imgG.fillRect(0, resultY, w, resultH);
                
        cursorBlink.schedule(new TimerTask() {
                public void run() {
                    setCursor(!drawCursor);
                }
            }, 400, 400);
        resetState();
        repaint();
    }

    //Edit
    int editY, editH;
    Graphics editG;
    int drawnLen = 0;
    char drawn[] = new char[256];
    char buf[] = new char[256];

    void paintEdit() {
        int len = line.length();
        line.getChars(0, len, buf, 0);
        buf[len] = 0;

        int startRed = expr.tokenStart;
        int maxCommon = Math.min(Math.min(startRed, len), drawnLen);
        int common = 0;
        while (common < maxCommon && drawn[common] == buf[common]) { ++common; }
        int commonW = font.charsWidth(buf, 0, common);
        int paintLen = Math.max(len, drawnLen) - common;
        int paintW  = font.charsWidth(buf, common, paintLen);
        editG.setColor(0xffffff);
        editG.fillRect(commonW, 0, paintW, editH);
        int pos2 = commonW;
        if (startRed > common) {
            editG.setColor(0);
            editG.drawChars(buf, common, startRed - common, commonW, 0, 0);
            pos2 += font.charsWidth(buf, common, startRed - common);
        }
        if (startRed < len) {
            int start = Math.max(common, startRed);
            editG.setColor(0xff0000);
            editG.drawChars(buf, start, len - start, pos2, 0, 0);
        }

        repaint(commonW, editY, paintW, editH);
        System.arraycopy(buf, common, drawn, common, len - common + 1);
        drawnLen = Math.min(len, startRed);

        setCursor(drawCursor);
        cursorX = font.charsWidth(buf, 0, pos + 1);
        setCursor(true);
    }

    boolean drawCursor = true;

    void setCursor(boolean setOn) {
        drawCursor = setOn;
        repaint(cursorX, cursorY, cursorW, cursorH);
    }

    protected void paint(Graphics g) {
        g.drawImage(img, 0, 0, 0);
        if (drawCursor) {
            g.setColor(0);
            g.fillRect(cursorX, cursorY, cursorW, cursorH);
            //g.drawImage(cursorImg, cursorX, cursorY, 0);
        }
        KeyState.paint(g);
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
            if (isLetter(c) || c == ')' || c == '!') {
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
        KeyState.rootOp.set(5, (openParens > 0) ? ")" : null);
        KeyState.keypad = id ? KeyState.rootOp : KeyState.digits;
    }
    
    final boolean isDigitAt(int p) {
        return Character.isDigit(line.charAt(p));
    }

    static final boolean isLetter(char c) {
        return ('a' <= c && c <= 'z') || c == '_' || c == '\u03c0';
    }

    final boolean isLetterAt(int p) {
        return isLetter(line.charAt(p));
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
                    s.length() == 1 && !Character.isDigit(s.charAt(0))) {
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
            //System.out.println("key " + key + " action " + action);
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
                if (key == -8 || key == -11 || key == -12) { //delete
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
            String newResult = "";
            try {
                double value = expr.parseNoDecl(line.toString());
                newResult = Double.toString(value);
            } catch (Error e) {
            }
            if (!newResult.equals(result)) {
                result = newResult;
                imgG.setColor(0);
                imgG.fillRect(0, resultY, w, resultH);
                imgG.setColor(0xffffff);
                imgG.setFont(font);
                imgG.drawString(result, w, resultY, Graphics.TOP|Graphics.RIGHT);
                repaint(0, resultY, w, resultH);
            }
            paintEdit();
            resetState();
        } 
        KeyState.repaint(this);
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
