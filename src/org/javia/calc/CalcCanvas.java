/*
 * Copyright (C) 2006-2007 Mihai Preda & Carlo Teubner.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.javia.calc;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Canvas;

import java.io.*;

import org.javia.lib.midp.ImageCanvas;
import org.javia.lib.Log;
import org.javia.lib.DataOut;

import org.javia.eval.Compiler;
import org.javia.eval.SymbolTable;
import org.javia.eval.Function;
import org.javia.eval.Util;
import org.javia.eval.ArityException;

class CalcCanvas extends ImageCanvas {
    static final int 
        KEY_SOFT1 =  -6,
        KEY_SOFT2 =  -7,
        KEY_CLEAR =  -8, 
        KEY_END   = -11, 
        KEY_POWER = -12;
    private static final String arityParens[] = {"", "()", "(,)", "(,,)", "(,,,)", "(,,,,)"};
    //private static final String params[] = {"(x)", "(x,y)", "(x,y,z)" /* user-def fns have <= 3 params */ };

    private static final String[][] plotParamHelp = {
        { "function of x", "min x-value", "max x-value" },
        { "function of x,y", "min x-value", "max x-value", "min y-value", "max y-value" },
        { "function x(t)", "function y(t)", "min t-value", "max t-value" }
    };

    private static final String[] initHistory = {
        "0.5!*2)^2",
        "sqrt(3^2+4^2",
        "sin({PI}/2)"
    };

    History history; 

    int screenW, screenH;
    int cursorX, cursorY;
    int cursorW = 1, cursorH;

    Font font, historyFont;
    int lineHeight;

    EditBox historyWrap;
    CursorBox editor;

    int nEditLines;
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

    SymbolTable symbols = new SymbolTable();
    Compiler compiler = new Compiler();

    CalcCanvas() {
        screenW = getWidth();
        screenH = getHeight();

        font        = Font.getFont(0, 0, Font.SIZE_LARGE);
        historyFont = Font.getFont(0, 0, Font.SIZE_MEDIUM);
        lineHeight  = font.getHeight(); //+1

        Calc.cfg.initPiSymbol(new Font[] { font, historyFont });

        screenGraphics.setFont(font);

        KeyState.init(screenW, screenH, font);

        maxEditLines = (screenH - (KeyState.h + spaceTop + spaceEdit + spaceHist + 8)) / lineHeight - 1;
        clientW = screenW - 2*clientX;
        editor      = new CursorBox(clientW, font, maxEditLines);
        historyWrap = new EditBox(clientW, historyFont, 4);
        history     = new History(compiler, symbols);
        DataInputStream is = Calc.rs.readIS(Calc.RS_CURRENT);
        updateFromHistEntry(is == null ? new HistEntry("1+1") : new HistEntry(is));
        if (is == null) {
            for (int i = 0; i < initHistory.length; ++i) {
                String str = initHistory[i];
                int piIdx = str.indexOf("{PI}");
                if (piIdx != -1) {
                    str = str.substring(0, piIdx) + Calc.cfg.piString + str.substring(piIdx + 4, str.length());
                }
                history.enter(str);
            }
        }

        //Variables.load();

        cursorX = 20;
        cursorY = 10;
        cursorH = lineHeight;

        //initFrame(nEditLines);
        doChanged();
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
            screenGraphics.setColor(borderCol[i]);
            screenGraphics.drawRect(clientX-2, Y[i]-2, clientW+3, initFrameHeights[i]+3);
            screenGraphics.setColor(bgCol[i]);
            screenGraphics.drawRect(clientX-1, Y[i]-1, clientW+1, initFrameHeights[i]+1);
        }

        screenGraphics.setColor(BACKGR);
        screenGraphics.drawRect(0, 0, screenW-1, screenH-1);
        screenGraphics.fillRect(1, Y[EDIT] - 2 - spaceEdit, screenW-2, spaceEdit);
        screenGraphics.fillRect(1, Y[HISTORY] - 2 - spaceHist, screenW-2, spaceHist);
    }

    void updateResult() {
        Function fun = compiler.compile(editor.toString(), symbols);
        String strResult = null;
        if (fun != null) {
            try {
                strResult = format(fun.eval());
            } catch (ArityException e) {
                strResult = "function";
            }
            drawResultString(strResult);
        }
        drawResultString(null);
    }

    private boolean resultEmpty = false;
    private void drawResultString(String str) {
        boolean changed = false;
        if (!resultEmpty) {
            screenGraphics.setColor(bgCol[RESULT]);
            screenGraphics.fillRect(clientX, Y[RESULT], clientW, lineHeight);
            changed = true;
        }
        if (str != null) {
            screenGraphics.setColor(fgCol[RESULT]);
            screenGraphics.drawString(str, clientX + clientW, Y[RESULT], Graphics.TOP | Graphics.RIGHT);
            resultEmpty = false;
            changed = true;
        } else
            resultEmpty = true;
        if (changed)
            repaint(clientX, Y[RESULT], clientW, lineHeight);
    }

    void editChanged() {
        int oldNLines = nEditLines;
        nEditLines = editor.nLines();
        if (oldNLines != nEditLines) {
            initFrame(nEditLines);
            updateHistory();
            int y = Y[EDIT]-2-spaceEdit;
            repaint(spaceSide, y, screenW-(spaceSide<<1), Y[HISTORY]-y);
        }
        screenGraphics.setColor(bgCol[EDIT]);
        screenGraphics.fillRect(clientX, Y[EDIT], clientW, nEditLines*lineHeight);
        repaint(clientX, Y[EDIT], clientW, nEditLines*lineHeight);
        screenGraphics.setColor(fgCol[EDIT]);
        editor.drawLines(screenGraphics, clientX, Y[EDIT]);
    }

    void markError() {
        /*
        final int start = Compiler.result.errorStart, end = Compiler.result.errorEnd;
        int errLine, startOfLine, w, y, w2, y2,
            minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE,
            maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        screenGraphics.setColor(0xFF0000);
        for (int epos = start; epos <= end; ++epos) {
            errLine = posToLine(editLines, epos);
            startOfLine = errLine==0 ? 0 : editLines[errLine-1];
            w = clientX + font.charsWidth(line, startOfLine, epos - startOfLine);
            y = Y[EDIT] + errLine*lineHeight;
            screenGraphics.drawChar(line[epos], w, y, 0);
            w2 = w + font.charWidth(line[epos]);
            y2 = y + lineHeight;
            if (w < minX) minX = w;
            if (y < minY) minY = y;
            if (w2 > maxX) maxX = w2;
            if (y2 > maxY) maxY = y2;
        }
        repaint(minX, minY, maxX - minX, maxY - minY);
        */
    }

    boolean drawCursor = true;
    void setCursor(boolean setOn) {
        drawCursor = setOn;
        repaint(cursorX, cursorY, cursorW, cursorH);
    }

    int outRowX[] = new int[2];
    void updateCursor() {
        setCursor(drawCursor);

        editor.getCursorRowX(outRowX);
        cursorY = Y[EDIT] + outRowX[0] * lineHeight;
        cursorX = clientX + outRowX[1];

        if (cursorX > 0) {
            --cursorX;
        }
        //Log.log("pos " + pos + " row " + cursorRow + " col " + cursorCol + " x " + cursorX + " y " + cursorY);
        setCursor(true);

        /*
        final int[] cmdSlot = Lexer.getPlotCommandAndSlot(new String(line, 0, len), pos + 1);
        if (cmdSlot[0] != -1) {
            String help = null;
            if (cmdSlot[1] != -1) {
                String[] helps = plotParamHelp[cmdSlot[0] - VM.FIRST_PLOT_COMMAND];
                if (cmdSlot[1] < helps.length)
                    help = helps[cmdSlot[1]];
            }
            drawResultString(help);
        }
        */
    }

    int histLines[] = new int[8];
    void updateHistory() {
        screenGraphics.setColor(bgCol[HISTORY]);
        screenGraphics.fillRect(clientX, Y[HISTORY], clientW, historyH);
        screenGraphics.setColor(fgCol[HISTORY]);
        screenGraphics.setFont(historyFont);
        int histLineHeight = historyFont.getHeight();
        int y = Y[HISTORY] + histLineHeight / 2;
        int histSize = history.size();
        int yLimit = Y[HISTORY] + historyH - histLineHeight;
        for (int p = 1; p < histSize && y <= yLimit; ++p, y+= histLineHeight/2) {
            HistEntry entry = history.get(p);
                //String base   = entry.base;
            String result = /*entry.hasResult ? format(entry.result) :*/ "";
            String txt = entry.base;
            if (result.length() > 0) {
                txt += " = " + result;
            }
            historyWrap.set(txt);
            int saveY = y;
            y += historyWrap.nLines() * histLineHeight;
            if (y <= yLimit) {
                historyWrap.drawLines(screenGraphics, clientX, saveY);
            }
        }
        screenGraphics.setFont(font);
        repaint(clientX, Y[HISTORY], clientW, historyH);
    }

    String format(double v) {
        return Util.doubleToString(v, Calc.cfg.roundingDigits);
        /*
        int ePos = str.lastIndexOf('E');
        if (ePos == -1) {
            int n = wrap.fitWidth(str, clientW);
            return str.substring(0, n);
            //return str;
        } else {
            String tail = str.substring(ePos);
            str = str.substring(0, ePos);
            int tailW   = font.stringWidth(tail);
            int n = wrap.fitWidth(str, clientW - tailW);
            return str.substring(0, n) + tail;
        }
        */
    }

    protected void paint(Graphics g) {
        int keypadH = KeyState.getH();
        g.drawImage(screenImage, 0, 0, 0);
        /*
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
        */
        if (drawCursor) {
            g.setColor(0);
            g.fillRect(cursorX, cursorY, cursorW, cursorH);
        }
        KeyState.paint(g);        
    }
    
    private static final boolean isLetter(char c) {
        return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z');
    }

    //private int prevFlexPoint(int pos) {
    //    return pos < 0 ? pos : pos - 1;
        /*
        if (pos < 0) {
            return pos;
        }
        if (line[pos] == '=' && pos >= 1 && line[pos-1] == ':') {
            return pos - 2;
        }
        int p = pos;
        if (p >= 1 && line[p] == '(') {
          
            do { --p; } while (p >= 0 && isLetter(line[p]));
            final int sym = Lexer.getSymbol(new String(line, p + 1, pos));
            if (Lexer.isBuiltinFunction(sym) || Lexer.isPlotCommand(sym) ||
                Lexer.isVariable(sym) && Variables.isFunction(sym) && p > -1)
            { pos = p; }
            else
          
            --pos;
            return pos;
        }
        if (p >= 0 && isLetter(line[p])) {
            do { --p; } while (p >= 0 && isLetter(line[p]));
            return p;
        }
        return pos - 1;
        */
    //}

    /*
    private int nextFlexPoint(int pos) {
        return pos >= line.length() - 1 ? pos : pos + 1;
      
        if (pos >= len - 1) {
            return pos;
        }
        if (line[pos+1] == ':' && pos <= len-3 && line[pos+2] == '=') {
            return pos + 2;
        }
        int p = ++pos;
        if (isLetter(line[pos])) {
            while (pos < len && isLetter(line[pos]))
                ++pos;
            --pos;
          
            if (pos + 1 < len && line[pos + 1] == '(') {
                final int sym = Lexer.getSymbol(new String(line, p, pos + 1));
                if (Lexer.isBuiltinFunction(sym) || Lexer.isPlotCommand(sym) ||
                    Lexer.isVariable(sym) && Variables.isFunction(sym) && pos > 0)
                { ++pos; }
            }
          
        }
        return pos;

    }
        */

    /*
    void delFromLine(int p, int size) {
        buffer.delete(p+1, p+1+size);

        int p2 = p + size + 1;
        System.arraycopy(line, p2, line, p + 1, len - p2);
        len -= size;
        lineStr = null;
    }
    */

    protected void keyRepeated(int key) {
        keyPressed(key);
    }
    
    private void doChanged() {
        editChanged();
        updateCursor();
        updateResult();
    }
    
    void historyMove(int dir) {
        history.getCurrent().update(editor.toString(), editor.cursorPos());
        if (history.move(dir)) {
            updateFromHistEntry(history.getCurrent());
            doChanged();
        }        
    }

    void handleAction(int action) {
        switch (action) {
        case Canvas.LEFT:
            editor.moveLeft();
            updateCursor();
            break;
            
        case Canvas.RIGHT:
            editor.moveRight();
            updateCursor();
            break;
            
        case Canvas.UP:
            if (editor.moveUp()) {
                updateCursor();
            } else {
                historyMove(-1);                
            }
            break;
            
        case Canvas.DOWN:
            if (editor.moveDown()) {
                updateCursor();
            } else {
                historyMove(1);
            }
            break;

        case Canvas.FIRE:
            history.enter(editor.toString());
            /*
            Result res = Compiler.result;
            if (res.errorStart == -1 && res.plotCommand != -1)
                Calc.self.plotCanvas.plot(res);
            */
            updateFromHistEntry(history.getCurrent());
            doChanged();
            updateHistory();
            break;
            
        case Canvas.GAME_A:
        case Canvas.GAME_C:
            Calc.self.displayMenu();
            break;
            
        case Canvas.GAME_B:
        case Canvas.GAME_D:
            editor.delete(1);
            doChanged();
            break;
        default:                    
        }
    }
    
    int menuKey = 0;
    protected void keyPressed(int key) {
        //Log.log("key " + key + "; " + getKeyName(key) + "; action " + getGameAction(key));
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
            final String s = (keyPos >= 0) ?
                KeyState.handleKey(keyPos) :
                String.valueOf((char)key);
            
            if (s != null) {
                /*
                if (editor.size() == 0 && s.length() == 1 && isOperator) {
                    editor.insert("ans");
                }
                */
                editor.insert(s);

                /*
                if (sym != -1) {
                    int arity = Lexer.isVariable(sym)
                    ? (Variables.isFunction(sym)
                       ? Variables.getFunction(sym).arity()
                       : 0)
                    : Lexer.getBuiltinArity(sym);
                    if (!Lexer.matchesPlotArity(arity, new String(line, 0, oldPos + 1))) {
                        if (sym == VM.MAP && Calc.cfg.aspectRatio1)
                            arity = 4;
                        String parens = arityParens[arity];
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
                }
                */
                doChanged();
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
                Calc.self.displayMenu();
            } else if (action == 0 && key == KEY_END) {
                Calc.self.exit();
            } else {
                if (action != 0) {
                    handleAction(action);
                } else {
                    editor.delete();
                    doChanged();
                }
            }
        }
        KeyState.repaint(this);
    }

    void updateFromHistEntry(HistEntry entry) {
        editor.set(entry.edited, entry.pos);
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

    DataOut dataOut = new DataOut();
    void saveOnExit() {
        new HistEntry(editor.toString()).write(dataOut);
        Calc.rs.write(Calc.RS_CURRENT, dataOut.getBytesAndReset());
    }
}
