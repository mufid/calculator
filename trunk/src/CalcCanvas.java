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
    
    //Image img, cursorImg;
    //Graphics imgG, cursorG;
    int w, h, screenH;

    int resultY, resultH;
    int cursorX, cursorY, cursorW = 2, cursorH;

    //int editY, editH, nLines;
    Line drawn[] = new Line[10], splited[] = new Line[10];

    Timer cursorBlink = new Timer();
    Expr expr = new Expr();
    Constant ans = new Constant("ans", 0);
    double result;
    boolean hasResult = false;

    Font font;
    //int nEditLines;
    int maxEditLines;
    int lineHeight;
    int editH, historyH, keypadH;

    static final int RESULT = 0, EDIT = 1, HISTORY = 2, N_ZONES = 3;
    static final int bgCol[] = { 0x0,      0xffffff, 0xe0e0eff};
    static final int fgCol[] = { 0xffffff, 0x0     , 0x0};
    int height[] = new int[N_ZONES];
    Image img[] = new Image[N_ZONES];
    Graphics gg[] = new Graphics[N_ZONES];



    void setFont(Font afont) {
        font = afont;
        lineHeight = afont.getHeight() + 1;
        resultH = lineHeight;
        resultY = h - resultH;
        maxEditLines = (h - resultH)/lineHeight;
        height[RESULT]  = lineHeight;
        height[EDIT]    = maxEditLines * lineHeight;
        height[HISTORY] = h - resultH - lineHeight;

        for (int i = 0; i < N_ZONES; ++i) {
            img[i] = Image.createImage(w, height[i]);
            Graphics g = img[i].getGraphics();
            gg[i] = g;
            g.setColor(bgCol[i]);
            g.fillRect(0, 0, w, height[i]);
            g.setFont(font);
        }
    }

    CalcCanvas() {
        entry = new HistEntry();
        entry.line = new StringBuffer();
        entry.save = "";
        entry.pos  = -1;
        history.addElement(entry);

        for (int i = 0; i < drawn.length; ++i) {
            drawn[i]   = new Line();
            splited[i] = new Line();
        }

        setFullScreenMode(true);
        w = getWidth();
        screenH = getHeight();

        KeyState.init(w, screenH);
        h = screenH; // - KeyState.h;

        //setFont(h > largeHeight * 5 ? largeFont : normalFont);
        setFont(largeFont);
        gg[HISTORY].setFont(normalFont);

        cursorX = 0;
        cursorY = resultY;
        cursorH = lineHeight;

        cursorBlink.schedule(new TimerTask() {
                public void run() {
                    setCursor(!drawCursor);
                }
            }, 400, 400);
        repaint();
        expr.symbols.put(ans);
    }

    char splitBuf[] = new char[256];
    int split(StringBuffer str, int redPos, Line lines[]) {
        int len = str.length();
        str.getChars(0, len, splitBuf, 0);
        Line line;
        int start = 0, end = 0;
        int width;
        int nLines = 0;
        for (int i = 0; i < 4; ++i) {
            line = lines[i];
            width = 0;
            while (width < w && end < len) {
                width += font.charWidth(splitBuf[end++]);
            }
            if (width > w) { --end; }
            line.len = end - start;
            line.redPos = start <= redPos && redPos < end ? redPos - start : line.len;

            if (line.len > 0) {
                System.arraycopy(splitBuf, start, line.chars, 0, line.len);
                ++nLines;
            }
            start = end;
        }
        return nLines;
    }
               
    void paintLine(int y, Line drawn, Line line, int offsetY) {
        //int maxCommon = Math.min(Math.min(startRed, len), drawnStartRed);
        int maxCommon = Math.min(drawn.len, line.len);
        if (drawn.redPos != line.redPos) {
            if (drawn.redPos < maxCommon) { maxCommon = drawn.redPos; }
            if (line.redPos  < maxCommon) { maxCommon = line.redPos; }
        }
        int common = 0;
        while (common < maxCommon && drawn.chars[common] == line.chars[common]) { ++common; }
        int commonW = font.charsWidth(line.chars, 0, common);
        int w1 = font.charsWidth(line.chars,  common, line.len - common);
        int w2 = font.charsWidth(drawn.chars, common, drawn.len - common);
        int paintW  = Math.max(w1, w2);
        Graphics g = gg[EDIT];
        g.setColor(bgCol[EDIT]);
        g.fillRect(commonW, y, paintW, lineHeight);
        g.setColor(fgCol[EDIT]);
        g.drawChars(line.chars, common, line.len - common, commonW, y, 0);
        if (common <= line.redPos && line.redPos < line.len) {
            int pos = commonW + font.charsWidth(line.chars, common, line.redPos - common);
            g.setColor(0xff0000);
            g.drawChar(line.chars[line.redPos], pos, y, 0);
        }

        System.arraycopy(line.chars, common, drawn.chars, common, line.len - common);
        drawn.len = line.len;
        drawn.redPos = line.redPos;

        repaint(commonW, y + offsetY, paintW, lineHeight);
    }

    void updateEdit() {
        setCursor(drawCursor); //invalidate
        int redPos = expr.tokenStart - 2;
        int nEditLines = split(entry.line, redPos, splited);
        int y = 0;
        int cursorPos = entry.pos + 1;
        //System.out.println("cursor " + cursorPos);
        
        editH = nEditLines * lineHeight;
        keypadH = KeyState.getH();
        int avail = h - resultH - keypadH;
        if (avail < editH) { editH = avail; }
        historyH = avail - editH;

        Line line;
        for (int i = 0; i < nEditLines; ++i, y += lineHeight) {
            line = splited[i];
            paintLine(y, drawn[i], line, keypadH + historyH);
            if (cursorPos >= 0 && cursorPos <= line.len) {
                cursorX = font.charsWidth(line.chars, 0, cursorPos);
                cursorY = y;
                cursorPos = -1;
            } else {
                cursorPos -= line.len;
            }
        }
        setCursor(true);
    }

    void updateHistory() {
        Graphics g = gg[HISTORY];
        g.setColor(bgCol[HISTORY]);
        g.fillRect(0, 0, w, height[HISTORY]);
        g.setColor(fgCol[HISTORY]);

        int y = historyH;
        int p = 1;
        while (y > 0 && p < history.size()) {
            String str = ((HistEntry) history.elementAt(p)).save;
            StringBuffer strBuf = new StringBuffer(str);
            try {
                double v = expr.parseNoDecl(str);
                strBuf.append("= ").append(Float.toString((float)v));
            } catch (Error e) {
            }
            int nLines = split(strBuf, -1, splited);
            for (int i = nLines - 1; i >= 0 && y > 0; --i) {
                g.drawChars(splited[i].chars, 0, splited[i].len, 0, y, Graphics.BOTTOM|Graphics.LEFT);
                y-= lineHeight;
            }
            y-= lineHeight / 2;
            ++p;
        }
    }

    void updateResult() {
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
            Graphics g = gg[RESULT];
            g.setColor(bgCol[RESULT]);
            g.fillRect(0, 0, w, height[RESULT]);
            if (hasResult) {
                g.setColor(fgCol[RESULT]);
                g.drawString(Float.toString((float)result), w, resultY, Graphics.TOP|Graphics.RIGHT);
            }
            repaint(0, resultY, w, resultH);
        }        
    }

    boolean drawCursor = true;

    void setCursor(boolean setOn) {
        drawCursor = setOn;
        repaint(cursorX, cursorY, cursorW, cursorH);
    }

    protected void paint(Graphics g) {
        KeyState.paint(g);
        g.drawRegion(img[HISTORY], 0, height[HISTORY]-historyH, w, historyH, 0, 
                     0, keypadH, 0);
        g.drawRegion(img[EDIT], 0, height[EDIT]-editH, w, editH, 0, 
                     0, keypadH + historyH, 0);
        g.drawImage(img[RESULT], 0, resultY, 0);

        if (drawCursor) {
            g.setColor(0);
            g.fillRect(cursorX, cursorY, cursorW, cursorH);
        }
    }

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
        if (pos < 0) {
            return pos;
        }
        if (isDigitAt(pos)) {
            return pos-1;
        }
        int p = pos;
        if (pos >= 1 && entry.line.charAt(pos) == '(') {
            --p;
        }
        if (p >= 0 && isLetterAt(p)) {
            while (p >= 0 && isLetterAt(p)) --p;
            return p;
        }
        return pos - 1;
    }

    int nextFlexPoint(int pos) {
        int len = entry.line.length();
        if (pos >= len - 1) {
            return pos;
        }
        ++pos;
        if (isLetterAt(pos)) {
            do {
                ++pos;
            } while (pos < len && isLetterAt(pos));
            if (pos < len && entry.line.charAt(pos) == '(') ++pos;
            return pos - 1;
        }
        return pos;
    }
    
    final boolean isDigitAt(int p) {
        return Character.isDigit(entry.line.charAt(p));
    }

    final boolean isLetterAt(int p) {
        return Expr.isLetter(entry.line.charAt(p));
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
                    history.insertElementAt(entry.copyFlush(), 1);
                    historyPos = 0;
                    entry = (HistEntry) history.elementAt(historyPos);
                    redrawEdit = true;

                    updateHistory();
                    repaint(0, keypadH, w, historyH);

                    ans.value = hasResult ? result : 0;
                    break;
                }
            } else {
                if (key == -8 || key == -11 || key == -12) { //delete
                    if (KeyState.keypad != null) {
                        delFromLine();
                        redrawEdit = true;
                    } else {
                        KeyState.keypad = null;
                    }
                }
            }
        }
        if (redrawEdit) {
            updateEdit();
            updateResult();
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
