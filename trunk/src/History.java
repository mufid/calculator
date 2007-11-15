// Copyright (c) 2006-2007 Mihai Preda, Carlo Teubner.
// Available under the MIT License (see COPYING).

import java.util.*;
import java.io.*;

import org.javia.lib.*;

class HistEntry {
    String base, edited;
    double result;
    boolean hasResult;
    int pos;

    HistEntry(String str, double res, boolean hasRes) {
        base = str == null ? "" : str;
        result = res;
        hasResult = hasRes;
        flush();
    }

    HistEntry(DataInputStream in) {
        try {
            result = in.readDouble();
            hasResult = in.readBoolean();
            base = in.readUTF();
        } catch (IOException e) {
            Log.log(e);
            //throw new Error(e.toString());
        }
        flush();
    }

    void write(DataOutputStream out) {
        try {
            out.writeDouble(result);
            out.writeBoolean(hasResult);
            out.writeUTF(base);
        } catch (IOException e) {
            Log.log(e);
            //throw new Error(e.toString());
        }
    }

    void flush() {
        edited = base;
        pos = base.length() - 1;
    }

    void update(String str, int iniPos) {
        pos = iniPos;
        edited = str;
    }
}

class History {
    static double ans = 0;

    private Compiler compiler;
    private int historyPos;
    private Vector history;
    
    int posMaxSeq = -1;
    int maxSeq = 0;

    History(Compiler iniCompiler) {
        compiler = iniCompiler;
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
        addElement(new HistEntry(null, 0, false));
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
            if (entry.hasResult) {
                ans = entry.result;
                break;
            }
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
    void enter(char[] str, int len, String asString) {
        compiler.compile(str, len);
        Result res = Compiler.result;

        boolean hasValue = res.hasValue();
        if (hasValue)
            ans = res.function.evaluate();

        if (res.errorStart == -1 && res.definedSymbol != -1)
            Variables.persistDefine(res, ans);

        ((HistEntry)history.elementAt(historyPos)).flush();
        if (len > 0) {
            HistEntry newEntry = new HistEntry(asString, ans, hasValue);
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
            history.insertElementAt(newEntry, 1);
            if (history.size() > Calc.RS_MAX_HIST+1) {
                history.setSize(Calc.RS_MAX_HIST+1);
            }
        }
        historyPos = 0;
    }
}
