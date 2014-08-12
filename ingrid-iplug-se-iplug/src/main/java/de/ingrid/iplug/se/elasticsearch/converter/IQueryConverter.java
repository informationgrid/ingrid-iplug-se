package de.ingrid.iplug.se.elasticsearch.converter;

import org.elasticsearch.index.query.BoolQueryBuilder;

import de.ingrid.utils.query.IngridQuery;

public interface IQueryConverter {

    public void parse(IngridQuery ingridQuery, BoolQueryBuilder queryBuilder);
    
}
