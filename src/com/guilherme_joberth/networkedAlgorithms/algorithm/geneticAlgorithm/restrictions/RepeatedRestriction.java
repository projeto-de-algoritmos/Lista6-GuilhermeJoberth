package com.guilherme_joberth.networkedAlgorithms.algorithm.geneticAlgorithm.restrictions;

import com.guilherme_joberth.networkedAlgorithms.algorithm.geneticAlgorithm.Game;

public class RepeatedRestriction extends HardRestriction {

    @Override
    public int apply(Game g) {

        for (Integer num : g.getNumbers()){

            if (g.getNumbers().indexOf(num) != g.getNumbers().lastIndexOf(num)){
                return this.penality;
            }
        }

        return 0;
    }
}
