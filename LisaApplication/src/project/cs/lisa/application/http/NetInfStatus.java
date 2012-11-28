package project.cs.lisa.application.http;

/**
 * Represent the status of a response to a request.
 * @author Linus Sunde
 *
 */
public enum NetInfStatus {

    /** Request Succeeded. */
    OK,
    /** Request Failed. */
    FAILED,
    /** Response to request contained no content when it should have. */
    NO_CONTENT,
    /** Response to request contained invalid content. */
    INVALID_CONTENT,
    /** Response contained no file path when it should have. */
    NO_FILE_PATH,
    /** Response contained no content type when it should have. */
    NO_CONTENT_TYPE,
    /** Response contained a file path to a file that doesn't exist. */
    FILE_DOES_NOT_EXIST,
    /** Response contained no search results when it should have. */
    NO_SEARCH_RESULTS,
    /** Response contained invalid search results. */
    INVALID_SEARCH_RESULTS,
    /** Request was not executed. */
    NOT_EXECUTED;

}
