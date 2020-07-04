/*
 * Created by Denis Zvyagintsev on 03.07.2020
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LogCounter {


    public boolean isFinished = false;
    ConcurrentHashMap<String, LogElement> logs;
    ConcurrentHashMap<LocalTime, ConcurrentHashMap<String, ArrayList<Long>>> data;
    private String path = "results/";
    private BufferedWriter writer;
    private String header = "\"Время\", \"Тип ответа\", \"количество сообщений в секунду\", \"среднее\", \"медиана\", \"90% перцентиль\", \"99% перцентиль\", \"максимум\"\n";

    LogCounter(File file) {
        try {
            data = new ConcurrentHashMap<>();
            logs = new ConcurrentHashMap<>(10, 0.7f, 1);
            path += file.getName();
            writer = new BufferedWriter(new FileWriter(path));
            writer.write(header);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void addLogElement(LogElement logElement) {


        if (!logs.containsKey(logElement.id)) {
            logs.put(logElement.id, logElement);
            return;
        }

        LogElement input = logElement;
        LogElement output = logs.get(logElement.id);
        DataPart dataPart = new DataPart(input, output);
        logs.remove(logElement.id);

        addToData(dataPart);

    }

    private void addToData(DataPart dataPart) {
        LocalTime bucket = dataPart.time.minusNanos(dataPart.time.getNano());
        if (!data.containsKey(bucket)) {
            data.put(bucket, new ConcurrentHashMap<>());
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

    public void dataChecker(ConcurrentHashMap<Long, LocalTime> threadsTime) {
        try {


            while (true) {
                LocalTime minDataTime = getDataMinTime();
                LocalTime minThreadsTime = getThreadsMinTime(threadsTime);

                if (isFinished) {
                    if (minDataTime == null) {
                        break;
                    }
                    printBucket(minDataTime);
                    continue;
                }

                if (minDataTime == null || minThreadsTime == null || minDataTime.plusSeconds(1).isAfter(minThreadsTime)) {
                    Thread.sleep(1000);
                    continue;
                }

                printBucket(minDataTime);

            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void printBucket(LocalTime time) {

        ConcurrentHashMap<String, ArrayList<Long>> secondData = data.get(time);
        data.remove(time);
        for (Map.Entry<String, ArrayList<Long>> entry : secondData.entrySet()) {
            String key = entry.getKey();
            ArrayList<Long> value = entry.getValue();
            printType(time, key, value);
        }

    }

    private void printType(LocalTime time, String type, ArrayList<Long> completionTimes) {
        try {
            writer.write(time.toString() + ", ");
            writer.write(type + ", ");
            writer.write(completionTimes.size() + ", ");
            Collections.sort(completionTimes);
            long minTime = completionTimes.get(0);
            long maxTime = completionTimes.get(completionTimes.size() - 1);
            int per50index = (int) (completionTimes.size() * 0.5);
            int per90index = (int) (completionTimes.size() * 0.9);
            int per99index = (int) (completionTimes.size() * 0.99);
            long per50 = completionTimes.get(per50index);
            long per90 = completionTimes.get(per90index);
            long per99 = completionTimes.get(per99index);
            double mean = 0;
            for (int i = 0; i < completionTimes.size(); i++) {
                mean += completionTimes.get(i);
            }
            mean /= completionTimes.size();
            writer.write(mean + ", ");
            writer.write(per50 + ", ");
            writer.write(per90 + ", ");
            writer.write(per99 + ", ");
            writer.write(maxTime + "\n");
        } catch (Exception e) {
            e.printStackTrace();
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

    private LocalTime getThreadsMinTime(ConcurrentHashMap<Long, LocalTime> threadsTime) {
        LocalTime minTime = null;
        for (LocalTime time : threadsTime.values()) {
            if (minTime == null || minTime.isAfter(time)) {
                minTime = time;
            }
        }
        return minTime;
    }

}
