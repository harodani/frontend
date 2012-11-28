package project.cs.lisa.application.http;

/**
 * Represents a response to a NetInf request.
 * @author Linus Sunde
 *
 */
public abstract class NetInfResponse {

    /** Status of the request. */
    private NetInfStatus mStatus;

    /**
     * Creates a new response for a unsent request.
     */
    protected NetInfResponse() {
        mStatus = NetInfStatus.NOT_EXECUTED;
    }

    /**
     * Creates a new response with a given status.
     * @param status
     *      The status
     */
    protected void setStatus(NetInfStatus status) {
        mStatus = status;
    }

    /**
     * Gets the status of the response.
     * @return
     *      The status
     */
    public NetInfStatus getStatus() {
        return mStatus;
    }

}
