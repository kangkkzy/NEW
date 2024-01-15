package cn.com.nbport.zgt.demo.simulation;

public class Event {
    private static long cnt = 1;
    private final long id;
    private final long eventTime;
    private final EventEnum eventEnum;
    private final String position;
    private final String truckId;
    private final String ascId;
    private final String qcId;

    public Event(long eventTime, EventEnum eventEnum, String position, String truckId, String ascId, String qcId) {
        this.id = cnt++;
        this.eventTime = eventTime;
        this.eventEnum = eventEnum;
        this.position = position;
        this.truckId = truckId;
        this.ascId = ascId;
        this.qcId = qcId;
    }

    // Getter for id
    public long getId() {
        return id;
    }

    // Getter for eventTime
    public long getEventTime() {
        return eventTime;
    }

    // Getter for eventEnum
    public EventEnum getEventEnum() {
        return eventEnum;
    }

    // Getter for position
    public String getPosition() {
        return position;
    }

    // Getter for truckId
    public String getTruckId() {
        return truckId;
    }
    // Getter for ascId
    public String getAscId() {
        return ascId;
    }
    // Getter for qcId
    public String getQcId() {
        return qcId;
    }
}
