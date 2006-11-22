#define START_LINE(lines, n) (((n)==0)?0:lines[n-1])

import javax.microedition.lcdui.*;
import java.util.*;
import java.io.*;

class CalcCanvas extends Canvas implements Runnable {
    static final int KEY_CLEAR=-8, KEY_END=-11, KEY_POWER=-12;
    static final Font 
        normalFont = Font.getFont(0, 0, Font.SIZE_MEDIUM), 
        largeFont  = Font.getFont(0, 0, Font.SIZE_LARGE), 
        largeBold  = Font.getFont(0, Font.STYLE_BOLD, Font.SIZE_LARGE);
    static final int normalHeight, largeHeight;
    static {
        normalHeight = normalFont.getHeight();
        largeHeight  = largeFont.getHeight();
    }

    private static final String arityParens[] = {"", "()", "(,)", "(,,)"};
    private static final String params[] = {"(x)", "(x,y)", "(x,y,z)"};

    Expr parser = new Expr();
    History history; 

    int w, h;

    int cursorRow, cursorCol;
    int cursorX, cursorY, cursorW = 1, cursorH;


    Result result = new Result();
    boolean needUpdateResult = true;

    Font font = largeFont;
    int lineHeight = font.getHeight() + 1;

    char line[] = new char[256];
    int len = 0, pos = -1;
    int lastInsertLen = 0;
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

    CalcCanvas() {
        if (getHeight() <= 128) {
            setFullScreenMode(true);
        }
        w = getWidth();
        h = getHeight();

        history = new History(parser);

        DataInputStream is = C.rs.read(2);
        updateFromHistEntry(is == null ? new HistEntry("1+1", 0, false) : new HistEntry(is));
        //System.out.println(pos);
        if (is == null) {
            //first run, init history  
            history.enter("0.5!*2)^2");
            history.enter("sqrt(3^2+4^2");
            history.enter("sin(pi/2)");
            //history.enter("3!");
            //history.enter("e^ln(2");
        }
        
        KeyState.init(w, h);

        maxEditLines = 2; //(h - KeyState.h)/lineHeight;
        System.out.println("max edit lines " + maxEditLines);
        editLines = new int[maxEditLines + 1];
        
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

        editChanged(-1);
        updateCursor();
        updateHistory();
        repaint();
    }

    void threadRun() {
        while (true) {
            setCursor(!drawCursor);
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
            }
        }
    }

    public void run() {
        System.out.println("serially");
        if (needUpdateResult) {
            needUpdateResult = false;
            
            /*
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {}
            */
            Graphics rg = gg[RESULT];
            rg.setColor(bgCol[RESULT]);
            rg.fillRect(0, 0, w, height[RESULT]);
                
            if (parser.parse(String.valueOf(line, 0, len), result)) {
                String strResult = (result.arity > 0) ? 
                    result.name + params[result.arity-1] : format(result.value);
                rg.setColor(fgCol[RESULT]);
                rg.drawString(strResult, 0, 2, 0);
            } else {
                if (result.errorPos < len) {
                    markError(result.errorPos);
                }
            }
            int resultY = nEditLines * lineHeight + 2;
            int ry = resultY+2;
            int avail = h - ry - KeyState.getH();
            int rh = Math.min(height[RESULT]-3, avail);
            repaint(0, ry, w, rh);
        }
    }

    /*
    void clearHistory() {
        history.clear();
        updateHistory();
        repaint();
    }
    */
    
    /*
    void clearDefinitions() {
        parser.symbols.persistClear();        
        needUpdateResult = true;
        //todo: repaint edit
    }
    */
    
    int fitWidth(Font font, int targetWidth, char buf[], int start, int end) {
        int mW = font.charWidth('m');
        int n;
        while ((n = Math.min(targetWidth/mW, end - start)) >= 2) {
            targetWidth -= font.charsWidth(buf, start, n);
            start += n;
        }
        while (start < end && (targetWidth -= font.charWidth(buf[start])) >= 0) {
            ++start;
        }
        return start;
    }
    /*
        int baseW;
        if (end <= start || targetWidth < (baseW = font.charWidth(buf[start]))) {
            return start;
        }
        ++start;
        targetWidth -= baseW;
        int n = Math.min(targetWidth / baseW, end - start);
        int width = font.charsWidth(buf, start, n);
        baseW = width / n;
        if (width <= targetWidth) {
            if (n == end - start) { 
                return end; 
            }
            start += n;
            targetWidth -= width;
            n = Math.min(targetWidth * n / width, end - start);
        } else {
            end = start + n - 1;
            n = end - (width - targetWidth)/baseW - start;
        }
    }
    */

    int split(Font font, char buf[], int len, int w, 
              int changeLine, int lines[]) {
        //int width;
        //int mW = font.charWidth('m');
        //int i, n;
        //int sizeLeft = len - end;
        //int cw;
        int end = START_LINE(lines, changeLine);
        int i;
        for (i = changeLine; i < lines.length && end < len; ++i) {
            lines[i] = end = fitWidth(font, w, buf, end, len);
            
            /*
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
            */
            //System.out.println("line " + i + "; end " + end + "; len " + len);
            //if (end == len) { break; }
        }
        if (i == 0) {
            lines[0] = 0;
            return 1;
        }
        return i;
    }

    int posToLine(int lines[], int pos) {
        int line = 0;
        while (pos >= lines[line]) { ++line; }
        return line;
    }
               
    void editChanged(int changePos) {
        int changeLine = posToLine(editLines, changePos);
        int oldNLines = nEditLines;
        nEditLines = split(font, line, len, w, changeLine, editLines);
        //System.out.println("pos " + pos + " oldNLines " + oldNLines + " nEditLines " + nEditLines);
        if (nEditLines > maxEditLines) {
            pos = changePos;
            delFromLine(pos, lastInsertLen);
            nEditLines = maxEditLines;
            return;
        }
        //System.out.println("nEditLines " + nEditLines + "; changeLine " + changeLine);                           

        Graphics g = gg[EDIT];
        g.setColor(bgCol[EDIT]);
        g.fillRect(0, 2+changeLine*lineHeight, w, (nEditLines - changeLine)*lineHeight);
        repaint(0, 2+changeLine*lineHeight, w, (nEditLines - changeLine)*lineHeight);
        g.setColor(fgCol[EDIT]);
        int start = changeLine==0 ? 0 : editLines[changeLine-1];
        for (int i = changeLine, y = 2+changeLine*lineHeight,
                 end = editLines[i]; 
             i < nEditLines; ++i, y+=lineHeight, start = end) {
            end = editLines[i];
            g.drawChars(line, start, end-start, 0, y, 0);
        }
        if (nEditLines != oldNLines) {
            repaint();
        }
    }
    
    void markError(int errorPos) {
        //int errorPos = result.errorPos;
        //if (errorPos != -1) {
        int errLine = posToLine(editLines, errorPos);
        int startOfLine = errLine==0 ? 0 : editLines[errLine-1];
        int posInLine = errorPos - startOfLine;
        int w = font.charsWidth(line, startOfLine, posInLine);
        gg[EDIT].setColor(0xff0000);
        int y = 2+errLine*lineHeight;
        gg[EDIT].drawChar(line[errorPos], w, y, 0);
        repaint(w, y, font.charWidth(line[errorPos]), lineHeight);
        //} 
    }

    boolean drawCursor = true;
    void setCursor(boolean setOn) {
        drawCursor = setOn;
        repaint(cursorX, cursorY, cursorW, cursorH);
    }

    void updateCursor() {
        setCursor(drawCursor);
        int start;
        if (pos == -1) {
            cursorRow = 0;
            start     = 0;
            cursorCol = 0;
        } else {
            cursorRow = posToLine(editLines, pos); //pos+1
            start     = START_LINE(editLines, cursorRow);
            cursorCol = (pos+1) - start;
        }
        cursorY = cursorRow * lineHeight + 1;
        cursorX = font.charsWidth(line, start, cursorCol);
        if (cursorX > 0) {
            --cursorX;
        }
        System.out.println("pos " + pos + " row " + cursorRow + " col " + cursorCol + " x " + cursorX + " y " + cursorY);
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
    int ePos;
    private int formatAux(double v) {
        String s = Double.toString(v);
        ePos = s.lastIndexOf('E');
        int len = s.length();
        int exp;
        if (ePos == -1 || v <= 1 || (exp = Integer.parseInt(s.substring(ePos + 1))) > 10) {
            s.getChars(0, len, formatBuf, 0);
            return len;
        }
        formatBuf[0] = s.charAt(0);
        len = ePos;
        ePos = -1;
        if (exp + 2 < len) {
            int p = exp + 2;
            s.getChars(2, p, formatBuf, 1);            
            formatBuf[p-1] = '.';
            s.getChars(p, len, formatBuf, p);
            return len;
        } else {
            s.getChars(2, len, formatBuf, 1);
            for (int i = len - 2; i < exp; ++i) {
                formatBuf[i] = '0';
            }
            return exp+1;
        }
    }

    String format(double v) {
        int len = formatAux(v);
        if (ePos == -1) {
            ePos = len;
        }
        int baseLen = ePos;
        if (baseLen >= 2 && formatBuf[baseLen-1] == '0' && formatBuf[baseLen-2] == '.') {
            baseLen -= 2;
        }

        int tailW   = font.charsWidth(formatBuf, ePos, len - ePos);
        int n = fitWidth(font, w - tailW, formatBuf, 0, baseLen);
        return String.valueOf(formatBuf, 0, n) + String.valueOf(formatBuf, ePos, len - ePos);
    }

    protected void paint(Graphics g) {
        //System.out.println("paint");
        int editH = nEditLines * lineHeight + 2;
        int keypadH = KeyState.getH();
        KeyState.paint(g);
        //System.out.println("historyH "+historyH+" keypadH "+keypadH+" editH "+editH+" w "+w+" resultY "+resultY);
        //System.out.println("pos " + pos + "; nLines " + nEditLines + "; cX " + cursorX + "; cY " + cursorY);
        //System.out.println("a");
        g.drawRegion(img[EDIT], 0, 0, w, editH, 0,
                     0, 0, 0);
        int histAvail = h-editH-height[RESULT]-keypadH;
        if (histAvail > 0) {
            g.drawRegion(img[HISTORY], 0, 0, w, histAvail, 0,
                         0, editH+height[RESULT], 0);
        }
        //System.out.println("b");
        if (drawCursor) {
            //System.out.println("cursor on");
            g.setColor(0);
            g.fillRect(cursorX, cursorY, cursorW, cursorH);
        }
        //System.out.println("c");
        int resultAvail = h - editH - keypadH;
        if (resultAvail < height[RESULT]) {
            g.drawRegion(img[RESULT], 0, 0, w, resultAvail, 0,
                         0, editH, 0);
        } else {
            g.drawImage(img[RESULT], 0, editH, 0);
        }
        /*
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {}
        */
        if (needUpdateResult) {
            C.display.callSerially(this);
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
        if (p >= 0 && Expr.isLetter(line[p])) {
            do { --p; } while (p >= 0 && Expr.isLetter(line[p]));
            return p;
        }
        return pos - 1;
    }

    int nextFlexPoint(int pos) {
        if (pos >= len - 1) {
            return pos;
        }
        ++pos;
        if (Expr.isLetter(line[pos])) {
            do {
                ++pos;
            } while (pos < len && Expr.isLetter(line[pos]));
            if (pos < len && line[pos] == '(') ++pos;
            return pos - 1;
        }
        return pos;
    }

    /*
    final boolean isDigitAt(int p) {
        return Character.isDigit(line[p]);
    }
    */

    /*
    final boolean isLetterAt(int p) {
        return Expr.isLetter(line[p]);
    }
    */

    void delFromLine(int p, int size) {
        int p2 = p + size + 1;
        System.arraycopy(line, p2, line, p + 1, len - p2);
        len -= size;
    }

    void delFromLine() {
        int prev = prevFlexPoint(pos);
        delFromLine(prev, pos-prev);
        /*
        System.arraycopy(line, pos+1, line, prev+1, len-(pos+1));
        len -= pos-prev;
        */
        pos = prev;
    }

    void insertIntoLine(String s) {
        int strLen = s.length();
        if (len + strLen <= 255) {            
            System.arraycopy(line, pos+1, line, pos + strLen + 1, len-(pos+1));
            s.getChars(0, strLen, line, pos+1);
            pos += strLen;
            len += strLen;
        }
    }

    protected void keyRepeated(int key) {
        keyPressed(key);
    }

    private void doChanged(int changePos) {
        editChanged(changePos);
        updateCursor();
        needUpdateResult = true;
    }

    protected void keyPressed(int key) {
        System.out.println("key");
        int oldPos = pos;
        int keyPos = getKeyPos(key);
        lastInsertLen = 0;
        if (keyPos >= 0) {
            String s = KeyState.handleKey(keyPos);
            if (s != null) {
                if (pos == -1 && s.length() == 1 &&
                    "+-*/%^!".indexOf(s.charAt(0)) != -1) {
                    insertIntoLine("ans");
                }
                insertIntoLine(s);
                lastInsertLen += s.length();
                Symbol symbol = Expr.symbols.get(s);
                if (symbol != null) { // && symbol.arity > 0) {
                    String parens = arityParens[symbol.arity];
                    int parensLen = parens.length();
                    if (parensLen > 0) {
                        if (pos == len-1) {
                            insertIntoLine(parens);
                            pos -= parensLen - 1;
                            lastInsertLen += parensLen;
                        } else {
                            insertIntoLine("(");
                            ++lastInsertLen;
                        }
                    }
                }
                doChanged(oldPos);
            }
        } else {
            boolean clearedKeypad = KeyState.keypad != null;
            KeyState.keypad = null;
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
                    if (cursorRow == 0) {
                        history.getCurrent().update(String.valueOf(line, 0, len), pos);
                        if (history.move(-1)) {
                            updateFromHistEntry(history.getCurrent());
                            doChanged(-1);
                            needUpdateResult = true;
                        }
                    } else {
                        int width = font.charsWidth(line, editLines[cursorRow-1], cursorCol);
                        int startPrev = START_LINE(editLines, cursorRow-1);
                        int targetPos = fitWidth(font, width, line, startPrev, len)-1;
                        System.out.println("width " + width + " target " + targetPos);
                        int aheadPos;
                        while (true) {
                            aheadPos = prevFlexPoint(pos);
                            if (aheadPos < targetPos || pos == -1) { break; }
                            pos = aheadPos;
                        }
                        updateCursor();
                    }
                    break;
                    
                case Canvas.DOWN:
                    if (cursorRow == nEditLines-1) {
                        history.getCurrent().update(String.valueOf(line, 0, len), pos);
                        if (history.move(1)) {
                            updateFromHistEntry(history.getCurrent());
                            doChanged(-1);
                            needUpdateResult = true;
                        }
                    } else {
                        int width = font.charsWidth(line, START_LINE(editLines, cursorRow), cursorCol);
                        int startNext = editLines[cursorRow];
                        int targetPos = pos == -1 ? editLines[0] : fitWidth(font, width, line, startNext, len)-1;
                        int aheadPos;
                        while (true) {
                            aheadPos = nextFlexPoint(pos);
                            if (aheadPos > targetPos || pos == len-1) { break; }
                            pos = aheadPos;
                        }
                        updateCursor();
                    } 
                    break;
                    
                case Canvas.FIRE:
                    String str = String.valueOf(line, 0, len);
                    history.enter(str);
                    updateFromHistEntry(history.getCurrent());
                    doChanged(-1);
                    needUpdateResult = true;
                    updateHistory();
                    repaint();
                    break;
                }
            } else { //if (key == KEY_CLEAR || key == KEY_END || key == KEY_POWER) { //delete
                if (!clearedKeypad) {
                    delFromLine();
                    doChanged(pos);
                }
            }
        }
        KeyState.repaint(this);
    }

    void updateFromHistEntry(HistEntry entry) {
        pos = entry.pos;
        String str = entry.edited;
        len = str.length();
        str.getChars(0, len, line, 0);
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

    void saveOnExit() {
        String str = String.valueOf(line, 0, len);
        HistEntry entry = new HistEntry(str, 0, false);
        entry.write(C.rs.out);
        C.rs.write(2);        
    }
}
