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

package cn.taketoday.polaris;

import cn.taketoday.polaris.util.Nullable;

/**
 * @author <a href="https://github.com/TAKETODAY">Harry Yang</a>
 * @since 1.0 2022/9/20 12:47
 */
public interface BatchPersistListener {

  /**
   * before batch processing
   *
   * @param execution batch execution
   * @param implicitExecution implicit Execution
   */
  default void beforeProcessing(BatchExecution execution, boolean implicitExecution) { }

  /**
   * after batch processing
   *
   * @param execution batch execution
   * @param implicitExecution implicit Execution
   * @param exception batch execution error
   */
  void afterProcessing(BatchExecution execution, boolean implicitExecution, @Nullable Throwable exception);

}
