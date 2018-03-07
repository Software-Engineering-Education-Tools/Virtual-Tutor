import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LogWriter {

    Path path;
    BufferedWriter writer;

    public LogWriter(String path, String header){
        this.path = Paths.get(path + ".csv");
        try {
            writer = Files.newBufferedWriter(this.path);
            writeHeaderLine(header);
        } catch (IOException e) {
            System.out.println("Could not write LogFile. Check Path!");
        }

    }

    private void writeHeaderLine(String header) throws IOException {
        writer.write(header);
    }

    public void writeLine(String line){
        try {
            writer.write(line);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
