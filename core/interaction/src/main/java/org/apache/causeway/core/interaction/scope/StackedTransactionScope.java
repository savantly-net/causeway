
/*
 * Copyright 2002-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.causeway.core.interaction.scope;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.lang.Nullable;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class StackedTransactionScope implements Scope {


    @Override
	public Object get(String name, ObjectFactory<?> objectFactory) {

        Object key = currentKeyOnTransactionStack();

        ScopedObjectsHolder scopedObjects = (ScopedObjectsHolder) TransactionSynchronizationManager.getResource(key);
		if (scopedObjects == null) {
			scopedObjects = new ScopedObjectsHolder();
			TransactionSynchronizationManager.registerSynchronization(new CleanupSynchronization(scopedObjects));
			TransactionSynchronizationManager.bindResource(key, scopedObjects);
		}
		// NOTE: Do NOT modify the following to use Map::computeIfAbsent. For details,
		// see https://github.com/spring-projects/spring-framework/issues/25801.
		Object scopedObject = scopedObjects.scopedInstances.get(name);
		if (scopedObject == null) {
			scopedObject = objectFactory.getObject();
			scopedObjects.scopedInstances.put(name, scopedObject);
		}
		return scopedObject;
	}

    @Override
	@Nullable
	public Object remove(String name) {
        Object key = currentKeyOnTransactionStack();
        ScopedObjectsHolder scopedObjects = (ScopedObjectsHolder) TransactionSynchronizationManager.getResource(key);
		if (scopedObjects != null) {
			scopedObjects.destructionCallbacks.remove(name);
			return scopedObjects.scopedInstances.remove(name);
		}
		else {
			return null;
		}
	}

	@Override
	public void registerDestructionCallback(String name, Runnable callback) {
		ScopedObjectsHolder scopedObjects = (ScopedObjectsHolder) TransactionSynchronizationManager.getResource(this);
		if (scopedObjects != null) {
			scopedObjects.destructionCallbacks.put(name, callback);
		}
	}

    private static final ThreadLocal<Stack<Object>> transactionStackThreadLocal = ThreadLocal.withInitial(() -> {
        Stack<Object> stack = new Stack<>();
        stack.push(new Object());
        return stack;
    });

    /**
     * Maintains a stack of keys, where the top-most is the key managed by {@link TransactionSynchronizationManager}
     * holding the {@link ScopedObjectsHolder} for the current transaction.
     *
     * <p>
     *     If a transaction is suspended, then the {@link CleanupSynchronization#suspend() suspend} callback is used
     *     to pop a new key onto the stack, unbinding the previous key's resources (in other words, the
     *     {@link org.apache.causeway.applib.annotation.TransactionScope transaction-scope}d beans of the suspended
     *     transaction) from {@link TransactionSynchronizationManager}.  As transaction-scoped beans are then resolved,
     *     they will be associated with the new key.
     * </p>
     *
     * <p>
     *     Conversely, when a tranaction is resumed, then the process is reversed; the old key is popped, and the previous
     *     key is rebound to the {@link TransactionSynchronizationManager}, meaning that the previous transaction's
     *     {@link org.apache.causeway.applib.annotation.TransactionScope transaction-scope}d beans are brought back.
     * </p>
     *
     * @see #currentKeyOnTransactionStack()
     * @see #pushNewKeyOntoTransactionStack()
     * @see #popOldKeyFromTransactionStack()
     * @see #transactionStackThreadLocal
     */
    private static Stack<Object> transactionStack() {
        return transactionStackThreadLocal.get();
    }
    /**
     * @see #transactionStack()
     */
    private Object currentKeyOnTransactionStack() {
        return transactionStack().peek();
    }
    /**
     * @see #transactionStack()
     */
    private static void pushNewKeyOntoTransactionStack() {
        transactionStack().push(new Object());
    }
    /**
     * @see #transactionStack()
     */
    private static void popOldKeyFromTransactionStack() {
        transactionStack().pop();
    }


    @Override
	@Nullable
	public Object resolveContextualObject(String key) {
		return null;
	}

	@Override
	@Nullable
	public String getConversationId() {
		return TransactionSynchronizationManager.getCurrentTransactionName();
	}


	/**
	 * Holder for scoped objects.
	 */
	static class ScopedObjectsHolder {

		final Map<String, Object> scopedInstances = new HashMap<>();

		final Map<String, Runnable> destructionCallbacks = new LinkedHashMap<>();
	}


	private class CleanupSynchronization implements TransactionSynchronization {

		private final ScopedObjectsHolder scopedObjects;

		public CleanupSynchronization(ScopedObjectsHolder scopedObjects) {
			this.scopedObjects = scopedObjects;
		}

		@Override
		public void suspend() {
            TransactionSynchronizationManager.unbindResource(currentKeyOnTransactionStack());
            pushNewKeyOntoTransactionStack();  // subsequent calls to obtain a @TransactionScope'd bean will be against this key
		}

		@Override
		public void resume() {
            popOldKeyFromTransactionStack(); // the now-completed transaction's @TransactionScope'd beans are no longer required, and will be GC'd.
			TransactionSynchronizationManager.bindResource(currentKeyOnTransactionStack(), this.scopedObjects);
		}

		@Override
		public void afterCompletion(int status) {
			TransactionSynchronizationManager.unbindResourceIfPossible(StackedTransactionScope.this.currentKeyOnTransactionStack());
			for (Runnable callback : this.scopedObjects.destructionCallbacks.values()) {
				callback.run();
			}
			this.scopedObjects.destructionCallbacks.clear();
			this.scopedObjects.scopedInstances.clear();
		}
	}

}
