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

import java.util.*;
import java.io.*;

import org.javia.lib.*;
import org.javia.eval.SymbolTable;
import org.javia.eval.FunParser;
import org.javia.eval.Fun;
import org.javia.eval.ArityException;

class History {
    static double ans = 0;

    private int historyPos;
    private Vector history;
    private SymbolTable symbols;
    
    int posMaxSeq = -1;
    int maxSeq = 0;

    History(SymbolTable symbols) {
        this.symbols = symbols;
        historyPos = 0;
        
        Vector v = new Vector(Calc.RS_MAX_HIST);
        DataInputStream is;
        int recId = Calc.RS_HIST_START, seq = 0;
        while (recId < Calc.RS_MAX_HIST+Calc.RS_HIST_START && (is=Calc.rs.readIS(recId)) != null) {
            try {
                seq = is.readInt();
            } catch (IOException e) {
                Log.log(e);
            }
            if (seq > maxSeq) {
                posMaxSeq = v.size();
                maxSeq = seq;
            }
            v.addElement(new HistEntry(is));
            ++recId;
        }
        history = new Vector(v.size() + 1);
        addElement(new HistEntry(""));
        int n = v.size();
        for (int i = posMaxSeq; i >= 0; --i) {
            addElement(v.elementAt(i));
        }
        for (int i = n-1; i > posMaxSeq; --i) {
            addElement(v.elementAt(i));
        }
        HistEntry entry;
        for (int i = 1; i <= n; ++i) {
            entry = get(i);
            /*
            if (entry.hasResult) {
                ans = entry.result;
                break;
            }
            */
        }
    }
    
    private void addElement(Object o) {
        history.addElement(o);
    }

    int size() { 
        return history.size(); 
    }

    HistEntry get(int p) { 
        return (HistEntry) history.elementAt(p);
    }
    
    HistEntry getCurrent() {
        return get(historyPos);
    }

    boolean move(int delta) {
        int newPos = historyPos + delta;
        if (newPos < 0 || newPos >= history.size()) {
            return false;
        }
        //HistEntry entry = (HistEntry) history.elementAt(newPos);
        historyPos = newPos;
        return true;
    }

    DataOut dataOut = new DataOut();
    void enter(String str) {
        Fun fun = FunParser.compile(str, symbols);
        if (fun != null && fun.arity() == 0) {
            try {
                ans = fun.eval();
            } catch (ArityException e) {
                throw new Error(""+e);
            }
        }
        ((HistEntry)history.elementAt(historyPos)).flush();
        if (str.length() > 0) {
            HistEntry newEntry = new HistEntry(str);
            /*
            try {
                dataOut.writeInt(++maxSeq);
            } catch (IOException e) {
            }
            newEntry.write(dataOut);
            ++posMaxSeq;
            if (posMaxSeq >= Calc.RS_MAX_HIST) {
                posMaxSeq = 0;
            }
            int recId = posMaxSeq + Calc.RS_HIST_START;
            Calc.rs.write(recId, dataOut.getBytesAndReset());
            */
            history.insertElementAt(newEntry, 1);
            if (history.size() > Calc.RS_MAX_HIST+1) {
                history.setSize(Calc.RS_MAX_HIST+1);
            }
        }
        historyPos = 0;
    }
}
