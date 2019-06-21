package com.guilherme_joberth.networkedAlgorithms.algorithm.geneticAlgorithm;

import com.guilherme_joberth.networkedAlgorithms.algorithm.Algorithm;
import com.guilherme_joberth.networkedAlgorithms.algorithm.geneticAlgorithm.restrictions.Restriction;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GeneticAlgorithm implements Serializable, Algorithm {

    private int id;

    private int generationsRun = 0;
    private final int MAX_GENERATIONS = 100;

    private Map<UUID, Game> population;
    private List<Restriction> restrictions;

    private boolean ready = false;

    public GeneticAlgorithm(List<Restriction> restrictions, int id) {

        this.population = new HashMap<>();
        this.restrictions = restrictions;
        this.id = id;

    }

    public String result() {

        String description = "";

        if(population != null){

            for (Game g : population.values()) {

                description += g.toString() + "\n";

            }

        }

        return description;
    }

    public String getStatus(){
        return  this.generationsRun + "/" + this.MAX_GENERATIONS;
    }

    public int getId() {
        return id;
    }

    public void execute(){

        if (this.generationsRun >= this.MAX_GENERATIONS) {
            ready = true;
            return;
        }

        Map<UUID, Double> avaliation = this.avaliate();
        Map<UUID, Game> selected = this.select(avaliation);
        this.population = this.crossover(selected);

        this.generationsRun++;

        try {
            Thread.currentThread().sleep(ThreadLocalRandom.current().nextInt(500,2000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean getReady(){
        return ready;
    }


    private Map<UUID, Double> avaliate() {

        Map<UUID, Double> calculatedFitness = new HashMap<>();

        for (Game g : this.population.values()){

            calculatedFitness.put(g.getId(), g.getFitness(this.restrictions));
        }

        return calculatedFitness;
    }

    private Map<UUID, Game> select(Map<UUID, Double> avaliations){

        // TODO
        return population;
    }

    private Map<UUID, Game> crossover(Map<UUID, Game> selected){
        // TODO
        return selected;
    }





}
