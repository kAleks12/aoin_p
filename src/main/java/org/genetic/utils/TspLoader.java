package org.genetic.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.genetic.utils.entities.DistFormat;
import org.genetic.utils.entities.DistanceMatrix;
import org.genetic.utils.entities.Node;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TspLoader {
    private static final Logger logger = LogManager.getLogger(TspLoader.class);

    public static Optional<DistanceMatrix> load(String path) {
        List<Node> nodes = new ArrayList<>();
        DistFormat format = null;
        try (FileReader fReader = new FileReader(path)) {
            BufferedReader reader = new BufferedReader(fReader);
            String line;

            //Read header data
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("EDGE_WEIGHT_TYPE")) {
                    String type = line.substring(line.indexOf(':') + 1).trim();
                    logger.info("Using edge weight type: {}", type);
                    switch (type) {
                        case "EUC_2D" -> format = DistFormat.EUC_2D;
                        case "GEO" -> format = DistFormat.GEO;
                        default -> throw new UnsupportedOperationException("Unsupported EDGE_WEIGHT_TYPE: " + type);
                    }
                }
                if (line.startsWith("NODE_COORD_SECTION")) {
                    break;
                }
            }

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.equals("EOF")) {
                    break;
                }
                String[] params = line.split(" ");
                if (params.length >= 3) {
                    double x = Double.parseDouble(params[1]);
                    double y = Double.parseDouble(params[2]);
                    Node newNode = new Node(x, y);
                    nodes.add(newNode);
                }
            }
        } catch (FileNotFoundException e) {
            logger.error("File not found", e);
        } catch (IOException e) {
            logger.error("Error reading file", e);
        } catch (NumberFormatException e) {
            logger.error("Invalid cord value", e);
        } catch (UnsupportedOperationException e) {
            logger.error("Unsupported edge weight type", e);
        }
        logger.info("Loaded {} nodes from {}", nodes.size(), path);

        if (nodes.isEmpty() || format == null) {
            logger.warn("Failed to load nodes from {}", path);
            return Optional.empty();
        }

        // Build distance matrix
        return Optional.of(getDistanceMatrix(nodes, format));
    }

    private static DistanceMatrix getDistanceMatrix(List<Node> nodes, DistFormat format) {
        int size = nodes.size();
        double[][] distanceMatrix = new double[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (i != j) {
                    distanceMatrix[i][j] = nodes.get(i).getDistance(nodes.get(j), format);
                } else {
                    distanceMatrix[i][j] = 0.0;
                }
            }
        }

        return new DistanceMatrix(distanceMatrix, size);
    }
}
