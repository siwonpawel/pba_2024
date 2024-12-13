package pl.edu.zut.pba;

import org.assertj.core.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.*;

class RangeTest
{

    private Range range;

    @BeforeEach
    void init()
    {
        range = new Range(4, 6);
    }

    @ParameterizedTest
    @CsvSource({
            "5,4",
            "10,-10",
            "-10,-11"
    })
    void shouldThrowExceptionWhenMinIsLessThanMax(int min, int max)
    {
        // given

        // when
        Throwable throwable = Assertions.catchThrowable(() -> new Range(min, max));

        // then
        assertThat(throwable)
                .isInstanceOf(BoundriesException.class)
                .hasMessage("max [%d] have to be equal to or larger than min [%d]".formatted(max, min));
    }

    @ParameterizedTest
    @CsvSource({
            "3,false",
            "5,true",
            "4,true",
            "6,true",
            "7,false"
    })
    void shouldCorrectlyClassifyValue(int value, boolean expectedResult)
    {
        // given

        // when
        boolean result = range.isInRange(value);

        // then
        assertThat(result).isEqualTo(expectedResult);
    }

}