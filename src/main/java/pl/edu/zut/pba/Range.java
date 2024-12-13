package pl.edu.zut.pba;

public class Range
{

    private final int min;
    private final int max;

    public Range(int min, int max)
    {
        if (min > max)
        {
            throw new BoundriesException("max [%d] have to be equal to or larger than min [%d]".formatted(max, min));
        }

        this.min = min;
        this.max = max;
    }

    public boolean isInRange(int number)
    {
        return number >= min && number <= max;
    }
}