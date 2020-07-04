/*
 * Created by Denis Zvyagintsev on 03.07.2020
 */

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class DataPart {
    long duration;
    LocalTime time;
    String type;

    public DataPart(LogElement a, LogElement b){

        LogElement input = a;
        LogElement output = b;

        if(b == null){
            System.out.println();
        }
        if(output.isInputProcess()){
            LogElement tmp = input;
            input = output;
            output = tmp;
        }

        duration = input.time.until(output.time, ChronoUnit.MICROS);
        type = output.type;
        time = output.time;
    }
}
