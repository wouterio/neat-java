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
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class CarLocation {

  private double x;
  private double y;
  private float angle;
  private final List<Antenna> antennas = new ArrayList<>();

  private static final double startX = 400;
  private static final double startY = 460;

  public CarLocation() {
    this(CarLocation.startX, CarLocation.startY, 0);
  }

  public CarLocation(final double x, final double y, final float angle) {
    this.x = x;
    this.y = y;
    this.angle = angle;

    this.antennas.add(new Antenna(this, 0));
    for (int i = 1; i < 9; i++) {
      this.antennas.add(new Antenna(this, i * 9));
      this.antennas.add(new Antenna(this, -i * 9));
    }
  }

  public List<Antenna> getAntennas() {
    return this.antennas;
  }

  public double getX() {
    return this.x;
  }

  public void setX(final double x) {
    this.x = x;
  }

  public double getY() {
    return this.y;
  }

  public void setY(final double y) {
    this.y = y;
  }

  public float getAngle() {
    return this.angle;
  }

  public void setAngle(final float angle) {
    this.angle = angle;
  }

  private static final double MAX_CAR_SPEED = 10;
  private double currentSpeed = 4;

  public void tick(final boolean rightClicked, final boolean leftClicked, final double gasPercentage /*0 = no has, 0.5 = same speed , 1 = full gas*/) {

    if (gasPercentage < 0 || gasPercentage > 1)
      throw new IllegalArgumentException();

    if (leftClicked) {
      this.angle -= 3.5;
    }
    if (rightClicked) {
      this.angle += 3.5;
    }

    final double gasChange = (gasPercentage - 0.5) * 0.3D;
    this.currentSpeed += gasChange;

    if (this.currentSpeed < 4) {
      this.currentSpeed = 4;
    }

    if (this.currentSpeed > CarLocation.MAX_CAR_SPEED)
      this.currentSpeed = CarLocation.MAX_CAR_SPEED;

    // update the x and y using angle and speed
    final double dx = this.currentSpeed * Math.cos(Math.toRadians(this.angle));
    final double dy = this.currentSpeed * Math.sin(Math.toRadians(this.angle));

    this.x += dx;
    this.y += dy;
  }

  public boolean isAlive(final BufferedImage background) {
    return background.getRGB((int) this.x, (int) this.y) == Antenna.ROAD_COLOR.getRGB() || background.getRGB((int) this.x, (int) this.y) == Color.RED.getRGB();
  }

  public boolean isOnFinish(final BufferedImage background) {
    return background.getRGB((int) this.x, (int) this.y) == Color.RED.getRGB();
  }

  public double getCurrentSpeed() {
    return this.currentSpeed / CarLocation.MAX_CAR_SPEED; // scale
  }
}
