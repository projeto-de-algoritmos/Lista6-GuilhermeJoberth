package com.guilherme_joberth.networkedAlgorithms.algorithm.geneticAlgorithm;

import com.guilherme_joberth.networkedAlgorithms.algorithm.geneticAlgorithm.restrictions.Restriction;

import javax.swing.text.TabableView;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Game implements Serializable {

    private List<Integer> numbers;
    private UUID id;

    Game(List numbers){

        if (numbers == null){
            numbers = Game.randomNumbers();
        }

        this.numbers = numbers;

        this.id = UUID.randomUUID();
    }

    public List<Integer> getNumbers() {
        return numbers;
    }

    static List randomNumbers(){

        List<Integer> random_generation = new LinkedList<>();
        Random rng = new Random();

        for (int i = 1; i <= 6; i++)
            random_generation.add(rng.nextInt(60) + 1);

        return random_generation;
    }

    UUID getId() {
        return id;
    }

    double getFitness(List<Restriction> restrictions){

        double fitness = 0;

        for (Restriction restriction : restrictions) {

            fitness += restriction.apply(this);
        }

        return 100 / (100 + fitness);
    }

    @Override
    public String toString() {
        String str = "[";

        this.numbers.sort(Integer::compareTo);
        for (Integer n : this.numbers){
            str += " " + n.toString() + " ";
        }

        return str + "]";
    }
}
