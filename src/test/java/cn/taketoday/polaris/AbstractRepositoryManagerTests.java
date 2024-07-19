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

import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.stream.Stream;

import cn.taketoday.polaris.jdbc.JdbcConnection;
import cn.taketoday.polaris.jdbc.RepositoryManager;

/**
 * @author <a href="https://github.com/TAKETODAY">Harry Yang</a>
 * @since 4.0 2022/9/7 20:10
 */
public abstract class AbstractRepositoryManagerTests {

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  @ParameterizedTest(name = "[{index}] {0}")
  @MethodSource("cn.taketoday.persistence.AbstractRepositoryManagerTests#data")
  public @interface ParameterizedRepositoryManagerTest {

  }

  public Stream<Named<RepositoryManager>> data() {
    return Stream.of(
            Named.named("H2", createRepositoryManager(DbType.H2))
//            Named.named("HyperSQL", createRepositoryManager(DbType.HyperSQL))
    );
  }

  protected RepositoryManager createRepositoryManager(DbType dbType) {
    RepositoryManager repositoryManager = new RepositoryManager(dbType.url, dbType.user, dbType.pass);
    if (dbType == DbType.HyperSQL) {
      try (JdbcConnection con = repositoryManager.open()) {
        con.createNamedQuery("set database sql syntax MSS true")
                .executeUpdate();
      }
    }

    prepareTestsData(dbType, repositoryManager);
    return repositoryManager;
  }

  protected void prepareTestsData(DbType dbType, RepositoryManager repositoryManager) { }

  public enum DbType {
    H2("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "sa", ""),
    HyperSQL("jdbc:hsqldb:mem:testmemdb", "SA", "");

    public final String url;
    public final String user;
    public final String pass;

    DbType(String url, String user, String pass) {
      this.url = url;
      this.user = user;
      this.pass = pass;
    }
  }

}