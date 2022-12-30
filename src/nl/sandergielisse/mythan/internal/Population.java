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

import java.util.ArrayList;
import java.util.List;

import nl.sandergielisse.mythan.internal.genes.Genome;

public class Population {

  private final EvolutionCore core;
  private final List<Species> species = new ArrayList<>();

  public Population(final EvolutionCore core) {
    this.core = core;
  }

  public int getPopulationSize() {
    return this.core.getPopulationManager().getPopulationSize();
  }

  public List<Species> getSpecies() {
    return this.species;
  }

  public void addGenome(final Genome genome) {
    final Species species = this.classify(genome);
    species.getMembers().add(genome);
  }

  /**
   * Sets the genome's species and uses it as a representative if a new species is created.
   */
  private Species classify(final Genome genome) {
    for (final Species existing : this.species) {
      if (existing.isCompatible(genome)) {
        genome.setSpecies(existing);
        return existing;
      }
    }

    final Species ge = new Species(genome);
    this.species.add(ge);

    return ge;
  }

  /**
   * Returns the best performing genome of the current population.
   */
  public Genome getBestPerforming() {
    Genome best = null;
    double bestFitness = -1;

    for (final Species sp : this.species) {
      for (final Genome g : sp.getMembers()) {
        if (best == null || g.getFitness() > bestFitness) {
          best = g;
          bestFitness = g.getFitness();
        }
      }
    }

    return best;

  }
}
