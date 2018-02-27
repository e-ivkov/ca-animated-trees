import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Project: ca-animated-trees
 *
 * @author Егор Ивков
 * @since 17.12.2017
 */
public class Helper {

    public static <T> void shuffleArray(T[] ar) {
        Random rnd = new Random();
        for (int i = ar.length - 1; i > 0; i--) {
            int index = rnd.nextInt(i + 1);
            // Simple swap
            T a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

    public static int[][] parseCSV(String filename) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(filename));
        List<List<Integer>> parsed = new ArrayList<>();
        while (scanner.hasNextLine()) {
            parsed.add(Arrays.stream(scanner.nextLine().split(",")).map(Integer::parseInt).collect(Collectors.toList()));
        }
        int arr[][] = new int[parsed.size()][parsed.size()];
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[0].length; j++) {
                arr[i][j] = parsed.get(i).get(j);
            }
        }
        return arr;
    }
}
