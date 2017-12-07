import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        int pop = 50;
        double mutProb = 0.2;
        Chromosome chromosome = getBest(pop, 0.75, mutProb);
        int[][] env = chromosome.getEnv();
        try {
            PrintWriter printWriter = new PrintWriter(new FileWriter("chromosome.txt", true));
            printWriter.printf("pop=%d mutProb=%f minMatches=%d steps=%d initPattern=std%n", pop, mutProb, Chromosome.minMatches, Chromosome.steps);
            for (int i = 0; i < env.length; i++) {
                for (int j = 0; j < env[i].length; j++) {
                    System.out.print(env[i][j]);
                    printWriter.print(env[i][j]);
                }
                System.out.println();
                printWriter.println();
            }
            System.out.println(Arrays.toString(chromosome.getGenes()));
            printWriter.println(Arrays.toString(chromosome.getGenes()));
            System.out.println(chromosome.getFitness());
            printWriter.println(chromosome.getFitness());
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
            population[i] = new Chromosome();
        }
        Chromosome best = population[0];
        for (Chromosome c :
                population) {
            if (c.getFitness() > best.getFitness()) best = c;
        }
        double maxFitness = Chromosome.getMaxFitness();
        double bestFitness = best.getFitness();
        while (bestFitness < accuracy * maxFitness) {
            System.out.println(bestFitness + " / " + maxFitness);
            Chromosome[] nextPopulation = new Chromosome[populationSize];
            for (int j = 0; j < populationSize; j++) {
                Chromosome x = getRandom(population);
                Chromosome y = getRandom(population);
                Chromosome child = x.getWithCrossover(y);
                child = child.getMutated(mutProb);
                nextPopulation[j] = child;
            }
            population = nextPopulation;
            for (Chromosome c :
                    population) {
                if (c.getFitness() > best.getFitness()) best = c;
            }
            bestFitness = best.getFitness();
        }
        return best;
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
