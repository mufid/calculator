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
    int cursorX, cursorY, cursorW = 2, cursorH;

    int editY, editH;
    Line drawn[] = new Line[3], splited[] = new Line[4];

    Timer cursorBlink = new Timer();
    Expr expr = new Expr();
    Constant ans = new Constant("ans", 0);
    double result;
    boolean hasResult = false;

    Font font;
    int nEditLines;
    int lineHeight;

    void setFont(Font afont) {
        font = afont;
        lineHeight = afont.getHeight() + 1;
        resultH = lineHeight;
        resultY = h - resultH;
        int avail = resultY / lineHeight;
        nEditLines = Math.min(avail, 3);
        editH   = nEditLines * lineHeight;
        editY   = resultY - editH;
        //System.out.println("lines " + nEditLines + " y " + editY);
    }

    CalcCanvas() {
        entry = new HistEntry();
        entry.line = new StringBuffer();
        entry.save = "";
        entry.pos  = -1;
        history.addElement(entry);

        for (int i = 0; i < 3; ++i) {
            drawn[i] = new Line();
            splited[i] = new Line();
        }
        splited[3] = new Line();

        setFullScreenMode(true);
        w = getWidth();
        screenH = getHeight();

        KeyState.init(w, screenH);
        h = screenH - KeyState.h;

        setFont(h > largeHeight * 5 ? largeFont : normalFont);

        cursorX = 0;
        cursorY = editY;
        cursorH = largeHeight;
        cursorImg = Image.createImage(cursorW, cursorH);
        cursorG   = cursorImg.getGraphics();

        img  = Image.createImage(w, h);
        imgG = img.getGraphics();

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
        repaint();
        expr.symbols.put(ans);
    }

    char splitBuf[] = new char[128];
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
               
    void paintLine(int y, Line drawn, Line line) {
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
        imgG.setColor(0xffffff);
        imgG.fillRect(commonW, y, paintW, lineHeight);
        imgG.setColor(0);
        imgG.drawChars(line.chars, common, line.len - common, commonW, y, 0);
        if (common <= line.redPos && line.redPos < line.len) {
            int pos = commonW + font.charsWidth(line.chars, common, line.redPos - common);
            imgG.setColor(0xff0000);
            imgG.drawChar(line.chars[line.redPos], pos, y, 0);
        }

        System.arraycopy(line.chars, common, drawn.chars, common, line.len - common);
        drawn.len = line.len;
        drawn.redPos = line.redPos;

        repaint(commonW, y, paintW, lineHeight);
    }

    void paintEdit() {
        setCursor(drawCursor); //invalidate
        int redPos = expr.tokenStart - 2;
        split(entry.line, redPos, splited);
        int y = editY;
        imgG.setFont(font);
        int cursorPos = entry.pos + 1;
        //System.out.println("cursor " + cursorPos);
        Line line;
        for (int i = 0; i < 3; ++i, y += lineHeight) {
            line = splited[i];
            paintLine(y, drawn[i], line);
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

                    int y = editY - lineHeight/2;
                    imgG.setColor(0xe0e0ff);
                    imgG.fillRect(0, 0, w, y);
                    imgG.setColor(0);
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
                            imgG.drawChars(splited[i].chars, 0, splited[i].len, 0, y, Graphics.BOTTOM|Graphics.LEFT);
                            y-= lineHeight;
                        }
                        y-= lineHeight / 2;
                        ++p;
                    }
                    repaint(0, 0, w, y);

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
                if (hasResult) {
                    imgG.setColor(0xffffff);
                    imgG.setFont(font);
                    imgG.drawString(Float.toString((float)result), w, resultY, Graphics.TOP|Graphics.RIGHT);
                }
                repaint(0, resultY, w, resultH);
            }
            paintEdit();
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
