/*
 * Created by Denis Zvyagintsev on 03.07.2020
 */

import java.time.LocalTime;

public class LogElement {

    boolean isInputProcess;
    LocalTime time;
    String id;
    String type;

    public LogElement(boolean isInputProcess, LocalTime time, String id, String type) {
        this.isInputProcess = isInputProcess;
        this.time = time;
        this.id = id;
        this.type = type;
    }

    public boolean isInputProcess() {
        return isInputProcess;
    }

    public LocalTime getTime() {
        return time;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }


}
