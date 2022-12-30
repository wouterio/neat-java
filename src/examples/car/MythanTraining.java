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
package examples.car;

import java.awt.image.BufferedImage;
import javax.swing.WindowConstants;

import nl.sandergielisse.mythan.CustomizedSigmoidActivation;
import nl.sandergielisse.mythan.FitnessCalculator;
import nl.sandergielisse.mythan.Mythan;
import nl.sandergielisse.mythan.Network;
import nl.sandergielisse.mythan.Setting;

public class MythanTraining {

  private final BufferedImage background;
  private final Car car;
  private final Frame board;
  private final int interval;

  public MythanTraining(final Car car, final BufferedImage background, final int interval) {
    this.background = background;
    this.car = car;
    this.interval = interval;

    car.add(this.board = new Frame(this.car));
    car.pack();
    car.setLocationRelativeTo(null);
    car.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    car.setVisible(true);
    car.setTitle("Mythan Driving Car Example (AI Powered)");
    car.setResizable(false);
  }

  public void setTitle(final String title) {
    this.car.setTitle(title);
  }

  public void start() {
    /**
     * The inputs are the X antenna's. When the antenna hits something,
     * the input is 0, or 1 for an antenna not hitting something.
     *
     * There are 3 output types.
     *
     * 0.0 - 0.3 = steer left
     * 0.3 - 0.7 = don't steer
     * 0.7 - 1.0 = steer right
     */

    final Mythan mythan = Mythan.newInstance(new CarLocation().getAntennas().size() + 1, 2, new CustomizedSigmoidActivation(), new FitnessCalculator() {

      @Override
      public double getFitness(final Network network) {

        final CarLocation carLocation = new CarLocation();

        long ticksLived = 0;

        while (carLocation.isAlive(MythanTraining.this.background) && !carLocation.isOnFinish(MythanTraining.this.background)) {
          boolean rightClicked = false;
          boolean leftClicked = false;

          final double[] inputs = new double[carLocation.getAntennas().size() + 1];

          for (int i = 0; i < carLocation.getAntennas().size(); i++) {
            final Antenna ant = carLocation.getAntennas().get(i);
            double len = ant.getFreeDistance(MythanTraining.this.background);
            if (len > 200)
              len = 200;
            inputs[i] = len / 200.0D;
          }
          inputs[inputs.length - 1] = carLocation.getCurrentSpeed();

          final double[] ans = network.calculate(inputs);
          final double output = ans[0];
          final double speed = ans[1];

          if (output >= 0 && output <= 0.3)
            leftClicked = true;

          if (output >= 0.7 && output <= 1)
            rightClicked = true;

          carLocation.tick(rightClicked, leftClicked, speed);
          ticksLived++;
        }

        /**
         * First 50 fitness is for actually making it (0-100%), rest is for speed.
         */
        double fitness = 0;
        final double secondsLived = ticksLived / 30.0D; // 30 ticks per second

        if (secondsLived > 45)
          throw new RuntimeException();

        if (carLocation.isOnFinish(MythanTraining.this.background)) {
          // we finished
          fitness = (45 - secondsLived);
        }

        return fitness * fitness;
      }

      private int generation = 1;

      @Override
      public void generationFinished(final Network bestPerforming) {
        this.generation++;

        if (this.getFitness(bestPerforming) != bestPerforming.getFitness())
          throw new AssertionError();

        if (MythanTraining.this.interval != 0 && this.generation % MythanTraining.this.interval != 0) {
          return;
        }

        MythanTraining.this.setTitle("Mythan Driving Car Example (AI Powered) - Generation " + this.generation + " - Fitness " + bestPerforming.getFitness());

        MythanTraining.this.board.setLocation(new CarLocation());
        while (true) {
          try {
            Thread.sleep((long) (1000.0D / 30.0D)); // 30 FPS

            boolean rightClicked = false;
            boolean leftClicked = false;

            final double[] inputs = new double[MythanTraining.this.board.getCarLocation().getAntennas().size() + 1];
            for (int i = 0; i < MythanTraining.this.board.getCarLocation().getAntennas().size(); i++) {
              final Antenna ant = MythanTraining.this.board.getCarLocation().getAntennas().get(i);
              double len = ant.getFreeDistance(MythanTraining.this.background);
              if (len > 200)
                len = 200;
              inputs[i] = len / 200.0D;
            }
            inputs[inputs.length - 1] = MythanTraining.this.board.getCarLocation().getCurrentSpeed();

            final double[] ans = bestPerforming.calculate(inputs);
            final double output = ans[0];
            final double speed = ans[1];

            if (output >= 0 && output <= 0.3)
              leftClicked = true;

            if (output >= 0.7 && output <= 1)
              rightClicked = true;

            MythanTraining.this.board.getCarLocation().tick(rightClicked, leftClicked, speed);

            if (!MythanTraining.this.board.getCarLocation().isAlive(MythanTraining.this.board.getBackgroundImage())) {
              // restart
              MythanTraining.this.board.setLocation(new CarLocation());
              continue;
            }

            if (MythanTraining.this.board.getCarLocation().isOnFinish(MythanTraining.this.board.getBackgroundImage())) {
              break;
            }

            MythanTraining.this.car.repaint();
          } catch (final InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    });

    mythan.setSetting(Setting.GENE_DISABLE_CHANCE, 0.75);
    mythan.setSetting(Setting.MUTATION_WEIGHT_CHANCE, 0.7);
    mythan.setSetting(Setting.MUTATION_WEIGHT_RANDOM_CHANCE, 0.10);
    mythan.setSetting(Setting.MUTATION_WEIGHT_MAX_DISTURBANCE, 0.1);

    mythan.setSetting(Setting.MUTATION_NEW_CONNECTION_CHANCE, 0.03);
    mythan.setSetting(Setting.MUTATION_NEW_NODE_CHANCE, 0.05);

    mythan.setSetting(Setting.DISTANCE_EXCESS_WEIGHT, 1.0);
    mythan.setSetting(Setting.DISTANCE_DISJOINT_WEIGHT, 1.0);
    mythan.setSetting(Setting.DISTANCE_WEIGHTS_WEIGHT, 0.4);

    mythan.setSetting(Setting.SPECIES_COMPATIBILTY_DISTANCE, 0.8); // the bigger the less species
    mythan.setSetting(Setting.MUTATION_WEIGHT_CHANCE_RANDOM_RANGE, 3);

    mythan.setSetting(Setting.GENERATION_ELIMINATION_PERCENTAGE, 0.85);
    mythan.setSetting(Setting.BREED_CROSS_CHANCE, 0.75);

    mythan.trainToFitness(1000, Double.MAX_VALUE);
  }
}
