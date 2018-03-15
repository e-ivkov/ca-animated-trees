import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Project: SAW Genetic Algorithms
 *
 * @author Егор Ивков
 * @since 02.10.2017
 */
public class Chromosome {

    public static final int N_NEIGHBOURS = 4;
    public static final int left = 0;
    public static final int nValues = 3;
    public static final int steps = 6;
    public static final int envSize = 7;
    private int length;
    private int right;
    private transient int[][][] treeGrowthBorders;
    private int[] genes;                                       //should be of length length for this task
    private int[][] env;
    private boolean fitnessCached = false;
    private double fitness;
    private double[] weights;

    public Chromosome(int[] genes, double[] weights) throws Exception {
        length = (int) Math.pow(nValues, N_NEIGHBOURS);
        right = nValues - 1;
        for (int gene :
                genes) {
            if (gene > right || gene < left)
                throw new Exception("Genes string should contain only values specified in possibleGenes");
        }
        if (genes.length == length)
            this.genes = genes;
        else
            throw new Exception("Genes string should be of length " + length + " for this task");
        initEnv();
        this.weights = weights.clone();
        parseBorders(new String[]{"1-2.csv", "3-4.csv", "5-6.csv"});

    }

    public Chromosome(double[] weights) throws Exception {
        this(getRandomGenes((int) Math.pow(nValues, N_NEIGHBOURS), nValues - 1), weights);
    }

    private static int[] getRandomGenes(int length, int right) {
        Random random = new Random();
        int genes[] = new int[length];
        for (int i = 0; i < length; i++) {
            genes[i] = random.nextInt(right - left + 1) + left;
        }
        return genes;
    }

    private void parseBorders(String[] filenames) {
        treeGrowthBorders = new int[filenames.length][][];
        try {
            for (int i = 0; i < filenames.length; i++) {
                treeGrowthBorders[i] = Helper.parseCSV(filenames[i], ";");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public double getMaxFitness() {
        return 1;
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

    public List<Integer> getUpperConnectednessNeighborhood(int x, int y) {
        List<Integer> neighbours = new ArrayList<>();
        neighbours.add(getBlock(x + 1, y + 1));
        neighbours.add(getBlock(x + 1, y));
        neighbours.add(getBlock(x + 1, y - 1));
        return neighbours;
    }

    public List<Integer> getLowerConnectednessNeighborhood(int x, int y) {
        List<Integer> neighbours = new ArrayList<>();
        neighbours.add(getBlock(x - 1, y + 1));
        neighbours.add(getBlock(x - 1, y));
        neighbours.add(getBlock(x - 1, y - 1));
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

    private void advance() {
        int[][] nextEnv = new int[env.length][env[0].length];
        for (int i = 0; i < env.length; i++) {
            int finalI = i;
            Arrays.parallelSetAll(nextEnv[i], (j) -> getAdvancedBlockValue(finalI, j));
        }
        env = nextEnv;
    }

    private int getAdvancedBlockValue(int x, int y) {
        int[] neighbours = getNeighbours(x, y);
        int offset = 0;
        for (int i = 0; i < N_NEIGHBOURS; i++) {
            offset += neighbours[i] * Math.pow(nValues, i);
        }
        return genes[offset];
    }

    private void initEnv() {
        env = new int[envSize][envSize];
        env[envSize - 1][envSize / 2 + 1] = 2;
    }

    private double getConnectedness() {
        int woodBlocks = 0;
        double connectedBlocks = 0;
        for (int i = 0; i < env.length; i++) {
            for (int j = 0; j < env[0].length; j++) {
                if (getBlock(i, j) == 2) {
                    woodBlocks++;
                    if (getUpperConnectednessNeighborhood(i, j).contains(2))
                        connectedBlocks += 0.5;
                    if (getLowerConnectednessNeighborhood(i, j).contains(2))
                        connectedBlocks += 0.5;
                }
            }
        }
        if (woodBlocks == 0)
            return 0;
        return connectedBlocks / woodBlocks;
    }

    public void simulateGrowth() {
        initEnv();
        int[][] temp;
        for (int i = 0; i < steps; i++) {
            advance();
            temp = env.clone();
            for (int[] t :
                    env) {
                System.out.println(Arrays.toString(t));
            }
            System.out.println();
            env = temp.clone();
        }
    }

    //works only without coloring
    private boolean checkBorders(int step) {
        for (int i = 0; i < env.length; i++) {
            for (int j = 0; j < env[0].length; j++) {
                if (treeGrowthBorders[step / 2][i][j] == 0 && env[i][j] != 0)
                    return false;
            }
        }
        return true;
    }

    private double getColoringFitness() {
        double maxFitness = 0;
        double currentFitness = 0;
        for (int i = 0; i < env.length; i++) {
            for (int j = 0; j < env[0].length; j++) {
                double hkWood = i / env.length;
                double hkLeaf = 1 - hkWood;
                double wkWood = Math.abs(env[0].length / 2 - j) / (env[0].length / 2);
                double wkLeaf = 1 - wkWood;
                if (treeGrowthBorders[2][i][j] > 0) {
                    maxFitness += Math.max(hkWood * wkWood, hkLeaf * wkLeaf);
                    switch (getBlock(i, j)) {
                        case 1:
                            currentFitness += hkLeaf * wkLeaf;
                            break;
                        case 2:
                            currentFitness += hkWood * hkWood;
                            break;
                    }
                }
            }
        }
        return currentFitness / maxFitness;
    }

    public double getFitness() {
        if (fitnessCached)
            return fitness;
        initEnv();
        boolean inBounds = true;
        int stepsInBounds = 0;
        double connectedness = 0;
        for (int i = 0; i < steps; i++) {
            advance();
            if (!checkBorders(i))
                inBounds = false;
            if (inBounds)
                stepsInBounds++;
            connectedness += getConnectedness();
        }
        fitnessCached = true;
        double apoptoticK = stepsInBounds / steps;
        double connectednessK = connectedness / steps;
        double coloringK = getColoringFitness();
        fitness = weights[0] * connectednessK + weights[1] * apoptoticK + weights[2] * coloringK;
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
            mutated = new Chromosome(mutGenes, weights);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mutated;
    }

    Chromosome getWithHalfCrossover(Chromosome other) {

        int crossGenes[] = new int[length];
        System.arraycopy(genes, 0, crossGenes, 0, length / 2);
        System.arraycopy(other.getGenes(), length / 2, crossGenes, length / 2, length - (length / 2));
        for (int i = length / 2; i < length; i++) {
            crossGenes[i] = other.getGenes()[i];
        }
        try {
            return new Chromosome(crossGenes, weights);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    Chromosome getWithUniformCrossover(Chromosome other) {

        int crossGenes[] = new int[length];
        int otherGenes[] = other.getGenes();
        for (int i = 0; i < length; i++) {
            if (Math.random() > 0.5)
                crossGenes[i] = genes[i];
            else
                crossGenes[i] = otherGenes[i];
        }
        try {
            return new Chromosome(crossGenes, weights);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int[] getGenes() {
        return genes.clone();
    }

}
