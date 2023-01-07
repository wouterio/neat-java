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
import java.util.Map;
import java.util.Set;

import nl.sandergielisse.mythan.ActivationFunction;
import nl.sandergielisse.mythan.FitnessCalculator;
import nl.sandergielisse.mythan.Mythan;
import nl.sandergielisse.mythan.Setting;
import nl.sandergielisse.mythan.internal.genes.Gene;
import nl.sandergielisse.mythan.internal.genes.Genome;

public class EvolutionCore implements Mythan {

  /**
   * Amount of input nodes.
   */
  private final int inputSize;

  /**
   * Amount of output nodes.
   */
  private final int outputSize;

  /**
   * Map of settings.
   */
  private final Map<Setting, Double> settings = new HashMap<>() {
    private static final long serialVersionUID = 2033443707932774057L;

    {
      for (final Setting setting : Setting.values()) {
        this.put(setting, setting.getDefaultValue());
      }
    }
  };

  private final FitnessCalculator fitnessCalculator;
  private final ActivationFunction activationFunction;
  private final PopulationManager populationManager = new PopulationManager(this);

  public EvolutionCore(final int in, final int out, final ActivationFunction activationFunction, final FitnessCalculator calc) {
    this.inputSize = in;
    this.outputSize = out;
    this.activationFunction = activationFunction;
    this.fitnessCalculator = calc;
  }

  @Override
  public int getInputSize() {
    return this.inputSize;
  }

  @Override
  public int getOutputSize() {
    return this.outputSize;
  }

  @Override
  public double getSetting(final Setting setting) {
    return this.settings.get(setting);
  }

  @Override
  public void setSetting(final Setting setting, final double value) {
    this.settings.put(setting, value);
  }

  @Override
  public ActivationFunction getActivationFunction() {
    return this.activationFunction;
  }

  @Override
  public FitnessCalculator getFitnessCalculator() {
    return this.fitnessCalculator;
  }

  public PopulationManager getPopulationManager() {
    return this.populationManager;
  }

  @Override
  public void trainToFitness(final int populationSize, final double targetFitness) {
    this.populationManager.initialize(populationSize);
    while (true) {
      this.populationManager.newGeneration();
      final Genome best = this.populationManager.getLatestFitness();
      this.fitnessCalculator.generationFinished(best);

      if (best.getFitness() >= targetFitness) {

        final Set<Integer> hiddenNodes = new HashSet<>();
        int enabledConns = 0;

        for (final Gene g : best.getGenes()) {
          if (g.isEnabled()) {
            enabledConns++;
          }

          {
            final int node = g.getFrom();
            if (!best.isInputNode(node) && !best.isOutputNode(node)) {
              hiddenNodes.add(node);
            }
          }
          {
            final int node = g.getTo();
            if (!best.isInputNode(node) && !best.isOutputNode(node)) {
              hiddenNodes.add(node);
            }
          }
        }

        System.out.println("======================================= Mythan =======================================");
        System.out.println("Solution was found with a fitness of " + best.getFitness() + " in generation " + this.populationManager.getGeneration());
        System.out.println("The system had " + hiddenNodes.size() + " hidden units and " + enabledConns + " enabled connections");
        for (final Gene gene : best.getGenes()) {
          System.out.println("	~ " + gene.toString());
        }
        System.out.println("======================================================================================");
        return;
      }
    }
  }
}
