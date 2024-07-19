/*
 * Copyright 2017 - 2023 the original author or authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see [http://www.gnu.org/licenses/]
 */

package cn.taketoday.jdbc;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * User defined operations when build a PreparedStatement
 *
 * @author <a href="https://github.com/TAKETODAY">Harry Yang</a>
 * @since 4.0 2022/9/10 00:33
 */
public interface QueryStatementCallback {

  /**
   * User defined operations when build a PreparedStatement
   *
   * @param statement PreparedStatement
   * @see NamedQuery#buildStatement
   */
  void doWith(PreparedStatement statement) throws SQLException;

}
