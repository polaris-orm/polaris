/*
 * Copyright 2024 the original author or authors.
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

package cn.taketoday.polaris.beans;

import java.lang.reflect.Constructor;

import cn.taketoday.lang.Nullable;
import cn.taketoday.reflect.Accessor;

/**
 * @author TODAY 2021/8/27 21:41
 */
public abstract class ConstructorAccessor extends BeanInstantiator implements Accessor {

  protected final Constructor<?> constructor;

  public ConstructorAccessor(Constructor<?> constructor) {
    this.constructor = constructor;
  }

  @Override
  public Constructor<?> getConstructor() {
    return constructor;
  }

  /**
   * Invoke {@link java.lang.reflect.Constructor} with given args
   *
   * @return returns Object
   * @since 4.0
   */
  @Override
  public abstract Object doInstantiate(@Nullable Object[] args);
}
