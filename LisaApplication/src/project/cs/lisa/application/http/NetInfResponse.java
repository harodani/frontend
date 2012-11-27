package project.cs.lisa.application.http;


public abstract class NetInfResponse {

    private NetInfStatus mStatus;

    protected NetInfResponse() {
        mStatus = NetInfStatus.NOT_EXECUTED;
    }

    protected void setStatus(NetInfStatus status) {
        mStatus = status;
    }

    public NetInfStatus getStatus() {
        return mStatus;
    }

}
