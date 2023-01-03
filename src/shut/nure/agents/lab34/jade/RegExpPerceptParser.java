package shut.nure.agents.lab34.jade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * regular expressions based implementation of {@link PerceptParser}.
 */
public class RegExpPerceptParser implements PerceptParser {

    private static final String SENTENCE_SEPARATOR_PATTERN = "\\.";

    private static final Map<EnvPerceptType, List<Pattern>> DEFAULT_DICT = new HashMap<EnvPerceptType, List<Pattern>>() {{
        put(EnvPerceptType.BREEZE, Arrays.asList(
                Pattern.compile("breeze")
        ));
        put(EnvPerceptType.BUMP, Arrays.asList(
                Pattern.compile("bumped|hit")
        ));
        put(EnvPerceptType.GLITTER, Arrays.asList(
                Pattern.compile("gold|glitter(ing)?")
        ));
        put(EnvPerceptType.SCREAM, Arrays.asList(
                Pattern.compile("scream(ing)?")
        ));
        put(EnvPerceptType.STENCH, Arrays.asList(
                Pattern.compile("stench|stink(s)?")
        ));
    }};

    private final Map<EnvPerceptType, List<Pattern>> dict;

    public RegExpPerceptParser() {
        this(DEFAULT_DICT);
    }

    public RegExpPerceptParser(Map<EnvPerceptType, List<Pattern>> dict) {
        this.dict = dict;
    }

    @Override
    public Set<EnvPerceptType> parse(String percepts) {
        Set<EnvPerceptType> result = new HashSet<>();

        if (percepts != null && !percepts.isEmpty()) {
            final List<String> sentences = Stream.of(percepts.split(SENTENCE_SEPARATOR_PATTERN))
                    .map(String::trim)
                    .filter(((Predicate<String>) String::isEmpty).negate())
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());

            result = sentences.stream()
                    .map(this::parseSentence)
                    .collect(Collectors.toSet());
        }
        return result.isEmpty() ? EnumSet.noneOf(EnvPerceptType.class) : EnumSet.copyOf(result);
    }

    private EnvPerceptType parseSentence(String sentence) {
        // in assumption that single sentence can describe only single percept
        List<EnvPerceptType> result = new ArrayList<>();

        if (sentence != null && !sentence.isEmpty()) {
            final List<String> words = WumpusUtils.splitToWords(sentence);
            final List<String> allChunks = Stream.concat(words.stream(), Stream.of(sentence))
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());
            percept_loop:
            for (Map.Entry<EnvPerceptType, List<Pattern>> typePatterns : dict.entrySet()) {
                EnvPerceptType perceptType = typePatterns.getKey();
                List<Pattern> patterns = typePatterns.getValue();
                for (Pattern pattern : patterns) {
                    for (String chunk : allChunks) {
                        if (pattern.matcher(chunk).matches()) {
                            result.add(perceptType);
                            continue percept_loop;
                        }
                    }
                }
            }
        }
        return result.isEmpty() ? null
                : result.get(ThreadLocalRandom.current().nextInt(0, result.size()));
    }
    
}
