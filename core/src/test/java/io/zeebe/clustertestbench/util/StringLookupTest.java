package io.zeebe.clustertestbench.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class StringLookupTest {

  private static final Item CANDIDATE_A = new Item("name");
  private static final Item CANDIDATE_B = new Item("anotherName");
  private static final Item CANDIDATE_C = new Item("something else");
  private static final Item CANDIDATE_D = new Item("lorem ipsum");

  private final List<Item> testCandidates =
      List.of(CANDIDATE_A, CANDIDATE_B, CANDIDATE_C, CANDIDATE_D);

  private static final class Item {
    private final String name;

    Item(final String name) {
      this.name = name;
    }

    String getName() {
      return name;
    }
  }

  @Nested
  class CaseSensitive {

    @Test
    void shouldFindItem() {
      // given
      final var sutLookup =
          new StringLookup<>("item", "name", testCandidates, Item::getName, false);
      final var expected = CANDIDATE_A;

      // when
      final var actual = sutLookup.lookup();

      assertThat(actual.isRight()).isTrue();
      assertThat(actual.get()).isSameAs(expected);
    }

    @Test
    void shouldNotFindItemWrongCase() {
      // given
      final var sutLookup =
          new StringLookup<>("item", "Name", testCandidates, Item::getName, false);

      // when
      final var actual = sutLookup.lookup();

      assertThat(actual.isLeft()).isTrue();
    }

    @Test
    void shouldNotFindItemNoMatch() {
      // given
      final var sutLookup =
          new StringLookup<>("item", "non existing name", testCandidates, Item::getName, false);

      // when
      final var actual = sutLookup.lookup();

      assertThat(actual.isLeft()).isTrue();
    }

    @Test
    void shouldProduceErrorMessageWhenItemNotFound() {
      // given
      final var sutLookup =
          new StringLookup<>("item", "Another Name", testCandidates, Item::getName, false);
      final var expected =
          "Unable to find item 'Another Name'; closest candidates: ['anotherName', 'name', 'something else']; available candidates: ['name', 'anotherName', 'something else', 'lorem ipsum']";

      // when
      final var actual = sutLookup.lookup();

      assertThat(actual.isLeft()).isTrue();
      assertThat(actual.getLeft()).isEqualTo(expected);
    }
  }

  @Nested
  class CaseInsensitive {

    @Test
    void shouldFindItem() {
      // given
      final var sutLookup = new StringLookup<>("item", "name", testCandidates, Item::getName, true);
      final var expected = CANDIDATE_A;

      // when
      final var actual = sutLookup.lookup();

      assertThat(actual.isRight()).isTrue();
      assertThat(actual.get()).isSameAs(expected);
    }

    @Test
    void shouldFindItemWithDifferentCase() {
      // given
      final var sutLookup = new StringLookup<>("item", "Name", testCandidates, Item::getName, true);
      final var expected = CANDIDATE_A;

      // when
      final var actual = sutLookup.lookup();

      assertThat(actual.isRight()).isTrue();
      assertThat(actual.get()).isSameAs(expected);
    }

    @Test
    void shouldNotFindItemNoMatch() {
      // given
      final var sutLookup =
          new StringLookup<>("item", "non existing name", testCandidates, Item::getName, true);

      // when
      final var actual = sutLookup.lookup();

      assertThat(actual.isLeft()).isTrue();
    }

    @Test
    void shouldProduceErrorMessageWhenItemNotFound() {
      // given
      final var sutLookup =
          new StringLookup<>("item", "Another Name", testCandidates, Item::getName, true);
      final var expected =
          "Unable to find item 'Another Name'; closest candidates: ['anotherName', 'name', 'something else']; available candidates: ['name', 'anotherName', 'something else', 'lorem ipsum']";

      // when
      final var actual = sutLookup.lookup();

      assertThat(actual.isLeft()).isTrue();
      assertThat(actual.getLeft()).isEqualTo(expected);
    }
  }
}
