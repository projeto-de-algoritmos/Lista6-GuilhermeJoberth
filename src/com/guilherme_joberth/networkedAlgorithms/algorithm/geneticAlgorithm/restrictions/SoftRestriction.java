package com.guilherme_joberth.networkedAlgorithms.algorithm.geneticAlgorithm.restrictions;

import com.guilherme_joberth.networkedAlgorithms.algorithm.geneticAlgorithm.Game;

import java.io.Serializable;

public abstract class SoftRestriction implements Restriction, Serializable {

    final int penality = 10;

    @Override
    public abstract int apply(Game g);
}
