package de.ingrid.utils.processor.impl;

import de.ingrid.utils.IngridDocument;
import de.ingrid.utils.IngridHit;
import de.ingrid.utils.processor.IPostProcessor;
import de.ingrid.utils.query.IngridQuery;

/**
 * This class normalizes the score to 0 and 1, by finding out the highest
 * score first, then normalizing it and updating
 * might be inside an IngridHit. This happens if the same domain belongs to
 * several URLs within the URL maintenance. 
 * @author Andre
 *
 */
public class NormalizeScoreProcessor implements IPostProcessor {
    
    @Override
    public void process(IngridQuery ingridQueries, IngridDocument[] documents) throws Exception {
        float maxScore = 0F;
        
        // find the highest score
        for (IngridHit ingridHit : (IngridHit[])documents) {
            if (ingridHit.getScore() > maxScore)
                maxScore = ingridHit.getScore();
        }
        
        // normalize Score
        for (IngridHit ingridHit : (IngridHit[])documents) {
            ingridHit.setScore(ingridHit.getScore()/maxScore);
        }
    }
}
