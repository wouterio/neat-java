/**
 * Copyright 2016 Alexander Gielisse
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.sandergielisse.mythan.internal;

import java.util.List;
import java.util.Set;

public enum Random {
  ;

  private static final java.util.Random random = new java.util.Random();




  static {
    random.setSeed(0L);
  }

  private static java.util.Random getRandom() {
    return Random.random;
  }

  /**
   * Returns a random object from the given array.
   */
  public static <T> T random(final T[] array) {
    if (array.length == 0)
      throw new UnsupportedOperationException("Given array can not be empty");

    return array[random.nextInt(array.length)];
  }

  /**
   * Returns a random object from the given set.
   */
  public static <T> T random(final Set<T> set) {
    if (set.size() == 0)
      throw new UnsupportedOperationException("Given set can not be empty");

    final int size = set.size();
    final int item = random.nextInt(size);
    int i = 0;
    for (final T t : set) {
      if (i == item)
        return t;
      i = i + 1;
    }
    throw new AssertionError();
  }

  /**
   * Returns a random object from the given list.
   */
  public static <T> T random(final List<T> list) {
    if (list.size() == 0)
      throw new UnsupportedOperationException("Given list can not be empty");

    return list.get(random.nextInt(list.size()));
  }

  /**
   * Picks a random number X (for which 0 <= X < 1) and returns true if the random number is smaller than the chance.
   *
   * The smaller the given chance, the more unlikely this method will return true.
   */
  public static boolean success(final double chance) {
    return random.nextDouble() <= chance;
  }

  /**
   * Returns a random double between a given min and max
   */
  public static double random(final double min, final double max) {
    if (min >= max)
      throw new IllegalArgumentException("Min (" + min + ") can not be bigger than or equal to max (" + max + ")");
    return min + (max - min) * random.nextDouble();
  }
}
