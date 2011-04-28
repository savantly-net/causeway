/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */


package org.apache.isis.runtimes.dflt.runtime.persistence.objectfactory;

import java.lang.reflect.Modifier;

import org.apache.isis.core.metamodel.services.ServicesInjector;
import org.apache.isis.core.metamodel.spec.ObjectInstantiationException;
import org.apache.isis.core.metamodel.spec.SpecificationLoader;
import org.apache.isis.core.metamodel.spec.SpecificationLoaderAware;
import org.apache.isis.runtimes.dflt.runtime.system.context.IsisContext;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.ObjectFactory;
import org.apache.isis.runtimes.dflt.runtime.system.persistence.PersistenceSession;

/**
 * Abstract adapter for {@link ObjectFactory}.
 * 
 * <p>
 * Implementation note: rather than use the <tt>*Aware</tt> interfaces, we
 * instead look up dependencies from the {@link IsisContext}. This is
 * necessary, for the {@link PersistenceSession} at least, because class
 * enhancers may hold a reference to the factory as part of the generated
 * bytecode. Since the {@link PersistenceSession} could change over the lifetime
 * of the instance (eg when using the {@link InMemoryObjectStore}), we must
 * always look the {@link PersistenceSession} from the
 * {@link IsisContext}. The same applies to the {@link ServicesInjector}.
 * 
 * <p>
 * In theory it would be possible to cache the {@link SpecificationLoader} and
 * inject using {@link SpecificationLoaderAware}, but since we are already using
 * the {@link IsisContext}, decided instead to use the same approach throughout.
 */
public abstract class ObjectFactoryAbstract implements ObjectFactory {

	private final Mode mode;

	public enum Mode {
		/**
		 * Fail if no {@link ObjectAdapterPersistor} has been injected.
		 */
		STRICT,
		/**
		 * Ignore if no {@link ObjectAdapterPersistor} has been injected (intended
		 * for testing only).
		 */
		RELAXED
	}

	public ObjectFactoryAbstract() {
		this(Mode.STRICT);
	}

	public ObjectFactoryAbstract(Mode mode) {
		this.mode = mode;
	}

	public <T> T instantiate(Class<T> cls) throws ObjectInstantiationException {

		if (mode == Mode.STRICT && getServicesInjector() == null) {
			throw new IllegalStateException(
					"ServicesInjector has not been injected into ObjectFactory");
		}
		if (Modifier.isAbstract(cls.getModifiers())) {
			throw new ObjectInstantiationException(
					"Cannot create an instance of an abstract class: " + cls);
		}
		T newInstance = doInstantiate(cls);

		if (getServicesInjector() != null) {
			getServicesInjector().injectDependencies(newInstance);
		}
		return newInstance;
	}

	// /////////////////////////////////////////////////////////////////
	// open, close
	// /////////////////////////////////////////////////////////////////

	/**
	 * Default implementation does nothing.
	 */
	public void open() {
	}

	/**
	 * Default implementation does nothing.
	 */
	public void close() {
	}

	// /////////////////////////////////////////////////////////////////
	// doInstantiate
	// /////////////////////////////////////////////////////////////////

	/**
	 * Hook method for subclasses to override.
	 */
	protected abstract <T> T doInstantiate(Class<T> cls)
			throws ObjectInstantiationException;

	// /////////////////////////////////////////////////////////////////
	// Dependencies (looked up from context)
	// /////////////////////////////////////////////////////////////////

	protected SpecificationLoader getSpecificationLoader() {
		return IsisContext.getSpecificationLoader();
	}

	protected PersistenceSession getPersistenceSession() {
		return IsisContext.getPersistenceSession();
	}

	protected ServicesInjector getServicesInjector() {
		return getPersistenceSession().getServicesInjector();
	}

}
