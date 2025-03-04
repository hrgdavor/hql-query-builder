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
        queryString.append(queryWithParams);
        if(values.length == 0) return this;
        
        int idx = 0;
        int pos = 0;
        int paramEnd = -1;
        boolean tilEnd = false;
        try {
            while(true) {
                idx = queryWithParams.indexOf(":", paramEnd);
                if(idx == -1) break;
                tilEnd = true;
                for(paramEnd = idx+1; paramEnd<queryWithParams.length(); paramEnd++) {
                    var ch = queryWithParams.charAt(paramEnd);
                    if(!Character.isJavaIdentifierPart(ch)) {
                        tilEnd = false;
                        break;
                    }
                }
                String paramName = tilEnd ? queryWithParams.substring(idx+1) : queryWithParams.substring(idx+1, paramEnd);
                if(values.length <= pos) throw new RuntimeException("Missing value for parameter "+paramName);
                params.put(paramName, values[pos]);
                pos++;
                if(tilEnd) break;
            }
        } catch (Exception e) {
            throw new RuntimeException("Problem paring pos:"+pos+", idx:"+idx+", tilEnd:"+tilEnd+", paramEnd:"+paramEnd+" query: "+queryWithParams, e);
        }
        return this;
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
}