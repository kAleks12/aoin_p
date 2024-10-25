package org.genetic.alg;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.genetic.alg.entities.*;
import org.genetic.utils.RandomGenerator;
import org.genetic.utils.entities.DistanceMatrix;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Setter
@Getter
public class GeneticAlgorithm {
    private InitializationType initType;
    private MutationType mutType;
    private CrossoverType crossoverType;
    private SelectionType selType;
    private float mutationProbability;
    private float crossoverProbability;
    private int generationLimit;
    private int populationSize;
    private int eliteSize;
    private int tournamentSize;

    public GeneticAlgorithm(GeneticAlgorithm existing) {
        this.selType = existing.selType;
        this.initType = existing.initType;
        this.mutType = existing.mutType;
        this.crossoverType = existing.crossoverType;
        this.mutationProbability = existing.mutationProbability;
        this.crossoverProbability = existing.crossoverProbability;
        this.generationLimit = existing.generationLimit;
        this.populationSize = existing.populationSize;
        this.eliteSize = existing.eliteSize;
        this.tournamentSize = existing.tournamentSize;
    }

    public GeneticAlgorithm(Builder builder) {
        this.selType = builder.selectionType;
        this.initType = builder.initializationType;
        this.mutType = builder.mutationType;
        this.crossoverType = builder.crossoverType;
        this.mutationProbability = builder.mutationProbability;
        this.crossoverProbability = builder.crossoverProbability;
        this.generationLimit = builder.generationLimit;
        this.populationSize = builder.populationSize;
        this.eliteSize = builder.eliteSize;
        this.tournamentSize = builder.tournamentSize;
    }

    public Path execute(DistanceMatrix graph, String filename) {
        int generation = 0;
        Path bestPath = null;
        FileWriter fileWriter = null;
        if (filename != null) {
            File csvFile = new File(filename);
            try {
                fileWriter = new FileWriter(csvFile);
                fileWriter.write("iterations,best,worst,avg\n");
            } catch (IOException e) {
                return null;
            }
        }
        var population = GeneticOperatorHelper.initialize(this.initType, graph, populationSize);
        var newPopulation = new ArrayList<Path>(populationSize);
        while (generation < generationLimit) {
            generation++;
            population.sort(Comparator.comparing(Path::getCost));
            var currBest = population.get(0);
            if (bestPath == null || currBest.getCost() < bestPath.getCost()) {
                bestPath = new Path(currBest.getNodes(), currBest.getCost());
            }
            if (fileWriter != null) {
                try {
                    saveMetrics(generation, population, fileWriter);
                } catch (IOException e) {
                    return null;
                }
            }

            //Transfer the best paths unchanged
            for (int i = 0; i < eliteSize; i++) {
                var currPath = population.get(i);
                newPopulation.add(new Path(currPath.getNodes(), currPath.getCost()));
            }

            //Create the rest of new population
            while (newPopulation.size() < populationSize) {
                var path1 = GeneticOperatorHelper.select(selType, population, this.tournamentSize);
                var path2 = GeneticOperatorHelper.select(selType, population, this.tournamentSize);
                List<Path> children;
                if (RandomGenerator.randomDouble() < crossoverProbability) {
                    children = GeneticOperatorHelper.crossover(this.crossoverType, path1, path2, graph);
                } else {
                    children = List.of(path1);
                }
                for (var child : children) {
                    if (RandomGenerator.randomDouble() < mutationProbability) {
                        GeneticOperatorHelper.mutate(this.mutType, child, graph);
                    }
                    newPopulation.add(child);
                }
            }
            population = newPopulation.subList(0, populationSize);
            newPopulation = new ArrayList<>(populationSize);
        }
        if (fileWriter != null) {
            try {
                fileWriter.close();
            } catch (IOException e) {
                return null;
            }
        }
        return bestPath;
    }

    private void saveMetrics(int generationCounter, List<Path> population, FileWriter fileWriter) throws IOException {
        StringBuilder line = new StringBuilder();
        line.append(generationCounter).append(',');
        line.append(population.get(0).getCost()).append(',');
        line.append(population.get(populationSize - 1).getCost()).append(',');
        var avg = population.stream()
                .mapToDouble(Path::getCost)
                .average()
                .orElse(0.0);
        line.append(avg);
        line.append("\n");
        fileWriter.write(line.toString());
    }

    @NoArgsConstructor
    public static class Builder {
        private MutationType mutationType = MutationType.Swap;
        private CrossoverType crossoverType = CrossoverType.OX;
        private InitializationType initializationType = InitializationType.Greedy;
        private SelectionType selectionType = SelectionType.Tournament;

        private float mutationProbability = 0.5f;
        private float crossoverProbability = 0.5f;
        private int generationLimit = 3000;
        private int populationSize = 1000;
        private int eliteSize = 3;
        private int tournamentSize = 5;


        public Builder setMutationType(MutationType mutationType) {
            this.mutationType = mutationType;
            return this;
        }

        public Builder setCrossoverType(CrossoverType crossoverType) {
            this.crossoverType = crossoverType;
            return this;
        }

        public Builder setInitializationType(InitializationType initializationType) {
            this.initializationType = initializationType;
            return this;
        }

        public Builder setSelectionType(SelectionType selectionType) {
            this.selectionType = selectionType;
            return this;
        }

        public Builder setMutationProbability(float mutationProbability) {
            this.mutationProbability = mutationProbability;
            return this;
        }

        public Builder setCrossoverProbability(float crossoverProbability) {
            this.crossoverProbability = crossoverProbability;
            return this;
        }

        public Builder setGenerationLimit(int generationLimit) {
            this.generationLimit = generationLimit;
            return this;
        }

        public Builder setPopulationSize(int populationSize) {
            this.populationSize = populationSize;
            return this;
        }

        public Builder setEliteSize(int eliteSize) {
            this.eliteSize = eliteSize;
            return this;
        }

        public Builder setTournamentSize(int tournamentSize) {
            this.tournamentSize = tournamentSize;
            return this;
        }

        public GeneticAlgorithm build() {
            return new GeneticAlgorithm(this);
        }
    }
}
