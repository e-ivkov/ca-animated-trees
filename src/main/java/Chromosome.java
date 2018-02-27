import java.util.Arrays;
import java.util.Random;

/**
 * Project: SAW Genetic Algorithms
 *
 * @author Егор Ивков
 * @since 02.10.2017
 */
public class Chromosome {

    public static final int N_NEIGHBOURS = 4;
    public static final int N_COLORS = 3;
    public static final int left = 0;
    private boolean coloringDisabled = false;
    private int nValues;
    private int length;
    private int right;
    private int steps;
    private int[] coloringScheme;
    private transient int[][] perfectTree;
    private transient int[][] iniTree;
    private int[] genes;                                       //should be of length length for this task
    private transient int[][] env;
    private int[][] advanced;
    private boolean fitnessCached = false;
    private double fitness;
    private int coloringPopSize;
    private double coloringMutProb;

    public Chromosome(int[] genes, int coloringPopSize, double coloringMutProb, int nValues, int steps) throws Exception {
        this.coloringPopSize = coloringPopSize;
        this.coloringMutProb = coloringMutProb;
        this.nValues = nValues;
        this.steps = steps;
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
        perfectTree = Helper.parseCSV("perfectTree7x7.csv");
        iniTree = Helper.parseCSV("iniTree7x7.csv");
        env = new int[perfectTree.length][perfectTree[0].length];
    }

    public Chromosome(int coloringPopSize, double coloringMutProb, int nValues, int steps) throws Exception {
        this(getRandomGenes((int) Math.pow(nValues, N_NEIGHBOURS), nValues - 1), coloringPopSize, coloringMutProb, nValues, steps);
    }

    private static int[] getRandomGenes(int length, int right) {
        Random random = new Random();
        int genes[] = new int[length];
        for (int i = 0; i < length; i++) {
            genes[i] = random.nextInt(right - left + 1) + left;
        }
        return genes;
    }

    public void setPerfectTree(int[][] perfectTree) {
        this.perfectTree = perfectTree;
    }

    public void setIniTree(int[][] iniTree) {
        this.iniTree = iniTree;
    }

    private int[] getRandomColoring() {
        Random random = new Random();
        int coloring[] = new int[nValues];
        for (int i = 0; i < coloring.length; i++) {
            coloring[i] = random.nextInt(N_COLORS);
        }
        return coloring;
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

    private void advance() {
        int[][] nextEnv = new int[env.length][env[0].length];
        advanced = new int[env.length][env[0].length];
        for (int i = 0; i < env.length; i++) {
            int finalI = i;
            Arrays.parallelSetAll(nextEnv[i], (j) -> getAdvancedBlockValue(finalI, j));
        }
        advanced = nextEnv.clone();
        env = nextEnv;
    }

    private void color(int[] coloringScheme) {
        int[][] nextEnv = new int[env.length][env[0].length];
        for (int i = 0; i < env.length; i++) {
            for (int j = 0; j < env[i].length; j++) {
                nextEnv[i][j] = coloringScheme[env[i][j]];
            }
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
        env = iniTree.clone();
    }

    private void initAdvancedEnv() {
        env = advanced.clone();
    }

    private double getMaxDistance() {
        return Math.sqrt(env.length * env[0].length * Math.pow(2, 2));
    }

    /**
     * requires coloring to be ready
     */
    public void simulateGrowth() {
        initEnv();
        int[][] temp;
        for (int i = 0; i < steps; i++) {
            advance();
            temp = env.clone();
            if (!coloringDisabled)
                color(coloringScheme);
            for (int[] t :
                    env) {
                System.out.println(Arrays.toString(t));
            }
            System.out.println();
            env = temp.clone();
        }
    }

    public double getFitness() {
        if (fitnessCached)
            return fitness;
        initEnv();
        for (int i = 0; i < steps; i++) {
            advance();
        }
        if (!coloringDisabled)
            coloringScheme = getBestColoring(coloringPopSize, 200, coloringMutProb);
        fitnessCached = true;
        fitness = getAdvancedFitness(coloringScheme);
        return fitness;
    }

    private double getAdvancedFitness(int[] coloringScheme) {
        initAdvancedEnv();
        if (!coloringDisabled)
            color(coloringScheme);
        double distance = 0;
        for (int i = 0; i < env.length; i++) {
            for (int j = 0; j < env[i].length; j++) {
                distance += Math.pow(perfectTree[i][j] - env[i][j], 2);
            }
        }
        distance = Math.sqrt(distance);
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
            mutated = new Chromosome(mutGenes, coloringPopSize, coloringMutProb, nValues, steps);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mutated;
    }

    private int[] getMutatedColoring(int[] coloring, double p) {
        int[] mutGenes = coloring.clone();
        for (int i = 0; i < mutGenes.length; i++) {
            if (Math.random() <= p) {
                Random random = new Random();
                mutGenes[i] = random.nextInt(N_COLORS);
            }
        }
        return mutGenes;
    }

    Chromosome getWithHalfCrossover(Chromosome other) {

        int crossGenes[] = new int[length];
        System.arraycopy(genes, 0, crossGenes, 0, length / 2);
        System.arraycopy(other.getGenes(), length / 2, crossGenes, length / 2, length - (length / 2));
        for (int i = length / 2; i < length; i++) {
            crossGenes[i] = other.getGenes()[i];
        }
        try {
            return new Chromosome(crossGenes, coloringPopSize, coloringMutProb, nValues, steps);
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
            return new Chromosome(crossGenes, coloringPopSize, coloringMutProb, nValues, steps);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private int[] getColoringWithUniformCrossover(int[] first, int[] second) {

        int crossGenes[] = new int[first.length];
        for (int i = 0; i < first.length; i++) {
            if (Math.random() > 0.5)
                crossGenes[i] = first[i];
            else
                crossGenes[i] = second[i];
        }
        try {
            return crossGenes;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int[] getGenes() {
        return genes.clone();
    }

    public int[] getBestColoring(int populationSize, int nSteps, double mutProb) {
        int[][] population = new int[populationSize][nValues];
        for (int i = 0; i < populationSize; i++) {
            try {
                population[i] = getRandomColoring();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        double previousFitness = 0;
        int[] fittest = getFittest(population);
        double bestFitness = getAdvancedFitness(fittest);
        try {
            for (int i = 0; i < nSteps; i++) {
                generateNextPopulation(population, 4, mutProb);
                previousFitness = bestFitness;
                fittest = getFittest(population);
                bestFitness = getAdvancedFitness(fittest);
                //System.out.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getFittest(population);
    }

    private void generateNextPopulation(int[][] population, int nTournament, double mutProb) {
        Helper.shuffleArray(population);
        for (int i = 0; i < population.length / nTournament; i++) {
            double firstFitness = Double.MIN_VALUE;
            double secondFitness = Double.MIN_VALUE;
            int firstBestIndex = -1;
            int secondBestIndex = -1;
            for (int j = 0; j < nTournament; j++) {
                double f = getAdvancedFitness(population[i * nTournament + j]);
                if (f > firstFitness) {
                    firstFitness = f;
                    firstBestIndex = j;
                }
            }
            for (int j = 0; j < nTournament; j++) {
                double f = getAdvancedFitness(population[i * nTournament + j]);
                if (f > secondFitness && j != firstBestIndex) {
                    secondFitness = f;
                    secondBestIndex = j;
                }
            }
            int[] firstBest = population[i * nTournament + firstBestIndex];
            int[] secondBest = population[i * nTournament + secondBestIndex];
            for (int j = 0; j < nTournament; j++) {
                if (j != firstBestIndex && j != secondBestIndex)
                    population[i * nTournament + j] = getMutatedColoring(getColoringWithUniformCrossover(firstBest, secondBest), mutProb);
            }
        }
    }

    private int[] getFittest(int[][] population) {
        int[] best = population[0];
        double bestFitness = getAdvancedFitness(best);
        for (int[] c :
                population) {
            double cFitness = getAdvancedFitness(c);
            if (cFitness > bestFitness) {
                best = c;
                bestFitness = cFitness;
            }
            /*System.out.print(c.getFitness()+" ");
            System.out.println();*/
        }
        return best;
    }
}
