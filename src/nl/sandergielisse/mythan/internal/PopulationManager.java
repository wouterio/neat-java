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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.sandergielisse.mythan.Setting;
import nl.sandergielisse.mythan.internal.genes.Gene;
import nl.sandergielisse.mythan.internal.genes.Genome;

public class PopulationManager {

  private int currentGeneration = 1;
  private final EvolutionCore evolutionCore;
  private final Population currentPopulation;
  private int populationSize = 500;
  private Genome latestFitness;

  public PopulationManager(final EvolutionCore evolutionCore) {
    this.evolutionCore = evolutionCore;
    this.currentPopulation = new Population(this.evolutionCore);
  }

  public Population getPopulation() {
    return this.currentPopulation;
  }

  public int getPopulationSize() {
    return this.populationSize;
  }

  public EvolutionCore getCore() {
    return this.evolutionCore;
  }

  public List<Species> getSpecies() {
    return this.currentPopulation.getSpecies();
  }

  public Genome getLatestFitness() {
    return this.latestFitness;
  }

  public void newGeneration() {
    this.currentGeneration++;

    // start with calling getBestPerforming() for every species so that calculateFitness() is executed
    final Map<Species, List<Genome>> bestPerforming = new HashMap<>();
    for (final Species sp : this.getSpecies()) {
      bestPerforming.put(sp, sp.getBestPerforming());
    }

    // calculate the total average
    double sum = 0;
    for (final Species sp : this.getSpecies()) {
      sum += sp.getAverageFitness();
    }

    final HashMap<Species, Genome> vips = new HashMap<>();
    final Iterator<Species> it = this.getSpecies().iterator();
    while (it.hasNext()) {
      final Species sp = it.next();

      /**
       * We start by eliminating the worst performing genome's from every species.
       */
      final List<Genome> best = bestPerforming.get(sp);
      if (best == null)
        throw new AssertionError();

      final double remove = Math.ceil(best.size() * this.evolutionCore.getSetting(Setting.GENERATION_ELIMINATION_PERCENTAGE));
      final int start = (int) (Math.floor(best.size() - remove) + 1);

      for (int i = start; i < best.size(); i++) {
        final Genome bad = best.get(i);
        sp.remove(bad);
      }

      /**
       * Remove all species who's fitness has not reached the max for 15 generations.
       */
      sp.setFailedGenerations(sp.getFailedGenerations() + 1);

      if (sp.getFailedGenerations() > 15) {
        System.out.println("Species was removed, because it failed for 15 generations.");
        it.remove();
        continue;
      }

      /**
       * Remove all species which don't get any breeding spots in the next generation.
       */

      final double totalSize = this.populationSize;
      final double breedsAllowed = Math.floor(sp.getAverageFitness() / sum * totalSize) - 1.0;

      if (breedsAllowed < 1) {
        // System.out.println("Species was removed, breeds allowed < 1.");
        it.remove();
        continue;
      }

      /**
       * Copy the best of every species directly into the next generation.
       */
      final Genome bestOfSpecies = best.get(0);
      // vips.put(sp, bestOfSpecies);
    }

    {
      int size = 0;
      for (final Species sp : this.getSpecies()) {
        size += sp.getMembers().size();
      }
      System.out.println("Building generation " + this.currentGeneration + "... Now " + this.getSpecies().size() + " species active (with a total size of " + size + ").");
    }

    if (this.getSpecies().isEmpty()) {
      throw new RuntimeException("All species died");
    }

    int populationSize = 0;

    final Map<Species, Set<Genome>> oldMembers = new HashMap<>();
    for (final Species sp : this.getSpecies()) {
      oldMembers.put(sp, new HashSet<>(sp.getMembers()));

      sp.getMembers().clear();

      final Genome vip = vips.get(sp);
      if (vip != null) {
        sp.getMembers().add(vip);
        populationSize++;
      }
    }

    /**
     * Fill the population with new children.
     */
    while (populationSize < this.populationSize) {
      final Species randomSpecies = Random.random(this.getSpecies());
      final Set<Genome> oldMems = oldMembers.get(randomSpecies);

      if (oldMems != null) {
        if (Random.success(this.evolutionCore.getSetting(Setting.BREED_CROSS_CHANCE))) {
          // cross
          final Genome father = Random.random(oldMems);
          final Genome mother = Random.random(oldMems);

          Genome.crossAndAdd(father, mother);
        } else {
          // don't cross just copy
          final Genome g = Random.random(oldMems).clone();
          g.mutate();
          randomSpecies.getMembers().add(g);
        }
        populationSize++;
      }
    }

    final Iterator<Species> its = this.getSpecies().iterator();
    while (its.hasNext()) {
      final Species sp = its.next();
      if (sp.getMembers().isEmpty()) {
        its.remove();
      }
    }

    for (final Species sp : this.getSpecies()) {
      sp.update();
    }

    /**
     * Display how the new population performed.
     */
    this.latestFitness = this.currentPopulation.getBestPerforming();

    System.out.println("Best performing genome [" + this.latestFitness.getId() + "] had fitness of " + this.latestFitness.getFitness() + " and was part of species " + this.latestFitness.getSpecies().getId() + " which has " + this.latestFitness.getSpecies().getMembers().size() + " members");
    System.out.println(this.latestFitness.toString());
  }

  public void initialize(final int populationSize) {
    this.populationSize = populationSize;

    if (this.currentGeneration != 1)
      throw new UnsupportedOperationException("The initialize() method should only be called for the first generation");

    final Genome init = this.initial();

    for (int i = 0; i < this.populationSize; i++) {
      // new genome, choose random weights
      final Genome genome = init.clone();
      for (final Gene gene : genome.getGenes()) { // genes are cloned as well
        final double dist = this.evolutionCore.getSetting(Setting.MUTATION_WEIGHT_CHANCE_RANDOM_RANGE);
        gene.setWeight(Random.random(-dist, dist));
      }
      // System.out.println("GENOME " + genome.toString());
      this.evolutionCore.getPopulationManager().currentPopulation.addGenome(genome);
    }
  }

  private Genome initial() {
    final Integer[] inputs = new Integer[this.evolutionCore.getInputSize()];
    for (int i = 0; i < inputs.length; i++)
      inputs[i] = i + 1;

    final Integer[] outputs = new Integer[this.evolutionCore.getOutputSize()];
    for (int i = 0; i < outputs.length; i++)
      outputs[i] = inputs.length + i + 1;

    final double dist = this.evolutionCore.getSetting(Setting.MUTATION_WEIGHT_CHANCE_RANDOM_RANGE);
    final Genome gen = new Genome(this.evolutionCore, null, inputs, outputs);
    for (int in = 1; in <= this.evolutionCore.getInputSize(); in++) {
      for (int out = 1; out <= this.evolutionCore.getOutputSize(); out++) {
        gen.addGene(new Gene(this.evolutionCore.getNextInnovationNumber(), in, this.evolutionCore.getInputSize() + out, Random.random(-dist, dist), true), null, null);
      }
    }
    return gen;
  }

  public int getGeneration() {
    return this.currentGeneration;
  }
}
