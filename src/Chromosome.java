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
    public static final int N_VALUES = 9;
    public static final int N_COLORS = 3;
    public static final int length = (int) Math.pow(N_VALUES, N_NEIGHBOURS);
    public static final int left = 0;
    public static final int right = N_VALUES - 1;
    public static final int minMatches = 2;
    public static final int steps = 10;
    private int[] coloringScheme;
    private int[][] perfectTree;
    private int[][] iniTree;
    private int[] genes = new int[length];                                       //should be of length length for this task
    private int[][] env;
    private int[][] advanced;
    private boolean fitnessCached = false;
    private double fitness;

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

    private static int[] getRandomColoring() {
        Random random = new Random();
        int coloring[] = new int[N_VALUES];
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

    public void advance() {
        int[][] nextEnv = new int[env.length][env[0].length];
        advanced = new int[env.length][env[0].length];
        for (int i = 0; i < env.length; i++) {
            for (int j = 0; j < env[i].length; j++) {
                nextEnv[i][j] = getAdvancedBlockValue(i, j);
                advanced[i][j] = nextEnv[i][j];
            }
        }
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
            offset += neighbours[i] * Math.pow(N_VALUES, i);
        }
        return genes[offset];
    }

    private void initEnv() {
        env = iniTree.clone();
    }

    private void initAdvancedEnv() {
        env = advanced.clone();
    }

    public double getMaxDistance() {
        return Math.sqrt(env.length * env[0].length * Math.pow(2, 2));
    }

    public double getFitness() {
        if (fitnessCached)
            return fitness;
        initEnv();
        for (int i = 0; i < steps; i++) {
            advance();
        }
        coloringScheme = getBestColoring(100, 1000, 0.1);
        fitnessCached = true;
        fitness = getAdvancedFitness(coloringScheme);
        return fitness;
    }

    private double getAdvancedFitness(int[] coloringScheme) {
        initAdvancedEnv();
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
            mutated = new Chromosome(mutGenes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mutated;
    }

    int[] getMutatedColoring(int[] coloring, double p) {
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
            return new Chromosome(crossGenes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    int[] getColoringWithUniformCrossover(int[] first, int[] second) {

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
        int[][] population = new int[populationSize][N_VALUES];
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
            double maxFitness = (new Chromosome()).getMaxFitness();
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

    public void generateNextPopulation(int[][] population, int nTournament, double mutProb) {
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

    public int[] getFittest(int[][] population) {
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
