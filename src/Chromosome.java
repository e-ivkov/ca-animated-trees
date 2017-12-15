import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Project: SAW Genetic Algorithms
 *
 * @author Егор Ивков
 * @since 02.10.2017
 */
public class Chromosome {

    public static final int N_NEIGHBOURS = 4;
    public static final int N_VALUES = 12;
    public static final int N_COLORS = 3;
    public static final int length = N_NEIGHBOURS * N_VALUES;
    public static final int left = 0;
    public static final int right = N_VALUES - 1;
    public static final int minMatches = 2;
    public static final int steps = 10;
    public static final int[] coloringScheme = {0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2};
    private int[][] perfectTree;
    private int[][] iniTree;
    private int[] genes = new int[length];                                       //should be of length length for this task
    private int[][] env;

    public Chromosome(int[] genes) throws Exception {
        for (int gene :
                genes) {
            if (gene > right || gene < left)
                throw new Exception("Genes string should contain only values specified in possibleGenes");
        }
        if (genes.length == length)
            this.genes = genes;
        else
            throw new Exception("Genes string should be of length " + length + " for this task");
        perfectTree = parseCSV("perfectTree7x7.csv", ",");
        iniTree = parseCSV("iniTree7x7.csv", ",");
        env = new int[perfectTree.length][perfectTree[0].length];
    }

    public Chromosome() throws Exception {
        this(getRandomGenes());
    }

    private static int[][] parseCSV(String filename, String delimiter) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(filename));
        List<List<Integer>> parsed = new ArrayList<>();
        while (scanner.hasNextLine()) {
            parsed.add(Arrays.stream(scanner.nextLine().split(delimiter)).map(Integer::parseInt).collect(Collectors.toList()));
        }
        int arr[][] = new int[parsed.size()][parsed.size()];
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[0].length; j++) {
                arr[i][j] = parsed.get(i).get(j);
            }
        }
        return arr;
    }

    private static int[] getRandomGenes() {
        Random random = new Random();
        int genes[] = new int[length];
        for (int i = 0; i < length; i++) {
            genes[i] = random.nextInt(right - left + 1) + left;
        }
        return genes;
    }

    public double getMaxFitness() {
        return getMaxDistance();
    }

    public int[][] getEnv() {
        return env.clone();
    }

    public int[] getNeighbours(int x, int y) {
        int[] neighbours = new int[4];
        neighbours[0] = getBlock(x + 1, y);
        neighbours[1] = getBlock(x - 1, y);
        neighbours[2] = getBlock(x, y + 1);
        neighbours[3] = getBlock(x, y - 1);
        return neighbours;
    }

    public int getBlock(int x, int y) {
        if (isValid(x, y))
            return env[x][y];
        return 0;
    }

    public boolean isValid(int x, int y) {
        return (x < env.length && x >= 0) && (y < env[0].length && y >= 0);
    }

    public void advance() {
        int[][] nextEnv = new int[env.length][env[0].length];
        for (int i = 0; i < env.length; i++) {
            for (int j = 0; j < env[i].length; j++) {
                nextEnv[i][j] = getAdvancedBlockValue(i, j);
            }
        }
        env = nextEnv;
    }

    private void color() {
        int[][] nextEnv = new int[env.length][env[0].length];
        for (int i = 0; i < env.length; i++) {
            for (int j = 0; j < env[i].length; j++) {
                nextEnv[i][j] = coloringScheme[env[i][j]];
            }
        }
        env = nextEnv;
    }

    private int getAdvancedBlockValue(int x, int y) {
        int value = getBlock(x, y);
        int[] neighbours = getNeighbours(x, y);
        for (int i = 0; i < N_VALUES; i++) {
            int offset = N_NEIGHBOURS * i;
            int matches = 0;
            for (int j = 0; j < N_NEIGHBOURS; j++) {
                if (genes[offset + j] == neighbours[j]) {
                    matches++;
                }
            }
            if (matches >= minMatches) {
                value = i;
            }
        }
        return value;
    }

    private void initEnv() {
        env = iniTree;
    }

    public double getMaxDistance() {
        return env.length * env[0].length * Math.pow(2, 2);
    }

    public double getFitness() {
        double distance = 0;
        initEnv();
        for (int i = 0; i < steps; i++) {
            advance();
        }
        color();
        for (int i = 0; i < env.length; i++) {
            for (int j = 0; j < env[i].length; j++) {
                distance += Math.pow(perfectTree[i][j] - env[i][j], 2);
            }
        }
        return getMaxDistance() - distance;
    }

    Chromosome getMutated(double p) {
        int[] mutGenes = genes.clone();
        for (int i = 0; i < mutGenes.length; i++) {
            if (Math.random() <= p) {
                Random random = new Random();
                mutGenes[i] = random.nextInt(right - left + 1) + left;
            }
        }
        Chromosome mutated = null;
        try {
            mutated = new Chromosome(mutGenes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mutated;
    }

    Chromosome getWithCrossover(Chromosome other) {

        int crossGenes[] = new int[length];
        for (int i = 0; i < length / 2; i++) {
            crossGenes[i] = genes[i];
        }
        for (int i = length / 2; i < length; i++) {
            crossGenes[i] = other.getGenes()[i];
        }
        try {
            return new Chromosome(crossGenes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int[] getGenes() {
        return genes.clone();
    }
}
