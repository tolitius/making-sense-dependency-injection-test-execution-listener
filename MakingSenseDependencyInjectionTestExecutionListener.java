package org.gitpod;


import junit.framework.Assert;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * <p> This listener is mostly useful for Spring / JUnit tests as well as JUnit based test frameworks implementations
 *     ( e.g. Spock ). It will ensure to pre load Spring {@link org.springframework.context.ApplicationContext} and
 *     autowire all the beans (by type) <em>BEFORE</em> @BeforeClass is called.</p>
 *
 * <p> This proves really useful in case a database needs to be setup ONCE per execution of all the tests.</p>
 *
 * <p> Or e.g. DbUnit's DbTester needs to be created ONCE before executing all the tests. </p>
 *
 * <p> Or you just NEED it in @BeforeClass, and without this listener there is no way to get it.</p>
 *
 * <p> Yes, you can slap another layer of abstraction such as Unitils, but it is... another layer of abstraction for
 *     such a trivial task. Or you can just use TestNG with a more trivial listener, but what if you have to use JUnit:
 *     think Spock for instance which is based on JUnit.</p>
 *
 * <p> One caveat that you're going to have to live with:</p>
 *
 * <p> Since @BeforeClass is a static method, you can only work with beans that were declared as static.
 *     In order to inject such beans ( since Spring will not inject them by default ),
 *     you will need to create "workaround" looking setters for them:</p>
 *
 * <pre>
 *   {@code
 *   private static dataSource;
 *   ... ...
 *   }
 *   {@code @Autowired
 *   public void setDataSource( DataSource ds ) {
 *       dataSource = ds;
 *   }
 * }</pre>
 *
 * <p> You'd register this listener as any other TestExecutionListener:</p>
 *
 * <pre>
 *   Java   : {@code @TestExecutionListeners( inheritListeners = false, listeners = { MakingSenseDependencyInjectionTestExecutionListener.class })
 *}</pre>
 *
 * <pre>
 *   Groovy : {@code @TestExecutionListeners( inheritListeners = false, listeners = [ MakingSenseDependencyInjectionTestExecutionListener.class ])
 *}</pre>
 *
 * <p>  Now, all your beans will be available in @BeforeClass and / or setupSpec() methods </p>
 *
 * @author anatoly.polinsky
 */
public class MakingSenseDependencyInjectionTestExecutionListener extends DependencyInjectionTestExecutionListener {

    /**
     * <p> Called by ...JUnit4ClassRunner before @BeforeClass. Creates an instance for a current test context, loads an
     *     {@link org.springframework.context.ApplicationContext }, and autowires beans ( by type ) for this newly
     *     created test instance. </p>
     */
    public void beforeTestClass( TestContext testContext ) throws Exception {

        createTestContextInstance(testContext);

        Object testContextInstance = testContext.getTestInstance();

        AutowireCapableBeanFactory beanFactory = testContext.getApplicationContext().getAutowireCapableBeanFactory();
        beanFactory.autowireBeanProperties( testContextInstance, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false );
        beanFactory.initializeBean( testContextInstance, testContext.getTestClass().getName() );
        testContext.removeAttribute( REINJECT_DEPENDENCIES_ATTRIBUTE );
    }

    /**
     * <p> Given a {@link TestContext}, based on a test class, creates a test instance, and updates a
     *     test context with it</p>
     */
    private void createTestContextInstance( TestContext testContext ) throws Exception {

        // create instance of the actual test
        Constructor<?>[] constructors= testContext.getTestClass().getConstructors();
        Assert.assertEquals( 1, constructors.length );
        Object testContextInstance = constructors[0].newInstance();

        // update test context with this new instance
        Method updateStatusMethod =
                TestContext.class.getDeclaredMethod( "updateState", Object.class, Method.class, Throwable.class );

        updateStatusMethod.setAccessible( true );
        updateStatusMethod.invoke(
                testContext, testContextInstance, testContext.getTestMethod(), testContext.getTestException() );
    }
}
