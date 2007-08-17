// Copyright (c) 2006-2007, Mihai Preda.
// Licensed under the MIT License (see COPYING).

import java.io.*;
import javax.microedition.rms.*;

class Store {
    private RecordStore rs;
    
    Store(String name, int formatVersion) {
        try {
            rs = RecordStore.openRecordStore(name, true);
            if (rs.getNextRecordID() == 1) { //init rs
                write(1, new byte[]{(byte)formatVersion});
            }
        } catch (Exception e) {
            Log.log("creation err " + e);
            throw new Error(e.toString());
        }
    }
    
    DataInputStream read(int recId) {
        byte buf[] = null;
        try {
            insureRecord(recId);
            buf = rs.getRecord(recId);
        } catch (InvalidRecordIDException e) {
        } catch (Exception e) { //IOException, RecordStoreException
            Log.log("read err " + e);
            throw new Error(e.toString());
        }
        return buf==null ? null : new DataInputStream(new ByteArrayInputStream(buf));
    }

    void write(int recId, byte[] data) {
        insureRecord(recId);
        setRecord(recId, data);
    }

    private void insureRecord(int recId) {
        try {
            for (int i = rs.getNextRecordID(); i <= recId; ++i) {
                rs.addRecord(null, 0, 0);
            }
        } catch (Exception e) {
            Log.log(e);
            throw new Error(e.toString());
        }
    }

    private void setRecord(int recId, byte[] data) {
        try {
            rs.setRecord(recId, data, 0, data.length);
        } catch (Exception e) {
            Log.log(e);
            throw new Error(e.toString());
        }
    }
}
