package hr.hrg.hql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class TestUtil {

    public static String joinCommaSorted(Collection<?> col) {
        List<String> list = new ArrayList<>();
        for(var tmp:col) {
            list.add(tmp == null ? "":tmp.toString());
        }
        Collections.sort(list);
        return joinCollection(",", list);
    }

    public static String joinCollection(String delimiter, Collection col) {
        if(col.size() == 0) return "";
        return joinCollection(new StringBuffer(), delimiter, col).toString();
    }

    public static StringBuffer joinCollection(StringBuffer buffer, String delimiter, Collection<?> col) {
        if(col.size() == 0) return buffer;
        Iterator<?> iterator = col.iterator();
        var tmp = iterator.next();
        buffer.append(tmp == null ? "":tmp.toString());
        if(col.size() == 1) {
            return buffer;
        }
        while(iterator.hasNext()) {
            tmp = iterator.next();
            buffer.append(delimiter);
            buffer.append(tmp == null ? "":tmp.toString());
        }
        return buffer;
    }    
}
