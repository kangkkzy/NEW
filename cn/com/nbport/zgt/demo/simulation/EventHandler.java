package cn.com.nbport.zgt.demo.simulation;

import cn.com.nbport.zgt.demo.simulation.entity.*;

import java.util.ArrayList;
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
        // 消费集卡到达事件
        //--------------------------------------------------------------------------------------------------------------
        // 当前位置与集卡等待队列
        String position = event.getPosition();
        if (!context.getPositionToWaitingEntities().containsKey(position)) {
            context.getPositionToWaitingEntities().put(position, new ArrayList<>());
        }
        List<String> waitingList = context.getPositionToWaitingEntities().get(position);
        //--------------------------------------------------------------------------------------------------------------
        // 刷新集卡当前位置
        String truckId = event.getTruckId();
        Truck truck = context.getTruckMap().get(truckId);
        truck.setCurrentPosition(position);
        //--------------------------------------------------------------------------------------------------------------
        // 获取当前位置的龙门吊 / 桥吊
        String currentPositionDevice = context.getPositionToCurrentOccupiedEntity().get(position);
        //--------------------------------------------------------------------------------------------------------------
        if (waitingList.isEmpty()) {
            // 当前位置没有车排队
            // - - - - - - - - - - - - - - - - - - - -
            if (Objects.isNull(currentPositionDevice) || currentPositionDevice.isEmpty()) {
                // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                // 当前位置没有桥吊 / 龙门吊，集卡进入等待队列
                truck.setStatus(TruckStatusEnum.WAITING);
                waitingList.add(truckId);
                dm.waitingListSort(context, position);
                // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                // 决策是否有龙门吊 / 桥吊需要移动
                String pulledDevice = dm.pullDeviceToOtherPosition(context, position);
                if (Objects.isNull(pulledDevice) || pulledDevice.isEmpty()) {
                    // 龙门吊 / 桥吊移动，修改设备状态，生成新的事件
                    if (context.getAscMap().containsKey(pulledDevice)) {
                        ascMove(context, event, dg, pulledDevice, position);
                    } else if (context.getQcMap().containsKey(pulledDevice)) {
                        qcMove(context, event, dg, pulledDevice, position);
                    }
                }
                // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            } else {
                // 当前位置有桥吊 / 龙门吊
                // 判断是否能执行工作
                if (dm.judgeRationalToStartWork(context, event)) {
                    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                    // 集卡 / 龙门吊 / 桥吊的设备状态，生成新的事件
                    truckCraneWork(context, event, dg, position, truckId, currentPositionDevice);
                    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                } else {
                    // 当前位置有车排队，集卡进入等待队列
                    truck.setStatus(TruckStatusEnum.WAITING);
                    waitingList.add(truckId);
                    dm.waitingListSort(context, position);
                }
            }
        } else {
            if (dm.judgeRationalToStartWork(context, event)) {
                // 可以超车 / 调整顺序
                // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
                // 集卡 / 龙门吊 / 桥吊的设备状态，生成新的事件
                truckCraneWork(context, event, dg, position, truckId, currentPositionDevice);
                // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            }
            // 当前位置有车排队，集卡进入等待队列
            truck.setStatus(TruckStatusEnum.WAITING);
            waitingList.add(truckId);
            dm.waitingListSort(context, position);
        }
    }

    private static void handleQCArrival(SimulationContext context, Event event, DecisionMaker dm, DataGenerator dg) {
        // 消费桥吊到达事件
        //--------------------------------------------------------------------------------------------------------------
        // 当前位置与集卡等待队列
        String position = event.getPosition();
        if (!context.getPositionToWaitingEntities().containsKey(position)) {
            context.getPositionToWaitingEntities().put(position, new ArrayList<>());
        }
        List<String> waitingList = context.getPositionToWaitingEntities().get(position);
        //--------------------------------------------------------------------------------------------------------------
        // 刷新桥吊当前位置
        String qcId = event.getQcId();
        QC qc = context.getQcMap().get(qcId);
        qc.setCurrentPosition(position);
        //--------------------------------------------------------------------------------------------------------------
        if (waitingList.isEmpty()) {
            // 当前位置没有车辆排队
            qc.setStatus(QCStatusEnum.WAITING);
        } else {
            // 当前位置有车辆排队
            //- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            // 生成集卡完成事件
            if (dm.judgeRationalToStartWork(context, event)) {
                String truckId = waitingList.get(0);
                truckWork(context, event, dg, position, truckId, null, qcId);
                qcWork(context, event, dg, position, qcId, truckId);
            } else {
                qc.setStatus(QCStatusEnum.WAITING);
            }
            //- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

        }
    }

    private static void handleASCArrival(SimulationContext context, Event event, DecisionMaker dm, DataGenerator dg) {
        // 消费龙门吊到达事件
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
            // 当前位置没有车辆排队
            asc.setStatus(ASCStatusEnum.WAITING);
        } else {
            // 当前位置有车辆排队
            //- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
            // 生成集卡完成事件
            if (dm.judgeRationalToStartWork(context, event)) {
                String truckId = waitingList.get(0);
                truckWork(context, event, dg, position, truckId, ascId, null);
                ascWork(context, event, dg, position, ascId, truckId);
            }
            //- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
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
            Event truckArrivalEvent = new Event(event.getEventTime() + truckMoveTimePrediction, EventEnum.TRUCK_ARRIVAL, nextPosition, truckId, null, null);
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
            } else {
                String truckId = waitingList.get(0);
                qcWork(context, event, dg, position, qcId, truckId);
                truckWork(context, event,dg, position, truckId, null, qcId);
            }
        } else {
            qcMove(context, event, dg, qcId, nextPosition);
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
            } else {
                String truckId = waitingList.get(0);
                ascWork(context, event, dg, position, ascId, truckId);
                truckWork(context, event,dg, position, truckId, ascId, null);
            }
        } else {
            ascMove(context, event, dg, ascId, nextPosition);
        }
    }

    private static void qcMove(SimulationContext context, Event event, DataGenerator dg, String qcId, String nextPosition) {
        QC qc = context.getQcMap().get(qcId);
        qc.setStatus(QCStatusEnum.MOVING);
        qc.setTargetPosition(nextPosition);
        long qcMoveTimePrediction = dg.predictQCMoveTime(context, qcId);
        Event qcArrivalEvent = new Event(event.getEventTime() + qcMoveTimePrediction, EventEnum.QC_ARRIVAL, nextPosition, null, null, qcId);
        context.getEventList().add(qcArrivalEvent);
    }

    private static void ascMove(SimulationContext context, Event event, DataGenerator dg, String ascId, String nextPosition) {
        ASC asc = context.getAscMap().get(ascId);
        asc.setStatus(ASCStatusEnum.MOVING);
        asc.setTargetPosition(nextPosition);
        long ascMoveTimePrediction = dg.predictASCMoveTime(context, ascId);
        Event ascArrivalEvent = new Event(event.getEventTime() + ascMoveTimePrediction, EventEnum.ASC_ARRIVAL, nextPosition, null, ascId, null);
        context.getEventList().add(ascArrivalEvent);
    }

    private static void qcWork(SimulationContext context, Event event, DataGenerator dg, String position, String qcId, String truckId) {
        QC qc = context.getQcMap().get(qcId);
        Truck truck = context.getTruckMap().get(truckId);
        qc.setStatus(QCStatusEnum.WORKING);
        qc.setCurrentWIRef(truck.getCurrentWIRef());
        long qcWorkTimePrediction = dg.predictQCWorkTime(context, qcId);
        Event qcWorkDoneEvent = new Event(event.getEventTime() + qcWorkTimePrediction,
                EventEnum.QC_WORK_DONE, position, truckId, null, qcId);
        context.getEventList().add(qcWorkDoneEvent);
    }

    private static void ascWork(SimulationContext context, Event event, DataGenerator dg, String position, String ascId, String truckId) {
        ASC asc = context.getAscMap().get(ascId);
        Truck truck = context.getTruckMap().get(truckId);
        asc.setStatus(ASCStatusEnum.WORKING);
        asc.setCurrentWIRef(truck.getCurrentWIRef());
        long ascWorkTimePrediction = dg.predictASCWorkTime(context, ascId);
        Event ascWorkDoneEvent = new Event(event.getEventTime() + ascWorkTimePrediction,
                EventEnum.ASC_WORK_DONE, position, truckId, ascId, null);
        context.getEventList().add(ascWorkDoneEvent);
    }

    private static void truckWork(SimulationContext context, Event event, DataGenerator dg, String position, String truckId, String ascId, String qcId) {
        Truck truck = context.getTruckMap().get(truckId);
        truck.setStatus(TruckStatusEnum.WORKING);
        long truckWorkTimePrediction = dg.predictTruckWorkTime(context, truckId);
        Event truckWorkDoneEvent = new Event(event.getEventTime() + truckWorkTimePrediction,
                EventEnum.TRUCK_WORK_DONE, position, truckId, ascId, qcId);
        context.getEventList().add(truckWorkDoneEvent);
    }

    private static void truckCraneWork(SimulationContext context, Event event, DataGenerator dg, String position, String truckId, String currentPositionDevice) {
        if (context.getAscMap().containsKey(currentPositionDevice)) {
            truckWork(context, event, dg, position, truckId, currentPositionDevice, null);
            ascWork(context, event, dg, position, currentPositionDevice, truckId);
        } else if (context.getQcMap().containsKey(currentPositionDevice)) {
            truckWork(context, event, dg, position, truckId, null, currentPositionDevice);
            qcWork(context, event, dg, position, currentPositionDevice, truckId);
        }
    }
}