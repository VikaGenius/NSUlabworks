package task3;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (Scanner reader = new Scanner(new InputStreamReader(System.in))) {
            System.out.println("Enter the location you want to know more about: ");

            String location  = reader.nextLine();

            Executor executor = new Executor();
            executor.Execute(location);

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

    }
}
