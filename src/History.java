import java.util.Vector;
import java.io.*;

class HistEntry {
    String base, edited;
    double result;
    boolean hasResult;
    int pos;

    HistEntry(String str, double res, boolean hasRes) {
        base = str == null ? "" : str;;
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
    //static RMS rs = C.rs;
    static double ans = 0;

    private Expr parser;
    private int historyPos;
    private Vector history; // = new Vector();
    
    int posMaxSeq = -1;
    int maxSeq = 0;

    History(Expr iniParser) {
        parser = iniParser;
        historyPos = 0;
        
        Vector v = new Vector(C.RS_MAX_HIST);
        DataInputStream is;
        int recId = C.RS_HIST_START, seq = 0;
        while (recId < C.RS_MAX_HIST+C.RS_HIST_START && (is=C.rs.read(recId)) != null) {
            try {
                seq = is.readInt();
            } catch (IOException e) {
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

    int size() { return history.size(); }

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
        //((HistEntry) history.elementAt(historyPos)).update(parent.line, parent.len, parent.pos);
        HistEntry entry = (HistEntry) history.elementAt(newPos);
        historyPos = newPos;
        //getFrom(entry);
        return true;
    }
    
    /*
    private void getFrom(HistEntry entry) {
        parent.pos = entry.pos;
        String str = entry.edited;
        str.getChars(0, str.length(), parent.line, 0);
        parent.len = str.length();
    }
    */

    /*
    void clear() {
        history.setSize(1);
        historyPos = 0;
        posMaxSeq = -1;
        maxSeq = 0;
        for (int i = RS_HIST_START; i < C.RS_MAX_HIST+RS_HIST_START; ++i) {
            C.rs.write(i);
        }
    }
    */
    private Result result = new Result();
    void enter(String str) {
        if (parser.parse(str, result)) {
            if (result.name != null) {
                parser.define(result);
            }
        }

        boolean hasValue = result.hasValue();
        if (hasValue) {
            ans = result.value;
        }
        ((HistEntry)history.elementAt(historyPos)).flush();
        if (str.length() > 0) {
            HistEntry newEntry = new HistEntry(str, result.value, hasValue);
            try {
                C.rs.out.writeInt(++maxSeq);
            } catch (IOException e) {
            }
            newEntry.write(C.rs.out);
            ++posMaxSeq;
            if (posMaxSeq >= C.RS_MAX_HIST) {
                posMaxSeq = 0;
            }
            int recId = posMaxSeq + C.RS_HIST_START;
            C.rs.write(recId);
            history.insertElementAt(newEntry, 1);
            if (history.size() > C.RS_MAX_HIST+1) {
                history.setSize(C.RS_MAX_HIST+1);
            }
        }
        historyPos = 0;
        //getFrom((HistEntry)history.elementAt(historyPos));
    }
}
