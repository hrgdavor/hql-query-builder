# Hibernate HQL query builder
A single utility class for writing HQL in more readable way. Simplifies writing multiline queries, and queries that have optional parts depending on parameter values (like API adding extra filter by date if supplied date != null).

## The utility

For now, this not published as a maven library. **Just copy** the utility class [HqlBuilder.java](src/main/java/hr/hrg/hql/HqlBuilder.java)  to your project. If you like it, but not 100%, just tweak it to your own coding style.

**Rant:** *I both love and hate(more) Hibernate. I try to avoid it, but that is not always possible. Technologically Hibernate is great , and has sooo many features. For the same reason it is also is a huge bloat, slows down start-up, does freaky bytecode manipulation, and is pain in the ass so many times. Takes a ton of time to learn and* obscures learning actual SQL. 

## Basic comparison and sample use 
Standard way to write **HQL** is ok for smaller queries, but gets ugly very quickly.

```java
Query q = session.createQuery(
  "SELECT id FROM Adddress WHERE userId = :userId AND houseNo > :houseNo")
q.setParameter("userId", userId);
q.setParameter("houseNo", 2L);
```

This style is also supported by `HqlBuilder` (but it allows much more)

```java
var hb = new HqlBuilder(
  "SELECT id FROM Adddress WHERE userId = :userId AND houseNo > :houseNo");
hb.p("userId", userId);
hb.p("houseNo", 2L);
```

The above query can be written like this:

```java
var hb = new HqlBuilder();
hb.add("SELECT id");
hb.add("FROM Adddress");
hb.add("WHERE");
hb.add("  userId = :userId", userId);
hb.add("  AND houseNo > :houseNo", 2L);
```

in a way that allows more formatting options for readability, and also placing parameter values close to where they are used in the query itself. 

Each call to `add` puts a newline before each line(except the first), so, for the code above, **HQL** will look like this:

```sql
SELECT id
FROM Adddress
WHERE
  userId = :userId
  AND houseNo > :houseNo  
```

## Debug and logging

You can get the query that is going into hibernate by calling `hb.getQueryString()` and you will get the query above.

You can also dump what query with values looks like with `hb.toString()`. Not actual SQL that will go into database, just an approximation where parameter placeholders are replaced with values(NULL for null values, and for other `.toString()`)

```java
SELECT id,street,city
FROM Adddress
WHERE
  userId = 11
  AND houseNo > 2
```

You can improve this output by implementing more logic into `valueToString` method.

## Executing the query using Hibernate

When you finish building the desired HQL, just call `.build(session)` or `.build(session, resultType)` to create a Hibernate Query object (that you can then use to list results) 

```java
hb.build(session, Long.class).list()
```



## More details on .add

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
hb.add("WHERE userId = :userId", userId);
```

If you are providing values for parameters, you must match count of parameters in query part. Either provide all parameter values, or provide none (and define them later using `.p`).



Add a new query part with parameter placeholders, and provide parameter values separately)

```java
hb.add("  AND city = :city");
hb.p("city", city);
```



## Hibernate HQL works nice with records

As you can give a record to Hibernate Query to fill it with query results, this gives a nice way of working with selected data (this is part of Hibernate, not a feature added by this utility).

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
  
  return hb.build(session, AddressPart.class).list()
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
  
  return hb.build(session, User.class).list()
}
```



## Dilemma 1: parameters without name

Not sure if it s good or bad to use the builder like this, but I could not help but to allow this use-case. If you just use `:` without defining parameter name, then values are required at the `.add` method call. 

```java
var hb = new HqlBuilder();
hb.add("SELECT id,street,city");
hb.add("FROM Adddress");
hb.add("WHERE");
hb.add("  userId = :", userId);
hb.add("  AND houseNo > :", 2L);
```

For the code above, HQL will look like this:

```sql
SELECT id,street,city
FROM Adddress
WHERE
  userId = :_param_1
  AND houseNo > :_param_2  
```

## Dilemma 2: conditional code formatting

I would like to keep consistent visual formatting of indent in conditional statements. Example of such is:

```java
  var hb = new HqlBuilder();
  hb.add("FROM User");// HQL when selecting whole entities only needs FROM
  hb.add("WHERE");
  hb.add("  deleted = :deleted", false);
  if(name != null && !name.isEmpty()){
    hb.add("  name LIKE :name", "%"+name+"%");
  }
```

Now that `if` statement moves/confuses the indent of the HQL slightly. This is not a huge issue, but it bothers me. I have not found perfect solution, but am playing with few ideas.

One idea is to move java code indent back:
```java
  var hb = new HqlBuilder();
  hb.add("FROM User");FROM
  hb.add("WHERE");
  hb.add("  deleted = :deleted", false);
if(name != null && !name.isEmpty()){
  hb.add("  name LIKE :name", "%"+name+"%");
}
```
Another idea is utility function in the builder (but it works only for one line of HQL)
```java
  var hb = new HqlBuilder();
  hb.add("FROM User");
  hb.add("WHERE");
  hb.add("  deleted = :deleted", false);
  hb.addNextIf(name != null && !name.isEmpty());
  hb.add("  name LIKE :name", "%"+name+"%");
```
