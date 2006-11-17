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

    void update(char line[], int len, int iniPos) {
        pos = iniPos;
        edited = new String(line, 0, len);
    }
}

class History {
    static final int MAX_HIST = 4; //32;
    static RMS rs = new RMS("calc");
    static double ans = 0;

    private CalcCanvas parent;
    private int historyPos;
    private Vector history; // = new Vector();
    
    int posMaxSeq = -1;
    int maxSeq = 0;

    History(CalcCanvas calc) {
        parent = calc;
        historyPos = 0;
        
        Vector v = new Vector(MAX_HIST);
        DataInputStream is;
        int recId = 2, seq = 0;
        while (recId < MAX_HIST+2 && (is=rs.read(recId)) != null) {
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
        history.addElement(new HistEntry(null, 0, false));
        int n = v.size();
        for (int i = posMaxSeq; i >= 0; --i) {
            history.addElement(v.elementAt(i));
        }
        for (int i = n-1; i > posMaxSeq; --i) {
            history.addElement(v.elementAt(i));
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
    
    int size() { return history.size(); }

    HistEntry get(int p) { 
        return (HistEntry) history.elementAt(p);
    }

    boolean move(int delta) {
        int newPos = historyPos + delta;
        if (newPos < 0 || newPos >= history.size()) {
            return false;
        }
        ((HistEntry) history.elementAt(historyPos)).update(parent.line, parent.len, parent.pos);
        HistEntry entry = (HistEntry) history.elementAt(newPos);
        historyPos = newPos;
        getFrom(entry);
        return true;
    }
    
    private void getFrom(HistEntry entry) {
        parent.pos = entry.pos;
        String str = entry.edited;
        str.getChars(0, str.length(), parent.line, 0);
        parent.len = str.length();
    }

    /*
    void clear() {
        history.setSize(1);
        historyPos = 0;
        posMaxSeq = -1;
        maxSeq = 0;
        for (int i = 2; i < MAX_HIST+2; ++i) {
            rs.write(i);
        }
    }
    */

    void enter(String str, Result result) {
        boolean hasValue = result.hasValue();
        if (hasValue) {
            ans = result.value;
        }
        ((HistEntry)history.elementAt(historyPos)).flush();
        if (str.length() > 0) {
            HistEntry newEntry = new HistEntry(str, result.value, hasValue);
            try {
                rs.os.writeInt(++maxSeq);
            } catch (IOException e) {
            }
            newEntry.write(rs.os);
            ++posMaxSeq;
            if (posMaxSeq >= MAX_HIST) {
                posMaxSeq = 0;
            }
            int recId = posMaxSeq + 2;
            rs.write(recId);
            history.insertElementAt(newEntry, 1);
            if (history.size() > MAX_HIST+1) {
                history.setSize(MAX_HIST+1);
            }
        }
        historyPos = 0;
        getFrom((HistEntry)history.elementAt(historyPos));
    }
}
