// Copyright (c) 2007, Carlo Teubner
// Available under the MIT License (see COPYING).

public class StringWrapper {
    private char[] data;
    private int start, end;
    private int hash;

    private static StringWrapper temp;

    // Use with care!
    public static StringWrapper getTemp(char[] data, int start, int end) {
        if (temp == null)
            temp = new StringWrapper(data, start, end);
        else
            temp.init(data, start, end);
        return temp;
    }

    // Use with care!
    public static StringWrapper getTemp(String str) {
        if (temp == null)
            temp = new StringWrapper(str);
        else
            temp.init(str);
        return temp;
    }

    public StringWrapper(char[] data, int start, int end) {
        // System.out.println("new StringWrapper[char](" + String.valueOf(data, start, end - start) + ")");
        this.data = data;
        this.start = start;
        this.end = end;
    }

    public StringWrapper(String str) {
        // System.out.println("new StringWrapper[str](" + str + ")");
        start = 0;
        end = str.length();
        data = str.toCharArray();
    }

    public void init(char[] data, int start, int end) {
        // System.out.println("SW#init[char](" + String.valueOf(data, start, end - start) + ")");
        this.data = data;
        this.start = start;
        this.end = end;
        this.hash = 0;
    }

    public void init(String str) {
        start = 0;
        end = str.length();
        data = str.toCharArray();
        hash = 0;
    }

    public int length() {
        return end - start;
    }
    
    public char charAt(int index) {
        return data[start + index];
    }

    public boolean startsWith(String prefix) {
        final int len = length(), pre_len = prefix.length();
        if (len < pre_len)
            return false;
        for (int i = start, j = 0; j < pre_len; ++i, ++j)
            if (data[i] != prefix.charAt(j))
                return false;
        return true;
    }

    public boolean equals(Object other) {
        // System.out.println("SW#equals");
        if (other instanceof StringWrapper) {
            StringWrapper sw = (StringWrapper) other;
            if (end - start != sw.end - sw.start)
                return false;
            for (int i = start, j = sw.start; i < end; ++i, ++j)
                if (data[i] != sw.data[j])
                    return false;
            return true;
        }
        return false;
    }

    public int hashCode() {
        // System.out.println("SW#hash");
        int h = hash;
        if (h == 0) {
            for (int i = start; i < end; ++i) {
                h = 31 * h + data[i];
            }
            hash = h;
        }
        return h;
    }
}
