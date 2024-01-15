package cn.com.nbport.zgt.demo.simulation;

import cn.com.nbport.zgt.demo.simulation.entity.*;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class EventHandler {
    public static void handleEvent(SimulationContext context, Event event, DecisionMaker dm, DataGenerator dg) {
        EventEnum eventEnum = event.getEventEnum();
        if (EventEnum.TRUCK_ARRIVAL.equals(eventEnum)) {
            handleTruckArrival(context, event, dm, dg);
        } else if (EventEnum.ASC_ARRIVAL.equals(eventEnum)) {
            handleASCArrival(context, event, dm, dg);
        } else if (EventEnum.QC_ARRIVAL.equals(eventEnum)) {
            handleQCArrival(context, event, dm, dg);
        } else if (EventEnum.TRUCK_WORK_DONE.equals(eventEnum)) {
            handleTruckWorkDone(context, event, dm, dg);
        } else if (EventEnum.ASC_WORK_DONE.equals(eventEnum)) {
            handleASCWorkDone(context, event, dm, dg);
        } else if (EventEnum.QC_WORK_DONE.equals(eventEnum)) {
            handleQCWorkDone(context, event, dm, dg);
        }
    }

    private static void handleTruckArrival(SimulationContext context, Event event, DecisionMaker dm, DataGenerator dg) {
        //--------------------------------------------------------------------------------------------------------------
        String position = event.getPosition();
        //--------------------------------------------------------------------------------------------------------------
        String truckId = event.getTruckId();
        Truck truck = context.getTruckMap().get(truckId);
        truck.setCurrentPosition(position);
        //--------------------------------------------------------------------------------------------------------------
        List<String> waitingList = context.getPositionToWaitingEntities().getOrDefault(position, Collections.emptyList());
        //--------------------------------------------------------------------------------------------------------------
        if (waitingList.isEmpty()) {
            // 当前位置没有车排队
            String currentPositionDevice = context.getPositionToCurrentOccupiedEntity().get(position);
            if (Objects.isNull(currentPositionDevice) || currentPositionDevice.isEmpty()) {
                // 当前位置没有桥吊 / 龙门吊，集卡进入等待队列
                truck.setStatus(TruckStatusEnum.WAITING);
                waitingList.add(truckId);
                // 决策是否有龙门吊 / 桥吊需要移动
                String pulledDevice = dm.pullDeviceToOtherPosition(context, position);
                if (Objects.isNull(pulledDevice) || pulledDevice.isEmpty()) {
                    // 龙门吊 / 桥吊移动，修改设备状态，生成新的事件
                    if (context.getAscMap().containsKey(pulledDevice)) {
                        ASC asc = context.getAscMap().get(pulledDevice);
                        asc.setStatus(ASCStatusEnum.MOVING);
                        asc.setTargetPosition(position);
                        long arrivalTimePrediction = dg.predictASCMoveTime(context, pulledDevice);
                        Event ascArrivalEvent = new Event(event.getEventTime() + arrivalTimePrediction,
                                EventEnum.ASC_ARRIVAL, position, null, pulledDevice, null);
                        context.getEventList().add(ascArrivalEvent);
                    } else if (context.getQcMap().containsKey(pulledDevice)) {
                        QC qc = context.getQcMap().get(pulledDevice);
                        qc.setStatus(QCStatusEnum.MOVING);
                        qc.setTargetPosition(position);
                        long arrivalTimePrediction = dg.predictQCMoveTime(context, pulledDevice);
                        Event qcArrivalEvent = new Event(event.getEventTime() + arrivalTimePrediction,
                                EventEnum.QC_ARRIVAL, position, null, null, pulledDevice);
                        context.getEventList().add(qcArrivalEvent);
                    }
                }
            } else {
                // 当前位置有桥吊 / 龙门吊
                // 判断是否能执行工作
                if (dm.judgeRationalToStartWork(context, event)){
                    //--------------------------------------------------------------------------------------------------
                    // 更新集卡的状态，生成新的事件
                    truck.setStatus(TruckStatusEnum.WORKING);
                    long truckWorkTimePrediction = dg.predictTruckWorkTime(context, truckId);
                    Event truckWorkDoneEvent = new Event(event.getEventTime() + truckWorkTimePrediction,
                            EventEnum.TRUCK_WORK_DONE, position, truckId, currentPositionDevice, null);
                    context.getEventList().add(truckWorkDoneEvent);
                    //--------------------------------------------------------------------------------------------------
                    // 龙门吊 / 桥吊的设备状态，生成新的事件
                    // todo QC extends Crane, ASC extends Crane，就不需要判断是龙门吊还是桥吊
                    if (context.getAscMap().containsKey(currentPositionDevice)) {
                        ASC asc = context.getAscMap().get(currentPositionDevice);
                        asc.setStatus(ASCStatusEnum.WORKING);
                        asc.setCurrentWIRef(truck.getCurrentWIRef());
                        long ascWorkTimePrediction = dg.predictASCWorkTime(context, currentPositionDevice);
                        Event ascWorkDoneEvent = new Event(event.getEventTime() + ascWorkTimePrediction,
                                EventEnum.ASC_WORK_DONE, position, truckId, currentPositionDevice, null);
                        context.getEventList().add(ascWorkDoneEvent);
                    } else if (context.getQcMap().containsKey(currentPositionDevice)) {
                        QC qc = context.getQcMap().get(currentPositionDevice);
                        qc.setStatus(QCStatusEnum.WORKING);
                        qc.setCurrentWIRef(truck.getCurrentWIRef());
                    long qcWorkTimePrediction = dg.predictQCWorkTime(context, currentPositionDevice);
                        Event qcWorkDoneEvent = new Event(event.getEventTime() + qcWorkTimePrediction,
                                EventEnum.QC_WORK_DONE, position, truckId, null, currentPositionDevice);
                        context.getEventList().add(qcWorkDoneEvent);
                    }
                } else {
                    // 当前位置有车排队，集卡进入等待队列
                    truck.setStatus(TruckStatusEnum.WAITING);
                    waitingList.add(truckId);
                }
            }
        } else {
            // 当前位置有车排队，集卡进入等待队列
            truck.setStatus(TruckStatusEnum.WAITING);
            waitingList.add(truckId);
        }
    }
    private static void handleQCArrival(SimulationContext context, Event event, DecisionMaker dm, DataGenerator dg) {

    }
    private static void handleASCArrival(SimulationContext context, Event event, DecisionMaker dm, DataGenerator dg) {

    }
    private static void handleTruckWorkDone(SimulationContext context, Event event, DecisionMaker dm, DataGenerator dg) {

    }
    private static void handleQCWorkDone(SimulationContext context, Event event, DecisionMaker dm, DataGenerator dg) {

    }
    private static void handleASCWorkDone(SimulationContext context, Event event, DecisionMaker dm, DataGenerator dg) {

    }
}
