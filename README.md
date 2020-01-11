This is a sub-project of [UCanAccess](https://sourceforge.net/projects/ucanaccess/) to support development of a Hibernate dialect.

**Usage notes:**

Look in the src/test/java branch for code to run. 

To override the hibernate.cfg.xml `connection.url` property at runtime, use a JVM argument to specify a system property named `HIBERNATE_CONNECTION_URL`, e.g.,

~~~text
-DHIBERNATE_CONNECTION_URL=jdbc:ucanaccess://C:/Users/gord/Desktop/foo.accdb;newDatabaseVersion=V2010
~~~
