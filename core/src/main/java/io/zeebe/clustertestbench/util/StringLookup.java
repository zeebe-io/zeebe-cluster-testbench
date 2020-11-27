package io.zeebe.clustertestbench.util;

import static java.util.Comparator.comparing;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Either;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.apache.commons.text.similarity.LevenshteinDistance;

/**
 * Class that looks for a given string in a list of candidates and produces a helpful error message
 * if no match was found
 *
 * @param <T> the type of the candidates to search in
 */
public class StringLookup<T> {

  private static final LevenshteinDistance DISTANCE_FUNCTION =
      LevenshteinDistance.getDefaultInstance();
  private static final Collector<CharSequence, ?, String> STRING_JOINING_COLLECTOR =
      Collectors.joining("', '", "['", "']");
  final Predicate<T> predicate;
  private final String entityToFind;
  private final String searchTerm;
  private final List<T> candidates;
  private final Function<T, String> extractor;

  /**
   * @param entityToFind what kind of entity shall be found (e.g. channel, cluster plan, region);
   *     this is only used in the error message
   * @param searchTerm the string to look for
   * @param candidates the candidates to be searched
   * @param extractor function that extracts the relevant string from a candidate
   * @param ignoreCase flag whether the search should respect or ignore case
   */
  public StringLookup(
      final String entityToFind,
      final String searchTerm,
      final List<T> candidates,
      final Function<T, String> extractor,
      final boolean ignoreCase) {
    this.entityToFind = entityToFind;
    this.searchTerm = searchTerm;
    this.candidates = candidates;
    this.extractor = extractor;

    if (ignoreCase) {
      this.predicate = item -> searchTerm.equalsIgnoreCase(extractor.apply(item));
    } else {
      this.predicate = item -> searchTerm.equals(extractor.apply(item));
    }
  }

  public Either<String, T> lookup() {

    final Optional<T> optMatch = candidates.stream().filter(predicate).findFirst();

    if (optMatch.isPresent()) {
      return Either.right(optMatch.get());
    } else {
      return Either.left(
          "Unable to find "
              + entityToFind
              + " '"
              + searchTerm
              + "'; closest candidates: "
              + getBestMatches()
              + "; available candidates: "
              + getAvailableOptions());
    }
  }

  private String getAvailableOptions() {
    return candidates.stream().map(extractor).collect(STRING_JOINING_COLLECTOR);
  }

  /**
   * Returns the three best matches according to {@code DISTANCE_FUNCTION}
   *
   * @return three best matches according to {@code DISTANCE_FUNCTION}
   */
  private String getBestMatches() {
    return candidates.stream()
        .map(extractor)
        .map(candidate -> Tuple.of(candidate, DISTANCE_FUNCTION.apply(searchTerm, candidate)))
        .sorted(comparing(Tuple2::_2))
        .limit(3)
        .map(Tuple2::_1)
        .collect(STRING_JOINING_COLLECTOR);
  }
}
