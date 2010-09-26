package org.dcm4che.data;

import java.util.ServiceLoader;

public abstract class ElementDictionary {
    private static final ServiceLoader<ElementDictionary> loader =
            ServiceLoader.load(ElementDictionary.class);
    private final String privateCreator;
    private final Class<?> tagClass;

    protected ElementDictionary(String privateCreator, Class<?> tagClass) {
        this.privateCreator = privateCreator;
        this.tagClass = tagClass;
    }

    public static ElementDictionary getStandardElementDictionary() {
        return StandardElementDictionary.INSTANCE;
    }

    public static ElementDictionary getElementDictionary(
            String privateCreator) {
        if (privateCreator != null)
            for (ElementDictionary dict : loader)
                if (privateCreator.equals(dict.getPrivateCreator()))
                    return dict;
        return getStandardElementDictionary();
    }

    public static void reload() {
        loader.reload();
    }

    public static VR vrOf(int tag, String privateCreator) {
        return getElementDictionary(privateCreator).vrOf(tag);
    }

    public static String nameOf(int tag, String privateCreator) {
        return getElementDictionary(privateCreator).nameOf(tag);
    }

    public static int tagForName(String name, String privateCreatorID) {
        return getElementDictionary(privateCreatorID).tagForName(name);
    }

    public final String getPrivateCreator() {
        return privateCreator;
    }

    public abstract VR vrOf(int tag);

    public abstract String nameOf(int tag);

    public int tagForName(String keyword) {
        if (tagClass != null)
            try {
                return tagClass.getField(keyword).getInt(null);
            } catch (Exception ignore) { }
        return -1;
    }
}
