package cn.com.nbport.zgt.demo.simulation;

import cn.com.nbport.zgt.demo.simulation.entity.ASC;
import cn.com.nbport.zgt.demo.simulation.entity.QC;
import cn.com.nbport.zgt.demo.simulation.entity.Truck;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SimulationContext {
    private long simulationClock;
    private Map<String, Truck> truckMap;
    private Map<String, QC> qcMap;
    private Map<String, ASC> ascMap;
    private Map<String, List<String>> positionToWaitingEntities;
    private Map<String, String> positionToCurrentOccupiedEntity;
    private LinkedList<Event> eventList;

    // Getter for simulationClock
    public long getSimulationClock() {
        return simulationClock;
    }

    // Setter for simulationClock
    public void setSimulationClock(long simulationClock) {
        this.simulationClock = simulationClock;
    }

    // Getter for truckMap
    public Map<String, Truck> getTruckMap() {
        return truckMap;
    }

    // Setter for truckMap
    public void setTruckMap(Map<String, Truck> truckMap) {
        this.truckMap = truckMap;
    }

    // Getter for qcMap
    public Map<String, QC> getQcMap() {
        return qcMap;
    }

    // Setter for qcMap
    public void setQcMap(Map<String, QC> qcMap) {
        this.qcMap = qcMap;
    }

    // Getter for ascMap
    public Map<String, ASC> getAscMap() {
        return ascMap;
    }

    // Setter for ascMap
    public void setAscMap(Map<String, ASC> ascMap) {
        this.ascMap = ascMap;
    }

    // Getter for positionToWaitingEntities
    public Map<String, List<String>> getPositionToWaitingEntities() {
        return positionToWaitingEntities;
    }

    // Setter for positionToWaitingEntities
    public void setPositionToWaitingEntities(Map<String, List<String>> positionToWaitingEntities) {
        this.positionToWaitingEntities = positionToWaitingEntities;
    }

    // Getter for positionToCurrentOccupiedEntity
    public Map<String, String> getPositionToCurrentOccupiedEntity() {
        return positionToCurrentOccupiedEntity;
    }

    // Setter for positionToCurrentOccupiedEntity
    public void setPositionToCurrentOccupiedEntity(Map<String, String> positionToCurrentOccupiedEntity) {
        this.positionToCurrentOccupiedEntity = positionToCurrentOccupiedEntity;
    }

    // Getter for eventList
    public LinkedList<Event> getEventList() {
        return eventList;
    }

    // Setter for eventList
    public void setEventList(LinkedList<Event> eventList) {
        this.eventList = eventList;
    }
}

