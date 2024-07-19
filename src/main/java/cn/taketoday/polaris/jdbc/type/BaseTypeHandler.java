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

package cn.taketoday.polaris.jdbc.type;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import cn.taketoday.lang.Nullable;

/**
 * The base {@link TypeHandler} for references a generic type.
 * <p>
 * Important: This class never call the {@link ResultSet#wasNull()} and
 * {@link CallableStatement#wasNull()} method for handling the SQL {@code NULL} value.
 * In other words, {@code null} value handling should be performed on subclass.
 * </p>
 *
 * @author Clinton Begin
 * @author Simone Tripodi
 * @author Kzuki Shimizu
 * @author <a href="https://github.com/TAKETODAY">Harry Yang</a>
 */
public abstract class BaseTypeHandler<T> implements TypeHandler<T> {

  @Override
  public void setParameter(PreparedStatement ps, int parameterIndex, @Nullable T parameter) throws SQLException {
    if (parameter == null) {
      setNullParameter(ps, parameterIndex);
    }
    else {
      setNonNullParameter(ps, parameterIndex, parameter);
    }
  }

  public void setNullParameter(PreparedStatement ps, int parameterIndex) throws SQLException {
    ps.setObject(parameterIndex, null);
  }

  public void setNonNullParameter(PreparedStatement ps, int parameterIndex, T parameter) throws SQLException {
    ps.setObject(parameterIndex, parameter);
  }

}
