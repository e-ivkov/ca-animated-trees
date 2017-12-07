import java.util.Random;

/**
 * Project: SAW Genetic Algorithms
 *
 * @author Егор Ивков
 * @since 02.10.2017
 */
public class Chromosome {

    public static final int length = 4 * 3 * 3;
    public static final int left = 0;
    public static final int right = 2;
    public static final int minMatches = 3;
    public static final int steps = 10;
    private static final int[][] perfectTree = {{0, 0, 2, 2, 2, 0, 0},
            {0, 2, 2, 1, 2, 2, 0},
            {2, 1, 2, 1, 2, 1, 2},
            {0, 2, 1, 1, 1, 2, 0},
            {0, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 0}};
    private static final int[][] iniTree = {{0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 0, 0, 0, 0},
            {0, 0, 0, 1, 0, 0, 0}};
    private int[] genes = new int[length];                                       //should be of length length for this task
    private int[][] env = new int[7][7];

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

    }

    public Chromosome() {
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            genes[i] = random.nextInt(right - left + 1) + left;
        }
    }

    public static double getMaxFitness() {
        double fitness = 0;
        for (int i = 0; i < perfectTree.length; i++) {
            for (int j = 0; j < perfectTree[i].length; j++) {
                if (perfectTree[i][j] != 0)
                    fitness += 1;
                else
                    fitness += 0.6;
            }
        }
        return fitness;
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

    private int getAdvancedBlockValue(int x, int y) {
        int value = getBlock(x, y);
        int[] neighbours = getNeighbours(x, y);
        for (int i = 0; i < 3; i++) {
            int offset = value * 12 + 4 * i;
            int matches = 0;
            for (int j = 0; j < 4; j++) {
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

    public double getFitness() {
        double fitness = 0;
        initEnv();
        for (int i = 0; i < steps; i++) {
            advance();
        }
        for (int i = 0; i < env.length; i++) {
            for (int j = 0; j < env[i].length; j++) {
                if (env[i][j] == perfectTree[i][j])
                    if (env[i][j] != 0)
                        fitness += 1;
                    else
                        fitness += 0.6;
            }
        }
        return fitness;
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
