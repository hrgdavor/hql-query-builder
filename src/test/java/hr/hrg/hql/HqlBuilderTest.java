package hr.hrg.hql;

import static java.lang.String.join;
import static org.junit.Assert.*;
import static hr.hrg.hql.TestUtil.*;

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

        hb = new HqlBuilder("SELECT id,street,city FROM Adddress WHERE userId = :userId AND houseNo > :houseNo");
        hb.p("userId", userId);
        hb.p("houseNo", 2L);

        assertEquals("houseNo,userId", joinCommaSorted(hb.params.keySet()));
    }

}
