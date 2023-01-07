package nl.sandergielisse.mythan.internal;

import java.util.ArrayList;
import java.util.List;

public class InnovationNumber implements Comparable<InnovationNumber> {

  private static final List<InnovationNumber> innovationNumbers = new ArrayList<>();


  public static InnovationNumber next() {
    final int number = innovationNumbers.size();
    final InnovationNumber innovationNumber = new InnovationNumber(number);
    innovationNumbers.add(innovationNumber);
    return innovationNumber;
  }

  public static InnovationNumber get(final int index) {
    while (innovationNumbers.size() <= index)
      next();
    return innovationNumbers.get(index);
  }


  //


  private final int number;


  private InnovationNumber(final int number) {
    this.number = number;
  }


  public int getNumber() {
    return this.number;
  }


  @Override
  public int hashCode() {
    return Integer.hashCode(this.number);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o)
      return true;
    if (o == null || this.getClass() != o.getClass())
      return false;
    final InnovationNumber that = (InnovationNumber) o;
    return this.number == that.number;
  }

  @Override
  public int compareTo(final InnovationNumber that) {
    return Integer.compare(this.number, that.number);
  }
}
