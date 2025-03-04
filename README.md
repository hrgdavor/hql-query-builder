# Hibernate HQL query builder
I both love and hate(more) Hibernate, and try to avoid it, but that is not always possible. Hibernate is great , and has many features. For the same reason it is also a huge bloat, slows down start-up, does freaky bytecode manipulation, and is pain in the ass so many times. 

This utility is for writing HQL in more readable way. To simplify writing multiline queries, and queries that are different depending on parameter values (like optional filters).

For now, this not published as a maven library just copy the utility class [LiveTest4j.java](src/main/java/hr/hrg/livetest4j/LiveTest4j.java)  to your project.

## Using

The builder in the build phase does not need Hibernate. When you finish building the desired HQL, just call `.build(session)` or `.build(session, resultType)` to create a Hibernate Query object (that you can then use to list results) 



Standard way to write HQL is ok for smaller queries, but gets ugly very quickly.

```java
Query q = session.createQuery(
  "SELECT id,street,city FROM Adddress WHERE userId = :userId AND houseNo > :houseNo")
q.setParameter("userId", userId);
q.setParameter("houseNo", 2L);
```



This style is also supported by `HqlBuilder` (but it allows much more)

```java
var hb = new HqlBuilder(
  "SELECT id,street,city FROM Adddress WHERE userId = :userId AND houseNo > :houseNo");
hb.p("userId", userId);
hb.p("houseNo", 2L);
```

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

Each call to `add` puts a newline before each line(except the first), so, for the code above, HQL will look like this:

```sql
SELECT id,street,city
FROM Adddress
WHERE
  userId = :userId
  AND houseNo > :houseNo  
```



## Using .add

The builder can be constructed empty, or with parameters (it is just alias to `.add`)

```java
var hb = new HqlBuilder();
hb.add("SELECT * FROM User");
// is same as
var hb = new HqlBuilder("SELECT * FROM User");
```

So, the further examples we will be for `.add` method



Add just a new query part (with or without parameter placeholders)

```java
hb.add("FROM User");
```



Add query part with parameters and values

```java
hb.add("WHERE userId:userId", userId);
```

If you are providing values for parameters, you must match count of parameters in query part. Either provide all parameter values, or provide none (and define them later using `.p`).



Add a new query part with parameter placeholders, and provide parameter values separately)

```java
hb.add("  AND city=:city");
hb.p("city", city);
```



## HQL works nice with records

As you can give a record to Hibernate Query to fill it with query results, this gives a nice way of working with selected data.

```java
// Address entity has many more fields, here we define those that we plan to select
public record AddressPart(Long id, String street, String city){};

public List<AddressPart> getUserAddresses(Long userId){
  var hb = new HqlBuilder();
  hb.add("SELECT id,street,city");
  hb.add("FROM Adddress");
  hb.add("WHERE");
  hb.add("  userId = :userId", userId);
  
  Session session = ...;// obtain hibernate session in your app
  
  return hb.build(AddressPart.class).list()
}
```



## More examples

A query that has optional name filter. If name is not provided, name filter is not added to query.

```java
public List<User> getUsers(String name){
  var hb = new HqlBuilder();
  hb.add("FROM User");// HQL when selecting whole entities only needs FROM
  hb.add("WHERE");
  hb.add("  deleted = :deleted", false);
	if(name != null && !name.isEmpty()){
	  hb.add("  name LIKE :name", "%"+name+"%");
  }
  Session session = ...;// obtain hibernate session in your app
  
  return hb.build(User.class).list()
}
```

