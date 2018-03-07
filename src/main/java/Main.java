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
import java.util.Random;
import java.util.stream.Collectors;

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
            printWriter.println("popSize;mutProb;coloringPopSize;coloringMutProb;nValues;animSteps;stepN;fitness;chromosome");
            printWriter.close();
            getBest(100, 0.2, 40, 0.2, 3, 10, 100000);
            /*showAnimated("chromosomeColored.json");
            char[] c = new char[100];
            Arrays.fill(c, '-');
            System.out.println(c);
            showAnimated("chromosome.json");*/
            //gatherStats();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void gatherStats() {

        int i = 0;
        for (int popSize : popSizes) {
            getBest(popSize, mutProbs[1], coloringPopSizes[1], coloringMutProbs[1], nValues[1], animSteps[1], 1000);
            System.out.println(++i + "/30");
        }
        for (double mutProb : mutProbs) {
            getBest(popSizes[1], mutProb, coloringPopSizes[1], coloringMutProbs[1], nValues[1], animSteps[1], 1000);
            System.out.println(++i + "/30");
        }
        for (int nv : nValues) {
            getBest(popSizes[1], mutProbs[1], coloringPopSizes[1], coloringMutProbs[1], nv, animSteps[1], 1000);
            System.out.println(++i + "/30");
        }
        for (int cPopSize : coloringPopSizes) {
            getBest(popSizes[1], mutProbs[1], cPopSize, coloringMutProbs[1], nValues[1], animSteps[1], 1000);
            System.out.println(++i + "/30");
        }
        for (double cMutProb : coloringMutProbs) {
            getBest(popSizes[1], mutProbs[1], coloringPopSizes[1], cMutProb, nValues[1], animSteps[1], 1000);
            System.out.println(++i + "/30");
        }
        for (int as : animSteps) {
            getBest(popSizes[1], mutProbs[1], coloringPopSizes[1], coloringMutProbs[1], nValues[1], as, 1000);
            System.out.println(++i + "/30");
        }

    }

    /**
     * @param populationSize
     * @return
     */
    public static Chromosome getBest(int populationSize, double mutProb, int coloringPopSize, double coloringMutProb, int nValues, int animSteps, int n) {
        Chromosome[] population = new Chromosome[populationSize];
        for (int i = 0; i < populationSize; i++) {
            try {
                population[i] = new Chromosome(coloringPopSize, coloringMutProb, nValues, animSteps);
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
            double maxFitness = (new Chromosome(coloringPopSize, coloringMutProb, nValues, animSteps)).getMaxFitness();
            for (int i = 0; i < n; i++) {
                System.out.println((i + 1) + "/" + n);
                NumberFormat formatter = new DecimalFormat("#0.0000");
                if (bestFitness > previousFitness) {
                    PrintWriter printWriter = new PrintWriter(new FileWriter("results.csv", true));
                    PrintWriter landscapeWriter = new PrintWriter(new FileWriter("fitnessLandscape.csv", true));
                    String ks = populationSize + ";" + mutProb + ";" + coloringPopSize + ";" + coloringMutProb + ";" + nValues + ";" + animSteps + ";";
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
            int firstBestIndex = -1;
            int secondBestIndex = -1;
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
            String lines = Files.readAllLines((new File(filename)).toPath()).stream().collect(Collectors.joining());
            Chromosome chromosome = (new Gson()).fromJson(lines, Chromosome.class);
            chromosome.setIniTree(Helper.parseCSV("iniTree7x7.csv", ","));
            chromosome.simulateGrowth();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
