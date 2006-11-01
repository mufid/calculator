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

    //static final int MAX_HIST = 32;
    Vector history = new Vector();
    int historyPos = 0;
    HistEntry entry;
    
    Image img, cursorImg;
    Graphics imgG, cursorG;
    int w, h, screenH;

    int resultY, resultH;

    int blank3Y, blank3H, blank4X;
    int keypadH;
    int cursorX, cursorY, cursorW = 2, cursorH;

    Timer cursorBlink = new Timer();
    Expr expr = new Expr();
    Constant ans = new Constant("ans", 0);
    double result;
    boolean hasResult = false;

    Font font;

    void setFont(Font afont) {
        font = afont;
        int height = afont.getHeight();
        editH = resultH = height + 1;
        resultY = h - resultH;
        editY   = resultY - 1 - editH;
    }

    CalcCanvas() {
        entry = new HistEntry();
        entry.line = new StringBuffer();
        entry.save = "";
        entry.pos  = -1;
        history.addElement(entry);

        setFullScreenMode(true);
        w = getWidth();
        screenH = getHeight();

        KeyState.init(w, screenH);
        keypadH = KeyState.h;
        h = screenH - keypadH;

        setFont(h > largeHeight * 5 ? largeFont : normalFont);

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

        expr.symbols.put(ans);
    }

    //Edit
    int editY, editH;
    Graphics editG;
    int drawnLen = 0, drawnStartRed = 0;
    char drawn[] = new char[256];
    char buf[] = new char[256];

    void paintEdit() {
        int len = entry.line.length();
        //System.out.println("Len " + len);
        entry.line.getChars(0, len, buf, 0);
        buf[len] = 0;

        int startRed = expr.tokenStart - 2;
        int maxCommon = Math.min(Math.min(startRed, len), drawnStartRed);
        int common = 0;
        while (common < maxCommon && drawn[common] == buf[common]) { ++common; }
        int commonW = font.charsWidth(buf, 0, common);
        //int paintLen = Math.max(len, drawnLen) - common;
        int paintW  = Math.max(font.charsWidth(buf, common, len-common), font.charsWidth(drawn, common, drawnLen-common));
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
        drawnLen = len;
        drawnStartRed = startRed;
        setCursor(drawCursor);
        cursorX = font.charsWidth(buf, 0, entry.pos + 1);
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

    /*
    StringBuffer line = new StringBuffer();
    int  pos = -1;
    */

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
        if (p >= 0 && entry.line.charAt(p) == '(') --p;
        while (p >= 0 && isDigitAt(p)) --p;
        if (p >= 0 && isLetterAt(p)) {
            while (p >= 0 && (isLetterAt(p) || isDigitAt(p))) --p;
            return p;
        }
        return pos - 1;
    }

    int nextFlexPoint(int pos) {
        int len = entry.line.length();
        if (pos >= len - 1) {
            return pos;
        }
        int p = pos + 1;
        if (isLetterAt(p)) {
            ++p;
            while (p < len && (isLetterAt(p) || isDigitAt(p))) ++p;
            if (p < len && entry.line.charAt(p) == '(') ++p;
            return p - 1;
        }
        return pos + 1;
    }

    void resetState() {
        int p = entry.pos;
        boolean number = false;
        while (p >= 0 && isDigitAt(p)) {
            number = true;
            --p;
        }
        boolean pastE = false;
        if (p >= 0 && entry.line.charAt(p) == 'E') {
            pastE = true;
            --p;
        } else if (p >= 1 && entry.line.charAt(p) == '-' && entry.line.charAt(p-1) == 'E') {
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
            char c = Character.toLowerCase(entry.line.charAt(p));
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
        boolean acceptMinus = !pastE || entry.line.charAt(entry.pos) == 'E';
        if (pastE && (entry.line.charAt(entry.pos) == 'E' || entry.line.charAt(entry.pos) == '-')) {
            number = false;
        }
        boolean atStart = acceptDot && (entry.pos == -1 || !isDigitAt(entry.pos));

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
        KeyState.keypad = id ? KeyState.rootOp : KeyState.digits;
    }
    
    final boolean isDigitAt(int p) {
        return Character.isDigit(entry.line.charAt(p));
    }

    static final boolean isLetter(char c) {
        return ('a' <= c && c <= 'z') || c == '_' || c == '\u03c0';
    }

    final boolean isLetterAt(int p) {
        return isLetter(entry.line.charAt(p));
    }

    void delFromLine() {
        int prev = prevFlexPoint(entry.pos);
        entry.line.delete(prev + 1, entry.pos + 1);
        entry.pos = prev;
    }

    void insertIntoLine(String s) {
        entry.line.insert(entry.pos + 1, s);
        entry.pos += s.length();
    }

    void movePrev() {
        int prev = prevFlexPoint(entry.pos);
        entry.pos = prev;
    }

    void moveNext() {
        int next = nextFlexPoint(entry.pos);
        entry.pos = next;
    }

    protected void keyPressed(int key) {
        boolean redrawEdit = false;
        int keyPos = getKeyPos(key);
        if (keyPos >= 0) {
            String s = KeyState.keypad.handleKey(keyPos);
            if (s != null) {
                if (entry.pos >= 0 && entry.line.charAt(entry.pos) == '.' && 
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
                    if (entry.pos >= 0) {
                        movePrev();
                    }
                    redrawEdit = true;
                    break;
                    
                case Canvas.RIGHT:
                    if (entry.pos < entry.line.length()) {
                        moveNext();
                    }
                    redrawEdit = true;
                    break;
                    
                case Canvas.UP:
                    if (historyPos < history.size() - 1) {
                        entry = (HistEntry) history.elementAt(++historyPos);
                        //System.out.println("size " + history.size() + " pos " + historyPos + " entry " + entry);
                        redrawEdit = true;
                    }
                    break;
                    
                case Canvas.DOWN:
                    if (historyPos > 0) {
                        entry = (HistEntry) history.elementAt(--historyPos);
                        redrawEdit = true;
                    }
                    break;
                    
                case Canvas.FIRE:
                    ans.value = hasResult ? result : 0;
                    history.insertElementAt(entry.copyFlush(), 1);
                    historyPos = 0;
                    entry = (HistEntry) history.elementAt(historyPos);
                    /*
                    line.setLength(0);
                    pos = -1;
                    */
                    redrawEdit = true;
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
            double newResult = 0;
            boolean hasNewResult = false;
            try {
                newResult = expr.parseNoDecl(entry.line.toString());
                hasNewResult = true;
                //newResult = 
            } catch (Error e) {
            }
            if (hasNewResult != hasResult || newResult != result) {
                result = newResult;
                hasResult = hasNewResult;
                imgG.setColor(0);
                imgG.fillRect(0, resultY, w, resultH);
                imgG.setColor(0xffffff);
                imgG.setFont(font);
                imgG.drawString(Float.toString((float)result), w, resultY, Graphics.TOP|Graphics.RIGHT);
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
