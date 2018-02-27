import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LogReader {


    public static void main(String[] args) {
        LogReader reader = new LogReader();
        if (args.length != 0) {
            reader.read(Paths.get(args[0]));
        } else {

            String subfolderName = "logs";
            reader.read(Paths.get(System.getProperty("user.dir"), subfolderName));
        }

    }

    private void read(Path path) {
        System.out.println("reading from " + path);
        try {
            Files.walk(path)
                    .filter(Files::isRegularFile)
                    .forEach(this::readFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readFile(Path path) {
        System.out.println("\n\nCURRENT PATH = " + path);
        try {
            JsonStreamParser streamParser = new JsonStreamParser(new FileReader(path.toString()));
            while (streamParser.hasNext()) {
                JsonElement object = streamParser.next();
                System.out.println(object.getAsJsonObject());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


}
