package com.guilherme_joberth.networkedAlgorithms.algorithm.geneticAlgorithm;

import com.guilherme_joberth.networkedAlgorithms.algorithm.Algorithm;
import com.guilherme_joberth.networkedAlgorithms.algorithm.geneticAlgorithm.restrictions.Restriction;
import com.sun.org.apache.xpath.internal.FoundIndex;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class GeneticAlgorithm implements Serializable, Algorithm {

    private final int populationSize;
    private int id;

    private int generationsRun = 0;
    private final int MAX_GENERATIONS = 100;

    private Map<UUID, Game> population;
    private List<Restriction> restrictions;

    private List<Double> lastAverages;

    private boolean ready = false;

    public GeneticAlgorithm(List<Restriction> restrictions, int id, int populationSize) {

        this.population = new HashMap<>();
        this.restrictions = restrictions;
        this.id = id;

        this.populationSize = populationSize;

        this.generateInitialPopulation();

        this.lastAverages = new ArrayList<>();
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

    private void generateInitialPopulation(){

        for (int i = 0; i < this.populationSize; i++){

            Game g = new Game(Game.randomNumbers());

            this.population.put(g.getId(), g);
        }

    }

    public void execute(){

        double globalAverage = 0;
        for (Double avg : lastAverages) {
            globalAverage += avg;
        }
        globalAverage /= lastAverages.size();


        boolean converged = (globalAverage > 0.9);

        if (this.generationsRun >= this.MAX_GENERATIONS || converged) {
            ready = true;
            return;
        }

        Map<UUID, Double> avaliation = this.avaliate();
        Map<UUID, Game> selected = this.select(avaliation);
        this.population = this.crossover(selected);

        this.generationsRun++;
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

        double mean = 0;
        for(Double fitness : avaliations.values()){

            mean += fitness;
        }
        mean /= avaliations.size();

        lastAverages.add(mean);
        if(lastAverages.size() > 5){
            lastAverages.remove(0);
        }

        mean *= 0.9;

        for(Map.Entry<UUID, Double> entry : avaliations.entrySet() ){

            if(entry.getValue() < mean){
                population.remove(entry.getKey());
            }
        }

        return population;
    }

    private Map<UUID, Game> crossover(Map<UUID, Game> selected){

        List<Game> toProcess = new LinkedList<>(selected.values());
        List<Game> newGeneration = new LinkedList<>();

        Random rng = new Random();

        Collections.shuffle(toProcess);

        if(toProcess.size() % 2 != 0){
            newGeneration.add(toProcess.remove(0));
        }

        Iterator<Game> itr = toProcess.iterator();
        while (itr.hasNext()){

            Game parent1 = itr.next();
            Game parent2 = itr.next();

            List<Integer> finalNumbers = new LinkedList<>();

            Iterator<Integer> parent1Numbers = parent1.getNumbers().iterator();
            Iterator<Integer> parent2Numbers = parent2.getNumbers().iterator();

            while (parent1Numbers.hasNext() && parent2Numbers.hasNext()){

                Integer val1 = parent1Numbers.next();
                Integer val2 = parent2Numbers.next();

                int take = rng.nextInt(1);

                if(take == 0){

                    finalNumbers.add(val1);

                }else{

                    finalNumbers.add(val2);

                }
            }

            newGeneration.add(parent1);
            newGeneration.add(parent2);
            newGeneration.add(new Game(finalNumbers));

        }

        Map<UUID, Game> newPopulation = new HashMap<>();
        itr = newGeneration.iterator();
        while (itr.hasNext()){
            Game g = itr.next();

            newPopulation.put(g.getId(), g);
        }

        return newPopulation;
    }
}
