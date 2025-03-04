# Hibernate HQL query builder
This utility is for writing HQL in more readable way. To simplify writing multiline queries, and queries that are different depending on parameter values (like optional filters).

The builder works without reference to Hibernate, and when you are finished with creating the desired HQL, just call `.build(session)` or `.build(session, resultType)` .



Standard way to write HQL is ok for smaller queries, but gets ugly very quickly.

```java
  Query q = session.createQuery(
    "SELECT id,street,city FROM Adddress WHERE userId = :userId AND houseNo > :houseNo")
  q.setParameter("userId", userId);
  q.setParameter("houseNo", 2L);
```



This style is also supported by `HqlBuilder` 

```java
  HqlBuilder hb = new HqlBuilder(
    "SELECT id,street,city FROM Adddress WHERE userId = :userId AND houseNo > :houseNo");
  hb.p("userId", userId);
  hb.p("houseNo", 2L);
```

but it allows much more.

The above query can be written like this:

```java
var hb = new HqlBuilder();
hb.add("SELECT id,street,city");
hb.add("FROM Adddress");
hb.add("WHERE");
hb.add("  userId = :userId", userId);
hb.add("  AND houseNo > :houseNo", 2L);
```

in a way that allows more formatting options for readability, and also placing parameter values close to where they are used in the query itself. 

Each call to `add` puts a newline before each line(except the first). The above HQL will look like this:

```sql
SELECT id,street,city
FROM Adddress
WHERE
  userId = :userId
  AND houseNo > :houseNo  
```



