package com.guilherme_joberth.networkedAlgorithms.algorithm.geneticAlgorithm.restrictions;

import com.guilherme_joberth.networkedAlgorithms.algorithm.geneticAlgorithm.Game;

import java.io.Serializable;

public abstract class HardRestriction implements Restriction, Serializable {

    final int penality = 100;

    @Override
    public abstract int apply(Game g);
}
