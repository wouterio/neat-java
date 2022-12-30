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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.swing.JFrame;

public class Car extends JFrame implements Runnable {

  public static void main(final String[] args) {
    new Car().run();
  }

  private static final long serialVersionUID = 1L;
  private BufferedImage background;

  {
    try {
      this.background = ImageIO.read(this.getClass().getResource("route.png"));
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  public BufferedImage getBackgroundImage() {
    return this.background;
  }

  @Override
  public void run() {
    System.out.println("Enter the preview interval please...");

    final Scanner input = new Scanner(System.in, StandardCharsets.UTF_8);
    while (input.hasNextLine()) {
      final String nextLine = input.nextLine();

      input.close();
      int interval = -1;

      try {
        interval = Integer.parseInt(nextLine);
      } catch (final NumberFormatException e) {
        System.out.println(nextLine + " is not a number.");
        System.exit(1);
      }

      if (interval == -1)
        throw new AssertionError();

      new MythanTraining(this, this.background, interval).start();
    }
    input.close();
  }
}
