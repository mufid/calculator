import java.util.*;
import java.io.*;
import javax.microedition.lcdui.*;

/*
  The fact that Config extends Canvas is the result of a 'class merge',
  in order to reduce the number of classes. 
  Otherwise these two, the config and the canvas, have nothing to do with each other.
  The canvas is used exculsively for getActionCode(), and is never displayed.
*/

class Config extends Canvas {
    Hashtable ht = new Hashtable();
    RMS rs;
    int recId;

    Config(RMS rs, int recId) {
        this.rs = rs;
        this.recId = recId;
        DataInputStream is = rs.read(recId);
        if (is != null) {
            String s1, s2;
            int sz = 0;
            try {
                sz = is.readInt();
                for (int i = 0; i < sz; ++i) {
                    s1 = is.readUTF();
                    s2 = is.readUTF();
                    ht.put(s1, s2);
                    System.out.println("--- " + s1 + " " + s2 + " ---");
                }
            } catch (IOException e) {
                if (sz != ht.size()) {
                    throw new Error("config read " + e);
                }
            }
        }
    }

    void set(String key, String value) {
        ht.put(key, value);
    }
    
    String get(String key) {
        return (String) ht.get(key);
    }

    int size() {
        return ht.size();
    }
    
    void save() {
        int sz = ht.size();
        Enumeration keys = ht.keys();        
        String s1, s2;
        try {
            rs.out.writeInt(sz);
            for (int i = 0; i < sz; ++i) {
                s1 = (String) keys.nextElement();
                s2 = get(s1);
                rs.out.writeUTF(s1);
                rs.out.writeUTF(s2);
            }
        } catch (IOException e) {
            throw new Error("config save " + e);
        }
        rs.write(recId);
        System.out.println("config saved");
    }

    //---- Canvas below, not related to Config above ----
    
    protected void paint(Graphics g) {
    }
}
