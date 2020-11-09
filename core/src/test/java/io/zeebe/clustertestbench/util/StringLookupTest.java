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

  private List<Item> testCandidates = List.of(CANDIDATE_A, CANDIDATE_B, CANDIDATE_C, CANDIDATE_D);

  @Nested
  class CaseSensitive {

    @Test
    void shouldFindItem() {
      // given
      var sutLookup = new StringLookup<>("item", "name", testCandidates, Item::getName, false);
      var expected = CANDIDATE_A;

      // when
      var actual = sutLookup.lookup();

      assertThat(actual.isRight()).isTrue();
      assertThat(actual.get()).isSameAs(expected);
    }

    @Test
    void shouldNotFindItemWrongCase() {
      // given
      var sutLookup = new StringLookup<>("item", "Name", testCandidates, Item::getName, false);

      // when
      var actual = sutLookup.lookup();

      assertThat(actual.isLeft()).isTrue();
    }

    @Test
    void shouldNotFindItemNoMatch() {
      // given
      var sutLookup =
          new StringLookup<>("item", "non existing name", testCandidates, Item::getName, false);

      // when
      var actual = sutLookup.lookup();

      assertThat(actual.isLeft()).isTrue();
    }

    @Test
    void shouldProduceErrorMessageWhenItemNotFound() {
      // given
      var sutLookup =
          new StringLookup<>("item", "Another Name", testCandidates, Item::getName, false);
      var expected =
          "Unable to find item 'Another Name'; closest candidates: ['anotherName', 'name', 'something else']; available candidates: ['name', 'anotherName', 'something else', 'lorem ipsum']";

      // when
      var actual = sutLookup.lookup();

      assertThat(actual.isLeft()).isTrue();
      assertThat(actual.getLeft()).isEqualTo(expected);
    }
  }

  @Nested
  class CaseInsensitive {

    @Test
    void shouldFindItem() {
      // given
      var sutLookup = new StringLookup<>("item", "name", testCandidates, Item::getName, true);
      var expected = CANDIDATE_A;

      // when
      var actual = sutLookup.lookup();

      assertThat(actual.isRight()).isTrue();
      assertThat(actual.get()).isSameAs(expected);
    }

    @Test
    void shouldFindItemWithDifferentCase() {
      // given
      var sutLookup = new StringLookup<>("item", "Name", testCandidates, Item::getName, true);
      var expected = CANDIDATE_A;

      // when
      var actual = sutLookup.lookup();

      assertThat(actual.isRight()).isTrue();
      assertThat(actual.get()).isSameAs(expected);
    }

    @Test
    void shouldNotFindItemNoMatch() {
      // given
      var sutLookup =
          new StringLookup<>("item", "non existing name", testCandidates, Item::getName, true);

      // when
      var actual = sutLookup.lookup();

      assertThat(actual.isLeft()).isTrue();
    }

    @Test
    void shouldProduceErrorMessageWhenItemNotFound() {
      // given
      var sutLookup =
          new StringLookup<>("item", "Another Name", testCandidates, Item::getName, true);
      var expected =
          "Unable to find item 'Another Name'; closest candidates: ['anotherName', 'name', 'something else']; available candidates: ['name', 'anotherName', 'something else', 'lorem ipsum']";

      // when
      var actual = sutLookup.lookup();

      assertThat(actual.isLeft()).isTrue();
      assertThat(actual.getLeft()).isEqualTo(expected);
    }
  }

  private static final class Item {
    private String name;

    Item(String name) {
      this.name = name;
    }

    String getName() {
      return name;
    }
  }
}
