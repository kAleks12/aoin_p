package org.genetic.alg.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class Path {
    private double cost;
    private List<Integer> nodes = new ArrayList<>();

    public Path(List<Integer> path, double sumCost) {
        this.nodes.addAll(path);
        this.cost = sumCost;
    }

    public void addNode(int node) {
        this.nodes.add(node);
    }

    public int getNode(int index) {
        return this.nodes.get(index);
    }

    @Override
    public String toString() {
        return "Path ( " +
                "cost = " + cost +
                ", nodes = " + nodes +
                " )";
    }
}
