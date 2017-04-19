package org.vitrivr.cineast.core.data.score;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.vitrivr.cineast.core.data.CorrespondenceFunction;
import org.vitrivr.cineast.core.util.GroupingUtils;
import org.vitrivr.cineast.core.util.ReflectionHelper;

public interface DistanceElement {
  String getId();

  double getDistance();

  ScoreElement toScore(CorrespondenceFunction f);

  static <T extends DistanceElement> List<ScoreElement> toScore(List<T> distances,
      CorrespondenceFunction f) {
    return distances.stream().map(e -> e.toScore(f)).collect(Collectors.toList());
  }

  static <T extends DistanceElement> T create(Class<T> clazz, String id, double distance) {
    // TODO: use ReflectionHelper.instanciate instead?
    if (clazz == SegmentDistanceElement.class) {
      return clazz.cast(new SegmentDistanceElement(id, distance));
    } else if (clazz == ObjectDistanceElement.class) {
      return clazz.cast(new ObjectDistanceElement(id, distance));
    } else {
      String className = clazz.getSimpleName();
      Logger logger = LogManager.getLogger();
      logger.error("Unknown subclass {} of DistanceElement in ADAMproSelector.getNearestNeighbours",
          className);
      // TODO: Throw IllegalArgumentException instead of returning null?
      return null; // Using null because this is a programming error
    }
  }

  Comparator<DistanceElement> INVERSE_DISTANCE_COMPARATOR =
      Comparator.<DistanceElement>comparingDouble(e -> e.getDistance()).reversed();

  static List<DistanceElement> filterMinimumDistances(Stream<DistanceElement> elements) {
    return GroupingUtils.filterMaxByGroup(elements, e -> e.getId(), INVERSE_DISTANCE_COMPARATOR);
  }
}
