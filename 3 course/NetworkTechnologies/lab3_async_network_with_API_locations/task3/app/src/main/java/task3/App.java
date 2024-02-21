package task3;

public class App {
    public static void main(String[] args) {
        
        String place = "";
        for(String arg : args) {
            place += arg + " ";
        }
        
        Executor executor = new Executor();
        executor.Start(place);
        
    }
}
