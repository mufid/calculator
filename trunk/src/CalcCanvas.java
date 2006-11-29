#define START_LINE(lines, n) (((n)==0)?0:lines[n-1])

import javax.microedition.lcdui.*;
import java.util.*;
import java.io.*;

class CalcCanvas extends Canvas /* implements Runnable */ {
    static final int 
        KEY_SOFT1 =  -6,
        KEY_SOFT2 =  -7,
        KEY_CLEAR =  -8, 
        KEY_END   = -11, 
        KEY_POWER = -12;
    private static final String arityParens[] = {"", "()", "(,)", "(,,)"};
    private static final String params[] = {"(x)", "(x,y)", "(x,y,z)"};

    Expr parser = new Expr();
    History history; 

    int screenW, screenH;

    int cursorRow, cursorCol;
    int cursorX, cursorY, cursorW = 1, cursorH;


    Result result = new Result();

    Font font, historyFont;
    int lineHeight;

    char line[] = new char[256];
    int len = 0, pos = -1;
    int lastInsertLen = 0;
    int editLines[];
    int nEditLines = 0;
    int maxEditLines;
    int editH;

    static final int RESULT = 0, EDIT = 1, HISTORY = 2, N_ZONES = 3;
    static final int bgCol[]     = { 0x0,      0xffffff, 0xe0e0e0};
    static final int fgCol[]     = { 0xffffff, 0x0     , 0x0};
    static final int borderCol[] = { 0xffffff, 0x0,      0x808080};
    static final int BACKGR = 0xe0e0e0;
    static final int spaceSide = 1, spaceTop = 1, spaceBot = 1, 
        spaceEdit = 2, spaceHist = 2;
    
    int Y[] = new int[N_ZONES];
    static final int clientX = spaceSide + 2;
    static int clientW, historyH;

    Image img;
    Graphics gg;

    CalcCanvas() {
        boolean isSmallScreen = getHeight() <= 128;
        //if (isSmallScreen) {
            setFullScreenMode(true);
            //}

        screenW = getWidth();
        screenH = getHeight();

        font        = Font.getFont(0, 0, isSmallScreen ? Font.SIZE_MEDIUM : Font.SIZE_LARGE);
        historyFont = Font.getFont(0, 0, isSmallScreen ? Font.SIZE_SMALL  : Font.SIZE_MEDIUM);
        lineHeight  = font.getHeight(); //+1

        img = Image.createImage(screenW, screenH);
        gg  = img.getGraphics();
        gg.setFont(font);
        
        KeyState.init(screenW, screenH);

        maxEditLines = (screenH - (KeyState.h + spaceTop + spaceEdit + spaceHist + 8)) / lineHeight - 1;
        System.out.println("max edit lines " + maxEditLines);
        editLines = new int[maxEditLines + 1];
        clientW = screenW - 2*clientX;

        history = new History(parser);
        DataInputStream is = C.rs.read(2);
        updateFromHistEntry(is == null ? new HistEntry("1+1", 0, false) : new HistEntry(is));
        if (is == null) {
            history.enter("0.5!*2)^2");
            history.enter("sqrt(3^2+4^2");
            history.enter("sin(pi/2)");
        }
        
        cursorX = 20;
        cursorY = 10;
        cursorH = lineHeight;

        //initFrame(nEditLines);
        doChanged(-1);
        //updateHistory();
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

    private int initFrameHeights[] = new int[N_ZONES];
    private void initFrame(int nLines) {
        Y[RESULT]  = spaceTop + 2;
        Y[EDIT]    = spaceTop + spaceEdit + lineHeight + 6;
        Y[HISTORY] = spaceTop + spaceEdit + spaceHist + lineHeight*(nLines + 1) + 10;
        historyH   = screenH - Y[HISTORY] - spaceBot - 2;

        initFrameHeights[RESULT]  = lineHeight;
        initFrameHeights[EDIT]    = lineHeight*nLines;
        initFrameHeights[HISTORY] = historyH;

        for (int i = 0; i < N_ZONES; ++i) {
            gg.setColor(borderCol[i]);
            gg.drawRect(clientX-2, Y[i]-2, clientW+3, initFrameHeights[i]+3);
            gg.setColor(bgCol[i]);
            gg.drawRect(clientX-1, Y[i]-1, clientW+1, initFrameHeights[i]+1);
        }

        //gg.setColor(0x808080);
        gg.setColor(BACKGR);
        gg.drawRect(0, 0, screenW-1, screenH-1);
        gg.fillRect(1, Y[EDIT] - 2 - spaceEdit, screenW-2, spaceEdit);
        gg.fillRect(1, Y[HISTORY] - 2 - spaceHist, screenW-2, spaceHist);

        //repaint();
    }

    private void updateResult() {
        //Graphics rg = gg[RESULT];
        gg.setColor(bgCol[RESULT]);
        gg.fillRect(clientX, Y[RESULT], clientW, lineHeight);
        
        if (parser.parse(String.valueOf(line, 0, len), result)) {
            String strResult = (result.arity > 0) ? 
                result.name + params[result.arity-1] : format(result.value);
            gg.setColor(fgCol[RESULT]);
            //gg.drawString(strResult, clientX, Y[RESULT], 0);
            gg.drawString(strResult, clientX + clientW, Y[RESULT], Graphics.TOP|Graphics.RIGHT); //Graphics.BOTTOM|Graphics.RIGHT);
        } else {
            if (result.errorPos < len) {
                markError(result.errorPos);
            }
        }
        repaint(clientX, Y[RESULT], clientW, lineHeight);
    }

    static int fitWidth(Font font, int targetWidth, char buf[], int start, int end) {
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

    static int split(Font font, char buf[], int len, int w, int lines[]) {
        return split(font, buf, len, w, lines, 0);
    }

    static int split(Font font, char buf[], int len, int w, 
              int lines[], int changeLine) {
        int end = START_LINE(lines, changeLine);
        int i;
        for (i = changeLine; i < lines.length && end < len; ++i) {
            lines[i] = end = fitWidth(font, w, buf, end, len);            
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
        nEditLines = split(font, line, len, clientW, editLines, changeLine);
        if (nEditLines > maxEditLines) {
            pos = changePos;
            delFromLine(pos, lastInsertLen);
            nEditLines = maxEditLines;
            return;
        }
        if (oldNLines != nEditLines) {
            initFrame(nEditLines);
            updateHistory();
            int y = Y[EDIT]-2-spaceEdit;
            repaint(spaceSide, y, screenW-(spaceSide<<1), Y[HISTORY]-y);
        }
        //System.out.println("pos " + pos + " oldNLines " + oldNLines + " nEditLines " + nEditLines);
        //System.out.println("nEditLines " + nEditLines + "; changeLine " + changeLine);                           

        //Graphics g = gg[EDIT];
        gg.setColor(bgCol[EDIT]);
        gg.fillRect(clientX, Y[EDIT] + changeLine*lineHeight, clientW, (nEditLines - changeLine)*lineHeight);
        repaint(clientX, Y[EDIT] + changeLine*lineHeight, clientW, (nEditLines - changeLine)*lineHeight);
        //repaint(0, 2+changeLine*lineHeight, screenW, (nEditLines - changeLine)*lineHeight);
        
        gg.setColor(fgCol[EDIT]);
        int start = changeLine==0 ? 0 : editLines[changeLine-1];
        for (int i = changeLine, y = Y[EDIT] + changeLine*lineHeight,
                 end = editLines[i]; 
             i < nEditLines; ++i, y+=lineHeight, start = end) {
            end = editLines[i];
            gg.drawChars(line, start, end-start, clientX, y, 0); //Graphics.BOTTOM|Graphics.LEFT);
        }
    }
    
    void markError(int errorPos) {
        //int errorPos = result.errorPos;
        //if (errorPos != -1) {
        int errLine = posToLine(editLines, errorPos);
        int startOfLine = errLine==0 ? 0 : editLines[errLine-1];
        int posInLine = errorPos - startOfLine;
        int w = clientX + font.charsWidth(line, startOfLine, posInLine);
        int y = Y[EDIT] + errLine*lineHeight;
        gg.setColor(0xff0000);
        gg.drawChar(line[errorPos], w, y, 0);
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
        cursorY = Y[EDIT] + cursorRow * lineHeight;
        cursorX = clientX + font.charsWidth(line, start, cursorCol);
        if (cursorX > 0) {
            --cursorX;
        }
        //System.out.println("pos " + pos + " row " + cursorRow + " col " + cursorCol + " x " + cursorX + " y " + cursorY);
        setCursor(true);
    }

    char histBuf[] = new char[256+30];
    int histBufLen;
    int histLines[] = new int[8];
    void updateHistory() {
        gg.setColor(bgCol[HISTORY]);
        gg.fillRect(clientX, Y[HISTORY], clientW, historyH);
        gg.setColor(fgCol[HISTORY]);
        gg.setFont(historyFont);
        int histLineHeight = historyFont.getHeight();
        int y = Y[HISTORY] + histLineHeight / 2;
        int histSize = history.size();
        int yLimit = Y[HISTORY] + historyH - histLineHeight;
        for (int p = 1; p < histSize && y <= yLimit; ++p, y+= histLineHeight/2) {
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
            int nLines = split(historyFont, histBuf, histBufLen, clientW, histLines);
            for (int i = 0, start = 0; i < nLines && y <= yLimit; ++i, y+= histLineHeight) {
                gg.drawChars(histBuf, start, histLines[i]-start, clientX, y, 0);
                start = histLines[i];
            }
        }
        gg.setFont(font);
        repaint(clientX, Y[HISTORY], clientW, historyH);
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
        int n = fitWidth(font, clientW - tailW, formatBuf, 0, baseLen);
        return String.valueOf(formatBuf, 0, n) + String.valueOf(formatBuf, ePos, len - ePos);
    }

    protected void paint(Graphics g) {
        int keypadH = KeyState.getH();
        /*
        if (keypadH == 0 && helpText != null) {
        }
        */
        if (keypadH == 0) {
            g.drawImage(img, 0, 0, 0);
        } else {
            int border = spaceBot+2;
            int h = screenH - keypadH - border;
            g.drawRegion(img, 0, 0, screenW, h, 0,
                         0, 0, 0);
            int historySpace = h - Y[HISTORY];
            if (historySpace > 0) {
                g.drawRegion(img, 0, screenH - border, screenW, border, 0,
                             0, h, 0);
            }
        }
        if (drawCursor) {
            g.setColor(0);
            g.fillRect(cursorX, cursorY, cursorW, cursorH);
        }
        KeyState.paint(g);        
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
        updateResult();
    }
    
    void historyMove(int dir) {
        history.getCurrent().update(String.valueOf(line, 0, len), pos);
        if (history.move(dir)) {
            updateFromHistEntry(history.getCurrent());
            doChanged(-1);
        }        
    }

    void handleAction(int action) {
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
                historyMove(-1);
            } else {
                int width = font.charsWidth(line, editLines[cursorRow-1], cursorCol);
                int startPrev = START_LINE(editLines, cursorRow-1);
                int targetPos = fitWidth(font, width, line, startPrev, len)-1;
                //System.out.println("width " + width + " target " + targetPos);
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
                historyMove(1);
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
            updateHistory();
            break;
            
        case Canvas.GAME_A:
        case Canvas.GAME_C:
            C.self.displayMenu();
            break;
            
        case Canvas.GAME_B:
        case Canvas.GAME_D:
            delFromLine();
            doChanged(pos);
            break;
        default:                    
        }
    }
    
    int menuKey = 0;
    protected void keyPressed(int key) {
        System.out.println("key " + key + "; " + getKeyName(key) + "; action " + getGameAction(key));
        int saveKey = key;
        if (key > 0 && (key < 32 || key > 10000)) {
            //also handles backspace (unicode 8) -> KEY_CLEAR (-8)
            key = -key;
        }
        int keyPos = key > 0 ? getKeyPos(key) : -1;
        if (keyPos < 0 && KeyState.keypad != null) {
            //a not-numeric key was pressed while the keypad was active,
            //disable keypad and ignore key
            KeyState.keypad = null;
            KeyState.repaint(this);
            return;
        }

        if (key > 0) {
            int oldPos = pos;
            lastInsertLen = 0;
            String s;
            if (keyPos >= 0) {
                s = KeyState.handleKey(keyPos);
            } else {
                s = String.valueOf((char)key);
            }
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
            int action = 0;
            try {
                action = getGameAction(key);
            } catch (IllegalArgumentException e) {
            }
            if ((action == 0 || action == Canvas.FIRE) && 
                (key == KEY_SOFT1 || //RI, Nokia, SE
                 key == -21 ||       //Motorola
                 key == -57345 ||    //Qtek
                 key == -202 ||      //LG?
                 key == -4)) {       //Siemens, right soft key
                C.self.displayMenu();
            } else {
                if (action != 0) {
                    handleAction(action);
                } else {
                    delFromLine();
                    doChanged(pos);
                }
            }
        }
        KeyState.repaint(this);
    }

 /* else {
                        try {
                            String name = getKeyName(saveKey).toLowerCase();
                            if ((name.indexOf("soft") != -1 && name.indexOf("1") != -1) ||
                                name.indexOf("left") != -1) {
                                menuKey = key;
                            }
                        } catch (IllegalArgumentException e) {
                        }
                    }
 */

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
