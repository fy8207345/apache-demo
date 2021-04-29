package org.example.lang3;

import com.sun.xml.internal.bind.v2.runtime.RuntimeUtil;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.compare.ComparableUtils;

import java.util.Comparator;
import java.util.function.Predicate;

public class BuilderTests {

    public static void main(String[] args) {
        Obj obj1 = new Obj(1, "test1");
        Obj obj2 = new Obj(2, "test2");
        Predicate<Obj> between = ComparableUtils.ge(obj1);
        System.out.println(between.test(obj2));
        System.out.println(obj1);
    }

    public static class Obj implements Comparator<Obj>, Comparable<Obj> {
        private Integer id;
        private String name;

        public Obj(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public int compare(Obj o1, Obj o2) {
            return new CompareToBuilder().append(o1.id, o2.id)
                    .append(o1.name, o2.name)
                    .toComparison();
        }

        @Override
        public int compareTo(Obj o) {
            return new CompareToBuilder().append(this.id, o.id)
                    .append(this.name, o.name)
                    .toComparison();
        }

        @Override
        public String toString() {
            ToStringBuilder toStringBuilder = new ToStringBuilder(this, ToStringStyle.JSON_STYLE);
            return toStringBuilder.append("id", id)
                    .append("name", name)
                    .toString();
        }
    }
}
