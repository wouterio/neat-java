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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import nl.sandergielisse.mythan.BackTraceTask;
import nl.sandergielisse.mythan.Network;
import nl.sandergielisse.mythan.Setting;
import nl.sandergielisse.mythan.internal.ArrayUtils;
import nl.sandergielisse.mythan.internal.EvolutionCore;
import nl.sandergielisse.mythan.internal.InnovationNumber;
import nl.sandergielisse.mythan.internal.Random;
import nl.sandergielisse.mythan.internal.Species;

public class Genome implements Cloneable, Network {

  private static int counter;
  private final int id = Genome.counter++;

  public int getId() {
    return this.id;
  }

  /**
   * The TreeMap will make sure the genes are always ordered by increasing innovation number.
   */
  private Map<InnovationNumber, Gene> genes = new TreeMap<>();
  private final EvolutionCore core;

  private List<Integer> inputNodes = new ArrayList<>();
  private List<Integer> outputNodes = new ArrayList<>();

  private Species species;
  private double fitness = Double.NaN;


  public Genome(final EvolutionCore core, final Species member, final Integer[] inputNodes, final Integer[] outputNodes) {
    this.core = core;
    this.species = member;
    for (final int in : inputNodes)
      this.addInputNode(in);
    for (final int out : outputNodes)
      this.addOutputNode(out);
  }

  public void setSpecies(final Species sp) {
    if (Double.isFinite(this.fitness))
      throw new UnsupportedOperationException("setSpecies() must be called before getFitness()");
    this.species = sp;
  }

  public Species getSpecies() {
    return this.species;
  }

  public Integer[] getInputs() {
    return this.inputNodes.toArray(new Integer[this.inputNodes.size()]);
  }

  public Integer[] getOutputs() {
    return this.outputNodes.toArray(new Integer[this.outputNodes.size()]);
  }

  /**
   * Please note that the returned list is read-only.
   */
  public List<Integer> getNodes(final boolean includeInput, final boolean includeHidden, final boolean includeOutput) {
    final List<Integer> ids = new ArrayList<>();

    for (final int input : this.getAllNodes()) {
      if (this.isInputNode(input) && !includeInput)
        continue;
      if (this.isHiddenNode(input) && !includeHidden)
        continue;
      if (this.isOutputNode(input) && !includeOutput)
        continue;

      ids.add(input);
    }

    return ids;
  }

  public int getHighestNode() {
    final List<Integer> its = this.getAllNodes();
    return its.get(its.size() - 1);
  }

  public List<Integer> getAllNodes() {
    final List<Integer> ids = new ArrayList<>();
    for (final Gene gene : this.getGenes()) {
      if (!ids.contains(gene.getFrom()))
        ids.add(gene.getFrom());
      if (!ids.contains(gene.getTo()))
        ids.add(gene.getTo());
    }
    Collections.sort(ids);
    return ids;
  }

  public boolean isHiddenNode(final int node) {
    return !this.isInputNode(node) && !this.isOutputNode(node);
  }

  public void addInputNode(final int node) {
    if (Double.isFinite(this.fitness))
      throw new UnsupportedOperationException("addInputNode() must be called before getFitness()");

    if (this.inputNodes.contains(node))
      throw new IllegalArgumentException();

    this.inputNodes.add(node);
  }

  public void addOutputNode(final int node) {
    if (Double.isFinite(this.fitness))
      throw new UnsupportedOperationException("addOutputNode() must be called before getFitness()");

    if (this.outputNodes.contains(node))
      throw new IllegalArgumentException();

    this.outputNodes.add(node);
  }

  public List<Integer> getInputNodes() {
    return this.inputNodes;
  }

  public List<Integer> getOutputNodes() {
    return this.outputNodes;
  }

  public List<Integer> getHiddenNodes() {
    final List<Integer> its = new ArrayList<>();
    for (final int node : this.getAllNodes())
      if (!this.isInputNode(node) && !this.isOutputNode(node))
        its.add(node);
    return its;
  }

  public boolean isInputNode(final int node) {
    return this.inputNodes.contains(node);
  }

  public boolean isOutputNode(final int node) {
    return this.outputNodes.contains(node);
  }

  public EvolutionCore getCore() {
    return this.core;
  }

  public void addGene(final Gene gene, final Genome parent1, final Genome parent2) {
    if (Double.isFinite(this.fitness))
      throw new UnsupportedOperationException("addGene() must be called before getFitness()");

    if (this.genes.containsKey(gene.getInnovationNumber()))
      throw new UnsupportedOperationException("Genome already has gene with innovation number " + gene.getInnovationNumber());

    final Gene clone = gene.clone(); // make sure we're working with a cloned instance
    if (parent1 != null && parent2 != null)
      if (parent1.hasGene(clone.getInnovationNumber()) && parent2.hasGene(clone.getInnovationNumber())) {
        /**
         * There is a chance that a gene which is disabled in one of the parents is disabled.
         */
        final boolean dis1 = !parent1.getGene(clone.getInnovationNumber()).isEnabled();
        final boolean dis2 = !parent2.getGene(clone.getInnovationNumber()).isEnabled();

        // only one of them is disabled
        if ((dis1 && !dis2) || (!dis1 && dis2)) {
          final boolean disabled = Random.success(this.core.getSetting(Setting.GENE_DISABLE_CHANCE));
          clone.setEnabled(!disabled);
        }
      }

    this.genes.put(clone.getInnovationNumber(), clone); // clone it so we are sure we have a new instance
  }

  public Collection<Gene> getGenes() {
    return this.genes.values();
  }

  public InnovationNumber getHighestInnovationNumber() {
    if (this.genes.isEmpty())
      throw new UnsupportedOperationException("Genes may not be empty");
    final Iterator<Gene> it = this.genes.values().iterator();
    Gene last = null;
    while (it.hasNext())
      last = it.next();
    // can't be null otherwise UnsupportedOperationException has already been thrown
    if (last == null)
      throw new AssertionError();

    return last.getInnovationNumber();
  }

  private boolean hasGene(final InnovationNumber innovationNumber) {
    return this.genes.containsKey(innovationNumber);
  }

  private Gene getGene(final InnovationNumber innovationNumber) {
    return this.genes.get(innovationNumber);
  }

  /**
   * ArrayList
   */
  public List<Connection> getAllConnections() {
    final List<Connection> conns = new ArrayList<>();
    for (final Gene gene : this.getGenes())
      conns.add(new Connection(gene.getFrom(), gene.getTo()));
    return conns;
  }

  /**
   * HashSet
   */
  public Collection<? extends Connection> getActiveConnections() {
    final Set<Connection> conns = new HashSet<>();
    for (final Gene gene : this.getGenes())
      if (gene.isEnabled())
        conns.add(new Connection(gene.getFrom(), gene.getTo()));
    return conns;
  }

  /**
   * Cloned object has cloned maps and lists, the genes are also cloned.
   * But the contents of the other maps and lists are not cloned.
   */
  @Override
  public Genome clone() {
    final Genome newGenome = new Genome(this.core, this.species, this.getInputs(), this.getOutputs());

    // clone the values of the genes map
    newGenome.genes = new TreeMap<>();
    for (final Map.Entry<InnovationNumber, Gene> s : this.genes.entrySet())
      newGenome.genes.put(s.getKey(), s.getValue().clone());

    newGenome.inputNodes = new ArrayList<>(this.inputNodes);
    newGenome.outputNodes = new ArrayList<>(this.outputNodes);
    return newGenome;
  }

  /**
   * If a genome has exactly the same genes as an already existing genome but has different
   * innovation numbers, we replace it.
   */
  public void fixDuplicates() {
    if (Double.isFinite(this.fitness))
      throw new UnsupportedOperationException("fixDuplicates() must be called before getFitness()");

    for (final Species sp : this.core.getPopulationManager().getPopulation().getSpecies())
      for (final Genome genome : sp.getMembers()) {
        final List<Connection> conA = this.getAllConnections();
        final List<Connection> conB = genome.getAllConnections();

        if (ArrayUtils.equals(conB, conA)) {
          final Iterator<Gene> toCloneFrom = new ArrayList<>(genome.genes.values()).iterator();
          final Iterator<Gene> toReplace = new ArrayList<>(this.genes.values()).iterator();

          while (toCloneFrom.hasNext() && toReplace.hasNext()) {
            final Gene from = toCloneFrom.next();
            final Gene to = toReplace.next();

            final InnovationNumber oldInno = to.getInnovationNumber();
            final InnovationNumber changeTo = from.getInnovationNumber();

            final Gene old = this.genes.remove(oldInno);
            old.setInnovationNumber(changeTo);
            this.genes.put(old.getInnovationNumber(), old);
          }
          if (toCloneFrom.hasNext() || toReplace.hasNext())
            throw new AssertionError();
          return;
        }
      }
  }

  /**
   * Make sure calculateFitness() has been called already.
   */
  public static void crossAndAdd(final Genome a, final Genome b) {
    if (!a.species.equals(b.species))
      throw new UnsupportedOperationException("Species must match when crossing");

    final double aFitness = a.getFitness();
    final double bFitness = b.getFitness();
    final Genome strongest;
    final Genome weakest;
    if (aFitness > bFitness) {
      strongest = a;
      weakest = b;
    } else {
      strongest = b;
      weakest = a;
    }
    final Genome child = Genome.crossDominant(strongest, weakest);
    a.core.getPopulationManager().getPopulation().addGenome(child);
  }

  /**
   * Also calls the mutations.
   */
  private static Genome crossDominant(final Genome dominant, final Genome other) {
    if (!dominant.species.equals(other.species))
      throw new UnsupportedOperationException("Species must match when crossing");

    if (dominant.getGenes().isEmpty() || other.getGenes().isEmpty())
      throw new UnsupportedOperationException("Genes may not be empty");

    final Genome newGenome = new Genome(dominant.core, null, dominant.getInputs(), dominant.getOutputs()); // inputs/outputs should match so it doesn't matter where we get it from
    // the following should also be random if both parents have the gene
    for (int i = 0; i <= dominant.getHighestInnovationNumber().getNumber(); i++) {
      final InnovationNumber innovationNumber = InnovationNumber.get(i);
      if (dominant.hasGene(innovationNumber))
        if (other.hasGene(innovationNumber))
          newGenome.addGene(Random.random(new Gene[]{dominant.getGene(innovationNumber), other.getGene(innovationNumber)}), dominant, other);
        else
          newGenome.addGene(dominant.getGene(innovationNumber), dominant, other);
    }

    // make sure there are no duplicates
    newGenome.fixDuplicates();

    // do mutations
    newGenome.mutate();

    return newGenome;
  }

  public void mutate() {
    final Mutation mutation = new Mutation(this);
    mutation.mutate();
  }

  /**
   * Returns the distance between two existing genomes using the following formula.
   * d = (c1 * E) / N + (c2 * D) / N + c3 * W
   */
  public static double distance(final Genome a, final Genome b) {
    // find the longest
    final int aLength = a.getHighestInnovationNumber().getNumber();
    final int bLength = b.getHighestInnovationNumber().getNumber();

    final Genome longest;
    final Genome shortest;

    if (aLength > bLength) {
      longest = a;
      shortest = b;
    } else {
      longest = b;
      shortest = a;
    }

    final int shortestLength = shortest.getHighestInnovationNumber().getNumber();
    final int longestLength = longest.getHighestInnovationNumber().getNumber();

    double disjoint = 0; // use double so it won't be used as an int in the formula
    double excess = 0; // use double so it won't be used as an int in the formula

    final List<Double> weights = new ArrayList<>();
    for (int i = 0; i <= longestLength; i++) {
      final InnovationNumber innovationNumber = InnovationNumber.get(i);
      final Gene aa = longest.getGene(innovationNumber);
      final Gene bb = shortest.getGene(innovationNumber);

      // only present in one of them
      if ((aa == null && bb != null) || (aa != null && bb == null))
        if (i <= shortestLength)
          disjoint++;
        else if (i > shortestLength)
          excess++;
      if (aa != null && bb != null) {
        // matching gene
        final double distance = Math.abs(aa.getWeight() - bb.getWeight());
        weights.add(distance);
      }
    }

    double total = 0;
    double size = 0;

    for (final double w : weights) {
      total += w;
      size++;
    }

    final double averageWeightDistance = total / size;
    final double n = longest.getGenes().size();
    final double c1 = a.core.getSetting(Setting.DISTANCE_EXCESS_WEIGHT);
    final double c2 = a.core.getSetting(Setting.DISTANCE_DISJOINT_WEIGHT);
    final double c3 = a.core.getSetting(Setting.DISTANCE_WEIGHTS_WEIGHT);

    // formula: d = (c1 * E) / N + (c2 * D) / N + c3 * W
    final double d = ((c1 * excess) / n) + ((c2 * disjoint) / n) + (c3 * averageWeightDistance);
    return d;
  }

  @Override
  public double[] calculate(final double[] input) {
    return new BackTraceTask(this, this.core.getActivationFunction(), input).calculateOutput();
  }

  private double calculateFitness() {
    this.fitness = this.core.getFitnessCalculator().getFitness(this);
    if (this.fitness > this.species.getHighestFitness())
      this.species.setHighestFitness(this.fitness);
    return this.fitness;
  }

  /**
   * Returns the same value as the most recent call of calculateFitness()
   * or -1 if calculateFitness() hasn't been called yet.
   */
  public double getFitness() {
    if (Double.isNaN(this.fitness))
      return this.calculateFitness();
    return this.fitness;
  }

  /**
   * Class sorts by descending order, so best comes first.
   * Make sure calculateFitness() has been called already.
   */
  public static class GenomeSorter implements Comparator<Genome>, Serializable {

    @Override
    public int compare(final Genome o1, final Genome o2) {
      final double a1 = o1.getFitness();
      final double a2 = o2.getFitness();
//      return Double.compare(a2, a1);
      if (a1 > a2)
        return -1;
      if (a1 < a2)
        return 1;
      return 0;
    }
  }

  @Override
  public String toString() {
    final StringBuilder genes = new StringBuilder();
    for (final Map.Entry<InnovationNumber, Gene> gen : this.genes.entrySet()) {
      final Gene gene = gen.getValue();
      genes.append("[ " + gen.getKey() + "=" + gene.getInnovationNumber() + " , " + gene.getFrom() + " , " + gene.getTo() + " , " + gene.getWeight() + " " + gene.isEnabled() + " ] ");
    }
    return genes.toString();
  }
}
