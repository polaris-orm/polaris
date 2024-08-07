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

package cn.taketoday.polaris.jdbc;

import java.sql.SQLException;

import cn.taketoday.polaris.InvalidDataAccessResourceUsageException;

/**
 * SQL 无效时抛出的异常。这类异常总是有一个{@code java.sql.SQLException} 作为 root cause。
 *
 * @author Rod Johnson
 * @author <a href="https://github.com/TAKETODAY">Harry Yang</a>
 * @see InvalidResultSetAccessException
 */
public class BadSqlGrammarException extends InvalidDataAccessResourceUsageException {

  private final String sql;

  /**
   * Constructor for BadSqlGrammarException.
   *
   * @param task name of current task
   * @param sql the offending SQL statement
   * @param ex the root cause
   */
  public BadSqlGrammarException(String task, String sql, SQLException ex) {
    super(task + "; bad SQL grammar [" + sql + "]", ex);
    this.sql = sql;
  }

  /**
   * Return the wrapped SQLException.
   */
  public SQLException getSQLException() {
    return (SQLException) getCause();
  }

  /**
   * Return the SQL that caused the problem.
   */
  public String getSql() {
    return this.sql;
  }

}
