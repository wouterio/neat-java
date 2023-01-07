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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import nl.sandergielisse.mythan.Setting;
import nl.sandergielisse.mythan.internal.InnovationNumber;
import nl.sandergielisse.mythan.internal.Random;

/**
 * There are three types of mutations.
 * <p>
 * 1. Add a new node. The new input weight to that node will be 1.
 * The output from the new node will be set to the old connection's weight value.
 * <p>
 * 2. Add a new link with a random weight between two existing nodes.
 * <p>
 * 3. The weights of an existing connection are changed.
 */
public class Mutation {

  private final Genome genome;

  public Mutation(final Genome genome) {
    this.genome = genome;
  }

  public void mutate() {

    /**
     * 1. Add a new node. The new input weight to that node will be 1.
     * 	  The output from the new node will be set to the old connection's weight value.
     */
    if (Random.success(this.genome.getCore().getSetting(Setting.MUTATION_NEW_NODE_CHANCE))) {
      final Gene randomGene = Random.random(new ArrayList<>(this.genome.getGenes()));
      randomGene.setEnabled(false);

      // two new genes
      final int from = randomGene.getFrom();
      final int to = randomGene.getTo();

      // this.genome.getCore().getNextInnovationNumber();

      final int newNodeId = this.genome.getHighestNode() + 1;
      this.genome.addGene(new Gene(InnovationNumber.next(), from, newNodeId, 1.0D, true), null, null);
      this.genome.addGene(new Gene(InnovationNumber.next(), newNodeId, to, randomGene.getWeight(), true), null, null);
    }

    /**
     * 2. Add a new link with a random weight between two existing nodes.
     *    Start by finding two yet unconnected nodes. One of them must be a hidden node.
     */
    if (Random.success(this.genome.getCore().getSetting(Setting.MUTATION_NEW_CONNECTION_CHANCE))) {
      try {
        /**
         * Instead of looping through all possible connections and choosing one from
         * the obtained list, we pick a random connection and hope it doesn't exist
         * yet. We do this because once the network gets bigger, looping through all
         * possible connections would be a very intensive task.
         */
        final Collection<? extends Connection> currentConnections = this.genome.getAllConnections();

        int attempts = 0;

        Connection maybeNew = null;
        do {
          {
            if (attempts > 40)
              throw new MutationFailedException("New connection could not be created after 40 attempts.");
            attempts++;
          }

          final int from = Random.random(this.genome.getNodes(true, true, false));

          final List<Integer> leftOver = this.genome.getNodes(false, true, true);
          leftOver.remove((Object) from); // cast to Object, otherwise the wrong method remove(int index); will be called

          if (leftOver.isEmpty())
            continue;

          final int to = Random.random(leftOver);

          maybeNew = new Connection(from, to);
        } while (maybeNew == null || maybeNew.getFrom() == maybeNew.getTo() || currentConnections.contains(maybeNew) || this.isRecurrent(maybeNew));

        // add it to the network
        this.genome.addGene(new Gene(InnovationNumber.next(), maybeNew.getFrom(), maybeNew.getTo(), Random.random(-1, 1), true), null, null);
      } catch (final MutationFailedException e) {
        // System.out.println("Mutation Failed: " + e.getMessage());
      }
    }

    /**
     * 3. The weights of an existing connection are changed.
     */
    if (Random.success(this.genome.getCore().getSetting(Setting.MUTATION_WEIGHT_CHANCE))) {
      if (Random.success(this.genome.getCore().getSetting(Setting.MUTATION_WEIGHT_RANDOM_CHANCE))) {
        // assign a random new value
        for (final Gene gene : this.genome.getGenes()) {
          final double range = this.genome.getCore().getSetting(Setting.MUTATION_WEIGHT_CHANCE_RANDOM_RANGE);
          gene.setWeight(Random.random(-range, range));
        }
      } else {
        // uniformly perturb
        for (final Gene gene : this.genome.getGenes()) {
          final double disturbance = this.genome.getCore().getSetting(Setting.MUTATION_WEIGHT_MAX_DISTURBANCE);
          final double uniform = Random.random(-disturbance, disturbance);
          gene.setWeight(gene.getWeight() + uniform);
        }
      }
    }
  }

  public boolean isRecurrent(final Connection with) {
    final Genome copy = this.genome.clone(); // clone so we can change its genes without actually affecting the original genome

    if (with != null) {
      final InnovationNumber next = InnovationNumber.get(copy.getHighestInnovationNumber().getNumber() + 1);
      final Gene gene = new Gene(next, with.getFrom(), with.getTo(), 0, true);
      copy.addGene(gene, null, null);
    }

    boolean recc = false;
    for (final int hiddenNode : copy.getHiddenNodes()) {
      if (this.isRecurrent(new ArrayList<>(), copy, hiddenNode)) {
        recc = true;
      }
    }
    return recc;
  }

  private boolean isRecurrent(final List<Integer> path, final Genome genome, final int node) {
    if (path.contains(node)) {
      /**
       * We've been here before, we're in an infinite loop.
       */
      return true;
    }
    path.add(node);

    boolean recc = false;
    for (final int from : this.getInputs(genome, node)) {
      if (!genome.isInputNode(from)) {
        if (this.isRecurrent(path, genome, from)) {
          recc = true;
        }
      }
    }
    return recc;
  }

  private List<Integer> getInputs(final Genome genome, final int node) {
    final List<Integer> froms = new ArrayList<>();
    for (final Gene gene : genome.getGenes()) {
      if (gene.getTo() == node) {
        froms.add(gene.getFrom());
      }
    }
    return froms;
  }
}
