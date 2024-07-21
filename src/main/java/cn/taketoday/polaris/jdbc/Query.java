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

import java.io.InputStream;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;

import cn.taketoday.lang.Nullable;
import cn.taketoday.polaris.jdbc.type.TypeHandler;

/**
 * <p>
 * better to use :
 * create queries with {@link JdbcConnection} class instead,
 * using try-with-resource blocks
 * <pre>
 * try (Connection con = repositoryManager.open()) {
 *    return repositoryManager.createQuery(query, name, returnGeneratedKeys)
 *                .fetch(Pojo.class);
 * }
 * </pre>
 * </p>
 *
 * @author <a href="https://github.com/TAKETODAY">Harry Yang</a>
 * @since 1.0 2023/1/18 23:17
 */
public final class Query extends AbstractQuery {

  private final ArrayList<ParameterBinder> queryParameters = new ArrayList<>();

  public Query(JdbcConnection connection, String querySQL, boolean generatedKeys) {
    super(connection, querySQL, generatedKeys);
  }

  public Query(JdbcConnection connection, String querySQL, @Nullable String[] columnNames) {
    super(connection, querySQL, columnNames);
  }

  protected Query(JdbcConnection connection, String querySQL, boolean generatedKeys, @Nullable String[] columnNames) {
    super(connection, querySQL, generatedKeys, columnNames);
  }

  public Query addParameter(int value) {
    addParameter(ParameterBinder.forInt(value));
    return this;
  }

  public Query addParameter(long value) {
    addParameter(ParameterBinder.forLong(value));
    return this;
  }

  public Query addParameter(String value) {
    addParameter(ParameterBinder.forString(value));
    return this;
  }

  public Query addParameter(boolean value) {
    addParameter(ParameterBinder.forBoolean(value));
    return this;
  }

  public Query addParameter(InputStream value) {
    addParameter(ParameterBinder.forBinaryStream(value));
    return this;
  }

  public Query addParameter(LocalDate value) {
    addParameter(ParameterBinder.forDate(Date.valueOf(value)));
    return this;
  }

  public Query addParameter(LocalTime value) {
    addParameter(ParameterBinder.forTime(Time.valueOf(value)));
    return this;
  }

  public Query addParameter(LocalDateTime value) {
    addParameter(ParameterBinder.forTimestamp(Timestamp.valueOf(value)));
    return this;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Query addParameter(Object value) {
    TypeHandler typeHandler = getTypeHandlerManager().getTypeHandler(value.getClass());
    addParameter(ParameterBinder.forTypeHandler(typeHandler, value));
    return this;
  }

  public Query addParameter(ParameterBinder binder) {
    queryParameters.add(binder);
    return this;
  }

  //

  public Query setParameter(int pos, String value) {
    setParameter(pos, ParameterBinder.forString(value));
    return this;
  }

  public Query setParameter(int pos, int value) {
    setParameter(pos, ParameterBinder.forInt(value));
    return this;
  }

  public Query setParameter(int pos, long value) {
    setParameter(pos, ParameterBinder.forLong(value));
    return this;
  }

  public Query setParameter(int pos, boolean value) {
    setParameter(pos, ParameterBinder.forBoolean(value));
    return this;
  }

  public Query setParameter(int pos, InputStream value) {
    setParameter(pos, ParameterBinder.forBinaryStream(value));
    return this;
  }

  public Query setParameter(int pos, LocalDate value) {
    setParameter(pos, ParameterBinder.forDate(Date.valueOf(value)));
    return this;
  }

  public Query setParameter(int pos, LocalTime value) {
    setParameter(pos, ParameterBinder.forTime(Time.valueOf(value)));
    return this;
  }

  public Query setParameter(int pos, LocalDateTime value) {
    setParameter(pos, ParameterBinder.forTimestamp(Timestamp.valueOf(value)));
    return this;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public Query setParameter(int pos, Object value) {
    TypeHandler typeHandler = getTypeHandlerManager().getTypeHandler(value.getClass());
    setParameter(pos, ParameterBinder.forTypeHandler(typeHandler, value));
    return this;
  }

  public Query setParameter(int pos, ParameterBinder binder) {
    queryParameters.set(pos, binder);
    return this;
  }

  public ArrayList<ParameterBinder> getQueryParameters() {
    return queryParameters;
  }

  /**
   * clear parameter
   */
  public void clearParameters() {
    queryParameters.clear();
  }

  //

  @Override
  protected void postProcessStatement(PreparedStatement statement) {
    // parameters assignation to query
    int paramIdx = 1;
    for (ParameterBinder binder : queryParameters) {
      try {
        binder.bind(statement, paramIdx++);
      }
      catch (SQLException e) {
        throw new ParameterBindFailedException(
                "Error binding parameter index: '" + (paramIdx - 1) + "' - " + e.getMessage(), e);
      }
    }
  }

  //
  @Override
  public Query setCaseSensitive(boolean caseSensitive) {
    super.setCaseSensitive(caseSensitive);
    return this;
  }

  @Override
  public Query setAutoDerivingColumns(boolean autoDerivingColumns) {
    super.setAutoDerivingColumns(autoDerivingColumns);
    return this;
  }

  @Override
  public Query throwOnMappingFailure(boolean throwOnMappingFailure) {
    super.throwOnMappingFailure(throwOnMappingFailure);
    return this;
  }

  @Override
  public Query addColumnMapping(String columnName, String propertyName) {
    super.addColumnMapping(columnName, propertyName);
    return this;
  }

  /**
   * add a Statement processor when {@link  #buildStatement() build a PreparedStatement}
   */
  @Override
  public Query processStatement(@Nullable QueryStatementCallback callback) {
    super.processStatement(callback);
    return this;
  }

  @Override
  public Query addToBatch() {
    super.addToBatch();
    return this;
  }

}
