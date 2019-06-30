package com.guilherme_joberth.networkedAlgorithms.algorithm.geneticAlgorithm.restrictions.utils;

import com.guilherme_joberth.networkedAlgorithms.algorithm.geneticAlgorithm.Game;

import java.util.Iterator;
import java.util.List;

public class CountSequence {

    public static int count(Game g){

        int sequence = 0;
        boolean keep = true;

        List<Integer> numbers = g.getNumbers();
        numbers.sort(Integer::compareTo);

        Iterator<Integer> itr = numbers.iterator();
        Integer current = itr.next();

        while (itr.hasNext()){

            Integer future = itr.next();
            if(future - current < 2){
                if(keep)
                    sequence++;
                else{
                    sequence = 1;
                    keep = true;
                }
            }else{
                keep = false;
            }

            current = future;

        }

        return sequence;
    }

}
