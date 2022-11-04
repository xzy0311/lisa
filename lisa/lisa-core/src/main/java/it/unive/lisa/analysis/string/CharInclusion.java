package it.unive.lisa.analysis.string;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

public class CharInclusion extends BaseNonRelationalValueDomain<CharInclusion> {

    private final Collection<Character> certainlyContained;

    private final Collection<Character> maybeContained;

    private static final CharInclusion TOP = new CharInclusion();
    private static final CharInclusion BOTTOM = new CharInclusion(null, null);

    public CharInclusion() {
        this(new HashSet<>(), getAlphabet());
    }

    public CharInclusion(Collection<Character> certainlyContained,
                         Collection<Character> maybeContained) {
        this.certainlyContained = certainlyContained;
        this.maybeContained = maybeContained;
    }

    @Override
    protected CharInclusion lubAux(CharInclusion other) throws SemanticException { //TODO
        return null;
    }

    @Override
    protected CharInclusion wideningAux(CharInclusion other) throws SemanticException {
        return lubAux(other);
    }

    @Override
    protected boolean lessOrEqualAux(CharInclusion other) throws SemanticException {
        if (this.getCertainlyContained().size() > other.getCertainlyContained().size() ||
                this.getMaybeContained().size() > other.getMaybeContained().size())
            return false;

        for (Character certainlyContainedCharacter : this.getCertainlyContained())
            if (!other.getCertainlyContained().contains(certainlyContainedCharacter))
                return false;

        for (Character maybeContainedCharacter : this.getMaybeContained())
            if (!other.getMaybeContained().contains(maybeContainedCharacter))
                return false;

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CharInclusion that = (CharInclusion) o;
        return Objects.equals(certainlyContained, that.certainlyContained) && Objects.equals(maybeContained, that.maybeContained);
    }

    @Override
    public int hashCode() {
        return Objects.hash(certainlyContained, maybeContained);
    }

    @Override
    public CharInclusion top() {
        return TOP;
    }

    @Override
    public CharInclusion bottom() {
        return BOTTOM;
    }

    @Override
    public boolean isTop() {
        return this.equals(TOP);
    }

    @Override
    public boolean isBottom() {
        return this.equals(BOTTOM);
    }

    @Override
    public DomainRepresentation representation() {
        if (isBottom())
            return Lattice.bottomRepresentation();
        if (isTop())
            return Lattice.topRepresentation();

        return new StringRepresentation(formatCharInclusionRepresentation());
    }

    public Collection<Character> getCertainlyContained() {
        return certainlyContained;
    }

    public Collection<Character> getMaybeContained() {
        return maybeContained;
    }

    private String formatCharInclusionRepresentation(){
        StringBuilder stringBuilder = new StringBuilder("CertainlyContained: {");
        int counter = 0;

        for(Character certainlyContainedCharacter: this.getCertainlyContained()){
            String formattedCharacter;

            formattedCharacter = counter != this.getCertainlyContained().size() - 1 ?
                    certainlyContainedCharacter + ", " : certainlyContainedCharacter + "}";
            counter++;

            stringBuilder.append(formattedCharacter);
        }

        counter = 0;
        stringBuilder.append(", MaybeContained: {");

        for(Character maybeContainedCharacter: this.getMaybeContained()){
            String formattedCharacter;

            formattedCharacter = counter != this.getCertainlyContained().size() - 1 ?
                    maybeContainedCharacter + ", " : maybeContainedCharacter + "}";
            counter++;

            stringBuilder.append(formattedCharacter);
        }

        return stringBuilder.toString();
    }

    private static HashSet<Character> getAlphabet() {
        HashSet<Character> alphabet = new HashSet<>();

        for (char character = 'a'; character <= 'z'; character++) {
            alphabet.add(character);
        }

        return alphabet;
    }
}
