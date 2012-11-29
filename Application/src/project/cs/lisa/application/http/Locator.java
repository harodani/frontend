package project.cs.lisa.application.http;

/**
 * Represents a locator used in NetInf Publishes.
 * @author Linus Sunde
 *
 */
public class Locator {

    /**
     * Represents a specific locator type.
     * @author Linus Sunde
     *
     */
    public enum Type {

        /** Bluetooth Locator. */
        BLUETOOTH ("btmac");

        /** The HTTP query key belonging to a specific locator type. */
        private String mKey;

        /** Creates a new locator type.
         *  @param key
         *      The HTTP query key associated with this locator type
         */
        private Type(String key) {
            mKey = key;
        }

        /**
         * Gets the HTTP query key associated with this locator type.
         * @return
         *      The HTTP query key
         */
        public String getKey() {
            return mKey;
        }

    }

    /** Locator type. */
    private Type mType;
    /** Locator. */
    private String mLocator;

    /**
     * Creates a new locator.
     * @param type
     *      The type of the locator
     * @param locator
     *      The locator, for example the Bluetooth MAC address
     */
    public Locator(Type type, String locator) {
        mType = type;
        mLocator = locator;
    }

    /**
     * Gets the HTTP query key associated with this locator.
     * @return
     *      The HTTP query key
     */
    public String getQueryKey() {
        return mType.getKey();
    }

    /**
     * Gets the HTTP query value associated with this locator.
     * @return
     *      The HTTP query value
     */
    public String getQueryValue() {
        return mLocator;
    }

}
