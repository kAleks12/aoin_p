package org.genetic.alg.entities;

import org.genetic.utils.entities.Interval;

public record Member(Path path, Interval<Double> interval) {
}
