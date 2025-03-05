package hr.hrg.hql;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;
import org.hibernate.query.Query;

/**
 * Builder made for composing queries in a more readable way, and also to simplify dynamic parts of
 * queries(when some filters are optional for example)
 */
public class HqlBuilder{
    Map<String, Object> params = new HashMap<>();
    private StringBuilder queryString = new StringBuilder();
    private boolean addNext = true;
    private boolean firstLine = true;
    private int seq = 1;

    public static record ParamPos(int start, int end, String name) {}
    public static final ParamPos NOPE = new ParamPos(-1, -1, "");
    private static final Object[] ARR_WITH_NULL = {null};
    
    /**
     * Constructor for separating construction and composition
     */
    public HqlBuilder() {}
    
    /** Add to query string and if present also parameter values.
     * 
     * @param queryWithParams - query string
     * @param values - optional parameters
     */
    public HqlBuilder(String queryWithParams , Object ...values) {
        this.add(queryWithParams, values);
    }
    
    /** Set a parameter value.
     * 
     * @param key parameter name/key
     * @param value parameterValue
     * @return
     */
    public HqlBuilder p(String key, Object value) {
        params.put(key, value);
        return this;
    }
    
    /** Add if the first parameter is true.
     * 
     * @param should - should the query be added
     * @param queryWithParams - query string
     * @param values - optional parameters
     * @return
     */
    public HqlBuilder addNextIf(boolean condition) {
        addNext = condition;
        return this;
    }
    
    /** Add to query string and if present also parameter values.
     * 
     * @param queryWithParams - query string
     * @param values - optional parameters
     * @return self
     */
    public HqlBuilder add(String queryWithParams, Object ...values) {
        if(!addNext) {
            addNext = true;
            return this;
        }
        if(firstLine) {
            firstLine = false;
        }else {
            queryString.append('\n');
        }
        if(values == null) values = ARR_WITH_NULL;
        int pos = 0;
        int offset = 0;
        ParamPos paramPos = NOPE;
        try {
            while(true) {
                paramPos = nextParam(queryWithParams, paramPos.end);
                if(paramPos.start == -1) break;
                
                String paramName = paramPos.name;
                if(values.length <= pos) {
                    if(paramName.isEmpty()) throw new RuntimeException("Value must be provided for parameters without name");

                    // allow adding query parts with params to be defined later
                    // but only if no params are provided
                    if(values.length == 0) {
                        queryString.append(queryWithParams);
                        return this; 
                    }

                    throw new RuntimeException("Missing value for parameter "+paramName);
                }
                if(paramName.isEmpty()) paramName = "_param_" + seq++;
                params.put(paramName, values[pos]);
                pos++;
                
                if(paramPos.start > 0) queryString.append(queryWithParams.subSequence(offset, paramPos.start-1));
                queryString.append(":").append(paramName);
                
                offset = paramPos.end;                
                if(paramPos.end >= queryWithParams.length()) break;
            }
            if(offset < queryWithParams.length()) {
                queryString.append(queryWithParams.subSequence(offset, queryWithParams.length()));
            }            
        } catch (Exception e) {
            throw new RuntimeException("Problem parsing pos:"+pos+" query: "+queryWithParams, e);
        }
        return this;
    } 
    
    public static ParamPos nextParam(String str, int offset) {
        int start = str.indexOf(":", offset);
        if(start == -1) return NOPE;
        start ++;// skip :

        int end = start;
        int strLen = str.length();
        for(; end<strLen; end++) {
            var ch = str.charAt(end);
            if(!Character.isJavaIdentifierPart(ch)) {
                break;
            }
        }

        return new ParamPos(start, end, str.substring(start, end));
    }
    
    
    /**
     * Shortcut for instantiating a hibernate query.
     * @param <T> - query return type
     * @param session hibernate session
     * @param type class matching the return type
     * @return hibernate Query
     */
    public <T> Query<T> build(Session session){
        return make(session, null, this);
    }

    /**
     * Shortcut for instantiating a hibernate query.
     * @param <T> - query return type
     * @param session hibernate session
     * @param type class matching the return type
     * @return hibernate Query
     */
    public <T> Query<T> build(Session session, Class<T> type){
        return make(session, type, this);
    }
    
    /**
     * Shortcut for listing results.
     * @param <T> - query return type
     * @param session hibernate session
     * @param type class matching the return type
     * @param qb - query builder
     * @return results in a List<T>
     */
    public static <T> List<T> list(Session session, Class<T> type, HqlBuilder qb){
        return make(session, type, qb).list();
    }
    
    /**
     * Shortcut for instantiating a hibernate query.
     * @param <T> - query return type
     * @param session hibernate session
     * @param type class matching the return type
     * @param qb - query builder
     * @return hibernate Query
     */
    public static <T> Query<T> make(Session session, Class<T> type, HqlBuilder qh){
        Query<T> query = type == null ? session.createQuery(qh.getQueryString()) : session.createQuery(qh.getQueryString(), type);
        for(var entry:qh.params.entrySet()) {
            try {
                query.setParameter(entry.getKey(), entry.getValue());
            } catch (Exception e) {
                throw new RuntimeException("Error setting param "+entry.getKey()+"="+entry.getValue(), e);
            }
        }
        return query;
    }
    
    public String getQueryString() {
        return queryString.toString();
    }
    
    public CharSequence valueToString(Object value) {
        return value == null ? "NULL":value.toString();
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        ParamPos paramPos = NOPE;
        String query = queryString.toString();
        int offset = 0;
        while(true) {
            paramPos = nextParam(query, paramPos.end);
            if(paramPos.start == -1) break;

            // start can never be zero, as it either there is not ":" or ":" is first, and then
            // start = 1 (start does not include ":")
            sb.append(query.subSequence(offset, paramPos.start-1));
            sb.append(valueToString(params.get(paramPos.name)));
            
            offset = paramPos.end;
        }
        if(offset < query.length()) {
            sb.append(query.subSequence(offset, query.length()));
        }
        return sb.toString();
    }
}