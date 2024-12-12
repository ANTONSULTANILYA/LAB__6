import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public abstract class FileReader {
    public static List<String> readFile(String path) throws IOException {
        try{
            return Files.readAllLines(Paths.get(path));
        }catch (IOException ex){
            System.out.println(ex.getMessage());
            throw new IOException("Не удалось открыть файл");
        }
    }
}