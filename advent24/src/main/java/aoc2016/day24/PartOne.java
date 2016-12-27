package aoc2016.day24;

import aoc2016.day24.Blueprint.FinalPosition;
import aoc2016.day24.Duct.Passage;
import com.google.common.io.CharSource;
import com.google.common.io.Resources;
import com.google.common.math.IntMath;
import org.jgrapht.GraphPath;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.BinaryOperator;

public class PartOne {

    public static void main(String[] args) throws IOException {
        CharSource input = Resources.asCharSource(PartOne.class.getResource("/input.txt"), StandardCharsets.US_ASCII);
        Blueprint blueprint = Blueprint.parse(input);
        List<GraphPath<Duct, Passage>> paths = blueprint.findShortestPathToNumberedDucts(FinalPosition.WHEREVER);
        int totalLength = paths.stream().map(GraphPath::getLength).reduce(0, IntMath::checkedAdd);
        System.out.format("length: %d%n", totalLength);
    }
}
