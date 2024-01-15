package cn.com.nbport.zgt.demo.simulation.entity;

/**
 * 内集卡
 */
public class Truck {
    private String id;
    private TruckStatusEnum status;
    private String currentPosition;
    private String targetPosition;
    private Integer currentWIRef;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public TruckStatusEnum getStatus() {
        return status;
    }
    public void setStatus(TruckStatusEnum status){
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
