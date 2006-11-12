import javax.microedition.lcdui.*;
import java.util.*;

class CalcCanvas extends Canvas implements Runnable {
    static final Font 
        normalFont = Font.getFont(0, 0, Font.SIZE_MEDIUM), 
        largeFont  = Font.getFont(0, 0, Font.SIZE_LARGE), 
        largeBold  = Font.getFont(0, Font.STYLE_BOLD, Font.SIZE_LARGE);
    static final int normalHeight, largeHeight;
    static {
        normalHeight = normalFont.getHeight();
        largeHeight  = largeFont.getHeight();
    }

    static final String arityParens[] = { "", "()", "(,)", "(,,)"};
    History history;    
    int w, h;

    int cursorX, cursorY, cursorW = 2, cursorH;

    Expr parser = new Expr();
    Constant ans = new Constant("ans", 0);
    double result;
    boolean hasResult = false;
    boolean needUpdateResult, changed;

    Font font = largeFont;
    int lineHeight = font.getHeight() + 1;

    char line[] = new char[256];
    int len = 0, pos = -1;
    int editLines[];
    int nEditLines;
    int maxEditLines;
    int editH;

    static final int RESULT = 0, EDIT = 1, HISTORY = 2, N_ZONES = 3;
    static final int bgCol[] = { 0x0,      0xffffff, 0xe0e0ff};
    static final int fgCol[] = { 0xffffff, 0x0     , 0x0};

    int height[] = new int[N_ZONES];
    Image img[] = new Image[N_ZONES];
    Graphics gg[] = new Graphics[N_ZONES];
    Thread thread;

    CalcCanvas() {
        history = new History(this);

        setFullScreenMode(true);
        w = getWidth();
        h = getHeight();

        KeyState.init(w, h);

        maxEditLines = (h - KeyState.h)/lineHeight;
        System.out.println("max edit lines " + maxEditLines);
        editLines = new int[maxEditLines];
        
        height[RESULT]  = lineHeight + 2;
        height[EDIT]    = maxEditLines * lineHeight + 2;
        height[HISTORY] = h - height[RESULT] - lineHeight - 2;

        for (int i = 0; i < N_ZONES; ++i) {
            img[i] = Image.createImage(w, height[i]);
            Graphics g = img[i].getGraphics();
            gg[i] = g;
            g.setColor(bgCol[i]);
            g.fillRect(0, 0, w, height[i]);
            g.setFont(font);
        }
        gg[HISTORY].setFont(normalFont);

        cursorX = 20;
        cursorY = 10;
        cursorH = lineHeight;

        editChanged(0);
        updateCursor();
        updateHistory();
        repaint();
        parser.symbols.put(ans);
        thread = new Thread(this);
        thread.start();
    }

    boolean twiced;
    public void run() {
        while (true) {
            if (twiced) {
                setCursor(!drawCursor);
            }
            twiced = !twiced;
            if (!changed && needUpdateResult) {
                computeResult(false);
                updateResult();
            }
            changed = false;
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
            }
        }
    }

    int split(Font font, char buf[], int len, int w, 
              int changeLine, int lines[]) {
        int end = changeLine == 0 ? 0 : lines[changeLine - 1];
        int width;
        int mW = font.charWidth('m');
        int i, n;
        int sizeLeft = len - end;
        int cw;
        for (i = changeLine; i<lines.length; ++i) {
            int left = w;
            while ((n = Math.min(left/mW, sizeLeft)) > 0) {
                //System.out.println("n " + n + "; left " + left);
                left-= font.charsWidth(buf, end, n);
                end += n;
                sizeLeft -= n;
            }
            while (sizeLeft > 0 && left > (cw = font.charWidth(buf[end]))) {
                ++end;
                --sizeLeft;
                left-= cw;
            }
            lines[i] = end;
            //System.out.println("line " + i + "; end " + end + "; len " + len);
            if (end == len) { break; }
        }
        return i + 1;
    }

    int posToLine(int lines[], int pos) {
        int line = 0;
        while (pos > lines[line]) { ++line; }
        return line;
    }
               
    void editChanged(int changePos) {
        int changeLine = posToLine(editLines, changePos);
        int oldNLines = nEditLines;
        nEditLines = split(font, line, len, w, changeLine, editLines);
        //System.out.println("nEditLines " + nEditLines + "; changeLine " + changeLine);                           

        Graphics g = gg[EDIT];
        g.setColor(bgCol[EDIT]);
        g.fillRect(0, 2+changeLine*lineHeight, w, (nEditLines - changeLine)*lineHeight);
        repaint(0, 2+changeLine*lineHeight, w, (nEditLines - changeLine)*lineHeight);
        g.setColor(fgCol[EDIT]);
        int start = changeLine==0 ? 0 : editLines[changeLine-1];
        for (int i = changeLine, y = 2+changeLine*lineHeight,
                 end = editLines[i]; 
             i < nEditLines; ++i, y+=lineHeight, start = end, end = editLines[i]) {
            g.drawChars(line, start, end-start, 0, y, 0);
        }
        if (nEditLines != oldNLines) {
            repaint();
        }
    }

    boolean drawCursor = true;
    void setCursor(boolean setOn) {
        drawCursor = setOn;
        repaint(cursorX, cursorY, cursorW, cursorH);
    }

    void updateCursor() {
        setCursor(drawCursor);
        int cursorL = posToLine(editLines, pos+1);;
        cursorY = cursorL * lineHeight + 1;
        int start = cursorL==0?0:editLines[cursorL-1];
        cursorX = font.charsWidth(line, start, pos - start +1);
        //System.out.println("cursor: l " + cursorL + " c " + (pos-start+1) + " x " + cursorX + " y " + cursorY);
        setCursor(true);
    }

    char histBuf[] = new char[256+30];
    int histBufLen;
    int histLines[] = new int[8];
    void updateHistory() {
        Graphics g = gg[HISTORY];
        g.setColor(bgCol[HISTORY]);
        g.fillRect(0, 0, w, height[HISTORY]);
        g.setColor(fgCol[HISTORY]);
        
        int y = normalHeight / 2;
        int histSize = history.size();
        int yLimit = height[HISTORY] - normalHeight;
        for (int p = 1; p < histSize && y <= yLimit; ++p, y+= normalHeight/2) {
            HistEntry entry = history.get(p);            
            String base = entry.base;
            String result = entry.hasResult ? format(entry.result) : "";
            base.getChars(0, base.length(), histBuf, 0);
            histBufLen = base.length();
            if (result.length() > 0) {
                histBuf[histBufLen++] = ' ';
                histBuf[histBufLen++] = '=';
                histBuf[histBufLen++] = ' ';
                result.getChars(0, result.length(), histBuf, histBufLen);
                histBufLen += result.length();
            }
            int nLines = split(normalFont, histBuf, histBufLen, w, 0, histLines);
            for (int i = 0, start = 0; i < nLines && y <= yLimit; ++i, y+=normalHeight) {
                g.drawChars(histBuf, start, histLines[i]-start, 0, y, 0);
                start = histLines[i];
            }
        }
    }

    char formatBuf[] = new char[30];
    private int formatLines[] = new int[3];
    String format(double v) {
        String s = Double.toString(v);
        int ePos = s.lastIndexOf('E');
        String tail = null;
        int tailW = 0;
        if (ePos != -1) {
            tail = s.substring(ePos);
            tailW = font.stringWidth(tail);
            s = s.substring(0, ePos);
        }
        int len = s.length();
        s.getChars(0, len, formatBuf, 0);
        if (len > 2 && formatBuf[len-1] == '0' && formatBuf[len-2] == '.') {
            len -= 2;
        }
        split(font, formatBuf, len, w - tailW, 0, formatLines);
        len = formatLines[0];
        if (tail != null) {
            tail.getChars(0, tail.length(), formatBuf, len);
            len += tail.length();
        }
        return new String(formatBuf, 0, len);
    }

    String definedName;
    int arity;
    void computeResult(boolean define) {
        definedName = null;
        hasResult = false;
        arity = 0;

        if (len > 0) {
            String expr;
            char c = line[0];
            if (len > 3 && line[1] == ':' && line[2] == '=' && 
                (('a' <= c && c <= 'c') || ('f' <= c && c <= 'h'))) {
                expr = new String(line, 3, len - 3);
                definedName = String.valueOf(c);
            } else {
                expr = new String(line, 0, len);
            }
        
            try {
                result = parser.parse(expr);
                hasResult = true;
                arity = parser.arity;
            } catch (Error e) {
            }

            if (define && definedName != null) {
                parser.symbols.put(arity == 0 ? (Symbol) new Constant(definedName, result) : (Symbol) new DefinedFun(definedName, arity, expr));
            }
        }
    }

    private static final String params[] = {"(x)", "(x,y)", "(x,y,z)"};
    void updateResult() {
        needUpdateResult = false;
        computeResult(false);

        Graphics g = gg[RESULT];
        g.setColor(bgCol[RESULT]);
        g.fillRect(0, 0, w, height[RESULT]);

        if (hasResult) {
            String strResult = (definedName != null && arity > 0) ? 
                "Def " + definedName + params[arity-1] : format(result);
            g.setColor(fgCol[RESULT]);
            g.drawString("= " + strResult, 0, 0, 0);
        }

        int resultY = nEditLines * lineHeight + 2;
        repaint(0, resultY, w, height[RESULT]);
    }

    protected void paint(Graphics g) {
        KeyState.paint(g);
        //System.out.println("historyH "+historyH+" keypadH "+keypadH+" editH "+editH+" w "+w+" resultY "+resultY);
        int editH = nEditLines * lineHeight + 2;
        int keypadH = KeyState.getH();
        //System.out.println("pos " + pos + "; nLines " + nEditLines + "; cX " + cursorX + "; cY " + cursorY);
        KeyState.paint(g);
        g.drawRegion(img[EDIT], 0, 0, w, editH, 0,
                     0, 0, 0);
        g.drawImage(img[RESULT], 0, editH, 0);
        g.drawRegion(img[HISTORY], 0, 0, w, h-editH-height[RESULT]-keypadH, 0,
                     0, editH+height[RESULT], 0);
        if (drawCursor) {
            g.setColor(0);
            g.fillRect(cursorX, cursorY, cursorW, cursorH);
        }
    }
    
    int prevFlexPoint(int pos) {
        if (pos < 0) {
            return pos;
        }
        int p = pos;
        if (p >= 0 && line[p] == '(') {
            --p;
        }
        if (p >= 0 && isLetterAt(p)) {
            while (p >= 0 && isLetterAt(p)) --p;
            return p;
        }
        return pos - 1;
    }

    int nextFlexPoint(int pos) {
        if (pos >= len - 1) {
            return pos;
        }
        ++pos;
        if (isLetterAt(pos)) {
            do {
                ++pos;
            } while (pos < len && isLetterAt(pos));
            if (pos < len && line[pos] == '(') ++pos;
            return pos - 1;
        }
        return pos;
    }
    
    final boolean isDigitAt(int p) {
        return Character.isDigit(line[p]);
    }

    final boolean isLetterAt(int p) {
        return Expr.isLetter(line[p]);
    }

    void delFromLine() {
        int prev = prevFlexPoint(pos);
        System.arraycopy(line, pos+1, line, prev+1, len-(pos+1));
        len -= pos-prev;
        pos = prev;
    }

    void insertIntoLine(String s) {
        int strLen = s.length();
        System.arraycopy(line, pos+1, line, pos + strLen + 1, len-(pos+1));
        s.getChars(0, strLen, line, pos+1);
        pos += strLen;
        len += strLen;
    }

    protected void keyRepeated(int key) {
        keyPressed(key);
    }

    private void doChanged(int changePos) {
        editChanged(changePos);
        updateCursor();
        needUpdateResult = true;
        changed = true;
    }

    protected void keyPressed(int key) {
        int oldPos = pos;
        int keyPos = getKeyPos(key);
        if (keyPos >= 0) {
            String s = KeyState.handleKey(keyPos);
            if (s != null) {
                insertIntoLine(s);
                Symbol symbol = Expr.symbols.get(s);
                if (symbol != null) { // && symbol.arity > 0) {
                    String parens = arityParens[symbol.arity];
                    int len = parens.length();
                    if (len > 0) {
                        insertIntoLine(parens);
                        pos -= len - 1;
                    }
                }
                doChanged(oldPos);
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
                    pos = prevFlexPoint(pos);
                    updateCursor();
                    //changed = true;
                    break;
                    
                case Canvas.RIGHT:
                    pos = nextFlexPoint(pos);
                    updateCursor();
                    //changed = true;
                    break;
                    
                case Canvas.UP:
                    if (history.move(-1)) {
                        doChanged(0);
                    }
                    break;
                    
                case Canvas.DOWN:
                    if (history.move(1)) {
                        doChanged(0);
                    }
                    break;
                    
                case Canvas.FIRE:
                    computeResult(true);
                    boolean hasValue = hasResult && arity == 0;
                    if (hasValue) { 
                        ans.value = result; 
                    }
                    history.enter(new String(line, 0, len), result, hasValue);
                    doChanged(0);
                    updateResult();
                    updateHistory();
                    repaint();
                    break;
                }
            } else {
                if (key == -8 || key == -11 || key == -12) { //delete
                    if (KeyState.keypad == null) {
                        delFromLine();
                        doChanged(pos);
                    } else {
                        KeyState.keypad = null;
                        //changed = true;
                    }
                }
            }
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