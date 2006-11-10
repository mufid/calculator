import java.util.Vector;
class HistEntry {
    String base, edited;
    double result;
    boolean hasResult;
    int pos;

    HistEntry(char line[], int len, double res, boolean hasRes) {
        base = len == 0 ? "" : new String(line, 0, len);
        result = res;
        hasResult = hasRes;
        flush();
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
    private CalcCanvas parent;
    private int historyPos;
    private Vector history = new Vector();

    History(CalcCanvas calc) {
        parent = calc;
        history.addElement(new HistEntry(null, 0, 0, false));
    }
    
    int size() { return history.size(); }

    HistEntry get(int p) { 
        return (HistEntry) history.elementAt(p);
    }

    String getBase(int p) {
        return ((HistEntry) history.elementAt(p)).base;
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

    void enter() {
        ((HistEntry)history.elementAt(historyPos)).flush();
        HistEntry newEntry = new HistEntry(parent.line, parent.len, parent.result, parent.hasResult);
        history.insertElementAt(newEntry, 1);
        historyPos = 0;
        getFrom((HistEntry)history.elementAt(historyPos));
    }
}
