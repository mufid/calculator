// Copyright (c) 2006-2007, Mihai Preda

#include "defines.inc"
import javax.microedition.rms.*;

class RMS {
    private RecordStore rs;
    private byte buf[] = new byte[300];
    private ByteArrayOutputStream bos = new ByteArrayOutputStream();
    DataOutputStream out = new DataOutputStream(bos);
    
    RMS(String name) {
        try {
            rs = RecordStore.openRecordStore("calc", true);
            if (rs.getNextRecordID() == 1) {
                //init rs
                buf[0] = 0;
                rs.addRecord(buf, 0, 1); //rec 1
            }
        } catch (Exception e) {
            LOG("creation err " + e);
            throw new Error(e.toString());
        }
    }
    
    DataInputStream read(int recId) {
        try {
            insureRecord(recId);
            int recSize = rs.getRecord(recId, buf, 0);
            if (recSize > 0) {
                return new DataInputStream(new ByteArrayInputStream(buf, 0, recSize));
            }
        } catch (InvalidRecordIDException e) { //to get out of the while()
        } catch (Exception e) { //IOException, RecordStoreException
            LOG("read err " + e);
            throw new Error(e.toString());
        }
        return null;
    }

    private void insureRecord(int recId) throws RecordStoreException {
        for (int i = rs.getNextRecordID(); i <= recId; ++i) {
            rs.addRecord(null, 0, 0);
        }
    }

    void write(int recId) {
        try {
            insureRecord(recId);
            int size = bos.size();
            rs.setRecord(recId, size==0 ? null : bos.toByteArray(), 0, size);
        } catch (Exception e) {
            LOG("write err " + e);
            throw new Error(e.toString());
        } finally {
            bos.reset();
        }
    }
}
