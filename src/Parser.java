/*
 * Created by Denis Zvyagintsev on 03.07.2020
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

public class Parser {
    private final String[] listToIgnore = new String[]{"SQLProxy", "P2_COD"};

    private int ignoredLinesCount = 0;
    private ConcurrentHashMap<Long, LocalTime> threadsTime;

    LogCounter logCounter;

    public void parseFile(File file, LogCounter logCounter) {
        threadsTime = new ConcurrentHashMap<>();
        this.logCounter = logCounter;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));) {
            //ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
            ExecutorService threadPool = Executors.newFixedThreadPool(2);
            //threadPool.submit(() -> logCounter.dataChecker());

            Thread dataCheckerThread = new Thread(() -> logCounter.dataChecker(threadsTime));
            dataCheckerThread.start();
            List<Future<Double>> futures = new ArrayList<>();

            for (String line = null; (line = br.readLine()) != null; ) {
                final String lineToParse = line;
                threadPool.execute(() -> parseLine(lineToParse));
            }
            threadPool.shutdown();
            threadPool.awaitTermination(3, TimeUnit.DAYS);

            logCounter.isFinished = true;
            dataCheckerThread.join();
            System.out.println("Lines ignored" + ignoredLinesCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void parseLine(String line) {

        for (var word : listToIgnore) {
            if (line.contains(word)) {
                ignoredLinesCount++;
                return;
            }
        }

        try {

            boolean inputProcess = line.contains("input process");

            String timeString = line.substring(0, line.indexOf(';'));
            LocalTime time = LocalTime.parse(timeString, DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSS"));


            int idStart = line.indexOf("id") + 3;
            int idEnd = line.indexOf(',', idStart);
            String idString = line.substring(idStart, idEnd);

            int typeStart = line.indexOf("type") + 5;
            int typeEnd = line.indexOf(',', typeStart);
            String typeString = line.substring(typeStart, typeEnd);

            LogElement element = new LogElement(inputProcess, time, idString, typeString);

            threadsTime.put(Thread.currentThread().getId(), time);
            logCounter.addLogElement(element);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
