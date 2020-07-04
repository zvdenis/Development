/*
 * Created by Denis Zvyagintsev on 03.07.2020
 */

import java.io.File;

public class Application {

    private static final String folderToParse = "filesToParse";
    public static Parser parser;


    public static void main(String[] args) {
        File folder = new File(folderToParse + "/");
        File[] listOfFiles = folder.listFiles();
        parser = new Parser();
        parseFiles(listOfFiles);
    }

    public static void parseFiles(File[] listOfFiles) {


        System.out.println("List of files:");
        for (var file : listOfFiles) {
            System.out.println(file.getName());
        }
        System.out.println();

        for (var file : listOfFiles) {
            LogCounter logCounter = new LogCounter(file);
            System.out.println("Parsing " + file.getName() + "  Please wait ..");
            long start = System.currentTimeMillis();
            parser.parseFile(file, logCounter);
            long time = System.currentTimeMillis() - start;
            System.out.println("Finished in " + time);
        }
    }

}
