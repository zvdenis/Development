/*
 * Created by Denis Zvyagintsev on 03.07.2020
 */

import java.io.*;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LogCounter {


    public boolean isFinished = false;
    HashMap<String, LogElement> logs;
    HashMap<LocalTime, HashMap<String, ArrayList<Long>>> data;
    private ConcurrentLinkedQueue<LogElement> queue;
    private DataWriter writer;

    LogCounter(File file) {
        try {
            queue = new ConcurrentLinkedQueue<>();
            data = new HashMap<>();
            logs = new HashMap<>();
            writer = new DataWriter(file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void addLogElement(LogElement logElement) {
        queue.add(logElement);
    }


    private void processLogElement(LogElement logElement) {

        if (!logs.containsKey(logElement.id)) {
            logs.put(logElement.id, logElement);
            return;
        }

        LogElement output = logs.get(logElement.id);
        DataPart dataPart = new DataPart(logElement, output);
        logs.remove(logElement.id);

        addToData(dataPart);

    }

    private void addToData(DataPart dataPart) {
        LocalTime bucket = dataPart.time.minusNanos(dataPart.time.getNano());
        if (!data.containsKey(bucket)) {
            data.put(bucket, new HashMap<>());
        }
        if (!data.get(bucket).containsKey(dataPart.type)) {
            data.get(bucket).put(dataPart.type, new ArrayList<>());
        }

        try {
            data.get(bucket).get(dataPart.type).add(dataPart.duration);

        } catch (Exception e) {
//            System.out.println(data);
//            System.out.println(data.get(bucket));
//            System.out.println(data.get(bucket).get(dataPart.type));
            System.out.println(bucket + "   " + bucket.getNano());
            System.out.println(dataPart.type);
            System.out.println(dataPart.duration);
            e.printStackTrace();
        }

    }

    public void dataChecker() {
        try {


            while (true) {
                LocalTime minDataTime = getDataMinTime();
                //LocalTime minThreadsTime = getThreadsMinTime(threadsTime);
                //LocalTime minThreadsTime = getThreadsMinTime();

                if (isFinished || data.keySet().size() > 9) {
                    if (minDataTime == null) {
                        break;
                    }
                    printBucket(minDataTime);
                    continue;
                }


                for (int i = 0; i < 15 && !queue.isEmpty(); i++) {
                    processLogElement(queue.remove());
                }


            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printBucket(LocalTime time) {

        HashMap<String, ArrayList<Long>> secondData = data.get(time);
        data.remove(time);
        for (Map.Entry<String, ArrayList<Long>> entry : secondData.entrySet()) {
            String key = entry.getKey();
            ArrayList<Long> value = entry.getValue();
            writer.addToQueue(time, key, value);
        }

    }



    private LocalTime getDataMinTime() {
        LocalTime minTime = null;
        for (LocalTime time : data.keySet()) {
            if (minTime == null || minTime.isAfter(time)) {
                minTime = time;
            }
        }
        return minTime;
    }

    private LocalTime getThreadsMinTime(HashMap<Long, LocalTime> threadsTime) {
        LocalTime minTime = null;
        for (LocalTime time : threadsTime.values()) {
            if (minTime == null || minTime.isAfter(time)) {
                minTime = time;
            }
        }
        return minTime;

    }

    private LocalTime getThreadsMinTime() {
        LocalTime minTime = null;
        Iterator<LogElement> iterator = queue.iterator();
        for (int i = 0; i < 50 && iterator.hasNext(); i++) {
            LogElement element = iterator.next();
            if (minTime == null && !element.isInputProcess || !element.isInputProcess && minTime.isAfter(element.time)) {
                minTime = element.time;
            }
        }
        return minTime;

    }

}
