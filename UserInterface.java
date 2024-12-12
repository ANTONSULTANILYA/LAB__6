import java.io.IOException;
import java.util.List;

public class UserInterface {
    public void run(){
        Magazine magazine = new Magazine();
        try{
            List<String> list = FileReader.readFile("resource.txt");
            System.out.println(magazine.processProgram(list));
        }catch (IOException ex){
            System.out.println("Не удалось открыть файл");
        }
    }
}