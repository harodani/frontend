package project.cs.lisa.application.html.transfer;

import java.io.File;

/**
 * A representation of a web object.
 *
 * @author Paolo Boschini
 * @author Linus Sunde
 * @author Kim-Anh Tran
 *
 */
public class WebObject {

    /** The content type associated to this web object. */
    private String mContentType;

    /** The file associated to this web object. */
    private File mFile;
    
    /** The hash associated to this web object. */
    private String mHash;
   
    /**
     * Default constructor. Created a new web object.
     * 
     * @param contentType   the content type of this web object
     * @param file          the file that contains this web object
     * @param hash          the hash of this web object
     */
    public WebObject(String contentType, File file, String hash) {
        mContentType = contentType;
        mFile = file;
        mHash = hash;
    }

    /**
     * Returns the content type of this web object.
     * @return the content type
     */
    public String getContentType() {
        return mContentType;
    }

    /**
     * Returns the file of this web object.
     * @return the file
     */
    public File getFile() {
        return mFile;
    }

    /**
     * Returns the hash of this web object.
     * @return  the hash
     */
    public String getHash() {
        return mHash;
    }
}
