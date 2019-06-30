package com.guilherme_joberth.networkedAlgorithms.algorithm.geneticAlgorithm.restrictions;

import com.guilherme_joberth.networkedAlgorithms.algorithm.geneticAlgorithm.Game;
import com.guilherme_joberth.networkedAlgorithms.algorithm.geneticAlgorithm.restrictions.utils.CountSequence;

import java.util.Iterator;
import java.util.List;

public class HighSequenceRestriction extends HardRestriction {


    @Override
    public int apply(Game g) {

        return (CountSequence.count(g) > 3 ? penality : 0);
    }
}
