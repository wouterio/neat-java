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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Antenna {

  private final CarLocation carLocation;
  private final float angle;

  public Antenna(final CarLocation carLocation, final float angle) {
    this.carLocation = carLocation;
    this.angle = angle;
  }

  public float getAngle() {
    return this.angle;
  }

  public CarLocation getCarLocation() {
    return this.carLocation;
  }

  public double getPreviewLength() {
    return 200;
  }

  public double getEndX(final BufferedImage background) {
    final double startX = this.carLocation.getX();
    final double totalAngle = this.carLocation.getAngle() + this.angle;
    final double dx = this.getFreeDistance(background) * Math.cos(Math.toRadians(totalAngle));
    return startX + dx;
  }

  public double getEndY(final BufferedImage background) {
    final double startY = this.carLocation.getY();
    final double totalAngle = this.carLocation.getAngle() + this.angle;
    final double dy = this.getFreeDistance(background) * Math.sin(Math.toRadians(totalAngle));
    return startY + dy;
  }

  private static final double STEP_SIZE = 3.0;
  public static final Color ROAD_COLOR = new Color(255, 174, 0);

  public double getFreeDistance(final BufferedImage background) {
    final double totalAngle = this.carLocation.getAngle() + this.angle;
    double startX = this.carLocation.getX();
    double startY = this.carLocation.getY();

    final double dx = Antenna.STEP_SIZE * Math.cos(Math.toRadians(totalAngle));
    final double dy = Antenna.STEP_SIZE * Math.sin(Math.toRadians(totalAngle));

    while (true) {
      startX += dx;
      startY += dy;

      if (background.getRGB((int) startX, (int) startY) != ROAD_COLOR.getRGB() && background.getRGB((int) startX, (int) startY) != Color.RED.getRGB()) {
        // we're off the road

        final double distX = this.carLocation.getX() - startX;
        final double distY = this.carLocation.getY() - startY;

        return Math.sqrt(distX * distX + distY * distY);
      }
    }
  }

  public void draw(final BufferedImage background, final Graphics2D g2d) {
    g2d.setColor(Color.BLUE);

    g2d.drawLine((int) this.carLocation.getX(), (int) this.carLocation.getY(), (int) this.getEndX(background), (int) this.getEndY(background));
  }
}
