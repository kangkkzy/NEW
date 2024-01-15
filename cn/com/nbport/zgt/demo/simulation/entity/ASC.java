package cn.com.nbport.zgt.demo.simulation.entity;

/**
 * 龙门吊
 */
public class ASC {
    private String id;
    private ASCStatusEnum status;
    private String currentPosition;
    private String targetPosition;
    private Integer currentWIRef;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public ASCStatusEnum getStatus() {
        return status;
    }
    public void setStatus(ASCStatusEnum status){
        this.status = status;
    }
    public String getCurrentPosition() {
        return currentPosition;
    }
    public void setCurrentPosition(String currentPosition) {
        this.currentPosition = currentPosition;
    }
    public String getTargetPosition() {
        return targetPosition;
    }
    public void setTargetPosition(String targetPosition) {
        this.targetPosition = targetPosition;
    }
    public Integer getCurrentWIRef() {
        return currentWIRef;
    }
    public void setCurrentWIRef(Integer currentWIRef) {
        this.currentWIRef = currentWIRef;
    }
}
