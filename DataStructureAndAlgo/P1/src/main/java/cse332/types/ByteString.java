package cse332.types;

import cse332.interfaces.worklists.FixedSizeFIFOWorkList;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

public class ByteString extends BString<Byte> {
    public ByteString(String s) {
        super(BString.wrap(s.getBytes()));
    }

    public ByteString(byte[] s) {
        super(BString.wrap(s));
    }

    public ByteString(FixedSizeFIFOWorkList<Byte> q) {
        super(q);
    }

    public ByteString(Byte[] s) {
        super(s);
    }

    public static Class<Byte> getLetterType() {
        return Byte.class;
    }

    public String toString() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = 0; i < this.str.size(); i++) {
            out.write(this.str.peek(i));
        }
        try {
            return out.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return out.toString();
        }
    }
}
