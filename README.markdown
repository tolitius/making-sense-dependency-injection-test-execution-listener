#What is "Making Sense Dependency Injection Test Execution Listener"?

This listener is mostly useful for Spring / JUnit tests as well as JUnit based test frameworks implementations ( e.g. Spock ). 
It will ensure to pre load Spring ApplicationContext and autowire all the beans (by type) BEFORE @BeforeClass is called.

#Why Would I Ever Need It!?

This proves really useful in case:

* DbUnit's DbTester needs to be created ONCE before executing all the tests OR
* A database needs to be setup ONCE per execution of all the tests OR
* You just NEED it in @BeforeClass, and without this listener there is no way to get it

Yes, you can slap another layer of abstraction such as Unitils, but it is... another layer of abstraction for such a trivial task.
Or you can just use TestNG with a more trivial listener, but what if you have to use JUnit: think "Spock" for instance which is based on JUnit.

One caveat that you're going to have to live with:

    Since @BeforeClass is a static method, you can only work with beans that are declared as static.

In order to inject such beans ( since Spring will not inject them by default ), you will need to create "workaround" looking setters for them:

```java
   private static dataSource;
   ... ...
   
   @Autowired
   public void setDataSource( DataSource ds ) {
       dataSource = ds;
   }
```

You'd register this listener as any other TestExecutionListener:

```java
   Java   : @TestExecutionListeners( inheritListeners = false, listeners = { MakingSenseDependencyInjectionTestExecutionListener.class })
   Groovy : @TestExecutionListeners( inheritListeners = false, listeners = [ MakingSenseDependencyInjectionTestExecutionListener.class ])
```

Now, all your beans will be available in @BeforeClass and / or setupSpec() methods
