package org.dcm4che.data;

enum StringType {
    ASCII,
    UI {
        @Override
        public String substring(String s, int beginIndex, int endIndex) {
            while (beginIndex < endIndex && s.charAt(endIndex - 1) <= ' ')
                endIndex--;
            return s.substring(beginIndex, endIndex);
        }
    },
    STRING {
        @Override
        protected SpecificCharacterSet cs(SpecificCharacterSet cs) {
            return cs;
        }
    },
    TEXT {
        @Override
        protected SpecificCharacterSet cs(SpecificCharacterSet cs) {
            return cs;
        }

        @Override
        public String first(String s) {
            int endIndex = s.length();
            while (endIndex > 0 && s.charAt(endIndex - 1) == ' ')
                endIndex--;
            return s.substring(0, endIndex);
        }

        @Override
        public String[] split(String s) {
            return new String[] { first(s) };
        }

        @Override
        public String join(String[] strings) {
            throw new UnsupportedOperationException();
        }
    };

    public static String[] EMPTY_STRINGS = {};

    protected SpecificCharacterSet cs(SpecificCharacterSet cs) {
        return SpecificCharacterSet.DEFAULT;
    }

    public String first(String s) {
        int delimPos1 = s.indexOf('\\');
        return substring(s, 0,
                delimPos1 < 0 ? s.length() : delimPos1);
    }

    public String[] split(String s) {
        int count = 1;
        int delimPos = -1;
        while ((delimPos = s.indexOf('\\', delimPos+1)) >= 0)
            count++;

        String[] strings = new String[count];
        int delimPos2 = s.length();
        while (--count >= 0) {
            delimPos = s.lastIndexOf('\\', delimPos2-1);
            strings[count] = substring(s, delimPos+1, delimPos2);
            delimPos2 = delimPos;
        }
        return strings;
    }

    public byte[] toBytes(String s, SpecificCharacterSet cs) {
        return cs(cs).encode(s);
    }

    public String toString(byte[] bytearr, SpecificCharacterSet cs) {
        return cs(cs).decode(bytearr);
    }

    protected String substring(String s, int beginIndex, int endIndex) {
        while (beginIndex < endIndex && s.charAt(beginIndex) == ' ')
            beginIndex++;
        while (beginIndex < endIndex && s.charAt(endIndex - 1) == ' ')
            endIndex--;
        return s.substring(beginIndex, endIndex);
    }

    public String join(String[] strings) {
        if (strings.length == 0)
            return null;
        
        if (strings.length == 1) {
            String s = strings[0];
            return s != null ? s : "";
        }
        int len = strings.length - 1;
        for (String s : strings)
            len += s != null ? s.length() : 0;

        StringBuilder sb = new StringBuilder(len);
        sb.append(strings[0]);
        for (int i = 1; i < strings.length; i++) {
            sb.append('\\');
            String s = strings[i];
            if (s != null)
                sb.append(s);
        }
        return sb.toString();
    }

}
