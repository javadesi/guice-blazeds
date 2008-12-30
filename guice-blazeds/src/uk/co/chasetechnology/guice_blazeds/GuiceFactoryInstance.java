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

import flex.messaging.FlexConfigurable;
import flex.messaging.config.ConfigMap;
import flex.messaging.factories.JavaFactoryInstance;

/**
 * Instances of this class are created by the <code>AbstractGuiceFactory</code>.
 * All methods are the same as its parent <code>JavaFactoryInstance</code>
 * with the exception of <code>createInstance</code>, which delegates 
 * object creation to the calling <code>AbstractGuiceFactory</code>.
 * 
 * @author Doug Satchwell
 */
public class GuiceFactoryInstance extends JavaFactoryInstance{
	
	// hiding the super class's member of the same name
	Object applicationInstance;
	protected AbstractGuiceFactory factory;

    /**
     * Constructs a <code>GuiceFactoryInstance</code>, assigning its factory, id, 
     * and properties.
     * 
     * @param factory that created this instance.
     * @param id for the parent <code>JavaFactoryInstance</code>.
     * @param properties for the parent <code>JavaFactoryInstance</code>.
     */
	public GuiceFactoryInstance(AbstractGuiceFactory factory, String id,
			ConfigMap properties) {
		super(factory, id, properties);
		this.factory = factory;
	}

	/**
	 * Delegates object creation to the <code>AbstractGuiceFactory</code> where Guice-injected
	 * objects can be made.
	 * 
	 * @return a Guice-injected object
	 */
	@Override
	public Object createInstance() {
		// this line is the only change
		Object inst = factory.createInjectedObject(getInstanceClass());
		
		if (inst instanceof FlexConfigurable)
			((FlexConfigurable) inst).initialize(getId(), getProperties());

		return inst;
	}
}
