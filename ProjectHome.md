# guice-blazeds #
This project allows you to inject BlazeDS remoted objects with Guice. The jar is very small and there are no dependencies on any libraries except the servlet API and the Blaze jars.

With `guice-blazeds` you can annotate Blaze remoted services like this:

```
public class MyGuiceInjectedBlazeService {
	private MyDAO dao;

	@Inject           // a Guice annotation
	public MyGuiceInjectedBlazeService(MyDAO dao) {
		this.dao = dao;
	}

	@Transactional    // an annotation from warp-persist
	public MyDomainObject getMyDomainObject(Long id) {
		return dao.loadMyDomainObject(id);
	}
}
```


## Using guice-blazeds ##
`guice-blazeds.jar` has been tested with Blaze 3.2, but hopefully should work with all 3.x versions. Once you have BlazeDS installed, add the `guice-blazeds.jar` to your classpath, and then follow these simple steps:

### 1. Subclass AbstractGuiceFactory ###
Override the abstract `createInjectedObject` method so that objects returned are Guice-injected ones. Below is an example class that does this (note that the injector is created inside the constructor, but it does not have to be like that):

```
package mypackage;

import uk.co.chasetechnology.guice_blazeds.AbstractGuiceFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class MyGuiceFactory extends AbstractGuiceFactory{

	private Injector injector;

	public MyGuiceFactory() {
		injector = Guice.createInjector();
	}
	
	public <T> T createInjectedObject(Class<T> clazz) {
		return injector.getInstance(clazz);
	}
}
```

### 2. Configure BlazeDS ###
Add your `AbstractGuiceFactory` implementation as a factory in `WEB-INF/flex/services-config.xml` by appending a `<factories>` element at the end of the config file, containing a `<factory>` with an id of 'guice' and your factory as the class:

```
<services-config>
    ....
    <factories>
    	<factory id="guice" class="mypackage.MyGuiceFactory"/>
    </factories>
</services-config>
```

### 3. Guice Inject your Services ###
For every remoted class that you wish to be Guice-injected (i.e. the destinations in `WEB-INF/flex/remoting-config.xml`) simply make Guice the factory by adding a `<factory>` element:

```
<destination id="my-service">
   <properties>
      <source>mypackage.MyRemotedService</source>
      <factory>guice</factory>
   </properties>
</destination>
```


## Thats It! ##
Hope you find this useful. Courtesy of the [Chase Technology software development](http://www.chasetechnology.co.uk) team.