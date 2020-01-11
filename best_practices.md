## Protection against column names with spaces, keywords, etc.

In hibernate.cfg.xml use the `globally_quoted_identifiers` property, e.g.,

~~~xml
<property name="dialect">net.ucanaccess.hibernate.dialect.UCanAccessDialect</property>
<property name="globally_quoted_identifiers">true</property>
~~~
&nbsp;

## "Long Text" (formerly "MEMO") fields

~~~java
@Lob
private String comments;
~~~
&nbsp;

## money fields

~~~java
// auto-created columns will be DECIMAL() instead of CURRENCY
@Column(precision = 19, scale = 4)  // required, otherwise defaults to (19,2)
private BigDecimal fee;
~~~
&nbsp;
