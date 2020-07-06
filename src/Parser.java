/*
 * Created by Denis Zvyagintsev on 03.07.2020
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Parser {
    private final String[] listToIgnore = new String[]{"SQLProxy", "P2_COD"};

    private AtomicInteger ignoredLinesCount;

    LogCounter logCounter;

    public void parseFile(File file, LogCounter logCounter) {

        ignoredLinesCount = new AtomicInteger(0);
        this.logCounter = logCounter;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 3 + 1);

            Thread dataCheckerThread = new Thread(logCounter::dataChecker);
            dataCheckerThread.start();

            for (String line; (line = br.readLine()) != null; ) {
                final String lineToParse = line;
                threadPool.execute(() -> parseLine(lineToParse));
            }

            threadPool.shutdown();
            threadPool.awaitTermination(3, TimeUnit.DAYS);

            logCounter.isFinished = true;
            dataCheckerThread.join();
            System.out.println("Lines ignored " + ignoredLinesCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void parseLine(String line) {

        for (var word : listToIgnore) {
            if (line.contains(word)) {
                ignoredLinesCount.incrementAndGet();
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

            logCounter.addLogElement(element);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
