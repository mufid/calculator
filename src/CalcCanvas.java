import javax.microedition.lcdui.*;
import java.util.*;

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

    int cursorX, cursorY, cursorW = 2, cursorH;


    Result result = new Result();
    boolean needUpdateResult;

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

    CalcCanvas() {
        if (getHeight() <= 128) {
            setFullScreenMode(true);
        }
        w = getWidth();
        h = getHeight();

        history = new History(this);
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
    }

    void threadRun() {
        while (true) {
            setCursor(!drawCursor);
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
            }
        }
    }

    public void run() {
        System.out.println("serially");
        if (needUpdateResult) {
            needUpdateResult = false;
            
            int resultY = nEditLines * lineHeight + 2;
            int ry = resultY+2, rh = height[RESULT]-3;
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
    synchronized void setCursor(boolean setOn) {
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

    protected void paint(Graphics g) {
        System.out.println("paint");
        KeyState.paint(g);
        //System.out.println("historyH "+historyH+" keypadH "+keypadH+" editH "+editH+" w "+w+" resultY "+resultY);
        int editH = nEditLines * lineHeight + 2;
        int keypadH = KeyState.getH();
        //System.out.println("pos " + pos + "; nLines " + nEditLines + "; cX " + cursorX + "; cY " + cursorY);
        KeyState.paint(g);
        g.drawRegion(img[EDIT], 0, 0, w, editH, 0,
                     0, 0, 0);
        g.drawRegion(img[HISTORY], 0, 0, w, h-editH-height[RESULT]-keypadH, 0,
                     0, editH+height[RESULT], 0);
        if (drawCursor) {
            g.setColor(0);
            g.fillRect(cursorX, cursorY, cursorW, cursorH);
        }
        g.drawImage(img[RESULT], 0, editH, 0);
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
    }

    protected void keyPressed(int key) {
        System.out.println("key");
        int oldPos = pos;
        int keyPos = getKeyPos(key);
        if (keyPos >= 0) {
            String s = KeyState.handleKey(keyPos);
            if (s != null) {
                if (pos == -1 && s.length() == 1 &&
                    "+*/%^!".indexOf(s.charAt(0)) != -1) {
                    insertIntoLine("ans");
                }
                insertIntoLine(s);
                Symbol symbol = Expr.symbols.get(s);
                if (symbol != null) { // && symbol.arity > 0) {
                    String parens = arityParens[symbol.arity];
                    int parensLen = parens.length();
                    if (parensLen > 0) {
                        if (pos == len-1) {
                            insertIntoLine(parens);
                            pos -= parensLen - 1;
                        } else {
                            insertIntoLine("(");
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
                    if (history.move(-1)) {
                        doChanged(0);
                        needUpdateResult = true;
                    }
                    break;
                    
                case Canvas.DOWN:
                    if (history.move(1)) {
                        doChanged(0);
                        needUpdateResult = true;
                    }
                    break;
                    
                case Canvas.FIRE:
                    String str = String.valueOf(line, 0, len);
                    if (parser.parse(str, result)) {
                        if (result.name != null) {
                            parser.define(result);
                        }
                    }
                    history.enter(str, result);
                    doChanged(0);
                    needUpdateResult = true;
                    updateHistory();
                    repaint();
                    break;
                }
            } else if (key == KEY_CLEAR || key == KEY_END || key == KEY_POWER) { //delete
                if (!clearedKeypad) {
                    delFromLine();
                    doChanged(pos);
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
