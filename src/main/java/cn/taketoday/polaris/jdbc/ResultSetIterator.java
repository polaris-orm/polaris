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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import cn.taketoday.polaris.IncorrectResultSizeDataAccessException;
import cn.taketoday.polaris.jdbc.support.JdbcUtils;
import cn.taketoday.polaris.util.Nullable;

/**
 * Iterator for a {@link ResultSet}. Tricky part here is getting
 * {@link #hasNext()} to work properly, meaning it can be called multiple times
 * without calling {@link #next()}.
 *
 * @author <a href="https://github.com/TAKETODAY">Harry Yang</a>
 * @since 1.0
 */
public abstract class ResultSetIterator<T> implements Iterator<T>, Spliterator<T>, AutoCloseable {
  // fields needed to read rows set
  protected final ResultSet resultSet;

  /**
   * Index of objects returned using next(), and as such, visible to users.
   */
  protected int iteratorIndex = -1;

  protected ResultSetIterator(ResultSet rs) {
    this.resultSet = rs;
  }

  // fields needed to properly implement
  @Nullable
  private ResultSetValue<T> next; // keep track of next item in case hasNext() is called multiple times

  private boolean resultSetFinished; // used to note when rows set exhausted

  @Override
  public boolean hasNext() {
    // check if we already fetched next item
    if (next != null) {
      return true;
    }
    // check if rows set already finished
    if (resultSetFinished) {
      return false;
    }
    // now fetch next item
    next = safeReadNext();
    // check if we got something
    if (next != null) {
      return true;
    }
    // no more items
    resultSetFinished = true;
    return false;
  }

  @Override
  public T next() {
    ResultSetValue<T> result = next;
    if (result == null) {
      if (resultSetFinished) {
        throw new NoSuchElementException();
      }
      result = safeReadNext();
      if (result == null) {
        resultSetFinished = true;
        throw new NoSuchElementException();
      }
    }
    else {
      next = null;
    }

    iteratorIndex++;
    return result.value;
  }

  @Override
  public void close() {
    JdbcUtils.closeQuietly(resultSet);
  }

  /**
   * Get the current item index. The first item has the index 0.
   *
   * @return -1 if the first item has not been retrieved. The index of the current item retrieved.
   */
  public int getCurrentIndex() {
    return iteratorIndex;
  }

  /**
   * If a remaining element exists, performs the given action on it,
   * returning {@code true}; else returns {@code false}.  If this
   * Spliterator is {@link #ORDERED} the action is performed on the
   * next element in encounter order.  Exceptions thrown by the
   * action are relayed to the caller.
   * <p>
   * Subsequent behavior of a spliterator is unspecified if the action throws
   * an exception.
   *
   * @param action The action
   * @return {@code false} if no remaining elements existed
   * upon entry to this method, else {@code true}.
   * @throws NullPointerException if the specified action is null
   */
  @Override
  public boolean tryAdvance(Consumer<? super T> action) {
    T next = readNext();
    if (next != null) {
      action.accept(next);
      return true;
    }
    return false;
  }

  @Override
  @Nullable
  public Spliterator<T> trySplit() {
    return null;
  }

  @Override
  public long estimateSize() {
    return Long.MAX_VALUE;
  }

  @Override
  public int characteristics() {
    return Spliterator.ORDERED;
  }

  /**
   * Performs the given action for each remaining element until all elements
   * have been processed or the action throws an exception.  Actions are
   * performed in the order of iteration, if that order is specified.
   * Exceptions thrown by the action are relayed to the caller.
   * <p>
   * The behavior of an iterator is unspecified if the action modifies the
   * collection in any way (even by calling the {@link #remove remove} method
   * or other mutator methods of {@code Iterator} subtypes),
   * unless an overriding class has specified a concurrent modification policy.
   * <p>
   * Subsequent behavior of an iterator is unspecified if the action throws an
   * exception.
   *
   * @param action The action to be performed for each element
   * @throws NullPointerException if the specified action is null
   * @implSpec <p>The default implementation behaves as if:
   * <pre>{@code
   *     while (hasNext())
   *         action.accept(next());
   * }</pre>
   */
  @Override
  public void forEachRemaining(Consumer<? super T> action) {
    final ResultSet rows = this.resultSet;
    try {
      while (rows.next()) {
        action.accept(readNext(rows));
      }
    }
    catch (SQLException ex) {
      throw handleReadError(ex);
    }
    finally {
      close();
    }
  }

  // -----------------------------------------------------------------------------------------------
  // Common methods
  // -----------------------------------------------------------------------------------------------

  /**
   * Returns a sequential {@code Stream} with this iterator as its source.
   *
   * @return a sequential {@code Stream} over the elements in this iterator
   * @apiNote This method must be used within a try-with-resources statement or similar
   * control structure to ensure that the stream's open connection is closed
   * promptly after the stream's operations have completed.
   */
  public Stream<T> stream() {
    return StreamSupport.stream(this, false)
            .onClose(this::close);
  }

  /**
   * Returns a possibly parallel {@code Stream} with this iterator as its
   * source. It is allowable for this method to return a sequential stream.
   *
   * @return a possibly parallel {@code Stream} over the elements in this
   * iterator
   * @apiNote This method must be used within a try-with-resources statement or similar
   * control structure to ensure that the stream's open connection is closed
   * promptly after the stream's operations have completed.
   */
  public Stream<T> parallelStream() {
    return StreamSupport.stream(this, true)
            .onClose(this::close);
  }

  public ResultSetIterable<T> asIterable() {
    return new ResultSetIterable<>() {

      @Override
      public Iterator<T> iterator() {
        return ResultSetIterator.this;
      }

      @Override
      public Spliterator<T> spliterator() {
        return ResultSetIterator.this;
      }

      @Override
      public void close() {
        ResultSetIterator.this.close();
      }
    };
  }

  /**
   * consume elements
   */
  public void consume(Consumer<T> consumer) {
    final ResultSet rows = this.resultSet;
    try {
      while (rows.next()) {
        consumer.accept(readNext(rows));
      }
    }
    catch (SQLException ex) {
      throw handleReadError(ex);
    }
    finally {
      close();
    }
  }

  /**
   * Fetch unique element
   */
  @Nullable
  public T unique() {
    try {
      T returnValue = readNext();
      if (returnValue != null && readNext() != null) {
        throw new IncorrectResultSizeDataAccessException(1);
      }
      return returnValue;
    }
    finally {
      close();
    }
  }

  /**
   * Fetch first element
   */
  @Nullable
  public T first() {
    try {
      return readNext();
    }
    finally {
      close();
    }
  }

  /**
   * Fetch list of elements
   */
  public List<T> list() {
    ArrayList<T> entities = new ArrayList<>();
    collect(entities);
    return entities;
  }

  /**
   * Fetch list of elements
   */
  public List<T> list(int initialCapacity) {
    ArrayList<T> entities = new ArrayList<>(initialCapacity);
    collect(entities);
    return entities;
  }

  /**
   * Fetch list of elements
   */
  public void collect(Collection<T> entities) {
    final ResultSet resultSet = this.resultSet;
    try {
      while (resultSet.next()) {
        entities.add(readNext(resultSet));
      }
    }
    catch (SQLException ex) {
      throw handleReadError(ex);
    }
    finally {
      close();
    }
  }

  private ResultSetValue<T> safeReadNext() {
    final ResultSet resultSet = this.resultSet;
    try {
      return resultSet.next() ? new ResultSetValue<>(readNext(resultSet)) : null;
    }
    catch (SQLException ex) {
      throw handleReadError(ex);
    }
  }

  @Nullable
  private T readNext() {
    final ResultSet resultSet = this.resultSet;
    try {
      return resultSet.next() ? readNext(resultSet) : null;
    }
    catch (SQLException ex) {
      throw handleReadError(ex);
    }
  }

  protected RuntimeException handleReadError(SQLException ex) {
    return new PersistenceException("Database read error: " + ex.getMessage(), ex);
  }

  protected abstract T readNext(ResultSet resultSet) throws SQLException;

  static final class ResultSetValue<T> {
    public final T value;

    private ResultSetValue(T value) {
      this.value = value;
    }
  }

}
