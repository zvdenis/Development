/*
 * Created by Denis Zvyagintsev on 03.07.2020
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.*;

public class DataWriter {

    class DataChunk {
        LocalTime time;
        String type;
        ArrayList<Long> completionTimes;

        public DataChunk(LocalTime time, String type, ArrayList<Long> completionTimes) {
            this.time = time;
            this.type = type;
            this.completionTimes = completionTimes;
        }
    }

    boolean closed = false;
    private String path = "results/";
    private BufferedWriter writer;
    private String header = "\"time\"; \"type\"; \"msg per second\"; \"mean\"; \"median\"; \"90% percentile\"; \"99% percentile\"; \"max\";\n";
    private LinkedBlockingQueue<DataChunk> queue = new LinkedBlockingQueue<>();

    public DataWriter(File file) {

        try {
            path += file.getName().replace(".log", ".csv");
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8));
            writer.write(header);
            new Thread(this::processQueue).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processQueue() {
        try {
            while (!closed || !queue.isEmpty()) {
                DataChunk chunk = queue.take();
                printType(chunk.time, chunk.type, chunk.completionTimes);
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                writer.close();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }


    public void addToQueue(LocalTime time, String type, ArrayList<Long> completionTimes) {
        DataChunk chunk = new DataChunk(time, type, completionTimes);
        queue.add(chunk);
    }

    private void printType(LocalTime time, String type, ArrayList<Long> completionTimes) {
        try {
            writer.write(time.format(DateTimeFormatter.ofPattern("HH:mm:ss")) + "; ");
            writer.write(type + "; ");
            writer.write(completionTimes.size() + "; ");
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
            for (Long completionTime : completionTimes) {
                mean += completionTime;
            }
            mean /= completionTimes.size();
            writer.write(mean + "; ");
            writer.write(per50 + "; ");
            writer.write(per90 + "; ");
            writer.write(per99 + "; ");
            writer.write(maxTime + ";\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            closed = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
