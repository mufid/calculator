class HistEntry {
    String save;
    StringBuffer line;
    int pos;

    HistEntry copyFlush() {
        HistEntry e = new HistEntry();
        e.line = line;
        e.pos  = pos;
        e.save = line.toString();
        line = new StringBuffer(save);
        pos = save.length() - 1;
        return e;
    }
}
