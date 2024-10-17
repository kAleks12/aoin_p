package org.genetic;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.genetic.alg.GeneticAlgorithm;
import org.genetic.alg.entities.CrossoverType;
import org.genetic.alg.entities.InitializationType;
import org.genetic.alg.entities.MutationType;
import org.genetic.alg.entities.SelectionType;
import org.genetic.utils.TspLoader;

import java.io.IOException;
import java.time.LocalDateTime;


public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);


    public static void main(String[] args) throws IOException {
        var graph = TspLoader.load("src/main/resources/data/kroA200.tsp");
        if (graph.isPresent()) {
            logger.info("Graph loaded successfully");

            var genetic = new GeneticAlgorithm.Builder()
                    .setInitializationType(InitializationType.Greedy)
                    .setMutationType(MutationType.Inverse)
                    .setCrossoverType(CrossoverType.PMX)
                    .setSelectionType(SelectionType.Tournament)
                    .setTournamentSize(5)
                    .setCrossoverProbability(0.7f)
                    .setMutationProbability(0.1f)
                    .setEliteSize(1)
                    .setPopulationSize(10)
                    .setGenerationLimit(100)
                    .build();
            var timeBody = LocalDateTime.now()
                    .toString()
                    .replace(':','_')
                    .replace('.', '_')
                    .replace('-', '_');
            genetic.execute(graph.get(), "result_" + timeBody + ".csv");
        }
    }
}
