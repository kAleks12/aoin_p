package org.genetic.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.genetic.alg.entities.Path;
import org.genetic.utils.entities.Algorithm;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class OverviewWriter {
    private final static Logger logger = LogManager.getLogger(OverviewWriter.class);

    public static void saveMetrics(Algorithm alg, List<Path> results, String filename) {
        if (filename == null) {
            logger.warn("Filename is null, skipping saving metrics");
            return;
        }
        results.sort(Comparator.comparing(Path::getCost));
        File csvFile = new File(filename);
        try (var fileWriter = new FileWriter(csvFile, true)) {
            StringBuilder line = new StringBuilder();
            line.append(alg.toString()).append(',');
            line.append(results.get(0).getCost()).append(',');
            line.append(results.get(results.size() - 1).getCost()).append(',');
            var avg = results.stream()
                    .mapToDouble(Path::getCost)
                    .average()
                    .orElse(0.0);
            line.append(avg).append(',');
            var sum = results.stream()
                    .mapToDouble(Path::getCost)
                    .map(cost -> Math.pow(cost - avg, 2))
                    .sum();
            var mean = Math.sqrt(sum / results.size());
            line.append(mean);
            line.append("\n");
            fileWriter.write(line.toString());
        } catch (IOException e) {
            logger.error("Error while saving metrics", e);
        }
        logger.info("Metrics for {} saved to {}", alg, filename);
    }
}
