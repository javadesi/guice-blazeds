/**
 * Copyright (C) 2009 Chase Technology Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.chasetechnology.guice_blazeds;

import javax.servlet.ServletContext;

import flex.messaging.FactoryInstance;
import flex.messaging.FlexContext;
import flex.messaging.MessageBroker;
import flex.messaging.config.ConfigMap;
import flex.messaging.config.ConfigurationException;
import flex.messaging.config.ConfigurationManager;
import flex.messaging.factories.JavaFactory;
import flex.messaging.log.Log;
import flex.messaging.services.ServiceException;
import flex.messaging.util.ExceptionUtil;
import flex.messaging.util.StringUtils;

/**
 * This abstract class is meant to be overridden so that it can be used for Guice injection.
 * Guice should be injected in the <code>createInjectedObject</code> method. Exactly how you inject is up to you,
 * but one possibility is to create an injector in the constructor of this factory, and then reference that
 * injector within <code>createInjectedObject</code>, as follows:
 * 
 * <pre>
 * public class MyGuiceFactory extends AbstractGuiceFactory {
 *  
 *  private Injector injector;
 *  
 *  public MyGuiceFactory() {
 *  	injector = Guice.createInjector(....);
 *  }
 *  
 * 	public <T> T createInjectedObject(Class<T> clazz) {
 * 		return injector.getInstance(clazz);
 * 	}
 * }
 * </pre>
 * 
 * @author Doug Satchwell
 */
public abstract class AbstractGuiceFactory extends JavaFactory {
	private static final String ATTRIBUTE_ID = "attribute-id";

    /**
     * This method is overridden so that the <code>FactoryInstance</code> returned is a <code>GuiceFactoryInstance</code>.
     * With the exception of changing the instance created, the rest of the method is exactly the same
     * as for its superclass, thus keeping the ability to specify the scope.
     */
	@Override
	public FactoryInstance createFactoryInstance(String id, ConfigMap properties) {
		// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		// ++ only need to override the super method for this following line:
		// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		GuiceFactoryInstance instance = new GuiceFactoryInstance(this, id,properties);

		// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		// ++ the rest must be as for the super method
		// ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
		if (properties == null) {
			// Use destination id as the default attribute id to prevent
			// unwanted sharing.
			instance.setSource(instance.getId());
			instance.setScope(SCOPE_REQUEST);
			instance.setAttributeId(id);
		} else {
			instance.setSource(properties.getPropertyAsString(SOURCE, instance
					.getId()));
			instance.setScope(properties.getPropertyAsString(SCOPE,
					SCOPE_REQUEST));
			// Use destination id as the default attribute id to prevent
			// unwanted sharing.
			instance.setAttributeId(properties.getPropertyAsString(
					ATTRIBUTE_ID, id));
		}

		if (instance.getScope().equalsIgnoreCase(SCOPE_APPLICATION)) {
			try {
				ServletContext ctx = FlexContext.getServletConfig()
						.getServletContext();

				synchronized (ctx) {
					Object inst = ctx.getAttribute(instance.getAttributeId());
					if (inst == null) {
						inst = instance.createInstance();
						ctx.setAttribute(instance.getAttributeId(), inst);
					} else {
						Class configuredClass = instance.getInstanceClass();
						Class instClass = inst.getClass();
						if (configuredClass != instClass
								&& !configuredClass.isAssignableFrom(instClass)) {
							ServiceException e = new ServiceException();
							// e.setMessage(INVALID_CLASS_FOUND, new Object[] {
							// instance.getAttributeId(), "application",
							// instance.getId(),
							// instance.getInstanceClass(), inst.getClass()});
							e.setCode("Server.Processing");
							throw e;
						}
					}
					instance.applicationInstance = inst;

					// increment attribute-id reference count on MB
					MessageBroker mb = FlexContext.getMessageBroker();
					if (mb != null) {
						mb.incrementAttributeIdRefCount(instance
								.getAttributeId());
					}
				}
			} catch (Throwable t) {
				ConfigurationException ex = new ConfigurationException();
				// ex.setMessage(SINGLETON_ERROR, new Object[] {
				// instance.getSource(), id });
				ex.setRootCause(t);

				if (Log.isError())
					Log.getLogger(ConfigurationManager.LOG_CATEGORY).error(
							ex.getMessage() + StringUtils.NEWLINE
									+ ExceptionUtil.toString(t));

				throw ex;
			}
		} else if (instance.getScope().equalsIgnoreCase(SCOPE_SESSION)) {
			// increment attribute-id reference count on MB for Session scoped
			// instances
			MessageBroker mb = FlexContext.getMessageBroker();
			if (mb != null) {
				mb.incrementAttributeIdRefCount(instance.getAttributeId());
			}
		}
		return instance;
	}

	/**
	 * This is where we do the Guice injection. 
	 * 
	 * @param <T> type of class
	 * @param clazz to create an instance of
	 * @return an instance of the class
	 */
	public abstract <T> T createInjectedObject(Class<T> clazz);
}
