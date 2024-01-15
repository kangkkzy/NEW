package cn.com.nbport.zgt.demo.simulation;

import cn.com.nbport.zgt.demo.simulation.entity.*;

import java.util.ArrayList;
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
        if (!context.getPositionToWaitingEntities().containsKey(position)) {
            context.getPositionToWaitingEntities().put(position, new ArrayList<>());
        }
        List<String> waitingList = context.getPositionToWaitingEntities().get(position);
        //--------------------------------------------------------------------------------------------------------------
        String truckId = event.getTruckId();
        Truck truck = context.getTruckMap().get(truckId);
        truck.setCurrentPosition(position);
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
                        ascMove(context, event, dg, pulledDevice, asc, position);
                    } else if (context.getQcMap().containsKey(pulledDevice)) {
                        QC qc = context.getQcMap().get(pulledDevice);
                        qcMove(context, event, dg, pulledDevice, qc, position);
                    }
                }
            } else {
                // 当前位置有桥吊 / 龙门吊
                // 判断是否能执行工作
                if (dm.judgeRationalToStartWork(context, event)) {
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
                        ascWork(context, event, dg, position, currentPositionDevice, asc, truckId, truck);
                    } else if (context.getQcMap().containsKey(currentPositionDevice)) {
                        QC qc = context.getQcMap().get(currentPositionDevice);
                        qcWork(context, event, dg, position, currentPositionDevice, qc, truckId, truck);
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
        //--------------------------------------------------------------------------------------------------------------
        String position = event.getPosition();
        if (!context.getPositionToWaitingEntities().containsKey(position)) {
            context.getPositionToWaitingEntities().put(position, new ArrayList<>());
        }
        List<String> waitingList = context.getPositionToWaitingEntities().get(position);
        //--------------------------------------------------------------------------------------------------------------
        // 处理桥吊的当前状态
        String qcId = event.getQcId();
        QC qc = context.getQcMap().get(qcId);
        qc.setCurrentPosition(position);
        //--------------------------------------------------------------------------------------------------------------
        if (waitingList.isEmpty()) {
            qc.setStatus(QCStatusEnum.WAITING);
        } else {
            //- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            // 生成集卡完成事件
            String truckId = waitingList.get(0);
            Truck truck = context.getTruckMap().get(truckId);
            truck.setStatus(TruckStatusEnum.WORKING);
            long truckWorkTimePrediction = dg.predictTruckWorkTime(context, truckId);
            Event truckWorkDoneEvent = new Event(event.getEventTime() + truckWorkTimePrediction,
                    EventEnum.TRUCK_WORK_DONE, position, truckId, null, qcId);
            context.getEventList().add(truckWorkDoneEvent);
            //- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            // 生成桥吊完成事件
            qcWork(context, event, dg, position, qcId, qc, truckId, truck);
        }
    }

    private static void handleASCArrival(SimulationContext context, Event event, DecisionMaker dm, DataGenerator dg) {
        //--------------------------------------------------------------------------------------------------------------
        String position = event.getPosition();
        if (!context.getPositionToWaitingEntities().containsKey(position)) {
            context.getPositionToWaitingEntities().put(position, new ArrayList<>());
        }
        List<String> waitingList = context.getPositionToWaitingEntities().get(position);
        //--------------------------------------------------------------------------------------------------------------
        // 处理龙门吊的当前状态
        String ascId = event.getAscId();
        ASC asc = context.getAscMap().get(ascId);
        asc.setCurrentPosition(position);
        //--------------------------------------------------------------------------------------------------------------
        if (waitingList.isEmpty()) {
            asc.setStatus(ASCStatusEnum.WAITING);
        } else {
            //- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            // 生成集卡完成事件
            String truckId = waitingList.get(0);
            Truck truck = context.getTruckMap().get(truckId);
            truck.setStatus(TruckStatusEnum.WORKING);
            long truckWorkTimePrediction = dg.predictTruckWorkTime(context, truckId);
            Event truckWorkDoneEvent = new Event(event.getEventTime() + truckWorkTimePrediction,
                    EventEnum.TRUCK_WORK_DONE, position, truckId, ascId, null);
            context.getEventList().add(truckWorkDoneEvent);
            //- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            // 生成龙门吊完成事件
            ascWork(context, event, dg, position, ascId, asc, truckId, truck);
        }
    }

    private static void handleTruckWorkDone(SimulationContext context, Event event, DecisionMaker dm, DataGenerator dg) {
        String truckId = event.getTruckId();
        Integer nextPositionWIRef = dm.getTruckNextPositionWIRef(context, truckId);
        String nextPosition = dm.getTruckNextPosition(context, truckId);
        Truck truck = context.getTruckMap().get(truckId);
        truck.setCurrentWIRef(nextPositionWIRef);
        if (Objects.isNull(nextPosition) || nextPosition.isEmpty()) {
            truck.setStatus(TruckStatusEnum.END);
        } else {
            truck.setStatus(TruckStatusEnum.MOVING);
            truck.setTargetPosition(nextPosition);
            long truckMoveTimePrediction = dg.predictTruckMoveTime(context, truckId);
            Event truckArrivalEvent = new Event(event.getEventTime() + truckMoveTimePrediction, EventEnum.TRUCK_ARRIVAL,nextPosition, truckId, null, null);
            context.getEventList().add(truckArrivalEvent);
        }
    }

    private static void handleQCWorkDone(SimulationContext context, Event event, DecisionMaker dm, DataGenerator dg) {
        String position = event.getPosition();
        String qcId = event.getQcId();
        QC qc = context.getQcMap().get(qcId);
        String nextPosition = dm.getQCNextPosition(context, qcId);
        if (position.equals(nextPosition)) {
            List<String> waitingList = context.getPositionToWaitingEntities().get(position);
            if (waitingList.isEmpty()) {
                qc.setStatus(QCStatusEnum.WAITING);
            }
        } else {
            qcMove(context, event, dg, qcId, qc, nextPosition);
        }
    }

    private static void handleASCWorkDone(SimulationContext context, Event event, DecisionMaker dm, DataGenerator dg) {
        String position = event.getPosition();
        String ascId = event.getAscId();
        ASC asc = context.getAscMap().get(ascId);
        String nextPosition = dm.getASCNextPosition(context, ascId);
        if (position.equals(nextPosition)) {
            List<String> waitingList = context.getPositionToWaitingEntities().get(position);
            if (waitingList.isEmpty()) {
                asc.setStatus(ASCStatusEnum.WAITING);
            }
        } else {
            ascMove(context, event, dg, ascId, asc, nextPosition);
        }
    }

    private static void qcMove(SimulationContext context, Event event, DataGenerator dg, String qcId, QC qc, String nextPosition) {
        qc.setStatus(QCStatusEnum.MOVING);
        qc.setTargetPosition(nextPosition);
        long qcMoveTimePrediction = dg.predictQCMoveTime(context, qcId);
        Event qcArrivalEvent = new Event(event.getEventTime() + qcMoveTimePrediction, EventEnum.QC_ARRIVAL, nextPosition, null, null ,qcId);
        context.getEventList().add(qcArrivalEvent);
    }

    private static void ascMove(SimulationContext context, Event event, DataGenerator dg, String ascId, ASC asc, String nextPosition) {
        asc.setStatus(ASCStatusEnum.MOVING);
        asc.setTargetPosition(nextPosition);
        long ascMoveTimePrediction = dg.predictASCMoveTime(context, ascId);
        Event ascArrivalEvent = new Event(event.getEventTime() + ascMoveTimePrediction, EventEnum.ASC_ARRIVAL, nextPosition, null, ascId, null);
        context.getEventList().add(ascArrivalEvent);
    }

    private static void qcWork(SimulationContext context, Event event, DataGenerator dg, String position, String qcId, QC qc, String truckId, Truck truck) {
        qc.setStatus(QCStatusEnum.WORKING);
        qc.setCurrentWIRef(truck.getCurrentWIRef());
        long qcWorkTimePrediction = dg.predictQCWorkTime(context, qcId);
        Event qcWorkDoneEvent = new Event(event.getEventTime() + qcWorkTimePrediction,
                EventEnum.QC_WORK_DONE, position, truckId, null, qcId);
        context.getEventList().add(qcWorkDoneEvent);
    }

    private static void ascWork(SimulationContext context, Event event, DataGenerator dg, String position, String ascId, ASC asc, String truckId, Truck truck) {
        asc.setStatus(ASCStatusEnum.WORKING);
        asc.setCurrentWIRef(truck.getCurrentWIRef());
        long ascWorkTimePrediction = dg.predictASCWorkTime(context, ascId);
        Event ascWorkDoneEvent = new Event(event.getEventTime() + ascWorkTimePrediction,
                EventEnum.ASC_WORK_DONE, position, truckId, ascId, null);
        context.getEventList().add(ascWorkDoneEvent);
    }
}
