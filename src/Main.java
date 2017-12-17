import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        int pop = 10000;
        double mutProb = 0.3;
        Chromosome chromosome = getBest(pop, 0.95, mutProb);
        int[][] env = chromosome.getEnv();
        try {
            PrintWriter printWriter = new PrintWriter(new FileWriter("chromosome.txt", true));
            printWriter.printf("pop=%d mutProb=%f minMatches=%d steps=%d coloring=%n", pop, mutProb, Chromosome.minMatches, Chromosome.steps);
            for (int i = 0; i < env.length; i++) {
                for (int j = 0; j < env[i].length; j++) {
                    System.out.print(env[i][j]);
                    printWriter.print(env[i][j]);
                }
                System.out.println();
                printWriter.println();
            }
            System.out.println("Genes: " + Arrays.toString(chromosome.getGenes()));
            printWriter.println("Genes: " + Arrays.toString(chromosome.getGenes()));
            System.out.println("Coloring: " + Arrays.toString(Chromosome.coloringScheme));
            printWriter.println("Coloring: " + Arrays.toString(Chromosome.coloringScheme));
            System.out.println("Fitness: " + chromosome.getFitness());
            printWriter.println("Fitness: " + chromosome.getFitness());
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param populationSize
     * @param accuracy       from 0 to 1
     * @return
     */
    public static Chromosome getBest(int populationSize, double accuracy, double mutProb) {
        Chromosome[] population = new Chromosome[populationSize];
        for (int i = 0; i < populationSize; i++) {
            try {
                population[i] = new Chromosome();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        double bestFitness = getFittest(population).getFitness();
        try {
            double maxFitness = (new Chromosome()).getMaxFitness();
            while (bestFitness < accuracy * maxFitness) {
                NumberFormat formatter = new DecimalFormat("#0.00");
                System.out.print(formatter.format(bestFitness / maxFitness * 100));
                System.out.println("%");
                generateNextPopulation(population, 4, mutProb);
                bestFitness = getFittest(population).getFitness();
                //System.out.println();
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
