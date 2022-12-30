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
package nl.sandergielisse.mythan.internal.genes;

public class Gene implements Cloneable {

  private int innovationNumber;
  private final int from;
  private final int to;
  private double weight;
  private boolean enabled;

  public Gene(final int innovationNumber, final int from, final int to, final double weight, final boolean enabled) {
    this.innovationNumber = innovationNumber;
    this.from = from;
    this.to = to;
    this.weight = weight;
    this.enabled = enabled;
  }

  public int getInnovationNumber() {
    return this.innovationNumber;
  }

  public void setInnovationNumber(final int innovationNumber) {
    this.innovationNumber = innovationNumber;
  }

  public int getFrom() {
    return this.from;
  }

  public int getTo() {
    return this.to;
  }

  public double getWeight() {
    return this.weight;
  }

  public void setWeight(final double weight) {
    this.weight = weight;
  }

  public boolean isEnabled() {
    return this.enabled;
  }

  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;
  }

  @Override
  protected Gene clone() {
    return new Gene(this.innovationNumber, this.from, this.to, this.weight, this.enabled);
  }

  @Override
  public String toString() {
    return "Gene [innovationNumber=" + this.innovationNumber + ", from=" + this.from + ", to=" + this.to + ", weight=" + this.weight + ", enabled=" + this.enabled + "]";
  }
}
