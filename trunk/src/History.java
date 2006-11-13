import java.util.Vector;
import java.io.*;
import javax.microedition.rms.*;

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

    HistEntry(DataInput in) {
        try {
            result = in.readDouble();
            hasResult = in.readBoolean();
            base = in.readUTF();
        } catch (IOException e) {
            throw new Error(e.toString());
        }
        flush();
    }

    void write(DataOutput out) {
        try {
            out.writeDouble(result);
            out.writeBoolean(hasResult);
            out.writeUTF(base);
        } catch (IOException e) {
            throw new Error(e.toString());
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
    private static final int MAX_HIST = 32; //64;
    private CalcCanvas parent;
    private int historyPos;
    private Vector history; // = new Vector();
    private RecordStore rs;
    int posMaxSeq = -1;
    int maxSeq = 0;
    double ans = 0;

    History(CalcCanvas calc) {
        parent = calc;
        historyPos = 0;
        
        Vector v = new Vector(MAX_HIST);

        try {    
            byte buf[] = new byte[300];
            rs = RecordStore.openRecordStore("calc", true);
            if (rs.getNextRecordID() == 1) {
                //init rs
                buf[0] = 0;
                rs.addRecord(buf, 0, 1); //rec 1
            }
            
            int recSize;
            int recId = 2;
            DataInputStream is;
            int seq;
            while (true) {
                recSize = rs.getRecord(recId, buf, 0);
                if (recSize == 0) { break; }
                is = new DataInputStream(new ByteArrayInputStream(buf, 0, recSize));
                seq = is.readInt();
                if (seq > maxSeq) {
                    posMaxSeq = v.size();
                    maxSeq = seq;
                }
                v.addElement(new HistEntry(is));
                ++recId;
            }
        } catch (InvalidRecordIDException e) { //to get out of the while()
        } catch (Exception e) { //IOException, RecordStoreException
            System.out.println("unexpected RS " + e);
            throw new Error(e.toString());
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

    /*
    String getBase(int p) {
        return ((HistEntry) history.elementAt(p)).base;
    }
    */

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

    void clear() {
        history.setSize(1);
        historyPos = 0;
        posMaxSeq = -1;
        maxSeq = 0;
        try {
            int n = rs.getNumRecords();
            for (int i = 2; i <= n; ++i) {
                rs.setRecord(i, null, 0, 0);
            }
        } catch (RecordStoreException e) {
            throw new Error(e.toString());
        }
    }

    private ByteArrayOutputStream bos = new ByteArrayOutputStream();
    void enter(String str, double result, boolean hasResult) {
        ((HistEntry)history.elementAt(historyPos)).flush();
        if (str.length() > 0) {
            HistEntry newEntry = new HistEntry(str, result, hasResult);
            bos.reset();
            DataOutputStream os = new DataOutputStream(bos);
            try {
                os.writeInt(++maxSeq);
                newEntry.write(os);
                ++posMaxSeq;
                if (posMaxSeq == MAX_HIST) {
                    posMaxSeq = 0;
                }
                int recId = posMaxSeq + 2;
                byte data[] = bos.toByteArray();
                System.out.println("id " + recId + "; next " + rs.getNextRecordID());
                if (rs.getNextRecordID() == recId) {
                    rs.addRecord(data, 0, data.length);
                } else {
                    rs.setRecord(recId, data, 0, data.length);
                }
            } catch (Exception e) {
                throw new Error(e.toString());
            }
            history.insertElementAt(newEntry, 1);
        }
        historyPos = 0;
        getFrom((HistEntry)history.elementAt(historyPos));
    }
}
