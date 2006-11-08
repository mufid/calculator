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

    //static final int MAX_HIST = 32;
    Vector history = new Vector();
    int historyPos = 0;
    HistEntry entry;
    
    int w, h;

    //int resultY, resultH;
    int cursorX, cursorY, cursorW = 2, cursorH;

    private static final int MAX_LINES = 10;
    Line drawn[], splited[];

    Expr expr = new Expr();
    Constant ans = new Constant("ans", 0);
    double result;
    boolean hasResult = false;
    boolean needUpdateResult, changed;

    Font font = largeFont;
    int lineHeight = font.getHeight() + 1;

    int nEditLines, nDrawnLines;
    int maxEditLines;
    int editH;
    //, historyH, keypadH;

    static final int RESULT = 0, EDIT = 1, HISTORY = 2, N_ZONES = 3;
    static final int bgCol[] = { 0x0,      0xffffff, 0xe0e0ff};
    static final int fgCol[] = { 0xffffff, 0x0     , 0x0};
    int height[] = new int[N_ZONES];
    Image img[] = new Image[N_ZONES];
    Graphics gg[] = new Graphics[N_ZONES];
    Thread thread;

    CalcCanvas() {
        entry = new HistEntry();
        entry.line = new StringBuffer();
        entry.save = "";
        entry.pos  = -1;
        history.addElement(entry);

        setFullScreenMode(true);
        w = getWidth();
        h = getHeight();

        KeyState.init(w, h);
        // - KeyState.h;
        //resultH = lineHeight + 2;
        //resultY = h - resultH;

        maxEditLines = (h - KeyState.h)/lineHeight;
        System.out.println("max edit lines " + maxEditLines);
        drawn   = new Line[maxEditLines];
        splited = new Line[maxEditLines];

        for (int i = 0; i < maxEditLines; ++i) {
            drawn[i]   = new Line();
            splited[i] = new Line();
        }

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

        editChanged();
        updateCursor();
        repaint();
        expr.symbols.put(ans);
        thread = new Thread(this);
    }

    boolean twiced;
    public void run() {
        if (twiced) {
            setCursor(!drawCursor);
        }
        twiced = !twiced;
        if (!changed && needUpdateResult) {
            needUpdateResult = false;
            updateResult();
        }
        changed = false;
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
        }
    }

    char splitBuf[] = new char[256];
    int split(Font font, StringBuffer str, int w, Line lines[]) {
        int len = str.length();
        str.getChars(0, len, splitBuf, 0);
        Line line;
        int start = 0, end = 0;
        int width;
        int mW = font.charWidth('m');
        int i, n;
        int sizeLeft = len;
        int cw;
        for (i = 0; i < MAX_LINES; ++i) {
            line = lines[i];
            int left = w;
            //assert(left >= 0);
            while ((n = Math.min(left/mW, sizeLeft)) > 0) {
                //System.out.println("n " + n + "; left " + left);
                left-= font.charsWidth(splitBuf, end, n);
                end += n;
                sizeLeft -= n;
            }
            while (sizeLeft > 0 && left > (cw = font.charWidth(splitBuf[end]))) {
                ++end;
                --sizeLeft;
                left-= cw;
            }
            line.len = end - start;
            if (start == end) { break; }
            //line.redPos = start <= redPos && redPos < end ? redPos - start : line.len;
            System.arraycopy(splitBuf, start, line.chars, 0, line.len);
            start = end;
        }
        int nLines = i;
        return nLines > 0 ? nLines : 1;
    }

    void computeLineCoords(Line lines[], int nLines, int p, int outLC[]) {
        int line = 0;
        while (line < nLines && p >= lines[line].len) { p-= lines[line++].len; }
        outLC[0] = line;
        outLC[1] = p;
    }
               
    void paintLine(int y, Line drawn, Line line) {
        //int maxCommon = Math.min(Math.min(startRed, len), drawnStartRed);
        int maxCommon = Math.min(drawn.len, line.len);
        /*
        if (drawn.redPos != line.redPos) {
            if (drawn.redPos < maxCommon) { maxCommon = drawn.redPos; }
            if (line.redPos  < maxCommon) { maxCommon = line.redPos; }
        }
        */
        int common = 0;
        while (common < maxCommon && drawn.chars[common] == line.chars[common]) { 
            ++common; 
        }
        int commonW = font.charsWidth(line.chars, 0, common);
        int w1 = line.len  == common ? 0 : font.charsWidth(line.chars,  common, line.len - common);
        int w2 = drawn.len == common ? 0 : font.charsWidth(drawn.chars, common, drawn.len - common);
        int paintW  = Math.max(w1, w2);
        Graphics g = gg[EDIT];
        g.setColor(bgCol[EDIT]);
        g.fillRect(commonW, y, paintW, lineHeight);
        g.setColor(fgCol[EDIT]);
        g.drawChars(line.chars, common, line.len - common, commonW, y, 0);
        /*
        if (common <= line.redPos && line.redPos < line.len) {
            int pos = commonW + font.charsWidth(line.chars, common, line.redPos - common);
            g.setColor(0xff0000);
            g.drawChar(line.chars[line.redPos], pos, y, 0);
        }
        */

        System.arraycopy(line.chars, common, drawn.chars, common, line.len - common);
        drawn.len = line.len;
        //drawn.redPos = line.redPos;
        if (paintW > 0) {
            repaint(commonW, y, paintW, lineHeight);
        }
    }

    /*
    void computeHeights() {
        editH = nEditLines * lineHeight;
        //keypadH = KeyState.getH();
        //historyH = heigt[HISTORY] - keypadH;
    }
    */

    void editChanged() {
        //int redPos = expr.tokenStart - 2;
        //int y = 0;
        nEditLines = split(font, entry.line, w, splited);
        //computeHeights();
        for (int i = 0, y = 2; i < nEditLines; ++i, y+=lineHeight) {
            paintLine(y, drawn[i], splited[i]);
        }
        if (nEditLines != nDrawnLines) {
            repaint();
            nDrawnLines = nEditLines;
        }
    }

    private final int cursorLC[] = new int[2];
    void updateCursor() {
        setCursor(drawCursor);
        computeLineCoords(splited, nEditLines, entry.pos, cursorLC);
        int cursorL = cursorLC[0];
        cursorY = cursorL * lineHeight + 1;
        cursorX = font.charsWidth(splited[cursorL].chars, 0, cursorLC[1] + 1);
        //System.out.println("cursor: l " + cursorL + " c " + cursorLC[1] +
        //                   " x " + cursorX + " y " + cursorY);
        setCursor(true);
    }

    void updateHistory() {
        Graphics g = gg[HISTORY];
        g.setColor(bgCol[HISTORY]);
        g.fillRect(0, 0, w, height[HISTORY]);
        g.setColor(fgCol[HISTORY]);
        
        int y = normalHeight / 2;
        int histSize = history.size();
        int yLimit = height[HISTORY] - normalHeight;
        for (int p = 1; p < histSize && y <= yLimit; ++p, y+= normalHeight/2) {
            String str = ((HistEntry) history.elementAt(p)).save;
            StringBuffer strBuf = new StringBuffer(str);
            try {
                double v = expr.parseNoDecl(str);
                strBuf.append(" = ").append(format(v));
            } catch (Error e) {
            }
            int nLines = split(normalFont, strBuf, w, splited);
            for (int i = 0; i < nLines && y <= yLimit; ++i, y+=normalHeight) {
                g.drawChars(splited[i].chars, 0, splited[i].len, 0, y, 0);
            }
        }
    }

    private StringBuffer formatBuf = new StringBuffer();
    private Line formatLines[] = {new Line(), new Line()};
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
        formatBuf.setLength(0);
        formatBuf.append(s);
        if (len > 2 && formatBuf.charAt(len-1) == '0' && formatBuf.charAt(len-2) == '.') {
            formatBuf.setLength(len - 2);
        }
        split(font, formatBuf, w - tailW, formatLines);
        formatBuf.setLength(formatLines[0].len);
        if (tail != null) {
            formatBuf.append(tail);
        }
        return formatBuf.toString();
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
        //System.out.println("result " + newResult);
        if (hasNewResult != hasResult || newResult != result) {
            result = newResult;
            hasResult = hasNewResult;
            Graphics g = gg[RESULT];
            g.setColor(bgCol[RESULT]);
            g.fillRect(0, 0, w, height[RESULT]);
            if (hasResult) {
                g.setColor(fgCol[RESULT]);
                g.drawString("= " + format(result), 0, 0, 0);
            }
            int resultY = nEditLines * lineHeight + 2;
            repaint(0, resultY, w, height[RESULT]);
        }        
    }

    boolean drawCursor = true;

    void setCursor(boolean setOn) {
        drawCursor = setOn;
        repaint(cursorX, cursorY, cursorW, cursorH);
    }

    protected void paint(Graphics g) {
        //computeHeights();
        KeyState.paint(g);
        /*
        System.out.println("historyH " + historyH + " keypadH " + keypadH +
                           " editH " + editH + " w " + w + " resultY " + resultY);
        */
        int editH = nEditLines * lineHeight + 2;
        int keypadH = KeyState.getH();
        KeyState.paint(g);
        g.drawRegion(img[EDIT], 0, 0, w, editH, 0,
                     0, 0, 0);
        g.drawImage(img[RESULT], 0, editH, 0);
        g.drawRegion(img[HISTORY], 0, 0, w, h-editH-height[RESULT]-keypadH, 0,
                     0, editH+height[RESULT], 0);
        
        /*
        g.drawRegion(img[HISTORY], 0, height[HISTORY]-historyH, w, historyH, 0, 
                     0, keypadH, 0);
        g.drawRegion(img[EDIT], 0, height[EDIT]-editH, w, editH, 0, 
                     0, keypadH + historyH, 0);
        g.drawImage(img[RESULT], 0, resultY, 0);
        */
        
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

    protected void keyRepeated(int key) {
        keyPressed(key);
    }

    protected void keyPressed(int key) {
        boolean redrawEdit = false;
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
                        entry.pos -= len - 1;
                    }
                }
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
                    repaint();

                    ans.value = hasResult ? result : 0;
                    break;
                }
            } else {
                if (key == -8 || key == -11 || key == -12) { //delete
                    if (KeyState.keypad == null) {
                        delFromLine();
                        redrawEdit = true;
                    } else {
                        KeyState.keypad = null;
                    }
                }
            }
        }
        if (redrawEdit) {
            editChanged();
            updateCursor();
            needUpdateResult = true;
            changed = true;
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
