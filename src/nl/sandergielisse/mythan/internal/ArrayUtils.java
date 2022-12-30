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

import nl.sandergielisse.mythan.internal.genes.Connection;

public enum ArrayUtils {
  ;

  public static boolean equals(final List<Connection> a, final List<Connection> b) {
    if (a.size() != b.size())
      return false;

    for (int i = 0; i < a.size(); i++) {
      if (!a.get(i).equals(b.get(i))) {
        return false;
      }
    }
    return true;
  }

  public static double getAverage(final List<Double> list) {
    double total = 0;
    double counter = 0;
    for (final double d : list) {
      total += d;
      counter++;
    }
    return total / counter;
  }

  public static boolean allClose(final List<Double> list, final double allowedDistance) {
    final double average = ArrayUtils.getAverage(list);
    boolean allClose = true;
    for (final double answer : list) {
      if (Math.abs(average - answer) > allowedDistance) {
        allClose = false;
        break;
      }
    }
    return allClose;
  }
}
