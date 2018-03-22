import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Main {

    public static final int[] popSizes = {20, 40, 60, 100, 120};
    public static final double[] mutProbs = {0.05, 0.1, 0.15, 0.2, 0.25};
    public static final int[] nValues = {3, 6, 9, 12, 15};
    public static final int[] coloringPopSizes = {20, 40, 60, 100, 120};
    public static final double[] coloringMutProbs = {0.05, 0.1, 0.15, 0.2, 0.25};
    public static final int[] animSteps = {7, 10, 13, 16, 19};

    public static void main(String[] args) {
        try {
            PrintWriter printWriter = new PrintWriter(new FileWriter("results.csv", false));
            PrintWriter landscapeWriter = new PrintWriter(new FileWriter("fitnessLandscape.csv", false));
            printWriter.println("popSize;mutProb;weights;stepN;fitness;chromosome");
            printWriter.close();
            /*getBest(1000, 0.2, 3000, new double[]{0.5,0.25,0.25}); //connectedness accent
            getBest(1000, 0.2, 3000, new double[]{0.25,0.5,0.25}); //apoptotic accent
            getBest(1000, 0.2, 3000, new double[]{0.25,0.25,0.5}); //coloring accent*/
            getBest(100, 0.2, 500000, new double[]{0.1, 0.6, 0.3}); //optimal
            //showAnimated("chromosome.json");
            //gatherStats();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param populationSize
     * @return
     */
    public static Chromosome getBest(int populationSize, double mutProb, int n, double weights[]) {
        Chromosome[] population = new Chromosome[populationSize];
        for (int i = 0; i < populationSize; i++) {
            try {
                population[i] = new Chromosome(weights);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        double previousFitness = 0;
        Chromosome fittest = getFittest(population);
        double bestFitness = fittest.getFitness();
        try {
            Gson gson = new GsonBuilder()
                    .excludeFieldsWithModifiers(Modifier.TRANSIENT)
                    .create();
            double maxFitness = (new Chromosome(weights)).getMaxFitness();
            for (int i = 0; i < n; i++) {
                System.out.println((i + 1) + "/" + n);
                NumberFormat formatter = new DecimalFormat("#0.0000");
                if (bestFitness > previousFitness) {
                    PrintWriter printWriter = new PrintWriter(new FileWriter("results.csv", true));
                    PrintWriter landscapeWriter = new PrintWriter(new FileWriter("fitnessLandscape.csv", true));
                    String ks = populationSize + ";" + mutProb + ";" + Arrays.toString(weights) + ";";
                    printWriter.println(ks + i + ";" + formatter.format(fittest.getFitness() / fittest.getMaxFitness() * 100) + "%;" + gson.toJson(fittest));
                    printWriter.close();
                    for (Chromosome ch :
                            population) {
                        landscapeWriter.print(formatter.format(ch.getFitness() / fittest.getMaxFitness() * 100) + ";");
                    }
                    landscapeWriter.println();
                    landscapeWriter.close();
                }
                generateNextPopulation(population, 4, mutProb);
                previousFitness = bestFitness;
                fittest = getFittest(population);
                bestFitness = fittest.getFitness();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getFittest(population);
    }

    public static Chromosome getFittest(Chromosome[] population) {
        Chromosome best = population[0];
        double bestFitness = best.getFitness();
        for (Chromosome c :
                population) {
            double cFitness = c.getFitness();
            if (cFitness > bestFitness) {
                best = c;
                bestFitness = cFitness;
            }
            /*System.out.print(c.getFitness()+" ");
            System.out.println();*/
        }
        return best;
    }

    //size n tournament selection with uniform crossover and mutations
    public static void generateNextPopulation(Chromosome[] population, int nTournament, double mutProb) {
        Helper.shuffleArray(population);
        for (int i = 0; i < population.length / nTournament; i++) {
            double firstFitness = Double.MIN_VALUE;
            double secondFitness = Double.MIN_VALUE;
            int firstBestIndex = 0;
            int secondBestIndex = 0;
            for (int j = 0; j < nTournament; j++) {
                double f = population[i * nTournament + j].getFitness();
                if (f > firstFitness) {
                    firstFitness = f;
                    firstBestIndex = j;
                }
            }
            for (int j = 0; j < nTournament; j++) {
                double f = population[i * nTournament + j].getFitness();
                if (f > secondFitness && j != firstBestIndex) {
                    secondFitness = f;
                    secondBestIndex = j;
                }
            }
            Chromosome firstBest = population[i * nTournament + firstBestIndex];
            Chromosome secondBest = population[i * nTournament + secondBestIndex];
            for (int j = 0; j < nTournament; j++) {
                if (j != firstBestIndex && j != secondBestIndex)
                    population[i * nTournament + j] = firstBest.getWithUniformCrossover(secondBest).getMutated(mutProb);
            }
        }
    }

    public static Chromosome getRandom(Chromosome[] population) {
        int totalFitness = 0;
        for (Chromosome c :
                population) {
            totalFitness += c.getFitness();
        }
        Random random = new Random();
        int number = random.nextInt(totalFitness);
        double fitness = population[0].getFitness();
        int i = 0;
        while (number > fitness) {
            fitness += population[++i].getFitness();
        }
        return population[i];
    }

    public static void showAnimated(String filename) {
        try {
            List<String> lines = Files.readAllLines((new File(filename)).toPath());
            for (String line :
                    lines) {
                for (int i = 0; i < 100; i++)
                    System.out.print("-");
                System.out.println();
                Chromosome chromosome = (new Gson()).fromJson(line, Chromosome.class);
                chromosome.simulateGrowth();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
