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

    public static int[][] parseCSV(String filename, String separator) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(filename));
        List<List<Integer>> parsed = new ArrayList<>();
        while (scanner.hasNextLine()) {
            parsed.add(Arrays.stream(scanner.nextLine().split(separator)).map(Integer::parseInt).collect(Collectors.toList()));
        }
        int arr[][] = new int[parsed.size()][parsed.size()];
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[0].length; j++) {
                arr[i][j] = parsed.get(i).get(j);
            }
        }
        return arr;
    }

    /**
     * Clones the provided array
     *
     * @param src
     * @return a new clone of the provided array
     */
    public static int[][][] cloneArray(int[][][] src) {
        int length = src.length;
        int[][][] target = new int[length][src[0].length][src[0][0].length];
        for (int i = 0; i < length; i++) {
            for (int j = 0; j < src[0].length; j++) {
                System.arraycopy(src[i][j], 0, target[i][j], 0, src[i][j].length);
            }
        }
        return target;
    }
}
