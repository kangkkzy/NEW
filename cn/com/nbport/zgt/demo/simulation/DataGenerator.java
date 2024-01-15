package cn.com.nbport.zgt.demo.simulation;

public class DataGenerator {
    public long predictTruckMoveTime(SimulationContext context, String truckId){
        return 10;
    }
    public long predictASCMoveTime(SimulationContext context, String ascId){
        return 10;
    }
    public long predictQCMoveTime(SimulationContext context, String qcId){
        return 10;
    }
    public long predictTruckWorkTime(SimulationContext context, String truckId){
        return 5;
    }
    public long predictQCWorkTime(SimulationContext context, String qcId){
        return 15;
    }
    public long predictASCWorkTime(SimulationContext context, String ascId){
        return 5;
    }
}
