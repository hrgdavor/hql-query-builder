package hr.hrg.hql;

import static java.lang.String.join;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static hr.hrg.hql.TestUtil.*;
import static hr.hrg.hql.HqlBuilder.*;

import org.junit.Test;


public class HqlBuilderTest {

    @Test
    public void test() {
        Long userId = 11l;

        var hb = new HqlBuilder();
        hb.add("SELECT id,street,city");
        hb.add("FROM Adddress");
        hb.add("WHERE");
        hb.add("  userId = :userId", userId);
        hb.add("  AND houseNo > :houseNo", 2L);
        
        assertEquals(join("\n", "SELECT id,street,city", "FROM Adddress", "WHERE", "  userId = :userId", "  AND houseNo > :houseNo"), hb.getQueryString());
        assertEquals("houseNo,userId", joinCommaSorted(hb.params.keySet()));
        assertEquals(join("\n", "SELECT id,street,city", "FROM Adddress", "WHERE", "  userId = 11", "  AND houseNo > 2"), hb.toString());

        hb = new HqlBuilder("SELECT id,street,city FROM Adddress WHERE userId = :userId AND houseNo > :houseNo");
        hb.p("userId", userId);
        hb.p("houseNo", 2L);

        assertEquals("houseNo,userId", joinCommaSorted(hb.params.keySet()));

        hb = new HqlBuilder("SELECT id,street,city FROM Adddress WHERE userId = : AND houseNo > :", userId, 2l);

        assertEquals("SELECT id,street,city FROM Adddress WHERE userId = :_param_1 AND houseNo > :_param_2", hb.getQueryString());
        assertEquals("_param_1,_param_2", joinCommaSorted(hb.params.keySet()));

    }
    
    @Test
    public void testNextParam(){
        assertEquals(new ParamPos(1, 2, "a"), nextParam(":a", -1));
        assertEquals(new ParamPos(1, 2, "a"), nextParam(":a ", -1));
        assertEquals(new ParamPos(2, 3, "a"), nextParam(" :a ", -1));

        assertEquals(new ParamPos(1, 1, ""), nextParam(":", -1));
        assertEquals(new ParamPos(1, 1, ""), nextParam(": ", -1));
        assertEquals(new ParamPos(2, 2, ""), nextParam(" : ", -1));

        assertEquals(new ParamPos(1, 5, "test"), nextParam(":test", -1));
        assertEquals(new ParamPos(1, 5, "test"), nextParam(":test ", -1));
        assertEquals(new ParamPos(7, 11, "test"), nextParam("where :test ", -1));
    }
    
    @Test
    public void testToString(){
        new TestUtil();// do this stupid instantiation to get 100% coverage
        assertEquals(",bla", joinCommaSorted(Arrays.asList("bla",null)));
        assertEquals("bla", joinCommaSorted(Arrays.asList("bla")));
        assertEquals("", joinCommaSorted(Arrays.asList("")));
        List<?> list = new ArrayList<>();
        list.add(null);
        assertEquals("", joinCommaSorted(list));
        assertEquals("", joinCommaSorted(new ArrayList<>()));
        assertEquals("", joinCollection(new StringBuffer(),",",new ArrayList<>()).toString());
        assertEquals("bla,", joinCollection(new StringBuffer(),",",Arrays.asList("bla",null)).toString());
        assertEquals(",bla", joinCollection(new StringBuffer(),",",Arrays.asList(null,"bla")).toString());
        

        assertEquals("NULL", new HqlBuilder(":a").toString());
        assertEquals(" NULL", new HqlBuilder(" :a").toString());
        

        var hb = new HqlBuilder("SELECT a,b from C WHERE a>:a AND b>:b", 1,2);
        assertEquals("SELECT a,b from C WHERE a>1 AND b>2", hb.toString());
        
        hb = new HqlBuilder();
        hb.add("SELECT a,b");
        hb.add("FROM C");
        hb.add("WHERE");
        hb.add("  a>:a",null);
        hb.add("  AND b>: ",2);
        assertEquals(String.join("\n", "SELECT a,b","FROM C","WHERE","  a>:a","  AND b>:_param_1 "), hb.getQueryString());
        assertEquals(String.join("\n", "SELECT a,b","FROM C","WHERE","  a>NULL","  AND b>2 "), hb.toString());
    }

    @Test
    public void testAddNextIf(){
        boolean someReasongToWork = true;

        var hb = new HqlBuilder();
        hb.add("SELECT a,b");
        hb.addNextIf(someReasongToWork);
        hb.add(":a FROM C");
        
        assertEquals(String.join("\n", "SELECT a,b",":a FROM C"), hb.getQueryString());
        
        someReasongToWork = false;

        hb = new HqlBuilder();
        hb.add("SELECT a,b");
        hb.addNextIf(someReasongToWork);
        hb.add("FROM C");
        
        assertEquals(String.join("\n", "SELECT a,b"), hb.getQueryString());
        
    }
    
    
}
