import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Random;

public class Main {

    public static final int[] popSizes = {50, 100, 150, 200, 250};
    public static final double[] mutProbs = {0.05, 0.1, 0.15, 0.2, 0.25};
    public static final int[] nValues = {3, 6, 9, 12, 15};
    public static final int[] coloringPopSizes = {50, 100, 150, 200, 250};
    public static final double[] coloringMutProbs = {0.05, 0.1, 0.15, 0.2, 0.25};
    public static final int[] animSteps = {7, 10, 13, 16, 19};

    public static void main(String[] args) {
        try {
            PrintWriter printWriter = new PrintWriter(new FileWriter("results.csv", false));
            printWriter.println("popSize,mutProb,coloringPopSize,coloringMutProb,nValues,animSteps,stepN,chromosome");
            printWriter.close();
            for (int popSize :
                    popSizes) {
                for (double mutProb :
                        mutProbs) {
                    for (int nv :
                            nValues) {
                        for (int coloringPopSize :
                                coloringPopSizes) {
                            for (double coloringMutProb :
                                    coloringMutProbs) {
                                for (int nas :
                                        animSteps) {
                                    getBest(popSize, mutProb, coloringPopSize, coloringMutProb, nv, nas, 1000);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
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
                //System.out.println(i);
                NumberFormat formatter = new DecimalFormat("#0.00");
                if (bestFitness > previousFitness) {
                    System.out.print(formatter.format(bestFitness / maxFitness * 100));
                    System.out.println("%");
                    PrintWriter printWriter = new PrintWriter(new FileWriter("results.csv", true));
                    String ks = populationSize + "," + mutProb + "," + coloringPopSize + "," + coloringMutProb + "," + nValues + "," + animSteps + ",";
                    printWriter.println(ks + i + "," + gson.toJson(fittest));
                    printWriter.close();
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
}
